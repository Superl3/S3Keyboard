package com.superl3.s3keyboard;

import android.content.Context;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

final class AndroidImeStatus {
    enum State {
        DISABLED,
        ENABLED,
        SELECTED
    }

    private AndroidImeStatus() {
    }

    static State resolve(Context context) {
        if (context == null) {
            return State.DISABLED;
        }
        String packageName = context.getPackageName();
        String serviceName = S3KeyboardService.class.getName();
        String selectedId = null;
        try {
            selectedId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.DEFAULT_INPUT_METHOD);
        } catch (RuntimeException ignored) {
            // Keep the setup screen usable on vendor builds with restricted settings providers.
        }
        if (matchesSelectedComponent(packageName, serviceName, selectedId)) {
            return State.SELECTED;
        }

        InputMethodManager manager = context.getSystemService(InputMethodManager.class);
        List<InputMethodInfo> enabledMethods = null;
        try {
            enabledMethods = manager == null ? null : manager.getEnabledInputMethodList();
        } catch (RuntimeException ignored) {
            // A missing status is safer than blocking the settings screen.
        }
        if (enabledMethods != null) {
            for (InputMethodInfo method : enabledMethods) {
                if (method != null && matchesService(
                        packageName,
                        serviceName,
                        method.getPackageName(),
                        method.getServiceName())) {
                    return State.ENABLED;
                }
            }
        }
        return State.DISABLED;
    }

    static boolean matchesSelectedComponent(
            String packageName,
            String serviceName,
            String selectedComponentId) {
        if (selectedComponentId == null || selectedComponentId.isEmpty()) {
            return false;
        }
        int separator = selectedComponentId.indexOf('/');
        if (separator <= 0 || separator >= selectedComponentId.length() - 1) {
            return false;
        }
        return matchesService(
                packageName,
                serviceName,
                selectedComponentId.substring(0, separator),
                selectedComponentId.substring(separator + 1));
    }

    static boolean matchesService(
            String packageName,
            String serviceName,
            String candidatePackage,
            String candidateService) {
        if (packageName == null
                || serviceName == null
                || candidatePackage == null
                || candidateService == null
                || !packageName.equals(candidatePackage)) {
            return false;
        }
        return normalizeClassName(packageName, serviceName).equals(
                normalizeClassName(candidatePackage, candidateService));
    }

    private static String normalizeClassName(String packageName, String className) {
        if (className.startsWith(".")) {
            return packageName + className;
        }
        if (className.indexOf('.') < 0) {
            return packageName + "." + className;
        }
        return className;
    }
}
