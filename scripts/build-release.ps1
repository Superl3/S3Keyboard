$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")

Write-Host "Building release APK"
& (Join-Path $Root "gradlew.bat") --no-daemon assembleRelease
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

$ReleaseDirectory = Join-Path $Root "app\build\outputs\apk\release"
$Apks = @(Get-ChildItem -LiteralPath $ReleaseDirectory -Filter "*.apk")
if ($Apks.Count -eq 0) {
    throw "Release build completed without an APK output."
}

$BuildTools = Join-Path $env:ANDROID_SDK_ROOT "build-tools"
$ApkSigner = Get-ChildItem -LiteralPath $BuildTools -Recurse -Filter "apksigner.bat" |
    Sort-Object FullName -Descending |
    Select-Object -First 1
if ($null -eq $ApkSigner) {
    throw "apksigner.bat was not found under $BuildTools"
}

foreach ($Apk in $Apks) {
    & $ApkSigner.FullName verify --verbose $Apk.FullName
    if ($LASTEXITCODE -ne 0) {
        throw "APK signature verification failed: $($Apk.FullName)"
    }
}

Write-Host "Verified release outputs:"
$Apks | Select-Object FullName, Length, LastWriteTime
