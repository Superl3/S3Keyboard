# Windows Remote Mode Notes

## Current implementation

- Remote mode is a user setting and defaults to off.
- Remote mode can also be auto-enabled for configured app package names. The
  default auto list covers Parsec (`tv.parsec.client`), Moonlight
  (`com.limelight`), Microsoft Remote Desktop (`com.microsoft.rdc.android` and
  `com.microsoft.rdc.androidx`), and Chrome Remote Desktop
  (`com.google.chromeremotedesktop`). It also includes Steam Link
  (`com.valvesoftware.steamlink`), AnyDesk (`com.anydesk.anydeskandroid`), and
  TeamViewer remote/support packages. This uses `EditorInfo.packageName` from
  the focused input field, not a foreground-app polling service. The automatic
  list is gated by the user's remote-auto setting; turning that setting off
  keeps known remote packages on the normal local input profile unless remote
  mode is enabled manually. Matched remote packages keep family-specific profile
  ids (`remote_parsec`, `remote_moonlight`,
  `remote_microsoft_rdp`, `remote_chrome_remote_desktop`, `remote_steam_link`,
  `remote_anydesk`, and `remote_teamviewer`) so exported issue reports and
  smoke reports can compare compatibility by app family instead of collapsing
  every target into one generic remote profile.
- When enabled, the keyboard keeps the normal input engine but overlays a Windows-oriented runtime layout. It must not mutate the saved Hangul/English number-row visibility preferences or theme/icon choices.
- Quick theme selection and clipboard theme import load the persisted settings
  as their save base, then reapply the active app session's Remote/language/
  forced-number-row state only to the live keyboard. Remote and number-row quick
  toggles persist only their own preference keys, so an auto-remote session
  cannot accidentally turn its temporary QWERTY/number-row policy into the
  global default.
- The overlay always uses the PC QWERTY surface while remote mode is enabled, even if the saved local language mode is Hangul. Leaving remote mode restores the normal saved layout choice.
- The current target is remote desktop style apps that accept Android IME `InputConnection.sendKeyEvent(...)` events and forward them to Windows. Modifier shortcuts are sent as explicit modifier down, main key down/up, modifier up sequences instead of relying only on a key event meta state.
- Ctrl, Win, Alt, and Shift can be composed in one chord. Android-side tests verify
  all modifier downs before the main key, reverse-order modifier ups, matching
  `downTime` values, one-shot consumption, and locked-modifier persistence.
  The configured remote IME shortcut first clears pending/locked modifiers and
  then sends only its own Alt+Shift, Ctrl+Space, Win+Space, or LanguageSwitch
  sequence so an earlier latch cannot contaminate the language command.
- Remote mode forces fixed text labels only for the visible PC modifier keys: `Ctrl`, `Win`, and `Alt`. Shift, Backspace, Space, Lang, Menu, and Enter keep the normal icon/display-pack rendering path. The Menu key still exposes quick settings, and Menu long press remains the local escape path to app settings.
- When remote mode is on, quick settings exposes a collapsed compatibility test pad for
  `Esc`, `Tab`, `Shift+Tab`, `Ctrl+Tab`, `Alt+Tab`, `Ctrl+A`, `F1..F12`,
  `Alt+Shift`, `Ctrl+Space`, `Win+Space`, and `LanguageSwitch`. Expanding
  `원격 키 전달 테스트` reveals the pad without pushing normal quick toggles out
  of the initial view. Each press is
  logged locally with package name, label, key code, meta state, timestamp, and
  accepted local `InputConnection` event count. Reports also include the
  expected generated event count for that shortcut and mark local transport as
  complete only when every generated event was accepted, so partial shortcut
  delivery is not mistaken for a usable remote mapping.
- The quick settings pad shows a per-package matrix summary and can copy a JSON
  compatibility report. After checking the remote Windows session, the tester
  can mark the most recent case as `pass` or `fail`; unmarked sent cases remain
  `unknown`, and missing or unknown cases keep
  `manualRemoteResultRequired=true`. The JSON report also exposes `appFamily`
  (`parsec`, `moonlight`, `microsoft_rdp`, `chrome_remote_desktop`, or
  `custom`), `missingLabels`, `unknownLabels`, `failedLabels`,
  `localIncompleteLabels`, `acceptedEventCount`, and `expectedEventCount` so
  compatibility notes can be reviewed without parsing every matrix row. It also
  carries `requiredLabels` and `requiredAppFamilies`, so a copied report states
  which shortcuts and remote-client families still belong in the manual
  comparison matrix.

## Default PC keyboard mapping

- Bottom row: `Ctrl Win Alt Space Lang Menu Enter`. Remote mode keeps this order fixed even when the normal keyboard handedness preset is left-handed.
- `Ctrl`, `Win`, and `Alt`: tap one-shot latches the modifier for the next remote key or next English letter/digit without a persistent indicator. Long press toggles a sticky modifier lock with an indicator, and tapping the same modifier while locked turns that lock off.
- Space: tap `Space`; slide up/down/left/right sends arrow keys.
- Lang: tap remote IME shortcut; long press internal Hangul/English toggle.
- Menu: tap quick settings; long press app settings.
- Enter: tap `Enter`; long press `Ctrl+Enter`.
- Number row: remote mode force-enables the row at runtime. Tap keeps digits. Down slide maps `1..0 = F1..F10`; up slide maps `1 = Esc`, `9 = F11`, and `0 = F12`. Normal non-remote number rows keep only tap digits and down-slide symbols.
- QWERTY alpha remote cluster uses up/down slide hints where they are most readable: `q` up slide is `Tab`; `r/t/y` up slide maps `Shift+Tab/Ctrl+Tab/Alt+Tab`; `i/o/p` up slide maps `Ins/Home/PgUp`; and `i/o/p` down slide maps `Del/End/PgDn`. Plain ASCII alpha, digit, and common punctuation taps are sent as remote key events rather than `commitText`.
- QWERTY alpha keys do not use long press for alternate input in either normal or remote mode.

