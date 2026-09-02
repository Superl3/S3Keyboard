# S01 - Enter Text Actions and Undo

## Read first
- `docs/agent-workflow.md`
- `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`
- `app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java`
- `KeyboardCommands.java`, `KeyboardCommandDispatcher.java`, `S3KeyboardCommandTarget.java`
- `ImeConnectionDispatcher.java`, `EditorPolicy.java`, `KeyboardPreferences.java`

## Scope
1. Audit the existing `CMD_CORRECT_TEXT`/Enter slide behavior before adding anything.
2. Define a small `TextAction` model: correct, polish, shorter, polite, translate, restore-original.
3. Add a focusable IME action panel invoked only by explicit Enter gesture/long action; normal Enter remains unchanged.
4. Extract only the selected text or bounded current sentence through `InputConnection`; never capture an entire password/sensitive field.
5. Implement a deterministic local `correct` path first so the feature is testable without network credentials.
6. Store one reversible pre-transform snapshot per active editor/session and provide immediate Undo/Restore.
7. Disable actions in password, number, remote/raw-key, or unsupported editor policies.
8. Add focused unit tests for extraction bounds, replacement, selection restoration, disabled policies, and undo.

## Runtime verification
- multiline local field: correction changes only target sentence.
- selection: only selected range changes.
- undo restores exact original text/cursor.
- password/number/remote fields do not expose the action.
- normal Enter, newline, long-press/slide semantics outside the new action remain unchanged.

## Done gate
`check.ps1`, targeted tests, `git diff --check`, and real IME runtime evidence pass. Update master ledger S01=DONE and handoff. Do not begin S02 before this gate.