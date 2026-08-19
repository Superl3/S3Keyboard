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

`scripts\check.ps1` runs theme validation, the `KeyboardSettings` runtime-usage
audit, unit tests, Android lint, and the debug APK build. The settings audit
fails when a persisted setting no longer has a runtime consumer. Lint remains
part of the normal gate so API-level, resource, and accessibility regressions
fail the same command as input tests.

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
- Windows remote mode uses the normal IME path plus Android `KeyEvent` dispatch.
  See `docs/remote-mode.md` before changing remote key mappings, Windows IME
  shortcuts, or any Accessibility-based bypass idea.
- Remote mode is a runtime overlay: it force-enables the number row while remote
  is on, restores the saved number-row state when remote is off, disables theme
  display/icon overrides, and uses a PC QWERTY surface with plain text command
  keys. Function keys are on number-row down slides; QWERTY alpha long press
  remains empty. Remote ASCII taps and shortcuts are sent as explicit KeyEvent
  down/up sequences, including modifier down/up events for chords.
- `RemoteCommandResolver` maps remote command strings to key, modifier latch,
  modifier lock, or IME-toggle actions. `RemoteInputController` owns
  `RemoteKeySession`, pending/locked modifier state, Windows IME shortcut
  execution, and `InputConnection` soft-key dispatch through
  `ImeConnectionDispatcher`. `S3KeyboardService` only commits pending local text
  before remote commands and updates the view through the controller callback.
- Remote compatibility proofing uses `RemoteCompatibilityMatrix`,
  `RemoteCompatibilityLog`, `RemoteCompatibilityReport`, and
  `RemoteCompatibilityPanelController`. The quick settings test pad sends the
  matrix cases through a narrow `S3KeyboardService` callback, records
  Android-side accepted-event evidence as `acceptedEventCount` plus
  `expectedEventCount`, and lets testers mark the latest case as pass/fail
  after checking the remote desktop result. Local transport is considered
  complete only when every generated event was accepted; that complete local
  count is still not proof that the remote Windows session received the key.
  Remote reports include `groupSummaries` for `BASIC`, `FUNCTION`, and `IME`
  cases so a failed run shows which shortcut family is blocked without manually
  scanning every case.
- App input profiles are resolved when a new editor starts. `AppPackageCatalog`
  owns package-family lists, `RemoteAppCatalog` owns remote package families,
  and `AppInputProfileCatalog` owns the profile policy values. Keep
  `AppInputProfileResolver` as routing glue so package lists and profile
  behavior do not drift. `InputSessionSettingsResolver` is the service-facing
  boundary that combines the editor policy, built-in profile, user override
  lists, enter action label, forced number row, and runtime remote-mode overlay;
  keep that calculation out of `S3KeyboardService` so `onStartInput` remains
  thin and testable. Built-in profiles cover requested remote desktop
  packages, password fields, number-like fields, URL/email/search/browser
  fields, explicit WebView provider/shell packages, and common messaging
  packages. The built-in remote set includes Parsec,
  Moonlight, Microsoft/Chrome Remote Desktop, Steam Link, AnyDesk, and
  TeamViewer; the browser and messaging sets include common stable/beta
  variants such as Chrome beta/dev/canary, Edge, Brave, Firefox beta, WhatsApp,
  Line, Signal, Discord, and Facebook Messenger. Profiles tune remote mode,
  ASCII preference, number-row forcing, composing, and text convenience behavior
  without overwriting the user's saved language mode. Remote package matching is
  gated by the user's remote-auto setting or explicit remote mode; a package is
  never promoted to remote mode just because it is known. Matched remote
  packages use family-specific profile ids such as `remote_parsec`,
  `remote_moonlight`, `remote_microsoft_rdp`, and
  `remote_chrome_remote_desktop`; user-configured remote matches fall back to
  `remote_desktop` with source `remote_auto_package`. URL and email profiles
  prefer ASCII, disable text
  conveniences, and also use commit-only input so fragile browser/editor fields
  do not receive Hangul composing spans.
- User app-profile overrides are additive package lists stored in
  `KeyboardPreferences` and applied by `AppInputProfileOverrides` after the
  built-in resolver. Use them to force ASCII, force the number row, disable
  composing, or disable text conveniences for exact app package tokens without
  changing the built-in catalog defaults.
