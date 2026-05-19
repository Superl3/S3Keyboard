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

The repo also includes wrapper scripts for the common path:

```powershell
.\scripts\check.ps1
.\scripts\build-debug.ps1
.\scripts\install-debug.ps1 -Serial <device-ip>:<connect-port>
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

## Theme Architecture

- `KeyboardThemePreset` is the source of truth for built-in themes. Keep GMK-style
  themes as deterministic JSON presets rather than image-matched screenshots.
- Theme colors are split into global key colors plus per-key overrides:
  - `keyTextColorOverrides` changes legends, hint text, and icon foregrounds.
  - `keyBackgroundColorOverrides` changes individual key backgrounds. Supported
    keys include `tap:<value>`, `label:<value>`, `space`, `enter`, `backspace`,
    `shift`, `language`, `options`, `reserved`, and `icon:<id>`.
- Both override maps are imported through `KeyboardThemeJson`. Runtime storage
  normalizes background overrides with a `background:` prefix so the renderer can
  keep one immutable override map without mixing foreground and background lookups.
- `LegendStylePreset.DOTS` hides slide hints and draws the main legend as a
  Canvas circle instead of using a text bullet. Dots themes should provide colorful
  `keyTextColorOverrides`; the dot renderer uses those colors as the dot fill.
- `ThemeSelectorActivity` persists the applied preset/custom theme id through
  `KeyboardPreferences.SELECTED_THEME_ID`. Avoid relying on card index alone
  because user themes can be added or removed.

## Settings UI Styling

Settings screens must follow the phone light/dark mode, not the active keyboard
skin. Reuse `SettingsUiPalette`, `SettingsArrayAdapter`, and `SettingsViewStyler`
for text, spinner rows, buttons, checkboxes/radio buttons, and numeric inputs.
This prevents Android default widgets from leaving black text or black checkbox
tints on dark system UI.
