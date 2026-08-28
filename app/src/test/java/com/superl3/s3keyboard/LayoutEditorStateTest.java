package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LayoutEditorStateTest {
    @Test
    public void selectedPaddingOnlyChangesTheActiveLayoutSide() {
        KeyboardSettings initial = KeyboardSettings.defaults()
                .withHangulSidePadding(4, 7)
                .withEnglishSidePadding(2, 9);
        LayoutEditorState state = LayoutEditorState.from(initial, KeyboardMode.HANGUL)
                .select(LayoutEditorState.Control.LEFT_PADDING)
                .withValue(12);

        assertEquals(12, state.settings.hangulLeftPaddingDp);
        assertEquals(7, state.settings.hangulRightPaddingDp);
        assertEquals(2, state.settings.englishLeftPaddingDp);
        assertEquals(9, state.settings.englishRightPaddingDp);
    }

    @Test
    public void qwertyHeightAndGapDoNotTouchDingulValues() {
        KeyboardSettings initial = KeyboardSettings.defaults()
                .withHangulHeight(370)
                .withEnglishHeight(235)
                .withHangulKeyGap(3)
                .withEnglishKeyGap(4);
        LayoutEditorState height = LayoutEditorState.from(initial, KeyboardMode.ENGLISH)
                .select(LayoutEditorState.Control.HEIGHT)
                .withValue(260);
        LayoutEditorState gap = height.select(LayoutEditorState.Control.KEY_GAP).withValue(8);

        assertEquals(260, gap.settings.englishKeyboardHeightDp);
        assertEquals(370, gap.settings.hangulKeyboardHeightDp);
        assertEquals(8, gap.settings.englishKeyGapDp);
        assertEquals(3, gap.settings.hangulKeyGapDp);
    }

    @Test
    public void selectedValueBoundsMatchTheDisplayedControl() {
        LayoutEditorState state = LayoutEditorState.from(
                        KeyboardSettings.defaults(), KeyboardMode.HANGUL)
                .select(LayoutEditorState.Control.HEIGHT);

        assertEquals(KeyboardSettings.MIN_HEIGHT_DP, state.minValue());
        assertEquals(KeyboardSettings.MAX_HEIGHT_DP, state.maxValue());
    }
}