- `KeyboardSettingsSections` is the current read-only typed schema boundary for
  issue reports and future settings refactors. It groups the large legacy
  `KeyboardSettings` object into `appearance`, `layout`, `input`, `remote`, and
  `ergonomics` sections without changing the saved preference schema yet.
  Appearance snapshots include theme colors, typography, depth/outline, display
  pack ids, override counts, and visual effects; layout snapshots include both
  stored per-layout dimensions and runtime-derived visibility such as
  `showNumberRow`. `ProductionReadinessConfigTest` checks that package-level
  `KeyboardSettings` fields remain represented in `KeyboardSettingsSections` so
  new fields cannot silently disappear from redacted issue reports.
- `KeyboardSettingsSchema` is the preference-key descriptor. Every package-level
  `KeyboardPreferences` key must be assigned to a section and storage-risk class
  there, so new settings cannot silently skip privacy/debug/remote
  classification. Redacted issue reports include an aggregate schema summary so
  exported diagnostics show how many local-text, local-diagnostic, and
  compatibility keys the build knows about without dumping raw local data.
- `KeyboardSettingsSnapshot` serializes `KeyboardSettingsSections` for redacted
  issue reports, including the currently loaded Dingul ergonomics options.
- `InputIssueReportClipboardController` owns copying redacted input issue
  reports to the clipboard. `S3KeyboardService` only flushes pending learning
  state and exposes the current editor/package/settings snapshot through the
  controller host callback. Reports include the effective app input profile
  details, including profile source, remote-mode activation, and nullable ASCII,
  number-row, composing, and text-convenience overrides, so per-app behavior can
  be debugged without exporting typed text. User app-profile override lists are
  not exported; reports include only per-flag match booleans for the current
  package so compatibility debugging does not leak the user's configured app
  list. Reports also include current, Hangul, and QWERTY layout accessibility
  summaries with hard 30dp issue counts and recommended 40dp advisory counts,
  so cramped touch targets can be debugged without exporting typed text.
  `localDataSummary` exports only local feature state and counts, including
  clipboard-history enabled state, clipboard entry count, input-log counts, and
  remote-test count; it never exports clipboard entries themselves. Journal
  export must also redact any future text-like, clipboard, or phrase preview
  fields, not only the current code-point fields.
- `ImeConnectionDispatcher` owns testable `InputConnection` transport decisions
  for Enter fallback, raw ASCII key dispatch, and soft-key event generation.
  Raw ASCII/newline dispatch should prefer soft key events but fall back to
  `commitText` if the sender is missing, rejects the event, or accepts only a
  partial down/up sequence, so text does not silently disappear in custom
  editors. Cursor movement also belongs here so boundary checks and soft D-pad
  dispatch do not drift back into `S3KeyboardService`. Keep service-level
  command routing separate from this helper so WebView/custom editor fallback
  behavior can stay covered by JVM tests.
- `InputConnectionTextOperator` owns low-level composing, committed-text delete,
  cursor-boundary checks, normal text commits, and the commit-only Hangul sink.
  A committed backspace first removes the active selection and otherwise deletes
  one extended grapheme cluster, so emoji modifiers, flags, ZWJ sequences,
  combining marks, and decomposed Hangul are not split. `EditorTextBoundaryPolicy`
  owns pure word, code-point, and grapheme boundary calculations.
  Keep direct `commitText`, `setComposingText`,
  `deleteSurroundingTextInCodePoints`, and stale composing cleanup out of
  `S3KeyboardService` so conventional editor behavior can be regression-tested
  with fake `InputConnection` instances. Service code should not call
  `commitText`, `deleteSurroundingText`, `setComposingText`, or
  `finishComposingText` directly.
- `InputConnectionSequenceTest` verifies the higher-level editor sequence:
  composing text updates, commit-current finishing, stale-composing cleanup
  around delete, rejected `performEditorAction`, soft Enter `sendKeyEvent`
  fallback, and the final newline commit fallback when the soft sender rejects
  Enter. Update it when changing IME transport order.
- `KeyboardCommandRouter` maps raw key gesture strings to local command routes.
  `KeyboardCommandDispatcher` owns the route-to-callback dispatch table, and
  `S3KeyboardCommandTarget` owns the route callback implementation that bridges
  to service side effects. `S3KeyboardService` keeps the actual IME state,
  lifecycle, and input side effects instead of embedding the full dispatch
  target table in its body. `RemoteCommandResolver` remains responsible only for
  Windows remote commands.
