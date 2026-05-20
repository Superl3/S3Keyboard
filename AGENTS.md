@C:\Users\bug95\.codex\RTK.md
@C:\Users\bug95\.config\JJ.md

# Project Working Rules

- Read `docs/agent-workflow.md` before changing theme, icon, rendering, settings, input, or learning behavior.
- This repository is Git-only unless a `.jj` directory is later added. Use `git status --short --branch` before staging or committing.
- Keep settings screens on `SettingsUiPalette`, `SettingsArrayAdapter`, and `SettingsViewStyler`; keyboard theme colors must not leak into Android settings widgets.
- For theme/icon work, update Android runtime, theme JSON import/export, web theme builder, preview renderer, and focused tests together.
- Verify with `rtk powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1`, `rtk node --check web-theme-builder/app.js`, and `rtk git diff --check` before pushing.
