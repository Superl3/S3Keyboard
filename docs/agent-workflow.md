# Agent Workflow

This document is the handoff map for future coding contexts. Start here when changing this Android keyboard, especially theme, icon, display override, preview, or input-learning behavior.

## Repository Rules

- Run commands through `rtk`.
- Prefer focused code changes and preserve unrelated local edits.
- Use `apply_patch` for manual edits.
- This checkout is currently Git-backed. If `.jj/` exists in a future checkout, follow the local Jujutsu instructions before any mutating VCS operation.
- Settings UI follows phone light/dark mode, not the active keyboard theme. Reuse `SettingsUiPalette`, `SettingsArrayAdapter`, and `SettingsViewStyler`.

## Verification Commands

```powershell
rtk powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1
rtk node --check web-theme-builder/app.js
rtk powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\render-theme-previews.ps1
rtk git diff --check
```

For a wireless device, refresh the transport id first:

```powershell
rtk .\.android-tools\android-sdk\platform-tools\adb.exe devices -l
rtk .\.android-tools\android-sdk\platform-tools\adb.exe -t <transport_id> install -r .\app\build\outputs\apk\debug\app-debug.apk
rtk .\.android-tools\android-sdk\platform-tools\adb.exe -t <transport_id> shell ime set com.superl3.s3keyboard/.S3KeyboardService
```

## Code Map

- `KeyboardSettings`: immutable settings object and normalization point.
- `KeyboardPreferences`: SharedPreferences load/save for runtime settings.
- `KeyboardThemePreset`: built-in theme source of truth.
- `ThemeOption`: applies preset/user/custom themes while preserving non-appearance preferences.
- `KeyboardThemeJson`: import/export schema, theme layering, icon pack import, visual effects, key color overrides, key display overrides.
- `KeyDisplayOverride`, `KeyDisplayOverrideResolver`, `KeyDisplayOverridePackCatalog`: text/icon override model, priority, and built-in display packs.
- `ModifierIconCatalog`: built-in modifier icon pack ids, intrinsic colors, and pack behavior flags.
- `KeyboardKeyVisualClassifier`: key role, foreground/background override resolution, dot display detection.
- `HangulKeyboardView`: actual Canvas renderer, touch handling, preview bubble, visual effects, custom modifier/dot/metropolis/script drawing, touch-learning sample capture.
- `S3KeyboardService`: IME lifecycle, input commands, quick settings popup, clipboard, editor policy integration.
- `MainActivity`, `ThemeEditorActivity`, `ThemeSelectorActivity`: settings, theme editing, theme preview and reset UI.
- `web-theme-builder/app.js`: browser-side theme authoring and preview parity.
- `scripts/render-theme-previews.ps1`: static preview image parity for themes.
- `TouchBiasStore`: local input learning stats and raw typing pattern event log.

## Theme And Icon Rules

Theme changes usually need all of these:

1. Add or update the built-in preset in `KeyboardThemePreset`.
2. Make sure `KeyboardThemeJson` imports and exports the new schema without losing layered base settings.
3. Keep Android preview cards using `ThemePreviewSettings` and `ThemeOption`.
4. Mirror authoring behavior in `web-theme-builder/app.js`.
5. Mirror static preview behavior in `scripts/render-theme-previews.ps1`.
6. Add or update tests in `KeyboardThemePresetTest`, `KeyboardThemeJsonTest`, `ThemePreviewSettingsTest`, and focused renderer/classifier tests.

Do not reintroduce global `LegendStylePreset.DOTS` as a forced renderer. Dot themes are represented as `keyDisplayOverrides`, usually `alpha: { "type": "icon", "value": "dot" }`, with exact `keys` entries when needed.

Override priority is:

```text
exact key > alpha/modifiers group > default label/icon
```

`alpha` includes English letters, Hangul jamo/syllables, Dingul action keys `ㅣ.`, `ㅡㅐ`, the `..`/`. .` key, and punctuation `?`, `.`, `/`. Those keys should receive alpha group display and color overrides.

## Modifier Icon Packs

Built-in modifier pack ids:

- `line-mono`: theme foreground.
- `accent-color`: intrinsic accent color, ignores theme foreground.
- `dots-lines`: proportional dot/line-dot renderer, theme foreground.
- `metropolis-points`: colored point-key renderer, ignores theme foreground.

Dot sizing rules currently live in `HangulKeyboardView`:

- alpha dot legend scales from the real key surface bounds.
- `space` uses five dots with generous horizontal padding.
- Dots pack `language` and `reserved` use a single dot, not a line-dot motif.
- `backspace`, `enter`, `settings`, `shift`, and related command variants use line-dot treatment.
- metropolis icons must also scale to the actual key bounds and keep large margins.

For external authoring/import details, see `docs/icon-pack-import.md`.

## Display Override Packs

Display packs replace labels or command icons with either:

```json
{ "type": "icon", "value": "dot" }
{ "type": "text", "value": "hihihi" }
```

The simple text pack is separate from Olivia as a theme. `hihihi` is rendered as a vector path in `HangulKeyboardView`, not as a font string, so keep script-like replacements in the renderer or a future imported vector renderer.

The `git-commands` display pack is used by Oblivion-style themes. It replaces modifier keys with short Git/workflow labels such as `exec`, `fetch`, `pull`, `rebase`, `reset`, and `commit`.

## Visual Effects

`KeyboardVisualEffects` is part of `KeyboardSettings` and `KeyboardThemeJson`.

- blur and metal are currently Canvas-simulated effects inside `HangulKeyboardView`.
- angular preview bubble shape is `PreviewBubbleDrawable`, used by `S3KeyboardService`.
- Effects must be kept lightweight enough to not break IME redraw, but they are allowed to be visually heavier when explicitly requested.

## Input Learning

`TouchBiasStore` has two roles:

- aggregate correction bias for touch center and gesture threshold adjustments.
- local raw typing pattern log in `typing_pattern_log`, capped by `MAX_TYPING_PATTERN_EVENTS`.

The raw log is local SharedPreferences data and is reset by the same input correction reset path. If this app is prepared for external testers or Play distribution, update privacy docs and UI disclosure because typed key values are stored locally.

## Settings And Quick UI

- Quick settings in the IME should use `PopupWindow`, be focusable, be outside-touch dismissible, and have an explicit OK close action.
- The bottom-row options key opens quick settings on tap; the full settings activity is the long-press fallback.
- The launcher instant keyboard test should hide the IME when tapping outside the test `EditText`.
- Spinner/dropdown rows must use `SettingsArrayAdapter`; platform default rows can render black text/icons on dark system UI.

## Documentation To Keep Current

- `docs/development.md`: local build, architecture, current implementation notes.
- `docs/icon-pack-import.md`: external icon/display pack schema.
- `docs/privacy-notice.md` and `docs/play-data-safety-draft.md`: must match any local raw text or clipboard storage behavior.
- `README.md`: public top-level commands and feature summary.