- `QuickThemePanelController` owns the quick-settings theme spinner and theme
  application persistence. It applies the theme to a freshly loaded persisted
  base, then overlays the current session language, remote state, enter label,
  and forced number row only for the active view. App-profile runtime state must
  never be written back as a global preference.
- `QuickSettingsPanelController` owns the quick-settings panel body: remote
  toggle, remote compatibility pad insertion, number-row toggle, handedness
  buttons, theme selector, clipboard theme import, issue-report copy, and close
  action. Common controls are ordered before remote/diagnostic controls.
  `S3KeyboardService` keeps only the `PopupWindow` lifecycle, measures the panel,
  bounds it to the display in a scroll container, and exposes IME/session side
  effects behind host callbacks.
- `ThemeClipboardImportController` owns quick-settings theme import from the
  clipboard, including clipboard reading, JSON import, preference persistence,
  toast feedback, and dismissal. Like quick preset selection, it keeps separate
  persisted and runtime settings suppliers so a URL/password/remote session
  cannot leak forced mode state into the saved theme base. `S3KeyboardService`
  only applies the imported runtime settings through a host callback.
- `ClipboardPanelController` owns optional clipboard-history UI, clipboard
  listener registration, password-field suppression, and local history refresh.
  The toggle action repeats the password/disabled guard rather than relying only
  on toolbar visibility.
  `S3KeyboardService` only provides the current editor policy/settings snapshot
  and commits a selected clipboard item into the active `InputConnection`.
- `HangulCommitOnlyEditor` is the fallback for editors that do not support
  composing spans. It records the expected cursor delta of each fallback
  delete/commit and consumes either individual or coalesced selection callbacks.
  A selected range or an unexpected caret delta is treated as an external edit
  and ends the fallback composition instead of rewriting text at the new cursor.
- Previous-character Hangul repair and QWERTY suggestion/autocorrect paths must
  not run while the editor has a selected range. Direct input then replaces the
  selection using the editor's normal `commitText` contract.
- Display assistance is grouped by `InputAssistanceMode`: clean mode hides
  hints and preview, learning mode keeps hints and preview visible, and debug
  mode also enables the debug key-bounds overlay on debuggable builds. Each mode
  carries a recommended Dingul ergonomics preset for diagnostics, but applying
  an input-assistance mode does not automatically change ergonomics layout
  settings because that would move existing users' keys. The mode selector
  derives its current state from the real preferences, so manual edits show as
  custom rather than writing a second source of truth.
  `InputAssistanceSettingsController` owns mode persistence, visible mode lists
  for debug/release builds, and individual hint/debug-overlay saves; keep that
  logic out of `MainActivity`.
- `KeyboardMode` is the language/composition mode only. `KeyboardLayoutProfiles`
  stores the physical surface per language, defaulting to Hangul Dingul and
  English QWERTY while allowing Hangul QWERTY and English Dingul from settings.
- `AndroidImeActions` is the shared activation boundary for the quick-start and
  Android/IME screens. Keep direct input-method settings/picker intents out of
  individual settings controllers.
- `AndroidImeStatus` derives the quick-start state from the enabled input-method
  list and `Settings.Secure.DEFAULT_INPUT_METHOD`. It must fail closed to the
  activation prompt rather than crashing settings when a vendor build restricts
  either provider.
- In one-finger Dingul input, a selected key remains armed until a direction
  locks or an unmapped direction cancels it; leaving a narrow visual/hit rect is
  not itself a cancellation signal. Movement beyond the tap dead zone pauses
  the tap timer so a slow directional slide cannot become an accidental tap.
- English QWERTY is tap-first. `EnglishQwertyInputAssistant` keeps only the
  current in-memory word for suggestion strip updates, applies exact typo
  corrections before boundary keys such as space and punctuation, and lets the
  user tap a suggestion to replace the current word through the active
  `InputConnection`. It is disabled for remote, raw-key, password, and other
  fields where `EditorInputPolicy` disables text conveniences.
