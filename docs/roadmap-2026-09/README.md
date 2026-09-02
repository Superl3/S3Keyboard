# Roadmap Session Entry Point

For every new implementation session:

1. Read `docs/agent-workflow.md`.
2. Read `docs/agent-handoff-current.md`.
3. Read `docs/roadmap-2026-09/00-master-plan.md`.
4. Find the first session whose ledger state is not DONE.
5. Read that session file only plus its `Read first` list.
6. Implement the complete scope; do not cherry-pick only the easy items.
7. Satisfy its Done gate before marking it DONE.
8. Update both the master ledger and `docs/agent-handoff-current.md` with exact tests/artifacts/blockers.
9. Stop at the session boundary unless the user explicitly asks to continue into the next session.

Current next session at roadmap creation: **S01 - `01-enter-text-actions.md`**.

A future agent must not infer completion from source presence. Completion requires the session's specified automatic and runtime evidence.