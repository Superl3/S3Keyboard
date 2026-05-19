package com.academic.hangulgestureime;

import android.content.Context;
import android.content.SharedPreferences;

final class TouchBiasStore {
    private static final String PREF_NAME = "keyboard_preferences";
    static final String TOUCH_BIAS_STATS = "touch_bias_stats";
    static final float MAX_BIAS_DP = 6f;
    private static final float LEARNING_RATE = 0.05f;

    private final SharedPreferences preferences;

    TouchBiasStore(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    Bias load() {
        return Bias.decode(preferences.getString(TOUCH_BIAS_STATS, ""));
    }

    void recordImmediateDelete(float touchOffsetXDp, float touchOffsetYDp) {
        Bias next = load().recordImmediateDelete(touchOffsetXDp, touchOffsetYDp);
        preferences.edit().putString(TOUCH_BIAS_STATS, next.encode()).apply();
    }

    static void reset(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(TOUCH_BIAS_STATS)
                .apply();
    }

    static final class Bias {
        final float xDp;
        final float yDp;
        final int samples;

        Bias(float xDp, float yDp, int samples) {
            this.xDp = clamp(xDp);
            this.yDp = clamp(yDp);
            this.samples = Math.max(0, samples);
        }

        static Bias none() {
            return new Bias(0f, 0f, 0);
        }

        Bias recordImmediateDelete(float touchOffsetXDp, float touchOffsetYDp) {
            return new Bias(
                    xDp - touchOffsetXDp * LEARNING_RATE,
                    yDp - touchOffsetYDp * LEARNING_RATE,
                    samples + 1);
        }

        String encode() {
            return xDp + "," + yDp + "," + samples;
        }

        static Bias decode(String encoded) {
            if (encoded == null || encoded.isEmpty()) {
                return none();
            }
            String[] parts = encoded.split(",");
            if (parts.length != 3) {
                return none();
            }
            try {
                return new Bias(
                        Float.parseFloat(parts[0]),
                        Float.parseFloat(parts[1]),
                        Integer.parseInt(parts[2]));
            } catch (NumberFormatException ex) {
                return none();
            }
        }

        private static float clamp(float value) {
            return Math.max(-MAX_BIAS_DP, Math.min(MAX_BIAS_DP, value));
        }
    }
}