- Fields that reject composing spans, including `TYPE_NULL` raw-key targets and
  `TYPE_TEXT_VARIATION_WEB_EDIT_TEXT`, use a commit-only Hangul fallback. The
  app keeps the internal automata state, deletes the previous visible fallback
  composition, and commits the updated complete syllable so sequences like
  `ㄱㅏㄴ` do not remain split when composing text is unavailable.
- `HangulKeyboardView` keeps the preview strip inside the measured keyboard
  height, so preview space does not create a transparent area over the app UI.
- Non-interactive keyboard previews in settings, theme selection, and accent
  placement screens should be created through `KeyboardPreviewFactory`; it
  centralizes compact rendering, touch suppression, and accessibility exclusion
  for preview-only views.
- `HangulKeyboardView` exposes a concise accessibility summary with mode,
  surface, key count, remote mode, preview, and debug-overlay state. It also
  exposes per-key virtual accessibility nodes with tap/slide descriptions,
  matching text/content descriptions, hit-bound based node bounds, focusable
  click actions, explicit accessibility-focus lifecycle, and custom up/down/
  left/right/long-press actions mapped to the key's real gesture output. The
  virtual node provider lives in `KeyboardVirtualKeyAccessibilityProvider`; keep
  TalkBack node construction out of the Canvas view body.
- `KeyboardAccessibilityLayoutTest` verifies that default Hangul and QWERTY
  layouts expose non-empty, non-raw-command labels for every key, and that
  layout hit bounds do not shrink below the base key bounds. Keep this test
  updated when adding command keys, surfaces, or ergonomics hitbox changes.
- `KeyboardAccessibilityAudit.audit(...)` is the hard runtime/layout gate and
  intentionally keeps the compact keyboard's minimum hit target at 30dp.
  `KeyboardAccessibilityAudit.advisoryAudit(...)` uses a 40dp recommended
  target so QA and diagnostic work can find cramped keys without silently
  changing the default keyboard size or QWERTY layout.
- Preview tooltip display is split between `PreviewBubbleAnimation` for
  testable bubble lifetime/progress/alpha state, `HangulKeyboardView` for
  touch-to-preview value resolution, and `PreviewOverlayController` for the
  popup window/TextView rendering. Keep animation timing changes in
  `PreviewBubbleAnimationTest` rather than burying them in Canvas-only code.
- `TouchBiasStore` learns from local input patterns. It stores aggregate touch
  center and gesture-threshold statistics, and also keeps a capped local raw key
  event log for future typo analysis. Runtime learning state is cached in memory
  and flushed to preferences on a short debounce or IME session finish, so normal
  key input does not wait on preference reads/writes. Resetting input correction
  clears both.
- Settings expose separate local deletion controls for touch correction/input
  logs, optional clipboard history, and remote compatibility test logs.
- `scripts/render-theme-previews.ps1` treats its output directory as a generated
  preview set once `theme-preview-grid.png` exists. It rejects colliding safe
  filenames and removes PNGs that no longer correspond to the current theme
  catalog, preventing deleted presets from appearing as stale previews.
  `LocalDataControlsController` owns these destructive local-data actions and
  the clipboard-history toggle side effect. It also formats the read-only local
  data summary shown in settings, so `MainActivity` should only wire controls,
  display controller text, and resync state.
- `KeyboardSettingsSchema` tracks both user-facing preference keys and internal
  local-data keys such as clipboard entries, touch-bias stats, typing pattern
  logs, gesture journals, Dingul touch profiles, and remote compatibility logs.
  Mark internal local-data entries as `userFacing=false` while preserving their
  storage-risk classification.
- `LocalDataControlsController.managedLocalDataKeys()` is the deletion contract
  for internal local data. Any non-user-facing schema entry with local text,
  diagnostic, or compatibility risk must be covered there so privacy/debug data
  cannot be added without a matching clear path.
- Local-data and remote-compatibility status text is user-facing UI, not a log
  constant. Keep summary and empty-state copy in `strings.xml`; readiness tests
  guard the local-data summary and remote-test empty state against drifting back
  into Java literals.
- Main settings copy that explains local-only input logs, clipboard history, or
  reserved phrase behavior should also live in `strings.xml`. Keep these labels
  resource-backed so localization, mojibake scanning, and privacy copy review do
  not depend on escaped Java string literals.
