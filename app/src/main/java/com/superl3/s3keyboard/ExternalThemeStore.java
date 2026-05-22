package com.superl3.s3keyboard;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class ExternalThemeStore {
    static final String EXTERNAL_THEME_PREFIX = "external-theme:";

    private static final String PREF_NAME = "keyboard_external_themes";
    private static final String KEY_DIRECTORY_PATH = "external_theme_directory_path";

    private ExternalThemeStore() {
    }

    static UserThemeStore.UserTheme[] load(Context context) {
        return loadFromDirectory(new File(loadDirectoryPath(context)));
    }

    static String loadDirectoryPath(Context context) {
        if (context == null) {
            return "";
        }
        String fallback = defaultDirectoryPath(context);
        return prefs(context).getString(KEY_DIRECTORY_PATH, fallback);
    }

    static void saveDirectoryPath(Context context, String path) {
        if (context == null) {
            return;
        }
        String cleanPath = path == null ? "" : path.trim();
        if (cleanPath.isEmpty()) {
            cleanPath = defaultDirectoryPath(context);
        }
        prefs(context).edit()
                .putString(KEY_DIRECTORY_PATH, cleanPath)
                .apply();
        ensureDirectory(new File(cleanPath));
    }

    static String defaultDirectoryPath(Context context) {
        File base = context.getExternalFilesDir(null);
        if (base == null) {
            base = context.getFilesDir();
        }
        return new File(base, "themes").getAbsolutePath();
    }

    static boolean ensureThemeDirectory(Context context) {
        if (context == null) {
            return false;
        }
        return ensureDirectory(new File(loadDirectoryPath(context)));
    }

    static UserThemeStore.UserTheme[] loadFromDirectory(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return new UserThemeStore.UserTheme[0];
        }
        File[] files = directory.listFiles(file ->
                file != null
                        && file.isFile()
                        && file.getName().toLowerCase(Locale.US).endsWith(".json"));
        if (files == null || files.length == 0) {
            return new UserThemeStore.UserTheme[0];
        }
        Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        List<UserThemeStore.UserTheme> themes = new ArrayList<>();
        for (File file : files) {
            UserThemeStore.UserTheme theme = themeFromFile(file);
            if (theme != null) {
                themes.add(theme);
            }
        }
        return themes.toArray(new UserThemeStore.UserTheme[0]);
    }

    static UserThemeStore.UserTheme find(Context context, String id) {
        if (id == null || !id.startsWith(EXTERNAL_THEME_PREFIX)) {
            return null;
        }
        for (UserThemeStore.UserTheme theme : load(context)) {
            if (id.equals(theme.id)) {
                return theme;
            }
        }
        return null;
    }

    private static UserThemeStore.UserTheme themeFromFile(File file) {
        try {
            String json = readUtf8(file);
            KeyboardThemeJson.importTheme(KeyboardSettings.defaults(), json);
            JSONObject object = new JSONObject(json);
            String name = object.optString("name", fileNameWithoutExtension(file));
            return new UserThemeStore.UserTheme(
                    stableId(file),
                    name,
                    json,
                    true,
                    file.getAbsolutePath());
        } catch (IOException | JSONException | IllegalArgumentException exception) {
            return null;
        }
    }

    private static String readUtf8(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            return output.toString("UTF-8");
        } finally {
            input.close();
        }
    }

    private static String stableId(File file) {
        String path;
        try {
            path = file.getCanonicalPath();
        } catch (IOException exception) {
            path = file.getAbsolutePath();
        }
        return EXTERNAL_THEME_PREFIX + sha256Prefix(path, 16);
    }

    private static String sha256Prefix(String value, int length) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes("UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (byte item : bytes) {
                builder.append(String.format(Locale.US, "%02x", item & 0xFF));
            }
            return builder.substring(0, Math.min(length, builder.length()));
        } catch (NoSuchAlgorithmException | IOException exception) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private static String fileNameWithoutExtension(File file) {
        String name = file == null ? "External Theme" : file.getName();
        int dot = name.lastIndexOf('.');
        return dot <= 0 ? name : name.substring(0, dot);
    }

    private static boolean ensureDirectory(File directory) {
        return directory != null && (directory.isDirectory() || directory.mkdirs());
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
