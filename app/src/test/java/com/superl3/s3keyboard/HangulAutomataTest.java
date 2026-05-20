package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class HangulAutomataTest {
    @Test
    public void composesConsonantVowelFinal() {
        assertEquals("한", type("ㅎㅏㄴ"));
    }

    @Test
    public void standaloneVowelStaysJamoWithoutImplicitIeung() {
        assertEquals("ㅏ", type("ㅏ"));
        assertEquals("ㅔ", type("ㅔ"));
    }

    @Test
    public void vowelAfterIncompleteConsonantComposesSyllable() {
        assertEquals("게", type("ㄱㅔ"));
    }

    @Test
    public void standaloneVowelsCanComposeBeforeConsonantStarts() {
        assertEquals("ㅔㄱ", type("ㅓㅣㄱ"));
    }

    @Test
    public void movesFinalConsonantBeforeFollowingVowel() {
        assertEquals("가나", type("ㄱㅏㄴㅏ"));
    }

    @Test
    public void composesCompoundVowel() {
        assertEquals("과", type("ㄱㅗㅏ"));
    }

    @Test
    public void composesRepeatedSyllablesWithoutSplittingSecondInitial() {
        assertEquals("심심해", type("ㅅㅣㅁㅅㅣㅁㅎㅐ"));
        assertEquals("심심심심심", type("ㅅㅣㅁㅅㅣㅁㅅㅣㅁㅅㅣㅁㅅㅣㅁ"));
        assertEquals("모하는뎅", type("ㅁㅗㅎㅏㄴㅡㄴㄷㅔㅇ"));
    }

    @Test
    public void decomposesCommittedOpenSyllableForFinalRepair() {
        assertEquals("ㄷㅔ", HangulAutomata.decomposeOpenSyllable('데'));
    }

    @Test
    public void repeatedSimpleVowelsStayAsRepeatedInput() {
        assertEquals("가아", type("ㄱㅏㅏ"));
        assertEquals("거어", type("ㄱㅓㅓ"));
        assertEquals("고오", type("ㄱㅗㅗ"));
        assertEquals("구우", type("ㄱㅜㅜ"));
        assertEquals("게에", type("ㄱㅔㅔ"));
        assertEquals("개애", type("ㄱㅐㅐ"));
        assertEquals("ㅜㅜ", type("ㅜㅜ"));
    }

    @Test
    public void exposesCurrentVowelForDingulContextualTap() {
        HangulAutomata automata = new HangulAutomata();

        automata.input('ㄱ');
        automata.input('ㅏ');
        assertEquals('ㅏ', automata.currentVowelWithoutFinal());

        automata.input('ㄴ');
        assertEquals('\0', automata.currentVowelWithoutFinal());
    }

    @Test
    public void commitsImpossibleInitialClusterAsNewInput() {
        assertEquals("ㄱ나", type("ㄱㄴㅏ"));
    }

    @Test
    public void repeatedNieunStaysTwoStandaloneConsonants() {
        assertEquals("\u3134\u3134", type("\u3134\u3134"));
    }

    private String type(String input) {
        HangulAutomata automata = new HangulAutomata();
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            output.append(automata.input(input.charAt(i)));
        }
        output.append(automata.flush());
        return output.toString();
    }
}
