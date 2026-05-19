# Play Data Safety Draft

This draft matches the current closed-beta implementation and should be reviewed against the final artifact before upload.

## Collection

- Collects user data: No.
- Shares user data: No.
- Data transmitted off device: No.
- Network permission: No `android.permission.INTERNET` permission is declared.

Under Google Play Data safety guidance, collection means transmitting data off the user's device. This app keeps keyboard settings and touch correction statistics locally.

## Local data handled by the app

- Keyboard preferences: mode, handedness, margins, height, visual theme, haptic setting, number-row settings.
- Touch correction statistics: aggregate touch offset bias after immediate delete patterns.
- No typed content, passwords, personal identifiers, account information, contacts, location, photos, files, microphone, or camera data.

## Security practices

- Data is processed locally.
- No third-party SDKs are included.
- `android:allowBackup="false"` is set to avoid cloud backup of local keyboard preferences.
- Release build is configured for minification and resource shrinking; signing credentials are supplied through Gradle properties, not source control.

## Play Console answers to confirm

- Data collection: No.
- Data sharing: No.
- Data encryption in transit: Not applicable because no data is transmitted.
- Users can request data deletion: Not applicable for server-side data; local data can be deleted by resetting touch correction, clearing app storage, or uninstalling.
- Privacy policy: Required for Play closed/open/production testing, even if no user data is collected.

References:

- Google Play Data safety form guidance: https://support.google.com/googleplay/android-developer/answer/10787469
- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
