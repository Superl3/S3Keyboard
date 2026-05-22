import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import {
  keyDisplayPackIds,
  modifierIconPackIds,
  numberRowModeIds,
  requiredColorKeys,
  requiredShapeKeys,
  themeContract,
  webThemeContract
} from "./theme-contract.mjs";

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(scriptDir, "..");
const themesDir = path.join(rootDir, "themes");
const defaultJavaDir = path.join(rootDir, "app", "build", "generated", "source", "themePresets", "java");
const defaultWebIndex = path.join(rootDir, "web-theme-builder", "theme-index.generated.js");
const defaultWebContract = path.join(rootDir, "web-theme-builder", "theme-contract.generated.js");
const defaultHtmlReport = path.join(rootDir, "captures", "theme-classification-report.html");
const minAccentBackgroundDistance = 48;

const requiredThemeColorKeys = requiredColorKeys();
const requiredThemeShapeKeys = [...requiredShapeKeys(), "depthEnabled"];
const requiredTypographyKeys = themeContract.typographyFields;
const numberRowModes = new Set(numberRowModeIds(true));
const modifierPackIds = new Set(modifierIconPackIds(true));
const keyDisplayIds = new Set(keyDisplayPackIds(true));
const displayOverrideTypes = new Set(themeContract.displayOverrideTypes);
const supportedRootKeys = new Set(themeContract.supportedRootKeys);
const accentPolicyTargets = new Set(themeContract.accentPolicyTargets);

const args = process.argv.slice(2);
const options = parseArgs(args);

main().catch(error => {
  console.error(error.stack || error.message);
  process.exit(1);
});

async function main() {
  if (!fs.existsSync(themesDir)) {
    throw new Error(`Missing themes directory: ${themesDir}`);
  }

  if (options.migrateHints) {
    migrateHints();
  }

  const themes = readThemes();
  const diagnostics = validateThemes(themes);
  printDiagnostics(diagnostics);
  if (diagnostics.errors.length > 0) {
    process.exit(1);
  }

  if (options.report) {
    printReport(themes, diagnostics);
  }
  if (options.htmlReport) {
    writeFileIfChanged(options.htmlReport, generatedHtmlReport(themes, diagnostics, options.htmlReport));
  }

  if (options.generateJava) {
    writeFileIfChanged(
      path.join(options.javaDir, "com", "superl3", "s3keyboard", "GeneratedKeyboardThemePresets.java"),
      generatedJava(themes));
  }

  if (options.generateWeb) {
    writeFileIfChanged(options.webIndex, generatedWebIndex(themes));
    writeFileIfChanged(options.webContract, generatedWebContract());
  }

  if (options.check) {
    const expectedWeb = generatedWebIndex(themes);
    const actualWeb = fs.existsSync(options.webIndex)
      ? fs.readFileSync(options.webIndex, "utf8")
      : "";
    if (actualWeb !== expectedWeb) {
      console.error(`Generated web theme index is stale: ${relative(options.webIndex)}`);
      console.error("Run: rtk node tools/sync-themes.mjs --generate-web");
      process.exit(1);
    }
    const expectedContract = generatedWebContract();
    const actualContract = fs.existsSync(options.webContract)
      ? fs.readFileSync(options.webContract, "utf8")
      : "";
    if (actualContract !== expectedContract) {
      console.error(`Generated web theme contract is stale: ${relative(options.webContract)}`);
      console.error("Run: rtk node tools/sync-themes.mjs --generate-web");
      process.exit(1);
    }
  }
}

function parseArgs(values) {
  const parsed = {
    check: false,
    generateJava: false,
    generateWeb: false,
    javaDir: defaultJavaDir,
    webIndex: defaultWebIndex,
    webContract: defaultWebContract,
    htmlReport: null,
    report: false,
    migrateHints: false
  };

  for (let index = 0; index < values.length; index += 1) {
    const value = values[index];
    switch (value) {
      case "--check":
        parsed.check = true;
        break;
      case "--generate":
        parsed.generateJava = true;
        parsed.generateWeb = true;
        break;
      case "--generate-java":
        parsed.generateJava = true;
        if (values[index + 1] && !values[index + 1].startsWith("--")) {
          parsed.javaDir = path.resolve(rootDir, values[index + 1]);
          index += 1;
        }
        break;
      case "--generate-web":
        parsed.generateWeb = true;
        if (values[index + 1] && !values[index + 1].startsWith("--")) {
          parsed.webIndex = path.resolve(rootDir, values[index + 1]);
          index += 1;
        }
        break;
      case "--report":
        parsed.report = true;
        break;
      case "--html-report":
        parsed.htmlReport = values[index + 1] && !values[index + 1].startsWith("--")
          ? path.resolve(rootDir, values[index + 1])
          : defaultHtmlReport;
        if (values[index + 1] && !values[index + 1].startsWith("--")) {
          index += 1;
        }
        break;
      case "--migrate-hints":
        parsed.migrateHints = true;
        break;
      case "--help":
      case "-h":
        printHelp();
        process.exit(0);
        break;
      default:
        throw new Error(`Unknown argument: ${value}`);
    }
  }

  if (!parsed.check && !parsed.generateJava && !parsed.generateWeb && !parsed.report && !parsed.htmlReport && !parsed.migrateHints) {
    parsed.check = true;
    parsed.report = true;
  }
  return parsed;
}

