# SUI04 — Secondary Surfaces and Responsive Cases

## Purpose
Audit and repair the specialized screens and constrained layouts that are easy to miss in the main settings pass.

## Read first
- `docs/agent-workflow.md`
- `docs/agent-handoff-current.md`
- `docs/ui-stabilization-2026-09/00-master-plan.md`
- SUI01 anomaly ledger/captures assigned to secondary surfaces
- SUI02/SUI03 completion notes so already-fixed shared components are not patched twice

## Scope
- Theme selector and theme editor, including search/filter/favorite/recent/A-B/system-pair controls.
- Layout Editor handles, value controls, apply/reset flows, QWERTY/Dingul previews.
- Text Tools/reserved phrase editing and any add/edit/delete dialogs.
- Backup/restore selection/confirmation/error surfaces.
- App profile editing, diagnostics/privacy/export/reset surfaces, Remote compatibility/configuration screens.
- Onboarding/practice/test surfaces and any reachable modal/pop-up not covered earlier.
- Constrained-height and landscape behavior for affected screens; verify controls remain reachable and scrolling does not trap content.

## Repair rule
Only repair SUI01 `FIX/BLOCKER` items or newly exposed defects caused by SUI02/SUI03 shared-component changes. Record new issues in the same ledger before fixing them.

## Runtime evidence
- Save before/after captures for every modified specialized surface.
- Include at least one constrained-height or landscape capture for each shared layout pattern changed here.
- For dialogs/panels, capture open and dismissed states to prove the underlying Activity/IME returns intact.

## Verification
- Focused tests for any touched editor/import/export/state-persistence logic.
- Confirm theme/layout previews still reflect runtime settings and do not mutate settings before Apply where that contract exists.
- Run `scripts/check.ps1` and `git diff --check`.

## Done gate
All specialized and responsive `FIX/BLOCKER` entries are resolved or explicitly accepted/reclassified, with evidence under `captures/ui-stabilization-202609/sui04-secondary/`. No feature behavior is changed merely to simplify layout.
