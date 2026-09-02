# Privacy Notice Draft

New Dingul Research is a clean-room research keyboard for closed beta testing.

## What stays on the device

- Keystrokes, Hangul composing state, English shift state, and gesture decisions are processed locally.
- The app does not request network permissions.
- Optional voice input opens the Android speech-recognition provider installed on the device.
  The keyboard itself does not request microphone permission or retain audio, but the selected
  provider's privacy and network behavior applies while recognition is active.
- Optional AI text actions are disabled by default. The current closed-beta artifact provides only an on-device test provider and does not send text to an external AI or network endpoint. Provider requests contain only the explicitly selected text or the bounded current sentence, never unrelated document context, and are capped at 2,048 characters. Password, number-like, URI, email, web-edit, raw-key, replaced-row, and remote-input policies block provider requests below the UI layer as well as in the UI. Provider results are previewed before Apply, cancellation/failure leaves the editor unchanged, and request text/results are not persisted.
- The app can store local typed key pattern events and a local gesture-intent journal for input correction research. The legacy typed-key pattern log stores event type, gesture action, timing, correction geometry, and redacted text length rather than raw typed text. The gesture-intent journal may store key/value code points, touch geometry, shadow correction candidates, and delete/replacement correction metadata for non-sensitive fields.
- Password, number-like, URI, email, and web-edit fields redact typed key values in the local gesture-intent journal.
- Quick settings can copy an input issue report to the clipboard. The report is a local redacted JSON payload: it removes typed text, text-like future fields, clipboard/phrase preview fields, and code-point value fields, includes a `redaction` summary, and keeps gesture actions, timing, geometry, correction labels, effective app input profile settings, local remote-test accepted-event counts, and manual pass/fail metadata for debugging.
- Touch correction also stores aggregate local offset statistics when a typed key is immediately deleted.
- Clipboard history can store up to 10 recent clipboard text entries locally when the clipboard history setting is enabled. Entries expire automatically after seven days. Individual entries longer than 4,096 characters are not stored and are never silently truncated into a different paste value.
- The app does not transmit typed content, passwords, clipboard contents, contacts, account data, or identifiers.
- Users can review a local-data summary in settings and reset touch correction, the local typing pattern log, the local gesture-intent journal, clipboard history, and remote compatibility test logs from app settings.

## Data sharing

The app does not transmit app data to the developer, analytics services, ad networks, its own cloud service, or an external AI provider in the current artifact. When the user explicitly starts voice input, recognized audio is handled by the Android speech-recognition provider selected on the device.

## Retention and deletion

Local keyboard settings, AI text-action enablement/provider configuration, touch correction statistics, typing pattern logs, gesture-intent journal entries, and remote test logs remain on the device until the user changes settings, uses the relevant clear/reset button, clears app storage, or uninstalls the app. AI request text and AI results are not persisted. Optional clipboard entries expire after seven days and can also be cleared manually.

## Closed beta disclosure text

This keyboard processes typed input locally on your device. It has no network permission and does not upload typed text. Optional AI text actions are off by default; the current artifact includes only an on-device test provider, sends only the selected text or bounded current sentence into that local provider, previews the result before Apply, and does not persist requests or results. Optional voice input is processed by the device's Android speech-recognition provider and is subject to that provider's behavior. For local input improvement, the keyboard may store recent typed key pattern events, gesture-intent journal entries, and correction statistics on the device; these can be reset from settings.

## Release checklist

- Replace this draft with the developer or organization name used in the Play listing.
- Add a privacy contact email or web form before closed testing.
- If a future build adds an external AI/network provider, update this notice, the Data Safety answers, in-app consent/configuration text, network security requirements, and retention/deletion disclosures before distribution.
- Publish the final privacy policy on a public, non-editable, non-geofenced web page if distributing through Google Play closed testing.

References:

- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Google Play Data safety form guidance: https://support.google.com/googleplay/android-developer/answer/10787469
