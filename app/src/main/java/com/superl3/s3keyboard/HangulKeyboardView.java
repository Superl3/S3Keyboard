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
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class HangulKeyboardView extends View {
    private static final int TOOL_TYPE_PALM = 5;
    private static final long KEY_PRESS_ANIMATION_MS = 105;
    private static final long SLIDE_LOCK_ANIMATION_MS = 170;
    private static final long PREVIEW_POP_ANIMATION_MS = 120;
    private static final long PREVIEW_BUBBLE_ANIMATION_MS = 360;
    private static final long PREVIEW_RELEASE_ANIMATION_MS = 420;
    private static final int MAX_RELEASED_PREVIEW_BUBBLES = 4;
    private static final long LONG_PRESS_PULSE_MS = 280;
    private static final long MODE_TRANSITION_MS = 260;

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
    private final List<PreviewBubbleAnimation> releasedPreviewBubbles = new ArrayList<>();
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
    private int remoteLockedMetaState;
    private int previewPointerId = -1;
    private MotionEffectLevel motionEffectLevel = KeyboardPreferences.DEFAULT_MOTION_EFFECT_LEVEL;
    private long modeTransitionStartMs = -1;
    private boolean settingsInitialized;
    private boolean compactPreviewRendering;
    private boolean showHangulConsonantSlideHints = true;
    private boolean showHangulVowelSlideHints = true;
    private boolean showSpacebarSlideHints = true;
    private OnPreviewOverlayListener previewOverlayListener;
    private long nextTouchSequence;
    private TouchSample lastTextTouchSample;
    private boolean differentiatedHapticEnabled = true;
    private boolean touchBiasAutoCorrectionEnabled = true;
    private boolean palmRejectionEnabled;
    private long previewGestureGeneration;

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
        palmRejectionEnabled = KeyboardPreferences.loadPalmRejectionEnabled(context);
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
        KeyboardSettings previousSettings = this.settings;
        this.settings = settings == null ? KeyboardSettings.defaults() : settings;
        motionEffectLevel = KeyboardPreferences.loadMotionEffectLevel(getContext());
        maybeStartModeTransition(previousSettings, this.settings);
        feedback.setEnabled(this.settings.hapticFeedbackEnabled);
        feedback.reloadPreferences(getContext());
        differentiatedHapticEnabled = KeyboardPreferences.loadDifferentiatedHapticEnabled(getContext());
        touchBiasAutoCorrectionEnabled = KeyboardPreferences.loadTouchBiasAutoCorrectionEnabled(getContext());
        palmRejectionEnabled = KeyboardPreferences.loadPalmRejectionEnabled(getContext());
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
        settingsInitialized = true;
    }

    void setEnglishShiftState(boolean active, boolean locked) {
        englishShiftActive = active;
        englishCapsLocked = locked;
        invalidate();
    }

    void setRemoteMetaState(int pendingMetaState, int lockedMetaState) {
        remoteLockedMetaState = lockedMetaState;
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
        drawModeTransition(canvas);
        updatePreviewBubbles();
        scheduleNextAnimationFrameIfNeeded();
    }

    private void drawKeyboardPanel(Canvas canvas) {
        keyPaint.setShader(null);
        if (settings.visualEffects.panelGradientEnabled) {
            keyPaint.setShader(new LinearGradient(
                    0,
                    0,
                    0,
                    Math.max(1, getHeight()),
                    settings.visualEffects.panelGradientStartColor,
                    settings.visualEffects.panelGradientEndColor,
                    Shader.TileMode.CLAMP));
        } else {
            keyPaint.setColor(settings.keyboardBackgroundColor);
        }
        keyPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), keyPaint);
        keyPaint.setShader(null);
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

    private void maybeStartModeTransition(
            KeyboardSettings previous,
            KeyboardSettings next) {
        if (!settingsInitialized || next == null || !motionEffectsEnabled()) {
            return;
        }
        boolean modeChanged = previous.keyboardMode != next.keyboardMode;
        if (modeChanged) {
            modeTransitionStartMs = SystemClock.uptimeMillis();
        }
    }

    private void drawModeTransition(Canvas canvas) {
        if (modeTransitionStartMs < 0 || !motionEffectsEnabled()) {
            return;
        }
        float progress = clamp01(
                (SystemClock.uptimeMillis() - modeTransitionStartMs)
                        / (MODE_TRANSITION_MS * motionDurationScale()));
        if (progress >= 1f) {
            modeTransitionStartMs = -1;
            return;
        }
        float eased = easeOut(progress);
        int alpha = Math.round((1f - smoothStep(progress)) * 118f * motionIntensityScale());
        float sweepWidth = Math.max(renderDp(96), getWidth() * 0.52f);
        float left = -sweepWidth + (getWidth() + sweepWidth * 2f) * eased;
        float right = left + sweepWidth;
        overlayPaint.setStyle(Paint.Style.FILL);
        overlayPaint.setShader(new LinearGradient(
                left,
                0,
                right,
                0,
                new int[] {
                        withAlpha(settings.accentColor, 0),
                        withAlpha(settings.accentColor, alpha),
                        withAlpha(settings.accentColor, 0)
                },
                new float[] {0f, 0.48f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
        overlayPaint.setShader(null);

        float lineHeight = Math.max(renderDp(4), getHeight() * 0.018f);
        float y = getHeight() - lineHeight - renderDp(4);
        overlayPaint.setColor(withAlpha(settings.accentColor, Math.round(alpha * 0.92f)));
        canvas.drawRoundRect(left, y, right, y + lineHeight, lineHeight, lineHeight, overlayPaint);
    }

    private void scheduleNextAnimationFrameIfNeeded() {
        if (!motionEffectsEnabled()) {
            return;
        }
        boolean needsFrame = !activeTouches.isEmpty()
                || modeTransitionStartMs >= 0
                || !releasedPreviewBubbles.isEmpty();
        if (!needsFrame) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            postInvalidateOnAnimation();
        } else {
            postInvalidateDelayed(16);
        }
        updatePreviewPopup();
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
        if (isRejectedPalmTouch(event, pointerIndex)) {
            return true;
        }
        KeySlot keySlot = findKey(event.getX(pointerIndex), event.getY(pointerIndex));
        if (keySlot == null) {
            return false;
        }

        int pointerId = event.getPointerId(pointerIndex);
        boolean startingNewGesture = activeTouches.isEmpty();
        removeTouchState(pointerId);
        if (startingNewGesture) {
            previewGestureGeneration++;
        }
        pruneReleasedPreviewBubbles();
        TouchState state = new TouchState(
                pointerId,
                keySlot,
                event.getX(pointerIndex),
                event.getY(pointerIndex),
                nextTouchSequence++,
                previewGestureGeneration,
                SystemClock.uptimeMillis(),
                !keySlot.contains(event.getX(pointerIndex), event.getY(pointerIndex)));
        updatePreviewBubbleForTouch(state, false);
        activeTouches.add(state);
        if (previewPointerId == -1) {
            previewPointerId = pointerId;
        }
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
                baseGestureThresholdPx(state.keySlot.key),
                gestureThresholdPxFor(state.keySlot.key, GestureAction.UP),
                gestureThresholdPxFor(state.keySlot.key, GestureAction.DOWN),
                gestureThresholdPxFor(state.keySlot.key, GestureAction.LEFT),
                gestureThresholdPxFor(state.keySlot.key, GestureAction.RIGHT));
        if (state.gestureState.isLocked() && !wasLocked) {
            state.lockAnimationStartMs = SystemClock.uptimeMillis();
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
                    baseGestureThresholdPx(state.keySlot.key),
                    gestureThresholdPxFor(state.keySlot.key, GestureAction.UP),
                    gestureThresholdPxFor(state.keySlot.key, GestureAction.DOWN),
                    gestureThresholdPxFor(state.keySlot.key, GestureAction.LEFT),
                    gestureThresholdPxFor(state.keySlot.key, GestureAction.RIGHT));
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
                state.longPressAnimationStartMs = SystemClock.uptimeMillis();
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
        releasedPreviewBubbles.clear();
        previewGestureGeneration++;
        previewPointerId = -1;
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
        if (state.pointerId == previewPointerId) {
            enqueuePreviewBubble(state);
            previewPointerId = -1;
        }
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
        if (previewPointerId == -1) {
            return null;
        }
        return findTouchState(previewPointerId);
    }

    private boolean isActiveKey(KeySlot keySlot) {
        return touchForKeySlot(keySlot) != null;
    }

    private TouchState touchForKeySlot(KeySlot keySlot) {
        for (TouchState state : activeTouches) {
            if (state.keySlot == keySlot) {
                return state;
            }
        }
        return null;
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
        hintPaint.setFakeBoldText(true);
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
        TouchState touchState = touchForKeySlot(keySlot);
        boolean active = touchState != null;
        float pressProgress = keyPressProgress(touchState);
        boolean shiftOnceActive = isShiftKey(key) && englishShiftActive && !englishCapsLocked;
        boolean shiftLockedActive = isShiftKey(key) && englishCapsLocked;
        boolean remoteModifierLocked = isRemoteMetaLocked(key);
        boolean englishLetterKey = isEnglishLetterKey(key);
        RectF visualBounds = keySlot.visualBounds();
        RectF surfaceBounds = keySurfaceBounds(visualBounds, pressProgress);
        drawKeyDepth(canvas, keySlot, visualBounds, pressProgress);
        int faceColor = active || shiftOnceActive
                ? settings.keyPressedColor
                : baseColorForKey(keySlot);
        drawKeyFace(canvas, surfaceBounds, faceColor, pressProgress);
        drawBorderShape(canvas, surfaceBounds);
        drawHitSlopResolveCue(canvas, touchState, surfaceBounds);

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
                    ? englishMainLegendCenterY(surfaceBounds, key)
                    : surfaceBounds.centerY();
            float centerY = labelCenterY - textCenterOffset(textPaint);
            canvas.drawText(paintLabel, centerX, centerY, textPaint);
        } else {
            RectF iconBounds = iconSurfaceBoundsForKey(key, surfaceBounds);
            drawKeyIcon(canvas, key, icon, iconBounds, active);
        }
        if (shiftLockedActive || remoteModifierLocked) {
            drawModifierStateIndicator(canvas, surfaceBounds, false);
        }

        if (shouldShowSlideHints()
                && (displayOverride == null || settings.remoteModeEnabled)
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
                drawSpaceCursorHints(canvas, keySlot, key, surfaceBounds, hintTextSize);
            } else if (englishLetterKey) {
                drawEnglishSlideHints(canvas, keySlot, key, surfaceBounds, hintTextSize, bottomHintInset);
            } else {
                String topHint = settings.remoteModeEnabled && key.longPress != null
                        ? key.longPress
                        : key.upSlide;
                if (numberRowKey) {
                    drawNumberRowSlideHints(
                            canvas,
                            keySlot,
                            key,
                            surfaceBounds,
                            hintTextSize,
                            topHint,
                            bottomHintInset);
                    return;
                }
                drawHint(canvas, key, topHint, centerX, surfaceBounds.top + topHintInset,
                        hintTextSize, selectedHintScale(keySlot, GestureAction.UP));
                drawHint(canvas, key, key.downSlide, centerX, surfaceBounds.bottom - bottomHintInset,
                        hintTextSize, selectedHintScale(keySlot, GestureAction.DOWN));
                drawHint(canvas, key, key.leftSlide, surfaceBounds.left + horizontalHintInset,
                        surfaceBounds.centerY() - textCenterOffset(hintPaint),
                        hintTextSize, selectedHintScale(keySlot, GestureAction.LEFT));
                drawHint(canvas, key, key.rightSlide, surfaceBounds.right - horizontalHintInset,
                        surfaceBounds.centerY() - textCenterOffset(hintPaint),
                        hintTextSize, selectedHintScale(keySlot, GestureAction.RIGHT));
            }
        }
        drawLongPressPulse(canvas, touchState, surfaceBounds);
    }

    private void drawSpaceCursorHints(
            Canvas canvas,
            KeySlot keySlot,
            GestureKey key,
            RectF surfaceBounds,
            float hintTextSize) {
        float y = surfaceBounds.centerY();
        float inset = Math.min(renderDp(28), surfaceBounds.width() * 0.10f);
        if (settings.remoteModeEnabled) {
            drawHint(canvas, key, key.upSlide, surfaceBounds.centerX(),
                    surfaceBounds.top + topHintInset(surfaceBounds, null, false, false),
                    hintTextSize, selectedHintScale(keySlot, GestureAction.UP));
            drawHint(canvas, key, key.downSlide, surfaceBounds.centerX(),
                    surfaceBounds.bottom - bottomHintInset(surfaceBounds, false, false),
                    hintTextSize, selectedHintScale(keySlot, GestureAction.DOWN));
        }
        drawHint(canvas, key, key.leftSlide, surfaceBounds.left + inset, y,
                hintTextSize, selectedHintScale(keySlot, GestureAction.LEFT));
        drawHint(canvas, key, key.rightSlide, surfaceBounds.right - inset, y,
                hintTextSize, selectedHintScale(keySlot, GestureAction.RIGHT));
    }

    private float englishMainLegendCenterY(RectF surfaceBounds, GestureKey key) {
        float ratio = settings.remoteModeEnabled && KeyboardCommands.isRemoteCommand(key.upSlide)
                ? 0.34f
                : 0.36f;
        return surfaceBounds.top + surfaceBounds.height() * ratio;
    }

    private RectF iconSurfaceBoundsForKey(GestureKey key, RectF surfaceBounds) {
        if (!settings.remoteModeEnabled || !isSpaceKey(key)) {
            return surfaceBounds;
        }
        RectF adjusted = new RectF(surfaceBounds);
        adjusted.offset(0, -Math.min(renderDp(5), surfaceBounds.height() * 0.08f));
        return adjusted;
    }

    private void drawNumberRowSlideHints(
            Canvas canvas,
            KeySlot keySlot,
            GestureKey key,
            RectF surfaceBounds,
            float hintTextSize,
            String topHint,
            float bottomHintInset) {
        drawHint(canvas, key, topHint, surfaceBounds.centerX(), remoteTopHintY(surfaceBounds),
                hintTextSize, selectedHintScale(keySlot, GestureAction.UP));
        drawHint(canvas, key, key.downSlide, surfaceBounds.centerX(), surfaceBounds.bottom - bottomHintInset,
                hintTextSize, selectedHintScale(keySlot, GestureAction.DOWN));
        float y = surfaceBounds.bottom - bottomHintInset;
        drawHint(canvas, key, key.leftSlide, surfaceBounds.left + surfaceBounds.width() * 0.32f, y,
                hintTextSize, selectedHintScale(keySlot, GestureAction.LEFT));
        drawHint(canvas, key, key.rightSlide, surfaceBounds.right - surfaceBounds.width() * 0.32f, y,
                hintTextSize, selectedHintScale(keySlot, GestureAction.RIGHT));
    }

    private void drawDotLegend(Canvas canvas, GestureKey key, RectF surfaceBounds, boolean englishLetterKey) {
        textPaint.setColor(KeyboardKeyVisualClassifier.textColorFor(settings, key));
        if (isDingulPunctuationDotGlyph(key)) {
            drawTwoDotLegend(canvas, surfaceBounds, textPaint.getColor());
            return;
        }
        float radius = dotRadiusFor(surfaceBounds);
        float centerY = englishLetterKey
                ? surfaceBounds.top + surfaceBounds.height() * 0.36f
                : surfaceBounds.centerY();
        canvas.drawCircle(surfaceBounds.centerX(), centerY, radius, textPaint);
    }

    private boolean isDingulPunctuationDotGlyph(GestureKey key) {
        return settings.keyboardMode == KeyboardMode.HANGUL
                && key != null
                && (".".equals(key.label) || "/".equals(key.label)
                || ".".equals(key.tap) || "/".equals(key.tap));
    }

    private void drawTwoDotLegend(Canvas canvas, RectF bounds, int color) {
        textPaint.setColor(color);
        float radius = dotRadiusFor(bounds);
        float gap = Math.max(radius * 1.65f, renderDp(3.0f));
        float cx = bounds.centerX();
        float cy = bounds.centerY();
        canvas.drawCircle(cx - gap / 2f, cy, radius, textPaint);
        canvas.drawCircle(cx + gap / 2f, cy, radius, textPaint);
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
        if (ModifierIconCatalog.GLYPH_ESC.equals(override.value)) {
            drawEscDisplayOverride(canvas, key, surfaceBounds);
            return;
        }
        textPaint.setColor(KeyboardKeyVisualClassifier.textColorFor(settings, key));
        textPaint.setTextSize(textSizeFor(override.value, surfaceBounds, false));
        float centerY = surfaceBounds.centerY() - textCenterOffset(textPaint);
        canvas.drawText(override.value, surfaceBounds.centerX(), centerY, textPaint);
    }

    private void drawEscDisplayOverride(Canvas canvas, GestureKey key, RectF surfaceBounds) {
        int color = KeyboardKeyVisualClassifier.textColorFor(settings, key);
        String packId = ModifierIconCatalog.effectivePackId(settings);
        if (ModifierIconCatalog.isDotsLinePack(packId)) {
            drawSingleModifierDot(canvas, surfaceBounds, color);
            return;
        }
        if (ModifierIconCatalog.isMetropolisPack(packId)) {
            drawMetropolisEscGlyph(canvas, surfaceBounds, color);
            return;
        }
        textPaint.setColor(color);
        textPaint.setTypeface(KeyboardTypefaceCatalog.typefaceFor(getContext(), settings.fontFamily, true, false));
        textPaint.setTextSize(textSizeFor("Esc", surfaceBounds, false) * 0.72f);
        canvas.drawText("Esc", surfaceBounds.centerX(), surfaceBounds.centerY() - textCenterOffset(textPaint), textPaint);
    }

    private void drawMetropolisEscGlyph(Canvas canvas, RectF bounds, int color) {
        modifierIconPaint.reset();
        modifierIconPaint.setAntiAlias(true);
        modifierIconPaint.setStyle(Paint.Style.STROKE);
        modifierIconPaint.setStrokeCap(Paint.Cap.ROUND);
        modifierIconPaint.setColor(color);
        modifierIconPaint.setStrokeWidth(Math.max(renderDp(1.6f), bounds.height() * 0.04f));
        float left = bounds.left + bounds.width() * 0.32f;
        float right = bounds.right - bounds.width() * 0.32f;
        float top = bounds.top + bounds.height() * 0.40f;
        float middle = bounds.centerY();
        float bottom = bounds.top + bounds.height() * 0.60f;
        canvas.drawLine(left, top, right, top, modifierIconPaint);
        canvas.drawLine(left, middle, right * 0.94f + left * 0.06f, middle, modifierIconPaint);
        canvas.drawLine(left, bottom, right, bottom, modifierIconPaint);
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
            KeySlot keySlot,
            GestureKey key,
            RectF surfaceBounds,
            float hintTextSize,
            float bottomHintInset) {
        float y = surfaceBounds.bottom - bottomHintInset;
        hintPaint.setTextSize(hintTextSize);
        String topValue = key.longPress != null
                ? key.longPress
                : (settings.remoteModeEnabled && KeyboardCommands.isRemoteCommand(key.upSlide) ? key.upSlide : null);
        if (topValue != null) {
            drawHint(canvas, key, topValue, surfaceBounds.centerX(),
                    remoteTopHintY(surfaceBounds), hintTextSize, selectedHintScale(keySlot, GestureAction.UP));
        }
        y = surfaceBounds.top + surfaceBounds.height() * 0.73f - textCenterOffset(hintPaint);
        boolean hasLeft = displayFor(key.leftSlide) != null;
        boolean hasRight = displayFor(key.rightSlide) != null;
        if (hasLeft && hasRight) {
            drawHint(canvas, key, key.leftSlide, surfaceBounds.left + surfaceBounds.width() * 0.32f, y,
                    hintTextSize, selectedHintScale(keySlot, GestureAction.LEFT));
            drawHint(canvas, key, key.rightSlide, surfaceBounds.right - surfaceBounds.width() * 0.32f, y,
                    hintTextSize, selectedHintScale(keySlot, GestureAction.RIGHT));
        } else if (hasLeft) {
            drawHint(canvas, key, key.leftSlide, surfaceBounds.left + surfaceBounds.width() * 0.32f, y,
                    hintTextSize, selectedHintScale(keySlot, GestureAction.LEFT));
        } else if (hasRight) {
            drawHint(canvas, key, key.rightSlide, surfaceBounds.right - surfaceBounds.width() * 0.32f, y,
                    hintTextSize, selectedHintScale(keySlot, GestureAction.RIGHT));
        }
        if (displayFor(key.downSlide) != null) {
            drawHint(canvas, key, key.downSlide, surfaceBounds.centerX(), y,
                    hintTextSize, selectedHintScale(keySlot, GestureAction.DOWN));
        }
    }

    private float remoteTopHintY(RectF surfaceBounds) {
        hintPaint.setTextSize(renderSp(8.4f) * secondaryTextScale());
        return surfaceBounds.top + surfaceBounds.height() * 0.22f - textCenterOffset(hintPaint);
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

    private float keyPressProgress(TouchState state) {
        if (state == null) {
            return 0f;
        }
        if (!motionEffectsEnabled()) {
            return 1f;
        }
        float duration = KEY_PRESS_ANIMATION_MS * motionDurationScale();
        return easeOut(clamp01((SystemClock.uptimeMillis() - state.downTimeMs) / duration));
    }

    private float selectedHintScale(KeySlot keySlot, GestureAction action) {
        TouchState state = touchForKeySlot(keySlot);
        if (state == null
                || !motionEffectsEnabled()
                || !state.gestureState.isLocked()
                || state.activeAction != action) {
            return 1f;
        }
        float pulse = 1f - clamp01(
                (SystemClock.uptimeMillis() - state.lockAnimationStartMs)
                        / (SLIDE_LOCK_ANIMATION_MS * motionDurationScale()));
        return 1.16f + 0.10f * pulse * motionIntensityScale();
    }

    private void drawHitSlopResolveCue(Canvas canvas, TouchState state, RectF bounds) {
        if (state == null || !state.hitSlopResolved || !motionEffectsEnabled()) {
            return;
        }
        float progress = 1f - clamp01(
                (SystemClock.uptimeMillis() - state.downTimeMs)
                        / (KEY_PRESS_ANIMATION_MS * 1.8f * motionDurationScale()));
        if (progress <= 0f) {
            return;
        }
        overlayPaint.setStyle(Paint.Style.STROKE);
        overlayPaint.setStrokeWidth(Math.max(renderDp(1), renderDp(settings.keyBorderWidthDp + 1)));
        overlayPaint.setColor(withAlpha(settings.accentColor, Math.round(80 * progress * motionIntensityScale())));
        RectF cueBounds = new RectF(bounds);
        cueBounds.inset(overlayPaint.getStrokeWidth(), overlayPaint.getStrokeWidth());
        drawKeyShape(canvas, cueBounds, overlayPaint);
        overlayPaint.setStyle(Paint.Style.FILL);
    }

    private void drawLongPressPulse(Canvas canvas, TouchState state, RectF bounds) {
        if (state == null || state.longPressAnimationStartMs < 0 || !motionEffectsEnabled()) {
            return;
        }
        float progress = clamp01(
                (SystemClock.uptimeMillis() - state.longPressAnimationStartMs)
                        / (LONG_PRESS_PULSE_MS * motionDurationScale()));
        if (progress >= 1f) {
            return;
        }
        float alpha = (1f - progress) * 90f * motionIntensityScale();
        float inset = -renderDp(2) * (1f + progress);
        RectF pulseBounds = new RectF(bounds);
        pulseBounds.inset(inset, inset);
        overlayPaint.setStyle(Paint.Style.STROKE);
        overlayPaint.setStrokeWidth(Math.max(renderDp(1), renderDp(2) * (1f - progress * 0.45f)));
        overlayPaint.setColor(withAlpha(settings.accentColor, Math.round(alpha)));
        drawKeyShape(canvas, pulseBounds, overlayPaint);
        overlayPaint.setStyle(Paint.Style.FILL);
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

    private void drawKeyFace(Canvas canvas, RectF bounds, int faceColor, float pressProgress) {
        keyPaint.setStyle(Paint.Style.FILL);
        if (!shouldDrawKeyFaceGradient()) {
            keyPaint.setShader(null);
            keyPaint.setColor(faceColor);
            drawKeyShape(canvas, bounds, keyPaint);
            return;
        }
        int[] colors = keyFaceGradientColors(faceColor, pressProgress);
        keyPaint.setShader(new LinearGradient(
                0,
                bounds.top,
                0,
                bounds.bottom,
                colors,
                new float[] { 0f, 0.42f, 1f },
                Shader.TileMode.CLAMP));
        drawKeyShape(canvas, bounds, keyPaint);
        keyPaint.setShader(null);
    }

    private boolean shouldDrawKeyFaceGradient() {
        return settings.keyDepthEnabled
                && settings.keyDepthDp > 0
                && settings.visualEffects.keyFaceGradientEnabled
                && settings.visualEffects.keyFaceGradientStrengthPercent > 0;
    }

    private RectF keySurfaceBounds(RectF bounds, float pressProgress) {
        if (!settings.keyDepthEnabled || settings.keyDepthDp <= 0) {
            return new RectF(bounds);
        }
        float pressOffset = Math.min(renderDp(settings.keyDepthDp) * 0.60f, bounds.height() * 0.06f)
                * pressProgress;
        return new RectF(bounds.left, bounds.top + pressOffset, bounds.right, bounds.bottom + pressOffset);
    }

    private void drawKeyDepth(Canvas canvas, KeySlot keySlot, RectF bounds, float pressProgress) {
        if (!settings.keyDepthEnabled || settings.keyDepthDp <= 0) {
            return;
        }
        float configuredDepth = renderDp(settings.keyDepthDp);
        float fullDepth = Math.min(configuredDepth, bounds.height() * 0.12f);
        float pressedDepth = Math.min(configuredDepth * 0.35f, bounds.height() * 0.035f);
        float depth = fullDepth + (pressedDepth - fullDepth) * pressProgress;
        if (depth <= 0f) {
            return;
        }
        RectF depthBounds = new RectF(bounds.left, bounds.top + depth, bounds.right, bounds.bottom + depth);
        depthPaint.setColor(depthColor(keySlot, pressProgress));
        drawKeyShape(canvas, depthBounds, depthPaint);
    }

    private int baseColorForKey(KeySlot keySlot) {
        return KeyboardKeyVisualClassifier.colorFor(settings, keySlot.key);
    }

    private int previewBubbleBackgroundFor(KeySlot keySlot) {
        int baseColor = baseColorForKey(keySlot);
        int r = (baseColor >> 16) & 0xFF;
        int g = (baseColor >> 8) & 0xFF;
        int b = baseColor & 0xFF;
        int luminance = (r * 299 + g * 587 + b * 114) / 1000;
        return luminance > 150 ? darkenColor(baseColor, 0.94f) : lightenColor(baseColor, 1.12f);
    }

    private int depthColor(KeySlot keySlot, float pressProgress) {
        if (settings.customDepthColorEnabled) {
            return shadeColor(settings.depthColor, 0.88f + (0.72f - 0.88f) * pressProgress);
        }
        int keyBackground = baseColorForKey(keySlot);
        return dimmedDepthColorForBackground(keyBackground, pressProgress);
    }

    private int dimmedDepthColorForBackground(int background, float pressProgress) {
        int luminance = perceivedLuminance(background);
        float amount = 0.16f + 0.08f * pressProgress;
        if (luminance < 42) {
            return blendColor(0xFFFFFFFF, background, 0.10f + 0.04f * pressProgress);
        }
        return blendColor(0xFF000000, background, amount);
    }

    private int[] keyFaceGradientColors(int background, float pressProgress) {
        float strength = settings.visualEffects.keyFaceGradientStrengthPercent / 100f;
        strength *= 1f - 0.35f * clamp01(pressProgress);
        int luminance = perceivedLuminance(background);
        float topAmount = (luminance < 42 ? 0.08f : 0.06f) + 0.24f * strength;
        float bottomAmount = (luminance < 42 ? 0.04f : 0.05f) + 0.18f * strength;
        return new int[] {
                blendColor(0xFFFFFFFF, background, topAmount),
                background,
                blendColor(0xFF000000, background, bottomAmount)
        };
    }

    private int perceivedLuminance(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (r * 299 + g * 587 + b * 114) / 1000;
    }

    private int blendColor(int foreground, int background, float foregroundAmount) {
        float amount = clamp01(foregroundAmount);
        float inverse = 1f - amount;
        int a = Math.round(((foreground >>> 24) & 0xFF) * amount
                + ((background >>> 24) & 0xFF) * inverse);
        int r = Math.round(((foreground >> 16) & 0xFF) * amount
                + ((background >> 16) & 0xFF) * inverse);
        int g = Math.round(((foreground >> 8) & 0xFF) * amount
                + ((background >> 8) & 0xFF) * inverse);
        int b = Math.round((foreground & 0xFF) * amount
                + (background & 0xFF) * inverse);
        return (clampColor(a) << 24)
                | (clampColor(r) << 16)
                | (clampColor(g) << 8)
                | clampColor(b);
    }

    private int shadeColor(int color, float factor) {
        int a = color & 0xFF000000;
        int r = Math.round(((color >> 16) & 0xFF) * factor);
        int g = Math.round(((color >> 8) & 0xFF) * factor);
        int b = Math.round((color & 0xFF) * factor);
        return a | (clampColor(r) << 16) | (clampColor(g) << 8) | clampColor(b);
    }

    private boolean motionEffectsEnabled() {
        return motionEffectLevel != MotionEffectLevel.OFF;
    }

    private float motionIntensityScale() {
        return motionEffectLevel == MotionEffectLevel.NORMAL ? 1f : 0.58f;
    }

    private float motionDurationScale() {
        return motionEffectLevel == MotionEffectLevel.NORMAL ? 1f : 0.82f;
    }

    private float easeOut(float value) {
        float t = clamp01(value);
        return 1f - (1f - t) * (1f - t);
    }

    private float smoothStep(float value) {
        float t = clamp01(value);
        return t * t * (3f - 2f * t);
    }

    private float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
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
        drawHint(canvas, key, value, x, y, textSize, 1f);
    }

    private void drawHint(
            Canvas canvas,
            GestureKey key,
            String value,
            float x,
            float y,
            float textSize,
            float scale) {
        int icon = KeyIcon.forCommand(value);
        float effectiveScale = Math.max(0.75f, scale);
        if (icon != KeyIcon.NONE) {
            drawIconCentered(canvas, icon, x, y, hintIconSize() * effectiveScale, false);
            return;
        }

        String label = displayFor(value);
        if (label != null && label.length() <= 4) {
            hintPaint.setColor(scale > 1.02f
                    ? settings.accentColor
                    : KeyboardKeyVisualClassifier.hintColorFor(settings, key));
            hintPaint.setTextSize(textSize * effectiveScale);
            hintPaint.setFakeBoldText(true);
            canvas.drawText(textPresentation(label), x, y, hintPaint);
        }
    }

    private void updatePreviewPopup() {
        updatePreviewBubbles();
    }

    private void updatePreviewBubbles() {
        if (!settings.showBeginnerTooltipPreview) {
            releasedPreviewBubbles.clear();
            hidePreviewPopup();
            return;
        }
        pruneReleasedPreviewBubbles();
        List<PreviewOverlaySpec> specs = new ArrayList<>();
        for (PreviewBubbleAnimation bubble : releasedPreviewBubbles) {
            PreviewOverlaySpec spec = previewBubbleSpec(bubble);
            if (spec != null) {
                specs.add(spec);
            }
        }
        for (TouchState state : activeTouches) {
            PreviewBubbleAnimation bubble = previewBubbleForTouch(state);
            if (bubble != null) {
                PreviewOverlaySpec spec = previewBubbleSpec(bubble);
                if (spec != null) {
                    specs.add(spec);
                }
            }
        }
        if (previewOverlayListener == null) {
            return;
        }
        if (specs.isEmpty()) {
            previewOverlayListener.onPreviewOverlayHidden();
        } else {
            previewOverlayListener.onPreviewOverlaysChanged(specs);
        }
    }

    private PreviewOverlaySpec previewBubbleSpec(PreviewBubbleAnimation bubble) {
        overlayTextPaint.setTextSize(overlayTextSizeFor(bubble.label));
        overlayTextPaint.setTypeface(KeyboardTypefaceCatalog.typefaceFor(
                getContext(),
                settings.fontFamily,
                settings.primaryTextBold,
                settings.primaryTextItalic));
        int popupWidth = Math.min(renderDp(92), Math.max(
                renderDp(48),
                Math.round(overlayTextPaint.measureText(bubble.label)) + renderDp(28)));
        int popupHeight = renderDp(61);
        float previewProgress = previewPopProgress(bubble);
        float previewMotionProgress = previewMotionProgress(bubble);
        float previewScale = 0.99f + 0.01f * previewProgress;
        int previewLift = previewBubbleLift(previewMotionProgress);
        int x = Math.round(clamp(
                bubble.anchorCenterX - popupWidth / 2f,
                renderDp(2),
                Math.max(renderDp(2), getWidth() - popupWidth - renderDp(2))));
        float preferredY = bubble.anchorTop - renderDp(3) - popupHeight - previewLift;
        int y = Math.round(preferredY);
        float alpha = previewBubbleAlpha(bubble);
        if (alpha <= 0f) {
            return null;
        }
        return new PreviewOverlaySpec(
                bubble.label,
                x,
                y,
                popupWidth,
                popupHeight,
                overlayTextSizeFor(bubble.label),
                bubble.textColor,
                bubble.backgroundColor,
                bubble.borderColor,
                bubble.borderWidthPx,
                previewBubbleCornerRadius(),
                true,
                alpha,
                previewScale);
    }

    private PreviewBubbleAnimation previewBubbleForTouch(TouchState state) {
        if (state == null || !shouldShowPreviewForTouch(state)) {
            return null;
        }
        updatePreviewBubbleForTouch(state, false);
        return state.previewBubble;
    }

    private void updatePreviewBubbleForTouch(TouchState state, boolean released) {
        String value = previewValueForTouch(state);
        String label = displayFor(value);
        if (label == null) {
            state.previewBubble = null;
            return;
        }
        String presentation = textPresentation(label);
        if (state.previewBubble == null) {
            state.previewBubble = previewBubbleSnapshot(state, state.downTimeMs, presentation, released);
        } else {
            state.previewBubble.update(
                    presentation,
                    KeyboardKeyVisualClassifier.textColorFor(settings, state.keySlot.key),
                    previewBubbleBackgroundFor(state.keySlot),
                    settings.borderColor,
                    renderDp(settings.keyBorderWidthDp),
                    released);
        }
        if (released) {
            state.previewBubble.markReleased(SystemClock.uptimeMillis());
        }
    }

    private void enqueuePreviewBubble(TouchState state) {
        if (!motionEffectsEnabled()) {
            return;
        }
        if (state.previewGeneration != previewGestureGeneration) {
            return;
        }
        updatePreviewBubbleForTouch(state, true);
        PreviewBubbleAnimation bubble = state.previewBubble;
        if (bubble == null) {
            return;
        }
        releasedPreviewBubbles.remove(bubble);
        releasedPreviewBubbles.add(bubble);
        trimReleasedPreviewBubbles();
    }

    private void pruneReleasedPreviewBubbles() {
        for (int i = releasedPreviewBubbles.size() - 1; i >= 0; i--) {
            if (previewBubbleExpired(releasedPreviewBubbles.get(i))) {
                releasedPreviewBubbles.remove(i);
            }
        }
        if (!releasedPreviewBubbles.isEmpty()) {
            trimReleasedPreviewBubbles();
        }
    }

    private void trimReleasedPreviewBubbles() {
        while (releasedPreviewBubbles.size() > MAX_RELEASED_PREVIEW_BUBBLES) {
            releasedPreviewBubbles.remove(0);
        }
    }

    private PreviewBubbleAnimation previewBubbleSnapshot(
            TouchState state,
            long startTimeMs,
            String label,
            boolean released) {
        RectF anchor = state.keySlot.visualBounds();
        return new PreviewBubbleAnimation(
                label,
                state.sequence,
                anchor.centerX(),
                anchor.top,
                anchor.bottom,
                KeyboardKeyVisualClassifier.textColorFor(settings, state.keySlot.key),
                previewBubbleBackgroundFor(state.keySlot),
                settings.borderColor,
                renderDp(settings.keyBorderWidthDp),
                startTimeMs,
                released);
    }

    private int previewBubbleLift(float progress) {
        if (!motionEffectsEnabled()) {
            return 0;
        }
        float peakLiftDp = 9f * motionIntensityScale();
        float settleLiftDp = 6f * motionIntensityScale();
        if (progress < 0.30f) {
            return Math.round(renderDp(peakLiftDp) * smoothStep(progress / 0.30f));
        } else if (progress < 0.52f) {
            float descend = smoothStep((progress - 0.30f) / 0.22f);
            return Math.round(renderDp(peakLiftDp + (settleLiftDp - peakLiftDp) * descend));
        }
        return Math.round(renderDp(settleLiftDp));
    }

    private int previewBubbleCornerRadius() {
        int keyRadius = renderDp(settings.keyRoundnessDp);
        if (settings.visualEffects.angularPreviewBubble) {
            return Math.max(renderDp(2), Math.min(renderDp(6), keyRadius));
        }
        return Math.max(renderDp(2), Math.min(renderDp(18), keyRadius));
    }

    private float previewPopProgress(PreviewBubbleAnimation bubble) {
        if (bubble == null || !motionEffectsEnabled()) {
            return 1f;
        }
        return easeOut(clamp01(
                (SystemClock.uptimeMillis() - bubble.startTimeMs)
                        / (PREVIEW_POP_ANIMATION_MS * motionDurationScale())));
    }

    private float previewMotionProgress(PreviewBubbleAnimation bubble) {
        if (bubble == null || !motionEffectsEnabled()) {
            return 1f;
        }
        return clamp01(
                (SystemClock.uptimeMillis() - bubble.startTimeMs)
                        / (PREVIEW_BUBBLE_ANIMATION_MS * motionDurationScale()));
    }

    private float previewBubbleAlpha(PreviewBubbleAnimation bubble) {
        if (!motionEffectsEnabled()) {
            return 1f;
        }
        if (!bubble.released) {
            return 1f;
        }
        float progress = previewReleaseProgress(bubble);
        if (progress < 0.45f) {
            return 1f;
        }
        return 1f - smoothStep((progress - 0.45f) / 0.55f);
    }

    private float previewReleaseProgress(PreviewBubbleAnimation bubble) {
        if (bubble == null || !bubble.released || !motionEffectsEnabled()) {
            return 0f;
        }
        return clamp01(
                (SystemClock.uptimeMillis() - bubble.releaseTimeMs)
                        / (PREVIEW_RELEASE_ANIMATION_MS * motionDurationScale()));
    }

    private boolean previewBubbleExpired(PreviewBubbleAnimation bubble) {
        return bubble != null && bubble.released && previewReleaseProgress(bubble) >= 1f;
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

    private boolean isRemoteMetaLocked(GestureKey key) {
        int meta = remoteMetaForKey(key);
        return meta != 0 && (remoteLockedMetaState & meta) == meta;
    }

    private int remoteMetaForKey(GestureKey key) {
        if (key == null) {
            return 0;
        }
        if (KeyboardCommands.CMD_REMOTE_CTRL_LATCH.equals(key.tap)) {
            return KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
        }
        if (KeyboardCommands.CMD_REMOTE_WIN_LATCH.equals(key.tap)) {
            return KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON;
        }
        if (KeyboardCommands.CMD_REMOTE_ALT_LATCH.equals(key.tap)) {
            return KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;
        }
        return 0;
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

    private boolean isRejectedPalmTouch(MotionEvent event, int pointerIndex) {
        if (!palmRejectionEnabled || event == null || pointerIndex < 0 || pointerIndex >= event.getPointerCount()) {
            return false;
        }
        int toolType = event.getToolType(pointerIndex);
        if (toolType == TOOL_TYPE_PALM) {
            return true;
        }
        if (toolType != MotionEvent.TOOL_TYPE_FINGER && toolType != MotionEvent.TOOL_TYPE_UNKNOWN) {
            return false;
        }
        float touchMajor = event.getTouchMajor(pointerIndex);
        return touchMajor > Math.max(renderDp(42), getHeight() * 0.16f);
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
        drawModifierLine(canvas, bounds, color, dotsLineSidePaddingRatio(icon));
    }

    private float dotsLineSidePaddingRatio(int icon) {
        if (icon == KeyIcon.BACKSPACE || icon == KeyIcon.SHIFT || icon == KeyIcon.CAPS_LOCK) {
            return 0.30f;
        }
        if (icon == KeyIcon.OPTIONS
                || icon == KeyIcon.SETTINGS
                || icon == KeyIcon.ENTER
                || icon == KeyIcon.DONE
                || icon == KeyIcon.NEXT) {
            return 0.39f;
        }
        return 0.34f;
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
        return dotsLineWeightFor(bounds) * 0.69f;
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

    private void drawModifierStateIndicator(Canvas canvas, RectF bounds, boolean pendingOnly) {
        float radius = Math.min(renderDp(5.2f), bounds.height() * 0.085f);
        float cx = (bounds.centerX() + bounds.right) / 2f;
        cx = Math.min(bounds.right - radius - renderDp(6), cx);
        float cy = bounds.centerY() - keyIconSize() * 1.08f - renderDp(1);
        cy = Math.max(bounds.top + radius + Math.min(renderDp(4), bounds.height() * 0.07f), cy);
        int color = KeyboardKeyVisualClassifier.shiftIndicatorColorFor(settings);
        iconPaint.setColor(color);
        if (pendingOnly) {
            iconPaint.setStyle(Paint.Style.STROKE);
            iconPaint.setStrokeWidth(Math.max(renderDp(1.4f), radius * 0.32f));
            canvas.drawCircle(cx, cy, radius, iconPaint);
            iconPaint.setStyle(Paint.Style.FILL);
            return;
        }
        iconPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, radius, iconPaint);
        iconPaint.setColor(contrastColor(color));
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
                || KeyboardCommands.CMD_SPACE.equals(value)
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
                || KeyboardCommands.CMD_SPACE.equals(key.tap)
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

    private int baseGestureThresholdPx(GestureKey key) {
        return dp(GestureThresholdPolicy.baseThresholdDp(settings, key));
    }

    private int gestureThresholdPxFor(GestureKey key, GestureAction action) {
        return dp(GestureThresholdPolicy.thresholdDp(settings, touchBias, key, action));
    }

    public interface OnKeyGestureListener {
        void onKeyGesture(String value);
    }

    interface OnPreviewKeySelectionListener {
        void onPreviewKeySelected(GestureKey key);
    }

    interface OnPreviewOverlayListener {
        void onPreviewOverlayChanged(PreviewOverlaySpec spec);

        void onPreviewOverlaysChanged(List<PreviewOverlaySpec> specs);

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
        final float alpha;
        final float scale;

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
                boolean angularBubble,
                float alpha,
                float scale) {
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
            this.alpha = alpha;
            this.scale = scale;
        }
    }

    private static final class TouchState {
        final int pointerId;
        final KeySlot keySlot;
        final GestureState gestureState = new GestureState();
        final float downX;
        final float downY;
        final long sequence;
        final long previewGeneration;
        final long downTimeMs;
        final boolean hitSlopResolved;
        GestureAction activeAction = GestureAction.TAP;
        boolean longPressTriggered;
        boolean tapOutputAlreadyEmitted;
        long lockAnimationStartMs = -1;
        long longPressAnimationStartMs = -1;
        Runnable longPressRunnable;
        PreviewBubbleAnimation previewBubble;

        TouchState(
                int pointerId,
                KeySlot keySlot,
                float downX,
                float downY,
                long sequence,
                long previewGeneration,
                long downTimeMs,
                boolean hitSlopResolved) {
            this.pointerId = pointerId;
            this.keySlot = keySlot;
            this.downX = downX;
            this.downY = downY;
            this.sequence = sequence;
            this.previewGeneration = previewGeneration;
            this.downTimeMs = downTimeMs;
            this.hitSlopResolved = hitSlopResolved;
        }
    }

    private static final class PreviewBubbleAnimation {
        String label;
        final long sequence;
        final float anchorCenterX;
        final float anchorTop;
        final float anchorBottom;
        int textColor;
        int backgroundColor;
        int borderColor;
        int borderWidthPx;
        final long startTimeMs;
        long releaseTimeMs;
        boolean released;

        PreviewBubbleAnimation(
                String label,
                long sequence,
                float anchorCenterX,
                float anchorTop,
                float anchorBottom,
                int textColor,
                int backgroundColor,
                int borderColor,
                int borderWidthPx,
                long startTimeMs,
                boolean released) {
            this.label = label;
            this.sequence = sequence;
            this.anchorCenterX = anchorCenterX;
            this.anchorTop = anchorTop;
            this.anchorBottom = anchorBottom;
            this.textColor = textColor;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.borderWidthPx = borderWidthPx;
            this.startTimeMs = startTimeMs;
            this.releaseTimeMs = startTimeMs;
            this.released = released;
        }

        void update(
                String label,
                int textColor,
                int backgroundColor,
                int borderColor,
                int borderWidthPx,
                boolean released) {
            this.label = label;
            this.textColor = textColor;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.borderWidthPx = borderWidthPx;
            this.released = released;
        }

        void markReleased(long releaseTimeMs) {
            this.releaseTimeMs = releaseTimeMs;
            this.released = true;
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
            float insetY = Math.min(visualGap / 2f, bounds.height() * 0.18f);
            return new RectF(
                    bounds.left,
                    bounds.top + insetY,
                    bounds.right,
                    bounds.bottom - insetY);
        }

        RectF hitBounds() {
            float insetX = Math.max(0f, visualGap / 2f);
            return new RectF(
                    bounds.left - insetX,
                    bounds.top,
                    bounds.right + insetX,
                    bounds.bottom);
        }

        @Override
        public boolean contains(float x, float y) {
            return hitBounds().contains(x, y);
        }

        @Override
        public boolean expandedContains(float x, float y, float slop) {
            RectF hitBounds = hitBounds();
            return x >= hitBounds.left - slop
                    && x <= hitBounds.right + slop
                    && y >= hitBounds.top - slop
                    && y <= hitBounds.bottom + slop;
        }

        @Override
        public float distanceSquaredTo(float x, float y) {
            float nearestX = Math.max(bounds.left, Math.min(bounds.right, x));
            float nearestY = Math.max(bounds.top, Math.min(bounds.bottom, y));
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
