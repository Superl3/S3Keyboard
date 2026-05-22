package com.superl3.s3keyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public final class ThemeEditorActivity extends Activity {
    private static final int MODE_HANGUL_ID = 101;
    private static final int MODE_ENGLISH_ID = 102;
    private static final int EDIT_GLOBAL_ID = 201;
    private static final int EDIT_KEY_TEXT_ID = 202;

    private KeyboardSettings settings;
    private boolean syncing;
    private HangulKeyboardView preview;
    private GestureKey selectedKey;
    private String selectedOverrideKey = "";

    private Spinner keyIdleColorSpinner;
    private Spinner functionKeyColorSpinner;
    private Spinner accentKeyColorSpinner;
    private Spinner keyPressedColorSpinner;
    private Spinner keyboardBackgroundColorSpinner;
    private Spinner accentColorSpinner;
    private Spinner secondaryColorSpinner;
    private Spinner borderColorSpinner;
    private Spinner depthColorSpinner;
    private Spinner fontFamilySpinner;
    private Spinner modifierIconPackSpinner;
    private Spinner keyDisplayPackSpinner;
    private Spinner selectedKeyColorSpinner;
    private Spinner selectedKeyBackgroundColorSpinner;
    private Button addSelectedKeyOverrideButton;
    private Button resetSelectedKeyButton;
    private RadioGroup modeGroup;
    private RadioGroup editScopeGroup;
    private SeekBar roundnessSeekBar;
    private SeekBar keyBorderWidthSeekBar;
    private SeekBar keyGapSeekBar;
    private SeekBar keyDepthSeekBar;
    private SeekBar keyFaceGradientStrengthSeekBar;
    private SeekBar primaryTextSizeSeekBar;
    private SeekBar secondaryTextSizeSeekBar;
    private CheckBox keyDepthCheckBox;
    private CheckBox customDepthColorCheckBox;
    private CheckBox keyFaceGradientCheckBox;
    private CheckBox primaryTextBoldCheckBox;
    private CheckBox primaryTextItalicCheckBox;
    private CheckBox secondaryTextBoldCheckBox;
    private CheckBox secondaryTextItalicCheckBox;
    private TextView selectedKeyLabel;
    private TextView previewMeta;
    private TextView roundnessValue;
    private TextView keyBorderWidthValue;
    private TextView keyGapValue;
    private TextView keyDepthValue;
    private TextView keyFaceGradientStrengthValue;
    private TextView primaryTextSizeValue;
    private TextView secondaryTextSizeValue;
    private View keyIdleColorSwatch;
    private View functionKeyColorSwatch;
    private View accentKeyColorSwatch;
    private View keyPressedColorSwatch;
    private View keyboardBackgroundColorSwatch;
    private View borderColorSwatch;
    private View depthColorSwatch;
    private View accentColorSwatch;
    private View secondaryColorSwatch;
    private View selectedKeyColorSwatch;
    private View selectedKeyBackgroundColorSwatch;
    private final Map<View, TextView> swatchCodeLabels = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        settings = KeyboardPreferences.load(this);
        setContentView(createContentView());
        syncControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        settings = KeyboardPreferences.load(this);
        syncControls();
    }

    private View createContentView() {
        int padding = dp(16);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(padding, padding, padding, padding);
        root.setBackgroundColor(ui.background);

        TextView title = label("테마 편집기");
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title, matchWrap());

        modeGroup = new RadioGroup(this);
        modeGroup.setOrientation(RadioGroup.HORIZONTAL);
        modeGroup.addView(radio(MODE_HANGUL_ID, "딩굴"));
        modeGroup.addView(radio(MODE_ENGLISH_ID, "쿼티"));
        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (syncing) {
                return;
            }
            selectedKey = null;
            selectedOverrideKey = "";
            updateSettings(settings.withKeyboardMode(checkedId == MODE_ENGLISH_ID
                    ? KeyboardMode.ENGLISH
                    : KeyboardMode.HANGUL));
        });
        root.addView(modeGroup, matchWrapWithTop(8));

        previewMeta = label("");
        root.addView(previewMeta, matchWrapWithTop(6));

        preview = new HangulKeyboardView(this);
        preview.setOnPreviewKeySelectionListener(key -> {
            selectedKey = key;
            selectedOverrideKey = overrideKeyFor(key);
            editScopeGroup.check(EDIT_KEY_TEXT_ID);
            syncSelectedKeyInspector();
        });
        root.addView(preview, matchWrapWithTop(8));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(ui.background);
        LinearLayout editorRoot = new LinearLayout(this);
        editorRoot.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(editorRoot);
        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f));

        addThemeSaveControls(editorRoot);
        LinearLayout globalSection = addExpandableSection(editorRoot, "전체", true);
        addColorControls(addExpandableSection(globalSection, "색상", true));
        addShapeControls(addExpandableSection(globalSection, "형태", false));
        addIconPackControls(addExpandableSection(globalSection, "Icon packs", true));
        addTypographyControls(addExpandableSection(globalSection, "글꼴", false));
        addSelectedKeyInspector(editorRoot);
        return root;
    }

    private void addThemeSaveControls(LinearLayout root) {
        Button saveThemeButton = new Button(this);
        saveThemeButton.setText("현재 테마 저장");
        styleSystemButton(saveThemeButton);
        saveThemeButton.setOnClickListener(v -> {
            UserThemeStore.UserTheme saved = UserThemeStore.saveCurrent(this, settings);
            KeyboardPreferences.saveSelectedThemeId(this, saved.id);
            Toast.makeText(this, "테마 저장됨: " + saved.name, Toast.LENGTH_SHORT).show();
        });
        root.addView(saveThemeButton, buttonParams());

        Button exportJsonButton = new Button(this);
        exportJsonButton.setText("테마 JSON 복사");
        styleSystemButton(exportJsonButton);
        exportJsonButton.setOnClickListener(v -> copyThemeJsonToClipboard());
        root.addView(exportJsonButton, buttonParams());

        Button importJsonButton = new Button(this);
        importJsonButton.setText("테마 JSON 가져오기");
        styleSystemButton(importJsonButton);
        importJsonButton.setOnClickListener(v -> showThemeJsonImportDialog());
        root.addView(importJsonButton, buttonParams());
    }

    private void addSelectedKeyInspector(LinearLayout root) {
        LinearLayout section = addExpandableSection(root, "키별 색상 재정의", true);
        selectedKeyLabel = label("선택된 키 없음");
        section.addView(selectedKeyLabel, matchWrapWithTop(8));

        editScopeGroup = new RadioGroup(this);
        editScopeGroup.setOrientation(RadioGroup.HORIZONTAL);
        editScopeGroup.addView(radio(EDIT_GLOBAL_ID, "전체 스타일"));
        editScopeGroup.addView(radio(EDIT_KEY_TEXT_ID, "선택 키"));
        editScopeGroup.check(EDIT_GLOBAL_ID);
        editScopeGroup.setOnCheckedChangeListener((group, checkedId) -> syncSelectedKeyInspector());
        section.addView(editScopeGroup, matchWrapWithTop(8));

        ColorChangeListener selectedKeyTextListener = color -> {
            if (selectedOverrideKey.isEmpty() || editScopeGroup.getCheckedRadioButtonId() != EDIT_KEY_TEXT_ID) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.put(selectedOverrideKey, color);
            updateSettings(settings.withKeyColorOverrides(overrides));
        };
        selectedKeyColorSpinner = colorSpinner(selectedKeyTextListener);
        section.addView(label("선택 키 글자/아이콘 색상"), matchWrapWithTop(8));
        selectedKeyColorSwatch = addInlineSwatch(section, settings.accentColor);
        selectedKeyColorSwatch.setOnClickListener(v ->
                showColorEditDialog("선택 키 글자/아이콘 색상", colorTag(selectedKeyColorSwatch), selectedKeyTextListener));
        section.addView(selectedKeyColorSpinner, matchWrap());

        ColorChangeListener selectedKeyBackgroundListener = color -> {
            if (selectedOverrideKey.isEmpty() || editScopeGroup.getCheckedRadioButtonId() != EDIT_KEY_TEXT_ID) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.put(backgroundOverrideKey(selectedOverrideKey), color);
            updateSettings(settings.withKeyColorOverrides(overrides));
        };
        selectedKeyBackgroundColorSpinner = colorSpinner(selectedKeyBackgroundListener);
        section.addView(label("선택 키 배경색"), matchWrapWithTop(8));
        selectedKeyBackgroundColorSwatch = addInlineSwatch(section, settings.keyIdleColor);
        selectedKeyBackgroundColorSwatch.setOnClickListener(v ->
                showColorEditDialog("선택 키 배경색", colorTag(selectedKeyBackgroundColorSwatch), selectedKeyBackgroundListener));
        section.addView(selectedKeyBackgroundColorSpinner, matchWrap());

        addSelectedKeyOverrideButton = new Button(this);
        addSelectedKeyOverrideButton.setText("+ 선택 키 재정의 추가");
        styleSystemButton(addSelectedKeyOverrideButton);
        addSelectedKeyOverrideButton.setOnClickListener(v -> {
            if (selectedOverrideKey.isEmpty()) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.put(
                    selectedOverrideKey,
                    KeyboardKeyVisualClassifier.textColorFor(settings, selectedKey));
            overrides.put(
                    backgroundOverrideKey(selectedOverrideKey),
                    KeyboardKeyVisualClassifier.colorFor(settings, selectedKey));
            updateSettings(settings.withKeyColorOverrides(overrides));
        });
        section.addView(addSelectedKeyOverrideButton, buttonParams());

        resetSelectedKeyButton = new Button(this);
        resetSelectedKeyButton.setText("선택 키 재정의 초기화");
        styleSystemButton(resetSelectedKeyButton);
        resetSelectedKeyButton.setOnClickListener(v -> {
            if (selectedOverrideKey.isEmpty()) {
                return;
            }
            Map<String, Integer> overrides = new HashMap<>(settings.keyColorOverrides);
            overrides.remove(KeyboardSettings.normalizeKeyOverrideName(selectedOverrideKey));
            overrides.remove(KeyboardSettings.normalizeKeyOverrideName(backgroundOverrideKey(selectedOverrideKey)));
            updateSettings(settings.withKeyColorOverrides(overrides));
        });
        section.addView(resetSelectedKeyButton, buttonParams());
    }

    private void addColorControls(LinearLayout root) {
        ColorChangeListener keyIdleListener = color -> updateSettings(settings.withThemeColors(
                color,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor));
        keyIdleColorSpinner = colorSpinner(keyIdleListener);
        keyIdleColorSwatch = addColorControl(
                root,
                "전체 - 기본 키",
                "글자, 모음, 기호, 스페이스처럼 일반 입력 키의 기본 배경색입니다.",
                keyIdleColorSpinner,
                keyIdleListener);
        root.addView(keyIdleColorSpinner, matchWrap());

        ColorChangeListener functionKeyListener = color -> updateSettings(settings.withExtendedThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor,
                color,
                settings.accentKeyColor,
                settings.borderColor,
                settings.customDepthColorEnabled,
                settings.depthColor));
        functionKeyColorSpinner = colorSpinner(functionKeyListener);
        functionKeyColorSwatch = addColorControl(
                root,
                "전체 - 기능 키",
                "옵션, 예약어, 한/영처럼 입력 보조 동작을 실행하는 키의 배경색입니다.",
                functionKeyColorSpinner,
                functionKeyListener);
        root.addView(functionKeyColorSpinner, matchWrap());
        root.addView(actionButton(
                "기능 키 색상 = 기본 키를 살짝 어둡게",
                v -> functionKeyListener.onColorChanged(dimColor(settings.keyIdleColor, 0.90f))),
                buttonParams());
        ColorChangeListener accentKeyListener = color -> updateSettings(settings.withExtendedThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor,
                settings.functionKeyColor,                color,
                settings.borderColor,
                settings.customDepthColorEnabled,
                settings.depthColor));
        accentKeyColorSpinner = colorSpinner(accentKeyListener);
        accentKeyColorSwatch = addColorControl(
                root,
                "전체 - 강조 키",
                "딩굴 특수열과 테마에서 강조되는 키 그룹의 배경색입니다.",
                accentKeyColorSpinner,
                accentKeyListener);
        root.addView(accentKeyColorSpinner, matchWrap());
        root.addView(actionButton(
                "강조 키 = 기능 키 전경/배경 반전",
                v -> updateSettings(settings.withExtendedThemeColors(
                        settings.keyIdleColor,
                        settings.keyPressedColor,
                        settings.keyboardBackgroundColor,
                        settings.accentColor,
                        settings.functionKeyColor,
                        settings.functionKeyColor,                        settings.secondaryColor,
                        settings.borderColor,
                        settings.customDepthColorEnabled,
                        settings.depthColor))),
                buttonParams());

        ColorChangeListener keyPressedListener = color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                color,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor));
        keyPressedColorSpinner = colorSpinner(keyPressedListener);
        keyPressedColorSwatch = addColorControl(root, "눌림", "키를 누르고 있는 동안 잠시 표시되는 키 배경색입니다.", keyPressedColorSpinner, keyPressedListener);
        root.addView(keyPressedColorSpinner, matchWrap());

        ColorChangeListener keyboardBackgroundListener = color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                color,
                settings.accentColor,
                settings.secondaryColor));
        keyboardBackgroundColorSpinner = colorSpinner(keyboardBackgroundListener);
        keyboardBackgroundColorSwatch = addColorControl(root, "키보드 배경", "키 사이와 키 뒤쪽 영역의 색상입니다.", keyboardBackgroundColorSpinner, keyboardBackgroundListener);
        root.addView(keyboardBackgroundColorSpinner, matchWrap());

        ColorChangeListener borderListener = color -> updateSettings(settings.withExtendedThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                settings.secondaryColor,
                settings.functionKeyColor,
                settings.accentKeyColor,
                color,
                settings.customDepthColorEnabled,
                settings.depthColor));
        borderColorSpinner = colorSpinner(borderListener);
        borderColorSwatch = addColorControl(root, "테두리", "각 키 외곽선 색상입니다. 입체 효과 색상을 따로 지정하지 않으면 이 색상을 기준으로 씁니다.", borderColorSpinner, borderListener);
        root.addView(borderColorSpinner, matchWrap());

        customDepthColorCheckBox = checkBox("입체 효과 색상 직접 지정", checked ->
                updateSettings(settings.withDepthColor(checked, settings.depthColor)));
        root.addView(customDepthColorCheckBox, matchWrapWithTop(12));

        ColorChangeListener depthListener = color -> updateSettings(settings.withDepthColor(true, color));
        depthColorSpinner = colorSpinner(depthListener);
        depthColorSwatch = addColorControl(root, "입체 효과 색상", "키 아래쪽 입체 효과에 쓰는 색상입니다. 입체 효과가 꺼져 있으면 적용되지 않습니다.", depthColorSpinner, depthListener);
        root.addView(depthColorSpinner, matchWrap());

        ColorChangeListener accentListener = color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                color,
                settings.secondaryColor));
        accentColorSpinner = colorSpinner(accentListener);
        accentColorSwatch = addColorControl(root, "주 글자", "키 중앙 글자, 아이콘, 선택 표시, 입력 미리보기의 기본 색상입니다.", accentColorSpinner, accentListener);
        root.addView(accentColorSpinner, matchWrap());

        ColorChangeListener secondaryListener = color -> updateSettings(settings.withThemeColors(
                settings.keyIdleColor,
                settings.keyPressedColor,
                settings.keyboardBackgroundColor,
                settings.accentColor,
                color));
        secondaryColorSpinner = colorSpinner(secondaryListener);
        secondaryColorSwatch = addColorControl(root, "보조 글자", "슬라이드 힌트, 보조 라벨, 비활성 아이콘 디테일의 색상입니다.", secondaryColorSpinner, secondaryListener);
        root.addView(secondaryColorSpinner, matchWrap());
    }

    private void addShapeControls(LinearLayout root) {
        roundnessValue = label("");
        roundnessSeekBar = seekBar(KeyboardSettings.MAX_KEY_ROUNDNESS_DP, progress ->
                updateSettings(settings.withKeyRoundness(progress)));
        root.addView(roundnessValue, matchWrapWithTop(8));
        root.addView(roundnessSeekBar, matchWrap());

        keyBorderWidthValue = label("");
        keyBorderWidthSeekBar = seekBar(KeyboardSettings.MAX_KEY_BORDER_WIDTH_DP, progress ->
                updateSettings(settings.withKeyBorderWidth(progress)));
        root.addView(keyBorderWidthValue, matchWrapWithTop(8));
        root.addView(keyBorderWidthSeekBar, matchWrap());

        keyGapValue = label("");
        keyGapSeekBar = seekBar(KeyboardSettings.MAX_KEY_GAP_DP, progress ->
                updateSettings(settings.withKeyGap(progress)));
        root.addView(keyGapValue, matchWrapWithTop(8));
        root.addView(keyGapSeekBar, matchWrap());

        keyDepthCheckBox = checkBox("입체 효과 사용", checked ->
                updateSettings(settings.withKeyDepth(checked, settings.keyDepthDp)));
        root.addView(keyDepthCheckBox, matchWrapWithTop(12));

        keyDepthValue = label("");
        keyDepthSeekBar = seekBar(KeyboardSettings.MAX_KEY_DEPTH_DP, progress ->
                updateSettings(settings.withKeyDepth(settings.keyDepthEnabled, progress)));
        root.addView(keyDepthValue, matchWrapWithTop(8));
        root.addView(keyDepthSeekBar, matchWrap());

        keyFaceGradientCheckBox = checkBox("\uD0A4 \uD45C\uBA74 \uADF8\uB77C\uB370\uC774\uC158", checked ->
                updateSettings(settings.withVisualEffects(
                        settings.visualEffects.withKeyFaceGradient(
                                checked,
                                settings.visualEffects.keyFaceGradientStrengthPercent))));
        root.addView(keyFaceGradientCheckBox, matchWrapWithTop(12));

        keyFaceGradientStrengthValue = label("");
        keyFaceGradientStrengthSeekBar = seekBar(100, progress ->
                updateSettings(settings.withVisualEffects(
                        settings.visualEffects.withKeyFaceGradient(
                                settings.visualEffects.keyFaceGradientEnabled,
                                progress))));
        root.addView(keyFaceGradientStrengthValue, matchWrapWithTop(8));
        root.addView(keyFaceGradientStrengthSeekBar, matchWrap());
    }

    private void addTypographyControls(LinearLayout root) {
        fontFamilySpinner = fontSpinner();
        root.addView(label("폰트"), matchWrapWithTop(8));
        root.addView(fontFamilySpinner, matchWrap());

        primaryTextSizeValue = label("");
        primaryTextSizeSeekBar = textSizeSeekBar(progress -> updateSettings(settings.withTypography(
                settings.fontFamily,
                KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                settings.secondaryTextSizePercent,
                settings.primaryTextBold,
                settings.primaryTextItalic,
                settings.secondaryTextBold,
                settings.secondaryTextItalic)));
        root.addView(primaryTextSizeValue, matchWrapWithTop(8));
        root.addView(primaryTextSizeSeekBar, matchWrap());

        secondaryTextSizeValue = label("");
        secondaryTextSizeSeekBar = textSizeSeekBar(progress -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                KeyboardSettings.MIN_TEXT_SIZE_PERCENT + progress,
                settings.primaryTextBold,
                settings.primaryTextItalic,
                settings.secondaryTextBold,
                settings.secondaryTextItalic)));
        root.addView(secondaryTextSizeValue, matchWrapWithTop(8));
        root.addView(secondaryTextSizeSeekBar, matchWrap());

        primaryTextBoldCheckBox = checkBox("주 글자 굵게", checked -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                settings.secondaryTextSizePercent,
                checked,
                settings.primaryTextItalic,
                settings.secondaryTextBold,
                settings.secondaryTextItalic)));
        primaryTextItalicCheckBox = checkBox("주 글자 기울임", checked -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                settings.secondaryTextSizePercent,
                settings.primaryTextBold,
                checked,
                settings.secondaryTextBold,
                settings.secondaryTextItalic)));
        secondaryTextBoldCheckBox = checkBox("보조 힌트 굵게", checked -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                settings.secondaryTextSizePercent,
                settings.primaryTextBold,
                settings.primaryTextItalic,
                checked,
                settings.secondaryTextItalic)));
        secondaryTextItalicCheckBox = checkBox("보조 힌트 기울임", checked -> updateSettings(settings.withTypography(
                settings.fontFamily,
                settings.primaryTextSizePercent,
                settings.secondaryTextSizePercent,
                settings.primaryTextBold,
                settings.primaryTextItalic,
                settings.secondaryTextBold,
                checked)));
        root.addView(primaryTextBoldCheckBox, matchWrapWithTop(12));
        root.addView(primaryTextItalicCheckBox, matchWrapWithTop(4));
        root.addView(secondaryTextBoldCheckBox, matchWrapWithTop(8));
        root.addView(secondaryTextItalicCheckBox, matchWrapWithTop(4));
    }

    private void addIconPackControls(LinearLayout root) {
        modifierIconPackSpinner = modifierIconPackSpinner();
        root.addView(label("\uBAA8\uB514\uD30C\uC774\uC5B4 \uC544\uC774\uCF58"), matchWrapWithTop(8));
        root.addView(modifierIconPackSpinner, matchWrap());

        keyDisplayPackSpinner = keyDisplayPackSpinner();
        root.addView(label("Key display override pack"), matchWrapWithTop(8));
        root.addView(keyDisplayPackSpinner, matchWrap());
    }

    private void updateSettings(KeyboardSettings next) {
        settings = next;
        KeyboardPreferences.saveSelectedThemeId(this, "");
        KeyboardPreferences.saveSettings(this, settings);
        syncControls();
    }

    private void syncControls() {
        if (preview == null) {
            return;
        }
        syncing = true;
        modeGroup.check(settings.keyboardMode == KeyboardMode.ENGLISH ? MODE_ENGLISH_ID : MODE_HANGUL_ID);
        setProgress(roundnessSeekBar, settings.keyRoundnessDp);
        setProgress(keyBorderWidthSeekBar, settings.keyBorderWidthDp);
        setProgress(keyGapSeekBar, settings.keyGapDp);
        setProgress(keyDepthSeekBar, settings.keyDepthDp);
        setProgress(
                keyFaceGradientStrengthSeekBar,
                settings.visualEffects.keyFaceGradientStrengthPercent);
        setProgress(primaryTextSizeSeekBar, settings.primaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        setProgress(secondaryTextSizeSeekBar, settings.secondaryTextSizePercent - KeyboardSettings.MIN_TEXT_SIZE_PERCENT);
        setSelection(keyIdleColorSpinner, indexOfColor(settings.keyIdleColor));
        setSelection(functionKeyColorSpinner, indexOfColor(settings.functionKeyColor));
        setSelection(accentKeyColorSpinner, indexOfColor(settings.accentKeyColor));
        setSelection(keyPressedColorSpinner, indexOfColor(settings.keyPressedColor));
        setSelection(keyboardBackgroundColorSpinner, indexOfColor(settings.keyboardBackgroundColor));
        setSelection(accentColorSpinner, indexOfColor(settings.accentColor));
        setSelection(secondaryColorSpinner, indexOfColor(settings.secondaryColor));
        setSelection(borderColorSpinner, indexOfColor(settings.borderColor));
        setSelection(depthColorSpinner, indexOfColor(settings.depthColor));
        setSelection(fontFamilySpinner, indexOfFont(settings.fontFamily));
        setSelection(modifierIconPackSpinner, indexOfModifierIconPack(settings.modifierIconThemePackId));
        setSelection(keyDisplayPackSpinner, indexOfKeyDisplayPack(settings.keyDisplayThemePackId));
        setChecked(keyDepthCheckBox, settings.keyDepthEnabled);
        setChecked(customDepthColorCheckBox, settings.customDepthColorEnabled);
        setChecked(keyFaceGradientCheckBox, settings.visualEffects.keyFaceGradientEnabled);
        setChecked(primaryTextBoldCheckBox, settings.primaryTextBold);
        setChecked(primaryTextItalicCheckBox, settings.primaryTextItalic);
        setChecked(secondaryTextBoldCheckBox, settings.secondaryTextBold);
        setChecked(secondaryTextItalicCheckBox, settings.secondaryTextItalic);
        setEnabled(depthColorSpinner, settings.customDepthColorEnabled);
        setEnabled(keyDepthSeekBar, settings.keyDepthEnabled);
        setEnabled(keyFaceGradientCheckBox, settings.keyDepthEnabled);
        setEnabled(
                keyFaceGradientStrengthSeekBar,
                settings.keyDepthEnabled && settings.visualEffects.keyFaceGradientEnabled);
        setSwatch(keyIdleColorSwatch, settings.keyIdleColor);
        setSwatch(functionKeyColorSwatch, settings.functionKeyColor);
        setSwatch(accentKeyColorSwatch, settings.accentKeyColor);
        setSwatch(keyPressedColorSwatch, settings.keyPressedColor);
        setSwatch(keyboardBackgroundColorSwatch, settings.keyboardBackgroundColor);
        setSwatch(borderColorSwatch, settings.borderColor);
        setSwatch(depthColorSwatch, settings.customDepthColorEnabled ? settings.depthColor : settings.borderColor);
        setSwatch(accentColorSwatch, settings.accentColor);
        setSwatch(secondaryColorSwatch, settings.secondaryColor);

        setText(roundnessValue, "둥글기: " + settings.keyRoundnessDp + "dp");
        setText(keyBorderWidthValue, "테두리 굵기: " + settings.keyBorderWidthDp + "dp");
        setText(keyGapValue, "키 사이 시각 간격: " + settings.keyGapDp + "dp");
        setText(keyDepthValue, "입체 높이: " + settings.keyDepthDp + "dp"
                + (settings.keyDepthEnabled ? "" : " (flat)"));
        setText(
                keyFaceGradientStrengthValue,
                "\uD45C\uBA74 \uADF8\uB77C\uB370\uC774\uC158 \uAC15\uB3C4: "
                        + settings.visualEffects.keyFaceGradientStrengthPercent
                        + "%");
        setText(primaryTextSizeValue, "주 글자 크기: " + settings.primaryTextSizePercent + "%");
        setText(secondaryTextSizeValue, "보조 힌트 크기: " + settings.secondaryTextSizePercent + "%");
        previewMeta.setText((settings.keyboardMode == KeyboardMode.ENGLISH ? "쿼티" : "딩굴")
                + " 미리보기 / "
                + settings.measuredHeightDp()
                + "dp / 키를 눌러 색상 재정의");
        updatePreviewHeight();
        preview.setSettings(settings);
        syncSelectedKeyInspector();
        preview.post(() -> syncing = false);
    }

    private void syncSelectedKeyInspector() {
        boolean keySelected = selectedKey != null && !selectedOverrideKey.isEmpty();
        boolean keyEdit = keySelected && editScopeGroup.getCheckedRadioButtonId() == EDIT_KEY_TEXT_ID;
        KeyVisualRole role = keySelected
                ? KeyboardKeyVisualClassifier.roleFor(settings, selectedKey)
                : KeyVisualRole.ALPHA;
        selectedKeyLabel.setText(keySelected
                ? "선택: " + displayKeyName(selectedKey)
                        + " / 그룹: " + visualRoleLabel(role)
                        + " / 키: " + selectedOverrideKey
                : "선택된 키 없음");
        selectedKeyColorSpinner.setEnabled(keyEdit);
        selectedKeyBackgroundColorSpinner.setEnabled(keyEdit);
        addSelectedKeyOverrideButton.setEnabled(keyEdit);
        boolean hasTextOverride = keySelected && settings.keyColorOverrides.containsKey(
                KeyboardSettings.normalizeKeyOverrideName(selectedOverrideKey));
        boolean hasBackgroundOverride = keySelected && settings.keyColorOverrides.containsKey(
                KeyboardSettings.normalizeKeyOverrideName(backgroundOverrideKey(selectedOverrideKey)));
        resetSelectedKeyButton.setEnabled(keyEdit && (hasTextOverride || hasBackgroundOverride));
        Integer override = keySelected
                ? settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName(selectedOverrideKey))
                : null;
        Integer backgroundOverride = keySelected
                ? settings.keyColorOverrides.get(KeyboardSettings.normalizeKeyOverrideName(
                        backgroundOverrideKey(selectedOverrideKey)))
                : null;
        boolean wasSyncing = syncing;
        syncing = true;
        setSelection(selectedKeyColorSpinner, indexOfColor(override == null ? settings.accentColor : override));
        setSwatch(selectedKeyColorSwatch, override == null ? settings.accentColor : override);
        setSelection(
                selectedKeyBackgroundColorSpinner,
                indexOfColor(backgroundOverride == null
                        ? KeyboardKeyVisualClassifier.colorFor(settings, selectedKey)
                        : backgroundOverride));
        setSwatch(
                selectedKeyBackgroundColorSwatch,
                backgroundOverride == null
                        ? KeyboardKeyVisualClassifier.colorFor(settings, selectedKey)
                        : backgroundOverride);
        syncing = wasSyncing;
    }

    private String backgroundOverrideKey(String key) {
        return "background:" + key;
    }

    private void updatePreviewHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) preview.getLayoutParams();
        params.height = dp(settings.measuredHeightDp());
        preview.setLayoutParams(params);
    }

    private String overrideKeyFor(GestureKey key) {
        if (key == null) {
            return "";
        }
        if (KeyboardCommands.CMD_SPACE.equals(key.tap)) {
            return "space";
        }
        if (KeyboardCommands.CMD_DELETE.equals(key.tap)) {
            return "backspace";
        }
        if (KeyboardCommands.CMD_ENTER.equals(key.tap)) {
            return "enter";
        }
        if (KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap)) {
            return "shift";
        }
        if (KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(key.tap)) {
            return "language";
        }
        if (key.tap != null && !key.tap.isEmpty() && !KeyboardCommands.isCommand(key.tap)) {
            return "tap:" + key.tap;
        }
        return key.label == null ? "" : "label:" + key.label;
    }

    private String displayKeyName(GestureKey key) {
        if (key == null) {
            return "";
        }
        if (key.label != null && !key.label.isEmpty()) {
            return key.label;
        }
        return key.tap == null ? "" : key.tap;
    }

    private String visualRoleLabel(KeyVisualRole role) {
        if (role == null) {
            return "alpha";
        }
        switch (role) {
            case ACCENT:
                return "accent";
            case MODIFIER:
                return "modifier";
            case ALPHA:
            default:
                return "alpha";
        }
    }

    private LinearLayout addExpandableSection(LinearLayout root, String text, boolean expandedByDefault) {
        TextView title = label("");
        title.setTextSize(17);
        title.setGravity(Gravity.START);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setVisibility(expandedByDefault ? View.VISIBLE : View.GONE);
        setExpandableTitle(title, text, expandedByDefault);
        title.setOnClickListener(v -> {
            boolean expanded = content.getVisibility() != View.VISIBLE;
            content.setVisibility(expanded ? View.VISIBLE : View.GONE);
            setExpandableTitle(title, text, expanded);
        });
        root.addView(title, matchWrapWithTop(18));
        root.addView(content, matchWrap());
        return content;
    }

    private void setExpandableTitle(TextView title, String text, boolean expanded) {
        title.setText((expanded ? "- " : "+ ") + text);
    }

    private View addColorControl(
            LinearLayout root,
            String title,
            String description,
            Spinner spinner,
            ColorChangeListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = label(title);
        row.addView(label, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        View swatch = colorSwatch();
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(dp(42), dp(28));
        swatchParams.leftMargin = dp(8);
        row.addView(swatch, swatchParams);

        TextView code = label("");
        code.setTextSize(12);
        LinearLayout.LayoutParams codeParams = new LinearLayout.LayoutParams(dp(86), LinearLayout.LayoutParams.WRAP_CONTENT);
        codeParams.leftMargin = dp(8);
        row.addView(code, codeParams);
        swatchCodeLabels.put(swatch, code);

        TextView info = infoButton();
        info.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("확인", null)
                .show());
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(dp(30), dp(30));
        infoParams.leftMargin = dp(8);
        row.addView(info, infoParams);
        swatch.setOnClickListener(v -> showColorEditDialog(title, colorTag(swatch), listener));
        code.setOnClickListener(v -> showColorEditDialog(title, colorTag(swatch), listener));
        root.addView(row, matchWrapWithTop(8));
        if (spinner != null) {
            spinner.setContentDescription(title);
        }
        return swatch;
    }

    private TextView infoButton() {
        TextView info = new TextView(this);
        info.setText("?");
        info.setGravity(Gravity.CENTER);
        info.setTextSize(13);
        info.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        info.setTextColor(ui.textSecondary);
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(ui.controlFill);
        background.setStroke(Math.max(1, dp(1)), ui.border);
        info.setBackground(background);
        return info;
    }

    private View addInlineSwatch(LinearLayout root, int color) {
        View swatch = colorSwatch();
        setSwatch(swatch, color);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(18));
        params.topMargin = dp(4);
        params.bottomMargin = dp(4);
        root.addView(swatch, params);
        return swatch;
    }

    private View colorSwatch() {
        View swatch = new View(this);
        swatch.setMinimumHeight(dp(18));
        return swatch;
    }

    private void setSwatch(View swatch, int color) {
        if (swatch == null) {
            return;
        }
        int opaqueColor = 0xFF000000 | (color & 0x00FFFFFF);
        GradientDrawable background = new GradientDrawable();
        background.setColor(opaqueColor);
        background.setCornerRadius(dp(6));
        background.setStroke(Math.max(1, dp(1)), SettingsUiPalette.from(this).border);
        swatch.setBackground(background);
        swatch.setTag(opaqueColor);
        TextView code = swatchCodeLabels.get(swatch);
        if (code != null) {
            code.setText(colorHex(opaqueColor));
        }
    }

    private int colorTag(View swatch) {
        Object tag = swatch == null ? null : swatch.getTag();
        return tag instanceof Integer ? (Integer) tag : 0xFF000000;
    }

    private void showColorEditDialog(String title, int currentColor, ColorChangeListener listener) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(12), dp(8), dp(12), dp(4));

        TextView description = label("프리셋을 고르거나 hex 색상 값을 직접 입력하세요.");
        layout.addView(description, matchWrap());

        EditText editor = new EditText(this);
        editor.setSingleLine(true);
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editor.setText(colorHex(currentColor).substring(1));
        editor.setSelectAllOnFocus(true);
        SettingsViewStyler.editText(editor, this);
        layout.addView(editor, matchWrapWithTop(8));

        LinearLayout presetGrid = new LinearLayout(this);
        presetGrid.setOrientation(LinearLayout.VERTICAL);
        layout.addView(presetGrid, matchWrapWithTop(10));
        addPresetColorRows(presetGrid, listener);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setNegativeButton("취소", null)
                .setPositiveButton("적용", (dialog, which) -> {
                    Integer parsed = parseHexColor(editor.getText().toString());
                    if (parsed != null) {
                        listener.onColorChanged(parsed);
                    }
                })
                .show();
    }

    private void addPresetColorRows(LinearLayout root, ColorChangeListener listener) {
        LinearLayout row = null;
        for (int i = 0; i < ColorOption.EDITOR_OPTIONS.length; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                root.addView(row, matchWrapWithTop(i == 0 ? 0 : 6));
            }
            ColorOption option = ColorOption.EDITOR_OPTIONS[i];
            Button button = new Button(this);
            button.setText(option.label);
            button.setAllCaps(false);
            button.setTextSize(11);
            button.setTextColor(contrastColor(option.color));
            GradientDrawable background = new GradientDrawable();
            background.setColor(option.color);
            background.setCornerRadius(dp(7));
            background.setStroke(Math.max(1, dp(1)), SettingsUiPalette.from(this).border);
            button.setBackground(background);
            button.setOnClickListener(v -> listener.onColorChanged(option.color));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(40), 1f);
            params.leftMargin = dp(i % 3 == 0 ? 0 : 6);
            row.addView(button, params);
        }
    }

    private Integer parseHexColor(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() != 6 && normalized.length() != 8) {
            return null;
        }
        try {
            long parsed = Long.parseLong(normalized, 16);
            if (normalized.length() == 6) {
                parsed |= 0xFF000000L;
            }
            return (int) parsed;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String colorHex(int color) {
        return String.format("#%06X", color & 0x00FFFFFF);
    }

    private int dimColor(int color, float factor) {
        int red = Math.max(0, Math.min(255, Math.round(((color >> 16) & 0xFF) * factor)));
        int green = Math.max(0, Math.min(255, Math.round(((color >> 8) & 0xFF) * factor)));
        int blue = Math.max(0, Math.min(255, Math.round((color & 0xFF) * factor)));
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private int contrastColor(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return red * 299 + green * 587 + blue * 114 > 150000 ? 0xFF111827 : 0xFFFFFFFF;
    }

    private Button actionButton(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        styleSystemButton(button);
        button.setOnClickListener(listener);
        return button;
    }

    private void copyThemeJsonToClipboard() {
        String json = KeyboardThemeJson.exportTheme(settings, "Current Theme", "local", null);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("New Dingul theme JSON", json));
        }
        Toast.makeText(this, "테마 JSON을 복사했습니다", Toast.LENGTH_SHORT).show();
    }

    private void showThemeJsonImportDialog() {
        EditText editor = new EditText(this);
        editor.setMinLines(8);
        editor.setGravity(Gravity.TOP | Gravity.START);
        editor.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        SettingsViewStyler.editText(editor, this);
        String clipboardText = currentClipboardText();
        if (!clipboardText.isEmpty()) {
            editor.setText(clipboardText);
            editor.setSelection(editor.length());
        }
        new AlertDialog.Builder(this)
                .setTitle("테마 JSON 가져오기")
                .setView(editor)
                .setNegativeButton("취소", null)
                .setPositiveButton("가져오기", (dialog, which) -> importThemeJson(editor.getText().toString()))
                .show();
    }

    private String currentClipboardText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip() || clipboard.getPrimaryClip() == null
                || clipboard.getPrimaryClip().getItemCount() == 0) {
            return "";
        }
        CharSequence text = clipboard.getPrimaryClip().getItemAt(0).coerceToText(this);
        return text == null ? "" : text.toString();
    }

    private void importThemeJson(String json) {
        try {
            settings = KeyboardThemeJson.importTheme(settings, json);
            selectedKey = null;
            selectedOverrideKey = "";
            KeyboardPreferences.saveSelectedThemeId(this, "");
            KeyboardPreferences.saveSettings(this, settings);
            syncControls();
            Toast.makeText(this, "테마 JSON을 가져왔습니다", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException exception) {
            new AlertDialog.Builder(this)
                    .setTitle("테마 JSON 가져오기 실패")
                    .setMessage(exception.getMessage())
                    .setPositiveButton("확인", null)
                    .show();
        }
    }

    private Spinner colorSpinner(final ColorChangeListener listener) {
        Spinner spinner = new Spinner(this);
        spinner.setTag(Boolean.FALSE);
        ArrayAdapter<ColorOption> adapter = new SettingsArrayAdapter<>(
                this,
                ColorOption.EDITOR_OPTIONS);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (Boolean.FALSE.equals(spinner.getTag())) {
                    spinner.setTag(Boolean.TRUE);
                    return;
                }
                if (!syncing) {
                    listener.onColorChanged(ColorOption.EDITOR_OPTIONS[position].color);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner fontSpinner() {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<FontOption> adapter = new SettingsArrayAdapter<>(
                this,
                FontOption.EDITOR_OPTIONS);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    updateSettings(settings.withFontFamily(FontOption.EDITOR_OPTIONS[position].value));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner modifierIconPackSpinner() {
        Spinner spinner = new Spinner(this);
        String[] ids = ModifierIconCatalog.selectablePackIds(false);
        String[] labels = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            labels[i] = ModifierIconCatalog.displayName(ids[i]);
        }
        ArrayAdapter<String> adapter = new SettingsArrayAdapter<>(this, labels);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    updateSettings(settings.withModifierIconThemePack(ids[position]));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private Spinner keyDisplayPackSpinner() {
        Spinner spinner = new Spinner(this);
        String[] ids = KeyDisplayOverridePackCatalog.selectablePackIds(false);
        String[] labels = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            labels[i] = KeyDisplayOverridePackCatalog.displayName(ids[i]);
        }
        ArrayAdapter<String> adapter = new SettingsArrayAdapter<>(this, labels);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!syncing) {
                    updateSettings(settings.withKeyDisplayThemePack(ids[position]));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    private CheckBox checkBox(String label, BooleanChangeListener listener) {
        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(label);
        SettingsViewStyler.compoundButton(checkBox, this);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!syncing) {
                    listener.onChanged(isChecked);
                }
            }
        });
        return checkBox;
    }

    private SeekBar seekBar(int max, IntChangeListener listener) {
        SeekBar seekBar = new SeekBar(this);
        seekBar.setMax(max);
        seekBar.setOnSeekBarChangeListener(new SimpleSeekListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !syncing) {
                    listener.onChanged(progress);
                }
            }
        });
        return seekBar;
    }

    private SeekBar textSizeSeekBar(IntChangeListener listener) {
        return seekBar(KeyboardSettings.MAX_TEXT_SIZE_PERCENT - KeyboardSettings.MIN_TEXT_SIZE_PERCENT, listener);
    }

    private RadioButton radio(int id, String label) {
        RadioButton button = new RadioButton(this);
        button.setId(id);
        button.setText(label);
        SettingsViewStyler.compoundButton(button, this);
        return button;
    }

    private TextView label(String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(SettingsUiPalette.from(this).textPrimary);
        label.setTextSize(14);
        label.setGravity(Gravity.START);
        return label;
    }

    private void styleSystemButton(Button button) {
        SettingsViewStyler.button(button, this, false);
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(10);
        return params;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams matchWrapWithTop(int topMarginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void setProgress(SeekBar seekBar, int progress) {
        if (seekBar != null) {
            seekBar.setProgress(progress);
        }
    }

    private void setSelection(Spinner spinner, int position) {
        if (spinner != null && position >= 0) {
            spinner.setSelection(position, false);
        }
    }

    private void setChecked(CheckBox checkBox, boolean checked) {
        if (checkBox != null) {
            checkBox.setChecked(checked);
        }
    }

    private void setText(TextView view, String text) {
        if (view != null) {
            view.setText(text);
        }
    }

    private void setEnabled(View view, boolean enabled) {
        if (view != null) {
            view.setEnabled(enabled);
        }
    }

    private int indexOfColor(Integer color) {
        if (color == null) {
            return 0;
        }
        int opaqueColor = 0xFF000000 | (color & 0x00FFFFFF);
        for (int i = 0; i < ColorOption.EDITOR_OPTIONS.length; i++) {
            if (ColorOption.EDITOR_OPTIONS[i].color == opaqueColor) {
                return i;
            }
        }
        return -1;
    }

    private int indexOfFont(String fontFamily) {
        String normalized = KeyboardSettings.normalizeFontFamily(fontFamily);
        for (int i = 0; i < FontOption.EDITOR_OPTIONS.length; i++) {
            if (FontOption.EDITOR_OPTIONS[i].value.equals(normalized)) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfModifierIconPack(String packId) {
        String[] ids = ModifierIconCatalog.selectablePackIds(false);
        String normalized = ModifierIconCatalog.normalizePackId(packId);
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(normalized)) {
                return i;
            }
        }
        return 0;
    }

    private int indexOfKeyDisplayPack(String packId) {
        String[] ids = KeyDisplayOverridePackCatalog.selectablePackIds(false);
        String normalized = KeyDisplayOverridePackCatalog.normalizePackId(packId);
        for (int i = 0; i < ids.length; i++) {
            if (ids[i].equals(normalized)) {
                return i;
            }
        }
        return 0;
    }

    private interface ColorChangeListener {
        void onColorChanged(int color);
    }

    private interface BooleanChangeListener {
        void onChanged(boolean checked);
    }

    private interface IntChangeListener {
        void onChanged(int value);
    }

    private abstract static class SimpleSeekListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

}
