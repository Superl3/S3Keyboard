# Theme style/runtime audit handoff

This handoff is superseded by the 2026-09-01 stabilization pass.

## Current supported material contract

- `solid`
- `soft_keycap`
- `frosted`
- `acrylic`

Frosted uses Android platform blur plus theme tint/highlight/border treatment. It does not acquire screen content or require Accessibility permission.

## Current catalog and generated outputs

- Built-in themes: **42**.
- Generated Android presets, web contract/index, static previews, and the classification report are produced from `themes/*.json` through the repository generators.
- Do not hand-edit generated theme files.

## Runtime audit gate

Use `scripts/capture-all-theme-runtime.ps1`. A frame is committed only when the exact theme/mode render-ready event is present, the S3 IME is selected and shown, geometry is non-zero/in-bounds, the keyboard content bottom matches screen bottom minus navigation inset, and two geometry samples at least 250 ms apart are identical. Failed frames stay in staging and never overwrite a PNG.

The trusted 2026-09-01 audit is `captures/runtime-theme-audit-20260901-geometry-final`: 42 themes ? QWERTY/Dingul = 84 PNG, with a complete CSV manifest and zero failed combinations.

## Visual review priorities

Continue comparing GMK/minimal/outline/soft-keycap/frosted/acrylic, Dots display overrides, and Marigold foreground overrides against the generated static previews. Fix shared classification or theme data causes rather than applying arbitrary palette-only corrections.
