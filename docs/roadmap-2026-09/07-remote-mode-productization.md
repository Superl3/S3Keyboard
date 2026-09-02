# S07 - Remote Mode Productization

## Read first
- `docs/agent-workflow.md`, `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`
- `docs/remote-mode.md`
- remote app catalog/profile classes, remote input controller
- `KeyboardLayoutFactory.java`, `S3KeyboardService.java`, remote tests/scripts

## Scope
1. Preserve existing KeyEvent transport contract and remote bottom-row semantics.
2. Add first-class Parsec, Moonlight, Microsoft RDP, Chrome Remote Desktop, Steam Link, AnyDesk, TeamViewer presets.
3. Add compact remote toolbar/navigation access for Esc, Tab, arrows, Home/End, PgUp/PgDn, Insert/Delete, F1-F12.
4. Surface Ctrl/Alt/Win latch vs lock states clearly and make clearing behavior explicit.
5. Add app-entry auto-enable and app-exit auto-restore through S03 profile system.
6. Never claim Android-side acceptance proves delivery to remote Windows; keep manual matrix evidence model.
7. Ensure remote mode bypasses incompatible theme display overrides and composing/text convenience paths.
8. Extend export/manual compatibility scripts where necessary; add state-machine tests.

## Runtime verification
- enter/exit at least one installed remote target or synthetic profile and verify state restoration.
- latch/lock visuals match actual modifier state and clear on session end.
- navigation/function controls emit expected Android KeyEvents locally.
- manual remote Windows delivery remains explicitly unverified unless a real remote session is available.

## Done gate
Local runtime/automatic tests pass and remote evidence limitations are documented. Mark S07=DONE and update handoff.