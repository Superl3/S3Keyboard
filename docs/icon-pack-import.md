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
- `dots-lines`: proportional dot alpha glyphs, solid modifier lines, and a four-color spacebar dot cluster.
- `metropolis-graph`: recognizable modifier glyph shapes for Metropolis-style command keys. Colored keycaps should pair this with explicit text overrides for contrast. Legacy `metropolis-points` imports normalize to this renderer.

If an imported modifier pack has `extends: "dots-lines"`, `extends: "metropolis-graph"`, or legacy `extends: "metropolis-points"`, the app uses that built-in renderer while preserving imported display overrides.

## Override Keys

Priority is exact key, then group, then the default label/icon.

- `alpha`: all letter-like keys, including Hangul action keys `ㅣ.`, `ㅡㅐ`, and `..`.
- `modifiers`: command/modifier keys such as shift, enter, backspace, settings, language, space, plus Dingul punctuation `?`, `.`, and `/`.
- `keys`: exact overrides.

Useful exact names:

- `tap:q`, `tap:ㄱ`
- `space`, `shift`, `enter`, `backspace`, `settings`, `language`, `options`, `reserved`
- `label:.`
- `..`, `?`, `.`, `/`

## Override Values

Override values render as text or a built-in semantic glyph id:

```json
{ "type": "icon", "value": "dot" }
{ "type": "icon", "value": "orbit" }
{ "type": "text", "value": "hihihi" }
```

`icon:dot` is rendered by the active decorative glyph renderer. Built-in dot metrics use the same small meta-dot source size for alpha and number-row/numpad dot legends, single-dot modifier keys, and the four-dot spacebar cluster; punctuation two-dot glyphs use a wider center gap so the dots never overlap. Built-in point glyph ids currently include `ring`, `diamond`, `square`, `plus`, `cross`, `star`, `spark`, `chevron_up`, `chevron_left`, `chevron_right`, `slash_dot`, `orbit`, `gear_dot`, `bookmark_dot`, `space_dots`, `two_dots`, `grid_4`, `terminal`, and `cursor`. Keyboard-oriented glyph ids include `keyboard_return`, `keyboard_tab`, `keyboard_capslock`, `keyboard_command`, `keyboard_option`, `keyboard_control`, `keyboard_hide`, `keyboard_full`, `keyboard_keys`, `keyboard_language`, `keyboard_arrow_up`, `keyboard_arrow_down`, `keyboard_arrow_left`, `keyboard_arrow_right`, `keyboard_double_left`, `keyboard_double_right`, `keyboard_backspace`, and `keyboard_space`. These names are aligned with Google Material Symbols / Material Icons naming where possible; Material Symbols are published by Google under Apache 2.0.

GMK/KBDfans-style glyph ids are generic reconstructions of common kit grammar rather than copies of a specific set's protected novelty art. They include `gmk_accent_bar`, `gmk_accent_corner`, `gmk_accent_stripe`, `gmk_triple_dot`, `gmk_twin_ticks`, `gmk_space_dash`, `gmk_macro_stack`, `gmk_macro_brackets`, `gmk_target`, `gmk_pulse`, `gmk_wave`, `gmk_moon`, `gmk_sun`, `gmk_leaf`, `gmk_flower`, `gmk_mountain`, `gmk_droplet`, `gmk_orbit_star`, `gmk_diamond_cluster`, and `gmk_pixel_steps`. They are based on recurring high-end keycap kit patterns: accent bars, icon mods, novelty minis, macro-column symbols, and spacebar marks. In the built-in simple text pack, only enter-like keys use `text:hihihi`, and it is rendered from the same normalized vector glyph definition in Android and previews rather than a font string.

Built-in display pack ids:

- `simple-text`: the Olivia-style `hihihi` replacement for Dingul's visual `.` key.
- `git-commands`: workflow labels such as `exec`, `fetch`, `pull`, `diff`, and `log`.
- `geo-points`: geometric point glyphs for command keys and punctuation.
- `soft-symbols`: calmer rings, plus signs, diamonds, and dots.
- `terminal-points`: terminal/cursor flavored point glyphs.
- `punctuation-points`: a narrow pack focused on `?`, `.`, `/`, enter, and backspace.
- `full-decorative`: alpha dots plus the geo point mapping for command keys.
- `keyboard-symbols`: keyboard-specific glyphs such as return, tab, capslock, command, option, control, backspace, and space.
- `keyboard-navigation`: arrow-heavy keyboard glyphs for movement and navigation-flavored command keys.
- `gmk-style-points`: accent, punctuation, macro, and spacebar point marks inspired by GMK/KBDfans kit structure.
- `gmk-style-novelties`: generic novelty mini motifs such as sun, moon, wave, leaf, flower, mountain, and droplet.
- `gmk-style-macros`: macro-column and extension-kit flavored brackets, stacks, pulse, target, and pixel-step marks.
- `gmk-style-celestial`: constellation, planet, comet, crescent, sparkle, compass, and snow-style novelty marks.
- `gmk-style-nature`: flower, leaf, sprout, wave, petal, rain, cloud, flame, and droplet-style novelty marks.
- `gmk-style-spacebars`: long-bar, split-bar, stepped-bar, side-stripe, equalizer, matrix, and utility marks for wider keys.
- `font-symbols`: 25 built-in symbol-font glyphs such as return, tab, delete, command, option, control, media, star, and triangle marks.
- `image-mask-marks`: bundled monochrome PNG alpha-mask glyphs tinted by the app foreground color.
- `tall-mod-glyphs`: vertically biased PNG mask glyphs for modifier columns and non-1u command surfaces.
- `mixed-source-novelties`: a ready-to-apply set that mixes font glyphs and image masks under one display pack.

The built-in point and GMK-style glyphs are semantic monochrome geometry. They should inherit the app-injected key text color by default. Only explicitly colored packs should bypass that injection with intrinsic colors.
The imported-source glyphs follow the same color rule: font glyphs use `currentColor` / Android text paint color, and image glyphs are stored as alpha masks under `drawable-nodpi` then tinted at draw time. Treat raster assets as masks, not colored art, unless the pack is explicitly declared as intrinsic-color.
Point keycaps have two valid roles: semantic modifier symbols and pure novelty marks. Strong command surfaces such as Enter, Backspace, Shift, and Space should carry a recognizable action meaning unless the novelty silhouette is strong enough to be valuable on its own. Weaker pure novelties should be placed on 1x1, reserved, language/options, or punctuation slots where semantic weight is lower.

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
