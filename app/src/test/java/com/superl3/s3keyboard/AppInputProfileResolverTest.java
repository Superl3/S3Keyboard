package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import org.junit.Test;

public final class AppInputProfileResolverTest {
    @Test
    public void knownRemotePackagesStayStandardUnlessRemoteProfileIsRequested() {
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        AppInputProfile profile = AppInputProfileResolver.resolve("com.limelight", base, false);

        assertEquals("standard", profile.id);
        assertFalse(profile.remoteMode);
    }

    @Test
    public void requestedRemotePackagesForceRemoteAsciiNoComposingProfile() {
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        assertRemoteProfile(
                AppInputProfileResolver.resolve("com.limelight", base, true),
                base,
                "remote_moonlight",
                "remote_auto_package");
        assertRemoteProfile(
                AppInputProfileResolver.resolve("tv.parsec.client", base, true),
                base,
                "remote_parsec",
                "remote_auto_package");
        assertRemoteProfile(
                AppInputProfileResolver.resolve("com.microsoft.rdc.android", base, true),
                base,
                "remote_microsoft_rdp",
                "remote_auto_package");
        assertRemoteProfile(
                AppInputProfileResolver.resolve("com.microsoft.rdc.androidx", base, true),
                base,
                "remote_microsoft_rdp",
                "remote_auto_package");
        assertRemoteProfile(
                AppInputProfileResolver.resolve("com.google.chromeremotedesktop", base, true),
                base,
                "remote_chrome_remote_desktop",
                "remote_auto_package");
        assertRemoteProfile(
                AppInputProfileResolver.resolve("com.valvesoftware.steamlink", base, true),
                base,
                "remote_steam_link",
                "remote_auto_package");
        assertRemoteProfile(
                AppInputProfileResolver.resolve("com.anydesk.anydeskandroid", base, true),
                base,
                "remote_anydesk",
                "remote_auto_package");
        assertRemoteProfile(
                AppInputProfileResolver.resolve("com.teamviewer.teamviewer.market.mobile", base, true),
                base,
                "remote_teamviewer",
                "remote_auto_package");
        assertRemoteProfile(
                AppInputProfileResolver.resolve("com.teamviewer.quicksupport.market", base, true),
                base,
                "remote_teamviewer",
                "remote_auto_package");
    }

