package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

final class KeyboardTypefaceCatalog {
    static final String ASSET_NOTO_SANS_KR = "fonts/NotoSansKR.ttf";
    static final String ASSET_NOTO_SERIF_KR = "fonts/NotoSerifKR.ttf";
    static final String ASSET_D2CODING = "fonts/D2Coding.ttf";

    private static final Map<String, Typeface> CACHE = new HashMap<>();

    private KeyboardTypefaceCatalog() {
    }

    static Typeface typefaceFor(Context context, String fontFamily, boolean bold, boolean italic) {
        int style = Typeface.NORMAL;
        if (bold) {
            style |= Typeface.BOLD;
        }
        if (italic) {
            style |= Typeface.ITALIC;
        }
        Typeface base = bundledTypeface(context, KeyboardSettings.normalizeFontFamily(fontFamily));
        return Typeface.create(base, style);
    }

    static String assetPathFor(String fontFamily) {
        switch (KeyboardSettings.normalizeFontFamily(fontFamily)) {
            case KeyboardSettings.FONT_NOTO_SERIF_KR:
                return ASSET_NOTO_SERIF_KR;
            case KeyboardSettings.FONT_D2CODING:
                return ASSET_D2CODING;
            case KeyboardSettings.FONT_NOTO_SANS_KR:
                return ASSET_NOTO_SANS_KR;
            case KeyboardSettings.FONT_DEFAULT:
            default:
                return null;
        }
    }

    private static Typeface bundledTypeface(Context context, String fontFamily) {
        String assetPath = assetPathFor(fontFamily);
        if (assetPath == null || context == null) {
            return fallbackTypeface(fontFamily);
        }
        Typeface cached = CACHE.get(assetPath);
        if (cached != null) {
            return cached;
        }
        try {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), assetPath);
            CACHE.put(assetPath, typeface);
            return typeface;
        } catch (RuntimeException exception) {
            return fallbackTypeface(fontFamily);
        }
    }

    private static Typeface fallbackTypeface(String fontFamily) {
        switch (KeyboardSettings.normalizeFontFamily(fontFamily)) {
            case KeyboardSettings.FONT_NOTO_SERIF_KR:
                return Typeface.create("serif", Typeface.NORMAL);
            case KeyboardSettings.FONT_D2CODING:
                return Typeface.create("monospace", Typeface.NORMAL);
            case KeyboardSettings.FONT_NOTO_SANS_KR:
            case KeyboardSettings.FONT_DEFAULT:
            default:
                return Typeface.create("sans-serif", Typeface.NORMAL);
        }
    }
}