function printHelp() {
  console.log(`Usage: node tools/sync-themes.mjs [options]

Options:
  --check              Validate themes and verify generated web index freshness.
  --generate           Generate Android preset source and web theme index.
  --generate-java DIR  Generate Android preset source under DIR.
  --generate-web FILE  Generate web-theme-builder theme index and contract files.
  --report             Print a compact theme diversity/review report.
  --html-report FILE   Write a readable HTML theme classification report.
  --migrate-hints      Move root hints to metadata.recommendedHints.
`);
}

function readThemes() {
  return fs.readdirSync(themesDir)
    .filter(file => file.endsWith(".json"))
    .sort((left, right) => left.localeCompare(right))
    .map(file => {
      const absolutePath = path.join(themesDir, file);
      const raw = fs.readFileSync(absolutePath, "utf8");
      let json;
      try {
        json = JSON.parse(raw);
      } catch (error) {
        throw new Error(`${relative(absolutePath)}: invalid JSON: ${error.message}`);
      }
      return {
        id: path.basename(file, ".json"),
        file,
        path: absolutePath,
        raw,
        json
      };
    });
}

function validateThemes(themes) {
  const errors = [];
  const warnings = [];
  const seenIds = new Set();
  const seenNames = new Map();

  for (const theme of themes) {
    const at = key => `${theme.file}${key ? `:${key}` : ""}`;
    const json = theme.json;

    if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(theme.id)) {
      errors.push(`${at()}: filename id must be lowercase kebab-case`);
    }
    if (seenIds.has(theme.id)) {
      errors.push(`${at()}: duplicate theme id`);
    }
    seenIds.add(theme.id);

    if (json.schemaVersion !== 1) {
      errors.push(`${at("schemaVersion")}: expected schemaVersion 1`);
    }
    if (typeof json.name !== "string" || json.name.trim() === "") {
      errors.push(`${at("name")}: name is required`);
    } else if (seenNames.has(json.name)) {
      warnings.push(`${at("name")}: duplicate display name also used by ${seenNames.get(json.name)}`);
    } else {
      seenNames.set(json.name, theme.file);
    }
    if (json.author !== undefined && typeof json.author !== "string") {
      errors.push(`${at("author")}: author must be a string`);
    }
    if (json.hints !== undefined) {
      errors.push(`${at("hints")}: root hints are deprecated; use metadata.recommendedHints`);
    }

    for (const key of Object.keys(json)) {
      if (!supportedRootKeys.has(key)) {
        warnings.push(`${at(key)}: unknown root field`);
      }
    }

    validateMetadata(json.metadata, at, errors);
    validateColors(json.colors, at, errors);
    validateShape(json.shape, at, errors);
    validateTypography(json.typography, at, errors);
    validateAdditionalNumberRow(json.additionalNumberRow, at, errors);
    validateAccentPolicy(json.accentPolicy, at, errors);
    validateAccentPolicyColorway(json, at, errors);
    validateIcons(json.icons, at, errors);
    validateEffects(json.effects, at, errors);
    validateColorMap(json.keyTextColorOverrides, at("keyTextColorOverrides"), errors);
    validateColorMap(json.keyColorOverrides, at("keyColorOverrides"), errors);
    validateColorMap(json.keyBackgroundColorOverrides, at("keyBackgroundColorOverrides"), errors);
    validateDisplayOverrides(json.keyDisplayOverrides, at("keyDisplayOverrides"), errors);
    validateBuiltInAppearanceContract(json, at, errors);

    for (const warning of contrastWarnings(theme)) {
      warnings.push(warning);
    }
  }

  return { errors, warnings };
}

function validateMetadata(metadata, at, errors) {
  if (metadata === undefined) {
    return;
  }
  if (!metadata || typeof metadata !== "object" || Array.isArray(metadata)) {
    errors.push(`${at("metadata")}: metadata must be an object`);
    return;
  }
  for (const field of themeContract.metadataArrayFields) {
    if (metadata[field] !== undefined && !isStringArray(metadata[field])) {
      errors.push(`${at(`metadata.${field}`)}: expected an array of strings`);
    }
  }
  if (metadata.recommendedHints !== undefined) {
    const hints = metadata.recommendedHints;
    if (!hints || typeof hints !== "object" || Array.isArray(hints)) {
      errors.push(`${at("metadata.recommendedHints")}: expected an object`);
      return;
    }
    for (const key of themeContract.metadataRecommendedHintFields) {
      if (hints[key] !== undefined && typeof hints[key] !== "boolean") {
        errors.push(`${at(`metadata.recommendedHints.${key}`)}: expected boolean`);
      }
    }
  }
}

function validateColors(colors, at, errors) {
  if (!colors || typeof colors !== "object" || Array.isArray(colors)) {
    errors.push(`${at("colors")}: colors object is required`);
    return;
  }
  for (const key of requiredThemeColorKeys) {
    if (!Object.hasOwn(colors, key)) {
      errors.push(`${at(`colors.${key}`)}: required color is missing`);
      continue;
    }
    if (key === "depth" && colors[key] === null) {
      continue;
    }
    if (!isColor(colors[key])) {
      errors.push(`${at(`colors.${key}`)}: expected #RRGGBB color`);
    }
  }
  if (colors.panelBackground !== undefined && !isColor(colors.panelBackground)) {
    errors.push(`${at("colors.panelBackground")}: expected #RRGGBB color`);
  }
}

