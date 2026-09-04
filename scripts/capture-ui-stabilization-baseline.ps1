param(
    [string] $Serial = "emulator-5558",
    [string] $OutDir = "",
    [switch] $SkipSettings
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Adb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
$Package = "com.superl3.s3keyboard"
$Ime = "$Package/.S3KeyboardService"
$CaptureDir = if ([string]::IsNullOrWhiteSpace($OutDir)) {
    Join-Path $Root "captures\ui-stabilization-202609\baseline"
} else { $OutDir }

New-Item -ItemType Directory -Force -Path $CaptureDir | Out-Null

function New-UnicodeText {
    param([int[]] $Codes)
    return -join ($Codes | ForEach-Object { [char] $_ })
}

# Keep this harness ASCII-only so Windows PowerShell 5.1 does not corrupt UTF-8 literals.
$TextNext = New-UnicodeText @(0xB2E4, 0xC74C)
$TextThemeSelect = New-UnicodeText @(0xD14C, 0xB9C8, 0x20, 0xC120, 0xD0DD)
$TextThemeEdit = New-UnicodeText @(0xD14C, 0xB9C8, 0x20, 0xD3B8, 0xC9D1)
$TextAccentPlacement = New-UnicodeText @(0xC2DC, 0xAC01, 0x20, 0xC5ED, 0xD560, 0x20, 0xD3B8, 0xC9D1)
$TextLayoutEdit = New-UnicodeText @(0xB808, 0xC774, 0xC544, 0xC6C3, 0x20, 0xC2DC, 0xAC01, 0x20, 0xD3B8, 0xC9D1)
$TextRemoteAuto = New-UnicodeText @(0xC571, 0x20, 0xC790, 0xB3D9, 0x20, 0xC804, 0xD658)
$TextRemoteOverrides = New-UnicodeText @(0xACE0, 0xAE09, 0x20, 0xC571, 0xBCC4, 0x20, 0xC608, 0xC678)
$TextDiagnostics = New-UnicodeText @(0xC548, 0xC804, 0x20, 0xC9C4, 0xB2E8, 0x20, 0xB9AC, 0xD3EC, 0xD2B8, 0x20, 0xC5F4, 0xAE30)
$TextBackupRestore = New-UnicodeText @(0xBC31, 0xC5C5, 0x20, 0xBC0F, 0x20, 0xBCF5, 0xC6D0)

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]] $Arguments)
    & $Adb -s $Serial @Arguments | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "adb failed: $($Arguments -join ' ')" }
}

function Start-Main {
    Invoke-Adb shell am force-stop $Package
    Invoke-Adb shell am start -n "$Package/.MainActivity"
    Start-Sleep -Milliseconds 900
}
function Dump-Ui {
    param([string] $Name)
    $remote = "/sdcard/sui01-$Name.xml"
    $local = Join-Path $CaptureDir "$Name.xml"
    for ($attempt = 0; $attempt -lt 5; $attempt++) {
        & $Adb -s $Serial shell rm -f $remote | Out-Null
        & $Adb -s $Serial shell uiautomator dump $remote | Out-Null
        if ($LASTEXITCODE -eq 0) {
            & $Adb -s $Serial pull $remote $local | Out-Null
            if ($LASTEXITCODE -eq 0 -and (Test-Path -LiteralPath $local)) {
                return [xml](Get-Content -LiteralPath $local -Raw -Encoding UTF8)
            }
        }
        Start-Sleep -Milliseconds 650
    }
    throw "Failed to capture hierarchy $Name after retries"
}

function Capture-State {
    param([string] $Name)
    $xml = Dump-Ui $Name
    $remote = "/sdcard/sui01-$Name.png"
    $local = Join-Path $CaptureDir "$Name.png"
    for ($attempt = 0; $attempt -lt 4; $attempt++) {
        & $Adb -s $Serial shell rm -f $remote | Out-Null
        & $Adb -s $Serial shell screencap -p $remote | Out-Null
        if ($LASTEXITCODE -eq 0) {
            & $Adb -s $Serial pull $remote $local | Out-Null
            if ($LASTEXITCODE -eq 0 -and (Test-Path -LiteralPath $local)) {
                return $xml
            }
        }
        Start-Sleep -Milliseconds 500
    }
    throw "Failed to capture $Name after retries"
}

