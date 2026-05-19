package com.academic.hangulgestureime;

final class KeyboardThemePreset {
    static final KeyboardThemePreset[] PRESETS = {
            theme("ios-clean-light", "iOS Clean Light",
                    "FBFBFD", "EEF0F4", "E4E7ED", "CCD3DE", "D6D8DD",
                    "D1D5DB", "C2C7D0", null, "111827", "707780",
                    5, 5, false, 0, KeyboardSettings.FONT_NOTO_SANS_KR, false, false),
            theme("ios-clean-dark", "iOS Clean Dark",
                    "2B2D31", "3A3D43", "202328", "202328", "5A5E66",
                    "1D1F23", "202328", null, "F8FAFC", "B8BEC8",
                    5, 5, false, 0, KeyboardSettings.FONT_NOTO_SANS_KR, false, false),
            theme("macos-frost-light", "macOS Frost Light",
                    "F6F7F9", "E7EAEE", "DCE2EA", "DCE2EA", "C9D2DC",
                    "EEF1F4", "B8C0CA", "B8C0CA", "1F2937", "667085",
                    4, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, false),
            theme("macos-graphite-dark", "macOS Graphite Dark",
                    "30343A", "3B414A", "252B32", "252B32", "596271",
                    "20242A", "15191F", "15191F", "F4F7FA", "AEB7C4",
                    4, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, false),
            theme("android-material-light", "Android Material Light",
                    "FFFBFE", "ECE6F0", "E1DCE8", "E1DCE8", "D0C4DB",
                    "F3EDF7", "CAC4D0", null, "1D1B20", "625B71",
                    7, 5, false, 0, KeyboardSettings.FONT_NOTO_SANS_KR, true, false),
            theme("android-material-dark", "Android Material Dark",
                    "211F26", "2B2930", "17151B", "17151B", "4A4458",
                    "141218", "49454F", null, "E6E1E5", "CAC4D0",
                    7, 5, false, 0, KeyboardSettings.FONT_NOTO_SANS_KR, true, false),
            theme("paper-mono-flat", "Paper Mono Flat",
                    "FAFAFA", "EFEFEF", "E5E5E5", "E5E5E5", "DADADA",
                    "F4F4F4", "D0D0D0", null, "111111", "777777",
                    3, 6, false, 0, KeyboardSettings.FONT_D2CODING, false, false),
            theme("amoled-black", "AMOLED Black",
                    "050505", "111111", "000000", "000000", "2D2D2D",
                    "000000", "303030", null, "F7F7F7", "A7A7A7",
                    4, 5, false, 0, KeyboardSettings.FONT_NOTO_SANS_KR, false, false),
            theme("nord-snow", "Nord Snow",
                    "F8FAFC", "EEF2F7", "E4EAF2", "E4EAF2", "D8DEE9",
                    "E5E9F0", "B8C2CC", "B8C2CC", "2E3440", "667085",
                    5, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, false),
            theme("nord-night", "Nord Night",
                    "2E3440", "3B4252", "252B35", "252B35", "4C566A",
                    "242933", "1E2430", "1E2430", "ECEFF4", "C7D0DC",
                    5, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, false),
            theme("slate-glass", "Slate Glass",
                    "F7F9FC", "E9EEF5", "DFE7F0", "DFE7F0", "D7E2EF",
                    "EAF0F6", "AEB8C6", null, "0F172A", "64748B",
                    8, 5, false, 0, KeyboardSettings.FONT_NOTO_SANS_KR, false, false),
            theme("graphite-mono", "Graphite Mono",
                    "F5F5F5", "E8E8E8", "DDDDDD", "DDDDDD", "CFCFCF",
                    "EDEDED", "B8B8B8", "B8B8B8", "161616", "686868",
                    4, 6, true, 1, KeyboardSettings.FONT_D2CODING, false, false),
            theme("high-contrast-light", "High Contrast Light",
                    "FFFFFF", "EFEFEF", "E0E0E0", "E0E0E0", "C8D7EA",
                    "F3F4F6", "111111", null, "000000", "424242",
                    5, 6, false, 0, KeyboardSettings.FONT_NOTO_SANS_KR, true, false),
            theme("gmk-bento", "GMK Bento Inspired",
                    "E9DCC9", "D9C6B1", "C96F62", "8FB9BE", "D7BDA7",
                    "C7B49F", "927E6F", "927E6F", "2E5F6A", "B7645F",
                    5, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, true),
            theme("gmk-oblivion", "GMK Oblivion Inspired",
                    "4B5158", "393F46", "2F343B", "35463B", "343A41",
                    "1F2328", "171B20", "171B20", "F0EEE7", "AEB8C4",
                    4, 5, true, 2, KeyboardSettings.FONT_D2CODING, true, true),
            theme("gmk-8008", "GMK 8008 Inspired",
                    "666E79", "505864", "38404A", "3F4853", "454D58",
                    "2A3038", "20262D", "20262D", "F2D7DF", "91B8D5",
                    5, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, true),
            theme("gmk-hammerhead", "GMK HammerHead Inspired",
                    "233B42", "1A3037", "12252C", "17343A", "173139",
                    "0E1B20", "0A1519", "0A1519", "C6F3EC", "62B7BA",
                    5, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, true),
            theme("gmk-dracula", "GMK Dracula Inspired",
                    "44475A", "343746", "282A36", "363949", "50546A",
                    "1E2029", "191B22", "191B22", "F8F8F2", "BD93F9",
                    5, 5, true, 2, KeyboardSettings.FONT_D2CODING, true, true),
            theme("gmk-modern-dolch", "GMK Modern Dolch Inspired",
                    "C9CDD2", "AEB5BE", "8F98A4", "75AEB0", "A6ADB6",
                    "737E8A", "5F6873", "5F6873", "20252C", "4E5964",
                    5, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, true),
            theme("marigold-fiesta-dark", "Marigold Fiesta Dark",
                    "202225", "2A2C31", "111318", "111318", "3C4048",
                    "111214", "45484F", "2F3339", "F8F1DF", "B8A9BF",
                    4, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, true,
                    marigoldFiestaDarkOverrides()),
            theme("marigold-fiesta-light", "Marigold Fiesta Light",
                    "FAFAF7", "F1F0EC", "E8E7E2", "E8E7E2", "DAD8D1",
                    "F3F2EF", "B9B7B0", "CCC9C2", "25201C", "64605A",
                    4, 5, true, 2, KeyboardSettings.FONT_NOTO_SANS_KR, true, true,
                    marigoldFiestaLightOverrides())
    };