function validateShape(shape, at, errors) {
  if (!shape || typeof shape !== "object" || Array.isArray(shape)) {
    errors.push(`${at("shape")}: shape object is required`);
    return;
  }
  for (const key of requiredThemeShapeKeys) {
    if (!Object.hasOwn(shape, key)) {
      errors.push(`${at(`shape.${key}`)}: required shape field is missing`);
    }
  }
  for (const key of ["roundnessDp", "borderWidthDp", "keyGapDp", "depthDp"]) {
    if (shape[key] !== undefined && !isIntegerInRange(shape[key], 0, 48)) {
      errors.push(`${at(`shape.${key}`)}: expected integer 0..48`);
    }
  }
  if (shape.depthEnabled !== undefined && typeof shape.depthEnabled !== "boolean") {
    errors.push(`${at("shape.depthEnabled")}: expected boolean`);
  }
}

function validateTypography(typography, at, errors) {
  if (!typography || typeof typography !== "object" || Array.isArray(typography)) {
    errors.push(`${at("typography")}: typography object is required`);
    return;
  }
  for (const key of requiredTypographyKeys) {
    if (!Object.hasOwn(typography, key)) {
      errors.push(`${at(`typography.${key}`)}: required typography field is missing`);
    }
  }
  for (const key of themeContract.metadataRecommendedHintFields) {
    if (Object.hasOwn(typography, key)) {
      errors.push(`${at(`typography.${key}`)}: hint visibility is a user setting, not typography`);
    }
  }
  for (const key of ["primaryTextSizePercent", "secondaryTextSizePercent"]) {
    if (typography[key] !== undefined && !isIntegerInRange(typography[key], 50, 200)) {
      errors.push(`${at(`typography.${key}`)}: expected integer 50..200`);
    }
  }
  for (const key of ["primaryTextBold", "primaryTextItalic", "secondaryTextBold", "secondaryTextItalic"]) {
    if (typography[key] !== undefined && typeof typography[key] !== "boolean") {
      errors.push(`${at(`typography.${key}`)}: expected boolean`);
    }
  }
}

function validateAdditionalNumberRow(numberRow, at, errors) {
  if (!numberRow || typeof numberRow !== "object" || Array.isArray(numberRow)) {
    errors.push(`${at("additionalNumberRow")}: additionalNumberRow object is required`);
    return;
  }
  if (!numberRowModes.has(numberRow.colorMode)) {
    errors.push(`${at("additionalNumberRow.colorMode")}: expected one of ${[...numberRowModes].sort().join(", ")}`);
  }
}

function validateAccentPolicy(accentPolicy, at, errors) {
  if (accentPolicy === undefined) {
    return;
  }
  if (!accentPolicy || typeof accentPolicy !== "object" || Array.isArray(accentPolicy)) {
    errors.push(`${at("accentPolicy")}: accentPolicy must be an object`);
    return;
  }
  for (const layout of ["qwerty", "dingul"]) {
    const targets = accentPolicy[layout];
    if (targets === undefined) {
      continue;
    }
    if (!Array.isArray(targets)) {
      errors.push(`${at(`accentPolicy.${layout}`)}: expected an array of target ids`);
      continue;
    }
    for (const target of targets) {
      if (!accentPolicyTargets.has(target)) {
        errors.push(`${at(`accentPolicy.${layout}`)}: unknown target ${target}`);
      }
    }
  }
  for (const property of ["spacebar", "space"]) {
    if (accentPolicy[property] === undefined) {
      continue;
    }
    const roles = new Set(themeContract.accentPolicySpacebarRoles || []);
    if (!roles.has(accentPolicy[property])) {
      errors.push(`${at(`accentPolicy.${property}`)}: expected one of ${[...roles].sort().join(", ")}`);
    }
  }
  for (const property of ["question", "questionMark"]) {
    if (accentPolicy[property] === undefined) {
      continue;
    }
    const roles = new Set(themeContract.accentPolicyQuestionRoles || themeContract.accentPolicySpacebarRoles || []);
    if (!roles.has(accentPolicy[property])) {
      errors.push(`${at(`accentPolicy.${property}`)}: expected one of ${[...roles].sort().join(", ")}`);
    }
  }
}

function validateAccentPolicyColorway(json, at, errors) {
  const policy = json.accentPolicy;
  if (!policy || typeof policy !== "object" || Array.isArray(policy)) {
    return;
  }
  const hasTargets = ["qwerty", "dingul"].some(layout =>
    Array.isArray(policy[layout]) && policy[layout].length > 0)
    || policy.spacebar === "accent"
    || policy.space === "accent"
    || policy.question === "accent"
    || policy.questionMark === "accent";
  if (!hasTargets) {
    return;
  }
  const colors = json.colors || {};
  const accent = colors.accentKey;
  const baseBackgrounds = [
    ["alphaKey", colors.alphaKey],
    ["modifierKey", colors.modifierKey]
  ].filter(([, value]) => isColor(value));
  if (!isColor(accent) || baseBackgrounds.length === 0) {
    return;
  }
  for (const [field, background] of baseBackgrounds) {
    if (sameColor(accent, background)
        || colorDistance(accent, background) < minAccentBackgroundDistance) {
      errors.push(`${at("accentPolicy")}: accentPolicy requires a distinct accentKey background; ${field} is too close to accentKey`);
      return;
    }
  }
}

function validateIcons(icons, at, errors) {
  if (icons === undefined) {
    return;
  }
  if (!icons || typeof icons !== "object" || Array.isArray(icons)) {
    errors.push(`${at("icons")}: icons must be an object`);
    return;
  }
  if (icons.modifierPackId !== undefined && !modifierPackIds.has(icons.modifierPackId)) {
    errors.push(`${at("icons.modifierPackId")}: unknown modifier pack id`);
  }
  if (icons.keyDisplayPackId !== undefined && !keyDisplayIds.has(icons.keyDisplayPackId)) {
    errors.push(`${at("icons.keyDisplayPackId")}: unknown key display pack id`);
  }
}

