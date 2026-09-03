# SUI02 — IME Chrome and Overlay Visibility

## Purpose
Resolve only baseline-confirmed IME surface defects, especially the unwanted clipboard-looking/top auxiliary bar and any overlay that steals keyboard space or appears in the wrong state.

## Read first
- `docs/agent-workflow.md`
- `docs/agent-handoff-current.md`
- `docs/ui-stabilization-2026-09/00-master-plan.md`
- SUI01 anomaly ledger and captures
- `S3KeyboardService`, IME root/layout construction, quick settings/Text Tools/Remote toolbar code identified by SUI01

## Scope
- Reproduce each SUI01 `FIX/BLOCKER` assigned to IME chrome before editing.
- Trace visibility conditions for the ordinary state row, clipboard/Text Tools affordances, candidate/suggestion surfaces, Remote toolbar, text-action overlay, and quick-settings surfaces.
- Ordinary typing must not show an auxiliary toolbar merely because the feature exists.
- A toolbar/panel that is intentionally invoked must remain reachable and must dismiss/exit cleanly.
- Preserve keyboard height and key hit geometry unless the captured defect is specifically caused by wrongly reserved chrome space.
- Preserve sensitive-field suppression and Remote-mode semantics.

## Non-goals
- No theme redesign or keycap styling changes.
- No changes to input mappings, suggestions/correction semantics, Text Tools contents, or Remote key behavior.
- Do not remove a valid status/control row solely to gain vertical space; fix its incorrect visibility or sizing instead.

## Required runtime matrix
Capture before/after for at least:
- ordinary QWERTY and Dingul typing,
- password and number fields,
- Text Action panel closed/open/closed,
- Text Tools closed/open/closed,
- Remote off/on/off with toolbar,
- Caps/one-finger/runtime-state indicators where visible.

## Verification
- Focused unit/config tests for visibility/state logic.
- Dingul typing smoke and representative QWERTY typing smoke after IME root changes.
- Confirm `bottomDelta=0` or equivalent geometry invariant for representative English/Hangul states.
- `scripts/check.ps1` and `git diff --check` before marking done.

## Done gate
All SUI01 IME `FIX/BLOCKER` items are resolved or explicitly reclassified with evidence. Ordinary typing no longer contains the unwanted top bar, intentional surfaces still work, and before/after captures exist under `captures/ui-stabilization-202609/sui02-ime/`.
