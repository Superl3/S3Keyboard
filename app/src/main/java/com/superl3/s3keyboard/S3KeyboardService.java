package com.superl3.s3keyboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.SurroundingText;
import android.view.inputmethod.InputMethodManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public final class S3KeyboardService extends InputMethodService {
    private static final long VOICE_INPUT_RESULT_TIMEOUT_MS = 30_000L;
    private static final long VOICE_INPUT_RECONNECT_DELAY_MS = 350L;

    private final HangulAutomata automata = new HangulAutomata();
    private final HangulCommitOnlyEditor commitOnlyEditor = new HangulCommitOnlyEditor();
    private final DoubleSpacePeriodState doubleSpacePeriodState = new DoubleSpacePeriodState();
    private final EnglishShiftState englishShiftState = new EnglishShiftState();
    private final PreviousTextRepairState previousTextRepairState = new PreviousTextRepairState();
    private final EnglishQwertyInputAssistant qwertyInputAssistant = new EnglishQwertyInputAssistant();
    private KeyboardSettings settings = KeyboardSettings.defaults();
    private KeyboardLayoutProfiles layoutProfiles = KeyboardLayoutProfiles.defaults();
    private ResolvedImeAction enterAction = ImeActionLabelResolver.defaultAction();
    private EditorInputPolicy editorPolicy = EditorInputPolicy.DEFAULT;
    private AppInputProfile appInputProfile = AppInputProfile.STANDARD;
    private HangulKeyboardView inputView;
    private WatchRadialKeyboardView watchRadialInputView;
    private FrameLayout inputRoot;
    private LinearLayout inputContentContainer;
    private TextView runtimeStateIndicatorView;
    private View clipboardOverlayView;
    private final int[] touchRegionLocation = new int[2];
    private final Rect touchRegionBounds = new Rect();
    private PreviewOverlayController previewOverlayController;
    private RemoteCompatibilityPanelController remoteCompatibilityPanelController;
    private InputIssueReportClipboardController inputIssueReportController;
    private QuickThemePanelController quickThemePanelController;
    private AppProfileQuickSettingsController appProfileQuickSettingsController;
    private QuickSettingsPanelController quickSettingsPanelController;
    private ClipboardPanelController clipboardPanelController;
    private RemoteNavigationToolbarController remoteNavigationToolbarController;
    private ThemeClipboardImportController themeClipboardImportController;
    private PopupWindow quickSettingsPopup;
    private View textActionOverlayView;
    private TextActionTransaction textActionTransaction;
    private AiTextActionSettings aiTextActionSettings = AiTextActionSettings.DEFAULT;
    private TextActionProviderClient.Operation activeTextActionProviderOperation;
    private PendingProviderTextAction pendingProviderTextAction;
    private String pendingProviderReplacement;
    private long textActionProviderRequestId;
    private final RemoteInputController remoteInputController = new RemoteInputController(
            () -> settings.remoteImeShortcut,
            (pendingMetaState, lockedMetaState) -> updateShiftStateView());
    private boolean remoteModeAutoActivated;
    private boolean transparentOverlayInputEnabled;
    private boolean watchRadialInputEnabled;
    private boolean watchRadialInputVisible;
    private boolean inputSessionBoundaryPending;
    private TransparentOverlayStyle transparentOverlayStyle =
            TransparentOverlayStyle.TRANSLUCENT_KEYS;
    private String currentEditorPackageName = "";
    private InputConnection commandDispatchInputConnection;
    private int pendingOwnComposingSelectionUpdates;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable pendingVoiceCommitRunnable = this::commitPendingVoiceInputIfReady;
    private long voiceInputRequestId;
    private PendingVoiceInput pendingVoiceInput;
    private final KeyboardCommandDispatcher.Target commandDispatchTarget =
            new S3KeyboardCommandTarget(this);

    @Override
    public View onCreateInputView() {
        dismissPreviewPopup();
        settings = withSessionRuntimeState(KeyboardPreferences.load(this));
        layoutProfiles = KeyboardPreferences.loadLayoutProfiles(this);
        transparentOverlayInputEnabled = KeyboardPreferences.loadTransparentOverlayInputEnabled(this);
        transparentOverlayStyle = KeyboardPreferences.loadTransparentOverlayStyle(this);
        watchRadialInputEnabled = KeyboardPreferences.loadWatchRadialInputEnabled(this);
        aiTextActionSettings = KeyboardPreferences.loadAiTextActionSettings(this);

        clipboardPanelController = new ClipboardPanelController(
                this,
                () -> settings,
                () -> editorPolicy,
                this::commitClipboardText);
        remoteNavigationToolbarController = new RemoteNavigationToolbarController(
                this,
                () -> settings,
                command -> handleRemoteCommand(getCurrentInputConnection(), command),
                () -> {
                    remoteInputController.reset();
                    updateShiftStateView();
                    updateRuntimeStateIndicator();
                });

        inputRoot = new FrameLayout(this);
        inputRoot.setBackgroundColor(Color.TRANSPARENT);
        inputRoot.setClipChildren(false);
        inputRoot.setClipToPadding(false);

        LinearLayout mainContainer = SettingsRowBuilder.vertical(this);
        inputContentContainer = mainContainer;

        inputView = new HangulKeyboardView(this);
        inputView.setKeyboardSurface(editorPolicy.surface);
        inputView.setSettings(settings);
        inputView.setTransparentOverlayPresentation(transparentOverlayInputEnabled);
        inputView.setTransparentOverlayStyle(transparentOverlayStyle);
        inputView.setRedactTypingEventText(!editorPolicy.allowTextConveniences);
        updateShiftStateView();
        inputView.setOnKeyGestureListener(this::onKeyGesture);
        inputView.setOnPreviewOverlayListener(this::showPreviewOverlays);
        applyPendingInputSessionBoundary();

        watchRadialInputView = new WatchRadialKeyboardView(this);
        watchRadialInputView.setSettings(settings);
        watchRadialInputView.setOnKeyGestureListener(this::onKeyGesture);
        watchRadialInputView.setPreferredPageSupplier(this::preferredWatchRadialPage);

        FrameLayout keyboardSurfaceContainer = new FrameLayout(this);
        FrameLayout.LayoutParams surfaceParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        keyboardSurfaceContainer.addView(inputView, surfaceParams);
        keyboardSurfaceContainer.addView(watchRadialInputView, surfaceParams);

        runtimeStateIndicatorView = SettingsRowBuilder.bodyLabel(this, "");
        runtimeStateIndicatorView.setGravity(Gravity.CENTER);
        runtimeStateIndicatorView.setPadding(
                SettingsRowBuilder.dp(this, 6),
                SettingsRowBuilder.dp(this, 2),
                SettingsRowBuilder.dp(this, 6),
                SettingsRowBuilder.dp(this, 2));
        mainContainer.addView(runtimeStateIndicatorView, SettingsRowBuilder.matchWrap());
        mainContainer.addView(
                remoteNavigationToolbarController.createView(),
                SettingsRowBuilder.matchWrap());
        mainContainer.addView(keyboardSurfaceContainer, SettingsRowBuilder.matchWrap());

        inputRoot.addView(mainContainer, SettingsRowBuilder.frameMatchWrap());

        clipboardOverlayView = clipboardPanelController.createOverlayView();
        inputRoot.addView(clipboardOverlayView, clipboardPanelController.overlayLayoutParams());

        initializePanelControllers();

        updateInputSurfaceVisibility();
        updateToolbarVisibility();
        updateClipboardListener();
        updateRuntimeStateIndicator();
        applyImeWindowBlur();
        return inputRoot;
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return KeyboardPreferences.loadTransparentOverlayInputEnabled(this)
                || KeyboardPreferences.loadWatchRadialInputEnabled(this);
    }

    @Override
    public void onUpdateExtractingVisibility(EditorInfo editorInfo) {
        // Fullscreen is used only as a transparent overlay. The host application's editor is
        // the source of truth, so Android's mirrored extract editor must never be shown.
        setExtractViewShown(false);
    }

    @Override
    public void onConfigureWindow(Window window, boolean isFullscreen, boolean isCandidatesOnly) {
        super.onConfigureWindow(window, isFullscreen, isCandidatesOnly);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0f);
        applyImeWindowBlur(window);
    }

    @Override
    public void onComputeInsets(Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (transparentOverlayInputEnabled || watchRadialInputEnabled) {
            // Fullscreen prevents legacy adjustResize. Only visible IME controls consume touch;
            // host-app controls remain interactive through the rest of the transparent window.
            View decorView = getWindow().getWindow().getDecorView();
            int windowHeight = decorView.getHeight();
            outInsets.contentTopInsets = windowHeight;
            outInsets.visibleTopInsets = windowHeight;
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION;
            outInsets.touchableRegion.setEmpty();
            addTouchableViewBounds(outInsets.touchableRegion, inputContentContainer);
            addTouchableViewBounds(outInsets.touchableRegion, clipboardOverlayView);
            addTouchableViewBounds(outInsets.touchableRegion, textActionOverlayView);
        }
    }

    private void applyPendingInputSessionBoundary() {
        if (!inputSessionBoundaryPending || inputView == null) {
            return;
        }
        inputView.beginInputSession();
        inputSessionBoundaryPending = false;
    }

    private void addTouchableViewBounds(Region region, View view) {
        if (region == null
                || view == null
                || !view.isShown()
                || view.getWidth() <= 0
                || view.getHeight() <= 0) {
            return;
        }
        view.getLocationInWindow(touchRegionLocation);
        touchRegionBounds.set(
                touchRegionLocation[0],
                touchRegionLocation[1],
                touchRegionLocation[0] + view.getWidth(),
                touchRegionLocation[1] + view.getHeight());
        region.op(touchRegionBounds, Region.Op.UNION);
    }

    private void requestOverlayTouchableRegionUpdate() {
        if (inputRoot == null || (!transparentOverlayInputEnabled && !watchRadialInputEnabled)) {
            return;
        }
        inputRoot.requestLayout();
        inputRoot.requestApplyInsets();
    }

    private void initializePanelControllers() {
        previewOverlayController = new PreviewOverlayController(this);
        previewOverlayController.setSettings(settings);
        remoteCompatibilityPanelController = new RemoteCompatibilityPanelController(
                this,
                () -> currentEditorPackageName,
                this::sendCompatibilityKey);
        inputIssueReportController = new InputIssueReportClipboardController(
                this,
                this::prepareIssueReport,
                () -> currentEditorPackageName,
                () -> appInputProfile,
                () -> settings,
                () -> editorPolicy);
        themeClipboardImportController = new ThemeClipboardImportController(
                this,
                () -> settings,
                this::enterActionLabel,
                () -> editorPolicy.forceNumberRow,
                this::applyRuntimeSettings,
                this::dismissQuickSettings);
        quickThemePanelController = new QuickThemePanelController(
                this,
                () -> settings,
                this::enterActionLabel,
                () -> editorPolicy.forceNumberRow,
                this::applyRuntimeSettings,
                this::dismissQuickSettings);
        appProfileQuickSettingsController = new AppProfileQuickSettingsController(
                this,
                () -> currentEditorPackageName,
                () -> appInputProfile,
                () -> editorPolicy,
                this::reloadCurrentEditorSessionSettings,
                this::dismissQuickSettings);
        quickSettingsPanelController = new QuickSettingsPanelController(
                this,
                remoteCompatibilityPanelController,
                quickThemePanelController,
                appProfileQuickSettingsController,
                () -> settings,
                this::remoteModeToggleLabel,
                this::toggleRemoteMode,
                this::singleTapCommitModeToggleLabel,
                this::singleTapCommitModeEnabled,
                this::toggleSingleTapCommitMode,
                this::watchRadialInputToggleLabel,
                this::watchRadialInputEnabled,
                this::toggleWatchRadialInput,
                this::numberRowToggleLabel,
                this::activeNumberRowVisible,
                this::toggleActiveNumberRow,
                this::setHandedness,
                this::openTextToolsFromQuickSettings,
                this::importThemeFromClipboard,
                this::copyInputIssueReport,
                this::dismissQuickSettings);
    }

    private void commitClipboardText(String text) {
        if (!TextToolsPolicy.allowsInsertion(editorPolicy, settings.remoteModeEnabled, text)) {
            return;
        }
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return;
        }
        commitCurrent(inputConnection);
        if (!InputConnectionTextOperator.commitText(inputConnection, text)) {
            return;
        }
        doubleSpacePeriodState.reset();
        previousTextRepairState.reset();
        qwertyInputAssistant.reset();

    }

    private void prepareIssueReport() {
        if (inputView != null) {
            inputView.flushLearningState();
        }
    }

    private void applyRuntimeSettings(KeyboardSettings nextSettings) {
        settings = withSessionRuntimeState(nextSettings);
        applyCurrentSettingsToInputView();
    }

    private void applyImeWindowBlur() {
        applyImeWindowBlur(getWindow().getWindow());
    }

    private void applyImeWindowBlur(Window window) {
        if (BuildConfig.DEBUG) {
            KeyboardVisualEffects effects = settings == null
                    ? KeyboardVisualEffects.DEFAULT
                    : settings.visualEffects;
            Log.d(
                    "S3KeyboardBlur",
                    "service allow=" + (!transparentOverlayInputEnabled && !watchRadialInputEnabled)
                            + " enabled=" + effects.blurEnabled
                            + " radiusDp=" + effects.blurRadiusDp);
        }
        boolean applied = ImeWindowBlurController.apply(
                window,
                settings == null ? KeyboardVisualEffects.DEFAULT : settings.visualEffects,
                !transparentOverlayInputEnabled && !watchRadialInputEnabled,
                getResources().getDisplayMetrics().density);
        if (inputView != null) {
            inputView.setSystemBlurApplied(applied);
        }
    }

    private void importThemeFromClipboard() {
        if (themeClipboardImportController != null) {
            themeClipboardImportController.importFromClipboard();
        }
    }

    private void copyInputIssueReport() {
        inputIssueReportController.copyToClipboard();
    }

    private void reloadCurrentEditorSessionSettings() {
        InputConnection connection = getCurrentInputConnection();
        if (connection != null) {
            commitCurrent(connection);
        }
        automata.reset();
        commitOnlyEditor.reset();
        qwertyInputAssistant.reset();
        remoteInputController.reset();
        loadSettingsForEditor(getCurrentInputEditorInfo());
        applyCurrentSettingsToInputView();
        updateToolbarVisibility();
        updateClipboardListener();
    }

    private void updateRuntimeStateIndicator() {
        if (runtimeStateIndicatorView == null) {
            return;
        }
        String language = settings.keyboardMode == KeyboardMode.ENGLISH ? "EN" : "한글";
        String layout = SettingsDisplayLabels.label(this, activeLayoutProfile());
        StringBuilder active = new StringBuilder();
        if (settings.remoteModeEnabled) {
            active.append(" · Remote");
            appendRemoteModifierState(active);
        }
        if (englishShiftState.isLocked()) active.append(" · CAPS");
        if (singleTapCommitModeEnabled()) active.append(" · 1F");
        runtimeStateIndicatorView.setText(getString(
                R.string.runtime_state_indicator_format, language, layout, active.toString()));
    }

    private void appendRemoteModifierState(StringBuilder active) {
        int pending = remoteInputController.pendingMetaState();
        int locked = remoteInputController.lockedMetaState();
        appendRemoteModifierState(active, "Ctrl", KeyEvent.META_CTRL_ON, pending, locked);
        appendRemoteModifierState(active, "Alt", KeyEvent.META_ALT_ON, pending, locked);
        appendRemoteModifierState(active, "Win", KeyEvent.META_META_ON, pending, locked);
    }

    private void appendRemoteModifierState(
            StringBuilder active, String label, int meta, int pending, int locked) {
        if ((locked & meta) == meta) {
            active.append(" · ").append(label).append(":LOCK");
        } else if ((pending & meta) == meta) {
            active.append(" · ").append(label).append(":1x");
        }
    }

    private void updateToolbarVisibility() {
        if (clipboardPanelController != null) {
            clipboardPanelController.updateVisibility();
        }
        if (remoteNavigationToolbarController != null) {
            remoteNavigationToolbarController.updateVisibility();
        }
        requestOverlayTouchableRegionUpdate();
    }

    private void updateClipboardListener() {
        if (clipboardPanelController != null) {
            clipboardPanelController.updateClipboardListener();
        }
    }

    private void removeClipboardListener() {
        if (clipboardPanelController != null) {
            clipboardPanelController.removeClipboardListener();
        }
    }

    private void scheduleAuditRenderReadyLog() {
        if (!BuildConfig.DEBUG || inputView == null || settings == null) {
            return;
        }
        String themeId = RuntimeDefaults.stringOrDefault(
                KeyboardPreferences.loadSelectedThemeId(this),
                "");
        String mode = settings.keyboardMode.preferenceValue;
        String material = settings.visualEffects.materialStyle;
        inputView.postOnAnimation(() -> inputView.postOnAnimation(() -> {
            logAuditGeometry("renderReady", themeId, mode, material);
            inputView.postDelayed(
                    () -> logAuditGeometry("geometry", themeId, mode, material),
                    300L);
            inputView.postDelayed(
                    () -> logAuditGeometry("geometry", themeId, mode, material),
                    600L);
            inputView.postDelayed(
                    () -> logAuditGeometry("geometry", themeId, mode, material),
                    900L);
            inputView.postDelayed(
                    () -> logAuditGeometry("geometry", themeId, mode, material),
                    1200L);
            inputView.postDelayed(
                    () -> logAuditGeometry("geometry", themeId, mode, material),
                    1500L);
        }));
    }

    private void logAuditGeometry(String event, String themeId, String mode, String material) {
        if (!BuildConfig.DEBUG || inputView == null) {
            return;
        }
        int[] location = new int[2];
        inputView.getLocationOnScreen(location);
        int screenWidth;
        int screenHeight;
        android.view.WindowManager windowManager =
                (android.view.WindowManager) getSystemService(WINDOW_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.graphics.Rect bounds = windowManager.getMaximumWindowMetrics().getBounds();
            screenWidth = bounds.width();
            screenHeight = bounds.height();
        } else {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
        }
        int navBottomInset = 0;
        android.view.WindowInsets rootInsets = inputView.getRootWindowInsets();
        if (rootInsets != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                navBottomInset = rootInsets.getInsetsIgnoringVisibility(
                        android.view.WindowInsets.Type.navigationBars()).bottom;
            } else {
                navBottomInset = rootInsets.getStableInsetBottom();
            }
        }
        int width = inputView.getWidth();
        int height = inputView.getHeight();
        int rawInputBottom = location[1] + height;
        int keyboardBottom = rawInputBottom - navBottomInset;
        int expectedBottom = screenHeight - navBottomInset;
        int bottomDelta = keyboardBottom - expectedBottom;
        Log.d(
                "S3KeyboardAudit",
                event
                        + " theme=" + themeId
                        + " mode=" + mode
                        + " material=" + material
                        + " width=" + width
                        + " height=" + height
                        + " x=" + location[0]
                        + " y=" + location[1]
                        + " screenWidth=" + screenWidth
                        + " screenHeight=" + screenHeight
                        + " navBottomInset=" + navBottomInset
                        + " rawInputBottom=" + rawInputBottom
                        + " keyboardBottom=" + keyboardBottom
                        + " expectedBottom=" + expectedBottom
                        + " bottomDelta=" + bottomDelta
                        + " uptimeMs=" + android.os.SystemClock.uptimeMillis());
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        updateTransparentOverlayPresentation();
        loadSettingsForEditor(info);
        if (inputView != null) {
            inputView.setRedactTypingEventText(!editorPolicy.allowTextConveniences);
            applyCurrentSettingsToInputView();
            scheduleAuditRenderReadyLog();
        }
        updateToolbarVisibility();
        updateClipboardListener();
        mainHandler.post(pendingVoiceCommitRunnable);
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        updateTransparentOverlayPresentation();
        loadSettingsForEditor(attribute);
        updateToolbarVisibility();
        updateClipboardListener();
        automata.reset();
        commitOnlyEditor.reset();
        pendingOwnComposingSelectionUpdates = 0;
        textActionTransaction = null;
        cancelActiveProviderTextAction(true);
        dismissTextActionPanel();
        previousTextRepairState.reset();
        doubleSpacePeriodState.reset();
        englishShiftState.reset();
        qwertyInputAssistant.reset();
        remoteInputController.reset();
        inputSessionBoundaryPending = true;
        applyPendingInputSessionBoundary();
        if (watchRadialInputView != null) {
            watchRadialInputView.resetSession();
        }
        if (inputView != null) {
            inputView.setRedactTypingEventText(!editorPolicy.allowTextConveniences);
            applyCurrentSettingsToInputView();
        }
        updateShiftStateView();

        mainHandler.post(pendingVoiceCommitRunnable);
    }

    @Override
    public void onFinishInput() {
        dismissPreviewPopup();
        dismissQuickSettings();
        removeClipboardListener();
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection != null) {
            commitCurrent(inputConnection);
        }
        if (inputView != null) {
            inputView.flushLearningState();
        }
        automata.reset();
        commitOnlyEditor.reset();
        pendingOwnComposingSelectionUpdates = 0;
        textActionTransaction = null;
        cancelActiveProviderTextAction(true);
        dismissTextActionPanel();
        previousTextRepairState.reset();
        englishShiftState.reset();
        qwertyInputAssistant.reset();
        remoteInputController.reset();
        remoteModeAutoActivated = false;
        if (watchRadialInputView != null) {
            watchRadialInputView.resetSession();
        }
        if (remoteCompatibilityPanelController != null) {
            remoteCompatibilityPanelController.reset();
        }
        super.onFinishInput();
    }

    @Override
    public void onUpdateSelection(
            int oldSelStart,
            int oldSelEnd,
            int newSelStart,
            int newSelEnd,
            int candidatesStart,
            int candidatesEnd) {
        super.onUpdateSelection(
                oldSelStart,
                oldSelEnd,
                newSelStart,
                newSelEnd,
                candidatesStart,
                candidatesEnd);
        if (commitOnlyEditor.shouldAcceptExternalSelectionChange(
                oldSelStart,
                oldSelEnd,
                newSelStart,
                newSelEnd,
                candidatesStart,
                candidatesEnd)) {
            commitOnlyEditor.acceptDisplayedComposition(automata);
            InputConnection inputConnection = getCurrentInputConnection();
            if (inputConnection != null) {
                InputConnectionTextOperator.finishComposing(inputConnection);
            }
            pendingOwnComposingSelectionUpdates = 0;
            previousTextRepairState.reset();
            doubleSpacePeriodState.reset();
            return;
        }
        if (automata.getComposingText().isEmpty()) {

            return;
        }
        boolean selectionMismatch = isComposingSelectionMismatch(
                oldSelStart,
                oldSelEnd,
                newSelStart,
                newSelEnd,
                candidatesStart,
                candidatesEnd);
        if (pendingOwnComposingSelectionUpdates > 0 && !selectionMismatch) {
            pendingOwnComposingSelectionUpdates--;
            return;
        }
        if (selectionMismatch) {
            InputConnection inputConnection = getCurrentInputConnection();
            if (inputConnection != null) {
                releaseCurrentCompositionForExternalCursorMove(inputConnection);
            } else {
                automata.reset();
                commitOnlyEditor.reset();
                pendingOwnComposingSelectionUpdates = 0;
            }
            previousTextRepairState.reset();
            doubleSpacePeriodState.reset();
        }
    }

    public void onKeyGesture(String value) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null || value == null || value.isEmpty()) {
            return;
        }
        ReleaseSafeDiagnostics.recordGesture(this, value);
        commandDispatchInputConnection = inputConnection;
        try {
            KeyboardCommandDispatcher.dispatch(value, commandDispatchTarget);
        } finally {
            commandDispatchInputConnection = null;
        }
    }

    InputConnection commandInputConnection() {
        if (commandDispatchInputConnection == null) {
            throw new IllegalStateException("Keyboard command dispatched without an input connection.");
        }
        return commandDispatchInputConnection;
    }

    void resetDoubleSpacePeriodState() {
        doubleSpacePeriodState.reset();
    }

    private boolean explicitEnglishCorrectionAvailable(InputConnection inputConnection) {
        return inputConnection != null
                && settings.keyboardMode == KeyboardMode.ENGLISH
                && !settings.remoteModeEnabled
                && layoutProfiles.activeIsQwerty(KeyboardMode.ENGLISH)
                && editorPolicy.allowTextConveniences
                && !editorPolicy.rawKeyInput
                && !editorPolicy.replacesMainRows()
                && !InputConnectionTextOperator.hasSelection(inputConnection);
    }

    void correctCurrentEnglishWord(InputConnection inputConnection) {
        doubleSpacePeriodState.reset();
        if (!explicitEnglishCorrectionAvailable(inputConnection)) {
            qwertyInputAssistant.reset();
            return;
        }
        qwertyInputAssistant.correctCurrentWordExplicitly(inputConnection);
        qwertyInputAssistant.reset();
    }

    void toggleClipboardPanel() {
        if (clipboardPanelController != null) {
            clipboardPanelController.toggle();
            requestOverlayTouchableRegionUpdate();
        }
    }

    private void openTextToolsFromQuickSettings() {
        dismissQuickSettings();
        toggleClipboardPanel();
    }

    void handleVoiceInput() {
        if (editorPolicy.password) {
            Toast.makeText(this, R.string.voice_input_secure_field, Toast.LENGTH_SHORT).show();
            return;
        }
        if (editorPolicy.rawKeyInput || settings.remoteModeEnabled) {
            Toast.makeText(this, R.string.voice_input_unsupported_field, Toast.LENGTH_SHORT).show();
            return;
        }
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null || currentEditorPackageName.isEmpty()) {
            Toast.makeText(this, R.string.voice_input_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        commitCurrent(inputConnection);
        if (inputView != null) {
            inputView.cancelActiveGestureSession();
        }
        dismissPreviewPopup();
        dismissQuickSettings();

        long requestId = ++voiceInputRequestId;
        String targetPackage = currentEditorPackageName;
        pendingVoiceInput = null;
        ResultReceiver receiver = new ResultReceiver(mainHandler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                handleVoiceInputResult(requestId, targetPackage, resultCode, resultData);
            }
        };
        try {
            startActivity(VoiceInputActivity.intent(this, receiver, voiceInputLanguageTag()));
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.voice_input_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private String voiceInputLanguageTag() {
        return settings.keyboardMode == KeyboardMode.HANGUL ? "ko-KR" : "en-US";
    }

    private void handleVoiceInputResult(
            long requestId,
            String targetPackage,
            int resultCode,
            Bundle resultData) {
        if (requestId != voiceInputRequestId) {
            return;
        }
        if (resultCode == VoiceInputResult.CANCELLED) {
            return;
        }
        if (resultCode == VoiceInputResult.UNAVAILABLE) {
            Toast.makeText(this, R.string.voice_input_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        String text = resultData == null
                ? ""
                : VoiceInputResult.normalizeRecognizedText(
                        resultData.getString(VoiceInputResult.EXTRA_TEXT));
        if (resultCode != VoiceInputResult.RECOGNIZED || text.isEmpty()) {
            Toast.makeText(this, R.string.voice_input_no_match, Toast.LENGTH_SHORT).show();
            return;
        }
        pendingVoiceInput = new PendingVoiceInput(
                requestId,
                targetPackage,
                text,
                SystemClock.uptimeMillis());
        mainHandler.removeCallbacks(pendingVoiceCommitRunnable);
        mainHandler.postDelayed(pendingVoiceCommitRunnable, VOICE_INPUT_RECONNECT_DELAY_MS);
    }

    private void commitPendingVoiceInputIfReady() {
        PendingVoiceInput pending = pendingVoiceInput;
        if (pending == null) {
            return;
        }
        long nowMs = SystemClock.uptimeMillis();
        if (pending.requestId != voiceInputRequestId
                || pending.isExpired(nowMs, VOICE_INPUT_RESULT_TIMEOUT_MS)) {
            pendingVoiceInput = null;
            return;
        }
        if (!pending.targets(currentEditorPackageName)) {
            if (!currentEditorPackageName.isEmpty()) {
                pendingVoiceInput = null;
            }
            return;
        }
        if (editorPolicy.password || editorPolicy.rawKeyInput || settings.remoteModeEnabled) {
            pendingVoiceInput = null;
            return;
        }
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return;
        }
        commitCurrent(inputConnection);
        if (InputConnectionTextOperator.commitText(inputConnection, pending.text)) {
            pendingVoiceInput = null;
            doubleSpacePeriodState.reset();
            previousTextRepairState.reset();
            qwertyInputAssistant.reset();

            return;
        }
        if (pending.recordFailedCommitAndShouldRetry(4)) {
            mainHandler.postDelayed(pendingVoiceCommitRunnable, VOICE_INPUT_RECONNECT_DELAY_MS);
        }
    }

    void handleUndo() {
        InputConnection inputConnection = commandInputConnection();
        commitCurrent(inputConnection);
        doubleSpacePeriodState.reset();
        if (textActionTransaction != null
                && textActionTransaction.restore(
                        new InputConnectionTextActionEditor(inputConnection), currentEditorPackageName)) {
            textActionTransaction = null;
            dismissTextActionPanel();
            return;
        }
        if (!ImeConnectionDispatcher.performUndo(inputConnection)) {
            Toast.makeText(this, R.string.undo_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    void inputDingulContextualVowel(InputConnection inputConnection, boolean centerVowelKey) {
        doubleSpacePeriodState.reset();
        boolean suppressPreviousTextRepair = previousTextRepairState.consumeSuppressNextRepair();
        if (usesCommitOnlyHangul()) {
            inputDingulContextualVowelCommitOnly(inputConnection, centerVowelKey);
            return;
        }

        char currentVowel = automata.currentVowelWithoutFinal();
        char replacementVowel = centerVowelKey
                ? dingulCenterReplacementVowel(currentVowel)
                : dingulWideReplacementVowel(currentVowel);
        if (replacementVowel != '\0' && automata.replaceCurrentVowelWithoutFinal(replacementVowel)) {
            updateComposing(inputConnection);
            return;
        }
        char nextVowel = centerVowelKey
                ? dingulCenterTapValue(currentVowel)
                : dingulWideTapValue(currentVowel);
        if (!suppressPreviousTextRepair
                && currentVowel == '\0'
                && combineWithPreviousOpenSyllableForDingulVowel(inputConnection, centerVowelKey)) {
            return;
        }
        inputText(inputConnection, String.valueOf(nextVowel), suppressPreviousTextRepair);
    }

    private void inputDingulContextualVowelCommitOnly(
            InputConnection inputConnection,
            boolean centerVowelKey) {
        char currentVowel = automata.currentVowelWithoutFinal();
        char replacementVowel = centerVowelKey
                ? dingulCenterReplacementVowel(currentVowel)
                : dingulWideReplacementVowel(currentVowel);
        if (replacementVowel != '\0' && automata.replaceCurrentVowelWithoutFinal(replacementVowel)) {
            commitOnlyEditor.refreshDisplayedComposing(
                    automata,
                    InputConnectionTextOperator.commitOnlySink(inputConnection));
            return;
        }
        char nextVowel = centerVowelKey
                ? dingulCenterTapValue(currentVowel)
                : dingulWideTapValue(currentVowel);
        inputHangulCommitOnly(inputConnection, String.valueOf(nextVowel));
    }

    private boolean combineWithPreviousOpenSyllableForDingulVowel(
            InputConnection inputConnection,
            boolean centerVowelKey) {
        if (InputConnectionTextOperator.hasSelection(inputConnection)) {
            return false;
        }
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(1, 0);
        if (beforeCursor == null || beforeCursor.length() != 1) {
            return false;
        }

        String decomposed = HangulAutomata.decomposeOpenSyllable(beforeCursor.charAt(0));
        if (decomposed == null || decomposed.length() != 2) {
            return false;
        }

        char previousVowel = decomposed.charAt(1);
        char replacementVowel = centerVowelKey
                ? dingulCenterReplacementVowel(previousVowel)
                : dingulWideReplacementVowel(previousVowel);
        if (replacementVowel != '\0') {
            InputConnectionTextOperator.deleteBeforeCursorCodePoint(inputConnection);
            automata.reset();
            for (int i = 0; i < decomposed.length(); i++) {
                String committed = automata.input(decomposed.charAt(i));
                commitAutomataFragment(inputConnection, committed);
            }
            if (automata.replaceCurrentVowelWithoutFinal(replacementVowel)) {
                updateComposing(inputConnection);
                return true;
            }
        }

        char nextVowel = centerVowelKey
                ? dingulCenterTapValue(previousVowel)
                : dingulWideTapValue(previousVowel);
        if (nextVowel == (centerVowelKey ? 'ㅣ' : 'ㅡ')) {
            return false;
        }

        InputConnectionTextOperator.deleteBeforeCursorCodePoint(inputConnection);
        automata.reset();
        for (int i = 0; i < decomposed.length(); i++) {
            String committed = automata.input(decomposed.charAt(i));
            commitAutomataFragment(inputConnection, committed);
        }
        String committed = automata.input(nextVowel);
        commitAutomataFragment(inputConnection, committed);
        updateComposing(inputConnection);
        return true;
    }

    static char dingulCenterReplacementVowel(char currentVowel) {
        switch (currentVowel) {
            case 'ㅏ':
                return 'ㅑ';
            case 'ㅓ':
                return 'ㅕ';
            case 'ㅗ':
                return 'ㅛ';
            case 'ㅜ':
                return 'ㅠ';
            default:
                return '\0';
        }
    }

    static char dingulWideReplacementVowel(char currentVowel) {
        switch (currentVowel) {
            case 'ㅏ':
                return 'ㅐ';
            case 'ㅓ':
                return 'ㅔ';
            case 'ㅔ':
                return 'ㅖ';
            case 'ㅐ':
                return 'ㅒ';
            case 'ㅑ':
                return 'ㅒ';
            case 'ㅕ':
                return 'ㅖ';
            default:
                return '\0';
        }
    }

    static char dingulCenterTapValue(char currentVowel) {
        switch (currentVowel) {
            case 'ㅓ':
            case 'ㅏ':
            case 'ㅗ':
            case 'ㅜ':
                return currentVowel;
            default:
                return 'ㅣ';
        }
    }

    static char dingulWideTapValue(char currentVowel) {
        switch (currentVowel) {
            case 'ㅔ':
            case 'ㅐ':
                return currentVowel;
            default:
                return 'ㅡ';
        }
    }

    void inputText(InputConnection inputConnection, String text) {
        inputText(inputConnection, text, previousTextRepairState.consumeSuppressNextRepair());
    }

    private void inputText(
            InputConnection inputConnection,
            String text,
            boolean suppressPreviousTextRepair) {
        doubleSpacePeriodState.reset();
        if (settings.remoteModeEnabled) {
            String remoteText = englishShiftState.applyToInput(text);
            commitCurrent(inputConnection);
            RemoteKeyStroke stroke = RemoteKeyStroke.forText(remoteText);
            if (stroke != null) {
                remoteInputController.sendKey(inputConnection, stroke.keyCode, stroke.metaState);
            } else {
                InputConnectionTextOperator.commitText(inputConnection, remoteText);
            }
            updateShiftStateView();
            return;
        }
        if (editorPolicy.rawKeyInput) {
            String rawText = settings.keyboardMode == KeyboardMode.ENGLISH
                    ? englishShiftState.applyToInput(text)
                    : applyHangulQwertyShift(text);
            if (settings.keyboardMode == KeyboardMode.HANGUL && containsHangulAutomataText(rawText)) {
                inputHangulCommitOnly(inputConnection, rawText);
                updateShiftStateView();
                return;
            }
            automata.reset();
            commitOnlyEditor.reset();
            sendRawText(rawText, inputConnection);
            updateShiftStateView();
            return;
        }
        if (settings.keyboardMode == KeyboardMode.ENGLISH) {
            inputEnglishText(inputConnection, text);
            updateShiftStateView();
            return;
        }

        if (!editorPolicy.allowComposingText) {
            inputHangulCommitOnly(inputConnection, applyHangulQwertyShift(text));
            return;
        }

        String hangulText = applyHangulQwertyShift(text);
        for (int i = 0; i < hangulText.length(); i++) {
            char ch = hangulText.charAt(i);
            if (!suppressPreviousTextRepair
                    && automata.isEmpty()
                    && HangulAutomata.isVowel(ch)
                    && combineWithPreviousStandaloneConsonant(inputConnection, ch)) {
                continue;
            }
            if (!suppressPreviousTextRepair
                    && automata.isEmpty()
                    && HangulAutomata.canBeFinalConsonant(ch)
                    && combineWithPreviousOpenSyllable(inputConnection, ch)) {
                continue;
            }
            String committed = automata.input(ch);
            commitAutomataFragment(inputConnection, committed);
            updateComposing(inputConnection);
        }
    }

    private void inputEnglishText(InputConnection inputConnection, String text) {
        String committedText = englishShiftState.applyToInput(text);
        InputConnectionTextOperator.commitText(inputConnection, committedText);
        qwertyInputAssistant.reset();
    }

    private void inputHangulCommitOnly(InputConnection inputConnection, String text) {
        commitOnlyEditor.input(
                automata,
                text,
                InputConnectionTextOperator.commitOnlySink(inputConnection));
    }

    private boolean usesCommitOnlyHangul() {
        return usesCommitOnlyHangul(settings, editorPolicy);
    }

    static boolean usesCommitOnlyHangul(KeyboardSettings settings, EditorInputPolicy editorPolicy) {
        KeyboardSettings safeSettings = RuntimeDefaults.keyboardSettings(settings);
        EditorInputPolicy safePolicy = editorPolicy == null ? EditorInputPolicy.DEFAULT : editorPolicy;
        return safeSettings.keyboardMode == KeyboardMode.HANGUL
                && (safePolicy.rawKeyInput || !safePolicy.allowComposingText);
    }

    private static boolean containsHangulAutomataText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (HangulAutomata.isInitialConsonant(ch) || HangulAutomata.isVowel(ch)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAsciiLetters(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))) {
                return false;
            }
        }
        return true;
    }

    private String applyHangulQwertyShift(String text) {
        if (!activeHangulQwerty() || text == null || text.length() != 1) {
            return text;
        }
        if (!englishShiftState.isActive()) {
            return text;
        }
        String shifted = shiftedHangulQwertyJamo(text.charAt(0));
        englishShiftState.consumeOnce();
        return shifted == null ? text : shifted;
    }

    private String shiftedHangulQwertyJamo(char ch) {
        switch (ch) {
            case '\u3142':
                return "\u3143";
            case '\u3148':
                return "\u3149";
            case '\u3137':
                return "\u3138";
            case '\u3131':
                return "\u3132";
            case '\u3145':
                return "\u3146";
            case '\u3150':
                return "\u3152";
            case '\u3154':
                return "\u3156";
            default:
                return null;
        }
    }

    private boolean shiftSupportedByActiveLayout() {
        return settings.remoteModeEnabled || settings.keyboardMode == KeyboardMode.ENGLISH || activeHangulQwerty();
    }

    private boolean activeHangulQwerty() {
        return settings.keyboardMode == KeyboardMode.HANGUL
                && layoutProfiles.activeIsQwerty(settings.keyboardMode);
    }

    private boolean combineWithPreviousStandaloneConsonant(InputConnection inputConnection, char vowel) {
        if (InputConnectionTextOperator.hasSelection(inputConnection)) {
            return false;
        }
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(1, 0);
        if (beforeCursor == null || beforeCursor.length() != 1) {
            return false;
        }
        char previous = beforeCursor.charAt(0);
        if (!HangulAutomata.isInitialConsonant(previous)) {
            return false;
        }

        InputConnectionTextOperator.deleteBeforeCursorCodePoint(inputConnection);
        automata.reset();
        String committed = automata.input(previous);
        commitAutomataFragment(inputConnection, committed);
        committed = automata.input(vowel);
        commitAutomataFragment(inputConnection, committed);
        updateComposing(inputConnection);
        return true;
    }

    private boolean combineWithPreviousOpenSyllable(InputConnection inputConnection, char finalConsonant) {
        if (InputConnectionTextOperator.hasSelection(inputConnection)) {
            return false;
        }
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(1, 0);
        if (beforeCursor == null || beforeCursor.length() != 1) {
            return false;
        }
        String decomposed = HangulAutomata.decomposeOpenSyllable(beforeCursor.charAt(0));
        if (decomposed == null) {
            return false;
        }

        InputConnectionTextOperator.deleteBeforeCursorCodePoint(inputConnection);
        automata.reset();
        for (int i = 0; i < decomposed.length(); i++) {
            String committed = automata.input(decomposed.charAt(i));
            commitAutomataFragment(inputConnection, committed);
        }
        String committed = automata.input(finalConsonant);
        commitAutomataFragment(inputConnection, committed);
        updateComposing(inputConnection);
        return true;
    }

    private void commitAutomataFragment(InputConnection inputConnection, String committed) {
        if (!committed.isEmpty()) {
            InputConnectionTextOperator.commitTextReplacingComposing(inputConnection, committed);
        }
    }

    private void loadSettingsForEditor(EditorInfo info) {
        KeyboardSettings storedSettings = KeyboardPreferences.load(this);

        layoutProfiles = KeyboardPreferences.loadLayoutProfiles(this);
        boolean remotePackageMatched = KeyboardPreferences.shouldAutoEnableRemoteMode(
                this,
                AppPackageCatalog.normalizePackageName(info == null ? null : info.packageName));
        InputSessionSettings session = InputSessionSettingsResolver.resolve(
                info,
                storedSettings,
                remotePackageMatched,
                KeyboardPreferences.loadAppInputProfileOverrides(this),
                ImeActionLabelResolver.resolve(info).label(this));
        enterAction = session.enterAction;
        editorPolicy = session.editorPolicy;
        currentEditorPackageName = session.packageName;
        appInputProfile = session.appInputProfile;
        remoteModeAutoActivated = session.remoteModeAutoActivated;
        settings = withSessionRuntimeState(session.runtimeSettings);
        ReleaseSafeDiagnostics.recordSession(
                this,
                info,
                appInputProfile,
                editorPolicy,
                settings,
                layoutProfiles.activeFor(settings.keyboardMode),
                KeyboardPreferences.loadSingleTapCommitModeEnabled(this));
    }

    private KeyboardSettings withSessionRuntimeState(KeyboardSettings source) {
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(source);
        return safe
                .withEnterKeyLabel(enterActionLabel())
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
    }

    void commitSpace(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        if (settings.remoteModeEnabled) {
            doubleSpacePeriodState.reset();
            remoteInputController.sendKey(inputConnection, KeyEvent.KEYCODE_SPACE, 0);
            qwertyInputAssistant.reset();
            return;
        }
        if (editorPolicy.rawKeyInput) {
            doubleSpacePeriodState.reset();
            sendSoftKey(inputConnection, KeyEvent.KEYCODE_SPACE, 0);
            qwertyInputAssistant.reset();
            return;
        }
        DoubleSpacePeriodState.SpaceResult result = doubleSpacePeriodState.onSpace(
                settings.keyboardMode,
                settings.englishDoubleSpacePeriodEnabled && editorPolicy.allowTextConveniences,
                System.currentTimeMillis());
        if (result == DoubleSpacePeriodState.SpaceResult.REPLACE_PREVIOUS_SPACE_WITH_PERIOD_SPACE) {
            InputConnectionTextOperator.deleteBeforeCursorCodePoint(inputConnection);
            InputConnectionTextOperator.commitText(inputConnection, ". ");
        } else {
            InputConnectionTextOperator.commitText(inputConnection, " ");
        }
        qwertyInputAssistant.reset();
    }

    void performEnter(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        ImeConnectionDispatcher.performEnter(
                inputConnection,
                enterAction,
                editorPolicy.rawKeyInput,
                settings.remoteModeEnabled,
                (keyCode, metaState) -> sendSoftKey(inputConnection, keyCode, metaState),
                (keyCode, metaState) -> remoteInputController.sendKey(inputConnection, keyCode, metaState));
        qwertyInputAssistant.reset();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
    }

    void commitExplicitNewline(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        ImeConnectionDispatcher.commitExplicitNewline(inputConnection);
        qwertyInputAssistant.reset();

    }

    void toggleLanguage(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        doubleSpacePeriodState.reset();
        englishShiftState.reset();
        qwertyInputAssistant.reset();
        if (editorPolicy.locksLanguageToggle()) {
            updateShiftStateView();
            return;
        }
        KeyboardMode nextMode = settings.keyboardMode.next();
        settings = withSessionRuntimeState(settings.withKeyboardMode(nextMode));
        KeyboardPreferences.saveKeyboardMode(this, nextMode);
        applyCurrentSettingsToInputView();
    }

    void handleShiftOnce() {
        if (!shiftSupportedByActiveLayout()) {
            return;
        }
        doubleSpacePeriodState.reset();
        englishShiftState.onShiftOnceCommand();
        updateShiftStateView();
    }

    void handleShiftLock() {
        if (!shiftSupportedByActiveLayout()) {
            return;
        }
        doubleSpacePeriodState.reset();
        englishShiftState.onShiftLockCommand();
        updateShiftStateView();
    }

    private void updateShiftStateView() {
        if (inputView != null) {
            inputView.setEnglishShiftState(
                    shiftSupportedByActiveLayout() && englishShiftState.isActive(),
                    shiftSupportedByActiveLayout() && englishShiftState.isLocked());
            inputView.setRemoteMetaState(
                    remoteInputController.pendingMetaState(),
                    remoteInputController.lockedMetaState());
        }
        updateRuntimeStateIndicator();
    }

    private String enterActionLabel() {
        return enterAction.label(this);
    }

    void delete(InputConnection inputConnection) {
        previousTextRepairState.markDelete();
        if (settings.remoteModeEnabled) {
            automata.reset();
            commitOnlyEditor.reset();
            InputConnectionTextOperator.finishComposing(inputConnection);
            remoteInputController.sendKey(inputConnection, KeyEvent.KEYCODE_DEL, 0);
            return;
        }
        if (editorPolicy.rawKeyInput) {
            automata.reset();
            commitOnlyEditor.reset();
            InputConnectionTextOperator.finishComposing(inputConnection);
            sendSoftKey(inputConnection, KeyEvent.KEYCODE_DEL, 0);
            return;
        }
        if (usesCommitOnlyHangul() && commitOnlyEditor.backspace(
                automata,
                InputConnectionTextOperator.commitOnlySink(inputConnection))) {
            return;
        }
        if (editorPolicy.allowComposingText && automata.backspace()) {
            updateComposing(inputConnection);
        } else {
            deleteCommittedText(inputConnection);
        }
    }

    void deleteWord(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        previousTextRepairState.markDelete();
        doubleSpacePeriodState.reset();
        if (settings.remoteModeEnabled) {
            remoteInputController.sendKey(inputConnection, KeyEvent.KEYCODE_DEL, KeyEvent.META_CTRL_ON);
            return;
        }
        if (editorPolicy.rawKeyInput) {
            sendSoftKey(inputConnection, KeyEvent.KEYCODE_DEL, KeyEvent.META_CTRL_ON);

            return;
        }
        if (InputConnectionTextOperator.deleteSelectedText(inputConnection)) {
            InputConnectionTextOperator.finishComposing(inputConnection);

            return;
        }
        int deleteCount = wordDeleteCount(inputConnection);
        if (deleteCount <= 0) {
            deleteCommittedText(inputConnection);
        } else {
            InputConnectionTextOperator.finishComposing(inputConnection);
            InputConnectionTextOperator.deleteBeforeCursorCodePoints(inputConnection, deleteCount);
            InputConnectionTextOperator.finishComposing(inputConnection);
        }
    }

    private void deleteCommittedText(InputConnection inputConnection) {
        automata.reset();
        commitOnlyEditor.reset();
        InputConnectionTextOperator.deleteCommittedGrapheme(inputConnection);
    }

    private int wordDeleteCount(InputConnection inputConnection) {
        if (inputConnection == null) {
            return 0;
        }
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(64, 0);
        return EditorTextBoundaryPolicy.trailingWordCodePointCount(beforeCursor);
    }

    void moveCursor(InputConnection inputConnection, boolean right) {
        commitCurrent(inputConnection);
        if (settings.remoteModeEnabled) {
            remoteInputController.moveCursor(inputConnection, right);
            return;
        }
        ImeConnectionDispatcher.moveCursor(
                inputConnection,
                right,
                (keyCode, metaState) -> sendSoftKey(inputConnection, keyCode, metaState));
    }

    private void updateComposing(InputConnection inputConnection) {
        if (!automata.getComposingText().isEmpty()) {
            pendingOwnComposingSelectionUpdates = Math.min(pendingOwnComposingSelectionUpdates + 2, 4);
        }
        InputConnectionTextOperator.updateComposing(inputConnection, automata, commitOnlyEditor);
    }

    private boolean isComposingSelectionMismatch(
            int oldSelStart,
            int oldSelEnd,
            int newSelStart,
            int newSelEnd,
            int candidatesStart,
            int candidatesEnd) {
        if (newSelStart != newSelEnd) {
            return true;
        }
        if (candidatesStart < 0 || candidatesEnd < 0) {
            return oldSelStart != newSelStart || oldSelEnd != newSelEnd;
        }
        return newSelStart < candidatesStart || newSelStart != candidatesEnd;
    }

    private void sendRawText(String text, InputConnection inputConnection) {
        ImeConnectionDispatcher.sendRawText(
                inputConnection,
                text,
                (keyCode, metaState) -> sendSoftKey(inputConnection, keyCode, metaState));
    }

    void commitCurrent(InputConnection inputConnection) {
        InputConnectionTextOperator.commitCurrent(inputConnection, automata, commitOnlyEditor);
        pendingOwnComposingSelectionUpdates = 0;
    }

    private void releaseCurrentCompositionForExternalCursorMove(InputConnection inputConnection) {
        InputConnectionTextOperator.finishComposing(inputConnection);
        automata.reset();
        commitOnlyEditor.reset();
        pendingOwnComposingSelectionUpdates = 0;
    }

    void commitReservedPhrase(InputConnection inputConnection, String command) {
        commitCurrent(inputConnection);
        doubleSpacePeriodState.reset();
        String phrase = KeyboardPreferences.loadReservedPhraseForCommand(this, command);
        if (phrase == null || phrase.isEmpty()) {
            return;
        }
        if (editorPolicy.rawKeyInput) {
            sendRawText(phrase, inputConnection);
        } else {
            InputConnectionTextOperator.commitText(inputConnection, phrase);
        }
    }

    void handleRemoteCommand(InputConnection inputConnection, String command) {
        commitCurrent(inputConnection);
        doubleSpacePeriodState.reset();

        remoteInputController.handleCommand(inputConnection, command);
    }

    private int sendSoftKey(InputConnection inputConnection, int keyCode, int metaState) {
        return ImeConnectionDispatcher.sendSoftKey(inputConnection, keyCode, metaState);
    }

    void showInputPicker() {
        InputMethodManager imm = getSystemService(InputMethodManager.class);
        if (imm != null) {
            imm.showInputMethodPicker();
        }
    }

    void openOptions(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        if (inputView != null) {
            inputView.cancelActiveGestureSession();
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void setHandedness(HandednessMode mode) {
        doubleSpacePeriodState.reset();
        settings = withSessionRuntimeState(settings.withHandednessPreset(mode));
        KeyboardPreferences.saveHandednessPreset(this, settings);
        applyCurrentSettingsToInputView();
    }

    void openInputSettings() {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void showTextActionPanel() {
        InputConnection inputConnection = commandInputConnection();
        commitCurrent(inputConnection);
        if (!textActionsAllowed()) {
            dismissTextActionPanel();
            Toast.makeText(this, R.string.text_action_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        if (inputRoot == null) {
            return;
        }
        dismissTextActionPanel();
        int padding = SettingsRowBuilder.dp(this, 12);
        LinearLayout panel = SettingsRowBuilder.vertical(this);
        panel.setPadding(padding, padding, padding, padding);
        panel.setBackgroundColor(SettingsUiPalette.from(this).surfaceRaised);
        panel.setClickable(true);
        panel.setFocusable(true);
        panel.setFocusableInTouchMode(true);
        SettingsRowBuilder.labelRow(this, panel, R.string.text_action_title, 0);
        SettingsRowBuilder.labelRow(this, panel, R.string.text_action_provider_privacy, 2);
        aiTextActionSettings = KeyboardPreferences.loadAiTextActionSettings(this);
        LinearLayout providerRow = SettingsRowBuilder.horizontal(this);
        android.widget.Button providerToggle = SettingsRowBuilder.weightedButton(
                this, providerRow, R.string.text_action_provider_disabled, 0, 2,
                view -> toggleTextActionProvider());
        android.widget.Button timeoutToggle = SettingsRowBuilder.weightedButton(
                this, providerRow, R.string.text_action_timeout_default, 2, 2,
                view -> cycleTextActionProviderTimeout());
        android.widget.Button targetToggle = SettingsRowBuilder.weightedButton(
                this, providerRow, R.string.text_action_translate_target_ko, 2, 0,
                view -> toggleTextActionTranslateTarget());
        providerToggle.setText(textActionProviderToggleLabel());
        timeoutToggle.setText(textActionProviderTimeoutLabel());
        targetToggle.setText(textActionTranslateTargetLabel());
        panel.addView(providerRow, SettingsRowBuilder.matchWrapWithTop(this, 4));

        LinearLayout primaryRow = SettingsRowBuilder.horizontal(this);
        android.widget.Button correct = SettingsRowBuilder.weightedButton(
                this, primaryRow, R.string.text_action_correct, 0, 3,
                view -> applyTextAction(TextAction.CORRECT));
        android.widget.Button restore = SettingsRowBuilder.weightedButton(
                this, primaryRow, R.string.text_action_restore, 3, 0,
                view -> restoreOriginalTextAction());
        restore.setEnabled(textActionTransaction != null);
        panel.addView(primaryRow, SettingsRowBuilder.matchWrapWithTop(this, 6));

        LinearLayout rewriteRow = SettingsRowBuilder.horizontal(this);
        android.widget.Button polish = SettingsRowBuilder.weightedButton(
                this, rewriteRow, R.string.text_action_polish, 0, 3,
                view -> applyTextAction(TextAction.POLISH));
        android.widget.Button shorter = SettingsRowBuilder.weightedButton(
                this, rewriteRow, R.string.text_action_shorter, 3, 0,
                view -> applyTextAction(TextAction.SHORTER));
        panel.addView(rewriteRow, SettingsRowBuilder.matchWrapWithTop(this, 4));

        LinearLayout toneRow = SettingsRowBuilder.horizontal(this);
        android.widget.Button polite = SettingsRowBuilder.weightedButton(
                this, toneRow, R.string.text_action_polite, 0, 3,
                view -> applyTextAction(TextAction.POLITE));
        android.widget.Button translate = SettingsRowBuilder.weightedButton(
                this, toneRow, R.string.text_action_translate, 3, 0,
                view -> applyTextAction(TextAction.TRANSLATE));
        panel.addView(toneRow, SettingsRowBuilder.matchWrapWithTop(this, 4));
        boolean providerReady = aiTextActionSettings.enabled && configuredTextActionProvider() != null;
        polish.setEnabled(providerReady);
        shorter.setEnabled(providerReady);
        polite.setEnabled(providerReady);
        translate.setEnabled(providerReady);

        showTextActionOverlay(panel, correct);
    }

    private boolean textActionsAllowed() {
        return editorPolicy.allowsTextActions(settings.remoteModeEnabled);
    }

    private void applyTextAction(TextAction action) {
        if (action == null || action == TextAction.RESTORE_ORIGINAL || !textActionsAllowed()) {
            return;
        }
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return;
        }
        TextActionRange range = currentTextActionRange(inputConnection);
        if (range == null) {
            Toast.makeText(this, R.string.text_action_no_target, Toast.LENGTH_SHORT).show();
            return;
        }
        aiTextActionSettings = KeyboardPreferences.loadAiTextActionSettings(this);
        if (action == TextAction.CORRECT && !aiTextActionSettings.enabled) {
            applyTextActionReplacement(inputConnection, range, TextActionEngine.correct(range.text));
            return;
        }
        startProviderTextAction(action, range);
    }

    private void applyTextActionReplacement(
            InputConnection inputConnection,
            TextActionRange range,
            String replacement) {
        TextActionTransaction transaction = TextActionTransaction.apply(
                new InputConnectionTextActionEditor(inputConnection),
                currentEditorPackageName,
                range,
                replacement);
        if (transaction == null) {
            Toast.makeText(this, R.string.text_action_no_change, Toast.LENGTH_SHORT).show();
            return;
        }
        textActionTransaction = transaction;
        clearPendingProviderTextAction();
        dismissTextActionPanel();
        doubleSpacePeriodState.reset();
        previousTextRepairState.reset();
        qwertyInputAssistant.reset();
    }

    private TextActionProvider configuredTextActionProvider() {
        if (!AiTextActionSettings.LOCAL_TEST_PROVIDER_ID.equals(aiTextActionSettings.providerId)) {
            return null;
        }
        return new LocalTestTextActionProvider(textActionTaskScheduler());
    }

    private TextActionTaskScheduler textActionTaskScheduler() {
        return (runnable, delayMs) -> {
            mainHandler.postDelayed(runnable, Math.max(0L, delayMs));
            return () -> mainHandler.removeCallbacks(runnable);
        };
    }

    private void startProviderTextAction(TextAction action, TextActionRange range) {
        TextActionProviderRequest.BuildResult built = TextActionProviderRequest.build(
                action,
                range,
                editorPolicy,
                settings.remoteModeEnabled,
                aiTextActionSettings.enabled,
                aiTextActionSettings.translateTargetLanguage);
        if (built.error != null) {
            showTextActionProviderError(built.error);
            return;
        }
        cancelActiveProviderTextAction(false);
        TextActionProvider provider = configuredTextActionProvider();
        pendingProviderTextAction = new PendingProviderTextAction(
                action,
                range,
                currentEditorPackageName,
                built.request);
        pendingProviderReplacement = null;
        long requestId = ++textActionProviderRequestId;
        showTextActionProviderLoading();
        TextActionProviderClient client = new TextActionProviderClient(
                provider,
                textActionTaskScheduler(),
                aiTextActionSettings.timeoutMs);
        activeTextActionProviderOperation = client.start(
                built.request,
                result -> mainHandler.post(() -> handleTextActionProviderResult(requestId, result)));
    }

    private void handleTextActionProviderResult(long requestId, TextActionProviderResult result) {
        if (requestId != textActionProviderRequestId) {
            return;
        }
        activeTextActionProviderOperation = null;
        if (result == null) {
            showTextActionProviderError(TextActionProviderError.MALFORMED_RESULT);
            return;
        }
        if (!result.succeeded()) {
            if (result.error == TextActionProviderError.CANCELLED) {
                clearPendingProviderTextAction();
                dismissTextActionPanel();
                return;
            }
            showTextActionProviderError(result.error);
            return;
        }
        pendingProviderReplacement = result.text;
        showTextActionProviderPreview();
    }

    private void showTextActionProviderLoading() {
        LinearLayout panel = newTextActionPanel();
        SettingsRowBuilder.labelRow(this, panel, R.string.text_action_provider_loading, 0);
        android.widget.Button cancel = SettingsRowBuilder.buttonRow(
                this,
                panel,
                R.string.text_action_cancel,
                6,
                view -> cancelProviderTextActionAndDismiss());
        showTextActionOverlay(panel, cancel);
    }

    private void showTextActionProviderPreview() {
        PendingProviderTextAction pending = pendingProviderTextAction;
        if (pending == null || pendingProviderReplacement == null) {
            showTextActionProviderError(TextActionProviderError.MALFORMED_RESULT);
            return;
        }
        LinearLayout panel = newTextActionPanel();
        SettingsRowBuilder.labelRow(this, panel, R.string.text_action_preview_title, 0);
        panel.addView(textActionBody(getString(
                R.string.text_action_preview_before,
                pending.range.text)), SettingsRowBuilder.matchWrapWithTop(this, 4));
        panel.addView(textActionBody(getString(
                R.string.text_action_preview_after,
                pendingProviderReplacement)), SettingsRowBuilder.matchWrapWithTop(this, 4));
        LinearLayout actions = SettingsRowBuilder.horizontal(this);
        android.widget.Button apply = SettingsRowBuilder.weightedButton(
                this, actions, R.string.text_action_apply, 0, 3,
                view -> applyProviderTextActionPreview());
        SettingsRowBuilder.weightedButton(
                this, actions, R.string.text_action_cancel, 3, 0,
                view -> cancelProviderTextActionAndDismiss());
        panel.addView(actions, SettingsRowBuilder.matchWrapWithTop(this, 6));
        showTextActionOverlay(panel, apply);
    }

    private void showTextActionProviderError(TextActionProviderError error) {
        LinearLayout panel = newTextActionPanel();
        SettingsRowBuilder.labelRow(this, panel, R.string.text_action_provider_error_title, 0);
        panel.addView(textActionBody(textActionProviderErrorLabel(error)),
                SettingsRowBuilder.matchWrapWithTop(this, 4));
        LinearLayout actions = SettingsRowBuilder.horizontal(this);
        android.widget.Button retry = SettingsRowBuilder.weightedButton(
                this, actions, R.string.text_action_retry, 0, 3,
                view -> retryProviderTextAction());
        SettingsRowBuilder.weightedButton(
                this, actions, R.string.text_action_cancel, 3, 0,
                view -> cancelProviderTextActionAndDismiss());
        retry.setEnabled(pendingProviderTextAction != null);
        panel.addView(actions, SettingsRowBuilder.matchWrapWithTop(this, 6));
        showTextActionOverlay(panel, retry);
    }

    private void retryProviderTextAction() {
        PendingProviderTextAction pending = pendingProviderTextAction;
        if (pending == null || !textActionsAllowed()) {
            cancelProviderTextActionAndDismiss();
            return;
        }
        cancelActiveProviderTextAction(false);
        pendingProviderReplacement = null;
        long requestId = ++textActionProviderRequestId;
        showTextActionProviderLoading();
        TextActionProviderClient client = new TextActionProviderClient(
                configuredTextActionProvider(),
                textActionTaskScheduler(),
                aiTextActionSettings.timeoutMs);
        activeTextActionProviderOperation = client.start(
                pending.request,
                result -> mainHandler.post(() -> handleTextActionProviderResult(requestId, result)));
    }

    private void applyProviderTextActionPreview() {
        PendingProviderTextAction pending = pendingProviderTextAction;
        String replacement = pendingProviderReplacement;
        InputConnection inputConnection = getCurrentInputConnection();
        if (pending == null || replacement == null || inputConnection == null) {
            showTextActionProviderError(TextActionProviderError.STALE_EDITOR);
            return;
        }
        TextActionRange activeRange = currentTextActionRange(inputConnection);
        if (!pending.targets(currentEditorPackageName, activeRange)) {
            showTextActionProviderError(TextActionProviderError.STALE_EDITOR);
            return;
        }
        applyTextActionReplacement(inputConnection, pending.range, replacement);
    }

    private void cancelProviderTextActionAndDismiss() {
        cancelActiveProviderTextAction(true);
        dismissTextActionPanel();
    }

    private void cancelActiveProviderTextAction(boolean clearPending) {
        TextActionProviderClient.Operation operation = activeTextActionProviderOperation;
        activeTextActionProviderOperation = null;
        if (clearPending) {
            textActionProviderRequestId++;
        }
        if (operation != null && !operation.isDone()) {
            operation.cancel();
        }
        if (clearPending) {
            clearPendingProviderTextAction();
        }
    }

    private void clearPendingProviderTextAction() {
        pendingProviderTextAction = null;
        pendingProviderReplacement = null;
    }

    private LinearLayout newTextActionPanel() {
        int padding = SettingsRowBuilder.dp(this, 12);
        LinearLayout panel = SettingsRowBuilder.vertical(this);
        panel.setPadding(padding, padding, padding, padding);
        panel.setBackgroundColor(SettingsUiPalette.from(this).surfaceRaised);
        panel.setClickable(true);
        panel.setFocusable(true);
        panel.setFocusableInTouchMode(true);
        return panel;
    }

    private android.widget.TextView textActionBody(String text) {
        android.widget.TextView view = new android.widget.TextView(this);
        SettingsUiPalette palette = SettingsUiPalette.from(this);
        view.setText(text == null ? "" : text);
        view.setTextColor(palette.textPrimary);
        view.setTextSize(14f);
        view.setTextIsSelectable(false);
        int padding = SettingsRowBuilder.dp(this, 8);
        view.setPadding(padding, padding, padding, padding);
        view.setBackgroundColor(palette.surface);
        return view;
    }

    private void showTextActionOverlay(LinearLayout panel, View focusView) {
        if (inputRoot == null || panel == null) {
            return;
        }
        dismissTextActionPanel();
        int margin = SettingsRowBuilder.dp(this, 12);
        FrameLayout overlay = new FrameLayout(this);
        overlay.setClickable(true);
        overlay.setFocusable(true);
        overlay.setOnClickListener(view -> cancelProviderTextActionAndDismiss());
        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM);
        panelParams.setMargins(margin, margin, margin, margin);
        overlay.addView(panel, panelParams);
        textActionOverlayView = overlay;
        inputRoot.addView(overlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        overlay.bringToFront();
        if (focusView != null) {
            focusView.requestFocus();
        }
        requestOverlayTouchableRegionUpdate();
    }

    private void toggleTextActionProvider() {
        aiTextActionSettings = aiTextActionSettings.withEnabled(!aiTextActionSettings.enabled);
        KeyboardPreferences.saveAiTextActionSettings(this, aiTextActionSettings);
        cancelActiveProviderTextAction(true);
        dismissTextActionPanel();
        Toast.makeText(this, textActionProviderToggleLabel(), Toast.LENGTH_SHORT).show();
    }

    private void cycleTextActionProviderTimeout() {
        int current = aiTextActionSettings.timeoutMs;
        int next = current < 5000 ? 5000 : (current < 10000 ? 10000 : 3000);
        aiTextActionSettings = aiTextActionSettings.withTimeoutMs(next);
        KeyboardPreferences.saveAiTextActionSettings(this, aiTextActionSettings);
        dismissTextActionPanel();
        Toast.makeText(this, textActionProviderTimeoutLabel(), Toast.LENGTH_SHORT).show();
    }

    private void toggleTextActionTranslateTarget() {
        String next = "ko".equals(aiTextActionSettings.translateTargetLanguage) ? "en" : "ko";
        aiTextActionSettings = aiTextActionSettings.withTranslateTarget(next);
        KeyboardPreferences.saveAiTextActionSettings(this, aiTextActionSettings);
        dismissTextActionPanel();
        Toast.makeText(this, textActionTranslateTargetLabel(), Toast.LENGTH_SHORT).show();
    }

    private String textActionProviderToggleLabel() {
        return getString(aiTextActionSettings.enabled
                ? R.string.text_action_provider_enabled
                : R.string.text_action_provider_disabled);
    }

    private String textActionProviderTimeoutLabel() {
        return getString(R.string.text_action_timeout_format, aiTextActionSettings.timeoutMs / 1000);
    }

    private String textActionTranslateTargetLabel() {
        return getString("en".equals(aiTextActionSettings.translateTargetLanguage)
                ? R.string.text_action_translate_target_en
                : R.string.text_action_translate_target_ko);
    }

    private String textActionProviderErrorLabel(TextActionProviderError error) {
        TextActionProviderError safe = error == null ? TextActionProviderError.FAILED : error;
        switch (safe) {
            case DISABLED:
                return getString(R.string.text_action_error_disabled);
            case DENIED:
                return getString(R.string.text_action_error_denied);
            case UNAVAILABLE:
                return getString(R.string.text_action_error_unavailable);
            case TIMEOUT:
                return getString(R.string.text_action_error_timeout);
            case CANCELLED:
                return getString(R.string.text_action_error_cancelled);
            case EMPTY_RESULT:
                return getString(R.string.text_action_error_empty);
            case MALFORMED_RESULT:
                return getString(R.string.text_action_error_malformed);
            case STALE_EDITOR:
                return getString(R.string.text_action_error_stale);
            case TOO_LARGE:
                return getString(R.string.text_action_error_too_large);
            default:
                return getString(R.string.text_action_error_failed);
        }
    }

    private void restoreOriginalTextAction() {
        InputConnection inputConnection = getCurrentInputConnection();
        TextActionTransaction transaction = textActionTransaction;
        if (inputConnection != null && transaction != null
                && transaction.restore(new InputConnectionTextActionEditor(inputConnection), currentEditorPackageName)) {
            textActionTransaction = null;
            dismissTextActionPanel();
        }
    }

    private TextActionRange currentTextActionRange(InputConnection inputConnection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SurroundingText surrounding = inputConnection.getSurroundingText(512, 512, 0);
            if (surrounding == null || surrounding.getText() == null) {
                return null;
            }
            int offset = surrounding.getOffset();
            return TextActionRange.resolve(surrounding.getText().toString(), offset,
                    offset + surrounding.getSelectionStart(), offset + surrounding.getSelectionEnd());
        }
        ExtractedTextRequest request = new ExtractedTextRequest();
        request.hintMaxChars = 1024;
        request.hintMaxLines = 4;
        ExtractedText extracted = inputConnection.getExtractedText(request, 0);
        if (extracted == null || extracted.text == null || extracted.text.length() > 2048) {
            return null;
        }
        return TextActionRange.resolve(extracted.text.toString(), extracted.startOffset,
                extracted.startOffset + extracted.selectionStart,
                extracted.startOffset + extracted.selectionEnd);
    }

    private void dismissTextActionPanel() {
        if (inputRoot != null && textActionOverlayView != null) {
            inputRoot.removeView(textActionOverlayView);
            requestOverlayTouchableRegionUpdate();
        }
        textActionOverlayView = null;
    }

    private static final class InputConnectionTextActionEditor implements TextActionTransaction.Editor {
        private final InputConnection inputConnection;

        InputConnectionTextActionEditor(InputConnection inputConnection) {
            this.inputConnection = inputConnection;
        }

        @Override
        public boolean setSelection(int start, int end) {
            return inputConnection.setSelection(start, end);
        }

        @Override
        public boolean commitText(String text) {
            return InputConnectionTextOperator.commitText(inputConnection, text);
        }
    }

    void showQuickSettings() {
        if (inputRoot == null) {
            return;
        }
        if (inputView != null) {
            inputView.cancelActiveGestureSession();
        }
        if (quickSettingsPopup != null && quickSettingsPopup.isShowing()) {
            dismissQuickSettings();
            return;
        }
        View panel = quickSettingsPanelController.createPanel();
        int popupWidth = Math.max(
                1,
                getResources().getDisplayMetrics().widthPixels - SettingsRowBuilder.dp(this, 24));
        panel.measure(
                View.MeasureSpec.makeMeasureSpec(popupWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int maximumHeight = Math.max(
                1,
                getResources().getDisplayMetrics().heightPixels - SettingsRowBuilder.dp(this, 48));
        int popupHeight = Math.max(1, Math.min(panel.getMeasuredHeight(), maximumHeight));
        ScrollView panelScroll = new ScrollView(this);
        panelScroll.setFillViewport(false);
        panelScroll.setVerticalScrollBarEnabled(panel.getMeasuredHeight() > maximumHeight);
        panelScroll.addView(panel, SettingsRowBuilder.frameMatchWrap());
        quickSettingsPopup = new PopupWindow(
                panelScroll,
                popupWidth,
                popupHeight,
                true);
        quickSettingsPopup.setOutsideTouchable(true);
        quickSettingsPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        quickSettingsPopup.setClippingEnabled(false);
        quickSettingsPopup.showAtLocation(
                inputRoot,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0,
                SettingsRowBuilder.dp(this, 12));
    }

    private int sendCompatibilityKey(int keyCode, int metaState) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return RemoteCompatibilityPanelController.SEND_SKIPPED;
        }
        commitCurrent(inputConnection);
        return remoteInputController.sendCompatibilityKey(inputConnection, keyCode, metaState);
    }

    private void dismissQuickSettings() {
        if (quickSettingsPopup != null) {
            quickSettingsPopup.dismiss();
        }
        quickSettingsPopup = null;
    }

    private boolean activeNumberRowVisible() {
        return settings.showNumberRow;
    }

    private String numberRowToggleLabel() {
        if (editorPolicy.replacesMainRows()) {
            return getString(
                    R.string.field_layout_label,
                    editorPolicy.surface.name().toLowerCase(Locale.ROOT));
        }
        String layout = SettingsDisplayLabels.label(this, activeLayoutProfile());
        return getString(
                R.string.number_row_toggle_label,
                layout,
                getString(activeNumberRowVisible() ? R.string.state_on : R.string.state_off));
    }

    private KeyboardLayoutProfile activeLayoutProfile() {
        if (settings.remoteModeEnabled) {
            return KeyboardLayoutProfile.QWERTY;
        }
        return layoutProfiles.activeFor(settings.keyboardMode);
    }

    private String remoteModeToggleLabel() {
        if (remoteModeAutoActivated && settings.remoteModeEnabled) {
            return getString(R.string.remote_mode_auto);
        }
        return getString(
                R.string.remote_mode_label,
                getString(settings.remoteModeEnabled ? R.string.state_on : R.string.state_off));
    }

    private boolean singleTapCommitModeEnabled() {
        return KeyboardPreferences.loadSingleTapCommitModeEnabled(this);
    }

    private String singleTapCommitModeToggleLabel() {
        return getString(
                R.string.one_finger_quick_toggle_format,
                getString(singleTapCommitModeEnabled() ? R.string.state_on : R.string.state_off));
    }

    private boolean watchRadialInputEnabled() {
        return KeyboardPreferences.loadWatchRadialInputEnabled(this);
    }

    private String watchRadialInputToggleLabel() {
        return getString(
                R.string.watch_radial_quick_toggle_format,
                getString(watchRadialInputEnabled() ? R.string.state_on : R.string.state_off));
    }

    private void toggleRemoteMode() {
        settings = withSessionRuntimeState(settings.withRemoteOptions(
                !settings.remoteModeEnabled,
                settings.remoteKeyPreset,
                settings.remoteImeShortcut));
        remoteModeAutoActivated = false;
        remoteInputController.reset();
        if (remoteCompatibilityPanelController != null) {
            remoteCompatibilityPanelController.reset();
        }
        KeyboardPreferences.saveRemoteOptions(
                this,
                settings.remoteModeEnabled,
                settings.remoteKeyPreset,
                settings.remoteImeShortcut);
        applyCurrentSettingsToInputView();
        updateToolbarVisibility();
        dismissQuickSettings();
    }

    private void toggleSingleTapCommitMode() {
        KeyboardPreferences.saveSingleTapCommitModeEnabled(this, !singleTapCommitModeEnabled());
        if (inputView != null) {
            inputView.setSettings(settings);
        }
        updateRuntimeStateIndicator();
        dismissQuickSettings();
    }

    private void toggleWatchRadialInput() {
        watchRadialInputEnabled = !watchRadialInputEnabled();
        KeyboardPreferences.saveWatchRadialInputEnabled(this, watchRadialInputEnabled);
        updateFullscreenMode();
        updateTransparentOverlayPresentation();
        applyCurrentSettingsToInputView();
        dismissQuickSettings();
    }

    private void toggleActiveNumberRow() {
        if (editorPolicy.replacesMainRows()) {
            Toast.makeText(this, numberRowToggleLabel(), Toast.LENGTH_SHORT).show();
            dismissQuickSettings();
            return;
        }
        if (settings.remoteModeEnabled) {
            Toast.makeText(this, R.string.remote_number_row_forced, Toast.LENGTH_SHORT).show();
            dismissQuickSettings();
            return;
        }
        settings = withSessionRuntimeState(settings.keyboardMode == KeyboardMode.ENGLISH
                ? settings.withEnglishNumberRow(!settings.showEnglishNumberRow)
                : settings.withHangulNumberRow(!settings.showHangulNumberRow));
        KeyboardPreferences.saveNumberRowVisibility(
                this,
                settings.showHangulNumberRow,
                settings.showEnglishNumberRow);
        applyCurrentSettingsToInputView();
        Toast.makeText(this, numberRowToggleLabel(), Toast.LENGTH_SHORT).show();
        dismissQuickSettings();
    }

    private void applyCurrentSettingsToInputView() {
        if (inputView != null) {
            inputView.setKeyboardSurface(editorPolicy.surface);
            inputView.setLayoutProfiles(layoutProfiles);
            inputView.setSettings(settings);
            inputView.setTransparentOverlayPresentation(
                    transparentOverlayInputEnabled || watchRadialInputEnabled);
            inputView.setTransparentOverlayStyle(transparentOverlayStyle);
            updateShiftStateView();
        }
        applyImeWindowBlur();
        if (watchRadialInputView != null) {
            watchRadialInputView.setSettings(settings);
        }
        updateInputSurfaceVisibility();
        if (previewOverlayController != null) {
            previewOverlayController.setSettings(settings);
        }
        updateRuntimeStateIndicator();
    }

    private void updateTransparentOverlayPresentation() {
        transparentOverlayInputEnabled =
                KeyboardPreferences.loadTransparentOverlayInputEnabled(this);
        transparentOverlayStyle = KeyboardPreferences.loadTransparentOverlayStyle(this);
        watchRadialInputEnabled = KeyboardPreferences.loadWatchRadialInputEnabled(this);
        aiTextActionSettings = KeyboardPreferences.loadAiTextActionSettings(this);
        if (transparentOverlayStyle == TransparentOverlayStyle.EXTREME_FLOATING) {
            dismissPreviewPopup();
        }
        if (transparentOverlayInputEnabled || watchRadialInputEnabled) {
            setExtractViewShown(false);
        }
        if (inputRoot != null) {
            inputRoot.setBackgroundColor(Color.TRANSPARENT);
        }
        if (inputView != null) {
            inputView.setTransparentOverlayPresentation(
                    transparentOverlayInputEnabled || watchRadialInputEnabled);
            inputView.setTransparentOverlayStyle(transparentOverlayStyle);
        }
        updateInputSurfaceVisibility();
    }


    private boolean watchRadialInputActive() {
        return WatchRadialInputPolicy.isActive(
                watchRadialInputEnabled,
                settings,
                layoutProfiles,
                editorPolicy.surface);
    }

    private void updateInputSurfaceVisibility() {
        boolean watchActive = watchRadialInputActive();
        if (inputView != null) {
            inputView.setVisibility(watchActive ? View.GONE : View.VISIBLE);
        }
        if (watchRadialInputView != null) {
            watchRadialInputView.setVisibility(watchActive ? View.VISIBLE : View.GONE);
            if (watchActive) {
                watchRadialInputView.setSettings(settings);
                if (!watchRadialInputVisible) {
                    watchRadialInputView.resetSession();
                }
            }
        }
        watchRadialInputVisible = watchActive;
        if (watchActive) {
            dismissPreviewPopup();
        }
    }

    private WatchRadialPage preferredWatchRadialPage() {
        return automata.prefersVowelInput()
                ? WatchRadialPage.VOWELS
                : WatchRadialPage.CONSONANTS;
    }

    private void showPreviewOverlays(List<PreviewOverlaySpec> specs) {
        if (previewOverlayController == null) {
            return;
        }
        if (watchRadialInputActive()
                || transparentOverlayStyle == TransparentOverlayStyle.EXTREME_FLOATING) {
            previewOverlayController.dismiss();
            return;
        }
        previewOverlayController.show(inputView, specs);
    }

    private void dismissPreviewPopup() {
        if (previewOverlayController != null) {
            previewOverlayController.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        cancelActiveProviderTextAction(true);
        mainHandler.removeCallbacksAndMessages(null);
        pendingVoiceInput = null;
        dismissPreviewPopup();
        dismissQuickSettings();
        dismissTextActionPanel();
        removeClipboardListener();
        if (inputView != null) {
            inputView.flushLearningState();
        }
        super.onDestroy();
    }

}
