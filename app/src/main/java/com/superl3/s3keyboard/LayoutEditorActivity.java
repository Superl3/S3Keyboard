package com.superl3.s3keyboard;

import android.app.Activity;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

public final class LayoutEditorActivity extends Activity {
    private static final int STATUS_BAR_DP = 28;
    private static final int BAR_THICK_DP = 10;
    private static final int BAR_LEN_DP = 48;
    private static final int MIN_HANDLE_OFFSET_PX = 8;

    private KeyboardSettings currentSettings;
    private KeyboardLayoutProfiles currentProfiles;
    private KeyboardMode currentMode = KeyboardMode.HANGUL;

    private HangulKeyboardView preview;
    private FrameLayout keyboardArea;
    private LayoutEditorOverlay overlay;
    private LayoutEditorHandleView leftMarginHandle;
    private LayoutEditorHandleView rightMarginHandle;
    private LayoutEditorHandleView specialColumnHandle;
    private LayoutEditorHandleView topPaddingHandle;
    private LayoutEditorHandleView bottomPaddingHandle;
    private LayoutEditorHandleView bottomRowGapHandle;
    private LayoutEditorHandleView heightHandle;

    private SeekBar heightSeekBar;
    private SeekBar specialColumnSeekBar;
    private SeekBar topPaddingSeekBar;
    private SeekBar bottomPaddingSeekBar;
    private SeekBar bottomRowGapSeekBar;
    private SeekBar keyGapSeekBar;
    private SeekBar roundnessSeekBar;
    private TextView heightValue;
    private TextView specialColumnValue;
    private TextView topPaddingValue;
    private TextView bottomPaddingValue;
    private TextView bottomRowGapValue;
    private TextView keyGapValue;
    private TextView roundnessValue;
    private CheckBox numberRowCheckBox;
    private CheckBox showZonesCheckBox;
    private CheckBox symmetricCheckBox;
    private LinearLayout specialColumnRow;

    private Button hangulButton;
    private Button englishButton;
    private Button balancedButton;
    private Button leftButton;
    private Button rightButton;

    private boolean syncingSeekBar;
    private boolean symmetricMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        currentSettings = KeyboardPreferences.load(this);
        currentProfiles = KeyboardPreferences.loadLayoutProfiles(this);
        currentMode = currentSettings.keyboardMode;
        if (currentSettings.englishLeftPaddingDp == 6 && currentSettings.englishRightPaddingDp == 6) {
            currentSettings = currentSettings.withEnglishSidePadding(0, 0);
        }
        setContentView(createContentView());
        updatePreview();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private View createContentView() {
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(ui.background);
        root.setPadding(0, dp(STATUS_BAR_DP), 0, 0);

        root.addView(buildToolbar(ui), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(buildControlPanel(ui), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f));

        keyboardArea = new FrameLayout(this);
        root.addView(keyboardArea, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                2f));

