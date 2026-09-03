# S09 Final Verification Report

Date: 2026-09-03 KST
Branch: `main`

## Result
Engineering integration for S01-S09 is COMPLETE. All repository-controlled gates pass. Closed-beta release signing and a few receiver/device checks remain external/manual blockers only.

## Prior-session audit
- S01 text actions: completion evidence and reversible/sensitive-field gates present.
- S02 provider/privacy: provider failure/privacy evidence and tests present.
- S03 app profiles: real Chrome/Messages and Remote auto enter/exit evidence present.
- S04 Text Tools: ordering/edit/delete/sensitive suppression evidence present.
- S05 backup/restore: migration/atomic/selective/malformed safety evidence present.
- S06 theme management: favorites/recent/filter/pairing/A-B runtime evidence present.
- S07 Remote: Android toolbar/modifier/runtime evidence present; Windows receiver delivery remains manual.
- S08 diagnostics/privacy: safe diagnostics/reset/privacy runtime evidence present.

## Fresh automatic verification
- `scripts/check.ps1`: PASS after fixing caller-working-directory dependence.
- Theme catalog: 42 themes, 0 warnings.
- Material surfaces: `solid`, `soft_keycap`, `frosted`, `acrylic` aligned.
- KeyboardSettings audit: 70 fields, 0 unused.
- `testDebugUnitTest`, `lintDebug`, `assembleDebug`: PASS; Gradle `BUILD SUCCESSFUL`.

## Fresh runtime verification
- Dingul probe: PASS, 16 emitted actions; `captures\dingul-typing-20260903-103232`.
- Editor/app matrix: PASS for local, password, number, URL, email, web-edit, search, multiline, installed Chrome and Google Messages; `captures\s09-integration-20260903-runtime\app-smoke`.
- Theme geometry: English + Hangul both PASS with `bottomDelta=0` for solid `gmk-8008`, soft-keycap `8008-soft-keycap`, frosted `nord-frost-night`, acrylic `laser-outline-acrylic`.
- Theme evidence: `captures\s09-integration-20260903-runtime\theme-solid-alt`, `theme-soft-keycap`, `theme-frosted`, `theme-acrylic`.
- Core QWERTY Shift/Caps/Backspace, Hangul composition/backspace, cursor movement and one-finger continuous input were independently re-audited against the current completion evidence in `captures\runtime-input-followup-20260901`; production input semantics were not modified by S03-S09.

## Release gate
- `scripts/build-release.ps1` was fixed to run Gradle from repository root.
- Release build reaches `:app:verifyClosedBetaSigning` and then correctly fails because all four external signing properties are absent: `HANGUL_IME_KEYSTORE`, `HANGUL_IME_KEYSTORE_PASSWORD`, `HANGUL_IME_KEY_ALIAS`, `HANGUL_IME_KEY_PASSWORD`.
- No signing secret or keystore was fabricated or committed.

## External/manual blockers
- Confirm Remote Esc/Tab/F-key/IME shortcuts reach a real Windows receiver in supported remote apps.
- Provide final developer entity/contact details for distribution/privacy metadata.
- Provide closed-beta signing keystore/properties, then rerun `scripts/build-release.ps1`.
- Physical-device/TalkBack breadth remains a tester gate rather than a repository engineering blocker.
