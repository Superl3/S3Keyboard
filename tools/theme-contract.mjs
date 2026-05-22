export const themeContract = {
  schemaVersion: 1,
  colorFields: [
    {
      key: "alphaKey",
      label: "\uC804\uCCB4 - \uAE30\uBCF8 \uD0A4",
      description: "\uC77C\uBC18 \uC785\uB825 \uD0A4\uC640 \uC2A4\uD398\uC774\uC2A4\uBC14 \uBC30\uACBD\uC0C9\uC785\uB2C8\uB2E4."
    },
    {
      key: "modifierKey",
      label: "\uC804\uCCB4 - \uAE30\uB2A5 \uD0A4",
      description: "\uC635\uC158, \uC608\uC57D\uC5B4, \uD55C/\uC601 \uD0A4 \uBC30\uACBD\uC0C9\uC785\uB2C8\uB2E4."
    },
    {
      key: "accentKey",
      label: "\uC804\uCCB4 - \uAC15\uC870 \uD0A4",
      description: "\uB529\uAD74 \uAC15\uC870 \uD2B9\uC218 \uD0A4\uB97C \uD3EC\uD568\uD55C \uAC15\uC870 \uADF8\uB8F9 \uBC30\uACBD\uC0C9\uC785\uB2C8\uB2E4."
    },
    {
      key: "keyPressed",
      label: "\uB20C\uB9BC",
      description: "\uD0A4\uB97C \uB204\uB974\uB294 \uB3D9\uC548 \uD45C\uC2DC\uB418\uB294 \uBC30\uACBD\uC0C9\uC785\uB2C8\uB2E4."
    },
    {
      key: "keyboardBackground",
      label: "\uD0A4\uBCF4\uB4DC \uBC30\uACBD",
      description: "\uD0A4 \uC0AC\uC774\uC640 \uD0A4 \uB4A4\uCABD \uC601\uC5ED\uC758 \uC0C9\uC0C1\uC785\uB2C8\uB2E4."
    },
    {
      key: "panelBackground",
      label: "Panel background",
      description: "Actual keyboard panel color. This overrides keyboardBackground when exported.",
      optional: true
    },
    {
      key: "border",
      label: "\uD14C\uB450\uB9AC",
      description: "\uD0A4 \uC678\uACFD\uC120 \uC0C9\uC0C1\uC785\uB2C8\uB2E4."
    },
    {
      key: "depth",
      label: "\uC785\uCCB4 \uD6A8\uACFC \uC0C9\uC0C1",
      description: "\uD0A4 \uC544\uB798\uCABD \uC785\uCCB4 \uD6A8\uACFC \uC0C9\uC0C1\uC785\uB2C8\uB2E4. null\uC774\uBA74 \uAC01 \uD0A4 \uBC30\uACBD\uC0C9\uC744 \uC790\uB3D9\uC73C\uB85C dim \uCC98\uB9AC\uD569\uB2C8\uB2E4.",
      nullable: true
    },
    {
      key: "accent",
      label: "\uC8FC \uAE00\uC790",
      description: "\uC911\uC559 \uAE00\uC790, \uC544\uC774\uCF58, \uBBF8\uB9AC\uBCF4\uAE30 \uD14D\uC2A4\uD2B8 \uC0C9\uC0C1\uC785\uB2C8\uB2E4."
    },
    {
      key: "secondary",
      label: "\uBCF4\uC870 \uAE00\uC790",
      description: "\uC2AC\uB77C\uC774\uB4DC \uD78C\uD2B8\uC640 \uBCF4\uC870 \uD14D\uC2A4\uD2B8 \uC0C9\uC0C1\uC785\uB2C8\uB2E4."
    }
  ],
  shapeFields: [
    { key: "roundnessDp", label: "\uB465\uAE00\uAE30", min: 0, max: 24 },
    { key: "borderWidthDp", label: "\uD14C\uB450\uB9AC \uAD75\uAE30", min: 0, max: 8 },
    { key: "keyGapDp", label: "\uD0A4 \uC0AC\uC774 \uC2DC\uAC01 \uAC04\uACA9", min: 0, max: 18 },
    { key: "depthDp", label: "\uC785\uCCB4 \uB192\uC774", min: 0, max: 8 }
  ],
  typographyFields: [
    "fontFamily",
    "primaryTextSizePercent",
    "secondaryTextSizePercent",
    "primaryTextBold",
    "primaryTextItalic",
    "secondaryTextBold",
    "secondaryTextItalic"
  ],
  fontFamilies: [
    { id: "default", label: "Default" },
    { id: "noto_sans_kr", label: "Noto Sans KR" },
    { id: "noto_serif_kr", label: "Noto Serif KR" },
    { id: "d2coding", label: "D2Coding" }
  ],
  dingulRoleFields: [
    { role: "alpha", label: "Alpha" },
    { role: "mod", label: "Mod" },
    { role: "modInv", label: "Mod inv" }
  ],
  visualRoles: [
    {
      id: "alpha",
      label: "Alpha",
      contrastIntent: "primary",
      description: "Main typing surface. This should remain readable."
    },
    {
      id: "mod",
      label: "Mod",
      contrastIntent: "primary",
      description: "Regular modifier surface. This should remain readable."
    },
    {
      id: "accent",
      label: "Accent",
      contrastIntent: "decorative",
      description: "Visual highlight role chosen by the theme or user accent policy."
    },
    {
      id: "modEnter",
      label: "Mod enter",
      contrastIntent: "decorative",
      description: "Visual accent role for dot-like enter punctuation keys."
    },
    {
      id: "modShift",
      label: "Mod shift",
      contrastIntent: "decorative",
      description: "Visual accent role for slash-like shift punctuation keys."
    },
    {
      id: "modCtrl",
      label: "Mod ctrl",
      contrastIntent: "primary",
      description: "Bottom command keys such as settings and enter."
    },
    {
      id: "modMeta",
      label: "Mod meta",
      contrastIntent: "primary",
      description: "Bottom meta keys such as reserved and language."
    },
    {
      id: "modCommand",
      label: "Mod command",
      contrastIntent: "primary",
      description: "Shift/backspace command keys. Their final visual role is intentionally still open."
    },
    {
      id: "qwertyShift",
      label: "QWERTY shift",
      contrastIntent: "primary",
      description: "The single QWERTY Shift key."
    },
    {
      id: "backspace",
      label: "Backspace",
      contrastIntent: "primary",
      description: "Backspace/delete command key."
    },
    {
      id: "enter",
      label: "Enter",
      contrastIntent: "primary",
      description: "The actual Enter/send command key without options or settings."
    },
    {
      id: "settingsEnter",
      label: "Settings and enter",
      contrastIntent: "primary",
      description: "Bottom options/settings and enter/send command surfaces."
    },
    {
      id: "space",
      label: "Space",
      contrastIntent: "primary",
      description: "Spacebar; treated as alpha for text entry and as a long modifier surface visually."
    },
    {
      id: "question",
      label: "Dingul question",
      contrastIntent: "primary",
      description: "Dingul ? key. Defaults to alpha, but can be promoted to mod or accent by policy."
    },
    {
      id: "escPoint",
      label: "Esc point keycap",
      contrastIntent: "primary",
      description: "Optional point keycap accent: number-row 1 when visible, otherwise QWERTY q or Dingul ㄱ."
    }
  ],
  layoutRoleMaps: {
    dingul: {
      numberOuter: ["1", "2", "3", "8", "9", "0"],
      numberInner: ["4", "5", "6", "7"],
      alpha: [
        "tap:\u3131",
        "tap:\u3134",
        "tap:\u3162",
        "tap:\u3139",
        "tap:\u3141",
        "tap:\u3163",
        "tap:\u3145",
        "tap:\u3147",
        "tap:\u3161",
        "tap:\u3148",
        "tap:\u314E",
        "?",
        "space"
      ],
      mod: [",;", "@/"],
      modEnter: ["."],
      modShift: ["/"],
      dingulDot: ["."],
      dingulSlash: ["/"],
      question: ["?"],
      escPoint: ["1", "tap:\u3131"],
      modCtrl: ["options", "settings", "enter"],
      enter: ["enter"],
      settingsEnter: ["options", "settings", "enter"],
      modMeta: ["reserved", "language"],
      modCommand: ["shift", "backspace"],
      qwertyShift: ["shift"],
      backspace: ["backspace"]
    },
    qwerty: {
      numberOuter: ["1", "2", "3", "8", "9", "0"],
      numberInner: ["4", "5", "6", "7"],
      alpha: [
        "q",
        "w",
        "e",
        "r",
        "t",
        "y",
        "u",
        "i",
        "o",
        "p",
        "a",
        "s",
        "d",
        "f",
        "g",
        "h",
        "j",
        "k",
        "l",
        "z",
        "x",
        "c",
        "v",
        "b",
        "n",
        "m",
        "space"
      ],
      modCtrl: ["options", "settings", "enter"],
      enter: ["enter"],
      settingsEnter: ["options", "settings", "enter"],
      modMeta: ["reserved", "language"],
      modCommand: ["shift", "backspace"],
      qwertyShift: ["shift"],
      backspace: ["backspace"],
      escPoint: ["1", "q"]
    }
  },
  accentPolicyTargets: [
    "none",
    "modEnter",
    "modShift",
    "dingulDot",
    "dingulSlash",
    "modCtrl",
    "modMeta",
    "modCommand",
    "enter",
    "settingsEnter",
    "qwertyShift",
    "backspace",
    "question",
    "escPoint",
    "shift",
    "punctuation",
    "perKey"
  ],
  accentPolicySpacebarRoles: ["default", "alpha", "mod", "modifier", "accent"],
  accentPolicyQuestionRoles: ["default", "alpha", "mod", "modifier", "accent"],
  defaultAccentPolicy: {
    qwerty: ["modMeta", "qwertyShift", "backspace"],
    dingul: ["modCtrl", "dingulDot"]
  },
  userPreferenceCandidates: [
    {
      id: "shiftLongPressColor",
      label: "Shift long-press color",
      reason: "This behaves more like interaction feedback than theme identity, so it should stay user-controlled unless a future visual role proves otherwise."
    }
  ],
  themeCoverageClasses: [
    { id: "1", label: "all same fore/back color" },
    { id: "2", label: "alpha/mod two tone" },
    { id: "3", label: "alpha/mod/accent three tone" },
    { id: "4.1", label: "custom per mod keys" },
    { id: "4.2", label: "custom few alpha keys" },
    { id: "5", label: "custom mod and alpha keys" }
  ],
  colorwayClasses: [
    { id: "a", label: "one colorway" },
    { id: "b", label: "two colorway" },
    { id: "c", label: "three colorway" },
    { id: "d", label: "colorful" }
  ],
  reviewClassification: {
    coreRolePairs: ["alpha", "mod"],
    authoredAccentBackgroundField: "accentKey",
    minimumAccentBackgroundDistance: 48,
    metadataOnlyPairs: ["modInv"],
    ignoredColorFields: ["keyPressed"],
    note: "Pressed colors and derived/inverted pairs are not counted as accent colorways by themselves. A modInv pair backed by a visually distinct authored accentKey background counts as the third colorway."
  },
  contrastPolicy: {
    primaryMinimumRatio: 1.6,
    dimmedMinimumRatio: 1.15,
    decorativeMinimumRatio: 1.0,
    warningRoles: ["alpha", "mod"]
  },
  numberRowModes: [
    { id: "full_alpha", label: "All alpha", outerRole: "alpha", innerRole: "alpha", legacyAliases: ["full_default"] },
    { id: "half_mod_4567", label: "Center mod", outerRole: "alpha", innerRole: "mod", legacyAliases: ["center_dimmed"] },
    { id: "alpha_accent", label: "Center accent", outerRole: "alpha", innerRole: "accent" },
    { id: "mod_alpha", label: "Outer mod / center alpha", outerRole: "mod", innerRole: "alpha" },
    { id: "full_mod", label: "All mod", outerRole: "mod", innerRole: "mod", legacyAliases: ["full_dimmed"] },
    { id: "mod_accent", label: "Outer mod / center accent", outerRole: "mod", innerRole: "accent" },
    { id: "accent_alpha", label: "Outer accent / center alpha", outerRole: "accent", innerRole: "alpha" },
    { id: "accent_mod", label: "Outer accent / center mod", outerRole: "accent", innerRole: "mod" },
    { id: "full_accent", label: "All accent", outerRole: "accent", innerRole: "accent" }
  ],
  defaultNumberRowMode: "full_mod",
  modifierIconPacks: [
    { id: "", label: "Theme default (line mono)" },
    { id: "line-mono", label: "Line Mono" },
    { id: "accent-color", label: "Accent Color" },
    { id: "dots-lines", label: "Dots Lines" },
    { id: "metropolis-graph", label: "Metropolis Graph" }
  ],
  legacyModifierIconPackIds: {
    "metropolis-points": "metropolis-graph"
  },
  keyDisplayPacks: [
    { id: "", label: "None" },
    { id: "simple-text", label: "Simple Text" },
    { id: "git-commands", label: "Git Commands" }
  ],
  legacyKeyDisplayPackIds: {
    "olivia-script-text": "simple-text"
  },
  simpleTextPackIds: ["simple-text", "olivia-script-text"],
  displayOverrideTypes: ["icon", "text"],
  subLegendPolicy: {
    hideWhenMainGlyphIsCustom: true,
    appliesTo: [
      "keyDisplayOverrides",
      "keyDisplayPacks",
      "dots-lines",
      "metropolis-graph"
    ]
  },
  metadataArrayFields: ["tags", "features"],
  metadataRecommendedHintFields: [
    "showHangulSlideHints",
    "showEnglishSlideHints",
    "showBeginnerTooltipPreview"
  ],
  appearanceRequiredTextColorOverrides: ["shiftIndicator"],
  supportedRootKeys: [
    "schemaVersion",
    "name",
    "author",
    "description",
    "metadata",
    "colors",
    "shape",
    "additionalNumberRow",
    "accentPolicy",
    "typography",
    "dingulColors",
    "icons",
    "effects",
    "keyTextColorOverrides",
    "keyColorOverrides",
    "keyBackgroundColorOverrides",
    "keyDisplayOverrides",
    "legendStyle",
    "layers",
    "themeLayers",
    "extends",
    "baseTheme",
    "modifierPacks",
    "keyDisplayPacks",
    "iconPacks"
  ]
};

