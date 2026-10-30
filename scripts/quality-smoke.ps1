param([string]$Base='http://localhost:8102')
$ErrorActionPreference='Stop'
$s=New-Object Microsoft.PowerShell.Commands.WebRequestSession
function GetPage($path){(Invoke-WebRequest ($Base+$path) -WebSession $s).Content}
function PostPage($path,$body){(Invoke-WebRequest ($Base+$path) -Method Post -Body $body -WebSession $s).Content}
function Login($name){$null=GetPage '/login?lang=en';$null=PostPage '/login' @{username=$name;password='123456'}}
function Check($value,$name){if(!$value){throw "FAILED: $name"};Write-Output "PASS: $name"}
Login 'ceo'
$html=GetPage '/planning'
$cycle=[regex]::Match($html,'href="/planning/([a-f0-9-]+)"').Groups[1].Value
$html=GetPage "/planning/$cycle"
$formula=[regex]::Match($html,'value="([a-f0-9-]{36})">HTTP-').Groups[1].Value
$worker=[regex]::Match($html,'value="([a-f0-9-]{36})">Demo Worker').Groups[1].Value
$html=PostPage "/planning/$cycle/plans" @{formulaId=$formula;workerId=$worker;plannedDate=(Get-Date -Format yyyy-MM-dd);targetQuantity='100'}
Check ($html.Contains('Production task added')) 'CEO can add task to locked month'
Login 'pc'
$html=PostPage "/planning/$cycle/plans" @{formulaId=$formula;workerId=$worker;plannedDate=(Get-Date -Format yyyy-MM-dd);targetQuantity='100'}
Check ($html.Contains('Locked planning cycle cannot be edited')) 'PC cannot alter locked month'
Login 'worker1'
$html=GetPage '/worker/tasks'
$links=[regex]::Matches($html,'/worker/plans/([a-f0-9-]+)')
$plan=$null
foreach($link in $links){$candidate=$link.Groups[1].Value;if((GetPage "/worker/plans/$candidate").Contains('Start entire batch')){$plan=$candidate;break}}
Check ($null -ne $plan) 'New assigned task available'
$html=PostPage "/worker/plans/$plan/start" @{lossPercentage='5'}
Check ($html.Contains('1050.000 ML')) 'Materials scale to 105 units'
$html=PostPage "/worker/plans/$plan/complete" @{actualProduced='105';rejected='10'}
Check ($html.Contains('Good quantity: 95 units')) 'Exception output calculated'
$denied=Invoke-WebRequest ($Base+"/quality/$plan/review") -Method Post -Body @{note='Not allowed'} -WebSession $s -SkipHttpErrorCheck
Check ($denied.StatusCode -eq 403) 'Worker cannot review QC output'
Login 'qc'
$html=PostPage "/quality/$plan/review" @{note='Verified quality smoke counts'}
Check ($html.Contains('Verified quality smoke counts')) 'QC review persisted'
$html=PostPage "/quality/$plan/review" @{note='Overwrite attempt'}
Check (!$html.Contains('Overwrite attempt')) 'Second review cannot overwrite history'
Login 'ceo'
$html=GetPage "/worker/plans/$plan"
Check ($html.Contains('Quality review recorded') -and $html.Contains('Good quantity: 95 units')) 'Reviewed status and quantities preserved'
Write-Output 'QUALITY FLOW PASSED'
