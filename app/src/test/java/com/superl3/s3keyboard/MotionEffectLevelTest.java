package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MotionEffectLevelTest {
    @Test
    public void parsesPreferenceValues() {
        assertEquals(MotionEffectLevel.OFF, MotionEffectLevel.fromPreference("off"));
        assertEquals(MotionEffectLevel.SUBTLE, MotionEffectLevel.fromPreference("subtle"));
        assertEquals(MotionEffectLevel.NORMAL, MotionEffectLevel.fromPreference("normal"));
    }

    @Test
    public void unknownPreferenceFallsBackToNormal() {
        assertEquals(MotionEffectLevel.NORMAL, MotionEffectLevel.fromPreference("unknown"));
        assertEquals(MotionEffectLevel.NORMAL, MotionEffectLevel.fromPreference(null));
    }
}
