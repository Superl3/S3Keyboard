# Product Expansion Master Plan

Updated: 2026-09-02 KST
Repository: `C:\Users\bug95\Documents\Codex\2026-05-19\mimic-apk-ux`

## Goal
Turn the stabilized keyboard into a polished product without destabilizing Dingul/QWERTY input, Hangul composition, touch resolution, or one-finger continuous input.

## Mandatory session protocol
1. Read `docs/agent-workflow.md` and `docs/agent-handoff-current.md` first.
2. Read only the assigned session file plus its explicit reading list.
3. Preserve all existing worktree changes; never reset/revert/stash unrelated work.
4. Finish the session acceptance criteria before starting the next session.
5. Separate automatic verification from runtime/manual verification in reports.
6. Update this roadmap progress and `docs/agent-handoff-current.md` before ending.
7. If blocked by credentials/external service/device, finish every non-blocked item and record the exact blocker.

## Global constraints
- Do not redesign input arrays, Hangul automata, touch hit-testing, or one-finger gesture semantics.
- Do not reintroduce Accessibility/screen-capture visual effects.
- Password/sensitive fields must disable unsafe text capture/history/actions.
- AI/text transformation must be explicit user action, never per-keystroke interception.
- Every destructive text transform must support immediate undo/restore.
- New persisted data requires privacy/data-safety review.
## Session order
- S01 `01-enter-text-actions.md`: Enter action surface, local correction pipeline, undo contract.
- S02 `02-ai-provider-privacy.md`: pluggable AI provider path, consent/privacy, failure handling.
- S03 `03-app-profiles-status.md`: per-app profiles, automatic policy UI, state indicators.
- S04 `04-text-tools.md`: clipboard + reserved phrases + pinned text tools.
- S05 `05-backup-restore.md`: versioned backup/restore of settings and user data.
- S06 `06-theme-management.md`: favorites, recent, filters, light/dark pairing, runtime preview UX.
- S07 `07-remote-mode-productization.md`: remote presets, toolbar/navigation, modifier state UX.
- S08 `08-diagnostics-privacy.md`: safe diagnostics/report export and privacy alignment.
- S09 `09-integration-release.md`: cross-feature integration, regression matrix, docs, commit/push/release gate.

## Progress ledger
| Session | State | Gate |
|---|---|---|
| S01 | DONE | local text actions + exact restore pass; real IME matrix passed on fresh `hangul_gesture_s01` / `emulator-5558`, including sentence/selection scope, cursor/selection restore, password/number/remote blocking, Enter tap/long-press/slide regression |
| S02 | DONE | provider abstraction + privacy/failure pass; fake-provider tests (success/timeout/cancel/malformed/unavailable/sensitive denial/payload cap) pass; real IME matrix passed on `emulator-5558` with the on-device test provider: provider-off local correct, polish/shorter/polite/translate/correct preview -> Apply, failure/cancel/timeout leave editor unchanged, password/number/remote blocked, timeout/translate-target config; evidence `captures\s02-ai-provider-20260902-runtime` |
| S03 | TODO | app override + state UI runtime pass |
| S04 | TODO | text tools insertion + sensitive-field pass |
| S05 | TODO | round-trip + migration pass |
| S06 | TODO | management UX + preview parity pass |
| S07 | TODO | remote preset/state runtime pass |
| S08 | TODO | redacted report + privacy docs pass |
| S09 | TODO | full checks + runtime matrix + release/commit gate |

## Definition of done
The roadmap is complete only when S01-S09 are DONE, all non-external blockers are closed, current handoff points to no unfinished product work, and the final verification report clearly separates debug/runtime/release evidence.