$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$TextFiles = @(
    Get-ChildItem -Path (Join-Path $Root "app\src\main"), (Join-Path $Root "docs") `
        -Recurse -File -Include *.java, *.xml, *.md
    Get-Item (Join-Path $Root "README.md")
)
$MojibakePattern = @(
    [char]0xFFFD,
    [char]0x00C3,
    [char]0x00C2,
    [char]0x6E72,
    [char]0x5A9B,
    [char]0xF9CD
) -join "|"
$BrokenText = @($TextFiles | Select-String -Pattern $MojibakePattern -Encoding UTF8)
if ($BrokenText.Count -gt 0) {
    $BrokenText | ForEach-Object { Write-Host $_ }
    throw "Potential mojibake detected in user-facing source text."
}
& node (Join-Path $Root "tools\sync-themes.mjs") --check --report
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
& (Join-Path $PSScriptRoot "audit-settings-usage.ps1") -FailOnUnused
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
& (Join-Path $Root "gradlew.bat") --no-daemon testDebugUnitTest lintDebug assembleDebug
exit $LASTEXITCODE