function Find-NodeCenter {
    param([xml] $Hierarchy, [string] $Text, [switch] $Contains)
    $nodes = @($Hierarchy.SelectNodes("//node"))
    $node = $nodes | Where-Object {
        $candidate = [string]$_.text
        if ($Contains) { $candidate.Contains($Text) } else { $candidate -eq $Text }
    } | Select-Object -First 1
    if ($null -eq $node -or $node.bounds -notmatch '\[(\d+),(\d+)\]\[(\d+),(\d+)\]') {
        return $null
    }
    return @(
        [int](([int]$Matches[1] + [int]$Matches[3]) / 2),
        [int](([int]$Matches[2] + [int]$Matches[4]) / 2)
    )
}

function Tap-Text {
    param([string] $Text, [switch] $Contains)
    $xml = Dump-Ui "tap-probe"
    $center = Find-NodeCenter $xml $Text -Contains:$Contains
    if ($null -eq $center) { return $false }
    Invoke-Adb shell input tap $center[0] $center[1]
    Start-Sleep -Milliseconds 700
    return $true
}

function Swipe-ContentDown {
    Invoke-Adb shell input swipe 540 2050 540 930 350
    Start-Sleep -Milliseconds 500
}

function Capture-ScrollSeries {
    param([string] $Prefix, [int] $Count = 3)
    Capture-State "$Prefix-top" | Out-Null
    for ($i = 1; $i -le $Count; $i++) {
        Swipe-ContentDown
        Capture-State "$Prefix-scroll$i" | Out-Null
    }
}
Invoke-Adb wait-for-device
Invoke-Adb shell ime enable $Ime
Invoke-Adb shell ime set $Ime
if (-not $SkipSettings) {
    Start-Main
    # Main settings wizard: capture every step plus scrolled content.
    for ($step = 1; $step -le 8; $step++) {
        Capture-ScrollSeries ("settings-step{0:d2}" -f $step) 3
        if ($step -lt 8) {
            if (-not (Tap-Text $TextNext)) { throw "Could not advance from settings step $step" }
        }
    }
}

# Secondary surfaces reachable from Quick Start.
Start-Main
if (Tap-Text $TextThemeSelect) {
    Capture-ScrollSeries "theme-selector" 3
    Invoke-Adb shell input keyevent 4
    Start-Sleep -Milliseconds 600
}
if (Tap-Text $TextThemeEdit) {
    Capture-ScrollSeries "theme-editor" 3
    Invoke-Adb shell input keyevent 4
    Start-Sleep -Milliseconds 600
}
# Accent placement is reached from the Display wizard step, not Theme Editor.
Start-Main
for ($i = 0; $i -lt 4; $i++) { Tap-Text $TextNext | Out-Null }
if (Tap-Text $TextAccentPlacement) {
    Capture-ScrollSeries "accent-placement" 2
    Invoke-Adb shell input keyevent 4
    Start-Sleep -Milliseconds 500
}
# Layout editor button is at the top of the Layout wizard step.
Start-Main
Tap-Text $TextNext | Out-Null
Tap-Text $TextNext | Out-Null
if (Tap-Text $TextLayoutEdit) {
    Capture-ScrollSeries "layout-editor" 2
    Invoke-Adb shell input keyevent 4
    Start-Sleep -Milliseconds 600
}

# Remote settings with both collapsed advanced subsections expanded.
Start-Main
for ($i = 0; $i -lt 6; $i++) { Tap-Text $TextNext | Out-Null }
Tap-Text $TextRemoteAuto | Out-Null
Tap-Text $TextRemoteOverrides | Out-Null
Capture-ScrollSeries "remote-expanded" 4

# Android/IME tools, diagnostics, backup/restore.
Start-Main
for ($i = 0; $i -lt 7; $i++) { Tap-Text $TextNext | Out-Null }
Capture-ScrollSeries "android-ime-settings" 3
Start-Main
for ($i = 0; $i -lt 7; $i++) { Tap-Text $TextNext | Out-Null }
if (Tap-Text $TextDiagnostics) {
    Capture-ScrollSeries "diagnostics" 3
    Invoke-Adb shell input keyevent 4
    Start-Sleep -Milliseconds 500
}
Start-Main
for ($i = 0; $i -lt 7; $i++) { Tap-Text $TextNext | Out-Null }
if (Tap-Text $TextBackupRestore) {
    Capture-ScrollSeries "backup-restore" 3
    Invoke-Adb shell input keyevent 4
}

Write-Host "SUI01 app/settings baseline captured at $CaptureDir"
