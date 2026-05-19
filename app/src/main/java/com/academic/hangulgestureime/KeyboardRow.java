package com.academic.hangulgestureime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class KeyboardRow {
    final List<GestureKey> keys;
    final int baseUnits;

    KeyboardRow(List<GestureKey> keys, int baseUnits) {
        this.keys = Collections.unmodifiableList(new ArrayList<>(keys));
        this.baseUnits = Math.max(1, baseUnits);
    }
}
