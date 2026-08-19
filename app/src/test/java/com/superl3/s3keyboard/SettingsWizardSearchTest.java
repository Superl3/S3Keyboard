package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SettingsWizardSearchTest {
    @Test
    public void normalizeIgnoresCaseAndWhitespace() {
        assertEquals("remote모드", SettingsWizardSearch.normalize("  Remote 모드  "));
    }

    @Test
    public void emptyQueryMatchesEverySettingsStep() {
        assertTrue(SettingsWizardSearch.matches("레이아웃 높이 여백", null));
        assertTrue(SettingsWizardSearch.matches("레이아웃 높이 여백", "   "));
    }

    @Test
    public void matchesKoreanAndEnglishKeywords() {
        String searchableText = "원격/Windows Parsec Moonlight Ctrl Alt";

        assertTrue(SettingsWizardSearch.matches(searchableText, "원격"));
        assertTrue(SettingsWizardSearch.matches(searchableText, "moon light"));
        assertTrue(SettingsWizardSearch.matches(searchableText, "CTRL"));
        assertFalse(SettingsWizardSearch.matches(searchableText, "글꼴"));
    }

    @Test
    public void multipleTermsCanMatchInAnyOrderAcrossVisibleLabels() {
        String searchableText = "레이아웃 키보드 크기 한글 높이 영문 높이 좌우 패딩";

        assertTrue(SettingsWizardSearch.matches(searchableText, "높이 키보드"));
        assertTrue(SettingsWizardSearch.matches(searchableText, "영문/높이"));
        assertTrue(SettingsWizardSearch.matches(searchableText, "패딩, 좌우"));
        assertFalse(SettingsWizardSearch.matches(searchableText, "높이 원격"));
    }
}
