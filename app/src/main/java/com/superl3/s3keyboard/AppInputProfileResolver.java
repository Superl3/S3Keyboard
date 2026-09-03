package com.superl3.s3keyboard;

final class AppInputProfileResolver {
    private AppInputProfileResolver() {
    }

    static AppInputProfile resolve(
            String packageName,
            EditorInputPolicy basePolicy,
            boolean remoteModeRequested) {
        EditorInputPolicy policy = RuntimeDefaults.editorInputPolicy(basePolicy);
        String normalizedPackage = AppPackageCatalog.normalizePackageName(packageName);
        if (policy.password) {
            return AppInputProfileCatalog.password();
        }
        if (policy.numberLike || policy.rawKeyInput) {
            return AppInputProfileCatalog.number();
        }
        if (remoteModeRequested) {
            return AppInputProfileCatalog.remote(normalizedPackage);
        }
        if (policy.uriLike) {
            return AppInputProfileCatalog.url();
        }
        if (policy.emailLike) {
            return AppInputProfileCatalog.email();
        }
        if (policy.webEditLike) {
            return AppInputProfileCatalog.webEdit();
        }
        if (AppPackageCatalog.isWebViewPackage(normalizedPackage)) {
            return AppInputProfileCatalog.webView();
        }
        if (policy.searchAction || AppPackageCatalog.isBrowserPackage(normalizedPackage)) {
            return AppInputProfileCatalog.browserSearch(policy.searchAction);
        }
        if (AppPackageCatalog.isMessagingPackage(normalizedPackage)) {
            return AppInputProfileCatalog.messaging();
        }
        return AppInputProfile.STANDARD;
    }

}
