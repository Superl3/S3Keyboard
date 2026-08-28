$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
Push-Location $Root
try {
    & (Join-Path $Root "gradlew.bat") --no-daemon assembleDebug
} finally {
    Pop-Location
}
