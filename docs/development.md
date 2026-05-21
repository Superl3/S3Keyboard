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
adb -s <device-ip>:<connect-port> shell ime enable com.superl3.s3keyboard/.S3KeyboardService
adb -s <device-ip>:<connect-port> shell ime set com.superl3.s3keyboard/.S3KeyboardService
```

If a previously-installed APK was signed with a different key, Android will reject
an update with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Uninstall the package first:

```powershell
adb -s <device-ip>:<connect-port> uninstall com.superl3.s3keyboard
```

## Current Input Feel Architecture

- `KeyboardFeedback` owns haptic queuing and emits short vibrator pulses in order.
- `KeyboardPreferences` stores haptic tick duration and gap independently from the
  broader immutable `KeyboardSettings` object.
- `HangulKeyboardView` keeps the preview strip inside the measured keyboard
  height, so preview space does not create a transparent area over the app UI.
- `TouchBiasStore` learns from local input patterns. It stores aggregate touch
  center and gesture-threshold statistics, and also keeps a capped local raw key
  event log for future typo analysis. Resetting input correction clears both.

## Theme Architecture

- `themes/*.json` is the source of truth for built-in theme appearance. Keep
  generated Android presets, web builder presets, static previews, and tests in
  parity with those deterministic JSON themes rather than image-matched screenshots.
- Built-in presets are generated at build time from `themes/*.json` by
  `tools/sync-themes.mjs`. `KeyboardThemePreset` should stay a thin wrapper
  around `GeneratedKeyboardThemePresets.PRESETS`; do not add a second manual
  color mirror there.
- Theme authoring rules live in `tools/theme-contract.mjs`. Generated web
  editor files, validation, and preview checks should consume that contract
  rather than carrying their own copy of allowed fields, pack ids, or legacy
  aliases.
- Run `rtk node tools/sync-themes.mjs --generate --report` after editing theme
  JSON. The same tool validates schema fields, duplicate ids/names, deprecated
  root `hints`, required shift-indicator appearance data, basic contrast risks,
  and writes `web-theme-builder/theme-contract.generated.js` plus
  `web-theme-builder/theme-index.generated.js`.
- Root `hints` is deprecated because slide hint visibility belongs to user
  preferences, not theme appearance. Old recommendations can live under
  `metadata.recommendedHints` for audit context, but import/apply/export must
  not change `showHangulSlideHints`, `showEnglishSlideHints`, or beginner
  tooltip settings.
- `metadata.tags` and `metadata.features` are optional review metadata for
  diversity reporting. Use them to describe families such as `dark`, `light`,
  `minimal`, `highContrast`, `dots`, `textPack`, `metal`, `glassLike`, or
  `gmkInspired` without adding renderer-only custom code.
- Theme review classifies `coverage` and `colorway` from the shared contract:
  `1` is all-same color, `2` is alpha/mod, `3` is alpha/mod/accent, `4.1` is
  custom modifier coverage, `4.2` is custom alpha coverage, and `5` is both.
  Colorway classes are `a` one colorway, `b` two colorway, `c` three colorway,
  and `d` colorful.
- Contrast checks should follow visual intent. Primary `alpha` and `mod`
  legends can warn when genuinely unreadable, but dimmed `modInv`, secondary,
  decorative, and accent-marker pairs are valid aesthetic choices and should be
  reported as metadata rather than failure.
- Every keyboard theme starts from the same three-tone keycap model:
  `alpha` keys use `alphaKeyColor` and `accentColor`, `modifier` keys use
  `modifierKeyColor` and `secondaryColor`, and selected command/accent keys use
  `accentKeyColor`. Per-key overrides should be treated as explicit exceptions
  on top of that model, not as the default way to build a theme.
- Theme colors are split into global key colors plus per-key overrides:
  - `keyTextColorOverrides` changes legends, hint text, and icon foregrounds.
  - `keyBackgroundColorOverrides` changes individual key backgrounds. Supported
    keys include `tap:<value>`, `label:<value>`, `space`, `enter`, `backspace`,
    `shift`, `language`, `options`, `reserved`, and `icon:<id>`.
- Both override maps are imported through `KeyboardThemeJson`. Runtime storage
  normalizes background overrides with a `background:` prefix so the renderer can
  keep one immutable override map without mixing foreground and background lookups.
- Dingul themes can declare semantic role colors through `dingulColors.alpha`,
  `dingulColors.mod`, and `dingulColors.modInv`. `modInv` means foreground and
  background are intentionally inverted for keys such as space and enter. Use
  these role colors for normal themes before reaching for exact per-key color
  overrides.
- Layout role taxonomy lives in `tools/theme-contract.mjs`. Dingul alpha covers
  the top 4x3 typing keys plus `?` and `space`; visual accent punctuation can be
  treated as `modEnter` for `.` and `modShift` for `/`; bottom command keys are
  grouped as `modCtrl` (`settings`, `enter`) and `modMeta` (`reserved`,
  `language`). QWERTY alpha covers `q-p`, `a-l`, `z-m`, and `space`; the bottom
  command grouping is shared. Shift and backspace remain `modCommand` until the
  visual role is settled.
- Dot-style themes should not use global forced `LegendStylePreset.DOTS`.
  They should use `keyDisplayOverrides`, usually `alpha: icon:dot`, plus exact
  key overrides for punctuation or command keys. Exact key overrides win over
  `alpha` or `modifiers` group overrides.
- Dot-style themes may use many colorful foreground dots, but their keycap
  surfaces should still preserve the alpha/mod split. Treat the color dots as
  glyph-level decoration layered on top of a two-tone or three-tone keycap
  system, not as a replacement for role backgrounds.
- `alpha` display overrides apply to letter keys plus Dingul action keys
  (`ㅣ.`, `ㅡㅐ`, `..`/`. .`). Dingul punctuation (`?`, `.`, `/`) is a modifier
  role for color and display unless an explicit theme exception overrides it.
- Optional number-row visibility remains a user layout preference. Themes only
  define `additionalNumberRow.colorMode`, which maps outer `123890` and inner
  `4567` digit sets onto `alpha`, `mod`, or `accent` styling.
- Custom display glyphs suppress slide-hint sub items. Dot legends, text display
  packs, exact display overrides, and non-default custom modifier glyph packs
  should render as a clean owned glyph surface without extra hint text.
- Theme review classification ignores `keyPressed` and derived `modInv` pairs
  when deciding whether a theme is two-color or three-color. They stay visible
  as interaction/dimmed metadata, not as extra colorway coverage by themselves.
  A `modInv` pair backed by a visually distinct authored `colors.accentKey`
  background still counts as the third visual pair when it is clearly separated
  from the alpha/mod/primary backgrounds; nearby mod shading remains part of the
  two-tone family.
- Foreground-only per-key color maps are reviewed as glyph decoration. They can
  add `colorfulForeground` without upgrading keycap coverage; Marigold Dark is
  three-tone because of its orange accent background, while Marigold Light stays
  a white/soft-gray two-tone theme even with colorful legends.
- `ModifierIconCatalog` owns built-in modifier icon pack ids. Monochrome packs
  use theme foreground colors; colored packs use intrinsic foreground colors and
  ignore theme foreground. Treat modifier icon packs as the preferred way to
  make modifier keys visually distinctive once the theme JSON policy is stable.
  New packs must be added to Android runtime, `tools/theme-contract.mjs`,
  web builder preview, static preview, and focused tests together.
- `metropolis-graph` is a modifier glyph pack, not a preview line pattern. It
  should render the same recognizable command icons as the normal modifier pack;
  colored Metropolis keycaps should use explicit text overrides for visible
  glyph contrast.
- `KeyDisplayOverridePackCatalog` owns built-in text/icon replacement packs. The
  simple text pack is separate from a theme and only replaces enter-like keys
  with the `hihihi` vector glyph; other command keys remain modifier icons.
- `KeyboardThemeJson` accepts imported icon/display pack metadata. In v1,
  external packs select a built-in renderer through `extends` and can add
  `keyDisplayOverrides`; future path renderers can consume the preserved glyph
  authoring metadata. See `docs/icon-pack-import.md`.
- `KeyboardVisualEffects` carries blur, metal, and angular preview-bubble flags.
  Android runtime, theme JSON, web builder, and preview scripts should stay in
  sync when effects change.
- `ThemeSelectorActivity` persists the applied preset/custom theme id through
  `KeyboardPreferences.SELECTED_THEME_ID`. Avoid relying on card index alone
  because user themes can be added or removed.

## Agent Handoff

The short project-specific handoff is `docs/agent-workflow.md`. Keep it current
whenever a new cross-cutting theme, icon, preview, input, or settings workflow is
added. The root `AGENTS.md` includes that document so new Codex contexts can find
the same source map and verification commands.

## App Icon Assets

The launcher icon source prompt is stored at:

```text
assets\source-prompts\s3keyboard-launcher-icon.md
```

The full-resolution generated source image is stored at:

```text
assets\generated\icons\s3keyboard-launcher-icon.png
```

Launcher PNGs are derived from that source into the Android `mipmap-*`
directories. Regenerate the source with the local `codex-image-gen` workflow
from `C:\Users\bug95\.codex\vendor\codex-image-gen`, then resize into
`app\src\main\res\mipmap-mdpi` through `mipmap-xxxhdpi`.

## Settings UI Styling

Settings screens must follow the phone light/dark mode, not the active keyboard
skin. Reuse `SettingsUiPalette`, `SettingsArrayAdapter`, and `SettingsViewStyler`
for text, spinner rows, buttons, checkboxes/radio buttons, and numeric inputs.
This prevents Android default widgets from leaving black text or black checkbox
tints on dark system UI.