- `scripts\smoke-ime-apps.ps1` installs the debug APK, activates the IME, opens
  the local practice surface plus installed browser, messaging, notes, and
  remote-desktop packages, and writes `captures\smoke\smoke-ime-apps-report.json`
  with package, expected profile policy, IME-selected, IME-visible, dump, and
  screenshot evidence. Report schema v2 stores `profileExpectation` fields for
  expected remote mode, ASCII preference, number-row forcing, composing usage,
  text conveniences, and whether manual remote delivery confirmation is still
  required. Each result also includes `remoteCompatibilityEvidence`, including
  the local matrix export command, required shortcut labels, and required remote
  app families. That block is a next-step checklist, not proof of Windows
  delivery. Use `-Serial <device>` for a physical device, or omit it to launch
  the local emulator path.
- `scripts\analyze-device-learning.ps1` summarizes pulled
  `keyboard_preferences.xml` captures, including gesture-intent labels, delete
  bursts, pattern counts, touch bias, and Dingul profile size. It handles UTF-16
  captures produced by redirected ADB output.

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
- `DecorativeGlyphCatalog` owns built-in decorative glyph metrics and the
  normalized `hihihi` path plus the semantic point glyph ids used by display
  packs. Android, web preview, and static preview should mirror these values so
  dot, two-dot, four-dot space, point glyph, and script glyph sizing does not
  drift by renderer.
- `metropolis-graph` is a modifier glyph pack, not a preview line pattern. It
  should render the same recognizable command icons as the normal modifier pack;
  colored Metropolis keycaps should use explicit text overrides for visible
  glyph contrast.
- `KeyDisplayOverridePackCatalog` owns built-in text/icon replacement packs. The
  simple text pack is separate from a theme and only replaces enter-like keys
  with the `hihihi` vector glyph; other command keys remain modifier icons.
  Point packs such as `geo-points`, `soft-symbols`, `terminal-points`,
  `punctuation-points`, and `full-decorative` should remain predefined semantic
  mappings, with exact key overrides as the user-facing escape hatch. Keyboard
  packs such as `keyboard-symbols` and `keyboard-navigation` should keep
  Material Symbols-aligned glyph ids instead of hard-coding theme-specific
  image assets. GMK-style packs should model the recurring kit vocabulary
  (accent bars, novelty minis, macro marks, spacebar marks) rather than
  exact product artwork.
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
- Theme selector cards and `scripts/render-theme-previews.ps1` show QWERTY and
  Dingul together without per-layout captions. QWERTY keeps its default number
  row, Dingul keeps its default four input rows, and both render their actual
  directional slide legends. Display-override themes such as GMK Dots continue
  to suppress text hints on keys replaced by glyphs.

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

## Dingul Ergonomics Manual Checklist

Use this checklist on a real device after installing a debug APK:

- Apply `Legacy` and confirm the Dingul layout matches the previous default
  layout.
- Apply `Stable` and confirm the main 12 keys are centered while the right
  function rail remains usable.
- Apply `Ergonomic` and confirm the left assist rail appears, the 5-column
  visual grid is aligned, and main key order does not change.
- Apply `Aggressive` and confirm the left assist rail, main 12 keys, and right
  function rail stay inside the keyboard area.
- Change one individual ergonomics toggle after applying a preset and confirm
  the preset state changes to custom.
- Type with every main Dingul key using tap, up, down, left, and right slide.
- Hold backspace and confirm repeated deletion starts naturally.
- Tap the four right function rail keys, especially near the right bezel.
- Enable left assist rail and tap clipboard, voice, undo, and tools. Clipboard
  should explain how to enable local history when it is off, and should open the existing panel
  without replacing an active Hangul syllable when it is on. Tools should open quick settings;
  voice should launch the Android speech recognizer,
  return recognized text to the same editor, and remain unavailable in password/raw/remote
  fields. Undo should use the editor's conventional undo context-menu action when the target
  app supports it and report unsupported editors instead of failing silently.
- With compact function rail and ergonomic hitbox enabled, confirm backspace
  accepts touches toward the main keys and downward edge.
- Confirm left assist rail, main 12 keys, and right function rail look like one
  aligned 5-column group when left assist rail and uniform gap are enabled.
- On a large-screen device, confirm Dingul main keys are centered only when the
  centering option is enabled.
- Confirm QWERTY mode keeps its previous layout and is not centered or compacted
  by Dingul ergonomics settings.
