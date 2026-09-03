# S03 - App Profiles and State Indicators

Status: **DONE** (2026-09-02 KST)

## Read first
- `docs/agent-workflow.md`, `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`
- `EditorPolicy.java`, profile/catalog classes, `KeyboardPreferences.java`
- `S3KeyboardService.java`, `MainActivity.java`, settings UI helpers
- `docs/remote-mode.md`

## Scope
1. Audit existing automatic editor/app policy and package overrides before adding UI.
2. Add per-app override storage with schema/versioning and safe defaults.
3. Expose current effective profile: standard/browser-search/webview/messaging/password/number/url/email/remote.
4. Add “Always use for this app” overrides for language preference, number row, composing/text conveniences, remote mode where valid.
5. Add compact runtime indicators for active mode only: Hangul/English, Dingul/QWERTY, Remote, Caps, one-finger.
6. Keep indicators visually minimal and theme-safe; never obscure keys or alter touch bounds.
7. Add reset-to-auto per app and global clear overrides.
8. Add tests for precedence: editor policy < app override < hard sensitive-field restrictions.

## Runtime verification
- Chrome profile reflects browser/search behavior.
- Messages profile reflects messaging behavior.
- password/number restrictions cannot be overridden unsafely.
- remote app preset auto-enters/exits without leaking state to normal apps.
- indicators track real runtime state and disappear when inactive.

## Done gate
Automatic checks + real-app runtime evidence pass. Mark S03=DONE and update handoff.
## Completion evidence
- Added versioned per-app override JSON with legacy package-list compatibility and safe auto defaults.
- Per-app controls cover language, number row, composing, text conveniences, and Remote; per-app reset and global clear are available from quick settings.
- Resolver precedence is now built-in editor/app policy -> per-app override -> hard password/number/raw restrictions. Per-app `remote=false` can suppress stored/auto Remote for that app.
- Runtime indicator is a separate non-touch-obscuring row and reports effective language/layout plus active Remote/Caps/one-finger states only.
- Focused S03 resolver/schema tests pass, including JSON round-trip, Remote on/off, language/number-row overrides, and sensitive-field precedence.
- Final `scripts/check.ps1` -> PASS: `testDebugUnitTest`, `lintDebug`, `assembleDebug`, theme validation/material/settings audits; `git diff --check` -> PASS before documentation-only edits.

## Runtime evidence
- Canonical synthetic/real-app matrix: `captures\s03-app-profiles-20260902-runtime`.
- Synthetic password and number fields keep their hard restrictions; password shows `EN · QWERTY` with forced number row, and number uses the numeric surface without unsafe Remote promotion.
- Real Google Messages recipient `ContactSearchField`: `messages-recipient-ime.png` shows the effective `EN · QWERTY` state for that phone/email-style field.
- Real Chrome `com.android.chrome:id/url_bar`: `chrome-url-focused-hangul-stored.png` shows `EN · QWERTY` even when the stored keyboard mode was temporarily Hangul, proving URL/browser field policy wins.
- Remote auto entry/exit evidence: `captures\s03-app-profiles-20260902-remote-auto\remote-standard-active.png` shows `EN · QWERTY · Remote` and Remote keys in an auto-matched standard field; `remote-exit-chrome.png` immediately after leaving that package shows `EN · QWERTY` with no Remote state leakage.
- The same Remote-auto test kept password/number fields out of Remote; `field-password.png` and `field-number.png` are the pass frames.
- Test-only AVD preference edits were restored afterward to `keyboard_mode_last=english` and `remote_mode_enabled=false`.
- `uiautomator` intermittently returned a null root on this AVD; the smoke harness now retries dumps and validates screenshot pulls instead of treating a transient first failure as an app failure.

The Done gate is satisfied. The next roadmap session is S04 `04-text-tools.md`.
