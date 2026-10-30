param([string]$Base='http://localhost:8100')
$ErrorActionPreference='Stop'
$s=New-Object Microsoft.PowerShell.Commands.WebRequestSession
function GetPage($path){ (Invoke-WebRequest ($Base+$path) -WebSession $s).Content }
function PostPage($path,$body){ (Invoke-WebRequest ($Base+$path) -Method Post -Body $body -WebSession $s).Content }
function Check($condition,$label){if(!$condition){throw "FAILED: $label"}; Write-Output "PASS: $label"}
function Login($user){$null=GetPage '/login?lang=en'; $null=PostPage '/login' @{username=$user;password='123456'}}
Login 'qc'
$tag='HTTP-'+[DateTimeOffset]::Now.ToUnixTimeSeconds()
$html=PostPage '/formulas' @{documentNo=$tag;name='Acceptance serum';materialIds='';newMaterialCodes=$tag;newMaterialNames='Acceptance water';newMaterialUnits='ML';quantitiesPerUnit='10'}
Check ($html.Contains($tag)) 'QC creates formula with new material'
$id=[regex]::Match($html,'/formulas/([a-f0-9-]+)/submit').Groups[1].Value
Check ($id.Length -eq 36) 'Formula has submit action'
$html=PostPage "/formulas/$id/submit" @{}
Check ($html.Contains('Awaiting approval')) 'Submit formula'
$denied=Invoke-WebRequest ($Base+"/formulas/$id/confirm") -Method Post -WebSession $s -SkipHttpErrorCheck
Check ($denied.StatusCode -eq 403) 'QC cannot approve'
Login 'ceo'
$html=PostPage "/formulas/$id/confirm" @{}
Check ($html.Contains('Confirmed')) 'CEO approves'
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
$html=PostPage "/worker/plans/$plan/complete" @{actualProduced='105';rejected='106'}
Check ($html.Contains('invalid')) 'Invalid rejected count blocked'
$html=PostPage "/worker/plans/$plan/complete" @{actualProduced='105';rejected='5'}
Check ($html.Contains('Good quantity: 100 units')) 'Batch completed with 100 good units'
$html=GetPage "/worker/plans/$plan`?lang=th"
Check ($html.Contains('จำนวนดี: 100 ชิ้น')) 'Thai result translated'
$html=GetPage "/worker/plans/$plan"
Check ($html.Contains('จำนวนดี: 100 ชิ้น')) 'Language persists across navigation'
Login 'ceo'
foreach($path in @('/workflow','/materials','/formulas','/formulas/new','/planning',"/planning/$cycle",'/worker/tasks',"/worker/plans/$plan")){
 foreach($lang in @('en','th')){$html=GetPage ($path+'?lang='+$lang);Check (!$html.Contains('??')) "$path language=$lang renders"}
}
Write-Output 'HTTP DEMO FLOW PASSED'
