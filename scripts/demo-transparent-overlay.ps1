param(
    [string] $AvdName = "hangul_gesture_demo",
    [string] $OutputDirectory = "",
    [ValidateSet("translucent_keys", "extreme_floating")]
    [string] $OverlayStyle = "extreme_floating"
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Adb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
$Package = "com.superl3.s3keyboard"
$Ime = "$Package/.S3KeyboardService"
$Activity = "$Package/.MainActivity"

if ([string]::IsNullOrWhiteSpace($OutputDirectory)) {
    $OutputDirectory = Join-Path $Root "artifacts\transparent-overlay"
}
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

& (Join-Path $PSScriptRoot "build-debug.ps1")
& (Join-Path $PSScriptRoot "launch-emulator.ps1") -AvdName $AvdName

$deviceLine = & $Adb devices |
    Select-String -Pattern "emulator-\d+\s+device" |
    Select-Object -First 1
if (-not $deviceLine) {
    throw "No running Android emulator was found"
}
$Serial = ($deviceLine.ToString() -split "\s+")[0]
$Target = @("-s", $Serial)
$Apk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"

function Select-TargetIme {
    for ($attempt = 1; $attempt -le 4; $attempt++) {
        & $Adb @Target shell ime enable $Ime | Out-Null
        & $Adb @Target shell ime set $Ime | Out-Null
        Start-Sleep -Milliseconds 700
        $currentIme = (& $Adb @Target shell settings get secure default_input_method).Trim()
        if ($currentIme -eq $Ime) {
            return
        }
    }
    throw "Could not select target IME. Current=$currentIme"
}

& $Adb @Target install -r $Apk | Out-Host
& $Adb @Target shell pm clear $Package | Out-Host
Select-TargetIme
& $Adb @Target shell settings put secure show_ime_with_hard_keyboard 1 | Out-Null
& $Adb @Target logcat -c

$CommonArgs = @(
    "shell", "am", "start", "-S", "-n", $Activity,
    "--ez", "demo_settings", "true",
    "--ez", "demo_overlay_testbed", "true",
    "--ez", "demo_watch_radial_input", "false",
    "--es", "demo_overlay_style", $OverlayStyle
)
& $Adb @Target @CommonArgs --ez demo_show_keyboard false | Out-Host
Start-Sleep -Seconds 2
Select-TargetIme

$BeforeDevice = "/sdcard/transparent-overlay-before.png"
$AfterDevice = "/sdcard/transparent-overlay-after.png"
$BeforeCapture = Join-Path $OutputDirectory "transparent-overlay-before.png"
$AfterCapture = Join-Path $OutputDirectory "transparent-overlay-after.png"
$HierarchyDevice = "/sdcard/transparent-overlay.xml"
$HierarchyFile = Join-Path $OutputDirectory "transparent-overlay.xml"

& $Adb @Target shell screencap -p $BeforeDevice | Out-Null
& $Adb @Target pull $BeforeDevice $BeforeCapture | Out-Null
& $Adb @Target shell uiautomator dump $HierarchyDevice | Out-Host
& $Adb @Target pull $HierarchyDevice $HierarchyFile | Out-Null

$hierarchyText = [System.Text.Encoding]::UTF8.GetString(
    [System.IO.File]::ReadAllBytes($HierarchyFile))
[xml] $hierarchy = $hierarchyText
$textBox = $hierarchy.SelectSingleNode("//*[@content-desc='overlay_test_textbox']")
if (-not $textBox) {
    throw "Overlay test TextBox was not found"
}
$initialEditorText = [string] $textBox.text
if ($textBox.bounds -notmatch "\[(\d+),(\d+)\]\[(\d+),(\d+)\]") {
    throw "Could not parse overlay test TextBox bounds: $($textBox.bounds)"
}
$tapX = [int]((([int] $Matches[1]) + ([int] $Matches[3])) / 2)
$tapY = [int]((([int] $Matches[2]) + ([int] $Matches[4])) / 2)
& $Adb @Target shell input tap $tapX $tapY | Out-Null
Start-Sleep -Seconds 7

& $Adb @Target shell screencap -p $AfterDevice | Out-Null
& $Adb @Target pull $AfterDevice $AfterCapture | Out-Null

$geometryLines = @(& $Adb @Target logcat -d -s "OverlayImeTestbed:I" "*:S") |
    Where-Object { $_ -match "viewport=(\d+)x(\d+) textbox=(\d+),(\d+),(\d+)x(\d+)" }
if ($geometryLines.Count -eq 0) {
    throw "The overlay testbed did not report geometry"
}

$first = $geometryLines[0]
$last = $geometryLines[$geometryLines.Count - 1]
$geometryPattern = "viewport=(\d+)x(\d+) textbox=(\d+),(\d+),(\d+)x(\d+)"
$first -match $geometryPattern | Out-Null
$beforeGeometry = $Matches[1..6] -join ","
$last -match $geometryPattern | Out-Null
$afterGeometry = $Matches[1..6] -join ","
if ($beforeGeometry -ne $afterGeometry) {
    throw "Host layout moved when IME opened. Before=$beforeGeometry After=$afterGeometry"
}

$imeState = (& $Adb @Target shell dumpsys input_method) -join "`n"
if ($imeState -notmatch "mInputShown=true|mInputViewShown=true") {
    throw "IME input view is not shown"
}
if ($imeState -notmatch [regex]::Escape("mCurMethodId=$Ime")) {
    throw "The visible IME is not S3Keyboard"
}
if ($imeState -notmatch "mFullscreenMode=true") {
    throw "The S3Keyboard overlay is not in fullscreen mode"
}
if ($imeState -notmatch "mExtractViewHidden=true") {
    throw "Android's extracted editor is still visible"
}
if ($imeState -notmatch "contentTopInsets=(\d+) visibleTopInsets=(\d+)") {
    throw "Could not read the IME occupied-area insets"
}
$contentTopInsets = [int] $Matches[1]
$visibleTopInsets = [int] $Matches[2]
if ($contentTopInsets -le 0 -or $visibleTopInsets -ne $contentTopInsets) {
    throw "Transparent IME still reports occupied space. content=$contentTopInsets visible=$visibleTopInsets"
}

$Report = Join-Path $OutputDirectory "transparent-overlay-report.txt"
$WatchStageOneDevice = "/sdcard/watch-radial-ime-stage-1.png"
$WatchStageTwoDevice = "/sdcard/watch-radial-ime-stage-2.png"
$WatchCommittedDevice = "/sdcard/watch-radial-ime-committed.png"
$WatchStageOneCapture = Join-Path $OutputDirectory "watch-radial-ime-stage-1.png"
$WatchStageTwoCapture = Join-Path $OutputDirectory "watch-radial-ime-stage-2.png"
$WatchCommittedCapture = Join-Path $OutputDirectory "watch-radial-ime-committed.png"

& $Adb @Target logcat -c
& $Adb @Target shell am start -S -n $Activity `
    --ez demo_settings true `
    --ez demo_overlay_testbed true `
    --ez demo_show_keyboard true `
    --ez demo_watch_radial_input true | Out-Host
Start-Sleep -Seconds 2
Select-TargetIme
& $Adb @Target shell input tap $tapX $tapY | Out-Null
Start-Sleep -Seconds 3

$watchGeometryLines = @()
for ($attempt = 1; $attempt -le 10 -and $watchGeometryLines.Count -eq 0; $attempt++) {
    $watchGeometryLines = @(& $Adb @Target logcat -d -s "WatchRadialIme:I" "*:S") |
        Where-Object { $_ -match "bounds=(\d+),(\d+),(\d+),(\d+) center=(\d+),(\d+) radius=(\d+)" }
    if ($watchGeometryLines.Count -eq 0) {
        Start-Sleep -Milliseconds 750
    }
}
if ($watchGeometryLines.Count -eq 0) {
    throw "The production watch radial IME surface did not report geometry"
}
$watchGeometry = $watchGeometryLines[$watchGeometryLines.Count - 1]
$watchGeometry -match "bounds=(\d+),(\d+),(\d+),(\d+) center=(\d+),(\d+) radius=(\d+)" | Out-Null
$watchLeft = [int] $Matches[1]
$watchTop = [int] $Matches[2]
$watchCenterX = $watchLeft + [int] $Matches[5]
$watchCenterY = $watchTop + [int] $Matches[6]
$watchRadius = [int] $Matches[7]
$watchTopKeyY = [int] ($watchCenterY - $watchRadius * 0.76)

& $Adb @Target shell screencap -p $WatchStageOneDevice | Out-Null
& $Adb @Target pull $WatchStageOneDevice $WatchStageOneCapture | Out-Null
& $Adb @Target shell input tap $watchCenterX $watchTopKeyY | Out-Null
Start-Sleep -Milliseconds 500
& $Adb @Target shell screencap -p $WatchStageTwoDevice | Out-Null
& $Adb @Target pull $WatchStageTwoDevice $WatchStageTwoCapture | Out-Null
& $Adb @Target shell input tap $watchCenterX $watchCenterY | Out-Null
Start-Sleep -Milliseconds 800
& $Adb @Target shell screencap -p $WatchCommittedDevice | Out-Null
& $Adb @Target pull $WatchCommittedDevice $WatchCommittedCapture | Out-Null

& $Adb @Target shell uiautomator dump $HierarchyDevice | Out-Host
& $Adb @Target pull $HierarchyDevice $HierarchyFile | Out-Null
$committedHierarchyText = [System.Text.Encoding]::UTF8.GetString(
    [System.IO.File]::ReadAllBytes($HierarchyFile))
[xml] $committedHierarchy = $committedHierarchyText
$committedTextBox = $committedHierarchy.SelectSingleNode("//*[@content-desc='overlay_test_textbox']")
if (-not $committedTextBox -or [string] $committedTextBox.text -eq $initialEditorText) {
    throw "Watch radial IME did not change the host editor after committing a consonant"
}

@(
    "serial=$Serial"
    "overlayStyle=$OverlayStyle"
    "geometry=$afterGeometry"
    "inputViewShown=true"
    "fullscreen=true"
    "extractViewHidden=true"
    "contentTopInsets=$contentTopInsets"
    "visibleTopInsets=$visibleTopInsets"
    "before=$BeforeCapture"
    "after=$AfterCapture"
    "watchRadialInput=true"
    "watchStage1=$WatchStageOneCapture"
    "watchStage2=$WatchStageTwoCapture"
    "watchCommitted=$WatchCommittedCapture"
) | Set-Content -LiteralPath $Report -Encoding UTF8

Write-Host "Transparent overlay test passed"
Write-Host "Host geometry remained: $afterGeometry"
Write-Host "Screenshot: $AfterCapture"
Write-Host "Watch radial IME stage 1: $WatchStageOneCapture"
Write-Host "Watch radial IME stage 2: $WatchStageTwoCapture"
Write-Host "Watch radial IME committed: $WatchCommittedCapture"
