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
const accentPolicyReference = buildAccentPolicyReference();
const perKeyOverrideReference = buildPerKeyOverrideReference();
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
  keyFaceGradientEnabled: document.getElementById("keyFaceGradientEnabled"),
  keyFaceGradientStrength: document.getElementById("keyFaceGradientStrengthPercent"),
  keyFaceGradientOut: document.getElementById("keyFaceGradientOut"),
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
  numberRowGuide: document.getElementById("numberRowModeGuide"),
  modifierPack: document.getElementById("modifierPackId"),
  keyDisplayPack: document.getElementById("keyDisplayPackId"),
  accentPolicyMap: document.getElementById("accentPolicyMap"),
  perKeyMap: document.getElementById("perKeyOverrideMap"),
  aiPrompt: document.getElementById("aiPrompt"),
  textOverrides: document.getElementById("textOverridesText"),
  backgroundOverrides: document.getElementById("backgroundOverridesText"),
  output: document.getElementById("jsonOutput"),
  preview: document.getElementById("keyboardPreview"),
  dingulPreview: document.getElementById("dingulKeyboardPreview"),
  status: document.getElementById("status")
};

init();

async function init() {
  await loadExternalPresets();
  if (presets["ios-clean-light"]) {
    state = cloneTheme(presets["ios-clean-light"]);
  }
  if (ids.preset) {
    Object.entries(presets).forEach(([id, preset]) => {
      const option = document.createElement("option");
      option.value = id;
      option.textContent = preset.name;
      ids.preset.appendChild(option);
    });
  }
  buildColorControls();
  buildShapeControls();
  buildDingulRoleControls();
  renderNumberRowModeGuide();
  renderAccentPolicyMap();
  renderPerKeyOverrideMap();
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
      alphaKey: "#FBFBFD",
      modifierKey: "#EEF0F4",      accentKey: "#CCD3DE",
      keyPressed: "#D6D8DD",
      keyboardBackground: "#D1D5DB",
      border: "#C2C7D0",
      depth: null,
      accent: "#111827",
      secondary: "#707780"
    },
    shape: { roundnessDp: 5, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0 },
    typography: defaultTypography(),
    additionalNumberRow: { colorMode: "half_mod_4567" },
    accentPolicy: cloneAccentPolicy(themeContract.defaultAccentPolicy),
    icons: {},
    effects: defaultVisualEffects(),
    keyDisplayOverrides: {},
    keyTextColorOverrides: {},
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
    secondaryTextBold: true,
    secondaryTextItalic: false
  };
}

function defaultVisualEffects() {
  return {
    keyFaceGradient: {
      enabled: true,
      strengthPercent: 22
    }
  };
}

function defaultDingulColors(colors) {
  return {
    alpha: { foreground: colors.accent, background: colors.alphaKey },
    mod: { foreground: colors.secondary, background: colors.modifierKey },
    modInv: { foreground: colors.modifierKey, background: colors.accentKey }
  };
}

function buildAccentPolicyReference() {
  const explicit = {
    modMeta: {
      qwerty: "bottom Reserved + Lang",
      dingul: "bottom Reserved + Lang"
    },
    modCtrl: {
      qwerty: "bottom Settings/Options + Enter",
      dingul: "bottom Settings/Options + Enter"
    },
    settingsEnter: {
      qwerty: "bottom Settings/Options + Enter",
      dingul: "bottom Settings/Options + Enter"
    },
    enter: {
      qwerty: "bottom Enter only",
      dingul: "bottom Enter only"
    },
    modCommand: {
      qwerty: "Shift + Backspace",
      dingul: "Backspace; Shift if present"
    },
    qwertyShift: {
      qwerty: "QWERTY Shift",
      dingul: "not used"
    },
    shift: {
      qwerty: "QWERTY Shift",
      dingul: "Shift if present"
    },
    backspace: {
      qwerty: "Backspace",
      dingul: "Backspace"
    },
    modEnter: {
      qwerty: "not used",
      dingul: "right column . visual Enter"
    },
    dingulDot: {
      qwerty: "not used",
      dingul: "right column . visual Enter"
    },
    modShift: {
      qwerty: "not used",
      dingul: "right column / visual Shift"
    },
    dingulSlash: {
      qwerty: "not used",
      dingul: "right column / visual Shift"
    },
    punctuation: {
      qwerty: "not used",
      dingul: "right column . + /"
    },
    question: {
      qwerty: "not used",
      dingul: "right column ?"
    },
    escPoint: {
      qwerty: "number row 1 when visible; otherwise q",
      dingul: "number row 1 when visible; otherwise ㄱ"
    },
    perKey: {
      qwerty: "no group; use exact key overrides",
      dingul: "no group; use exact key overrides"
    },
    none: {
      qwerty: "no accent keys",
      dingul: "no accent keys"
    }
  };
  const targetSet = new Set(themeContract.accentPolicyTargets || []);
  return Object.entries(explicit)
    .filter(([id]) => targetSet.has(id))
    .map(([id, value]) => ({ id, ...value }));
}

