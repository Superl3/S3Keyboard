# S04 - Unified Text Tools

## Read first
- `docs/agent-workflow.md`, `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`
- clipboard/history/reserved-phrase code and settings UI
- `S3KeyboardService.java`, `KeyboardPreferences.java`, `EditorPolicy.java`
- `docs/privacy-notice.md`, `docs/play-data-safety-draft.md`

## Scope
1. Merge clipboard history, pinned clips, and reserved phrases into one Text Tools panel.
2. Preserve existing insertion behavior; do not alter key hit-testing or gesture semantics.
3. Add pin/unpin, rename for user phrases, delete, clear history, and recent ordering.
4. Keep user-created pinned items local by default and version their persistence schema.
5. Disable history/panel insertion where editor policy marks a password/sensitive field.
6. Never capture raw typed text just to populate history; clipboard source stays clipboard/user-created items only.
7. Add optional “recent transformed result” slot only if S01/S02 explicitly opts in; default off.
8. Add focused tests for ordering, dedupe, pin persistence, sensitive-field suppression, and insertion.

## Runtime verification
- insert a recent clipboard item into normal field.
- pin item, restart IME/app, verify persistence.
- insert reserved phrase and edit/delete it.
- password field does not expose unsafe clipboard/history content.
- clear history leaves pinned/user phrases according to defined contract.

## Done gate
Checks, privacy review, and runtime evidence pass. Mark S04=DONE and update handoff.
## Completion — 2026-09-03 KST
- State: **DONE**.
- Unified Text Tools persistence, ordering, pin/edit/delete/clear behavior and sensitive-field suppression are covered by the current unit suite.
- Runtime evidence: `captures\s04-runtime` (normal insertion, recent item, restart persistence, reserved-phrase editing and panel state).
- Privacy contract remains clipboard/user-created source only; no raw typed-text capture was added.
