$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "env.ps1")

$AvdName = "hangul_gesture_demo"
$SystemImage = "system-images;android-35;google_apis;x86_64"
$SdkManager = Join-Path $env:ANDROID_SDK_ROOT "cmdline-tools\latest\bin\sdkmanager.bat"
$AvdManager = Join-Path $env:ANDROID_SDK_ROOT "cmdline-tools\latest\bin\avdmanager.bat"

New-Item -ItemType Directory -Force -Path $env:ANDROID_USER_HOME, $env:ANDROID_AVD_HOME | Out-Null

Write-Host "Installing emulator packages if needed"
(1..80 | ForEach-Object { "y" }) | & $SdkManager --sdk_root=$env:ANDROID_SDK_ROOT "emulator" $SystemImage | Out-Host

$avdDirectory = Join-Path $env:ANDROID_AVD_HOME "$AvdName.avd"
if (-not (Test-Path $avdDirectory)) {
    Write-Host "Creating AVD $AvdName"
    "no" | & $AvdManager create avd --force --name $AvdName --package $SystemImage --device "pixel_5" | Out-Host
} else {
    Write-Host "AVD $AvdName already exists"
}

$configPath = Join-Path $env:ANDROID_AVD_HOME "$AvdName.avd\config.ini"
if (Test-Path $configPath) {
    $config = Get-Content -LiteralPath $configPath
    $desired = @{
        "hw.keyboard" = "no"
        "showDeviceFrame" = "no"
        "skin.dynamic" = "yes"
        "disk.dataPartition.size" = "2048M"
    }

    foreach ($entry in $desired.GetEnumerator()) {
        $pattern = "^$([regex]::Escape($entry.Key))="
        if ($config -match $pattern) {
            $config = $config | ForEach-Object {
                if ($_ -match $pattern) { "$($entry.Key)=$($entry.Value)" } else { $_ }
            }
        } else {
            $config += "$($entry.Key)=$($entry.Value)"
        }
    }
    Set-Content -LiteralPath $configPath -Encoding ASCII -Value $config
}

Write-Host "Emulator setup complete: $AvdName"
