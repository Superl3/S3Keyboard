const colorFields = [
  ["keyIdle", "Default key"],
  ["functionKey", "Function key"],
  ["primaryFunctionKey", "Primary function"],
  ["accentKey", "Accent key"],
  ["keyPressed", "Pressed"],
  ["keyboardBackground", "Keyboard bg"],
  ["border", "Border"],
  ["depth", "Depth"],
  ["accent", "Text accent"],
  ["secondary", "Secondary text"]
];

const shapeFields = [
  ["roundnessDp", "Roundness", 0, 24],
  ["borderWidthDp", "Border width", 0, 8],
  ["keyGapDp", "Key gap", 0, 18],
  ["depthDp", "Depth", 0, 8],
  ["keyboardTopPaddingDp", "Top padding", 0, 48]
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
    shape: { roundnessDp: 5, borderWidthDp: 1, keyGapDp: 5, depthEnabled: false, depthDp: 0, keyboardTopPaddingDp: 6 },
    typography: defaultTypography(false, false),
    additionalNumberRow: { colorMode: "full_dimmed" },
    keyTextColorOverrides: {
      "shiftIndicator": "#2563EB"
    }
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
    shape: { roundnessDp: 4, borderWidthDp: 1, keyGapDp: 5, depthEnabled: true, depthDp: 2, keyboardTopPaddingDp: 6 },
    typography: defaultTypography(true, true),
    additionalNumberRow: { colorMode: "center_dimmed" },
    keyTextColorOverrides: {
      "tap:q": "#7C3CB3",
      "tap:w": "#008B82",
      "tap:e": "#B85F19",
      "tap:r": "#C02666",
      "shiftIndicator": "#008B82",
      "__dingul_center_vowel__": "#C98900",
      "__dingul_wide_vowel__": "#007C89",
      "space": "#2B2D31",
      "enter": "#A95B00"
    }
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
    shape: { roundnessDp: 4, borderWidthDp: 1, keyGapDp: 5, depthEnabled: true, depthDp: 2, keyboardTopPaddingDp: 6 },
    typography: defaultTypography(true, true),
    additionalNumberRow: { colorMode: "center_dimmed" },
    keyTextColorOverrides: {
      "tap:q": "#C75DFF",
      "tap:w": "#4DE4D2",
      "tap:e": "#FF9B48",
      "tap:r": "#FF5DAE",
      "shiftIndicator": "#36E7F4",
      "__dingul_center_vowel__": "#FFD25A",
      "__dingul_wide_vowel__": "#36E7F4",
      "space": "#F7EEDB",
      "enter": "#FF9F32"
    }
  }
};

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
  overrides: document.getElementById("overridesText"),
  output: document.getElementById("jsonOutput"),
  preview: document.getElementById("keyboardPreview"),
  status: document.getElementById("status")
};

init();

function init() {
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

function buildColorControls() {
  colorFields.forEach(([key, label]) => {
    const row = document.createElement("label");
    row.className = "color-row";
    row.innerHTML = `<span>${label}</span><input id="color-${key}" type="color"><input id="text-${key}" type="text">`;
    ids.colors.appendChild(row);
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
  ids.overrides.addEventListener("input", () => {
    state.keyTextColorOverrides = parseOverrides(ids.overrides.value);
    update();
  });
  document.getElementById("copyJson").addEventListener("click", copyJson);
  document.getElementById("importJson").addEventListener("click", importJson);
  document.getElementById("downloadJson").addEventListener("click", downloadJson);
}

function renderForm() {
  ids.name.value = state.name || "Untitled Theme";
  colorFields.forEach(([key]) => {
    const value = state.colors[key] || "#000000";
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
  ids.overrides.value = formatOverrides(state.keyTextColorOverrides);
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
  return {
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
      border: state.colors.border,
      depth: ids.customDepth.checked ? state.colors.depth : null,
      accent: state.colors.accent,
      secondary: state.colors.secondary
    },
    shape: { ...state.shape },
    additionalNumberRow: { colorMode: state.additionalNumberRow.colorMode },
    typography: { ...state.typography },
    keyTextColorOverrides: { ...state.keyTextColorOverrides }
  };
}

function renderPreview(theme) {
  ids.preview.style.background = theme.colors.keyboardBackground;
  ids.preview.style.gap = `${theme.shape.keyGapDp}px`;
  ids.preview.style.paddingTop = `${10 + theme.shape.keyboardTopPaddingDp}px`;
  ids.preview.style.setProperty("--depth-color", theme.colors.depth || theme.colors.border);
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
    rowEl.style.gridTemplateColumns = row.map(key => key === "Space" ? "2fr" : "1fr").join(" ");
    row.forEach(label => {
      const key = document.createElement("div");
      const control = ["Shift", "Bksp", "Lang", "Space", "Enter"].includes(label);
      const number = /^[0-9]$/.test(label);
      key.className = "key";
      key.textContent = label;
      const numberAccent = number && numberRowUsesAccent(theme.additionalNumberRow.colorMode, label);
      key.style.background = number
        ? (numberAccent ? theme.colors.accentKey : theme.colors.keyIdle)
        : (control ? theme.colors.accentKey : theme.colors.keyIdle);
      key.style.borderColor = theme.colors.border;
      key.style.borderRadius = `${theme.shape.roundnessDp}px`;
      key.style.boxShadow = theme.shape.depthEnabled
        ? `inset 0 -${theme.shape.depthDp}px ${theme.colors.depth || theme.colors.border}`
        : "none";
      key.style.color = number
        ? (numberAccent ? theme.colors.secondary : theme.colors.accent)
        : textColorFor(label, theme);
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

function numberRowUsesAccent(mode, label) {
  if (mode === "full_default") {
    return false;
  }
  if (mode === "center_dimmed") {
    return label >= "4" && label <= "7";
  }
  return true;
}

function textColorFor(label, theme) {
  const key = label.length === 1 ? `tap:${label.toLowerCase()}` : label.toLowerCase();
  return theme.keyTextColorOverrides[key] || theme.colors.accent;
}

function validateTheme(theme) {
  const required = ["keyIdle", "keyPressed", "keyboardBackground", "accent", "secondary", "accentKey"];
  const missing = required.filter(key => !normalizeColor(theme.colors[key]));
  if (missing.length) {
    return `Missing or invalid colors: ${missing.join(", ")}`;
  }
  return "Valid schemaVersion 1 theme JSON.";
}

function importJson() {
  try {
    const parsed = JSON.parse(ids.output.value);
    if (parsed.schemaVersion !== 1) {
      throw new Error("Only schemaVersion 1 is supported.");
    }
    state = {
      name: parsed.name || "Imported Theme",
      colors: { ...cloneTheme(presets["ios-clean-light"]).colors, ...(parsed.colors || {}) },
      shape: { ...cloneTheme(presets["ios-clean-light"]).shape, ...(parsed.shape || {}) },
      typography: { ...cloneTheme(presets["ios-clean-light"]).typography, ...(parsed.typography || {}) },
      additionalNumberRow: {
        colorMode: parsed.additionalNumberRow?.colorMode || "full_dimmed"
      },
      keyTextColorOverrides: parsed.keyTextColorOverrides || parsed.keyColorOverrides || {}
    };
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
