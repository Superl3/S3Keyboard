package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

final class LearningEpoch {
    private static final String PREF_NAME = "keyboard_preferences";

    private LearningEpoch() {
    }

    static long current(Context context) {
        if (context == null) {
            return 0L;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long epoch = prefs.getLong(KeyboardPreferences.INPUT_LEARNING_EPOCH, 0L);
        if (epoch > 0L) {
            return epoch;
        }
        return reset(context);
    }

    static long reset(Context context) {
        if (context == null) {
            return 0L;
        }
        long epoch = Math.max(1L, System.currentTimeMillis());
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(KeyboardPreferences.INPUT_LEARNING_EPOCH, epoch)
                .apply();
        return epoch;
    }

    static boolean matches(long storedEpoch, long currentEpoch) {
        return storedEpoch > 0L && currentEpoch > 0L && storedEpoch == currentEpoch;
    }
}
