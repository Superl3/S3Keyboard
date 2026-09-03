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
- The release-safe Diagnostics screen stores only the latest sanitized input-session state plus up to 12 recent action categories. It keeps a coarse app category (or a truncated SHA-256 package hash for an otherwise unknown app), profile id, EditorInfo class/variation/flag integers, effective policy booleans, layout/mode, Remote and one-finger state, theme/material id, timestamp, and category names such as `text_input`, `delete`, `enter`, or `remote_command`. It never stores or exports raw typed text, clipboard or saved-phrase content, AI request/result text, provider credentials, raw package names, touch coordinates, or debug probe geometry. Users can copy the safe JSON or explicitly export it through Android's document picker.
- The older detailed input issue report remains a debug-build-only support tool. It is a local redacted JSON payload that removes typed text, text-like future fields, clipboard/phrase preview fields, and code-point value fields while retaining geometry/correction/debug metadata for engineering investigation.
- Touch correction also stores aggregate local offset statistics when a typed key is immediately deleted.
- Clipboard history can store up to 10 recent clipboard text entries locally when the clipboard history setting is enabled. Entries expire automatically after seven days. Individual entries longer than 4,096 characters are not stored and are never silently truncated into a different paste value.
- Text Tools can also keep user-saved clipboard items and the four user-configured reserved phrases locally. Saved Text Tools items use an explicitly versioned local schema, may be pinned/unpinned or renamed, and remain until the user deletes them, clears all local data, clears app storage, or uninstalls the app. Clearing only clipboard history does not delete saved/pinned items or reserved phrases.
- Text Tools is suppressed for password, number-like, URI, email, web-edit, raw-key, replaced-row, and remote-input policies. Its clipboard listener is not registered in those fields and panel insertion is denied again below the UI. Text Tools does not capture raw typed text, and recent AI transformation results are not persisted or exposed in the panel by the current artifact.
- Manual backup/export is user initiated and writes a versioned JSON document only to the document destination the user chooses. It can include portable keyboard settings, app-profile package rules, user/custom theme JSON, portable copies of external theme JSON, reserved phrases and saved/pinned Text Tools items, plus explicitly opted-in local configuration such as AI-action enablement/provider id/timeout/translation target. It excludes clipboard-history contents, typing/gesture learning logs, diagnostics, remote compatibility logs, transient AI request/result text, external-theme source paths, and unknown credential/secret keys. Import validates and normalizes the whole backup before applying any selected section.
- The app does not automatically transmit typed content, passwords, clipboard contents, contacts, account data, or identifiers. A backup file may contain the user-created text and app-profile data described above if the user explicitly exports it to a document provider they select.
- Users can review a local-data summary and the separate release-safe Diagnostics report in settings. The diagnostics/input-learning reset deletes only the safe diagnostics snapshot/action categories and touch/input-learning state; clipboard history, saved Text Tools items, reserved phrases, themes, and ordinary preferences are preserved. The existing local-data controls can separately clear clipboard history, saved Text Tools data, and remote compatibility test logs.

## Data sharing

The app does not transmit app data to the developer, analytics services, ad networks, its own cloud service, or an external AI provider in the current artifact. When the user explicitly starts voice input, recognized audio is handled by the Android speech-recognition provider selected on the device.

## Retention and deletion

Local keyboard settings, user-configured reserved phrases, saved Text Tools items, AI text-action enablement/provider configuration, the latest release-safe diagnostics snapshot/action categories, touch correction statistics, typing pattern logs, gesture-intent journal entries, and remote test logs remain on the device until the user changes settings, uses the relevant clear/reset/delete action, clears app storage, or uninstalls the app. The diagnostics/input-learning reset removes the safe diagnostics state and touch/input-learning state without deleting clipboard history, saved Text Tools items, reserved phrases, themes, or ordinary preferences. AI request text and AI results are not persisted. Optional clipboard-history entries expire after seven days and can also be cleared manually without deleting saved/pinned Text Tools items or reserved phrases.

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
