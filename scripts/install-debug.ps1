param(
    [string] $Serial = "",
    [switch] $SkipImeSelect
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

$Adb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
& $Adb @adbArgs

if (-not $SkipImeSelect) {
    $targetIme = "com.superl3.s3keyboard/.S3KeyboardService"
    $shellArgs = @()
    if (-not [string]::IsNullOrWhiteSpace($Serial)) {
        $shellArgs += @("-s", $Serial)
    }

    & $Adb @shellArgs "shell" "am" "force-stop" "com.superl3.s3keyboard"
    & $Adb @shellArgs "shell" "ime" "enable" $targetIme
    & $Adb @shellArgs "shell" "ime" "set" $targetIme
    & $Adb @shellArgs "shell" "monkey" "-p" "com.superl3.s3keyboard" "1"
}
