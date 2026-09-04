package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.xml.sax.InputSource;

public final class ProductionReadinessConfigTest {
    private static final String[] MOJIBAKE_MARKERS = {
            "\uFFFD",
            "\u5BC3",
            "\u71AC",
            "\u75AB",
            "\u63F6",
            "\u7670",
            "\u91CE",
            "\u7B4C",
            "\u6028",
            "\u8881",
            "\uF9CF",
            "\uF9C1",
            "\uF9D4"
    };

    @Test
    public void subtypeDeclaresAsciiCapableWithLegacyExtraValue() throws Exception {
        String methodXml = readWorkspaceFile("app/src/main/res/xml/method.xml");

        assertTrue(methodXml.contains("android:isAsciiCapable=\"true\""));
        assertTrue(methodXml.contains("android:imeSubtypeExtraValue=\"AsciiCapable\""));
    }

    @Test
    public void mainTextSourcesDoNotContainKnownMojibakeMarkers() throws Exception {
        Path root = findWorkspaceRoot();
        List<Path> files = new ArrayList<>();
        collectTextFiles(root.resolve("app/src/main/java"), files);
        collectTextFiles(root.resolve("app/src/main/res/values"), files);

        for (Path file : files) {
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            for (String marker : MOJIBAKE_MARKERS) {
                assertFalse(
                        "Mojibake marker '" + printable(marker) + "' found in " + root.relativize(file),
                        text.contains(marker));
            }
        }
    }

