const colorFields = [
  ["keyIdle", "\uC804\uCCB4 - \uAE30\uBCF8 \uD0A4", "\uC77C\uBC18 \uC785\uB825 \uD0A4\uC640 \uC2A4\uD398\uC774\uC2A4\uBC14 \uBC30\uACBD\uC0C9\uC785\uB2C8\uB2E4."],
  ["functionKey", "\uC804\uCCB4 - \uAE30\uB2A5 \uD0A4", "\uC635\uC158, \uC608\uC57D\uC5B4, \uD55C/\uC601 \uD0A4 \uBC30\uACBD\uC0C9\uC785\uB2C8\uB2E4."],
  ["primaryFunctionKey", "\uC804\uCCB4 - \uC8FC\uC694 \uAE30\uB2A5 \uD0A4", "\uC2DC\uD504\uD2B8, \uC0AD\uC81C, \uC5D4\uD130 \uBC30\uACBD\uC0C9\uC785\uB2C8\uB2E4."],
  ["accentKey", "\uC804\uCCB4 - \uAC15\uC870 \uD0A4", "\uB529\uAD74 \uAC15\uC870 \uD2B9\uC218 \uD0A4\uB97C \uD3EC\uD568\uD55C \uAC15\uC870 \uADF8\uB8F9 \uBC30\uACBD\uC0C9\uC785\uB2C8\uB2E4."],
  ["keyPressed", "\uB20C\uB9BC", "\uD0A4\uB97C \uB204\uB974\uB294 \uB3D9\uC548 \uD45C\uC2DC\uB418\uB294 \uBC30\uACBD\uC0C9\uC785\uB2C8\uB2E4."],
  ["keyboardBackground", "\uD0A4\uBCF4\uB4DC \uBC30\uACBD", "\uD0A4 \uC0AC\uC774\uC640 \uD0A4 \uB4A4\uCABD \uC601\uC5ED\uC758 \uC0C9\uC0C1\uC785\uB2C8\uB2E4."],
  ["panelBackground", "Panel background", "Actual keyboard panel color. This overrides keyboardBackground when exported."],
  ["border", "\uD14C\uB450\uB9AC", "\uD0A4 \uC678\uACFD\uC120 \uC0C9\uC0C1\uC785\uB2C8\uB2E4. \uC785\uCCB4 \uD6A8\uACFC \uC0C9\uC0C1\uC744 \uB530\uB85C \uC9C0\uC815\uD558\uC9C0 \uC54A\uC73C\uBA74 \uC774 \uC0C9\uC0C1\uC744 \uC4F0\uB2C8\uB2E4."],
  ["depth", "\uC785\uCCB4 \uD6A8\uACFC \uC0C9\uC0C1", "\uD0A4 \uC544\uB798\uCABD \uC785\uCCB4 \uD6A8\uACFC \uC0C9\uC0C1\uC785\uB2C8\uB2E4."],
  ["accent", "\uC8FC \uAE00\uC790", "\uC911\uC559 \uAE00\uC790, \uC544\uC774\uCF58, \uBBF8\uB9AC\uBCF4\uAE30 \uD14D\uC2A4\uD2B8 \uC0C9\uC0C1\uC785\uB2C8\uB2E4."],
  ["secondary", "\uBCF4\uC870 \uAE00\uC790", "\uC2AC\uB77C\uC774\uB4DC \uD78C\uD2B8\uC640 \uBCF4\uC870 \uD14D\uC2A4\uD2B8 \uC0C9\uC0C1\uC785\uB2C8\uB2E4."]
];