function validateEffects(effects, at, errors) {
  if (effects === undefined) {
    return;
  }
  if (!effects || typeof effects !== "object" || Array.isArray(effects)) {
    errors.push(`${at("effects")}: effects must be an object`);
    return;
  }
  validateEffectToggle(effects.blur, at("effects.blur"), errors, ["enabled", "radiusDp"]);
  validateEffectToggle(effects.metal, at("effects.metal"), errors, ["enabled", "strengthPercent"]);
  validateEffectToggle(
    effects.keyFaceGradient,
    at("effects.keyFaceGradient"),
    errors,
    ["enabled", "strengthPercent"]);
  validatePanelGradient(effects.panelGradient, at("effects.panelGradient"), errors);
  if (effects.previewBubble !== undefined) {
    const style = effects.previewBubble?.style;
    if (style !== "rounded" && style !== "angular") {
      errors.push(`${at("effects.previewBubble.style")}: expected rounded or angular`);
    }
  }
}

function validatePanelGradient(object, label, errors) {
  if (object === undefined) {
    return;
  }
  if (!object || typeof object !== "object" || Array.isArray(object)) {
    errors.push(`${label}: expected object`);
    return;
  }
  if (object.enabled !== undefined && typeof object.enabled !== "boolean") {
    errors.push(`${label}.enabled: expected boolean`);
  }
  if (object.startColor !== undefined && !isColor(object.startColor)) {
    errors.push(`${label}.startColor: expected #RRGGBB color`);
  }
  if (object.endColor !== undefined && !isColor(object.endColor)) {
    errors.push(`${label}.endColor: expected #RRGGBB color`);
  }
}

function validateEffectToggle(object, label, errors, keys) {
  if (object === undefined) {
    return;
  }
  if (!object || typeof object !== "object" || Array.isArray(object)) {
    errors.push(`${label}: expected object`);
    return;
  }
  if (object[keys[0]] !== undefined && typeof object[keys[0]] !== "boolean") {
    errors.push(`${label}.${keys[0]}: expected boolean`);
  }
  if (object[keys[1]] !== undefined && !isIntegerInRange(object[keys[1]], 0, 100)) {
    errors.push(`${label}.${keys[1]}: expected integer 0..100`);
  }
}

function validateColorMap(map, label, errors) {
  if (map === undefined) {
    return;
  }
  if (!map || typeof map !== "object" || Array.isArray(map)) {
    errors.push(`${label}: expected object`);
    return;
  }
  for (const [key, value] of Object.entries(map)) {
    if (typeof key !== "string" || key.trim() === "") {
      errors.push(`${label}: override key cannot be blank`);
    }
    if (!isColor(value)) {
      errors.push(`${label}.${key}: expected #RRGGBB color`);
    }
  }
}

function validateDisplayOverrides(overrides, label, errors) {
  if (overrides === undefined) {
    return;
  }
  if (!overrides || typeof overrides !== "object" || Array.isArray(overrides)) {
    errors.push(`${label}: expected object`);
    return;
  }
  const direct = { ...overrides };
  delete direct.keys;
  for (const [key, value] of Object.entries(direct)) {
    validateDisplayOverride(value, `${label}.${key}`, errors);
  }
  if (overrides.keys !== undefined) {
    if (!overrides.keys || typeof overrides.keys !== "object" || Array.isArray(overrides.keys)) {
      errors.push(`${label}.keys: expected object`);
      return;
    }
    for (const [key, value] of Object.entries(overrides.keys)) {
      validateDisplayOverride(value, `${label}.keys.${key}`, errors);
    }
  }
}

function validateDisplayOverride(value, label, errors) {
  if (!value || typeof value !== "object" || Array.isArray(value)) {
    errors.push(`${label}: expected object`);
    return;
  }
  if (!displayOverrideTypes.has(value.type)) {
    errors.push(`${label}.type: expected icon or text`);
  }
  if (typeof value.value !== "string" || value.value.trim() === "") {
    errors.push(`${label}.value: expected non-empty string`);
  }
}

function validateBuiltInAppearanceContract(json, at, errors) {
  const textOverrides = json.keyTextColorOverrides;
  if (!textOverrides || typeof textOverrides !== "object" || Array.isArray(textOverrides)) {
    errors.push(`${at("keyTextColorOverrides")}: built-in themes must declare required appearance overrides`);
    return;
  }
  for (const key of themeContract.appearanceRequiredTextColorOverrides) {
    if (!Object.hasOwn(textOverrides, key)) {
      errors.push(`${at(`keyTextColorOverrides.${key}`)}: built-in themes must declare the ${key} color`);
    }
  }
}

function contrastWarnings(theme) {
  const warnings = [];
  const dingul = theme.json.dingulColors || {};
  const checks = [
    ["alpha", dingul.alpha?.foreground, dingul.alpha?.background],
    ["mod", dingul.mod?.foreground, dingul.mod?.background]
  ];
  for (const [label, foreground, background] of checks) {
    if (isColor(foreground)
        && isColor(background)
        && contrastRatio(foreground, background) < themeContract.contrastPolicy.primaryMinimumRatio) {
      warnings.push(`${theme.file}:${label}: primary readability risk (${contrastRatio(foreground, background).toFixed(2)})`);
    }
  }
  return warnings;
}

