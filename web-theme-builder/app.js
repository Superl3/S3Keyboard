const themeContract = window.S3_THEME_CONTRACT;
if (!themeContract) {
  throw new Error("Missing generated theme contract. Run: node ../tools/sync-themes.mjs --generate-web");
}

const colorFields = themeContract.colorFields.map(field => [field.key, field.label, field.description]);
const shapeFields = themeContract.shapeFields.map(field => [field.key, field.label, field.min, field.max]);
const dingulRoleFields = themeContract.dingulRoleFields.map(field => [field.role, field.label]);
const numberRowModes = themeContract.numberRowModes || [];
const fontFamilies = themeContract.fontFamilies || [];
const modifierIconPacks = themeContract.modifierIconPacks || [];
const keyDisplayPacks = themeContract.keyDisplayPacks || [];
const presets = {};
const themeIndex = Array.isArray(window.S3_THEME_INDEX) ? window.S3_THEME_INDEX : [];

let state = createDefaultTheme();

const ids = {
  name: document.getElementById("themeName"),
  author: document.getElementById("themeAuthor"),
  preset: document.getElementById("presetSelect"),
  colors: document.getElementById("colorControls"),
  shape: document.getElementById("shapeControls"),
  dingulRoles: document.getElementById("dingulRoleControls"),
  depthEnabled: document.getElementById("depthEnabled"),
  customDepth: document.getElementById("customDepth"),
  fontFamily: document.getElementById("fontFamily"),
  primary: document.getElementById("primaryTextSizePercent"),
  secondary: document.getElementById("secondaryTextSizePercent"),
  primaryOut: document.getElementById("primaryOut"),
  secondaryOut: document.getElementById("secondaryOut"),
  primaryBold: document.getElementById("primaryTextBold"),
  primaryItalic: document.getElementById("primaryTextItalic"),
  secondaryBold: document.getElementById("secondaryTextBold"),
  secondaryItalic: document.getElementById("secondaryTextItalic"),
  numberRow: document.getElementById("numberRowMode"),
  modifierPack: document.getElementById("modifierPackId"),
  keyDisplayPack: document.getElementById("keyDisplayPackId"),
  textOverrides: document.getElementById("textOverridesText"),
  backgroundOverrides: document.getElementById("backgroundOverridesText"),
  output: document.getElementById("jsonOutput"),
  preview: document.getElementById("keyboardPreview"),
  status: document.getElementById("status")
};

init();

async function init() {
  await loadExternalPresets();
  if (presets["ios-clean-light"]) {
    state = cloneTheme(presets["ios-clean-light"]);
  }
  Object.entries(presets).forEach(([id, preset]) => {
    const option = document.createElement("option");
    option.value = id;
    option.textContent = preset.name;
    ids.preset.appendChild(option);
  });
  buildColorControls();
  buildShapeControls();
  buildDingulRoleControls();
  populateContractSelects();
  bindStaticControls();
  renderForm();
  update();
}

async function loadExternalPresets() {
  const loaded = await Promise.all(themeIndex.map(async entry => {
    try {
      if (entry.theme) {
        return [entry.id, themeJsonToPreset(entry.theme)];
      }
      const response = await fetch(entry.url, { cache: "no-store" });
      if (!response.ok) {
        return null;
      }
      return [entry.id, themeJsonToPreset(await response.json())];
    } catch (error) {
      return null;
    }
  }));
  loaded
      .filter(Boolean)
      .forEach(([id, preset]) => {
        presets[id] = preset;
      });
}

