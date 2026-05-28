param(
    [ValidateRange(10, 30)]
    [int] $HangulSpecialColumnPercent = 17,
    [int] $HangulMainRegionRatio = 0,
    [ValidateSet(
        "default",
        "rounded-blue",
        "dark",
        "mint-compact",
        "flat",
        "ios-light",
        "ios-dark",
        "macos-light",
        "macos-dark",
        "android-light",
        "android-dark",
        "paper-mono"
    )]
    [string] $ThemeVariant = "default",
    [string] $ThemePresetId = "",
    [switch] $ShowNumberRow,
    [switch] $CaptureShiftActive,
    [switch] $ResetAppData
)

$ErrorActionPreference = "Stop"

if ($PSBoundParameters.ContainsKey("HangulMainRegionRatio")) {
    if ($HangulMainRegionRatio -ne 4 -and $HangulMainRegionRatio -ne 5) {
        throw "HangulMainRegionRatio must be 4 or 5."
    }
    $HangulSpecialColumnPercent = [int][Math]::Round(100 / ($HangulMainRegionRatio + 1))
}

. (Join-Path $PSScriptRoot "env.ps1")

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Adb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"
$Package = "com.superl3.s3keyboard"
$Activity = "$Package/.MainActivity"
$Ime = "$Package/.S3KeyboardService"
$Apk = Join-Path $Root "app\build\outputs\apk\debug\app-debug.apk"
$CaptureDir = Join-Path $Root "captures"

function Export-KeyboardCrop {
    param(
        [Parameter(Mandatory = $true)]
        [string] $SourcePath,
        [Parameter(Mandatory = $true)]
        [string] $DestinationPath,
        [int] $ExpectedHeightPx = 0
    )

    Add-Type -AssemblyName System.Drawing
    $bitmap = [System.Drawing.Bitmap]::FromFile($SourcePath)
    try {
        $width = [int]$bitmap.Width
        $height = [int]$bitmap.Height

        if ($ExpectedHeightPx -gt 0) {
            $cropHeight = [Math]::Min($height, [Math]::Max(1, $ExpectedHeightPx))
            $top = [Math]::Max(0, $height - $cropHeight)
        } else {
            $scanStart = [Math]::Max(0, [int]($height * 0.50))
            $sampleStep = 4
            $sampleCount = [Math]::Ceiling($width / $sampleStep)
            $top = $scanStart

            for ($y = $height - 1; $y -ge $scanStart; $y--) {
                $darkCount = 0
                for ($x = 0; $x -lt $width; $x += $sampleStep) {
                    $pixel = $bitmap.GetPixel($x, $y)
                    if ($pixel.R -lt 150 -and $pixel.G -lt 150 -and $pixel.B -lt 150) {
                        $darkCount++
                    }
                }

                if ($darkCount -gt ($sampleCount * 0.45)) {
                    $top = $y
                }
            }

            $top = [Math]::Max(0, $top - 2)
        }

        $crop = [System.Drawing.Rectangle]::new(0, [int]$top, $width, [int]($height - $top))
        $cropped = $bitmap.Clone($crop, $bitmap.PixelFormat)
        try {
            $cropped.Save($DestinationPath, [System.Drawing.Imaging.ImageFormat]::Png)
        } finally {
            $cropped.Dispose()
        }
    } finally {
        $bitmap.Dispose()
    }
}

function Test-ImeVisible {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Adb,
        [Parameter(Mandatory = $true)]
        [string[]] $AdbTarget
    )

    $dump = (& $Adb @AdbTarget shell dumpsys input_method | Out-String)
    return $dump -match "mInputShown=true"
}

function Ensure-DemoKeyboardVisible {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Adb,
        [Parameter(Mandatory = $true)]
        [string[]] $AdbTarget,
        [Parameter(Mandatory = $true)]
        [string] $Ime
    )

    for ($attempt = 0; $attempt -lt 3; $attempt++) {
        & $Adb @AdbTarget shell input tap 540 565 | Out-Null
        Start-Sleep -Milliseconds 500
        & $Adb @AdbTarget shell ime set $Ime | Out-Null
        Start-Sleep -Milliseconds 500
        & $Adb @AdbTarget shell input tap 540 565 | Out-Null
        Start-Sleep -Milliseconds 700
        if (Test-ImeVisible -Adb $Adb -AdbTarget $AdbTarget) {
            return
        }
    }
}

