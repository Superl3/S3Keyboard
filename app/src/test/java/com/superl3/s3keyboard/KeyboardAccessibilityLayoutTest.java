package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class KeyboardAccessibilityLayoutTest {
    @Test
    public void allDefaultHangulKeysHaveAccessibleLabels() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withHangulNumberRow(false);

        assertAccessibleLayout(KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                360f,
                settings.measuredHeightDp(),
                1f));
    }

    @Test
    public void allDefaultQwertyKeysHaveAccessibleLabels() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(true);

        assertAccessibleLayout(KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                360f,
                settings.measuredHeightDp(),
                1f));
    }

    @Test
    public void layoutHitBoundsNeverShrinkBelowBaseBoundsForAccessibility() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withHangulNumberRow(false);

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.AGGRESSIVE.options,
                360f,
                settings.measuredHeightDp(),
                1f);

        assertFalse(slots.isEmpty());
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            assertTrue(slot.hitLeft <= slot.left);
            assertTrue(slot.hitTop <= slot.top);
            assertTrue(slot.hitRight >= slot.right);
            assertTrue(slot.hitBottom >= slot.bottom);
            assertTrue(slot.hitRight > slot.hitLeft);
            assertTrue(slot.hitBottom > slot.hitTop);
        }
    }

    @Test
    public void aggressiveDingulErgonomicsKeepsAccessibleTouchTargets() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withHangulNumberRow(true);

        assertAccessibleLayout(KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.AGGRESSIVE.options,
                360f,
                settings.measuredHeightDp(),
                1f));
    }

    @Test
    public void qwertyLayoutKeepsAccessibleTouchTargetsAtDefaultHeight() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(true);

        assertAccessibleLayout(KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                KeyboardErgonomicsPreset.AGGRESSIVE.options,
                360f,
                settings.measuredHeightDp(),
                1f));
    }

    @Test
    public void compactQwertyExposesRecommendedTouchTargetAdvisoriesWithoutHardFailure() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withEnglishNumberRow(true);

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                360f,
                settings.measuredHeightDp(),
                1f);

        assertEquals(0, KeyboardAccessibilityAudit.audit(slots).size());
        List<KeyboardAccessibilityAudit.Issue> advisories =
                KeyboardAccessibilityAudit.advisoryAudit(slots);
        assertFalse(advisories.isEmpty());
        for (KeyboardAccessibilityAudit.Issue advisory : advisories) {
            assertEquals("recommended touch target below 48dp", advisory.reason);
        }
    }

    @Test
    public void compactHangulExposesRecommendedTouchTargetAdvisoriesWithoutHardFailure() {
        KeyboardSettings settings = KeyboardSettings.defaults()
                .withKeyboardMode(KeyboardMode.HANGUL)
                .withHangulNumberRow(false);

        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                KeyboardLayoutFactory.build(settings),
                settings,
                360f,
                settings.measuredHeightDp(),
                1f);

        assertEquals(0, KeyboardAccessibilityAudit.audit(slots).size());
        List<KeyboardAccessibilityAudit.Issue> advisories =
                KeyboardAccessibilityAudit.advisoryAudit(slots);
        assertFalse(advisories.isEmpty());
        for (KeyboardAccessibilityAudit.Issue advisory : advisories) {
            assertEquals("recommended touch target below 48dp", advisory.reason);
        }
    }

    private static void assertAccessibleLayout(List<KeyboardLayoutCalculator.Slot> slots) {
        assertFalse(slots.isEmpty());
        assertEquals(0, KeyboardAccessibilityAudit.audit(slots).size());
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            String description = KeyboardKeyAccessibilityLabel.describe(slot.key);
            assertFalse("empty accessibility label for " + slot.key.label, description.trim().isEmpty());
            assertFalse("raw command leaked to accessibility label: " + description, description.contains("cmd_"));
        }
    }
}
