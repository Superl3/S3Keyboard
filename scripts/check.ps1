$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
& (Join-Path $Root "gradlew.bat") --no-daemon testDebugUnitTest assembleDebug