function Get-ThemeExtras {
    param([string] $Variant)

    switch ($Variant) {
        "ios-light" {
            return @{
                KeyIdle = "FBFBFD"
                KeyPressed = "D6D8DD"
                KeyboardBackground = "D1D5DB"
                Accent = "111827"
                Secondary = "707780"
                FunctionKey = "EEF0F4"
                PrimaryFunctionKey = "E4E7ED"
                AccentKey = "EAF2FF"
                Border = "C2C7D0"
                DepthColor = "C2C7D0"
                CustomDepthColorEnabled = "false"
                FontFamily = "noto_sans_kr"
                Roundness = 8
                Gap = 5
                DepthEnabled = "false"
                Depth = 0
            }
        }
        "ios-dark" {
            return @{
                KeyIdle = "2B2D31"
                KeyPressed = "5A5E66"
                KeyboardBackground = "1D1F23"
                Accent = "F8FAFC"
                Secondary = "B8BEC8"
                FunctionKey = "3A3D43"
                PrimaryFunctionKey = "464A52"
                AccentKey = "263B58"
                Border = "202328"
                DepthColor = "202328"
                CustomDepthColorEnabled = "false"
                FontFamily = "noto_sans_kr"
                Roundness = 8
                Gap = 5
                DepthEnabled = "false"
                Depth = 0
            }
        }
        "macos-light" {
            return @{
                KeyIdle = "F6F7F9"
                KeyPressed = "C9D2DC"
                KeyboardBackground = "EEF1F4"
                Accent = "1F2937"
                Secondary = "667085"
                FunctionKey = "E7EAEE"
                PrimaryFunctionKey = "DCE2EA"
                AccentKey = "E5F0FF"
                Border = "B8C0CA"
                DepthColor = "B8C0CA"
                CustomDepthColorEnabled = "true"
                FontFamily = "noto_sans_kr"
                Roundness = 6
                Gap = 5
                DepthEnabled = "true"
                Depth = 2
            }
        }
        "macos-dark" {
            return @{
                KeyIdle = "30343A"
                KeyPressed = "596271"
                KeyboardBackground = "20242A"
                Accent = "F4F7FA"
                Secondary = "AEB7C4"
                FunctionKey = "3B414A"
                PrimaryFunctionKey = "454D58"
                AccentKey = "31475F"
                Border = "15191F"
                DepthColor = "15191F"
                CustomDepthColorEnabled = "true"
                FontFamily = "noto_sans_kr"
                Roundness = 6
                Gap = 5
                DepthEnabled = "true"
                Depth = 2
            }
        }
        "android-light" {
            return @{
                KeyIdle = "FFFBFE"
                KeyPressed = "D0C4DB"
                KeyboardBackground = "F3EDF7"
                Accent = "1D1B20"
                Secondary = "625B71"
                FunctionKey = "ECE6F0"
                PrimaryFunctionKey = "E1DCE8"
                AccentKey = "EADDFF"
                Border = "CAC4D0"
                DepthColor = "CAC4D0"
                CustomDepthColorEnabled = "false"
                FontFamily = "noto_sans_kr"
                Roundness = 12
                Gap = 5
                DepthEnabled = "false"
                Depth = 0
            }
        }
        "android-dark" {
            return @{
                KeyIdle = "211F26"
                KeyPressed = "4A4458"
                KeyboardBackground = "141218"
                Accent = "E6E1E5"
                Secondary = "CAC4D0"
                FunctionKey = "2B2930"
                PrimaryFunctionKey = "36323D"
                AccentKey = "3A3151"
                Border = "49454F"
                DepthColor = "49454F"
                CustomDepthColorEnabled = "false"
                FontFamily = "noto_sans_kr"
                Roundness = 12
                Gap = 5
                DepthEnabled = "false"
                Depth = 0
            }
        }
        "paper-mono" {
            return @{
                KeyIdle = "FAFAFA"
                KeyPressed = "DADADA"
                KeyboardBackground = "F4F4F4"
                Accent = "111111"
                Secondary = "777777"
                FunctionKey = "EFEFEF"
                PrimaryFunctionKey = "E5E5E5"
                AccentKey = "F1F6FF"
                Border = "D0D0D0"
                DepthColor = "D0D0D0"
                CustomDepthColorEnabled = "false"
                FontFamily = "d2coding"
                Roundness = 3
                Gap = 6
                DepthEnabled = "false"
                Depth = 0
            }
        }
        "rounded-blue" {
            return @{
                KeyIdle = "F4F7FB"
                KeyPressed = "D7E5FF"
                KeyboardBackground = "DDE4ED"
                Accent = "1D4ED8"
                Secondary = "5D6675"
                FunctionKey = "E8F0FF"
                PrimaryFunctionKey = "D4E4FF"
                AccentKey = "EAF1FF"
                Border = "5D6675"
                DepthColor = "5D6675"
                CustomDepthColorEnabled = "false"
                FontFamily = "noto_sans_kr"
                Roundness = 10
                Gap = 6
                DepthEnabled = "true"
                Depth = 4
            }
        }
        "dark" {
            return @{
                KeyIdle = "252A31"
                KeyPressed = "3E4652"
                KeyboardBackground = "15181D"
                Accent = "F7F2E8"
                Secondary = "B7C0CC"
                FunctionKey = "303844"
                PrimaryFunctionKey = "3A4350"
                AccentKey = "263F64"
                Border = "596373"
                DepthColor = "596373"
                CustomDepthColorEnabled = "false"
                FontFamily = "noto_sans_kr"
                Roundness = 8
                Gap = 5
                DepthEnabled = "true"
                Depth = 3
            }
        }
        "mint-compact" {
            return @{
                KeyIdle = "FAFBF7"
                KeyPressed = "CDEBDD"
                KeyboardBackground = "D9E5DD"
                Accent = "155C4A"
                Secondary = "66716B"
                FunctionKey = "E8F3EC"
                PrimaryFunctionKey = "D8EFE2"
                AccentKey = "DFF2EA"
                Border = "66716B"
                DepthColor = "66716B"
                CustomDepthColorEnabled = "false"
                FontFamily = "noto_sans_kr"
                Roundness = 4
                Gap = 4
                DepthEnabled = "true"
                Depth = 2
            }
        }
        "flat" {
            return @{
                KeyIdle = "F8F8F8"
                KeyPressed = "D6D6D6"
                KeyboardBackground = "ECECEC"
                Accent = "232323"
                Secondary = "696969"
                FunctionKey = "E7EAF0"
                PrimaryFunctionKey = "DDE3EC"
                AccentKey = "EAF1FF"
                Border = "696969"
                DepthColor = "696969"
                CustomDepthColorEnabled = "false"
                FontFamily = "default"
                Roundness = 0
                Gap = 5
                DepthEnabled = "false"
                Depth = 0
            }
        }
        default {
            return @{
                KeyIdle = "F8F8F8"
                KeyPressed = "B2B2B2"
                KeyboardBackground = "EBEBEB"
                Accent = "232323"
                Secondary = "696969"
                FunctionKey = "E7EAF0"
                PrimaryFunctionKey = "DDE3EC"
                AccentKey = "EAF1FF"
                Border = "696969"
                DepthColor = "696969"
                CustomDepthColorEnabled = "false"
                FontFamily = "default"
                Roundness = 0
                Gap = 5
                DepthEnabled = "true"
                Depth = 3
            }
        }
    }
}

