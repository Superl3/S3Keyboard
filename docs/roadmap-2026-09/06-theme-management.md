# S06 - Theme Management UX

## Read first
- `docs/agent-workflow.md`, `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`
- `themes/*.json`, `tools/theme-contract.mjs`, `tools/sync-themes.mjs`
- `KeyboardThemePreset`, `ThemeOption`, `ThemePreviewSettings`
- theme selector/editor activities and static/runtime preview scripts

## Scope
1. Do not add more built-in themes in this session; improve discovery/management of the existing set.
2. Add favorites, recent themes, material filters, light/dark filters, and text search.
3. Add user-defined light/dark theme pairing tied to system appearance; preserve manual override behavior.
4. Add quick A/B preview between two themes in both Dingul and QWERTY without applying until confirmed.
5. Ensure preview uses the same ThemeOption/ThemePreviewSettings path as runtime.
6. Keep existing 42-theme source of truth and generator contract intact.
7. Persist favorites/recent/pairs separately from theme appearance JSON.
8. Add tests for filtering, pairing precedence, favorite persistence, and no mutation of typography/hints.

## Runtime verification
- favorite/recent ordering survives restart.
- filter/search result counts are correct.
- system light/dark change swaps paired themes and returns predictably.
- A/B preview matches real runtime for representative solid/soft/frosted/acrylic themes.
- no regression in 84-theme-mode capture audit assumptions.

## Done gate
Generator/checks, preview parity checks, and representative runtime captures pass. Mark S06=DONE and update handoff.