# Play Data Safety Draft

This draft matches the current closed-beta implementation and should be reviewed against the final artifact before upload.

## Collection

- Collects user data: No, under the Google Play definition of collection, because app data is not transmitted off the user's device.
- Shares user data: No.
- Data transmitted off device: No.
- Network permission: No `android.permission.INTERNET` permission is declared.

Under Google Play Data safety guidance, collection means transmitting data off the user's device. This app keeps keyboard settings, local input-learning data, and optional clipboard history on device.

## Local data handled by the app

- Keyboard preferences: mode, handedness, margins, height, visual theme, haptic setting, number-row settings.
- Touch correction statistics: aggregate touch offset bias after immediate delete patterns.
- Typing pattern log: recent local key input and correction events, including raw typed key values, gesture action, and correction metadata. This is capped and reset with input correction.
- Gesture-intent journal: recent local key input events, touch geometry, policy snapshot fields, shadow correction candidates, delete rollback targets, and derived labels such as missed slide, false slide, wrong direction, wrong origin key, accepted tap, accepted slide, and shadow false alarm. Typed key values are redacted for password, number-like, URI, and email fields.
- Optional clipboard history: recent clipboard text when the clipboard history setting is enabled.
- No typed content, clipboard content, passwords, personal identifiers, account information, contacts, location, photos, files, microphone, or camera data is transmitted off device.

## Security practices

- Data is processed locally.
- No third-party SDKs are included.
- `android:allowBackup="false"` is set to avoid cloud backup of local keyboard preferences.
- Release build is configured for minification and resource shrinking; signing credentials are supplied through Gradle properties, not source control.

## Play Console answers to confirm

- Data collection: No, if the final artifact still has no data transmission.
- Data sharing: No.
- Data encryption in transit: Not applicable because no data is transmitted.
- Users can request data deletion: Not applicable for server-side data; local data can be deleted by resetting touch correction, clearing clipboard history, clearing app storage, or uninstalling.
- Privacy policy: Required for Play closed/open/production testing, even if no user data is collected.

References:

- Google Play Data safety form guidance: https://support.google.com/googleplay/android-developer/answer/10787469
- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
