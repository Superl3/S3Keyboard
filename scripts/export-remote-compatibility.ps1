param(
    [string] $Serial = "",
    [string] $TargetPackage = "",
    [string] $OutDir = ""
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Adb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
$ImePackage = "com.superl3.s3keyboard"
if ([string]::IsNullOrWhiteSpace($OutDir)) {
    $OutDir = Join-Path $Root "artifacts\remote-compatibility"
}
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$adbArgs = @()
if (-not [string]::IsNullOrWhiteSpace($Serial)) {
    $adbArgs += @("-s", $Serial)
}

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)] [string[]] $Arguments)
    $output = & $Adb @adbArgs @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $($Arguments -join ' ')"
    }
    return ($output | Out-String)
}

function Get-RemoteAppFamily {
    param([string] $PackageName)
    switch ($PackageName) {
        "tv.parsec.client" { return "parsec" }
        "com.limelight" { return "moonlight" }
        "com.microsoft.rdc.android" { return "microsoft_rdp" }
        "com.microsoft.rdc.androidx" { return "microsoft_rdp" }
        "com.google.chromeremotedesktop" { return "chrome_remote_desktop" }
        "com.valvesoftware.steamlink" { return "steam_link" }
        "com.anydesk.anydeskandroid" { return "anydesk" }
        "com.teamviewer.teamviewer.market.mobile" { return "teamviewer" }
        "com.teamviewer.quicksupport.market" { return "teamviewer" }
        default {
            if ([string]::IsNullOrWhiteSpace($PackageName)) {
                return "unknown"
            }
            return "custom"
        }
    }
}

$matrix = @(
    @{ Label = "Esc"; ExpectedEventCount = 2; Group = "BASIC" },
    @{ Label = "Tab"; ExpectedEventCount = 2; Group = "BASIC" },
    @{ Label = "Shift+Tab"; ExpectedEventCount = 4; Group = "BASIC" },
    @{ Label = "Ctrl+Tab"; ExpectedEventCount = 4; Group = "BASIC" },
    @{ Label = "Alt+Tab"; ExpectedEventCount = 4; Group = "BASIC" },
    @{ Label = "Ctrl+A"; ExpectedEventCount = 4; Group = "BASIC" },
    @{ Label = "F1"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F2"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F3"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F4"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F5"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F6"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F7"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F8"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F9"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F10"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F11"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "F12"; ExpectedEventCount = 2; Group = "FUNCTION" },
    @{ Label = "Alt+Shift"; ExpectedEventCount = 4; Group = "IME" },
    @{ Label = "Ctrl+Space"; ExpectedEventCount = 4; Group = "IME" },
    @{ Label = "Win+Space"; ExpectedEventCount = 4; Group = "IME" },
    @{ Label = "Lang"; ExpectedEventCount = 2; Group = "IME" }
)

$requiredLabels = @($matrix | ForEach-Object { $_.Label })
$requiredAppFamilies = @(
    "parsec",
    "moonlight",
    "microsoft_rdp",
    "chrome_remote_desktop",
    "steam_link",
    "anydesk",
    "teamviewer"
)

$prefsText = Invoke-Adb shell run-as $ImePackage cat shared_prefs/keyboard_preferences.xml
$prefsPath = Join-Path $OutDir "keyboard_preferences.xml"
$prefsText | Set-Content -LiteralPath $prefsPath -Encoding UTF8

[xml] $prefs = $prefsText
$logNode = $prefs.SelectSingleNode("//string[@name='remote_compatibility_test_log']")
$entries = @()
if ($logNode -and -not [string]::IsNullOrWhiteSpace($logNode.InnerText)) {
    $entries = $logNode.InnerText | ConvertFrom-Json
}

if ([string]::IsNullOrWhiteSpace($TargetPackage) -and $entries.Count -gt 0) {
    $TargetPackage = [string] $entries[0].packageName
}

$sent = @{}
foreach ($entry in $entries) {
    if (-not [string]::IsNullOrWhiteSpace($TargetPackage) -and $entry.packageName -ne $TargetPackage) {
        continue
    }
    if (-not $sent.ContainsKey($entry.label)) {
        $sent[$entry.label] = $entry
    }
}

