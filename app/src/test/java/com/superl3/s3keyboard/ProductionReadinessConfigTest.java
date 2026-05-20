package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class ProductionReadinessConfigTest {
    private static final String[] MOJIBAKE_MARKERS = {
            "\uFFFD",
            "?ㅻ",
            "袁",
            "湲",
            "媛",
            "蹂",
            "寃",
            "뚯",
            "덉",
            "쒖",
            "珥",
            "紐",
            "쇱",
            "ㅻ",
            "⑤",
            "곸",
            "낅",
            "젰",
            "꾩",
            "슦"
    };

    @Test
    public void subtypeDeclaresAsciiCapableWithLegacyExtraValue() throws Exception {
        String methodXml = readWorkspaceFile("app/src/main/res/xml/method.xml");

        assertTrue(methodXml.contains("android:isAsciiCapable=\"true\""));
        assertTrue(methodXml.contains("android:imeSubtypeExtraValue=\"AsciiCapable\""));
    }

    @Test
    public void mainTextSourcesDoNotContainKnownMojibakeMarkers() throws Exception {
        Path root = findWorkspaceRoot();
        List<Path> files = new ArrayList<>();
        collectTextFiles(root.resolve("app/src/main/java"), files);
        collectTextFiles(root.resolve("app/src/main/res/values"), files);

        for (Path file : files) {
            String text = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            for (String marker : MOJIBAKE_MARKERS) {
                assertFalse(
                        "Mojibake marker '" + marker + "' found in " + root.relativize(file),
                        text.contains(marker));
            }
        }
    }

    @Test
    public void manifestKeepsKeyboardLocalWithoutNetworkPermission() throws Exception {
        String manifest = readWorkspaceFile("app/src/main/AndroidManifest.xml");

        assertTrue(manifest.contains("android:allowBackup=\"false\""));
        assertFalse(manifest.contains("android.permission.INTERNET"));
    }

    @Test
    public void releaseBuildHasClosedBetaHardeningDecisions() throws Exception {
        String buildGradle = readWorkspaceFile("app/build.gradle");

        assertTrue(buildGradle.contains("versionCode 1"));
        assertTrue(buildGradle.contains("versionName \"0.1.0\""));
        assertTrue(buildGradle.contains("minifyEnabled true"));
        assertTrue(buildGradle.contains("shrinkResources true"));
        assertTrue(buildGradle.contains("HANGUL_IME_KEYSTORE"));
    }

    @Test
    public void demoIntentOverridesAreDebugGated() throws Exception {
        String mainActivity = readWorkspaceFile(
                "app/src/main/java/com/superl3/s3keyboard/MainActivity.java");

        assertTrue(mainActivity.contains("EXTRA_DEMO_SETTINGS"));
        assertTrue(mainActivity.contains("isDebuggable()"));
        assertTrue(mainActivity.contains("debugDemoIntent"));
    }

    private String readWorkspaceFile(String relativePath) throws IOException {
        Path root = findWorkspaceRoot();
        return new String(Files.readAllBytes(root.resolve(relativePath)), StandardCharsets.UTF_8);
    }

    private void collectTextFiles(Path directory, List<Path> files) throws IOException {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.endsWith(".java") || name.endsWith(".xml");
                    })
                    .forEach(files::add);
        }
    }

    private Path findWorkspaceRoot() {
        Path current = Paths.get("").toAbsolutePath();
        for (Path candidate = current; candidate != null; candidate = candidate.getParent()) {
            if (Files.exists(candidate.resolve("settings.gradle"))
                    && Files.exists(candidate.resolve("app/src/main/AndroidManifest.xml"))) {
                return candidate;
            }
        }
        throw new AssertionError("Workspace root not found from " + current);
    }
}
