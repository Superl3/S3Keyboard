param(
    [string] $Serial = ""
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Apk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"

if (-not (Test-Path $Apk)) {
    throw "Debug APK not found. Run scripts\build-debug.ps1 first."
}

$adbArgs = @()
if (-not [string]::IsNullOrWhiteSpace($Serial)) {
    $adbArgs += @("-s", $Serial)
}
$adbArgs += @("install", "-r", $Apk)

& (Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe") @adbArgs
