# UI Stabilization Session Entry Point

This roadmap starts after the 2026-09 product-expansion roadmap and the installable beta publishing work are treated as complete.

For every new UI stabilization session:

1. Read `docs/agent-workflow.md`.
2. Read `docs/agent-handoff-current.md`.
3. Read `docs/ui-stabilization-2026-09/00-master-plan.md`.
4. Find the first session whose state is not `DONE`.
5. Read only that session file plus its explicit `Read first` list.
6. Capture the relevant runtime views before deciding what is wrong.
7. Fix only clearly abnormal layout/usability behavior; do not redesign healthy views.
8. Recapture the modified views and satisfy the session Done gate.
9. Update the master ledger and `docs/agent-handoff-current.md` before stopping.

Do not start the next session unless the user explicitly asks to continue beyond the current session boundary.

Roadmap state: **COMPLETE. SUI01 through SUI05 are DONE; there is no next SUI session.**