function printDiagnostics(diagnostics) {
  for (const warning of diagnostics.warnings) {
    console.warn(`warning: ${warning}`);
  }
  for (const error of diagnostics.errors) {
    console.error(`error: ${error}`);
  }
  if (diagnostics.errors.length === 0) {
    console.log(`Theme validation passed with ${diagnostics.warnings.length} warning(s).`);
  }
}

function printReport(themes, diagnostics) {
  const rows = reportRows(themes, diagnostics);
  console.log("");
  console.log("Theme review report");
  console.table(rows.map(row => ({
    id: row.id,
    tone: row.tone,
    depth: row.depth,
    icons: row.icons,
    display: row.display,
    effects: row.effects,
    dingul: row.dingul,
    coverage: row.coverage,
    colorway: row.colorway,
    dimmed: row.dimmed,
    accentPolicy: row.accentPolicy,
    overrides: row.overrides,
    features: row.features,
    warnings: row.warnings
  })));
}

function reportRows(themes, diagnostics) {
  const warningCounts = warningCountsByFile(diagnostics.warnings);
  return themes.map(theme => {
    const features = inferredFeatures(theme);
    return {
      id: theme.id,
      name: theme.json.name || theme.id,
      file: theme.file,
      preview: `theme-previews/${previewSlug(theme.json.name || theme.id)}.png`,
      tone: luminanceTone(theme.json.colors?.keyboardBackground),
      depth: theme.json.shape?.depthEnabled ? `depth${theme.json.shape.depthDp ?? ""}` : "flat",
      icons: theme.json.icons?.modifierPackId || "-",
      display: theme.json.icons?.keyDisplayPackId || (theme.json.keyDisplayOverrides ? "overrides" : "-"),
      effects: effectSummary(theme.json.effects),
      dingul: theme.json.dingulColors ? "roles" : "-",
      coverage: coverageClass(theme),
      colorway: colorwayClass(theme),
      dimmed: dimmedSummary(theme),
      accentPolicy: accentPolicySummary(theme),
      overrides: `${Object.keys(theme.json.keyTextColorOverrides || theme.json.keyColorOverrides || {}).length}/${Object.keys(theme.json.keyBackgroundColorOverrides || {}).length}`,
      features: features.join(","),
      warnings: warningCounts.get(theme.file) || 0
    };
  });
}

function generatedHtmlReport(themes, diagnostics, outputFile) {
  const rows = reportRows(themes, diagnostics);
  const coverageLabels = new Map(themeContract.themeCoverageClasses.map(item => [item.id, item.label]));
  const colorwayLabels = new Map(themeContract.colorwayClasses.map(item => [item.id, item.label]));
  const generatedAt = new Date().toISOString();
  const reportDir = path.dirname(outputFile);
  const body = rows.map(row => {
    const previewPath = path.relative(reportDir, path.join(rootDir, "captures", row.preview)).replaceAll(path.sep, "/");
    return `<article class="theme-card">
      <header>
        <div>
          <h2>${escapeHtml(row.name)}</h2>
          <p>${escapeHtml(row.id)} · ${escapeHtml(row.file)}</p>
        </div>
        <span class="badge ${row.warnings > 0 ? "warn" : "ok"}">${row.warnings > 0 ? `${row.warnings} warning` : "clear"}</span>
      </header>
      <a href="${escapeHtml(previewPath)}"><img src="${escapeHtml(previewPath)}" alt="${escapeHtml(row.name)} preview"></a>
      <dl>
        <div><dt>Coverage</dt><dd><strong>${escapeHtml(row.coverage)}</strong> ${escapeHtml(coverageLabels.get(row.coverage) || "")}</dd></div>
        <div><dt>Colorway</dt><dd><strong>${escapeHtml(row.colorway)}</strong> ${escapeHtml(colorwayLabels.get(row.colorway) || "")}</dd></div>
        <div><dt>Surface</dt><dd>${escapeHtml(row.tone)} · ${escapeHtml(row.depth)}</dd></div>
        <div><dt>Packs</dt><dd>icon ${escapeHtml(row.icons)} · display ${escapeHtml(row.display)}</dd></div>
        <div><dt>Effects</dt><dd>${escapeHtml(row.effects)}</dd></div>
        <div><dt>Dimmed</dt><dd>${escapeHtml(row.dimmed)}</dd></div>
        <div><dt>Accent policy</dt><dd>${escapeHtml(row.accentPolicy)}</dd></div>
        <div><dt>Overrides</dt><dd>text/background ${escapeHtml(row.overrides)}</dd></div>
      </dl>
      <p class="tags">${escapeHtml(row.features).split(",").filter(Boolean).map(item => `<span>${item}</span>`).join("")}</p>
    </article>`;
  }).join("\n");
  return `<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>S3 Keyboard Theme Classification Report</title>
  <style>
    :root { color-scheme: light; font-family: "Segoe UI", Arial, sans-serif; color: #1f2328; background: #f4f5f7; }
    body { margin: 0; padding: 28px; }
    h1 { margin: 0 0 8px; font-size: 28px; letter-spacing: 0; }
    .intro { margin: 0 0 22px; color: #657080; }
    .legend, .grid { display: grid; gap: 14px; }
    .legend { grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); margin-bottom: 20px; }
    .legend section, .theme-card { background: #fff; border: 1px solid #d9dee6; border-radius: 8px; padding: 14px; }
    .legend h2 { margin: 0 0 8px; font-size: 14px; }
    .legend p { margin: 4px 0; color: #4d5664; font-size: 13px; }
    .grid { grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); }
    .theme-card header { display: flex; justify-content: space-between; align-items: start; gap: 12px; margin-bottom: 10px; }
    .theme-card h2 { margin: 0; font-size: 18px; letter-spacing: 0; }
    .theme-card header p { margin: 3px 0 0; color: #657080; font-size: 12px; }
    .theme-card img { width: 100%; max-height: 260px; object-fit: cover; object-position: top; border: 1px solid #e1e5eb; border-radius: 6px; background: #f8f9fb; }
    .badge { white-space: nowrap; border-radius: 999px; padding: 3px 8px; font-size: 12px; border: 1px solid; }
    .badge.ok { color: #1f6f43; background: #eaf7ee; border-color: #b9e2c5; }
    .badge.warn { color: #8a4b00; background: #fff5db; border-color: #f1d38d; }
    dl { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px 12px; margin: 12px 0; }
    dt { color: #657080; font-size: 11px; text-transform: uppercase; }
    dd { margin: 2px 0 0; font-size: 13px; }
    .tags { display: flex; flex-wrap: wrap; gap: 5px; margin: 0; }
    .tags span { background: #eef1f5; border: 1px solid #d9dee6; border-radius: 999px; padding: 2px 7px; font-size: 12px; color: #4d5664; }
  </style>
</head>
<body>
  <h1>S3 Keyboard Theme Classification Report</h1>
  <p class="intro">Generated ${escapeHtml(generatedAt)} from themes/*.json. Low contrast warnings are limited to primary alpha/mod readability risks.</p>
  <div class="legend">
    <section><h2>Coverage</h2>${themeContract.themeCoverageClasses.map(item => `<p><strong>${escapeHtml(item.id)}</strong> ${escapeHtml(item.label)}</p>`).join("")}</section>
    <section><h2>Colorway</h2>${themeContract.colorwayClasses.map(item => `<p><strong>${escapeHtml(item.id)}</strong> ${escapeHtml(item.label)}</p>`).join("")}</section>
  </div>
  <main class="grid">
${body}
  </main>
</body>
</html>
`;
}

