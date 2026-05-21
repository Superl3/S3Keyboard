# Windows Remote Mode Notes

## Current implementation

- Remote mode is a user setting and defaults to off.
- When enabled, the keyboard keeps the normal input engine but remaps the bottom modifier row and number-row long press slots to Windows-oriented commands.
- The current target is remote desktop style apps that accept Android IME `InputConnection.sendKeyEvent(...)` events and forward them to Windows.
- The options key long press always remains a local escape path to app settings.

## Default PC keyboard mapping

- Options: tap `Esc`, up `Home`, left `Ctrl latch`, right `Alt latch`, long press app settings.
- Reserved: tap `Tab`, left `Shift+Tab`, right `Ctrl+Tab`, up `Alt+Tab`.
- Space: tap `Space`, left/right cursor movement, up `PageUp`, long press repeated space.
- Language: tap remote IME shortcut, up `End`, long press internal Hangul/English toggle.
- Enter: tap `Enter`, long press `Ctrl+Enter`.
- Number row: tap keeps digits, long press `1..0 = F1..F10`, `9 left = F11`, `0 right = F12`.
- Ctrl/Alt latch applies to the next remote key event or to the next English letter/digit key event, then clears.

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

- `InputConnection.sendKeyEvent(...)` is the current IME path. It is cheap and does not require extra user permissions, but it depends on the focused app and remote client accepting soft-keyboard key events.
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

1. Build a remote key test screen that sends the exact shortcuts and records which remote app receives them.
2. Add per-app compatibility profiles only after real-device evidence, because RDP, Moonlight, Chrome Remote Desktop, and WebView-backed clients do not treat soft keyboard events identically.
3. If a Moonlight/Sunshine path is needed, prefer a client-specific integration or companion transport over a generic Accessibility workaround.
4. Only add Accessibility as an explicit opt-in experimental transport when the test screen proves which failures it fixes.
5. Treat Bluetooth HID or a small companion bridge as separate future transports for cases where the Android IME path is fundamentally insufficient.

## Recommended next work

- Add a remote key test screen that sends `Esc`, `Tab`, `F1`, `Ctrl+A`, `Alt+Shift`, `Ctrl+Space`, `Win+Space`, and `LanguageSwitch`.
- Add remote-app compatibility presets after real-device testing, for example Moonlight, Microsoft Remote Desktop, Chrome Remote Desktop, and generic WebView/RDP.
- Add a transport selector only after the test screen proves where `sendKeyEvent` fails.
- Keep Bluetooth HID and Accessibility bridge work out of the default path until the compatibility cost is proven worth it.
