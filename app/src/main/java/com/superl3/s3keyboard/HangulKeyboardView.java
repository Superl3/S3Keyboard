package com.superl3.s3keyboard;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private static final String TYPING_PROBE_TAG = "DingulTypingProbe";
    private static final float DINGUL_AXIS_DOMINANCE_RATIO = 1.15f;
    private static final GestureAction[] TYPING_PROBE_ACTIONS = {
            GestureAction.TAP,
            GestureAction.UP,
            GestureAction.DOWN,
            GestureAction.LEFT,
            GestureAction.RIGHT
    };
    private static final int MAX_RECENT_TEXT_TOUCH_SAMPLES = 8;
    private static final long DELETE_CORRECTION_WINDOW_MS = 6000L;

    private final List<KeySlot> keySlots = new ArrayList<>();
    private final Paint keyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint modifierIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, Bitmap> imageGlyphCache = new HashMap<>();
    private final Paint depthPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlayTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final KeyboardIconRegistry iconRegistry;
    private final List<TouchState> activeTouches = new ArrayList<>();
    private final List<PendingTouchOutput> pendingTouchOutputs = new ArrayList<>();
    private final List<TouchSample> recentTextTouchSamples = new ArrayList<>();
    private final List<PreviewBubbleAnimation> releasedPreviewBubbles = new ArrayList<>();
    private final KeyboardFeedback feedback = new KeyboardFeedback(this);
    private final RepeatController repeatController = new RepeatController(this, new RepeatController.Callback() {
        @Override
        public void onRepeat(String value) {
            emitValue(value);
        }
    });
    private final DingulSlideIntentResolver.Policy dingulSlideIntentPolicy =
            new DingulSlideIntentResolver.Policy() {
                @Override
                public boolean isDingulTypingKey(GestureKey key) {
                    return HangulKeyboardView.this.isDingulTypingKey(key);
                }

                @Override
                public GestureAction actionFor(GestureKey key, float dx, float dy) {
                    return new GestureState().release(
                            dx,
                            dy,
                            baseGestureThresholdPx(key),
                            gestureThresholdPxFor(key, GestureAction.UP),
                            gestureThresholdPxFor(key, GestureAction.DOWN),
                            gestureThresholdPxFor(key, GestureAction.LEFT),
                            gestureThresholdPxFor(key, GestureAction.RIGHT),
                            axisDominanceRatioFor(key));
                }

                @Override
                public float thresholdPx(GestureKey key, GestureAction action) {
                    return gestureThresholdPxFor(key, action);
                }

                @Override
                public GestureAction shadowActionFor(GestureKey key, float dx, float dy) {
                    int threshold = shadowGestureThresholdPxFor(key);
                    return new GestureState().release(
                            dx,
                            dy,
                            threshold,
                            threshold,
                            threshold,
                            threshold,
                            threshold,
                            axisDominanceRatioFor(key));
                }

                @Override
                public float shadowThresholdPx(GestureKey key, GestureAction action) {
                    return shadowGestureThresholdPxFor(key);
                }

                @Override
                public boolean hasOutput(GestureKey key, GestureAction action) {
                    String value = key == null ? null : key.valueFor(action);
                    return value != null && !value.isEmpty() && !KeyboardCommands.CMD_NOOP.equals(value);
                }
            };

    private KeyboardSettings settings = KeyboardSettings.defaults();
    private KeyboardSurface keyboardSurface = KeyboardSurface.NORMAL;
    private List<KeyboardRow> rows = Collections.emptyList();
    private TouchBiasStore touchBiasStore;
    private TouchBiasStore.Bias touchBias = TouchBiasStore.Bias.none();
    private TouchBiasStore.DingulTouchProfile dingulTouchProfile = TouchBiasStore.DingulTouchProfile.empty();
    private TypingEventJournal.CorrectionStats typingCorrectionStats =
            TypingEventJournal.correctionStats("");
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
    private boolean redactTypingEventText;
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
        dingulTouchProfile = touchBiasStore.loadDingulTouchProfile();
        typingCorrectionStats = touchBiasStore.loadTypingCorrectionStats();
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
        dingulTouchProfile = touchBiasStore.loadDingulTouchProfile();
        typingCorrectionStats = touchBiasStore.loadTypingCorrectionStats();
        rows = KeyboardLayoutFactory.build(this.settings, keyboardSurface);
        applyTypeface();
        if (getWidth() > 0 && getHeight() > 0) {
            layoutKeys(getWidth(), getHeight());
        }
        requestLayout();
        updatePreviewPopup();
        invalidate();
        settingsInitialized = true;
    }

    void setKeyboardSurface(KeyboardSurface surface) {
        KeyboardSurface safeSurface = surface == null ? KeyboardSurface.NORMAL : surface;
        if (keyboardSurface == safeSurface) {
            return;
        }
        keyboardSurface = safeSurface;
        rows = KeyboardLayoutFactory.build(settings, keyboardSurface);
        if (getWidth() > 0 && getHeight() > 0) {
            layoutKeys(getWidth(), getHeight());
        }
        requestLayout();
        updatePreviewPopup();
        invalidate();
    }

    void setRedactTypingEventText(boolean redactTypingEventText) {
        this.redactTypingEventText = redactTypingEventText;
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
        int desiredHeight = dp(measuredHeightDp());
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    private int measuredHeightDp() {
        if (!settings.remoteModeEnabled
                && (keyboardSurface == KeyboardSurface.NUMPAD
                || keyboardSurface == KeyboardSurface.PHONEPAD
                || keyboardSurface == KeyboardSurface.DATEPAD
                || keyboardSurface == KeyboardSurface.PINPAD)) {
            return settings.hangulKeyboardHeightDp;
        }
        return settings.measuredHeightDp();
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
                gestureThresholdPxFor(state.keySlot.key, GestureAction.RIGHT),
                axisDominanceRatioFor(state.keySlot.key));
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
                    gestureThresholdPxFor(state.keySlot.key, GestureAction.RIGHT),
                    axisDominanceRatioFor(state.keySlot.key));
            ResolvedTouchOutput output = resolveReleaseOutput(
                    state,
                    event.getX(pointerIndex),
                    event.getY(pointerIndex),
                    action);
            state.activeAction = output.action;
            if (output.action == GestureAction.TAP) {
                feedbackForKey(output.keySlot.key, output.action);
            }
            queueTouchOutput(
                    state,
                    output,
                    output.keySlot.key.valueFor(output.action),
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

    private ResolvedTouchOutput resolveReleaseOutput(
            TouchState state,
            float upX,
            float upY,
            GestureAction fallbackAction) {
        if (fallbackAction != GestureAction.TAP) {
            return new ResolvedTouchOutput(
                    state.keySlot,
                    fallbackAction,
                    fallbackAction,
                    null,
                    null,
                    0f,
                    false);
        }
        DingulSlideIntentResolver.Result<KeySlot> result = DingulSlideIntentResolver.resolve(
                keySlots,
                state.keySlot,
                state.downX,
                state.downY,
                state.downX + dp(touchBias.xDp),
                state.downY + dp(settings.touchYOffsetDp) + dp(touchBias.yDp),
                upX,
                upY,
                dp(settings.hitSlopDp),
                dingulSlideIntentPolicy);
        if (result != null) {
            return new ResolvedTouchOutput(
                        result.target,
                        result.action,
                        fallbackAction,
                        result.target,
                        result.action,
                        result.score,
                        true);
        }
        DingulSlideIntentResolver.Result<KeySlot> shadow = DingulSlideIntentResolver.resolveShadow(
                keySlots,
                state.keySlot,
                state.downX,
                state.downY,
                state.downX + dp(touchBias.xDp),
                state.downY + dp(settings.touchYOffsetDp) + dp(touchBias.yDp),
                upX,
                upY,
                dp(settings.hitSlopDp),
                dingulSlideIntentPolicy);
        if (shadow != null && shouldApplyActiveSlideCorrection(state.keySlot, shadow)) {
            return new ResolvedTouchOutput(
                    shadow.target,
                    shadow.action,
                    fallbackAction,
                    shadow.target,
                    shadow.action,
                    shadow.score,
                    true);
        }
        return shadow == null
                ? new ResolvedTouchOutput(
                        state.keySlot,
                        fallbackAction,
                        fallbackAction,
                        null,
                        null,
                        0f,
                        false)
                : new ResolvedTouchOutput(
                        state.keySlot,
                        fallbackAction,
                        fallbackAction,
                        shadow.target,
                        shadow.action,
                        shadow.score,
                        false);
    }

    private boolean shouldApplyActiveSlideCorrection(
            KeySlot origin,
            DingulSlideIntentResolver.Result<KeySlot> shadow) {
        if (!touchBiasAutoCorrectionEnabled || redactTypingEventText
                || origin == null || shadow == null || shadow.target == null) {
            return false;
        }
        return typingCorrectionStats.shouldApplyActiveSlide(
                codePoints(origin.key.label),
                codePoints(shadow.target.key.label),
                shadow.action,
                shadow.score,
                shadow.target == origin);
    }

    private void queueTouchOutput(
            TouchState state,
            ResolvedTouchOutput output,
            String value,
            float x,
            float y) {
        pendingTouchOutputs.add(new PendingTouchOutput(
                state.sequence,
                output.keySlot,
                output.action,
                value,
                state.downX,
                state.downY,
                x,
                y,
                state.downTimeMs,
                output.fallbackAction,
                output.shadowKeySlot,
                output.shadowAction,
                output.shadowScore,
                output.shadowApplied));
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
            rememberTextTouch(next);
            logTypingProbeEmit(next);
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

    private void rememberTextTouch(PendingTouchOutput output) {
        KeySlot keySlot = output == null ? null : output.keySlot;
        String value = output == null ? null : valueOrNull(output.value);
        if (keySlot == null || value == null) {
            return;
        }
        boolean textValue = !KeyboardCommands.isCommand(value);
        boolean dingulTypingKey = isDingulTypingKey(keySlot.key);
        boolean dingulCommandValue = isDingulVowelCommand(value);
        if (!textValue && !(dingulTypingKey && dingulCommandValue)) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        float offsetXDp = (output.x - keySlot.bounds.centerX()) / density;
        float offsetYDp = (output.y - keySlot.bounds.centerY()) / density;
        String keyCodePoints = dingulTypingKey ? codePoints(keySlot.key.label) : "";
        TouchSample sample = new TouchSample(
                value,
                keyCodePoints,
                offsetXDp,
                offsetYDp,
                output.action,
                System.currentTimeMillis());
        lastTextTouchSample = sample;
        rememberRecentTextTouchSample(sample);
        if (touchBiasAutoCorrectionEnabled) {
            recordTypingJournalInput(output, keyCodePoints, value, density);
            if (textValue) {
                touchBiasStore.recordTextInput(redactTypingEventText ? "" : value, output.action);
                touchBias = touchBiasStore.load();
            }
            if (dingulTypingKey && !keyCodePoints.isEmpty()) {
                touchBiasStore.recordDingulTextInput(keyCodePoints, output.action);
                dingulTouchProfile = touchBiasStore.loadDingulTouchProfile();
            }
        }
    }

    private void recordTypingJournalInput(
            PendingTouchOutput output,
            String keyCodePoints,
            String value,
            float density) {
        if (output == null) {
            return;
        }
        String eventId = "t-" + System.currentTimeMillis() + "-" + output.sequence;
        String journalKeyCodePoints = redactTypingEventText ? "" : keyCodePoints;
        String journalValueCodePoints = redactTypingEventText ? "" : codePoints(value);
        String shadowKeyCodePoints = "";
        if (!redactTypingEventText && output.shadowKeySlot != null) {
            shadowKeyCodePoints = codePoints(output.shadowKeySlot.key.label);
        }
        touchBiasStore.recordTypingJournalInput(new TypingEventJournal.Input(
                eventId,
                System.currentTimeMillis(),
                settings.keyboardMode,
                journalKeyCodePoints,
                journalValueCodePoints,
                output.action,
                output.fallbackAction,
                output.downX / density,
                output.downY / density,
                output.x / density,
                output.y / density,
                Math.max(0L, SystemClock.uptimeMillis() - output.downTimeMs),
                Math.round(gestureThresholdPxFor(output.keySlot.key, output.action) / density),
                settings.hitSlopDp,
                settings.keyGapDp,
                settings.touchYOffsetDp,
                touchBias.xDp,
                touchBias.yDp,
                shadowKeyCodePoints,
                output.shadowAction,
                output.shadowScore,
                output.shadowApplied));
        typingCorrectionStats = touchBiasStore.loadTypingCorrectionStats();
    }

    private void recordImmediateDeleteIfNeeded(String value) {
        if (!KeyboardCommands.CMD_DELETE.equals(value) || recentTextTouchSamples.isEmpty()) {
            if (!KeyboardCommands.isCommand(value)) {
                return;
            }
            if (!KeyboardCommands.CMD_DELETE.equals(value)) {
                lastTextTouchSample = null;
                recentTextTouchSamples.clear();
            } else if (touchBiasAutoCorrectionEnabled) {
                touchBiasStore.recordTypingJournalDelete(System.currentTimeMillis());
                typingCorrectionStats = touchBiasStore.loadTypingCorrectionStats();
            }
            return;
        }

        TouchSample deletedSample = popRecentTextTouchSample();
        lastTextTouchSample = recentTextTouchSamples.isEmpty()
                ? null
                : recentTextTouchSamples.get(recentTextTouchSamples.size() - 1);
        if (touchBiasAutoCorrectionEnabled) {
            touchBiasStore.recordTypingJournalDelete(System.currentTimeMillis());
            typingCorrectionStats = touchBiasStore.loadTypingCorrectionStats();
        }
        if (deletedSample != null && System.currentTimeMillis() - deletedSample.timeMs <= DELETE_CORRECTION_WINDOW_MS) {
            if (touchBiasAutoCorrectionEnabled) {
                if (!KeyboardCommands.isCommand(deletedSample.value)) {
                    touchBiasStore.recordImmediateDelete(
                            deletedSample.offsetXDp,
                            deletedSample.offsetYDp,
                            deletedSample.action,
                            redactTypingEventText ? "" : deletedSample.value);
                    touchBias = touchBiasStore.load();
                }
                if (!deletedSample.keyCodePoints.isEmpty()) {
                    touchBiasStore.recordDingulCorrection(
                            deletedSample.keyCodePoints,
                            deletedSample.action,
                            deletedSample.offsetXDp,
                            deletedSample.offsetYDp);
                    dingulTouchProfile = touchBiasStore.loadDingulTouchProfile();
                }
            }
        }
    }

    private void rememberRecentTextTouchSample(TouchSample sample) {
        if (sample == null) {
            return;
        }
        recentTextTouchSamples.add(sample);
        while (recentTextTouchSamples.size() > MAX_RECENT_TEXT_TOUCH_SAMPLES) {
            recentTextTouchSamples.remove(0);
        }
    }

    private TouchSample popRecentTextTouchSample() {
        if (recentTextTouchSamples.isEmpty()) {
            return null;
        }
        return recentTextTouchSamples.remove(recentTextTouchSamples.size() - 1);
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
        recentTextTouchSamples.clear();
        lastTextTouchSample = null;
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
                    visualGap,
                    slot.bottomSpaceDirection));
        }
        scheduleTypingProbePlanLog();
    }

    private void scheduleTypingProbePlanLog() {
        if (!shouldLogTypingProbe()) {
            return;
        }
        post(new Runnable() {
            @Override
            public void run() {
                logTypingProbePlan();
            }
        });
    }

    private void logTypingProbePlan() {
        if (!shouldLogTypingProbe() || keySlots.isEmpty()) {
            return;
        }
        int[] location = new int[2];
        getLocationOnScreen(location);
        int sequence = 0;
        for (KeySlot slot : keySlots) {
            if (!isTypingProbeKey(slot.key)) {
                continue;
            }
            for (GestureAction action : TYPING_PROBE_ACTIONS) {
                String value = slot.key.valueFor(action);
                if (value == null || KeyboardCommands.CMD_NOOP.equals(value)) {
                    continue;
                }
                TypingProbeTouch touch = typingProbeTouch(slot, action);
                Log.i(TYPING_PROBE_TAG, String.format(
                        Locale.US,
                        "PLAN\tseq=%d\tkeyCp=%s\taction=%s\tvalueCp=%s\tdown=%d,%d\tup=%d,%d\trange=%d,%d,%d,%d",
                        sequence++,
                        codePoints(slot.key.label),
                        action.name(),
                        codePoints(value),
                        Math.round(location[0] + touch.downX),
                        Math.round(location[1] + touch.downY),
                        Math.round(location[0] + touch.upX),
                        Math.round(location[1] + touch.upY),
                        Math.round(location[0] + touch.range.left),
                        Math.round(location[1] + touch.range.top),
                        Math.round(location[0] + touch.range.right),
                        Math.round(location[1] + touch.range.bottom)));
            }
        }
    }

    private void logTypingProbeEmit(PendingTouchOutput output) {
        if (!shouldLogTypingProbe() || output == null || output.keySlot == null) {
            return;
        }
        int[] location = new int[2];
        getLocationOnScreen(location);
        Log.i(TYPING_PROBE_TAG, String.format(
                Locale.US,
                "EMIT\tkeyCp=%s\taction=%s\tvalueCp=%s\tup=%d,%d",
                codePoints(output.keySlot.key.label),
                output.action.name(),
                codePoints(output.value),
                Math.round(location[0] + output.x),
                Math.round(location[1] + output.y)));
    }

    private boolean shouldLogTypingProbe() {
        return isDebuggableBuild()
                && settings.keyboardMode == KeyboardMode.HANGUL;
    }

    private boolean isDebuggableBuild() {
        return getContext() != null
                && (getContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private boolean isTypingProbeKey(GestureKey key) {
        if (key == null) {
            return false;
        }
        String keyCodePoints = codePoints(key.label);
        return "3131".equals(keyCodePoints)
                || "3145".equals(keyCodePoints)
                || "3163+2E".equals(keyCodePoints)
                || "3161+3150".equals(keyCodePoints);
    }

    private TypingProbeTouch typingProbeTouch(KeySlot slot, GestureAction action) {
        RectF hitBounds = slot.hitBounds();
        float biasX = dp(touchBias.xDp);
        float biasY = dp(settings.touchYOffsetDp) + dp(touchBias.yDp);
        RectF rawRange = new RectF(
                hitBounds.left - biasX,
                hitBounds.top - biasY,
                hitBounds.right - biasX,
                hitBounds.bottom - biasY);
        float downX = rawRange.centerX();
        float downY = rawRange.centerY();
        float upX = downX;
        float upY = downY;
        if (action != GestureAction.TAP) {
            float distance = Math.max(dp(18), gestureThresholdPxFor(slot.key, action) * 1.35f);
            switch (action) {
                case UP:
                    upY -= distance;
                    break;
                case DOWN:
                    upY += distance;
                    break;
                case LEFT:
                    upX -= distance;
                    break;
                case RIGHT:
                    upX += distance;
                    break;
                default:
                    break;
            }
        }
        return new TypingProbeTouch(downX, downY, upX, upY, rawRange);
    }

    private String codePoints(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            if (builder.length() > 0) {
                builder.append('+');
            }
            builder.append(Integer.toHexString(codePoint).toUpperCase(Locale.US));
            i += Character.charCount(codePoint);
        }
        return builder.toString();
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
        canvas.drawCircle(surfaceBounds.centerX(), surfaceBounds.centerY(), radius, textPaint);
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
        float gap = DecorativeGlyphCatalog.twoDotCenterGap(radius, renderDp(5.4f));
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
        int color = KeyboardKeyVisualClassifier.textColorFor(settings, key);
        if (drawBuiltInPointGlyph(canvas, override.value, surfaceBounds, color)) {
            return;
        }
        if (drawBuiltInKeyboardGlyph(canvas, override.value, surfaceBounds, color)) {
            return;
        }
        if (drawBuiltInGmkStyleGlyph(canvas, override.value, surfaceBounds, color)) {
            return;
        }
        if (drawBuiltInFontGlyph(canvas, override.value, surfaceBounds, color)) {
            return;
        }
        if (drawBuiltInImageMaskGlyph(canvas, override.value, surfaceBounds, color)) {
            return;
        }
        textPaint.setColor(KeyboardKeyVisualClassifier.textColorFor(settings, key));
        textPaint.setTextSize(textSizeFor(override.value, surfaceBounds, false));
        float centerY = surfaceBounds.centerY() - textCenterOffset(textPaint);
        canvas.drawText(override.value, surfaceBounds.centerX(), centerY, textPaint);
    }

    private boolean drawBuiltInPointGlyph(Canvas canvas, String glyphId, RectF bounds, int color) {
        if (!DecorativeGlyphCatalog.isBuiltInPointGlyph(glyphId)) {
            return false;
        }
        float radius = dotRadiusFor(bounds);
        float stroke = Math.max(renderDp(1.4f), dotsLineWeightFor(bounds) * 0.78f);
        float cx = bounds.centerX();
        float cy = bounds.centerY();
        modifierIconPaint.reset();
        modifierIconPaint.setAntiAlias(true);
        modifierIconPaint.setColor(color);
        modifierIconPaint.setStrokeWidth(stroke);
        modifierIconPaint.setStrokeCap(Paint.Cap.ROUND);
        modifierIconPaint.setStrokeJoin(Paint.Join.ROUND);
        modifierIconPaint.setStyle(Paint.Style.STROKE);
        if (DecorativeGlyphCatalog.GLYPH_RING.equals(glyphId)) {
            canvas.drawCircle(cx, cy, radius * 1.45f, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_DIAMOND.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            Path path = new Path();
            float size = radius * 2.45f;
            path.moveTo(cx, cy - size);
            path.lineTo(cx + size, cy);
            path.lineTo(cx, cy + size);
            path.lineTo(cx - size, cy);
            path.close();
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_SQUARE.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            float size = radius * 2.15f;
            canvas.drawRoundRect(
                    cx - size,
                    cy - size,
                    cx + size,
                    cy + size,
                    radius * 0.42f,
                    radius * 0.42f,
                    modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_PLUS.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 2f, cy, cx + radius * 2f, cy);
            drawPointLine(canvas, cx, cy - radius * 2f, cx, cy + radius * 2f);
        } else if (DecorativeGlyphCatalog.GLYPH_CROSS.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 1.45f, cy - radius * 1.45f,
                    cx + radius * 1.45f, cy + radius * 1.45f);
            drawPointLine(canvas, cx + radius * 1.45f, cy - radius * 1.45f,
                    cx - radius * 1.45f, cy + radius * 1.45f);
        } else if (DecorativeGlyphCatalog.GLYPH_STAR.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 2.1f, cy, cx + radius * 2.1f, cy);
            drawPointLine(canvas, cx, cy - radius * 2.1f, cx, cy + radius * 2.1f);
            drawPointLine(canvas, cx - radius * 1.5f, cy - radius * 1.5f,
                    cx + radius * 1.5f, cy + radius * 1.5f);
            drawPointLine(canvas, cx + radius * 1.5f, cy - radius * 1.5f,
                    cx - radius * 1.5f, cy + radius * 1.5f);
        } else if (DecorativeGlyphCatalog.GLYPH_SPARK.equals(glyphId)) {
            drawPointLine(canvas, cx, cy - radius * 2.5f, cx, cy + radius * 2.5f);
            drawPointLine(canvas, cx - radius * 1.7f, cy, cx + radius * 1.7f, cy);
        } else if (DecorativeGlyphCatalog.GLYPH_CHEVRON_UP.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 2.1f, cy + radius * 1.15f);
            path.lineTo(cx, cy - radius * 1.35f);
            path.lineTo(cx + radius * 2.1f, cy + radius * 1.15f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_CHEVRON_LEFT.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx + radius * 1.35f, cy - radius * 2.1f);
            path.lineTo(cx - radius * 1.15f, cy);
            path.lineTo(cx + radius * 1.35f, cy + radius * 2.1f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_CHEVRON_RIGHT.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 1.35f, cy - radius * 2.1f);
            path.lineTo(cx + radius * 1.15f, cy);
            path.lineTo(cx - radius * 1.35f, cy + radius * 2.1f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_SLASH_DOT.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 1.8f, cy + radius * 1.8f,
                    cx + radius * 1.8f, cy - radius * 1.8f);
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx + radius * 2.35f, cy + radius * 1.85f, radius * 0.72f, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_ORBIT.equals(glyphId)) {
            RectF oval = new RectF(
                    cx - radius * 2.55f,
                    cy - radius * 1.35f,
                    cx + radius * 2.55f,
                    cy + radius * 1.35f);
            canvas.drawOval(oval, modifierIconPaint);
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx + radius * 1.65f, cy - radius * 0.75f, radius * 0.75f, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GEAR_DOT.equals(glyphId)) {
            canvas.drawCircle(cx, cy, radius * 1.35f, modifierIconPaint);
            drawPointLine(canvas, cx - radius * 2.3f, cy, cx - radius * 1.75f, cy);
            drawPointLine(canvas, cx + radius * 1.75f, cy, cx + radius * 2.3f, cy);
            drawPointLine(canvas, cx, cy - radius * 2.3f, cx, cy - radius * 1.75f);
            drawPointLine(canvas, cx, cy + radius * 1.75f, cx, cy + radius * 2.3f);
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy, radius * 0.55f, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_BOOKMARK_DOT.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 1.65f, cy - radius * 2.2f);
            path.lineTo(cx + radius * 1.65f, cy - radius * 2.2f);
            path.lineTo(cx + radius * 1.65f, cy + radius * 2.15f);
            path.lineTo(cx, cy + radius * 1.25f);
            path.lineTo(cx - radius * 1.65f, cy + radius * 2.15f);
            path.close();
            canvas.drawPath(path, modifierIconPaint);
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy - radius * 0.3f, radius * 0.48f, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_SPACE_DOTS.equals(glyphId)) {
            drawMonochromeSpaceDots(canvas, bounds, color);
        } else if (DecorativeGlyphCatalog.GLYPH_TWO_DOTS.equals(glyphId)) {
            drawTwoDotLegend(canvas, bounds, color);
        } else if (DecorativeGlyphCatalog.GLYPH_GRID_4.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            float gap = radius * 1.45f;
            float small = radius * 0.78f;
            canvas.drawCircle(cx - gap, cy - gap, small, modifierIconPaint);
            canvas.drawCircle(cx + gap, cy - gap, small, modifierIconPaint);
            canvas.drawCircle(cx - gap, cy + gap, small, modifierIconPaint);
            canvas.drawCircle(cx + gap, cy + gap, small, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_TERMINAL.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 2.3f, cy - radius * 1.25f);
            path.lineTo(cx - radius * 0.65f, cy);
            path.lineTo(cx - radius * 2.3f, cy + radius * 1.25f);
            canvas.drawPath(path, modifierIconPaint);
            drawPointLine(canvas, cx - radius * 0.1f, cy + radius * 1.45f,
                    cx + radius * 2.2f, cy + radius * 1.45f);
        } else if (DecorativeGlyphCatalog.GLYPH_CURSOR.equals(glyphId)) {
            drawPointLine(canvas, cx, cy - radius * 2.35f, cx, cy + radius * 2.35f);
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx + radius * 1.45f, cy + radius * 1.75f, radius * 0.58f, modifierIconPaint);
        }
        return true;
    }

    private void drawPointLine(Canvas canvas, float startX, float startY, float stopX, float stopY) {
        modifierIconPaint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(startX, startY, stopX, stopY, modifierIconPaint);
    }

    private void drawMonochromeSpaceDots(Canvas canvas, RectF bounds, int color) {
        modifierIconPaint.setStyle(Paint.Style.FILL);
        modifierIconPaint.setColor(color);
        float radius = dotRadiusFor(bounds);
        float gap = DecorativeGlyphCatalog.spaceDotGap(radius, renderDp(3.2f));
        float totalWidth = radius * 8f + gap * 3f;
        float start = bounds.centerX() - totalWidth / 2f + radius;
        float cy = bounds.centerY();
        for (int i = 0; i < 4; i++) {
            canvas.drawCircle(start + i * (radius * 2f + gap), cy, radius, modifierIconPaint);
        }
    }

    private boolean drawBuiltInKeyboardGlyph(Canvas canvas, String glyphId, RectF bounds, int color) {
        if (!DecorativeGlyphCatalog.isBuiltInKeyboardGlyph(glyphId)) {
            return false;
        }
        float radius = dotRadiusFor(bounds);
        float stroke = Math.max(renderDp(1.45f), dotsLineWeightFor(bounds) * 0.72f);
        float cx = bounds.centerX();
        float cy = bounds.centerY();
        modifierIconPaint.reset();
        modifierIconPaint.setAntiAlias(true);
        modifierIconPaint.setColor(color);
        modifierIconPaint.setStrokeWidth(stroke);
        modifierIconPaint.setStrokeCap(Paint.Cap.ROUND);
        modifierIconPaint.setStrokeJoin(Paint.Join.ROUND);
        modifierIconPaint.setStyle(Paint.Style.STROKE);
        if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_RETURN.equals(glyphId)) {
            drawPointLine(canvas, cx + radius * 2.4f, cy - radius * 2.2f,
                    cx + radius * 2.4f, cy + radius * 0.7f);
            drawPointLine(canvas, cx + radius * 2.4f, cy + radius * 0.7f,
                    cx - radius * 1.8f, cy + radius * 0.7f);
            Path path = new Path();
            path.moveTo(cx - radius * 1.8f, cy + radius * 0.7f);
            path.lineTo(cx - radius * 0.45f, cy - radius * 0.6f);
            path.moveTo(cx - radius * 1.8f, cy + radius * 0.7f);
            path.lineTo(cx - radius * 0.45f, cy + radius * 2.0f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_TAB.equals(glyphId)) {
            drawPointLine(canvas, cx + radius * 2.4f, cy - radius * 2.2f,
                    cx + radius * 2.4f, cy + radius * 2.2f);
            drawPointLine(canvas, cx - radius * 2.4f, cy, cx + radius * 1.3f, cy);
            drawPointLine(canvas, cx + radius * 1.3f, cy,
                    cx + radius * 0.15f, cy - radius * 1.15f);
            drawPointLine(canvas, cx + radius * 1.3f, cy,
                    cx + radius * 0.15f, cy + radius * 1.15f);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_CAPSLOCK.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 2.2f, cy + radius * 0.25f);
            path.lineTo(cx, cy - radius * 2.0f);
            path.lineTo(cx + radius * 2.2f, cy + radius * 0.25f);
            canvas.drawPath(path, modifierIconPaint);
            drawPointLine(canvas, cx - radius * 2.4f, cy + radius * 2.0f,
                    cx + radius * 2.4f, cy + radius * 2.0f);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_COMMAND.equals(glyphId)) {
            drawCommandGlyph(canvas, cx, cy, radius);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_OPTION.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 2.6f, cy - radius * 1.65f,
                    cx - radius * 1.05f, cy - radius * 1.65f);
            drawPointLine(canvas, cx - radius * 0.95f, cy - radius * 1.65f,
                    cx + radius * 1.25f, cy + radius * 1.65f);
            drawPointLine(canvas, cx + radius * 1.25f, cy + radius * 1.65f,
                    cx + radius * 2.6f, cy + radius * 1.65f);
            drawPointLine(canvas, cx + radius * 0.9f, cy - radius * 1.65f,
                    cx + radius * 2.6f, cy - radius * 1.65f);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_CONTROL.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 2.35f, cy + radius * 0.75f);
            path.lineTo(cx, cy - radius * 1.75f);
            path.lineTo(cx + radius * 2.35f, cy + radius * 0.75f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_HIDE.equals(glyphId)) {
            drawKeyboardFrame(canvas, bounds, radius, false);
            Path path = new Path();
            path.moveTo(cx - radius * 1.4f, cy + radius * 2.55f);
            path.lineTo(cx, cy + radius * 3.65f);
            path.lineTo(cx + radius * 1.4f, cy + radius * 2.55f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_FULL.equals(glyphId)) {
            drawKeyboardFrame(canvas, bounds, radius, true);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_KEYS.equals(glyphId)) {
            drawKeyboardKeys(canvas, cx, cy, radius);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_LANGUAGE.equals(glyphId)) {
            canvas.drawCircle(cx, cy, radius * 2.6f, modifierIconPaint);
            drawPointLine(canvas, cx - radius * 2.6f, cy, cx + radius * 2.6f, cy);
            drawPointLine(canvas, cx, cy - radius * 2.6f, cx, cy + radius * 2.6f);
            RectF oval = new RectF(cx - radius * 1.25f, cy - radius * 2.6f,
                    cx + radius * 1.25f, cy + radius * 2.6f);
            canvas.drawOval(oval, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_UP.equals(glyphId)) {
            drawKeyboardArrow(canvas, cx, cy, radius, GestureAction.UP, false);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_DOWN.equals(glyphId)) {
            drawKeyboardArrow(canvas, cx, cy, radius, GestureAction.DOWN, false);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_LEFT.equals(glyphId)) {
            drawKeyboardArrow(canvas, cx, cy, radius, GestureAction.LEFT, false);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_ARROW_RIGHT.equals(glyphId)) {
            drawKeyboardArrow(canvas, cx, cy, radius, GestureAction.RIGHT, false);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_DOUBLE_LEFT.equals(glyphId)) {
            drawKeyboardArrow(canvas, cx - radius * 0.9f, cy, radius, GestureAction.LEFT, true);
            drawKeyboardArrow(canvas, cx + radius * 1.25f, cy, radius, GestureAction.LEFT, true);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_DOUBLE_RIGHT.equals(glyphId)) {
            drawKeyboardArrow(canvas, cx - radius * 1.25f, cy, radius, GestureAction.RIGHT, true);
            drawKeyboardArrow(canvas, cx + radius * 0.9f, cy, radius, GestureAction.RIGHT, true);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_BACKSPACE.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 2.8f, cy);
            path.lineTo(cx - radius * 1.25f, cy - radius * 1.6f);
            path.lineTo(cx + radius * 2.45f, cy - radius * 1.6f);
            path.lineTo(cx + radius * 2.45f, cy + radius * 1.6f);
            path.lineTo(cx - radius * 1.25f, cy + radius * 1.6f);
            path.close();
            canvas.drawPath(path, modifierIconPaint);
            drawPointLine(canvas, cx - radius * 0.4f, cy - radius * 0.75f,
                    cx + radius * 0.95f, cy + radius * 0.75f);
            drawPointLine(canvas, cx + radius * 0.95f, cy - radius * 0.75f,
                    cx - radius * 0.4f, cy + radius * 0.75f);
        } else if (DecorativeGlyphCatalog.GLYPH_KEYBOARD_SPACE.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 3.1f, cy - radius * 0.8f,
                    cx - radius * 3.1f, cy + radius * 0.95f);
            drawPointLine(canvas, cx - radius * 3.1f, cy + radius * 0.95f,
                    cx + radius * 3.1f, cy + radius * 0.95f);
            drawPointLine(canvas, cx + radius * 3.1f, cy + radius * 0.95f,
                    cx + radius * 3.1f, cy - radius * 0.8f);
        }
        return true;
    }

    private void drawCommandGlyph(Canvas canvas, float cx, float cy, float radius) {
        float loop = radius * 1.35f;
        float offset = radius * 1.45f;
        canvas.drawRoundRect(cx - offset - loop, cy - offset - loop,
                cx - offset + loop, cy - offset + loop, loop, loop, modifierIconPaint);
        canvas.drawRoundRect(cx + offset - loop, cy - offset - loop,
                cx + offset + loop, cy - offset + loop, loop, loop, modifierIconPaint);
        canvas.drawRoundRect(cx - offset - loop, cy + offset - loop,
                cx - offset + loop, cy + offset + loop, loop, loop, modifierIconPaint);
        canvas.drawRoundRect(cx + offset - loop, cy + offset - loop,
                cx + offset + loop, cy + offset + loop, loop, loop, modifierIconPaint);
        drawPointLine(canvas, cx - offset, cy - offset, cx + offset, cy - offset);
        drawPointLine(canvas, cx - offset, cy + offset, cx + offset, cy + offset);
        drawPointLine(canvas, cx - offset, cy - offset, cx - offset, cy + offset);
        drawPointLine(canvas, cx + offset, cy - offset, cx + offset, cy + offset);
    }

    private void drawKeyboardFrame(Canvas canvas, RectF bounds, float radius, boolean includeKeys) {
        RectF frame = new RectF(
                bounds.centerX() - radius * 4.4f,
                bounds.centerY() - radius * 2.75f,
                bounds.centerX() + radius * 4.4f,
                bounds.centerY() + radius * 2.35f);
        canvas.drawRoundRect(frame, radius * 0.65f, radius * 0.65f, modifierIconPaint);
        if (includeKeys) {
            drawKeyboardKeys(canvas, bounds.centerX(), bounds.centerY() + radius * 0.1f, radius);
        }
    }

    private void drawKeyboardKeys(Canvas canvas, float cx, float cy, float radius) {
        modifierIconPaint.setStyle(Paint.Style.FILL);
        float d = Math.max(radius * 0.7f, renderDp(1.2f));
        for (int row = -1; row <= 1; row++) {
            int count = row == 1 ? 3 : 5;
            float start = cx - (count - 1) * radius * 1.05f;
            float y = cy + row * radius * 1.35f;
            for (int i = 0; i < count; i++) {
                canvas.drawCircle(start + i * radius * 2.1f, y, d, modifierIconPaint);
            }
        }
        modifierIconPaint.setStyle(Paint.Style.STROKE);
    }

    private void drawKeyboardArrow(
            Canvas canvas,
            float cx,
            float cy,
            float radius,
            GestureAction direction,
            boolean compact) {
        float span = compact ? radius * 1.65f : radius * 2.55f;
        Path path = new Path();
        if (direction == GestureAction.LEFT) {
            path.moveTo(cx + span * 0.55f, cy - span);
            path.lineTo(cx - span * 0.55f, cy);
            path.lineTo(cx + span * 0.55f, cy + span);
        } else if (direction == GestureAction.RIGHT) {
            path.moveTo(cx - span * 0.55f, cy - span);
            path.lineTo(cx + span * 0.55f, cy);
            path.lineTo(cx - span * 0.55f, cy + span);
        } else if (direction == GestureAction.UP) {
            path.moveTo(cx - span, cy + span * 0.55f);
            path.lineTo(cx, cy - span * 0.55f);
            path.lineTo(cx + span, cy + span * 0.55f);
        } else if (direction == GestureAction.DOWN) {
            path.moveTo(cx - span, cy - span * 0.55f);
            path.lineTo(cx, cy + span * 0.55f);
            path.lineTo(cx + span, cy - span * 0.55f);
        }
        canvas.drawPath(path, modifierIconPaint);
    }

    private boolean drawBuiltInGmkStyleGlyph(Canvas canvas, String glyphId, RectF bounds, int color) {
        if (!DecorativeGlyphCatalog.isBuiltInGmkStyleGlyph(glyphId)) {
            return false;
        }
        float radius = dotRadiusFor(bounds);
        float stroke = Math.max(renderDp(1.35f), dotsLineWeightFor(bounds) * 0.68f);
        float cx = bounds.centerX();
        float cy = bounds.centerY();
        modifierIconPaint.reset();
        modifierIconPaint.setAntiAlias(true);
        modifierIconPaint.setColor(color);
        modifierIconPaint.setStrokeWidth(stroke);
        modifierIconPaint.setStrokeCap(Paint.Cap.ROUND);
        modifierIconPaint.setStrokeJoin(Paint.Join.ROUND);
        modifierIconPaint.setStyle(Paint.Style.STROKE);
        if (DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_BAR.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 3.2f, cy, cx + radius * 3.2f, cy);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_CORNER.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 2.4f, cy - radius * 1.65f);
            path.lineTo(cx + radius * 1.9f, cy - radius * 1.65f);
            path.lineTo(cx + radius * 1.9f, cy + radius * 2.2f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_ACCENT_STRIPE.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 2.8f, cy - radius * 1.3f,
                    cx + radius * 2.8f, cy - radius * 1.3f);
            drawPointLine(canvas, cx - radius * 2.8f, cy + radius * 1.3f,
                    cx + radius * 2.8f, cy + radius * 1.3f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_TRIPLE_DOT.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx - radius * 2.0f, cy, radius * 0.72f, modifierIconPaint);
            canvas.drawCircle(cx, cy, radius * 0.72f, modifierIconPaint);
            canvas.drawCircle(cx + radius * 2.0f, cy, radius * 0.72f, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_TWIN_TICKS.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 1.3f, cy - radius * 1.8f,
                    cx - radius * 2.2f, cy + radius * 1.8f);
            drawPointLine(canvas, cx + radius * 2.2f, cy - radius * 1.8f,
                    cx + radius * 1.3f, cy + radius * 1.8f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_SPACE_DASH.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 3.8f, cy + radius * 0.7f,
                    cx + radius * 3.8f, cy + radius * 0.7f);
            drawPointLine(canvas, cx - radius * 3.8f, cy - radius * 0.7f,
                    cx - radius * 2.6f, cy - radius * 0.7f);
            drawPointLine(canvas, cx + radius * 2.6f, cy - radius * 0.7f,
                    cx + radius * 3.8f, cy - radius * 0.7f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_MACRO_STACK.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 2.5f, cy - radius * 1.7f,
                    cx + radius * 2.5f, cy - radius * 1.7f);
            drawPointLine(canvas, cx - radius * 1.7f, cy,
                    cx + radius * 1.7f, cy);
            drawPointLine(canvas, cx - radius * 2.5f, cy + radius * 1.7f,
                    cx + radius * 2.5f, cy + radius * 1.7f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_MACRO_BRACKETS.equals(glyphId)) {
            drawBracketPair(canvas, cx, cy, radius);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_TARGET.equals(glyphId)) {
            canvas.drawCircle(cx, cy, radius * 2.25f, modifierIconPaint);
            canvas.drawCircle(cx, cy, radius * 0.9f, modifierIconPaint);
            drawPointLine(canvas, cx - radius * 3.0f, cy, cx - radius * 2.25f, cy);
            drawPointLine(canvas, cx + radius * 2.25f, cy, cx + radius * 3.0f, cy);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_PULSE.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 3.2f, cy);
            path.lineTo(cx - radius * 1.6f, cy);
            path.lineTo(cx - radius * 0.8f, cy - radius * 1.7f);
            path.lineTo(cx + radius * 0.25f, cy + radius * 1.8f);
            path.lineTo(cx + radius * 1.1f, cy);
            path.lineTo(cx + radius * 3.2f, cy);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_WAVE.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 3.0f, cy + radius * 0.4f);
            path.cubicTo(cx - radius * 1.9f, cy - radius * 1.6f,
                    cx - radius * 0.9f, cy + radius * 2.0f,
                    cx, cy + radius * 0.2f);
            path.cubicTo(cx + radius * 0.9f, cy - radius * 1.6f,
                    cx + radius * 1.9f, cy + radius * 2.0f,
                    cx + radius * 3.0f, cy);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_MOON.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            Path path = new Path();
            path.moveTo(cx + radius * 1.0f, cy - radius * 2.25f);
            path.cubicTo(cx - radius * 1.8f, cy - radius * 2.0f,
                    cx - radius * 2.9f, cy + radius * 0.8f,
                    cx - radius * 0.5f, cy + radius * 2.5f);
            path.cubicTo(cx + radius * 0.65f, cy + radius * 3.3f,
                    cx + radius * 2.1f, cy + radius * 2.45f,
                    cx + radius * 2.55f, cy + radius * 1.25f);
            path.cubicTo(cx + radius * 0.75f, cy + radius * 1.9f,
                    cx - radius * 0.35f, cy + radius * 0.55f,
                    cx, cy - radius * 0.65f);
            path.cubicTo(cx + radius * 0.25f, cy - radius * 1.55f,
                    cx + radius * 0.85f, cy - radius * 2.1f,
                    cx + radius * 1.0f, cy - radius * 2.25f);
            path.close();
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_SUN.equals(glyphId)) {
            canvas.drawCircle(cx, cy, radius * 1.3f, modifierIconPaint);
            for (int i = 0; i < 8; i++) {
                double angle = Math.PI * 2d * i / 8d;
                float inner = radius * 2.05f;
                float outer = radius * 3.0f;
                drawPointLine(canvas,
                        cx + (float) Math.cos(angle) * inner,
                        cy + (float) Math.sin(angle) * inner,
                        cx + (float) Math.cos(angle) * outer,
                        cy + (float) Math.sin(angle) * outer);
            }
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_LEAF.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 1.9f, cy + radius * 1.8f);
            path.cubicTo(cx - radius * 1.7f, cy - radius * 1.8f,
                    cx + radius * 1.9f, cy - radius * 2.1f,
                    cx + radius * 2.1f, cy + radius * 1.4f);
            path.cubicTo(cx + radius * 0.2f, cy + radius * 2.0f,
                    cx - radius * 1.0f, cy + radius * 2.1f,
                    cx - radius * 1.9f, cy + radius * 1.8f);
            canvas.drawPath(path, modifierIconPaint);
            drawPointLine(canvas, cx - radius * 1.4f, cy + radius * 1.4f,
                    cx + radius * 1.2f, cy - radius * 1.1f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_FLOWER.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy - radius * 1.45f, radius * 0.95f, modifierIconPaint);
            canvas.drawCircle(cx + radius * 1.35f, cy, radius * 0.95f, modifierIconPaint);
            canvas.drawCircle(cx, cy + radius * 1.45f, radius * 0.95f, modifierIconPaint);
            canvas.drawCircle(cx - radius * 1.35f, cy, radius * 0.95f, modifierIconPaint);
            canvas.drawCircle(cx, cy, radius * 0.55f, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_MOUNTAIN.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 3.0f, cy + radius * 2.0f);
            path.lineTo(cx - radius * 0.8f, cy - radius * 1.6f);
            path.lineTo(cx + radius * 0.4f, cy + radius * 0.3f);
            path.lineTo(cx + radius * 1.3f, cy - radius * 0.9f);
            path.lineTo(cx + radius * 3.0f, cy + radius * 2.0f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_DROPLET.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx, cy - radius * 2.8f);
            path.cubicTo(cx + radius * 2.2f, cy - radius * 0.8f,
                    cx + radius * 2.0f, cy + radius * 2.2f,
                    cx, cy + radius * 2.4f);
            path.cubicTo(cx - radius * 2.0f, cy + radius * 2.2f,
                    cx - radius * 2.2f, cy - radius * 0.8f,
                    cx, cy - radius * 2.8f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_ORBIT_STAR.equals(glyphId)) {
            RectF oval = new RectF(cx - radius * 2.8f, cy - radius * 1.3f,
                    cx + radius * 2.8f, cy + radius * 1.3f);
            canvas.drawOval(oval, modifierIconPaint);
            drawPointLine(canvas, cx + radius * 1.75f, cy - radius * 1.6f,
                    cx + radius * 1.75f, cy + radius * 1.6f);
            drawPointLine(canvas, cx + radius * 0.55f, cy,
                    cx + radius * 2.95f, cy);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_DIAMOND_CLUSTER.equals(glyphId)) {
            drawSmallDiamond(canvas, cx, cy - radius * 1.7f, radius * 0.85f);
            drawSmallDiamond(canvas, cx - radius * 1.55f, cy + radius * 0.9f, radius * 0.85f);
            drawSmallDiamond(canvas, cx + radius * 1.55f, cy + radius * 0.9f, radius * 0.85f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_PIXEL_STEPS.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            float size = radius * 1.25f;
            for (int i = 0; i < 4; i++) {
                canvas.drawRect(
                        cx - radius * 2.4f + i * size,
                        cy + radius * 1.6f - i * size,
                        cx - radius * 2.4f + (i + 1) * size,
                        cy + radius * 1.6f - (i - 1) * size,
                        modifierIconPaint);
            }
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_CONSTELLATION.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx - radius * 2.1f, cy - radius * 1.0f, radius * 0.48f, modifierIconPaint);
            canvas.drawCircle(cx - radius * 0.35f, cy + radius * 0.15f, radius * 0.56f, modifierIconPaint);
            canvas.drawCircle(cx + radius * 1.7f, cy - radius * 1.35f, radius * 0.48f, modifierIconPaint);
            canvas.drawCircle(cx + radius * 2.2f, cy + radius * 1.45f, radius * 0.48f, modifierIconPaint);
            drawPointLine(canvas, cx - radius * 2.1f, cy - radius * 1.0f,
                    cx - radius * 0.35f, cy + radius * 0.15f);
            drawPointLine(canvas, cx - radius * 0.35f, cy + radius * 0.15f,
                    cx + radius * 1.7f, cy - radius * 1.35f);
            drawPointLine(canvas, cx + radius * 1.7f, cy - radius * 1.35f,
                    cx + radius * 2.2f, cy + radius * 1.45f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_PLANET_RING.equals(glyphId)) {
            canvas.drawCircle(cx, cy, radius * 1.55f, modifierIconPaint);
            RectF oval = new RectF(cx - radius * 3.0f, cy - radius * 1.05f,
                    cx + radius * 3.0f, cy + radius * 1.05f);
            canvas.drawOval(oval, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_COMET_TAIL.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx + radius * 1.85f, cy - radius * 1.2f, radius * 0.85f, modifierIconPaint);
            drawPointLine(canvas, cx + radius * 0.8f, cy - radius * 0.45f,
                    cx - radius * 2.8f, cy + radius * 1.7f);
            drawPointLine(canvas, cx + radius * 0.55f, cy - radius * 1.25f,
                    cx - radius * 2.45f, cy - radius * 0.35f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_CRESCENT_STAR.equals(glyphId)) {
            drawBuiltInGmkStyleGlyph(canvas, DecorativeGlyphCatalog.GLYPH_GMK_MOON, bounds, color);
            modifierIconPaint.setColor(color);
            drawPointLine(canvas, cx + radius * 2.0f, cy - radius * 2.2f,
                    cx + radius * 2.0f, cy - radius * 0.8f);
            drawPointLine(canvas, cx + radius * 1.3f, cy - radius * 1.5f,
                    cx + radius * 2.7f, cy - radius * 1.5f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_SPARKLE_PAIR.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 1.5f, cy - radius * 2.2f,
                    cx - radius * 1.5f, cy - radius * 0.4f);
            drawPointLine(canvas, cx - radius * 2.4f, cy - radius * 1.3f,
                    cx - radius * 0.6f, cy - radius * 1.3f);
            drawPointLine(canvas, cx + radius * 1.55f, cy + radius * 0.15f,
                    cx + radius * 1.55f, cy + radius * 2.35f);
            drawPointLine(canvas, cx + radius * 0.45f, cy + radius * 1.25f,
                    cx + radius * 2.65f, cy + radius * 1.25f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_PLUS_CLUSTER.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 2.0f, cy - radius * 0.7f,
                    cx - radius * 0.6f, cy - radius * 0.7f);
            drawPointLine(canvas, cx - radius * 1.3f, cy - radius * 1.4f,
                    cx - radius * 1.3f, cy);
            drawPointLine(canvas, cx + radius * 0.7f, cy + radius * 1.0f,
                    cx + radius * 2.3f, cy + radius * 1.0f);
            drawPointLine(canvas, cx + radius * 1.5f, cy + radius * 0.2f,
                    cx + radius * 1.5f, cy + radius * 1.8f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_DOT_MATRIX.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    canvas.drawCircle(cx + (col - 1) * radius * 1.45f,
                            cy + (row - 1) * radius * 1.45f,
                            radius * 0.42f,
                            modifierIconPaint);
                }
            }
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_CORNER_DOTS.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx - radius * 2.4f, cy - radius * 1.8f, radius * 0.55f, modifierIconPaint);
            canvas.drawCircle(cx - radius * 1.0f, cy - radius * 1.8f, radius * 0.55f, modifierIconPaint);
            canvas.drawCircle(cx - radius * 2.4f, cy - radius * 0.4f, radius * 0.55f, modifierIconPaint);
            canvas.drawCircle(cx + radius * 2.4f, cy + radius * 1.8f, radius * 0.55f, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_SIDE_STRIPES.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 2.9f, cy - radius * 2.0f,
                    cx - radius * 2.9f, cy + radius * 2.0f);
            drawPointLine(canvas, cx + radius * 2.9f, cy - radius * 2.0f,
                    cx + radius * 2.9f, cy + radius * 2.0f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_CENTER_CROSS.equals(glyphId)) {
            canvas.drawCircle(cx, cy, radius * 2.5f, modifierIconPaint);
            drawPointLine(canvas, cx - radius * 1.5f, cy, cx + radius * 1.5f, cy);
            drawPointLine(canvas, cx, cy - radius * 1.5f, cx, cy + radius * 1.5f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_ARCADE_DIAMOND.equals(glyphId)) {
            drawSmallDiamond(canvas, cx, cy, radius * 1.8f);
            drawPointLine(canvas, cx - radius * 2.6f, cy, cx - radius * 1.7f, cy);
            drawPointLine(canvas, cx + radius * 1.7f, cy, cx + radius * 2.6f, cy);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_ISO_ENTER_MARK.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx + radius * 2.0f, cy - radius * 2.1f);
            path.lineTo(cx + radius * 2.0f, cy + radius * 0.8f);
            path.lineTo(cx - radius * 2.0f, cy + radius * 0.8f);
            path.lineTo(cx - radius * 0.8f, cy - radius * 0.4f);
            path.moveTo(cx - radius * 2.0f, cy + radius * 0.8f);
            path.lineTo(cx - radius * 0.8f, cy + radius * 2.0f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_SPLIT_BAR.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 3.4f, cy + radius * 0.75f,
                    cx - radius * 0.45f, cy + radius * 0.75f);
            drawPointLine(canvas, cx + radius * 0.45f, cy + radius * 0.75f,
                    cx + radius * 3.4f, cy + radius * 0.75f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_LONG_BAR_TICKS.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 3.4f, cy, cx + radius * 3.4f, cy);
            drawPointLine(canvas, cx - radius * 1.7f, cy - radius * 0.9f,
                    cx - radius * 1.7f, cy + radius * 0.9f);
            drawPointLine(canvas, cx + radius * 1.7f, cy - radius * 0.9f,
                    cx + radius * 1.7f, cy + radius * 0.9f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_STEPPED_BAR.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 3.0f, cy + radius * 1.4f,
                    cx - radius * 0.8f, cy + radius * 1.4f);
            drawPointLine(canvas, cx - radius * 0.8f, cy + radius * 1.4f,
                    cx - radius * 0.8f, cy - radius * 0.2f);
            drawPointLine(canvas, cx - radius * 0.8f, cy - radius * 0.2f,
                    cx + radius * 2.7f, cy - radius * 0.2f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_RISING_BLOCKS.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < 4; i++) {
                float h = radius * (0.9f + i * 0.55f);
                float x = cx - radius * 2.4f + i * radius * 1.5f;
                canvas.drawRect(x, cy + radius * 1.8f - h, x + radius * 0.8f,
                        cy + radius * 1.8f, modifierIconPaint);
            }
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_EQUALIZER.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 2.2f, cy + radius * 1.8f,
                    cx - radius * 2.2f, cy - radius * 0.8f);
            drawPointLine(canvas, cx, cy + radius * 1.8f,
                    cx, cy - radius * 1.8f);
            drawPointLine(canvas, cx + radius * 2.2f, cy + radius * 1.8f,
                    cx + radius * 2.2f, cy - radius * 0.2f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_WAVE_DOUBLE.equals(glyphId)) {
            drawBuiltInGmkStyleGlyph(canvas, DecorativeGlyphCatalog.GLYPH_GMK_WAVE, bounds, color);
            RectF shifted = new RectF(bounds);
            shifted.offset(0f, radius * 1.25f);
            drawBuiltInGmkStyleGlyph(canvas, DecorativeGlyphCatalog.GLYPH_GMK_WAVE, shifted, color);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_FLOWER_ALT.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(cx - radius * 1.0f, cy - radius * 0.8f, radius * 1.05f, modifierIconPaint);
            canvas.drawCircle(cx + radius * 1.0f, cy - radius * 0.8f, radius * 1.05f, modifierIconPaint);
            canvas.drawCircle(cx, cy + radius * 1.0f, radius * 1.05f, modifierIconPaint);
            modifierIconPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy, radius * 0.55f, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_LEAF_PAIR.equals(glyphId)) {
            RectF left = new RectF(bounds);
            left.offset(-radius * 1.25f, 0f);
            drawBuiltInGmkStyleGlyph(canvas, DecorativeGlyphCatalog.GLYPH_GMK_LEAF, left, color);
            RectF right = new RectF(bounds);
            right.offset(radius * 1.25f, 0f);
            drawBuiltInGmkStyleGlyph(canvas, DecorativeGlyphCatalog.GLYPH_GMK_LEAF, right, color);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_SPROUT.equals(glyphId)) {
            drawPointLine(canvas, cx, cy + radius * 2.2f, cx, cy - radius * 0.7f);
            Path left = new Path();
            left.moveTo(cx, cy - radius * 0.4f);
            left.cubicTo(cx - radius * 2.2f, cy - radius * 1.6f,
                    cx - radius * 2.4f, cy + radius * 0.6f,
                    cx, cy + radius * 0.25f);
            canvas.drawPath(left, modifierIconPaint);
            Path right = new Path();
            right.moveTo(cx, cy - radius * 0.8f);
            right.cubicTo(cx + radius * 2.2f, cy - radius * 2.0f,
                    cx + radius * 2.4f, cy + radius * 0.2f,
                    cx, cy - radius * 0.05f);
            canvas.drawPath(right, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_PETALS.equals(glyphId)) {
            modifierIconPaint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < 5; i++) {
                double angle = Math.PI * 2d * i / 5d - Math.PI / 2d;
                canvas.drawOval(new RectF(
                        cx + (float) Math.cos(angle) * radius * 1.35f - radius * 0.65f,
                        cy + (float) Math.sin(angle) * radius * 1.35f - radius * 1.0f,
                        cx + (float) Math.cos(angle) * radius * 1.35f + radius * 0.65f,
                        cy + (float) Math.sin(angle) * radius * 1.35f + radius * 1.0f),
                        modifierIconPaint);
            }
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_RAIN.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 1.7f, cy - radius * 1.9f,
                    cx - radius * 2.4f, cy - radius * 0.6f);
            drawPointLine(canvas, cx, cy - radius * 0.9f,
                    cx - radius * 0.7f, cy + radius * 0.4f);
            drawPointLine(canvas, cx + radius * 1.7f, cy + radius * 0.1f,
                    cx + radius * 1.0f, cy + radius * 1.4f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_SNOW.equals(glyphId)) {
            drawPointLine(canvas, cx - radius * 2.3f, cy, cx + radius * 2.3f, cy);
            drawPointLine(canvas, cx - radius * 1.15f, cy - radius * 2.0f,
                    cx + radius * 1.15f, cy + radius * 2.0f);
            drawPointLine(canvas, cx + radius * 1.15f, cy - radius * 2.0f,
                    cx - radius * 1.15f, cy + radius * 2.0f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_CLOUD.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 2.7f, cy + radius * 1.0f);
            path.cubicTo(cx - radius * 2.7f, cy - radius * 0.2f,
                    cx - radius * 1.4f, cy - radius * 0.3f,
                    cx - radius * 1.1f, cy - radius * 0.8f);
            path.cubicTo(cx - radius * 0.6f, cy - radius * 2.0f,
                    cx + radius * 1.0f, cy - radius * 1.8f,
                    cx + radius * 1.2f, cy - radius * 0.6f);
            path.cubicTo(cx + radius * 2.8f, cy - radius * 0.8f,
                    cx + radius * 3.0f, cy + radius * 1.0f,
                    cx + radius * 1.6f, cy + radius * 1.0f);
            path.close();
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_FLAME.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx, cy - radius * 2.8f);
            path.cubicTo(cx + radius * 2.1f, cy - radius * 0.9f,
                    cx + radius * 1.8f, cy + radius * 2.4f,
                    cx, cy + radius * 2.5f);
            path.cubicTo(cx - radius * 1.7f, cy + radius * 1.9f,
                    cx - radius * 2.2f, cy - radius * 0.5f,
                    cx, cy - radius * 2.8f);
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_BOLT.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx + radius * 0.6f, cy - radius * 3.0f);
            path.lineTo(cx - radius * 1.4f, cy + radius * 0.4f);
            path.lineTo(cx + radius * 0.2f, cy + radius * 0.4f);
            path.lineTo(cx - radius * 0.6f, cy + radius * 3.0f);
            path.lineTo(cx + radius * 1.8f, cy - radius * 0.7f);
            path.lineTo(cx + radius * 0.3f, cy - radius * 0.7f);
            path.close();
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_CRYSTAL.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx, cy - radius * 2.7f);
            path.lineTo(cx + radius * 2.1f, cy - radius * 0.5f);
            path.lineTo(cx + radius * 1.2f, cy + radius * 2.5f);
            path.lineTo(cx - radius * 1.2f, cy + radius * 2.5f);
            path.lineTo(cx - radius * 2.1f, cy - radius * 0.5f);
            path.close();
            canvas.drawPath(path, modifierIconPaint);
            drawPointLine(canvas, cx, cy - radius * 2.7f, cx, cy + radius * 2.5f);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_COMPASS.equals(glyphId)) {
            canvas.drawCircle(cx, cy, radius * 2.6f, modifierIconPaint);
            Path path = new Path();
            path.moveTo(cx, cy - radius * 2.0f);
            path.lineTo(cx + radius * 0.75f, cy + radius * 0.75f);
            path.lineTo(cx, cy + radius * 0.25f);
            path.lineTo(cx - radius * 0.75f, cy + radius * 0.75f);
            path.close();
            canvas.drawPath(path, modifierIconPaint);
        } else if (DecorativeGlyphCatalog.GLYPH_GMK_LAB_FLASK.equals(glyphId)) {
            Path path = new Path();
            path.moveTo(cx - radius * 0.8f, cy - radius * 2.4f);
            path.lineTo(cx + radius * 0.8f, cy - radius * 2.4f);
            path.lineTo(cx + radius * 0.4f, cy - radius * 0.4f);
            path.lineTo(cx + radius * 2.0f, cy + radius * 2.3f);
            path.lineTo(cx - radius * 2.0f, cy + radius * 2.3f);
            path.lineTo(cx - radius * 0.4f, cy - radius * 0.4f);
            path.close();
            canvas.drawPath(path, modifierIconPaint);
        }
        return true;
    }

    private boolean drawBuiltInFontGlyph(Canvas canvas, String glyphId, RectF bounds, int color) {
        if (!DecorativeGlyphCatalog.isBuiltInFontGlyph(glyphId)) {
            return false;
        }
        String text = DecorativeGlyphCatalog.fontGlyphText(glyphId);
        if (text.isEmpty()) {
            return false;
        }
        modifierIconPaint.reset();
        modifierIconPaint.setAntiAlias(true);
        modifierIconPaint.setStyle(Paint.Style.FILL);
        modifierIconPaint.setTextAlign(Paint.Align.CENTER);
        modifierIconPaint.setColor(color);
        modifierIconPaint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        modifierIconPaint.setFakeBoldText(true);
        float maxWidth = bounds.width() * 0.76f;
        float maxHeight = bounds.height() * 0.66f;
        float textSize = Math.min(maxHeight, bounds.height() * 0.72f);
        modifierIconPaint.setTextSize(textSize);
        Rect textBounds = new Rect();
        modifierIconPaint.getTextBounds(text, 0, text.length(), textBounds);
        if (textBounds.width() > 0 && textBounds.width() > maxWidth) {
            textSize *= maxWidth / textBounds.width();
            modifierIconPaint.setTextSize(textSize);
        }
        float centerY = bounds.centerY() - textCenterOffset(modifierIconPaint);
        canvas.drawText(text, bounds.centerX(), centerY, modifierIconPaint);
        return true;
    }

    private boolean drawBuiltInImageMaskGlyph(Canvas canvas, String glyphId, RectF bounds, int color) {
        if (!DecorativeGlyphCatalog.isBuiltInImageMaskGlyph(glyphId)) {
            return false;
        }
        Bitmap bitmap = imageGlyphBitmap(glyphId);
        if (bitmap == null) {
            return false;
        }
        float targetHeight = bounds.height() * 0.68f;
        float targetWidth = Math.min(bounds.width() * 0.72f,
                targetHeight * DecorativeGlyphCatalog.glyphAspectRatio(glyphId));
        RectF target = new RectF(
                bounds.centerX() - targetWidth / 2f,
                bounds.centerY() - targetHeight / 2f,
                bounds.centerX() + targetWidth / 2f,
                bounds.centerY() + targetHeight / 2f);
        modifierIconPaint.reset();
        modifierIconPaint.setAntiAlias(true);
        modifierIconPaint.setFilterBitmap(true);
        modifierIconPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, target, modifierIconPaint);
        modifierIconPaint.setColorFilter(null);
        return true;
    }

    private Bitmap imageGlyphBitmap(String glyphId) {
        Bitmap cached = imageGlyphCache.get(glyphId);
        if (cached != null) {
            return cached;
        }
        int resId = imageGlyphResourceId(glyphId);
        if (resId == 0) {
            return null;
        }
        Bitmap decoded = BitmapFactory.decodeResource(getResources(), resId);
        if (decoded != null) {
            imageGlyphCache.put(glyphId, decoded);
        }
        return decoded;
    }

    private int imageGlyphResourceId(String glyphId) {
        if (DecorativeGlyphCatalog.GLYPH_IMG_TALL_CAPSULE.equals(glyphId)) {
            return R.drawable.glyph_mask_img_tall_capsule;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_VERTICAL_RIBBON.equals(glyphId)) {
            return R.drawable.glyph_mask_img_vertical_ribbon;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_SPLIT_PILL.equals(glyphId)) {
            return R.drawable.glyph_mask_img_split_pill;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_KEYHOLE.equals(glyphId)) {
            return R.drawable.glyph_mask_img_keyhole;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_BADGE_CUT.equals(glyphId)) {
            return R.drawable.glyph_mask_img_badge_cut;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_SIDE_NOTCH.equals(glyphId)) {
            return R.drawable.glyph_mask_img_side_notch;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_STACKED_TILES.equals(glyphId)) {
            return R.drawable.glyph_mask_img_stacked_tiles;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_FOLDED_CORNER.equals(glyphId)) {
            return R.drawable.glyph_mask_img_folded_corner;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_FLAG_TAB.equals(glyphId)) {
            return R.drawable.glyph_mask_img_flag_tab;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_TALL_BRACKET.equals(glyphId)) {
            return R.drawable.glyph_mask_img_tall_bracket;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_HORIZON_BARS.equals(glyphId)) {
            return R.drawable.glyph_mask_img_horizon_bars;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_LADDER.equals(glyphId)) {
            return R.drawable.glyph_mask_img_ladder;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_DUAL_POSTS.equals(glyphId)) {
            return R.drawable.glyph_mask_img_dual_posts;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_PIN_DROP.equals(glyphId)) {
            return R.drawable.glyph_mask_img_pin_drop;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_TICKET.equals(glyphId)) {
            return R.drawable.glyph_mask_img_ticket;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_LEAF_SLAB.equals(glyphId)) {
            return R.drawable.glyph_mask_img_leaf_slab;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_BLOB_STAR.equals(glyphId)) {
            return R.drawable.glyph_mask_img_blob_star;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_ARC_GATE.equals(glyphId)) {
            return R.drawable.glyph_mask_img_arc_gate;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_CORNER_FRAME.equals(glyphId)) {
            return R.drawable.glyph_mask_img_corner_frame;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_CAPSULE_DOTS.equals(glyphId)) {
            return R.drawable.glyph_mask_img_capsule_dots;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_WAVE_TILE.equals(glyphId)) {
            return R.drawable.glyph_mask_img_wave_tile;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_DIAMOND_STACK.equals(glyphId)) {
            return R.drawable.glyph_mask_img_diamond_stack;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_TALL_ORBIT.equals(glyphId)) {
            return R.drawable.glyph_mask_img_tall_orbit;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_PUNCH_CARD.equals(glyphId)) {
            return R.drawable.glyph_mask_img_punch_card;
        }
        if (DecorativeGlyphCatalog.GLYPH_IMG_SOFT_CROSS.equals(glyphId)) {
            return R.drawable.glyph_mask_img_soft_cross;
        }
        return 0;
    }

    private void drawBracketPair(Canvas canvas, float cx, float cy, float radius) {
        Path left = new Path();
        left.moveTo(cx - radius * 1.1f, cy - radius * 2.2f);
        left.lineTo(cx - radius * 2.5f, cy - radius * 2.2f);
        left.lineTo(cx - radius * 2.5f, cy + radius * 2.2f);
        left.lineTo(cx - radius * 1.1f, cy + radius * 2.2f);
        canvas.drawPath(left, modifierIconPaint);
        Path right = new Path();
        right.moveTo(cx + radius * 1.1f, cy - radius * 2.2f);
        right.lineTo(cx + radius * 2.5f, cy - radius * 2.2f);
        right.lineTo(cx + radius * 2.5f, cy + radius * 2.2f);
        right.lineTo(cx + radius * 1.1f, cy + radius * 2.2f);
        canvas.drawPath(right, modifierIconPaint);
    }

    private void drawSmallDiamond(Canvas canvas, float cx, float cy, float size) {
        modifierIconPaint.setStyle(Paint.Style.FILL);
        Path path = new Path();
        path.moveTo(cx, cy - size);
        path.lineTo(cx + size, cy);
        path.lineTo(cx, cy + size);
        path.lineTo(cx - size, cy);
        path.close();
        canvas.drawPath(path, modifierIconPaint);
        modifierIconPaint.setStyle(Paint.Style.STROKE);
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
        modifierIconPaint.setStrokeWidth(Math.max(bounds.height() * 0.06f, renderDp(1.5f)));

        Path path = DecorativeGlyphCatalog.createHihihiPath();

        canvas.save();
        float scale = Math.min(
                bounds.width() * DecorativeGlyphCatalog.HIHIHI_MAX_WIDTH_RATIO
                        / DecorativeGlyphCatalog.HIHIHI_VIEWBOX_WIDTH,
                bounds.height() * DecorativeGlyphCatalog.HIHIHI_MAX_HEIGHT_RATIO
                        / DecorativeGlyphCatalog.HIHIHI_VIEWBOX_HEIGHT);
        float left = bounds.centerX() - DecorativeGlyphCatalog.HIHIHI_VIEWBOX_WIDTH * scale / 2f;
        float top = bounds.centerY() - DecorativeGlyphCatalog.HIHIHI_VIEWBOX_HEIGHT * scale / 2f;
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
                keyFaceGradientStops(),
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
                blendColor(settings.visualEffects.keyFaceGradientStartColor, background, topAmount),
                background,
                blendColor(settings.visualEffects.keyFaceGradientEndColor, background, bottomAmount)
        };
    }

    private float[] keyFaceGradientStops() {
        String curve = settings.visualEffects.keyFaceGradientCurve;
        if (KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_LINEAR.equals(curve)) {
            return new float[] { 0f, 0.5f, 1f };
        }
        if (KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_TOP_GLOW.equals(curve)) {
            return new float[] { 0f, 0.30f, 1f };
        }
        if (KeyboardVisualEffects.KEY_FACE_GRADIENT_CURVE_BOTTOM_SHADE.equals(curve)) {
            return new float[] { 0f, 0.62f, 1f };
        }
        return new float[] { 0f, 0.42f, 1f };
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
        return DecorativeGlyphCatalog.dotRadiusForKeyHeight(bounds.height(), renderDp(2.1f));
    }

    private float dotsLineWeightFor(RectF bounds) {
        return DecorativeGlyphCatalog.lineWeightForDotRadius(dotRadiusFor(bounds));
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
        float gap = DecorativeGlyphCatalog.spaceDotGap(radius, renderDp(3.2f));
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
        int thresholdDp = GestureThresholdPolicy.thresholdDp(settings, touchBias, key, action);
        if (touchBiasAutoCorrectionEnabled && isDingulTypingKey(key)) {
            thresholdDp += dingulTouchProfile.penaltyDp(codePoints(key.label), action);
            if (!redactTypingEventText) {
                thresholdDp += typingCorrectionStats.thresholdAdjustmentDp(codePoints(key.label), action);
            }
        }
        return dp(Math.max(8, thresholdDp));
    }

    private int shadowGestureThresholdPxFor(GestureKey key) {
        int baseDp = GestureThresholdPolicy.baseThresholdDp(settings, key);
        return dp(Math.max(8, Math.round(baseDp * 0.72f)));
    }

    private float axisDominanceRatioFor(GestureKey key) {
        return isDingulTypingKey(key) ? DINGUL_AXIS_DOMINANCE_RATIO : 0f;
    }

    private boolean isDingulTypingKey(GestureKey key) {
        if (settings.keyboardMode != KeyboardMode.HANGUL || key == null) {
            return false;
        }
        if (KeyboardCommands.isCommand(key.tap) && !isDingulVowelCommand(key.tap)) {
            return false;
        }
        return isHangulConsonantHintKey(key) || isHangulVowelHintKey(key);
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
        final float downX;
        final float downY;
        final float x;
        final float y;
        final long downTimeMs;
        final GestureAction fallbackAction;
        final KeySlot shadowKeySlot;
        final GestureAction shadowAction;
        final float shadowScore;
        final boolean shadowApplied;

        PendingTouchOutput(
                long sequence,
                KeySlot keySlot,
                GestureAction action,
                String value,
                float downX,
                float downY,
                float x,
                float y,
                long downTimeMs,
                GestureAction fallbackAction,
                KeySlot shadowKeySlot,
                GestureAction shadowAction,
                float shadowScore,
                boolean shadowApplied) {
            this.sequence = sequence;
            this.keySlot = keySlot;
            this.action = action == null ? GestureAction.TAP : action;
            this.value = value;
            this.downX = downX;
            this.downY = downY;
            this.x = x;
            this.y = y;
            this.downTimeMs = Math.max(0L, downTimeMs);
            this.fallbackAction = fallbackAction == null ? GestureAction.TAP : fallbackAction;
            this.shadowKeySlot = shadowKeySlot;
            this.shadowAction = shadowAction;
            this.shadowScore = shadowScore;
            this.shadowApplied = shadowApplied;
        }
    }

    private static final class ResolvedTouchOutput {
        final KeySlot keySlot;
        final GestureAction action;
        final GestureAction fallbackAction;
        final KeySlot shadowKeySlot;
        final GestureAction shadowAction;
        final float shadowScore;
        final boolean shadowApplied;

        ResolvedTouchOutput(
                KeySlot keySlot,
                GestureAction action,
                GestureAction fallbackAction,
                KeySlot shadowKeySlot,
                GestureAction shadowAction,
                float shadowScore,
                boolean shadowApplied) {
            this.keySlot = keySlot;
            this.action = action == null ? GestureAction.TAP : action;
            this.fallbackAction = fallbackAction == null ? GestureAction.TAP : fallbackAction;
            this.shadowKeySlot = shadowKeySlot;
            this.shadowAction = shadowAction;
            this.shadowScore = shadowScore;
            this.shadowApplied = shadowApplied;
        }
    }

    private static final class TypingProbeTouch {
        final float downX;
        final float downY;
        final float upX;
        final float upY;
        final RectF range;

        TypingProbeTouch(float downX, float downY, float upX, float upY, RectF range) {
            this.downX = downX;
            this.downY = downY;
            this.upX = upX;
            this.upY = upY;
            this.range = range;
        }
    }

    private static final class KeySlot implements TouchResolver.Target, DingulSlideIntentResolver.Target {
        final GestureKey key;
        final RectF bounds;
        final boolean primaryBottomControl;
        final boolean compactSpecialColumn;
        final float visualGap;
        final int bottomSpaceDirection;

        KeySlot(
                GestureKey key,
                RectF bounds,
                boolean primaryBottomControl,
                boolean compactSpecialColumn,
                float visualGap,
                int bottomSpaceDirection) {
            this.key = key;
            this.bounds = bounds;
            this.primaryBottomControl = primaryBottomControl;
            this.compactSpecialColumn = compactSpecialColumn;
            this.visualGap = Math.max(0f, visualGap);
            this.bottomSpaceDirection = bottomSpaceDirection;
        }

        @Override
        public GestureKey key() {
            return key;
        }

        RectF visualBounds() {
            float insetY = Math.min(visualGap / 2f, bounds.height() * 0.18f);
            float insetX = Math.min(visualGap, bounds.width() * 0.32f);
            float left = bounds.left;
            float right = bounds.right;
            if (bottomSpaceDirection < 0) {
                left += insetX;
            } else if (bottomSpaceDirection > 0) {
                right -= insetX;
            }
            return new RectF(
                    left,
                    bounds.top + insetY,
                    right,
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
        public boolean coreContains(float x, float y, float inset) {
            float insetX = Math.min(Math.max(0f, inset), bounds.width() * 0.32f);
            float insetY = Math.min(Math.max(0f, inset), bounds.height() * 0.32f);
            return x >= bounds.left + insetX
                    && x <= bounds.right - insetX
                    && y >= bounds.top + insetY
                    && y <= bounds.bottom - insetY;
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
        public float width() {
            return bounds.width();
        }

        @Override
        public float height() {
            return bounds.height();
        }

        @Override
        public float centerX() {
            return bounds.centerX();
        }

        @Override
        public float centerY() {
            return bounds.centerY();
        }

        @Override
        public boolean isPrimaryBottomControl() {
            return primaryBottomControl;
        }
    }

    private static final class TouchSample {
        final String value;
        final String keyCodePoints;
        final float offsetXDp;
        final float offsetYDp;
        final GestureAction action;
        final long timeMs;

        TouchSample(
                String value,
                String keyCodePoints,
                float offsetXDp,
                float offsetYDp,
                GestureAction action,
                long timeMs) {
            this.value = value == null ? "" : value;
            this.keyCodePoints = keyCodePoints == null ? "" : keyCodePoints;
            this.offsetXDp = offsetXDp;
            this.offsetYDp = offsetYDp;
            this.action = action == null ? GestureAction.TAP : action;
            this.timeMs = timeMs;
        }
    }
}
