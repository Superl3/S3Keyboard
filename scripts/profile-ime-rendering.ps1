param(
    [int]$TransportId = 0,
    [int]$DurationSeconds = 12,
    [string]$OutputDirectory = ".\artifacts\performance"
)

$ErrorActionPreference = "Stop"
$packageName = "com.superl3.s3keyboard"
$adb = Join-Path $PSScriptRoot "..\.android-tools\android-sdk\platform-tools\adb.exe"

if (-not (Test-Path $adb)) {
    throw "ADB not found at $adb"
}

$adbArgs = @()
if ($TransportId -gt 0) {
    $adbArgs += @("-t", $TransportId)
}

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputPath = Join-Path $OutputDirectory "ime-framestats-$timestamp.txt"

& $adb @adbArgs shell dumpsys gfxinfo $packageName reset | Out-Null
Write-Host "For the next $DurationSeconds seconds, type continuously with the keyboard."
Start-Sleep -Seconds $DurationSeconds
& $adb @adbArgs shell dumpsys gfxinfo $packageName framestats | Set-Content -Encoding UTF8 $outputPath

Write-Host "Saved IME frame statistics to $outputPath"
