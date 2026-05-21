$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$ToolsRoot = Join-Path $Root ".android-tools"
$BundledJdkRoot = Join-Path $ToolsRoot "jdk-17"
$BundledAndroidSdk = Join-Path $ToolsRoot "android-sdk"
$GradleRoot = Join-Path $ToolsRoot "gradle-8.10.2"
$AndroidUserHome = Join-Path $ToolsRoot "android-user"
$AndroidAvdHome = Join-Path $ToolsRoot "avd"

$JdkCandidates = @(
    $BundledJdkRoot,
    $env:JAVA_HOME,
    "C:\Program Files\Android\openjdk\jdk-21.0.8"
) | Where-Object { $_ }

$JdkRoot = $JdkCandidates |
    Where-Object { Test-Path (Join-Path $_ "bin\java.exe") } |
    Select-Object -First 1

if (-not $JdkRoot) {
    throw "JDK not found. Run scripts\install-android-env.ps1 first or set JAVA_HOME."
}

$DefaultUserAndroidSdk = Join-Path ([Environment]::GetFolderPath("LocalApplicationData")) "Android\Sdk"
$AndroidSdkCandidates = @(
    $BundledAndroidSdk,
    $env:ANDROID_SDK_ROOT,
    $env:ANDROID_HOME,
    $DefaultUserAndroidSdk
) | Where-Object { $_ }

$AndroidSdk = $AndroidSdkCandidates |
    Where-Object { Test-Path (Join-Path $_ "platforms\android-35") } |
    Select-Object -First 1

if (-not $AndroidSdk) {
    throw "Android SDK platform android-35 not found. Run scripts\install-android-env.ps1 first or set ANDROID_SDK_ROOT."
}

$CmdlineToolsBin = Join-Path $AndroidSdk "cmdline-tools\latest\bin"
if (-not (Test-Path (Join-Path $CmdlineToolsBin "sdkmanager.bat"))) {
    Write-Warning "Android SDK command-line tools not found under $AndroidSdk. Build/test can continue, but emulator setup scripts may need scripts\install-android-env.ps1."
}

$env:JAVA_HOME = $JdkRoot
$env:ANDROID_HOME = $AndroidSdk
$env:ANDROID_SDK_ROOT = $AndroidSdk
$env:ANDROID_USER_HOME = $AndroidUserHome
$env:ANDROID_EMULATOR_HOME = $AndroidUserHome
$env:ANDROID_AVD_HOME = $AndroidAvdHome
$PathEntries = @(
    (Join-Path $JdkRoot "bin"),
    (Join-Path $AndroidSdk "platform-tools")
)

if (Test-Path $CmdlineToolsBin) {
    $PathEntries += $CmdlineToolsBin
}
if (Test-Path (Join-Path $GradleRoot "bin")) {
    $PathEntries += (Join-Path $GradleRoot "bin")
}

$env:Path = ($PathEntries + $env:Path) -join ";"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "ANDROID_SDK_ROOT=$env:ANDROID_SDK_ROOT"
Write-Host "ANDROID_AVD_HOME=$env:ANDROID_AVD_HOME"
