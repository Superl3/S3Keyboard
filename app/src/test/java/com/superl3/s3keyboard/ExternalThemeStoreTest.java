package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public final class ExternalThemeStoreTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void loadFromDirectoryReadsValidJsonThemesInFileOrder() throws Exception {
        File themes = folder.newFolder("themes");
        write(new File(themes, "b-theme.json"), "{\"schemaVersion\":1,\"name\":\"Beta\"}");
        write(new File(themes, "a-theme.json"), "{\"schemaVersion\":1,\"name\":\"Alpha\"}");
        write(new File(themes, "broken.json"), "not-json");
        write(new File(themes, "ignored.txt"), "{\"schemaVersion\":1,\"name\":\"Ignored\"}");

        UserThemeStore.UserTheme[] loaded = ExternalThemeStore.loadFromDirectory(themes);

        assertEquals(2, loaded.length);
        assertEquals("Alpha", loaded[0].name);
        assertEquals("Beta", loaded[1].name);
        assertTrue(loaded[0].external);
        assertTrue(loaded[0].id.startsWith(ExternalThemeStore.EXTERNAL_THEME_PREFIX));
        assertTrue(loaded[0].sourcePath.endsWith("a-theme.json"));
    }

    @Test
    public void missingDirectoryReturnsEmptyList() {
        UserThemeStore.UserTheme[] loaded = ExternalThemeStore.loadFromDirectory(
                new File(folder.getRoot(), "missing"));

        assertEquals(0, loaded.length);
    }

    @Test
    public void oversizedThemeIsIgnoredBeforeParsing() throws Exception {
        File themes = folder.newFolder("large-themes");
        File oversized = new File(themes, "oversized.json");
        FileOutputStream output = new FileOutputStream(oversized);
        try {
            output.write(new byte[(int) ExternalThemeStore.MAX_THEME_FILE_BYTES + 1]);
        } finally {
            output.close();
        }

        assertEquals(0, ExternalThemeStore.loadFromDirectory(themes).length);
    }

    @Test
    public void directoryScanHasABoundedFileCount() throws Exception {
        File themes = folder.newFolder("many-themes");
        for (int i = 0; i < ExternalThemeStore.MAX_THEME_FILES + 5; i++) {
            write(new File(themes, String.format("%03d.json", i)),
                    "{\"schemaVersion\":1,\"name\":\"Theme " + i + "\"}");
        }

        assertEquals(ExternalThemeStore.MAX_THEME_FILES,
                ExternalThemeStore.loadFromDirectory(themes).length);
    }

    private static void write(File file, String value) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        try {
            writer.write(value);
        } finally {
            writer.close();
        }
    }
}
