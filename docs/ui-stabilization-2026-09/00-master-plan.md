# UI Stabilization Master Plan

Updated: 2026-09-03 KST
Repository: `C:\Users\bug95\Documents\Codex\2026-05-19\mimic-apk-ux`

## Goal
Audit every meaningful app/settings/IME view with runtime captures, identify only objectively abnormal layout or usability problems, and correct those problems without broad visual redesign.

## Core rule: anomaly repair, not redesign
- Healthy screens are evidence to preserve, not invitations to restyle.
- Do not change colors, typography, spacing, grouping, or interaction merely because another style seems prettier.
- A change needs a concrete defect: clipping, overlap, broken sizing, inconsistent alignment, unreachable control, forced/unwanted surface, misleading hierarchy, poor scroll behavior, or an equivalent usability failure.
- Every changed view requires before/after capture evidence.
- Existing input behavior and feature semantics remain unchanged unless the visible defect is caused by an incorrect visibility/state condition.

## Mandatory session protocol
1. Read workflow, current handoff, this file, then only the assigned session file.
2. Preserve unrelated work; never reset/revert/stash it away.
3. Capture before modifying whenever the view can be reproduced.
4. Record each issue as `KEEP`, `MINOR`, `FIX`, or `BLOCKER`; only `FIX/BLOCKER` authorizes code changes.
5. Prefer shared layout/component fixes only when multiple captured defects prove the common cause.
6. Run focused checks after each fix and the session-level gate before marking `DONE`.
7. Update this ledger and the rolling handoff at the session boundary.

## Global constraints
- Do not alter Dingul/QWERTY key maps, Hangul automata, touch hit-testing, one-finger semantics, text-action semantics, backup schema, or privacy boundaries during this roadmap.
- Do not make broad aesthetic changes to themes or the keyboard renderer.
- The unwanted IME top bar/clipboard-looking surface is a priority defect, but its cause must be identified from runtime state before changing visibility logic.
- Settings may be reordered only when captures show discoverability, hierarchy, overflow, or reachability problems; preserve labels and behavior unless a concrete defect requires otherwise.
- Test light/dark system UI where a settings component is shared across both.

## Session order
- SUI01 `01-baseline-capture-inventory.md`: enumerate and capture all meaningful views; produce anomaly ledger without product changes.
- SUI02 `02-ime-chrome-and-overlays.md`: repair forced/unwanted IME chrome, toolbar/overlay visibility, and keyboard-space regressions.
- SUI03 `03-settings-layout-and-hierarchy.md`: repair broken settings layouts and minimally reorder controls where captured usability defects justify it.
- SUI04 `04-secondary-surfaces-and-responsive.md`: audit/fix theme, layout editor, Text Tools, backup, diagnostics, Remote and dialog/small-screen surfaces.
- SUI05 `05-final-visual-regression.md`: recapture the complete matrix, close residual anomalies, run regression gates, and finalize documentation/VCS state.

## Progress ledger
| Session | State | Gate |
|---|---|---|
| SUI01 | NEXT | complete runtime view inventory + capture matrix + anomaly ledger; no unjustified visual changes |
| SUI02 | TODO | ordinary IME has no forced auxiliary top bar; intentional overlays/toolbars appear only in their valid states; before/after evidence |
| SUI03 | TODO | main settings sections have no clipping/overlap/reachability defects and any reorder is tied to a documented usability defect |
| SUI04 | TODO | specialized/secondary surfaces and small-height/landscape cases have no remaining FIX/BLOCKER items |
| SUI05 | TODO | full post-fix capture matrix reviewed; no unresolved FIX/BLOCKER; canonical checks and final handoff pass |

## Definition of done
This roadmap is complete only when every meaningful view has post-change runtime evidence, all ledger items are either `KEEP`, accepted `MINOR`, or resolved, and the final report lists exactly what changed and what was deliberately left untouched.