        preview = new HangulKeyboardView(this, true);
        preview.setCompactPreviewRendering(false);
        preview.setEnabled(false);
        preview.setFocusable(false);
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        preview.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM));
        keyboardArea.addView(preview);

        overlay = new LayoutEditorOverlay(this);
        keyboardArea.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        int thickPx = dp(BAR_THICK_DP);
        int lenPx = dp(BAR_LEN_DP);

        leftMarginHandle = new LayoutEditorHandleView(this, currentSettings.accentColor,
                0, KeyboardSettings.MAX_MARGIN_DP, currentMarginDp(), true, false,
                new MarginDragListener(true), "dp",
                LayoutEditorHandleView.STYLE_VERTICAL_BAR);
        leftMarginHandle.setLayoutParams(new FrameLayout.LayoutParams(thickPx, lenPx));
        keyboardArea.addView(leftMarginHandle);

        rightMarginHandle = new LayoutEditorHandleView(this, currentSettings.accentColor,
                0, KeyboardSettings.MAX_MARGIN_DP, currentRightMarginDp(), true, true,
                new MarginDragListener(false), "dp",
                LayoutEditorHandleView.STYLE_VERTICAL_BAR);
        rightMarginHandle.setLayoutParams(new FrameLayout.LayoutParams(thickPx, lenPx));
        keyboardArea.addView(rightMarginHandle);

        specialColumnHandle = new LayoutEditorHandleView(this, currentSettings.accentColor,
                KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT,
                KeyboardSettings.MAX_HANGUL_SPECIAL_COLUMN_PERCENT,
                currentSettings.hangulSpecialColumnPercent, true, true,
                new SpecialColumnDragListener(), "%",
                LayoutEditorHandleView.STYLE_VERTICAL_BAR);
        specialColumnHandle.setLayoutParams(new FrameLayout.LayoutParams(thickPx, lenPx));
        keyboardArea.addView(specialColumnHandle);

        topPaddingHandle = new LayoutEditorHandleView(this, currentSettings.accentColor,
                0, KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP,
                currentSettings.keyboardTopPaddingDp, false, false,
                new TopPaddingDragListener(), "dp",
                LayoutEditorHandleView.STYLE_HORIZONTAL_BAR);
        topPaddingHandle.setFullSpan(true);
        topPaddingHandle.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, thickPx));
        keyboardArea.addView(topPaddingHandle);

        bottomPaddingHandle = new LayoutEditorHandleView(this, currentSettings.accentColor,
                0, KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP,
                currentSettings.keyboardBottomPaddingDp, false, true,
                new BottomPaddingDragListener(), "dp",
                LayoutEditorHandleView.STYLE_HORIZONTAL_BAR);
        bottomPaddingHandle.setFullSpan(true);
        bottomPaddingHandle.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, thickPx));
        keyboardArea.addView(bottomPaddingHandle);

        bottomRowGapHandle = new LayoutEditorHandleView(this, currentSettings.accentColor,
                0, KeyboardSettings.MAX_BOTTOM_ROW_TOP_PADDING_DP,
                currentSettings.bottomRowTopPaddingDp, false, true,
                new BottomRowGapDragListener(), "dp",
                LayoutEditorHandleView.STYLE_HORIZONTAL_BAR);
        bottomRowGapHandle.setFullSpan(true);
        bottomRowGapHandle.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, thickPx));
        keyboardArea.addView(bottomRowGapHandle);

        heightHandle = new LayoutEditorHandleView(this, currentSettings.accentColor,
                KeyboardSettings.MIN_HEIGHT_DP, KeyboardSettings.MAX_HEIGHT_DP,
                currentHeightDp(), false, true,
                new HeightDragListener(), "dp",
                LayoutEditorHandleView.STYLE_HORIZONTAL_BAR);
        heightHandle.setFullSpan(true);
        heightHandle.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, thickPx));
        keyboardArea.addView(heightHandle);

        keyboardArea.post(this::positionHandles);
        return root;
    }

    private LinearLayout buildToolbar(SettingsUiPalette ui) {
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(8), dp(4), dp(8), dp(4));
        toolbar.setBackgroundColor((ui.surfaceRaised & 0x00FFFFFF) | 0xDD000000);

        hangulButton = SettingsRowBuilder.button(this, "한글");
        hangulButton.setOnClickListener(v -> setMode(KeyboardMode.HANGUL));
        toolbar.addView(hangulButton, SettingsRowBuilder.wrapContentWithLeft(this, 4));

        englishButton = SettingsRowBuilder.button(this, "English");
        englishButton.setOnClickListener(v -> setMode(KeyboardMode.ENGLISH));
        toolbar.addView(englishButton, SettingsRowBuilder.wrapContentWithLeft(this, 4));

        View div1 = new View(this);
        div1.setLayoutParams(new LinearLayout.LayoutParams(dp(1), dp(20)));
        div1.setBackgroundColor(0x33000000);
        toolbar.addView(div1, SettingsRowBuilder.wrapContentWithLeft(this, 8));

        balancedButton = SettingsRowBuilder.button(this, "Center");
        balancedButton.setOnClickListener(v -> setHandedness(HandednessMode.BALANCED));
        toolbar.addView(balancedButton, SettingsRowBuilder.wrapContentWithLeft(this, 4));

        leftButton = SettingsRowBuilder.button(this, "L");
        leftButton.setOnClickListener(v -> setHandedness(HandednessMode.LEFT));
        toolbar.addView(leftButton, SettingsRowBuilder.wrapContentWithLeft(this, 4));

        rightButton = SettingsRowBuilder.button(this, "R");
        rightButton.setOnClickListener(v -> setHandedness(HandednessMode.RIGHT));
        toolbar.addView(rightButton, SettingsRowBuilder.wrapContentWithLeft(this, 4));

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        toolbar.addView(spacer);

        Button resetButton = SettingsRowBuilder.button(this, "초기화");
        resetButton.setOnClickListener(v -> resetSettings());
        toolbar.addView(resetButton, SettingsRowBuilder.wrapContentWithLeft(this, 4));

        Button applyButton = SettingsRowBuilder.button(this, "적용");
        applyButton.setOnClickListener(v -> saveAndClose());
        toolbar.addView(applyButton, SettingsRowBuilder.wrapContentWithLeft(this, 4));

        Button closeButton = SettingsRowBuilder.button(this, "✕");
        closeButton.setOnClickListener(v -> closeWithoutSaving());
        toolbar.addView(closeButton, SettingsRowBuilder.wrapContentWithLeft(this, 4));

        return toolbar;
    }

    private View buildControlPanel(SettingsUiPalette ui) {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor((ui.surface & 0x00FFFFFF) | 0xF0000000);

        LinearLayout toggleRow = new LinearLayout(this);
        toggleRow.setOrientation(LinearLayout.HORIZONTAL);
        toggleRow.setGravity(Gravity.CENTER_VERTICAL);
        toggleRow.setPadding(dp(8), dp(4), dp(8), dp(4));

        numberRowCheckBox = SettingsRowBuilder.checkBoxRow(this, toggleRow,
                R.string.settings_hangul_number_row, 0,
                () -> !syncingSeekBar,
                isChecked -> {
                    if (currentMode == KeyboardMode.HANGUL) {
                        applySettings(currentSettings.withHangulNumberRow(isChecked));
                    } else {
                        applySettings(currentSettings.withEnglishNumberRow(isChecked));
                    }
                });

        symmetricCheckBox = new CheckBox(this);
        symmetricCheckBox.setText("대칭");
        symmetricCheckBox.setChecked(false);
        symmetricCheckBox.setOnCheckedChangeListener((v, isChecked) -> symmetricMode = isChecked);
        toggleRow.addView(symmetricCheckBox, SettingsRowBuilder.wrapContentWithLeft(this, 8));

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        toggleRow.addView(spacer);

        showZonesCheckBox = new CheckBox(this);
        showZonesCheckBox.setText("영역표시");
        showZonesCheckBox.setChecked(true);
        showZonesCheckBox.setOnCheckedChangeListener((v, isChecked) -> {
            overlay.setShowZones(isChecked);
            for (int i = 0; i < keyboardArea.getChildCount(); i++) {
                View child = keyboardArea.getChildAt(i);
                if (child instanceof LayoutEditorHandleView) {
                    child.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                }
            }
        });
        toggleRow.addView(showZonesCheckBox, SettingsRowBuilder.wrapContentWithLeft(this, 8));

        panel.addView(toggleRow, SettingsRowBuilder.matchWrap());

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout seekBarList = new LinearLayout(this);
        seekBarList.setOrientation(LinearLayout.VERTICAL);
        seekBarList.setPadding(dp(12), dp(4), dp(12), dp(8));

        heightValue = SettingsRowBuilder.valueLabel(this);
        heightSeekBar = SettingsRowBuilder.seekBarRow(this, seekBarList, heightValue,
                KeyboardSettings.MAX_HEIGHT_DP - KeyboardSettings.MIN_HEIGHT_DP, 3,
                () -> !syncingSeekBar,
                this::onHeightSeekBarChanged);

        specialColumnValue = SettingsRowBuilder.valueLabel(this);
        specialColumnRow = SettingsSubsection.add(this, seekBarList,
                R.string.layout_editor_special_column, false).content;
        specialColumnSeekBar = SettingsRowBuilder.seekBarRow(this, specialColumnRow, specialColumnValue,
                KeyboardSettings.MAX_HANGUL_SPECIAL_COLUMN_PERCENT
                        - KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT,
                3, () -> !syncingSeekBar,
                this::onSpecialColumnSeekBarChanged);

        topPaddingValue = SettingsRowBuilder.valueLabel(this);
        topPaddingSeekBar = SettingsRowBuilder.seekBarRow(this, seekBarList, topPaddingValue,
                KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP, 3,
                () -> !syncingSeekBar,
                this::onTopPaddingSeekBarChanged);

        bottomPaddingValue = SettingsRowBuilder.valueLabel(this);
        bottomPaddingSeekBar = SettingsRowBuilder.seekBarRow(this, seekBarList, bottomPaddingValue,
                KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP, 3,
                () -> !syncingSeekBar,
                this::onBottomPaddingSeekBarChanged);

        bottomRowGapValue = SettingsRowBuilder.valueLabel(this);
        bottomRowGapSeekBar = SettingsRowBuilder.seekBarRow(this, seekBarList, bottomRowGapValue,
                KeyboardSettings.MAX_BOTTOM_ROW_TOP_PADDING_DP, 3,
                () -> !syncingSeekBar,
                this::onBottomRowGapSeekBarChanged);

        keyGapValue = SettingsRowBuilder.valueLabel(this);
        keyGapSeekBar = SettingsRowBuilder.seekBarRow(this, seekBarList, keyGapValue,
                KeyboardSettings.MAX_KEY_GAP_DP, 3,
                () -> !syncingSeekBar,
                this::onKeyGapSeekBarChanged);

        roundnessValue = SettingsRowBuilder.valueLabel(this);
        roundnessSeekBar = SettingsRowBuilder.seekBarRow(this, seekBarList, roundnessValue,
                KeyboardSettings.MAX_KEY_ROUNDNESS_DP, 3,
                () -> !syncingSeekBar,
                this::onRoundnessSeekBarChanged);

        scroll.addView(seekBarList);
        panel.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return panel;
    }

    private void setMode(KeyboardMode mode) {
        if (currentMode == mode) {
            return;
        }
        currentMode = mode;
        applySettings(currentSettings.withKeyboardMode(mode));
        syncToolbarButtons();
    }

    private void setHandedness(HandednessMode mode) {
        applySettings(currentSettings.withHandednessPreset(mode));
        syncToolbarButtons();
    }

    private void resetSettings() {
        KeyboardSettings defaults = KeyboardSettings.defaults()
                .withEnglishSidePadding(0, 0);
        currentSettings = defaults;
        currentMode = defaults.keyboardMode;
        applySettings(currentSettings);
        syncAllControls();
    }

    private void closeWithoutSaving() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void saveAndClose() {
        try {
            KeyboardPreferences.saveSettings(this, currentSettings);
            setResult(RESULT_OK);
        } catch (Exception e) {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void applySettings(KeyboardSettings newSettings) {
        currentSettings = newSettings;
        updatePreview();
        updateOverlay();
        syncSeekBars();
        syncHandles();
        positionHandles();
    }

    private void updatePreview() {
        preview.setSettings(RuntimeDefaults.keyboardSettings(currentSettings));
        preview.setLayoutProfiles(currentProfiles);
    }

    private void updateOverlay() {
        overlay.update(
                currentMarginDp(),
                currentRightMarginDp(),
                currentSettings.keyboardTopPaddingDp,
                currentSettings.keyboardBottomPaddingDp,
                currentMeasuredHeightDp(),
                currentSettings.hangulSpecialColumnPercent,
                currentSettings.hangulMainSpecialGapDp,
                currentSettings.bottomRowTopPaddingDp,
                currentSettings.keyGapDp,
                currentMode == KeyboardMode.HANGUL);
    }

    private int currentHeightDp() {
        return currentMode == KeyboardMode.HANGUL
                ? currentSettings.hangulKeyboardHeightDp
                : currentSettings.englishKeyboardHeightDp;
    }

    private int currentMeasuredHeightDp() {
        return currentSettings.withKeyboardMode(currentMode).measuredHeightDp();
    }

    private int currentMarginDp() {
        return currentMode == KeyboardMode.ENGLISH
                ? currentSettings.englishLeftPaddingDp
                : currentSettings.hangulLeftPaddingDp;
    }

    private int currentRightMarginDp() {
        return currentMode == KeyboardMode.ENGLISH
                ? currentSettings.englishRightPaddingDp
                : currentSettings.hangulRightPaddingDp;
    }

    private void onHeightSeekBarChanged(int progress) {
        int valueDp = KeyboardSettings.MIN_HEIGHT_DP + progress;
        applySettings(currentMode == KeyboardMode.HANGUL
                ? currentSettings.withHangulHeight(valueDp)
                : currentSettings.withEnglishHeight(valueDp));
    }

    private void onSpecialColumnSeekBarChanged(int progress) {
        applySettings(currentSettings.withHangulSpecialColumnPercent(
                KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT + progress));
    }

    private void onTopPaddingSeekBarChanged(int progress) {
        applySettings(currentSettings.withLayoutSpacing(
                currentSettings.hangulMainSpecialGapDp, progress,
                currentSettings.keyboardBottomPaddingDp,
                currentSettings.bottomRowTopPaddingDp));
    }

    private void onBottomPaddingSeekBarChanged(int progress) {
        applySettings(currentSettings.withLayoutSpacing(
                currentSettings.hangulMainSpecialGapDp,
                currentSettings.keyboardTopPaddingDp, progress,
                currentSettings.bottomRowTopPaddingDp));
    }

    private void onBottomRowGapSeekBarChanged(int progress) {
        applySettings(currentSettings.withLayoutSpacing(
                currentSettings.hangulMainSpecialGapDp,
                currentSettings.keyboardTopPaddingDp,
                currentSettings.keyboardBottomPaddingDp, progress));
    }

    private void onKeyGapSeekBarChanged(int progress) {
        applySettings(currentMode == KeyboardMode.HANGUL
                ? currentSettings.withHangulKeyGap(progress)
                : currentSettings.withEnglishKeyGap(progress));
    }

    private void onRoundnessSeekBarChanged(int progress) {
        applySettings(currentSettings.withKeyRoundness(progress));
    }

    private void syncSeekBars() {
        syncingSeekBar = true;
        int heightVal = currentHeightDp();
        heightSeekBar.setProgress(heightVal - KeyboardSettings.MIN_HEIGHT_DP);
        heightValue.setText(getString(R.string.keyboard_height_format, heightVal));
        specialColumnSeekBar.setProgress(
                currentSettings.hangulSpecialColumnPercent - KeyboardSettings.MIN_HANGUL_SPECIAL_COLUMN_PERCENT);
        specialColumnValue.setText(getString(R.string.special_column_format,
                currentSettings.hangulSpecialColumnPercent));
        topPaddingSeekBar.setProgress(currentSettings.keyboardTopPaddingDp);
        topPaddingValue.setText(getString(R.string.settings_keyboard_top_padding_format,
                currentSettings.keyboardTopPaddingDp));
        bottomPaddingSeekBar.setProgress(currentSettings.keyboardBottomPaddingDp);
        bottomPaddingValue.setText(getString(R.string.settings_keyboard_bottom_padding_format,
                currentSettings.keyboardBottomPaddingDp));
        bottomRowGapSeekBar.setProgress(currentSettings.bottomRowTopPaddingDp);
        bottomRowGapValue.setText(getString(R.string.bottom_row_gap_format,
                currentSettings.bottomRowTopPaddingDp));
        int gapVal = currentMode == KeyboardMode.HANGUL
                ? currentSettings.hangulKeyGapDp : currentSettings.englishKeyGapDp;
        keyGapSeekBar.setProgress(gapVal);
        keyGapValue.setText(getString(R.string.key_gap_format, gapVal));
        roundnessSeekBar.setProgress(currentSettings.keyRoundnessDp);
        roundnessValue.setText(getString(R.string.settings_roundness_format,
                currentSettings.keyRoundnessDp));
        numberRowCheckBox.setChecked(currentMode == KeyboardMode.HANGUL
                ? currentSettings.showHangulNumberRow : currentSettings.showEnglishNumberRow);
        syncingSeekBar = false;
    }

    private void syncHandles() {
        leftMarginHandle.setValue(currentMarginDp());
        rightMarginHandle.setValue(currentRightMarginDp());
        specialColumnHandle.setValue(currentSettings.hangulSpecialColumnPercent);
        topPaddingHandle.setValue(currentSettings.keyboardTopPaddingDp);
        bottomPaddingHandle.setValue(currentSettings.keyboardBottomPaddingDp);
        bottomRowGapHandle.setValue(currentSettings.bottomRowTopPaddingDp);
        heightHandle.setValue(currentHeightDp());
    }

    private void syncToolbarButtons() {
        updateButtonState(hangulButton, currentMode == KeyboardMode.HANGUL);
        updateButtonState(englishButton, currentMode == KeyboardMode.ENGLISH);
        updateButtonState(balancedButton, currentSettings.handednessMode == HandednessMode.BALANCED);
        updateButtonState(leftButton, currentSettings.handednessMode == HandednessMode.LEFT);
        updateButtonState(rightButton, currentSettings.handednessMode == HandednessMode.RIGHT);
        int labelRes = currentMode == KeyboardMode.HANGUL
                ? R.string.settings_hangul_number_row : R.string.settings_english_number_row;
        numberRowCheckBox.setText(labelRes);
        boolean showSpecial = currentMode == KeyboardMode.HANGUL;
        specialColumnHandle.setVisibility(showSpecial ? View.VISIBLE : View.GONE);
        specialColumnRow.setVisibility(showSpecial ? View.VISIBLE : View.GONE);
    }

    private void updateButtonState(Button button, boolean selected) {
        SettingsViewStyler.button(button, this, selected);
    }

    private void syncAllControls() {
        syncSeekBars();
        syncHandles();
        syncToolbarButtons();
        positionHandles();
    }

    private void positionHandles() {
        int areaW = keyboardArea.getWidth();
        int areaH = keyboardArea.getHeight();
        if (areaW <= 0 || areaH <= 0) {
            return;
        }

        int previewH = preview.getHeight();
        int kbTop = Math.max(0, areaH - previewH);
        int kbBottom = areaH;

        List<HangulKeyboardView.KeySlot> slots = preview.accessibilityKeySlots();
        if (slots == null || slots.isEmpty()) {
            return;
        }

        float minLeft = Float.MAX_VALUE;
        float maxRight = Float.MIN_VALUE;
        float minTop = Float.MAX_VALUE;
        float maxBottom = Float.MIN_VALUE;
        for (HangulKeyboardView.KeySlot slot : slots) {
            RectF b = slot.visualBounds();
            if (b.left < minLeft) minLeft = b.left;
            if (b.right > maxRight) maxRight = b.right;
            if (b.top < minTop) minTop = b.top;
            if (b.bottom > maxBottom) maxBottom = b.bottom;
        }

        int kbLeft = (int) minLeft;
        int kbRight = (int) maxRight;
        int charTopY = (int) minTop;
        int charBottomY = (int) maxBottom;
        int charCenterY = (charTopY + charBottomY) / 2;

        int marginPx = kbLeft;
        int rightMarginPx = areaW - kbRight;
        int thickHalf = dp(BAR_THICK_DP) / 2;
        int barLenHalf = dp(BAR_LEN_DP) / 2;
        int topPadPx = dp(currentSettings.keyboardTopPaddingDp);
        int bottomPadPx = dp(currentSettings.keyboardBottomPaddingDp);
        int bottomRowH = dp(KeyboardSettings.DEFAULT_BOTTOM_CONTROL_ROW_HEIGHT_DP);
        int bottomRowGapPx = dp(currentSettings.bottomRowTopPaddingDp);
        int bottomRowTopY = kbBottom - bottomPadPx - bottomRowH;

        FrameLayout.LayoutParams lp;

        lp = (FrameLayout.LayoutParams) leftMarginHandle.getLayoutParams();
        lp.leftMargin = Math.max(marginPx - thickHalf, -thickHalf + MIN_HANDLE_OFFSET_PX);
        lp.topMargin = charCenterY - barLenHalf;
        lp.gravity = Gravity.TOP | Gravity.START;
        leftMarginHandle.setLayoutParams(lp);

        lp = (FrameLayout.LayoutParams) rightMarginHandle.getLayoutParams();
        lp.leftMargin = Math.min(areaW - rightMarginPx - thickHalf,
                areaW - thickHalf - MIN_HANDLE_OFFSET_PX);
        lp.topMargin = charCenterY - barLenHalf;
        lp.gravity = Gravity.TOP | Gravity.START;
        rightMarginHandle.setLayoutParams(lp);

        if (currentMode == KeyboardMode.HANGUL && slots.size() >= 4) {
            RectF thirdKey = slots.get(2).visualBounds();
            RectF fourthKey = slots.get(3).visualBounds();
            int dividerX = (int) ((thirdKey.right + fourthKey.left) / 2f);

            lp = (FrameLayout.LayoutParams) specialColumnHandle.getLayoutParams();
            lp.leftMargin = dividerX - thickHalf;
            lp.topMargin = charCenterY - barLenHalf;
            lp.gravity = Gravity.TOP | Gravity.START;
            specialColumnHandle.setLayoutParams(lp);
        }

        lp = (FrameLayout.LayoutParams) topPaddingHandle.getLayoutParams();
        lp.topMargin = kbTop + Math.max(topPadPx / 2 - thickHalf, -thickHalf + MIN_HANDLE_OFFSET_PX);
        lp.gravity = Gravity.TOP | Gravity.START;
        topPaddingHandle.setLayoutParams(lp);

        lp = (FrameLayout.LayoutParams) bottomRowGapHandle.getLayoutParams();
        lp.topMargin = bottomRowTopY - Math.max(bottomRowGapPx / 2 - thickHalf,
                -thickHalf + MIN_HANDLE_OFFSET_PX);
        lp.gravity = Gravity.TOP | Gravity.START;
        bottomRowGapHandle.setLayoutParams(lp);

        lp = (FrameLayout.LayoutParams) bottomPaddingHandle.getLayoutParams();
        lp.topMargin = kbBottom - Math.max(bottomPadPx / 2 + thickHalf,
                thickHalf + MIN_HANDLE_OFFSET_PX);
        lp.gravity = Gravity.TOP | Gravity.START;
        bottomPaddingHandle.setLayoutParams(lp);

        lp = (FrameLayout.LayoutParams) heightHandle.getLayoutParams();
        lp.topMargin = kbBottom - thickHalf;
        lp.gravity = Gravity.TOP | Gravity.START;
        heightHandle.setLayoutParams(lp);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private class MarginDragListener implements LayoutEditorHandleView.OnDragListener {
        private final boolean isLeft;
        private int initialLeft;
        private int initialRight;

        MarginDragListener(boolean isLeft) {
            this.isLeft = isLeft;
        }

        @Override
        public void onDragStarted(int rawValue) {
            initialLeft = currentMarginDp();
            initialRight = currentRightMarginDp();
        }

        @Override
        public void onDragMoved(int newValue, int rawDeltaDp) {
            applyDelta(rawDeltaDp);
        }

        @Override
        public void onDragEnded(int finalValue, int rawDeltaDp) {
            applyDelta(rawDeltaDp);
        }

        private void applyDelta(int rawDeltaDp) {
            if (symmetricMode) {
                int left = clamp(initialLeft + (isLeft ? rawDeltaDp : -rawDeltaDp),
                        0, KeyboardSettings.MAX_MARGIN_DP);
                int right = clamp(initialRight + (isLeft ? -rawDeltaDp : rawDeltaDp),
                        0, KeyboardSettings.MAX_MARGIN_DP);
                applySidePadding(left, right);
            } else {
                if (isLeft) {
                    applySidePadding(clamp(initialLeft + rawDeltaDp, 0, KeyboardSettings.MAX_MARGIN_DP), initialRight);
                } else {
                    applySidePadding(initialLeft, clamp(initialRight - rawDeltaDp, 0, KeyboardSettings.MAX_MARGIN_DP));
                }
            }
        }

        private void applySidePadding(int left, int right) {
            applySettings(currentMode == KeyboardMode.HANGUL
                    ? currentSettings.withHangulSidePadding(left, right)
                    : currentSettings.withEnglishSidePadding(left, right));
        }
    }

    private class SpecialColumnDragListener implements LayoutEditorHandleView.OnDragListener {
        @Override
        public void onDragStarted(int rawValue) {
        }

        @Override
        public void onDragMoved(int newValue, int rawDeltaDp) {
            applySettings(currentSettings.withHangulSpecialColumnPercent(newValue));
        }

        @Override
        public void onDragEnded(int finalValue, int rawDeltaDp) {
            applySettings(currentSettings.withHangulSpecialColumnPercent(finalValue));
        }
    }

    private class TopPaddingDragListener implements LayoutEditorHandleView.OnDragListener {
        private int initialValue;

        @Override
        public void onDragStarted(int rawValue) {
            this.initialValue = currentSettings.keyboardTopPaddingDp;
        }

        @Override
        public void onDragMoved(int newValue, int rawDeltaDp) {
            int value = clamp(initialValue + rawDeltaDp, 0,
                    KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP);
            applySettings(currentSettings.withLayoutSpacing(
                    currentSettings.hangulMainSpecialGapDp, value,
                    currentSettings.keyboardBottomPaddingDp,
                    currentSettings.bottomRowTopPaddingDp));
        }

        @Override
        public void onDragEnded(int finalValue, int rawDeltaDp) {
            int value = clamp(initialValue + rawDeltaDp, 0,
                    KeyboardSettings.MAX_KEYBOARD_TOP_PADDING_DP);
            applySettings(currentSettings.withLayoutSpacing(
                    currentSettings.hangulMainSpecialGapDp, value,
                    currentSettings.keyboardBottomPaddingDp,
                    currentSettings.bottomRowTopPaddingDp));
        }
    }

    private class BottomPaddingDragListener implements LayoutEditorHandleView.OnDragListener {
        private int initialValue;

        @Override
        public void onDragStarted(int rawValue) {
            this.initialValue = currentSettings.keyboardBottomPaddingDp;
        }

        @Override
        public void onDragMoved(int newValue, int rawDeltaDp) {
            int value = clamp(initialValue - rawDeltaDp, 0,
                    KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP);
            applySettings(currentSettings.withLayoutSpacing(
                    currentSettings.hangulMainSpecialGapDp,
                    currentSettings.keyboardTopPaddingDp, value,
                    currentSettings.bottomRowTopPaddingDp));
        }

        @Override
        public void onDragEnded(int finalValue, int rawDeltaDp) {
            int value = clamp(initialValue - rawDeltaDp, 0,
                    KeyboardSettings.MAX_KEYBOARD_BOTTOM_PADDING_DP);
            applySettings(currentSettings.withLayoutSpacing(
                    currentSettings.hangulMainSpecialGapDp,
                    currentSettings.keyboardTopPaddingDp, value,
                    currentSettings.bottomRowTopPaddingDp));
        }
    }

    private class BottomRowGapDragListener implements LayoutEditorHandleView.OnDragListener {
        private int initialValue;

        @Override
        public void onDragStarted(int rawValue) {
            this.initialValue = currentSettings.bottomRowTopPaddingDp;
        }

        @Override
        public void onDragMoved(int newValue, int rawDeltaDp) {
            int value = clamp(initialValue - rawDeltaDp, 0,
                    KeyboardSettings.MAX_BOTTOM_ROW_TOP_PADDING_DP);
            applySettings(currentSettings.withLayoutSpacing(
                    currentSettings.hangulMainSpecialGapDp,
                    currentSettings.keyboardTopPaddingDp,
                    currentSettings.keyboardBottomPaddingDp, value));
        }

        @Override
        public void onDragEnded(int finalValue, int rawDeltaDp) {
            int value = clamp(initialValue - rawDeltaDp, 0,
                    KeyboardSettings.MAX_BOTTOM_ROW_TOP_PADDING_DP);
            applySettings(currentSettings.withLayoutSpacing(
                    currentSettings.hangulMainSpecialGapDp,
                    currentSettings.keyboardTopPaddingDp,
                    currentSettings.keyboardBottomPaddingDp, value));
        }
    }

    private class HeightDragListener implements LayoutEditorHandleView.OnDragListener {
        @Override
        public void onDragStarted(int rawValue) {
        }

        @Override
        public void onDragMoved(int newValue, int rawDeltaDp) {
            int h = clamp(newValue, KeyboardSettings.MIN_HEIGHT_DP, KeyboardSettings.MAX_HEIGHT_DP);
            applySettings(currentMode == KeyboardMode.HANGUL
                    ? currentSettings.withHangulHeight(h)
                    : currentSettings.withEnglishHeight(h));
        }

        @Override
        public void onDragEnded(int finalValue, int rawDeltaDp) {
            int h = clamp(finalValue, KeyboardSettings.MIN_HEIGHT_DP, KeyboardSettings.MAX_HEIGHT_DP);
            applySettings(currentMode == KeyboardMode.HANGUL
                    ? currentSettings.withHangulHeight(h)
                    : currentSettings.withEnglishHeight(h));
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
