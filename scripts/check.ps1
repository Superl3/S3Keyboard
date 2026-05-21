$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
& node (Join-Path $Root "tools\sync-themes.mjs") --check --report
& (Join-Path $Root "gradlew.bat") --no-daemon testDebugUnitTest assembleDebug
