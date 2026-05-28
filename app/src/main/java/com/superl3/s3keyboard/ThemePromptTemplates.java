package com.superl3.s3keyboard;

final class ThemePromptTemplates {
    private ThemePromptTemplates() {
    }

    static String keyboardImagePrompt(String sampleJson) {
        return String.join(
                "\n",
                "Use the attached keyboard or keycap image as visual reference and create a New Dingul Keyboard theme JSON.",
                "Return JSON only. Do not wrap it in markdown, do not add comments, and do not add explanation.",
                "",
                commonRules(),
                "",
                "Keyboard-image mapping rules:",
                "- Read the actual keycap color groups from the image.",
                "- Prefer alpha/mod/accent role colors before adding per-key overrides.",
                "- 2-tone = alpha + mod only. Do not invent an accent just to make the theme more colorful.",
                "- 3-tone = alpha + mod + a clearly distinct point keycap color.",
                "- If a whole visual group is accented, use accentPolicy targets instead of many exact key overrides.",
                "- Use keyTextColorOverrides or keyBackgroundColorOverrides only for isolated special keys.",
                "",
                roleGuide(),
                "",
                "Use this JSON shape as the starting point and replace values based on the image:",
                safeSampleJson(sampleJson));
    }

    static String paletteImagePrompt(String sampleJson) {
        return String.join(
                "\n",
                "Use the attached drawing, illustration, photo, poster, product image, or artwork as a color palette reference and create a New Dingul Keyboard theme JSON.",
                "Return JSON only. Do not wrap it in markdown, do not add comments, and do not add explanation.",
                "",
                commonRules(),
                "",
                "General-image palette mapping rules:",
                "- Extract 4 to 7 dominant colors from the image, then reduce them into readable keyboard roles.",
                "- alphaKey should be the broad typing surface color. Prefer a calm, readable neutral or the dominant low-saturation color.",
                "- modifierKey should be visibly different from alphaKey but still comfortable for repeated command keys.",
                "- accentKey should use the most memorable vivid color only when the image has a clear point color.",
                "- keyboardBackground and panelBackground should be a quiet backdrop derived from the image, not the loudest color.",
                "- Use effects.panelGradient only when the image suggests a quiet keyboard backplate gradient behind the keys.",
                "- accent is the main legend/icon color and must remain readable on alphaKey.",
                "- secondary is the sub legend/modifier legend color and must remain readable on modifierKey.",
                "- If the image is colorful but not keycap-like, do not create many per-key overrides. Keep the keyboard coherent.",
                "- Use effects.keyFaceGradient only for subtle surface depth; do not simulate the image texture. Minimal themes should disable it or keep strengthPercent at 12 or lower.",
                "",
                roleGuide(),
                "",
                "Use this JSON shape as the starting point and replace values based on the image palette:",
                safeSampleJson(sampleJson));
    }

    private static String commonRules() {
        return String.join(
                "\n",
                "Hard rules:",
                "- schemaVersion must be 1.",
                "- Use only #RRGGBB colors.",
                "- Do not include raster images, vector assets, icons, icon packs, layout settings, hint settings, or user preference fields.",
                "- Do not include explanatory prose in the response.",
                "- Preserve readability for alpha and modifier foreground/background pairs.",
                "- Allowed color keys: alphaKey, modifierKey, accentKey, keyPressed, keyboardBackground, panelBackground, border, depth, accent, secondary.",
                "- Allowed shape keys: roundnessDp, borderWidthDp, keyGapDp, depthEnabled, depthDp.",
                "- Allowed typography keys: fontFamily, primaryTextSizePercent, secondaryTextSizePercent, primaryTextBold, primaryTextItalic, secondaryTextBold, secondaryTextItalic.",
                "- Allowed fontFamily values: default, noto_sans_kr, noto_serif_kr, d2coding.",
                "- Allowed effects: effects.keyFaceGradient.enabled boolean, strengthPercent integer 0..100, startColor/endColor #RRGGBB, and curve linear|soft|top_glow|bottom_shade; effects.panelGradient.enabled boolean plus startColor/endColor #RRGGBB for the keyboard backplate.",
                "- Allowed number row color modes: full_alpha, half_mod_4567, alpha_accent, mod_alpha, full_mod, mod_accent, accent_alpha, accent_mod, full_accent.",
                "- Allowed accentPolicy targets: none, modEnter, modShift, dingulDot, dingulSlash, modCtrl, modMeta, modCommand, enter, settingsEnter, qwertyShift, backspace, question, escPoint, shift, punctuation, perKey.");
    }

    private static String roleGuide() {
        return String.join(
                "\n",
                "Role guide:",
                "- alphaKey: main typing key background.",
                "- modifierKey: command/modifier key background.",
                "- accentKey: point keycap background, only if a real third tone exists.",
                "- keyPressed: pressed key state.",
                "- border: key outline.",
                "- depth: fixed depth color; use null to auto-dim from each key background.",
                "- accent: main legend/icon color.",
                "- secondary: slide hint, sub legend, and muted modifier legend color.",
                "- dingulColors.alpha/mod/modInv should mirror the same alpha/mod/accent colorway for Dingul.",
                "- additionalNumberRow.colorMode controls only number-row appearance, not visibility.",
                "- keyTextColorOverrides and keyBackgroundColorOverrides are sparse escape hatches, not the main colorway.");
    }

    private static String safeSampleJson(String sampleJson) {
        if (sampleJson == null || sampleJson.trim().isEmpty()) {
            return "{\n"
                    + "  \"schemaVersion\": 1,\n"
                    + "  \"name\": \"Image Inspired Theme\",\n"
                    + "  \"author\": \"local\",\n"
                    + "  \"colors\": {\n"
                    + "    \"alphaKey\": \"#F8F8F8\",\n"
                    + "    \"modifierKey\": \"#E5E7EB\",\n"
                    + "    \"accentKey\": \"#E5E7EB\",\n"
                    + "    \"keyPressed\": \"#CBD5E1\",\n"
                    + "    \"keyboardBackground\": \"#D1D5DB\",\n"
                    + "    \"panelBackground\": \"#D1D5DB\",\n"
                    + "    \"border\": \"#9CA3AF\",\n"
                    + "    \"depth\": null,\n"
                    + "    \"accent\": \"#111827\",\n"
                    + "    \"secondary\": \"#6B7280\"\n"
                    + "  },\n"
                    + "  \"effects\": {\n"
                    + "    \"keyFaceGradient\": { \"enabled\": true, \"strengthPercent\": 22, \"startColor\": \"#FFFFFF\", \"endColor\": \"#000000\", \"curve\": \"soft\" },\n"
                    + "    \"panelGradient\": { \"enabled\": false, \"startColor\": \"#D1D5DB\", \"endColor\": \"#D1D5DB\" }\n"
                    + "  }\n"
                    + "}";
        }
        return sampleJson.trim();
    }
}
