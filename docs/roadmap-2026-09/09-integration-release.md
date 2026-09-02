# S09 - Integration, Regression, Release Gate

## Read first
- `docs/agent-workflow.md`, `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`
- S01-S08 session files and their completion notes
- `docs/manual-test-checklist.md`, `docs/closed-beta-readiness.md`, `docs/development.md`, `README.md`
- build/check/smoke/runtime-capture scripts

## Scope
1. Audit S01-S08 completion against their actual code/tests/artifacts; do not trust ledger labels alone.
2. Run the full automatic verification suite and repair regressions without weakening tests.
3. Re-run core input regression: Dingul tap/directions, QWERTY Shift/Caps/Backspace, language toggle, composition/backspace, cursor move, one-finger continuous input.
4. Re-run representative real-app profiles: Chrome, Messages, password/number/web-edit, plus remote synthetic/real target if available.
5. Re-run representative theme parity/runtime captures and confirm no touch geometry or input layout regressions.
6. Verify AI/text actions, app profiles, Text Tools, backup/restore, theme management, remote UX, diagnostics together in one installed debug APK.
7. Update README/development/manual test/closed-beta/privacy/data-safety docs to current behavior.
8. Produce a final verification report with automatic, runtime, manual, blocked, and release-signing sections.

## Version-control gate
- Inspect pre-existing vs roadmap changes carefully; never blindly stage the whole mixed worktree.
- Propose focused commit boundaries and inspect staged diffs before each commit.
- Commit and push only when all non-release blockers are closed and the user's requested scope permits it.
- If signing properties are available, build/verify signed release; otherwise mark release signing BLOCKED with exact missing properties.

## Done gate
S01-S08 are independently verified, integration matrix passes, documentation is current, no non-external TODO remains, and `docs/agent-handoff-current.md` states either COMPLETE or only an explicit external signing/service blocker. Then mark S09=DONE and the master roadmap COMPLETE.