package com.superl3.s3keyboard;

final class SwipeCandidate {
    final String word;
    final float score;

    SwipeCandidate(String word, float score) {
        this.word = word == null ? "" : word;
        this.score = score;
    }
}