& (Join-Path $PSScriptRoot "build-debug.ps1")

$ApkHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $Apk).Hash.Substring(0, 12)
$ThemePresetMode = -not [string]::IsNullOrWhiteSpace($ThemePresetId)
$SafeThemeName = if ($ThemePresetMode) {
    $ThemePresetId -replace "[^A-Za-z0-9_-]", "-"
} else {
    $ThemeVariant -replace "[^A-Za-z0-9_-]", "-"
}
$RunId = "$(Get-Date -Format 'yyyyMMdd-HHmmss-fff')-sp$HangulSpecialColumnPercent-$SafeThemeName-$ApkHash"
$RunCaptureDir = Join-Path $CaptureDir $RunId
$DeviceCapture = "/sdcard/hangul_gesture_ime_demo_$RunId.png"
$LocalCapture = Join-Path $CaptureDir "hangul_gesture_ime_demo.png"
$RunLocalCapture = Join-Path $RunCaptureDir "hangul_gesture_ime_demo.png"
$DeviceEnglishCapture = "/sdcard/hangul_gesture_ime_english_demo_$RunId.png"
$LocalEnglishCapture = Join-Path $CaptureDir "hangul_gesture_ime_english_demo.png"
$RunLocalEnglishCapture = Join-Path $RunCaptureDir "hangul_gesture_ime_english_demo.png"
$LocalKeyboardOnlyCapture = Join-Path $CaptureDir "hangul_gesture_ime_keyboard_only.png"
$RunLocalKeyboardOnlyCapture = Join-Path $RunCaptureDir "hangul_gesture_ime_keyboard_only.png"
$LocalEnglishKeyboardOnlyCapture = Join-Path $CaptureDir "hangul_gesture_ime_english_keyboard_only.png"
$RunLocalEnglishKeyboardOnlyCapture = Join-Path $RunCaptureDir "hangul_gesture_ime_english_keyboard_only.png"
$DeviceShiftCapture = "/sdcard/hangul_gesture_ime_shift_active_$RunId.png"
$LocalShiftCapture = Join-Path $CaptureDir "hangul_gesture_ime_shift_active.png"
$RunLocalShiftCapture = Join-Path $RunCaptureDir "hangul_gesture_ime_shift_active.png"
$LocalShiftKeyboardOnlyCapture = Join-Path $CaptureDir "hangul_gesture_ime_shift_active_keyboard_only.png"
$RunLocalShiftKeyboardOnlyCapture = Join-Path $RunCaptureDir "hangul_gesture_ime_shift_active_keyboard_only.png"
$Theme = Get-ThemeExtras -Variant $ThemeVariant
$KeyIdleColor = $Theme["KeyIdle"]
$KeyPressedColor = $Theme["KeyPressed"]
$KeyboardBackgroundColor = $Theme["KeyboardBackground"]
$AccentColor = $Theme["Accent"]
$SecondaryColor = $Theme["Secondary"]
$FunctionKeyColor = $Theme["FunctionKey"]
$PrimaryFunctionKeyColor = $Theme["PrimaryFunctionKey"]
$AccentKeyColor = $Theme["AccentKey"]
$BorderColor = $Theme["Border"]
$DepthColor = $Theme["DepthColor"]
$CustomDepthColorEnabled = $Theme["CustomDepthColorEnabled"]
$FontFamily = $Theme["FontFamily"]
$KeyRoundness = $Theme["Roundness"]
$KeyGap = $Theme["Gap"]
$KeyDepthEnabled = $Theme["DepthEnabled"]
$KeyDepth = $Theme["Depth"]
$ShowHangulNumberRowValue = if ($ShowNumberRow) { "true" } else { "false" }
$ShowEnglishNumberRowValue = "true"