function coverageClass(theme) {
  const backgroundOverrides = meaningfulBackgroundOverrideKeys(theme);
  const textOverrides = Object.keys(theme.json.keyTextColorOverrides || theme.json.keyColorOverrides || {})
    .filter(key => !themeContract.appearanceRequiredTextColorOverrides.includes(key));
  const overrides = backgroundOverrides.length > 0 ? [...backgroundOverrides, ...textOverrides] : [];
  const alphaOverrides = overrides.filter(isAlphaOverrideKey).length;
  const modOverrides = overrides.filter(key => !isAlphaOverrideKey(key)).length;
  if (alphaOverrides > 0 && modOverrides > 0) {
    return "5";
  }
  if (modOverrides > 0) {
    return "4.1";
  }
  if (alphaOverrides > 0) {
    return "4.2";
  }
  const rolePairs = distinctVisualRolePairs(theme);
  if (rolePairs >= 3) {
    return "3";
  }
  if (rolePairs >= 2) {
    return "2";
  }
  return "1";
}

function meaningfulBackgroundOverrideKeys(theme) {
  return Object.entries(theme.json.keyBackgroundColorOverrides || {})
    .filter(([key]) => !themeContract.appearanceRequiredTextColorOverrides.includes(key))
    .filter(([key, value]) => {
      const expected = expectedBackgroundForOverrideKey(theme, key);
      return !expected || !sameColor(expected, value);
    })
    .map(([key]) => key);
}

function expectedBackgroundForOverrideKey(theme, key) {
  const normalized = key.toLowerCase();
  const raw = normalized.startsWith("background:")
    ? normalized.slice("background:".length)
    : normalized;
  const dingul = theme.json.dingulColors || {};
  if (isAlphaLikeKey(raw) || raw === "space" || raw === "alpha") {
    return dingul.alpha?.background || theme.json.colors?.alphaKey;
  }
  return dingul.mod?.background || theme.json.colors?.modifierKey;
}

function colorwayClass(theme) {
  const backgroundOverrideCount = meaningfulBackgroundOverrideKeys(theme).length;
  if (theme.json.icons?.modifierPackId === "dots-lines"
      || backgroundOverrideCount >= 12
      || theme.json.metadata?.tags?.includes("dots")) {
    return "d";
  }
  const rolePairs = distinctVisualRolePairs(theme);
  if (rolePairs >= 3) {
    return "c";
  }
  if (rolePairs >= 2) {
    return "b";
  }
  return "a";
}

function dimmedSummary(theme) {
  const dingul = theme.json.dingulColors || {};
  const values = [];
  for (const [role, pair] of Object.entries(dingul)) {
    if (isColor(pair?.foreground) && isColor(pair?.background)) {
      const ratio = contrastRatio(pair.foreground, pair.background);
      if (ratio < themeContract.contrastPolicy.primaryMinimumRatio) {
        values.push(`${role}:${ratio.toFixed(2)}`);
      }
    }
  }
  return values.length === 0 ? "-" : values.join(",");
}

function accentPolicySummary(theme) {
  const policy = theme.json.accentPolicy;
  if (!policy) {
    return "-";
  }
  const parts = [];
  for (const layout of ["qwerty", "dingul"]) {
    if (Array.isArray(policy[layout]) && policy[layout].length > 0) {
      parts.push(`${layout}:${policy[layout].join("+")}`);
    }
  }
  const spacebar = policy.spacebar || policy.space;
  if (spacebar && spacebar !== "default") {
    parts.push(`spacebar:${spacebar}`);
  }
  const question = policy.question || policy.questionMark;
  if (question && question !== "default") {
    parts.push(`question:${question}`);
  }
  return parts.length === 0 ? "-" : parts.join(" ");
}

