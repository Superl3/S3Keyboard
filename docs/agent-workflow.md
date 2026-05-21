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
- `themes/*.json`: built-in theme appearance source of truth.
- `tools/theme-contract.mjs`: single theme schema/authoring contract shared by validation, generated web editor rules, and preview checks.
- `tools/sync-themes.mjs`: validates `themes/*.json`, prints the theme review report, generates Android preset source, and generates the web builder theme index.
- `KeyboardThemePreset`: thin runtime wrapper around generated `GeneratedKeyboardThemePresets.PRESETS`.
- `ThemeOption`: applies preset/user/custom themes while preserving non-appearance preferences.
- `KeyboardThemeJson`: import/export schema, theme layering, icon pack import, visual effects, key color overrides, key display overrides.
- `KeyDisplayOverride`, `KeyDisplayOverrideResolver`, `KeyDisplayOverridePackCatalog`: text/icon override model, priority, and built-in display packs.
- `ModifierIconCatalog`: built-in modifier icon pack ids, intrinsic colors, and pack behavior flags.
- `KeyboardKeyVisualClassifier`: key role, foreground/background override resolution, dot display detection.
- `HangulKeyboardView`: actual Canvas renderer, touch handling, preview bubble, visual effects, custom modifier/dot/metropolis/script drawing, touch-learning sample capture.
- `S3KeyboardService`: IME lifecycle, input commands, quick settings popup, clipboard, editor policy integration.
- `docs/remote-mode.md`: remote input overlay contract and Android `KeyEvent` transport limits.
- `MainActivity`, `ThemeEditorActivity`, `ThemeSelectorActivity`: settings, theme editing, theme preview and reset UI.
- `web-theme-builder/app.js`: browser-side theme authoring and preview parity.
- `scripts/render-theme-previews.ps1`: static preview image parity for themes.
- `TouchBiasStore`: local input learning stats and raw typing pattern event log.

## Theme And Icon Rules

Theme changes usually need all of these:

1. Add or update the built-in appearance JSON in `themes/*.json`.
2. Run `rtk node tools/sync-themes.mjs --generate --report` so Android presets, `web-theme-builder/theme-contract.generated.js`, and `web-theme-builder/theme-index.generated.js` are regenerated from the same files and rules.
3. Make sure `KeyboardThemeJson` imports and exports the new schema without losing layered base settings.
4. Keep Android preview cards using `ThemePreviewSettings` and `ThemeOption`.
5. Mirror authoring behavior in `web-theme-builder/app.js`.
6. Mirror static preview behavior in `scripts/render-theme-previews.ps1`.
7. Add or update tests in `KeyboardThemePresetTest`, `KeyboardThemeJsonTest`, `ThemePreviewSettingsTest`, and focused renderer/classifier tests.

Do not hand-edit `GeneratedKeyboardThemePresets.java` or reintroduce manual preset construction in `KeyboardThemePreset`. A new built-in theme should be accepted by adding a valid JSON file and rerunning the generator.

Root-level `hints` is deprecated. Slide hint visibility is a user preference, not a theme appearance field, and `KeyboardThemeJson` intentionally does not import or export it. If an old preset recommendation needs to be kept for review history, store it only as `metadata.recommendedHints`; applying a theme must not change the user's hint settings.

When preview and runtime disagree, treat the current preview as the design target unless the user explicitly says the device rendering is better. Make runtime Canvas rendering, static preview, web builder, and exported theme JSON converge on the same colors and packs.

Typography is a user-level preference by default. Theme selection preserves the user's font family, size, bold, and italic settings unless `followThemeTypography` is enabled. This keeps Noto Sans KR from becoming too thin after applying a theme while still allowing theme-authored typography when explicitly desired.

`colors.panelBackground` is the actual keyboard panel background. If both `keyboardBackground` and `panelBackground` are present in theme JSON, `panelBackground` wins at runtime and in previews.

