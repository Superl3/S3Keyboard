package com.superl3.s3keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class KeyboardAccessibilityAudit {
    static final float MIN_TOUCH_TARGET_DP = 30f;
    static final float RECOMMENDED_TOUCH_TARGET_DP = 40f;

    private KeyboardAccessibilityAudit() {
    }

    static List<Issue> audit(List<KeyboardLayoutCalculator.Slot> slots) {
        return audit(slots, MIN_TOUCH_TARGET_DP, MIN_TOUCH_TARGET_DP, "touch target too small");
    }

    static List<Issue> advisoryAudit(List<KeyboardLayoutCalculator.Slot> slots) {
        return audit(
                slots,
                RECOMMENDED_TOUCH_TARGET_DP,
                RECOMMENDED_TOUCH_TARGET_DP,
                "recommended touch target below 40dp");
    }

    static List<Issue> audit(
            List<KeyboardLayoutCalculator.Slot> slots,
            float minimumWidth,
            float minimumHeight) {
        return audit(slots, minimumWidth, minimumHeight, "touch target too small");
    }

    private static List<Issue> audit(
            List<KeyboardLayoutCalculator.Slot> slots,
            float minimumWidth,
            float minimumHeight,
            String touchTargetReason) {
        if (slots == null || slots.isEmpty()) {
            return Collections.singletonList(new Issue("", "no keys", 0f, 0f));
        }
        List<Issue> issues = new ArrayList<>();
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            if (slot == null || slot.key == null) {
                issues.add(new Issue("", "missing key", 0f, 0f));
                continue;
            }
            String description = KeyboardKeyAccessibilityLabel.describe(slot.key);
            float width = slot.hitRight - slot.hitLeft;
            float height = slot.hitBottom - slot.hitTop;
            if (description.trim().isEmpty()) {
                issues.add(new Issue(slot.key.label, "empty label", width, height));
            } else if (description.contains("cmd_")) {
                issues.add(new Issue(slot.key.label, "raw command label", width, height));
            }
            if (width < minimumWidth || height < minimumHeight) {
                issues.add(new Issue(slot.key.label, touchTargetReason, width, height));
            }
        }
        return issues;
    }

    static final class Issue {
        final String keyLabel;
        final String reason;
        final float width;
        final float height;

        Issue(String keyLabel, String reason, float width, float height) {
            this.keyLabel = keyLabel == null ? "" : keyLabel;
            this.reason = reason == null ? "" : reason;
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return keyLabel + ": " + reason + " (" + width + "x" + height + ")";
        }
    }
}
