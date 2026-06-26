# Privacy Notice Draft

New Dingul Research is a clean-room research keyboard for closed beta testing.

## What stays on the device

- Keystrokes, Hangul composing state, English shift state, and gesture decisions are processed locally.
- The app does not request network permissions.
- The app can store local typed key pattern events and a local gesture-intent journal for input correction research. The legacy typed-key pattern log stores event type, gesture action, timing, correction geometry, and redacted text length rather than raw typed text. The gesture-intent journal may store key/value code points, touch geometry, shadow correction candidates, and delete/replacement correction metadata for non-sensitive fields.
- Password, number-like, URI, email, and web-edit fields redact typed key values in the local gesture-intent journal.
- Quick settings can copy an input issue report to the clipboard. The report is a local redacted JSON payload: it removes typed text, text-like future fields, clipboard/phrase preview fields, and code-point value fields, includes a `redaction` summary, and keeps gesture actions, timing, geometry, correction labels, effective app input profile settings, local remote-test accepted-event counts, and manual pass/fail metadata for debugging.
- Touch correction also stores aggregate local offset statistics when a typed key is immediately deleted.
- Clipboard history can store recent clipboard text locally when the clipboard history setting is enabled.
- The app does not transmit typed content, passwords, clipboard contents, contacts, account data, or identifiers.
- Users can review a local-data summary in settings and reset touch correction, the local typing pattern log, the local gesture-intent journal, clipboard history, and remote compatibility test logs from app settings.

## Data sharing

The app does not transmit app data to the developer, third parties, analytics services, ad networks, or cloud services.

## Retention and deletion

Local keyboard settings, touch correction statistics, typing pattern logs, gesture-intent journal entries, optional clipboard history, and remote test logs remain on the device until the user changes settings, uses the relevant clear/reset button, clears app storage, or uninstalls the app.

## Closed beta disclosure text

This keyboard processes input locally on your device. It has no network permission and does not send data off the device. For local input improvement, it may store recent typed key pattern events, gesture-intent journal entries, and correction statistics on the device; these can be reset from settings.

## Release checklist

- Replace this draft with the developer or organization name used in the Play listing.
- Add a privacy contact email or web form before closed testing.
- Publish the final privacy policy on a public, non-editable, non-geofenced web page if distributing through Google Play closed testing.

References:

- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Google Play Data safety form guidance: https://support.google.com/googleplay/android-developer/answer/10787469
