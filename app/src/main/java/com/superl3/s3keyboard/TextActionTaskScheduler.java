package com.superl3.s3keyboard;

interface TextActionTaskScheduler {
    interface CancelHandle {
        void cancel();
    }

    CancelHandle schedule(Runnable runnable, long delayMs);
}