function createDefaultTheme() {
  const theme = {
    name: "Untitled Theme",
    colors: {
      keyIdle: "#FBFBFD",
      functionKey: "#EEF0F4",
      primaryFunctionKey: "#E4E7ED",
      accentKey: "#CCD3DE",
      keyPressed: "#D6D8DD",
      keyboardBackground: "#D1D5DB",
      border: "#C2C7D0",
      depth: null,
      accent: "#111827",
      secondary: "#707780"
    },
    shape: { roundnessDp: 5, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
    typography: defaultTypography(),
    additionalNumberRow: { colorMode: "full_mod" },
    accentPolicy: { qwerty: [], dingul: [] },
    icons: {},
    effects: {},
    keyDisplayOverrides: {},
    keyTextColorOverrides: { shiftIndicator: "#2563EB" },
    keyBackgroundColorOverrides: {}
  };
  theme.dingulColors = normalizedDingulColors(theme);
  return theme;
}

function defaultTypography() {
  return {
    fontFamily: "noto_sans_kr",
    primaryTextSizePercent: 78,
    secondaryTextSizePercent: 80,
    primaryTextBold: false,
    primaryTextItalic: false,
    secondaryTextBold: false,
    secondaryTextItalic: false
  };
}

function defaultDingulColors(colors) {
  return {
    alpha: { foreground: colors.accent, background: colors.keyIdle },
    mod: { foreground: colors.secondary, background: colors.functionKey },
    modInv: { foreground: colors.functionKey, background: colors.accentKey }
  };
}

function dotTextOverrides(dark) {
  const palette = dark
    ? ["#EF476F", "#FFD166", "#06D6A0", "#4CC9F0", "#B5179E", "#F77F00"]
    : ["#E63946", "#F4A261", "#2A9D8F", "#457B9D", "#7B2CBF", "#FF6B6B"];
  const keys = [
    "tap:q", "tap:w", "tap:e", "tap:r", "tap:t", "tap:y", "tap:u", "tap:i", "tap:o", "tap:p",
    "tap:a", "tap:s", "tap:d", "tap:f", "tap:g", "tap:h", "tap:j", "tap:k", "tap:l",
    "tap:z", "tap:x", "tap:c", "tap:v", "tap:b", "tap:n", "tap:m",
    "tap:\u3131", "tap:\u3134", "tap:\u3162", "tap:\u3139", "tap:\u3141", "tap:\u3163",
    "__dingul_center_vowel__", "tap:\u3145", "tap:\u3147", "__dingul_wide_vowel__",
    "tap:\u3148", "tap:\u314E", "\u3163.", "\u3161\u3150", "..", ". .",
    "?", ".", "/", "space", "enter", "backspace", "shift", "language", "settings", "options", "reserved"
  ];
  return keys.reduce((overrides, key, index) => {
    overrides[key] = palette[index % palette.length];
    return overrides;
  }, { shiftIndicator: dark ? "#4CC9F0" : "#457B9D" });
}

function dotBackgroundOverrides(dark) {
  return {
    space: dark ? "#1B1F27" : "#E8E4D9",
    enter: dark ? "#252B34" : "#D8D4CA",
    backspace: dark ? "#252B34" : "#D8D4CA",
    shift: dark ? "#252B34" : "#D8D4CA",
    language: dark ? "#252B34" : "#D8D4CA",
    settings: dark ? "#252B34" : "#D8D4CA",
    options: dark ? "#252B34" : "#D8D4CA",
    reserved: dark ? "#252B34" : "#D8D4CA"
  };
}

function buildColorControls() {
  colorFields.forEach(([key, label, description]) => {
    const row = document.createElement("label");
    row.className = "color-row";
    row.innerHTML = `
      <span>${label}</span>
      <button class="info" type="button" aria-label="${label} help">i</button>
      <input id="color-${key}" type="color">
      <input id="text-${key}" type="text">
    `;
    ids.colors.appendChild(row);
    row.querySelector(".info").addEventListener("click", event => {
      event.preventDefault();
      alert(description);
    });
    row.querySelector(`#color-${key}`).addEventListener("input", event => {
      state.colors[key] = event.target.value.toUpperCase();
      document.getElementById(`text-${key}`).value = state.colors[key];
      update();
    });
    row.querySelector(`#text-${key}`).addEventListener("input", event => {
      const value = normalizeColor(event.target.value);
      if (value) {
        state.colors[key] = value;
        document.getElementById(`color-${key}`).value = value;
        update();
      }
    });
  });
}

function buildShapeControls() {
  shapeFields.forEach(([key, label, min, max]) => {
    const row = document.createElement("label");
    row.className = "range-row";
    row.innerHTML = `<span>${label}</span><input id="shape-${key}" type="range" min="${min}" max="${max}"><output id="out-${key}"></output>`;
    ids.shape.appendChild(row);
    row.querySelector(`#shape-${key}`).addEventListener("input", event => {
      state.shape[key] = Number(event.target.value);
      update();
    });
  });
}

function buildDingulRoleControls() {
  dingulRoleFields.forEach(([role, label]) => {
    const row = document.createElement("div");
    row.className = "dingul-role-row";
    row.innerHTML = `
      <span>${label}</span>
      <label>FG <input id="dingul-${role}-foreground" type="color"></label>
      <label>BG <input id="dingul-${role}-background" type="color"></label>
    `;
    ids.dingulRoles.appendChild(row);
    ["foreground", "background"].forEach(slot => {
      row.querySelector(`#dingul-${role}-${slot}`).addEventListener("input", event => {
        state.dingulColors = normalizedDingulColors(state);
        state.dingulColors[role][slot] = event.target.value.toUpperCase();
        update();
      });
    });
      });
}

function populateContractSelects() {
  replaceOptions(ids.fontFamily, fontFamilies);
  replaceOptions(ids.numberRow, numberRowModes);
  replaceOptions(ids.modifierPack, modifierIconPacks);
  replaceOptions(ids.keyDisplayPack, keyDisplayPacks);
}

function replaceOptions(select, options) {
  select.innerHTML = "";
  options.forEach(option => {
    const item = document.createElement("option");
    item.value = option.id;
    item.textContent = option.label;
    select.appendChild(item);
  });
}

function bindStaticControls() {
  ids.name.addEventListener("input", update);
  ids.author.addEventListener("input", update);
  ids.preset.addEventListener("change", () => {
    state = cloneTheme(presets[ids.preset.value]);
    renderForm();
    update();
  });
  ids.depthEnabled.addEventListener("change", () => {
    state.shape.depthEnabled = ids.depthEnabled.checked;
    update();
  });
  ids.customDepth.addEventListener("change", update);
  ids.fontFamily.addEventListener("change", () => {
    state.typography.fontFamily = ids.fontFamily.value;
    update();
  });
  ids.primary.addEventListener("input", () => {
    state.typography.primaryTextSizePercent = Number(ids.primary.value);
    update();
  });
  ids.secondary.addEventListener("input", () => {
    state.typography.secondaryTextSizePercent = Number(ids.secondary.value);
    update();
  });
  [
    [ids.primaryBold, "primaryTextBold"],
    [ids.primaryItalic, "primaryTextItalic"],
    [ids.secondaryBold, "secondaryTextBold"],
    [ids.secondaryItalic, "secondaryTextItalic"]
  ].forEach(([input, key]) => input.addEventListener("change", () => {
    state.typography[key] = input.checked;
    update();
  }));
  ids.numberRow.addEventListener("change", () => {
    state.additionalNumberRow.colorMode = ids.numberRow.value;
    update();
  });
  ids.modifierPack.addEventListener("change", () => {
    state.icons = setIconPack(state.icons, "modifierPackId", ids.modifierPack.value);
    update();
  });
  ids.keyDisplayPack.addEventListener("change", () => {
    state.icons = setIconPack(state.icons, "keyDisplayPackId", ids.keyDisplayPack.value);
    update();
  });
  ids.textOverrides.addEventListener("input", () => {
    state.keyTextColorOverrides = parseOverrides(ids.textOverrides.value);
    update();
  });
  ids.backgroundOverrides.addEventListener("input", () => {
    state.keyBackgroundColorOverrides = parseOverrides(ids.backgroundOverrides.value);
    update();
  });
  document.getElementById("copyJson").addEventListener("click", copyJson);
  document.getElementById("importJson").addEventListener("click", importJson);
  document.getElementById("downloadJson").addEventListener("click", downloadJson);
}

function renderForm() {
  ids.name.value = state.name || "Untitled Theme";
  colorFields.forEach(([key]) => {
    const value = state.colors[key]
      || (key === "panelBackground" ? state.colors.keyboardBackground : "#000000");
    document.getElementById(`color-${key}`).value = value === null ? "#000000" : value;
    document.getElementById(`text-${key}`).value = value === null ? "" : value;
  });
  shapeFields.forEach(([key]) => {
    document.getElementById(`shape-${key}`).value = state.shape[key];
  });
  ids.depthEnabled.checked = Boolean(state.shape.depthEnabled);
  ids.customDepth.checked = Boolean(state.colors.depth);
  ids.fontFamily.value = state.typography.fontFamily;
  ids.primary.value = state.typography.primaryTextSizePercent;
  ids.secondary.value = state.typography.secondaryTextSizePercent;
  ids.primaryBold.checked = Boolean(state.typography.primaryTextBold);
  ids.primaryItalic.checked = Boolean(state.typography.primaryTextItalic);
  ids.secondaryBold.checked = Boolean(state.typography.secondaryTextBold);
  ids.secondaryItalic.checked = Boolean(state.typography.secondaryTextItalic);
  ids.numberRow.value = state.additionalNumberRow.colorMode;
  const icons = normalizedIconPacks(state.icons || {});
  ids.modifierPack.value = icons.modifierPackId || "";
  ids.keyDisplayPack.value = icons.keyDisplayPackId || "";
  state.dingulColors = normalizedDingulColors(state);
  dingulRoleFields.forEach(([role]) => {
    document.getElementById(`dingul-${role}-foreground`).value = state.dingulColors[role].foreground;
    document.getElementById(`dingul-${role}-background`).value = state.dingulColors[role].background;
  });
  ids.textOverrides.value = formatOverrides(state.keyTextColorOverrides);
  ids.backgroundOverrides.value = formatOverrides(state.keyBackgroundColorOverrides);
}

function update() {
  shapeFields.forEach(([key]) => {
    document.getElementById(`out-${key}`).textContent = `${state.shape[key]}dp`;
  });
  ids.primaryOut.textContent = `${state.typography.primaryTextSizePercent}%`;
  ids.secondaryOut.textContent = `${state.typography.secondaryTextSizePercent}%`;
  const theme = buildTheme();
  ids.output.value = JSON.stringify(theme, null, 2);
  renderPreview(theme);
  ids.status.textContent = validateTheme(theme);
}

function buildTheme() {
  const theme = {
    schemaVersion: 1,
    name: ids.name.value.trim() || "Untitled Theme",
    author: ids.author.value.trim() || "local",
    colors: {
      keyIdle: state.colors.keyIdle,
      functionKey: state.colors.functionKey,
      primaryFunctionKey: state.colors.primaryFunctionKey,
      accentKey: state.colors.accentKey,
      keyPressed: state.colors.keyPressed,
      keyboardBackground: state.colors.keyboardBackground,
      panelBackground: state.colors.panelBackground || state.colors.keyboardBackground,
      border: state.colors.border,
      depth: ids.customDepth.checked ? state.colors.depth : null,
      accent: state.colors.accent,
      secondary: state.colors.secondary
    },
    shape: {
      roundnessDp: state.shape.roundnessDp,
      borderWidthDp: state.shape.borderWidthDp,
      keyGapDp: state.shape.keyGapDp,
      depthEnabled: Boolean(state.shape.depthEnabled),
      depthDp: state.shape.depthDp
    },
    additionalNumberRow: { colorMode: state.additionalNumberRow.colorMode },
    accentPolicy: normalizeAccentPolicy(state.accentPolicy),
    dingulColors: normalizedDingulColors(state),
    typography: {
      fontFamily: state.typography.fontFamily,
      primaryTextSizePercent: state.typography.primaryTextSizePercent,
      secondaryTextSizePercent: state.typography.secondaryTextSizePercent,
      primaryTextBold: Boolean(state.typography.primaryTextBold),
      primaryTextItalic: Boolean(state.typography.primaryTextItalic),
      secondaryTextBold: Boolean(state.typography.secondaryTextBold),
      secondaryTextItalic: Boolean(state.typography.secondaryTextItalic)
    },
    keyTextColorOverrides: { ...state.keyTextColorOverrides }
  };
  const icons = normalizedIconPacks(state.icons || {});
  if (icons.modifierPackId || icons.keyDisplayPackId) {
    theme.icons = {};
    if (icons.modifierPackId) {
      theme.icons.modifierPackId = icons.modifierPackId;
    }
    if (icons.keyDisplayPackId) {
      theme.icons.keyDisplayPackId = icons.keyDisplayPackId;
    }
  }
  if (state.effects) {
    theme.effects = { ...state.effects };
  }
  if (Object.keys(state.keyDisplayOverrides || {}).length > 0) {
    theme.keyDisplayOverrides = { ...state.keyDisplayOverrides };
  }
  if (Object.keys(state.keyBackgroundColorOverrides || {}).length > 0) {
    theme.keyBackgroundColorOverrides = { ...state.keyBackgroundColorOverrides };
  }
  return theme;
}

function renderPreview(theme) {
  ids.preview.style.background = theme.colors.panelBackground || theme.colors.keyboardBackground;
  ids.preview.style.backdropFilter = theme.effects?.blur?.enabled ? `blur(${theme.effects.blur.radiusDp || 8}px)` : "none";
  ids.preview.style.boxShadow = theme.effects?.metal?.enabled ? `inset 0 18px 28px rgba(255,255,255,.18), inset 0 -22px 35px rgba(0,0,0,.22)` : "none";
  ids.preview.style.gap = `${theme.shape.keyGapDp}px`;
  ids.preview.style.paddingTop = "16px";
  ids.preview.style.setProperty("--depth-color", theme.colors.depth || theme.colors.border);
  ids.preview.style.fontFamily = fontCss(theme.typography.fontFamily);
  ids.preview.innerHTML = "";

  const rows = [
    ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"],
    ["q", "w", "e", "r", "t", "y", "u", "i", "o", "p"],
    ["a", "s", "d", "f", "g", "h", "j", "k", "l"],
    ["Shift", "z", "x", "c", "v", "b", "n", "m", "Bksp"],
    ["Settings", "Reserved", "Space", "Lang", "Enter"]
  ];

  rows.forEach(row => {
    const rowEl = document.createElement("div");
    rowEl.className = "key-row";
    rowEl.style.gridTemplateColumns = row.map(key => key === "Space" ? "2.5fr" : "1fr").join(" ");
    row.forEach(label => {
      const key = document.createElement("div");
      const role = roleForPreview(label, "qwerty", theme);
      const number = /^[0-9]$/.test(label);
      const numberRole = number ? numberRowRole(theme.additionalNumberRow.colorMode, label) : null;
      const bgOverride = backgroundColorFor(label, theme);
      key.className = "key";
      key.style.background = bgOverride || backgroundForRole(numberRole || role, theme);
      key.style.borderColor = theme.colors.border;
      key.style.borderWidth = `${theme.shape.borderWidthDp}px`;
      key.style.borderRadius = `${theme.shape.roundnessDp}px`;
      key.style.boxShadow = theme.shape.depthEnabled
        ? `inset 0 -${theme.shape.depthDp}px ${theme.colors.depth || theme.colors.border}`
        : "none";
      key.style.color = number ? textColorForNumberRole(numberRole, theme) : textColorFor(label, theme);
      key.style.fontSize = `${14 * theme.typography.primaryTextSizePercent / 100}px`;
      key.style.fontWeight = theme.typography.primaryTextBold ? "700" : "400";
      const displayOverride = displayOverrideFor(label, theme);
      if (displayOverride?.type === "icon" && displayOverride.value === "dot") {
        const mainDot = document.createElement("span");
        mainDot.className = "main-dot";
        mainDot.style.background = textColorFor(label, theme);
        key.appendChild(mainDot);
      } else if (displayOverride?.type === "text") {
        if (displayOverride.value === "hihihi" && isSimpleTextPack(theme.icons?.keyDisplayPackId)) {
          appendHihihiGlyph(key, textColorFor(label, theme));
        } else {
          key.textContent = displayOverride.value;
        }
        if (displayOverride.value !== "hihihi" && isSimpleTextPack(theme.icons?.keyDisplayPackId)) {
          key.style.fontWeight = "700";
        }
      } else if (iconForPreview(label) && renderModifierPackGlyph(key, label, theme)) {
        // rendered by helper
      } else {
        key.textContent = label;
      }
      appendSubLegend(key, label, theme);
      if (label === "Shift") {
        const dot = document.createElement("span");
        dot.className = "shift-dot";
        dot.style.background = theme.keyTextColorOverrides.shiftIndicator || theme.colors.accent;
        key.appendChild(dot);
      }
      rowEl.appendChild(key);
    });
    ids.preview.appendChild(rowEl);
  });
}

function appendSubLegend(key, label, theme) {
  const sub = subLegendFor(label);
  if (!sub || shouldHideSubLegend(label, theme)) {
    return;
  }
  if (!theme.metadata?.previewSlideHints) {
    return;
  }
  const item = document.createElement("span");
  item.className = "sublegend";
  item.textContent = sub;
  item.style.color = theme.colors.secondary;
  item.style.fontSize = `${10 * theme.typography.secondaryTextSizePercent / 100}px`;
  item.style.fontWeight = theme.typography.secondaryTextBold ? "700" : "400";
  key.appendChild(item);
}

function subLegendFor(label) {
  const map = {
    q: "1", w: "2", e: "3", r: "4", t: "5", y: "6", u: "7", i: "8", o: "9", p: "0",
    a: "@", s: "#%", d: "/", f: "*", g: "~^", h: "-_", j: "+=", k: "<>", l: "\u2665",
    z: "()", x: "[]", c: ":", v: "\"", b: "&|", n: "!", m: "?"
  };
  return map[label] || "";
}

function roleForPreview(label, layout = "qwerty", theme = null) {
  if (label === "Space") {
    return "alpha";
  }
  if (label === "Enter") {
    return accentPolicyIncludes(theme, layout, "modCtrl") ? "accent" : "modifier";
  }
  if (["Shift", "Bksp", "Lang", "Options", "Reserved", "Settings", "?", ".", "/"].includes(label)) {
    const target = semanticTargetForPreview(label, layout);
    if (accentPolicyIncludes(theme, layout, target)
        || ([".", "/", "?"].includes(label) && accentPolicyIncludes(theme, layout, "punctuation"))) {
      return "accent";
    }
    return "modifier";
  }
  if (["?123"].includes(label)) {
    return "modifier";
  }
  return "alpha";
}

function backgroundForRole(role, theme) {
  const dingul = normalizedDingulColors(theme);
  switch (role) {
    case "mod":
    case "modifier":
      return dingul.mod.background;
    case "modInv":
      return dingul.modInv.background;
    case "accent":
      return theme.colors.accentKey;
    case "alpha":
    default:
      return dingul.alpha.background;
  }
}

function semanticTargetForPreview(label, layout) {
  const normalized = label.toLowerCase();
  if (["settings", "enter"].includes(normalized)) {
    return "modCtrl";
  }
  if (["reserved", "lang", "language"].includes(normalized)) {
    return "modMeta";
  }
  if (["shift", "bksp", "backspace"].includes(normalized)) {
    return "modCommand";
  }
  if (layout === "dingul" && label === ".") {
    return "modEnter";
  }
  if (layout === "dingul" && label === "/") {
    return "modShift";
  }
  if (layout === "dingul" && label === "?") {
    return "punctuation";
  }
  return "";
}

function accentPolicyIncludes(theme, layout, target) {
  if (!theme || !target) {
    return false;
  }
  return Array.isArray(theme.accentPolicy?.[layout]) && theme.accentPolicy[layout].includes(target);
}

function shouldHideSubLegend(label, theme) {
  if (displayOverrideFor(label, theme)) {
    return true;
  }
  const pack = theme.icons?.modifierPackId || "line-mono";
  return iconForPreview(label) && (pack === "dots-lines" || pack === "metropolis-graph" || pack === "metropolis-points");
}

function numberRowRole(mode, label) {
  const normalized = normalizeNumberRowMode(mode);
  const option = numberRowModes.find(item => item.id === normalized);
  const inner = label >= "4" && label <= "7";
  if (option) {
    return inner ? option.innerRole : option.outerRole;
  }
  return "mod";
}

function textColorForNumberRole(role, theme) {
  const dingul = normalizedDingulColors(theme);
  switch (role) {
    case "alpha":
      return dingul.alpha.foreground;
    case "accent":
      return dingul.modInv.foreground;
    case "mod":
    default:
      return dingul.mod.foreground;
  }
}

function overrideKeyForLabel(label) {
  if (label === "Shift") {
    return "shift";
  }
  if (label === "Bksp") {
    return "backspace";
  }
  if (label === "Lang") {
    return "language";
  }
  if (label === "Space") {
    return "space";
  }
  if (label === "Enter") {
    return "enter";
  }
  return label.length === 1 ? `tap:${label.toLowerCase()}` : label.toLowerCase();
}

function iconForPreview(label) {
  return ["Shift", "Bksp", "Options", "Reserved", "Space", "Lang", "Settings", "Enter"].includes(label);
}

function displayOverrideFor(label, theme) {
  const overrides = theme.keyDisplayOverrides || {};
  const keys = overrides.keys || {};
  const key = overrideKeyForLabel(label);
  const exact = keys[key] || keys[label.toLowerCase()] || keys[label];
  if (exact) {
    return exact;
  }
  const packExact = keyDisplayPackOverrideFor(label, theme.icons?.keyDisplayPackId);
  if (packExact) {
    return packExact;
  }
  if (iconForPreview(label) && overrides.modifiers) {
    return overrides.modifiers;
  }
  if (isAlphaPreviewLabel(label) && overrides.alpha) {
    return overrides.alpha;
  }
  return null;
}

function keyDisplayPackOverrideFor(label, pack) {
  if (isSimpleTextPack(pack)) {
    switch (label) {
      case "Enter":
        return { type: "text", value: "hihihi" };
      default:
        return null;
    }
  }
  if (isGitCommandPack(pack)) {
    switch (label) {
      case "Enter":
        return { type: "text", value: "exec" };
      case "Bksp":
        return { type: "text", value: "reset" };
      case "Shift":
        return { type: "text", value: "rebase" };
      case "Space":
        return { type: "text", value: "pull" };
      case "Lang":
        return { type: "text", value: "fetch" };
      default:
        return null;
    }
  }
  return null;
}

function renderModifierPackGlyph(key, label, theme) {
  const pack = theme.icons?.modifierPackId || "line-mono";
  if (pack === "dots-lines") {
    const line = document.createElement("span");
    if (label === "Space") {
      line.className = "mod-pack-line five-dot-line";
    } else if (label === "Lang" || label === "Reserved") {
      line.className = "mod-pack-line single-dot-line";
    } else {
      line.className = "mod-pack-line dotted-line";
    }
    line.style.background = textColorFor(label, theme);
    line.style.color = textColorFor(label, theme);
    key.appendChild(line);
    return true;
  }
  if (pack === "metropolis-graph" || pack === "metropolis-points") {
    appendLineIcon(key, label, metropolisIconColorFor(label, theme));
    return true;
  }
  appendLineIcon(key, label, modifierIconColorFor(pack, label, theme));
  return true;
}

function modifierIconColorFor(pack, label, theme) {
  if (pack === "accent-color") {
    return "#06B6D4";
  }
  return textColorFor(label, theme);
}

function appendLineIcon(key, label, color) {
  const icon = iconSvgFor(label);
  if (!icon) {
    return;
  }
  const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("class", "key-glyph");
  svg.setAttribute("viewBox", "0 0 24 24");
  svg.setAttribute("aria-hidden", "true");
  svg.style.color = color;
  for (const child of icon) {
    const node = document.createElementNS("http://www.w3.org/2000/svg", child.type);
    for (const [name, value] of Object.entries(child.attrs)) {
      node.setAttribute(name, value);
    }
    svg.appendChild(node);
  }
  key.appendChild(svg);
}

function appendHihihiGlyph(key, color) {
  const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  svg.setAttribute("class", "hihihi-glyph");
  svg.setAttribute("viewBox", "0 0 120 32");
  svg.setAttribute("aria-hidden", "true");
  svg.style.color = color;
  const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
  path.setAttribute("d", "M8 22C10 10 17 10 17 22M8 16H18M25 12C30 9 33 11 31 16C29 21 24 20 26 14M41 22C43 10 50 10 50 22M41 16H51M58 12C63 9 66 11 64 16C62 21 57 20 59 14M75 22C77 10 84 10 84 22M75 16H85M92 12C97 9 100 11 98 16C96 21 91 20 93 14M109 12C114 9 117 11 115 16C113 21 108 20 110 14");
  path.setAttribute("fill", "none");
  path.setAttribute("stroke", "currentColor");
  path.setAttribute("stroke-width", "4");
  path.setAttribute("stroke-linecap", "round");
  path.setAttribute("stroke-linejoin", "round");
  svg.appendChild(path);
  key.appendChild(svg);
}

function iconSvgFor(label) {
  const base = {
    fill: "none",
    stroke: "currentColor",
    "stroke-width": "2",
    "stroke-linecap": "round",
    "stroke-linejoin": "round"
  };
  switch (label) {
    case "Shift":
      return [{ type: "path", attrs: { ...base, d: "M12 4 4.5 12H8v7h8v-7h3.5L12 4Z" } }];
    case "Bksp":
      return [
        { type: "path", attrs: { ...base, d: "M3 12 8 6h13v12H8l-5-6Z" } },
        { type: "path", attrs: { ...base, d: "m11 9 5 6m0-6-5 6" } }
      ];
    case "Lang":
      return [
        { type: "circle", attrs: { ...base, cx: "12", cy: "12", r: "8" } },
        { type: "path", attrs: { ...base, d: "M4 12h16M12 4c2 2.2 3 4.8 3 8s-1 5.8-3 8M12 4c-2 2.2-3 4.8-3 8s1 5.8 3 8" } }
      ];
    case "Enter":
      return [{ type: "path", attrs: { ...base, d: "M20 5v7a4 4 0 0 1-4 4H6m4-4-4 4 4 4" } }];
    case "Space":
      return [{ type: "path", attrs: { ...base, d: "M5 10v4h14v-4" } }];
    case "Options":
      return [
        { type: "path", attrs: { ...base, d: "M4 7h16M4 12h16M4 17h16" } },
        { type: "circle", attrs: { ...base, cx: "9", cy: "7", r: "1.5" } },
        { type: "circle", attrs: { ...base, cx: "15", cy: "12", r: "1.5" } },
        { type: "circle", attrs: { ...base, cx: "9", cy: "17", r: "1.5" } }
      ];
    case "Reserved":
      return [{ type: "path", attrs: { ...base, d: "M7 4h10v16l-5-3-5 3V4Z" } }];
    case "Settings":
      return [
        { type: "circle", attrs: { ...base, cx: "12", cy: "12", r: "3" } },
        { type: "path", attrs: { ...base, d: "M12 3v3M12 18v3M4.2 7.5l2.6 1.5M17.2 15l2.6 1.5M19.8 7.5 17.2 9M6.8 15l-2.6 1.5" } }
      ];
    default:
      return null;
  }
}

function metropolisColorFor(label) {
  switch (label) {
    case "Options":
    case "Bksp":
      return "#FFB000";
    case "Shift":
    case "Reserved":
      return "#FF4B3E";
    case "Enter":
    case "Lang":
    case "Settings":
      return "#66E3C4";
    case "Space":
    default:
      return "#70D7E8";
  }
}

function metropolisIconColorFor(label, theme) {
  const overrides = theme.keyTextColorOverrides || {};
  const key = overrideKeyForLabel(label);
  return overrides[key]
    || overrides[label.toLowerCase()]
    || overrides[label]
    || metropolisColorFor(label);
}

function isSimpleTextPack(pack) {
  return (themeContract.simpleTextPackIds || ["simple-text"]).includes(pack);
}

function isGitCommandPack(pack) {
  return pack === "git-commands";
}

function isAlphaPreviewLabel(label) {
  return /^[a-z]$/i.test(label)
    || /^[0-9]$/.test(label)
    || /^[\u3131-\u318e\uac00-\ud7a3]$/.test(label)
    || ["?", ".", "/", "..", ". .", "\u3163.", "\u3161\u3150"].includes(label);
}

function legacyDisplayOverrides(legendStyle) {
  return legendStyle?.preset === "dots"
    ? { alpha: { type: "icon", value: "dot" } }
    : {};
}

function textColorFor(label, theme) {
  const key = overrideKeyForLabel(label);
  const role = roleForPreview(label, "qwerty", theme);
  const dingul = normalizedDingulColors(theme);
  return theme.keyTextColorOverrides[key]
    || theme.keyTextColorOverrides[label.toLowerCase()]
    || theme.keyTextColorOverrides[label]
    || (role === "alpha" ? theme.keyTextColorOverrides.alpha : null)
    || (role === "accent" ? (theme.keyTextColorOverrides.modInv || theme.keyTextColorOverrides.mod_inv) : null)
    || (role === "modInv" ? (theme.keyTextColorOverrides.modInv || theme.keyTextColorOverrides.mod_inv) : null)
    || (role === "modifier" ? (theme.keyTextColorOverrides.mod || theme.keyTextColorOverrides.modifiers) : null)
    || (role === "alpha" ? dingul.alpha.foreground : null)
    || (role === "accent" ? dingul.modInv.foreground : null)
    || (role === "modInv" ? dingul.modInv.foreground : null)
    || (role === "modifier" ? dingul.mod.foreground : null)
    || theme.colors.accent;
}

function backgroundColorFor(label, theme) {
  const key = overrideKeyForLabel(label);
  const overrides = theme.keyBackgroundColorOverrides || {};
  const role = roleForPreview(label, "qwerty", theme);
  return overrides[key]
    || overrides[label.toLowerCase()]
    || overrides[label]
    || (role === "alpha" ? overrides.alpha : null)
    || (role === "modInv" ? (overrides.modInv || overrides.mod_inv) : null)
    || (role === "modifier" ? (overrides.mod || overrides.modifiers) : null)
    || null;
}

function fontCss(fontFamily) {
  switch (fontFamily) {
    case "noto_serif_kr":
      return `"Noto Serif KR", Georgia, serif`;
    case "d2coding":
      return `"D2Coding", "Cascadia Mono", Consolas, monospace`;
    case "noto_sans_kr":
      return `"Noto Sans KR", "Segoe UI", sans-serif`;
    case "default":
    default:
      return `"Segoe UI", system-ui, sans-serif`;
  }
}

function validateTheme(theme) {
  const required = themeContract.colorFields
    .filter(field => !field.optional)
    .map(field => field.key);
  const missing = required.filter(key => !normalizeColor(theme.colors[key]));
  if (missing.length) {
    return `Missing or invalid colors: ${missing.join(", ")}`;
  }
  return "Valid schemaVersion 1 theme JSON.";
}

function normalizedDingulColors(theme) {
  const colors = theme.colors || {};
  const defaults = defaultDingulColors(colors);
  const raw = theme.dingulColors || {};
  return {
    alpha: normalizeDingulRole(raw.alpha, defaults.alpha),
    mod: normalizeDingulRole(raw.mod || raw.modifier, defaults.mod),
    modInv: normalizeDingulRole(raw.modInv || raw.mod_inv || raw.modifierInverted, defaults.modInv)
  };
}

function normalizeDingulRole(raw, fallback) {
  if (typeof raw === "string") {
    return { foreground: normalizeColor(raw) || fallback.foreground, background: fallback.background };
  }
  const value = raw || {};
  return {
    foreground: normalizeColor(value.foreground || value.text || value.fg) || fallback.foreground,
    background: normalizeColor(value.background || value.key || value.bg) || fallback.background
  };
}

function themeJsonToPreset(parsed) {
  const base = createDefaultTheme();
  const preset = {
    name: parsed.name || "Imported Theme",
    colors: { ...base.colors, ...(parsed.colors || {}) },
    shape: { ...base.shape, ...(parsed.shape || {}) },
    typography: normalizeTypography(parsed.typography, base.typography),
    metadata: parsed.metadata || {},
    effects: parsed.effects || base.effects || {},
    icons: normalizedIconPacks(parsed.icons || {}),
    accentPolicy: normalizeAccentPolicy(parsed.accentPolicy || base.accentPolicy),
    additionalNumberRow: {
      colorMode: normalizeNumberRowMode(parsed.additionalNumberRow?.colorMode)
    },
    keyDisplayOverrides: parsed.keyDisplayOverrides || legacyDisplayOverrides(parsed.legendStyle),
    keyTextColorOverrides: parsed.keyTextColorOverrides || parsed.keyColorOverrides || {},
    keyBackgroundColorOverrides: parsed.keyBackgroundColorOverrides || {}
  };
  preset.dingulColors = normalizedDingulColors({ ...preset, dingulColors: parsed.dingulColors });
  return preset;
}

function normalizeAccentPolicy(rawPolicy) {
  const policy = rawPolicy || {};
  const allowed = new Set(themeContract.accentPolicyTargets || []);
  return {
    qwerty: normalizeAccentTargets(policy.qwerty, allowed),
    dingul: normalizeAccentTargets(policy.dingul, allowed)
  };
}

function normalizeAccentTargets(rawTargets, allowed) {
  if (!Array.isArray(rawTargets)) {
    return [];
  }
  return rawTargets.filter(target => allowed.has(target));
}

function normalizeTypography(rawTypography, fallbackTypography) {
  const raw = rawTypography || {};
  return {
    fontFamily: raw.fontFamily || fallbackTypography.fontFamily,
    primaryTextSizePercent: Number.isFinite(raw.primaryTextSizePercent)
      ? raw.primaryTextSizePercent
      : fallbackTypography.primaryTextSizePercent,
    secondaryTextSizePercent: Number.isFinite(raw.secondaryTextSizePercent)
      ? raw.secondaryTextSizePercent
      : fallbackTypography.secondaryTextSizePercent,
    primaryTextBold: Boolean(raw.primaryTextBold),
    primaryTextItalic: Boolean(raw.primaryTextItalic),
    secondaryTextBold: Boolean(raw.secondaryTextBold),
    secondaryTextItalic: Boolean(raw.secondaryTextItalic)
  };
}

function normalizedIconPacks(rawIcons) {
  const icons = { ...rawIcons };
  const modifierAlias = themeContract.legacyModifierIconPackIds?.[icons.modifierPackId];
  if (modifierAlias) {
    icons.modifierPackId = modifierAlias;
  }
  const displayAlias = themeContract.legacyKeyDisplayPackIds?.[icons.keyDisplayPackId];
  if (displayAlias) {
    icons.keyDisplayPackId = displayAlias;
  }
  if (isSimpleTextPack(icons.modifierPackId)) {
    icons.keyDisplayPackId = "simple-text";
    delete icons.modifierPackId;
  }
  if (isSimpleTextPack(icons.keyDisplayPackId)) {
    icons.keyDisplayPackId = "simple-text";
  }
  return icons;
}

function setIconPack(rawIcons, key, value) {
  const icons = { ...(rawIcons || {}) };
  if (value) {
    icons[key] = value;
  } else {
    delete icons[key];
  }
  return normalizedIconPacks(icons);
}

function normalizeNumberRowMode(mode) {
  for (const option of numberRowModes) {
    if (option.id === mode || (option.legacyAliases || []).includes(mode)) {
      return option.id;
    }
  }
  return themeContract.defaultNumberRowMode || "full_mod";
}

function importJson() {
  try {
    const parsed = JSON.parse(ids.output.value);
    if (parsed.schemaVersion !== 1) {
      throw new Error("Only schemaVersion 1 is supported.");
    }
    state = themeJsonToPreset(parsed);
    renderForm();
    update();
    ids.status.textContent = "Imported JSON.";
  } catch (error) {
    ids.status.textContent = `Import failed: ${error.message}`;
  }
}

async function copyJson() {
  await navigator.clipboard.writeText(ids.output.value);
  ids.status.textContent = "Copied JSON to clipboard.";
}

function downloadJson() {
  const blob = new Blob([ids.output.value], { type: "application/json" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `${(ids.name.value || "s3keyboard-theme").replace(/[^a-z0-9_-]+/gi, "-")}.json`;
  link.click();
  URL.revokeObjectURL(link.href);
}

function parseOverrides(text) {
  const overrides = {};
  text.split(/\r?\n/).forEach(line => {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) {
      return;
    }
    const match = trimmed.match(/^(.+?)\s*=\s*(#[0-9a-fA-F]{6})$/);
    if (match) {
      overrides[match[1].trim()] = match[2].toUpperCase();
    }
  });
  return overrides;
}

function formatOverrides(overrides) {
  return Object.entries(overrides || {})
    .map(([key, value]) => `${key} = ${value}`)
    .join("\n");
}

function normalizeColor(value) {
  if (typeof value !== "string") {
    return null;
  }
  const text = value.trim();
  if (/^#[0-9a-fA-F]{6}$/.test(text)) {
    return text.toUpperCase();
  }
  if (/^[0-9a-fA-F]{6}$/.test(text)) {
    return `#${text.toUpperCase()}`;
  }
  return null;
}

function cloneTheme(theme) {
  return JSON.parse(JSON.stringify(theme));
}
