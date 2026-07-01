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
        assertFalse(strings.contains("?뚮"));
        assertFalse(strings.contains("?먭"));
        assertFalse(strings.contains("?ㅼ"));
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
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse("MainActivity should not carry Korean UI literals.", containsHangul(main));
        assertFalse("ThemeEditorActivity should not carry Korean UI literals.", containsHangul(editor));
        assertFalse("ThemeSelectorActivity should not carry Korean UI literals.", containsHangul(selector));
        assertFalse("AccentPlacementActivity should not carry Korean UI literals.", containsHangul(accent));
        assertFalse("AccentPlacementTarget should not carry Korean UI literals.", containsHangul(accentTarget));
        assertFalse("MainActivity should not carry escaped Korean UI literals.", main.contains("\\u"));
        assertFalse("ThemeEditorActivity should not carry escaped Korean UI literals.", editor.contains("\\u"));
        assertFalse("ThemeSelectorActivity should not carry escaped Korean UI literals.", selector.contains("\\u"));
        assertFalse("AccentPlacementActivity should not carry escaped Korean UI literals.", accent.contains("\\u"));
        assertFalse("AccentPlacementTarget should not carry escaped Korean UI literals.", accentTarget.contains("\\u"));
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

        assertTrue(buildGradle.contains("versionCode 1"));
        assertTrue(buildGradle.contains("versionName \"0.1.0\""));
        assertTrue(buildGradle.contains("minifyEnabled true"));
        assertTrue(buildGradle.contains("shrinkResources true"));
        assertTrue(buildGradle.contains("HANGUL_IME_KEYSTORE"));
    }

    @Test
    public void demoIntentOverridesAreDebugGated() throws Exception {
        String mainActivity = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");

        assertTrue(mainActivity.contains("EXTRA_DEMO_SETTINGS"));
        assertTrue(mainActivity.contains("EXTRA_DEMO_FIELD_PROFILE"));
        assertTrue(mainActivity.contains("applyDemoFieldProfile("));
        assertTrue(mainActivity.contains("TYPE_TEXT_VARIATION_WEB_PASSWORD"));
        assertTrue(mainActivity.contains("TYPE_TEXT_VARIATION_WEB_EDIT_TEXT"));
        assertTrue(mainActivity.contains("EditorInfo.IME_ACTION_SEARCH"));
        assertTrue(mainActivity.contains("isDebuggable()"));
        assertTrue(mainActivity.contains("debugDemoIntent"));
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
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String operator = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/InputConnectionTextOperator.java");

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
    }

    @Test
    public void previewOverlayTransportStaysDecoupledFromKeyboardViewInternals() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/PreviewOverlayController.java");
        String view = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/HangulKeyboardView.java");
        String spec = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/PreviewOverlaySpec.java");

        assertFalse(service.contains("HangulKeyboardView.PreviewOverlaySpec"));
        assertFalse(controller.contains("HangulKeyboardView.PreviewOverlaySpec"));
        assertFalse(view.contains("static final class PreviewOverlaySpec"));
        assertTrue(service.contains("onPreviewOverlayChanged(PreviewOverlaySpec spec)"));
        assertTrue(controller.contains("void show(View anchor, PreviewOverlaySpec spec)"));
        assertTrue(spec.contains("final class PreviewOverlaySpec"));
    }

    @Test
    public void nonInteractiveKeyboardPreviewsUseDedicatedFactory() throws Exception {
        String factory = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardPreviewFactory.java");
        String main = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
        String selector = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ThemeSelectorActivity.java");
        String accent = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/AccentPlacementActivity.java");

        assertTrue(factory.contains("new HangulKeyboardView(context)"));
        assertTrue(factory.contains("setCompactPreviewRendering(true)"));
        assertTrue(factory.contains("IMPORTANT_FOR_ACCESSIBILITY_NO"));
        assertTrue(main.contains("KeyboardPreviewFactory.nonInteractive("));
        assertTrue(selector.contains("KeyboardPreviewFactory.nonInteractive("));
        assertTrue(accent.contains("KeyboardPreviewFactory.nonInteractive("));
        assertFalse(main.contains("new HangulKeyboardView(this)"));
        assertFalse(selector.contains("new HangulKeyboardView(this)"));
        assertFalse(accent.contains("new HangulKeyboardView(this)"));
    }

    @Test
    public void remoteCompatibilityUiStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String panel = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteCompatibilityPanelController.java");
        String report = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteCompatibilityReport.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertTrue(service.contains("RemoteCompatibilityPanelController"));
        assertFalse(service.contains("RemoteCompatibilityLog.record("));
        assertFalse(service.contains("RemoteCompatibilityReport.describe("));
        assertFalse(service.contains("RemoteCompatibilityReport.toJson("));
        assertTrue(panel.contains("RemoteCompatibilityLog.record("));
        assertTrue(panel.contains("RemoteCompatibilityReport.describe("));
        assertTrue(panel.contains("RemoteCompatibilityReport.describe(\n                context,"));
        assertTrue(panel.contains("RemoteCompatibilityReport.toJson("));
        assertTrue(report.contains("R.string.remote_compatibility_summary_header"));
        assertTrue(report.contains("R.string.remote_compatibility_summary_counts"));
        assertTrue(report.contains("R.string.remote_compatibility_missing_cases"));
        assertTrue(report.contains("R.string.remote_compatibility_manual_required"));
        assertFalse(report.contains("\"원격 호환성: \""));
        assertFalse(report.contains("\"성공 \""));
        assertFalse(report.contains("\"실제 원격 세션"));
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
        String main = readWorkspaceFile("app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
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
        assertTrue(main.contains("settings_app_profile_overrides_help"));
        assertTrue(main.contains("saveAppProfileAsciiPackages"));
        assertTrue(main.contains("saveAppProfileNumberRowPackages"));
        assertTrue(main.contains("saveAppProfileNoComposingPackages"));
        assertTrue(main.contains("saveAppProfileNoTextConveniencesPackages"));
        assertTrue(strings.contains("name=\"settings_app_profile_ascii_packages\""));
        assertTrue(strings.contains("name=\"settings_app_profile_no_composing_packages\""));
    }

    @Test
    public void inputIssueReportClipboardFlowStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String report = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/InputIssueReport.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/InputIssueReportClipboardController.java");
        String quickSettings = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/QuickSettingsPanelController.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertTrue(service.contains("InputIssueReportClipboardController"));
        assertFalse(service.contains("InputIssueReport.build("));
        assertTrue(controller.contains("InputIssueReport.build("));
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
        assertTrue(quickSettings.contains("R.string.copy_input_issue_report"));
        assertTrue(strings.contains("name=\"copy_input_issue_report\""));
        assertTrue(strings.contains("이 입력이 이상해요"));
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

        assertTrue(provider.contains(
                "String description = KeyboardKeyAccessibilityLabel.describe(view.getContext(), keySlot.key)"));
        assertTrue(provider.contains("info.setContentDescription(description)"));
        assertTrue(provider.contains("info.setText(description)"));
        assertTrue(provider.contains("info.setFocusable(true)"));
        assertTrue(provider.contains("info.setClickable(true)"));
        assertTrue(provider.contains("info.addAction(AccessibilityNodeInfo.ACTION_CLICK)"));
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

        assertTrue(summary.contains("R.string.keyboard_accessibility_name"));
        assertTrue(summary.contains("R.string.keyboard_accessibility_mode_qwerty"));
        assertTrue(summary.contains("R.string.keyboard_accessibility_mode_dingul"));
        assertTrue(summary.contains("R.string.keyboard_surface_password"));
        assertFalse(summary.contains("\"한글"));
        assertFalse(summary.contains("\"영문"));
        assertFalse(summary.contains("\"비밀번호"));
        assertTrue(view.contains("KeyboardAccessibilitySummary.describe(\n                getContext(),"));
        assertTrue(provider.contains("KeyboardAccessibilitySummary.describe(\n                view.getContext(),"));
        assertTrue(strings.contains("name=\"keyboard_accessibility_name\""));
        assertTrue(strings.contains("name=\"keyboard_surface_password\""));
    }

    @Test
    public void debugKeyBoundsOverlayRenderingStaysOutOfCanvasViewBody() throws Exception {
        String view = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/HangulKeyboardView.java");
        String renderer = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardDebugOverlayRenderer.java");

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
    }

    @Test
    public void debugOverlaySettingsPersistenceStaysBehindController() throws Exception {
        String activity = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/DebugOverlaySettingsController.java");

        assertTrue(activity.contains("DebugOverlaySettingsController"));
        assertTrue(activity.contains("debugOverlaySettingsController.addTo("));
        assertFalse(activity.contains("debugKeyBoundsOverlayCheckBox"));
        assertFalse(activity.contains("debugShowResolverScoresCheckBox"));
        assertFalse(activity.contains("saveDebugShowResolverScores("));
        assertFalse(activity.contains("loadDebugShowResolverScores("));
        assertTrue(controller.contains("saveDebugOverlay("));
        assertTrue(controller.contains("saveDebugShowResolverScores("));
        assertTrue(controller.contains("loadDebugShowResolverScores("));
        assertTrue(controller.contains("setEnabled(overlayEnabled)"));
        assertTrue(controller.contains("SettingsViewStyler.compoundButton("));
    }

    @Test
    public void inputAssistanceModePersistenceStaysOutOfMainActivityBody() throws Exception {
        String activity = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/InputAssistanceSettingsController.java");

        assertTrue(activity.contains("InputAssistanceSettingsController"));
        assertFalse(activity.contains("saveShowHangulConsonantSlideHints("));
        assertFalse(activity.contains("saveShowHangulVowelSlideHints("));
        assertFalse(activity.contains("saveShowSpacebarSlideHints("));
        assertTrue(controller.contains("saveShowHangulConsonantSlideHints("));
        assertTrue(controller.contains("saveShowHangulVowelSlideHints("));
        assertTrue(controller.contains("saveShowSpacebarSlideHints("));
    }

    @Test
    public void hapticSettingsPersistenceStaysBehindController() throws Exception {
        String activity = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/HapticSettingsController.java");

        assertTrue(activity.contains("HapticSettingsController"));
        assertTrue(activity.contains("hapticSettingsController.addTo("));
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
    }

    @Test
    public void repeatSettingsPersistenceStaysBehindController() throws Exception {
        String activity = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RepeatSettingsController.java");

        assertTrue(activity.contains("RepeatSettingsController"));
        assertTrue(activity.contains("repeatSettingsController.addTo("));
        assertFalse(activity.contains("repeatStartDelaySeekBar"));
        assertFalse(activity.contains("repeatIntervalSeekBar"));
        assertFalse(activity.contains("repeatStartDelayValue"));
        assertFalse(activity.contains("repeatIntervalValue"));
        assertFalse(activity.contains("withRepeatTiming("));
        assertTrue(controller.contains("withRepeatTiming("));
        assertTrue(controller.contains("SettingsValueFormatter.repeatStartDelay("));
        assertTrue(controller.contains("SettingsValueFormatter.repeatInterval("));
    }

    @Test
    public void localDataDeletionStaysBehindSettingsController() throws Exception {
        String activity = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/LocalDataControlsController.java");

        assertTrue(activity.contains("LocalDataControlsController"));
        assertFalse(activity.contains("new ClipboardStore("));
        assertFalse(activity.contains("TouchBiasStore.reset("));
        assertFalse(activity.contains("RemoteCompatibilityLog.clear("));
        assertFalse(activity.contains("saveClipboardHistoryEnabled("));
        assertFalse(activity.contains("loadClipboardHistoryEnabled("));
        assertTrue(controller.contains("new ClipboardStore("));
        assertTrue(controller.contains("TouchBiasStore.reset("));
        assertTrue(controller.contains("RemoteCompatibilityLog.clear("));
        assertTrue(controller.contains("saveClipboardHistoryEnabled("));
        assertTrue(controller.contains("loadClipboardHistoryEnabled("));
        assertTrue(controller.contains("void clearAllLocalData()"));
        assertTrue(controller.contains("clearClipboardHistory();"));
        assertTrue(controller.contains("resetTouchCorrectionAndInputLogs();"));
        assertTrue(controller.contains("clearRemoteCompatibilityLog();"));
    }

    @Test
    public void localDataSummaryUsesStringResources() throws Exception {
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/LocalDataControlsController.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(controller.contains("\"로컬 데이터:"));
        assertFalse(controller.contains("\"입력 로그 "));
        assertFalse(controller.contains("\"원격 테스트 "));
        assertTrue(controller.contains("R.string.local_data_summary_format"));
        assertTrue(strings.contains("name=\"local_data_summary_format\""));
    }

    @Test
    public void mainSettingsLocalDataAndReservedPhraseCopyUseStringResources() throws Exception {
        String activity = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(activity.contains("\\uB85C\\uCEEC \\uC785\\uB825 \\uB85C\\uADF8"));
        assertFalse(activity.contains("\\uD130\\uCE58 \\uBCF4\\uC815/\\uC785\\uB825 \\uB85C\\uADF8"));
        assertFalse(activity.contains("\\uBE44\\uC6B0\\uBA74 \\uC785\\uB825\\uD558\\uC9C0"));
        assertTrue(activity.contains("R.string.local_data_disclosure"));
        assertTrue(activity.contains("R.string.clear_all_local_data"));
        assertTrue(activity.contains("localDataControlsController.clearAllLocalData()"));
        assertTrue(activity.contains("R.string.clear_touch_correction_and_input_logs"));
        assertTrue(activity.contains("R.string.reserved_phrase_empty_hint"));
        assertTrue(strings.contains("name=\"local_data_disclosure\""));
        assertTrue(strings.contains("name=\"clear_all_local_data\""));
        assertTrue(strings.contains("name=\"reserved_phrase_empty_hint\""));
    }

    @Test
    public void mainSettingsDynamicValueFormattingStaysInDedicatedFormatter() throws Exception {
        String activity = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");
        String formatter = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/SettingsValueFormatter.java");

        assertTrue(activity.contains("SettingsValueFormatter.hangulHeight("));
        assertTrue(activity.contains("SettingsValueFormatter.gestureThreshold("));
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

        assertFalse(resolver.contains("\"전송\""));
        assertFalse(resolver.contains("\"검색\""));
        assertFalse(resolver.contains("\"완료\""));
        assertFalse(resolver.contains("\"다음\""));
        assertFalse(resolver.contains("\"이동\""));
        assertFalse(resolver.contains("\"줄바꿈\""));
        assertTrue(resolver.contains("R.string.ime_action_send"));
        assertTrue(resolver.contains("R.string.ime_action_search"));
        assertTrue(resolver.contains("R.string.ime_action_done"));
        assertTrue(resolver.contains("R.string.ime_action_next"));
        assertTrue(resolver.contains("R.string.ime_action_go"));
        assertTrue(resolver.contains("R.string.ime_action_newline"));
        assertTrue(action.contains("final int labelResId"));
        assertTrue(action.contains("context.getString(labelResId)"));
        assertTrue(service.contains("enterActionLabel()"));
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
        assertFalse(commands.contains("\"삭제\""));
        assertFalse(commands.contains("\"스페이스\""));
        assertFalse(commands.contains("\"전송\""));
        assertFalse(commands.contains("\"한/영\""));
        assertFalse(commands.contains("\"빠른 설정\""));
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
    public void expandableSectionHeadersUseStringResources() throws Exception {
        String settingsSectionCard = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/SettingsSectionCard.java");
        String themeEditor = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ThemeEditorActivity.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertTrue(settingsSectionCard.contains("R.string.expandable_section_title_expanded"));
        assertTrue(settingsSectionCard.contains("R.string.expandable_section_title_collapsed"));
        assertTrue(themeEditor.contains("R.string.expandable_section_title_expanded"));
        assertTrue(themeEditor.contains("R.string.expandable_section_title_collapsed"));
        assertFalse(settingsSectionCard.contains("\"▼ "));
        assertFalse(settingsSectionCard.contains("\"▶ "));
        assertFalse(themeEditor.contains("\"- \""));
        assertFalse(themeEditor.contains("\"+ \""));
        assertTrue(strings.contains("name=\"expandable_section_title_expanded\""));
        assertTrue(strings.contains("name=\"expandable_section_title_collapsed\""));
    }

    @Test
    public void remoteCompatibilityEmptyHistoryUsesStringResource() throws Exception {
        String log = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteCompatibilityLog.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(log.contains("\"아직 원격 테스트 이력이 없습니다."));
        assertTrue(log.contains("R.string.remote_test_history_empty"));
        assertTrue(strings.contains("name=\"remote_test_history_empty\""));
    }

    @Test
    public void quickThemeSelectionStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/QuickThemePanelController.java");

        assertTrue(service.contains("QuickThemePanelController"));
        assertFalse(service.contains("ThemeOption.buildOptions("));
        assertFalse(service.contains("option.applyTo("));
        assertTrue(controller.contains("ThemeOption.buildOptions("));
        assertTrue(controller.contains("option.applyTo("));
    }

    @Test
    public void quickSettingsPanelStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/QuickSettingsPanelController.java");

        assertTrue(service.contains("QuickSettingsPanelController"));
        assertTrue(service.contains("quickSettingsPanelController.createPanel()"));
        assertFalse(service.contains("handednessButton("));
        assertFalse(service.contains("styleQuickButton("));
        assertFalse(service.contains("quickButton("));
        assertTrue(controller.contains("remoteCompatibilityPanelController.addTo("));
        assertTrue(controller.contains("quickThemePanelController.addTo("));
        assertTrue(controller.contains("handednessButton("));
    }

    @Test
    public void clipboardThemeImportStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ThemeClipboardImportController.java");

        assertTrue(service.contains("ThemeClipboardImportController"));
        assertFalse(service.contains("KeyboardThemeJson.importTheme("));
        assertFalse(service.contains("ClipboardManager clipboard"));
        assertFalse(service.contains("getPrimaryClip()"));
        assertTrue(controller.contains("KeyboardThemeJson.importTheme("));
        assertTrue(controller.contains("KeyboardPreferences.saveSelectedThemeId("));
        assertTrue(controller.contains("getPrimaryClip()"));
    }

    @Test
    public void clipboardHistoryPanelStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ClipboardPanelController.java");

        assertTrue(service.contains("ClipboardPanelController"));
        assertFalse(service.contains("capturePrimaryClipboard("));
        assertFalse(service.contains("addPrimaryClipChangedListener("));
        assertFalse(service.contains("new ClipboardView("));
        assertFalse(service.contains("new ClipboardStore("));
        assertTrue(controller.contains("capturePrimaryClipboard("));
        assertTrue(controller.contains("addPrimaryClipChangedListener("));
        assertTrue(controller.contains("new ClipboardView("));
        assertTrue(controller.contains("new ClipboardStore("));
    }

    @Test
    public void remoteInputExecutionStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String controller = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/RemoteInputController.java");

        assertTrue(service.contains("RemoteInputController"));
        assertFalse(service.contains("RemoteCommandResolver.resolve("));
        assertFalse(service.contains("RemoteCommandAction action"));
        assertFalse(service.contains("RemoteKeySession"));
        assertFalse(service.contains("sendRemoteImeToggle("));
        assertTrue(controller.contains("RemoteCommandResolver.resolve("));
        assertTrue(controller.contains("RemoteKeySession"));
        assertTrue(controller.contains("sendImeToggle("));
    }

    @Test
    public void keyboardCommandTargetStaysOutOfImeServiceBody() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String target = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardCommandTarget.java");

        assertTrue(service.contains("new S3KeyboardCommandTarget(this)"));
        assertFalse(service.contains("new KeyboardCommandDispatcher.Target()"));
        assertTrue(target.contains("extends KeyboardCommandDispatcher.Target"));
        assertTrue(target.contains("resetDoubleSpacePeriodState()"));
        assertTrue(target.contains("handleRemoteCommand("));
        assertTrue(target.contains("inputDingulContextualVowel("));
    }

    @Test
    public void assistRailCommandsHaveVisibleOrConventionalFallbacks() throws Exception {
        String service = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/S3KeyboardService.java");
        String dispatcher = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/ImeConnectionDispatcher.java");
        String strings = readWorkspaceFile("app/src/main/res/values/strings.xml");
        String development = readWorkspaceFile("docs/development.md");

        assertFalse(service.contains("TODO"));
        assertTrue(service.contains("R.string.voice_input_unavailable"));
        assertTrue(service.contains("ImeConnectionDispatcher.performUndo(inputConnection)"));
        assertTrue(dispatcher.contains("performContextMenuAction(android.R.id.undo)"));
        assertTrue(strings.contains("name=\"voice_input_unavailable\""));
        assertFalse(development.contains("safe stubs"));
        assertTrue(development.contains("voice should show the explicit"));
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
    public void keyboardSettingsFieldsStayRepresentedInTypedSections() throws Exception {
        String settings = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardSettings.java");
        String sections = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardSettingsSections.java");
        String model = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/KeyboardSettingsModel.java");
        String representedSettings = sections + "\n" + model;

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
