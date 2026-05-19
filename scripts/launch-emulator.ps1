param(
    [string] $AvdName = "hangul_gesture_demo",
    [int] $BootTimeoutSeconds = 240
)

$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$Emulator = Join-Path $env:ANDROID_SDK_ROOT "emulator\emulator.exe"
$Adb = Join-Path $env:ANDROID_SDK_ROOT "platform-tools\adb.exe"

if (-not (Test-Path $Emulator)) {
    throw "emulator.exe not found. Run scripts\setup-emulator.ps1 first."
}

$running = & $Adb devices | Select-String -Pattern "emulator-\d+\s+device"
if ($running) {
    Write-Host "An emulator is already running"
} else {
    Write-Host "Launching emulator $AvdName"
    Start-Process -FilePath $Emulator -WindowStyle Hidden -ArgumentList @(
        "-avd", $AvdName,
        "-no-window",
        "-no-audio",
        "-no-boot-anim",
        "-gpu", "swiftshader_indirect",
        "-netdelay", "none",
        "-netspeed", "full"
    ) | Out-Null
}

$deadline = (Get-Date).AddSeconds($BootTimeoutSeconds)
do {
    Start-Sleep -Seconds 3
    $device = (& $Adb devices | Select-String -Pattern "emulator-\d+\s+device" | Select-Object -First 1)
    if ($device) {
        $serial = ($device.ToString() -split "\s+")[0]
        $bootCompleted = (& $Adb -s $serial shell getprop sys.boot_completed 2>$null).Trim()
        if ($bootCompleted -eq "1") {
            & $Adb -s $serial shell input keyevent 82 | Out-Null
            Write-Host "Emulator booted"
            exit 0
        }
    }
} while ((Get-Date) -lt $deadline)

throw "Timed out waiting for emulator boot"