    final String id;
    final String displayName;
    final String json;

    private KeyboardThemePreset(String id, String displayName, String json) {
        this.id = id;
        this.displayName = displayName;
        this.json = json;
    }

    KeyboardSettings applyTo(KeyboardSettings base) {
        return KeyboardThemeJson.importTheme(base, json);
    }

    static KeyboardThemePreset find(String id) {
        if (id == null) {
            return null;
        }
        for (KeyboardThemePreset preset : PRESETS) {
            if (preset.id.equals(id)) {
                return preset;
            }
        }
        return null;
    }

    private static KeyboardThemePreset theme(
            String id,
            String displayName,
            String keyIdle,
            String functionKey,
            String primaryFunctionKey,
            String accentKey,
            String keyPressed,
            String keyboardBackground,
            String border,
            String depth,
            String accent,
            String secondary,
            int roundnessDp,
            int keyGapDp,
            boolean depthEnabled,
            int depthDp,
            String fontFamily,
            boolean showHangulSlideHints,
            boolean showEnglishSlideHints) {
        return theme(
                id,
                displayName,
                keyIdle,
                functionKey,
                primaryFunctionKey,
                accentKey,
                keyPressed,
                keyboardBackground,
                border,
                depth,
                accent,
                secondary,
                roundnessDp,
                keyGapDp,
                depthEnabled,
                depthDp,
                fontFamily,
                showHangulSlideHints,
                showEnglishSlideHints,
                null);
    }

