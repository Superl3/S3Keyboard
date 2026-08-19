param(
    [switch]$FailOnUnused,
    [switch]$ShowTable
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$sourceRoot = Join-Path $repoRoot "app\src\main\java\com\superl3\s3keyboard"
$settingsPath = Join-Path $sourceRoot "KeyboardSettings.java"

$settingsSource = Get-Content -LiteralPath $settingsPath -Raw -Encoding UTF8
$runtimeSource = Get-ChildItem -LiteralPath $sourceRoot -Filter "*.java" |
    Where-Object { $_.Name -ne "KeyboardSettings.java" } |
    ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8 }
$runtimeText = $runtimeSource -join [Environment]::NewLine

$fieldPattern = '(?m)^    final\s+(?<type>[A-Za-z0-9_$.<>?, ]+?)\s+(?<name>[A-Za-z_][A-Za-z0-9_]*);\r?$'
$rows = foreach ($match in [regex]::Matches($settingsSource, $fieldPattern)) {
    $name = $match.Groups["name"].Value
    $usagePattern = '\.' + [regex]::Escape($name) + '\b'
    [pscustomobject]@{
        Field = $name
        RuntimeReferences = [regex]::Matches($runtimeText, $usagePattern).Count
    }
}

$unused = @($rows | Where-Object { $_.RuntimeReferences -eq 0 })
if ($ShowTable) {
    $rows | Sort-Object RuntimeReferences, Field | Format-Table -AutoSize
}

if ($unused.Count -gt 0) {
    $names = ($unused.Field | Sort-Object) -join ", "
    if ($FailOnUnused) {
        Write-Error "KeyboardSettings fields without runtime consumers: $names"
        exit 1
    }
    Write-Warning "KeyboardSettings fields without runtime consumers: $names"
}

Write-Host "KeyboardSettings usage audit passed: $($rows.Count) fields, $($unused.Count) unused."
