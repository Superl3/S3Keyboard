package com.superl3.s3keyboard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class AndroidImeStatusTest {
    private static final String PACKAGE_NAME = "com.superl3.s3keyboard";
    private static final String SERVICE_NAME = PACKAGE_NAME + ".S3KeyboardService";

    @Test
    public void enabledServiceMatchesFullRelativeAndShortClassNames() {
        assertTrue(AndroidImeStatus.matchesService(
                PACKAGE_NAME,
                SERVICE_NAME,
                PACKAGE_NAME,
                SERVICE_NAME));
        assertTrue(AndroidImeStatus.matchesService(
                PACKAGE_NAME,
                SERVICE_NAME,
                PACKAGE_NAME,
                ".S3KeyboardService"));
        assertTrue(AndroidImeStatus.matchesService(
                PACKAGE_NAME,
                SERVICE_NAME,
                PACKAGE_NAME,
                "S3KeyboardService"));
    }

    @Test
    public void anotherPackageOrServiceDoesNotMatch() {
        assertFalse(AndroidImeStatus.matchesService(
                PACKAGE_NAME,
                SERVICE_NAME,
                "com.example.keyboard",
                SERVICE_NAME));
        assertFalse(AndroidImeStatus.matchesService(
                PACKAGE_NAME,
                SERVICE_NAME,
                PACKAGE_NAME,
                PACKAGE_NAME + ".OtherService"));
    }

    @Test
    public void selectedComponentParsesAndroidFlattenedIds() {
        assertTrue(AndroidImeStatus.matchesSelectedComponent(
                PACKAGE_NAME,
                SERVICE_NAME,
                PACKAGE_NAME + "/.S3KeyboardService"));
        assertFalse(AndroidImeStatus.matchesSelectedComponent(
                PACKAGE_NAME,
                SERVICE_NAME,
                "com.example.keyboard/.KeyboardService"));
        assertFalse(AndroidImeStatus.matchesSelectedComponent(
                PACKAGE_NAME,
                SERVICE_NAME,
                ""));
    }
}
