param(
    [string]$VersionName = "0.2.0-beta.1",
    [int]$VersionCode = 2,
    [string]$OutputName = "new-dingul-$VersionName-installable.apk"
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "env.ps1")
$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Dist = Join-Path $Root "dist"
New-Item -ItemType Directory -Force -Path $Dist | Out-Null

Push-Location $Root
try {
    & (Join-Path $Root "gradlew.bat") --no-daemon assembleDebug "-PS3_VERSION_NAME=$VersionName" "-PS3_VERSION_CODE=$VersionCode"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
} finally {
    Pop-Location
}

$Source = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path -LiteralPath $Source)) { throw "Debug APK was not produced: $Source" }
$Destination = Join-Path $Dist $OutputName
Copy-Item -LiteralPath $Source -Destination $Destination -Force
Write-Host "Installable beta APK: $Destination"
Get-Item -LiteralPath $Destination | Select-Object FullName, Length, LastWriteTime