`additionalNumberRow.colorMode` is the optional number-row appearance contract, not the visibility toggle. It splits digits into outer `123890` and inner `4567` sets; each set can use `alpha`, `mod`, or `accent` styling. Common values are `full_alpha`, `full_mod`, `half_mod_4567`, `alpha_accent`, and `mod_accent`; old imported values `full_default`, `center_dimmed`, and `full_dimmed` are compatibility aliases only.

Do not reintroduce global `LegendStylePreset.DOTS` as a forced renderer. Dot themes are represented as `keyDisplayOverrides`, usually `alpha: { "type": "icon", "value": "dot" }`, with exact `keys` entries when needed.

Any key whose main legend is replaced by a custom display glyph, whether text or icon, must suppress slide-hint sub items. This applies to alpha dot legends, text display packs, exact display overrides, and custom modifier glyph packs such as `dots-lines` and `metropolis-graph`. Sub items belong to default legends only; once a theme owns the main glyph, it also owns that key's visual surface.

Override priority is:

```text
exact key > alpha/modifiers group > default label/icon
```

`alpha` includes English letters, number-row digits, Hangul jamo/syllables, and Dingul action keys `ㅣ.`, `ㅡㅐ`, and `..`/`. .`. Dingul punctuation keys `?`, `.`, and `/` are modifier-role keys unless an exact theme exception says otherwise. Forced alpha display packs such as dot legends intentionally apply to the number row too; the dot legend also owns Dingul punctuation keys when no modifier display override exists, so Dots-style previews and Android runtime do not fall back to visible punctuation text plus slide hints.

Dingul role colors are authored as `dingulColors.alpha`, `dingulColors.mod`, and `dingulColors.modInv`. Normal themes should use that three-role model instead of many exact per-key colors. Heavy per-key color maps should stay limited to intentional exceptions such as Dots, Fiesta, or Metropolis graph styling.

Modifier icon packs are a first-class theme axis, especially after the core JSON policy is stable. Prefer contract-backed `icons.modifierPackId` values such as `line-mono`, `accent-color`, `dots-lines`, and `metropolis-graph` over one-off raster or preview-only decoration. If a new modifier style is needed, add it to the Android catalog, theme contract, web builder preview, static preview, JSON validation, and tests together.

Theme review uses visual taxonomy, not strict accessibility contrast for every color pair. Dimmed pairs are allowed for subtle platform-style themes and should appear as review metadata, not warnings. Warnings should be reserved for primary `alpha` and `mod` text that is genuinely at risk of becoming unreadable.

Theme review classification counts only the authored visual role pairs that define the keycap colorway. `alpha` and `mod` are the core pairs; `modInv` is a derived/inverted pair and `keyPressed` is an interaction state, so neither one upgrades a two-color theme into an accent/three-color theme by itself. A `modInv` pair backed by a visually distinct authored `colors.accentKey` background counts as the third pair when it is clearly separated from the alpha/modifier backgrounds; HammerHead-style inverted mod accents and GMK 8008-style fluorescent accent keys are intended three-tone examples, while Dracula-style nearby mod shading remains two-tone.

Foreground-only per-key colors are glyph decoration, not keycap coverage. Marigold-style themes can have many colorful legends while still classifying by their alpha/mod/accent keycap backgrounds; report this as `colorfulForeground` instead of inflating the theme to custom coverage just because text colors vary.

Visual roles are defined in `tools/theme-contract.mjs`. Dingul treats the top 4x3 typing area plus `?` and `space` as `alpha`; punctuation-like visual accents can be classified as `modEnter` for `.` and `modShift` for `/`; bottom command keys are split into `modCtrl` (`settings`, `enter`) and `modMeta` (`reserved`, `language`). QWERTY treats `q-p`, `a-l`, `z-m`, and `space` as `alpha`, with the same bottom command grouping. Shift and backspace currently remain `modCommand` because their final visual role is intentionally still open.

