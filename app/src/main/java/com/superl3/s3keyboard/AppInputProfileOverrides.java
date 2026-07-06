package com.superl3.s3keyboard;

final class AppInputProfileOverrides {
    static final AppInputProfileOverrides EMPTY = new AppInputProfileOverrides("", "", "", "");

    final String asciiPackages;
    final String numberRowPackages;
    final String noComposingPackages;
    final String noTextConveniencesPackages;

    AppInputProfileOverrides(
            String asciiPackages,
            String numberRowPackages,
            String noComposingPackages,
            String noTextConveniencesPackages) {
        this.asciiPackages = RuntimeDefaults.stringOrDefault(asciiPackages, "");
        this.numberRowPackages = RuntimeDefaults.stringOrDefault(numberRowPackages, "");
        this.noComposingPackages = RuntimeDefaults.stringOrDefault(noComposingPackages, "");
        this.noTextConveniencesPackages = RuntimeDefaults.stringOrDefault(noTextConveniencesPackages, "");
    }

    AppInputProfile apply(String packageName, AppInputProfile profile) {
        AppInputProfile safeProfile = RuntimeDefaults.appInputProfile(profile);
        Boolean preferAscii = KeyboardPreferences.packageListContains(asciiPackages, packageName)
                ? Boolean.TRUE
                : null;
        Boolean forceNumberRow = KeyboardPreferences.packageListContains(numberRowPackages, packageName)
                ? Boolean.TRUE
                : null;
        Boolean allowComposing = KeyboardPreferences.packageListContains(noComposingPackages, packageName)
                ? Boolean.FALSE
                : null;
        Boolean allowTextConveniences =
                KeyboardPreferences.packageListContains(noTextConveniencesPackages, packageName)
                        ? Boolean.FALSE
                        : null;
        if (preferAscii == null
                && forceNumberRow == null
                && allowComposing == null
                && allowTextConveniences == null) {
            return safeProfile;
        }
        return safeProfile.withPolicyOverrides(
                preferAscii,
                forceNumberRow,
                allowComposing,
                allowTextConveniences,
                "user_app_profile_override");
    }
}
