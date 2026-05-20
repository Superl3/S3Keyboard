# Icon Pack Import Schema

This app can import icon/display pack data from a theme JSON. The current renderer is intentionally small:

- modifier packs can select a built-in renderer with `extends`.
- key display packs can override alpha keys, modifier keys, or exact keys with text or decorative glyph names.
- imported vector path data can be kept in the JSON as authoring metadata, but v1 rendering uses the built-in renderer plus `keyDisplayOverrides`.

## Theme Entry Points

```json
{
  "schemaVersion": 1,
  "icons": {
    "modifierPackId": "my-wide-dots",
    "keyDisplayPackId": "my-script-labels"
  },
  "iconPacks": {
    "modifier": {
      "my-wide-dots": {
        "extends": "dots-lines",
        "glyphs": {
          "dot": {
            "glyphId": "dot",
            "colored": false,
            "paths": ["M 0.5 0.5 m -0.5 0 a 0.5 0.5 0 1 0 1 0 a 0.5 0.5 0 1 0 -1 0"],
            "backgroundPaths": null,
            "backgroundColor": null
          }
        },
        "keyDisplayOverrides": {
          "modifiers": { "type": "icon", "value": "dot" }
        }
      }
    },
    "keyDisplay": {
      "my-script-labels": {
        "overrides": {
          "alpha": { "type": "icon", "value": "dot" },
          "modifiers": { "type": "text", "value": "cmd" },
          "keys": {
            "enter": { "type": "text", "value": "hihihi" },
            "space": { "type": "icon", "value": "dot" },
            "tap:q": { "type": "text", "value": "Q" },
            "..": { "type": "icon", "value": "dot" },
            "?": { "type": "icon", "value": "dot" },
            ".": { "type": "icon", "value": "dot" },
            "/": { "type": "icon", "value": "dot" }
          }
        }
      }
    }
  }
}
```

Inline pack objects are also accepted:

```json
{
  "schemaVersion": 1,
  "icons": {
    "modifierPackId": "my-wide-dots",
    "modifierPack": {
      "extends": "dots-lines",
      "keyDisplayOverrides": {
        "modifiers": { "type": "icon", "value": "dot" }
      }
    }
  }
}
```

## Built-In Modifier Renderers

- `line-mono`: normal monochrome modifier icons. Uses theme foreground.
- `accent-color`: normal modifier shapes with intrinsic accent color. Ignores theme foreground.
- `dots-lines`: proportional dots and line-dot modifier glyphs. Uses theme foreground.
- `metropolis-points`: colored point-key style glyphs. Ignores theme foreground.

If an imported modifier pack has `extends: "dots-lines"` or `extends: "metropolis-points"`, the app uses that built-in renderer while preserving imported display overrides.

## Override Keys

Priority is exact key, then group, then the default label/icon.

- `alpha`: all letter-like keys, including Hangul action keys `ㅣ.`, `ㅡㅐ`, `..`, plus `?`, `.`, `/`.
- `modifiers`: command/modifier keys such as shift, enter, backspace, settings, language, space.
- `keys`: exact overrides.

Useful exact names:

- `tap:q`, `tap:ㄱ`
- `space`, `shift`, `enter`, `backspace`, `settings`, `language`, `options`, `reserved`
- `label:.`
- `..`, `?`, `.`, `/`

## Override Values

Only two override value types render in v1:

```json
{ "type": "icon", "value": "dot" }
{ "type": "text", "value": "hihihi" }
```

`icon:dot` is rendered by the active decorative glyph renderer. `text:hihihi` is rendered as an app vector script glyph in the simple text pack path.

## Glyph Authoring Metadata

External tools can include normalized path metadata for future renderers:

```json
{
  "glyphId": "my-mark",
  "colored": false,
  "intrinsicColor": "#66E3C4",
  "paths": ["M 0.12 0.50 L 0.88 0.50"],
  "backgroundPaths": null,
  "backgroundColor": null
}
```

Coordinate paths should be normalized to a `0..1` view box. Keep safe padding inside the path itself; wide keys such as `space`, `enter`, and `backspace` are stretched to the real key bounds by the active renderer.