export function webThemeContract() {
  return JSON.parse(JSON.stringify(themeContract));
}

export function requiredColorKeys() {
  return themeContract.colorFields
    .filter(field => !field.optional)
    .map(field => field.key);
}

export function requiredShapeKeys() {
  return themeContract.shapeFields.map(field => field.key);
}

export function numberRowModeIds(includeLegacy = false) {
  const ids = themeContract.numberRowModes.map(mode => mode.id);
  if (!includeLegacy) {
    return ids;
  }
  return ids.concat(themeContract.numberRowModes.flatMap(mode => mode.legacyAliases || []));
}

export function modifierIconPackIds(includeLegacy = false) {
  const ids = themeContract.modifierIconPacks.map(pack => pack.id).filter(Boolean);
  if (!includeLegacy) {
    return ids;
  }
  return ids.concat(Object.keys(themeContract.legacyModifierIconPackIds));
}

export function keyDisplayPackIds(includeLegacy = false) {
  const ids = themeContract.keyDisplayPacks.map(pack => pack.id).filter(Boolean);
  if (!includeLegacy) {
    return ids;
  }
  return ids.concat(Object.keys(themeContract.legacyKeyDisplayPackIds));
}

export function normalizeNumberRowMode(mode) {
  for (const option of themeContract.numberRowModes) {
    if (option.id === mode || (option.legacyAliases || []).includes(mode)) {
      return option.id;
    }
  }
  return themeContract.defaultNumberRowMode;
}