    private static KeyboardThemePreset theme(
            String id,
            String displayName,
            String keyIdle,
            String functionKey,
            String primaryFunctionKey,
            String accentKey,
            String keyPressed,
            String keyboardBackground,
            String border,
            String depth,
            String accent,
            String secondary,
            int roundnessDp,
            int keyGapDp,
            boolean depthEnabled,
            int depthDp,
            String fontFamily,
            boolean showHangulSlideHints,
            boolean showEnglishSlideHints,
            String keyColorOverridesJson) {
        return new KeyboardThemePreset(
                id,
                displayName,
                "{"
                        + "\"schemaVersion\":1,"
                        + "\"name\":\"" + displayName + "\","
                        + "\"author\":\"local\","
                        + "\"colors\":{"
                        + "\"keyIdle\":\"#" + keyIdle + "\","
                        + "\"functionKey\":\"#" + functionKey + "\","
                        + "\"primaryFunctionKey\":\"#" + primaryFunctionKey + "\","
                        + "\"accentKey\":\"#" + accentKey + "\","
                        + "\"keyPressed\":\"#" + keyPressed + "\","
                        + "\"keyboardBackground\":\"#" + keyboardBackground + "\","
                        + "\"border\":\"#" + border + "\","
                        + "\"depth\":" + nullableColor(depth) + ","
                        + "\"accent\":\"#" + accent + "\","
                        + "\"secondary\":\"#" + secondary + "\""
                        + "},"
                        + "\"shape\":{"
                        + "\"roundnessDp\":" + roundnessDp + ","
                        + "\"borderWidthDp\":" + KeyboardSettings.DEFAULT_KEY_BORDER_WIDTH_DP + ","
                        + "\"keyGapDp\":" + keyGapDp + ","
                        + "\"depthEnabled\":" + depthEnabled + ","
                        + "\"depthDp\":" + depthDp + ","
                        + "\"keyboardTopPaddingDp\":" + KeyboardSettings.DEFAULT_KEYBOARD_TOP_PADDING_DP
                        + "},"
                        + numberRowBlock(id)
                        + typographyBlock(id, fontFamily)
                        + keyColorOverridesBlock(id, keyColorOverridesJson)
                        + "}");
    }

    private static String nullableColor(String color) {
        return color == null ? "null" : "\"#" + color + "\"";
    }

    private static String typographyBlock(String id, String fontFamily) {
        boolean marigold = id != null && id.startsWith("marigold-fiesta");
        return "\"typography\":{"
                + "\"fontFamily\":\"" + fontFamily + "\","
                + "\"primaryTextSizePercent\":" + (marigold ? 82 : 78) + ","
                + "\"secondaryTextSizePercent\":" + (marigold ? 78 : 80) + ","
                + "\"primaryTextBold\":false,"
                + "\"primaryTextItalic\":false,"
                + "\"secondaryTextBold\":false,"
                + "\"secondaryTextItalic\":false"
                + "}";
    }

    private static String numberRowBlock(String id) {
        String mode = id != null && id.startsWith("marigold-fiesta")
                ? AdditionalNumberRowColorMode.CENTER_DIMMED.preferenceValue
                : AdditionalNumberRowColorMode.FULL_DIMMED.preferenceValue;
        return "\"additionalNumberRow\":{\"colorMode\":\"" + mode + "\"},";
    }

    private static String keyColorOverridesBlock(String id, String json) {
        String indicator = "\"shiftIndicator\":\"#" + shiftIndicatorColor(id) + "\"";
        if (json == null || json.isEmpty()) {
            return ",\"keyTextColorOverrides\":{" + indicator + "}";
        }
        return ",\"keyTextColorOverrides\":" + json.substring(0, json.length() - 1) + "," + indicator + "}";
    }

