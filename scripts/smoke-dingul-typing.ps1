param(
    [switch] $SkipBuild,
    [switch] $ResetAppData
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Adb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
$Package = "com.superl3.s3keyboard"
$Activity = "$Package/.MainActivity"
$Ime = "$Package/.S3KeyboardService"
$Apk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"
$RunId = Get-Date -Format "yyyyMMdd-HHmmss"
$CaptureDir = Join-Path $Root "captures\dingul-typing-$RunId"
$ProbeTag = "DingulTypingProbe"

$Cases = @(
    @{ KeyCp = "3131"; Action = "TAP"; Expected = "3131" },
    @{ KeyCp = "3131"; Action = "TAP"; Expected = "3131"; XRatio = 0.28; YRatio = 0.28 },
    @{ KeyCp = "3131"; Action = "TAP"; Expected = "3131"; XRatio = 0.72; YRatio = 0.28 },
    @{ KeyCp = "3131"; Action = "TAP"; Expected = "3131"; XRatio = 0.28; YRatio = 0.72 },
    @{ KeyCp = "3131"; Action = "TAP"; Expected = "3131"; XRatio = 0.72; YRatio = 0.72 },
    @{ KeyCp = "3131"; Action = "TAP"; Expected = "3131"; Dx = 46; Dy = 43 },
    @{ KeyCp = "3131"; Action = "UP"; Expected = "3132" },
    @{ KeyCp = "3131"; Action = "DOWN"; Expected = "23" },
    @{ KeyCp = "3145"; Action = "TAP"; Expected = "3145" },
    @{ KeyCp = "3145"; Action = "TAP"; Expected = "3145"; XRatio = 0.28; YRatio = 0.28 },
    @{ KeyCp = "3145"; Action = "TAP"; Expected = "3145"; XRatio = 0.72; YRatio = 0.72 },
    @{ KeyCp = "3145"; Action = "UP"; Expected = "3146" },
    @{ KeyCp = "3163+2E"; Action = "LEFT"; Expected = "3153" },
    @{ KeyCp = "3163+2E"; Action = "RIGHT"; Expected = "314F" },
    @{ KeyCp = "3161+3150"; Action = "LEFT"; Expected = "3154" },
    @{ KeyCp = "3161+3150"; Action = "RIGHT"; Expected = "3150" }
)

if (-not $SkipBuild) {
    & (Join-Path $PSScriptRoot "build-debug.ps1")
}

& (Join-Path $PSScriptRoot "setup-emulator.ps1")
& (Join-Path $PSScriptRoot "launch-emulator.ps1")

New-Item -ItemType Directory -Force -Path $CaptureDir | Out-Null

$Device = (& $Adb devices | Select-String -Pattern "emulator-\d+\s+device" | Select-Object -First 1).ToString().Split()[0]
if (-not $Device) {
    throw "No running emulator found."
}
$AdbTarget = @("-s", $Device)

function Invoke-AdbTarget {
    param(
        [Parameter(ValueFromRemainingArguments = $true)]
        [string[]] $Arguments
    )
    & $Adb @AdbTarget @Arguments | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $($Arguments -join ' ')"
    }
}

function Get-ImeDump {
    $dump = & $Adb @AdbTarget shell dumpsys input_method
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: shell dumpsys input_method"
    }
    return ($dump | Out-String)
}

function Test-ImeReady {
    $dump = Get-ImeDump
    return $dump.Contains("mInputShown=true") -and $dump.Contains($Ime)
}

function Focus-PracticeField {
    for ($attempt = 0; $attempt -lt 8; $attempt++) {
        Invoke-AdbTarget shell input tap 540 565
        Start-Sleep -Milliseconds 450
        Invoke-AdbTarget shell ime set $Ime
        Start-Sleep -Milliseconds 650
        if (Test-ImeReady) {
            return
        }
    }
    $dumpPath = Join-Path $CaptureDir "input_method-not-ready.txt"
    Get-ImeDump | Set-Content -LiteralPath $dumpPath -Encoding UTF8
    throw "IME did not become visible; wrote $dumpPath"
}

function Read-ProbeLog {
    $lines = & $Adb @AdbTarget logcat -d -s "$ProbeTag`:I" "*:S"
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: logcat -d"
    }
    return $lines
}

function Read-Plan {
    $plan = @{}
    $pattern = "PLAN\s+seq=(\d+)\s+keyCp=([0-9A-F+]+)\s+action=([A-Z_]+)\s+valueCp=([0-9A-F+]*)\s+down=(\d+),(\d+)\s+up=(\d+),(\d+)\s+range=(\d+),(\d+),(\d+),(\d+)"
    foreach ($line in Read-ProbeLog) {
        if ($line -match $pattern) {
            $id = "$($Matches[2])|$($Matches[3])"
            $plan[$id] = [pscustomobject]@{
                KeyCp = $Matches[2]
                Action = $Matches[3]
                ValueCp = $Matches[4]
                DownX = [int] $Matches[5]
                DownY = [int] $Matches[6]
                UpX = [int] $Matches[7]
                UpY = [int] $Matches[8]
                RangeLeft = [int] $Matches[9]
                RangeTop = [int] $Matches[10]
                RangeRight = [int] $Matches[11]
                RangeBottom = [int] $Matches[12]
                Range = "$($Matches[9]),$($Matches[10]),$($Matches[11]),$($Matches[12])"
                Line = $line
            }
        }
    }
    return $plan
}

