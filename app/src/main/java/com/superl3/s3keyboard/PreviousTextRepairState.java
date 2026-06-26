package com.superl3.s3keyboard;

final class PreviousTextRepairState {
    private boolean suppressNextRepair;

    void markDelete() {
        suppressNextRepair = true;
    }

    boolean consumeSuppressNextRepair() {
        boolean result = suppressNextRepair;
        suppressNextRepair = false;
        return result;
    }

    void reset() {
        suppressNextRepair = false;
    }
}
