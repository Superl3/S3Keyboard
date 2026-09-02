import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { themeContract } from "./theme-contract.mjs";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const expected = ["solid", "soft_keycap", "frosted", "acrylic"];
const contract = themeContract.materialStyles.map(item => item.id);

function fail(message) {
  console.error(`Material surface check failed: ${message}`);
  process.exitCode = 1;
}

function same(actual, label) {
  if (JSON.stringify(actual) !== JSON.stringify(expected)) {
    fail(`${label}: ${JSON.stringify(actual)} != ${JSON.stringify(expected)}`);
  }
}

same(contract, "theme contract");

const android = fs.readFileSync(path.join(root, "app/src/main/java/com/superl3/s3keyboard/KeyboardVisualEffects.java"), "utf8");
const androidOrder = [...android.matchAll(/MATERIAL_(SOLID|SOFT_KEYCAP|FROSTED|ACRYLIC)\s*,?/g)]
  .slice(0, 4)
  .map(match => match[1].toLowerCase());
same(androidOrder, "Android selector");

const web = fs.readFileSync(path.join(root, "web-theme-builder/app.js"), "utf8");
const webList = web.match(/return \["solid", "soft_keycap", "frosted", "acrylic"\]/);
if (!webList) fail("web normalizer does not expose exactly four materials");
if (!web.includes('parsed.effects?.materialStyle === "experimental_refraction"') ||
    !web.includes('parsed.effects.materialStyle = "frosted"')) {
  fail("web import-only legacy migration is missing");
}

const html = fs.readFileSync(path.join(root, "web-theme-builder/index.html"), "utf8");
const select = html.match(/<select id="materialStyle">([\s\S]*?)<\/select>/)?.[1] ?? "";
const webOptions = [...select.matchAll(/<option value="([^"]+)"/g)].map(match => match[1]);
same(webOptions, "web selector");

const preview = fs.readFileSync(path.join(root, "scripts/render-theme-previews.ps1"), "utf8");
for (const material of expected) {
  if (!preview.includes(`"${material}"`)) fail(`static preview missing ${material}`);
}
if (preview.includes("experimental_refraction")) fail("static preview exposes legacy material");

const json = fs.readFileSync(path.join(root, "app/src/main/java/com/superl3/s3keyboard/KeyboardThemeJson.java"), "utf8");
if (!json.includes('"experimental_refraction".equals(materialStyle)') ||
    !json.includes("materialStyle = KeyboardVisualEffects.MATERIAL_FROSTED")) {
  fail("Android import-only legacy migration is missing");
}

if (!process.exitCode) console.log(`Material surfaces aligned: ${expected.join(", ")}`);
