package com.superl3.s3keyboard;

enum WatchRadialPage {
    CONSONANTS,
    VOWELS,
    COMMANDS;

    WatchRadialPage next() {
        switch (this) {
            case CONSONANTS:
                return VOWELS;
            case VOWELS:
                return COMMANDS;
            case COMMANDS:
            default:
                return CONSONANTS;
        }
    }
}
