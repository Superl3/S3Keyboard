# Play Data Safety Draft

This draft matches the current closed-beta implementation and should be reviewed against the final artifact before upload.

## Collection

- Collects user data: No, under the Google Play definition of collection, because app data is not transmitted off the user's device.
- Shares user data: No.
- Data transmitted off device: No.
- Network permission: No `android.permission.INTERNET` permission is declared.
- Optional voice input delegates recognition to the Android speech-recognition provider. The app
  does not request microphone permission or retain audio; provider-side collection must be
  disclosed according to the provider used in the test/release environment.
- Optional AI text actions are disabled by default. The current artifact includes only an on-device test provider; it does not contact an external AI or network endpoint. Only the explicit selection or bounded current sentence can enter the provider request, sensitive/remote/raw-key policies are denied below the UI layer, results require preview + Apply, and request/result text is not persisted.

Under Google Play Data safety guidance, collection means transmitting data off the user's device. This app keeps keyboard settings, local input-learning data, optional AI-provider configuration, and optional clipboard history on device.

## Local data handled by the app

- Keyboard preferences: mode, handedness, margins, height, visual theme, haptic setting, number-row settings, and optional AI text-action enablement/provider configuration such as timeout and translation target.
- Touch correction statistics: aggregate touch offset bias after immediate delete patterns.
- Typing pattern log: recent local key input and correction events, including event type, gesture action, correction metadata, and redacted text length rather than raw typed text. This is capped and reset with input correction.
- Gesture-intent journal: recent local key input events, touch geometry, policy snapshot fields, shadow correction candidates, delete rollback targets, and derived labels such as missed slide, false slide, wrong direction, wrong origin key, accepted tap, accepted slide, and shadow false alarm. Typed key values are redacted for password, number-like, URI, email, and web-edit fields. Copied issue reports additionally redact text-like future fields, clipboard/phrase preview fields, and code-point values, and include a redaction summary plus remote compatibility accepted-event and pass/fail metadata without clipboard history contents.
- Optional clipboard history: up to 10 recent clipboard text entries when enabled, with automatic expiry after seven days.
- AI request text and AI results: processed in memory only by the current on-device test provider and not persisted.
- No typed content, clipboard content, passwords, personal identifiers, account information, contacts, location, photos, files, microphone, or camera data is transmitted off device.

## Security practices

- Data is processed locally.
- No third-party SDKs are included.
- `android:allowBackup="false"` is set to avoid cloud backup of local keyboard preferences.
- Provider requests have a second privacy gate below the UI and a 2,048-character payload cap.
- Provider failure, timeout, cancellation, malformed response, or stale editor state does not modify editor text.
- Release build is configured for minification and resource shrinking; signing credentials are supplied through Gradle properties, not source control.

## Play Console answers to confirm

- Data collection: No, if the final artifact still has no data transmission.
- Data sharing: No.
- Data encryption in transit: Not applicable because no app data is transmitted by the current artifact.
- Users can request data deletion: Not applicable for server-side data; local data can be deleted by resetting touch correction, clearing clipboard history, clearing app storage, or uninstalling.
- Privacy policy: Required for Play closed/open/production testing, even if no user data is collected.
- If an external AI provider is added later, re-evaluate Collection, Data sharing, encryption in transit, purpose categories, retention/deletion, and prominent in-app disclosure before release.

References:

- Google Play Data safety form guidance: https://support.google.com/googleplay/android-developer/answer/10787469
- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
