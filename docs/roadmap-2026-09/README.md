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

This roadmap is complete: **S01-S09 are DONE**.

The next independent workstream is the UI stabilization roadmap at `docs/ui-stabilization-2026-09/README.md`. A future agent must not reopen S01-S09 unless a regression is directly traced to that completed work.