# SUI01 — Baseline Capture Inventory

## Purpose
Create a complete visual inventory before changing product UI. This session is primarily observation and capture automation; it should not perform aesthetic cleanup.

## Read first
- `docs/agent-workflow.md`
- `docs/agent-handoff-current.md`
- `docs/ui-stabilization-2026-09/00-master-plan.md`
- `docs/feature-catalog.md`
- existing runtime capture/smoke scripts relevant to settings and IME surfaces

## Scope
- Enumerate every user-visible Activity, major settings section, editor/tool screen, modal/dialog/popup, and meaningful IME runtime state.
- Capture the launcher/settings flow at its normal portrait size, including scrolled states needed to expose every section.
- Capture ordinary QWERTY and Dingul IME, number row variations, Caps/continuous-input indicators, sensitive fields, Text Action panel, Text Tools, and Remote state.
- Include current theme selector/editor, layout editor, backup/restore, diagnostics/privacy, app profile, Remote configuration/test surfaces, and any onboarding/practice screen still reachable.
- Capture UI hierarchy XML alongside PNG where practical so clipped/overlapping bounds can be verified objectively.

## Required artifact
Create `captures/ui-stabilization-202609/baseline/` plus `view-inventory.md` or CSV mapping each capture to its surface and state.

## Anomaly ledger
For every captured surface, classify it as:
- `KEEP`: visually and functionally normal; do not modify.
- `MINOR`: imperfect but not worth destabilizing the UI; document only unless later evidence raises severity.
- `FIX`: clear clipping/overlap/sizing/visibility/hierarchy/reachability problem.
- `BLOCKER`: prevents normal use or makes the intended control/state inaccessible.

Each `FIX/BLOCKER` row must record: capture filename, reproduction state, exact abnormality, expected behavior, suspected owner/component, and target session (SUI02-SUI04).

## Explicit checks
- Confirm whether the clipboard-looking IME top bar is present in ordinary typing and exactly which state/config causes it.
- Look for horizontal overflow, clipped Korean text, uneven button rows, cards extending off-screen, nested-scroll traps, dead space, overlapping fixed headers, and controls hidden behind the IME/system bars.
- Do not label a merely subjective color/shape preference as a defect.

## Verification
- Build/install current `main` without product changes.
- Ensure capture inventory has no obvious missing reachable major surface.
- `git diff --check` for any capture-harness/documentation changes.

## Done gate
SUI01 is `DONE` only when the baseline inventory and anomaly ledger are complete enough that SUI02-SUI04 can work from explicit defects rather than visual guesswork. Stop after updating the master ledger/handoff.