## Compatibility risk

`InputConnection.sendKeyEvent(...)` is intentionally the lowest-cost v1 transport, but it is not a full HID keyboard channel. Some Android layers or remote apps can ignore or consume soft-IME key events, especially:

- `F1..F12`
- `Meta/Win` shortcuts
- `Alt+Tab`
- `LanguageSwitch`
- Windows IME toggle shortcuts

Text, Enter, Backspace, arrows, and Tab are generally more likely to work than system-level shortcuts, but every remote app still needs device verification.

## Windows IME toggle

Android's internal Hangul/English toggle does not control Windows IME state. Remote mode therefore sends a configurable shortcut instead:

- `Alt+Shift` by default
- `Ctrl+Space`
- `Win+Space`
- `LanguageSwitch` key event as a best-effort option

These shortcuts only work if the remote app forwards the Android key events to Windows without consuming them.

## Accessibility bridge consideration

Some game-streaming or remote-control forks use Accessibility control paths to bypass Android input interception. That approach should be treated as a separate experimental transport, not as a guaranteed upgrade to the IME path.

Android exposes different input/control surfaces with different limits:

- `InputConnection.sendKeyEvent(...)` is the current IME path. It is cheap and does not require extra user permissions, but it depends on the focused app and remote client accepting soft-keyboard key events. A case is locally complete only when `acceptedEventCount >= expectedEventCount`; even then, that only means Android accepted the generated soft-key sequence and does not prove that Windows received the key.
- `AccessibilityService.dispatchGesture(...)` can inject touch gestures, not an arbitrary reliable PC keyboard/HID stream.
- `AccessibilityService.performGlobalAction(...)` is for Android global actions such as back/home/recents, not forwarding Windows shortcuts.

That means Accessibility may help a specific remote client when the target can be controlled through on-screen controls or a documented accessibility surface, but it should not be assumed to solve intercepted `Alt+Tab`, `Win+Space`, function keys, or Windows IME toggles.

Before adding it, verify:

- whether Accessibility can actually inject the needed target-app key events on the supported Android versions,
- whether the target remote app exposes a stable control surface,
- whether Play/closed-beta permission disclosure is acceptable,
- and whether the user can clearly opt in and recover when the bridge fails.

## Transport strategy

Keep the v1 remote mode on `InputConnection.sendKeyEvent(...)` and add proof tooling before adding a privileged bridge:

1. Use the quick-settings remote test pad in the target remote app, compare the
   local accepted-event matrix with what actually arrives in the remote desktop,
   mark each tested case as pass/fail, copy the JSON compatibility report, and
   keep app-specific notes before changing defaults.
   For broader app-launch smoke coverage, `scripts\smoke-ime-apps.ps1` writes a
   schema v2 report with `profileExpectation` values for Parsec, Moonlight, RDP,
   Chrome Remote Desktop, Steam Link, AnyDesk, TeamViewer, browsers, WebView
   providers, and messaging packages.
   Those values document the expected app profile, but they are still not proof
   that Windows received the key sequence. Remote rows also include
   `remoteCompatibilityEvidence.localMatrixCommand` plus the required labels
   (`Esc`, `Tab`, `F1..F12`, `Alt+Tab`, `Win+Space`, and the IME shortcuts), so
   the next manual proof step is preserved inside the report.
   The same local matrix can be exported from a debug build with:

   ```powershell
   rtk powershell -ExecutionPolicy Bypass -File .\scripts\export-remote-compatibility.ps1 -TargetPackage com.limelight
   ```

   The exported JSON keeps the Android report shape: `appFamily` identifies
   Parsec, Moonlight, Microsoft RDP, Chrome Remote Desktop, Steam Link,
   AnyDesk, TeamViewer, custom, or unknown targets; every case includes its
   `group`, accepted/expected event counts, and local transport status; and the
   top-level label arrays (`missingLabels`, `unknownLabels`, `failedLabels`,
   `localIncompleteLabels`) make app-to-app comparison possible without reading
   the full case list. `groupSummaries` splits the same counts by `BASIC`,
   `FUNCTION`, and `IME`, which makes it easier to see whether a remote app is
   only blocking function keys, IME shortcuts, or all modifier chords.
   `requiredLabels` and `requiredAppFamilies` are included
   as an explicit checklist so a partial app-family run is not mistaken for
   complete Parsec/Moonlight/RDP coverage.

2. Add per-app compatibility profiles only after real-device evidence, because RDP, Moonlight, Chrome Remote Desktop, and WebView-backed clients do not treat soft keyboard events identically.
3. If a Moonlight/Sunshine path is needed, prefer a client-specific integration or companion transport over a generic Accessibility workaround.
4. Only add Accessibility as an explicit opt-in experimental transport when the test pad proves which failures it fixes.
5. Treat Bluetooth HID or a small companion bridge as separate future transports for cases where the Android IME path is fundamentally insufficient.

## Recommended next work

- Add remote-app compatibility presets after real-device testing, for example Moonlight, Microsoft Remote Desktop, Chrome Remote Desktop, and generic WebView/RDP.
- Add a transport selector only after the test screen proves where `sendKeyEvent` fails.
- Keep Bluetooth HID and Accessibility bridge work out of the default path until the compatibility cost is proven worth it.
