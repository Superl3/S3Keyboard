import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const themeDir = path.join(root, "themes");
const ids = fs.readdirSync(themeDir)
  .filter((name) => name.endsWith(".json"))
  .map((name) => name.slice(0, -5));

function read(id) {
  return JSON.parse(fs.readFileSync(path.join(themeDir, `${id}.json`), "utf8"));
}

function write(id, theme) {
  fs.writeFileSync(
    path.join(themeDir, `${id}.json`),
    `${JSON.stringify(theme, null, 2)}\n`,
    "utf8");
}

function merge(target, patch) {
  for (const [key, value] of Object.entries(patch)) {
    if (value && typeof value === "object" && !Array.isArray(value)) {
      target[key] = merge(target[key] ?? {}, value);
    } else {
      target[key] = value;
    }
  }
  return target;
}
function surface(theme, config) {
  const endColor = theme.colors?.depth ?? theme.colors?.border ?? theme.colors?.alphaKey ?? "#000000";
  merge(theme, {
    shape: config.shape,
    effects: {
      blur: config.blur ?? { enabled: false, radiusDp: 0 },
      glass: config.glass ?? {
        enabled: false,
        tintAlphaPercent: 86,
        highlightPercent: 18,
        borderAlphaPercent: 42
      },
      metal: config.metal ?? { enabled: false, strengthPercent: 0 },
      keyFaceGradient: {
        enabled: config.gradient?.enabled ?? false,
        strengthPercent: config.gradient?.strength ?? 0,
        startColor: config.gradient?.start ?? "#FFFFFF",
        endColor: config.gradient?.end ?? endColor,
        curve: config.gradient?.curve ?? "soft"
      },
      panelGradient: {
        ...(theme.effects?.panelGradient ?? {}),
        enabled: config.panelGradient ?? false
      },
      materialStyle: config.material
    }
  });
}

