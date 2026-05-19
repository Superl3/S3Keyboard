param(
    [switch] $SkipBuild
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Adb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
$Package = "com.superl3.s3keyboard"
$Ime = "$Package/.S3KeyboardService"
$Apk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"
$CaptureDir = Join-Path $Root "captures\smoke"

if (-not $SkipBuild) {
    & (Join-Path $PSScriptRoot "build-debug.ps1")
}

& (Join-Path $PSScriptRoot "setup-emulator.ps1")
& (Join-Path $PSScriptRoot "launch-emulator.ps1")

New-Item -ItemType Directory -Force -Path $CaptureDir | Out-Null
$Device = (& $Adb devices | Select-String -Pattern "emulator-\d+\s+device" | Select-Object -First 1).ToString().Split()[0]
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

Invoke-AdbTarget wait-for-device
Invoke-AdbTarget install -r $Apk
Invoke-AdbTarget shell ime enable $Ime
Invoke-AdbTarget shell ime set $Ime
Invoke-AdbTarget shell settings put secure show_ime_with_hard_keyboard 1

function Test-PackageInstalled {
    param([string] $PackageName)
    $result = (& $Adb @AdbTarget shell pm path $PackageName | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) {
        return $false
    }
    return $result.StartsWith("package:")
}

function Save-State {
    param([string] $Name)
    $safeName = $Name -replace "[^A-Za-z0-9_-]", "-"
    $state = & $Adb @AdbTarget shell dumpsys input_method
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: shell dumpsys input_method"
    }
    $state | Set-Content -LiteralPath (Join-Path $CaptureDir "$safeName-input_method.txt") -Encoding UTF8
    & $Adb @AdbTarget shell screencap -p "/sdcard/$safeName.png" | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: shell screencap -p /sdcard/$safeName.png"
    }
    Invoke-AdbTarget pull "/sdcard/$safeName.png" (Join-Path $CaptureDir "$safeName.png")
}

Write-Host "Smoke: local settings/practice field"
Invoke-AdbTarget shell am start -n "$Package/.MainActivity" --ez demo_settings true --ez demo_show_keyboard true
Start-Sleep -Seconds 2
Save-State "local-practice"

$Targets = @(
    @{ Name = "chrome-url"; PackageName = "com.android.chrome"; Command = @("shell", "am", "start", "-a", "android.intent.action.VIEW", "-d", "https://example.com") },
    @{ Name = "messages"; PackageName = "com.google.android.apps.messaging"; Command = @("shell", "monkey", "-p", "com.google.android.apps.messaging", "1") },
    @{ Name = "notes-keep"; PackageName = "com.google.android.keep"; Command = @("shell", "monkey", "-p", "com.google.android.keep", "1") }
)

foreach ($target in $Targets) {
    if (-not (Test-PackageInstalled $target.PackageName)) {
        Write-Host "Smoke: $($target.Name) skipped; package $($target.PackageName) is not installed"
        continue
    }
    Write-Host "Smoke: $($target.Name)"
    $CommandArgs = $target.Command
    Invoke-AdbTarget @CommandArgs
    Start-Sleep -Seconds 2
    Invoke-AdbTarget shell ime set $Ime
    Save-State $target.Name
}

Write-Host "Smoke artifacts: $CaptureDir"