const shapeFields = [
  ["roundnessDp", "\uB465\uAE00\uAE30", 0, 24],
  ["borderWidthDp", "\uD14C\uB450\uB9AC \uAD75\uAE30", 0, 8],
  ["keyGapDp", "\uD0A4 \uC0AC\uC774 \uC2DC\uAC01 \uAC04\uACA9", 0, 18],
  ["depthDp", "\uC785\uCCB4 \uB192\uC774", 0, 8]
];
const presets = {
  "ios-clean-light": {
    name: "iOS Clean Light",
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
    typography: defaultTypography(false, false),
    additionalNumberRow: { colorMode: "full_dimmed" },
    keyTextColorOverrides: { shiftIndicator: "#2563EB" },
    keyBackgroundColorOverrides: {}
  },
  "marigold-fiesta-light": {
    name: "Marigold Fiesta Light",
    colors: {
      keyIdle: "#FAFAF7",
      functionKey: "#F1F0EC",
      primaryFunctionKey: "#E8E7E2",
      accentKey: "#E8E7E2",
      keyPressed: "#DAD8D1",
      keyboardBackground: "#F3F2EF",
      border: "#B9B7B0",
      depth: "#CCC9C2",
      accent: "#25201C",
      secondary: "#64605A"
    },
    shape: { roundnessDp: 4, borderWidthDp: 1, keyGapDp: 5, depthEnabled: true, depthDp: 2 },
    typography: defaultTypography(true, true),
    additionalNumberRow: { colorMode: "center_dimmed" },
    keyTextColorOverrides: {
      "tap:q": "#7C3CB3",
      "tap:w": "#008B82",
      "tap:e": "#B85F19",
      "tap:r": "#C02666",
      "tap:t": "#4B8B2C",
      "tap:y": "#F08A00",
      "tap:u": "#8B5FBF",
      "tap:i": "#D94686",
      "tap:o": "#007C89",
      "tap:p": "#F06423",
      "shiftIndicator": "#008B82",
      "__dingul_center_vowel__": "#C98900",
      "__dingul_wide_vowel__": "#007C89",
      "space": "#2B2D31",
      "enter": "#A95B00"
    },
    keyBackgroundColorOverrides: {}
  },
  "marigold-fiesta-dark": {
    name: "Marigold Fiesta Dark",
    colors: {
      keyIdle: "#202225",
      functionKey: "#2A2C31",
      primaryFunctionKey: "#111318",
      accentKey: "#111318",
      keyPressed: "#3C4048",
      keyboardBackground: "#111214",
      border: "#45484F",
      depth: "#2F3339",
      accent: "#F8F1DF",
      secondary: "#B8A9BF"
    },
    shape: { roundnessDp: 4, borderWidthDp: 1, keyGapDp: 5, depthEnabled: true, depthDp: 2 },
    typography: defaultTypography(true, true),
    additionalNumberRow: { colorMode: "center_dimmed" },
    keyTextColorOverrides: {
      "tap:q": "#C75DFF",
      "tap:w": "#4DE4D2",
      "tap:e": "#FF9B48",
      "tap:r": "#FF5DAE",
      "tap:t": "#9BE564",
      "tap:y": "#FFD25A",
      "tap:u": "#A78BFA",
      "tap:i": "#F472B6",
      "tap:o": "#36E7F4",
      "tap:p": "#FF9F32",
      "shiftIndicator": "#36E7F4",
      "__dingul_center_vowel__": "#FFD25A",
      "__dingul_wide_vowel__": "#36E7F4",
      "space": "#F7EEDB",
      "enter": "#FF9F32"
    },
    keyBackgroundColorOverrides: {}
  },
  "gmk-dots-light": {
    name: "GMK Dots Light Inspired",
    colors: {
      keyIdle: "#F6F3EA",
      functionKey: "#E8E4D9",
      primaryFunctionKey: "#D8D4CA",
      accentKey: "#CFCBC0",
      keyPressed: "#E2DED3",
      keyboardBackground: "#ECE8DD",
      border: "#C3BEB3",
      depth: "#C3BEB3",
      accent: "#1D2430",
      secondary: "#6C7480"
    },
    shape: { roundnessDp: 5, borderWidthDp: 1, keyGapDp: 6, depthEnabled: true, depthDp: 1 },
    typography: defaultTypography(false, false),
    additionalNumberRow: { colorMode: "full_dimmed" },
    icons: { modifierPackId: "dots-lines" },
    keyDisplayOverrides: { alpha: { type: "icon", value: "dot" } },
    keyTextColorOverrides: dotTextOverrides(false),
    keyBackgroundColorOverrides: dotBackgroundOverrides(false)
  },
  "gmk-dots-dark": {
    name: "GMK Dots Dark Inspired",
    colors: {
      keyIdle: "#20242C",
      functionKey: "#1B1F27",
      primaryFunctionKey: "#15181E",
      accentKey: "#181C23",
      keyPressed: "#2C313C",
      keyboardBackground: "#101318",
      border: "#0B0E12",
      depth: "#0B0E12",
      accent: "#F4F6FA",
      secondary: "#AAB4C2"
    },
    shape: { roundnessDp: 5, borderWidthDp: 1, keyGapDp: 6, depthEnabled: true, depthDp: 1 },
    typography: defaultTypography(false, false),
    additionalNumberRow: { colorMode: "full_dimmed" },
    icons: { modifierPackId: "dots-lines" },
    keyDisplayOverrides: { alpha: { type: "icon", value: "dot" } },
    keyTextColorOverrides: dotTextOverrides(true),
    keyBackgroundColorOverrides: dotBackgroundOverrides(true)
  }
};

