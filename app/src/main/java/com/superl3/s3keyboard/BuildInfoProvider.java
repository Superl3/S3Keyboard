package com.superl3.s3keyboard;

import android.content.Context;

final class BuildInfoProvider {
    private BuildInfoProvider() {
    }

    static String summary(Context context) {
        if (context == null) {
            return "";
        }
        return context.getString(
                R.string.build_info_summary_format,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                BuildConfig.GIT_COMMIT,
                BuildConfig.BUILD_TIME);
    }
}
