package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class HangulKeyboardView extends View {
    private final List<KeySlot> keySlots = new ArrayList<>();
    private final Paint keyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint modifierIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint depthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final KeyboardIconRegistry iconRegistry;
    private final List<TouchState> activeTouches = new ArrayList<>();
    private final List<PendingTouchOutput> pendingTouchOutputs = new ArrayList<>();
    private final KeyboardFeedback feedback = new KeyboardFeedback(this);
    private final RepeatController repeatController = new RepeatController(this, new RepeatController.Callback() {
        @Override
        public void onRepeat(String value) {
            emitValue(value);
        }
    });

    private KeyboardSettings settings = KeyboardSettings.defaults();
    private List<KeyboardRow> rows = Collections.emptyList();
    private TouchBiasStore touchBiasStore;
    private TouchBiasStore.Bias touchBias = TouchBiasStore.Bias.none();
    private OnKeyGestureListener listener;
    private OnPreviewKeySelectionListener previewKeySelectionListener;
    private boolean englishShiftActive;
    private boolean englishCapsLocked;
    private boolean compactPreviewRendering;
    private boolean showHangulConsonantSlideHints = true;
    private boolean showHangulVowelSlideHints = true;
    private boolean showSpacebarSlideHints = true;
    private OnPreviewOverlayListener previewOverlayListener;
    private long nextTouchSequence;
    private TouchSample lastTextTouchSample;
    private boolean differentiatedHapticEnabled = true;
    private boolean touchBiasAutoCorrectionEnabled = true;

    public HangulKeyboardView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setContentDescription(context.getString(R.string.keyboard_view_description));
        iconRegistry = new KeyboardIconRegistry(context);
        touchBiasStore = new TouchBiasStore(context);
        touchBias = touchBiasStore.load();
        differentiatedHapticEnabled = KeyboardPreferences.loadDifferentiatedHapticEnabled(context);
        touchBiasAutoCorrectionEnabled = KeyboardPreferences.loadTouchBiasAutoCorrectionEnabled(context);
        initPaints();
        setSettings(KeyboardPreferences.load(context));
    }

    public void setOnKeyGestureListener(OnKeyGestureListener listener) {
        this.listener = listener;
    }

    void setOnPreviewKeySelectionListener(OnPreviewKeySelectionListener listener) {
        previewKeySelectionListener = listener;
    }

    void setOnPreviewOverlayListener(OnPreviewOverlayListener listener) {
        previewOverlayListener = listener;
    }

    void setSettings(KeyboardSettings settings) {
        this.settings = settings == null ? KeyboardSettings.defaults() : settings;
        feedback.setEnabled(this.settings.hapticFeedbackEnabled);
        feedback.reloadPreferences(getContext());
        differentiatedHapticEnabled = KeyboardPreferences.loadDifferentiatedHapticEnabled(getContext());
        touchBiasAutoCorrectionEnabled = KeyboardPreferences.loadTouchBiasAutoCorrectionEnabled(getContext());
        showHangulConsonantSlideHints =
                KeyboardPreferences.loadShowHangulConsonantSlideHints(getContext());
        showHangulVowelSlideHints =
                KeyboardPreferences.loadShowHangulVowelSlideHints(getContext());
        showSpacebarSlideHints = KeyboardPreferences.loadShowSpacebarSlideHints(getContext());
        touchBias = touchBiasStore.load();
        rows = KeyboardLayoutFactory.build(this.settings);
        applyTypeface();
        if (getWidth() > 0 && getHeight() > 0) {
            layoutKeys(getWidth(), getHeight());
        }
        requestLayout();
        updatePreviewPopup();
        invalidate();
    }

    void setEnglishShiftState(boolean active, boolean locked) {
        englishShiftActive = active;
        englishCapsLocked = locked;
        invalidate();
    }

    void setCompactPreviewRendering(boolean compactPreviewRendering) {
        this.compactPreviewRendering = compactPreviewRendering;
        if (getWidth() > 0 && getHeight() > 0) {
            layoutKeys(getWidth(), getHeight());
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = dp(settings.measuredHeightDp());
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        layoutKeys(width, height);
        updateSystemGestureExclusion(width, height);
    }

    @Override
    protected void onDetachedFromWindow() {
        clearTouchState();
        repeatController.stop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawKeyboardPanel(canvas);
        for (KeySlot keySlot : keySlots) {
            drawKey(canvas, keySlot);
        }
    }

    private void drawKeyboardPanel(Canvas canvas) {
        keyPaint.setShader(null);
        keyPaint.setColor(settings.keyboardBackgroundColor);
        keyPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), keyPaint);
        if (settings.visualEffects.blurEnabled && settings.visualEffects.blurRadiusDp > 0) {
            drawPanelBlurWash(canvas);
        }
        if (settings.visualEffects.metallicEnabled && settings.visualEffects.metallicStrengthPercent > 0) {
            drawMetallicReflection(canvas);
        }
        keyPaint.setShader(null);
    }

    private void drawPanelBlurWash(Canvas canvas) {
        int radius = renderDp(settings.visualEffects.blurRadiusDp);
        int alpha = Math.min(92, 28 + radius * 2);
        keyPaint.setShader(new LinearGradient(
                0,
                0,
                0,
                Math.max(1, getHeight()),
                new int[] {
                        withAlpha(lightenColor(settings.keyboardBackgroundColor, 1.16f), alpha),
                        withAlpha(settings.keyboardBackgroundColor, Math.max(12, alpha / 3)),
                        withAlpha(darkenColor(settings.keyboardBackgroundColor, 0.76f), Math.max(18, alpha / 2))
                },
                new float[] { 0f, 0.46f, 1f },
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, getWidth(), getHeight(), keyPaint);
        keyPaint.setShader(null);
    }

    private void drawMetallicReflection(Canvas canvas) {
        int strength = settings.visualEffects.metallicStrengthPercent;
        int lightAlpha = Math.min(120, 18 + strength);
        int darkAlpha = Math.min(96, 10 + strength / 2);
        keyPaint.setShader(new LinearGradient(
                0,
                0,
                Math.max(1, getWidth()),
                Math.max(1, getHeight()),
                new int[] {
                        withAlpha(0xFFFFFFFF, lightAlpha),
                        withAlpha(0xFFFFFFFF, Math.max(8, lightAlpha / 3)),
                        withAlpha(0xFF000000, darkAlpha),
                        withAlpha(0xFFFFFFFF, Math.max(8, lightAlpha / 4))
                },
                new float[] { 0f, 0.28f, 0.62f, 1f },
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, getWidth(), getHeight(), keyPaint);
        keyPaint.setShader(null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                return handlePointerDown(event, event.getActionIndex());
            case MotionEvent.ACTION_MOVE:
                return handleMove(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                return handlePointerUp(event, event.getActionIndex());
            case MotionEvent.ACTION_CANCEL:
                clearTouchState();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private boolean handlePointerDown(MotionEvent event, int pointerIndex) {
        KeySlot keySlot = findKey(event.getX(pointerIndex), event.getY(pointerIndex));
        if (keySlot == null) {
            return false;
        }

        int pointerId = event.getPointerId(pointerIndex);
        removeTouchState(pointerId);
        TouchState state = new TouchState(
                pointerId,
                keySlot,
                event.getX(pointerIndex),
                event.getY(pointerIndex),
                nextTouchSequence++);
        activeTouches.add(state);
        if (previewKeySelectionListener == null) {
            if (isDeleteKey(keySlot.key)) {
                state.tapOutputAlreadyEmitted = true;
                feedback.tapHeavy();
                emitValue(KeyboardCommands.CMD_DELETE);
            }
            int longPressDelay = longPressDelayFor(keySlot.key);
            scheduleLongPress(state, longPressDelay);
        }
        updatePreviewPopup();
        invalidate();
        return true;
    }

    private boolean handleMove(MotionEvent event) {
        if (activeTouches.isEmpty()) {
            return false;
        }

        boolean handled = false;
        for (int i = 0; i < event.getPointerCount(); i++) {
            TouchState state = findTouchState(event.getPointerId(i));
            if (state != null) {
                updateTouchMove(state, event.getX(i), event.getY(i));
                handled = true;
            }
        }
        if (handled) {
            updatePreviewPopup();
            invalidate();
        }
        return handled;
    }

    private void updateTouchMove(TouchState state, float x, float y) {
        boolean wasLocked = state.gestureState.isLocked();
        GestureAction action = state.gestureState.update(
                x - state.downX,
                y - state.downY,
                baseGestureThresholdPx(),
                gestureThresholdPxFor(GestureAction.UP),
                gestureThresholdPxFor(GestureAction.DOWN),
                gestureThresholdPxFor(GestureAction.LEFT),
                gestureThresholdPxFor(GestureAction.RIGHT));
        if (state.gestureState.isLocked() && !wasLocked) {
            cancelLongPressTimer(state);
            feedback.slideLock();
            String repeatValue = repeatableValue(state.keySlot.key.valueFor(action));
            if (previewKeySelectionListener == null
                    && repeatValue != null
                    && (isCursorMove(repeatValue) || isInputRepeatKey(state.keySlot.key))) {
                repeatController.start(
                        repeatValue,
                        settings.repeatStartDelayMs,
                        settings.repeatIntervalMs,
                        false);
            }
        }
        state.activeAction = state.longPressTriggered ? GestureAction.LONG_PRESS : action;
    }

    private boolean handlePointerUp(MotionEvent event, int pointerIndex) {
        TouchState state = findTouchState(event.getPointerId(pointerIndex));
        if (state == null) {
            return false;
        }

        cancelLongPressTimer(state);
        if (previewKeySelectionListener != null) {
            feedback.tap();
            previewKeySelectionListener.onPreviewKeySelected(state.keySlot.key);
            removeTouchState(state, true);
            return true;
        }
        boolean repeatAlreadyFired = repeatController.hasFired();
        if (!state.longPressTriggered && !repeatAlreadyFired && !state.tapOutputAlreadyEmitted) {
            GestureAction action = state.gestureState.release(
                    event.getX(pointerIndex) - state.downX,
                    event.getY(pointerIndex) - state.downY,
                    baseGestureThresholdPx(),
                    gestureThresholdPxFor(GestureAction.UP),
                    gestureThresholdPxFor(GestureAction.DOWN),
                    gestureThresholdPxFor(GestureAction.LEFT),
                    gestureThresholdPxFor(GestureAction.RIGHT));
            state.activeAction = action;
            if (action == GestureAction.TAP) {
                feedbackForKey(state.keySlot.key, action);
            }
            queueTouchOutput(
                    state,
                    state.keySlot.key.valueFor(action),
                    event.getX(pointerIndex),
                    event.getY(pointerIndex));
        }
        removeTouchState(state, true);
        flushPendingTouchOutputs();
        return true;
    }

    private void scheduleLongPress(final TouchState state, int delayMs) {
        state.longPressRunnable = new Runnable() {
            @Override
            public void run() {
                if (!activeTouches.contains(state) || state.gestureState.isLocked()) {
                    return;
                }
                state.longPressTriggered = true;
                state.activeAction = GestureAction.LONG_PRESS;
                feedback.longPress();
                String repeatValue = longPressRepeatValue(state.keySlot.key);
                if (repeatValue != null) {
                    repeatController.start(repeatValue, settings.repeatIntervalMs, settings.repeatIntervalMs, true);
                } else {
                    emitValue(state.keySlot.key.valueFor(GestureAction.LONG_PRESS));
                }
                updatePreviewPopup();
                invalidate();
            }
        };
        postDelayed(state.longPressRunnable, delayMs);
    }

    private void queueTouchOutput(TouchState state, String value, float x, float y) {
        pendingTouchOutputs.add(new PendingTouchOutput(
                state.sequence,
                state.keySlot,
                state.activeAction,
                value,
                x,
                y));
        flushPendingTouchOutputs();
    }

    private void flushPendingTouchOutputs() {
        while (!pendingTouchOutputs.isEmpty()) {
            int nextIndex = nextPendingOutputIndex();
            PendingTouchOutput next = pendingTouchOutputs.get(nextIndex);
            long oldestActiveSequence = oldestActiveTouchSequence();
            if (oldestActiveSequence >= 0 && oldestActiveSequence < next.sequence) {
                return;
            }
            pendingTouchOutputs.remove(nextIndex);
            rememberTextTouch(next.keySlot, valueOrNull(next.value), next.action, next.x, next.y);
            emitValue(next.value);
        }
    }

    private int longPressDelayFor(GestureKey key) {
        if (isDeleteKey(key)) {
            return Math.max(120, Math.min(settings.repeatStartDelayMs, 170));
        }
        return longPressRepeatValue(key) == null
                ? ViewConfiguration.getLongPressTimeout()
                : settings.repeatStartDelayMs;
    }

    private int nextPendingOutputIndex() {
        int nextIndex = 0;
        long nextSequence = pendingTouchOutputs.get(0).sequence;
        for (int i = 1; i < pendingTouchOutputs.size(); i++) {
            long sequence = pendingTouchOutputs.get(i).sequence;
            if (sequence < nextSequence) {
                nextSequence = sequence;
                nextIndex = i;
            }
        }
        return nextIndex;
    }

    private long oldestActiveTouchSequence() {
        long oldest = -1;
        for (TouchState state : activeTouches) {
            if (oldest < 0 || state.sequence < oldest) {
                oldest = state.sequence;
            }
        }
        return oldest;
    }

    private void emitValue(String value) {
        if (KeyboardCommands.CMD_NOOP.equals(value)) {
            return;
        }
        if (listener != null && value != null && !value.isEmpty()) {
            recordImmediateDeleteIfNeeded(value);
            listener.onKeyGesture(value);
        }
    }

    private void feedbackForKey(GestureKey key, GestureAction action) {
        if (!differentiatedHapticEnabled) {
            feedback.tap();
            return;
        }
        String value = key.valueFor(action);
        if (KeyboardCommands.CMD_ENTER.equals(value)) {
            feedback.tapConfirm();
        } else if (KeyboardCommands.CMD_SHIFT_ONCE.equals(value)
                || KeyboardCommands.CMD_SHIFT_LOCK.equals(value)
                || KeyboardCommands.CMD_TOGGLE_LANGUAGE.equals(value)) {
            feedback.tapClick();
        } else if (KeyboardCommands.CMD_DELETE.equals(value)) {
            feedback.tapHeavy();
        } else {
            feedback.tap();
        }
    }

    KeyboardFeedback getFeedback() {
        return feedback;
    }

    private void rememberTextTouch(KeySlot keySlot, String value, GestureAction action, float x, float y) {
        if (keySlot == null || value == null || KeyboardCommands.isCommand(value)) {
            return;
        }
        float offsetXDp = (x - keySlot.bounds.centerX()) / getResources().getDisplayMetrics().density;
        float offsetYDp = (y - keySlot.bounds.centerY()) / getResources().getDisplayMetrics().density;
        lastTextTouchSample = new TouchSample(value, offsetXDp, offsetYDp, action, System.currentTimeMillis());
        if (touchBiasAutoCorrectionEnabled) {
            touchBiasStore.recordTextInput(value, action);
            touchBias = touchBiasStore.load();
        }
    }

    private void recordImmediateDeleteIfNeeded(String value) {
        if (!KeyboardCommands.CMD_DELETE.equals(value) || lastTextTouchSample == null) {
            if (!KeyboardCommands.isCommand(value)) {
                return;
            }
            if (!KeyboardCommands.CMD_DELETE.equals(value)) {
                lastTextTouchSample = null;
            }
            return;
        }

        if (System.currentTimeMillis() - lastTextTouchSample.timeMs <= 1500) {
            if (touchBiasAutoCorrectionEnabled) {
                touchBiasStore.recordImmediateDelete(
                        lastTextTouchSample.offsetXDp,
                        lastTextTouchSample.offsetYDp,
                        lastTextTouchSample.action,
                        lastTextTouchSample.value);
                touchBias = touchBiasStore.load();
            }
        }
        lastTextTouchSample = null;
    }

    private String valueOrNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private void clearTouchState() {
        for (TouchState state : new ArrayList<>(activeTouches)) {
            cancelLongPressTimer(state);
        }
        activeTouches.clear();
        pendingTouchOutputs.clear();
        repeatController.stop();
        hidePreviewPopup();
        invalidate();
    }

    private void removeTouchState(int pointerId) {
        TouchState state = findTouchState(pointerId);
        if (state != null) {
            removeTouchState(state, false);
        }
    }

    private void removeTouchState(TouchState state, boolean stopRepeat) {
        cancelLongPressTimer(state);
        activeTouches.remove(state);
        if (stopRepeat) {
            repeatController.stop();
        }
        updatePreviewPopup();
        invalidate();
    }

    private TouchState findTouchState(int pointerId) {
        for (TouchState state : activeTouches) {
            if (state.pointerId == pointerId) {
                return state;
            }
        }
        return null;
    }

    private TouchState primaryTouch() {
        if (activeTouches.isEmpty()) {
            return null;
        }
        return activeTouches.get(activeTouches.size() - 1);
    }

    private boolean isActiveKey(KeySlot keySlot) {
        for (TouchState state : activeTouches) {
            if (state.keySlot == keySlot) {
                return true;
            }
        }
        return false;
    }

    private void cancelLongPressTimer(TouchState state) {
        if (state != null && state.longPressRunnable != null) {
            removeCallbacks(state.longPressRunnable);
            state.longPressRunnable = null;
        }
    }

    private void initPaints() {
        keyPaint.setStyle(Paint.Style.FILL);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(1));

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp(17));
        textPaint.setFakeBoldText(false);

        hintPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint.setTextSize(sp(10));

        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);

        depthPaint.setStyle(Paint.Style.FILL);

        overlayPaint.setStyle(Paint.Style.FILL);

        overlayTextPaint.setTextAlign(Paint.Align.CENTER);
        overlayTextPaint.setTextSize(sp(16));
        overlayTextPaint.setFakeBoldText(false);
        applyTypeface();
    }

    private void applyTypeface() {
        if (textPaint == null) {
            return;
        }
        Typeface primaryTypeface = KeyboardTypefaceCatalog.typefaceFor(
                getContext(),
                settings.fontFamily,
                settings.primaryTextBold,
                settings.primaryTextItalic);
        Typeface secondaryTypeface = KeyboardTypefaceCatalog.typefaceFor(
                getContext(),
                settings.fontFamily,
                settings.secondaryTextBold,
                settings.secondaryTextItalic);
        textPaint.setTypeface(primaryTypeface);
        hintPaint.setTypeface(secondaryTypeface);
        overlayTextPaint.setTypeface(primaryTypeface);
        textPaint.setFakeBoldText(settings.primaryTextBold);
        hintPaint.setFakeBoldText(settings.secondaryTextBold);
        overlayTextPaint.setFakeBoldText(settings.primaryTextBold);
        textPaint.setTextSkewX(settings.primaryTextItalic ? -0.22f : 0f);
        hintPaint.setTextSkewX(settings.secondaryTextItalic ? -0.22f : 0f);
        overlayTextPaint.setTextSkewX(settings.primaryTextItalic ? -0.22f : 0f);
    }

    private void layoutKeys(int width, int height) {
        keySlots.clear();
        if (rows.isEmpty()) {
            return;
        }

        int visualGap = renderDp(settings.keyGapDp);
        float density = getResources().getDisplayMetrics().density;
        List<KeyboardLayoutCalculator.Slot> slots = KeyboardLayoutCalculator.layout(
                rows,
                settings,
                width,
                Math.max(1, height),
                density);
        for (KeyboardLayoutCalculator.Slot slot : slots) {
            keySlots.add(new KeySlot(
                    slot.key,
                    new RectF(slot.left, slot.top, slot.right, slot.bottom),
                    slot.primaryBottomControl,
                    slot.compactSpecialColumn,
                    visualGap));
        }
    }

    private void updateSystemGestureExclusion(int width, int height) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || width <= 0 || height <= 0) {
            return;
        }
        setSystemGestureExclusionRects(Collections.singletonList(new Rect(0, 0, width, height)));
    }

    private void drawKey(Canvas canvas, KeySlot keySlot) {
        GestureKey key = keySlot.key;
        boolean active = isActiveKey(keySlot);
        boolean shiftOnceActive = isShiftKey(key) && englishShiftActive && !englishCapsLocked;
        boolean shiftLockedActive = isShiftKey(key) && englishCapsLocked;
        boolean englishLetterKey = isEnglishLetterKey(key);
        RectF visualBounds = keySlot.visualBounds();
        RectF surfaceBounds = keySurfaceBounds(visualBounds, active);
        drawKeyDepth(canvas, visualBounds, active);
        keyPaint.setColor(active || shiftOnceActive ? settings.keyPressedColor : baseColorForKey(keySlot));
        drawKeyShape(canvas, surfaceBounds, keyPaint);
        drawBorderShape(canvas, surfaceBounds);

        float centerX = surfaceBounds.centerX();
        int icon = iconFor(key);
        KeyDisplayOverride displayOverride = KeyDisplayOverrideResolver.resolve(settings, key);
        if (displayOverride != null) {
            drawDisplayOverride(canvas, displayOverride, key, surfaceBounds, englishLetterKey);
        } else if (icon == KeyIcon.NONE) {
            String label = displayLabelForKey(key);
            textPaint.setColor(KeyboardKeyVisualClassifier.textColorFor(settings, key));
            String paintLabel = textPresentation(label);
            boolean compactMainLabel = keySlot.compactSpecialColumn && !isFullSizeDingulSpecialLabel(key);
            textPaint.setTextSize(textSizeFor(paintLabel, surfaceBounds, compactMainLabel));
            float labelCenterY = englishLetterKey
                    ? surfaceBounds.top + surfaceBounds.height() * 0.36f
                    : surfaceBounds.centerY();
            float centerY = labelCenterY - textCenterOffset(textPaint);
            canvas.drawText(paintLabel, centerX, centerY, textPaint);
        } else {
            drawKeyIcon(canvas, key, icon, surfaceBounds, active);
            if (shiftLockedActive) {
                drawShiftLockIndicator(canvas, surfaceBounds);
            }
        }

        if (shouldShowSlideHints()
                && displayOverride == null
                && !drawsCustomModifierGlyph(key, icon)
                && shouldDrawSlideHintsForKey(key, icon)) {
            float hintTextSize = (englishLetterKey
                    ? renderSp(8.4f)
                    : (keySlot.compactSpecialColumn ? renderSp(7) : renderSp(8.5f))) * secondaryTextScale();
            float horizontalHintInset = keySlot.compactSpecialColumn
                    ? surfaceBounds.width() * 0.23f
                    : renderDp(19);
            boolean numberRowKey = isAdditionalNumberRowKey(key);
            float topHintInset = topHintInset(surfaceBounds, keySlot, englishLetterKey, numberRowKey);
            float bottomHintInset = bottomHintInset(surfaceBounds, englishLetterKey, numberRowKey);
            if (isSpaceKey(key)) {
                drawSpaceCursorHints(canvas, key, surfaceBounds, hintTextSize);
            } else if (englishLetterKey) {
                drawEnglishSlideHints(canvas, key, surfaceBounds, hintTextSize, bottomHintInset);
            } else {
                drawHint(canvas, key, key.upSlide, centerX, surfaceBounds.top + topHintInset, hintTextSize);
                drawHint(canvas, key, key.downSlide, centerX, surfaceBounds.bottom - bottomHintInset, hintTextSize);
                drawHint(canvas, key, key.leftSlide, surfaceBounds.left + horizontalHintInset,
                        surfaceBounds.centerY() - textCenterOffset(hintPaint), hintTextSize);
                drawHint(canvas, key, key.rightSlide, surfaceBounds.right - horizontalHintInset,
                        surfaceBounds.centerY() - textCenterOffset(hintPaint), hintTextSize);
            }
        }
    }

    private void drawSpaceCursorHints(Canvas canvas, GestureKey key, RectF surfaceBounds, float hintTextSize) {
        float y = surfaceBounds.centerY();
        float inset = Math.min(renderDp(28), surfaceBounds.width() * 0.10f);
        drawSpaceCursorHint(canvas, key.leftSlide, surfaceBounds.left + inset, y);
        drawSpaceCursorHint(canvas, key.rightSlide, surfaceBounds.right - inset, y);
    }

    private void drawSpaceCursorHint(Canvas canvas, String value, float x, float y) {
        int icon = KeyIcon.forCommand(value);
        if (icon == KeyIcon.NONE) {
            return;
        }
        drawIconCentered(canvas, icon, x, y, hintIconSize() * 0.74f, settings.secondaryColor);
    }

    private void drawDotLegend(Canvas canvas, GestureKey key, RectF surfaceBounds, boolean englishLetterKey) {
        textPaint.setColor(KeyboardKeyVisualClassifier.textColorFor(settings, key));
        float radius = dotsLineWeightFor(surfaceBounds) / 2f;
        float centerY = englishLetterKey
                ? surfaceBounds.top + surfaceBounds.height() * 0.36f
                : surfaceBounds.centerY();
        canvas.drawCircle(surfaceBounds.centerX(), centerY, radius, textPaint);
    }

    private void drawDisplayOverride(
            Canvas canvas,
            KeyDisplayOverride override,
            GestureKey key,
            RectF surfaceBounds,
            boolean englishLetterKey) {
        if (override.isText()) {
            drawTextDisplayOverride(canvas, override, key, surfaceBounds);
            return;
        }
        if (ModifierIconCatalog.GLYPH_DOT.equals(override.value)) {
            drawDotLegend(canvas, key, surfaceBounds, englishLetterKey);
            return;
        }
        textPaint.setColor(KeyboardKeyVisualClassifier.textColorFor(settings, key));
        textPaint.setTextSize(textSizeFor(override.value, surfaceBounds, false));
        float centerY = surfaceBounds.centerY() - textCenterOffset(textPaint);
        canvas.drawText(override.value, surfaceBounds.centerX(), centerY, textPaint);
    }

    private void drawTextDisplayOverride(
            Canvas canvas,
            KeyDisplayOverride override,
            GestureKey key,
            RectF surfaceBounds) {
        int color = KeyboardKeyVisualClassifier.textColorFor(settings, key);
        if (KeyDisplayOverridePackCatalog.shouldRenderSimpleText(settings, override)) {
            if ("hihihi".equals(override.value)) {
                drawHihihiScriptGlyph(canvas, surfaceBounds, color);
                return;
            }
            modifierIconPaint.reset();
            modifierIconPaint.setAntiAlias(true);
            modifierIconPaint.setStyle(Paint.Style.FILL);
            modifierIconPaint.setTextAlign(Paint.Align.CENTER);
            modifierIconPaint.setColor(color);
            modifierIconPaint.setTypeface(KeyboardTypefaceCatalog.typefaceFor(
                    getContext(),
                    settings.fontFamily,
                    true,
                    false));
            modifierIconPaint.setTextSkewX(0f);
            modifierIconPaint.setFakeBoldText(true);
            float textSize = textSizeForPaint(
                    modifierIconPaint,
                    override.value,
                    surfaceBounds.width() * 0.72f,
                    surfaceBounds.height() * 0.42f,
                    renderSp(16));
            modifierIconPaint.setTextSize(textSize);
            float centerY = surfaceBounds.centerY() - textCenterOffset(modifierIconPaint);
            canvas.drawText(override.value, surfaceBounds.centerX(), centerY, modifierIconPaint);
            return;
        }
        textPaint.setColor(color);
        textPaint.setTextSize(textSizeFor(override.value, surfaceBounds, false));
        float centerY = surfaceBounds.centerY() - textCenterOffset(textPaint);
        canvas.drawText(override.value, surfaceBounds.centerX(), centerY, textPaint);
    }

    private void drawHihihiScriptGlyph(Canvas canvas, RectF bounds, int color) {
        modifierIconPaint.reset();
        modifierIconPaint.setAntiAlias(true);
        modifierIconPaint.setStyle(Paint.Style.STROKE);
        modifierIconPaint.setStrokeCap(Paint.Cap.ROUND);
        modifierIconPaint.setStrokeJoin(Paint.Join.ROUND);
        modifierIconPaint.setColor(color);
        modifierIconPaint.setStrokeWidth(Math.max(bounds.height() * 0.045f, renderDp(1.7f)));

        Path path = new Path();
        path.moveTo(3f, 19f);
        path.cubicTo(7f, 5f, 11f, 5f, 9f, 18f);
        path.cubicTo(12f, 13f, 16f, 12f, 18f, 17f);
        path.cubicTo(20f, 22f, 15f, 24f, 13f, 19f);
        path.cubicTo(18f, 20f, 22f, 20f, 26f, 17f);
        path.cubicTo(29f, 14f, 32f, 14f, 31f, 18f);
        path.cubicTo(31f, 22f, 25f, 22f, 26f, 17f);
        path.cubicTo(31f, 20f, 36f, 20f, 40f, 17f);
        path.cubicTo(44f, 5f, 49f, 5f, 46f, 18f);
        path.cubicTo(49f, 13f, 54f, 12f, 56f, 17f);
        path.cubicTo(58f, 22f, 52f, 24f, 51f, 19f);
        path.cubicTo(56f, 20f, 60f, 20f, 64f, 17f);
        path.cubicTo(67f, 14f, 70f, 14f, 69f, 18f);
        path.cubicTo(69f, 22f, 63f, 22f, 64f, 17f);
        path.cubicTo(69f, 20f, 74f, 20f, 78f, 17f);
        path.cubicTo(81f, 14f, 84f, 14f, 83f, 18f);
        path.cubicTo(83f, 22f, 77f, 22f, 78f, 17f);
        path.cubicTo(83f, 20f, 87f, 20f, 91f, 17f);

        canvas.save();
        float scale = Math.min(bounds.width() * 0.66f / 94f, bounds.height() * 0.34f / 28f);
        float left = bounds.centerX() - 94f * scale / 2f;
        float top = bounds.centerY() - 28f * scale / 2f;
        canvas.translate(left, top);
        canvas.scale(scale, scale);
        canvas.drawPath(path, modifierIconPaint);
        canvas.restore();
    }

    private void drawEnglishSlideHints(
            Canvas canvas,
            GestureKey key,
            RectF surfaceBounds,
            float hintTextSize,
            float bottomHintInset) {
        float y = surfaceBounds.bottom - bottomHintInset;
        hintPaint.setTextSize(hintTextSize);
        y = surfaceBounds.top + surfaceBounds.height() * 0.73f - textCenterOffset(hintPaint);
        boolean hasLeft = displayFor(key.leftSlide) != null;
        boolean hasRight = displayFor(key.rightSlide) != null;
        if (hasLeft && hasRight) {
            drawHint(canvas, key, key.leftSlide, surfaceBounds.left + surfaceBounds.width() * 0.32f, y, hintTextSize);
            drawHint(canvas, key, key.rightSlide, surfaceBounds.right - surfaceBounds.width() * 0.32f, y, hintTextSize);
            return;
        }
        String centered = displayFor(key.downSlide) != null
                ? key.downSlide
                : (hasLeft ? key.leftSlide : key.rightSlide);
        drawHint(canvas, key, centered, surfaceBounds.centerX(), y, hintTextSize);
    }

    private float topHintInset(
            RectF bounds,
            KeySlot keySlot,
            boolean englishLetterKey,
            boolean numberRowKey) {
        if (numberRowKey || englishLetterKey) {
            return Math.max(renderDp(6), bounds.height() * 0.12f);
        }
        return renderDp(11);
    }

    private float bottomHintInset(RectF bounds, boolean englishLetterKey, boolean numberRowKey) {
        if (numberRowKey || englishLetterKey) {
            return Math.max(renderDp(6), bounds.height() * 0.12f);
        }
        return renderDp(7);
    }

    private boolean isFullSizeDingulSpecialLabel(GestureKey key) {
        if (settings.keyboardMode != KeyboardMode.HANGUL || key == null || key.label == null) {
            return false;
        }
        return "?".equals(key.label) || ".".equals(key.label) || "/".equals(key.label);
    }

    private void drawBorderShape(Canvas canvas, RectF bounds) {
        float strokeWidth = renderDp(settings.keyBorderWidthDp);
        if (strokeWidth <= 0f) {
            return;
        }
        borderPaint.setColor(settings.borderColor);
        borderPaint.setStrokeWidth(strokeWidth);
        RectF borderBounds = new RectF(bounds);
        float inset = strokeWidth / 2f;
        borderBounds.inset(inset, inset);
        if (borderBounds.width() <= 0f || borderBounds.height() <= 0f) {
            return;
        }
        drawKeyShape(canvas, borderBounds, borderPaint);
    }

    private RectF keySurfaceBounds(RectF bounds, boolean active) {
        if (!settings.keyDepthEnabled || settings.keyDepthDp <= 0) {
            return new RectF(bounds);
        }
        float pressOffset = active
                ? Math.min(renderDp(settings.keyDepthDp) * 0.60f, bounds.height() * 0.06f)
                : 0f;
        return new RectF(bounds.left, bounds.top + pressOffset, bounds.right, bounds.bottom + pressOffset);
    }

    private void drawKeyDepth(Canvas canvas, RectF bounds, boolean active) {
        if (!settings.keyDepthEnabled || settings.keyDepthDp <= 0) {
            return;
        }
        float configuredDepth = renderDp(settings.keyDepthDp);
        float depth = active
                ? Math.min(configuredDepth * 0.35f, bounds.height() * 0.035f)
                : Math.min(configuredDepth, bounds.height() * 0.12f);
        if (depth <= 0f) {
            return;
        }
        RectF depthBounds = new RectF(bounds.left, bounds.top + depth, bounds.right, bounds.bottom + depth);
        depthPaint.setColor(depthColor(active));
        drawKeyShape(canvas, depthBounds, depthPaint);
    }

    private int baseColorForKey(KeySlot keySlot) {
        return KeyboardKeyVisualClassifier.colorFor(settings, keySlot.key);
    }

    private int depthColor(boolean active) {
        int baseColor = settings.customDepthColorEnabled ? settings.depthColor : settings.borderColor;
        return shadeColor(baseColor, active ? 0.72f : 0.88f);
    }

    private int shadeColor(int color, float factor) {
        int a = color & 0xFF000000;
        int r = Math.round(((color >> 16) & 0xFF) * factor);
        int g = Math.round(((color >> 8) & 0xFF) * factor);
        int b = Math.round((color & 0xFF) * factor);
        return a | (clampColor(r) << 16) | (clampColor(g) << 8) | clampColor(b);
    }

    private int lightenColor(int color, float factor) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return 0xFF000000
                | (clampColor(Math.round(r + (255 - r) * (factor - 1f))) << 16)
                | (clampColor(Math.round(g + (255 - g) * (factor - 1f))) << 8)
                | clampColor(Math.round(b + (255 - b) * (factor - 1f)));
    }

    private int darkenColor(int color, float factor) {
        return shadeColor(color, factor);
    }

    private int withAlpha(int color, int alpha) {
        return (clampColor(alpha) << 24) | (color & 0x00FFFFFF);
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private float textSizeFor(String label, RectF bounds, boolean compactSpecialColumn) {
        float size;
        if (label.length() >= 5) {
            size = renderSp(8.5f);
        } else if (label.length() >= 3) {
            size = renderSp(compactSpecialColumn ? 8 : 10);
        } else {
            size = renderSp(compactSpecialColumn ? 12 : 17);
        }
        size *= primaryTextScale();

        float minSize = renderSp(7) * primaryTextScale();
        float maxWidth = bounds.width() * (compactSpecialColumn ? 0.60f : 0.78f);
        while (size > minSize) {
            textPaint.setTextSize(size);
            if (textPaint.measureText(label) <= maxWidth) {
                break;
            }
            size -= renderSp(1);
        }
        return Math.max(minSize, size);
    }

    private float overlayTextSizeFor(String label) {
        return (label.length() > 3 ? renderSp(10) : renderSp(16)) * primaryTextScale();
    }

    private float primaryTextScale() {
        return settings.primaryTextSizePercent / 100f;
    }

    private float secondaryTextScale() {
        return settings.secondaryTextSizePercent / 100f;
    }

    private void drawHint(Canvas canvas, GestureKey key, String value, float x, float y, float textSize) {
        int icon = KeyIcon.forCommand(value);
        if (icon != KeyIcon.NONE) {
            drawIconCentered(canvas, icon, x, y, hintIconSize(), false);
            return;
        }

        String label = displayFor(value);
        if (label != null && label.length() <= 4) {
            hintPaint.setColor(KeyboardKeyVisualClassifier.hintColorFor(settings, key));
            hintPaint.setTextSize(textSize);
            canvas.drawText(textPresentation(label), x, y, hintPaint);
        }
    }

    private void updatePreviewPopup() {
        TouchState activeTouch = primaryTouch();
        if (!settings.showBeginnerTooltipPreview || activeTouch == null) {
            hidePreviewPopup();
            return;
        }
        if (!shouldShowPreviewForTouch(activeTouch)) {
            hidePreviewPopup();
            return;
        }

        String value = previewValueForTouch(activeTouch);
        String label = displayFor(value);
        if (label == null) {
            hidePreviewPopup();
            return;
        }

        String paintLabel = textPresentation(label);
        overlayTextPaint.setTextSize(overlayTextSizeFor(paintLabel));
        int popupWidth = Math.min(renderDp(92), Math.max(
                renderDp(48),
                Math.round(overlayTextPaint.measureText(paintLabel)) + renderDp(28)));
        int popupHeight = renderDp(42);
        RectF anchor = activeTouch.keySlot.visualBounds();
        if (previewOverlayListener != null) {
            previewOverlayListener.onPreviewOverlayChanged(new PreviewOverlaySpec(
                    paintLabel,
                    Math.round(anchor.centerX() - popupWidth / 2f),
                    Math.round(anchor.top - renderDp(8) - popupHeight),
                    popupWidth,
                    popupHeight,
                    overlayTextSizeFor(paintLabel),
                    KeyboardKeyVisualClassifier.textColorFor(settings, activeTouch.keySlot.key),
                    baseColorForKey(activeTouch.keySlot),
                    settings.borderColor,
                    renderDp(settings.keyBorderWidthDp),
                    settings.visualEffects.angularPreviewBubble ? renderDp(3) : renderDp(8),
                    settings.visualEffects.angularPreviewBubble));
        }
    }

    private void hidePreviewPopup() {
        if (previewOverlayListener != null) {
            previewOverlayListener.onPreviewOverlayHidden();
        }
    }

    private boolean shouldShowPreviewForTouch(TouchState state) {
        return true;
    }

    private String previewValueForTouch(TouchState state) {
        if (state == null) {
            return null;
        }
        if (state.activeAction == GestureAction.LONG_PRESS
                && state.keySlot.key.longPress == null
                && repeatableValue(state.keySlot.key.tap) != null) {
            return state.keySlot.key.tap;
        }
        return previewValueWithShift(state.keySlot.key.valueFor(state.activeAction));
    }

    private String previewValueWithShift(String value) {
        if (settings.keyboardMode == KeyboardMode.ENGLISH
                && englishShiftActive
                && isSingleAsciiLetter(value)) {
            return value.toUpperCase(Locale.US);
        }
        return value;
    }

    private void drawOverlayItem(
            Canvas canvas,
            String value,
            float centerX,
            float centerY,
            boolean selected,
            float width,
            float height) {
        String label = displayFor(value);
        if (label == null) {
            return;
        }

        RectF rect = new RectF(
                centerX - width / 2f,
                centerY - height / 2f,
                centerX + width / 2f,
                centerY + height / 2f);
        overlayPaint.setColor(selected ? settings.keyPressedColor : settings.keyIdleColor);
        canvas.drawRoundRect(rect, renderDp(6), renderDp(6), overlayPaint);
        drawBorderShape(canvas, rect);
        int icon = isReservedPhraseCommand(value) ? KeyIcon.NONE : KeyIcon.forCommand(value);
        if (icon == KeyIcon.NONE) {
            overlayTextPaint.setColor(settings.accentColor);
            String paintLabel = textPresentation(label);
            overlayTextPaint.setTextSize(overlayTextSizeFor(paintLabel));
            canvas.drawText(paintLabel, centerX, centerY - textCenterOffset(overlayTextPaint), overlayTextPaint);
        } else {
            drawIconCentered(
                    canvas,
                    icon,
                    rect.centerX(),
                    rect.centerY(),
                    overlayIconSize(),
                    selected);
        }
    }

    private int iconFor(GestureKey key) {
        if (isShiftKey(key) && englishCapsLocked) {
            return KeyIcon.CAPS_LOCK;
        }
        return key.icon;
    }

    private boolean isShiftKey(GestureKey key) {
        return KeyboardCommands.CMD_SHIFT_ONCE.equals(key.tap);
    }

    private boolean isSpaceKey(GestureKey key) {
        return key != null && KeyboardCommands.CMD_SPACE.equals(key.tap);
    }

    private String displayLabelForKey(GestureKey key) {
        if (englishShiftActive && isEnglishLetterKey(key)) {
            return key.label.toUpperCase(Locale.US);
        }
        return key.label;
    }

    private boolean isEnglishLetterKey(GestureKey key) {
        return key != null
                && isSingleAsciiLetter(key.label)
                && isSingleAsciiLetter(key.tap)
                && isSingleAsciiLetter(key.upSlide);
    }

    private boolean isAdditionalNumberRowKey(GestureKey key) {
        return key != null
                && key.tap != null
                && key.tap.length() == 1
                && key.tap.charAt(0) >= '0'
                && key.tap.charAt(0) <= '9';
    }

    private boolean isSingleAsciiLetter(String value) {
        if (value == null || value.length() != 1) {
            return false;
        }
        char c = value.charAt(0);
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private void drawKeyIcon(Canvas canvas, GestureKey key, int icon, RectF bounds, boolean selected) {
        String packId = ModifierIconCatalog.effectivePackId(settings);
        if (ModifierIconCatalog.rendersCustomGlyphs(packId)
                && drawModifierPackIcon(canvas, key, icon, bounds, selected, packId)) {
            return;
        }
        float cy = bounds.centerY();
        if (icon == KeyIcon.SPACE) {
            cy += Math.min(renderDp(5), bounds.height() * 0.09f);
        }
        drawIconCentered(
                canvas,
                icon,
                bounds.centerX(),
                cy,
                keyIconSize(),
                ModifierIconCatalog.colorForEffectivePack(
                        settings,
                        KeyboardKeyVisualClassifier.iconColorFor(settings, key, selected)));
    }

    private boolean drawModifierPackIcon(
            Canvas canvas,
            GestureKey key,
            int icon,
            RectF bounds,
            boolean selected,
            String packId) {
        int color = KeyboardKeyVisualClassifier.iconColorFor(settings, key, selected);
        if (ModifierIconCatalog.isDotsLinePack(packId)) {
            drawDotsLineModifierIcon(canvas, icon, bounds, color);
            return true;
        }
        if (ModifierIconCatalog.isMetropolisPack(packId)) {
            drawMetropolisModifierIcon(canvas, icon, bounds, color);
            return true;
        }
        return false;
    }

    private void drawDotsLineModifierIcon(Canvas canvas, int icon, RectF bounds, int color) {
        if (icon == KeyIcon.SPACE) {
            drawVividSpacebarDots(canvas, bounds);
            return;
        }
        if (isSingleDotModifierIcon(icon)) {
            drawSingleModifierDot(canvas, bounds, color);
            return;
        }
        drawModifierLine(canvas, bounds, color, 0.30f);
    }

    private boolean isLineDotModifierIcon(int icon) {
        return icon == KeyIcon.BACKSPACE
                || icon == KeyIcon.ENTER
                || icon == KeyIcon.DONE
                || icon == KeyIcon.NEXT
                || icon == KeyIcon.SETTINGS
                || icon == KeyIcon.SHIFT
                || icon == KeyIcon.CAPS_LOCK;
    }

    private boolean isSingleDotModifierIcon(int icon) {
        return icon == KeyIcon.LANGUAGE
                || icon == KeyIcon.RESERVED;
    }

    private float dotRadiusFor(RectF bounds) {
        return dotsLineWeightFor(bounds) / 2f;
    }

    private float dotsLineWeightFor(RectF bounds) {
        float weight = Math.min(bounds.height() * 0.16f, bounds.width() * 0.12f);
        return Math.max(renderDp(3.0f), weight);
    }

    private void drawDotRow(
            Canvas canvas,
            RectF bounds,
            int count,
            float radius,
            int color,
            float sidePaddingRatio) {
        iconPaint.setColor(color);
        iconPaint.setStyle(Paint.Style.FILL);
        float left = bounds.left + bounds.width() * sidePaddingRatio;
        float right = bounds.right - bounds.width() * sidePaddingRatio;
        if (count <= 1 || right <= left) {
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), radius, iconPaint);
            return;
        }
        float step = (right - left) / (count - 1);
        for (int i = 0; i < count; i++) {
            canvas.drawCircle(left + step * i, bounds.centerY(), radius, iconPaint);
        }
    }

    private void drawModifierLine(Canvas canvas, RectF bounds, int color, float sidePaddingRatio) {
        iconPaint.setColor(color);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeWidth(dotsLineWeightFor(bounds));
        float left = bounds.left + bounds.width() * sidePaddingRatio;
        float right = bounds.right - bounds.width() * sidePaddingRatio;
        canvas.drawLine(left, bounds.centerY(), right, bounds.centerY(), iconPaint);
    }

    private void drawSingleModifierDot(Canvas canvas, RectF bounds, int color) {
        iconPaint.setColor(color);
        iconPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(bounds.centerX(), bounds.centerY(), dotRadiusFor(bounds), iconPaint);
    }

    private void drawVividSpacebarDots(Canvas canvas, RectF bounds) {
        int[] colors = { 0xFFEF476F, 0xFFFFD166, 0xFF06D6A0, 0xFF4CC9F0 };
        float radius = dotRadiusFor(bounds);
        float gap = Math.max(radius * 1.35f, renderDp(3.2f));
        float totalWidth = radius * 2f * colors.length + gap * (colors.length - 1);
        float x = bounds.centerX() - totalWidth / 2f + radius;
        iconPaint.setStyle(Paint.Style.FILL);
        for (int color : colors) {
            iconPaint.setColor(color);
            canvas.drawCircle(x, bounds.centerY(), radius, iconPaint);
            x += radius * 2f + gap;
        }
    }

    private void drawMetropolisModifierIcon(Canvas canvas, int icon, RectF bounds, int color) {
        float cy = bounds.centerY();
        if (icon == KeyIcon.SPACE) {
            cy += Math.min(renderDp(5), bounds.height() * 0.09f);
        }
        drawIconCentered(canvas, icon, bounds.centerX(), cy, keyIconSize(), color);
    }

    private void drawDottedLine(Canvas canvas, float left, float right, float cy, float radius, int color) {
        iconPaint.setStyle(Paint.Style.FILL);
        iconPaint.setColor(color);
        int count = Math.max(3, Math.round((right - left) / Math.max(renderDp(7), radius * 3.4f)));
        if (count == 1) {
            canvas.drawCircle((left + right) / 2f, cy, radius, iconPaint);
            return;
        }
        for (int i = 0; i < count; i++) {
            float t = count == 1 ? 0.5f : (float) i / (float) (count - 1);
            canvas.drawCircle(left + (right - left) * t, cy, radius, iconPaint);
        }
        iconPaint.setStyle(Paint.Style.STROKE);
    }

    private void drawMetropolisEndDots(Canvas canvas, float x, float y, int color) {
        iconPaint.setStyle(Paint.Style.FILL);
        iconPaint.setColor(color);
        float radius = renderDp(2.2f);
        canvas.drawCircle(x, y, radius, iconPaint);
        iconPaint.setStyle(Paint.Style.STROKE);
    }

    private float textSizeForPaint(Paint paint, String text, float maxWidth, float maxHeight, float startSize) {
        float minSize = renderSp(8);
        float size = startSize;
        while (size > minSize) {
            paint.setTextSize(size);
            Paint.FontMetrics metrics = paint.getFontMetrics();
            if (paint.measureText(text) <= maxWidth && metrics.descent - metrics.ascent <= maxHeight) {
                return size;
            }
            size -= 1f;
        }
        return minSize;
    }

    private void drawShiftLockIndicator(Canvas canvas, RectF bounds) {
        float radius = Math.min(renderDp(5.2f), bounds.height() * 0.085f);
        float cx = (bounds.centerX() + bounds.right) / 2f;
        cx = Math.min(bounds.right - radius - renderDp(6), cx);
        float cy = bounds.centerY() - keyIconSize() * 1.08f - renderDp(1);
        cy = Math.max(bounds.top + radius + Math.min(renderDp(4), bounds.height() * 0.07f), cy);
        iconPaint.setStyle(Paint.Style.FILL);
        iconPaint.setColor(KeyboardKeyVisualClassifier.shiftIndicatorColorFor(settings));
        canvas.drawCircle(cx, cy, radius, iconPaint);
        iconPaint.setColor(contrastColor(KeyboardKeyVisualClassifier.shiftIndicatorColorFor(settings)));
        canvas.drawCircle(cx, cy, Math.max(1f, radius * 0.38f), iconPaint);
    }

    private int contrastColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int luminance = (r * 299 + g * 587 + b * 114) / 1000;
        return luminance > 150 ? 0xFF111827 : 0xFFFFFFFF;
    }

    private void drawIconCentered(
            Canvas canvas,
            int icon,
            float cx,
            float cy,
            float size,
            boolean selected) {
        int iconColor = selected ? settings.accentColor : settings.secondaryColor;
        drawIconCentered(canvas, icon, cx, cy, size, iconColor);
    }

    private void drawIconCentered(
            Canvas canvas,
            int icon,
            float cx,
            float cy,
            float size,
            int iconColor) {
        float left = cx - size / 2f;
        float top = cy - size / 2f;
        float right = cx + size / 2f;
        float bottom = cy + size / 2f;

        RectF iconBounds = new RectF(left, top, right, bottom);
        if (iconRegistry.draw(canvas, icon, iconBounds, iconColor)) {
            return;
        }

        iconPaint.setColor(iconColor);
        iconPaint.setStrokeWidth(Math.max(renderDp(1.5f), size / 10f));
        iconPaint.setStyle(Paint.Style.STROKE);

        switch (icon) {
            case KeyIcon.OPTIONS:
                drawOptionsIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.RESERVED:
                drawBookmarkIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.SPACE:
                drawSpaceIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.LANGUAGE:
                drawLanguageIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.SEARCH:
                drawSearchIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.DONE:
                drawDoneIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.NEXT:
                drawNextIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.SHIFT:
            case KeyIcon.CAPS_LOCK:
                drawShiftIcon(canvas, left, top, right, bottom, icon == KeyIcon.CAPS_LOCK);
                break;
            case KeyIcon.BACKSPACE:
                drawBackspaceIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.HIDE:
                drawHideIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.SETTINGS:
                drawSettingsIcon(canvas, left, top, right, bottom);
                break;
            case KeyIcon.MOVE_LEFT:
                drawArrowIcon(canvas, left, top, right, bottom, false);
                break;
            case KeyIcon.MOVE_RIGHT:
                drawArrowIcon(canvas, left, top, right, bottom, true);
                break;
            case KeyIcon.ENTER:
            default:
                drawEnterIcon(canvas, left, top, right, bottom);
                break;
        }
    }

    private void drawOptionsIcon(Canvas canvas, float left, float top, float right, float bottom) {
        float third = (bottom - top) / 3f;
        for (int i = 0; i < 3; i++) {
            float y = top + third * (i + 0.5f);
            canvas.drawLine(left, y, right, y, iconPaint);
        }
        canvas.drawCircle(left + (right - left) * 0.35f, top + third * 0.5f, renderDp(2.5f), iconPaint);
        canvas.drawCircle(left + (right - left) * 0.7f, top + third * 1.5f, renderDp(2.5f), iconPaint);
        canvas.drawCircle(left + (right - left) * 0.5f, top + third * 2.5f, renderDp(2.5f), iconPaint);
    }

    private void drawBookmarkIcon(Canvas canvas, float left, float top, float right, float bottom) {
        Path path = new Path();
        path.moveTo(left + (right - left) * 0.25f, top);
        path.lineTo(right - (right - left) * 0.25f, top);
        path.lineTo(right - (right - left) * 0.25f, bottom);
        path.lineTo((left + right) / 2f, bottom - (bottom - top) * 0.22f);
        path.lineTo(left + (right - left) * 0.25f, bottom);
        path.close();
        canvas.drawPath(path, iconPaint);
    }

    private void drawSpaceIcon(Canvas canvas, float left, float top, float right, float bottom) {
        float y = bottom - (bottom - top) * 0.25f;
        canvas.drawLine(left, y, right, y, iconPaint);
        canvas.drawLine(left, y, left, y - (bottom - top) * 0.25f, iconPaint);
        canvas.drawLine(right, y, right, y - (bottom - top) * 0.25f, iconPaint);
    }

    private void drawLanguageIcon(Canvas canvas, float left, float top, float right, float bottom) {
        RectF oval = new RectF(left, top, right, bottom);
        canvas.drawOval(oval, iconPaint);
        canvas.drawLine(left, (top + bottom) / 2f, right, (top + bottom) / 2f, iconPaint);
        canvas.drawOval(new RectF(left + (right - left) * 0.28f, top, right - (right - left) * 0.28f, bottom), iconPaint);
    }

    private void drawEnterIcon(Canvas canvas, float left, float top, float right, float bottom) {
        float midY = (top + bottom) / 2f;
        canvas.drawLine(right, top, right, midY, iconPaint);
        canvas.drawLine(right, midY, left, midY, iconPaint);
        canvas.drawLine(left, midY, left + (right - left) * 0.25f, top + (bottom - top) * 0.32f, iconPaint);
        canvas.drawLine(left, midY, left + (right - left) * 0.25f, bottom - (bottom - top) * 0.32f, iconPaint);
    }

    private void drawSearchIcon(Canvas canvas, float left, float top, float right, float bottom) {
        float radius = (right - left) * 0.28f;
        float cx = left + (right - left) * 0.42f;
        float cy = top + (bottom - top) * 0.42f;
        canvas.drawCircle(cx, cy, radius, iconPaint);
        canvas.drawLine(cx + radius * 0.7f, cy + radius * 0.7f, right, bottom, iconPaint);
    }

    private void drawDoneIcon(Canvas canvas, float left, float top, float right, float bottom) {
        canvas.drawLine(left, (top + bottom) / 2f, left + (right - left) * 0.4f, bottom, iconPaint);
        canvas.drawLine(left + (right - left) * 0.4f, bottom, right, top, iconPaint);
    }

    private void drawNextIcon(Canvas canvas, float left, float top, float right, float bottom) {
        drawArrowIcon(canvas, left, top, right - (right - left) * 0.18f, bottom, true);
        canvas.drawLine(right, top, right, bottom, iconPaint);
    }

    private void drawShiftIcon(Canvas canvas, float left, float top, float right, float bottom, boolean locked) {
        Path path = new Path();
        float midX = (left + right) / 2f;
        path.moveTo(midX, top);
        path.lineTo(right, top + (bottom - top) * 0.45f);
        path.lineTo(right - (right - left) * 0.28f, top + (bottom - top) * 0.45f);
        path.lineTo(right - (right - left) * 0.28f, bottom - (locked ? (bottom - top) * 0.2f : 0));
        path.lineTo(left + (right - left) * 0.28f, bottom - (locked ? (bottom - top) * 0.2f : 0));
        path.lineTo(left + (right - left) * 0.28f, top + (bottom - top) * 0.45f);
        path.lineTo(left, top + (bottom - top) * 0.45f);
        path.close();
        canvas.drawPath(path, iconPaint);
        if (locked) {
            canvas.drawLine(left + (right - left) * 0.22f, bottom, right - (right - left) * 0.22f, bottom, iconPaint);
        }
    }

    private void drawBackspaceIcon(Canvas canvas, float left, float top, float right, float bottom) {
        Path path = new Path();
        path.moveTo(left + (right - left) * 0.28f, top);
        path.lineTo(right, top);
        path.lineTo(right, bottom);
        path.lineTo(left + (right - left) * 0.28f, bottom);
        path.lineTo(left, (top + bottom) / 2f);
        path.close();
        canvas.drawPath(path, iconPaint);
        canvas.drawLine(left + (right - left) * 0.45f, top + (bottom - top) * 0.35f,
                right - (right - left) * 0.18f, bottom - (bottom - top) * 0.35f, iconPaint);
        canvas.drawLine(right - (right - left) * 0.18f, top + (bottom - top) * 0.35f,
                left + (right - left) * 0.45f, bottom - (bottom - top) * 0.35f, iconPaint);
    }

    private void drawHideIcon(Canvas canvas, float left, float top, float right, float bottom) {
        RectF rect = new RectF(left, top, right, top + (bottom - top) * 0.58f);
        canvas.drawRect(rect, iconPaint);
        float cell = (right - left) / 4f;
        for (int i = 1; i < 4; i++) {
            canvas.drawLine(left + cell * i, rect.top, left + cell * i, rect.bottom, iconPaint);
        }
        canvas.drawLine((left + right) / 2f, rect.bottom + (bottom - top) * 0.12f,
                (left + right) / 2f, bottom, iconPaint);
        canvas.drawLine((left + right) / 2f, bottom, left + (right - left) * 0.35f,
                bottom - (bottom - top) * 0.16f, iconPaint);
        canvas.drawLine((left + right) / 2f, bottom, right - (right - left) * 0.35f,
                bottom - (bottom - top) * 0.16f, iconPaint);
    }

    private void drawSettingsIcon(Canvas canvas, float left, float top, float right, float bottom) {
        float cx = (left + right) / 2f;
        float cy = (top + bottom) / 2f;
        float radius = (right - left) * 0.25f;
        canvas.drawCircle(cx, cy, radius, iconPaint);
        canvas.drawCircle(cx, cy, radius * 0.42f, iconPaint);
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2 * i / 8.0;
            float sx = cx + (float) Math.cos(angle) * radius;
            float sy = cy + (float) Math.sin(angle) * radius;
            float ex = cx + (float) Math.cos(angle) * radius * 1.35f;
            float ey = cy + (float) Math.sin(angle) * radius * 1.35f;
            canvas.drawLine(sx, sy, ex, ey, iconPaint);
        }
    }

    private void drawArrowIcon(Canvas canvas, float left, float top, float right, float bottom, boolean rightward) {
        float midY = (top + bottom) / 2f;
        float headX = rightward ? right : left;
        float tailX = rightward ? left : right;
        canvas.drawLine(tailX, midY, headX, midY, iconPaint);
        float dir = rightward ? -1f : 1f;
        canvas.drawLine(headX, midY, headX + dir * (right - left) * 0.28f, top, iconPaint);
        canvas.drawLine(headX, midY, headX + dir * (right - left) * 0.28f, bottom, iconPaint);
    }

    private void drawKeyShape(Canvas canvas, RectF bounds, Paint paint) {
        float radius = renderDp(settings.keyRoundnessDp);
        if (paint.getStyle() == Paint.Style.STROKE) {
            radius = Math.max(0f, radius - paint.getStrokeWidth() / 2f);
        }
        if (radius <= 0f) {
            canvas.drawRect(bounds, paint);
        } else {
            canvas.drawRoundRect(bounds, radius, radius, paint);
        }
    }

    private KeySlot findKey(float x, float y) {
        return TouchResolver.resolve(
                keySlots,
                x,
                y,
                dp(settings.hitSlopDp),
                dp(settings.touchYOffsetDp),
                dp(touchBias.xDp),
                dp(touchBias.yDp));
    }

    private String longPressRepeatValue(GestureKey key) {
        if (key == null) {
            return null;
        }
        if (isDeleteKey(key)) {
            return KeyboardCommands.CMD_DELETE;
        }
        if (!isInputRepeatKey(key)) {
            return null;
        }
        String longPressValue = key.valueFor(GestureAction.LONG_PRESS);
        if (isRepeatableInputText(longPressValue)) {
            return longPressValue;
        }
        return repeatableValue(key.tap);
    }

    private String repeatableValue(String value) {
        if (KeyboardCommands.CMD_DELETE.equals(value)
                || KeyboardCommands.CMD_MOVE_LEFT.equals(value)
                || KeyboardCommands.CMD_MOVE_RIGHT.equals(value)
                || KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(value)
                || KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(value)) {
            return value;
        }
        if (isRepeatableInputText(value)) {
            return value;
        }
        return null;
    }

    private boolean isInputRepeatKey(GestureKey key) {
        return key != null
                && (isRepeatableInputText(key.tap)
                || KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(key.tap)
                || KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(key.tap));
    }

    private boolean isRepeatableInputText(String value) {
        return value != null && !value.isEmpty() && !KeyboardCommands.isCommand(value);
    }

    private boolean isCursorMove(String value) {
        return KeyboardCommands.CMD_MOVE_LEFT.equals(value)
                || KeyboardCommands.CMD_MOVE_RIGHT.equals(value);
    }

    private boolean isDeleteKey(GestureKey key) {
        return key != null && KeyboardCommands.CMD_DELETE.equals(key.tap);
    }

    private boolean shouldShowSlideHints() {
        return settings.keyboardMode == KeyboardMode.ENGLISH
                ? settings.showEnglishSlideHints
                : settings.showHangulSlideHints;
    }

    private boolean drawsCustomModifierGlyph(GestureKey key, int icon) {
        if (key == null || icon == KeyIcon.NONE) {
            return false;
        }
        String pack = ModifierIconCatalog.effectivePackId(settings);
        return ModifierIconCatalog.rendersCustomGlyphs(pack);
    }

    private boolean shouldDrawSlideHintsForKey(GestureKey key, int icon) {
        if (key == null) {
            return false;
        }
        if (isSpaceKey(key)) {
            return showSpacebarSlideHints;
        }
        if (settings.keyboardMode == KeyboardMode.HANGUL) {
            boolean vowelKey = isHangulVowelHintKey(key);
            boolean consonantKey = isHangulConsonantHintKey(key);
            if (vowelKey && !showHangulVowelSlideHints) {
                return false;
            }
            if (consonantKey && !showHangulConsonantSlideHints) {
                return false;
            }
        }
        if (icon == KeyIcon.NONE) {
            return true;
        }
        return hasVisibleSlideHint(key.upSlide)
                || hasVisibleSlideHint(key.downSlide)
                || hasVisibleSlideHint(key.leftSlide)
                || hasVisibleSlideHint(key.rightSlide);
    }

    private boolean isHangulVowelHintKey(GestureKey key) {
        return key != null
                && (isDingulVowelCommand(key.tap)
                || isDingulVowelCommand(key.upSlide)
                || isDingulVowelCommand(key.downSlide)
                || isDingulVowelCommand(key.leftSlide)
                || isDingulVowelCommand(key.rightSlide)
                || hasHangulVowel(key.label)
                || hasHangulVowel(key.tap)
                || hasHangulVowel(key.upSlide)
                || hasHangulVowel(key.downSlide)
                || hasHangulVowel(key.leftSlide)
                || hasHangulVowel(key.rightSlide));
    }

    private boolean isHangulConsonantHintKey(GestureKey key) {
        return key != null
                && (hasHangulConsonant(key.label)
                || hasHangulConsonant(key.tap)
                || hasHangulConsonant(key.upSlide)
                || hasHangulConsonant(key.downSlide)
                || hasHangulConsonant(key.leftSlide)
                || hasHangulConsonant(key.rightSlide));
    }

    private boolean hasVisibleSlideHint(String value) {
        if (value == null || value.isEmpty() || KeyboardCommands.CMD_NOOP.equals(value)) {
            return false;
        }
        if (isReservedPhraseCommand(value)) {
            return false;
        }
        return displayFor(value) != null;
    }

    private String displayFor(String value) {
        if (isReservedPhraseCommand(value)) {
            String phrase = KeyboardPreferences.loadReservedPhraseForCommand(getContext(), value);
            return phrase == null || phrase.isEmpty() ? null : phrase;
        }
        return KeyboardCommands.labelFor(value);
    }

    private boolean isReservedPhraseCommand(String value) {
        return KeyboardCommands.CMD_RESERVED_PHRASES.equals(value)
                || KeyboardCommands.CMD_RESERVED_LEFT.equals(value)
                || KeyboardCommands.CMD_RESERVED_RIGHT.equals(value)
                || KeyboardCommands.CMD_RESERVED_UP.equals(value);
    }

    private boolean isDingulVowelCommand(String value) {
        return KeyboardCommands.CMD_DINGUL_CENTER_VOWEL.equals(value)
                || KeyboardCommands.CMD_DINGUL_WIDE_VOWEL.equals(value);
    }

    private boolean hasHangulConsonant(String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (HangulAutomata.isInitialConsonant(ch) || HangulAutomata.canBeFinalConsonant(ch)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasHangulVowel(String value) {
        if (value == null) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (HangulAutomata.isVowel(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private String textPresentation(String label) {
        return "♥".equals(label) ? "♥\uFE0E" : label;
    }

    private float textCenterOffset(Paint paint) {
        Paint.FontMetrics metrics = paint.getFontMetrics();
        return (metrics.ascent + metrics.descent) / 2f;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    private int renderDp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density * renderScale());
    }

    private float renderSp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity * renderScale();
    }

    private float renderScale() {
        return compactPreviewRendering ? 0.62f : 1f;
    }

    private float keyIconSize() {
        return KeyboardIconSizing.keyIconSizePx(getDensity()) * renderScale();
    }

    private float hintIconSize() {
        return KeyboardIconSizing.hintIconSizePx(getDensity()) * renderScale();
    }

    private float overlayIconSize() {
        return KeyboardIconSizing.overlayIconSizePx(getDensity()) * renderScale();
    }

    private int baseGestureThresholdPx() {
        return dp(settings.gestureThresholdDp);
    }

    private int gestureThresholdPxFor(GestureAction action) {
        return dp(settings.gestureThresholdDp + touchBias.gestureThresholdAdjustmentForDirection(action));
    }

    public interface OnKeyGestureListener {
        void onKeyGesture(String value);
    }

    interface OnPreviewKeySelectionListener {
        void onPreviewKeySelected(GestureKey key);
    }

    interface OnPreviewOverlayListener {
        void onPreviewOverlayChanged(PreviewOverlaySpec spec);

        void onPreviewOverlayHidden();
    }

    static final class PreviewOverlaySpec {
        final String label;
        final int x;
        final int y;
        final int width;
        final int height;
        final float textSizePx;
        final int textColor;
        final int backgroundColor;
        final int borderColor;
        final int borderWidthPx;
        final int cornerRadiusPx;
        final boolean angularBubble;

        PreviewOverlaySpec(
                String label,
                int x,
                int y,
                int width,
                int height,
                float textSizePx,
                int textColor,
                int backgroundColor,
                int borderColor,
                int borderWidthPx,
                int cornerRadiusPx,
                boolean angularBubble) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.textSizePx = textSizePx;
            this.textColor = textColor;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.borderWidthPx = borderWidthPx;
            this.cornerRadiusPx = cornerRadiusPx;
            this.angularBubble = angularBubble;
        }
    }

    private static final class TouchState {
        final int pointerId;
        final KeySlot keySlot;
        final GestureState gestureState = new GestureState();
        final float downX;
        final float downY;
        final long sequence;
        GestureAction activeAction = GestureAction.TAP;
        boolean longPressTriggered;
        boolean tapOutputAlreadyEmitted;
        Runnable longPressRunnable;

        TouchState(int pointerId, KeySlot keySlot, float downX, float downY, long sequence) {
            this.pointerId = pointerId;
            this.keySlot = keySlot;
            this.downX = downX;
            this.downY = downY;
            this.sequence = sequence;
        }
    }

    private static final class PendingTouchOutput {
        final long sequence;
        final KeySlot keySlot;
        final GestureAction action;
        final String value;
        final float x;
        final float y;

        PendingTouchOutput(
                long sequence,
                KeySlot keySlot,
                GestureAction action,
                String value,
                float x,
                float y) {
            this.sequence = sequence;
            this.keySlot = keySlot;
            this.action = action == null ? GestureAction.TAP : action;
            this.value = value;
            this.x = x;
            this.y = y;
        }
    }

    private static final class KeySlot implements TouchResolver.Target {
        final GestureKey key;
        final RectF bounds;
        final boolean primaryBottomControl;
        final boolean compactSpecialColumn;
        final float visualGap;

        KeySlot(
                GestureKey key,
                RectF bounds,
                boolean primaryBottomControl,
                boolean compactSpecialColumn,
                float visualGap) {
            this.key = key;
            this.bounds = bounds;
            this.primaryBottomControl = primaryBottomControl;
            this.compactSpecialColumn = compactSpecialColumn;
            this.visualGap = Math.max(0f, visualGap);
        }

        RectF visualBounds() {
            float insetX = Math.min(visualGap / 2f, bounds.width() * 0.18f);
            float insetY = Math.min(visualGap / 2f, bounds.height() * 0.18f);
            return new RectF(
                    bounds.left + insetX,
                    bounds.top + insetY,
                    bounds.right - insetX,
                    bounds.bottom - insetY);
        }

        @Override
        public boolean contains(float x, float y) {
            return visualBounds().contains(x, y);
        }

        @Override
        public boolean expandedContains(float x, float y, float slop) {
            RectF hitBounds = visualBounds();
            return x >= hitBounds.left - slop
                    && x <= hitBounds.right + slop
                    && y >= hitBounds.top - slop
                    && y <= hitBounds.bottom + slop;
        }

        @Override
        public float distanceSquaredTo(float x, float y) {
            RectF hitBounds = visualBounds();
            float nearestX = Math.max(hitBounds.left, Math.min(hitBounds.right, x));
            float nearestY = Math.max(hitBounds.top, Math.min(hitBounds.bottom, y));
            float dx = x - nearestX;
            float dy = y - nearestY;
            return dx * dx + dy * dy;
        }

        @Override
        public boolean isPrimaryBottomControl() {
            return primaryBottomControl;
        }
    }

    private static final class TouchSample {
        final String value;
        final float offsetXDp;
        final float offsetYDp;
        final GestureAction action;
        final long timeMs;

        TouchSample(String value, float offsetXDp, float offsetYDp, GestureAction action, long timeMs) {
            this.value = value == null ? "" : value;
            this.offsetXDp = offsetXDp;
            this.offsetYDp = offsetYDp;
            this.action = action == null ? GestureAction.TAP : action;
            this.timeMs = timeMs;
        }
    }
}