Accent placement is a visual policy, not a fixed semantic truth. The user or theme can choose accent targets such as `modEnter`, `modShift`, `modCtrl`, `modMeta`, command keys, punctuation, or exact per-key overrides. Shift long-press color is currently better treated as a user preference candidate rather than theme identity because it behaves like interaction feedback.

Use `accentPolicy.qwerty` and `accentPolicy.dingul` for role-level accent placement before adding exact background overrides. Supported targets are `modEnter`, `modShift`, `modCtrl`, `modMeta`, `modCommand`, `punctuation`, and `perKey`. Android import expands this policy to normal key color overrides, and the static/web previews read the same policy so QWERTY and Dingul do not drift.

For colorway class `c` / coverage class `3`, accent targets may move, but the theme should still use only the alpha, mod, and accent pairs. If punctuation keys such as `.` and `/` are treated as accent, bottom ctrl/meta/enter keys that join that accent policy should share the same accent pair instead of inventing another pair.

## Modifier Icon Packs

Built-in modifier pack ids:

- `line-mono`: theme foreground.
- `accent-color`: intrinsic accent color, ignores theme foreground.
- `dots-lines`: dot alpha legends, solid modifier lines, and a four-color spacebar dot cluster.
- `metropolis-graph`: normal modifier icon shapes for Metropolis-style command keys. Legacy `metropolis-points` imports normalize to this id.

Dot sizing rules currently live in `HangulKeyboardView`:

- Dots use one visual weight family: alpha dot diameter, modifier line stroke, and single modifier dot diameter should match by ratio.
- alpha dot legend scales from the real key surface bounds.
- `space` uses four vivid dots with tight horizontal gaps.
- Dots pack `language` and `reserved` use a single dot, not a line-dot motif.
- `backspace`, `enter`, `settings`, `shift`, and related command variants use solid line treatment with the same visual weight as alpha dots.
- metropolis icons must use recognizable modifier glyph shapes and scale to the actual key bounds. If the keycap background is itself red/yellow/teal, the glyph color should come from the theme text override so it stays visible.
- Dot themes can be colorful in glyph foregrounds, but their alpha and mod keycap backgrounds must still be visibly different. Colorful dots are an overlay on top of the normal alpha/mod structure, not a reason to collapse modifier surfaces into alpha.

For external authoring/import details, see `docs/icon-pack-import.md`.

## Display Override Packs

Display packs replace labels or command icons with either:

```json
{ "type": "icon", "value": "dot" }
{ "type": "text", "value": "hihihi" }
```

The simple text pack is separate from Olivia as a theme. It replaces only Dingul's visual enter-position `.` key with `hihihi`; the real bottom-right Enter command must keep the default Enter icon. Shift, backspace, space, language, settings, options, and reserved remain modifier glyph icons. `hihihi` is rendered as a vector path in Android and previews, not as a font string, so keep script-like replacements in the renderer or a future imported vector renderer.

The `git-commands` display pack is used by Oblivion-style themes. It replaces modifier keys with short Git/workflow labels such as `exec`, `fetch`, `pull`, `rebase`, `reset`, `commit`, `diff`, and `log`.

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
- Remote mode is a runtime input overlay, not a theme. It forces the number row while enabled without changing saved Hangul/English number-row toggles, bypasses theme display/icon overrides, uses the fixed plain text bottom row `Ctrl Win Alt Space Lang Menu Enter`, and clears one-shot/sticky remote modifiers when remote mode or the input session ends.
- QWERTY alpha long press is intentionally empty. Remote number-row function keys live on down slide, not long press.
- The launcher instant keyboard test should hide the IME when tapping outside the test `EditText`.
- Spinner/dropdown rows must use `SettingsArrayAdapter`; platform default rows can render black text/icons on dark system UI.

## Documentation To Keep Current

- `docs/development.md`: local build, architecture, current implementation notes.
- `docs/icon-pack-import.md`: external icon/display pack schema.
- `docs/privacy-notice.md` and `docs/play-data-safety-draft.md`: must match any local raw text or clipboard storage behavior.
- `README.md`: public top-level commands and feature summary.
