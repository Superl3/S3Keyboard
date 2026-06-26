package com.superl3.s3keyboard;

final class AppInputProfileCatalog {
    private AppInputProfileCatalog() {
    }

    static AppInputProfile password() {
        return new AppInputProfile("password", false, true, true, false, false, "password_field");
    }

    static AppInputProfile number() {
        return new AppInputProfile("number", false, null, false, false, false, "number_field");
    }

    static AppInputProfile url() {
        return new AppInputProfile("url", false, true, false, false, false, "url_field");
    }

    static AppInputProfile email() {
        return new AppInputProfile("email", false, true, false, false, false, "email_field");
    }

    static AppInputProfile webEdit() {
        return new AppInputProfile("web_edit", false, true, false, false, false, "web_edit_field");
    }

    static AppInputProfile webView() {
        return new AppInputProfile("webview", false, true, false, false, true, "webview_package");
    }

    static AppInputProfile browserSearch(boolean searchAction) {
        return new AppInputProfile(
                "browser_search",
                false,
                true,
                false,
                null,
                true,
                searchAction ? "search_action" : "browser_package");
    }

    static AppInputProfile messaging() {
        return new AppInputProfile("messaging", false, false, false, true, true, "messaging_package");
    }

    static AppInputProfile remote(String packageName) {
        return new AppInputProfile(
                RemoteAppCatalog.remoteProfileIdForPackage(packageName),
                true,
                true,
                true,
                false,
                false,
                "remote_auto_package");
    }
}