    private static String shiftIndicatorColor(String id) {
        if (id == null) {
            return "06B6D4";
        }
        if (id.startsWith("marigold-fiesta")) {
            return id.endsWith("dark") ? "36E7F4" : "008B82";
        }
        if (id.equals("gmk-bento")) {
            return "2F8CA1";
        }
        if (id.equals("gmk-oblivion")) {
            return "6CBF84";
        }
        if (id.equals("gmk-8008")) {
            return "7DBBE8";
        }
        if (id.equals("gmk-hammerhead")) {
            return "39D4C8";
        }
        if (id.equals("gmk-dracula")) {
            return "8BE9FD";
        }
        if (id.equals("gmk-modern-dolch")) {
            return "55C7C8";
        }
        if (id.contains("android") || id.contains("paper") || id.contains("graphite")) {
            return "16A34A";
        }
        if (id.contains("macos") || id.contains("slate")) {
            return "0891B2";
        }
        return "06B6D4";
    }

    private static String marigoldFiestaDarkOverrides() {
        return "{"
                + "\"tap:q\":\"#C75DFF\","
                + "\"tap:w\":\"#4DE4D2\","
                + "\"tap:e\":\"#FF9B48\","
                + "\"tap:r\":\"#FF5DAE\","
                + "\"tap:t\":\"#FFD25A\","
                + "\"tap:y\":\"#9C7CFF\","
                + "\"tap:u\":\"#36E7F4\","
                + "\"tap:i\":\"#B66BFF\","
                + "\"tap:o\":\"#FFC857\","
                + "\"tap:p\":\"#FF6FAE\","
                + "\"tap:a\":\"#DDE868\","
                + "\"tap:s\":\"#8A86FF\","
                + "\"tap:d\":\"#FF9E45\","
                + "\"tap:f\":\"#756BFF\","
                + "\"tap:g\":\"#FF6AC5\","
                + "\"tap:h\":\"#42D68C\","
                + "\"tap:j\":\"#E7D84E\","
                + "\"tap:k\":\"#A887FF\","
                + "\"tap:l\":\"#F2A64A\","
                + "\"tap:z\":\"#FFB84D\","
                + "\"tap:x\":\"#FF62C7\","
                + "\"tap:c\":\"#50E8CF\","
                + "\"tap:v\":\"#A987FF\","
                + "\"tap:b\":\"#FFD95F\","
                + "\"tap:n\":\"#F28C4B\","
                + "\"tap:m\":\"#C2A2FF\","
                + "\"tap:\u3131\":\"#C75DFF\","
                + "\"tap:\u3134\":\"#A887FF\","
                + "\"tap:\u3137\":\"#4DE4D2\","
                + "\"tap:\u3163\":\"#FFD25A\","
                + "\"tap:\u3139\":\"#FF9B48\","
                + "\"tap:\u3141\":\"#FF5DAE\","
                + "\"tap:\u3145\":\"#DDE868\","
                + "\"tap:\u3147\":\"#B66BFF\","
                + "\"tap:\u3161\":\"#36E7F4\","
                + "\"tap:\u3148\":\"#FFB84D\","
                + "\"tap:\u314E\":\"#E7D84E\","
                + "\"__dingul_center_vowel__\":\"#FFD25A\","
                + "\"__dingul_wide_vowel__\":\"#36E7F4\","
                + "\"\u3163.\":\"#42D68C\","
                + "\"\u3161\u3150\":\"#36E7F4\","
                + "\"?\":\"#FF5DAE\","
                + "\".\":\"#DDE868\","
                + "\". .\":\"#E7D84E\","
                + "\"/\":\"#4DE4D2\","
                + "\"shift\":\"#E9D64A\","
                + "\"options\":\"#FF5DAE\","
                + "\"reserved\":\"#C75DFF\","
                + "\"icon:1\":\"#FF5DAE\","
                + "\"icon:2\":\"#C75DFF\","
                + "\"icon:3\":\"#F7EEDB\","
                + "\"backspace\":\"#EDEAF3\","
                + "\"bksp\":\"#EDEAF3\","
                + "\"space\":\"#F7EEDB\","
                + "\"lang\":\"#4DE4D2\","
                + "\"language\":\"#4DE4D2\","
                + "\"enter\":\"#FF9F32\","
                + "\",;\":\"#C78AFF\","
                + "\"@/\":\"#4DE4D2\""
                + "}";
    }