const groups = {
  classicGmk: [
    "gmk-8008", "gmk-bento", "gmk-dots-dark", "gmk-dots-light",
    "gmk-dracula", "gmk-hammerhead", "gmk-metropolis", "gmk-modern-dolch",
    "gmk-oblivion", "gmk-oblivion-hagoromo", "gmk-olivia-dark", "gmk-olivia-light"
  ],
  outlineSoft: [
    "bento-outline-soft", "botanical-outline-soft", "dracula-outline-soft", "dualshot-outline-soft"
  ],
  softKeycap: ["8008-soft-keycap", "metropolis-soft-keycap", "olivia-soft-keycap"],
  acrylic: ["laser-outline-acrylic", "modern-dolch-acrylic", "striker-outline-acrylic"]
};
const profiles = {
  classicGmk: {
    // Premium-flat is the default house language: broad faces, moderate corners,
    // no pedestal, and almost no face shading. Colorway/legend semantics carry identity.
    material: "solid",
    shape: { roundnessDp: 6, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
    gradient: { enabled: false, strength: 0, curve: "soft" },
    panelGradient: false
  },
  outlineSoft: {
    // Outline variants stay visibly outlined, but share the same flat body language.
    material: "solid",
    shape: { roundnessDp: 7, borderWidthDp: 2, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
    gradient: { enabled: false, strength: 0, curve: "soft" },
    panelGradient: false
  },
  softKeycap: {
    material: "soft_keycap",
    shape: { roundnessDp: 10, borderWidthDp: 1, keyGapDp: 5, depthEnabled: true, depthDp: 1 },
    gradient: { enabled: true, strength: 15, curve: "soft" },
    panelGradient: false
  },
  acrylic: {
    material: "acrylic",
    shape: { roundnessDp: 8, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
    gradient: { enabled: true, strength: 13, curve: "top_glow" },
    panelGradient: true
  }
};

const themes = new Map(ids.map((id) => [id, read(id)]));
for (const [profileName, themeIds] of Object.entries(groups)) {
  for (const id of themeIds) surface(themes.get(id), profiles[profileName]);
}

function patch(id, patchValue) {
  merge(themes.get(id), patchValue);
}
// Classic GMK sets now share the quiet Nord-like house geometry. They remain distinct through
// palette, role mapping, legends, novelties and accent placement rather than simulated depth.
for (const id of ["gmk-dots-dark", "gmk-dots-light"]) {
  patch(id, { shape: { roundnessDp: 5, keyGapDp: 5 } });
}
for (const id of ["gmk-oblivion", "gmk-oblivion-hagoromo"]) {
  patch(id, { shape: { roundnessDp: 6 } });
}
patch("gmk-metropolis", {
  shape: { roundnessDp: 6 },
  metadata: {
    tags: ["dark", "flat", "gmkInspired"],
    features: ["angularPreviewBubble", "dingulRoleColors", "heavyPerKeyOverrides", "modifierIconPack"]
  },
  effects: {
    blur: { enabled: false, radiusDp: 0 },
    glass: { enabled: false, tintAlphaPercent: 86, highlightPercent: 18, borderAlphaPercent: 42 },
    metal: { enabled: false, strengthPercent: 0 },
    keyFaceGradient: { enabled: false, strengthPercent: 0, curve: "soft" },
    materialStyle: "solid"
  }
});

// Material remixes change the surface treatment without losing their source theme's visual DNA.
patch("botanical-outline-soft", { shape: { roundnessDp: 9 } });
patch("dualshot-outline-soft", { shape: { roundnessDp: 7 } });
patch("8008-soft-keycap", { shape: { roundnessDp: 9 }, effects: { keyFaceGradient: { strengthPercent: 16 } } });
patch("metropolis-soft-keycap", { shape: { roundnessDp: 10 }, effects: { keyFaceGradient: { strengthPercent: 15 } } });
patch("olivia-soft-keycap", { shape: { roundnessDp: 11 }, effects: { keyFaceGradient: { strengthPercent: 14 } } });
patch("modern-dolch-acrylic", { shape: { roundnessDp: 10 }, effects: { keyFaceGradient: { strengthPercent: 16 } } });
for (const id of ["laser-outline-acrylic", "striker-outline-acrylic"]) {
  patch(id, { shape: { borderWidthDp: 2 } });
}
// Platform families intentionally use different geometry, not merely different colors.
for (const id of ["ios-clean-light", "ios-clean-dark"]) {
  surface(themes.get(id), {
    material: "solid",
    shape: { roundnessDp: 9, borderWidthDp: 0, keyGapDp: 4, depthEnabled: false, depthDp: 0 },
    gradient: { enabled: false }, panelGradient: false
  });
  patch(id, { typography: { primaryTextSizePercent: 80, secondaryTextSizePercent: 72, secondaryTextBold: false } });
}
for (const id of ["android-material-light", "android-material-dark"]) {
  surface(themes.get(id), {
    material: "solid",
    shape: { roundnessDp: 8, borderWidthDp: 0, keyGapDp: 4, depthEnabled: false, depthDp: 0 },
    gradient: { enabled: false }, panelGradient: false
  });
  patch(id, { typography: { primaryTextSizePercent: 80, secondaryTextSizePercent: 74, secondaryTextBold: false } });
}
surface(themes.get("macos-frost-light"), {
  material: "frosted",
  shape: { roundnessDp: 6, borderWidthDp: 1, keyGapDp: 4, depthEnabled: false, depthDp: 0 },
  blur: { enabled: true, radiusDp: 12 },
  glass: { enabled: true, tintAlphaPercent: 84, highlightPercent: 10, borderAlphaPercent: 24 },
  gradient: { enabled: true, strength: 2, curve: "soft" }, panelGradient: false
});
surface(themes.get("macos-graphite-dark"), {
  material: "solid",
  shape: { roundnessDp: 6, borderWidthDp: 1, keyGapDp: 4, depthEnabled: false, depthDp: 0 },
  gradient: { enabled: false, strength: 0, curve: "soft" }, panelGradient: false
});
// Nord base themes are quiet, cool keycaps. Frost variants preserve the same role mapping but
// exchange the physical face for a translucent surface.
for (const id of ["nord-night", "nord-snow"]) {
  surface(themes.get(id), {
    material: "solid",
    shape: { roundnessDp: 6, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
    gradient: { enabled: false, strength: 0, curve: "soft" }, panelGradient: false
  });
}
for (const id of ["nord-frost-night", "nord-frost-snow"]) {
  surface(themes.get(id), {
    material: "frosted",
    shape: { roundnessDp: 7, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
    blur: { enabled: true, radiusDp: 14 },
    glass: { enabled: true, tintAlphaPercent: 82, highlightPercent: 9, borderAlphaPercent: 24 },
    gradient: { enabled: true, strength: 2, curve: "soft" }, panelGradient: false
  });
}

const frostConfigs = {
  "midnight-ice": { roundnessDp: 8, tint: 76, highlight: 18, border: 34, blur: 18 },
  "pearl-mist": { roundnessDp: 9, tint: 80, highlight: 14, border: 28, blur: 18 },
  "slate-glass": { roundnessDp: 7, tint: 78, highlight: 13, border: 30, blur: 16 }
};
for (const [id, cfg] of Object.entries(frostConfigs)) {
  surface(themes.get(id), {
    material: "frosted",
    shape: { roundnessDp: cfg.roundnessDp, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
    blur: { enabled: true, radiusDp: cfg.blur },
    glass: { enabled: true, tintAlphaPercent: cfg.tint, highlightPercent: cfg.highlight, borderAlphaPercent: cfg.border },
    gradient: { enabled: true, strength: 2, curve: "soft" }, panelGradient: true
  });
}

surface(themes.get("amoled-black"), {
  material: "solid",
  shape: { roundnessDp: 5, borderWidthDp: 0, keyGapDp: 4, depthEnabled: false, depthDp: 0 },
  gradient: { enabled: false }, panelGradient: false
});
surface(themes.get("graphite-mono"), {
  material: "solid",
  shape: { roundnessDp: 6, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
  gradient: { enabled: false, strength: 0, curve: "soft" }, panelGradient: false
});
surface(themes.get("high-contrast-light"), {
  material: "solid",
  shape: { roundnessDp: 3, borderWidthDp: 2, keyGapDp: 6, depthEnabled: false, depthDp: 0 },
  gradient: { enabled: false }, panelGradient: false
});
patch("high-contrast-light", {
  typography: { primaryTextSizePercent: 82, secondaryTextSizePercent: 78, primaryTextBold: true, secondaryTextBold: true }
});
surface(themes.get("lavender-focus"), {
  material: "solid",
  shape: { roundnessDp: 12, borderWidthDp: 0, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
  gradient: { enabled: false }, panelGradient: false
});
patch("lavender-focus", { typography: { primaryTextSizePercent: 80, secondaryTextSizePercent: 74, secondaryTextBold: false } });
surface(themes.get("paper-mono-flat"), {
  material: "solid",
  shape: { roundnessDp: 2, borderWidthDp: 1, keyGapDp: 7, depthEnabled: false, depthDp: 0 },
  gradient: { enabled: false }, panelGradient: false
});
patch("paper-mono-flat", { typography: { primaryTextSizePercent: 76, secondaryTextSizePercent: 72, secondaryTextBold: false } });
for (const id of ["marigold-fiesta-dark", "marigold-fiesta-light"]) {
  surface(themes.get(id), {
    material: "solid",
    shape: { roundnessDp: 6, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
    gradient: { enabled: false, strength: 0, curve: "soft" }, panelGradient: false
  });
}

// Derivatives inherit role/legend semantics from their source. Material-only additions such as
// outline colors and novelties intentionally stay local to the derivative.
const lineage = {
  "8008-soft-keycap": "gmk-8008",
  "bento-outline-soft": "gmk-bento",
  "dracula-outline-soft": "gmk-dracula",
  "metropolis-soft-keycap": "gmk-metropolis",
  "modern-dolch-acrylic": "gmk-modern-dolch",
  "olivia-soft-keycap": "gmk-olivia-dark",
  "nord-frost-night": "nord-night",
  "nord-frost-snow": "nord-snow"
};
const inheritedFields = [
  "additionalNumberRow", "typography", "icons", "keyTextColorOverrides",
  "keyBackgroundColorOverrides", "dingulColors", "accentPolicy", "keyDisplayOverrides"
];
for (const [derivedId, baseId] of Object.entries(lineage)) {
  const derived = themes.get(derivedId);
  const base = themes.get(baseId);
  for (const field of inheritedFields) {
    if (base[field] === undefined) {
      delete derived[field];
    } else {
      derived[field] = structuredClone(base[field]);
    }
  }
}

// Re-apply derivative-only material geometry after lineage inheritance.
patch("dracula-outline-soft", { shape: { roundnessDp: 7, borderWidthDp: 2, keyGapDp: 5, depthEnabled: false, depthDp: 0 } });
patch("bento-outline-soft", { shape: { roundnessDp: 7, borderWidthDp: 2, keyGapDp: 5, depthEnabled: false, depthDp: 0 } });
patch("nord-frost-night", { shape: { roundnessDp: 7, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 } });
patch("nord-frost-snow", { shape: { roundnessDp: 7, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 } });

function reconcileStyleMetadata(theme) {
  const metadata = theme.metadata ?? (theme.metadata = {});
  const tags = new Set(metadata.tags ?? []);
  const features = new Set(metadata.features ?? []);
  const material = theme.effects?.materialStyle ?? "solid";
  const hasDepth = Boolean(theme.shape?.depthEnabled) && Number(theme.shape?.depthDp ?? 0) > 0;
  const hasGradient = Boolean(theme.effects?.keyFaceGradient?.enabled)
    && Number(theme.effects?.keyFaceGradient?.strengthPercent ?? 0) > 0;
  if (!hasDepth) { tags.delete("depth"); features.delete("keyDepth"); tags.add("flat"); }
  if (!hasGradient) features.delete("keyFaceGradient");
  if (material !== "soft_keycap") tags.delete("softKeycap");
  if (material !== "frosted") tags.delete("frosted");
  if (material !== "acrylic") tags.delete("acrylic");
  metadata.tags = [...tags];
  metadata.features = [...features];
}

for (const id of ids) {
  reconcileStyleMetadata(themes.get(id));
  write(id, themes.get(id));
}

console.log(`Curated style contracts for ${ids.length} themes.`);