& (Join-Path $PSScriptRoot "setup-emulator.ps1")
& (Join-Path $PSScriptRoot "launch-emulator.ps1")

New-Item -ItemType Directory -Force -Path $CaptureDir | Out-Null
New-Item -ItemType Directory -Force -Path $RunCaptureDir | Out-Null
$Device = (& $Adb devices | Select-String -Pattern "emulator-\d+\s+device" | Select-Object -First 1).ToString().Split()[0]
$AdbTarget = @("-s", $Device)
$DensityText = (& $Adb @AdbTarget shell wm density | Out-String)
$DeviceDensity = 440
if ($DensityText -match "(\d+)") {
    $DeviceDensity = [int]$Matches[1]
}
$HangulKeyboardCropDp = 286
if ($ShowHangulNumberRowValue -eq "true") {
    $HangulKeyboardCropDp += 42
}
$EnglishKeyboardCropDp = 286
if ($ShowEnglishNumberRowValue -eq "true") {
    $EnglishKeyboardCropDp += 42
}
$HangulKeyboardCropHeightPx = [int][Math]::Round($HangulKeyboardCropDp * $DeviceDensity / 160.0)
$EnglishKeyboardCropHeightPx = [int][Math]::Round($EnglishKeyboardCropDp * $DeviceDensity / 160.0)

