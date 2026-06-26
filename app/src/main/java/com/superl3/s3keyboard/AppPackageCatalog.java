package com.superl3.s3keyboard;

final class AppPackageCatalog {
    private static final String[] BROWSER_PACKAGES = {
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.chrome.canary",
            "com.sec.android.app.sbrowser",
            "com.microsoft.emmx",
            "com.brave.browser",
            "org.mozilla.firefox",
            "org.mozilla.firefox_beta",
            "com.opera.browser"
    };

    private static final String[] WEBVIEW_PACKAGES = {
            "com.google.android.webview",
            "com.android.webview",
            "com.google.android.webview.beta",
            "com.google.android.webview.dev",
            "org.chromium.webview_shell"
    };

    private static final String[] MESSAGING_PACKAGES = {
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.kakao.talk",
            "org.telegram.messenger",
            "org.telegram.messenger.web",
            "com.whatsapp",
            "jp.naver.line.android",
            "org.thoughtcrime.securesms",
            "com.discord",
            "com.facebook.orca"
    };

    private AppPackageCatalog() {
    }

    static boolean isBrowserPackage(String packageName) {
        return contains(BROWSER_PACKAGES, packageName);
    }

    static boolean isWebViewPackage(String packageName) {
        return contains(WEBVIEW_PACKAGES, packageName);
    }

    static boolean isMessagingPackage(String packageName) {
        return contains(MESSAGING_PACKAGES, packageName);
    }

    static String[] browserPackages() {
        return BROWSER_PACKAGES.clone();
    }

    static String[] webViewPackages() {
        return WEBVIEW_PACKAGES.clone();
    }

    static String[] messagingPackages() {
        return MESSAGING_PACKAGES.clone();
    }

    private static boolean contains(String[] packages, String packageName) {
        String normalized = normalizePackage(packageName);
        for (String candidate : packages) {
            if (candidate.equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizePackage(String packageName) {
        return packageName == null ? "" : packageName.trim();
    }
}
