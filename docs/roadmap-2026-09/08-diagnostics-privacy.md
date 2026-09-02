# S08 - Diagnostics and Privacy Alignment

## Read first
- `docs/agent-workflow.md`, `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`
- `TouchBiasStore.java`, debug/probe/report code, `S3KeyboardService.java`
- `docs/privacy-notice.md`, `docs/play-data-safety-draft.md`, `docs/manual-test-checklist.md`

## Scope
1. Add a user-facing diagnostics screen/export that never includes raw typed text.
2. Include package/profile, EditorInfo class/type flags, effective policy, layout/mode, remote state, one-finger state, theme/material id, and recent action categories only.
3. Redact package/user identifiers where unnecessary and exclude clipboard/phrases/AI source or result text.
4. Add “copy diagnostic report” and optional file export with schema/version/timestamp.
5. Keep debug-only geometry/probe details separate from release-safe user diagnostics.
6. Add a reset diagnostics/input-learning action with explicit scope confirmation.
7. Update privacy notice and Play data-safety draft to match every persisted/logged field after S01-S07.
8. Add tests proving forbidden raw text/clipboard/provider credentials cannot enter reports.

## Runtime verification
- generate report in normal, password, browser, and remote/synthetic profiles.
- inspect report for required state and absence of typed text/clipboard/AI text.
- reset clears only documented diagnostic/learning state.
- release-safe screen works without debug-only probe dependencies.

## Done gate
Privacy docs, tests, and runtime report inspection pass. Mark S08=DONE and update handoff.