const externalPresetFiles = [
  "gmk-bento",
  "gmk-metropolis",
  "gmk-oblivion",
  "gmk-oblivion-hagoromo",
  "gmk-8008",
  "gmk-hammerhead",
  "gmk-dracula",
  "gmk-modern-dolch",
  "gmk-olivia-light",
  "gmk-olivia-dark",
  "gmk-dots-light",
  "gmk-dots-dark"
].map(id => ({ id, url: `../themes/${id}.json` }));

let state = cloneTheme(presets["ios-clean-light"]);

const ids = {
  name: document.getElementById("themeName"),
  author: document.getElementById("themeAuthor"),
  preset: document.getElementById("presetSelect"),
  colors: document.getElementById("colorControls"),
  shape: document.getElementById("shapeControls"),
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
  textOverrides: document.getElementById("textOverridesText"),
  backgroundOverrides: document.getElementById("backgroundOverridesText"),
  output: document.getElementById("jsonOutput"),
  preview: document.getElementById("keyboardPreview"),
  status: document.getElementById("status")
};

init();

async function init() {
  await loadExternalPresets();
  Object.entries(presets).forEach(([id, preset]) => {
    const option = document.createElement("option");
    option.value = id;
    option.textContent = preset.name;
    ids.preset.appendChild(option);
  });
  buildColorControls();
  buildShapeControls();
  bindStaticControls();
  renderForm();
  update();
}

