package com.superl3.s3keyboard;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.Collections;

public final class LayoutEditorActivity extends Activity {
    private static final int STATUS_BAR_DP = 28;
    private static final int TOOLBAR_HEIGHT_DP = 64;
    private static final int BAR_THICK_DP = 10;
    private static final int BAR_LEN_DP = 48;
    private static final int HANDLE_TARGET_DP = 48;
    private static final int MIN_HANDLE_OFFSET_PX = 8;

    private KeyboardSettings currentSettings;
    private KeyboardLayoutProfiles currentProfiles;
    private KeyboardMode currentMode = KeyboardMode.HANGUL;
    private LayoutEditorState editorState;
    private LayoutGeometrySnapshot geometrySnapshot = LayoutGeometrySnapshot.empty();

    private HangulKeyboardView preview;
    private FrameLayout keyboardArea;
    private LayoutEditorOverlay overlay;
    private LayoutEditorHandleView leftMarginHandle;
    private LayoutEditorHandleView rightMarginHandle;
    private LayoutEditorHandleView keyGapHandle;
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
    private boolean handlesVisible = true;
    private CheckBox symmetricCheckBox;
    private LinearLayout specialColumnRow;
    private LinearLayout inspectorContainer;
    private LinearLayout inspectorPicker;
    private TextView inspectorTitle;
    private NumericStepperRow inspectorStepper;
    private LayoutEditorState.Control inspectorControl;

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
        editorState = LayoutEditorState.from(currentSettings, currentMode);
        setContentView(createContentView());
        getWindow().getDecorView().post(this::updateEditorGestureExclusion);
        updatePreview();
        syncAllControls();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateEditorGestureExclusion();
        }
    }

    private void updateEditorGestureExclusion() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }
        View decor = getWindow().getDecorView();
        int width = decor.getWidth();
        int height = decor.getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        // The editor is an intentional full-screen manipulation surface. Unlike the IME,
        // it may claim the side gesture bands so a margin handle can be dragged from the edge.
        decor.setSystemGestureExclusionRects(Collections.singletonList(
                new Rect(0, 0, width, height)));
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

        // Keep the toolbar out of the weighted preview/editor area. A WRAP_CONTENT
        // toolbar can consume the whole window on some Android framework versions.
        HorizontalScrollView toolbarScroll = new HorizontalScrollView(this);
        toolbarScroll.setHorizontalScrollBarEnabled(false);
        toolbarScroll.setFillViewport(false);
        toolbarScroll.addView(buildToolbar(ui), new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(toolbarScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(TOOLBAR_HEIGHT_DP)));

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
        preview.setPreviewErgonomicsEnabled(true);
        preview.setCompactPreviewRendering(false);
        // Keep the preview drawable. The overlay sits above it and consumes no
        // touch events, so the editor can remain non-interactive without using
        // View#setEnabled(false), which can suppress the custom keyboard surface
        // on some framework/rendering paths.
        preview.setEnabled(true);
        preview.setClickable(false);
        preview.setFocusable(false);
        preview.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        preview.addOnLayoutChangeListener((v, left, top, right, bottom,
                                            oldLeft, oldTop, oldRight, oldBottom) -> {
            refreshGeometryAndHandles();
        });
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

        keyGapHandle = new LayoutEditorHandleView(this, currentSettings.accentColor,
                0, KeyboardSettings.MAX_KEY_GAP_DP, currentKeyGapDp(), true, false,
                new KeyGapDragListener(), "dp",
                LayoutEditorHandleView.STYLE_VERTICAL_BAR);
        keyGapHandle.setLayoutParams(new FrameLayout.LayoutParams(thickPx, lenPx));
        keyboardArea.addView(keyGapHandle);

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
        spacer.setLayoutParams(new LinearLayout.LayoutParams(dp(16),
                ViewGroup.LayoutParams.MATCH_PARENT));
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
            handlesVisible = isChecked;
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

        panel.addView(buildInspector(ui), SettingsRowBuilder.matchWrap());

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
        // The compact inspector is the primary editor. Keep the old controls
        // available in code for compatibility, but do not present a second
        // competing scroll surface to users.
        scroll.setVisibility(View.GONE);
        panel.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return panel;
    }

    private View buildInspector(SettingsUiPalette ui) {
        LinearLayout inspector = new LinearLayout(this);
        inspector.setOrientation(LinearLayout.VERTICAL);
        inspector.setPadding(dp(12), dp(4), dp(12), dp(4));
        inspector.setBackgroundColor(ui.surfaceRaised);

        inspectorTitle = new TextView(this);
        inspectorTitle.setText("선택한 항목");
        inspectorTitle.setTextColor(ui.textPrimary);
        inspectorTitle.setTextSize(14);
        inspector.addView(inspectorTitle, SettingsRowBuilder.matchWrap());

        HorizontalScrollView pickerScroll = new HorizontalScrollView(this);
        pickerScroll.setHorizontalScrollBarEnabled(false);
        inspectorPicker = new LinearLayout(this);
        inspectorPicker.setOrientation(LinearLayout.HORIZONTAL);
        for (LayoutEditorState.Control control : LayoutEditorState.Control.values()) {
            Button button = SettingsRowBuilder.button(this, controlLabel(control));
            button.setTag(control);
            button.setOnClickListener(v -> selectControl(control));
            inspectorPicker.addView(button, SettingsRowBuilder.wrapContentWithLeft(this, 4));
        }
        pickerScroll.addView(inspectorPicker, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        inspector.addView(pickerScroll, SettingsRowBuilder.matchWrap());

        inspectorContainer = new LinearLayout(this);
        inspectorContainer.setOrientation(LinearLayout.VERTICAL);
        inspector.addView(inspectorContainer, SettingsRowBuilder.matchWrap());
        inspector.addView(SettingsRowBuilder.label(this,
                "드래그하거나 수치를 직접 입력할 수 있습니다. 값은 적용 전까지 임시 상태입니다."),
                SettingsRowBuilder.matchWrap());
        return inspector;
    }

    private String controlLabel(LayoutEditorState.Control control) {
        switch (control) {
            case LEFT_PADDING: return "왼쪽 여백";
            case RIGHT_PADDING: return "오른쪽 여백";
            case KEY_GAP: return "키 간격";
            case HEIGHT: return "높이";
            case HANGUL_SPECIAL_GAP: return "특수열 간격";
            case HANGUL_SPECIAL_COLUMN: return "특수열 비율";
            case TOP_PADDING: return "상단 여백";
            case BOTTOM_ROW_PADDING: return "하단 행 위";
            case BOTTOM_PADDING: return "하단 여백";
            case NUMBER_ROW_GAP: return "숫자줄 간격";
            default: return "값";
        }
    }

    private void selectControl(LayoutEditorState.Control control) {
        if (!isControlAvailable(control)) {
            return;
        }
        editorState = editorState.select(control);
        syncInspector();
        syncHandles();
    }

    private void syncInspector() {
        if (inspectorContainer == null || editorState == null) {
            return;
        }
        if (inspectorPicker != null) {
            for (int i = 0; i < inspectorPicker.getChildCount(); i++) {
                View child = inspectorPicker.getChildAt(i);
                if (!(child instanceof Button)) {
                    continue;
                }
                LayoutEditorState.Control control = (LayoutEditorState.Control) child.getTag();
                child.setVisibility(isControlAvailable(control) ? View.VISIBLE : View.GONE);
                SettingsViewStyler.button((Button) child,
                        this,
                        control == editorState.selectedControl);
            }
        }
        inspectorTitle.setText(controlLabel(editorState.selectedControl)
                + "  " + editorState.value() + valueUnit(editorState.selectedControl));
        if (inspectorStepper == null || inspectorControl != editorState.selectedControl) {
            inspectorContainer.removeAllViews();
            inspectorControl = editorState.selectedControl;
            inspectorStepper = new NumericStepperRow(
                    this,
                    editorState.value(),
                    editorState.minValue(),
                    editorState.maxValue(),
                    1,
                    0,
                    value -> applySettings(editorState.withValue(value).settings));
            inspectorContainer.addView(inspectorStepper, SettingsRowBuilder.matchWrap());
        } else {
            inspectorStepper.syncValue(editorState.value());
        }
    }

    private String valueUnit(LayoutEditorState.Control control) {
        return control == LayoutEditorState.Control.HANGUL_SPECIAL_COLUMN ? "%" : "dp";
    }

    private boolean isControlAvailable(LayoutEditorState.Control control) {
        return currentMode == KeyboardMode.HANGUL
                || (control != LayoutEditorState.Control.HANGUL_SPECIAL_GAP
                && control != LayoutEditorState.Control.HANGUL_SPECIAL_COLUMN);
    }

    private void setMode(KeyboardMode mode) {
        if (currentMode == mode) {
            return;
        }
        currentMode = mode;
        editorState = editorState.withMode(mode);
        if (!isControlAvailable(editorState.selectedControl)) {
            editorState = editorState.select(LayoutEditorState.Control.LEFT_PADDING);
        }
        applySettings(editorState.settings);
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
        editorState = LayoutEditorState.from(defaults, currentMode);
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
        editorState = LayoutEditorState.from(newSettings, currentMode)
                .select(editorState == null
                        ? LayoutEditorState.Control.LEFT_PADDING
                        : editorState.selectedControl);
        updatePreview();
        updateOverlay();
        syncSeekBars();
        syncHandles();
        syncInspector();
        keyboardArea.postOnAnimation(this::refreshGeometryAndHandles);
    }

    private void updatePreview() {
        preview.setSettings(RuntimeDefaults.keyboardSettings(currentSettings));
        preview.setLayoutProfiles(currentProfiles);
        preview.requestLayout();
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
        overlay.setGeometry(geometrySnapshot);
    }

    private void refreshGeometryAndHandles() {
        if (preview == null || keyboardArea == null) {
            return;
        }
        geometrySnapshot = LayoutGeometrySnapshot.from(
                preview.accessibilityKeySlots(),
                preview.getLeft(),
                preview.getTop());
        overlay.setGeometry(geometrySnapshot);
        positionHandles();
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

    private int currentKeyGapDp() {
        return currentMode == KeyboardMode.ENGLISH
                ? currentSettings.englishKeyGapDp
                : currentSettings.hangulKeyGapDp;
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
        keyGapHandle.setValue(currentKeyGapDp());
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
        specialColumnHandle.setVisibility(showSpecial && handlesVisible ? View.VISIBLE : View.GONE);
        specialColumnRow.setVisibility(showSpecial ? View.VISIBLE : View.GONE);
    }

    private void updateButtonState(Button button, boolean selected) {
        SettingsViewStyler.button(button, this, selected);
    }

    private void syncAllControls() {
        syncSeekBars();
        syncHandles();
        syncToolbarButtons();
        syncInspector();
        keyboardArea.postOnAnimation(this::refreshGeometryAndHandles);
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
        float maxRight = -Float.MAX_VALUE;
        float minTop = Float.MAX_VALUE;
        float maxBottom = -Float.MAX_VALUE;
        for (HangulKeyboardView.KeySlot slot : slots) {
            RectF b = slot.visualBounds();
            if (b.left < minLeft) minLeft = b.left;
            if (b.right > maxRight) maxRight = b.right;
            if (b.top < minTop) minTop = b.top;
            if (b.bottom > maxBottom) maxBottom = b.bottom;
        }

        int kbLeft = (int) minLeft;
        int kbRight = (int) maxRight;
        // Vertical handles are controls for the primary character grid, not a second
        // keyboard. Keep their anchor on the main key group so they visually belong to
        // the rendered keys even when the bottom control row is much lower.
        float primaryMinTop = Float.MAX_VALUE;
        float primaryMaxBottom = -Float.MAX_VALUE;
        for (HangulKeyboardView.KeySlot slot : slots) {
            boolean primaryKey = currentMode == KeyboardMode.HANGUL
                    ? slot.dingulMainKey
                    : !slot.primaryBottomControl && !slot.compactSpecialColumn;
            if (!primaryKey) {
                continue;
            }
            RectF bounds = slot.visualBounds();
            primaryMinTop = Math.min(primaryMinTop, bounds.top);
            primaryMaxBottom = Math.max(primaryMaxBottom, bounds.bottom);
        }
        if (primaryMinTop == Float.MAX_VALUE || primaryMaxBottom == -Float.MAX_VALUE) {
            primaryMinTop = minTop;
            primaryMaxBottom = maxBottom;
        }
        int charTopY = (int) primaryMinTop;
        int charBottomY = (int) primaryMaxBottom;
        int charCenterY = (charTopY + charBottomY) / 2;
        // KeySlot bounds are local to the preview view. Handles live directly in
        // keyboardArea, so include the preview's top offset before positioning them.
        int charCenterAreaY = kbTop + charCenterY;

        int marginPx = kbLeft;
        int rightMarginPx = areaW - kbRight;
        int thickHalf = dp(BAR_THICK_DP) / 2;
        int barLenHalf = dp(BAR_LEN_DP) / 2;
        int handleTargetHalf = dp(HANDLE_TARGET_DP) / 2;
        int topPadPx = dp(currentSettings.keyboardTopPaddingDp);
        int bottomPadPx = dp(currentSettings.keyboardBottomPaddingDp);
        int bottomRowH = dp(KeyboardSettings.DEFAULT_BOTTOM_CONTROL_ROW_HEIGHT_DP);
        int bottomRowGapPx = dp(currentSettings.bottomRowTopPaddingDp);
        int bottomRowTopY = kbBottom - bottomPadPx - bottomRowH;

        FrameLayout.LayoutParams lp;

        lp = (FrameLayout.LayoutParams) leftMarginHandle.getLayoutParams();
        lp.leftMargin = Math.max(marginPx - handleTargetHalf, -handleTargetHalf + MIN_HANDLE_OFFSET_PX);
        lp.topMargin = charCenterAreaY - handleTargetHalf;
        lp.gravity = Gravity.TOP | Gravity.START;
        leftMarginHandle.setLayoutParams(lp);

        lp = (FrameLayout.LayoutParams) rightMarginHandle.getLayoutParams();
        lp.leftMargin = Math.min(areaW - rightMarginPx - handleTargetHalf,
                areaW - handleTargetHalf - MIN_HANDLE_OFFSET_PX);
        lp.topMargin = charCenterAreaY - handleTargetHalf;
        lp.gravity = Gravity.TOP | Gravity.START;
        rightMarginHandle.setLayoutParams(lp);

        HangulKeyboardView.KeySlot firstGapKey = null;
        HangulKeyboardView.KeySlot secondGapKey = null;
        for (HangulKeyboardView.KeySlot slot : slots) {
            RectF bounds = slot.visualBounds();
            if (Math.abs(bounds.top - minTop) > dp(2)) {
                continue;
            }
            if (firstGapKey == null || bounds.left < firstGapKey.visualBounds().left) {
                secondGapKey = firstGapKey;
                firstGapKey = slot;
            } else if (secondGapKey == null || bounds.left < secondGapKey.visualBounds().left) {
                secondGapKey = slot;
            }
        }
        if (firstGapKey != null && secondGapKey != null) {
            RectF firstBounds = firstGapKey.visualBounds();
            RectF secondBounds = secondGapKey.visualBounds();
            lp = (FrameLayout.LayoutParams) keyGapHandle.getLayoutParams();
            lp.leftMargin = Math.round((firstBounds.right + secondBounds.left) / 2f)
                    - handleTargetHalf;
            lp.topMargin = charCenterAreaY - handleTargetHalf;
            lp.gravity = Gravity.TOP | Gravity.START;
            keyGapHandle.setLayoutParams(lp);
            keyGapHandle.setVisibility(handlesVisible ? View.VISIBLE : View.GONE);
        } else {
            // Do not leave a stale handle at the previous layout's coordinate.
            keyGapHandle.setVisibility(View.GONE);
        }

        if (currentMode == KeyboardMode.HANGUL) {
            HangulKeyboardView.KeySlot thirdKey = null;
            HangulKeyboardView.KeySlot fourthKey = null;
            float mainTop = Float.MAX_VALUE;
            for (HangulKeyboardView.KeySlot slot : slots) {
                if (slot.dingulMainKey) {
                    mainTop = Math.min(mainTop, slot.visualBounds().top);
                }
            }
            for (HangulKeyboardView.KeySlot slot : slots) {
                RectF bounds = slot.visualBounds();
                if (Math.abs(bounds.top - mainTop) > dp(2)) {
                    continue;
                }
                if (slot.dingulMainKey) {
                    if (thirdKey == null || bounds.left > thirdKey.visualBounds().left) {
                        thirdKey = slot;
                    }
                } else if (slot.compactSpecialColumn && fourthKey == null) {
                    fourthKey = slot;
                }
            }
            if (thirdKey != null && fourthKey != null) {
                RectF thirdBounds = thirdKey.visualBounds();
                RectF fourthBounds = fourthKey.visualBounds();
                int dividerX = (int) ((thirdBounds.right + fourthBounds.left) / 2f);

                lp = (FrameLayout.LayoutParams) specialColumnHandle.getLayoutParams();
                lp.leftMargin = dividerX - handleTargetHalf;
                lp.topMargin = charCenterAreaY - handleTargetHalf;
                lp.gravity = Gravity.TOP | Gravity.START;
                specialColumnHandle.setLayoutParams(lp);
                specialColumnHandle.setVisibility(handlesVisible ? View.VISIBLE : View.GONE);
            } else {
                specialColumnHandle.setVisibility(View.GONE);
            }
        }

        lp = (FrameLayout.LayoutParams) topPaddingHandle.getLayoutParams();
        lp.topMargin = kbTop + Math.max(topPadPx / 2 - handleTargetHalf,
                -handleTargetHalf + MIN_HANDLE_OFFSET_PX);
        lp.gravity = Gravity.TOP | Gravity.START;
        topPaddingHandle.setLayoutParams(lp);

        lp = (FrameLayout.LayoutParams) bottomRowGapHandle.getLayoutParams();
        lp.topMargin = bottomRowTopY - Math.max(bottomRowGapPx / 2 - handleTargetHalf,
                -handleTargetHalf + MIN_HANDLE_OFFSET_PX);
        lp.gravity = Gravity.TOP | Gravity.START;
        bottomRowGapHandle.setLayoutParams(lp);

        lp = (FrameLayout.LayoutParams) bottomPaddingHandle.getLayoutParams();
        lp.topMargin = kbBottom - Math.max(bottomPadPx / 2 + handleTargetHalf,
                handleTargetHalf + MIN_HANDLE_OFFSET_PX);
        lp.gravity = Gravity.TOP | Gravity.START;
        bottomPaddingHandle.setLayoutParams(lp);

        lp = (FrameLayout.LayoutParams) heightHandle.getLayoutParams();
        // Height is anchored to the actual keyboard top, so dragging it never
        // changes its visual reference point while the preview is remeasured.
        lp.topMargin = Math.max(-handleTargetHalf + MIN_HANDLE_OFFSET_PX,
                kbTop - handleTargetHalf);
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
            selectControl(isLeft
                    ? LayoutEditorState.Control.LEFT_PADDING
                    : LayoutEditorState.Control.RIGHT_PADDING);
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
            selectControl(LayoutEditorState.Control.HANGUL_SPECIAL_COLUMN);
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

    private class KeyGapDragListener implements LayoutEditorHandleView.OnDragListener {
        @Override
        public void onDragStarted(int rawValue) {
            selectControl(LayoutEditorState.Control.KEY_GAP);
        }

        @Override
        public void onDragMoved(int newValue, int rawDeltaDp) {
            applySettings(editorState.withValue(newValue).settings);
        }

        @Override
        public void onDragEnded(int finalValue, int rawDeltaDp) {
            applySettings(editorState.withValue(finalValue).settings);
        }
    }

    private class TopPaddingDragListener implements LayoutEditorHandleView.OnDragListener {
        private int initialValue;

        @Override
        public void onDragStarted(int rawValue) {
            selectControl(LayoutEditorState.Control.TOP_PADDING);
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
            selectControl(LayoutEditorState.Control.BOTTOM_PADDING);
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
            selectControl(LayoutEditorState.Control.BOTTOM_ROW_PADDING);
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
            selectControl(LayoutEditorState.Control.HEIGHT);
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
