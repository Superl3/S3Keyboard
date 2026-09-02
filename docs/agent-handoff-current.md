# Current Agent Handoff

Updated: 2026-09-02 11:28 KST
Repository: `C:\Users\bug95\Documents\Codex\2026-05-19\mimic-apk-ux`
Branch: `main`
Base HEAD when this work started: `1cbcfb7`

## How to resume

1. Read `docs/agent-workflow.md` first.
2. Then read this file in full before touching the worktree.
3. Do not reset/revert/stash away existing changes.
4. Treat this file as the canonical rolling handoff; update it before ending a session.
5. Keep automatic verification and runtime/manual verification separate in reports.

## Hard constraints

- Do not alter input arrays, Hangul automata, touch hit-testing, or one-hand input behavior as part of theme stabilization.
- Do not reintroduce Accessibility/screen-capture based visual effects.
- Do not expose `experimental_refraction` in Android or web UI.
- Legacy JSON import may map `experimental_refraction` to `frosted`; export must remain `frosted`.
- Supported materials are exactly: `solid`, `soft_keycap`, `frosted`, `acrylic`.
- Do not push until all intended verification is complete.

## Completed implementation

- Removed built-in themes `liquid-aurora`, `liquid-frost`, `liquid-graphite`.
- Removed Accessibility backdrop capture/service/store/controller and AGSL/RuntimeShader refraction path.
- Removed experimental refraction UI/options/prompt-contract/runtime/static-preview/capture handling.
- Kept one-way legacy import migration `experimental_refraction` -> `frosted` in Android and web.
- Theme source count is 42.
- Regenerated Android preset source, web contract/index, static previews, and theme report through existing generators.
- Added `tools/check-material-surfaces.mjs` and wired it into `scripts/check.ps1`.
- Consolidated runtime capture to `scripts/capture-all-theme-runtime.ps1`.
- Runtime capture success is geometry-aware and atomic; failed frames cannot overwrite final PNGs.
- `S3KeyboardService` debug audit logs theme/mode/view geometry/screen/nav inset/content bottom/expected bottom/delta.

## Runtime theme audit

Canonical new audit folder:
`captures\runtime-theme-audit-20260901-geometry-final`

Verified independently:
- 42 themes
- QWERTY + Dingul
- 84 PNG
- 84 manifest rows
- 84 summary rows
- 0 zero-byte files
- 0 missing theme/mode combinations
- 0 duplicate combinations
- 0 capture failures
- every stored frame had `bottomDelta=0`

## Visual audit result

Priority families visually compared between static preview and real IME:
GMK solid, minimal solid, outline, soft keycap, frosted, acrylic, Dots/display overrides, Marigold/colorful foreground.

No common classification/data defect requiring a new theme-color patch was found.
Runtime/static differences observed were consistent with renderer/blur/light differences rather than role inversion.

## Automatic verification status

Latest successful checks after smoke-harness edits:
- `rtk powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1` -> PASS
- theme validation -> 0 warnings
- material surface cross-check -> PASS: solid, soft_keycap, frosted, acrylic
- KeyboardSettings usage audit -> 70 fields, 0 unused
- `testDebugUnitTest` -> PASS
- `lintDebug` -> PASS
- `assembleDebug` -> PASS
- `rtk node --check web-theme-builder\app.js` -> PASS
- `rtk git diff --check` -> PASS

Release signing is not available in the current environment.
Required Gradle properties are absent: `HANGUL_IME_KEYSTORE`, `HANGUL_IME_KEYSTORE_PASSWORD`, `HANGUL_IME_KEY_ALIAS`, `HANGUL_IME_KEY_PASSWORD`.
Do not describe debug success as release success.

## Runtime input smoke status

`captures\smoke-ime-20260901-final` was produced on `emulator-5558`.
The smoke harness was fixed to pull `uiautomator` XML as UTF-8 instead of parsing corrupted console text, and now retries transient dump failures.

Synthetic field checks with actual IME visibility passed for:
- local practice
- password
- number
- URL
- email
- web-edit
- search
- multiline

Follow-up real-app artifacts are in `captures\runtime-input-followup-20260901`.

- Chrome first-run setup was completed and the real `com.android.chrome:id/url_bar` EditText was focused with S3 selected; `mInputShown=true` and `chrome-url-ime.png` confirm a real Chrome URL/search field pass.
- Google Messages first-run setup was completed and the real recipient `ContactSearchField` EditText on the New conversation screen was focused with S3 selected; `mInputShown=true` and `messages-recipient-ime.png` confirm a real Messages editable-field pass.
- This does not prove SMS sending or typing in the message-body composer; only the real recipient field was verified.
- Most other target apps are not installed on the audit AVD.
- System WebView provider is installed but has no directly launchable test surface.

