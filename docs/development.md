# Development Notes

This project is a Gradle Android IME app. The canonical setup path is documented in
the root `README.md`, and these notes cover the direct local workflow for an
already-installed Android SDK/JDK.

## Local Environment

On Windows, set these variables before running Gradle if `local.properties` is not
present:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\openjdk\jdk-21.0.8'
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
```

Alternatively, create an untracked `local.properties` file:

```properties
sdk.dir=C\:\\Users\\bug95\\AppData\\Local\\Android\\Sdk
```

## Build And Test

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

The debug APK is written to:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Wireless ADB Install

Pairing and connect ports can differ on Android wireless debugging. Pair with the
pairing port first, then connect to the `_adb-tls-connect` port shown by Android
or discovered by `adb mdns services`.

```powershell
adb pair <device-ip>:<pairing-port> <pairing-code>
adb connect <device-ip>:<connect-port>
adb -s <device-ip>:<connect-port> install -r app\build\outputs\apk\debug\app-debug.apk
adb -s <device-ip>:<connect-port> shell ime enable com.academic.hangulgestureime/.HangulGestureImeService
adb -s <device-ip>:<connect-port> shell ime set com.academic.hangulgestureime/.HangulGestureImeService
```

If a previously-installed APK was signed with a different key, Android will reject
an update with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstall the package first:

```powershell
adb -s <device-ip>:<connect-port> uninstall com.academic.hangulgestureime
```

## Current Input Feel Architecture

- `KeyboardFeedback` owns haptic queuing and emits short vibrator pulses in order.
- `KeyboardPreferences` stores haptic tick duration and gap independently from the
  broader immutable `KeyboardSettings` object.
- `HangulKeyboardView` keeps the preview strip inside the measured keyboard
  height, so preview space does not create a transparent area over the app UI.
- `TouchBiasStore` learns from immediate deletes. Tap mistakes adjust touch
  center bias; deleted slide outputs gradually raise the gesture threshold within
  a small cap.