$cases = @()
foreach ($case in $matrix) {
    $label = [string] $case.Label
    $entry = if ($sent.ContainsKey($label)) { $sent[$label] } else { $null }
    $manualResult = if ($entry -and $entry.PSObject.Properties.Name -contains "manualResult") {
        [string] $entry.manualResult
    } elseif ($entry) {
        "unknown"
    } else {
        "unknown"
    }
    $acceptedEventCount = if ($entry -and $entry.PSObject.Properties.Name -contains "acceptedEventCount") {
        [int] $entry.acceptedEventCount
    } elseif ($entry) {
        [int] $entry.eventCount
    } else {
        $null
    }
    $expectedEventCount = if ($entry -and $entry.PSObject.Properties.Name -contains "expectedEventCount") {
        [int] $entry.expectedEventCount
    } else {
        [int] $case.ExpectedEventCount
    }
    $localInputConnectionAccepted = $false
    if ($null -ne $acceptedEventCount) {
        $localInputConnectionAccepted = $expectedEventCount -gt 0 -and $acceptedEventCount -ge $expectedEventCount
    }
    $cases += [pscustomobject]@{
        label = $label
        group = [string] $case.Group
        sent = $null -ne $entry
        manualResult = $manualResult
        timestampMs = if ($entry) { [int64] $entry.timestampMs } else { $null }
        eventCount = $acceptedEventCount
        acceptedEventCount = $acceptedEventCount
        expectedEventCount = $expectedEventCount
        localInputConnectionAccepted = $localInputConnectionAccepted
        localTransportComplete = $localInputConnectionAccepted
    }
}

$passCount = ($cases | Where-Object { $_.manualResult -eq "pass" }).Count
$failCount = ($cases | Where-Object { $_.manualResult -eq "fail" }).Count
$unknownCount = ($cases | Where-Object { $_.sent -and $_.manualResult -ne "pass" -and $_.manualResult -ne "fail" }).Count
$missingCount = ($cases | Where-Object { -not $_.sent }).Count
$localIncompleteCount = ($cases | Where-Object { $_.sent -and -not $_.localInputConnectionAccepted }).Count
$missingLabels = @($cases | Where-Object { -not $_.sent } | ForEach-Object { $_.label })
$unknownLabels = @($cases | Where-Object {
    $_.sent -and $_.manualResult -ne "pass" -and $_.manualResult -ne "fail"
} | ForEach-Object { $_.label })
$failedLabels = @($cases | Where-Object { $_.manualResult -eq "fail" } | ForEach-Object { $_.label })
$localIncompleteLabels = @($cases | Where-Object {
    $_.sent -and -not $_.localInputConnectionAccepted
} | ForEach-Object { $_.label })

$groupSummaries = @()
foreach ($group in @("BASIC", "FUNCTION", "IME")) {
    $groupCases = @($cases | Where-Object { $_.group -eq $group })
    $groupSentCases = @($groupCases | Where-Object { $_.sent })
    $groupSummaries += [pscustomobject]@{
        group = $group
        totalCount = $groupCases.Count
        sentCount = $groupSentCases.Count
        passCount = @($groupCases | Where-Object { $_.manualResult -eq "pass" }).Count
        failCount = @($groupCases | Where-Object { $_.manualResult -eq "fail" }).Count
        unknownCount = @($groupCases | Where-Object {
            $_.sent -and $_.manualResult -ne "pass" -and $_.manualResult -ne "fail"
        }).Count
        missingCount = @($groupCases | Where-Object { -not $_.sent }).Count
        localIncompleteCount = @($groupCases | Where-Object {
            $_.sent -and -not $_.localInputConnectionAccepted
        }).Count
        missingLabels = @($groupCases | Where-Object { -not $_.sent } | ForEach-Object { $_.label })
        unknownLabels = @($groupCases | Where-Object {
            $_.sent -and $_.manualResult -ne "pass" -and $_.manualResult -ne "fail"
        } | ForEach-Object { $_.label })
        failedLabels = @($groupCases | Where-Object { $_.manualResult -eq "fail" } | ForEach-Object { $_.label })
        localIncompleteLabels = @($groupCases | Where-Object {
            $_.sent -and -not $_.localInputConnectionAccepted
        } | ForEach-Object { $_.label })
    }
}

$report = [pscustomobject]@{
    schemaVersion = 2
    packageName = $TargetPackage
    appFamily = Get-RemoteAppFamily $TargetPackage
    sentCount = ($cases | Where-Object { $_.sent }).Count
    totalCount = $cases.Count
    passCount = $passCount
    failCount = $failCount
    unknownCount = $unknownCount
    localIncompleteCount = $localIncompleteCount
    manualRemoteResultRequired = ($unknownCount -gt 0 -or $missingCount -gt 0 -or $localIncompleteCount -gt 0)
    requiredLabels = $requiredLabels
    requiredAppFamilies = $requiredAppFamilies
    missingLabels = $missingLabels
    unknownLabels = $unknownLabels
    failedLabels = $failedLabels
    localIncompleteLabels = $localIncompleteLabels
    groupSummaries = $groupSummaries
    cases = $cases
}

$safeName = if ([string]::IsNullOrWhiteSpace($TargetPackage)) { "unknown" } else { $TargetPackage -replace "[^A-Za-z0-9_.-]", "_" }
$reportPath = Join-Path $OutDir "remote-compatibility-$safeName.json"
$report | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $reportPath -Encoding UTF8

Write-Host "Remote compatibility report: $reportPath"
Write-Host "Sent $($report.sentCount)/$($report.totalCount), pass=$passCount, fail=$failCount, unknown=$unknownCount."
