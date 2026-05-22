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

    private static void write(File file, String value) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        try {
            writer.write(value);
        } finally {
            writer.close();
        }
    }
}
