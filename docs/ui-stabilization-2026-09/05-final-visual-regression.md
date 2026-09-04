# SUI05 — Final Visual Regression and Closeout

## Purpose
Recapture the complete view matrix after all targeted repairs, catch only residual objective anomalies, and close the UI stabilization roadmap without turning the final pass into a redesign cycle.

## Read first
- `docs/agent-workflow.md`
- `docs/agent-handoff-current.md`
- `docs/ui-stabilization-2026-09/00-master-plan.md`
- SUI01 inventory/anomaly ledger
- SUI02-SUI04 completion notes and before/after artifacts

## Scope
- Re-run the complete SUI01 capture inventory against the final build.
- Compare final captures with baseline and confirm every intentional change maps to a ledger item.
- Inspect for regressions introduced by shared component/layout fixes: new clipping, excessive empty space, hidden controls, incorrect toolbar visibility, scroll loss, modal dismissal problems, or IME height changes.
- Resolve only newly demonstrated `FIX/BLOCKER` items. Do not pursue aesthetic refinements after the matrix is clean.
- Produce a concise final report listing changed surfaces, preserved surfaces, accepted `MINOR` items, and any external/device-only caveats.

## Final verification matrix
- Main settings in portrait plus representative constrained-height/landscape and system dark mode.
- QWERTY/Dingul ordinary IME, sensitive fields, Text Action/Text Tools, Remote on/off, and any state row/toolbar combinations changed.
- Theme selector/editor, Layout Editor, backup/restore, profiles, diagnostics/privacy, onboarding/practice and dialogs touched by prior sessions.

## Verification
- Run focused tests for any final fixes, then `scripts/check.ps1`.
- Run fresh Dingul typing smoke and representative app/field smoke if IME layout/chrome changed anywhere in the roadmap.
- Run `git diff --check` and inspect the intended file list.
- Ensure the final capture matrix has no unresolved `FIX/BLOCKER` rows.

## Documentation/VCS closeout
- Add `final-visual-verification.md` in this roadmap folder with capture paths and the final anomaly ledger summary.
- Update `docs/agent-handoff-current.md` so it points to no unfinished UI stabilization session.
- Mark SUI01-SUI05 `DONE` in the master ledger.
- Commit/push only after all intended verification passes when the user has requested full finalization.

## Done gate
SUI05 and the roadmap are `DONE` only when the post-fix visual inventory is complete, there are no unresolved objective UI defects in scope, automatic/runtime evidence is separated clearly, and the rolling handoff accurately describes any remaining external-only caveats.

## Completion — DONE (2026-09-04 KST)
- Final clean-AVD app/settings matrix completed at `captures\ui-stabilization-202609\sui05-final\app-settings-clean-avd`; it matches the SUI01 baseline filename set 127/127 with missing 0 and extra 0.
- Representative repaired portrait screens plus dark portrait and forced-landscape frames were visually reviewed; no new in-scope `FIX/BLOCKER` was found.
- Strict IME smoke completed at `captures\ui-stabilization-202609\sui05-final\ime-smoke-strict2`; the harness was tightened so IME-visible PASS requires the actual input view/decor to be visible.
- Fresh clean-data Dingul runtime probe passed 16/16 actions at `captures\dingul-typing-20260904-151422`.
- `scripts\check.ps1`, web builder syntax check, and static preview regeneration all pass after the final harness changes.
- Final report: `docs\ui-stabilization-2026-09\final-visual-verification.md`.
- Remaining release signing, real Windows receiver delivery, and physical-device/TalkBack checks are external/manual caveats, not unfinished SUI05 engineering.
