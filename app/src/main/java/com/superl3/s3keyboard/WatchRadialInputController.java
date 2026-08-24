package com.superl3.s3keyboard;

import java.util.List;

final class WatchRadialInputController {
    enum Stage {
        SELECT_KEY,
        SELECT_ACTION
    }

    private WatchRadialPage page = WatchRadialPage.CONSONANTS;
    private Stage stage = Stage.SELECT_KEY;
    private int selectedIndex = -1;

    WatchRadialPage page() {
        return page;
    }

    Stage stage() {
        return stage;
    }

    int selectedIndex() {
        return selectedIndex;
    }

    List<GestureKey> keys() {
        return KeyboardLayoutFactory.watchRadialKeys(page);
    }

    GestureKey selectedKey() {
        List<GestureKey> keys = keys();
        return selectedIndex >= 0 && selectedIndex < keys.size()
                ? keys.get(selectedIndex)
                : null;
    }

    void cyclePage() {
        page = page.next();
        clearSelection();
    }

    void showPage(WatchRadialPage nextPage) {
        page = nextPage == null ? WatchRadialPage.CONSONANTS : nextPage;
        clearSelection();
    }

    boolean selectKey(int index) {
        if (index < 0 || index >= keys().size()) {
            return false;
        }
        selectedIndex = index;
        stage = Stage.SELECT_ACTION;
        return true;
    }

    String commit(GestureAction action) {
        GestureKey key = selectedKey();
        if (key == null) {
            return null;
        }
        String value = key.valueFor(action == null ? GestureAction.TAP : action);
        clearSelection();
        return value;
    }

    void cancelSelection() {
        clearSelection();
    }

    static GestureAction actionForDelta(float dx, float dy, float threshold) {
        if (Math.hypot(dx, dy) < Math.max(0f, threshold)) {
            return GestureAction.TAP;
        }
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx < 0 ? GestureAction.LEFT : GestureAction.RIGHT;
        }
        return dy < 0 ? GestureAction.UP : GestureAction.DOWN;
    }

    private void clearSelection() {
        selectedIndex = -1;
        stage = Stage.SELECT_KEY;
    }
}