Write-Host "Installing APK"
& $Adb @AdbTarget shell am force-stop $Package | Out-Null
& $Adb @AdbTarget install -r $Apk | Out-Host
if ($ResetAppData) {
    Write-Host "Resetting app data"
    & $Adb @AdbTarget shell pm clear $Package | Out-Host
}
& $Adb @AdbTarget shell am force-stop $Package | Out-Null

Write-Host "Enabling and selecting IME"
& $Adb @AdbTarget shell ime enable $Ime | Out-Host
& $Adb @AdbTarget shell ime set $Ime | Out-Host
& $Adb @AdbTarget shell settings put secure show_ime_with_hard_keyboard 1 | Out-Host

Write-Host "Launching demo activity"
& $Adb @AdbTarget shell am force-stop $Package | Out-Null
$StartArgs = @(
    "shell", "am", "start", "-n", $Activity,
    "--ei", "hangul_special_column_percent", "$HangulSpecialColumnPercent",
    "--ez", "show_hangul_slide_hints", "true",
    "--ez", "show_english_slide_hints", "true",
    "--ez", "show_beginner_tooltip_preview", "true",
    "--ez", "show_hangul_number_row", "$ShowHangulNumberRowValue",
    "--ez", "show_english_number_row", "$ShowEnglishNumberRowValue",
    "--ez", "demo_settings", "true",
    "--ez", "demo_show_keyboard", "true"
)
if ($ThemePresetMode) {
    $StartArgs += @("--es", "theme_preset_id", $ThemePresetId)
} else {
    $StartArgs += @(
        "--es", "key_idle_color", $KeyIdleColor,
        "--es", "key_pressed_color", $KeyPressedColor,
        "--es", "keyboard_background_color", $KeyboardBackgroundColor,
        "--es", "accent_color", $AccentColor,
        "--es", "secondary_color", $SecondaryColor,
        "--es", "function_key_color", $FunctionKeyColor,
        "--es", "primary_function_key_color", $PrimaryFunctionKeyColor,
        "--es", "accent_key_color", $AccentKeyColor,
        "--es", "border_color", $BorderColor,
        "--ei", "key_roundness_dp", "$KeyRoundness",
        "--ei", "key_gap_dp", "$KeyGap",
        "--ez", "key_depth_enabled", "$KeyDepthEnabled",
        "--ei", "key_depth_dp", "$KeyDepth",
        "--ez", "custom_depth_color_enabled", "$CustomDepthColorEnabled",
        "--es", "depth_color", $DepthColor,
        "--es", "font_family", $FontFamily
    )
}
& $Adb @AdbTarget @StartArgs | Out-Host
Start-Sleep -Seconds 2
& $Adb @AdbTarget shell ime set $Ime | Out-Host

# The Activity also requests focus via demo_show_keyboard; keep a tap fallback
# for emulator images that ignore the first soft-input request.
& $Adb @AdbTarget shell input tap 540 565 | Out-Null
Start-Sleep -Seconds 2
Ensure-DemoKeyboardVisible -Adb $Adb -AdbTarget $AdbTarget -Ime $Ime