function buildPerKeyOverrideReference() {
  return [
    {
      scope: "QWERTY alpha",
      keys: "tap:q ... tap:p, tap:a ... tap:l, tap:z ... tap:m"
    },
    {
      scope: "Dingul alpha",
      keys: "tap:\u3131, tap:\u3134, tap:\u3139, tap:\u3141, tap:\u3145, tap:\u3147, tap:\u3148, tap:\u314E, __dingul_center_vowel__, __dingul_wide_vowel__, \u3163., \u3161\u3150, .."
    },
    {
      scope: "Dingul punctuation",
      keys: "?, ., /"
    },
    {
      scope: "Bottom controls",
      keys: "settings, options, reserved, space, language, enter"
    },
    {
      scope: "Command keys",
      keys: "shift, backspace, shiftIndicator"
    },
    {
      scope: "Number row",
      keys: "1, 2, 3, 4, 5, 6, 7, 8, 9, 0"
    },
    {
      scope: "Semantic groups",
      keys: "alpha, modifiers, modInv"
    }
  ];
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

function renderAccentPolicyMap() {
  ids.accentPolicyMap.innerHTML = "";
  [
    ["Target", "QWERTY keys", "Dingul keys"],
    ...accentPolicyReference.map(item => [item.id, item.qwerty, item.dingul])
  ].forEach((row, index) => {
    row.forEach((value, column) => {
      const cell = document.createElement("div");
      cell.className = `mapping-cell${index === 0 ? " mapping-head" : ""}${column === 0 && index > 0 ? " mapping-target" : ""}`;
      cell.textContent = value;
      ids.accentPolicyMap.appendChild(cell);
    });
  });
}

function renderPerKeyOverrideMap() {
  ids.perKeyMap.innerHTML = "";
  [
    ["Scope", "Override keys"],
    ...perKeyOverrideReference.map(item => [item.scope, item.keys])
  ].forEach((row, index) => {
    row.forEach((value, column) => {
      const cell = document.createElement("div");
      cell.className = `mapping-cell${index === 0 ? " mapping-head" : ""}${column === 1 && index > 0 ? " mapping-target" : ""}`;
      cell.textContent = value;
      ids.perKeyMap.appendChild(cell);
    });
  });
}

function renderNumberRowModeGuide() {
  if (!ids.numberRowGuide) {
    return;
  }
  ids.numberRowGuide.innerHTML = "";
  numberRowModes.forEach(option => {
    const row = document.createElement("div");
    row.className = "number-row-mode";

    const name = document.createElement("span");
    name.className = "number-row-mode-name";
    name.textContent = option.label;

    const detail = document.createElement("span");
    detail.className = "number-row-mode-detail";
    detail.textContent = `1 2 3 8 9 0 = ${option.outerRole}; 4 5 6 7 = ${option.innerRole}`;

    row.appendChild(name);
    row.appendChild(detail);
    ids.numberRowGuide.appendChild(row);
  });
}

function numberRowModePromptGuide() {
  return numberRowModes
    .map(option => `- ${option.id}: 1 2 3 8 9 0 = ${option.outerRole}; 4 5 6 7 = ${option.innerRole}`)
    .join("\n");
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
  if (ids.preset) {
    ids.preset.addEventListener("change", () => {
      state = cloneTheme(presets[ids.preset.value]);
      renderForm();
      update();
    });
  }
  ids.depthEnabled.addEventListener("change", () => {
    state.shape.depthEnabled = ids.depthEnabled.checked;
    update();
  });
  ids.customDepth.addEventListener("change", update);
  ids.keyFaceGradientEnabled.addEventListener("change", () => {
    state.effects = normalizedVisualEffects(state.effects);
    state.effects.keyFaceGradient.enabled = ids.keyFaceGradientEnabled.checked;
    update();
  });
  ids.keyFaceGradientStrength.addEventListener("input", () => {
    state.effects = normalizedVisualEffects(state.effects);
    state.effects.keyFaceGradient.strengthPercent = Number(ids.keyFaceGradientStrength.value);
    update();
  });
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
  if (ids.textOverrides) {
    ids.textOverrides.addEventListener("input", () => {
      state.keyTextColorOverrides = parseOverrides(ids.textOverrides.value);
      update();
    });
  }
  if (ids.backgroundOverrides) {
    ids.backgroundOverrides.addEventListener("input", () => {
      state.keyBackgroundColorOverrides = parseOverrides(ids.backgroundOverrides.value);
      update();
    });
  }
  document.getElementById("copyJson").addEventListener("click", copyJson);
  document.getElementById("copyAiPrompt").addEventListener("click", copyAiPrompt);
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
  state.effects = normalizedVisualEffects(state.effects);
  ids.keyFaceGradientEnabled.checked = Boolean(state.effects.keyFaceGradient.enabled);
  ids.keyFaceGradientStrength.value = state.effects.keyFaceGradient.strengthPercent;
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
  if (ids.textOverrides) {
    ids.textOverrides.value = formatOverrides(state.keyTextColorOverrides);
  }
  if (ids.backgroundOverrides) {
    ids.backgroundOverrides.value = formatOverrides(state.keyBackgroundColorOverrides);
  }
}

function update() {
  shapeFields.forEach(([key]) => {
    document.getElementById(`out-${key}`).textContent = `${state.shape[key]}dp`;
  });
  ids.primaryOut.textContent = `${state.typography.primaryTextSizePercent}%`;
  ids.secondaryOut.textContent = `${state.typography.secondaryTextSizePercent}%`;
  ids.keyFaceGradientOut.textContent =
    `${normalizedVisualEffects(state.effects).keyFaceGradient.strengthPercent}%`;
  const theme = buildTheme();
  ids.output.value = JSON.stringify(theme, null, 2);
  ids.aiPrompt.value = buildImageThemePrompt(theme);
  renderPreview(theme);
  ids.status.textContent = validateTheme(theme);
}

function buildTheme() {
  const theme = {
    schemaVersion: 1,
    name: ids.name.value.trim() || "Untitled Theme",
    author: ids.author.value.trim() || "local",
    colors: {
      alphaKey: state.colors.alphaKey,
      modifierKey: state.colors.modifierKey,      accentKey: state.colors.accentKey,
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
  theme.effects = normalizedVisualEffects(state.effects);
  if (Object.keys(state.keyDisplayOverrides || {}).length > 0) {
    theme.keyDisplayOverrides = { ...state.keyDisplayOverrides };
  }
  if (Object.keys(state.keyBackgroundColorOverrides || {}).length > 0) {
    theme.keyBackgroundColorOverrides = { ...state.keyBackgroundColorOverrides };
  }
  return theme;
}

function renderPreview(theme) {
  renderKeyboardPreview(ids.preview, theme, "qwerty", qwertyPreviewRows());
  renderKeyboardPreview(ids.dingulPreview, theme, "dingul", dingulPreviewRows());
}

function qwertyPreviewRows() {
  return [
    { labels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"] },
    { labels: ["q", "w", "e", "r", "t", "y", "u", "i", "o", "p"] },
    { labels: ["a", "s", "d", "f", "g", "h", "j", "k", "l"] },
    { labels: ["Shift", "z", "x", "c", "v", "b", "n", "m", "Bksp"] },
    { labels: ["Settings", "Reserved", "Space", "Lang", "Enter"] }
  ];
}

function dingulPreviewRows() {
  const mainColumns = ["1.67fr", "1.67fr", "1.67fr", "1fr"];
  return [
    { labels: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"] },
    { labels: ["\u3131", "\u3134", "\u3162", "Bksp"], columns: mainColumns },
    { labels: ["\u3139", "\u3141", "\u3163.", "?"], columns: mainColumns },
    { labels: ["\u3145", "\u3147", "\u3161\u3150", "."], columns: mainColumns },
    { labels: ["\u3148", "\u314e", "..", "/"], columns: mainColumns },
    { labels: ["Settings", "Reserved", "Space", "Lang", "Enter"] }
  ];
}

function renderKeyboardPreview(container, theme, layout, rows) {
  const effects = normalizedVisualEffects(theme.effects);
  container.style.background = theme.colors.panelBackground || theme.colors.keyboardBackground;
  container.style.backdropFilter = effects.blur?.enabled ? `blur(${effects.blur.radiusDp || 8}px)` : "none";
  container.style.boxShadow = effects.metal?.enabled ? `inset 0 18px 28px rgba(255,255,255,.18), inset 0 -22px 35px rgba(0,0,0,.22)` : "none";
  container.style.gap = `${theme.shape.keyGapDp}px`;
  container.style.paddingTop = "16px";
  container.style.setProperty("--depth-color", normalizeColor(theme.colors.depth) || "transparent");
  container.style.fontFamily = fontCss(theme.typography.fontFamily);
  container.innerHTML = "";

  rows.forEach(row => {
    const labels = row.labels || row;
    const rowEl = document.createElement("div");
      rowEl.className = "key-row";
      rowEl.style.gridTemplateColumns = previewGridColumns(labels, row.columns);
      rowEl.style.gap = `${theme.shape.keyGapDp}px`;
      labels.forEach(label => {
        rowEl.appendChild(renderPreviewKey(label, theme, layout));
      });
      container.appendChild(rowEl);
  });
}

function previewGridColumns(labels, columns) {
  if (columns) {
    return columns.join(" ");
  }
  return labels.map(label => label === "Space" ? "2.5fr" : "1fr").join(" ");
}

function renderPreviewKey(label, theme, layout) {
  const key = document.createElement("div");
  const role = roleForPreview(label, layout, theme);
  const number = /^[0-9]$/.test(label);
  const numberRole = number ? numberRowRoleForPreview(
    theme.additionalNumberRow.colorMode,
    label,
    theme,
    layout) : null;
  const textColor = number ? textColorForNumberRole(numberRole, theme) : textColorFor(label, theme, layout);
  const bgOverride = backgroundColorFor(label, theme, layout);
  const background = bgOverride || backgroundForRole(numberRole || role, theme);
  key.className = "key";
  key.style.background = keyFaceBackgroundForPreview(theme, background);
  key.style.borderColor = theme.colors.border;
  key.style.borderWidth = `${theme.shape.borderWidthDp}px`;
  key.style.borderRadius = `${theme.shape.roundnessDp}px`;
  key.style.boxShadow = theme.shape.depthEnabled
    ? `inset 0 -${theme.shape.depthDp}px ${depthColorForPreview(theme, background)}`
    : "none";
  key.style.color = textColor;
  key.style.fontSize = `${14 * theme.typography.primaryTextSizePercent / 100}px`;
  key.style.fontWeight = theme.typography.primaryTextBold ? "700" : "400";
  key.style.fontStyle = theme.typography.primaryTextItalic ? "italic" : "normal";
  const displayOverride = displayOverrideFor(label, theme, layout);
  if (displayOverride?.type === "icon" && displayOverride.value === "dot") {
    if ((label === "." || label === "/") && (theme.icons?.modifierPackId || "") === "dots-lines") {
      const twoDots = document.createElement("span");
      twoDots.className = "main-dot two-dot-legend";
      twoDots.style.color = textColor;
      key.appendChild(twoDots);
    } else {
      const mainDot = document.createElement("span");
      mainDot.className = "main-dot";
      mainDot.style.background = textColor;
      key.appendChild(mainDot);
    }
  } else if (displayOverride?.type === "text") {
    if (displayOverride.value === "hihihi" && isSimpleTextPack(theme.icons?.keyDisplayPackId)) {
      appendHihihiGlyph(key, textColor);
    } else {
      key.textContent = displayOverride.value;
    }
    if (displayOverride.value !== "hihihi" && isSimpleTextPack(theme.icons?.keyDisplayPackId)) {
      key.style.fontWeight = "700";
    }
  } else if (iconForPreview(label) && renderModifierPackGlyph(key, label, theme, layout)) {
    // rendered by helper
  } else {
    key.textContent = label;
  }
  appendSubLegend(key, label, theme, layout);
  appendSlidePreviewHints(key, label, theme, layout);
  if (label === "Shift") {
    const dot = document.createElement("span");
    dot.className = "shift-dot";
    dot.style.background = indicatorColorForPreview(theme);
    key.appendChild(dot);
  }
  attachPreviewGestureHandlers(key, label, theme, layout);
  return key;
}

function attachPreviewGestureHandlers(key, label, theme, layout) {
  let gesture = null;
  const begin = (event, pointerId) => {
    if (gesture) {
      return;
    }
    gesture = {
      pointerId,
      startX: event.clientX,
      startY: event.clientY
    };
    if (typeof pointerId === "number") {
      key.setPointerCapture?.(pointerId);
    }
    updatePreviewGestureState(key, label, theme, layout, "tap");
  };
  const move = (event, pointerId) => {
    if (!gesture || gesture.pointerId !== pointerId) {
      return;
    }
    updatePreviewGestureState(
      key,
      label,
      theme,
      layout,
      previewActionFromPointer(gesture.startX, gesture.startY, event.clientX, event.clientY));
  };
  const release = (event, pointerId) => {
    if (!gesture || gesture.pointerId !== pointerId) {
      return;
    }
    gesture = null;
    schedulePreviewGestureClear(key);
  };
  key.addEventListener("pointerdown", event => begin(event, event.pointerId));
  key.addEventListener("pointermove", event => move(event, event.pointerId));
  key.addEventListener("pointerup", event => release(event, event.pointerId));
  key.addEventListener("pointercancel", event => release(event, event.pointerId));
  key.addEventListener("mousedown", event => begin(event, "mouse"));
  key.addEventListener("mousemove", event => move(event, "mouse"));
  key.addEventListener("mouseup", event => release(event, "mouse"));
  key.addEventListener("mouseleave", event => release(event, "mouse"));
}

function previewActionFromPointer(startX, startY, x, y) {
  const dx = x - startX;
  const dy = y - startY;
  const threshold = 10;
  if (Math.max(Math.abs(dx), Math.abs(dy)) < threshold) {
    return "tap";
  }
  if (Math.abs(dy) >= Math.abs(dx)) {
    return dy < 0 ? "up" : "down";
  }
  return dx < 0 ? "left" : "right";
}

function updatePreviewGestureState(key, label, theme, layout, action) {
  key.classList.toggle("is-preview-pressed", action === "tap");
  key.classList.toggle("is-preview-dragged", action !== "tap");
  ["tap", "up", "down", "left", "right"].forEach(name => {
    key.classList.toggle(`preview-action-${name}`, action === name);
  });
  key.dataset.previewAction = action;
  key.querySelector(".preview-gesture-bubble")?.remove();

  const value = gestureValueForPreview(label, layout, action);
  if (!value) {
    return;
  }
  const bubble = document.createElement("span");
  bubble.className = "preview-gesture-bubble";
  bubble.textContent = action === "tap" ? value : `${gestureArrow(action)} ${value}`;
  bubble.style.background = key.style.background;
  bubble.style.borderColor = theme.colors.border;
  bubble.style.color = key.style.color;
  key.appendChild(bubble);
}

function schedulePreviewGestureClear(key) {
  const token = String(Date.now());
  key.dataset.previewToken = token;
  setTimeout(() => {
    if (key.dataset.previewToken !== token) {
      return;
    }
    key.classList.remove("is-preview-pressed", "is-preview-dragged");
    ["tap", "up", "down", "left", "right"].forEach(name => {
      key.classList.remove(`preview-action-${name}`);
    });
    delete key.dataset.previewAction;
    key.querySelector(".preview-gesture-bubble")?.remove();
  }, 420);
}

function appendSubLegend(key, label, theme, layout = "qwerty") {
  const sub = subLegendFor(label, layout);
  if (!sub || shouldHideSubLegend(label, theme, layout)) {
    return;
  }
  if (!theme.metadata?.previewSlideHints) {
    return;
  }
  const item = document.createElement("span");
  item.className = "sublegend";
  item.textContent = sub;
  item.style.color = hintColorFor(label, theme, layout);
  item.style.fontSize = `${10 * theme.typography.secondaryTextSizePercent / 100}px`;
  item.style.fontWeight = theme.typography.secondaryTextBold ? "700" : "600";
  item.style.fontStyle = theme.typography.secondaryTextItalic ? "italic" : "normal";
  key.appendChild(item);
}

function appendSlidePreviewHints(key, label, theme, layout = "qwerty") {
  if (shouldHideSubLegend(label, theme, layout)) {
    return;
  }
  const map = gestureMapForPreview(label, layout);
  const entries = [
    ["up", map.up],
    ["down", map.down],
    ["left", map.left],
    ["right", map.right]
  ].filter(([, value]) => value);
  if (entries.length === 0) {
    return;
  }
  const hintColor = hintColorFor(label, theme, layout);
  entries.forEach(([direction, value]) => {
    const item = document.createElement("span");
    item.className = `slide-preview-hint slide-preview-${direction}`;
    item.textContent = value;
    item.style.color = hintColor;
    item.style.fontSize = `${9.5 * theme.typography.secondaryTextSizePercent / 100}px`;
    item.style.fontWeight = theme.typography.secondaryTextBold ? "700" : "600";
    item.style.fontStyle = theme.typography.secondaryTextItalic ? "italic" : "normal";
    key.appendChild(item);
  });
}

function hintColorFor(label, theme, layout = "qwerty") {
  const role = roleForPreview(label, layout, theme);
  const number = /^[0-9]$/.test(label);
  const numberRole = number ? numberRowRoleForPreview(
    theme.additionalNumberRow.colorMode,
    label,
    theme,
    layout) : null;
  const background = normalizeColor(backgroundColorFor(label, theme, layout))
    || normalizeColor(backgroundForRole(numberRole || role, theme));
  const foreground = normalizeColor(number ? textColorForNumberRole(numberRole, theme) : textColorFor(label, theme, layout));
  return softenedForegroundFor(foreground, background, 0.62, 1.45);
}

function subLegendFor(label, layout = "qwerty") {
  const qwertyMap = {
    q: "1", w: "2", e: "3", r: "4", t: "5", y: "6", u: "7", i: "8", o: "9", p: "0",
    a: "@", s: "#%", d: "/", f: "*", g: "~^", h: "-_", j: "+=", k: "<>", l: "\u2665",
    z: "()", x: "[]", c: ":", v: "\"", b: "&|", n: "!", m: "?"
  };
  const dingulMap = {
    "\u3131": "\u314b \u3132 #",
    "\u3134": "\u314c \u3138 \u3137",
    "\u3162": "\u315d \u315a \u3158 \u315f",
    "\u3139": "= - ^ ~",
    "\u3141": "\u314d \u3143 \u3142",
    "\u3163.": "\u3153 \u314f \u3157 \u315c",
    "\u3145": "1 \u3146 2",
    "\u3147": "4 \u2665 6 5",
    "\u3161\u3150": "\u3154 \u3150 \u3159 \u315e",
    "\u3148": "\u314a \u3149 ~",
    "\u314e": "7 9 0 8",
    "..": "\u3155 \u315b \u3151 \u3160",
    "?": "+ ! *",
    ".": ", \" `",
    "/": "@ : ;"
  };
  return (layout === "dingul" ? dingulMap : qwertyMap)[label] || "";
}

function gestureValueForPreview(label, layout = "qwerty", action = "tap") {
  const map = gestureMapForPreview(label, layout);
  return map[action] || "";
}

function gestureMapForPreview(label, layout = "qwerty") {
  const sharedCommands = {
    Settings: { tap: "settings" },
    Options: { tap: "settings" },
    Reserved: { tap: "reserved" },
    Space: { tap: "space", left: "\u2190", right: "\u2192" },
    Lang: { tap: "\ud55c/\uc601" },
    Enter: { tap: "\u21b5" },
    Shift: { tap: "shift" },
    Bksp: { tap: "bksp" }
  };
  if (sharedCommands[label]) {
    return sharedCommands[label];
  }
  const numberDown = {
    "1": "!", "2": "@", "3": "#", "4": "$", "5": "%",
    "6": "^", "7": "&", "8": "*", "9": "(", "0": ")"
  };
  if (/^[0-9]$/.test(label)) {
    return { tap: label, down: numberDown[label] };
  }
  if (layout === "dingul") {
    return dingulGestureMap()[label] || { tap: label };
  }
  return qwertyGestureMap()[label] || { tap: label };
}

function qwertyGestureMap() {
  const map = {};
  "qwertyuiopasdfghjklzxcvbnm".split("").forEach(letter => {
    map[letter] = { tap: letter, up: letter.toUpperCase() };
  });
  Object.assign(map, {
    q: { tap: "q", up: "Q", down: "1" },
    w: { tap: "w", up: "W", down: "2" },
    e: { tap: "e", up: "E", down: "3" },
    r: { tap: "r", up: "R", down: "4" },
    t: { tap: "t", up: "T", down: "5" },
    y: { tap: "y", up: "Y", down: "6" },
    u: { tap: "u", up: "U", down: "7" },
    i: { tap: "i", up: "I", down: "8" },
    o: { tap: "o", up: "O", down: "9" },
    p: { tap: "p", up: "P", down: "0" },
    a: { tap: "a", up: "A", down: "@" },
    s: { tap: "s", up: "S", left: "#", right: "%" },
    d: { tap: "d", up: "D", down: "/" },
    f: { tap: "f", up: "F", down: "*" },
    g: { tap: "g", up: "G", left: "~", right: "^" },
    h: { tap: "h", up: "H", left: "_", right: "-" },
    j: { tap: "j", up: "J", left: "+", right: "=" },
    k: { tap: "k", up: "K", left: "<", right: ">" },
    l: { tap: "l", up: "L", down: "\u2665" },
    z: { tap: "z", up: "Z", left: "(", right: ")" },
    x: { tap: "x", up: "X", left: "[", right: "]" },
    c: { tap: "c", up: "C", left: ";", right: ":" },
    v: { tap: "v", up: "V", left: "'", right: "\"" },
    b: { tap: "b", up: "B", left: "&", right: "|" },
    n: { tap: "n", up: "N", down: "!" },
    m: { tap: "m", up: "M", down: "?" }
  });
  return map;
}

function dingulGestureMap() {
  return {
    "\u3131": { tap: "\u3131", up: "\u314b", left: "\u314b", right: "\u3132", down: "#" },
    "\u3134": { tap: "\u3134", up: "\u314c", left: "\u314c", right: "\u3138", down: "\u3137" },
    "\u3162": { tap: "\u3162", up: "\u315a", left: "\u315d", right: "\u3158", down: "\u315f" },
    "\u3139": { tap: "\u3139", up: "^", left: "=", right: "-", down: "~" },
    "\u3141": { tap: "\u3141", up: "\u314d", left: "\u314d", right: "\u3143", down: "\u3142" },
    "\u3163.": { tap: "\u3163", up: "\u3157", left: "\u3153", right: "\u314f", down: "\u315c" },
    "\u3145": { tap: "\u3145", up: "\u3146", left: "1", right: "3", down: "2" },
    "\u3147": { tap: "\u3147", up: "\u2665", left: "4", right: "6", down: "5" },
    "\u3161\u3150": { tap: "\u3161", up: "\u3159", left: "\u3154", right: "\u3150", down: "\u315e" },
    "\u3148": { tap: "\u3148", up: "\u314a", left: "\u314a", right: "\u3149", down: "~" },
    "\u314e": { tap: "\u314e", up: "0", left: "7", right: "9", down: "8" },
    "..": { tap: "space", up: "\u315b", left: "\u3155", right: "\u3151", down: "\u3160" },
    "?": { tap: "?", up: "!", left: "+", down: "*" },
    ".": { tap: ".", up: "\"", left: ",", down: "`" },
    "/": { tap: "/", up: ":", left: "@", down: ";" }
  };
}

function gestureArrow(action) {
  switch (action) {
    case "up":
      return "\u2191";
    case "down":
      return "\u2193";
    case "left":
      return "\u2190";
    case "right":
      return "\u2192";
    default:
      return "";
  }
}

function roleForPreview(label, layout = "qwerty", theme = null) {
  if (label === "Space") {
    return spacebarRoleForPreview(theme);
  }
  if (layout === "dingul" && label === "?") {
    return questionRoleForPreview(theme);
  }
  if (label === "Enter") {
    return accentPolicyIncludesAny(theme, layout, ["enter", "settingsEnter", "modCtrl"]) ? "accent" : "modifier";
  }
  if (["Shift", "Bksp", "Lang", "Options", "Reserved", "Settings", "?", ".", "/"].includes(label)) {
    const targets = semanticTargetsForPreview(label, layout);
    if (accentPolicyIncludesAny(theme, layout, targets)
        || ([".", "/"].includes(label) && accentPolicyIncludes(theme, layout, "punctuation"))) {
      return "accent";
    }
    return "modifier";
  }
  if (["?123"].includes(label)) {
    return "modifier";
  }
  return "alpha";
}

function spacebarRoleForPreview(theme) {
  const raw = theme?.accentPolicy?.spacebar || theme?.accentPolicy?.space || "";
  if (raw === "accent") {
    return "accent";
  }
  if (raw === "mod" || raw === "modifier" || raw === "modifiers") {
    return "modifier";
  }
  return "alpha";
}

function questionRoleForPreview(theme) {
  const raw = theme?.accentPolicy?.question || theme?.accentPolicy?.questionMark || "";
  if (raw === "accent") {
    return "accent";
  }
  if (raw === "mod" || raw === "modifier" || raw === "modifiers") {
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
      return dingul.modInv.background || theme.colors.accentKey;
    case "alpha":
    default:
      return dingul.alpha.background;
  }
}

function semanticTargetsForPreview(label, layout) {
  const normalized = label.toLowerCase();
  if (normalized === "1") {
    return ["escPoint"];
  }
  if (["options", "settings", "enter"].includes(normalized)) {
    return normalized === "enter" ? ["enter", "settingsEnter", "modCtrl"] : ["settingsEnter", "modCtrl"];
  }
  if (["reserved", "lang", "language"].includes(normalized)) {
    return ["modMeta"];
  }
  if (normalized === "shift") {
    return ["qwertyShift", "shift", "modCommand"];
  }
  if (["bksp", "backspace"].includes(normalized)) {
    return ["backspace", "modCommand"];
  }
  if (layout === "dingul" && label === ".") {
    return ["dingulDot", "modEnter"];
  }
  if (layout === "dingul" && label === "/") {
    return ["dingulSlash", "modShift"];
  }
  if (layout === "dingul" && label === "?") {
    return ["question"];
  }
  return [];
}

function accentPolicyIncludesAny(theme, layout, targets) {
  return Array.isArray(targets) && targets.some(target => accentPolicyIncludes(theme, layout, target));
}

function accentPolicyIncludes(theme, layout, target) {
  if (!theme || !target) {
    return false;
  }
  const explicitTargets = Array.isArray(theme.accentPolicy?.[layout]) ? theme.accentPolicy[layout] : [];
  if (explicitTargets.includes(target)) {
    return true;
  }
  if (explicitTargets.length > 0 || !usesImplicitAccentPolicy(theme)) {
    return false;
  }
  const implicitTargets = layout === "dingul" ? ["modEnter", "modShift"] : ["modCtrl", "modMeta"];
  return implicitTargets.includes(target);
}

function cloneAccentPolicy(policy) {
  return {
    qwerty: Array.isArray(policy?.qwerty) ? [...policy.qwerty] : [],
    dingul: Array.isArray(policy?.dingul) ? [...policy.dingul] : [],
    spacebar: policy?.spacebar || policy?.space || "default",
    question: policy?.question || policy?.questionMark || "default"
  };
}

function usesImplicitAccentPolicy(theme) {
  const colors = theme?.colors || {};
  const accent = normalizeColor(colors.accentKey);
  if (!accent) {
    return false;
  }
  return [colors.alphaKey, colors.modifierKey]
    .map(normalizeColor)
    .filter(Boolean)
    .every(color => colorDistance(accent, color) >= 48);
}

function colorDistance(left, right) {
  const a = parseHexColor(left);
  const b = parseHexColor(right);
  if (!a || !b) {
    return 0;
  }
  return Math.hypot(a[0] - b[0], a[1] - b[1], a[2] - b[2]);
}

function contrastRatio(left, right) {
  const a = relativeLuminance(left);
  const b = relativeLuminance(right);
  if (!Number.isFinite(a) || !Number.isFinite(b)) {
    return 1;
  }
  const lighter = Math.max(a, b);
  const darker = Math.min(a, b);
  return (lighter + 0.05) / (darker + 0.05);
}

function softenedForegroundFor(foreground, background, foregroundAmount = 0.62, minimumContrast = 1.45) {
  const fg = normalizeColor(foreground);
  const bg = normalizeColor(background);
  if (!fg || !bg) {
    return fg || foreground || "#232323";
  }
  let amount = Math.max(0, Math.min(1, foregroundAmount));
  let color = blendColors(fg, bg, amount);
  while (amount < 1 && contrastRatio(color, bg) < minimumContrast) {
    amount = Math.min(1, amount + 0.08);
    color = blendColors(fg, bg, amount);
  }
  return contrastRatio(color, bg) >= minimumContrast ? color : fg;
}

function indicatorColorForPreview(theme) {
  const dingul = normalizedDingulColors(theme);
  const override = normalizeColor(theme.keyTextColorOverrides?.shiftIndicator)
    || normalizeColor(theme.keyTextColorOverrides?.shift_indicator);
  const alphaText = normalizeColor(dingul.alpha.foreground) || normalizeColor(theme.colors.accent) || "#232323";
  const modifierText = normalizeColor(dingul.mod.foreground) || normalizeColor(theme.colors.secondary) || alphaText;
  const modifierBackground = normalizeColor(dingul.mod.background)
    || normalizeColor(theme.colors.modifierKey)
    || "#E7EAF0";
  const base = override ? blendColors(override, alphaText, 0.34) : alphaText;
  return ensureContrast(base, modifierBackground, 2.1, [
    alphaText,
    modifierText,
    override,
    "#111827",
    "#FFFFFF"
  ]);
}

function depthColorForPreview(theme, background) {
  const customDepth = normalizeColor(theme.colors.depth);
  if (customDepth) {
    return customDepth;
  }
  return dimmedDepthColorForBackground(background);
}

function keyFaceBackgroundForPreview(theme, background) {
  const bg = normalizeColor(background) || "#F8F8F8";
  const effects = normalizedVisualEffects(theme.effects);
  const strength = effects.keyFaceGradient.strengthPercent;
  if (!theme.shape?.depthEnabled || !theme.shape?.depthDp
      || !effects.keyFaceGradient.enabled || strength <= 0) {
    return bg;
  }
  const [top, middle, bottom] = keyFaceGradientColors(bg, strength);
  return `linear-gradient(180deg, ${top} 0%, ${middle} 42%, ${bottom} 100%)`;
}

function keyFaceGradientColors(background, strengthPercent) {
  const bg = normalizeColor(background) || "#F8F8F8";
  const rgb = parseHexColor(bg);
  if (!rgb) {
    return [bg, bg, bg];
  }
  const luminance = (rgb[0] * 299 + rgb[1] * 587 + rgb[2] * 114) / 1000;
  const strength = Math.max(0, Math.min(100, Number(strengthPercent) || 0)) / 100;
  const topAmount = (luminance < 42 ? 0.08 : 0.06) + 0.24 * strength;
  const bottomAmount = (luminance < 42 ? 0.04 : 0.05) + 0.18 * strength;
  return [
    blendColors("#FFFFFF", bg, topAmount),
    bg,
    blendColors("#000000", bg, bottomAmount)
  ];
}

function dimmedDepthColorForBackground(background) {
  const bg = normalizeColor(background) || "#F8F8F8";
  const rgb = parseHexColor(bg);
  if (!rgb) {
    return bg;
  }
  const luminance = (rgb[0] * 299 + rgb[1] * 587 + rgb[2] * 114) / 1000;
  return luminance < 42
    ? blendColors("#FFFFFF", bg, 0.10)
    : blendColors("#000000", bg, 0.16);
}

function normalizedVisualEffects(effects) {
  const raw = effects || {};
  const keyFaceGradient = raw.keyFaceGradient || raw.keyGradient || {};
  const strength = Number(
    keyFaceGradient.strengthPercent
      ?? raw.keyFaceGradientStrengthPercent
      ?? defaultVisualEffects().keyFaceGradient.strengthPercent);
  return {
    ...raw,
    keyFaceGradient: {
      enabled: keyFaceGradient.enabled
        ?? raw.keyFaceGradientEnabled
        ?? defaultVisualEffects().keyFaceGradient.enabled,
      strengthPercent: Math.max(0, Math.min(100, Number.isFinite(strength)
        ? Math.round(strength)
        : defaultVisualEffects().keyFaceGradient.strengthPercent))
    }
  };
}

function ensureContrast(preferred, background, minimumContrast, fallbacks = []) {
  const bg = normalizeColor(background);
  const candidate = normalizeColor(preferred);
  if (!candidate || !bg) {
    return candidate || preferred;
  }
  if (contrastRatio(candidate, bg) >= minimumContrast) {
    return candidate;
  }
  let best = candidate;
  let bestContrast = contrastRatio(candidate, bg);
  fallbacks
    .map(normalizeColor)
    .filter(Boolean)
    .forEach(color => {
      const contrast = contrastRatio(color, bg);
      if (contrast > bestContrast) {
        best = color;
        bestContrast = contrast;
      }
    });
  return best;
}

function blendColors(foreground, background, foregroundAmount) {
  const fg = parseHexColor(foreground);
  const bg = parseHexColor(background);
  if (!fg || !bg) {
    return normalizeColor(foreground) || normalizeColor(background) || "#232323";
  }
  const amount = Math.max(0, Math.min(1, foregroundAmount));
  const inverse = 1 - amount;
  return `#${fg.map((channel, index) => {
    const value = Math.round(channel * amount + bg[index] * inverse);
    return value.toString(16).padStart(2, "0");
  }).join("").toUpperCase()}`;
}

function relativeLuminance(value) {
  const rgb = parseHexColor(value);
  if (!rgb) {
    return NaN;
  }
  const [r, g, b] = rgb.map(channel => {
    const normalized = channel / 255;
    return normalized <= 0.03928
      ? normalized / 12.92
      : ((normalized + 0.055) / 1.055) ** 2.4;
  });
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

function parseHexColor(value) {
  const normalized = normalizeColor(value);
  if (!normalized) {
    return null;
  }
  return [
    parseInt(normalized.slice(1, 3), 16),
    parseInt(normalized.slice(3, 5), 16),
    parseInt(normalized.slice(5, 7), 16)
  ];
}

function shouldHideSubLegend(label, theme, layout = "qwerty") {
  if (displayOverrideFor(label, theme, layout)) {
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

function numberRowRoleForPreview(mode, label, theme, layout = "qwerty") {
  if (label === "1" && accentPolicyIncludes(theme, layout, "escPoint")) {
    return "accent";
  }
  return numberRowRole(mode, label);
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

function overrideKeyForLabel(label, layout = "qwerty") {
  if (layout === "dingul") {
    switch (label) {
      case "\u3162":
        return "tap:\u3162";
      case "\u3163.":
        return "__dingul_center_vowel__";
      case "\u3161\u3150":
        return "__dingul_wide_vowel__";
      case "..":
      case ". .":
        return "..";
      default:
        break;
    }
  }
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

function displayOverrideFor(label, theme, layout = "qwerty") {
  const overrides = theme.keyDisplayOverrides || {};
  const keys = overrides.keys || {};
  const key = overrideKeyForLabel(label, layout);
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
      case ".":
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
      case "Options":
        return { type: "text", value: "stash" };
      case "Settings":
        return { type: "text", value: "config" };
      case "Reserved":
        return { type: "text", value: "commit" };
      case ".":
        return { type: "text", value: "diff" };
      case "/":
        return { type: "text", value: "log" };
      default:
        return null;
    }
  }
  return null;
}

function renderModifierPackGlyph(key, label, theme, layout = "qwerty") {
  const pack = theme.icons?.modifierPackId || "line-mono";
  if (pack === "dots-lines") {
    const line = document.createElement("span");
    if (label === "Space") {
      line.className = "mod-pack-line five-dot-line";
    } else if (label === "Lang" || label === "Reserved") {
      line.className = "mod-pack-line single-dot-line";
    } else if (label === "Options" || label === "Settings" || label === "Enter") {
      line.className = "mod-pack-line dotted-line short-mod-line";
    } else {
      line.className = "mod-pack-line dotted-line";
    }
    line.style.background = textColorFor(label, theme, layout);
    line.style.color = textColorFor(label, theme, layout);
    key.appendChild(line);
    return true;
  }
  if (pack === "metropolis-graph" || pack === "metropolis-points") {
    appendLineIcon(key, label, metropolisIconColorFor(label, theme, layout));
    return true;
  }
  appendLineIcon(key, label, modifierIconColorFor(pack, label, theme, layout));
  return true;
}

function modifierIconColorFor(pack, label, theme, layout = "qwerty") {
  if (pack === "accent-color") {
    return "#06B6D4";
  }
  return textColorFor(label, theme, layout);
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

function metropolisIconColorFor(label, theme, layout = "qwerty") {
  const overrides = theme.keyTextColorOverrides || {};
  const key = overrideKeyForLabel(label, layout);
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

function textColorFor(label, theme, layout = "qwerty") {
  const key = overrideKeyForLabel(label, layout);
  const role = roleForPreview(label, layout, theme);
  const dingul = normalizedDingulColors(theme);
  const targetColor = colorForSemanticTarget(label, theme.keyTextColorOverrides || {}, theme, layout);
  if (targetColor) {
    return targetColor;
  }
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

function backgroundColorFor(label, theme, layout = "qwerty") {
  const key = overrideKeyForLabel(label, layout);
  const overrides = theme.keyBackgroundColorOverrides || {};
  const role = roleForPreview(label, layout, theme);
  return colorForSemanticTarget(label, overrides, theme, layout)
    || overrides[key]
    || overrides[label.toLowerCase()]
    || overrides[label]
    || (role === "alpha" ? overrides.alpha : null)
    || (role === "modInv" ? (overrides.modInv || overrides.mod_inv) : null)
    || (role === "modifier" ? (overrides.mod || overrides.modifiers) : null)
    || null;
}

function colorForSemanticTarget(label, overrides, theme, layout = "qwerty") {
  const targets = semanticTargetsForPreview(label, layout, theme);
  for (const target of targets) {
    if (overrides[target]) {
      return overrides[target];
    }
  }
  return null;
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
    effects: normalizedVisualEffects(parsed.effects || base.effects || {}),
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
    dingul: normalizeAccentTargets(policy.dingul, allowed),
    spacebar: normalizeAccentKeyRole(policy.spacebar || policy.space, "accentPolicySpacebarRoles"),
    question: normalizeAccentKeyRole(policy.question || policy.questionMark, "accentPolicyQuestionRoles")
  };
}

function normalizeAccentTargets(rawTargets, allowed) {
  if (!Array.isArray(rawTargets)) {
    return [];
  }
  return rawTargets.filter(target => allowed.has(target));
}

function normalizeAccentKeyRole(rawRole, contractKey) {
  const allowed = new Set(themeContract[contractKey] || ["default", "alpha", "mod", "modifier", "accent"]);
  return allowed.has(rawRole) ? rawRole : "default";
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

async function copyAiPrompt() {
  await navigator.clipboard.writeText(ids.aiPrompt.value);
  ids.status.textContent = "Copied image-to-theme prompt to clipboard.";
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

function buildImageThemePrompt(theme) {
  const colorKeys = colorFields.map(([key]) => key).join(", ");
  const shapeKeys = shapeFields.map(([key]) => key).join(", ");
  const fontIds = fontFamilies.map(option => option.id).join(", ");
  const numberModes = numberRowModes.map(option => option.id).join(", ");
  const accentTargets = (themeContract.accentPolicyTargets || []).join(", ");
  const accentMap = accentPolicyReference
    .map(item => `- ${item.id}: QWERTY = ${item.qwerty}; Dingul = ${item.dingul}`)
    .join("\n");
  const perKeyMap = perKeyOverrideReference
    .map(item => `- ${item.scope}: ${item.keys}`)
    .join("\n");
  const baseName = JSON.stringify(theme.name || "Image Inspired Theme");
  const baseAuthor = JSON.stringify(theme.author || "local");
  const sample = {
    schemaVersion: 1,
    name: "Image Inspired Theme",
    author: "local",
    colors: {
      alphaKey: "#F8F8F8",
      modifierKey: "#E5E7EB",
      accentKey: "#E5E7EB",
      keyPressed: "#CBD5E1",
      keyboardBackground: "#D1D5DB",
      panelBackground: "#D1D5DB",
      border: "#9CA3AF",
      depth: null,
      accent: "#111827",
      secondary: "#6B7280"
    },
    shape: {
      roundnessDp: 5,
      borderWidthDp: 1,
      keyGapDp: 5,
      depthEnabled: true,
      depthDp: 2
    },
    additionalNumberRow: { colorMode: "half_mod_4567" },
    accentPolicy: {
      qwerty: [],
      dingul: [],
      spacebar: "default",
      question: "default"
    },
    dingulColors: {
      alpha: { foreground: "#111827", background: "#F8F8F8" },
      mod: { foreground: "#6B7280", background: "#E5E7EB" },
      modInv: { foreground: "#6B7280", background: "#E5E7EB" }
    },
    typography: {
      fontFamily: "noto_sans_kr",
      primaryTextSizePercent: 78,
      secondaryTextSizePercent: 80,
      primaryTextBold: false,
      primaryTextItalic: false,
      secondaryTextBold: true,
      secondaryTextItalic: false
    },
    effects: defaultVisualEffects(),
    keyTextColorOverrides: {},
    keyBackgroundColorOverrides: {}
  };
  sample.name = JSON.parse(baseName);
  sample.author = JSON.parse(baseAuthor);

  return [
    "Use the attached keyboard image as visual reference and create a New Dingul Keyboard theme JSON.",
    "Return JSON only. Do not wrap it in markdown, do not add comments, and do not add explanation.",
    "",
    "Hard rules:",
    "- schemaVersion must be 1.",
    "- Do not include icons, iconPacks, modifierPacks, keyDisplayPacks, keyDisplayOverrides, legendStyle, layers, layout, hints, or user preference fields.",
    "- Do not invent raster/vector assets. Icon pack selection is handled separately.",
    "- Use only #RRGGBB colors.",
    "- Preserve readability: alpha and mod foreground/background pairs should be readable.",
    "- Prefer alpha/mod/accent role colors over many per-key overrides. Use per-key overrides only for obvious special keycap or legend colors in the image.",
    "- Per-key foreground/background theming is supported with keyTextColorOverrides and keyBackgroundColorOverrides. Per-key shape, icon pack, layout, and hint settings are not part of this theme JSON.",
    "- Exact per-key overrides take precedence over alpha/mod/accent role colors. Keep them sparse.",
    "- Before choosing accent placement, classify the source keycap colorway as 2-tone or 3-tone.",
    "- 2-tone = alpha + mod only. Do not force accentKey, modInv, or accentPolicy just to create a third tone.",
    "- 3-tone = alpha + mod + a clearly distinct accent keycap color visible in the reference image. Use accentKey/modInv/accentPolicy only for those visible accent targets.",
    "- Spacebar is alpha unless the image clearly treats it as mod or accent.",
    "- Dingul punctuation can use accentPolicy rather than exact per-key colors when possible.",
    "- For the optional number row/numpad, default to additionalNumberRow.colorMode = half_mod_4567: digits 1 2 3 8 9 0 use alpha and digits 4 5 6 7 use mod.",
    "- If the reference image treats the number row differently, choose another additionalNumberRow.colorMode instead of adding per-key overrides.",
    "",
    `Allowed color keys: ${colorKeys}.`,
    `Allowed shape keys: ${shapeKeys}.`,
    "Allowed effects: effects.keyFaceGradient.enabled boolean and effects.keyFaceGradient.strengthPercent integer 0..100. Use this only for subtle key-surface depth; omit or keep the default unless the image clearly has glossy key faces.",
    `Allowed fontFamily values: ${fontIds}.`,
    `Allowed additionalNumberRow.colorMode values: ${numberModes}.`,
    `Allowed accentPolicy targets: ${accentTargets}.`,
    "",
    "Number row / numpad color mode map:",
    numberRowModePromptGuide(),
    "",
    "Role-first decision guide:",
    "1. First solve the theme with global roles: alphaKey/modifierKey/accentKey for backgrounds and accent/secondary for legends.",
    "2. Decide tone count from the image. For 2-tone themes, keep accentPolicy.qwerty/dingul as [] and let dingulColors.modInv mirror mod rather than inventing an inverted accent.",
    "3. For 3-tone themes, set accentKey and dingulColors.modInv to the visible point-keycap color pair, then choose accentPolicy targets for the keys that actually use it.",
    "4. Mirror alpha/mod roles into dingulColors.alpha and dingulColors.mod so QWERTY and Dingul stay visually consistent.",
    "5. If a whole visual group is accented, use accentPolicy.qwerty or accentPolicy.dingul targets instead of per-key overrides.",
    "6. Use keyTextColorOverrides and keyBackgroundColorOverrides only for isolated keys that cannot be expressed by alpha/modifier/accent roles or accentPolicy.",
    "7. If several keys share the same special style, prefer a role or accentPolicy target. Do not list many per-key overrides for a pattern.",
    "",
    "Accent policy target map. Use these names exactly when choosing accentPolicy.qwerty or accentPolicy.dingul:",
    accentMap,
    "",
    "Per-key override key map. Use these names exactly in keyTextColorOverrides or keyBackgroundColorOverrides:",
    perKeyMap,
    "",
    "Per-key override example. Use this only for exact key exceptions, not for the whole keyboard:",
    JSON.stringify({
      keyTextColorOverrides: {
        "tap:q": "#E11D48",
        "enter": "#111827"
      },
      keyBackgroundColorOverrides: {
        "tap:q": "#FFF7ED",
        "enter": "#A7F3D0"
      }
    }, null, 2),
    "",
    "Map the image to these roles:",
    "- alphaKey: main typing keycap background.",
    "- modifierKey: command/modifier keycap background.",
    "- accentKey: point/accent keycap background, only if the image has a clear third keycap color. In a 2-tone theme it may match modifierKey and should not be actively placed.",
    "- accent: main legend/icon color.",
    "- secondary: sub legend and modifier legend color.",
    "- dingulColors.alpha/mod define the 2-tone base. dingulColors.modInv should mirror mod unless the image is truly 3-tone.",
    "",
    "Use this exact JSON shape and replace values based on the image:",
    JSON.stringify(sample, null, 2)
  ].join("\n");
}

function cloneTheme(theme) {
  return JSON.parse(JSON.stringify(theme));
}
