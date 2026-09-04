# SUI03 — Settings Layout and Hierarchy

## Purpose
Repair baseline-confirmed layout and usability defects in the main settings experience while preserving established behavior and visual language.

## Read first
- `docs/agent-workflow.md`
- `docs/agent-handoff-current.md`
- `docs/ui-stabilization-2026-09/00-master-plan.md`
- SUI01 anomaly ledger/captures assigned to settings
- `MainActivity` and shared settings view builders/styles used by affected sections

## Scope
- Fix clipping, overlap, broken wrapping, inconsistent row sizing, unreachable controls, nested scrolling problems, and fixed-header/content collisions.
- Review the existing step/section order only where the capture ledger identifies a real discoverability or reachability defect.
- Reorder related controls minimally so frequent/runtime-impacting controls are reachable before advanced/diagnostic controls when the current placement is objectively confusing.
- Preserve existing labels, values, persistence keys, and feature behavior unless a documented defect requires a wording/state fix.
- Reuse shared settings components rather than one-off visual patches when a common component is proven to cause multiple defects.

## Explicit checks
- Quick Start and top navigation/search.
- Continuous input, layout, input feel, display/theme, Android/IME, Text Tools, Remote, backup, diagnostics/privacy entry points.
- Long Korean labels/help text, multi-button rows, steppers, spinners, checkboxes, expandable sections, and scroll restoration.

## Runtime evidence
For every changed settings area, save before/after portrait captures and hierarchy XML. Also sample system dark mode if a shared component or text/icon contrast changed.

## Verification
- Focused tests for settings persistence/navigation/recreation where touched.
- Rotate/recreate at least one affected step and verify selected step/search state remains intact.
- Confirm the inline practice field and fixed-vs-full-scroll switching still work when the IME opens.
- Run `scripts/check.ps1` and `git diff --check`.

## Done gate
All SUI01 main-settings `FIX/BLOCKER` entries are resolved or explicitly reclassified. No healthy settings section is restyled without an anomaly reference. Evidence lives under `captures/ui-stabilization-202609/sui03-settings/` and the ledger records every intentional reorder.

## Completion — 2026-09-04 KST
- SUI01's only main-settings `FIX` was the Android 15 edge-to-edge status-bar collision shared by all eight wizard steps. `SettingsSystemBars.applyTopInset(View)` now applies the runtime status inset to the MainActivity root, and MainActivity opts into it without changing section order, labels, persistence, or control behavior.
- Fresh post-fix matrix is `captures/ui-stabilization-202609/sui03-settings/full-matrix/`: all eight settings steps have top + three scroll PNG/XML pairs. Representative step 1/2/5/8 captures show the app title fully below the status icons and otherwise unchanged content geometry.
- Rotation/recreation check used the real window-manager user-rotation lock. Step 3 `레이아웃` remained selected in landscape and again after returning to portrait (`rotate-valid-landscape.png`, `rotate-valid-portrait-after.png`).
- Inline practice-field/IME check is `practice-ime-open-valid.png` plus `practice-ime-open-valid-input_method.txt`; the S3 IME opens over the Quick Start practice field while the fixed title/header remains correctly inset and the content remains usable.
- Focused `ProductionReadinessConfigTest` + `assembleDebug` PASS. Canonical `scripts/check.ps1` PASS (`testDebugUnitTest`, lint, assemble, theme/material/settings audits), and `git diff --check` reports no whitespace errors.
- No settings reorder or aesthetic restyle was justified by the baseline, so none was performed. SUI01's shared MainActivity settings defect is resolved; secondary-activity status-inset defects remain intentionally assigned to SUI04.