function distinctVisualRolePairs(theme) {
  const pairs = [];
  const dingul = theme.json.dingulColors || {};
  for (const role of ["alpha", "mod"]) {
    const pair = dingul[role];
    if (isColor(pair?.foreground) && isColor(pair?.background)) {
      pairs.push(`${pair.foreground.toUpperCase()}/${pair.background.toUpperCase()}`);
    }
  }
  const accentPair = authoredAccentPair(theme);
  if (accentPair) {
    pairs.push(accentPair);
  }
  if (pairs.length === 0 && theme.json.colors) {
    pairs.push(`${theme.json.colors.accent}/${theme.json.colors.alphaKey}`);
    pairs.push(`${theme.json.colors.secondary}/${theme.json.colors.modifierKey}`);
  }
  return new Set(pairs).size;
}

function authoredAccentPair(theme) {
  const colors = theme.json.colors || {};
  const dingul = theme.json.dingulColors || {};
  const background = colors.accentKey;
  if (!isColor(background)) {
    return null;
  }
  const primaryBackgrounds = [
    colors.alphaKey,
    colors.modifierKey
  ].filter(isColor);
  if (primaryBackgrounds.some(value => sameColor(value, background))) {
    return null;
  }
  if (primaryBackgrounds.length > 0
      && Math.min(...primaryBackgrounds.map(value => colorDistance(value, background))) < minAccentBackgroundDistance) {
    return null;
  }
  const foreground = isColor(dingul.modInv?.foreground)
    ? dingul.modInv.foreground
    : colors.accent;
  if (!isColor(foreground)) {
    return null;
  }
  return `${foreground.toUpperCase()}/${background.toUpperCase()}`;
}

function sameColor(left, right) {
  return isColor(left) && isColor(right) && left.toUpperCase() === right.toUpperCase();
}

function colorDistance(left, right) {
  const a = parseColor(left);
  const b = parseColor(right);
  if (!a || !b) {
    return 0;
  }
  return Math.hypot(a[0] - b[0], a[1] - b[1], a[2] - b[2]);
}

function parseColor(value) {
  if (!isColor(value)) {
    return null;
  }
  return [
    parseInt(value.slice(1, 3), 16),
    parseInt(value.slice(3, 5), 16),
    parseInt(value.slice(5, 7), 16)
  ];
}

function isAlphaOverrideKey(key) {
  const normalized = key.toLowerCase();
  const raw = normalized.startsWith("background:")
    ? normalized.slice("background:".length)
    : normalized;
  if (raw === "alpha" || raw === "space") {
    return true;
  }
  if (raw.startsWith("tap:")) {
    const value = raw.slice("tap:".length);
    return isAlphaLikeKey(value);
  }
  return isAlphaLikeKey(raw);
}

function isAlphaLikeKey(value) {
  return /^[a-z]$/i.test(value)
    || /^[0-9]$/.test(value)
    || /^[\u3131-\u318e\uac00-\ud7a3]$/.test(value)
    || value === "?";
}

function warningCountsByFile(warnings) {
  const counts = new Map();
  for (const warning of warnings) {
    const file = warning.split(":")[0];
    counts.set(file, (counts.get(file) || 0) + 1);
  }
  return counts;
}

function generatedJava(themes) {
  const entries = themes.map(theme => {
    const displayName = theme.json.name || theme.id;
    return `            new KeyboardThemePreset(
                    ${javaString(theme.id)},
                    ${javaString(displayName)},
${javaMultilineString(theme.raw, 20)}                    )`;
  }).join(",\n");
  return `package com.superl3.s3keyboard;

// Generated by tools/sync-themes.mjs. Do not edit by hand.
final class GeneratedKeyboardThemePresets {
    static final KeyboardThemePreset[] PRESETS = {
${entries}
    };

    private GeneratedKeyboardThemePresets() {
    }
}
`;
}

function generatedWebIndex(themes) {
  const entries = themes.map(theme => ({
    id: theme.id,
    name: theme.json.name,
    file: theme.file,
    url: `../themes/${theme.file}`,
    metadata: theme.json.metadata || {},
    theme: theme.json
  }));
  return `// Generated by ../tools/sync-themes.mjs. Do not edit by hand.
window.S3_THEME_INDEX = ${JSON.stringify(entries, null, 2)};
`;
}

function generatedWebContract() {
  return `// Generated by ../tools/sync-themes.mjs. Do not edit by hand.
window.S3_THEME_CONTRACT = ${JSON.stringify(webThemeContract(), null, 2)};
`;
}

function writeFileIfChanged(file, content) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  if (fs.existsSync(file) && fs.readFileSync(file, "utf8") === content) {
    return;
  }
  fs.writeFileSync(file, content, "utf8");
  console.log(`wrote ${relative(file)}`);
}

