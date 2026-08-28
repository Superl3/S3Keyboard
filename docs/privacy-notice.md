# Privacy Notice Draft

New Dingul Research is a clean-room research keyboard for closed beta testing.

## What stays on the device

- Keystrokes, Hangul composing state, English shift state, and gesture decisions are processed locally.
- The app does not request network permissions.
- Optional voice input opens the Android speech-recognition provider installed on the device.
  The keyboard itself does not request microphone permission or retain audio, but the selected
  provider's privacy and network behavior applies while recognition is active.
- Optional Glass 화면 소스는 사용자가 Android 접근성 설정에서 별도로 허용한 경우에만
  활성 앱 창의 화면을 캡처합니다. 캡처는 Glass 배경을 그리는 동안 메모리에 저해상도 한 장만
  유지하고 디스크나 네트워크에 저장하지 않으며, 키보드가 닫히거나 비밀번호 필드가 시작되면
  즉시 폐기합니다. 활성 앱 창을 구분하기 위한 창 목록 외에 접근성 노드의 텍스트나 입력 내용을
  읽지 않으며 Android 보안 창은 캡처하지 않습니다.
- The app can store local typed key pattern events and a local gesture-intent journal for input correction research. The legacy typed-key pattern log stores event type, gesture action, timing, correction geometry, and redacted text length rather than raw typed text. The gesture-intent journal may store key/value code points, touch geometry, shadow correction candidates, and delete/replacement correction metadata for non-sensitive fields.
- Password, number-like, URI, email, and web-edit fields redact typed key values in the local gesture-intent journal.
- Quick settings can copy an input issue report to the clipboard. The report is a local redacted JSON payload: it removes typed text, text-like future fields, clipboard/phrase preview fields, and code-point value fields, includes a `redaction` summary, and keeps gesture actions, timing, geometry, correction labels, effective app input profile settings, local remote-test accepted-event counts, and manual pass/fail metadata for debugging.
- Touch correction also stores aggregate local offset statistics when a typed key is immediately deleted.
- Clipboard history can store up to 10 recent clipboard text entries locally when the clipboard history setting is enabled. Entries expire automatically after seven days. Individual entries longer than 4,096 characters are not stored and are never silently truncated into a different paste value.
- The app does not transmit typed content, passwords, clipboard contents, contacts, account data, or identifiers.
- Users can review a local-data summary in settings and reset touch correction, the local typing pattern log, the local gesture-intent journal, clipboard history, and remote compatibility test logs from app settings.

## Data sharing

The app does not transmit app data to the developer, analytics services, ad networks, or its own cloud service. When the user explicitly starts voice input, recognized audio is handled by the Android speech-recognition provider selected on the device.

## Retention and deletion

Local keyboard settings, touch correction statistics, typing pattern logs, gesture-intent journal entries, and remote test logs remain on the device until the user changes settings, uses the relevant clear/reset button, clears app storage, or uninstalls the app. Optional clipboard entries expire after seven days and can also be cleared manually.

## Closed beta disclosure text

This keyboard processes typed input locally on your device. It has no network permission and does not upload typed text. Optional voice input is processed by the device's Android speech-recognition provider and is subject to that provider's behavior. For local input improvement, the keyboard may store recent typed key pattern events, gesture-intent journal entries, and correction statistics on the device; these can be reset from settings.

## Release checklist

- Replace this draft with the developer or organization name used in the Play listing.
- Add a privacy contact email or web form before closed testing.
- Play 배포 전에 Accessibility API 사용 목적과 화면 캡처 고지를 스토어 설명 및 앱 내 동의 흐름과 일치시킵니다.
- Publish the final privacy policy on a public, non-editable, non-geofenced web page if distributing through Google Play closed testing.

References:

- Google Play User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Google Play Data safety form guidance: https://support.google.com/googleplay/android-developer/answer/10787469
