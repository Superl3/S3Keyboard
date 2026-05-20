# New Dingul Research Prototype

This workspace contains a clean-room Android input method prototype for studying a Korean gesture-keyboard UX. It is built from the described interaction model and screenshot, not from proprietary APK code or assets.

## Setup

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\install-android-env.ps1
```

The setup script installs portable tools under `.android-tools/`:

- Temurin JDK 17
- Android SDK command-line tools
- Android platform tools
- Android 35 platform and build tools
- Gradle 8.10.2 wrapper

## Build

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\build-debug.ps1
```

The debug APK is produced at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

For a closed-beta release build, provide signing properties outside source control and run:

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\build-release.ps1
```

Expected Gradle properties:

```text
HANGUL_IME_KEYSTORE=C:\path\to\closed-beta.jks
HANGUL_IME_KEYSTORE_PASSWORD=...
HANGUL_IME_KEY_ALIAS=...
HANGUL_IME_KEY_PASSWORD=...
```

## Verify

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1
```

This runs the Hangul automata unit tests and rebuilds the debug APK.

For direct local Gradle use with an existing Android SDK/JDK, see
`docs\development.md`.

Before pushing theme/icon work, also run:

```powershell
rtk node --check web-theme-builder/app.js
rtk powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\render-theme-previews.ps1
rtk git diff --check
```

## Web Theme Builder

Open `web-theme-builder\index.html` in a browser to edit schemaVersion 1 theme
JSON that can be imported by the app theme editor.

External modifier/display icon pack authoring is documented in
`docs\icon-pack-import.md`.

## Icon Assets

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\generate-icons.ps1
```

The icon pipeline reads clean-room path data from `tools\icons\icons.json` and generates Android vector drawables under `app\src\main\res\drawable\`.

## Device Install

After enabling USB debugging on a device or starting an emulator:

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\install-debug.ps1
```

Then open the app once, enable `New Dingul` in Android keyboard settings, and select it as the active input method.

## Emulator Demo

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\demo-emulator.ps1
```

This installs the Android emulator packages if needed, creates a local AVD under `.android-tools\avd`, installs the debug APK, enables the IME, opens the inline test field, and saves a screenshot under `captures\`.

For best-effort closed-beta app coverage on the emulator:

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\smoke-ime-apps.ps1
```

The smoke script opens the local practice field and captures input-method state for Chrome, Messages, and Keep when those packages exist on the emulator image.

## Current Prototype Scope

- Custom Android `InputMethodService`
- 5-direction tap/slide key handling: center, up, down, left, right
- Long-press key slots, currently populated for English symbols and left empty for Hangul keys
- Preview overlay for the active key gesture
- Korean Hangul automata for consonant-vowel-final composition
- Automatic correction for cases such as vowel-start syllables and final consonants followed by a vowel
- Internal Hangul/English mode toggle
- English QWERTY layout with tap lowercase, up-slide uppercase, and long-press symbols
- Per-language top number row, default off for Hangul and on for English
- Clean-room generated vector icon pipeline for command keys and settings action buttons
- Conventional keyboard usability hacks: hit slop, touch Y offset, locked slide direction, haptic feedback, delete/cursor repeat, spacebar cursor movement, contextual Enter labels, and English double-space period
- Queued haptic ticks with adjustable duration/gap, plus bounded touch/slide correction learned from immediate deletes
- Local typing pattern logging for future typo correction experiments; data stays on device and resets with input correction
- Theme system with per-key foreground/background overrides, key display overrides, modifier icon packs, imported icon/display pack metadata, visual effects, and preview parity scripts
- Launcher settings for handedness, left/right margins, keyboard height, per-language number row, theme colors, key roundness/gap, Android input-method settings, and input-method picker
- Closed beta trust work: local privacy notice, Play Data safety draft, ASCII-capable IME subtype, explicit field policies, `TYPE_NULL` raw-key fallback, debug-gated demo overrides, and release build hardening

## Agent Handoff

Future coding contexts should start with `AGENTS.md` and `docs\agent-workflow.md`.
They describe the source map, theme/icon workflow, test expectations, and device
install flow.

## Closed Beta Notes

- Privacy notice draft: `docs\privacy-notice.md`
- Play Data safety draft: `docs\play-data-safety-draft.md`
- Closed beta readiness notes: `docs\closed-beta-readiness.md`
