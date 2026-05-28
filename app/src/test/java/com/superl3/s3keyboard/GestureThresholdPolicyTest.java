package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class GestureThresholdPolicyTest {
    @Test
    public void keepsNormalKeyThresholdUnchanged() {
        KeyboardSettings settings = KeyboardSettings.defaults();
        GestureKey normal = new GestureKey("a", "a", "A", "@", null, null, null);

        assertEquals(
                settings.gestureThresholdDp,
                GestureThresholdPolicy.baseThresholdDp(settings, normal));
        assertEquals(
                settings.gestureThresholdDp,
                GestureThresholdPolicy.thresholdDp(
                        settings,
                        TouchBiasStore.Bias.none(),
                        normal,
                        GestureAction.DOWN));
    }

    @Test
    public void vowelGestureKeysUseShorterDirectionalThreshold() {
        KeyboardSettings settings = KeyboardSettings.defaults();

        assertShortVowelThreshold(settings, centerVowelKey());
        assertShortVowelThreshold(settings, wideVowelKey());
        assertShortVowelThreshold(settings, topVowelKey());
        assertShortVowelThreshold(settings, dotDotVowelKey());
    }

    @Test
    public void consonantGestureKeysUseShorterDirectionalThreshold() {
        KeyboardSettings settings = KeyboardSettings.defaults();
        GestureKey key = consonantKey();

        assertEquals(18, GestureThresholdPolicy.baseThresholdDp(settings, key));
        assertEquals(
                18,
                GestureThresholdPolicy.thresholdDp(
                        settings,
                        TouchBiasStore.Bias.none(),
                        key,
                        GestureAction.RIGHT));
    }

    @Test
    public void shorterDingulThresholdTurnsShortFlickIntoSlide() {
        KeyboardSettings settings = KeyboardSettings.defaults();
        GestureKey key = consonantKey();
        int threshold = GestureThresholdPolicy.thresholdDp(
                settings,
                TouchBiasStore.Bias.none(),
                key,
                GestureAction.RIGHT);

        assertEquals(
                GestureAction.RIGHT,
                new GestureState().release(
                        13,
                        2,
                        threshold,
                        threshold,
                        threshold,
                        threshold,
                        threshold,
                        1.15f));
    }

    @Test
    public void centerVowelKeyRespectsMinimumThreshold() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withGestureThreshold(KeyboardSettings.MIN_GESTURE_THRESHOLD_DP);

        assertEquals(
                KeyboardSettings.MIN_GESTURE_THRESHOLD_DP,
                GestureThresholdPolicy.baseThresholdDp(settings, centerVowelKey()));
    }

    private GestureKey centerVowelKey() {
        return new GestureKey(
                "\u3163.",
                KeyboardCommands.CMD_DINGUL_CENTER_VOWEL,
                "\u3157",
                "\u315C",
                "\u3153",
                "\u314F",
                null);
    }

    private GestureKey consonantKey() {
        return new GestureKey(
                "\u3131",
                "\u3131",
                "\u3132",
                "#",
                "\u314B",
                "\u314B",
                null);
    }

    private GestureKey wideVowelKey() {
        return new GestureKey(
                "\u3161\u3150",
                KeyboardCommands.CMD_DINGUL_WIDE_VOWEL,
                "\u3159",
                "\u315E",
                "\u3154",
                "\u3150",
                null);
    }

    private GestureKey topVowelKey() {
        return new GestureKey(
                "\u3162",
                "\u3162",
                "\u315A",
                "\u315F",
                "\u315D",
                "\u3158",
                null);
    }

    private GestureKey dotDotVowelKey() {
        return new GestureKey(
                ". .",
                " ",
                "\u315B",
                "\u3160",
                "\u3155",
                "\u3151",
                null);
    }

    private void assertShortVowelThreshold(KeyboardSettings settings, GestureKey key) {
        assertEquals(16, GestureThresholdPolicy.baseThresholdDp(settings, key));
        assertEquals(
                16,
                GestureThresholdPolicy.thresholdDp(
                        settings,
                        TouchBiasStore.Bias.none(),
                        key,
                        GestureAction.LEFT));
    }
}
