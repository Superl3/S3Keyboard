package com.superl3.s3keyboard;

interface TextActionProvider {
    interface Callback {
        void onResult(TextActionProviderResult result);
    }

    interface RequestHandle {
        void cancel();
    }

    String id();

    boolean isAvailable();

    RequestHandle request(TextActionProviderRequest request, Callback callback);
}
