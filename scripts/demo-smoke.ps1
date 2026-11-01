param([string]$Base='http://localhost:8080', [string]$Password='123456')
$ErrorActionPreference='Stop'
$session=New-Object Microsoft.PowerShell.Commands.WebRequestSession
function GetPage($path){ (Invoke-WebRequest ($Base+$path) -WebSession $session).Content }
function CsrfToken {
  $token=$session.Cookies.GetCookies([uri]$Base) | Where-Object { $_.Name -eq 'XSRF-TOKEN' } | Select-Object -Last 1
  if($null -eq $token){ throw 'CSRF token was not issued. Request a page first.' }
  return $token.Value
}
function PostPage($path,$body){
  $body['_csrf']=CsrfToken
  (Invoke-WebRequest ($Base+$path) -Method Post -Body $body -WebSession $session).Content
}
function Check($condition,$label){if(!$condition){throw "FAILED: $label"};Write-Output "PASS: $label"}
function Login($user){$null=GetPage '/login?lang=en';$null=PostPage '/login' @{username=$user;password=$Password}}

Login 'qc'
$tag='HTTP-'+[DateTimeOffset]::Now.ToUnixTimeSeconds()
$html=PostPage '/formulas' @{documentNo=$tag;name='Acceptance serum';materialIds='';newMaterialCodes=$tag;newMaterialNames='Acceptance water';newMaterialUnits='G';materialPhases='A';calculationBasis='PERCENT_W_W';productUnitWeightGrams='10';percentages='100'}
Check ($html.Contains($tag)) 'QC creates % w/w formula with a new material'
$id=[regex]::Match($html,'/formulas/([a-f0-9-]+)/submit').Groups[1].Value
Check ($id.Length -eq 36) 'Formula has submit action'
$html=PostPage "/formulas/$id/submit" @{}
Check ($html.Contains('Awaiting approval')) 'QC submits formula for CEO approval'

Login 'ceo'
$html=PostPage "/formulas/$id/confirm" @{}
Check ($html.Contains('Confirmed')) 'CEO approves formula'
Login 'pc'
$month=Get-Date -Format yyyy-MM
$html=PostPage '/planning' @{planningMonth=$month}
$cycle=[regex]::Match($html,'href="/planning/([a-f0-9-]+)"').Groups[1].Value
Check ($cycle.Length -eq 36) 'PC creates month'
$html=GetPage "/planning/$cycle"
$worker=[regex]::Match($html,'value="([a-f0-9-]{36})">Demo Worker').Groups[1].Value
Check ($worker.Length -eq 36) 'Worker selectable'
$html=PostPage "/planning/$cycle/plans" @{formulaId=$id;workerId=$worker;plannedDate=(Get-Date -Format yyyy-MM-dd);targetQuantity='100'}
Check ($html.Contains('Acceptance serum')) 'Production plan saved'
$null=PostPage "/planning/$cycle/lock" @{}
Login 'worker1'
$html=GetPage '/worker/tasks'
$plan=[regex]::Match($html,'/worker/plans/([a-f0-9-]+)').Groups[1].Value
Check ($plan.Length -eq 36) 'Assigned task visible'
$html=PostPage "/worker/plans/$plan/start" @{lossPercentage='5'}
Check ($html.Contains('105 units')) '100 + 5% = 105'
$html=PostPage "/worker/plans/$plan/complete" @{actualProduced='105';rejected='5'}
Check ($html.Contains('Good quantity: 100 units')) 'Batch completed with 100 good units'
Write-Output 'HTTP DEMO FLOW PASSED'
