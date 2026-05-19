package com.academic.hangulgestureime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public final class ProductionReadinessConfigTest {
    @Test
    public void subtypeDeclaresAsciiCapableWithLegacyExtraValue() throws Exception {
        String methodXml = readWorkspaceFile("app/src/main/res/xml/method.xml");

        assertTrue(methodXml.contains("android:isAsciiCapable=\"true\""));
        assertTrue(methodXml.contains("android:imeSubtypeExtraValue=\"AsciiCapable\""));
    }

    @Test
    public void stringResourcesDoNotContainKnownMojibakeMarkers() throws Exception {
        String stringsXml = readWorkspaceFile("app/src/main/res/values/strings.xml");

        assertFalse(stringsXml.contains("\uFFFD"));
        assertFalse(stringsXml.contains("?ㅻ"));
        assertFalse(stringsXml.contains("袁"));
        assertFalse(stringsXml.contains("湲"));
        assertFalse(stringsXml.contains("媛"));
        assertFalse(stringsXml.contains("蹂"));
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
                "app/src/main/java/com/academic/hangulgestureime/MainActivity.java");

        assertTrue(mainActivity.contains("EXTRA_DEMO_SETTINGS"));
        assertTrue(mainActivity.contains("isDebuggable()"));
        assertTrue(mainActivity.contains("debugDemoIntent"));
    }

    private String readWorkspaceFile(String relativePath) throws IOException {
        Path root = findWorkspaceRoot();
        return new String(Files.readAllBytes(root.resolve(relativePath)), StandardCharsets.UTF_8);
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