Write-Host "Capturing Hangul keyboard screenshot"
& $Adb @AdbTarget shell screencap -p $DeviceCapture | Out-Null
& $Adb @AdbTarget pull $DeviceCapture $RunLocalCapture | Out-Host
& $Adb @AdbTarget shell rm $DeviceCapture | Out-Null
Copy-Item -LiteralPath $RunLocalCapture -Destination $LocalCapture -Force
Export-KeyboardCrop -SourcePath $RunLocalCapture -DestinationPath $RunLocalKeyboardOnlyCapture -ExpectedHeightPx $HangulKeyboardCropHeightPx
Copy-Item -LiteralPath $RunLocalKeyboardOnlyCapture -Destination $LocalKeyboardOnlyCapture -Force

Write-Host "Switching to English keyboard and capturing screenshot"
# Bottom order is Options / Reserved / Space / Language / Enter in the default
# right-hand layout, so the language key sits near the right side.
& $Adb @AdbTarget shell input tap 865 2230 | Out-Null
Start-Sleep -Seconds 1
& $Adb @AdbTarget shell screencap -p $DeviceEnglishCapture | Out-Null
& $Adb @AdbTarget pull $DeviceEnglishCapture $RunLocalEnglishCapture | Out-Host
& $Adb @AdbTarget shell rm $DeviceEnglishCapture | Out-Null
Copy-Item -LiteralPath $RunLocalEnglishCapture -Destination $LocalEnglishCapture -Force
Export-KeyboardCrop -SourcePath $RunLocalEnglishCapture -DestinationPath $RunLocalEnglishKeyboardOnlyCapture -ExpectedHeightPx $EnglishKeyboardCropHeightPx
Copy-Item -LiteralPath $RunLocalEnglishKeyboardOnlyCapture -Destination $LocalEnglishKeyboardOnlyCapture -Force

if ($CaptureShiftActive) {
    Write-Host "Activating Shift and capturing screenshot"
    & $Adb @AdbTarget shell input tap 80 2100 | Out-Null
    Start-Sleep -Milliseconds 700
    & $Adb @AdbTarget shell screencap -p $DeviceShiftCapture | Out-Null
    & $Adb @AdbTarget pull $DeviceShiftCapture $RunLocalShiftCapture | Out-Host
    & $Adb @AdbTarget shell rm $DeviceShiftCapture | Out-Null
    Copy-Item -LiteralPath $RunLocalShiftCapture -Destination $LocalShiftCapture -Force
    Export-KeyboardCrop -SourcePath $RunLocalShiftCapture -DestinationPath $RunLocalShiftKeyboardOnlyCapture -ExpectedHeightPx $EnglishKeyboardCropHeightPx
    Copy-Item -LiteralPath $RunLocalShiftKeyboardOnlyCapture -Destination $LocalShiftKeyboardOnlyCapture -Force
}

Write-Host "Capture run id: $RunId"
Write-Host "Hangul special column percent: $HangulSpecialColumnPercent%"
Write-Host "Theme variant: $ThemeVariant"
if ($ThemePresetMode) {
    Write-Host "Theme preset id: $ThemePresetId"
}
Write-Host "Hangul number row: $ShowHangulNumberRowValue"
Write-Host "English number row: $ShowEnglishNumberRowValue"
Write-Host "Hangul keyboard crop height: $HangulKeyboardCropHeightPx px"
Write-Host "English keyboard crop height: $EnglishKeyboardCropHeightPx px"
Write-Host "Hangul demo screenshot: $RunLocalCapture"
Write-Host "English demo screenshot: $RunLocalEnglishCapture"
Write-Host "Hangul keyboard-only screenshot: $RunLocalKeyboardOnlyCapture"
Write-Host "English keyboard-only screenshot: $RunLocalEnglishKeyboardOnlyCapture"
if ($CaptureShiftActive) {
    Write-Host "Shift-active keyboard-only screenshot: $RunLocalShiftKeyboardOnlyCapture"
}
Write-Host "Latest Hangul keyboard-only screenshot: $LocalKeyboardOnlyCapture"
Write-Host "Latest English keyboard-only screenshot: $LocalEnglishKeyboardOnlyCapture"
if ($CaptureShiftActive) {
    Write-Host "Latest shift-active keyboard-only screenshot: $LocalShiftKeyboardOnlyCapture"
}
