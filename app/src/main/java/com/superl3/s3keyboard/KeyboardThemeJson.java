package com.superl3.s3keyboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class KeyboardThemeJson {
    static final int SCHEMA_VERSION = 1;

    private KeyboardThemeJson() {
    }

    static String exportTheme(KeyboardSettings settings, String name, String author, String description) {
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        try {
            JSONObject root = new JSONObject();
            root.put("schemaVersion", SCHEMA_VERSION);
            root.put("name", emptyToDefault(name, "Untitled Theme"));
            if (author != null && !author.isEmpty()) {
                root.put("author", author);
            }
            if (description != null && !description.isEmpty()) {
                root.put("description", description);
            }

            JSONObject colors = new JSONObject();
            colors.put("alphaKey", colorToString(safeSettings.keyIdleColor));
            colors.put("modifierKey", colorToString(safeSettings.functionKeyColor));
            colors.put("accentKey", colorToString(safeSettings.accentKeyColor));
            colors.put("keyPressed", colorToString(safeSettings.keyPressedColor));
            colors.put("keyboardBackground", colorToString(safeSettings.keyboardBackgroundColor));
            colors.put("panelBackground", colorToString(safeSettings.keyboardBackgroundColor));
            colors.put("border", colorToString(safeSettings.borderColor));
            colors.put(
                    "depth",
                    safeSettings.customDepthColorEnabled
                            ? colorToString(safeSettings.depthColor)
                            : JSONObject.NULL);
            colors.put("accent", colorToString(safeSettings.accentColor));
            colors.put("secondary", colorToString(safeSettings.secondaryColor));
            root.put("colors", colors);

            JSONObject shape = new JSONObject();
            shape.put("roundnessDp", safeSettings.keyRoundnessDp);
            shape.put("borderWidthDp", safeSettings.keyBorderWidthDp);
            shape.put("keyGapDp", safeSettings.keyGapDp);
            shape.put("depthEnabled", safeSettings.keyDepthEnabled);
            shape.put("depthDp", safeSettings.keyDepthDp);
            root.put("shape", shape);

            JSONObject numberRow = new JSONObject();
            numberRow.put("colorMode", safeSettings.additionalNumberRowColorMode.preferenceValue);
            root.put("additionalNumberRow", numberRow);

            JSONObject typography = new JSONObject();
            typography.put("fontFamily", safeSettings.fontFamily);
            typography.put("primaryTextSizePercent", safeSettings.primaryTextSizePercent);
            typography.put("secondaryTextSizePercent", safeSettings.secondaryTextSizePercent);
            typography.put("primaryTextBold", safeSettings.primaryTextBold);
            typography.put("primaryTextItalic", safeSettings.primaryTextItalic);
            typography.put("secondaryTextBold", safeSettings.secondaryTextBold);
            typography.put("secondaryTextItalic", safeSettings.secondaryTextItalic);
            root.put("typography", typography);

            JSONObject icons = new JSONObject();
            if (!KeyboardSettings.DEFAULT_MODIFIER_ICON_PACK_ID.equals(safeSettings.modifierIconThemePackId)) {
                icons.put("modifierPackId", safeSettings.modifierIconThemePackId);
            }
            if (!KeyboardSettings.DEFAULT_KEY_DISPLAY_PACK_ID.equals(safeSettings.keyDisplayThemePackId)) {
                icons.put("keyDisplayPackId", safeSettings.keyDisplayThemePackId);
            }
            if (icons.length() > 0) {
                root.put("icons", icons);
            }
            if (safeSettings.visualEffects.hasExportableEffects()) {
                root.put("effects", encodeVisualEffectsObject(safeSettings.visualEffects));
            }

            if (!safeSettings.keyColorOverrides.isEmpty()) {
                JSONObject textOverrides = keyOverridesToJsonObject(safeSettings.keyColorOverrides, false);
                if (textOverrides.length() > 0) {
                    root.put("keyTextColorOverrides", textOverrides);
                }
                JSONObject backgroundOverrides = keyOverridesToJsonObject(safeSettings.keyColorOverrides, true);
                if (backgroundOverrides.length() > 0) {
                    root.put("keyBackgroundColorOverrides", backgroundOverrides);
                }
            }
            if (!safeSettings.keyDisplayOverrides.isEmpty()) {
                root.put("keyDisplayOverrides", encodeKeyDisplayOverridesObject(safeSettings.keyDisplayOverrides));
            }

            return root.toString(2);
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to export keyboard theme.", exception);
        }
    }

    static boolean locksUserAccentPlacement(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            JSONObject root = new JSONObject(json);
            JSONObject metadata = root.optJSONObject("metadata");
            if (containsMetadataToken(metadata, "colorfulForeground")
                    || containsMetadataToken(metadata, "colorfulModifier")
                    || containsMetadataToken(metadata, "colorfulModifiers")
                    || containsMetadataToken(metadata, "colorful")
                    || containsMetadataToken(metadata, "heavyPerKeyOverrides")) {
                return true;
            }
            JSONObject icons = root.optJSONObject("icons");
            String modifierPackId = icons == null ? "" : icons.optString("modifierPackId", "");
            String normalizedPackId = ModifierIconCatalog.normalizePackId(modifierPackId);
            if (ModifierIconCatalog.PACK_DOTS_LINES.equals(normalizedPackId)
                    || ModifierIconCatalog.isColoredPack(normalizedPackId)
                    || ModifierIconCatalog.PACK_METROPOLIS_POINTS.equals(normalizedPackId)) {
                return true;
            }
            JSONObject textOverrides = root.optJSONObject("keyTextColorOverrides");
            JSONObject backgroundOverrides = root.optJSONObject("keyBackgroundColorOverrides");
            int textCount = textOverrides == null ? 0 : textOverrides.length();
            int backgroundCount = backgroundOverrides == null ? 0 : backgroundOverrides.length();
            return textCount + backgroundCount >= 24;
        } catch (JSONException ex) {
            return false;
        }
    }

    private static boolean containsMetadataToken(JSONObject metadata, String token) {
        if (metadata == null || token == null) {
            return false;
        }
        return containsJsonArrayToken(metadata.optJSONArray("tags"), token)
                || containsJsonArrayToken(metadata.optJSONArray("features"), token);
    }

    private static boolean containsJsonArrayToken(JSONArray array, String token) {
        if (array == null) {
            return false;
        }
        for (int i = 0; i < array.length(); i++) {
            if (token.equals(array.optString(i))) {
                return true;
            }
        }
        return false;
    }

    static KeyboardSettings importTheme(KeyboardSettings baseSettings, String json) {
        KeyboardSettings base = baseSettings == null ? KeyboardSettings.defaults() : baseSettings;
        try {
            JSONObject root = new JSONObject(json);
            int schemaVersion = root.optInt("schemaVersion", SCHEMA_VERSION);
            if (schemaVersion != SCHEMA_VERSION) {
                throw new IllegalArgumentException("Unsupported theme schemaVersion: " + schemaVersion);
            }

            KeyboardSettings layeredBase = importThemeLayers(base, root);
            JSONObject colors = root.optJSONObject("colors");
            JSONObject shape = root.optJSONObject("shape");
            JSONObject typography = root.optJSONObject("typography");
            JSONObject legendStyle = root.optJSONObject("legendStyle");
            JSONObject icons = root.optJSONObject("icons");
            JSONObject numberRow = root.optJSONObject("additionalNumberRow");
            JSONObject keyColorOverrides = root.optJSONObject("keyTextColorOverrides");
            if (keyColorOverrides == null) {
                keyColorOverrides = root.optJSONObject("keyColorOverrides");
            }
            JSONObject keyBackgroundColorOverrides = root.optJSONObject("keyBackgroundColorOverrides");
            JSONObject dingulColors = root.optJSONObject("dingulColors");

            boolean customDepthColor = layeredBase.customDepthColorEnabled;
            int depthColor = layeredBase.depthColor;
            if (colors != null && colors.has("depth")) {
                if (colors.isNull("depth")) {
                    customDepthColor = false;
                    depthColor = layeredBase.borderColor;
                } else {
                    customDepthColor = true;
                    depthColor = parseColor(colors.optString("depth"), layeredBase.depthColor);
                }
            }

            KeyboardSettings themed = layeredBase.withExtendedThemeColors(
                    color(colors, "alphaKey", layeredBase.keyIdleColor),
                    color(colors, "keyPressed", layeredBase.keyPressedColor),
                    color(colors, "panelBackground", color(
                            colors,
                            "keyboardBackground",
                            layeredBase.keyboardBackgroundColor)),
                    color(colors, "accent", layeredBase.accentColor),
                    color(colors, "secondary", layeredBase.secondaryColor),
                    color(colors, "modifierKey", layeredBase.functionKeyColor),
                    color(colors, "accentKey", layeredBase.accentKeyColor),
                    color(colors, "border", layeredBase.borderColor),
                    customDepthColor,
                    depthColor);

            if (shape != null) {
                themed = themed
                        .withKeyRoundness(shape.optInt("roundnessDp", themed.keyRoundnessDp))
                        .withKeyBorderWidth(shape.optInt(
                                "borderWidthDp",
                                shape.optInt("outlineDensityDp", themed.keyBorderWidthDp)))
                        .withKeyGap(shape.optInt("keyGapDp", themed.keyGapDp))
                        .withKeyDepth(
                                shape.optBoolean("depthEnabled", themed.keyDepthEnabled),
                                shape.optInt("depthDp", themed.keyDepthDp));
            }

            if (typography != null) {
                themed = themed.withTypography(
                        typography.optString("fontFamily", themed.fontFamily),
                        typography.optInt(
                                "primaryTextSizePercent",
                                themed.primaryTextSizePercent),
                        typography.optInt(
                                "secondaryTextSizePercent",
                                themed.secondaryTextSizePercent),
                        typography.optBoolean("primaryTextBold", themed.primaryTextBold),
                        typography.optBoolean("primaryTextItalic", themed.primaryTextItalic),
                        typography.optBoolean("secondaryTextBold", themed.secondaryTextBold),
                        typography.optBoolean("secondaryTextItalic", themed.secondaryTextItalic));
            }

            if (numberRow != null) {
                themed = themed.withAdditionalNumberRowColorMode(AdditionalNumberRowColorMode.fromPreference(
                        numberRow.optString(
                                "colorMode",
                                themed.additionalNumberRowColorMode.preferenceValue)));
            }

            if (legendStyle != null) {
                themed = themed.withLegendStyle(LegendStylePreset.fromPreference(
                        legendStyle.optString(
                                "preset",
                                themed.legendStylePreset.preferenceValue)));
            }
            if (icons != null) {
                String modifierPackId = icons.optString(
                        "modifierPackId",
                        themed.modifierIconThemePackId);
                if (KeyDisplayOverridePackCatalog.isKnownNonEmptyPackId(modifierPackId)) {
                    themed = themed
                            .withModifierIconThemePack(ModifierIconCatalog.PACK_LINE_MONO)
                            .withKeyDisplayThemePack(modifierPackId);
                } else {
                    String importedBasePackId = importedModifierBasePackId(root, icons, modifierPackId);
                    themed = themed.withModifierIconThemePack(
                            importedBasePackId == null ? modifierPackId : importedBasePackId);
                }
                String keyDisplayPackId = icons.optString(
                        "keyDisplayPackId",
                        themed.keyDisplayThemePackId);
                if (hasImportedKeyDisplayPack(root, icons, keyDisplayPackId)) {
                    themed = themed.withKeyDisplayThemePack(KeyDisplayOverridePackCatalog.PACK_NONE);
                } else {
                    themed = themed.withKeyDisplayThemePack(keyDisplayPackId);
                }
            }

            Map<String, Integer> overrides = new HashMap<>(themed.keyColorOverrides);
            overrides.putAll(decodeDingulColors(dingulColors, themed));
            overrides.putAll(decodeAccentPolicy(
                    root.optJSONObject("accentPolicy"),
                    themed,
                    overrides,
                    keyBackgroundColorOverrides == null));
            overrides.putAll(decodeKeyColorOverrides(keyColorOverrides));
            overrides.putAll(decodeKeyBackgroundColorOverrides(keyBackgroundColorOverrides));
            themed = themed.withKeyColorOverrides(overrides);
            Map<String, KeyDisplayOverride> displayOverrides = new HashMap<>(themed.keyDisplayOverrides);
            Map<String, KeyDisplayOverride> importedDisplayOverrides = decodeKeyDisplayOverrides(
                    root.optJSONObject("keyDisplayOverrides"));
            importedDisplayOverrides.putAll(decodeImportedPackDisplayOverrides(root, icons));
            if (importedDisplayOverrides.isEmpty() && themed.legendStylePreset == LegendStylePreset.DOTS) {
                importedDisplayOverrides = legacyDotDisplayOverrides();
            }
            displayOverrides.putAll(importedDisplayOverrides);
            JSONObject effects = root.optJSONObject("effects");
            themed = themed
                    .withLegendStyle(LegendStylePreset.DEFAULT)
                    .withKeyDisplayOverrides(displayOverrides)
                    .withVisualEffects(decodeVisualEffects(effects, themed.visualEffects));
            return themed;
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid theme JSON.", exception);
        }
    }

    private static Map<String, KeyDisplayOverride> decodeImportedPackDisplayOverrides(
            JSONObject root,
            JSONObject icons) {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        if (root == null || icons == null) {
            return overrides;
        }

        String keyDisplayPackId = icons.optString("keyDisplayPackId", "");
        JSONObject keyDisplayPack = importedPackObject(root, icons, keyDisplayPackId, "keyDisplay");
        overrides.putAll(decodeImportedPackOverrides(keyDisplayPack));

        String modifierPackId = icons.optString("modifierPackId", "");
        JSONObject modifierPack = importedPackObject(root, icons, modifierPackId, "modifier");
        overrides.putAll(decodeImportedPackOverrides(modifierPack));
        return overrides;
    }

    private static Map<String, KeyDisplayOverride> decodeImportedPackOverrides(JSONObject pack) {
        if (pack == null) {
            return new HashMap<>();
        }
        JSONObject overrides = pack.optJSONObject("overrides");
        if (overrides == null) {
            overrides = pack.optJSONObject("keyDisplayOverrides");
        }
        if (overrides == null) {
            overrides = pack.optJSONObject("displayOverrides");
        }
        return decodeKeyDisplayOverrides(overrides);
    }

    private static boolean hasImportedKeyDisplayPack(JSONObject root, JSONObject icons, String packId) {
        return importedPackObject(root, icons, packId, "keyDisplay") != null;
    }

    private static String importedModifierBasePackId(JSONObject root, JSONObject icons, String packId) {
        JSONObject pack = importedPackObject(root, icons, packId, "modifier");
        if (pack == null) {
            return null;
        }
        String base = pack.optString("extends", "");
        if (base.isEmpty()) {
            base = pack.optString("basePackId", "");
        }
        if (base.isEmpty()) {
            base = pack.optString("renderer", "");
        }
        if (base.isEmpty()) {
            base = pack.optString("builtInRenderer", "");
        }
        if (base.isEmpty()) {
            return null;
        }
        String normalized = ModifierIconCatalog.normalizePackId(base);
        return ModifierIconCatalog.PACK_LINE_MONO.equals(normalized)
                && !ModifierIconCatalog.PACK_LINE_MONO.equals(base)
                ? null
                : normalized;
    }

    private static JSONObject importedPackObject(
            JSONObject root,
            JSONObject icons,
            String packId,
            String family) {
        if (root == null || icons == null || packId == null || packId.isEmpty()) {
            return null;
        }

        JSONObject embedded = icons.optJSONObject(family + "Pack");
        if (embedded != null && (!hasPackId(embedded) || matchesPackId(embedded, packId))) {
            return embedded;
        }

        JSONObject directCatalog = root.optJSONObject(family + "Packs");
        JSONObject directPack = objectById(directCatalog, packId);
        if (directPack != null) {
            return directPack;
        }

        JSONObject iconPacks = root.optJSONObject("iconPacks");
        if (iconPacks == null) {
            iconPacks = root.optJSONObject("decorativeGlyphCatalog");
        }
        if (iconPacks == null) {
            return null;
        }
        JSONObject familyCatalog = iconPacks.optJSONObject(family);
        if (familyCatalog == null) {
            familyCatalog = iconPacks.optJSONObject(family + "Packs");
        }
        JSONObject familyPack = objectById(familyCatalog, packId);
        return familyPack == null ? objectById(iconPacks, packId) : familyPack;
    }

    private static JSONObject objectById(JSONObject catalog, String packId) {
        if (catalog == null || packId == null || packId.isEmpty()) {
            return null;
        }
        JSONObject direct = catalog.optJSONObject(packId);
        if (direct != null) {
            return direct;
        }
        JSONArray packs = catalog.optJSONArray("packs");
        if (packs == null) {
            return null;
        }
        for (int i = 0; i < packs.length(); i++) {
            JSONObject candidate = packs.optJSONObject(i);
            if (matchesPackId(candidate, packId)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean matchesPackId(JSONObject object, String packId) {
        if (object == null || packId == null || packId.isEmpty()) {
            return false;
        }
        return packId.equals(object.optString("id", ""))
                || packId.equals(object.optString("packId", ""));
    }

    private static boolean hasPackId(JSONObject object) {
        return object != null && (object.has("id") || object.has("packId"));
    }

    static String colorToString(int color) {
        return String.format("#%06X", color & 0x00FFFFFF);
    }

    static String encodeKeyColorOverrides(Map<String, Integer> keyColorOverrides) {
        return keyOverridesToJsonObject(keyColorOverrides).toString();
    }

    static Map<String, Integer> decodeKeyColorOverrides(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return decodeKeyColorOverrides(new JSONObject(json));
        } catch (JSONException exception) {
            return new HashMap<>();
        }
    }

    static Map<String, Integer> decodeKeyColorOverrides(JSONObject object) {
        Map<String, Integer> overrides = new HashMap<>();
        if (object == null) {
            return overrides;
        }
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (object.isNull(key)) {
                continue;
            }
            int parsed = parseColor(object.optString(key), Integer.MIN_VALUE);
            if (parsed != Integer.MIN_VALUE) {
                overrides.put(key, parsed);
            }
        }
        return overrides;
    }

    static Map<String, Integer> decodeKeyBackgroundColorOverrides(JSONObject object) {
        Map<String, Integer> overrides = new HashMap<>();
        if (object == null) {
            return overrides;
        }
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (object.isNull(key)) {
                continue;
            }
            int parsed = parseColor(object.optString(key), Integer.MIN_VALUE);
            if (parsed != Integer.MIN_VALUE) {
                overrides.put("background:" + key, parsed);
            }
        }
        return overrides;
    }

    static Map<String, Integer> decodeDingulColors(JSONObject object, KeyboardSettings settings) {
        Map<String, Integer> overrides = new HashMap<>();
        if (object == null) {
            return overrides;
        }
        decodeDingulColorRole(object.opt("alpha"), "alpha", overrides, settings);
        decodeDingulColorRole(object.opt("mod"), "modifiers", overrides, settings);
        decodeDingulColorRole(object.opt("modifier"), "modifiers", overrides, settings);
        decodeDingulColorRole(object.opt("modInv"), "modInv", overrides, settings);
        decodeDingulColorRole(object.opt("mod_inv"), "modInv", overrides, settings);
        decodeDingulColorRole(object.opt("modifierInverted"), "modInv", overrides, settings);
        return overrides;
    }

    static Map<String, Integer> decodeAccentPolicy(
            JSONObject object,
            KeyboardSettings settings,
            Map<String, Integer> inheritedOverrides) {
        return decodeAccentPolicy(object, settings, inheritedOverrides, false);
    }

    static Map<String, Integer> decodeAccentPolicy(
            JSONObject object,
            KeyboardSettings settings,
            Map<String, Integer> inheritedOverrides,
            boolean useImplicitDefault) {
        Map<String, Integer> overrides = new HashMap<>();
        boolean distinctAccent = shouldUseImplicitAccentPolicy(settings);
        if (object == null) {
            if (useImplicitDefault && distinctAccent) {
                addAccentTargets(overrides, new JSONArray().put("modMeta"), false, settings, inheritedOverrides);
                addAccentTargets(overrides, new JSONArray().put("qwertyShift").put("backspace"), false, settings, inheritedOverrides);
                addAccentTargets(overrides, new JSONArray().put("modCtrl").put("dingulDot"), true, settings, inheritedOverrides);
            }
            return overrides;
        }
        addSingleKeyRolePolicy(
                overrides,
                object,
                settings,
                inheritedOverrides,
                distinctAccent,
                "space",
                new String[]{"spacebar", "space"});
        addSingleKeyRolePolicy(
                overrides,
                object,
                settings,
                inheritedOverrides,
                distinctAccent,
                "?",
                new String[]{"question", "questionMark"});
        if (!distinctAccent) {
            return overrides;
        }
        JSONArray qwertyTargets = object.optJSONArray("qwerty");
        JSONArray dingulTargets = object.optJSONArray("dingul");
        if (useImplicitDefault) {
            if (qwertyTargets == null) {
                qwertyTargets = new JSONArray().put("modMeta").put("qwertyShift").put("backspace");
            }
            if (dingulTargets == null) {
                dingulTargets = new JSONArray().put("modCtrl").put("dingulDot");
            }
        }
        addAccentTargets(overrides, qwertyTargets, false, settings, inheritedOverrides);
        addAccentTargets(overrides, dingulTargets, true, settings, inheritedOverrides);
        return overrides;
    }

    private static boolean shouldUseImplicitAccentPolicy(KeyboardSettings settings) {
        int accentKey = settings.accentKeyColor;
        return colorDistance(accentKey, settings.keyIdleColor) >= 48
                && colorDistance(accentKey, settings.functionKeyColor) >= 48;
    }

    private static double colorDistance(int left, int right) {
        int lr = (left >> 16) & 0xFF;
        int lg = (left >> 8) & 0xFF;
        int lb = left & 0xFF;
        int rr = (right >> 16) & 0xFF;
        int rg = (right >> 8) & 0xFF;
        int rb = right & 0xFF;
        return Math.sqrt(
                Math.pow(lr - rr, 2)
                        + Math.pow(lg - rg, 2)
                        + Math.pow(lb - rb, 2));
    }

    private static void addAccentTargets(
            Map<String, Integer> overrides,
            JSONArray targets,
            boolean dingul,
            KeyboardSettings settings,
            Map<String, Integer> inheritedOverrides) {
        if (targets == null) {
            return;
        }
        int foreground = accentPolicyForeground(settings, inheritedOverrides);
        int background = accentPolicyBackground(settings, inheritedOverrides);
        for (int index = 0; index < targets.length(); index++) {
            for (String key : accentPolicyKeys(targets.optString(index, ""), dingul, settings)) {
                overrides.put(key, foreground);
                overrides.put("background:" + key, background);
            }
        }
    }

    private static int accentPolicyForeground(
            KeyboardSettings settings,
            Map<String, Integer> inheritedOverrides) {
        Integer modInv = inheritedOverrides.get("modInv");
        if (modInv == null) {
            modInv = inheritedOverrides.get("modinv");
        }
        if (modInv != null) {
            return modInv;
        }
        Integer modifierInverted = inheritedOverrides.get("modifierinverted");
        if (modifierInverted == null) {
            modifierInverted = settings.keyColorOverrides.get("modifierinverted");
        }
        if (modifierInverted != null) {
            return modifierInverted;
        }
        return settings.accentColor;
    }

    private static int accentPolicyBackground(
            KeyboardSettings settings,
            Map<String, Integer> inheritedOverrides) {
        Integer modInv = inheritedOverrides.get("background:modInv");
        if (modInv == null) {
            modInv = inheritedOverrides.get("background:modinv");
        }
        if (modInv != null) {
            return modInv;
        }
        Integer modifierInverted = inheritedOverrides.get("background:modifierinverted");
        if (modifierInverted == null) {
            modifierInverted = settings.keyColorOverrides.get("background:modifierinverted");
        }
        return modifierInverted == null ? settings.accentKeyColor : modifierInverted;
    }

    private static String[] accentPolicyKeys(String target, boolean dingul, KeyboardSettings settings) {
        if ("modCtrl".equals(target) || "settingsEnter".equals(target)) {
            return new String[]{"options", "settings", "enter"};
        }
        if ("modMeta".equals(target)) {
            return new String[]{"reserved", "language"};
        }
        if ("modCommand".equals(target)) {
            return new String[]{"shift", "backspace"};
        }
        if ("qwertyShift".equals(target) || (!dingul && "shift".equals(target))) {
            return new String[]{"shift"};
        }
        if ("backspace".equals(target)) {
            return new String[]{"backspace"};
        }
        if (dingul && "modEnter".equals(target)) {
            return new String[]{"."};
        }
        if (dingul && "modShift".equals(target)) {
            return new String[]{"/"};
        }
        if (dingul && "dingulDot".equals(target)) {
            return new String[]{"."};
        }
        if (dingul && "dingulSlash".equals(target)) {
            return new String[]{"/"};
        }
        if (dingul && "question".equals(target)) {
            return new String[]{"?"};
        }
        if ("escPoint".equals(target) || "esc_point".equals(target)) {
            boolean hasNumberRow = settings != null
                    && (dingul ? settings.showHangulNumberRow : settings.showEnglishNumberRow);
            if (hasNumberRow) {
                return new String[]{"1"};
            }
            return dingul ? new String[]{"\u3131"} : new String[]{"q"};
        }
        if (dingul && "punctuation".equals(target)) {
            return new String[]{".", "/"};
        }
        return new String[0];
    }

    private static void addSingleKeyRolePolicy(
            Map<String, Integer> overrides,
            JSONObject object,
            KeyboardSettings settings,
            Map<String, Integer> inheritedOverrides,
            boolean distinctAccent,
            String key,
            String[] aliases) {
        String role = "";
        for (String alias : aliases) {
            role = firstNonEmpty(role, object.optString(alias, ""));
        }
        if (role.isEmpty() || "default".equals(role) || "theme".equals(role)) {
            return;
        }
        int foreground;
        int background;
        if ("alpha".equals(role)) {
            foreground = semanticColor(inheritedOverrides, "alpha", settings.accentColor, false);
            background = semanticColor(inheritedOverrides, "alpha", settings.keyIdleColor, true);
        } else if ("mod".equals(role) || "modifier".equals(role) || "modifiers".equals(role)) {
            foreground = semanticColor(inheritedOverrides, "modifiers", settings.secondaryColor, false);
            background = semanticColor(inheritedOverrides, "modifiers", settings.functionKeyColor, true);
        } else if ("accent".equals(role)) {
            if (!distinctAccent) {
                return;
            }
            foreground = accentPolicyForeground(settings, inheritedOverrides);
            background = accentPolicyBackground(settings, inheritedOverrides);
        } else {
            return;
        }
        overrides.put(key, foreground);
        overrides.put("background:" + key, background);
    }

    private static int semanticColor(
            Map<String, Integer> overrides,
            String role,
            int fallback,
            boolean background) {
        String normalized = KeyboardSettings.normalizeKeyOverrideName(role);
        Integer color = overrides.get(KeyboardSettings.normalizeKeyOverrideName(
                background ? "background:" + normalized : normalized));
        if (color == null && background) {
            color = overrides.get(KeyboardSettings.normalizeKeyOverrideName("bg:" + normalized));
        }
        return color == null ? fallback : color;
    }

    private static void decodeDingulColorRole(
            Object roleValue,
            String overrideKey,
            Map<String, Integer> overrides,
            KeyboardSettings settings) {
        if (roleValue == null || roleValue == JSONObject.NULL) {
            return;
        }
        if (roleValue instanceof JSONObject) {
            JSONObject object = (JSONObject) roleValue;
            int foreground = parseThemeColor(
                    firstNonEmpty(
                            object.optString("foreground", ""),
                            object.optString("text", ""),
                            object.optString("fg", "")),
                    settings,
                    Integer.MIN_VALUE);
            if (foreground != Integer.MIN_VALUE) {
                overrides.put(overrideKey, foreground);
            }
            int background = parseThemeColor(
                    firstNonEmpty(
                            object.optString("background", ""),
                            object.optString("key", ""),
                            object.optString("bg", "")),
                    settings,
                    Integer.MIN_VALUE);
            if (background != Integer.MIN_VALUE) {
                overrides.put("background:" + overrideKey, background);
            }
            return;
        }
        int foreground = parseThemeColor(String.valueOf(roleValue), settings, Integer.MIN_VALUE);
        if (foreground != Integer.MIN_VALUE) {
            overrides.put(overrideKey, foreground);
        }
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private static int parseThemeColor(String value, KeyboardSettings settings, int fallback) {
        if (value == null || value.isEmpty()) {
            return fallback;
        }
        int parsed = parseColor(value, Integer.MIN_VALUE);
        if (parsed != Integer.MIN_VALUE) {
            return parsed;
        }
        KeyboardSettings safeSettings = settings == null ? KeyboardSettings.defaults() : settings;
        switch (value) {
            case "alphaKey":
                return safeSettings.keyIdleColor;
            case "modifierKey":
            case "modKey":
                return safeSettings.functionKeyColor;
            case "accentKey":
            case "modInvKey":
                return safeSettings.accentKeyColor;
            case "accent":
            case "alpha":
                return safeSettings.accentColor;
            case "secondary":
            case "mod":
                return safeSettings.secondaryColor;
            case "keyboardBackground":
            case "panelBackground":
                return safeSettings.keyboardBackgroundColor;
            case "border":
                return safeSettings.borderColor;
            default:
                return fallback;
        }
    }

    static Map<String, KeyDisplayOverride> decodeKeyDisplayOverrides(JSONObject object) {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        if (object == null) {
            return overrides;
        }
        putDisplayOverride(overrides, "alpha", object.optJSONObject("alpha"));
        putDisplayOverride(overrides, "modifiers", object.optJSONObject("modifiers"));

        JSONObject keys = object.optJSONObject("keys");
        if (keys != null) {
            Iterator<String> names = keys.keys();
            while (names.hasNext()) {
                String key = names.next();
                putDisplayOverride(overrides, key, keys.optJSONObject(key));
            }
        } else {
            Iterator<String> names = object.keys();
            while (names.hasNext()) {
                String key = names.next();
                if (!"alpha".equals(key) && !"modifiers".equals(key)) {
                    putDisplayOverride(overrides, key, object.optJSONObject(key));
                }
            }
        }
        return overrides;
    }

    private static KeyboardSettings importThemeLayers(KeyboardSettings base, JSONObject root) {
        KeyboardSettings layered = base;
        JSONArray layers = root.optJSONArray("layers");
        if (layers == null) {
            layers = root.optJSONArray("themeLayers");
        }
        if (layers == null) {
            String baseTheme = root.optString("extends", "");
            if (baseTheme.isEmpty()) {
                baseTheme = root.optString("baseTheme", "");
            }
            return applyThemeLayer(layered, baseTheme);
        }
        for (int i = 0; i < layers.length(); i++) {
            Object layer = layers.opt(i);
            if (layer instanceof JSONObject) {
                layered = importTheme(layered, ((JSONObject) layer).toString());
            } else if (layer != null) {
                layered = applyThemeLayer(layered, String.valueOf(layer));
            }
        }
        return layered;
    }

    private static KeyboardSettings applyThemeLayer(KeyboardSettings base, String layerId) {
        if (layerId == null || layerId.isEmpty()) {
            return base;
        }
        KeyboardThemePreset preset = KeyboardThemePreset.find(layerId);
        return preset == null ? base : preset.applyTo(base);
    }

    static JSONObject encodeKeyDisplayOverridesObject(Map<String, KeyDisplayOverride> overrides) {
        JSONObject object = new JSONObject();
        JSONObject keys = new JSONObject();
        if (overrides == null || overrides.isEmpty()) {
            return object;
        }
        try {
            for (Map.Entry<String, KeyDisplayOverride> entry : overrides.entrySet()) {
                String key = entry.getKey();
                KeyDisplayOverride override = entry.getValue();
                if (key == null || override == null || override.value.isEmpty()) {
                    continue;
                }
                if ("alpha".equals(key) || "modifiers".equals(key)) {
                    object.put(key, displayOverrideToJson(override));
                } else {
                    keys.put(key, displayOverrideToJson(override));
                }
            }
            if (keys.length() > 0) {
                object.put("keys", keys);
            }
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to encode key display overrides.", exception);
        }
        return object;
    }

    private static void putDisplayOverride(
            Map<String, KeyDisplayOverride> overrides,
            String key,
            JSONObject object) {
        if (object == null) {
            return;
        }
        KeyDisplayOverride override = KeyDisplayOverride.create(
                object.optString("type", KeyDisplayOverride.TYPE_ICON),
                object.optString("value", ""));
        if (override != null) {
            overrides.put(key, override);
        }
    }

    private static JSONObject displayOverrideToJson(KeyDisplayOverride override) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("type", override.type);
        object.put("value", override.value);
        return object;
    }

    static JSONObject encodeVisualEffectsObject(KeyboardVisualEffects effects) {
        KeyboardVisualEffects safeEffects = effects == null ? KeyboardVisualEffects.DEFAULT : effects;
        JSONObject object = new JSONObject();
        try {
            JSONObject blur = new JSONObject();
            blur.put("enabled", safeEffects.blurEnabled);
            blur.put("radiusDp", safeEffects.blurRadiusDp);
            object.put("blur", blur);

            JSONObject metal = new JSONObject();
            metal.put("enabled", safeEffects.metallicEnabled);
            metal.put("strengthPercent", safeEffects.metallicStrengthPercent);
            object.put("metal", metal);

            JSONObject previewBubble = new JSONObject();
            previewBubble.put("style", safeEffects.angularPreviewBubble ? "angular" : "rounded");
            object.put("previewBubble", previewBubble);
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to encode visual effects.", exception);
        }
        return object;
    }

    static KeyboardVisualEffects decodeVisualEffects(JSONObject object, KeyboardVisualEffects fallback) {
        KeyboardVisualEffects safeFallback = fallback == null ? KeyboardVisualEffects.DEFAULT : fallback;
        if (object == null) {
            return safeFallback;
        }
        JSONObject blur = object.optJSONObject("blur");
        JSONObject metal = object.optJSONObject("metal");
        JSONObject previewBubble = object.optJSONObject("previewBubble");
        boolean blurEnabled = blur == null
                ? object.optBoolean("blurEnabled", safeFallback.blurEnabled)
                : blur.optBoolean("enabled", safeFallback.blurEnabled);
        int blurRadiusDp = blur == null
                ? object.optInt("blurRadiusDp", safeFallback.blurRadiusDp)
                : blur.optInt("radiusDp", safeFallback.blurRadiusDp);
        boolean metallicEnabled = metal == null
                ? object.optBoolean("metallicEnabled", safeFallback.metallicEnabled)
                : metal.optBoolean("enabled", safeFallback.metallicEnabled);
        int metallicStrength = metal == null
                ? object.optInt("metallicStrengthPercent", safeFallback.metallicStrengthPercent)
                : metal.optInt("strengthPercent", safeFallback.metallicStrengthPercent);
        String previewStyle = previewBubble == null
                ? object.optString(
                        "previewBubbleStyle",
                        safeFallback.angularPreviewBubble ? "angular" : "rounded")
                : previewBubble.optString(
                        "style",
                        safeFallback.angularPreviewBubble ? "angular" : "rounded");
        return new KeyboardVisualEffects(
                blurEnabled,
                blurRadiusDp,
                metallicEnabled,
                metallicStrength,
                !"rounded".equals(previewStyle));
    }

    private static Map<String, KeyDisplayOverride> legacyDotDisplayOverrides() {
        Map<String, KeyDisplayOverride> overrides = new HashMap<>();
        overrides.put("alpha", KeyDisplayOverride.icon(ModifierIconCatalog.GLYPH_DOT));
        return overrides;
    }

    private static String emptyToDefault(String value, String defaultValue) {
        return value == null || value.isEmpty() ? defaultValue : value;
    }

    private static int color(JSONObject object, String key, int fallback) {
        if (object == null || !object.has(key) || object.isNull(key)) {
            return fallback;
        }
        return parseColor(object.optString(key), fallback);
    }

    private static JSONObject keyOverridesToJsonObject(Map<String, Integer> keyColorOverrides) {
        return keyOverridesToJsonObject(keyColorOverrides, false);
    }

    private static JSONObject keyOverridesToJsonObject(Map<String, Integer> keyColorOverrides, boolean background) {
        JSONObject object = new JSONObject();
        if (keyColorOverrides == null || keyColorOverrides.isEmpty()) {
            return object;
        }
        try {
            for (Map.Entry<String, Integer> entry : keyColorOverrides.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    String key = entry.getKey();
                    String outputKey = backgroundOutputKey(key);
                    if (background && outputKey != null) {
                        object.put(outputKey, colorToString(entry.getValue()));
                    } else if (!background && outputKey == null) {
                        object.put(key, colorToString(entry.getValue()));
                    }
                }
            }
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to encode key color overrides.", exception);
        }
        return object;
    }

    private static String backgroundOutputKey(String key) {
        if (key == null) {
            return null;
        }
        String normalized = KeyboardSettings.normalizeKeyOverrideName(key);
        if (normalized.startsWith("background:")) {
            return key.substring(Math.min(key.length(), "background:".length()));
        }
        if (normalized.startsWith("bg:")) {
            return key.substring(Math.min(key.length(), "bg:".length()));
        }
        return null;
    }

    private static int parseColor(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        String text = value.trim();
        if (text.startsWith("#")) {
            text = text.substring(1);
        }
        if (text.length() != 6 && text.length() != 8) {
            return fallback;
        }
        try {
            long parsed = Long.parseLong(text, 16);
            if (text.length() == 6) {
                return 0xFF000000 | (int) parsed;
            }
            return 0xFF000000 | ((int) parsed & 0x00FFFFFF);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
