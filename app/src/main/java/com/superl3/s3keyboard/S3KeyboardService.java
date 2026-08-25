package com.superl3.s3keyboard;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.InputMethodService;
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
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
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
    private PreviewOverlayController previewOverlayController;
    private EnglishSuggestionStripController suggestionStripController;
    private RemoteCompatibilityPanelController remoteCompatibilityPanelController;
    private InputIssueReportClipboardController inputIssueReportController;
    private QuickThemePanelController quickThemePanelController;
    private QuickSettingsPanelController quickSettingsPanelController;
    private ClipboardPanelController clipboardPanelController;
    private ThemeClipboardImportController themeClipboardImportController;
    private PopupWindow quickSettingsPopup;
    private final RemoteInputController remoteInputController = new RemoteInputController(
            () -> settings.remoteImeShortcut,
            (pendingMetaState, lockedMetaState) -> updateShiftStateView());
    private boolean remoteModeAutoActivated;
    private boolean englishSuggestionsEnabled = true;
    private boolean englishAutoCorrectionEnabled = true;
    private boolean transparentOverlayInputEnabled;
    private boolean watchRadialInputEnabled;
    private boolean watchRadialInputVisible;
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

        clipboardPanelController = new ClipboardPanelController(
                this,
                () -> settings,
                () -> editorPolicy,
                this::commitClipboardText);

        inputRoot = new FrameLayout(this);
        inputRoot.setBackgroundColor(Color.TRANSPARENT);
        inputRoot.setClipChildren(false);
        inputRoot.setClipToPadding(false);

        LinearLayout mainContainer = SettingsRowBuilder.vertical(this);

        inputView = new HangulKeyboardView(this);
        inputView.setKeyboardSurface(editorPolicy.surface);
        inputView.setSettings(settings);
        inputView.setTransparentOverlayPresentation(transparentOverlayInputEnabled);
        inputView.setTransparentOverlayStyle(transparentOverlayStyle);
        inputView.setRedactTypingEventText(!editorPolicy.allowTextConveniences);
        updateShiftStateView();
        inputView.setOnKeyGestureListener(this::onKeyGesture);
        inputView.setOnPreviewOverlayListener(this::showPreviewOverlays);

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

        suggestionStripController = new EnglishSuggestionStripController(
                this,
                this::acceptEnglishSuggestion);

        mainContainer.addView(clipboardPanelController.createToolbar(), SettingsRowBuilder.matchWrap());
        mainContainer.addView(suggestionStripController.createView(), SettingsRowBuilder.matchWrap());
        mainContainer.addView(keyboardSurfaceContainer, SettingsRowBuilder.matchWrap());

        inputRoot.addView(mainContainer, SettingsRowBuilder.frameMatchWrap());

        inputRoot.addView(
                clipboardPanelController.createOverlayView(),
                clipboardPanelController.overlayLayoutParams());

        initializePanelControllers();

        updateInputSurfaceVisibility();
        updateToolbarVisibility();
        updateClipboardListener();
        applyImeWindowBlur();
        updateGlassBackdropCaptureState();
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
            // Fullscreen already prevents legacy adjustResize, but the default visible inset still
            // starts at the input frame. Some clients use that value to add their own IME padding.
            // Report no occupied content or visible area while keeping the full frame touchable.
            View decorView = getWindow().getWindow().getDecorView();
            int windowHeight = decorView.getHeight();
            outInsets.contentTopInsets = windowHeight;
            outInsets.visibleTopInsets = windowHeight;
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_FRAME;
        }
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
        quickSettingsPanelController = new QuickSettingsPanelController(
                this,
                remoteCompatibilityPanelController,
                quickThemePanelController,
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
                this::importThemeFromClipboard,
                this::copyInputIssueReport,
                this::dismissQuickSettings);
    }

    private void commitClipboardText(String text) {
        if (editorPolicy.password || text == null || text.isEmpty()) {
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
        updateSuggestionStrip();
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

    private void updateToolbarVisibility() {
        if (clipboardPanelController != null) {
            clipboardPanelController.updateVisibility();
        }
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

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        updateTransparentOverlayPresentation();
        loadSettingsForEditor(info);
        if (inputView != null) {
            inputView.setRedactTypingEventText(!editorPolicy.allowTextConveniences);
            applyCurrentSettingsToInputView();
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
        previousTextRepairState.reset();
        doubleSpacePeriodState.reset();
        englishShiftState.reset();
        qwertyInputAssistant.reset();
        remoteInputController.reset();
        if (watchRadialInputView != null) {
            watchRadialInputView.resetSession();
        }
        if (inputView != null) {
            inputView.setRedactTypingEventText(!editorPolicy.allowTextConveniences);
            applyCurrentSettingsToInputView();
        }
        updateShiftStateView();
        updateSuggestionStrip();
        mainHandler.post(pendingVoiceCommitRunnable);
    }

    @Override
    public void onFinishInput() {
        GlassBackdropSourceStore.setConsumerActive(false);
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
        updateSuggestionStrip();
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
            refreshQwertyAssistantFromEditor();
            return;
        }
        if (automata.getComposingText().isEmpty()) {
            refreshQwertyAssistantFromEditor();
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
            refreshQwertyAssistantFromEditor();
        }
    }

    public void onKeyGesture(String value) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null || value == null || value.isEmpty()) {
            return;
        }
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

    private boolean qwertyAssistanceActive() {
        return settings.keyboardMode == KeyboardMode.ENGLISH
                && (englishSuggestionsEnabled || englishAutoCorrectionEnabled)
                && !settings.remoteModeEnabled
                && layoutProfiles.activeIsQwerty(KeyboardMode.ENGLISH)
                && editorPolicy.allowTextConveniences
                && !editorPolicy.rawKeyInput
                && !editorPolicy.replacesMainRows();
    }

    private boolean qwertyAssistanceActive(InputConnection inputConnection) {
        return qwertyAssistanceActive()
                && !InputConnectionTextOperator.hasSelection(inputConnection);
    }

    private boolean qwertySuggestionsActive() {
        return englishSuggestionsEnabled && qwertyAssistanceActive();
    }

    private boolean qwertyAutoCorrectionActive(InputConnection inputConnection) {
        return englishAutoCorrectionEnabled && qwertyAssistanceActive(inputConnection);
    }

    private void refreshQwertyAssistantFromEditor() {
        InputConnection inputConnection = getCurrentInputConnection();
        if (qwertyAssistanceActive(inputConnection)) {
            qwertyInputAssistant.refreshFromEditor(inputConnection);
        } else {
            qwertyInputAssistant.reset();
        }
        updateSuggestionStrip();
    }

    private void updateSuggestionStrip() {
        if (suggestionStripController != null) {
            suggestionStripController.update(
                    settings,
                    qwertySuggestionsActive(),
                    qwertyInputAssistant.suggestions());
        }
    }

    private void acceptEnglishSuggestion(String suggestion) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null
                || !qwertySuggestionsActive()
                || InputConnectionTextOperator.hasSelection(inputConnection)) {
            return;
        }
        qwertyInputAssistant.replaceCurrentWord(inputConnection, suggestion);
        updateSuggestionStrip();
    }

    void toggleClipboardPanel() {
        if (clipboardPanelController != null) {
            clipboardPanelController.toggle();
        }
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
            updateSuggestionStrip();
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
            updateSuggestionStrip();
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
        boolean assistanceActive = qwertyAssistanceActive(inputConnection);
        if (qwertyAutoCorrectionActive(inputConnection)
                && !qwertyInputAssistant.continuesCurrentWord(committedText)) {
            qwertyInputAssistant.autoCorrectCurrentWord(inputConnection);
        }
        InputConnectionTextOperator.commitText(inputConnection, committedText);
        if (assistanceActive) {
            qwertyInputAssistant.recordCommittedText(committedText);
        } else {
            qwertyInputAssistant.reset();
        }
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
        englishSuggestionsEnabled = KeyboardPreferences.loadEnglishSuggestionsEnabled(this);
        englishAutoCorrectionEnabled = KeyboardPreferences.loadEnglishAutoCorrectionEnabled(this);
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
    }

    private KeyboardSettings withSessionRuntimeState(KeyboardSettings source) {
        KeyboardSettings safe = RuntimeDefaults.keyboardSettings(source);
        return safe
                .withEnterKeyLabel(enterActionLabel())
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
    }

    void commitSpace(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        if (qwertyAutoCorrectionActive(inputConnection)) {
            qwertyInputAssistant.autoCorrectCurrentWord(inputConnection);
        }
        if (settings.remoteModeEnabled) {
            doubleSpacePeriodState.reset();
            remoteInputController.sendKey(inputConnection, KeyEvent.KEYCODE_SPACE, 0);
            qwertyInputAssistant.reset();
            updateSuggestionStrip();
            return;
        }
        if (editorPolicy.rawKeyInput) {
            doubleSpacePeriodState.reset();
            sendSoftKey(inputConnection, KeyEvent.KEYCODE_SPACE, 0);
            qwertyInputAssistant.reset();
            updateSuggestionStrip();
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
        updateSuggestionStrip();
    }

    void performEnter(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        if (qwertyAutoCorrectionActive(inputConnection)) {
            qwertyInputAssistant.autoCorrectCurrentWord(inputConnection);
        }
        ImeConnectionDispatcher.performEnter(
                inputConnection,
                enterAction,
                editorPolicy.rawKeyInput,
                settings.remoteModeEnabled,
                (keyCode, metaState) -> sendSoftKey(inputConnection, keyCode, metaState),
                (keyCode, metaState) -> remoteInputController.sendKey(inputConnection, keyCode, metaState));
        qwertyInputAssistant.reset();
        updateSuggestionStrip();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        GlassBackdropSourceStore.setConsumerActive(false);
        super.onFinishInputView(finishingInput);
    }

    void commitExplicitNewline(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        if (qwertyAutoCorrectionActive(inputConnection)) {
            qwertyInputAssistant.autoCorrectCurrentWord(inputConnection);
        }
        ImeConnectionDispatcher.commitExplicitNewline(inputConnection);
        qwertyInputAssistant.reset();
        updateSuggestionStrip();
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
            refreshQwertyAssistantFromEditor();
            return;
        }
        if (editorPolicy.rawKeyInput) {
            automata.reset();
            commitOnlyEditor.reset();
            InputConnectionTextOperator.finishComposing(inputConnection);
            sendSoftKey(inputConnection, KeyEvent.KEYCODE_DEL, 0);
            refreshQwertyAssistantFromEditor();
            return;
        }
        if (usesCommitOnlyHangul() && commitOnlyEditor.backspace(
                automata,
                InputConnectionTextOperator.commitOnlySink(inputConnection))) {
            refreshQwertyAssistantFromEditor();
            return;
        }
        if (editorPolicy.allowComposingText && automata.backspace()) {
            updateComposing(inputConnection);
        } else {
            deleteCommittedText(inputConnection);
        }
        refreshQwertyAssistantFromEditor();
    }

    void deleteWord(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        previousTextRepairState.markDelete();
        doubleSpacePeriodState.reset();
        if (settings.remoteModeEnabled) {
            remoteInputController.sendKey(inputConnection, KeyEvent.KEYCODE_DEL, KeyEvent.META_CTRL_ON);
            refreshQwertyAssistantFromEditor();
            return;
        }
        if (editorPolicy.rawKeyInput) {
            sendSoftKey(inputConnection, KeyEvent.KEYCODE_DEL, KeyEvent.META_CTRL_ON);
            refreshQwertyAssistantFromEditor();
            return;
        }
        if (InputConnectionTextOperator.deleteSelectedText(inputConnection)) {
            InputConnectionTextOperator.finishComposing(inputConnection);
            refreshQwertyAssistantFromEditor();
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
        refreshQwertyAssistantFromEditor();
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
            refreshQwertyAssistantFromEditor();
            return;
        }
        ImeConnectionDispatcher.moveCursor(
                inputConnection,
                right,
                (keyCode, metaState) -> sendSoftKey(inputConnection, keyCode, metaState));
        refreshQwertyAssistantFromEditor();
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
        if (clipboardPanelController != null) {
            clipboardPanelController.updateAppearance();
        }
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
        updateGlassBackdropCaptureState();
        if (watchRadialInputView != null) {
            watchRadialInputView.setSettings(settings);
        }
        updateInputSurfaceVisibility();
        if (previewOverlayController != null) {
            previewOverlayController.setSettings(settings);
        }
        updateSuggestionStrip();
    }

    private void updateTransparentOverlayPresentation() {
        transparentOverlayInputEnabled =
                KeyboardPreferences.loadTransparentOverlayInputEnabled(this);
        transparentOverlayStyle = KeyboardPreferences.loadTransparentOverlayStyle(this);
        watchRadialInputEnabled = KeyboardPreferences.loadWatchRadialInputEnabled(this);
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
        updateGlassBackdropCaptureState();
    }

    private void updateGlassBackdropCaptureState() {
        boolean active = inputView != null
                && settings != null
                && settings.visualEffects.glassEnabled
                && GlassBackdropPreferences.isSourceEnabled(this)
                && !editorPolicy.password
                && !transparentOverlayInputEnabled
                && !watchRadialInputEnabled;
        GlassBackdropSourceStore.setConsumerActive(active);
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
        GlassBackdropSourceStore.setConsumerActive(false);
        mainHandler.removeCallbacksAndMessages(null);
        pendingVoiceInput = null;
        dismissPreviewPopup();
        dismissQuickSettings();
        removeClipboardListener();
        if (inputView != null) {
            inputView.flushLearningState();
        }
        super.onDestroy();
    }

}
