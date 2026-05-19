$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")

Write-Host "Building release APK"
& (Join-Path $Root "gradlew.bat") --no-daemon assembleRelease

Write-Host "Release outputs:"
Get-ChildItem -LiteralPath (Join-Path $Root "app\build\outputs\apk\release") -Filter "*.apk" |
    Select-Object FullName, Length, LastWriteTime