function Wait-ForPlan {
    for ($attempt = 0; $attempt -lt 16; $attempt++) {
        $plan = Read-Plan
        $missing = @()
        foreach ($case in $Cases) {
            $id = "$($case.KeyCp)|$($case.Action)"
            if (-not $plan.ContainsKey($id)) {
                $missing += $id
            }
        }
        if ($missing.Count -eq 0) {
            return $plan
        }
        Start-Sleep -Milliseconds 500
    }
    Read-ProbeLog | Set-Content -LiteralPath (Join-Path $CaptureDir "probe-plan-missing.txt") -Encoding UTF8
    throw "Dingul typing plan was incomplete."
}

function Read-Emits {
    $emits = New-Object System.Collections.Generic.List[object]
    $pattern = "EMIT\s+keyCp=([0-9A-F+]+)\s+action=([A-Z_]+)\s+valueCp=([0-9A-F+]*)\s+up=(\d+),(\d+)"
    foreach ($line in Read-ProbeLog) {
        if ($line -match $pattern) {
            $emits.Add([pscustomobject]@{
                KeyCp = $Matches[1]
                Action = $Matches[2]
                ValueCp = $Matches[3]
                UpX = [int] $Matches[4]
                UpY = [int] $Matches[5]
                Line = $line
            })
        }
    }
    return $emits
}

Invoke-AdbTarget shell setprop log.tag.$ProbeTag INFO
Invoke-AdbTarget logcat -c
Invoke-AdbTarget wait-for-device
Invoke-AdbTarget install -r $Apk
if ($ResetAppData) {
    Invoke-AdbTarget shell pm clear $Package
}
Invoke-AdbTarget shell settings put secure show_ime_with_hard_keyboard 1
Invoke-AdbTarget shell ime enable $Ime
Invoke-AdbTarget shell ime set $Ime
Invoke-AdbTarget shell am force-stop $Package
Invoke-AdbTarget shell am start -n $Activity --ez demo_settings true --ez demo_show_keyboard true
Start-Sleep -Seconds 3
Focus-PracticeField

$readyDump = Get-ImeDump
$readyDump | Set-Content -LiteralPath (Join-Path $CaptureDir "input_method-ready.txt") -Encoding UTF8
$plan = Wait-ForPlan
$plan.Values | Sort-Object KeyCp, Action | ForEach-Object { $_.Line } |
    Set-Content -LiteralPath (Join-Path $CaptureDir "probe-plan.txt") -Encoding UTF8

Invoke-AdbTarget logcat -c

foreach ($case in $Cases) {
    $id = "$($case.KeyCp)|$($case.Action)"
    $touch = $plan[$id]
    if ($touch.ValueCp -ne $case.Expected) {
        throw "Plan mismatch for $id; expected $($case.Expected), got $($touch.ValueCp)"
    }

    $downX = $touch.DownX
    $downY = $touch.DownY
    $upX = $touch.UpX
    $upY = $touch.UpY
    if ($case.ContainsKey("XRatio") -and $case.ContainsKey("YRatio")) {
        $downX = [int] [Math]::Round($touch.RangeLeft + ($touch.RangeRight - $touch.RangeLeft) * [double] $case.XRatio)
        $downY = [int] [Math]::Round($touch.RangeTop + ($touch.RangeBottom - $touch.RangeTop) * [double] $case.YRatio)
        $upX = $downX
        $upY = $downY
    }

    if ($case.ContainsKey("Dx") -or $case.ContainsKey("Dy")) {
        $dx = if ($case.ContainsKey("Dx")) { [int] $case.Dx } else { 0 }
        $dy = if ($case.ContainsKey("Dy")) { [int] $case.Dy } else { 0 }
        $upX = $downX + $dx
        $upY = $downY + $dy
        Invoke-AdbTarget shell input swipe $downX $downY $upX $upY 120
    } elseif ($case.Action -eq "TAP") {
        Invoke-AdbTarget shell input tap $downX $downY
    } else {
        Invoke-AdbTarget shell input swipe $downX $downY $upX $upY 120
    }
    Start-Sleep -Milliseconds 260
}

Start-Sleep -Seconds 1
$emits = Read-Emits
$emits | ForEach-Object { $_.Line } |
    Set-Content -LiteralPath (Join-Path $CaptureDir "probe-emits.txt") -Encoding UTF8

if ($emits.Count -lt $Cases.Count) {
    throw "Expected at least $($Cases.Count) Dingul emits, got $($emits.Count). Artifacts: $CaptureDir"
}

for ($i = 0; $i -lt $Cases.Count; $i++) {
    $case = $Cases[$i]
    $emit = $emits[$i]
    if ($emit.KeyCp -ne $case.KeyCp -or $emit.Action -ne $case.Action -or $emit.ValueCp -ne $case.Expected) {
        throw "Emit mismatch at $i; expected $($case.KeyCp)|$($case.Action)|$($case.Expected), got $($emit.KeyCp)|$($emit.Action)|$($emit.ValueCp). Artifacts: $CaptureDir"
    }
}

$finalDump = Get-ImeDump
$finalDump | Set-Content -LiteralPath (Join-Path $CaptureDir "input_method-final.txt") -Encoding UTF8
& $Adb @AdbTarget shell screencap -p /sdcard/dingul-typing-final.png | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "adb failed: shell screencap -p /sdcard/dingul-typing-final.png"
}
Invoke-AdbTarget pull /sdcard/dingul-typing-final.png (Join-Path $CaptureDir "dingul-typing-final.png")

Write-Host "Dingul typing probe passed: $($Cases.Count) key actions"
Write-Host "Artifacts: $CaptureDir"
