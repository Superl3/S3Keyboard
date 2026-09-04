# Final Visual Verification — 2026-09-04

## Result
SUI01-SUI05 UI stabilization is complete. The final pass found no unresolved in-scope `FIX` or `BLOCKER` after the targeted repairs; no broad aesthetic redesign was introduced.

## Complete post-fix view matrix
- Baseline: `captures\ui-stabilization-202609\baseline`
- Final clean-AVD matrix: `captures\ui-stabilization-202609\sui05-final\app-settings-clean-avd`
- Baseline/final filename comparison: 127/127 `.png`/`.xml`, missing 0, extra 0.
- Representative settings, Theme Selector/Editor, Accent Placement, Diagnostics, Backup/Restore and scrolling states were reviewed after the final build.
- Previously repaired status-bar collisions remain resolved; the Theme Selector management controls remain readable as two two-button rows.

## IME and interaction regression
- Strict field smoke: `captures\ui-stabilization-202609\sui05-final\ime-smoke-strict2`.
- Actual visible S3 IME was confirmed for local practice plus password, number, URL, email, web-edit, search and multiline synthetic profiles.
- The smoke harness now requires `mDecorViewVisible=true` and `mIsInputViewShown=true`, retries transient `dumpsys input_method`, and focuses the field after selecting S3.
- Google Messages launch breadth did not show an editable IME field in this fresh first-run frame and is not counted as IME-visible pass evidence here.
- Chrome launch/state output is likewise not used as visual IME pass evidence in SUI05; earlier dedicated real-field evidence remains the applicable proof.
- SUI02 explicit Text Tools/Text Action/Remote/ordinary IME interaction evidence remains under `captures\ui-stabilization-202609\sui02-ime`.

## Responsive evidence
- `captures\ui-stabilization-202609\sui05-final\responsive\dark-main.png`: dark portrait settings remain readable with correct top inset.
- `captures\ui-stabilization-202609\sui05-final\responsive\landscape-main.png`: forced landscape shows reachable/scrolled content without overlap or clipped primary controls in the reviewed viewport.

## Fresh input proof
- `scripts\smoke-dingul-typing.ps1` was hardened with strict IME visibility, transient `dumpsys` retries, focus ordering, and validated non-zero screenshot pull retries.
- Clean-data run: `captures\dingul-typing-20260904-151422`.
- Result: `Dingul typing probe passed: 16 key actions`.
- No input map, Hangul automata, hit-testing, or one-finger production semantics were changed by this roadmap.

## Automatic gates
- `scripts\check.ps1` -> PASS; `testDebugUnitTest`, `lintDebug`, `assembleDebug` -> `BUILD SUCCESSFUL`.
- Theme validation -> 0 warnings; material surface and KeyboardSettings usage audits -> PASS.
- `node --check web-theme-builder\app.js` -> PASS.
- `scripts\render-theme-previews.ps1` -> PASS; preview grid regenerated.
- `git diff --check` -> PASS immediately before final VCS closeout.

## Final anomaly ledger
- SUI01 baseline `FIX` items addressed by SUI02-SUI04 remain resolved in SUI05 recapture.
- No new `FIX/BLOCKER` was found in the complete portrait filename matrix or the reviewed dark/landscape/IME evidence.
- Healthy/accepted surfaces were deliberately preserved instead of restyled.

## External-only caveats
- Release signing properties are still unavailable, so debug verification is not release-signing verification.
- Real Windows Remote receiver delivery and physical-device/TalkBack breadth remain external/manual gates rather than unfinished UI stabilization work.