## Dingul probe status

`scripts\smoke-dingul-typing.ps1` was hardened to accept `-Serial`, avoid ambiguous emulator selection, locate the practice EditText through UTF-8 uiautomator XML, retry transient dumps, and force `keyboard_mode=hangul`.
The previous `Dingul typing plan was incomplete` state is no longer reproducible with the current APK/AVD state; no input-code change was required.

Latest run on `emulator-5558`:
- command: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-dingul-typing.ps1 -SkipBuild -SkipEmulatorLaunch -Serial emulator-5558`
- result: `Dingul typing probe passed: 16 key actions`
- artifacts: `captures\dingul-typing-20260901-133913`
- `probe-plan.txt`: 20 PLAN rows
- `probe-emits.txt`: 16 expected emitted actions
- final input-method state has S3 selected and `mInputShown=true`

## Runtime key behavior follow-up

All items below were exercised on `emulator-5558` without changing production input, Hangul automata, hit-testing, or one-finger behavior.

- QWERTY direct input: `q -> qa`.
- one-shot Shift: `qa -> qaA`.
- long-press Caps Lock: two following A taps produced `qaAAA`; `qwerty-caps-on.png` also shows uppercase legends and the lock marker.
- Backspace after Caps input: `qaAAA -> qaAA`.
- Hangul/English toggle: the keyboard visibly changed QWERTY -> Dingul using the language key's safe left-side tap area. A first test too close to the system switcher opened Android's input-method chooser and is not used as pass evidence.
- Hangul composing Backspace: `가` (`U+AC00`) -> `ㄱ` (`U+3131`).
- Backspace after explicit composition commit: `가 ` -> `가` -> empty suffix, so the committed syllable was deleted as a whole rather than decomposed.
- Spacebar cursor-left then new input: `qaAAb -> qaAAab`, proving insertion moved before the trailing `b`.
- One-finger continuous Dingul input: one injected DOWN/MOVE/UP stream committed `ㄱ` then `ㄴ` without lifting. The EditText appended `U+3131,U+3134`; `one-finger-logcat.txt` records `BEGIN -> COMMIT -> HOVER -> SELECT -> COMMIT` for the same stream.
- The one-finger setting was enabled through the app's official settings UI for the test and restored to `checked=false` afterward.

Canonical follow-up artifacts:
`captures\runtime-input-followup-20260901`

## Product expansion roadmap

A new end-to-end roadmap is now canonical at `docs\roadmap-2026-09\README.md` and `docs\roadmap-2026-09\00-master-plan.md`.
It splits the next product work into nine gated sessions, each with an explicit reading list, scope, runtime verification, and Done gate.

Session order:
1. S01 Enter text actions + reversible correction/undo.
2. S02 AI provider abstraction + privacy/failure handling.
3. S03 per-app profiles + runtime state indicators.
4. S04 unified clipboard/reserved/pinned Text Tools.
5. S05 versioned backup/restore.
6. S06 theme favorites/recent/filter/light-dark pairing/A-B preview.
7. S07 remote-mode presets/navigation/modifier-state productization.
8. S08 release-safe diagnostics + privacy/data-safety alignment.
9. S09 integration/regression/docs/version-control/release gate.

S01 and S02 are DONE. The next active implementation session is S03: `docs\roadmap-2026-09\03-app-profiles-status.md`.
Both runtime gates passed on the fresh test AVD `hangul_gesture_s01` (`emulator-5558`); the master ledger is DONE for S01 and S02.

### S01 Enter text actions — DONE

Implemented on 2026-09-02 KST without changing input arrays, Hangul automata, touch hit-testing, or one-finger semantics:
- Existing QWERTY Enter tap remains normal Enter and long-press remains explicit newline; existing Enter up-slide `CMD_CORRECT_TEXT` now opens a focusable IME text-action panel.
- Added `TextAction` model entries for correct, polish, shorter, polite, translate, and restore-original. Only deterministic local `correct` and restore are enabled in S01; AI-backed actions stay visibly disabled for S02.
- Added bounded selection/current-sentence extraction using `InputConnection.getSurroundingText(512, 512, 0)` on Android 12+ and bounded `ExtractedText` fallback on older APIs. Password/number/raw/remote/replaced-row policies reject text actions before extraction.
- Added deterministic local correction for whitespace/punctuation plus a small fixed English typo map; no network/provider path exists in S01.
- Added one per-editor/session `TextActionTransaction` snapshot containing the exact original range/text and original selection. Both the panel Restore action and existing Undo command restore the original text and cursor/selection.
- Text editing remains behind `InputConnectionTextOperator`; `ProductionReadinessConfigTest` caught and prevented a direct `InputConnection.commitText` regression during implementation.
- Runtime testing caught a second real regression: the initial focusable `PopupWindow` action panel caused Android to hide the IME with `HIDE_WINDOW_GAINED_FOCUS_WITHOUT_EDITOR`. The action surface now lives inside `inputRoot` as a focusable in-IME overlay, and `ProductionReadinessConfigTest.textActionPanelStaysInsideImeWindow` locks that architecture in.
- Snapshot/overlay state is cleared on input-session transitions and service destruction.

Automatic verification after the final patch:
- `TextActionTest` + `EditorInputPolicyTest` -> PASS.
- `TextActionTest`, `EditorInputPolicyTest`, `KeyboardLayoutFactoryTest`, `KeyboardCommandDispatcherTest`, `ImeConnectionDispatcherTest` -> PASS.
- `ProductionReadinessConfigTest` + S01 tests -> PASS.
- `rtk powershell -NoProfile -ExecutionPolicy Bypass -Command "Set-Location '<repo>'; .\\scripts\\check.ps1"` -> PASS (`testDebugUnitTest`, `lintDebug`, `assembleDebug`; `BUILD SUCCESSFUL in 1m 7s`).
- `rtk git diff --check` -> PASS; only existing LF/CRLF warnings were printed.

S01 runtime verification — PASS:
- The stale original `emulator-5554` / locked `hangul_gesture_demo` remains untouched; S01 was verified on a fresh AVD `hangul_gesture_s01` running as healthy `emulator-5558`.
- Canonical evidence: `captures\\s01-text-actions-20260902-runtime`.
- Current-sentence scope: `first sentence. teh middle. tail sentence.` -> `first sentence. The middle. tail sentence.` with surrounding sentences unchanged (`10-corrected.*`).
- Restore returned the exact original text (`11-restored.*`). A separate length-changing correction/restore test then inserted `q` at the exact original `middle|.` cursor position, yielding `middleq.` (`12-cursor-marker.*`).
- Explicit selection scope: selecting only `teh` in `teh adn middle` yielded `The adn middle`; the separate `adn` typo was untouched (`13-selection-before.png`, `14-selection-corrected.*`). After Restore, `q` replaced the restored selected `teh` range, yielding `q adn middle` (`15-selection-restore-marker.*`).
- Password and number profiles exposed no text-action panel (`16-password-blocked.png`, `16-number-blocked.png`).
- Remote mode was enabled only in the test AVD preference file and confirmed as `remote_mode_enabled=true`; Enter up-slide exposed no action panel (`17-remote-blocked.png`). The preference was immediately restored to `false`.
- Normal Enter tap preserved `beforeafter` unchanged and followed the existing SEND behavior that hides the demo IME (`18-enter-tap.*`).
- Enter long-press produced `before<NL>after` and kept `mWindowVisible=true` (`19-enter-longpress.*`).
- Valid post-fix action-panel evidence begins at `09-overlay-panel-new.png`; the earlier pre-fix captures document the rejected focus-stealing PopupWindow behavior and are not pass evidence.

### S02 AI provider path, privacy, failure handling — DONE

Implemented on 2026-09-02 KST without changing input arrays, Hangul automata, touch hit-testing, or one-finger semantics:
- Added a vendor-free provider abstraction: `TextActionProvider`, `TextActionProviderRequest` (bounded S01 range only, `MAX_TEXT_CHARS = 2048`), `TextActionProviderResult`, `TextActionProviderError`, `TextActionTaskScheduler`, and `TextActionProviderClient` (timeout, cancellation, malformed/empty-result rejection, unavailable-provider handling). UI and `InputConnection` code depend only on these types.
- `AiTextActionSettings` + `KeyboardPreferences.loadAiTextActionSettings` persist enablement, provider id, timeout, and translate target only. Request text and results are never persisted. `KeyboardSettingsSchema`/its test cover the new fields.
- The only shipped provider is `LocalTestTextActionProvider` (on-device, no network). It supports correct/polish/shorter/polite/translate and reacts to test markers `[[fail]]`, `[[slow]]`, `[[timeout]]` for runtime failure drills.
- Privacy gate below the UI: `TextActionProviderRequest.build` denies password/number/URI/email/web-edit/raw-key/replaced-row/remote policies before any provider is touched; the UI additionally disables AI actions while the provider is off.
- Provider results are never applied silently. `PendingProviderTextAction` holds the preview; the in-IME overlay shows loading state ("원문은 아직 변경되지 않았습니다"), before/after preview with Apply/Cancel, an error panel with 다시 시도, and Restore/Undo keep working through the S01 `TextActionTransaction`.
- Local deterministic `correct` remains the no-network fallback when the provider is disabled.
- `docs/privacy-notice.md` and `docs/play-data-safety-draft.md` now describe the on-device test provider, the 2,048-character cap, non-persistence, and the re-review required before any external provider is added. The old Accessibility/Glass capture statements were removed.

Automatic verification (artifacts timestamped 2026-09-02 10:20 KST, after the last source edit at 10:18):
- `TextActionProviderTest` 9/9 PASS: `requestContainsOnlyResolvedTargetText`, `sensitiveRawNumberAndRemoteRequestsAreDeniedBelowUi`, `oversizedProviderPayloadIsRejected`, `clientDeliversSuccessfulResultAndCancelsTimeout`, `clientTimeoutCancelsProviderAndReturnsTimeout`, `clientCancellationLeavesProviderCancelled`, `clientRejectsMalformedAndEmptyResults`, `unavailableProviderFailsWithoutSchedulingTimeout`, `localTestProviderSupportsEveryProviderAction`.
- `testDebugUnitTest` result folder has no failing suite; `lintDebug` report and `app-debug.apk` were regenerated at the same time.

S02 runtime verification — PASS on `emulator-5558`, evidence `captures\s02-ai-provider-20260902-runtime` (uiautomator XML + PNG per step):
- Provider off: AI actions shown disabled (`01`/`02`), local correct `teh sampl` -> `The sample` (`06-provider-off-correct.xml`).
- Provider on (`07-provider-on-panel.png`): polish `The sample` -> `Polished · The sample` (`08`/`09`), correct `teh sample` -> `The sample` (`12`/`13`), shorter -> `Short · teh sample` (`14`/`15`), polite -> `Please · The sample` (`16`/`17`), translate -> `[ko] teh sample` (`18`/`19`); each showed a preview frame before the applied frame. Restore returned `teh sample` (`20-provider-restore.xml`).
- Failure (`[[fail]] sample`, `21`), cancel during loading (`[[slow]] sample`, `22`/`23`), timeout (`[[timeout]] sample`, `24`): editor text unchanged in every case.
- Password, number, and remote profiles exposed no provider action (`25-password-blocked.png`, `25-number-blocked.png`, `26-remote-blocked.png`). Remote mode was toggled only in the test AVD preference and restored.
- Timeout/translate-target configuration changed to 10s / en and back (`27`/`28`); final sanity correct pass (`29`-`31`).
- Frames `03-after-miss.png`, `03-provider-off-correct.xml`, `04-provider-off-touch-fixed.png`, `05-current-panel.png` document an injected-touch coordinate miss and its correction; they are not pass evidence.
- Not covered: a real network provider does not exist in this artifact, so "privacy UI and actual request gate agree" was verified only against the request-builder gate and the local test provider.

## Remaining pre-roadmap verification caveats

- Messages recipient-field input is verified, but message-body typing/send delivery is still unverified; S09 should include it if feasible.
- Release verification remains blocked until real closed-beta signing properties are available.
- The worktree still mixes pre-existing changes with stabilization work; do not blindly stage everything.

## Git / commit caution

The worktree already contained substantial uncommitted theme/style/layout changes before this stabilization work began.
Current changes mix prior work and this stabilization in some of the same files.
Do not create broad commits by blindly staging the entire worktree unless the user explicitly accepts combining the pre-existing changes.
On 2026-09-02 the whole worktree (theme stabilization + S01 + S02 + docs) was committed as a single commit at the user's explicit request; the pre-existing mixed changes were accepted as combined. No push has been made.
Recommended conceptual commit titles remain:
- `Drop experimental Liquid refraction`
- `Make runtime theme captures geometry-aware`
- `Complete theme runtime audit and beta gates`

## Useful artifacts

- S01 text-action runtime: `captures\s01-text-actions-20260902-runtime`
- S02 AI provider runtime: `captures\s02-ai-provider-20260902-runtime`
- Runtime theme audit: `captures\runtime-theme-audit-20260901-geometry-final`
- Runtime manifest: `captures\runtime-theme-audit-20260901-geometry-final\capture-manifest.csv`
- Runtime summary: `captures\runtime-theme-audit-20260901-geometry-final\capture-summary.csv`
- Field smoke: `captures\smoke-ime-20260901-final\smoke-ime-apps-report.json`
- Dingul typing smoke: `captures\dingul-typing-20260901-133913`
- Runtime input follow-up: `captures\runtime-input-followup-20260901`
- One-finger runtime log: `captures\runtime-input-followup-20260901\one-finger-logcat.txt`
- Static previews: `captures\theme-previews`
- Theme classification report: `captures\theme-classification-report.html`
