# S05 - Versioned Backup and Restore

## Read first
- `docs/agent-workflow.md`, `docs/agent-handoff-current.md`
- `docs/roadmap-2026-09/00-master-plan.md`
- `KeyboardPreferences.java`, `KeyboardThemeJson.java`, theme/import code
- app-profile persistence from S03, text-tools persistence from S04
- `docs/privacy-notice.md`, `docs/play-data-safety-draft.md`

## Scope
1. Define one versioned backup envelope with `schemaVersion`, app version, generated timestamp, and sections.
2. Include user settings, app overrides, user/custom theme data, external-theme references where portable, reserved phrases/pins, and opted-in local preferences.
3. Exclude secrets, transient AI request data, clipboard history by default, diagnostics, and device-only transport ids.
4. Implement export to user-selected document and import with preview of affected sections.
5. Add migration handlers for at least current schema and one synthetic older schema fixture.
6. Import must be atomic: validate/normalize first, then apply; malformed sections cannot partially corrupt preferences.
7. Add selective restore checkboxes and reset-to-default recovery path.
8. Add unit tests for round trip, unknown fields, old schema migration, malformed backup, and sensitive exclusions.

## Runtime verification
- export configured app, change settings, restore, compare effective state.
- restore selective theme-only/settings-only sections.
- malformed file reports error without changing current state.
- exported JSON contains no secret/provider credential or raw clipboard history by default.

## Done gate
Round-trip and migration evidence pass. Mark S05=DONE and update handoff.