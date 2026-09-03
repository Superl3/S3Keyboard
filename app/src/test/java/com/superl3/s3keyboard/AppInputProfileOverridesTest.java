package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.text.InputType;

import org.junit.Test;

public final class AppInputProfileOverridesTest {
    @Test
    public void emptyOverridesReturnOriginalProfile() {
        AppInputProfile profile = AppInputProfileCatalog.messaging();

        AppInputProfile applied = AppInputProfileOverrides.EMPTY.apply("com.example.app", profile);

        assertSame(profile, applied);
    }

    @Test
    public void packageMatchingUsesExactTokensOnly() {
        AppInputProfileOverrides overrides = new AppInputProfileOverrides(
                "com.example.editor, com.example.other",
                "",
                "",
                "");
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        AppInputProfile exact = overrides.apply("com.example.editor", AppInputProfile.STANDARD);
        AppInputProfile partial = overrides.apply("com.example.editor.beta", AppInputProfile.STANDARD);

        assertTrue(exact.apply(base).preferAsciiLayout);
        assertSame(AppInputProfile.STANDARD, partial);
    }

    @Test
    public void asciiAndNoComposingOverrideMessagingPolicy() {
        AppInputProfileOverrides overrides = new AppInputProfileOverrides(
                "com.example.chat",
                "",
                "com.example.chat",
                "");
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        AppInputProfile profile = overrides.apply("com.example.chat", AppInputProfileCatalog.messaging());
        EditorInputPolicy applied = profile.apply(base);

        assertEquals("messaging", profile.id);
        assertTrue(profile.source.contains("messaging_package"));
        assertTrue(profile.source.contains("user_app_profile_override"));
        assertTrue(applied.preferAsciiLayout);
        assertFalse(applied.allowComposingText);
        assertTrue(applied.allowTextConveniences);
    }

    @Test
    public void numberRowOverrideForcesNumberRowOnly() {
        AppInputProfileOverrides overrides = new AppInputProfileOverrides(
                "",
                "com.example.remote",
                "",
                "");
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        EditorInputPolicy applied = overrides
                .apply("com.example.remote", AppInputProfile.STANDARD)
                .apply(base);

        assertFalse(applied.preferAsciiLayout);
        assertTrue(applied.forceNumberRow);
        assertTrue(applied.allowComposingText);
        assertTrue(applied.allowTextConveniences);
    }

    @Test
    public void noTextConveniencesOverrideDisablesOnlyTextConveniences() {
        AppInputProfileOverrides overrides = new AppInputProfileOverrides(
                "",
                "",
                "",
                "com.example.web");
        EditorInputPolicy base = EditorInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT);

        EditorInputPolicy applied = overrides
                .apply("com.example.web", AppInputProfile.STANDARD)
                .apply(base);

        assertFalse(applied.preferAsciiLayout);
        assertFalse(applied.forceNumberRow);
        assertTrue(applied.allowComposingText);
        assertFalse(applied.allowTextConveniences);
    }
    @Test
    public void versionedJsonRoundTripsPerAppOverride() {
        AppInputProfileOverride value = AppInputProfileOverride.AUTO
                .withKeyboardMode(KeyboardMode.ENGLISH)
                .withNumberRowVisible(Boolean.FALSE)
                .withAllowComposingText(Boolean.FALSE)
                .withAllowTextConveniences(Boolean.TRUE)
                .withRemoteMode(Boolean.FALSE);
        AppInputProfileOverrides source = AppInputProfileOverrides.EMPTY
                .withOverride("com.example.editor", value);

        AppInputProfileOverrides decoded = AppInputProfileOverrides.decode(
                source.encode(), "", "", "", "");
        AppInputProfileOverride result = decoded.forPackage("com.example.editor");

        assertEquals(1, decoded.storedOverrideCount());
        assertEquals(KeyboardMode.ENGLISH, result.keyboardMode);
        assertEquals(Boolean.FALSE, result.numberRowVisible);
        assertEquals(Boolean.FALSE, result.allowComposingText);
        assertEquals(Boolean.TRUE, result.allowTextConveniences);
        assertEquals(Boolean.FALSE, result.remoteMode);
    }


    @Test
    public void malformedOrFutureSchemaFallsBackToSafeEmptyRecords() {
        AppInputProfileOverrides malformed = AppInputProfileOverrides.decode(
                "not-json", "", "", "", "");
        AppInputProfileOverrides future = AppInputProfileOverrides.decode(
                "{\"version\":99,\"apps\":{\"com.example\":{\"remote\":true}}}",
                "", "", "", "");

        assertEquals(0, malformed.storedOverrideCount());
        assertEquals(0, future.storedOverrideCount());
        assertTrue(malformed.forPackage("com.example").isAuto());
        assertTrue(future.forPackage("com.example").isAuto());
    }


}