    private static String marigoldFiestaLightOverrides() {
        return "{"
                + "\"tap:q\":\"#7C3CB3\","
                + "\"tap:w\":\"#008B82\","
                + "\"tap:e\":\"#B85F19\","
                + "\"tap:r\":\"#C02666\","
                + "\"tap:t\":\"#A06F00\","
                + "\"tap:y\":\"#6D4BC1\","
                + "\"tap:u\":\"#007C89\","
                + "\"tap:i\":\"#7D43C6\","
                + "\"tap:o\":\"#B06F00\","
                + "\"tap:p\":\"#B52A62\","
                + "\"tap:a\":\"#7E7D00\","
                + "\"tap:s\":\"#5052A8\","
                + "\"tap:d\":\"#B75918\","
                + "\"tap:f\":\"#5F4EC7\","
                + "\"tap:g\":\"#B91F80\","
                + "\"tap:h\":\"#00814E\","
                + "\"tap:j\":\"#8E7600\","
                + "\"tap:k\":\"#6550B5\","
                + "\"tap:l\":\"#996019\","
                + "\"tap:z\":\"#B66B00\","
                + "\"tap:x\":\"#C82187\","
                + "\"tap:c\":\"#008F86\","
                + "\"tap:v\":\"#7255CC\","
                + "\"tap:b\":\"#987800\","
                + "\"tap:n\":\"#B9671E\","
                + "\"tap:m\":\"#7B5CC5\","
                + "\"tap:\u3131\":\"#7C3CB3\","
                + "\"tap:\u3134\":\"#6550B5\","
                + "\"tap:\u3137\":\"#008B82\","
                + "\"tap:\u3163\":\"#C98900\","
                + "\"tap:\u3139\":\"#B85F19\","
                + "\"tap:\u3141\":\"#C02666\","
                + "\"tap:\u3145\":\"#7E7D00\","
                + "\"tap:\u3147\":\"#7D43C6\","
                + "\"tap:\u3161\":\"#007C89\","
                + "\"tap:\u3148\":\"#B66B00\","
                + "\"tap:\u314E\":\"#8E7600\","
                + "\"__dingul_center_vowel__\":\"#C98900\","
                + "\"__dingul_wide_vowel__\":\"#007C89\","
                + "\"\u3163.\":\"#00814E\","
                + "\"\u3161\u3150\":\"#007C89\","
                + "\"?\":\"#C02666\","
                + "\".\":\"#7E7D00\","
                + "\". .\":\"#8E7600\","
                + "\"/\":\"#008B82\","
                + "\"shift\":\"#8E7600\","
                + "\"options\":\"#C02666\","
                + "\"reserved\":\"#7C3CB3\","
                + "\"icon:1\":\"#C02666\","
                + "\"icon:2\":\"#7C3CB3\","
                + "\"icon:3\":\"#2B2D31\","
                + "\"backspace\":\"#202225\","
                + "\"bksp\":\"#202225\","
                + "\"space\":\"#2B2D31\","
                + "\"lang\":\"#008B82\","
                + "\"language\":\"#008B82\","
                + "\"enter\":\"#A95B00\","
                + "\",;\":\"#713EA8\","
                + "\"@/\":\"#008B82\""
                + "}";
    }
}
