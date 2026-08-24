package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class WatchRadialKeyboardView extends View {
    private static final int INVALID_POINTER_ID = -1;
    private static final int INVALID_UTILITY_INDEX = -1;
    private static final String LOG_TAG = "WatchRadialIme";

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF iconBounds = new RectF();
    private final KeyboardIconRegistry iconRegistry;
    private final WatchRadialInputController controller = new WatchRadialInputController();
    private final List<GestureKey> persistentUtilityKeys =
            KeyboardLayoutFactory.watchPersistentUtilityKeys();

    private KeyboardSettings settings = KeyboardSettings.defaults();
    private Consumer<String> gestureListener = value -> { };
    private Supplier<WatchRadialPage> preferredPage = () -> WatchRadialPage.CONSONANTS;
    private int activePointerId = INVALID_POINTER_ID;
    private float centerX;
    private float centerY;
    private float radialRadius;
    private float downX;
    private float downY;
    private float currentX;
    private float currentY;
    private int pressedKeyIndex = -1;
    private int pressedUtilityIndex = INVALID_UTILITY_INDEX;
    private boolean centerPressed;

    WatchRadialKeyboardView(Context context) {
        super(context);
        iconRegistry = new KeyboardIconRegistry(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription(context.getString(R.string.watch_radial_surface_description));
        setBackgroundColor(Color.TRANSPARENT);
    }

    void setSettings(KeyboardSettings settings) {
        this.settings = RuntimeDefaults.keyboardSettings(settings);
        invalidate();
        requestLayout();
    }

    void setOnKeyGestureListener(Consumer<String> listener) {
        gestureListener = listener == null ? value -> { } : listener;
    }

    void setPreferredPageSupplier(Supplier<WatchRadialPage> supplier) {
        preferredPage = supplier == null ? () -> WatchRadialPage.CONSONANTS : supplier;
    }

    void resetSession() {
        activePointerId = INVALID_POINTER_ID;
        pressedKeyIndex = -1;
        pressedUtilityIndex = INVALID_UTILITY_INDEX;
        centerPressed = false;
        controller.showPage(preferredPage.get());
        invalidate();
    }

    WatchRadialInputController.Stage stage() {
        return controller.stage();
    }

    WatchRadialPage page() {
        return controller.page();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeightDp = Math.max(300, Math.min(360, settings.hangulKeyboardHeightDp + 80));
        int desiredHeight = dp(desiredHeightDp);
        int measuredWidth = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int measuredHeight = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        updateGeometry();
        drawHeader(canvas);
        if (controller.page() == WatchRadialPage.VOWELS
                && controller.stage() == WatchRadialInputController.Stage.SELECT_KEY) {
            drawVowelColumn(canvas);
        } else if (controller.page() != WatchRadialPage.VOWELS) {
            drawRingKeys(canvas);
        }
        if (controller.page() == WatchRadialPage.VOWELS
                && controller.stage() == WatchRadialInputController.Stage.SELECT_KEY) {
            drawVowelPageSwitcher(canvas);
        } else {
            drawCenter(canvas);
        }
        if (controller.stage() == WatchRadialInputController.Stage.SELECT_ACTION) {
            drawActionHints(canvas);
        }
        drawPersistentUtilities(canvas);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (!BuildConfig.DEBUG) {
            return;
        }
        post(() -> {
            updateGeometry();
            int[] location = new int[2];
            getLocationOnScreen(location);
            Log.i(LOG_TAG, "bounds=" + location[0] + "," + location[1]
                    + "," + (location[0] + getWidth()) + "," + (location[1] + getHeight())
                    + " center=" + Math.round(centerX) + "," + Math.round(centerY)
                    + " radius=" + Math.round(radialRadius));
        });
    }

    private void drawHeader(Canvas canvas) {
        String stageLabel = getContext().getString(
                controller.stage() == WatchRadialInputController.Stage.SELECT_KEY
                        ? R.string.watch_radial_select_key
                        : R.string.watch_radial_select_action);
        drawText(canvas, stageLabel, centerX, dp(24), 13, settings.secondaryColor, true);
    }

    private void drawRingKeys(Canvas canvas) {
        List<GestureKey> keys = controller.keys();
        float itemRadius = Math.max(dp(22), radialRadius * 0.12f);
        for (int index = 0; index < keys.size(); index++) {
            float x = keyCenterX(index);
            float y = keyCenterY(index);
            boolean selected = controller.stage() == WatchRadialInputController.Stage.SELECT_ACTION
                    && controller.selectedIndex() == index;
            boolean pressed = pressedKeyIndex == index;
            int fill = selected
                    ? settings.accentKeyColor
                    : controller.page() == WatchRadialPage.COMMANDS
                            ? settings.functionKeyColor
                            : settings.keyIdleColor;
            if (pressed) {
                fill = settings.keyPressedColor;
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(withAlpha(fill, selected || pressed ? 236 : 204));
            canvas.drawCircle(x, y, itemRadius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(selected ? 2 : 1));
            paint.setColor(withAlpha(selected ? settings.accentColor : settings.borderColor, 190));
            canvas.drawCircle(x, y, itemRadius, paint);
            drawKeyContent(canvas, keys.get(index), x, y, itemRadius, selected);
        }
    }

    private void drawVowelColumn(Canvas canvas) {
        List<GestureKey> keys = controller.keys();
        float radius = vowelKeyRadius();
        for (int index = 0; index < keys.size(); index++) {
            float x = vowelKeyCenterX(index);
            float y = vowelKeyCenterY(index);
            boolean selected = controller.stage() == WatchRadialInputController.Stage.SELECT_ACTION
                    && controller.selectedIndex() == index;
            boolean pressed = pressedKeyIndex == index;
            int fill = selected
                    ? settings.accentKeyColor
                    : pressed ? settings.keyPressedColor : settings.keyIdleColor;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(withAlpha(fill, selected || pressed ? 236 : 210));
            canvas.drawCircle(x, y, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(selected ? 2 : 1));
            paint.setColor(withAlpha(selected ? settings.accentColor : settings.borderColor, 190));
            canvas.drawCircle(x, y, radius, paint);
            drawKeyContent(canvas, keys.get(index), x, y, radius, selected);
        }
    }

    private void drawVowelPageSwitcher(Canvas canvas) {
        float x = vowelPageSwitcherX();
        float radius = centerRadius() * 0.82f;
        int fill = centerPressed ? settings.keyPressedColor : settings.functionKeyColor;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(fill, centerPressed ? 232 : 210));
        canvas.drawCircle(x, centerY, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(withAlpha(settings.accentColor, 210));
        canvas.drawCircle(x, centerY, radius, paint);
        drawText(canvas, pageLabel(controller.page()), x, centerY,
                13, settings.accentColor, true);
    }

    private void drawPersistentUtilities(Canvas canvas) {
        for (int index = 0; index < persistentUtilityKeys.size(); index++) {
            RectF bounds = utilityBounds(index);
            GestureKey key = persistentUtilityKeys.get(index);
            boolean pressed = pressedUtilityIndex == index;
            int fill = pressed ? settings.keyPressedColor : settings.functionKeyColor;
            float radius = Math.min(dp(10), bounds.height() * 0.25f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(withAlpha(fill, pressed ? 240 : 222));
            canvas.drawRoundRect(bounds, radius, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            paint.setColor(withAlpha(settings.borderColor, 200));
            canvas.drawRoundRect(bounds, radius, radius, paint);
            drawKeyContent(
                    canvas,
                    key,
                    bounds.centerX(),
                    bounds.centerY(),
                    Math.min(bounds.width(), bounds.height()) * 0.42f,
                    false);
        }
    }

    private void drawKeyContent(
            Canvas canvas,
            GestureKey key,
            float x,
            float y,
            float itemRadius,
            boolean selected) {
        int color = selected ? settings.accentColor : settings.secondaryColor;
        if (key.icon != KeyIcon.NONE) {
            float iconSize = itemRadius * 0.88f;
            iconBounds.set(
                    x - iconSize / 2f,
                    y - iconSize / 2f,
                    x + iconSize / 2f,
                    y + iconSize / 2f);
            if (iconRegistry.draw(canvas, key.icon, iconBounds, color)) {
                return;
            }
        }
        drawText(canvas, key.label, x, y, selected ? 18 : 16, color, true);
    }

    private void drawCenter(Canvas canvas) {
        float radius = centerRadius();
        int fill = centerPressed ? settings.keyPressedColor : settings.functionKeyColor;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(withAlpha(fill, centerPressed ? 232 : 210));
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(2));
        paint.setColor(withAlpha(settings.accentColor, 210));
        canvas.drawCircle(centerX, centerY, radius, paint);

        if (controller.stage() == WatchRadialInputController.Stage.SELECT_KEY) {
            drawText(canvas, pageLabel(controller.page()), centerX, centerY,
                    14, settings.accentColor, true);
            return;
        }
        GestureKey selected = controller.selectedKey();
        if (selected != null) {
            drawKeyContent(canvas, selected, centerX, centerY, radius * 0.86f, true);
        }
    }

    private void drawActionHints(Canvas canvas) {
        GestureKey key = controller.selectedKey();
        if (key == null) {
            return;
        }
        float distance = radialRadius * 0.43f;
        int color = withAlpha(settings.accentColor, 230);
        drawText(canvas, displayValue(key, GestureAction.UP), centerX, centerY - distance,
                13, color, true);
        drawText(canvas, displayValue(key, GestureAction.DOWN), centerX, centerY + distance,
                13, color, true);
        drawText(canvas, displayValue(key, GestureAction.LEFT), centerX - distance, centerY,
                13, color, true);
        drawText(canvas, displayValue(key, GestureAction.RIGHT), centerX + distance, centerY,
                13, color, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            activePointerId = event.getPointerId(0);
            downX = currentX = event.getX(0);
            downY = currentY = event.getY(0);
            updatePressedTarget(currentX, currentY);
            invalidate();
            return true;
        }
        if (activePointerId == INVALID_POINTER_ID) {
            return true;
        }
        int pointerIndex = event.findPointerIndex(activePointerId);
        if (pointerIndex < 0) {
            clearPressState();
            return true;
        }
        currentX = event.getX(pointerIndex);
        currentY = event.getY(pointerIndex);
        if (action == MotionEvent.ACTION_MOVE) {
            invalidate();
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            clearPressState();
            return true;
        }
        if (action != MotionEvent.ACTION_UP) {
            return true;
        }

        handleRelease(currentX, currentY);
        clearPressState();
        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void handleRelease(float x, float y) {
        int utilityIndex = utilityIndexAt(x, y);
        if (pressedUtilityIndex >= 0 && pressedUtilityIndex == utilityIndex) {
            controller.cancelSelection();
            dispatchValue(persistentUtilityKeys.get(utilityIndex).tap);
            controller.showPage(preferredPage.get());
            return;
        }
        if (pressedUtilityIndex >= 0) {
            return;
        }
        if (controller.stage() == WatchRadialInputController.Stage.SELECT_KEY) {
            if (insidePageSwitcher(x, y)) {
                controller.cyclePage();
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                return;
            }
            int index = keyIndexAt(x, y);
            if (index >= 0 && controller.selectKey(index)) {
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
            return;
        }

        if (pressedKeyIndex >= 0 && !insideCenter(downX, downY)) {
            controller.selectKey(pressedKeyIndex);
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            return;
        }
        GestureAction action = WatchRadialInputController.actionForDelta(
                x - downX,
                y - downY,
                Math.max(dp(22), dp(settings.gestureThresholdDp) * 0.8f));
        WatchRadialPage sourcePage = controller.page();
        String value = controller.commit(action);
        dispatchValue(value);
        if (sourcePage != WatchRadialPage.COMMANDS) {
            controller.showPage(preferredPage.get());
        }
    }

    private void dispatchValue(String value) {
        if (value == null || value.isEmpty() || KeyboardCommands.CMD_NOOP.equals(value)) {
            return;
        }
        gestureListener.accept(value);
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    private void updatePressedTarget(float x, float y) {
        pressedUtilityIndex = utilityIndexAt(x, y);
        if (pressedUtilityIndex >= 0) {
            centerPressed = false;
            pressedKeyIndex = -1;
            return;
        }
        centerPressed = insidePageSwitcher(x, y);
        pressedKeyIndex = centerPressed ? -1 : keyIndexAt(x, y);
    }

    private void clearPressState() {
        activePointerId = INVALID_POINTER_ID;
        pressedKeyIndex = -1;
        pressedUtilityIndex = INVALID_UTILITY_INDEX;
        centerPressed = false;
        invalidate();
    }

    private void updateGeometry() {
        centerX = getWidth() / 2f;
        centerY = getHeight() * 0.47f;
        radialRadius = Math.min(getWidth() * 0.39f, getHeight() * 0.34f);
    }

    private boolean insidePageSwitcher(float x, float y) {
        if (controller.page() == WatchRadialPage.VOWELS
                && controller.stage() == WatchRadialInputController.Stage.SELECT_KEY) {
            return Math.hypot(x - vowelPageSwitcherX(), y - centerY)
                    <= centerRadius() * 0.98f;
        }
        return insideCenter(x, y);
    }

    private boolean insideCenter(float x, float y) {
        return Math.hypot(x - centerX, y - centerY) <= centerRadius() * 1.18f;
    }

    private boolean insideRing(float x, float y) {
        float distance = (float) Math.hypot(x - centerX, y - centerY);
        return distance >= radialRadius * 0.42f && distance <= radialRadius * 1.24f;
    }

    private float centerRadius() {
        return Math.max(dp(34), radialRadius * 0.24f);
    }

    private int keyIndexAt(float x, float y) {
        if (controller.page() == WatchRadialPage.VOWELS) {
            if (controller.stage() != WatchRadialInputController.Stage.SELECT_KEY) {
                return -1;
            }
            for (int index = 0; index < controller.keys().size(); index++) {
                if (Math.hypot(x - vowelKeyCenterX(index), y - vowelKeyCenterY(index))
                        <= vowelKeyRadius() * 1.22f) {
                    return index;
                }
            }
            return -1;
        }
        return insideRing(x, y) ? nearestRingIndex(x - centerX, y - centerY) : -1;
    }

    private float vowelColumnX() {
        return centerX + radialRadius * 0.12f;
    }

    private float vowelKeyCenterX(int index) {
        return index < 4
                ? vowelColumnX()
                : vowelColumnX() + vowelKeyRadius() * 2.35f;
    }

    private float vowelPageSwitcherX() {
        return centerX - radialRadius * 0.58f;
    }

    private float vowelKeyRadius() {
        return Math.max(dp(22), Math.min(dp(29), radialRadius * 0.18f));
    }

    private float vowelKeyCenterY(int index) {
        int rowIndex = index < 4 ? index : index - 3;
        float top = dp(50);
        float bottom = utilityBounds(0).top - dp(8);
        float rowHeight = Math.max(dp(42), (bottom - top) / 4f);
        return top + rowHeight * (rowIndex + 0.5f);
    }

    private int utilityIndexAt(float x, float y) {
        for (int index = 0; index < persistentUtilityKeys.size(); index++) {
            if (utilityBounds(index).contains(x, y)) {
                return index;
            }
        }
        return INVALID_UTILITY_INDEX;
    }

    private RectF utilityBounds(int index) {
        float gap = dp(8);
        float side = dp(12);
        float height = dp(48);
        float bottom = getHeight() - dp(8);
        float deleteWidth = Math.max(dp(52), getWidth() * 0.19f);
        float spaceWidth = Math.max(dp(104), getWidth() * 0.42f);
        float groupWidth = spaceWidth + gap + deleteWidth;
        float left = Math.max(side, (getWidth() - groupWidth) / 2f);
        if (index == 0) {
            return new RectF(left, bottom - height, left + spaceWidth, bottom);
        }
        return new RectF(left + spaceWidth + gap, bottom - height,
                left + groupWidth, bottom);
    }

    private float keyCenterX(int index) {
        return centerX + (float) Math.cos(angleFor(index)) * radialRadius * 0.76f;
    }

    private float keyCenterY(int index) {
        return centerY + (float) Math.sin(angleFor(index)) * radialRadius * 0.76f;
    }

    private int nearestRingIndex(float dx, float dy) {
        double normalized = Math.atan2(dy, dx) + Math.PI / 2.0;
        if (normalized < 0) {
            normalized += Math.PI * 2.0;
        }
        return ((int) Math.round(normalized / (Math.PI / 4.0))) % 8;
    }

    private float angleFor(int index) {
        return (float) (-Math.PI / 2.0 + index * Math.PI / 4.0);
    }

    private String pageLabel(WatchRadialPage page) {
        if (page == WatchRadialPage.VOWELS) {
            return getContext().getString(R.string.watch_radial_page_vowels);
        }
        if (page == WatchRadialPage.COMMANDS) {
            return getContext().getString(R.string.watch_radial_page_commands);
        }
        return getContext().getString(R.string.watch_radial_page_consonants);
    }

    private String displayValue(GestureKey key, GestureAction action) {
        String value = key.valueFor(action);
        if (KeyboardCommands.CMD_DELETE.equals(value)) {
            return "⌫";
        }
        if (KeyboardCommands.CMD_SPACE.equals(value)) {
            return "␣";
        }
        if (KeyboardCommands.CMD_ENTER.equals(value)) {
            return "↵";
        }
        if (KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(value)) {
            return "한/영";
        }
        if (KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(value)) {
            return "ㅣ";
        }
        if (KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(value)) {
            return "ㅡ";
        }
        if (value == null || value.startsWith("__")) {
            return key.label;
        }
        return value;
    }

    private void drawText(
            Canvas canvas,
            String text,
            float x,
            float y,
            float sizeSp,
            int color,
            boolean bold) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(KeyboardTypefaceCatalog.typefaceFor(
                getContext(),
                settings.fontFamily,
                bold,
                false));
        paint.setTextSize(sp(sizeSp));
        paint.setColor(color);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        canvas.drawText(text == null ? "" : text, x, y - (metrics.ascent + metrics.descent) / 2f, paint);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(
                Math.max(0, Math.min(255, alpha)),
                Color.red(color),
                Color.green(color),
                Color.blue(color));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
