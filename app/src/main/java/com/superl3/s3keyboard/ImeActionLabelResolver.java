package com.superl3.s3keyboard;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

final class ImeActionLabelResolver {
    private ImeActionLabelResolver() {
    }

    static ResolvedImeAction defaultAction() {
        return new ResolvedImeAction(R.string.ime_action_send, EditorInfo.IME_ACTION_SEND, true);
    }

    static ResolvedImeAction resolve(EditorInfo info) {
        if (info == null) {
            return defaultAction();
        }
        return resolve(info.imeOptions, info.inputType);
    }

    static ResolvedImeAction resolve(int imeOptions, int inputType) {
        boolean noEnterAction = (imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0;
        boolean multiLine = (inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
        int action = imeOptions & EditorInfo.IME_MASK_ACTION;

        if (noEnterAction || action == EditorInfo.IME_ACTION_NONE
                || action == EditorInfo.IME_ACTION_UNSPECIFIED) {
            if (multiLine || noEnterAction) {
                return new ResolvedImeAction(R.string.ime_action_newline, EditorInfo.IME_ACTION_NONE, false);
            }
            return defaultAction();
        }

        switch (action) {
            case EditorInfo.IME_ACTION_SEARCH:
                return new ResolvedImeAction(R.string.ime_action_search, action, true);
            case EditorInfo.IME_ACTION_DONE:
                return new ResolvedImeAction(R.string.ime_action_done, action, true);
            case EditorInfo.IME_ACTION_NEXT:
                return new ResolvedImeAction(R.string.ime_action_next, action, true);
            case EditorInfo.IME_ACTION_GO:
                return new ResolvedImeAction(R.string.ime_action_go, action, true);
            case EditorInfo.IME_ACTION_SEND:
                return new ResolvedImeAction(R.string.ime_action_send, action, true);
            default:
                return defaultAction();
        }
    }
}
