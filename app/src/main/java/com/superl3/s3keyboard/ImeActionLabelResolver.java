package com.superl3.s3keyboard;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

final class ImeActionLabelResolver {
    private ImeActionLabelResolver() {
    }

    static ResolvedImeAction defaultAction() {
        return new ResolvedImeAction("전송", EditorInfo.IME_ACTION_SEND, true);
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

        if (noEnterAction || action == EditorInfo.IME_ACTION_NONE || action == EditorInfo.IME_ACTION_UNSPECIFIED) {
            if (multiLine || noEnterAction) {
                return new ResolvedImeAction("줄바꿈", EditorInfo.IME_ACTION_NONE, false);
            }
            return defaultAction();
        }

        switch (action) {
            case EditorInfo.IME_ACTION_SEARCH:
                return new ResolvedImeAction("검색", action, true);
            case EditorInfo.IME_ACTION_DONE:
                return new ResolvedImeAction("완료", action, true);
            case EditorInfo.IME_ACTION_NEXT:
                return new ResolvedImeAction("다음", action, true);
            case EditorInfo.IME_ACTION_GO:
                return new ResolvedImeAction("이동", action, true);
            case EditorInfo.IME_ACTION_SEND:
                return new ResolvedImeAction("전송", action, true);
            default:
                return defaultAction();
        }
    }
}