    @Test
    public void stringResourcesStayValidUtf8XmlWithoutBrokenClosingTags() throws Exception {
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(strings)));
        assertFalse(strings.matches("(?s).*[^<]/string>.*"));
        assertFalse(strings.contains("\uFFFD"));
        assertFalse(strings.contains("\u5360"));
        assertTrue(strings.contains("name=\"theme_key_display_override_pack\""));
        assertTrue(strings.contains("name=\"accent_placement_title\""));
    }

    @Test
    public void manifestKeepsKeyboardLocalWithoutNetworkPermission() throws Exception {
        String manifest = readWorkspaceFile("app/src/main/AndroidManifest.xml");

        assertTrue(manifest.contains("android:allowBackup=\"false\""));
        assertFalse(manifest.contains("android.permission.INTERNET"));
    }

    @Test
    public void imeUsesTransparentFullscreenWithoutExtractedEditorText() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String manifest = readWorkspaceFile("app/src/main/AndroidManifest.xml");
        String styles = readWorkspaceFile("app/src/main/res/values/styles.xml");

        assertTrue(service.contains("boolean onEvaluateFullscreenMode()"));
        assertTrue(service.contains("loadTransparentOverlayInputEnabled(this)"));
        assertTrue(service.contains("setExtractViewShown(false)"));
        assertTrue(service.contains("void onUpdateExtractingVisibility(EditorInfo editorInfo)"));
        assertTrue(service.contains("outInsets.contentTopInsets = windowHeight"));
        assertTrue(service.contains("outInsets.visibleTopInsets = windowHeight"));
        assertTrue(service.contains("Insets.TOUCHABLE_INSETS_REGION"));
        assertTrue(service.contains("addTouchableViewBounds(outInsets.touchableRegion, inputContentContainer)"));
        assertTrue(service.contains("addTouchableViewBounds(outInsets.touchableRegion, clipboardOverlayView)"));
        assertFalse(service.contains("outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_FRAME"));
        assertTrue(manifest.contains("android:theme=\"@style/TransparentInputMethodTheme\""));
        assertTrue(styles.contains("name=\"TransparentInputMethodTheme\""));
        assertFalse(manifest.contains("android.permission.SYSTEM_ALERT_WINDOW"));
    }

    @Test
    public void debugOverlayTestbedExposesStableGeometryMarkers() throws Exception {
        String testbed = javaSource("TransparentOverlayTestbedView");
        String overrides = javaSource("DemoSettingsIntentOverrides");

        assertTrue(testbed.contains("OverlayImeTestbed"));
        assertTrue(testbed.contains("overlay_test_textbox"));
        assertTrue(testbed.contains("overlay_test_behind_keyboard"));
        assertTrue(testbed.contains("TestBackdropView"));
        assertTrue(overrides.contains("demo_overlay_testbed"));
        assertTrue(overrides.contains("demo_wear_testbed"));
        assertTrue(overrides.contains("debugDemoIntent"));
        assertTrue(javaSource("WearOnePressTestbedView").contains("wear_one_press_surface"));
        assertTrue(javaSource("HangulKeyboardView").contains("usesExtremeFloatingOverlay()"));
    }

    @Test
    public void manifestUsesStringResourcesForUserVisibleLabels() throws Exception {
        String manifest = readWorkspaceFile("app/src/main/AndroidManifest.xml");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(manifest.contains("android:label=\"Theme Editor\""));
        assertFalse(manifest.contains("android:label=\"Theme Selector\""));
        assertFalse(manifest.contains("android:label=\"Accent Placement\""));
        assertTrue(manifest.contains("android:label=\"@string/theme_editor_activity_label\""));
        assertTrue(manifest.contains("android:label=\"@string/theme_selector_activity_label\""));
        assertTrue(manifest.contains("android:label=\"@string/accent_placement_activity_label\""));
        assertTrue(strings.contains("name=\"theme_editor_activity_label\""));
        assertTrue(strings.contains("name=\"theme_selector_activity_label\""));
        assertTrue(strings.contains("name=\"accent_placement_activity_label\""));
    }

    @Test
    public void settingsAndThemeScreensKeepUserVisibleKoreanCopyInStringResources() throws Exception {
        String main = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
        String editor = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ThemeEditorActivity.java");
        String selector = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ThemeSelectorActivity.java");
        String accent = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/AccentPlacementActivity.java");
        String accentTarget = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/AccentPlacementTarget.java");
        String themeOption = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ThemeOption.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse("MainActivity should not carry Korean UI literals.", containsHangul(main));
        assertFalse("ThemeEditorActivity should not carry Korean UI literals.", containsHangul(editor));
        assertFalse("ThemeSelectorActivity should not carry Korean UI literals.", containsHangul(selector));
        assertFalse("AccentPlacementActivity should not carry Korean UI literals.", containsHangul(accent));
        assertFalse("AccentPlacementTarget should not carry Korean UI literals.", containsHangul(accentTarget));
        assertFalse("ThemeOption should not carry Korean UI literals.", containsHangul(themeOption));
        assertFalse("MainActivity should not carry escaped Korean UI literals.", main.contains("\\u"));
        assertFalse("ThemeEditorActivity should not carry escaped Korean UI literals.", editor.contains("\\u"));
        assertFalse("ThemeSelectorActivity should not carry escaped Korean UI literals.", selector.contains("\\u"));
        assertFalse("AccentPlacementActivity should not carry escaped Korean UI literals.", accent.contains("\\u"));
        assertFalse("AccentPlacementTarget should not carry escaped Korean UI literals.", accentTarget.contains("\\u"));
        assertFalse("ThemeOption should not carry escaped Korean UI literals.", themeOption.contains("\\u"));
        assertTrue(strings.contains("name=\"settings_hub_title\""));
        assertTrue(strings.contains("name=\"settings_remote_mode_help\""));
        assertTrue(strings.contains("name=\"settings_current_state_format\""));
        assertTrue(strings.contains("name=\"settings_android_ime_section\""));
        assertTrue(strings.contains("name=\"theme_editor_title\""));
        assertTrue(strings.contains("name=\"theme_color_alpha_title\""));
        assertTrue(strings.contains("name=\"theme_color_modifier_description\""));
        assertTrue(strings.contains("name=\"theme_keyboard_image_prompt_clip_label\""));
        assertTrue(strings.contains("name=\"theme_palette_image_prompt_clip_label\""));
        assertTrue(strings.contains("name=\"theme_selector_title\""));
        assertTrue(strings.contains("name=\"theme_current_settings\""));
        assertTrue(strings.contains("name=\"theme_external_name_format\""));
        assertTrue(strings.contains("name=\"theme_reset_confirm_title\""));
        assertTrue(strings.contains("name=\"external_theme_summary_format\""));
        assertTrue(strings.contains("name=\"theme_preview_qwerty_label\""));
        assertTrue(strings.contains("name=\"theme_preview_dingul_label\""));
        assertTrue(strings.contains("name=\"accent_target_settings_enter\""));
        assertTrue(strings.contains("name=\"accent_target_dingul_slash\""));
        assertTrue(strings.contains("name=\"theme_key_display_override_pack\""));
        assertFalse(main.contains("\"QWERTY preview\""));
        assertFalse(main.contains("\"Dingul preview\""));
        assertFalse(selector.contains("previewModeButton(\"Dingul\""));
        assertFalse(selector.contains("previewModeButton(\"QWERTY\""));
        assertFalse(editor.contains("\"New Dingul keyboard image prompt\""));
        assertFalse(editor.contains("\"New Dingul palette image prompt\""));
    }

    @Test
    public void staticThemePreviewsMirrorKeyboardSamplesWithoutLayoutCaptions() throws Exception {
        String script = readWorkspaceFile("scripts/render-theme-previews.ps1");
        int dingulStart = script.indexOf("function Draw-DingulSample");
        int cardStart = script.indexOf("function Draw-ThemeCard");

        assertTrue(script.contains("function Draw-KeyHints"));
        assertTrue(script.contains("-HintFont $hintFont -Hints $hints -Layout \"qwerty\""));
        assertTrue(script.contains("-HintFont $hintFont -Hints $hints -Layout \"dingul\""));
        assertFalse(script.contains("QWERTY preview -"));
        assertFalse(script.contains("Dingul preview -"));
        assertTrue(script.contains("$englishHeight = 235"));
        assertTrue(script.contains("$hangulHeight = 260"));
        assertTrue(script.contains("$expectedPreviewFiles = @(\"theme-preview-grid.png\") + $previewFileNames"));
        assertTrue(script.contains("Where-Object { $expectedPreviewFiles -notcontains $_.Name }"));
        assertTrue(script.contains("Theme names produce duplicate preview filenames"));
        assertTrue(dingulStart >= 0 && cardStart > dingulStart);
        assertFalse(script.substring(dingulStart, cardStart).contains("@(\"1\",30,\"number\")"));
    }

    @Test
    public void stringResourcesDoNotContainCommonMojibakeMarkers() throws Exception {
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse("strings.xml contains replacement characters.", strings.contains("\uFFFD"));
        assertFalse("strings.xml contains question-mark mojibake runs.", strings.contains("???"));
        assertFalse("strings.xml contains common UTF-8 mojibake markers.",
                containsAny(strings, "\u00C3", "\u00C2", "\u00EC", "\u00EA", "\u00EB", "\u00ED"));
    }

    @Test
    public void settingsOptionLabelsUseResourceIdsInsteadOfInlineKoreanCopy() throws Exception {
        String inputAssistance = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/InputAssistanceMode.java");
        String colorOption = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ColorOption.java");
        String fontOption = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/FontOption.java");
        String ergonomicsPreset = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardErgonomicsPreset.java");
        String visualConsistency = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/VisualConsistencyLevel.java");
        String motionEffect = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MotionEffectLevel.java");
        String accentPlacementMode = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/AccentPlacementMode.java");
        String accessibilityLabel = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardKeyAccessibilityLabel.java");
        String labels = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/SettingsDisplayLabels.java");
        String labelOption = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/SettingsLabelOption.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(containsHangul(inputAssistance));
        assertFalse(containsHangul(colorOption));
        assertFalse(containsHangul(fontOption));
        assertFalse(containsHangul(ergonomicsPreset));
        assertFalse(containsHangul(visualConsistency));
        assertFalse(containsHangul(motionEffect));
        assertFalse(containsHangul(accentPlacementMode));
        assertFalse(containsHangul(accessibilityLabel));
        assertFalse(colorOption.contains("\\u"));
        assertFalse(fontOption.contains("\\u"));
        assertFalse(ergonomicsPreset.contains("\\u"));
        assertFalse(visualConsistency.contains("\\u"));
        assertFalse(motionEffect.contains("\\u"));
        assertFalse(accentPlacementMode.contains("\\u"));
        assertTrue(labels.contains("SettingsDisplayLabels"));
        assertTrue(labelOption.contains("interface SettingsLabelOption"));
        assertTrue(labelOption.contains("int labelResId()"));
        assertTrue(labels.contains("static <T extends SettingsLabelOption> String[] labels("));
        assertTrue(labels.contains("static String label(Context context, SettingsLabelOption value)"));
        assertTrue(inputAssistance.contains("implements SettingsLabelOption"));
        assertTrue(colorOption.contains("implements SettingsLabelOption"));
        assertTrue(fontOption.contains("implements SettingsLabelOption"));
        assertTrue(ergonomicsPreset.contains("implements SettingsLabelOption"));
        assertTrue(visualConsistency.contains("implements SettingsLabelOption"));
        assertTrue(motionEffect.contains("implements SettingsLabelOption"));
        assertTrue(accentPlacementMode.contains("implements SettingsLabelOption"));
        assertEquals(1, countOccurrences(labels, "new String["));
        assertEquals(1, countOccurrences(labels, "for (int i = 0; i < values.length; i++)"));
        assertEquals(1, countOccurrences(
                labels,
                "value == null ? \"\" : label(context, value.labelResId())"));
        assertEquals(1, countOccurrences(
                labels,
                "static String label(Context context, SettingsLabelOption value)"));
        assertFalse(labels.contains("values.length, index ->"));
        assertFalse(labels.contains("for (int i = 0; i < count; i++)"));
        assertFalse(labels.contains("Function<"));
        assertFalse(labels.contains("ToIntFunction"));
        assertFalse(labels.contains("static String[] labels(Context context, HandednessMode[]"));
        assertFalse(labels.contains("static String[] labels(Context context, ColorOption[]"));
        assertFalse(labels.contains("label(Context context, HandednessMode value)"));
        assertFalse(labels.contains("label(Context context, KeyboardMode value)"));
        assertFalse(labels.contains("label(Context context, FontOption value)"));
        assertFalse(labels.contains("fallbackLabelResId"));
        assertTrue(strings.contains("name=\"input_assistance_clean_mode\""));
        assertTrue(strings.contains("name=\"color_option_default_button\""));
        assertTrue(strings.contains("name=\"font_option_default\""));
        assertTrue(strings.contains("name=\"ergonomics_preset_ergonomic\""));
        assertTrue(strings.contains("name=\"motion_effect_subtle\""));
        assertTrue(strings.contains("name=\"accent_placement_mode_theme_default\""));
        assertTrue(strings.contains("name=\"keyboard_accessibility_action_tap\""));
    }

    @Test
    public void releaseBuildHasClosedBetaHardeningDecisions() throws Exception {
        String buildGradle = readWorkspaceFile("app/build.gradle");
        String checkScript = readWorkspaceFile("scripts/check.ps1");
        String buildRelease = readWorkspaceFile("scripts/build-release.ps1");

        assertTrue(buildGradle.contains("S3_VERSION_CODE"));
        assertTrue(buildGradle.contains("S3_VERSION_NAME"));
        assertTrue(buildGradle.contains("verifyClosedBetaSigning"));
        assertTrue(buildRelease.contains("apksigner.bat"));
        assertTrue(buildRelease.contains("verify --verbose"));
        assertTrue(buildGradle.contains("minifyEnabled true"));
        assertTrue(buildGradle.contains("shrinkResources true"));
        assertTrue(buildGradle.contains("HANGUL_IME_KEYSTORE"));
        assertTrue(buildGradle.contains("coreLibraryDesugaringEnabled true"));
        assertTrue(buildGradle.contains("com.android.tools:desugar_jdk_libs:2.1.5"));
        assertTrue(checkScript.contains("lintDebug"));
    }

    @Test
    public void demoIntentOverridesAreDebugGated() throws Exception {
        String mainActivity = javaSource("MainActivity");
        String overrides = javaSource("DemoSettingsIntentOverrides");
        String fieldProfile = javaSource("DemoFieldProfile");

        assertTrue(overrides.contains("EXTRA_DEMO_SETTINGS"));
        assertTrue(overrides.contains("EXTRA_DEMO_FIELD_PROFILE"));
        assertTrue(mainActivity.contains("DemoSettingsIntentOverrides.apply("));
        assertTrue(fieldProfile.contains("TYPE_TEXT_VARIATION_WEB_PASSWORD"));
        assertTrue(fieldProfile.contains("TYPE_TEXT_VARIATION_WEB_EDIT_TEXT"));
        assertTrue(fieldProfile.contains("EditorInfo.IME_ACTION_SEARCH"));
        assertTrue(mainActivity.contains("isDebuggableBuild()"));
        assertTrue(overrides.contains("debugDemoIntent"));
        assertTrue(overrides.contains("RuntimeDefaults.keyboardSettings(settings)"));
        assertTrue(overrides.contains("RuntimeDefaults.keyboardSettings(settings);"));
        assertTrue(overrides.contains("RuntimeDefaults.stringOrDefault("));
        assertFalse(overrides.contains("settings == null ? KeyboardSettings.defaults() : settings"));
        assertFalse(overrides.contains("private static String stringExtra("));
    }

    @Test
    public void rawKeyEventsStayBehindDispatcherHelpers() throws Exception {
        Path root = findWorkspaceRoot();
        List<Path> files = new ArrayList<>();
        collectTextFiles(root.resolve("app/src/main/java"), files);

        for (Path file : files) {
            String relative = root.relativize(file).toString().replace('\\', '/');
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            if (text.contains("new KeyEvent(")) {
                assertTrue(
                        "Direct KeyEvent construction must stay in RemoteKeyEventSequence: " + relative,
                        relative.endsWith("RemoteKeyEventSequence.java"));
            }
            if (text.contains(".sendKeyEvent(")) {
                assertTrue(
                        "InputConnection.sendKeyEvent must stay in ImeConnectionDispatcher: " + relative,
                        relative.endsWith("ImeConnectionDispatcher.java"));
            }
            if (text.contains(".performEditorAction(")) {
                assertTrue(
                        "InputConnection.performEditorAction must stay in ImeConnectionDispatcher: " + relative,
                        relative.endsWith("ImeConnectionDispatcher.java"));
            }
            if (text.contains(".performContextMenuAction(")) {
                assertTrue(
                        "InputConnection.performContextMenuAction must stay in ImeConnectionDispatcher: "
                                + relative,
                        relative.endsWith("ImeConnectionDispatcher.java"));
            }
        }
    }

    @Test
    public void textConnectionEditingStaysBehindOperatorHelper() throws Exception {
        String service = javaSource("S3KeyboardService");
        String operator = javaSource("InputConnectionTextOperator");

        assertTrue(service.contains("InputConnectionTextOperator"));
        assertFalse(service.contains("deleteSurroundingTextInCodePoints("));
        assertFalse(service.contains("deleteSurroundingText("));
        assertFalse(service.contains("setComposingText("));
        assertFalse(service.contains("finishComposingText("));
        assertFalse(service.contains("inputConnection.commitText("));
        assertTrue(service.contains("ImeConnectionDispatcher.moveCursor("));
        assertFalse(service.contains("InputConnectionTextOperator.isCursorAtBoundary("));
        assertFalse(service.contains("KEYCODE_DPAD_LEFT"));
        assertFalse(service.contains("KEYCODE_DPAD_RIGHT"));
        assertTrue(operator.contains("deleteSurroundingTextInCodePoints("));
        assertTrue(operator.contains("deleteSurroundingText("));
        assertTrue(operator.contains("setComposingText("));
        assertTrue(operator.contains("finishComposingText()"));
        assertTrue(operator.contains("commitText("));
        assertTrue(operator.contains("static boolean hasSelection(InputConnection inputConnection)"));
        assertTrue(service.contains("private boolean explicitEnglishCorrectionAvailable(InputConnection inputConnection)"));
        assertTrue(service.contains("!InputConnectionTextOperator.hasSelection(inputConnection)"));
        assertTrue(service.contains("if (InputConnectionTextOperator.hasSelection(inputConnection)) {"));
        assertFalse(service.contains("inputConnection.getSelectedText("));
        assertTrue(operator.contains("private static final class CommitOnlySink"));
        assertTrue(operator.contains("return new CommitOnlySink(inputConnection)"));
        assertFalse(operator.contains("new HangulCommitOnlyEditor.Sink()"));
        assertFalse(service.contains("private HangulCommitOnlyEditor.Sink commitOnlySink("));
        assertTrue(service.contains("pendingOwnComposingSelectionUpdates"));
        assertTrue(service.contains("pendingOwnComposingSelectionUpdates--"));
        assertTrue(service.contains("Math.min(pendingOwnComposingSelectionUpdates + 2, 4)"));
    }

    @Test
    public void textActionPanelStaysInsideImeWindow() throws Exception {
        String service = javaSource("S3KeyboardService");

        assertTrue(service.contains("private View textActionOverlayView;"));
        assertTrue(service.contains("inputRoot.addView(overlay, new FrameLayout.LayoutParams("));
        assertTrue(service.contains("inputRoot.removeView(textActionOverlayView);"));
        assertFalse(service.contains("PopupWindow textActionPopup"));
        assertFalse(service.contains("textActionPopup = new PopupWindow"));
    }

    @Test
    public void cursorMovementReleasesHangulComposingBeforePendingSelectionSuppression() throws Exception {
        String service = javaSource("S3KeyboardService");
        int mismatchIndex = service.indexOf("boolean selectionMismatch = isComposingSelectionMismatch(");
        int pendingIndex = service.indexOf(
                "if (pendingOwnComposingSelectionUpdates > 0 && !selectionMismatch)");

        assertTrue(mismatchIndex >= 0);
        assertTrue(pendingIndex > mismatchIndex);
        assertTrue(service.contains("if (candidatesStart < 0 || candidatesEnd < 0) {\n"
                + "            return oldSelStart != newSelStart || oldSelEnd != newSelEnd;"));
        assertTrue(service.contains("releaseCurrentCompositionForExternalCursorMove(inputConnection);"));
        assertTrue(service.contains("private void releaseCurrentCompositionForExternalCursorMove("));
        assertTrue(service.contains("InputConnectionTextOperator.finishComposing(inputConnection);\n"
                + "        automata.reset();\n"
                + "        commitOnlyEditor.reset();"));
        assertFalse(service.contains("if (pendingOwnComposingSelectionUpdates > 0) {\n"
                + "            pendingOwnComposingSelectionUpdates--;"));
    }

    @Test
    public void backspaceDoesNotEmitDeleteOnPointerDown() throws Exception {
        String view = javaSource("HangulKeyboardView");

        assertFalse(view.contains("if (isDeleteKey(keySlot.key)) {\n"
                + "                state.tapOutputAlreadyEmitted = true;"));
        assertTrue(view.contains("String repeatValue = longPressRepeatValue(state.keySlot.key);"));
    }

    @Test
    public void previewOverlayTransportStaysDecoupledFromKeyboardViewInternals() throws Exception {
        String service = javaSource("S3KeyboardService");
        String controller = javaSource("PreviewOverlayController");
        String canvasView = javaSource("PreviewOverlayCanvasView");
        String view = javaSource("HangulKeyboardView");
        String spec = javaSource("PreviewOverlaySpec");

        assertContainsNone(
                service,
                "HangulKeyboardView.PreviewOverlaySpec",
                "HangulKeyboardView.OnPreviewOverlayListener");
        assertContainsNone(
                view,
                "static final class PreviewOverlaySpec",
                "interface OnPreviewOverlayListener",
                "private PreviewOverlaySpec previewBubbleSpec(PreviewBubbleAnimation bubble)",
                "private void pruneReleasedPreviewBubbles()");
        assertContainsNone(
                controller,
                "HangulKeyboardView.PreviewOverlaySpec",
                "void show(View anchor, PreviewOverlaySpec spec)",
                "Collections.singletonList(",
                "new TextView(",
                "FrameLayout.LayoutParams",
                "GradientDrawable",
                "popup.update(popupX, popupY, popupWidth, popupHeight);\n        } else");
        assertContainsAll(
                view,
                "Consumer<List<PreviewOverlaySpec>> previewOverlayListener",
                "private static final int MAX_RELEASED_PREVIEW_BUBBLES = 2",
                "previewOverlayListener.accept(previewOverlaySpecs)",
                "previewOverlayListener.accept(Collections.emptyList())",
                "for (PreviewBubbleAnimation bubble : releasedPreviewBubbles)",
                "for (TouchState state : activeTouches)",
                "state.previewGeneration != previewGestureGeneration",
                "while (releasedPreviewBubbles.size() > MAX_RELEASED_PREVIEW_BUBBLES)",
                "pruneReleasedPreviewBubbles(nowMs)",
                "pooledPreviewOverlaySpec(specIndex++)",
                "private PreviewOverlaySpec previewBubbleSpec(",
                "private PreviewOverlaySpec pooledPreviewOverlaySpec(int index)",
                "private void pruneReleasedPreviewBubbles(long nowMs)",
                "previewOverlaySpecs.clear()",
                "private void postAnimationInvalidation()",
                "postInvalidateOnAnimation(0, 0, 1, 1)",
                "private boolean intersectsDrawClip(RectF bounds, float pad)",
                "private void invalidateTouchState(TouchState state)",
                "private void invalidateTouchBounds(RectF bounds, boolean hasDirtyBounds)",
                "collectActiveTouchBounds(moveDirtyScratch)");
        assertContainsAll(
                service,
                "inputView.setOnPreviewOverlayListener(this::showPreviewOverlays)",
                "private void showPreviewOverlays(List<PreviewOverlaySpec> specs)");
        assertContainsAll(
                controller,
                "SettingsRowBuilder.dp(context,",
                "private final PreviewOverlayCanvasView overlayView",
                "private final int[] windowLocation = new int[2]",
                "overlayView.setSpecs(specs, topPadPx)",
                "private int requiredTopPad(",
                "private int maxBottom(",
                "private void showOrUpdatePopup(",
                "if (popupX == lastPopupX",
                "private void rememberGeometry(");
        assertContainsAll(
                canvasView,
                "final class PreviewOverlayCanvasView extends View",
                "private final List<RenderState> renderStates = new ArrayList<>(4)",
                "void setSpecs(List<PreviewOverlaySpec> specs, int topPadPx)",
                "renderStates.get(i).copyFrom(specs.get(i))",
                "KeyboardTypefaceCatalog.typefaceFor(",
                "private void drawPreview(Canvas canvas, RenderState state)",
                "private void drawBody(",
                "private void drawText(",
                "textPaint.getFontMetrics(textMetrics)");
        assertContainsNone(
                canvasView,
                "android.widget.TextView",
                "new TextView(",
                "LinearGradient",
                "GradientDrawable",
                "PopupWindow");
        assertContainsAll(spec, "final class PreviewOverlaySpec");
    }

    @Test
    public void previewBubbleAnimationKeepsMotionGateAtPublicBoundary() throws Exception {
        String animation = javaSource("PreviewBubbleAnimation");
        String layout = javaSource("PreviewBubbleLayout");

        assertContainsAll(
                animation,
                "private static final long POP_ANIMATION_MS = 64",
                "private static final long RELEASE_ANIMATION_MS = 92",
                "private static final long SUPERSEDE_FADE_MS = 42",
                "private static final float POP_START_SCALE = 0.985f",
                "float scale(long nowMs, boolean motionEnabled, float durationScale)",
                "float alpha(long nowMs, boolean motionEnabled, float durationScale)",
                "boolean expired(long nowMs, boolean motionEnabled, float durationScale)");
        assertContainsNone(
                animation,
                "ACTIVE_FADE_IN_MS",
                "final long sequence",
                "anchorBottom",
                "commitGlowAlpha(",
                "inputImpactAlpha(",
                "commitLiftProgress(",
                "releaseFloatProgress(",
                "PRESS_SQUASH",
                "RELEASE_IMPULSE");
        assertContainsNone(layout, "liftPx(", "LIFT_RISE_END", "LIFT_HOLD_END");
    }

    @Test
    public void previewBubbleDrawableKeepsEffectsOnSharedPaintPath() throws Exception {
        String drawable = javaSource("PreviewBubbleDrawable");

        assertContainsAll(
                drawable,
                "private final Paint effectPaint",
                "private final RectF scratchRect",
                "private void setVerticalGradient(",
                "private void setHorizontalGradient(",
                "private void setTwoStopVerticalGradient(",
                "private void setCenteredHorizontalGradient(",
                "private void drawTwoStopVerticalRoundRect(",
                "private void drawAdaptiveTwoStopVerticalRoundRect(",
                "private void drawAdaptiveVerticalGradientRoundRect(",
                "private void drawPressedInset(",
                "private void drawInputCore(",
                "private void drawInputImpact(",
                "private static float clampUnit(float value)",
                "private static int clampByte(int value)",
                "TWO_STOP_POSITIONS",
                "CENTERED_HORIZONTAL_POSITIONS",
                "COMMIT_GLOW_POSITIONS",
                "INPUT_IMPACT_POSITIONS",
                "COMMIT_HALO_POSITIONS",
                "SHADOW_LUMINANCE_THRESHOLD",
                "HIGHLIGHT_LUMINANCE_THRESHOLD",
                "COMMIT_LUMINANCE_THRESHOLD",
                "RIM_LUMINANCE_THRESHOLD");
        assertContainsNone(
                drawable,
                "haloPaint",
                "highlightPaint",
                "commitGlowPaint",
                "commitSheenPaint",
                "rimPaint",
                "tailShadowPaint",
                "tailContactPaint",
                "lowerLipPaint",
                "lightAdaptiveColor(96)",
                "lightAdaptiveColor(110)",
                "lightAdaptiveColor(118)",
                "lightAdaptiveColor(126)",
                "adaptiveAlpha(96",
                "adaptiveAlpha(110",
                "adaptiveAlpha(118",
                "adaptiveAlpha(126",
                "private int adaptiveAlpha(int threshold, float darkAmount, float lightAmount)",
                "Math.max(0f, Math.min(1f",
                "Math.max(0, Math.min(255");
    }

    @Test
    public void nonInteractiveKeyboardPreviewsUseDedicatedFactory() throws Exception {
        String factory = javaSource("KeyboardPreviewFactory");
        String selector = javaSource("ThemeSelectorActivity");
        String accent = javaSource("AccentPlacementActivity");
        String editor = javaSource("ThemeEditorActivity");
        String view = javaSource("HangulKeyboardView");

        assertTrue(factory.contains("new HangulKeyboardView(context, true)"));
        assertTrue(factory.contains("setCompactPreviewRendering(true)"));
        assertTrue(factory.contains("RuntimeDefaults.keyboardSettings("));
        assertTrue(factory.contains("IMPORTANT_FOR_ACCESSIBILITY_NO"));
        assertTrue(selector.contains("KeyboardPreviewFactory.nonInteractive("));
        assertTrue(accent.contains("KeyboardPreviewFactory.nonInteractive("));
        assertFalse(selector.contains("new HangulKeyboardView(this)"));
        assertFalse(accent.contains("new HangulKeyboardView(this)"));
        assertTrue(editor.contains("new HangulKeyboardView(this, true)"));
        assertTrue(view.contains("private final boolean previewOnly;"));
        assertTrue(view.contains("touchBiasStore = null;"));
        assertTrue(view.contains("if (touchBiasStore != null)"));
        assertFalse(factory.contains("settings == null ? KeyboardSettings.defaults() : settings"));
    }

    @Test
    public void layoutFactoryUsesSharedRuntimeFallbacks() throws Exception {
        String factory = javaSource("KeyboardLayoutFactory");
        String view = javaSource("HangulKeyboardView");
        String layoutController = javaSource("LayoutSettingsController");
        String defaults = javaSource("RuntimeDefaults");

        assertContainsAll(
                factory,
                "RuntimeDefaults.keyboardSettings(settings)",
                "RuntimeDefaults.keyboardLayoutProfiles(layoutProfiles)",
                "RuntimeDefaults.keyboardSurface(surface)");
        assertContainsAll(
                view,
                "RuntimeDefaults.keyboardLayoutProfiles(profiles)");
        assertContainsAll(
                layoutController,
                "RuntimeDefaults.keyboardLayoutProfiles(layoutProfiles)");
        assertContainsAll(
                defaults,
                "static KeyboardLayoutProfiles keyboardLayoutProfiles(",
                "static KeyboardLayoutProfiles keyboardLayoutProfilesFrom(");
        assertContainsNone(
                factory + "\n" + view + "\n" + layoutController,
                "settings == null ? KeyboardSettings.defaults() : settings",
                "layoutProfiles == null",
                "profiles == null ? KeyboardLayoutProfiles.defaults() : profiles",
                "surface == null ? KeyboardSurface.NORMAL : surface");
    }

    @Test
    public void accentPlacementPolicyUsesSharedSettingsFallbacks() throws Exception {
        String policy = javaSource("AccentPlacementPolicy");
        String target = javaSource("AccentPlacementTarget");
        String editorInputPolicy = javaSource("EditorInputPolicy");

        assertTrue(policy.contains("RuntimeDefaults.keyboardSettings(settings)"));
        assertFalse(policy.contains("settings == null ? KeyboardSettings.defaults() : settings"));
        assertTrue(target.contains("RuntimeDefaults.keyboardSettings(settings)"));
        assertContainsNone(
                target,
                "settings == null ? KeyboardMode.HANGUL",
                "settings != null && settings.showNumberRow");
        assertTrue(editorInputPolicy.contains("this.surface = surface == null ? KeyboardSurface.NORMAL : surface;"));
    }

    @Test
    public void remoteCompatibilityUiStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String panel = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteCompatibilityPanelController.java");
        String report = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteCompatibilityReport.java");
        String log = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteCompatibilityLog.java");
        String remoteCatalog = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteAppCatalog.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");
        String normalizedPanel = normalizeNewlines(panel);

        assertTrue(service.contains("RemoteCompatibilityPanelController"));
        assertFalse(service.contains("RemoteCompatibilityLog.record("));
        assertFalse(service.contains("RemoteCompatibilityReport.describe("));
        assertFalse(service.contains("RemoteCompatibilityReport.toJson("));
        assertFalse(service.contains("RemoteCompatibilityPanelController.Host"));
        assertTrue(service.contains("() -> currentEditorPackageName"));
        assertTrue(service.contains("this::sendCompatibilityKey"));
        assertFalse(service.contains("private String currentPackageName()"));
        assertTrue(panel.contains("RemoteCompatibilityLog.record("));
        assertTrue(panel.contains("RemoteCompatibilityReport.describe("));
        assertTrue(normalizedPanel.contains("RemoteCompatibilityReport.describe(\n                context,"));
        assertTrue(panel.contains("RemoteCompatibilityReport.toJson("));
        assertFalse(panel.contains("interface Host"));
        assertTrue(panel.contains("private final Supplier<String> currentPackageName"));
        assertTrue(panel.contains("private final IntBinaryOperator keySender"));
        assertTrue(panel.contains("keySender.applyAsInt("));
        assertTrue(report.contains("R.string.remote_compatibility_summary_header"));
        assertTrue(report.contains("R.string.remote_compatibility_summary_counts"));
        assertTrue(report.contains("R.string.remote_compatibility_missing_cases"));
        assertTrue(report.contains("R.string.remote_compatibility_manual_required"));
        assertFalse(report.contains("\"?�격 ?�환?? \""));
        assertFalse(report.contains("\"?�공 \""));
        assertFalse(report.contains("\"?�제 ?�격 ?�션"));
        assertTrue(report.contains("AppPackageCatalog.normalizePackageName("));
        assertTrue(log.contains("AppPackageCatalog.normalizePackageName("));
        assertTrue(log.contains("RuntimeDefaults.stringOrDefault(label, \"\")"));
        assertTrue(log.contains("RuntimeDefaults.stringOrDefault(packageName, \"\")"));
        assertTrue(remoteCatalog.contains("AppPackageCatalog.normalizePackageName("));
        assertFalse(report.contains("private static String normalizePackage("));
        assertFalse(log.contains("private static String normalizePackage("));
        assertFalse(remoteCatalog.contains("private static String normalizePackage("));
        assertFalse(log.contains("label == null ? \"\" : label"));
        assertFalse(log.contains("packageName == null ? \"\" : packageName"));
        assertTrue(strings.contains("name=\"remote_compatibility_summary_header\""));
        assertTrue(strings.contains("name=\"remote_compatibility_manual_required\""));
    }

    @Test
    public void appPackageProfileListsStayOutOfResolverLogic() throws Exception {
        String resolver = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/AppInputProfileResolver.java");
        String catalog = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/AppPackageCatalog.java");
        String profileCatalog = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/AppInputProfileCatalog.java");
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String sessionResolver = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/InputSessionSettingsResolver.java");
        String sessionSettings = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/InputSessionSettings.java");
        String appProfile = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/AppInputProfile.java");
        String appProfileOverrides = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/AppInputProfileOverrides.java");
        String remoteController = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteWindowsSettingsController.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertTrue(resolver.contains("AppPackageCatalog.isBrowserPackage("));
        assertTrue(resolver.contains("AppPackageCatalog.isWebViewPackage("));
        assertTrue(resolver.contains("AppPackageCatalog.isMessagingPackage("));
        assertTrue(resolver.contains("AppInputProfileCatalog.password()"));
        assertFalse(resolver.contains("new AppInputProfile("));
        assertFalse(resolver.contains("com.android.chrome"));
        assertFalse(resolver.contains("com.google.android.webview"));
        assertFalse(resolver.contains("com.kakao.talk"));
        assertTrue(catalog.contains("com.android.chrome"));
        assertTrue(catalog.contains("com.google.android.webview"));
        assertTrue(catalog.contains("com.kakao.talk"));
        assertTrue(profileCatalog.contains("new AppInputProfile("));
        assertTrue(profileCatalog.contains("remote_auto_package"));
        assertTrue(service.contains("InputSessionSettingsResolver.resolve("));
        assertTrue(service.contains("loadAppInputProfileOverrides(this)"));
        assertFalse(service.contains("AppInputProfileResolver.resolve("));
        assertFalse(service.contains("EditorInputPolicy.from(info)"));
        assertTrue(sessionResolver.contains("AppInputProfileResolver.resolve("));
        assertTrue(sessionResolver.contains("EditorInputPolicy.from(info)"));
        assertTrue(sessionResolver.contains("withRuntimeNumberRowForced("));
        assertTrue(catalog.contains("static String normalizePackageName("));
        assertTrue(resolver.contains("AppPackageCatalog.normalizePackageName(packageName)"));
        assertTrue(sessionResolver.contains("AppPackageCatalog.normalizePackageName("));
        assertTrue(sessionResolver.contains("RuntimeDefaults.stringOrDefault(enterActionLabel, \"\")"));
        assertTrue(sessionSettings.contains("RuntimeDefaults.stringOrDefault(packageName, \"\")"));
        assertFalse(resolver.contains("packageName == null ? \"\" : packageName.trim()"));
        assertFalse(sessionResolver.contains("info == null || info.packageName == null ? \"\" : info.packageName"));
        String profileSources = resolver
                + "\n" + sessionResolver
                + "\n" + sessionSettings
                + "\n" + appProfile
                + "\n" + appProfileOverrides;
        assertContainsAll(
                profileSources,
                "RuntimeDefaults.editorInputPolicy(",
                "RuntimeDefaults.keyboardSettings(",
                "RuntimeDefaults.appInputProfile(",
                "RuntimeDefaults.appInputProfileOverrides(",
                "RuntimeDefaults.stringOrDefault(");
        assertContainsNone(
                profileSources,
                "basePolicy == null ? EditorInputPolicy.DEFAULT : basePolicy",
                "policy == null ? EditorInputPolicy.DEFAULT : policy",
                "editorPolicy == null ? EditorInputPolicy.DEFAULT : editorPolicy",
                "appInputProfile == null ? AppInputProfile.STANDARD : appInputProfile",
                "runtimeSettings == null ? KeyboardSettings.defaults() : runtimeSettings",
                "storedSettings == null",
                "private static String safe(String value)",
                "private static String safeLabel(",
                "packageName == null ? \"\" : packageName");
        assertTrue(remoteController.contains("settings_app_profile_overrides_help"));
        assertTrue(remoteController.contains("saveAppProfileAsciiPackages"));
        assertTrue(remoteController.contains("saveAppProfileNumberRowPackages"));
        assertTrue(remoteController.contains("saveAppProfileNoComposingPackages"));
        assertTrue(remoteController.contains("saveAppProfileNoTextConveniencesPackages"));
        assertTrue(strings.contains("name=\"settings_app_profile_ascii_packages\""));
        assertTrue(strings.contains("name=\"settings_app_profile_no_composing_packages\""));
    }

    @Test
    public void inputIssueReportClipboardFlowStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String report = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/InputIssueReport.java");
        String defaults = javaSource("RuntimeDefaults");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/InputIssueReportClipboardController.java");
        String quickSettings = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/QuickSettingsPanelController.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertTrue(service.contains("InputIssueReportClipboardController"));
        assertFalse(service.contains("InputIssueReport.build("));
        assertFalse(service.contains("InputIssueReportClipboardController.Host"));
        assertTrue(controller.contains("InputIssueReport.build("));
        assertFalse(controller.contains("interface Host"));
        assertTrue(controller.contains("private final Runnable prepareIssueReport"));
        assertTrue(controller.contains("private final Supplier<String> currentPackageName"));
        assertTrue(controller.contains("private final Supplier<AppInputProfile> inputProfile"));
        assertTrue(controller.contains("private final Supplier<KeyboardSettings> currentSettings"));
        assertTrue(controller.contains("private final Supplier<EditorInputPolicy> currentEditorPolicy"));
        assertTrue(controller.contains("RuntimeDefaults.runnable("));
        assertTrue(controller.contains("RuntimeDefaults.emptyStringSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.appInputProfileSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.editorInputPolicySupplier("));
        assertEquals("Input issue report fallbacks should come from RuntimeDefaults.",
                0,
                countOccurrences(controller, "() -> { }"));
        assertTrue(service.contains("this::prepareIssueReport"));
        assertTrue(service.contains("() -> currentEditorPackageName"));
        assertTrue(service.contains("() -> appInputProfile"));
        assertTrue(service.contains("() -> settings"));
        assertTrue(service.contains("() -> editorPolicy"));
        assertFalse(service.contains("public void prepareIssueReport()"));
        assertFalse(service.contains("public String currentPackageName()"));
        assertFalse(service.contains("private String currentPackageName()"));
        assertFalse(service.contains("public AppInputProfile inputProfile()"));
        assertFalse(service.contains("public KeyboardSettings currentSettings()"));
        assertFalse(service.contains("public EditorInputPolicy currentEditorPolicy()"));
        assertFalse(service.contains("private AppInputProfile inputProfile()"));
        assertFalse(service.contains("private KeyboardSettings currentSettings()"));
        assertFalse(service.contains("private EditorInputPolicy currentEditorPolicy()"));
        assertTrue(report.contains("\"layoutAccessibility\""));
        assertTrue(report.contains("\"localDataSummary\""));
        assertTrue(report.contains("\"clipboardEntriesIncluded\""));
        assertTrue(report.contains("\"clearPath\""));
        assertTrue(report.contains("KeyboardAccessibilityAudit.audit("));
        assertTrue(report.contains("KeyboardAccessibilityAudit.advisoryAudit("));
        assertTrue(report.contains("\"recommendedIssueCount\""));
        assertTrue(report.contains("\"recommendedErgonomicsPreset\""));
        assertTrue(report.contains("\"currentErgonomicsPreset\""));
        assertTrue(report.contains("\"appliesErgonomicsPreset\""));
        assertContainsAll(
                report,
                "RuntimeDefaults.keyboardSettings(",
                "RuntimeDefaults.keyboardErgonomics(",
                "RuntimeDefaults.appInputProfile(",
                "RuntimeDefaults.appInputProfileOverrides(",
                "RuntimeDefaults.keyboardSurface(",
                "RuntimeDefaults.stringOrDefault(packageName, \"\")");
        assertContainsAll(
                defaults,
                "static KeyboardErgonomicsOptions keyboardErgonomics(",
                "static AppInputProfile appInputProfile(",
                "static AppInputProfileOverrides appInputProfileOverrides(",
                "static KeyboardSurface keyboardSurface(");
        assertContainsNone(
                report,
                "settings == null ? KeyboardSettings.defaults() : settings",
                "profile == null ? AppInputProfile.STANDARD : profile",
                "context == null\n                ? KeyboardErgonomicsOptions.DEFAULT",
                "ergonomicsOptions == null ? KeyboardErgonomicsOptions.DEFAULT : ergonomicsOptions",
                "surface == null ? KeyboardSurface.NORMAL : surface",
                "packageName == null ? \"\" : packageName");
        assertTrue(quickSettings.contains("R.string.copy_input_issue_report"));
        assertTrue(strings.contains("name=\"copy_input_issue_report\""));
        assertTrue(strings.contains(text(0xC774, 0x20, 0xC785, 0xB825, 0xC774,
                0x20, 0xC774, 0xC0C1, 0xD574, 0xC694)));
    }

    @Test
    public void virtualKeyboardAccessibilityProviderStaysOutOfCanvasViewBody() throws Exception {
        String view = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/HangulKeyboardView.java");
        String provider = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardVirtualKeyAccessibilityProvider.java");

        assertTrue(view.contains("KeyboardVirtualKeyAccessibilityProvider"));
        assertFalse(view.contains("extends AccessibilityNodeProvider"));
        assertFalse(view.contains("createAccessibilityNodeInfo("));
        assertTrue(provider.contains("extends AccessibilityNodeProvider"));
        assertTrue(provider.contains("createAccessibilityNodeInfo("));
        assertTrue(provider.contains("KeyboardKeyAccessibilityLabel.describe("));
    }

    @Test
    public void virtualKeyboardAccessibilityNodesUseHitBoundsAndActionableLabels() throws Exception {
        String provider = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardVirtualKeyAccessibilityProvider.java");
        String view = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/HangulKeyboardView.java");
        String ids = readWorkspaceFile("app/src/main/res/values/ids.xml");

        assertTrue(provider.contains(
                "String description = KeyboardKeyAccessibilityLabel.describe(view.getContext(), keySlot.key)"));
        assertTrue(provider.contains("info.setContentDescription(description)"));
        assertTrue(provider.contains("info.setText(description)"));
        assertTrue(provider.contains("info.setFocusable(true)"));
        assertTrue(provider.contains("info.setClickable(true)"));
        assertTrue(provider.contains("info.addAction(AccessibilityNodeInfo.ACTION_CLICK)"));
        assertTrue(provider.contains("new AccessibilityNodeInfo.AccessibilityAction(actionId, description)"));
        assertTrue(provider.contains("AccessibilityNodeInfo.ACTION_LONG_CLICK"));
        assertTrue(provider.contains("info.setLongClickable(hasLongPress)"));
        assertTrue(provider.contains("key.mappedValueFor(action)"));
        assertTrue(provider.contains("host.performKeyAccessibilityGesture("));
        assertTrue(provider.contains("public AccessibilityNodeInfo findFocus(int focus)"));
        assertTrue(provider.contains("AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS"));
        assertTrue(provider.contains("AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS"));
        assertTrue(provider.contains("TYPE_VIEW_ACCESSIBILITY_FOCUSED"));
        assertTrue(view.contains("accessibilityNodeProvider.resetVirtualFocus()"));
        assertTrue(view.contains("public boolean performKeyAccessibilityGesture("));
        assertTrue(ids.contains("name=\"keyboard_accessibility_swipe_up\""));
        assertFalse(ids.contains("name=\"keyboard_accessibility_long_press\""));
        assertTrue(provider.contains("keySlot.hitBounds()"));
        assertFalse(provider.contains("keySlot.visualBounds()"));
    }

    @Test
    public void keyboardAccessibilitySummaryUsesStringResourcesInRuntimePath() throws Exception {
        String summary = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardAccessibilitySummary.java");
        String view = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/HangulKeyboardView.java");
        String provider = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardVirtualKeyAccessibilityProvider.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");
        String normalizedView = normalizeNewlines(view);
        String normalizedProvider = normalizeNewlines(provider);

        assertTrue(summary.contains("R.string.keyboard_accessibility_name"));
        assertTrue(summary.contains("R.string.keyboard_accessibility_mode_qwerty"));
        assertTrue(summary.contains("R.string.keyboard_accessibility_mode_dingul"));
        assertTrue(summary.contains("R.string.keyboard_surface_password"));
        assertTrue(summary.contains("RuntimeDefaults.keyboardSettings("));
        assertTrue(summary.contains("RuntimeDefaults.keyboardSurface("));
        assertFalse(summary.contains("settings == null ? KeyboardSettings.defaults() : settings"));
        assertFalse(summary.contains("surface == null ? KeyboardSurface.NORMAL : surface"));
        assertTrue(provider.contains("RuntimeDefaults.keyboardSettings("));
        assertTrue(provider.contains("RuntimeDefaults.keyboardSurface("));
        assertFalse(provider.contains("? KeyboardSettings.defaults()"));
        assertFalse(provider.contains("? KeyboardSurface.NORMAL"));
        assertFalse(summary.contains("\"?��?"));
        assertFalse(summary.contains("\"?�문"));
        assertFalse(summary.contains("\"비�?번호"));
        assertTrue(normalizedView.contains("KeyboardAccessibilitySummary.describe(\n                getContext(),"));
        assertTrue(normalizedProvider.contains("KeyboardAccessibilitySummary.describe(\n                view.getContext(),"));
        assertTrue(strings.contains("name=\"keyboard_accessibility_name\""));
        assertTrue(strings.contains("name=\"keyboard_surface_password\""));
    }

    @Test
    public void gestureThresholdPolicyUsesSharedSettingsFallback() throws Exception {
        String policy = javaSource("GestureThresholdPolicy");

        assertTrue(policy.contains("RuntimeDefaults.keyboardSettings("));
        assertFalse(policy.contains("settings == null ? KeyboardSettings.defaults() : settings"));
    }

    @Test
    public void layoutCalculatorUsesSharedErgonomicsFallback() throws Exception {
        String calculator = javaSource("KeyboardLayoutCalculator");

        assertTrue(calculator.contains("RuntimeDefaults.keyboardErgonomics(ergonomicsOptions)"));
        assertFalse(calculator.contains("ergonomicsOptions == null"));
        assertFalse(calculator.contains("? KeyboardErgonomicsOptions.DEFAULT"));
        assertFalse(calculator.contains("layout(rows, settings, KeyboardErgonomicsOptions.DEFAULT"));
    }

    @Test
    public void debugKeyBoundsOverlayRenderingStaysOutOfCanvasViewBody() throws Exception {
        String view = javaSource("HangulKeyboardView");
        String renderer = javaSource("KeyboardDebugOverlayRenderer");
        String score = javaSource("GestureCandidateScore");

        assertTrue(view.contains("KeyboardDebugOverlayRenderer"));
        assertTrue(view.contains("debugOverlayRenderer.draw("));
        assertFalse(view.contains("debug key bounds  key="));
        assertFalse(view.contains("drawDebugRect("));
        assertFalse(view.contains("drawDebugOrigin("));
        assertTrue(renderer.contains("debug key bounds  key="));
        assertTrue(renderer.contains("keySlot.hitBounds()"));
        assertTrue(renderer.contains("keySlot.visualBounds()"));
        assertTrue(renderer.contains("keySlot.gestureOriginX"));
        assertTrue(renderer.contains("keySlot.gestureOriginY"));
        assertTrue(renderer.contains("ACTIVE_ORIGIN_COLOR"));
        assertTrue(renderer.contains("pending="));
        assertTrue(renderer.contains("RuntimeDefaults.stringOrDefault(lastKeyId, \"\")"));
        assertFalse(renderer.contains("lastKeyId == null ? \"\" : lastKeyId"));
        assertTrue(score.contains("RuntimeDefaults.stringOrDefault(keyId, \"\")"));
        assertFalse(score.contains("keyId == null ? \"\" : keyId"));
    }

    @Test
    public void keyboardViewRepeaterUsesMethodReferenceCallback() throws Exception {
        String view = javaSource("HangulKeyboardView");

        assertTrue(view.contains("new RepeatController(this, this::emitValue)"));
        assertFalse(view.contains("implements RepeatController.Callback"));
        assertFalse(view.contains("public void onRepeat(String value)"));
        assertFalse(view.contains("new RepeatController.Callback()"));
    }

    @Test
    public void repeatControllerUsesDirectRunnableImplementation() throws Exception {
        String controller = javaSource("RepeatController");

        assertTrue(controller.contains("final class RepeatController implements Runnable"));
        assertTrue(controller.contains("interface Scheduler"));
        assertTrue(controller.contains("private final Consumer<String> callback"));
        assertTrue(controller.contains("RuntimeDefaults.stringConsumer(callback)"));
        assertTrue(controller.contains("callback.accept(value)"));
        assertTrue(controller.contains("public void run()"));
        assertTrue(controller.contains("scheduler.postDelayed(this,"));
        assertTrue(controller.contains("scheduler.removeCallbacks(this)"));
        assertTrue(controller.contains("boolean hasFired(Object owner)"));
        assertFalse(controller.contains("interface Callback"));
        assertFalse(controller.contains("onRepeat("));
        assertFalse(controller.contains("new Runnable()"));
        assertFalse(controller.contains("repeatRunnable"));
        assertFalse(controller.contains("this.callback = callback;"));
    }

    @Test
    public void keyboardViewUsesDirectDeferredWorkCallbacks() throws Exception {
        String view = javaSource("HangulKeyboardView");

        assertTrue(view.contains("state.longPressRunnable = () -> {"));
        assertTrue(view.contains("post(this::logTypingProbePlan)"));
        assertFalse(view.contains("new Runnable()"));
    }

    @Test
    public void keyboardViewPreviewKeySelectionUsesStandardConsumer() throws Exception {
        String view = javaSource("HangulKeyboardView");
        String editor = javaSource("ThemeEditorActivity");

        assertTrue(view.contains("import java.util.function.Consumer;"));
        assertTrue(view.contains("private Consumer<GestureKey> previewKeySelectionListener"));
        assertTrue(view.contains("void setOnPreviewKeySelectionListener(Consumer<GestureKey> listener)"));
        assertTrue(view.contains("previewKeySelectionListener.accept(state.keySlot.key)"));
        assertTrue(editor.contains("preview.setOnPreviewKeySelectionListener(key -> {"));
        assertFalse(view.contains("interface OnPreviewKeySelectionListener"));
        assertFalse(view.contains("onPreviewKeySelected("));
    }

    @Test
    public void keyboardViewKeyGestureUsesStandardConsumer() throws Exception {
        String view = javaSource("HangulKeyboardView");
        String service = javaSource("S3KeyboardService");

        assertTrue(view.contains("private Consumer<String> listener"));
        assertTrue(view.contains("public void setOnKeyGestureListener(Consumer<String> listener)"));
        assertTrue(view.contains("listener.accept(value)"));
        assertTrue(service.contains("inputView.setOnKeyGestureListener(this::onKeyGesture)"));
        assertFalse(service.contains("EnglishSuggestionStripController"));
        assertFalse(service.contains("acceptEnglishSuggestion"));
        assertFalse(service.contains("HangulKeyboardView.OnKeyGestureListener"));
        assertFalse(view.contains("interface OnKeyGestureListener"));
        assertFalse(view.contains("listener.onKeyGesture("));
    }

    @Test
    public void keyboardViewAccessibilityProviderUsesDirectHostImplementation() throws Exception {
        String view = javaSource("HangulKeyboardView");

        assertTrue(view.contains("KeyboardVirtualKeyAccessibilityProvider.Host"));
        assertTrue(view.contains("new KeyboardVirtualKeyAccessibilityProvider(this, this)"));
        assertTrue(view.contains("public List<KeySlot> accessibilityKeySlots()"));
        assertTrue(view.contains("public boolean performKeyAccessibilityClick("
                + "int virtualViewId, GestureKey key)"));
        assertFalse(view.contains("new KeyboardVirtualKeyAccessibilityProvider.Host()"));
    }

    @Test
    public void keyboardViewDingulSlidePolicyUsesDirectImplementation() throws Exception {
        String view = javaSource("HangulKeyboardView");

        assertTrue(view.contains("DingulSlideIntentResolver.Policy"));
        assertTrue(view.contains("DingulSlideIntentResolver.resolve("));
        assertTrue(view.contains("public GestureAction actionFor(GestureKey key, float dx, float dy)"));
        assertTrue(view.contains("public boolean isDingulTypingKey(GestureKey key)"));
        assertFalse(view.contains("new DingulSlideIntentResolver.Policy()"));
        assertFalse(view.contains("dingulSlideIntentPolicy"));
    }

    @Test
    public void debugOverlaySettingsPersistenceStaysBehindController() throws Exception {
        String activity = javaSource("MainActivity");
        String androidImeController = javaSource("AndroidImeSettingsController");
        String controller = javaSource("DebugOverlaySettingsController");

        assertTrue(activity.contains("AndroidImeSettingsController"));
        assertTrue(activity.contains("LinearLayout page = SettingsRowBuilder.vertical(this);"));
        assertTrue(activity.contains("LinearLayout contentRoot = SettingsRowBuilder.vertical(this);"));
        assertTrue(activity.contains(
                "new AndroidImeSettingsController(this, this::isDebuggableBuild, this::syncControls)"));
        assertTrue(androidImeController.contains("DebugOverlaySettingsController"));
        assertTrue(androidImeController.contains("debugOverlaySettingsController.addTo("));
        assertTrue(androidImeController.contains("private final BooleanSupplier debuggableBuild"));
        assertTrue(androidImeController.contains("private final Runnable onChanged"));
        assertTrue(androidImeController.contains("RuntimeDefaults.booleanSupplier("));
        assertTrue(androidImeController.contains("RuntimeDefaults.runnable("));
        assertFalse(activity.contains("AndroidImeSettingsController.Host"));
        assertFalse(androidImeController.contains("interface Host"));
        assertFalse(androidImeController.contains("this.onChanged = onChanged"));
        assertFalse(androidImeController.contains("this.debuggableBuild = debuggableBuild"));
        assertFalse(activity.contains("debugKeyBoundsOverlayCheckBox"));
        assertFalse(activity.contains("debugShowResolverScoresCheckBox"));
        assertFalse(activity.contains("new LinearLayout(this)"));
        assertFalse(activity.contains("setOrientation(LinearLayout.VERTICAL)"));
        assertFalse(activity.contains("saveDebugShowResolverScores("));
        assertFalse(activity.contains("loadDebugShowResolverScores("));
        assertTrue(controller.contains("saveDebugOverlay("));
        assertTrue(controller.contains("saveDebugShowResolverScores("));
        assertTrue(controller.contains("loadDebugShowResolverScores("));
        assertTrue(controller.contains("setEnabled(overlayEnabled)"));
        assertTrue(controller.contains("SettingsRowBuilder.checkBoxRow("));
        assertTrue(controller.contains("RuntimeDefaults.runnable("));
        assertTrue(controller.contains("onChanged.run()"));
        assertFalse(controller.contains("onChanged != null"));
        assertFalse(controller.contains("private void notifyChanged()"));
        assertFalse(controller.contains("notifyChanged();"));
        assertFalse(controller.contains("private CheckBox addCheckBox("));
        assertEquals(0, countOccurrences(controller, "UserInputListeners.checked("));
    }

    @Test
    public void quickStartExposesSharedImeActivationActionsBeforeDiagnostics() throws Exception {
        String hub = javaSource("SettingsHubController");
        String androidIme = javaSource("AndroidImeSettingsController");
        String actions = javaSource("AndroidImeActions");
        String status = javaSource("AndroidImeStatus");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertTrue(hub.contains("R.string.settings_hub_keyboard_setup"));
        assertTrue(hub.contains("R.string.settings_hub_enable_keyboard"));
        assertTrue(hub.contains("R.string.settings_hub_choose_keyboard"));
        assertTrue(hub.contains("R.string.settings_hub_manage_keyboards"));
        assertTrue(hub.contains("pickerButton.setEnabled(state != AndroidImeStatus.State.DISABLED)"));
        assertTrue(hub.contains("AndroidImeActions.openInputSettings(activity)"));
        assertTrue(hub.contains("AndroidImeActions.showInputPicker(activity)"));
        assertTrue(hub.contains("AndroidImeStatus.resolve(activity)"));
        assertFalse(hub.contains("BuildInfoProvider.summary(activity)"));
        assertTrue(androidIme.contains("BuildInfoProvider.summary(activity)"));
        assertTrue(androidIme.contains("AndroidImeActions.openInputSettings(activity)"));
        assertTrue(androidIme.contains("AndroidImeActions.showInputPicker(activity)"));
        assertTrue(actions.contains("Settings.ACTION_INPUT_METHOD_SETTINGS"));
        assertTrue(actions.contains("manager.showInputMethodPicker()"));
        assertTrue(status.contains("getEnabledInputMethodList()"));
        assertTrue(status.contains("Settings.Secure.DEFAULT_INPUT_METHOD"));
        assertTrue(strings.contains("name=\"settings_hub_keyboard_setup\""));
        assertTrue(strings.contains("name=\"settings_hub_keyboard_disabled\""));
        assertTrue(strings.contains("name=\"settings_hub_keyboard_enabled\""));
        assertTrue(strings.contains("name=\"settings_hub_keyboard_selected\""));
        assertTrue(strings.contains("name=\"settings_hub_manage_keyboards\""));
        assertTrue(strings.contains("name=\"settings_current_theme_format\""));
    }

    @Test
    public void inputAssistanceModePersistenceStaysOutOfMainActivityBody() throws Exception {
        String activity = javaSource("MainActivity");
        String controller = javaSource("InputAssistanceSettingsController");

        assertTrue(activity.contains("InputAssistanceSettingsController"));
        assertFalse(activity.contains("saveShowHangulConsonantSlideHints("));
        assertFalse(activity.contains("saveShowHangulVowelSlideHints("));
        assertFalse(activity.contains("saveShowSpacebarSlideHints("));
        assertTrue(controller.contains("saveShowHangulConsonantSlideHints("));
        assertTrue(controller.contains("saveShowHangulVowelSlideHints("));
        assertTrue(controller.contains("saveShowSpacebarSlideHints("));
        assertFalse(activity.contains("implements InputAssistanceSettingsController.Host"));
        assertFalse(controller.contains("interface Host"));
        assertTrue(controller.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(controller.contains("private final Supplier<KeyboardErgonomicsOptions> ergonomicsOptions"));
        assertTrue(controller.contains("private final BooleanSupplier debuggableBuild"));
        assertFalse(controller.contains("currentThemeCustomMarker"));
        assertTrue(controller.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(controller.contains(
                "private final BiConsumer<KeyboardSettings, KeyboardErgonomicsOptions>"));
        assertTrue(controller.contains("private final Runnable controlsSyncer"));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardErgonomicsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.booleanSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.runnable("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsAndErgonomicsConsumer("));
        assertTrue(controller.contains("InputAssistanceMode.displayOrder("));
        assertTrue(controller.contains("SettingsRowBuilder.optionSpinner("));
        assertTrue(controller.contains("InputAssistanceMode.indexOf("));
        assertFalse(controller.contains("SettingsDisplayLabels.labels(context, modes)"));
        assertFalse(controller.contains("InputAssistanceMode.values()"));
        assertFalse(controller.contains("modes[position]"));
        assertFalse(controller.contains("static InputAssistanceMode[] availableModes("));
        assertFalse(controller.contains("static int indexOf(InputAssistanceMode[] modes"));
        assertFalse(controller.contains("this.debuggableBuild = debuggableBuild"));
        assertFalse(controller.contains("this.settingsAndErgonomicsSaver = settingsAndErgonomicsSaver"));

        String mode = javaSource("InputAssistanceMode");
        assertTrue(mode.contains("static InputAssistanceMode[] displayOrder(boolean debuggableBuild)"));
        assertTrue(mode.contains("static int indexOf(InputAssistanceMode[] modes, InputAssistanceMode mode)"));
    }

    @Test
    public void hapticSettingsPersistenceStaysBehindController() throws Exception {
        String activity = javaSource("MainActivity");
        String inputFeelController = javaSource("InputFeelSettingsController");
        String controller = javaSource("HapticSettingsController");

        assertTrue(activity.contains("InputFeelSettingsController"));
        assertTrue(inputFeelController.contains("HapticSettingsController"));
        assertTrue(inputFeelController.contains("hapticSettingsController.addTo("));
        assertFalse(activity.contains("hapticCheckBox"));
        assertFalse(activity.contains("differentiatedHapticCheckBox"));
        assertFalse(activity.contains("hapticDurationSeekBar"));
        assertFalse(activity.contains("hapticGapSeekBar"));
        assertFalse(activity.contains("saveHapticTickDurationMs("));
        assertFalse(activity.contains("saveHapticTickGapMs("));
        assertFalse(activity.contains("saveDifferentiatedHapticEnabled("));
        assertTrue(controller.contains("withHapticFeedback("));
        assertTrue(controller.contains("saveHapticTickDurationMs("));
        assertTrue(controller.contains("saveHapticTickGapMs("));
        assertTrue(controller.contains("saveDifferentiatedHapticEnabled("));
        assertTrue(controller.contains("SettingsValueFormatter.hapticDuration("));
        assertTrue(controller.contains("setEnabled(safe.hapticFeedbackEnabled)"));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(controller.contains("RuntimeDefaults.runnable("));
        assertFalse(controller.contains("settings == null ? null"));
        assertFalse(controller.contains("settingsSaver != null"));
        assertFalse(controller.contains("controlsSyncer != null"));
        assertFalse(controller.contains("new LocalDataControlsController(context)"));
    }

    @Test
    public void repeatSettingsPersistenceStaysBehindController() throws Exception {
        String activity = javaSource("MainActivity");
        String inputFeelController = javaSource("InputFeelSettingsController");
        String controller = javaSource("RepeatSettingsController");

        assertTrue(activity.contains("InputFeelSettingsController"));
        assertTrue(inputFeelController.contains("RepeatSettingsController"));
        assertTrue(inputFeelController.contains("repeatSettingsController.addTo("));
        assertFalse(activity.contains("repeatStartDelaySeekBar"));
        assertFalse(activity.contains("repeatIntervalSeekBar"));
        assertFalse(activity.contains("repeatStartDelayValue"));
        assertFalse(activity.contains("repeatIntervalValue"));
        assertFalse(activity.contains("withRepeatTiming("));
        assertTrue(controller.contains("withRepeatTiming("));
        assertTrue(controller.contains("SettingsValueFormatter.repeatStartDelay("));
        assertTrue(controller.contains("SettingsValueFormatter.repeatInterval("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertFalse(controller.contains("settings == null ? null"));
        assertFalse(controller.contains("settingsSaver != null"));
    }

    @Test
    public void quickStartAndOneFingerFlowLeadTheSettingsWizard() throws Exception {
        String activity = javaSource("MainActivity");

        int inputFeelIndex = activity.indexOf("R.string.settings_input_feel_section");
        int hubIndex = activity.indexOf("R.string.settings_hub_title");
        int oneFingerIndex = activity.indexOf("R.string.settings_one_finger_section");
        int layoutIndex = activity.indexOf("R.string.settings_layout_section");
        int displayIndex = activity.indexOf("R.string.settings_display_section");

        assertTrue(hubIndex >= 0);
        assertTrue(oneFingerIndex > hubIndex);
        assertTrue(layoutIndex > oneFingerIndex);
        assertTrue(inputFeelIndex > layoutIndex);
        assertTrue(displayIndex > inputFeelIndex);
    }

    @Test
    public void mainSettingsUsesWizardNavigationInsteadOfExpandedLongList() throws Exception {
        String activity = javaSource("MainActivity");
        String wizard = javaSource("SettingsWizardController");
        String search = javaSource("SettingsWizardSearch");
        String card = javaSource("SettingsSectionCard");
        String subsection = javaSource("SettingsSubsection");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertTrue(activity.contains("private SettingsWizardController settingsWizardController;"));
        assertTrue(activity.contains("new SettingsWizardController("));
        assertTrue(activity.contains("contentScroll.scrollTo(0, 0)"));
        assertTrue(activity.contains("page.addView(contentScroll, SettingsRowBuilder.matchWeightedFill())"));
        assertTrue(activity.contains("private boolean usesCompactHeightSettingsLayout()"));
        assertTrue(activity.contains("screenHeightDp < 520"));
        assertTrue(activity.contains("demoShowKeyboard ||"));
        assertTrue(activity.contains("LinearLayout scrollingPage = SettingsRowBuilder.vertical(this)"));
        assertTrue(activity.contains("pageScroll.scrollTo(0, contentRoot.getTop())"));
        assertTrue(activity.contains("SOFT_INPUT_STATE_ALWAYS_HIDDEN"));
        assertTrue(activity.contains("if (!demoShowKeyboard)"));
        assertTrue(activity.contains("page.requestFocus();"));
        assertTrue(activity.contains("settingsWizardController.addStep(titleResId, keywordsResId)"));
        assertTrue(activity.contains("settingsWizardController.finishSetup();"));
        assertTrue(activity.contains("settingsWizardController.restoreState(savedInstanceState)"));
        assertTrue(activity.contains("settingsWizardController.saveState(outState)"));
        assertTrue(activity.contains("SettingsSubsection.add("));
        assertTrue(wizard.contains("private boolean showAll;"));
        assertTrue(wizard.contains("private final Runnable scrollContentToTop"));
        assertTrue(wizard.contains("scrollContentToTop.run();"));
        assertTrue(wizard.contains("contentRoot.addView(card.container"));
        assertTrue(wizard.contains("void finishSetup()"));
        assertTrue(wizard.contains("collectSearchableText(step.card.content)"));
        assertTrue(wizard.contains("appendSearchableText(group.getChildAt(index), destination)"));
        assertTrue(wizard.contains("void saveState(Bundle outState)"));
        assertTrue(wizard.contains("void restoreState(Bundle savedState)"));
        assertTrue(wizard.contains("STATE_SELECTED_INDEX"));
        assertTrue(wizard.contains("STATE_SEARCH_QUERY"));
        assertTrue(wizard.contains("selectRelative(-1)"));
        assertTrue(wizard.contains("selectRelative(1)"));
        assertTrue(wizard.contains("HorizontalScrollView stepScroller"));
        assertTrue(wizard.contains("searchInput.setSaveEnabled(false)"));
        assertTrue(wizard.contains("clearSearchButton.setVisibility(searchActive()"));
        assertTrue(wizard.contains("R.string.settings_search_clear"));
        assertTrue(wizard.contains("void hideKeyboardWhenTouchingOutside(MotionEvent event)"));
        assertTrue(wizard.contains("SettingsWizardSearch.normalize(query)"));
        assertTrue(wizard.contains("SettingsSubsection.setSearchExpansion("));
        assertTrue(wizard.contains("button.setSingleLine(true)"));
        assertTrue(wizard.contains("R.string.settings_wizard_show_all"));
        assertTrue(wizard.contains("R.string.settings_wizard_show_steps"));
        assertTrue(search.contains("static String normalize(CharSequence value)"));
        assertTrue(search.contains("for (String rawTerm : query.toString().split("));
        assertTrue(card.contains("void setWizardTitle("));
        assertTrue(card.contains("void setToggleEnabled(boolean enabled)"));
        assertFalse(subsection.contains("SettingsSectionCard"));
        assertTrue(subsection.contains("header.setMinimumHeight(SettingsRowBuilder.dp(context, 48))"));
        assertTrue(subsection.contains("R.drawable.ic_settings_chevron"));
        assertTrue(subsection.contains("expandedBeforeSearch"));
        assertTrue(subsection.contains("R.id.settings_subsection_tag"));
        assertFalse(strings.contains("name=\"settings_wizard_title\""));
        assertTrue(strings.contains("name=\"settings_search_hint\""));
        assertTrue(strings.contains("name=\"settings_search_empty\""));
        assertTrue(strings.contains("name=\"settings_subsection_expanded_format\""));
        assertTrue(strings.contains("name=\"settings_wizard_step_title_format\""));
        assertTrue(strings.contains("name=\"settings_wizard_show_all\""));
        assertTrue(strings.contains("name=\"settings_wizard_show_steps\""));
        assertTrue(javaSource("GestureTouchSettingsController").contains(".withHitSlop("));
        assertTrue(javaSource("RemoteWindowsSettingsController").contains("settings_remote_basics_subsection"));
        assertTrue(javaSource("RemoteWindowsSettingsController").contains("settings_remote_app_overrides_subsection"));
        assertTrue(strings.contains("name=\"settings_hit_slop_format\""));
    }

    @Test
    public void settingsActivitiesUsePaletteAwareSystemBars() throws Exception {
        String bars = javaSource("SettingsSystemBars");

        assertTrue(bars.contains("window.setStatusBarColor(palette.background)"));
        assertTrue(bars.contains("window.setNavigationBarColor(palette.background)"));
        assertTrue(bars.contains("SYSTEM_UI_FLAG_LIGHT_STATUS_BAR"));
        assertTrue(bars.contains("SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR"));
        assertTrue(bars.contains("static void applyTopInset(View root)"));
        assertTrue(bars.contains("insets.getSystemWindowInsetTop()"));
        assertTrue(javaSource("MainActivity").contains("SettingsSystemBars.apply(this);"));
        assertTrue(javaSource("MainActivity").contains("SettingsSystemBars.applyTopInset(page);"));
        assertTrue(javaSource("ThemeSelectorActivity").contains("SettingsSystemBars.apply(this);"));
        assertTrue(javaSource("ThemeSelectorActivity").contains("SettingsSystemBars.applyTopInset(content);"));
        assertTrue(javaSource("ThemeSelectorActivity").contains("group.addView(pairRow"));
        assertTrue(javaSource("ThemeEditorActivity").contains("SettingsSystemBars.apply(this);"));
        assertTrue(javaSource("ThemeEditorActivity").contains("SettingsSystemBars.applyTopInset(content);"));
        assertTrue(javaSource("AccentPlacementActivity").contains("SettingsSystemBars.apply(this);"));
        assertTrue(javaSource("AccentPlacementActivity").contains("SettingsSystemBars.applyTopInset(content);"));
        assertTrue(javaSource("DiagnosticsActivity").contains("SettingsSystemBars.applyTopInset(content);"));
        assertTrue(javaSource("BackupRestoreActivity").contains("SettingsSystemBars.applyTopInset(content);"));
    }

    @Test
    public void standardCheckRejectsMojibakeInUserFacingSources() throws Exception {
        String checkScript = readWorkspaceFile("scripts/check.ps1");
        String settingsAudit = readWorkspaceFile("scripts/audit-settings-usage.ps1");

        assertTrue(checkScript.contains("Potential mojibake detected"));
        assertTrue(checkScript.contains("Select-String -Pattern"));
        assertTrue(checkScript.contains("-Encoding UTF8"));
        assertTrue(checkScript.contains("audit-settings-usage.ps1"));
        assertTrue(checkScript.contains("-FailOnUnused"));
        assertTrue(settingsAudit.contains("KeyboardSettings fields without runtime consumers"));
    }

    @Test
    public void oneFingerTimingSettingsUseTheirOwnWizardStep() throws Exception {
        String activity = javaSource("MainActivity");
        String controller = javaSource("OneFingerInputSettingsController");
        String practiceController = javaSource("OneFingerPracticeController");
        String practiceSession = javaSource("OneFingerPracticeSession");
        String settingsHub = javaSource("SettingsHubController");
        String convenience = javaSource("InputConvenienceSettingsController");
        String preferences = javaSource("KeyboardPreferences");
        String formatter = javaSource("SettingsValueFormatter");

        assertFalse(activity.contains("singleTapStartHoldSeekBar"));
        assertFalse(activity.contains("singleTapCommitHoldSeekBar"));
        assertTrue(activity.contains("R.string.settings_one_finger_section"));
        assertTrue(activity.contains("new OneFingerInputSettingsController("));
        assertTrue(controller.contains("actionHoldSeekBar"));
        assertTrue(controller.contains("targetDwellSeekBar"));
        assertTrue(controller.contains("OneFingerInputSpeedPreset"));
        assertTrue(controller.contains("R.string.one_finger_current_state_format"));
        assertTrue(controller.contains("R.string.one_finger_current_state_off"));
        assertTrue(controller.contains("SettingsSubsection.add("));
        assertTrue(controller.contains("R.string.one_finger_help_subsection"));
        assertTrue(controller.contains("practiceController.addTo(enabledControls)"));
        assertTrue(practiceController.contains("input.setSaveEnabled(false)"));
        assertTrue(practiceController.contains("OneFingerPracticeSession"));
        assertTrue(practiceController.contains("gestureGuide.setText(lesson.guideResId)"));
        assertFalse(practiceController.contains("KeyboardPreferences"));
        assertFalse(practiceSession.contains("android.content.Context"));
        assertTrue(practiceSession.contains("final int guideResId"));
        assertTrue(practiceSession.contains("\"어아오우\""));
        assertTrue(practiceSession.contains("\"워와외위\""));
        assertTrue(practiceSession.contains("\"으에애왜웨\""));
        assertTrue(practiceSession.contains("\"요야유여\""));
        assertTrue(practiceSession.contains("\"?.,/\""));
        assertTrue(activity.contains("oneFingerInputSettingsController.hideKeyboardWhenTouchingOutside(event)"));
        assertTrue(activity.contains("settingsHubController.sync()"));
        assertTrue(settingsHub.contains("R.string.settings_hub_current_state_format"));
        assertTrue(settingsHub.contains("loadSingleTapCommitModeEnabled(activity)"));
        assertFalse(convenience.contains("singleTapStartHoldSeekBar"));
        assertFalse(convenience.contains("singleTapCommitHoldSeekBar"));
        assertTrue(controller.contains("saveSingleTapStartHoldMs("));
        assertTrue(controller.contains("saveSingleTapCommitHoldMs("));
        assertTrue(controller.contains("preset == OneFingerInputSpeedPreset.CUSTOM"));
        assertTrue(controller.contains("advancedVisible = true;"));
        assertTrue(preferences.contains("SINGLE_TAP_START_HOLD_MS"));
        assertTrue(preferences.contains("SINGLE_TAP_COMMIT_HOLD_MS"));
        assertTrue(formatter.contains("singleTapStartHold("));
        assertTrue(formatter.contains("singleTapCommitHold("));
    }

    @Test
    public void oneFingerContinuationKeepsTargetAsGestureOrigin() throws Exception {
        String view = javaSource("HangulKeyboardView");
        String service = javaSource("S3KeyboardService");
        String session = javaSource("OneFingerInputSession");
        String geometry = javaSource("OneFingerSelectionGeometry");
        String flowGuide = javaSource("OneFingerFlowGuideView");
        String policy = javaSource("SingleTapCommitModePolicy");

        assertFalse(view.contains("private static final class OneFingerInputSession"));
        assertTrue(session.contains("final class OneFingerInputSession<K>"));
        assertTrue(view.contains("state.oneFinger.selectKey(state.keySlot, state.downX, state.downY);"));
        assertTrue(view.contains("updateOneFingerFreeRoam(state, x, y);"));
        assertTrue(view.contains("isInsideOneFingerSelectionCircle(candidate, x, y, false)"));
        assertTrue(view.contains("isInsideOneFingerSelectionCircle(hoveredCandidate, x, y, true)"));
        assertTrue(view.contains("RectF touchBounds = keySlot.hitBounds();"));
        assertTrue(view.contains("keySlot.bounds.centerX()"));
        assertTrue(view.contains("touchBounds.width()"));
        assertTrue(view.contains("ONE_FINGER_TARGET_SETTLE_SLOP_DP = 6"));
        assertTrue(view.contains("state.oneFinger.candidateDriftedBeyond("));
        assertTrue(view.contains("scheduleOneFingerCandidateSelection(state, hoveredCandidate)"));
        assertTrue(session.contains("boolean candidateDriftedBeyond("));
        assertTrue(geometry.contains("ENTRY_RADIUS_FRACTION = 0.38f"));
        assertTrue(geometry.contains("EXIT_RADIUS_MULTIPLIER = 1.20f"));
        assertTrue(geometry.contains("float normalizedX = dx / radiusX"));
        assertTrue(view.contains("OneFingerSelectionGeometry.radiusX("));
        assertTrue(view.contains("OneFingerSelectionGeometry.radiusY("));
        assertTrue(view.contains("ONE_FINGER_MIN_SELECTION_RADIUS_DP = 20"));
        int pressStep = flowGuide.indexOf("R.string.one_finger_flow_press");
        int commitStep = flowGuide.indexOf("R.string.one_finger_flow_commit");
        int roamStep = flowGuide.indexOf("R.string.one_finger_flow_roam");
        int nextKeyStep = flowGuide.indexOf("R.string.one_finger_flow_select");
        assertTrue(pressStep >= 0);
        assertTrue(commitStep > pressStep);
        assertTrue(roamStep > commitStep);
        assertTrue(nextKeyStep > roamStep);
        assertTrue(flowGuide.contains("drawRepeatLoop("));
        assertTrue(view.contains("state.oneFinger.selectHoveredCandidate("));
        assertTrue(view.contains("ONE_FINGER_POST_SELECT_ARM_DELAY_MS"));
        assertTrue(session.contains("boolean slideCommitReady(long nowMs)"));
        assertTrue(view.contains("drawOneFingerSelectionFeedback(canvas);"));
        assertTrue(view.contains("state.oneFinger.pendingProgress(nowMs)"));
        assertTrue(javaSource("OneFingerInputSettingsController")
                .contains("new OneFingerFlowGuideView(context)"));
        assertTrue(view.contains("state.oneFinger.keySelected() && state.oneFinger.targetSlot == keySlot"));
        assertTrue(session.contains("selectKey(keySlot, currentX, currentY);"));
        assertTrue(view.contains("return state.oneFinger.keySelected() ? state.oneFinger.targetSlot : null;"));
        assertFalse(view.contains("state.oneFinger.startFlow(state.keySlot, state.downX, state.downY);"));
        assertFalse(view.contains("state.oneFinger.armTarget(target, x, y);"));
        assertTrue(view.contains("state.oneFinger.lastCommittedReentryBlocked()"));
        assertTrue(view.contains("state.oneFinger.allowLastCommittedReentry();"));
        assertTrue(view.contains("state.previewBubble = null;"));
        assertTrue(view.contains("effectivePreviewKeySlot(state)"));
        assertTrue(view.contains("previousOneFingerModeEnabled != singleTapCommitModeEnabled"));
        assertEquals(4, countOccurrences(view, "rebuildRowsForModelChange()"));
        assertTrue(view.contains("if (!activeTouches.isEmpty()) {\n            clearTouchState();"));
        assertTrue(view.contains("emitValue(value);\n        if (!activeTouches.contains(state))"));
        assertTrue(view.contains("void cancelActiveGestureSession()"));
        assertEquals(3, countOccurrences(service, "inputView.cancelActiveGestureSession();"));
        assertTrue(view.contains("SingleTapCommitModePolicy.selectedMoveDecision("));
        assertTrue(view.contains("handleOneFingerSelectedRelease("));
        assertTrue(view.contains("SingleTapCommitModePolicy.resolveSelectedRelease("));
        assertTrue(view.contains("SingleTapCommitModePolicy.pausesTapHoldForMovement("));
        assertTrue(policy.contains("static SelectedRelease resolveSelectedRelease("));
        assertTrue(policy.contains("!KeyboardCommands.CMD_NOOP.equals(value)"));
        assertTrue(view.contains("selected.key.mappedValueFor(action)"));
        assertTrue(view.contains("keySlot.key.mappedValueFor(state.activeAction)"));
        assertFalse(view.contains("pendingSingleTapOutput"));
        assertFalse(view.contains("pendingSingleTapCommitRunnable"));
        assertTrue(view.contains("gestureStateForKeySlot(state, keySlot).isLocked()"));
    }

    @Test
    public void gestureTouchSettingsPersistenceStaysBehindController() throws Exception {
        String activity = javaSource("MainActivity");
        String inputFeelController = javaSource("InputFeelSettingsController");
        String controller = javaSource("GestureTouchSettingsController");
        String rowBuilder = javaSource("SettingsRowBuilder");

        assertTrue(activity.contains("InputFeelSettingsController"));
        assertTrue(inputFeelController.contains("GestureTouchSettingsController"));
        assertTrue(inputFeelController.contains("gestureTouchSettingsController.addTo("));
        assertFalse(activity.contains("gestureThresholdSeekBar"));
        assertFalse(activity.contains("touchYOffsetSeekBar"));
        assertFalse(activity.contains("spacebarCursorDeadZoneSeekBar"));
        assertFalse(activity.contains("dingulVowelGestureProfileSpinner"));
        assertFalse(activity.contains("withGestureThreshold("));
        assertFalse(activity.contains("withTouchYOffset("));
        assertFalse(activity.contains("saveSpacebarCursorDeadZoneDp("));
        assertFalse(activity.contains("saveDingulVowelGestureProfile("));
        assertTrue(controller.contains("withGestureThreshold("));
        assertTrue(controller.contains("withTouchYOffset("));
        assertTrue(controller.contains("saveSpacebarCursorDeadZoneDp("));
        assertTrue(controller.contains("saveDingulVowelGestureProfile("));
        assertTrue(controller.contains("SettingsValueFormatter.gestureThreshold("));
        assertTrue(controller.contains("SettingsValueFormatter.spacebarCursorDeadZone("));
        assertTrue(controller.contains("SettingsValueFormatter.touchYOffset("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(controller.contains("RuntimeDefaults.runnable("));
        assertTrue(controller.contains("SettingsRowBuilder.optionSpinner("));
        assertTrue(controller.contains("DingulVowelGestureProfile.displayOrder()"));
        assertTrue(controller.contains("DingulVowelGestureProfile.indexOf("));
        assertFalse(controller.contains("DingulVowelGestureProfile.fromPosition"));
        assertFalse(controller.contains("SettingsDisplayLabels.labels(context, VOWEL_PROFILE_ORDER)"));
        assertFalse(controller.contains("DingulVowelGestureProfile.values()"));
        assertFalse(controller.contains("loadDingulVowelGestureProfile(context).ordinal()"));
        assertFalse(controller.contains("settings == null ? null"));
        assertFalse(controller.contains("settingsSaver != null"));
        assertFalse(controller.contains("controlsSyncer != null"));
        assertTrue(rowBuilder.contains(
                "static <T extends SettingsLabelOption> Spinner optionSpinner("));
        assertTrue(rowBuilder.contains("SettingsDisplayLabels.labels(context, options)"));
        assertTrue(rowBuilder.contains("safeConsumer.accept(options[position])"));

        String vowelProfile = javaSource("DingulVowelGestureProfile");
        assertTrue(vowelProfile.contains("private static final DingulVowelGestureProfile[] DISPLAY_ORDER"));
        assertTrue(vowelProfile.contains("static DingulVowelGestureProfile[] displayOrder()"));
        assertTrue(vowelProfile.contains("static int indexOf(DingulVowelGestureProfile selected)"));
        assertFalse(vowelProfile.contains("fromPosition("));
    }

    @Test
    public void inputConvenienceSettingsPersistenceStaysBehindController() throws Exception {
        String activity = javaSource("MainActivity");
        String inputFeelController = javaSource("InputFeelSettingsController");
        String controller = javaSource("InputConvenienceSettingsController");

        assertTrue(activity.contains("InputFeelSettingsController"));
        assertTrue(inputFeelController.contains("InputConvenienceSettingsController"));
        assertTrue(inputFeelController.contains("inputConvenienceSettingsController.addTo("));
        assertFalse(activity.contains("touchBiasAutoCorrectionCheckBox"));
        assertFalse(activity.contains("palmRejectionCheckBox"));
        assertFalse(activity.contains("clipboardHistoryCheckBox"));
        assertFalse(activity.contains("doubleSpacePeriodCheckBox"));
        assertFalse(activity.contains("saveTouchBiasAutoCorrectionEnabled("));
        assertFalse(activity.contains("savePalmRejectionEnabled("));
        assertTrue(controller.contains("saveTouchBiasAutoCorrectionEnabled("));
        assertTrue(controller.contains("savePalmRejectionEnabled("));
        assertTrue(controller.contains("withEnglishDoubleSpacePeriod("));
        assertTrue(controller.contains("localDataControls.get().setClipboardHistoryEnabled("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(controller.contains("RuntimeDefaults.runnable("));
        assertTrue(controller.contains("RuntimeDefaults.localDataControlsSupplier("));
        assertFalse(controller.contains("settings == null ? null"));
        assertFalse(controller.contains("settingsSaver != null"));
        assertFalse(controller.contains("controlsSyncer != null"));
        assertFalse(controller.contains("new LocalDataControlsController(context)"));
    }

    @Test
    public void localDataDeletionStaysBehindSettingsController() throws Exception {
        String activity = javaSource("MainActivity");
        String inputFeelController = javaSource("InputFeelSettingsController");
        String settingsController = javaSource("LocalDataSettingsController");
        String controlsController = javaSource("LocalDataControlsController");

        assertTrue(activity.contains("InputFeelSettingsController"));
        assertTrue(inputFeelController.contains("LocalDataSettingsController"));
        assertTrue(inputFeelController.contains("new LocalDataControlsController("));
        assertFalse(activity.contains("new ClipboardStore("));
        assertFalse(activity.contains("TouchBiasStore.reset("));
        assertFalse(activity.contains("RemoteCompatibilityLog.clear("));
        assertFalse(activity.contains("saveClipboardHistoryEnabled("));
        assertFalse(activity.contains("loadClipboardHistoryEnabled("));
        assertFalse(settingsController.contains("interface Host"));
        assertTrue(settingsController.contains("Supplier<LocalDataControlsController> localDataControls"));
        assertTrue(settingsController.contains("RuntimeDefaults.localDataControlsSupplier("));
        assertTrue(settingsController.contains("localDataControls.get().clearAllLocalData()"));
        assertTrue(settingsController.contains("localDataControls.get().resetTouchCorrectionAndInputLogs()"));
        assertTrue(settingsController.contains("localDataControls.get().clearClipboardHistory()"));
        assertFalse(settingsController.contains("private LocalDataControlsController localDataControls()"));
        assertFalse(settingsController.contains("new LocalDataControlsController(context)"));
        assertTrue(controlsController.contains("new ClipboardStore("));
        assertTrue(controlsController.contains("TouchBiasStore.reset("));
        assertTrue(controlsController.contains("RemoteCompatibilityLog.clear("));
        assertTrue(controlsController.contains("saveClipboardHistoryEnabled("));
        assertTrue(controlsController.contains("loadClipboardHistoryEnabled("));
        assertTrue(controlsController.contains("void clearAllLocalData()"));
        assertTrue(controlsController.contains("clearClipboardHistory();"));
        assertTrue(controlsController.contains("resetTouchCorrectionAndInputLogs();"));
        assertTrue(controlsController.contains("clearRemoteCompatibilityLog();"));
    }

    @Test
    public void localDataSummaryUsesStringResources() throws Exception {
        String controller = javaSource("LocalDataControlsController");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(controller.contains("\"로컬 ?�이??"));
        assertFalse(controller.contains("\"?�력 로그 "));
        assertFalse(controller.contains("\"?�격 ?�스??"));
        assertTrue(controller.contains("R.string.local_data_summary_format"));
        assertTrue(strings.contains("name=\"local_data_summary_format\""));
    }

    @Test
    public void mainSettingsLocalDataAndReservedPhraseCopyUseStringResources() throws Exception {
        String activity = javaSource("MainActivity");
        String localDataSettings = javaSource("LocalDataSettingsController");
        String reservedPhraseSettings = javaSource("ReservedPhraseSettingsController");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(activity.contains("\\uB85C\\uCEEC \\uC785\\uB825 \\uB85C\\uADF8"));
        assertFalse(activity.contains("\\uD130\\uCE58 \\uBCF4\\uC815/\\uC785\\uB825 \\uB85C\\uADF8"));
        assertFalse(activity.contains("\\uBE44\\uC6B0\\uBA74 \\uC785\\uB825\\uD558\\uC9C0"));
        assertTrue(localDataSettings.contains("R.string.local_data_disclosure"));
        assertTrue(localDataSettings.contains("R.string.clear_all_local_data"));
        assertTrue(localDataSettings.contains("localDataControls.get().clearAllLocalData()"));
        assertTrue(localDataSettings.contains("R.string.clear_touch_correction_and_input_logs"));
        assertTrue(reservedPhraseSettings.contains("R.string.reserved_phrase_empty_hint"));
        assertTrue(strings.contains("name=\"local_data_disclosure\""));
        assertTrue(strings.contains("name=\"clear_all_local_data\""));
        assertTrue(strings.contains("name=\"reserved_phrase_empty_hint\""));
    }

    @Test
    public void mainSettingsDynamicValueFormattingStaysInDedicatedFormatter() throws Exception {
        String activity = javaSource("MainActivity");
        String layoutController = javaSource("LayoutSettingsController");
        String gestureTouchController = javaSource("GestureTouchSettingsController");
        String motionEffectController = javaSource("MotionEffectSettingsController");
        String formatter = javaSource("SettingsValueFormatter");

        assertTrue(activity.contains("LayoutSettingsController"));
        assertFalse(activity.contains("initializeHiddenAppearanceControls()"));
        assertFalse(activity.contains("appearanceSettingsController"));
        assertTrue(activity.contains("SettingsRowBuilder.label(this, R.string.app_name)"));
        assertTrue(activity.contains("SettingsRowBuilder.matchWrap("));
        assertTrue(activity.contains("SettingsRowBuilder.dp("));
        assertFalse(activity.contains("private LinearLayout addExpandableSection("));
        assertFalse(activity.contains("addVisualControls("));
        assertFalse(activity.contains("unusedRoot"));
        assertFalse(activity.contains("new TextView("));
        assertFalse(activity.contains("new LinearLayout.LayoutParams("));
        assertFalse(activity.contains("MotionEffectSettingsController.Host"));
        assertTrue(activity.contains("new MotionEffectSettingsController(this, this::syncControls)"));
        assertTrue(motionEffectController.contains("private final Runnable onChanged"));
        assertTrue(motionEffectController.contains("RuntimeDefaults.runnable("));
        assertTrue(motionEffectController.contains("SettingsRowBuilder.optionSpinner("));
        assertTrue(motionEffectController.contains("MotionEffectLevel.displayOrder()"));
        assertTrue(motionEffectController.contains("MotionEffectLevel.indexOf("));
        assertFalse(motionEffectController.contains("MotionEffectLevel.fromPosition"));
        assertFalse(motionEffectController.contains("SettingsDisplayLabels.labels(activity, MOTION_EFFECT_ORDER)"));
        assertFalse(motionEffectController.contains("MotionEffectLevel.values()"));
        assertFalse(motionEffectController.contains("loadMotionEffectLevel(activity).ordinal()"));
        assertFalse(motionEffectController.contains("onChanged != null"));
        assertFalse(motionEffectController.contains("this.onChanged = onChanged"));
        assertFalse(motionEffectController.contains("interface Host"));

        String motionEffect = javaSource("MotionEffectLevel");
        assertTrue(motionEffect.contains("private static final MotionEffectLevel[] DISPLAY_ORDER"));
        assertTrue(motionEffect.contains("static MotionEffectLevel[] displayOrder()"));
        assertTrue(motionEffect.contains("static int indexOf(MotionEffectLevel selected)"));
        assertFalse(motionEffect.contains("fromPosition("));
        assertTrue(layoutController.contains("SettingsValueFormatter.hangulHeight("));
        assertTrue(gestureTouchController.contains("SettingsValueFormatter.gestureThreshold("));
        assertFalse(activity.contains("settings_hangul_height_format"));
        assertFalse(activity.contains("settings_gesture_threshold_format"));
        assertTrue(formatter.contains("R.string.settings_hangul_height_format"));
        assertTrue(formatter.contains("R.string.settings_gesture_threshold_format"));
        assertTrue(formatter.contains("numberRowSuffix("));
    }

    @Test
    public void imeActionLabelsUseStringResources() throws Exception {
        String resolver = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ImeActionLabelResolver.java");
        String action = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ResolvedImeAction.java");
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(resolver.contains(quotedText(0xC804, 0xC1A1)));
        assertFalse(resolver.contains(quotedText(0xAC80, 0xC0C9)));
        assertFalse(resolver.contains(quotedText(0xC644, 0xB8CC)));
        assertFalse(resolver.contains(quotedText(0xB2E4, 0xC74C)));
        assertFalse(resolver.contains(quotedText(0xC774, 0xB3D9)));
        assertFalse(resolver.contains(quotedText(0xC904, 0xBC14, 0xAFC8)));
        assertTrue(resolver.contains("R.string.ime_action_send"));
        assertTrue(resolver.contains("R.string.ime_action_search"));
        assertTrue(resolver.contains("R.string.ime_action_done"));
        assertTrue(resolver.contains("R.string.ime_action_next"));
        assertTrue(resolver.contains("R.string.ime_action_go"));
        assertFalse(resolver.contains("R.string.ime_action_newline"));
        assertTrue(action.contains("final int labelResId"));
        assertTrue(action.contains("context.getString(labelResId)"));
        assertTrue(service.contains("enterActionLabel()"));
        assertTrue(service.contains("commitExplicitNewline("));
        assertFalse(service.contains("enterAction.label;"));
        assertTrue(strings.contains("name=\"ime_action_send\""));
        assertTrue(strings.contains("name=\"ime_action_search\""));
        assertTrue(strings.contains("name=\"ime_action_done\""));
        assertTrue(strings.contains("name=\"ime_action_next\""));
        assertTrue(strings.contains("name=\"ime_action_go\""));
        assertTrue(strings.contains("name=\"ime_action_newline\""));
    }

    @Test
    public void keyboardCommandLabelsUseStringResources() throws Exception {
        String commands = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardCommands.java");
        String labels = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardCommandLabels.java");
        String accessibility = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardKeyAccessibilityLabel.java");
        String provider = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardVirtualKeyAccessibilityProvider.java");
        String view = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/HangulKeyboardView.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(commands.contains("labelFor("));
        assertFalse(commands.contains(quotedText(0xC0AD, 0xC81C)));
        assertFalse(commands.contains(quotedText(0xC2A4, 0xD398, 0xC774, 0xC2A4)));
        assertFalse(commands.contains(quotedText(0xC804, 0xC1A1)));
        assertFalse(commands.contains(quotedText(0xD55C, 0xC601)));
        assertFalse(commands.contains(quotedText(0xBE60, 0xB978, 0x20, 0xC124, 0xC815)));
        assertTrue(labels.contains("labelResIdFor("));
        assertTrue(labels.contains("R.string.command_delete"));
        assertTrue(labels.contains("R.string.command_space"));
        assertTrue(labels.contains("R.string.command_enter"));
        assertTrue(labels.contains("R.string.command_quick_settings"));
        assertTrue(labels.contains("R.string.command_remote_f12"));
        assertTrue(accessibility.contains("KeyboardCommandLabels.labelFor(context, value)"));
        assertTrue(accessibility.contains("R.string.keyboard_accessibility_action_tap"));
        assertTrue(accessibility.contains("R.string.keyboard_accessibility_action_up"));
        assertTrue(accessibility.contains("R.string.keyboard_accessibility_action_long_press"));
        assertTrue(provider.contains("KeyboardKeyAccessibilityLabel.describe(view.getContext(),"));
        assertTrue(view.contains("KeyboardCommandLabels.labelFor(getContext(), value)"));
        assertTrue(strings.contains("name=\"command_delete\""));
        assertTrue(strings.contains("name=\"command_space\""));
        assertTrue(strings.contains("name=\"command_enter\""));
        assertTrue(strings.contains("name=\"command_quick_settings\""));
        assertTrue(strings.contains("name=\"command_remote_f12\""));
        assertTrue(strings.contains("name=\"keyboard_accessibility_action_tap\""));
        assertTrue(strings.contains("name=\"keyboard_accessibility_action_up\""));
        assertTrue(strings.contains("name=\"keyboard_accessibility_action_long_press\""));
    }

    @Test
    public void expandableSectionHeadersUseChevronAndAccessibleState() throws Exception {
        String settingsSectionCard = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/SettingsSectionCard.java");
        String themeEditor = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ThemeEditorActivity.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertTrue(settingsSectionCard.contains("R.drawable.ic_settings_chevron"));
        assertTrue(settingsSectionCard.contains("R.string.settings_subsection_expanded_format"));
        assertTrue(settingsSectionCard.contains("R.string.settings_subsection_collapsed_format"));
        assertTrue(settingsSectionCard.contains("headerRow.setContentDescription("));
        assertTrue(settingsSectionCard.contains("params.topMargin = expanded"));
        assertTrue(settingsSectionCard.contains("SettingsRowBuilder.dp(context,"));
        assertTrue(settingsSectionCard.contains("SettingsRowBuilder.vertical(context)"));
        assertTrue(settingsSectionCard.contains("SettingsRowBuilder.matchWrap()"));
        assertTrue(settingsSectionCard.contains("SettingsRowBuilder.weightedWrap("));
        assertFalse(settingsSectionCard.contains("setOrientation(LinearLayout.VERTICAL)"));
        assertFalse(settingsSectionCard.contains("new LinearLayout.LayoutParams("));
        assertFalse(settingsSectionCard.contains("private static int dp("));
        assertTrue(themeEditor.contains("SettingsSectionCard.create(this, text, expandedByDefault)"));
        assertFalse(themeEditor.contains("private void setExpandableTitle("));
        assertFalse(themeEditor.contains("content.setVisibility(expandedByDefault"));
        assertFalse(settingsSectionCard.contains("\"??"));
        assertFalse(settingsSectionCard.contains("\"??"));
        assertFalse(themeEditor.contains("\"- \""));
        assertFalse(themeEditor.contains("\"+ \""));
        assertFalse(strings.contains("name=\"expandable_section_title_expanded\""));
        assertFalse(strings.contains("name=\"expandable_section_title_collapsed\""));
    }

    @Test
    public void remoteCompatibilityEmptyHistoryUsesStringResource() throws Exception {
        String log = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteCompatibilityLog.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(log.contains("\"?�직 ?�격 ?�스???�력???�습?�다."));
        assertTrue(log.contains("R.string.remote_test_history_empty"));
        assertTrue(strings.contains("name=\"remote_test_history_empty\""));
    }

    @Test
    public void quickThemeSelectionStaysOutOfImeServiceBody() throws Exception {
        String service = javaSource("S3KeyboardService");
        String controller = javaSource("QuickThemePanelController");

        assertTrue(service.contains("QuickThemePanelController"));
        assertFalse(service.contains("ThemeOption.buildOptions("));
        assertFalse(service.contains("option.applyTo("));
        assertFalse(service.contains("QuickThemePanelController.Host"));
        assertTrue(service.contains("() -> settings"));
        assertTrue(service.contains("this::enterActionLabel"));
        assertTrue(service.contains("() -> editorPolicy.forceNumberRow"));
        assertTrue(service.contains("this::applyRuntimeSettings"));
        assertTrue(service.contains("this::dismissQuickSettings"));
        assertFalse(service.contains("public KeyboardMode currentKeyboardMode()"));
        assertFalse(service.contains("public String enterKeyLabel()"));
        assertFalse(service.contains("public boolean forceNumberRow()"));
        assertFalse(service.contains("private KeyboardMode currentKeyboardMode()"));
        assertFalse(service.contains("private String enterKeyLabel()"));
        assertFalse(service.contains("private boolean forceNumberRow()"));
        assertFalse(service.contains("public void applyRuntimeSettings("));
        assertTrue(controller.contains("ThemeOption.buildOptions("));
        assertTrue(controller.contains("ExternalThemeStore.load(context),\n                true"));
        assertTrue(controller.contains("option.applyTo("));
        assertFalse(controller.contains("interface Host"));
        assertTrue(controller.contains("private final Supplier<KeyboardSettings> currentSettings"));
        assertTrue(controller.contains("private final Supplier<String> enterKeyLabel"));
        assertTrue(controller.contains("private final BooleanSupplier forceNumberRow"));
        assertTrue(controller.contains("private final Consumer<KeyboardSettings> runtimeSettingsApplier"));
        assertTrue(controller.contains("private final Runnable dismissQuickSettings"));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.nullStringSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.booleanSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(controller.contains("RuntimeDefaults.runnable("));
        assertTrue(controller.contains("RuntimeDefaults.withRuntimeImeState("));
        assertTrue(javaSource("RuntimeDefaults").contains("static KeyboardSettings withRuntimeImeState("));
        assertTrue(controller.contains("ThemeOption.at(options, position)"));
        assertEquals("Quick theme fallbacks should come from RuntimeDefaults.",
                0,
                countOccurrences(controller, "() -> { }"));
        assertEquals("Quick theme settings fallback should come from RuntimeDefaults.",
                0,
                countOccurrences(controller, "settings -> { }"));
        assertFalse(controller.contains("return mode == null ? fallback.keyboardMode : mode"));
        assertFalse(controller.contains("return label == null ? fallback.enterKeyLabel : label"));
        assertFalse(controller.contains(".withRuntimeNumberRowForced(forceNumberRow.getAsBoolean())"));
        assertContainsNone(
                service + "\n" + controller,
                "private KeyboardMode currentKeyboardMode(",
                "private String enterKeyLabel(",
                "private boolean forceNumberRow(");
        assertTrue(controller.contains("UserInputListeners.itemSelectedAfterInitialSelection("));
        assertTrue(controller.contains("QuickPanelUi.sectionLabel("));
        assertTrue(controller.contains("QuickPanelUi.addWithTop("));
        assertTrue(controller.contains("SettingsRowBuilder.spinner(context, options)"));
        assertFalse(controller.contains("SettingsViewStyler.spinner(spinner, context)"));
        assertFalse(controller.contains("options[position]"));
        assertFalse(controller.contains("new AdapterView.OnItemSelectedListener"));
        assertFalse(controller.contains("new Spinner("));
        assertFalse(controller.contains("new GradientDrawable("));
    }

    @Test
    public void themeApplyAndPreviewUseSharedSettingsFallback() throws Exception {
        String themeOption = javaSource("ThemeOption");
        String userThemeStore = javaSource("UserThemeStore");
        String preview = javaSource("ThemePreviewSettings");
        String json = javaSource("KeyboardThemeJson");

        assertContainsAll(
                themeOption,
                "RuntimeDefaults.keyboardSettings(",
                "RuntimeDefaults.stringOrDefault(sourcePath, \"\")",
                "RuntimeDefaults.stringOrDefault(userThemeId, \"\")",
                "static ThemeOption at(",
                "base.withAppearanceFrom(appearance)",
                "base.withFullAppearanceFrom(KeyboardSettings.defaults())");
        assertContainsAll(
                userThemeStore,
                "RuntimeDefaults.stringOrDefault(id, \"\")",
                "RuntimeDefaults.stringOrDefault(json, \"\")",
                "RuntimeDefaults.stringOrDefault(sourcePath, \"\")");
        assertContainsAll(
                preview,
                "RuntimeDefaults.keyboardSettings(",
                "base.withFullAppearanceFrom(appearance)");
        assertContainsAll(
                json,
                "RuntimeDefaults.keyboardSettings(settings)",
                "RuntimeDefaults.keyboardSettings(baseSettings)",
                "RuntimeDefaults.keyboardSettings(settings);",
                "fallback == null ? KeyboardVisualEffects.DEFAULT : fallback",
                "object.optBoolean(\"blurEnabled\", safeFallback.blurEnabled)");
        assertContainsNone(
                themeOption + "\n" + userThemeStore + "\n" + preview + "\n" + json,
                "settings == null ? KeyboardSettings.defaults() : settings",
                "sourcePath == null ? \"\" : sourcePath",
                "userThemeId == null ? \"\" : userThemeId",
                "id == null ? \"\" : id",
                "json == null ? \"\" : json");
    }

    @Test
    public void themeResetRequiresConfirmationAndCurrentThemeIsVisible() throws Exception {
        String confirmation = javaSource("ThemeResetConfirmation");
        String hub = javaSource("ThemeHubSettingsController");
        String selector = javaSource("ThemeSelectorActivity");
        String quickTheme = javaSource("QuickThemePanelController");

        assertTrue(confirmation.contains("new AlertDialog.Builder(activity)"));
        assertTrue(confirmation.contains("R.string.theme_reset_confirm_message"));
        assertTrue(confirmation.contains("R.string.action_restore"));
        assertTrue(hub.contains("ThemeResetConfirmation.show("));
        assertTrue(hub.contains("R.string.settings_current_theme_format"));
        assertTrue(selector.contains("ThemeResetConfirmation.show("));
        assertTrue(selector.contains("ThemeOption.buildOptions(this,"));
        assertTrue(quickTheme.contains("ThemeOption.buildOptions(\n                context,"));
    }

    @Test
    public void ordinaryImeDoesNotReserveClipboardToolbarAndKeepsExplicitTextToolsAccess() throws Exception {
        String service = javaSource("S3KeyboardService");
        String clipboard = javaSource("ClipboardPanelController");
        String quickSettings = javaSource("QuickSettingsPanelController");

        assertFalse(service.contains("clipboardPanelController.createToolbar()"));
        assertFalse(clipboard.contains("createToolbar()"));
        assertFalse(clipboard.contains("toolbarLayout"));
        assertTrue(service.contains("this::openTextToolsFromQuickSettings"));
        assertTrue(service.contains("dismissQuickSettings();\n        toggleClipboardPanel();"));
        assertTrue(quickSettings.contains("private final Runnable textToolsOpener"));
        assertEquals(1, countOccurrences(quickSettings, "R.string.clipboard_toolbar_button"));
        assertEquals(1, countOccurrences(quickSettings, "v -> textToolsOpener.run()"));
        assertTrue(clipboard.contains("if (!textToolsAllowed() && clipboardView != null)"));
    }

    @Test
    public void quickSettingsPanelStaysOutOfImeServiceBody() throws Exception {
        String service = javaSource("S3KeyboardService");
        String normalizedService = service.replace("\r\n", "\n");
        String controller = javaSource("QuickSettingsPanelController");
        String remote = javaSource("RemoteCompatibilityPanelController");
        String quickPanelUi = javaSource("QuickPanelUi");

        assertTrue(service.contains("QuickSettingsPanelController"));
        assertTrue(service.contains("initializePanelControllers()"));
        assertTrue(service.contains("quickSettingsPanelController.createPanel()"));
        assertTrue(service.contains("ScrollView panelScroll = new ScrollView(this)"));
        assertTrue(service.contains("panel.measure("));
        assertTrue(service.contains("panel.getMeasuredHeight() > maximumHeight"));
        assertTrue(service.contains("panelScroll.addView(panel, SettingsRowBuilder.frameMatchWrap())"));
        assertTrue(service.contains("int popupHeight = Math.max(1, Math.min("));
        assertFalse(service.contains("QuickSettingsPanelController.Host"));
        assertTrue(service.contains("this::remoteModeToggleLabel"));
        assertTrue(service.contains("this::toggleRemoteMode"));
        assertTrue(service.contains("this::singleTapCommitModeToggleLabel"));
        assertTrue(service.contains("this::singleTapCommitModeEnabled"));
        assertTrue(service.contains("this::toggleSingleTapCommitMode"));
        assertTrue(service.contains("this::numberRowToggleLabel"));
        assertTrue(service.contains("this::activeNumberRowVisible"));
        assertTrue(service.contains("this::toggleActiveNumberRow"));
        assertTrue(service.contains("KeyboardPreferences.saveRemoteOptions("));
        assertTrue(service.contains("KeyboardPreferences.saveNumberRowVisibility("));
        assertFalse(service.contains("KeyboardPreferences.saveSettings(this, settings)"));
        assertTrue(service.contains("this::setHandedness"));
        assertTrue(service.contains("this::importThemeFromClipboard"));
        assertTrue(service.contains("this::copyInputIssueReport"));
        assertTrue(service.contains("this::dismissQuickSettings"));
        assertFalse(service.contains("public void importThemeFromClipboard()"));
        assertFalse(service.contains("public void copyInputIssueReport()"));
        assertFalse(service.contains("public void setHandedness("));
        assertFalse(service.contains("public void dismissQuickSettings()"));
        assertFalse(service.contains("public boolean activeNumberRowVisible()"));
        assertFalse(service.contains("public String numberRowToggleLabel()"));
        assertFalse(service.contains("public String remoteModeToggleLabel()"));
        assertFalse(service.contains("public void toggleRemoteMode()"));
        assertFalse(service.contains("public String singleTapCommitModeToggleLabel()"));
        assertFalse(service.contains("public void toggleSingleTapCommitMode()"));
        assertFalse(service.contains("public void toggleActiveNumberRow()"));
        assertTrue(service.contains("SettingsRowBuilder.dp(this,"));
        assertTrue(service.contains("SettingsRowBuilder.matchWrap()"));
        assertTrue(service.contains("SettingsRowBuilder.frameMatchWrap()"));
        assertTrue(service.contains("LinearLayout mainContainer = SettingsRowBuilder.vertical(this);"));
        assertFalse(service.contains("FloatingModeController"));
        assertFalse(service.contains("applyFloatingMode"));
        assertFalse(normalizedService.contains("new LinearLayout.LayoutParams(\n"
                + "                LinearLayout.LayoutParams.MATCH_PARENT,\n"
                + "                LinearLayout.LayoutParams.WRAP_CONTENT)"));
        assertFalse(normalizedService.contains("new FrameLayout.LayoutParams(\n"
                + "                FrameLayout.LayoutParams.MATCH_PARENT,\n"
                + "                FrameLayout.LayoutParams.WRAP_CONTENT)"));
        assertFalse(service.contains("new LinearLayout(this)"));
        assertFalse(service.contains("setOrientation(LinearLayout.VERTICAL)"));
        assertFalse(service.contains("private int dp("));
        assertFalse(service.contains("handednessButton("));
        assertFalse(service.contains("styleQuickButton("));
        assertTrue(controller.contains("QuickPanelUi.titleLabel("));
        assertTrue(controller.contains("QuickPanelUi.addWithTop("));
        assertTrue(controller.contains("QuickPanelUi.quickButton("));
        assertTrue(controller.contains("SettingsRowBuilder.vertical(context)"));
        assertTrue(controller.contains("LinearLayout handRow = QuickPanelUi.row(context);"));
        assertTrue(controller.contains("QuickPanelUi.weightedParams("));
        assertTrue(controller.contains(
                "KeyboardSettings currentSettings = RuntimeDefaults.keyboardSettingsFrom(settings)"));
        assertFalse(controller.contains("interface Host"));
        assertFalse(controller.contains("private KeyboardSettings settings()"));
        assertTrue(controller.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(controller.contains("private final Supplier<String> remoteModeToggleLabel"));
        assertTrue(controller.contains("private final Runnable remoteModeToggler"));
        assertTrue(controller.contains("private final Supplier<String> singleTapCommitModeToggleLabel"));
        assertTrue(controller.contains("private final BooleanSupplier singleTapCommitModeEnabled"));
        assertTrue(controller.contains("private final Runnable singleTapCommitModeToggler"));
        assertTrue(controller.contains("private final Supplier<String> numberRowToggleLabel"));
        assertTrue(controller.contains("private final BooleanSupplier activeNumberRowVisible"));
        assertTrue(controller.contains("private final Runnable activeNumberRowToggler"));
        assertTrue(controller.contains("private final Consumer<HandednessMode> handednessApplier"));
        assertTrue(controller.contains("private final Runnable themeClipboardImporter"));
        assertTrue(controller.contains("private final Runnable inputIssueReportCopier"));
        assertTrue(controller.contains("private final Runnable dismissQuickSettings"));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.emptyStringSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.booleanSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.runnable("));
        assertTrue(controller.contains("RuntimeDefaults.handednessConsumer("));
        assertEquals("Quick settings fallbacks should come from RuntimeDefaults.",
                0,
                countOccurrences(controller, "() -> { }"));
        assertEquals("Quick settings handedness fallback should come from RuntimeDefaults.",
                0,
                countOccurrences(controller, "mode -> { }"));
        assertFalse(controller.contains("styleQuickButton("));
        assertFalse(controller.contains("private View quickButton("));
        assertFalse(controller.contains("button.setAllCaps(false)"));
        assertFalse(controller.contains("new TextView("));
        assertFalse(controller.contains("new LinearLayout(context)"));
        assertFalse(controller.contains("setOrientation(LinearLayout."));
        assertFalse(controller.contains("private LinearLayout.LayoutParams topWrap("));
        assertFalse(controller.contains("private LinearLayout.LayoutParams weightedParams("));
        assertFalse(controller.contains("QuickPanelUi.add(panel,"));
        assertFalse(service.contains("quickButton("));
        assertTrue(controller.contains("remoteCompatibilityPanelController::addTo"));
        assertTrue(controller.contains("private void addDisclosureSection("));
        assertTrue(controller.contains("R.string.quick_settings_remote_test_open"));
        assertTrue(controller.contains("R.string.quick_settings_tools_open"));
        assertTrue(controller.contains("content.setVisibility(View.GONE)"));
        assertTrue(controller.contains("quickThemePanelController.addTo("));
        assertTrue(controller.contains("handednessButton("));
        assertTrue(remote.contains("QuickPanelUi.addCompactButton("));
        assertTrue(remote.contains("QuickPanelUi.addWithTop("));
        assertTrue(remote.contains("QuickPanelUi.sectionLabel("));
        assertTrue(remote.contains("QuickPanelUi.row("));
        assertTrue(remote.contains("RuntimeDefaults.emptyStringSupplier("));
        assertTrue(remote.contains("RuntimeDefaults.stringOrDefault(testCase.label, \"\")"));
        assertTrue(remote.contains("AppPackageCatalog.normalizePackageName(currentPackageName.get())"));
        assertTrue(remote.contains("SKIPPED_KEY_SENDER"));
        assertEquals("Remote package fallback should come from RuntimeDefaults.",
                0,
                countOccurrences(remote, "() -> \"\""));
        assertFalse(remote.contains("testCase.label == null ? \"\" : testCase.label"));
        assertFalse(remote.contains("packageName == null ? \"\" : packageName"));
        assertEquals("Remote skipped sender fallback should use one named callback.",
                1,
                countOccurrences(remote, "(keyCode, metaState) -> SEND_SKIPPED"));
        assertTrue(remote.contains("private void addCaseRow("));
        assertTrue(remote.contains("private void addFunctionCaseRow("));
        assertTrue(remote.contains("private TextView createHistoryView()"));
        assertTrue(remote.contains("SettingsRowBuilder.secondaryLabel(context, \"\")"));
        assertFalse(remote.contains("addCaseRange("));
        assertFalse(remote.contains("new TextView("));
        assertFalse(remote.contains("private void addActionButton("));
        assertFalse(remote.contains("private View button("));
        assertFalse(remote.contains("private LinearLayout.LayoutParams topWrap("));
        assertFalse(remote.contains("private LinearLayout.LayoutParams weightedParams("));
        assertFalse(remote.contains("QuickPanelUi.compactButton("));
        assertFalse(remote.contains("QuickPanelUi.weightedParams("));
        assertTrue(quickPanelUi.contains("static TextView titleLabel("));
        assertTrue(quickPanelUi.contains("static Button addCompactButton("));
        assertTrue(quickPanelUi.contains("static <T extends View> T addWithTop("));
        assertTrue(quickPanelUi.contains("return SettingsRowBuilder.horizontal(context);"));
        assertFalse(quickPanelUi.contains("static <T extends View> T add("));
        assertFalse(quickPanelUi.contains("static LinearLayout.LayoutParams matchWrapWithTop("));
        assertFalse(quickPanelUi.contains("new LinearLayout(context)"));
        assertFalse(quickPanelUi.contains("setOrientation(LinearLayout."));
        assertTrue(quickPanelUi.contains("SettingsRowBuilder.button(context, text, selected, listener)"));
        assertTrue(quickPanelUi.contains("SettingsRowBuilder.button(context, text, false, listener)"));
        assertTrue(quickPanelUi.contains("button.setTextSize(11)"));
        assertTrue(quickPanelUi.contains("button.setMaxLines(2)"));
        assertTrue(quickPanelUi.contains("button.setMinHeight(dp(context, 48))"));
        assertTrue(quickPanelUi.contains("button.setMaxHeight(dp(context, 48))"));
        assertFalse(quickPanelUi.contains("button.setOnClickListener(listener)"));
        assertFalse(quickPanelUi.contains("new Button("));
    }

    @Test
    public void disabledFloatingModeScaffoldIsRemoved() throws Exception {
        Path controller = findWorkspaceRoot().resolve(
                "app/src/main/java/com/superl3/s3keyboard/FloatingModeController.java");
        String service = javaSource("S3KeyboardService");
        String clipboard = javaSource("ClipboardPanelController");
        String preferences = javaSource("KeyboardPreferences");

        assertFalse(Files.exists(controller));
        assertFalse(service.contains("FloatingModeController"));
        assertFalse(service.contains("applyFloatingMode"));
        assertFalse(clipboard.contains("dragHandle"));
        assertFalse(clipboard.contains("remoteIndicator"));
        assertFalse(preferences.contains("FLOATING_MODE_ENABLED"));
    }

    @Test
    public void runtimeDefaultsOwnsRuntimeFallbackNaming() throws Exception {
        Path root = findWorkspaceRoot();
        List<Path> files = new ArrayList<>();
        collectTextFiles(root.resolve("app/src/main/java/com/superl3/s3keyboard"), files);
        String oldName = "Callback" + "Defaults";

        for (Path file : files) {
            String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            assertFalse("Runtime fallback helpers should use RuntimeDefaults: " + file,
                    source.contains(oldName));
        }
        assertTrue(javaSource("RuntimeDefaults").contains("final class RuntimeDefaults"));
    }

    @Test
    public void runtimeFallbacksUseTypedDefaultHelpersInsteadOfInlineTernaries() throws Exception {
        Path root = findWorkspaceRoot();
        List<Path> files = new ArrayList<>();
        collectTextFiles(root.resolve("app/src/main/java/com/superl3/s3keyboard"), files);
        String defaults = javaSource("RuntimeDefaults");
        String typingJournal = javaSource("TypingEventJournal");

        assertContainsAll(
                defaults,
                "static Runnable runnable(",
                "static BooleanSupplier booleanSupplier(",
                "static Supplier<String> emptyStringSupplier(",
                "static Supplier<String> nullStringSupplier(",
                "static Supplier<KeyboardSettings> keyboardSettingsSupplier(",
                "static Supplier<EditorInputPolicy> editorInputPolicySupplier(",
                "static Supplier<AppInputProfile> appInputProfileSupplier(",
                "static Supplier<LocalDataControlsController> localDataControlsSupplier(",
                "static BooleanSupplier trueBooleanSupplier(",
                "static <T> Consumer<T> consumer(",
                "static Consumer<String> stringConsumer(",
                "static Consumer<Boolean> booleanConsumer(",
                "static IntConsumer intConsumer(",
                "static Consumer<HandednessMode> handednessConsumer(",
                "static Consumer<KeyboardSettings> keyboardSettingsConsumer(",
                "static BiConsumer<KeyboardSettings, KeyboardErgonomicsOptions> "
                        + "keyboardSettingsAndErgonomicsConsumer(");
        assertContainsAll(
                defaults,
                "static LocalDataControlsController localDataControls(",
                "static KeyboardSettings keyboardSettingsFrom(",
                "static KeyboardErgonomicsOptions keyboardErgonomicsFrom(",
                "static AppInputProfileOverrides appInputProfileOverrides(",
                "static EnglishQwertyCorrectionEngine englishQwertyCorrectionEngine(",
                "static EditorInputPolicy editorInputPolicyFrom(",
                "static RemoteImeShortcut remoteImeShortcut(",
                "static String stringOrDefault(",
                "static String stringOrEmpty(CharSequence value)",
                "static String trimmedStringOrEmpty(CharSequence value)",
                "static String trimmedStringOrEmptyFrom(");
        assertTrue(typingJournal.contains("RuntimeDefaults.stringOrDefault("));
        assertFalse(typingJournal.contains("private static String safe(String value)"));
        assertFalse(typingJournal.contains("value == null ? \"\" : value"));
        for (Path file : files) {
            if ("RuntimeDefaults.java".equals(file.getFileName().toString())) {
                continue;
            }
            String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            assertFalse("Runtime fallback ternary should use typed RuntimeDefaults helpers: " + file,
                    source.contains("== null ? RuntimeDefaults."));
            assertFalse("Runtime fallback ternary should not directly branch to RuntimeDefaults: " + file,
                    source.contains("? RuntimeDefaults."));
        }
    }

    @Test
    public void clipboardThemeImportStaysOutOfImeServiceBody() throws Exception {
        String service = javaSource("S3KeyboardService");
        String controller = javaSource("ThemeClipboardImportController");

        assertTrue(service.contains("ThemeClipboardImportController"));
        assertFalse(service.contains("KeyboardThemeJson.importTheme("));
        assertFalse(service.contains("ClipboardManager clipboard"));
        assertFalse(service.contains("getPrimaryClip()"));
        assertTrue(controller.contains("KeyboardThemeJson.importTheme("));
        assertTrue(controller.contains("KeyboardPreferences.saveSelectedThemeId("));
        assertTrue(controller.contains("getPrimaryClip()"));
        assertTrue(controller.contains("java.util.function.Supplier"));
        assertTrue(controller.contains("java.util.function.BooleanSupplier"));
        assertTrue(controller.contains("java.util.function.Consumer"));
        assertTrue(controller.contains("java.util.function.IntConsumer"));
        assertTrue(controller.contains("private final Supplier<KeyboardSettings> storedSettings"));
        assertTrue(controller.contains("private final Supplier<KeyboardSettings> currentSettings"));
        assertTrue(controller.contains("private final Supplier<String> enterKeyLabel"));
        assertTrue(controller.contains("private final BooleanSupplier forceNumberRow"));
        assertTrue(controller.contains("private final Consumer<KeyboardSettings> runtimeSettingsApplier"));
        assertTrue(controller.contains("private final Runnable dismissQuickSettings"));
        assertTrue(controller.contains("private final Supplier<String> clipboardTextReader"));
        assertTrue(controller.contains("private final Consumer<KeyboardSettings> settingsStore"));
        assertTrue(controller.contains("private final IntConsumer notifier"));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.nullStringSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.booleanSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(controller.contains("RuntimeDefaults.runnable("));
        assertTrue(controller.contains("RuntimeDefaults.emptyStringSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.intConsumer("));
        assertTrue(controller.contains("RuntimeDefaults.withRuntimeImeState("));
        assertTrue(controller.contains("() -> KeyboardPreferences.load(context)"));
        assertTrue(controller.contains("RuntimeDefaults.trimmedStringOrEmpty("));
        assertTrue(controller.contains("RuntimeDefaults.trimmedStringOrEmptyFrom("));
        assertFalse(service.contains("ThemeClipboardImportController.Host"));
        assertFalse(controller.contains("interface Host"));
        assertFalse(controller.contains("interface ClipboardTextReader"));
        assertFalse(controller.contains("interface SettingsStore"));
        assertFalse(controller.contains("interface Notifier"));
        assertFalse(controller.contains("RuntimeDefaults.trimmedStringOrEmpty(clipboardTextReader.get())"));
        assertFalse(controller.contains(".withRuntimeNumberRowForced(forceNumberRow.getAsBoolean())"));
        assertFalse(controller.contains("text == null ? \"\" : text.trim()"));
        assertFalse(controller.contains("text == null ? \"\" : text.toString().trim()"));
    }

    @Test
    public void clipboardHistoryPanelStaysOutOfImeServiceBody() throws Exception {
        String service = javaSource("S3KeyboardService");
        String controller = javaSource("ClipboardPanelController");

        assertTrue(service.contains("ClipboardPanelController"));
        assertFalse(service.contains("ClipboardPanelController.Host"));
        assertTrue(service.contains("() -> settings"));
        assertTrue(service.contains("() -> editorPolicy"));
        assertTrue(service.contains("this::commitClipboardText"));
        assertFalse(service.contains("public KeyboardSettings settings()"));
        assertFalse(service.contains("public EditorInputPolicy editorPolicy()"));
        assertFalse(service.contains("private KeyboardSettings settings()"));
        assertFalse(service.contains("private EditorInputPolicy editorPolicy()"));
        assertFalse(service.contains("public void commitClipboardText("));
        assertFalse(service.contains("capturePrimaryClipboard("));
        assertFalse(service.contains("addPrimaryClipChangedListener("));
        assertFalse(service.contains("new ClipboardView("));
        assertFalse(service.contains("new ClipboardStore("));
        assertFalse(controller.contains("interface Host"));
        assertTrue(controller.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(controller.contains("private final Supplier<EditorInputPolicy> editorPolicy"));
        assertTrue(controller.contains("private final Consumer<String> clipboardTextCommitter"));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.editorInputPolicySupplier("));
        assertTrue(controller.contains("RuntimeDefaults.stringConsumer("));
        assertTrue(controller.contains("RuntimeDefaults.keyboardSettingsFrom("));
        assertTrue(controller.contains("RuntimeDefaults.editorInputPolicyFrom("));
        assertTrue(controller.contains("clipboardTextCommitter);"));
        assertFalse(controller.contains("RuntimeDefaults.editorInputPolicy(editorPolicy.get())"));
        assertFalse(controller.contains("settings == null ? null"));
        assertFalse(controller.contains("editorPolicy == null ? null"));
        assertFalse(controller.contains("return current == null ? KeyboardSettings.defaults() : current"));
        assertFalse(controller.contains("return policy == null ? EditorInputPolicy.DEFAULT : policy"));
        assertFalse(controller.contains("clipboardTextCommitter != null"));
        assertTrue(controller.contains("capturePrimaryClipboard("));
        assertTrue(controller.contains("addPrimaryClipChangedListener("));
        assertTrue(controller.contains("new ClipboardView("));
        assertTrue(controller.contains("new ClipboardStore("));
        assertFalse(service.contains("clipboardPanelController.createToolbar()"));
        assertFalse(controller.contains("createToolbar()"));
        assertFalse(controller.contains("createClipboardButton()"));
        assertTrue(controller.contains("TextToolsPolicy.allows("));
        assertTrue(controller.contains("R.string.text_tools_sensitive_field"));
        assertTrue(controller.contains("TextToolsStore"));
        assertFalse(controller.contains("createDragHandle()"));
        assertFalse(controller.contains("createRemoteIndicator()"));
        assertFalse(controller.contains("TypedValue.applyDimension("));
    }

    @Test
    public void remoteInputExecutionStaysOutOfImeServiceBody() throws Exception {
        String service = javaSource("S3KeyboardService");
        String controller = javaSource("RemoteInputController");

        assertTrue(service.contains("RemoteInputController"));
        assertFalse(service.contains("implements RemoteInputController.Host"));
        assertTrue(service.contains("() -> settings.remoteImeShortcut"));
        assertTrue(service.contains("(pendingMetaState, lockedMetaState) -> updateShiftStateView()"));
        assertFalse(service.contains("public RemoteImeShortcut remoteImeShortcut()"));
        assertFalse(service.contains("private RemoteImeShortcut remoteImeShortcut()"));
        assertFalse(service.contains("public void onRemoteMetaStateChanged("));
        assertFalse(service.contains("private void onRemoteMetaStateChanged("));
        assertFalse(service.contains("RemoteCommandResolver.resolve("));
        assertFalse(service.contains("RemoteCommandAction action"));
        assertFalse(service.contains("RemoteKeySession"));
        assertFalse(service.contains("sendRemoteImeToggle("));
        assertFalse(controller.contains("interface Host"));
        assertTrue(controller.contains("java.util.function.BiConsumer"));
        assertTrue(controller.contains("java.util.function.Supplier"));
        assertTrue(controller.contains("private final Supplier<RemoteImeShortcut> remoteImeShortcut"));
        assertTrue(controller.contains("private final BiConsumer<Integer, Integer> metaStateListener"));
        assertTrue(controller.contains("metaStateListener.accept("));
        assertTrue(controller.contains("RemoteCommandResolver.resolve("));
        assertTrue(controller.contains("RemoteKeySession"));
        assertTrue(controller.contains("sendImeToggle("));
        assertTrue(controller.contains("RuntimeDefaults.remoteImeShortcutSupplier("));
        assertTrue(controller.contains("RuntimeDefaults.remoteImeShortcut("));
        assertTrue(controller.contains("RuntimeDefaults.integerPairConsumer("));
        assertTrue(controller.contains("RuntimeDefaults.longSupplier("));
        assertTrue(controller.contains("java.util.function.LongSupplier"));
        assertTrue(controller.contains("private final LongSupplier clock"));
        assertTrue(controller.contains("clock.getAsLong()"));
        assertFalse(controller.contains("interface Clock"));
        assertFalse(controller.contains("eventTimeMs()"));
        assertContainsNone(
                controller,
                "remoteImeShortcut == null ? null : remoteImeShortcut.get()",
                "shortcut == null ? RemoteImeShortcut.ALT_SHIFT : shortcut",
                "clock == null ? SystemClock::uptimeMillis : clock",
                "if (metaStateListener != null)",
                "private RemoteImeShortcut remoteImeShortcut()");
    }

    @Test
    public void keyboardCommandTargetStaysOutOfImeServiceBody() throws Exception {
        String service = javaSource("S3KeyboardService");
        String target = javaSource("S3KeyboardCommandTarget");

        assertTrue(service.contains("new S3KeyboardCommandTarget(this)"));
        assertFalse(service.contains("new KeyboardCommandDispatcher.Target()"));
        assertTrue(target.contains("extends KeyboardCommandDispatcher.Target"));
        assertTrue(target.contains("resetDoubleSpacePeriodState()"));
        assertTrue(target.contains("handleRemoteCommand("));
        assertTrue(target.contains("inputDingulContextualVowel("));
    }

    @Test
    public void assistRailCommandsHaveVisibleOrConventionalFallbacks() throws Exception {
        String service = javaSource("S3KeyboardService");
        String clipboardPanel = javaSource("ClipboardPanelController");
        String clipboardAccess = javaSource("ClipboardPanelAccess");
        String voiceActivity = javaSource("VoiceInputActivity");
        String manifest = readWorkspaceFile("app/src/main/AndroidManifest.xml");
        String dispatcher = javaSource("ImeConnectionDispatcher");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");
        String development = readWorkspaceFile("docs/development.md");

        assertFalse(service.contains("TODO"));
        assertTrue(service.contains("R.string.voice_input_unavailable"));
        assertTrue(clipboardPanel.contains("R.string.text_tools_sensitive_field"));
        assertTrue(clipboardPanel.contains("TextToolsPolicy.allows("));
        assertTrue(service.contains("R.string.undo_unavailable"));
        assertFalse(clipboardPanel.contains("ClipboardPanelAccess.resolve("));
        assertTrue(clipboardPanel.contains("TextToolsPolicy.allows("));
        assertTrue(clipboardAccess.contains("enum Result"));
        int clipboardCommitMethod = service.indexOf("private void commitClipboardText(String text)");
        int finishComposition = service.indexOf("commitCurrent(inputConnection);", clipboardCommitMethod);
        int commitClipboard = service.indexOf(
                "InputConnectionTextOperator.commitText(inputConnection, text)",
                clipboardCommitMethod);
        assertTrue(clipboardCommitMethod >= 0);
        assertTrue(finishComposition > clipboardCommitMethod);
        assertTrue(commitClipboard > finishComposition);
        assertTrue(service.contains("VoiceInputActivity.intent("));
        assertTrue(service.contains("editorPolicy.password"));
        assertTrue(service.contains("PendingVoiceInput"));
        assertTrue(service.contains("pending.targets(currentEditorPackageName)"));
        assertTrue(service.contains("VOICE_INPUT_RESULT_TIMEOUT_MS"));
        assertTrue(voiceActivity.contains("RecognizerIntent.ACTION_RECOGNIZE_SPEECH"));
        assertTrue(voiceActivity.contains("VoiceInputResult.firstRecognizedText("));
        assertTrue(manifest.contains("android:name=\".VoiceInputActivity\""));
        assertTrue(manifest.contains("android:excludeFromRecents=\"true\""));
        assertFalse(manifest.contains("android.permission.RECORD_AUDIO"));
        assertTrue(service.contains("ImeConnectionDispatcher.performUndo(inputConnection)"));
        assertTrue(dispatcher.contains("performContextMenuAction(android.R.id.undo)"));
        assertTrue(dispatcher.contains("java.util.function.IntBinaryOperator"));
        assertTrue(dispatcher.contains("IntBinaryOperator softKeySender"));
        assertTrue(dispatcher.contains("sender.applyAsInt(keyCode, metaState)"));
        assertFalse(dispatcher.contains("interface KeySender"));
        assertTrue(strings.contains("name=\"voice_input_unavailable\""));
        assertFalse(development.contains("safe stubs"));
        assertTrue(development.contains("voice should launch the Android speech recognizer"));
        assertTrue(development.contains("conventional undo"));
    }

    @Test
    public void smokeImeAppsReportKeepsAppProfileExpectations() throws Exception {
        String script = readWorkspaceFile("scripts/smoke-ime-apps.ps1");
        String exportScript = readWorkspaceFile("scripts/export-remote-compatibility.ps1");

        assertTrue(script.contains("schemaVersion = 2"));
        assertTrue(script.contains("profileExpectation = New-ProfileExpectation $ProfileHint"));
        assertTrue(script.contains("expectedRemoteMode"));
        assertTrue(script.contains("expectedPreferAsciiLayout"));
        assertTrue(script.contains("expectedForceNumberRow"));
        assertTrue(script.contains("expectedAllowComposingText"));
        assertTrue(script.contains("manualRemoteDeliveryRequired"));
        assertTrue(script.contains("remoteCompatibilityEvidence = New-RemoteEvidence $ProfileHint $PackageName"));
        assertTrue(script.contains("requires_manual_windows_confirmation"));
        assertTrue(script.contains("localMatrixCommand"));
        assertTrue(script.contains("export-remote-compatibility.ps1 -TargetPackage $PackageName"));
        assertTrue(script.contains("demo_field_profile"));
        assertTrue(script.contains("field-password"));
        assertTrue(script.contains("field-number"));
        assertTrue(script.contains("field-url"));
        assertTrue(script.contains("field-email"));
        assertTrue(script.contains("field-web-edit"));
        assertTrue(script.contains("field-search"));
        assertTrue(script.contains("field-multiline"));
        assertTrue(script.contains("requiredCaseLabels"));
        assertTrue(script.contains("requiredAppFamilies"));
        assertTrue(exportScript.contains("groupSummaries"));
        assertTrue(exportScript.contains("group = $group"));
        assertTrue(readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteCompatibilityReport.java")
                .contains("\"groupSummaries\""));
        assertTrue(exportScript.contains("requiredLabels"));
        assertTrue(exportScript.contains("requiredAppFamilies"));
        assertTrue(script.contains("\"Alt+Tab\""));
        assertTrue(script.contains("\"F12\""));
        assertTrue(script.contains("\"Win+Space\""));
        assertTrue(script.contains("manualResultRequired = $isRemote"));
        for (RemoteCompatibilityMatrix.Case testCase : RemoteCompatibilityMatrix.all()) {
            assertTrue(
                    "smoke report remote evidence must list matrix case: " + testCase.label,
                    script.contains("\"" + testCase.label + "\""));
            assertTrue(
                    "remote compatibility export script must list matrix case: " + testCase.label,
                    exportScript.contains("Label = \"" + testCase.label + "\""));
        }
        assertTrue(exportScript.contains("ExpectedEventCount"));
        assertTrue(exportScript.contains("acceptedEventCount"));
        assertTrue(exportScript.contains("expectedEventCount"));
        assertTrue(exportScript.contains("localInputConnectionAccepted"));
        assertTrue(exportScript.contains("Get-RemoteAppFamily"));
        assertTrue(exportScript.contains("appFamily"));
        assertTrue(exportScript.contains("Group = \"BASIC\""));
        assertTrue(exportScript.contains("Group = \"FUNCTION\""));
        assertTrue(exportScript.contains("Group = \"IME\""));
        assertTrue(exportScript.contains("missingLabels"));
        assertTrue(exportScript.contains("unknownLabels"));
        assertTrue(exportScript.contains("failedLabels"));
        assertTrue(exportScript.contains("localIncompleteLabels"));
        assertTrue(script.contains("expectedPreferAsciiLayout = $true"));
        assertTrue(script.contains("expectedAllowComposingText = $false"));
        assertTrue(script.contains("expectedAllowTextConveniences = $false"));
        assertTrue(exportScript.contains("localIncompleteCount"));
        assertTrue(script.contains("tv.parsec.client"));
        assertTrue(script.contains("com.limelight"));
        assertTrue(script.contains("com.microsoft.rdc.androidx"));
        assertTrue(script.contains("com.microsoft.rdc.android"));
        assertTrue(script.contains("com.google.chromeremotedesktop"));
        assertTrue(script.contains("com.valvesoftware.steamlink"));
        assertTrue(script.contains("com.anydesk.anydeskandroid"));
        assertTrue(script.contains("com.teamviewer.teamviewer.market.mobile"));
        assertTrue(script.contains("com.teamviewer.quicksupport.market"));
        assertTrue(script.contains("com.chrome.beta"));
        assertTrue(script.contains("com.chrome.dev"));
        assertTrue(script.contains("com.chrome.canary"));
        assertTrue(script.contains("com.sec.android.app.sbrowser"));
        assertTrue(script.contains("com.microsoft.emmx"));
        assertTrue(script.contains("com.brave.browser"));
        assertTrue(script.contains("org.mozilla.firefox"));
        assertTrue(script.contains("org.mozilla.firefox_beta"));
        assertTrue(script.contains("com.opera.browser"));
        assertTrue(script.contains("com.kakao.talk"));
        assertTrue(script.contains("org.telegram.messenger"));
        assertTrue(script.contains("com.whatsapp"));
        assertTrue(script.contains("jp.naver.line.android"));
        assertTrue(script.contains("org.thoughtcrime.securesms"));
        assertTrue(script.contains("com.discord"));
        assertTrue(script.contains("com.facebook.orca"));
        for (String family : RemoteAppCatalog.reportFamilies()) {
            assertTrue("smoke report remote evidence must list remote family: " + family,
                    script.contains("\"" + family + "\""));
            assertTrue("remote compatibility export script must list remote family: " + family,
                    exportScript.contains("\"" + family + "\""));
        }
    }

    @Test
    public void remoteCompatibilityExportMatrixMatchesRuntimeMatrix() throws Exception {
        String exportScript = readWorkspaceFile("scripts/export-remote-compatibility.ps1");
        Map<String, ScriptRemoteCase> scriptCases = parseExportScriptMatrix(exportScript);

        assertEquals(RemoteCompatibilityMatrix.all().length, scriptCases.size());
        for (RemoteCompatibilityMatrix.Case testCase : RemoteCompatibilityMatrix.all()) {
            ScriptRemoteCase scriptCase = scriptCases.get(testCase.label);
            assertTrue("remote compatibility export script is missing: " + testCase.label,
                    scriptCase != null);
            assertEquals(testCase.group.name(), scriptCase.group);
            assertEquals(
                    "remote compatibility export event count drifted for " + testCase.label,
                    RemoteKeyEventSequence.eventCount(testCase.keyCode, testCase.metaState),
                    scriptCase.expectedEventCount);
        }
    }

    @Test
    public void smokeImeAppsCoversEveryRuntimeAppCatalogPackage() throws Exception {
        String script = readWorkspaceFile("scripts/smoke-ime-apps.ps1");

        for (String packageName : AppPackageCatalog.browserPackages()) {
            assertTrue("smoke script missing browser package: " + packageName,
                    script.contains(packageName));
        }
        for (String packageName : AppPackageCatalog.webViewPackages()) {
            assertTrue("smoke script missing WebView package: " + packageName,
                    script.contains(packageName));
        }
        for (String packageName : AppPackageCatalog.messagingPackages()) {
            assertTrue("smoke script missing messaging package: " + packageName,
                    script.contains(packageName));
        }
        for (String packageName : RemoteAppCatalog.defaultAutoPackageList().split("\\R")) {
            assertTrue("smoke script missing remote package: " + packageName,
                    script.contains(packageName));
        }
    }

    @Test
    public void settingsSelectionRowsUseSharedUserInputListeners() throws Exception {
        String listener = javaSource("UserInputListeners");
        assertTrue(listener.contains("new AdapterView.OnItemSelectedListener"));
        assertTrue(listener.contains("onNothingSelected(AdapterView<?> parent)"));
        assertTrue(listener.contains("static AdapterView.OnItemSelectedListener itemSelected("));
        assertTrue(listener.contains("static AdapterView.OnItemSelectedListener itemSelectedAfterInitialSelection("));
        String rowBuilder = javaSource("SettingsRowBuilder");
        assertTrue(rowBuilder.contains("UserInputListeners.itemSelected("));
        assertTrue(rowBuilder.contains("static Spinner spinnerAfterInitialSelection("));
        assertTrue(rowBuilder.contains("shouldHandleAfterInitialSelection("));

        String[] sharedSelectionSources = {
                "AccentPlacementActivity",
                "DisplayStyleSettingsController",
                "ErgonomicsSettingsController",
                "GestureTouchSettingsController",
                "InputAssistanceSettingsController",
                "LayoutSettingsController",
                "MotionEffectSettingsController",
                "RemoteWindowsSettingsController",
                "ThemeEditorActivity",
                "TypographySettingsController"
        };
        for (String className : sharedSelectionSources) {
            String source = javaSource(className);
            assertTrue(className + " should use shared spinner selection filtering",
                    source.contains("SettingsRowBuilder.spinner(")
                            || source.contains("SettingsRowBuilder.optionSpinner(")
                            || source.contains("SettingsRowBuilder.spinnerAfterInitialSelection("));
            assertFalse(className + " should not wire simple spinner listeners directly",
                    source.contains("UserInputListeners.itemSelected("));
            assertFalse(className + " should not duplicate raw spinner listener shells",
                    source.contains("new AdapterView.OnItemSelectedListener"));
        }
    }

    @Test
    public void simpleSettingsListenersUseFactoryHelpers() throws Exception {
        String rowBuilder = javaSource("SettingsRowBuilder");
        String viewStyler = javaSource("SettingsViewStyler");
        String userInputListeners = javaSource("UserInputListeners");

        assertContainsAll(
                rowBuilder,
                "static CheckBox checkBoxRow(",
                "static Spinner spinner(",
                "private static Spinner spinner(Context context)",
                "SettingsViewStyler.spinner(spinner, context)");
        assertEquals(1, countOccurrences(rowBuilder, "new Spinner(context)"));
        assertEquals(1, countOccurrences(rowBuilder, "SettingsViewStyler.spinner(spinner, context)"));
        assertContainsAll(
                rowBuilder,
                "static SeekBar seekBar(",
                "static EditText editText(",
                "static Button buttonRow(",
                "String label,",
                "static Button weightedButton(",
                "static Button button(Context context, int labelResId, View.OnClickListener listener)",
                "static Button button(Context context, String label, View.OnClickListener listener)",
                "boolean selected,",
                "static Button iconButtonRow(",
                "static Button button(Context context, String label, boolean selected)",
                "static TextView labelRow(",
                "static TextView secondaryLabelRow(",
                "static TextView sectionLabel(",
                "static TextView sectionLabelRow(",
                "static TextView bodyLabelRow(",
                "static TextView valueLabel(",
                "static TextView valueLabelRow(",
                "static <T extends View> T labeledControl(",
                "static LinearLayout vertical(Context context)",
                "static LinearLayout horizontal(Context context)",
                "static <T extends View> T addView(",
                "static <T extends View> T addViewWithTop(",
                "static LinearLayout.LayoutParams wrapContentWithLeft(",
                "static LinearLayout.LayoutParams fixedSizeWithLeft(",
                "static LinearLayout.LayoutParams fixedWidthWrapWithLeft(",
                "static LinearLayout.LayoutParams weightedWrap(",
                "SettingsViewStyler.editText(input, context)",
                "UserInputListeners.checked(",
                "UserInputListeners.itemSelected(",
                "UserInputListeners.seekBar(",
                "UserInputListeners.text(",
                "Consumer<Boolean> onUserCheckedChanged",
                "Consumer<String> onUserTextChanged",
                "IntConsumer onUserItemSelected",
                "IntConsumer onUserProgressChanged");
        assertContainsAll(
                viewStyler,
                "SettingsRowBuilder.dp(context,",
                "button.setSelected(selected)",
                "button.setMinHeight(SettingsRowBuilder.dp(context, 48))",
                "drawable.setTint(iconTint)");
        assertContainsNone(viewStyler, "private static int dp(", "getDisplayMetrics().density");
        assertContainsAll(
                userInputListeners,
                "final class UserInputListeners",
                "static CompoundButton.OnCheckedChangeListener checked(",
                "static AdapterView.OnItemSelectedListener itemSelected(",
                "static SeekBar.OnSeekBarChangeListener seekBar(",
                "static TextWatcher text(",
                "static AdapterView.OnItemSelectedListener itemSelectedAfterInitialSelection(",
                "Consumer<Boolean> onUserCheckedChanged",
                "IntConsumer onUserItemSelected",
                "IntConsumer onUserProgressChanged",
                "Consumer<String> onUserTextChanged",
                "RuntimeDefaults.trueBooleanSupplier(",
                "RuntimeDefaults.booleanConsumer(",
                "RuntimeDefaults.intConsumer(",
                "RuntimeDefaults.stringConsumer(",
                "RuntimeDefaults.stringOrEmpty(");
        assertEquals(4, countOccurrences(userInputListeners, "RuntimeDefaults.trueBooleanSupplier("));
        assertEquals(1, countOccurrences(userInputListeners, "RuntimeDefaults.booleanConsumer("));
        assertEquals(2, countOccurrences(userInputListeners, "RuntimeDefaults.intConsumer("));
        assertContainsNone(
                userInputListeners,
                "abstract void",
                "canHandleUserChange != null",
                "listener != null",
                "onUserCheckedChanged.accept(isChecked)",
                "onUserItemSelected.accept(position)",
                "onUserProgressChanged.accept(progress)",
                "onUserTextChanged != null",
                "private static String textOrEmpty(");
        assertTrue(rowBuilder.contains("RuntimeDefaults.stringOrDefault(initialValue, \"\")"));
        assertFalse(rowBuilder.contains("initialValue == null ? \"\" : initialValue"));

        String themeHub = javaSource("ThemeHubSettingsController");
        String settingsHubController = javaSource("SettingsHubController");
        String mainActivity = javaSource("MainActivity");
        String androidIme = javaSource("AndroidImeSettingsController");
        String displayStyle = javaSource("DisplayStyleSettingsController");
        String inputAssistanceController = javaSource("InputAssistanceSettingsController");
        String inputFeelController = javaSource("InputFeelSettingsController");
        String layoutController = javaSource("LayoutSettingsController");
        String remoteWindowsController = javaSource("RemoteWindowsSettingsController");
        String typographyController = javaSource("TypographySettingsController");
        String localDataSettings = javaSource("LocalDataSettingsController");
        String extractedSettingsControllers = String.join(
                "\n",
                displayStyle,
                inputAssistanceController,
                inputFeelController,
                layoutController,
                remoteWindowsController,
                themeHub,
                typographyController);
        assertTrue(extractedSettingsControllers.contains("RuntimeDefaults.keyboardSettings("));
        assertContainsNone(
                extractedSettingsControllers,
                "settings == null ? KeyboardSettings.defaults() : settings",
                "current == null ? KeyboardSettings.defaults() : current",
                "currentSettings == null ? KeyboardSettings.defaults() : currentSettings",
                "KeyboardSettings current = settings.get();",
                "KeyboardSettings currentSettings = settings.get();",
                "return RuntimeDefaults.keyboardSettings(current);",
                "return RuntimeDefaults.keyboardSettings(currentSettings);",
                "private KeyboardErgonomicsOptions ergonomicsOptions()");
        assertTrue(themeHub.contains("SettingsRowBuilder.iconButtonRow("));
        assertFalse(themeHub.contains("interface Host"));
        assertTrue(themeHub.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(themeHub.contains("private final Runnable currentThemeCustomMarker"));
        assertTrue(themeHub.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(themeHub.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(themeHub.contains("RuntimeDefaults.runnable("));
        assertTrue(themeHub.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(themeHub.contains("RuntimeDefaults.keyboardSettingsFrom(settings)"));
        assertFalse(themeHub.contains("settings == null ? null"));
        assertFalse(themeHub.contains("currentThemeCustomMarker != null"));
        assertFalse(themeHub.contains("settingsSaver != null"));
        assertFalse(themeHub.contains("private KeyboardSettings settings()"));
        assertFalse(settingsHubController.contains("interface Host"));
        assertTrue(settingsHubController.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(settingsHubController.contains("private final Runnable currentThemeCustomMarker"));
        assertTrue(settingsHubController.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(settingsHubController.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(settingsHubController.contains("RuntimeDefaults.runnable("));
        assertTrue(settingsHubController.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertFalse(settingsHubController.contains("implements ThemeHubSettingsController.Host"));
        assertFalse(settingsHubController.contains("public KeyboardSettings settings()"));
        assertFalse(settingsHubController.contains("public void markCurrentThemeCustom()"));
        assertFalse(mainActivity.contains("implements SettingsHubController.Host"));
        assertFalse(mainActivity.contains("implements DisplayStyleSettingsController.Host"));
        assertFalse(mainActivity.contains("implements InputAssistanceSettingsController.Host"));
        assertFalse(mainActivity.contains("implements InputFeelSettingsController.Host"));
        assertFalse(mainActivity.contains("implements LayoutSettingsController.Host"));
        assertFalse(mainActivity.contains("implements RemoteWindowsSettingsController.Host"));
        assertFalse(mainActivity.contains("implements TypographySettingsController.Host"));
        assertTrue(displayStyle.contains("private Spinner packSpinner("));
        assertTrue(displayStyle.contains("IntFunction<KeyboardSettings> change"));
        assertTrue(displayStyle.contains("ModifierIconCatalog.selectablePackIdAt("));
        assertTrue(displayStyle.contains("KeyDisplayOverridePackCatalog.selectablePackIdAt("));
        assertFalse(displayStyle.contains("ModifierIconCatalog.selectablePackIds(true)"));
        assertFalse(displayStyle.contains("KeyDisplayOverridePackCatalog.selectablePackIds(true)"));
        assertFalse(displayStyle.contains("ids[position]"));
        assertFalse(mainActivity.contains("public KeyboardSettings settings()"));
        assertFalse(mainActivity.contains("public KeyboardErgonomicsOptions ergonomicsOptions()"));
        assertFalse(mainActivity.contains("public void saveSettings("));
        assertFalse(mainActivity.contains("public void saveSettingsAndErgonomics("));
        assertFalse(mainActivity.contains("public void saveHangulLayoutProfile("));
        assertFalse(mainActivity.contains("public void saveEnglishLayoutProfile("));
        assertFalse(mainActivity.contains("public void saveErgonomicsOptions("));
        assertFalse(mainActivity.contains("public void syncControls()"));
        assertFalse(mainActivity.contains("public boolean isDebuggableBuild()"));
        assertFalse(mainActivity.contains("public void markCurrentThemeCustom()"));
        assertTrue(mainActivity.contains("private void loadCurrentPreferences()"));
        assertEquals(1, countOccurrences(mainActivity, "KeyboardPreferences.load(this)"));
        assertEquals(1, countOccurrences(mainActivity, "KeyboardPreferences.loadLayoutProfiles(this)"));
        assertEquals(1, countOccurrences(mainActivity, "KeyboardPreferences.loadErgonomicsOptions(this)"));
        assertEquals(0, countOccurrences(mainActivity, "FloatingMode"));
        assertTrue(mainActivity.contains("private KeyboardSettings settings()"));
        assertTrue(mainActivity.contains("private KeyboardErgonomicsOptions ergonomicsOptions()"));
        assertTrue(mainActivity.contains("private void saveSettings("));
        assertTrue(mainActivity.contains("private void syncControls()"));
        assertTrue(mainActivity.contains("this::settings"));
        assertTrue(mainActivity.contains("this::markCurrentThemeCustom"));
        assertTrue(mainActivity.contains("this::saveSettings"));
        assertTrue(androidIme.contains("SettingsRowBuilder.iconButtonRow("));
        String visualThemeControllers = String.join(
                "\n",
                displayStyle,
                themeHub,
                typographyController);
        assertContainsNone(
                visualThemeControllers,
                "private void markCurrentThemeCustom()",
                "private void saveSettings(KeyboardSettings settings)",
                "private KeyboardSettings settings()",
                "markCurrentThemeCustom();",
                "saveSettings(settings");
        assertTrue(displayStyle.contains("SettingsRowBuilder.buttonRow("));
        assertFalse(displayStyle.contains("interface Host"));
        assertTrue(displayStyle.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(displayStyle.contains("private final Runnable currentThemeCustomMarker"));
        assertTrue(displayStyle.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(displayStyle.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(displayStyle.contains("RuntimeDefaults.runnable("));
        assertTrue(displayStyle.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertFalse(displayStyle.contains("settings == null ? null"));
        assertFalse(displayStyle.contains("currentThemeCustomMarker != null"));
        assertFalse(displayStyle.contains("settingsSaver != null"));
        assertFalse(displayStyle.contains("private KeyboardSettings settings()"));
        assertTrue(displayStyle.contains("RuntimeDefaults.keyboardSettingsFrom(settings)"));
        assertEquals(2, countOccurrences(displayStyle, "currentThemeCustomMarker.run();"));
        assertFalse(inputAssistanceController.contains("interface Host"));
        assertTrue(inputAssistanceController.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(inputAssistanceController.contains(
                "private final Supplier<KeyboardErgonomicsOptions> ergonomicsOptions"));
        assertTrue(inputAssistanceController.contains("private final BooleanSupplier debuggableBuild"));
        assertFalse(inputAssistanceController.contains("currentThemeCustomMarker"));
        assertTrue(inputAssistanceController.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(inputAssistanceController.contains(
                "private final BiConsumer<KeyboardSettings, KeyboardErgonomicsOptions>"));
        assertTrue(inputAssistanceController.contains("private final Runnable controlsSyncer"));
        assertTrue(inputAssistanceController.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(inputAssistanceController.contains("RuntimeDefaults.keyboardErgonomicsSupplier("));
        assertTrue(inputAssistanceController.contains("RuntimeDefaults.booleanSupplier("));
        assertTrue(inputAssistanceController.contains("RuntimeDefaults.runnable("));
        assertTrue(inputAssistanceController.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(inputAssistanceController.contains("RuntimeDefaults.keyboardSettingsAndErgonomicsConsumer("));
        assertTrue(inputAssistanceController.contains("RuntimeDefaults.keyboardErgonomics("));
        assertContainsNone(
                inputAssistanceController,
                "current == null\n                ? KeyboardErgonomicsOptions.DEFAULT",
                "currentOptions == null ? KeyboardErgonomicsOptions.DEFAULT : currentOptions",
                "private void markCurrentThemeCustom()",
                "private void saveSettings(KeyboardSettings settings)",
                "private void syncControls()",
                "private KeyboardSettings settings()",
                "markCurrentThemeCustom();",
                "saveSettings(settings",
                "syncControls();");
        assertFalse(inputFeelController.contains("interface Host"));
        assertTrue(inputFeelController.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(inputFeelController.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(inputFeelController.contains("private final Runnable controlsSyncer"));
        assertTrue(inputFeelController.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(inputFeelController.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(inputFeelController.contains("RuntimeDefaults.runnable("));
        assertFalse(inputFeelController.contains("this::settings"));
        assertFalse(inputFeelController.contains("private KeyboardSettings settings()"));
        String inputFeelControllers = String.join(
                "\n",
                inputFeelController,
                javaSource("GestureTouchSettingsController"),
                javaSource("HapticSettingsController"),
                javaSource("InputConvenienceSettingsController"),
                javaSource("RepeatSettingsController"));
        assertContainsNone(
                inputFeelControllers,
                "private void saveSettings(KeyboardSettings settings)",
                "private void syncControls()",
                "this::saveSettings",
                "this::syncControls",
                "saveSettings(settings().",
                "syncControls();",
                "private KeyboardSettings settings()");
        assertTrue(inputFeelControllers.contains("RuntimeDefaults.keyboardSettingsFrom(settings)"));
        assertFalse(layoutController.contains("interface Host"));
        assertTrue(layoutController.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(layoutController.contains("private final Supplier<KeyboardErgonomicsOptions> ergonomicsOptions"));
        assertTrue(layoutController.contains("private final Runnable currentThemeCustomMarker"));
        assertEquals(2, countOccurrences(layoutController, "currentThemeCustomMarker.run();"));
        assertTrue(layoutController.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(layoutController.contains(
                "private final Consumer<KeyboardLayoutProfile> hangulLayoutProfileSaver"));
        assertTrue(layoutController.contains(
                "private final Consumer<KeyboardLayoutProfile> englishLayoutProfileSaver"));
        assertTrue(layoutController.contains(
                "private final Consumer<KeyboardErgonomicsOptions> ergonomicsOptionsSaver"));
        assertTrue(layoutController.contains("private final Runnable controlsSyncer"));
        assertTrue(layoutController.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(layoutController.contains("RuntimeDefaults.keyboardErgonomicsSupplier("));
        assertTrue(layoutController.contains("RuntimeDefaults.runnable("));
        assertTrue(layoutController.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(layoutController.contains("RuntimeDefaults.keyboardLayoutProfileConsumer("));
        assertTrue(layoutController.contains("RuntimeDefaults.keyboardErgonomicsConsumer("));
        assertTrue(layoutController.contains("RuntimeDefaults.keyboardErgonomicsFrom("));
        assertTrue(layoutController.contains("RuntimeDefaults.keyboardSettingsFrom(settings)"));
        assertFalse(layoutController.contains("private KeyboardSettings settings()"));
        assertFalse(layoutController.contains("private KeyboardErgonomicsOptions ergonomicsOptions()"));
        assertTrue(layoutController.contains("private static final KeyboardLayoutProfile[] LAYOUT_PROFILE_ORDER"));
        assertTrue(layoutController.contains("HandednessMode.displayOrder()"));
        assertTrue(layoutController.contains("KeyboardLayoutProfile.displayOrder()"));
        assertTrue(layoutController.contains("SettingsRowBuilder.optionSpinner("));
        assertTrue(layoutController.contains("HandednessMode.indexOf("));
        assertTrue(layoutController.contains("KeyboardLayoutProfile.indexOf("));
        assertFalse(layoutController.contains(".ordinal()"));
        assertFalse(layoutController.contains("KeyboardLayoutProfile.values()[position]"));
        assertFalse(layoutController.contains("SettingsDisplayLabels.labels(context, LAYOUT_PROFILE_ORDER)"));
        assertFalse(layoutController.contains("SettingsDisplayLabels.labels(context, HANDEDNESS_ORDER)"));
        assertFalse(layoutController.contains("RuntimeDefaults.keyboardSettings(settings.get())"));
        assertFalse(layoutController.contains("RuntimeDefaults.keyboardErgonomics(ergonomicsOptions.get())"));
        assertFalse(layoutController.contains("this.settings = settings;"));
        assertFalse(layoutController.contains("this.ergonomicsOptions = ergonomicsOptions;"));
        assertFalse(layoutController.contains("this.currentThemeCustomMarker = currentThemeCustomMarker;"));
        assertFalse(layoutController.contains("this.settingsSaver = settingsSaver;"));
        assertFalse(layoutController.contains("this.hangulLayoutProfileSaver = hangulLayoutProfileSaver;"));
        assertFalse(layoutController.contains("this.englishLayoutProfileSaver = englishLayoutProfileSaver;"));
        assertFalse(layoutController.contains("this.ergonomicsOptionsSaver = ergonomicsOptionsSaver;"));
        assertFalse(layoutController.contains("this.controlsSyncer = controlsSyncer;"));
        assertFalse(layoutController.contains("currentOptions == null ? KeyboardErgonomicsOptions.DEFAULT"));
        String layoutSettingsControllers = layoutController + "\n" + javaSource("ErgonomicsSettingsController");
        assertContainsNone(
                layoutSettingsControllers,
                "private void saveSettings(KeyboardSettings settings)",
                "private void markCurrentThemeCustom()",
                "private void syncControls()",
                "private void saveHangulLayoutProfile(",
                "private void saveEnglishLayoutProfile(",
                "private void saveErgonomicsOptions(",
                "private KeyboardSettings safe(",
                "private KeyboardSettings settings()",
                "this::saveSettings",
                "this::markCurrentThemeCustom",
                "this::syncControls",
                "this::saveErgonomicsOptions",
                "saveSettings(settings().",
                "markCurrentThemeCustom();",
                "syncControls();");
        assertFalse(remoteWindowsController.contains("interface Host"));
        assertTrue(remoteWindowsController.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(remoteWindowsController.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(remoteWindowsController.contains("private final Runnable controlsSyncer"));
        assertContainsNone(
                remoteWindowsController,
                "private void saveSettings(KeyboardSettings settings)",
                "private void syncControls()",
                "private KeyboardSettings settings()",
                "saveSettings(settings",
                "syncControls();");
        assertTrue(remoteWindowsController.contains("RuntimeDefaults.keyboardSettingsFrom(settings)"));
        assertFalse(typographyController.contains("interface Host"));
        assertTrue(typographyController.contains("private final Supplier<KeyboardSettings> settings"));
        assertFalse(typographyController.contains("currentThemeCustomMarker"));
        assertTrue(typographyController.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(typographyController.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(typographyController.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertFalse(typographyController.contains("settings == null ? null"));
        assertFalse(typographyController.contains("currentThemeCustomMarker != null"));
        assertFalse(typographyController.contains("settingsSaver != null"));
        assertFalse(typographyController.contains("private KeyboardSettings settings()"));
        assertTrue(typographyController.contains("RuntimeDefaults.keyboardSettingsFrom(settings)"));
        assertTrue(localDataSettings.contains("SettingsRowBuilder.iconButtonRow("));
        assertTrue(localDataSettings.contains("RuntimeDefaults.runnable("));
        assertFalse(localDataSettings.contains("controlsSyncer != null"));
        assertContainsNone(
                localDataSettings,
                "private void syncControls()",
                "syncControls();");
        assertFalse(themeHub.contains("private Button systemButton("));
        assertFalse(androidIme.contains("private Button systemButton("));
        assertFalse(localDataSettings.contains("private Button button("));
        assertFalse(themeHub.contains("private LinearLayout.LayoutParams buttonParams("));
        assertFalse(androidIme.contains("private LinearLayout.LayoutParams buttonParams("));
        assertFalse(displayStyle.contains("private LinearLayout.LayoutParams buttonParams("));
        assertFalse(localDataSettings.contains("private LinearLayout.LayoutParams buttonParams("));

        for (String className : new String[] {
                "AccentPlacementActivity",
                "QuickPanelUi",
                "ThemeEditorActivity",
                "ThemeSelectorActivity"
        }) {
            String source = javaSource(className);
            assertTrue(className + " should use shared weighted row helpers",
                    source.contains("SettingsRowBuilder.weightedWrap(")
                            || source.contains("SettingsRowBuilder.weightedButton("));
            assertFalse(className + " should not keep local weighted button params",
                    source.contains("weightedButtonParams("));
            assertFalse(className + " should not duplicate weighted layout allocation",
                    source.contains("new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)"));
        }

        String[] factoryBackedSources = {
                "AccentPlacementActivity",
                "DebugOverlaySettingsController",
                "DisplayStyleSettingsController",
                "ErgonomicsSettingsController",
                "GestureTouchSettingsController",
                "HapticSettingsController",
                "InputAssistanceSettingsController",
                "InputConvenienceSettingsController",
                "LayoutSettingsController",
                "MotionEffectSettingsController",
                "RepeatSettingsController",
                "RemoteWindowsSettingsController",
                "ThemeEditorActivity",
                "TypographySettingsController"
        };
        for (String className : factoryBackedSources) {
            String source = javaSource(className);
            assertFalse(className + " should not duplicate spinner listener shells",
                    source.contains("new AdapterView.OnItemSelectedListener"));
            assertFalse(className + " should not duplicate seekbar listener shells",
                    source.contains("new SeekBar.OnSeekBarChangeListener"));
            assertFalse(className + " should not duplicate text listener shells",
                    source.contains("new TextWatcher"));
        }

        for (String className : new String[] {
                "AccentPlacementActivity",
                "DebugOverlaySettingsController",
                "DisplayStyleSettingsController",
                "ErgonomicsSettingsController",
                "HapticSettingsController",
                "InputAssistanceSettingsController",
                "InputConvenienceSettingsController",
                "LayoutSettingsController",
                "RemoteWindowsSettingsController",
                "ThemeEditorActivity",
                "TypographySettingsController"
        }) {
            String source = javaSource(className);
            assertTrue(className + " should use shared checkbox wiring",
                    source.contains("SettingsRowBuilder.checkBoxRow(")
                            || source.contains("SettingsRowBuilder.checkBox("));
            assertFalse(className + " should not wire simple checkbox listeners directly",
                    source.contains("UserInputListeners.checked("));
            assertFalse(className + " should not keep local checkbox wrappers",
                    source.contains("private CheckBox checkBox("));
        }

        String sharedCheckboxSource = normalizeNewlines(javaSource("SettingsRowBuilder"));
        assertTrue(sharedCheckboxSource.contains("static CheckBox checkBoxRow(\n"
                + "            Context context,\n"
                + "            LinearLayout root,\n"
                + "            String label,"));

        for (String className : new String[] {
                "AccentPlacementActivity",
                "DisplayStyleSettingsController",
                "ErgonomicsSettingsController",
                "GestureTouchSettingsController",
                "InputAssistanceSettingsController",
                "LayoutSettingsController",
                "MotionEffectSettingsController",
                "RemoteWindowsSettingsController",
                "TypographySettingsController"
        }) {
            String source = javaSource(className);
            assertTrue(className + " should use shared spinner wiring",
                    source.contains("SettingsRowBuilder.spinner(")
                            || source.contains("SettingsRowBuilder.optionSpinner(")
                            || source.contains("SettingsRowBuilder.spinnerAfterInitialSelection("));
            assertFalse(className + " should not wire simple spinner listeners directly",
                    source.contains("UserInputListeners.itemSelected("));
        }

        for (String className : new String[] {
                "GestureTouchSettingsController",
                "HapticSettingsController",
                "LayoutSettingsController",
                "RepeatSettingsController",
                "ThemeEditorActivity",
                "TypographySettingsController"
        }) {
            String source = javaSource(className);
            assertTrue(className + " should use shared seekbar wiring",
                    source.contains("SettingsRowBuilder.seekBar(")
                            || source.contains("SettingsRowBuilder.seekBarRow("));
            assertFalse(className + " should not create raw seekbars directly",
                    source.contains("new SeekBar("));
            assertFalse(className + " should not wire simple seekbar listeners directly",
                    source.contains("UserInputListeners.seekBar("));
        }

        for (String className : new String[] {
                "GesturePracticeInputController",
                "NumericStepperRow",
                "RemoteWindowsSettingsController",
                "ReservedPhraseSettingsController",
                "ThemeEditorActivity",
                "ThemeSelectorActivity"
        }) {
            String source = javaSource(className);
            assertTrue(className + " should use shared text input wiring",
                    source.contains("SettingsRowBuilder.editText("));
            assertFalse(className + " should not create standard text inputs directly",
                    source.contains("new EditText("));
            assertFalse(className + " should not style standard text inputs directly",
                    source.contains("SettingsViewStyler.editText("));
        }

        for (String className : new String[] {
                "RemoteWindowsSettingsController",
                "ReservedPhraseSettingsController"
        }) {
            String source = javaSource(className);
            assertFalse(className + " should not wire saved settings text watchers directly",
                    source.contains("addTextChangedListener("));
            assertFalse(className + " should not wire text listener helpers directly",
                    source.contains("UserInputListeners.text("));
        }

        String remoteWindows = javaSource("RemoteWindowsSettingsController");
        assertTrue(remoteWindows.contains("Consumer<String> saver"));
        assertFalse(remoteWindows.contains("interface Host"));
        assertTrue(remoteWindows.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(remoteWindows.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(remoteWindows.contains("private final Runnable controlsSyncer"));
        assertTrue(remoteWindows.contains("SettingsRowBuilder.bodyLabelRow("));
        assertTrue(remoteWindows.contains("SettingsRowBuilder.secondaryLabelRow("));
        assertTrue(remoteWindows.contains("private String currentAppProfileSummary()"));
        assertTrue(remoteWindows.contains("SettingsRowBuilder.labeledControl("));
        assertTrue(remoteWindows.contains("SettingsRowBuilder.optionSpinner("));
        assertTrue(remoteWindows.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(remoteWindows.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(remoteWindows.contains("RuntimeDefaults.stringConsumer("));
        assertTrue(remoteWindows.contains("RuntimeDefaults.trimmedStringOrEmpty(value)"));
        assertTrue(remoteWindows.contains("RuntimeDefaults.stringOrDefault(value, \"\")"));
        assertTrue(remoteWindows.contains("RemoteKeyPreset.displayOrder()"));
        assertTrue(remoteWindows.contains("RemoteImeShortcut.displayOrder()"));
        assertTrue(remoteWindows.contains("RemoteKeyPreset.indexOf("));
        assertTrue(remoteWindows.contains("RemoteImeShortcut.indexOf("));
        assertFalse(remoteWindows.contains("PackageListSaver"));
        assertFalse(remoteWindows.contains("remoteOptionSpinner("));
        assertFalse(remoteWindows.contains("RemoteKeyPreset.values()"));
        assertFalse(remoteWindows.contains("RemoteImeShortcut.values()"));
        assertFalse(remoteWindows.contains("RemoteKeyPreset.values()[position]"));
        assertFalse(remoteWindows.contains("RemoteImeShortcut.values()[position]"));
        assertFalse(remoteWindows.contains("remoteKeyPreset.ordinal()"));
        assertFalse(remoteWindows.contains("remoteImeShortcut.ordinal()"));
        assertFalse(remoteWindows.contains("SettingsDisplayLabels.labels(context, values)"));
        assertFalse(remoteWindows.contains("safeSaver.accept(values[position])"));
        assertFalse(remoteWindows.contains("value == null || value.trim().isEmpty()"));
        assertFalse(remoteWindows.contains("value == null ? \"\" : value"));
        assertFalse(remoteWindows.contains("private void addBodyText"));
        assertFalse(remoteWindows.contains("private interface"));
        assertFalse(remoteWindows.contains("private LinearLayout.LayoutParams matchWrap"));
        assertFalse("Current app profile summary should stay inside the remote settings section.",
                Files.exists(findWorkspaceRoot().resolve(
                        "app/src/main/java/com/superl3/s3keyboard/CurrentAppProfilePanelController.java")));

        String remoteKeyPreset = javaSource("RemoteKeyPreset");
        assertTrue(remoteKeyPreset.contains("private static final RemoteKeyPreset[] DISPLAY_ORDER"));
        assertTrue(remoteKeyPreset.contains("static RemoteKeyPreset[] displayOrder()"));
        assertTrue(remoteKeyPreset.contains("static int indexOf(RemoteKeyPreset selected)"));

        String remoteImeShortcut = javaSource("RemoteImeShortcut");
        assertTrue(remoteImeShortcut.contains("private static final RemoteImeShortcut[] DISPLAY_ORDER"));
        assertTrue(remoteImeShortcut.contains("static RemoteImeShortcut[] displayOrder()"));
        assertTrue(remoteImeShortcut.contains("static int indexOf(RemoteImeShortcut selected)"));

        String layout = javaSource("LayoutSettingsController");
        assertTrue(layout.contains("Consumer<KeyboardLayoutProfile>"));
        assertTrue(layout.contains("IntConsumer listener"));
        assertFalse(layout.contains("interface Host"));
        assertTrue(layout.contains("private final Supplier<KeyboardSettings> settings"));
        assertTrue(layout.contains("private final Supplier<KeyboardErgonomicsOptions> ergonomicsOptions"));
        assertTrue(layout.contains("private final Runnable currentThemeCustomMarker"));
        assertTrue(layout.contains("private final Consumer<KeyboardSettings> settingsSaver"));
        assertTrue(layout.contains("private final Consumer<KeyboardLayoutProfile> hangulLayoutProfileSaver"));
        assertTrue(layout.contains("private final Consumer<KeyboardLayoutProfile> englishLayoutProfileSaver"));
        assertTrue(layout.contains("private final Consumer<KeyboardErgonomicsOptions> ergonomicsOptionsSaver"));
        assertTrue(layout.contains("private final Runnable controlsSyncer"));
        assertTrue(layout.contains("SettingsRowBuilder.labeledControl("));
        assertTrue(layout.contains("SettingsRowBuilder.valueLabelRow("));
        assertTrue(layout.contains("SettingsRowBuilder.seekBarRow("));
        assertTrue(layout.contains("R.string.settings_shared_padding_format"));
        assertTrue(layout.contains("NumericStepperRow sharedPaddingStepper"));
        assertFalse(layout.contains("implements ErgonomicsSettingsController.Host"));
        assertFalse(layout.contains("public KeyboardErgonomicsOptions ergonomicsOptions()"));
        assertFalse(layout.contains("public void saveErgonomicsOptions("));
        assertFalse(layout.contains("LayoutProfileChangeListener"));
        assertFalse(layout.contains("IntSettingListener"));
        assertFalse(layout.contains("private TextView addValueLabel("));
        assertFalse(layout.contains("private LinearLayout.LayoutParams matchWrap("));
        assertFalse(layout.contains("private LinearLayout.LayoutParams matchWrapWithTop("));

        String colorOption = javaSource("ColorOption");
        assertTrue(colorOption.contains("static int indexOf(ColorOption[] options, int color)"));
        assertTrue(colorOption.contains("static int indexOf(ColorOption[] options, Integer color, int missingIndex)"));
        assertTrue(colorOption.contains("static int basicIndexOf("));

        String numberRowMode = javaSource("AdditionalNumberRowColorMode");
        assertTrue(numberRowMode.contains("private static final AdditionalNumberRowColorMode[] DISPLAY_ORDER"));
        assertTrue(numberRowMode.contains("static AdditionalNumberRowColorMode[] displayOrder()"));
        assertTrue(numberRowMode.contains("static int indexOf("));
        assertTrue(numberRowMode.contains("for (AdditionalNumberRowColorMode mode : DISPLAY_ORDER)"));

        String numericStepper = javaSource("NumericStepperRow");
        assertTrue(numericStepper.contains("IntConsumer listener"));
        assertTrue(numericStepper.contains("RuntimeDefaults.intConsumer(listener)"));
        assertTrue(numericStepper.contains("SettingsRowBuilder.weightedWrap("));
        assertTrue(numericStepper.contains("R.drawable.ic_settings_minus"));
        assertTrue(numericStepper.contains("R.drawable.ic_settings_plus"));
        assertTrue(numericStepper.contains("R.string.settings_stepper_decrease_format"));
        assertTrue(numericStepper.contains("R.string.settings_stepper_increase_format"));
        assertTrue(numericStepper.contains("setButtonEnabled(minusButton, value > minValue)"));
        assertFalse(numericStepper.contains("interface Listener"));
        assertFalse(numericStepper.contains("onValueChanged"));
        assertFalse(numericStepper.contains("listener != null"));
        assertFalse(numericStepper.contains("private static LayoutParams buttonParams("));
        assertFalse(numericStepper.contains("private static LayoutParams inputParams("));
        assertFalse(numericStepper.contains("new LayoutParams("));
        assertFalse(numericStepper.contains("private static int dp("));

        String accentPlacement = javaSource("AccentPlacementActivity");
        assertTrue(accentPlacement.contains("SettingsRowBuilder.sectionLabelRow("));
        assertTrue(accentPlacement.contains("SettingsRowBuilder.addViewWithTop("));
        assertTrue(accentPlacement.contains("SettingsRowBuilder.matchHeightWithTop("));
        assertTrue(accentPlacement.contains("SettingsRowBuilder.optionSpinner("));
        assertTrue(accentPlacement.contains("AccentPlacementPolicy.SpaceRole.displayOrder()"));
        assertTrue(accentPlacement.contains("AccentPlacementPolicy.QuestionRole.displayOrder()"));
        assertTrue(accentPlacement.contains("AccentPlacementPolicy.SpaceRole.indexOf("));
        assertTrue(accentPlacement.contains("AccentPlacementPolicy.QuestionRole.indexOf("));
        assertTrue(accentPlacement.contains("AccentPlacementPolicy.SpaceRole.at("));
        assertTrue(accentPlacement.contains("AccentPlacementPolicy.QuestionRole.at("));
        assertTrue(accentPlacement.contains("AdditionalNumberRowColorMode.displayOrder()"));
        assertTrue(accentPlacement.contains("AdditionalNumberRowColorMode.indexOf("));
        assertTrue(accentPlacement.contains("numberRowModeSpinner.setEnabled(customPlacementEnabled);"));
        assertTrue(accentPlacement.contains("AccentPlacementTarget.displayOrder()"));
        assertTrue(accentPlacement.contains("AccentPlacementTarget.allDisplayTargets()"));
        assertTrue(accentPlacement.contains("SettingsRowBuilder.vertical(this)"));
        assertTrue(accentPlacement.contains("SettingsRowBuilder.horizontal(this)"));
        assertTrue(accentPlacement.contains("SettingsRowBuilder.weightedButton("));
        assertTrue(accentPlacement.contains("SettingsRowBuilder.buttonRow(this, root, R.string.action_close"));
        assertFalse(accentPlacement.contains("IntConsumer listener"));
        assertFalse(accentPlacement.contains("selectionSpinner("));
        assertFalse(accentPlacement.contains("AccentPlacementTarget.values()"));
        assertFalse(accentPlacement.contains("EnumSet.allOf(AccentPlacementTarget.class)"));
        assertFalse(accentPlacement.contains("SettingsDisplayLabels.labels(this, NUMBER_ROW_COLOR_MODE_ORDER)"));
        assertFalse(accentPlacement.contains("SettingsDisplayLabels.labels(this, SPACE_ROLE_ORDER)"));
        assertFalse(accentPlacement.contains("SettingsDisplayLabels.labels(this, QUESTION_ROLE_ORDER)"));
        assertFalse(accentPlacement.contains("spaceRole.ordinal()"));
        assertFalse(accentPlacement.contains("questionRole.ordinal()"));
        assertFalse(accentPlacement.contains("SpaceRole.values()["));
        assertFalse(accentPlacement.contains("QuestionRole.values()["));
        assertFalse(accentPlacement.contains("additionalNumberRowColorMode.ordinal()"));
        assertFalse(accentPlacement.contains("AdditionalNumberRowColorMode.values()[position]"));
        assertFalse(accentPlacement.contains("NUMBER_ROW_COLOR_MODE_ORDER[position]"));
        assertFalse(accentPlacement.contains("SelectionChangeListener"));
        assertFalse(accentPlacement.contains("private TextView label("));
        assertFalse(accentPlacement.contains("private TextView sectionLabel("));
        assertFalse(accentPlacement.contains("private Button actionButton("));
        assertFalse(accentPlacement.contains("SettingsRowBuilder.button(this, R.string.accent_placement_none)"));
        assertFalse(accentPlacement.contains("SettingsRowBuilder.button(this, R.string.accent_placement_select_all)"));
        assertFalse(accentPlacement.contains("SettingsRowBuilder.button(this, R.string.action_close)"));
        assertFalse(accentPlacement.contains("setOrientation(LinearLayout.HORIZONTAL)"));
        assertFalse(accentPlacement.contains("setOrientation(LinearLayout.VERTICAL)"));
        assertFalse(accentPlacement.contains("private LinearLayout.LayoutParams previewParams("));
        assertFalse(accentPlacement.contains("private LinearLayout.LayoutParams matchWrap("));
        assertFalse(accentPlacement.contains("private LinearLayout.LayoutParams topParams("));
        assertFalse(accentPlacement.contains("private int dp("));

        String userFacingStrings = readWorkspaceFile("app/src/main/res/values/strings.xml");
        assertContainsAll(
                userFacingStrings,
                ">입력 글자 미리보기<",
                ">키 표시 팩<",
                ">숫자줄 색상<",
                ">가운데만 기능 키<",
                ">가운데만 강조 키<",
                ">딩굴 . (Enter 위치)<",
                ">딩굴 / (Shift 위치)<");
        assertContainsNone(
                userFacingStrings,
                ">입력 preview 표시<",
                ">키 표시 override 팩<",
                ">Number row preview<",
                "가운데 4567",
                "바깥 123890",
                "visual Enter",
                "visual Shift",
                "raw key 입력란",
                "epoch %1$s",
                "journal %4$d");

        String accentPolicy = javaSource("AccentPlacementPolicy");
        assertTrue(accentPolicy.contains("private static final SpaceRole[] DISPLAY_ORDER"));
        assertTrue(accentPolicy.contains("static SpaceRole[] displayOrder()"));
        assertTrue(accentPolicy.contains("static SpaceRole at(int index)"));
        assertTrue(accentPolicy.contains("static int indexOf(SpaceRole selected)"));
        assertTrue(accentPolicy.contains("private static final QuestionRole[] DISPLAY_ORDER"));
        assertTrue(accentPolicy.contains("static QuestionRole[] displayOrder()"));
        assertTrue(accentPolicy.contains("static QuestionRole at(int index)"));
        assertTrue(accentPolicy.contains("static int indexOf(QuestionRole selected)"));

        String accentTarget = javaSource("AccentPlacementTarget");
        assertTrue(accentTarget.contains("private static final AccentPlacementTarget[] DISPLAY_ORDER"));
        assertTrue(accentTarget.contains("static AccentPlacementTarget[] displayOrder()"));
        assertTrue(accentTarget.contains("static EnumSet<AccentPlacementTarget> allDisplayTargets()"));

        String themeEditor = javaSource("ThemeEditorActivity");
        assertTrue(themeEditor.contains("IntConsumer selectedKeyTextListener"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.checkBoxRow("));
        assertFalse(themeEditor.contains("Consumer<Boolean> listener"));
        assertFalse(themeEditor.contains("private CheckBox checkBox("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.buttonRow("));
        assertFalse(themeEditor.contains("private Button addActionButton("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.valueLabel(this)"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.addViewWithTop("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.dp(this,"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.vertical(this)"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.horizontal(this)"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.label(this,"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.label(this, \"?\")"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.fixedSizeWithLeft(this, 42, 28, 8)"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.fixedWidthWrapWithLeft(this, 86, 8)"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.fixedSizeWithLeft(this, 30, 30, 8)"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.matchWeightedFill()"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.matchHeightWithVerticalMargins(this, 18, 4)"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.weightedHeight(this, 40,"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.setSelectionIfValid("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.setProgressIfPresent("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.setCheckedIfPresent("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.setTextIfPresent("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.setEnabledIfPresent("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.spinnerAfterInitialSelection("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.radioButton("));
        assertTrue(themeEditor.contains("RuntimeDefaults.stringOrEmpty(text)"));
        assertTrue(themeEditor.contains("ColorOption.editorIndexOf("));
        assertTrue(themeEditor.contains("SettingsRowBuilder.optionSpinner("));
        assertTrue(themeEditor.contains("private CheckBox addTypographyCheckBox("));
        assertTrue(themeEditor.contains("ColorOption.EDITOR_OPTIONS"));
        assertTrue(themeEditor.contains("FontOption.editorIndexOf("));
        assertTrue(themeEditor.contains("FontOption.EDITOR_OPTIONS"));
        assertTrue(themeEditor.contains("private static final class ColorControl"));
        assertTrue(themeEditor.contains("private ColorControl addColorSetting("));
        assertEquals("ThemeEditorActivity should keep standard color row creation behind one helper.",
                2,
                countOccurrences(themeEditor, "addColorSpinnerControl("));
        assertEquals("ThemeEditorActivity should not keep pass-through empty labels.",
                0,
                countOccurrences(themeEditor, "label(\"\")"));
        assertFalse(themeEditor.contains("ColorChangeListener"));
        assertFalse(themeEditor.contains("BooleanChangeListener"));
        assertFalse(themeEditor.contains("IntChangeListener"));
        assertFalse(themeEditor.contains("SpinnerSelectionListener"));
        assertFalse(themeEditor.contains("private int indexOfColor("));
        assertFalse(themeEditor.contains("private int indexOfEditorColor("));
        assertFalse(themeEditor.contains("private void setProgress("));
        assertFalse(themeEditor.contains("private void setChecked("));
        assertFalse(themeEditor.contains("private void setText("));
        assertFalse(themeEditor.contains("private void setEnabled("));
        assertFalse(themeEditor.contains("ColorOption.EDITOR_OPTIONS[position]"));
        assertFalse(themeEditor.contains("FontOption.EDITOR_OPTIONS[position]"));
        assertFalse(themeEditor.contains("ColorOption.editorColorAt(position)"));
        assertFalse(themeEditor.contains("FontOption.editorValueAt(position)"));
        assertFalse(themeEditor.contains("setOrientation(LinearLayout.HORIZONTAL)"));
        assertFalse(themeEditor.contains("setOrientation(LinearLayout.VERTICAL)"));
        assertTrue(themeEditor.contains("ModifierIconCatalog.selectablePackLabels(false"));
        assertTrue(themeEditor.contains("ModifierIconCatalog.selectablePackIndexOf("));
        assertTrue(themeEditor.contains("ModifierIconCatalog.selectablePackIdAt(position, false)"));
        assertTrue(themeEditor.contains("KeyDisplayOverridePackCatalog.selectablePackLabels(false"));
        assertTrue(themeEditor.contains("KeyDisplayOverridePackCatalog.selectablePackIndexOf("));
        assertTrue(themeEditor.contains("KeyDisplayOverridePackCatalog.selectablePackIdAt(position, false)"));
        assertTrue(themeEditor.contains("private Spinner themePackSpinner("));
        assertTrue(themeEditor.contains("IntFunction<String> packIdAt"));
        assertTrue(themeEditor.contains("Supplier<String> currentPackId"));
        assertTrue(themeEditor.contains("KeyboardVisualEffects.keyFaceGradientCurveLabels()"));
        assertTrue(themeEditor.contains("KeyboardVisualEffects.keyFaceGradientCurveIndexOf("));
        assertTrue(themeEditor.contains("KeyboardVisualEffects.keyFaceGradientCurveAt(position)"));
        assertFalse(themeEditor.contains("private int indexOfModifierIconPack("));
        assertFalse(themeEditor.contains("private int indexOfKeyDisplayPack("));
        assertFalse(themeEditor.contains("private int indexOfKeyFaceGradientCurve("));
        assertFalse(themeEditor.contains("private void setSelection(Spinner spinner"));
        assertTrue(themeEditor.contains("SettingsRowBuilder.button("));
        assertTrue(themeEditor.contains("SettingsDisplayLabels.label(this, option)"));
        assertFalse(themeEditor.contains("new Button("));
        assertFalse(themeEditor.contains("new RadioButton("));
        assertFalse(themeEditor.contains("ModifierIconCatalog.selectablePackIds(false)"));
        assertFalse(themeEditor.contains("KeyDisplayOverridePackCatalog.selectablePackIds(false)"));
        assertFalse(themeEditor.contains("KeyboardVisualEffects.keyFaceGradientCurveOrder()"));
        assertFalse(themeEditor.contains("ids[position]"));
        assertFalse(themeEditor.contains("private String[] keyFaceGradientCurveIds("));
        assertFalse(themeEditor.contains("private Button addActionButton("));
        assertFalse(themeEditor.contains("private Button actionButton("));
        assertFalse(themeEditor.contains("private Button actionButton(int labelResId"));
        assertFalse(themeEditor.contains("private LinearLayout.LayoutParams buttonParams("));
        assertFalse(themeEditor.contains("private Spinner selectionSpinner("));
        assertFalse(themeEditor.contains("private RadioButton radio("));
        assertFalse(themeEditor.contains("shouldHandleSpinnerSelection("));
        assertFalse(themeEditor.contains("UserInputListeners.itemSelected("));
        assertFalse(themeEditor.contains("private LinearLayout.LayoutParams matchWrap("));
        assertFalse(themeEditor.contains("private LinearLayout.LayoutParams matchWrapWithTop("));
        assertFalse(themeEditor.contains("LinearLayout.LayoutParams swatchParams"));

        assertFalse(themeEditor.contains("LinearLayout.LayoutParams codeParams"));
        assertFalse(themeEditor.contains("LinearLayout.LayoutParams infoParams"));
        assertFalse(themeEditor.contains("new LinearLayout.LayoutParams("));
        assertFalse(themeEditor.contains("private TextView label("));
        assertFalse(themeEditor.contains("new TextView("));
        assertFalse(themeEditor.contains("private int dp("));
        assertFalse(themeEditor.contains("private interface"));
        assertFalse(themeEditor.contains("text == null ? \"\" : text.toString()"));

        String ergonomics = javaSource("ErgonomicsSettingsController");
        assertTrue(ergonomics.contains(
                "BiFunction<KeyboardErgonomicsOptions, Boolean, KeyboardErgonomicsOptions>"));
        assertTrue(ergonomics.contains("Supplier<KeyboardErgonomicsOptions>"));
        assertTrue(ergonomics.contains("Consumer<KeyboardErgonomicsOptions>"));
        assertTrue(ergonomics.contains("SettingsRowBuilder.labelRow("));
        assertTrue(ergonomics.contains("RuntimeDefaults.keyboardErgonomicsSupplier("));
        assertTrue(ergonomics.contains("RuntimeDefaults.keyboardErgonomicsConsumer("));
        assertTrue(ergonomics.contains("RuntimeDefaults.keyboardErgonomicsFrom("));
        assertTrue(ergonomics.contains("SettingsRowBuilder.optionSpinner("));
        assertTrue(ergonomics.contains("KeyboardErgonomicsPreset.displayOrder()"));
        assertTrue(ergonomics.contains("VisualConsistencyLevel.displayOrder()"));
        assertTrue(ergonomics.contains("KeyboardErgonomicsPreset.indexOf("));
        assertTrue(ergonomics.contains("VisualConsistencyLevel.indexOf("));
        assertFalse(ergonomics.contains("KeyboardErgonomicsPreset.fromPosition"));
        assertFalse(ergonomics.contains("VisualConsistencyLevel.fromPosition"));
        assertFalse(ergonomics.contains("SettingsDisplayLabels.labels(context, PRESET_ORDER)"));
        assertFalse(ergonomics.contains("SettingsDisplayLabels.labels(context, VISUAL_CONSISTENCY_ORDER)"));
        assertFalse(ergonomics.contains("KeyboardErgonomicsPreset.values()[position]"));
        assertFalse(ergonomics.contains("VisualConsistencyLevel.values()[position]"));
        assertFalse(ergonomics.contains("matchingPreset.ordinal()"));
        assertFalse(ergonomics.contains("visualConsistencyLevel.ordinal()"));
        assertFalse(ergonomics.contains("private KeyboardErgonomicsOptions options()"));

        String ergonomicsPreset = javaSource("KeyboardErgonomicsPreset");
        assertTrue(ergonomicsPreset.contains("private static final KeyboardErgonomicsPreset[] DISPLAY_ORDER"));
        assertTrue(ergonomicsPreset.contains("static KeyboardErgonomicsPreset[] displayOrder()"));
        assertTrue(ergonomicsPreset.contains("static int indexOf(KeyboardErgonomicsPreset selected)"));
        assertFalse(ergonomicsPreset.contains("fromPosition("));

        String visualConsistency = javaSource("VisualConsistencyLevel");
        assertTrue(visualConsistency.contains("private static final VisualConsistencyLevel[] DISPLAY_ORDER"));
        assertTrue(visualConsistency.contains("static VisualConsistencyLevel[] displayOrder()"));
        assertTrue(visualConsistency.contains("static int indexOf(VisualConsistencyLevel selected)"));
        assertFalse(visualConsistency.contains("fromPosition("));
        assertFalse(ergonomics.contains("interface Host"));
        assertFalse(ergonomics.contains("ErgonomicOptionChange"));
        assertFalse(ergonomics.contains("ergonomicsOptions == null ? null"));
        assertFalse(ergonomics.contains("options == null ? KeyboardErgonomicsOptions.DEFAULT : options"));
        assertFalse(ergonomics.contains("ergonomicsOptionsSaver != null"));
        assertFalse(ergonomics.contains("private TextView label("));
        assertFalse(ergonomics.contains("private LinearLayout.LayoutParams matchWrap"));

        String typography = javaSource("TypographySettingsController");
        assertTrue(typography.contains("BiFunction<KeyboardSettings, Boolean, KeyboardSettings>"));
        assertTrue(typography.contains("RuntimeDefaults.keyboardSettingsSupplier("));
        assertTrue(typography.contains("RuntimeDefaults.keyboardSettingsConsumer("));
        assertTrue(typography.contains("SettingsRowBuilder.optionSpinner("));
        assertTrue(typography.contains("private SeekBar addTypographySizeSeekBar("));
        assertTrue(typography.contains("private CheckBox addTypographyCheckBox("));
        assertTrue(typography.contains("FontOption.basicIndexOf("));
        assertFalse(typography.contains("TypographyOptionChange"));
        assertFalse(typography.contains("settings == null ? null"));
        assertFalse(typography.contains("currentThemeCustomMarker != null"));
        assertFalse(typography.contains("settingsSaver != null"));
        assertFalse(typography.contains("SettingsDisplayLabels.labels(context, FontOption.BASIC_OPTIONS)"));
        assertFalse(typography.contains("FontOption.BASIC_OPTIONS[position]"));
        assertFalse(typography.contains("private int indexOfFont("));

        String fontOption = javaSource("FontOption");
        assertTrue(fontOption.contains("static int indexOf(FontOption[] options, String fontFamily)"));
        assertTrue(fontOption.contains("static int basicIndexOf("));

        String modifierIconCatalog = javaSource("ModifierIconCatalog");
        assertTrue(modifierIconCatalog.contains("static String[] selectablePackLabels("));
        assertTrue(modifierIconCatalog.contains("static String selectablePackIdAt("));
        assertTrue(modifierIconCatalog.contains("static int selectablePackIndexOf("));

        String keyDisplayPackCatalog = javaSource("KeyDisplayOverridePackCatalog");
        assertTrue(keyDisplayPackCatalog.contains("static String[] selectablePackLabels("));
        assertTrue(keyDisplayPackCatalog.contains("static String selectablePackIdAt("));
        assertTrue(keyDisplayPackCatalog.contains("static int selectablePackIndexOf("));

        String visualEffects = javaSource("KeyboardVisualEffects");
        assertTrue(visualEffects.contains("private static final String[] KEY_FACE_GRADIENT_CURVE_ORDER"));
        assertTrue(visualEffects.contains("static String[] keyFaceGradientCurveOrder()"));
        assertTrue(visualEffects.contains("static String[] keyFaceGradientCurveLabels()"));
        assertTrue(visualEffects.contains("static String keyFaceGradientCurveAt("));
        assertTrue(visualEffects.contains("static int keyFaceGradientCurveIndexOf(String curve)"));

        String themeSelector = javaSource("ThemeSelectorActivity");
        assertTrue(themeSelector.contains("SettingsRowBuilder.iconButtonRow("));
        assertTrue(themeSelector.contains("SettingsRowBuilder.weightedButton("));
        assertTrue(themeSelector.contains("SettingsRowBuilder.vertical(this)"));
        assertTrue(themeSelector.contains("SettingsRowBuilder.horizontal(this)"));
        assertTrue(themeSelector.contains("previewKeyboard(englishSettings)"));
        assertTrue(themeSelector.contains("previewKeyboard(hangulSettings)"));
        assertTrue(themeSelector.contains("R.string.external_theme_section"));
        assertFalse(themeSelector.contains("previewModeButton("));
        assertTrue(themeSelector.contains("SettingsRowBuilder.label(this, R.string.theme_selector_title)"));
        assertTrue(themeSelector.contains("SettingsRowBuilder.valueLabel(this)"));
        assertTrue(themeSelector.contains("SettingsRowBuilder.matchWrapWithTop("));
        assertTrue(themeSelector.contains("SettingsRowBuilder.matchHeightWithTop("));
        assertTrue(themeSelector.contains("SettingsRowBuilder.wrapContentWithLeft(this, 8)"));
        assertTrue(themeSelector.contains("SettingsRowBuilder.dp("));
        assertTrue(themeSelector.contains("ThemeOption.indexOfStableId("));
        assertFalse(themeSelector.contains("new Button("));
        assertFalse(themeSelector.contains("button.setOnClickListener(v ->"));
        assertFalse(themeSelector.contains("SettingsRowBuilder.button(this, R.string.external_theme_folder_setting)"));
        assertFalse(themeSelector.contains("SettingsRowBuilder.button(this, R.string.action_refresh)"));
        assertFalse(themeSelector.contains("setOrientation(LinearLayout.HORIZONTAL)"));
        assertFalse(themeSelector.contains("setOrientation(LinearLayout.VERTICAL)"));
        assertFalse(themeSelector.contains("new LinearLayout(this)"));
        assertFalse(themeSelector.contains("new LinearLayout.LayoutParams("));
        assertFalse(themeSelector.contains("private TextView label("));
        assertFalse(themeSelector.contains("private int indexOfSelectedTheme("));
        assertFalse(themeSelector.contains("private LinearLayout.LayoutParams previewParams("));
        assertTrue(themeEditor.contains("addColorControls(SettingsSubsection.add("));
        assertTrue(themeEditor.contains("addShapeControls(SettingsSubsection.add("));
        assertFalse(themeEditor.contains("previewMeta"));
        assertFalse(themeSelector.contains("private LinearLayout.LayoutParams topParams("));
        assertFalse(themeSelector.contains("private LinearLayout.LayoutParams matchWrap("));
        assertFalse(themeSelector.contains("private int dp("));

        String clipboardView = javaSource("ClipboardView");
        String clipboardPanel = javaSource("ClipboardPanelController");
        String englishSuggestions = javaSource("EnglishSuggestionStripController");
        String englishAssistant = javaSource("EnglishQwertyInputAssistant");
        String settingsRowBuilder = javaSource("SettingsRowBuilder");
        assertTrue(settingsRowBuilder.contains("static void setSelectionIfValid(Spinner spinner, int position)"));
        assertTrue(settingsRowBuilder.contains("static void setProgressIfPresent(SeekBar seekBar, int progress)"));
        assertTrue(settingsRowBuilder.contains("static void setCheckedIfPresent(CheckBox checkBox, boolean checked)"));
        assertTrue(settingsRowBuilder.contains("static void setTextIfPresent(TextView view, CharSequence text)"));
        assertTrue(settingsRowBuilder.contains("static void setEnabledIfPresent(View view, boolean enabled)"));
        assertTrue(settingsRowBuilder.contains("static Button button(Context context, int labelResId, View.OnClickListener listener)"));
        assertTrue(settingsRowBuilder.contains("static FrameLayout.LayoutParams frameMatchWrap()"));
        assertTrue(settingsRowBuilder.contains("static FrameLayout.LayoutParams frameMatchMatch()"));
        assertTrue(settingsRowBuilder.contains("static RadioButton radioButton(Context context, int id, int labelResId)"));
        assertTrue(clipboardView.contains("Consumer<String> onTextSelected"));
        assertTrue(clipboardView.contains("R.string.action_close"));
        assertTrue(clipboardView.contains("SettingsRowBuilder.label("));
        assertTrue(clipboardView.contains("SettingsRowBuilder.secondaryLabel("));
        assertTrue(clipboardView.contains("SettingsRowBuilder.dp("));
        assertTrue(clipboardView.contains("SettingsRowBuilder.vertical(getContext())"));
        assertTrue(clipboardView.contains("SettingsRowBuilder.horizontal(getContext())"));
        assertTrue(clipboardView.contains("private LinearLayout createHeader("));
        assertTrue(clipboardView.contains("private void addSavedItems()"));
        assertTrue(clipboardView.contains("private void addRecentClipboard()"));
        assertFalse(clipboardView.contains("OnTextSelectedListener"));
        assertFalse(clipboardView.contains("private LinearLayout createHeader(Context context)"));
        assertFalse(clipboardView.contains("new Button("));
        assertFalse(clipboardView.contains("new LinearLayout(context)"));
        assertFalse(clipboardView.contains("closeBtn.setOnClickListener("));
        assertFalse(clipboardView.contains("new TextView("));
        assertFalse(clipboardView.contains("private LayoutParams entryItemParams("));
        assertFalse(clipboardView.contains("private int dp("));
        assertFalse(clipboardView.contains("Math.round(value *"));
        String quickSettings = javaSource("QuickSettingsPanelController");
        String leftAssistRail = javaSource("LeftAssistRailItem");
        assertTrue(clipboardPanel.contains("SettingsRowBuilder.frameMatchMatch()"));
        assertFalse(clipboardPanel.contains("new ImageButton(context)"));
        assertFalse(clipboardPanel.contains("R.drawable.ic_keyboard_clipboard"));
        assertFalse(clipboardPanel.contains("R.string.clipboard_toolbar_button"));
        assertFalse(clipboardPanel.contains("SettingsRowBuilder.fixedSize(context, 48, 48)"));
        assertFalse(clipboardPanel.contains("SettingsRowBuilder.horizontal(context)"));
        assertFalse(clipboardPanel.contains("SettingsRowBuilder.weightedSpacer("));
        assertFalse(clipboardPanel.contains("private int toolbarForegroundColor("));
        assertEquals(0, countOccurrences(clipboardPanel, "KeyboardColorMath.contrastTextColor("));
        assertFalse(clipboardPanel.contains("new Button("));
        assertFalse(clipboardPanel.contains("new LinearLayout(context)"));
        assertFalse(clipboardPanel.contains("setOrientation(LinearLayout."));
        assertTrue(quickSettings.contains("private final Runnable textToolsOpener;"));
        assertEquals(1, countOccurrences(quickSettings, "R.string.clipboard_toolbar_button"));
        assertEquals(1, countOccurrences(quickSettings, "v -> textToolsOpener.run()"));
        assertTrue(leftAssistRail.contains("CLIPBOARD(KeyboardCommands.CMD_CLIPBOARD_PANEL"));
        assertFalse(clipboardPanel.contains("new TextView("));
        assertFalse(clipboardPanel.contains("new LinearLayout.LayoutParams("));
        assertFalse(clipboardPanel.contains("new FrameLayout.LayoutParams("));
        assertFalse(clipboardPanel.contains("private LinearLayout.LayoutParams dragHandleParams("));
        assertFalse(clipboardPanel.contains("private KeyboardSettings settings()"));
        assertFalse(clipboardPanel.contains("private EditorInputPolicy editorPolicy()"));
        assertFalse(clipboardPanel.contains("private int dp("));
        assertFalse(clipboardPanel.contains("Math.round(value *"));
        assertTrue(englishSuggestions.contains("Consumer<String> onSuggestionAccepted"));
        assertTrue(englishSuggestions.contains("RuntimeDefaults.stringConsumer("));
        assertTrue(englishSuggestions.contains("RuntimeDefaults.keyboardSettings("));
        assertTrue(englishSuggestions.contains("SettingsRowBuilder.dp("));
        assertTrue(englishSuggestions.contains("SettingsRowBuilder.horizontal(context)"));
        assertTrue(englishSuggestions.contains("SettingsRowBuilder.weightedHeight("));
        assertTrue(englishSuggestions.contains("SettingsRowBuilder.label(context, \"\")"));
        assertTrue(englishSuggestions.contains("private TextView createSlot()"));
        assertTrue(englishSuggestions.contains("private void resetSlot(TextView slot)"));
        assertTrue(englishSuggestions.contains("private void applySuggestion("));
        assertFalse(englishSuggestions.contains("interface Host"));
        assertFalse(englishSuggestions.contains("void acceptSuggestion("));
        assertFalse(englishSuggestions.contains("new TextView("));
        assertFalse(englishSuggestions.contains("new LinearLayout(context)"));
        assertFalse(englishSuggestions.contains("setOrientation(LinearLayout.HORIZONTAL)"));
        assertFalse(englishSuggestions.contains("new LinearLayout.LayoutParams("));
        assertFalse(englishSuggestions.contains("final TextView slot = new TextView(context);"));
        assertFalse(englishSuggestions.contains("if (onSuggestionAccepted != null)"));
        assertFalse(englishSuggestions.contains("settings == null ? KeyboardSettings.defaults() : settings"));
        assertFalse(settingsRowBuilder.contains("wrapWrap("));
        assertFalse(englishSuggestions.contains("private int dp("));
        assertFalse(englishSuggestions.contains("Math.round(value *"));
        assertTrue(englishAssistant.contains("RuntimeDefaults.englishQwertyCorrectionEngine("));
        assertFalse(englishAssistant.contains("correctionEngine == null"));
        assertFalse(englishAssistant.contains("? EnglishQwertyCorrectionEngine.DEFAULT"));

        String oneFingerPractice = javaSource("OneFingerPracticeController");
        assertTrue(oneFingerPractice.contains("inputController.createInput("));
        assertTrue(oneFingerPractice.contains("OneFingerPracticeSession"));
        String localData = javaSource("LocalDataSettingsController");
        assertFalse(localData.contains("PracticeModeController"));

        String reservedPhrase = javaSource("ReservedPhraseSettingsController");
        assertTrue(reservedPhrase.contains("SettingsRowBuilder.labeledControl("));
        assertFalse(reservedPhrase.contains("private LinearLayout.LayoutParams matchWrap"));

        assertTrue(localData.contains("SettingsRowBuilder.bodyLabelRow("));
        assertTrue(localData.contains("SettingsRowBuilder.iconButtonRow("));
        assertFalse(localData.contains("private void addBodyText"));
        assertFalse(localData.contains("private TextView label("));
        assertFalse(localData.contains("private LinearLayout.LayoutParams matchWrap"));

        String diagnostics = javaSource("DingulInputDiagnostics");
        assertTrue(diagnostics.contains("RuntimeDefaults.stringOrDefault(latestKeyCodePoints, \"\")"));
        assertTrue(diagnostics.contains("RuntimeDefaults.stringOrDefault(latestAction, \"\")"));
        assertTrue(diagnostics.contains("RuntimeDefaults.stringOrDefault(latestType, \"\")"));
        assertFalse(diagnostics.contains("latestKeyCodePoints == null ? \"\" : latestKeyCodePoints"));
        assertFalse(diagnostics.contains("latestAction == null ? \"\" : latestAction"));
        assertFalse(diagnostics.contains("latestType == null ? \"\" : latestType"));

        String settingsHub = javaSource("SettingsHubController");
        assertTrue(settingsHub.contains("SettingsRowBuilder.bodyLabelRow("));
        assertTrue(settingsHub.contains("SettingsRowBuilder.matchWrapWithTop("));
        assertFalse(settingsHub.contains("private void addBodyText"));
        assertFalse(settingsHub.contains("private LinearLayout.LayoutParams matchWrap"));
        assertFalse(settingsHub.contains("private int dp("));

        String colorMath = javaSource("KeyboardColorMath");
        assertTrue(colorMath.contains("contrastTextColor("));
        for (String className : new String[] {
                "HangulKeyboardView",
                "ThemeEditorActivity"
        }) {
            String source = javaSource(className);
            assertTrue(className + " should use shared contrast color math",
                    source.contains("KeyboardColorMath.contrastTextColor(")
                            || source.contains("perceivedLuminance("));
            assertFalse(className + " should not carry a local contrast helper",
                    source.contains("private int contrastColor("));
            assertFalse(className + " should not duplicate fractional luminance math",
                    source.contains("0.299 *"));
            assertFalse(className + " should not duplicate integer RGB luminance math",
                    source.contains("r * 299 + g * 587 + b * 114"));
            assertFalse(className + " should not duplicate integer RGB luminance math",
                    source.contains("red * 299 + green * 587 + blue * 114"));
        }
    }

    @Test
    public void englishCorrectionOrderingUsesNamedComparatorChain() throws Exception {
        String engine = javaSource("EnglishQwertyCorrectionEngine");

        assertTrue(engine.contains("private static final Comparator<Candidate> CANDIDATE_ORDER"));
        assertTrue(engine.contains("thenComparingInt(candidate -> candidate.text.length())"));
        assertTrue(engine.contains("candidates.sort(CANDIDATE_ORDER)"));
        assertFalse(engine.contains("new Comparator<Candidate>()"));
        assertFalse(engine.contains("Boolean.compare(second.exactCorrection, first.exactCorrection)"));
        assertFalse(engine.contains("Float.compare(second.score, first.score)"));
    }

    @Test
    public void keyboardSettingsFieldsStayRepresentedInTypedSections() throws Exception {
        String settings = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardSettings.java");
        String sections = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardSettingsSections.java");
        String model = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardSettingsModel.java");
        String representedSettings = sections + "\n" + model;

        assertContainsAll(
                model,
                "RuntimeDefaults.keyboardSettings(",
                "RuntimeDefaults.keyboardErgonomics(");
        assertTrue(sections.contains("RuntimeDefaults.keyboardErgonomics(options)"));
        assertTrue(sections.contains("RuntimeDefaults.keyboardVisualEffects(effects)"));
        assertContainsNone(
                model + "\n" + sections,
                "settings == null ? KeyboardSettings.defaults() : settings",
                "ergonomics == null ? KeyboardErgonomicsOptions.DEFAULT : ergonomics",
                "effects == null ? KeyboardVisualEffects.DEFAULT : effects",
                "options == null\n                    ? KeyboardErgonomicsOptions.DEFAULT",
                "return from(settings, KeyboardErgonomicsOptions.DEFAULT);");

        for (String field : keyboardSettingsFieldNames(settings)) {
            assertTrue(
                    "KeyboardSettings field must be represented in typed settings sections/model: " + field,
                    representedSettings.contains(field));
        }
    }

    private String readWorkspaceFile(String relativePath) throws IOException {
        Path root = findWorkspaceRoot();
        return new String(Files.readAllBytes(root.resolve(relativePath)), StandardCharsets.UTF_8);
    }

    private String javaSource(String className) throws IOException {
        return readWorkspaceFile("app/src/main/java/com/superl3/s3keyboard/" + className + ".java");
    }

    private static String normalizeNewlines(String source) {
        return source == null ? "" : source.replace("\r\n", "\n");
    }

    private static void assertContainsAll(String source, String... needles) {
        for (String needle : needles) {
            assertTrue("Expected source to contain: " + needle, source.contains(needle));
        }
    }

    private static void assertContainsNone(String source, String... needles) {
        for (String needle : needles) {
            assertFalse("Expected source not to contain: " + needle, source.contains(needle));
        }
    }

    private static List<String> keyboardSettingsFieldNames(String source) {
        List<String> fields = new ArrayList<>();
        for (String line : source.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("KeyboardSettings(")) {
                break;
            }
            if (!trimmed.startsWith("final ") || !trimmed.endsWith(";")) {
                continue;
            }
            String withoutSemicolon = trimmed.substring(0, trimmed.length() - 1);
            fields.add(withoutSemicolon.substring(withoutSemicolon.lastIndexOf(' ') + 1));
        }
        return fields;
    }

    private static int countOccurrences(String text, String needle) {
        if (text == null || needle == null || needle.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while (true) {
            index = text.indexOf(needle, index);
            if (index < 0) {
                return count;
            }
            count++;
            index += needle.length();
        }
    }

    private static Map<String, ScriptRemoteCase> parseExportScriptMatrix(String exportScript) {
        Pattern pattern = Pattern.compile(
                "@\\{\\s*Label\\s*=\\s*\"([^\"]+)\";\\s*ExpectedEventCount\\s*=\\s*(\\d+);\\s*Group\\s*=\\s*\"([A-Z]+)\"\\s*\\}");
        Matcher matcher = pattern.matcher(exportScript);
        Map<String, ScriptRemoteCase> cases = new HashMap<>();
        while (matcher.find()) {
            String label = matcher.group(1);
            assertFalse("duplicate remote compatibility script case: " + label,
                    cases.containsKey(label));
            cases.put(label, new ScriptRemoteCase(
                    Integer.parseInt(matcher.group(2)),
                    matcher.group(3)));
        }
        return cases;
    }

    private static final class ScriptRemoteCase {
        final int expectedEventCount;
        final String group;

        ScriptRemoteCase(int expectedEventCount, String group) {
            this.expectedEventCount = expectedEventCount;
            this.group = group;
        }
    }

    private void collectTextFiles(Path directory, List<Path> files) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.endsWith(".java") || name.endsWith(".xml");
                    })
                    .forEach(files::add);
        }
    }

    private Path findWorkspaceRoot() {
        Path current = Paths.get("").toAbsolutePath();
        for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
            if (Files.exists(candidate.resolve("settings.gradle"))
                    && Files.exists(candidate.resolve("app/src/main/AndroidManifest.xml"))) {
                return candidate;
            }
        }
        throw new AssertionError("Workspace root not found from " + current);
    }

    private static String printable(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append("U+").append(Integer.toHexString(value.charAt(i)).toUpperCase(java.util.Locale.ROOT));
        }
        return builder.toString();
    }

    private static String quotedText(int... codePoints) {
        return "\"" + text(codePoints) + "\"";
    }

    private static String text(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    private static boolean containsHangul(String text) {
        if (text == null) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch >= '\uAC00' && ch <= '\uD7A3') {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAny(String text, String... markers) {
        if (text == null || markers == null) {
            return false;
        }
        for (String marker : markers) {
            if (marker != null && !marker.isEmpty() && text.contains(marker)) {
                return true;
            }
        }
        return false;
    }
}
