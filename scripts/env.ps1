$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$ToolsRoot = Join-Path $Root ".android-tools"
$JdkRoot = Join-Path $ToolsRoot "jdk-17"
$AndroidSdk = Join-Path $ToolsRoot "android-sdk"
$GradleRoot = Join-Path $ToolsRoot "gradle-8.10.2"
$AndroidUserHome = Join-Path $ToolsRoot "android-user"
$AndroidAvdHome = Join-Path $ToolsRoot "avd"

if (-not (Test-Path (Join-Path $JdkRoot "bin\java.exe"))) {
    throw "JDK not found under $JdkRoot. Run scripts\install-android-env.ps1 first."
}

if (-not (Test-Path (Join-Path $AndroidSdk "cmdline-tools\latest\bin\sdkmanager.bat"))) {
    throw "Android SDK command-line tools not found under $AndroidSdk. Run scripts\install-android-env.ps1 first."
}

$env:JAVA_HOME = $JdkRoot
$env:ANDROID_HOME = $AndroidSdk
$env:ANDROID_SDK_ROOT = $AndroidSdk
$env:ANDROID_USER_HOME = $AndroidUserHome
$env:ANDROID_EMULATOR_HOME = $AndroidUserHome
$env:ANDROID_AVD_HOME = $AndroidAvdHome
$env:Path = @(
    (Join-Path $JdkRoot "bin"),
    (Join-Path $AndroidSdk "platform-tools"),
    (Join-Path $AndroidSdk "cmdline-tools\latest\bin"),
    (Join-Path $GradleRoot "bin"),
    $env:Path
) -join ";"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "ANDROID_SDK_ROOT=$env:ANDROID_SDK_ROOT"
Write-Host "ANDROID_AVD_HOME=$env:ANDROID_AVD_HOME"
