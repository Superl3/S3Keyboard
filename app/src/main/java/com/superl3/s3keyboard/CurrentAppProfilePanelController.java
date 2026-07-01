package com.superl3.s3keyboard;

import android.content.Context;

final class CurrentAppProfilePanelController {
    private CurrentAppProfilePanelController() {
    }

    static String summary(Context context) {
        if (context == null) {
            return "";
        }
        KeyboardSettings settings = KeyboardPreferences.load(context);
        return context.getString(
                R.string.current_app_profile_summary_format,
                context.getString(settings.remoteModeEnabled ? R.string.state_on : R.string.state_off),
                context.getString(KeyboardPreferences.loadRemoteAutoModeEnabled(context)
                        ? R.string.state_on
                        : R.string.state_off),
                packageListOrDash(KeyboardPreferences.loadRemoteAutoModePackages(context)),
                packageListOrDash(KeyboardPreferences.loadAppProfileAsciiPackages(context)),
                packageListOrDash(KeyboardPreferences.loadAppProfileNumberRowPackages(context)),
                packageListOrDash(KeyboardPreferences.loadAppProfileNoComposingPackages(context)),
                packageListOrDash(KeyboardPreferences.loadAppProfileNoTextConveniencesPackages(context)));
    }

    private static String packageListOrDash(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }
}
