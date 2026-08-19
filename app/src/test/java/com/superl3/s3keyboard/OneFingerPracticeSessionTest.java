package com.superl3.s3keyboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class OneFingerPracticeSessionTest {
    @Test
    public void lessonsCoverEveryDingulVowelFamilyBeforeSentences() {
        OneFingerPracticeSession session = new OneFingerPracticeSession();

        assertEquals("어아오우", session.currentLesson().target);
        assertTrue(session.currentLesson().guideResId != 0);
        assertFalse(session.hasPrevious());
        assertTrue(session.hasNext());

        session.next();
        assertEquals("워와외위", session.currentLesson().target);

        session.next();
        assertEquals("으에애왜웨", session.currentLesson().target);

        session.next();
        assertEquals("요야유여", session.currentLesson().target);

        session.next();
        assertEquals("?.,/", session.currentLesson().target);

        session.next();
        assertEquals("만나서 반가워요", session.currentLesson().target);

        session.next();
        assertEquals("타각기 어때요", session.currentLesson().target);
        assertTrue(session.currentLesson().guideResId != 0);
        assertFalse(session.hasNext());
    }

    @Test
    public void progressDistinguishesPrefixMismatchAndCompletion() {
        OneFingerPracticeSession session = new OneFingerPracticeSession();

        OneFingerPracticeSession.Progress empty = session.evaluate("");
        OneFingerPracticeSession.Progress mismatch = session.evaluate("어아오우나");
        OneFingerPracticeSession.Progress complete = session.evaluate("어아오우");

        assertEquals(0, empty.matchedLength);
        assertFalse(empty.mismatch);
        assertFalse(empty.complete);
        assertEquals(4, mismatch.matchedLength);
        assertTrue(mismatch.mismatch);
        assertFalse(mismatch.complete);
        assertTrue(complete.complete);
        assertFalse(complete.mismatch);
    }

    @Test
    public void activeHangulCompositionDoesNotFlashAsAMistake() {
        OneFingerPracticeSession session = new OneFingerPracticeSession();

        OneFingerPracticeSession.Progress composing = session.evaluate("ㅇ", 0);
        OneFingerPracticeSession.Progress committedMismatch = session.evaluate("악", -1);

        assertTrue(composing.compositionPending);
        assertFalse(composing.mismatch);
        assertFalse(committedMismatch.compositionPending);
        assertTrue(committedMismatch.mismatch);
    }

    @Test
    public void lessonNavigationStaysInsideTheAvailableRange() {
        OneFingerPracticeSession session = new OneFingerPracticeSession();

        session.previous();
        assertEquals(0, session.currentIndex());

        for (int i = 0; i < 10; i++) {
            session.next();
        }
        assertEquals(session.lessonCount() - 1, session.currentIndex());
    }
}