async function loadExternalPresets() {
  const loaded = await Promise.all(externalPresetFiles.map(async ({ id, url }) => {
    try {
      const response = await fetch(url, { cache: "no-store" });
      if (!response.ok) {
        return null;
      }
      return [id, themeJsonToPreset(await response.json())];
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

function defaultTypography(hangulHints, englishHints) {
  return {
    fontFamily: "noto_sans_kr",
    primaryTextSizePercent: hangulHints ? 82 : 78,
    secondaryTextSizePercent: hangulHints ? 78 : 80,
    primaryTextBold: false,
    primaryTextItalic: false,
    secondaryTextBold: false,
    secondaryTextItalic: false,
    showHangulSlideHints: hangulHints,
    showEnglishSlideHints: englishHints
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
    typography: { ...state.typography },
    keyTextColorOverrides: { ...state.keyTextColorOverrides }
  };
  if (state.icons?.modifierPackId || state.icons?.keyDisplayPackId) {
    theme.icons = {};
    if (state.icons.modifierPackId) {
      theme.icons.modifierPackId = state.icons.modifierPackId;
    }
    if (state.icons.keyDisplayPackId) {
      theme.icons.keyDisplayPackId = state.icons.keyDisplayPackId;
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
    ["Lang", "Space", "Enter"]
  ];

  rows.forEach(row => {
    const rowEl = document.createElement("div");
    rowEl.className = "key-row";
    rowEl.style.gridTemplateColumns = row.map(key => key === "Space" ? "2.5fr" : "1fr").join(" ");
    row.forEach(label => {
      const key = document.createElement("div");
      const role = roleForPreview(label);
      const number = /^[0-9]$/.test(label);
      const numberAccent = number && numberRowUsesAccent(theme.additionalNumberRow.colorMode, label);
      const bgOverride = backgroundColorFor(label, theme);
      key.className = "key";
      key.style.background = bgOverride || backgroundForRole(role, theme, numberAccent);
      key.style.borderColor = theme.colors.border;
      key.style.borderWidth = `${theme.shape.borderWidthDp}px`;
      key.style.borderRadius = `${theme.shape.roundnessDp}px`;
      key.style.boxShadow = theme.shape.depthEnabled
        ? `inset 0 -${theme.shape.depthDp}px ${theme.colors.depth || theme.colors.border}`
        : "none";
      key.style.color = number
        ? (numberAccent ? theme.colors.secondary : theme.colors.accent)
        : textColorFor(label, theme);
      key.style.fontSize = `${14 * theme.typography.primaryTextSizePercent / 100}px`;
      key.style.fontWeight = theme.typography.primaryTextBold ? "700" : "400";
      const displayOverride = displayOverrideFor(label, theme);
      if (displayOverride?.type === "icon" && displayOverride.value === "dot") {
        const mainDot = document.createElement("span");
        mainDot.className = "main-dot";
        mainDot.style.background = textColorFor(label, theme);
        key.appendChild(mainDot);
      } else if (displayOverride?.type === "text") {
        key.textContent = displayOverride.value;
        if (isSimpleTextPack(theme.icons?.keyDisplayPackId)) {
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
  if (!sub || !theme.typography.showEnglishSlideHints || displayOverrideFor(label, theme)) {
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

function roleForPreview(label) {
  if (["Shift", "Bksp", "Enter", "Lang", "Options", "Reserved", "Settings", ".", "/"].includes(label)) {
    return "primary";
  }
  if (["?123"].includes(label)) {
    return "modifier";
  }
  return "alpha";
}

function backgroundForRole(role, theme, numberAccent) {
  if (numberAccent) {
    return theme.colors.accentKey;
  }
  switch (role) {
    case "primary":
      return theme.colors.primaryFunctionKey;
    case "modifier":
      return theme.colors.functionKey;
    case "accent":
      return theme.colors.accentKey;
    case "alpha":
    default:
      return theme.colors.keyIdle;
  }
}

function numberRowUsesAccent(mode, label) {
  if (mode === "full_default") {
    return false;
  }
  if (mode === "center_dimmed") {
    return label >= "4" && label <= "7";
  }
  return true;
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
  return ["Shift", "Bksp", "Lang", "Enter"].includes(label);
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
      case "Bksp":
        return { type: "text", value: "del" };
      case "Shift":
        return { type: "text", value: "shift" };
      case "Space":
        return { type: "text", value: "space" };
      case "Lang":
        return { type: "text", value: "lang" };
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
  if (pack === "metropolis-points") {
    const line = document.createElement("span");
    line.className = "mod-pack-line metro-line";
    line.style.background = metropolisColorFor(label);
    line.style.color = metropolisColorFor(label);
    key.appendChild(line);
    return true;
  }
  return false;
}

function metropolisColorFor(label) {
  switch (label) {
    case "Shift":
      return "#FF4B3E";
    case "Bksp":
      return "#FFB000";
    case "Enter":
    case "Lang":
      return "#66E3C4";
    default:
      return "#70D7E8";
  }
}

function isSimpleTextPack(pack) {
  return pack === "simple-text" || pack === "olivia-script-text";
}

function isGitCommandPack(pack) {
  return pack === "git-commands";
}

function isAlphaPreviewLabel(label) {
  return /^[a-z]$/i.test(label)
    || /^[\u3131-\u318e\uac00-\ud7a3]$/.test(label)
    || ["?", ".", "/", ".."].includes(label);
}

function legacyDisplayOverrides(legendStyle) {
  return legendStyle?.preset === "dots"
    ? { alpha: { type: "icon", value: "dot" } }
    : {};
}

function textColorFor(label, theme) {
  const key = overrideKeyForLabel(label);
  return theme.keyTextColorOverrides[key]
    || (isAlphaPreviewLabel(label) ? theme.keyTextColorOverrides.alpha : null)
    || (iconForPreview(label) ? theme.keyTextColorOverrides.modifiers : null)
    || theme.colors.accent;
}

function backgroundColorFor(label, theme) {
  const key = overrideKeyForLabel(label);
  const overrides = theme.keyBackgroundColorOverrides || {};
  return overrides[key]
    || (isAlphaPreviewLabel(label) ? overrides.alpha : null)
    || (iconForPreview(label) ? overrides.modifiers : null)
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
  const required = ["keyIdle", "functionKey", "primaryFunctionKey", "keyPressed", "keyboardBackground", "border", "accent", "secondary", "accentKey"];
  const missing = required.filter(key => !normalizeColor(theme.colors[key]));
  if (missing.length) {
    return `Missing or invalid colors: ${missing.join(", ")}`;
  }
  return "Valid schemaVersion 1 theme JSON.";
}

function themeJsonToPreset(parsed) {
  const base = cloneTheme(presets["ios-clean-light"]);
  return {
    name: parsed.name || "Imported Theme",
    colors: { ...base.colors, ...(parsed.colors || {}) },
    shape: { ...base.shape, ...(parsed.shape || {}) },
    typography: { ...base.typography, ...(parsed.typography || {}) },
    effects: parsed.effects || base.effects || {},
    icons: normalizedIconPacks(parsed.icons || {}),
    additionalNumberRow: {
      colorMode: parsed.additionalNumberRow?.colorMode || "full_dimmed"
    },
    keyDisplayOverrides: parsed.keyDisplayOverrides || legacyDisplayOverrides(parsed.legendStyle),
    keyTextColorOverrides: parsed.keyTextColorOverrides || parsed.keyColorOverrides || {},
    keyBackgroundColorOverrides: parsed.keyBackgroundColorOverrides || {}
  };
}

function normalizedIconPacks(rawIcons) {
  const icons = { ...rawIcons };
  if (isSimpleTextPack(icons.modifierPackId)) {
    icons.keyDisplayPackId = "simple-text";
    delete icons.modifierPackId;
  }
  if (isSimpleTextPack(icons.keyDisplayPackId)) {
    icons.keyDisplayPackId = "simple-text";
  }
  return icons;
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
