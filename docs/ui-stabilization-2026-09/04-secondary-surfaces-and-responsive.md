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

## Completion — 2026-09-04 KST
- SUI01's five secondary-activity status-bar `FIX` items are resolved. `ThemeSelectorActivity`, `ThemeEditorActivity`, `AccentPlacementActivity`, `DiagnosticsActivity`, and `BackupRestoreActivity` now apply the shared runtime top inset to their content root before `setContentView`, matching the SUI03 MainActivity fix without changing feature behavior.
- Theme selector's per-card A/B/light/dark management row was also a captured SUI01 `FIX`. A temporary one-line/ellipsis treatment still truncated `☀ 라이트`/`☾ 다크`, so the final layout uses two equal-width rows: `A | B`, then `☀ 라이트 | ☾ 다크`. The final portrait capture shows all four labels intact.
- Canonical portrait evidence is `captures/ui-stabilization-202609/sui04-secondary/`; the latest `theme-selector-top.png`, `theme-editor-top.png`, `accent-placement-top.png`, `diagnostics-top.png`, and `backup-restore-top.png` all start below the status bar. Layout Editor and expanded Remote surfaces were rechecked and remain `KEEP`.
- Constrained-height evidence is under `captures/ui-stabilization-202609/sui04-secondary/landscape/`. Theme Selector and Theme Editor both preserve the fixed inset and usable content in landscape; Theme Editor's full preview remains visible. Broader automated landscape traversal was not used as pass evidence because repeated `uiautomator` polling destabilized the audit AVD/System UI; no production code change was made for that harness-only instability.
- Focused `ProductionReadinessConfigTest` + `assembleDebug` PASS. Canonical `scripts/check.ps1` PASS (`testDebugUnitTest`, lint, assemble, theme/material/settings audits), and `git diff --check` reports no whitespace errors.
- No visual redesign, input behavior, editor/import/export semantics, or persistence behavior was changed. All SUI01 secondary-surface `FIX` entries are resolved; no SUI04 `BLOCKER` remains.
