package com.superl3.s3keyboard;

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
            colors.put("keyIdle", colorToString(safeSettings.keyIdleColor));
            colors.put("functionKey", colorToString(safeSettings.functionKeyColor));
            colors.put("primaryFunctionKey", colorToString(safeSettings.primaryFunctionKeyColor));
            colors.put("accentKey", colorToString(safeSettings.accentKeyColor));
            colors.put("keyPressed", colorToString(safeSettings.keyPressedColor));
            colors.put("keyboardBackground", colorToString(safeSettings.keyboardBackgroundColor));
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
            shape.put("keyboardTopPaddingDp", safeSettings.keyboardTopPaddingDp);
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

            if (safeSettings.legendStylePreset != LegendStylePreset.DEFAULT) {
                JSONObject legendStyle = new JSONObject();
                legendStyle.put("preset", safeSettings.legendStylePreset.preferenceValue);
                root.put("legendStyle", legendStyle);
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

            return root.toString(2);
        } catch (JSONException exception) {
            throw new IllegalStateException("Failed to export keyboard theme.", exception);
        }
    }

    static KeyboardSettings importTheme(KeyboardSettings baseSettings, String json) {
        KeyboardSettings base = baseSettings == null ? KeyboardSettings.defaults() : baseSettings;
        try {
            JSONObject root = new JSONObject(json);
            int schemaVersion = root.optInt("schemaVersion", SCHEMA_VERSION);
            if (schemaVersion != SCHEMA_VERSION) {
                throw new IllegalArgumentException("Unsupported theme schemaVersion: " + schemaVersion);
            }

            JSONObject colors = root.optJSONObject("colors");
            JSONObject shape = root.optJSONObject("shape");
            JSONObject typography = root.optJSONObject("typography");
            JSONObject legendStyle = root.optJSONObject("legendStyle");
            JSONObject numberRow = root.optJSONObject("additionalNumberRow");
            JSONObject keyColorOverrides = root.optJSONObject("keyTextColorOverrides");
            if (keyColorOverrides == null) {
                keyColorOverrides = root.optJSONObject("keyColorOverrides");
            }
            JSONObject keyBackgroundColorOverrides = root.optJSONObject("keyBackgroundColorOverrides");

            boolean customDepthColor = base.customDepthColorEnabled;
            int depthColor = base.depthColor;
            if (colors != null && colors.has("depth")) {
                if (colors.isNull("depth")) {
                    customDepthColor = false;
                    depthColor = base.borderColor;
                } else {
                    customDepthColor = true;
                    depthColor = parseColor(colors.optString("depth"), base.depthColor);
                }
            }

            KeyboardSettings themed = base.withExtendedThemeColors(
                    color(colors, "keyIdle", base.keyIdleColor),
                    color(colors, "keyPressed", base.keyPressedColor),
                    color(colors, "keyboardBackground", base.keyboardBackgroundColor),
                    color(colors, "accent", base.accentColor),
                    color(colors, "secondary", base.secondaryColor),
                    color(colors, "functionKey", base.functionKeyColor),
                    color(colors, "primaryFunctionKey", base.primaryFunctionKeyColor),
                    color(colors, "accentKey", base.accentKeyColor),
                    color(colors, "border", base.borderColor),
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
                themed = themed.withLayoutSpacing(
                        themed.hangulMainSpecialGapDp,
                        shape.optInt("keyboardTopPaddingDp", themed.keyboardTopPaddingDp),
                        themed.keyboardBottomPaddingDp,
                        themed.bottomRowTopPaddingDp);
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

            Map<String, Integer> overrides = decodeKeyColorOverrides(keyColorOverrides);
            overrides.putAll(decodeKeyBackgroundColorOverrides(keyBackgroundColorOverrides));
            themed = themed.withKeyColorOverrides(overrides);
            return themed;
        } catch (JSONException exception) {
            throw new IllegalArgumentException("Invalid theme JSON.", exception);
        }
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