function migrateHints() {
  for (const file of fs.readdirSync(themesDir).filter(name => name.endsWith(".json")).sort()) {
    const filePath = path.join(themesDir, file);
    const theme = JSON.parse(fs.readFileSync(filePath, "utf8"));
    if (!theme.hints) {
      continue;
    }
    const migrated = {};
    let insertedMetadata = false;
    for (const [key, value] of Object.entries(theme)) {
      if (key === "hints") {
        continue;
      }
      if (!insertedMetadata && key === "colors") {
        migrated.metadata = {
          ...(theme.metadata || {}),
          recommendedHints: theme.hints
        };
        insertedMetadata = true;
      }
      if (key === "metadata") {
        migrated.metadata = {
          ...value,
          recommendedHints: theme.hints
        };
        insertedMetadata = true;
      } else {
        migrated[key] = value;
      }
    }
    if (!insertedMetadata) {
      migrated.metadata = {
        ...(theme.metadata || {}),
        recommendedHints: theme.hints
      };
    }
    writeFileIfChanged(filePath, `${JSON.stringify(migrated, null, 2)}\n`);
  }
}

function inferredFeatures(theme) {
  const json = theme.json;
  const metadataFeatures = [
    ...(json.metadata?.tags || []),
    ...(json.metadata?.features || [])
  ];
  const features = new Set(metadataFeatures);
  features.add(luminanceTone(json.colors?.keyboardBackground));
  if (json.shape?.depthEnabled) {
    features.add("depth");
  } else {
    features.add("flat");
  }
  if (json.icons?.modifierPackId) {
    features.add(json.icons.modifierPackId);
  }
  if (json.icons?.keyDisplayPackId) {
    features.add(json.icons.keyDisplayPackId);
  }
  if (json.keyDisplayOverrides) {
    features.add("displayOverrides");
  }
  if (json.effects?.blur?.enabled) {
    features.add("blur");
  }
  if (json.effects?.metal?.enabled) {
    features.add("metal");
  }
  if (json.effects?.keyFaceGradient?.enabled) {
    features.add("keyFaceGradient");
  }
  if (json.effects?.panelGradient?.enabled) {
    features.add("panelGradient");
  }
  if (json.effects?.previewBubble?.style === "angular") {
    features.add("angularPreview");
  }
  if (hasColorfulForegroundOverrides(theme)) {
    features.add("colorfulForeground");
  }
  const backgroundOverrideCount = Object.keys(json.keyBackgroundColorOverrides || {})
    .filter(key => !themeContract.appearanceRequiredTextColorOverrides.includes(key))
    .length;
  if (backgroundOverrideCount >= 12) {
    features.add("heavyPerKeyOverrides");
  }
  if ((json.name || "").toLowerCase().includes("gmk")) {
    features.add("gmkInspired");
  }
  return [...features].filter(Boolean).sort();
}

function hasColorfulForegroundOverrides(theme) {
  const overrides = Object.entries(theme.json.keyTextColorOverrides || theme.json.keyColorOverrides || {})
    .filter(([key, value]) => !themeContract.appearanceRequiredTextColorOverrides.includes(key) && isColor(value));
  if (overrides.length < 10) {
    return false;
  }
  const uniqueColors = new Set(overrides.map(([, value]) => value.toUpperCase()));
  return uniqueColors.size >= 6;
}

function effectSummary(effects) {
  const result = [];
  if (effects?.blur?.enabled) {
    result.push(`blur${effects.blur.radiusDp ?? ""}`);
  }
  if (effects?.metal?.enabled) {
    result.push(`metal${effects.metal.strengthPercent ?? ""}`);
  }
  if (effects?.keyFaceGradient?.enabled) {
    result.push(`keyGradient${effects.keyFaceGradient.strengthPercent ?? ""}`);
  }
  if (effects?.panelGradient?.enabled) {
    result.push("panelGradient");
  }
  if (effects?.previewBubble?.style === "angular") {
    result.push("angular");
  }
  return result.length === 0 ? "-" : result.join("+");
}

function luminanceTone(color) {
  if (!isColor(color)) {
    return "unknown";
  }
  return relativeLuminance(color) < 0.42 ? "dark" : "light";
}

function previewSlug(name) {
  return String(name)
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "") || "theme";
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;");
}

function contrastRatio(foreground, background) {
  const first = relativeLuminance(foreground);
  const second = relativeLuminance(background);
  const lighter = Math.max(first, second);
  const darker = Math.min(first, second);
  return (lighter + 0.05) / (darker + 0.05);
}

function relativeLuminance(color) {
  const [r, g, b] = rgb(color).map(channel => {
    const value = channel / 255;
    return value <= 0.03928 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4;
  });
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

function rgb(color) {
  const text = color.replace("#", "");
  return [
    Number.parseInt(text.slice(0, 2), 16),
    Number.parseInt(text.slice(2, 4), 16),
    Number.parseInt(text.slice(4, 6), 16)
  ];
}

function isColor(value) {
  return typeof value === "string" && /^#[0-9a-fA-F]{6}$/.test(value);
}

function isIntegerInRange(value, min, max) {
  return Number.isInteger(value) && value >= min && value <= max;
}

function isStringArray(value) {
  return Array.isArray(value) && value.every(item => typeof item === "string" && item.trim() !== "");
}

function javaMultilineString(value, indent) {
  const prefix = " ".repeat(indent);
  return value
    .replace(/\r\n/g, "\n")
    .split("\n")
    .map((line, index) => `${index === 0 ? prefix : `${prefix}+ `}"${escapeJava(line)}\\n"\n`)
    .join("");
}

function javaString(value) {
  return `"${escapeJava(value)}"`;
}

function escapeJava(value) {
  return String(value)
    .replace(/\\/g, "\\\\")
    .replace(/"/g, "\\\"")
    .replace(/\t/g, "\\t");
}

function relative(file) {
  return path.relative(rootDir, file).replace(/\\/g, "/");
}
