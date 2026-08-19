package com.superl3.s3keyboard;

final class OneFingerPracticeSession {
    private static final Lesson[] LESSONS = {
            new Lesson(
                    R.string.one_finger_practice_lesson_center_vowel,
                    R.string.one_finger_practice_guide_center_vowel,
                    "어아오우"),
            new Lesson(
                    R.string.one_finger_practice_lesson_top_vowel,
                    R.string.one_finger_practice_guide_top_vowel,
                    "워와외위"),
            new Lesson(
                    R.string.one_finger_practice_lesson_wide_vowel,
                    R.string.one_finger_practice_guide_wide_vowel,
                    "으에애왜웨"),
            new Lesson(
                    R.string.one_finger_practice_lesson_dot_vowel,
                    R.string.one_finger_practice_guide_dot_vowel,
                    "요야유여"),
            new Lesson(
                    R.string.one_finger_practice_lesson_special_rail,
                    R.string.one_finger_practice_guide_special_rail,
                    "?.,/"),
            new Lesson(
                    R.string.one_finger_practice_lesson_reentry,
                    R.string.one_finger_practice_guide_reentry,
                    "만나서 반가워요"),
            new Lesson(
                    R.string.one_finger_practice_lesson_sentence,
                    R.string.one_finger_practice_guide_sentence,
                    "타각기 어때요")
    };

    private int lessonIndex;

    Lesson currentLesson() {
        return LESSONS[lessonIndex];
    }

    int currentIndex() {
        return lessonIndex;
    }

    int lessonCount() {
        return LESSONS.length;
    }

    boolean hasPrevious() {
        return lessonIndex > 0;
    }

    boolean hasNext() {
        return lessonIndex < LESSONS.length - 1;
    }

    void previous() {
        if (hasPrevious()) {
            lessonIndex--;
        }
    }

    void next() {
        if (hasNext()) {
            lessonIndex++;
        }
    }

    Progress evaluate(CharSequence input) {
        return evaluate(input, -1);
    }

    Progress evaluate(CharSequence input, int composingStart) {
        String typed = input == null ? "" : input.toString();
        String target = currentLesson().target;
        int limit = Math.min(typed.length(), target.length());
        int matchedLength = 0;
        while (matchedLength < limit
                && typed.charAt(matchedLength) == target.charAt(matchedLength)) {
            matchedLength++;
        }
        boolean complete = typed.equals(target);
        boolean rawMismatch = !complete
                && (matchedLength < typed.length() || typed.length() > target.length());
        boolean compositionPending = rawMismatch
                && composingStart >= 0
                && matchedLength >= composingStart;
        return new Progress(
                target,
                matchedLength,
                complete,
                rawMismatch && !compositionPending,
                compositionPending);
    }

    static final class Lesson {
        final int labelResId;
        final int guideResId;
        final String target;

        Lesson(int labelResId, int guideResId, String target) {
            this.labelResId = labelResId;
            this.guideResId = guideResId;
            this.target = target;
        }
    }

    static final class Progress {
        final String target;
        final int matchedLength;
        final boolean complete;
        final boolean mismatch;
        final boolean compositionPending;

        Progress(
                String target,
                int matchedLength,
                boolean complete,
                boolean mismatch,
                boolean compositionPending) {
            this.target = target;
            this.matchedLength = matchedLength;
            this.complete = complete;
            this.mismatch = mismatch;
            this.compositionPending = compositionPending;
        }
    }
}
