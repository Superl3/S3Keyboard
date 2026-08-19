package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public final class EnglishQwertyCorrectionEngineTest {
    @Test
    public void exactTypoSuggestionsWin() {
        List<EnglishQwertyCorrectionEngine.Candidate> candidates =
                EnglishQwertyCorrectionEngine.DEFAULT.suggest("teh", 3);

        assertFalse(candidates.isEmpty());
        assertEquals("the", candidates.get(0).text);
        assertTrue(candidates.get(0).exactCorrection);
    }

    @Test
    public void preservesCapitalizationInSuggestion() {
        List<EnglishQwertyCorrectionEngine.Candidate> candidates =
                EnglishQwertyCorrectionEngine.DEFAULT.suggest("Teh", 3);

        assertFalse(candidates.isEmpty());
        assertEquals("The", candidates.get(0).text);
    }

    @Test
    public void missingApostropheCorrectionKeepsTheIntendedMeaning() {
        assertEquals("you're", EnglishQwertyCorrectionEngine.DEFAULT.autoCorrection("youre"));
        assertEquals("You're", EnglishQwertyCorrectionEngine.DEFAULT.autoCorrection("Youre"));
    }

    @Test
    public void usesQwertyAdjacentSubstitutionForFatFingerCandidate() {
        List<EnglishQwertyCorrectionEngine.Candidate> candidates =
                EnglishQwertyCorrectionEngine.DEFAULT.suggest("jeyboard", 3);

        assertFalse(candidates.isEmpty());
        assertEquals("keyboard", candidates.get(0).text);
    }

    @Test
    public void sameScoreSuggestionsPreferShorterCandidate() {
        EnglishQwertyCorrectionEngine engine =
                new EnglishQwertyCorrectionEngine(new String[] {"tests", "tent"});

        List<EnglishQwertyCorrectionEngine.Candidate> candidates = engine.suggest("test", 2);

        assertEquals(2, candidates.size());
        assertEquals("tent", candidates.get(0).text);
        assertEquals("tests", candidates.get(1).text);
    }

    @Test
    public void keepsUnrelatedInputWithoutSuggestions() {
        List<EnglishQwertyCorrectionEngine.Candidate> candidates =
                EnglishQwertyCorrectionEngine.DEFAULT.suggest("zxq", 3);

        assertTrue(candidates.isEmpty());
    }
}
