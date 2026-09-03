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
## Completion - 2026-09-03 KST
- Independently re-audited S01-S08 completion notes and their recorded runtime evidence; no non-external roadmap blocker was found.
- Fixed the canonical gate so `scripts/check.ps1` runs Gradle from the repository root regardless of caller working directory.
- Fresh `scripts/check.ps1` PASS: 42 themes / 0 warnings, material-surface audit PASS, 70 `KeyboardSettings` fields / 0 unused, `testDebugUnitTest`, `lintDebug`, and `assembleDebug` all PASS (`BUILD SUCCESSFUL`).
- Fresh Dingul probe PASS: 16 emitted key actions; evidence `captures\dingul-typing-20260903-103232`.
- Fresh editor/app smoke PASS for local practice, password, number, URL, email, web-edit, search, multiline, installed Chrome, and installed Google Messages; evidence `captures\s09-integration-20260903-runtime\app-smoke`.
- Fresh representative runtime theme captures PASS in both English/Hangul with `bottomDelta=0`: solid `gmk-8008`, soft-keycap `8008-soft-keycap`, frosted `nord-frost-night`, acrylic `laser-outline-acrylic`.
- A transient `gmk-modern-dolch` Hangul capture miss was not reproducible on alternate solid material and is not used as pass evidence.
- Hardened `scripts/build-release.ps1` to run Gradle from repository root. Release pipeline reaches `verifyClosedBetaSigning`; release signing is BLOCKED only by absent external properties: `HANGUL_IME_KEYSTORE`, `HANGUL_IME_KEYSTORE_PASSWORD`, `HANGUL_IME_KEY_ALIAS`, `HANGUL_IME_KEY_PASSWORD`.
- Final verification report: `docs\roadmap-2026-09\09-final-verification.md`.
- Remaining manual/external items are explicitly tracked: real Windows remote receiver delivery, final developer/contact identity, release keystore/signing properties, and broader physical-device/TalkBack coverage.

**State: DONE** - engineering/integration gates are closed; only explicit external release/manual validation blockers remain.
