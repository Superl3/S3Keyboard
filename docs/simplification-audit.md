# Simplification Audit

This audit tracks evidence-based simplification targets for the keyboard codebase. The goal is to keep the current UX behavior while reducing duplicated paths, unused scaffolding, and regression risk.

## Current Evidence

- `HangulKeyboardView` is the largest class and owns rendering, gesture resolution, preview bubbles, touch learning capture, accessibility, and debug overlay state. This should be split by behavior only after characterization tests exist for each extracted path.
- `KeyboardSettings` and `KeyboardPreferences` carry most user and theme settings directly. New options should be grouped by an existing settings section or moved into focused value objects instead of adding more parallel scalar fields.
- `MainActivity` mixes settings hub layout, category construction, preview setup, demo handling, and data reset actions. UI simplification should extract section builders without changing saved preference keys.
- `S3KeyboardService` owns IME lifecycle, editor policy, Hangul composition, English assistance, remote mode, clipboard, quick settings, and command dispatch. Input correctness fixes should keep `InputConnectionTextOperator` as the only direct composing/commit/delete boundary.
- `RecentInputCorrectionController` was unused and has been removed. Geometry-only correction is already available through `TouchBiasStore`, so keeping an extra controller added API surface without behavior.
- Repeated Hangul automata committed-fragment calls in `S3KeyboardService` now route through one helper, keeping the composing replacement contract in one local place.
- Preview bubble width, position, lift, and corner-radius rules now live in `PreviewBubbleLayout`, so popup tuning can be tested without touching the full keyboard view.

## Preferred Direction

- Keep the native `InputMethodService` implementation. It is the correct Android integration point and avoids framework mismatch for IME lifecycle, `EditorInfo`, and `InputConnection`.
- Keep preview popups on the existing `PopupWindow` overlay path. The bloat risk is not the native primitive, but duplicated animation/rendering state. Prefer one small animation model plus one drawable.
- Keep Hangul composition inside `HangulAutomata` plus `InputConnectionTextOperator`. Avoid committing text directly from service branches because composing behavior regresses easily across editors.
- Prefer existing deterministic gesture scoring over ML/swipe frameworks for Dingul input. The project target is small-grid Korean gesture input, not dictionary-driven full-word swipe typing.
- Treat debug, diagnostics, and practice panels as optional surfaces over existing state. They should not own input state or duplicate correction logic.

## Next Refactor Units

1. Move settings category construction out of `MainActivity` one section at a time, starting with debug/input-assistance sections because they are currently option-heavy.
2. Introduce grouped settings value objects only when they reduce constructor churn in `KeyboardSettings`; do not add compatibility aliases unless an importer actually needs them.
3. Keep removing unused one-method controller classes when their behavior is already expressed by an existing service/store.
