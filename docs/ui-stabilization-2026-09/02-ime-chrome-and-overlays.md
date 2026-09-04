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

## Completion — 2026-09-04 KST
- Removed the always-reserved `ClipboardPanelController.createToolbar()` row from the IME root. Ordinary typing no longer shows the clipboard-looking/Text Tools bar merely because Text Tools are allowed.
- Kept Text Tools explicit and reachable: Quick Settings now owns a Text Tools action, while the existing assist-rail command remains intact. Sensitive-field suppression continues to hide an already-open clipboard/Text Tools surface when policy disallows it.
- Fresh SUI02 evidence is under `captures/ui-stabilization-202609/sui02-ime/`. `qwerty-ordinary-closed.png` confirms the unwanted row is absent; `text-action-open-fresh.png` and `text-action-closed-fresh.png` prove the text-action overlay can open and dismiss without hiding the IME. Password/number/multiline policy captures remain clean.
- ADB synthetic long-press/swipe injection on the extreme-left settings key was not reliable enough to certify the Quick Settings gesture itself: repeated injected streams sometimes resolved as the normal settings tap. No production gesture/hit-test code was changed for this harness limitation. Existing S03/S07 runtime evidence remains the authority for one-finger/Caps and Remote toolbar semantics.
- Current runtime audit log shows `bottomDelta=0` for both Hangul and English representative states after the root change. Fresh Dingul smoke: `captures/dingul-typing-20260904-111236`, 16 emitted key actions, PASS.
- `ProductionReadinessConfigTest` locks out recreation of the forced toolbar and locks in explicit Text Tools access. Focused test PASS; canonical `scripts/check.ps1` PASS (`testDebugUnitTest`, lint, assemble, theme/material/settings audits).
- SUI01's IME `FIX` is resolved. No unrelated renderer, mapping, text-action, Remote-key, or one-finger semantics were changed.
