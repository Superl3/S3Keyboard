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
- API 23 builds use core-library desugaring for the Java functional APIs used by settings and input controllers.
- The canonical `scripts\check.ps1` gate includes Android lint in addition to unit tests and APK assembly.

## Manual closed-beta smoke matrix

- Chrome or default browser: URL bar, page search field, and a
  `TYPE_TEXT_VARIATION_WEB_EDIT_TEXT`/`contenteditable` field. Web-edit fields
  should use the commit-only text path and avoid composing spans.
- WebView-based app: plain text, search, email, URL, password, number, and multiline fields.
- Messages or notes app: Hangul composition, English QWERTY, number row, delete repeat, enter action, and multiline newline.
- Password manager/login form: password field starts in ASCII-capable mode and does not use composing text.
- Orientation and low-height screens: keyboard remains visible, touch targets are not clipped, and bottom controls remain reachable.

## Remaining beta risks

- TalkBack virtual-key nodes expose hit-bound based focus, tap, and custom up/down/left/right/long-press actions. Real-device TalkBack traversal, action-menu wording, and gesture-conflict smoke testing are still required.
- Browser and Messages/Notes smoke tests still require real-device or emulator app coverage because installed packages vary by system image.
- `scripts\smoke-ime-apps.ps1` now records a JSON artifact for installed
  browser, messaging, notes, and remote-desktop packages. Treat its
  `profileExpectation`, `imeSelected`, and `imeVisible` fields as Android-side
  smoke evidence only. Each remote target also records a
  `remoteCompatibilityEvidence` checklist with the export command and required
  shortcut labels; Parsec, Moonlight, RDP, and Chrome Remote Desktop still need
  a tester to confirm that the remote Windows session actually received Esc,
  Tab, F-key, and IME shortcut events.
- Remote mode has a quick-settings compatibility matrix and JSON report for
  Android-side accepted-event evidence plus tester-marked pass/fail results, but
  Parsec/Moonlight/RDP receiving behavior still requires per-app real-device
  confirmation.
- The privacy policy draft needs the final developer entity and contact point before Play upload.
