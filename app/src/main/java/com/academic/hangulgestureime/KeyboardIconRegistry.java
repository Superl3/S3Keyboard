package com.academic.hangulgestureime;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

final class KeyboardIconRegistry {
    private final Context context;
    private final SparseArray<Drawable> cache = new SparseArray<>();

    KeyboardIconRegistry(Context context) {
        this.context = context;
    }

    boolean draw(Canvas canvas, int icon, RectF bounds, int color) {
        int resId = drawableFor(icon);
        if (resId == 0) {
            return false;
        }

        Drawable drawable = cache.get(icon);
        if (drawable == null) {
            drawable = context.getResources().getDrawable(resId, null).mutate();
            cache.put(icon, drawable);
        }

        drawable.setTint(color);
        drawable.setBounds(
                Math.round(bounds.left),
                Math.round(bounds.top),
                Math.round(bounds.right),
                Math.round(bounds.bottom));
        drawable.draw(canvas);
        return true;
    }

    static int drawableFor(int icon) {
        switch (icon) {
            case KeyIcon.OPTIONS:
                return R.drawable.ic_keyboard_options;
            case KeyIcon.RESERVED:
                return R.drawable.ic_keyboard_reserved;
            case KeyIcon.SPACE:
                return R.drawable.ic_keyboard_space;
            case KeyIcon.LANGUAGE:
                return R.drawable.ic_keyboard_language;
            case KeyIcon.ENTER:
                return R.drawable.ic_keyboard_enter;
            case KeyIcon.SEARCH:
                return R.drawable.ic_keyboard_search;
            case KeyIcon.DONE:
                return R.drawable.ic_keyboard_done;
            case KeyIcon.NEXT:
                return R.drawable.ic_keyboard_next;
            case KeyIcon.SHIFT:
                return R.drawable.ic_keyboard_shift;
            case KeyIcon.CAPS_LOCK:
                return R.drawable.ic_keyboard_caps_lock;
            case KeyIcon.BACKSPACE:
                return R.drawable.ic_keyboard_backspace;
            case KeyIcon.HIDE:
                return R.drawable.ic_keyboard_hide;
            case KeyIcon.SETTINGS:
                return R.drawable.ic_keyboard_settings;
            case KeyIcon.MOVE_LEFT:
                return R.drawable.ic_keyboard_move_left;
            case KeyIcon.MOVE_RIGHT:
                return R.drawable.ic_keyboard_move_right;
            case KeyIcon.KEYBOARD:
                return R.drawable.ic_keyboard_keyboard;
            case KeyIcon.RESET:
                return R.drawable.ic_keyboard_reset;
            default:
                return 0;
        }
    }
}
