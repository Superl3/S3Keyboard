# Closed Beta Readiness Notes

## Current release target

Closed beta means a small tester group can install the keyboard without being surprised by privacy handling or basic input failures. It is not yet a public Play Store readiness bar.

## Implemented gates

- Local privacy disclosure is visible on the launcher settings screen.
- IME subtype is ASCII-capable and includes the legacy `AsciiCapable` extra value for older platform behavior.
- `TYPE_NULL` fields use raw key fallback for ASCII characters.
- Password, URI, email, number, phone, and datetime fields use explicit field policies.
- Password and number-like fields force the number row and prefer the English/ASCII layout at runtime without overwriting the saved user language mode.
- Demo/test intent overrides are ignored unless the app is a debuggable build and `demo_settings=true` is supplied.
- Release build config has versioning, minification, resource shrinking, and property-based signing separation.

## Manual closed-beta smoke matrix

- Chrome or default browser: URL bar, page search field, and a `contenteditable` field.
- WebView-based app: plain text, search, email, URL, password, number, and multiline fields.
- Messages or notes app: Hangul composition, English QWERTY, number row, delete repeat, enter action, and multiline newline.
- Password manager/login form: password field starts in ASCII-capable mode and does not use composing text.
- Orientation and low-height screens: keyboard remains visible, touch targets are not clipped, and bottom controls remain reachable.

## Remaining beta risks

- Full TalkBack virtual-key accessibility is not implemented yet; the custom keyboard view exposes a view-level description only.
- Browser and Messages/Notes smoke tests still require real-device or emulator app coverage because installed packages vary by system image.
- The privacy policy draft needs the final developer entity and contact point before Play upload.