    @Test
    public void remoteProfileAndReportFamilyShareRemoteAppCatalog() {
        assertEquals("moonlight", RemoteCompatibilityReport.appFamily("com.limelight"));
        assertEquals(
                "remote_moonlight",
                AppInputProfileResolver.resolve(
                        "com.limelight",
                        EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT),
                        true).id);
        assertTrue(RemoteAppCatalog.defaultAutoPackageList().contains("com.limelight"));
        assertTrue(RemoteAppCatalog.defaultAutoPackageList().contains("tv.parsec.client"));
    }

    @Test
    public void requestedAppFamiliesHaveDistinctRuntimeInputPolicies() {
        EditorInputPolicy text = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);
        EditorInputPolicy password = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        assertPolicy(
                AppInputProfileResolver.resolve("com.android.chrome", text, false).apply(text),
                true,
                false,
                true,
                true);
        assertPolicy(
                AppInputProfileResolver.resolve("com.google.android.webview", text, false).apply(text),
                true,
                false,
                false,
                true);
        assertPolicy(
                AppInputProfileResolver.resolve("com.kakao.talk", text, false).apply(text),
                false,
                false,
                true,
                true);
        assertPolicy(
                AppInputProfileResolver.resolve("com.example.login", password, false).apply(password),
                true,
                true,
                false,
                false);
        assertRemoteProfile(
                AppInputProfileResolver.resolve("tv.parsec.client", text, true),
                text,
                "remote_parsec",
                "remote_auto_package");
        assertRemoteProfile(
                AppInputProfileResolver.resolve("com.limelight", text, true),
                text,
                "remote_moonlight",
                "remote_auto_package");
    }

    @Test
    public void userMatchedRemotePackageUsesRemoteProfileEvenWhenPackageIsUnknown() {
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        AppInputProfile profile = AppInputProfileResolver.resolve("com.example.remoteclient", base, true);

        assertRemoteProfile(profile, base, "remote_desktop", "remote_auto_package");
        assertEquals("remote_auto_package", profile.source);
    }

    @Test
    public void passwordProfileKeepsSensitiveFieldPolicyExplicit() {
        EditorInputPolicy base = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        AppInputProfile profile = AppInputProfileResolver.resolve("com.example.login", base, false);
        EditorInputPolicy applied = profile.apply(base);

        assertEquals("password", profile.id);
        assertEquals("password_field", profile.source);
        assertFalse(profile.remoteMode);
        assertTrue(applied.preferAsciiLayout);
        assertTrue(applied.forceNumberRow);
        assertFalse(applied.allowComposingText);
        assertFalse(applied.allowTextConveniences);
    }

    @Test
    public void browserSearchProfilePrefersAsciiButKeepsTextConveniences() {
        EditorInputPolicy base = EditorInputPolicy.fromEditorInfo(
                InputType.TYPE_CLASS_TEXT,
                EditorInfo.IME_ACTION_SEARCH);

        for (String packageName : AppPackageCatalog.browserPackages()) {
            assertBrowserSearchProfile(
                    AppInputProfileResolver.resolve(packageName, base, false),
                    base);
        }
    }

    @Test
    public void urlAndEmailProfilesPreferAsciiWithoutForcingNumberRow() {
        EditorInputPolicy url = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        EditorInputPolicy email = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        AppInputProfile urlProfile = AppInputProfileResolver.resolve("com.example.browser", url, false);
        EditorInputPolicy appliedUrl = urlProfile.apply(url);

        assertEquals("url", urlProfile.id);
        assertEquals("url_field", urlProfile.source);
        assertTrue(appliedUrl.preferAsciiLayout);
        assertFalse(appliedUrl.forceNumberRow);
        assertFalse(appliedUrl.allowComposingText);
        assertFalse(appliedUrl.allowTextConveniences);

        AppInputProfile emailProfile = AppInputProfileResolver.resolve("com.example.mail", email, false);
        EditorInputPolicy appliedEmail = emailProfile.apply(email);

        assertEquals("email", emailProfile.id);
        assertEquals("email_field", emailProfile.source);
        assertTrue(appliedEmail.preferAsciiLayout);
        assertFalse(appliedEmail.forceNumberRow);
        assertFalse(appliedEmail.allowComposingText);
        assertFalse(appliedEmail.allowTextConveniences);
    }

    @Test
    public void numberProfileKeepsDedicatedNumericSurfaceWithoutRemoteMode() {
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_NUMBER);

        AppInputProfile profile = AppInputProfileResolver.resolve("com.example.checkout", base, false);
        EditorInputPolicy applied = profile.apply(base);

        assertEquals("number", profile.id);
        assertEquals("number_field", profile.source);
        assertFalse(profile.remoteMode);
        assertNull(profile.preferAsciiLayout);
        assertFalse(applied.forceNumberRow);
        assertFalse(applied.allowComposingText);
        assertFalse(applied.allowTextConveniences);
    }

    @Test
    public void webViewProfilePrefersAsciiAndDisablesComposing() {
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        for (String packageName : AppPackageCatalog.webViewPackages()) {
            AppInputProfile profile = AppInputProfileResolver.resolve(packageName, base, false);
            EditorInputPolicy applied = profile.apply(base);

            assertEquals("webview", profile.id);
            assertEquals("webview_package", profile.source);
            assertTrue(applied.preferAsciiLayout);
            assertFalse(applied.forceNumberRow);
            assertFalse(applied.allowComposingText);
            assertTrue(applied.allowTextConveniences);
        }
    }

    @Test
    public void webEditProfileUsesCommitOnlyTextPath() {
        EditorInputPolicy base = EditorInputPolicy.fromInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);

        AppInputProfile profile = AppInputProfileResolver.resolve("com.example.webapp", base, false);
        EditorInputPolicy applied = profile.apply(base);

        assertEquals("web_edit", profile.id);
        assertEquals("web_edit_field", profile.source);
        assertFalse(profile.remoteMode);
        assertTrue(applied.preferAsciiLayout);
        assertFalse(applied.forceNumberRow);
        assertFalse(applied.allowComposingText);
        assertFalse(applied.allowTextConveniences);
    }

    @Test
    public void messagingProfileKeepsHangulComposingAvailable() {
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        AppInputProfile profile = AppInputProfileResolver.resolve(
                "com.google.android.apps.messaging",
                base,
                false);

        assertMessagingProfile(profile, base);
        for (String packageName : AppPackageCatalog.messagingPackages()) {
            assertMessagingProfile(
                    AppInputProfileResolver.resolve(packageName, base, false),
                    base);
        }
    }

    @Test
    public void appProfilePackageListsLiveInCatalog() {
        assertTrue(AppPackageCatalog.isBrowserPackage(" com.android.chrome "));
        assertTrue(AppPackageCatalog.isWebViewPackage("com.google.android.webview.beta"));
        assertTrue(AppPackageCatalog.isMessagingPackage("com.kakao.talk"));
        assertFalse(AppPackageCatalog.isBrowserPackage("com.android.chrome.devtools"));
    }

    @Test
    public void appProfilePolicyValuesLiveInCatalog() {
        assertEquals("password", AppInputProfileCatalog.password().id);
        assertEquals("number", AppInputProfileCatalog.number().id);
        assertEquals("url", AppInputProfileCatalog.url().id);
        assertEquals("email", AppInputProfileCatalog.email().id);
        assertEquals("web_edit", AppInputProfileCatalog.webEdit().id);
        assertEquals("webview", AppInputProfileCatalog.webView().id);
        assertEquals("browser_package", AppInputProfileCatalog.browserSearch(false).source);
        assertEquals("search_action", AppInputProfileCatalog.browserSearch(true).source);
        assertEquals("messaging", AppInputProfileCatalog.messaging().id);
        assertEquals("remote_moonlight", AppInputProfileCatalog.remote("com.limelight").id);
    }

    private static void assertBrowserSearchProfile(AppInputProfile profile, EditorInputPolicy base) {
        EditorInputPolicy applied = profile.apply(base);

        assertEquals("browser_search", profile.id);
        assertTrue(
                "unexpected browser/search source: " + profile.source,
                "browser_package".equals(profile.source) || "search_action".equals(profile.source));
        assertFalse(profile.remoteMode);
        assertTrue(applied.preferAsciiLayout);
        assertFalse(applied.forceNumberRow);
        assertTrue(applied.allowTextConveniences);
    }

    private static void assertMessagingProfile(AppInputProfile profile, EditorInputPolicy base) {
        EditorInputPolicy applied = profile.apply(base);

        assertEquals("messaging", profile.id);
        assertEquals("messaging_package", profile.source);
        assertFalse(profile.remoteMode);
        assertFalse(applied.preferAsciiLayout);
        assertFalse(applied.forceNumberRow);
        assertTrue(applied.allowComposingText);
        assertTrue(applied.allowTextConveniences);
    }

    private static void assertRemoteProfile(
            AppInputProfile profile,
            EditorInputPolicy base,
            String expectedId,
            String expectedSource) {
        EditorInputPolicy applied = profile.apply(base);

        assertEquals(expectedId, profile.id);
        assertEquals(expectedSource, profile.source);
        assertTrue(profile.remoteMode);
        assertTrue(applied.preferAsciiLayout);
        assertTrue(applied.forceNumberRow);
        assertFalse(applied.allowComposingText);
        assertFalse(applied.allowTextConveniences);
    }

    private static void assertPolicy(
            EditorInputPolicy policy,
            boolean preferAsciiLayout,
            boolean forceNumberRow,
            boolean allowComposingText,
            boolean allowTextConveniences) {
        assertEquals(preferAsciiLayout, policy.preferAsciiLayout);
        assertEquals(forceNumberRow, policy.forceNumberRow);
        assertEquals(allowComposingText, policy.allowComposingText);
        assertEquals(allowTextConveniences, policy.allowTextConveniences);
    }
}
