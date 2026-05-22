package com.superl3.s3keyboard;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public final class S3KeyboardService extends InputMethodService
        implements HangulKeyboardView.OnKeyGestureListener, HangulKeyboardView.OnPreviewOverlayListener {
    private static final int PREVIEW_POPUP_TOP_RESERVE_DP = 112;

    private final HangulAutomata automata = new HangulAutomata();
    private final DoubleSpacePeriodState doubleSpacePeriodState = new DoubleSpacePeriodState();
    private final EnglishShiftState englishShiftState = new EnglishShiftState();
    private KeyboardSettings settings = KeyboardSettings.defaults();
    private ResolvedImeAction enterAction = ImeActionLabelResolver.defaultAction();
    private EditorInputPolicy editorPolicy = EditorInputPolicy.DEFAULT;
    private HangulKeyboardView inputView;
    private FrameLayout inputRoot;
    private FrameLayout previewOverlayContainer;
    private TextView previewOverlay;
    private final List<TextView> previewOverlayPool = new ArrayList<>();
    private TextView remoteIndicator;
    private PopupWindow previewPopup;
    private PopupWindow quickSettingsPopup;
    private int previewPopupTopPadPx;
    private int pendingRemoteMetaState;
    private int lockedRemoteMetaState;

    private FloatingModeController floatingModeController;
    private ClipboardStore clipboardStore;
    private LinearLayout toolbarLayout;
    private View dragHandle;
    private Button clipboardBtn;
    private ClipboardView clipboardView;
    private ClipboardManager clipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener clipboardListener;
    private boolean clipboardListenerRegistered;

    @Override
    public View onCreateInputView() {
        dismissPreviewPopup();
        settings = KeyboardPreferences.load(this).withEnterKeyLabel(enterAction.label);

        floatingModeController = new FloatingModeController(this);
        floatingModeController.setEnabled(false);
        clipboardStore = new ClipboardStore(this);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardListener = this::capturePrimaryClipboard;

        inputRoot = new FrameLayout(this);
        inputRoot.setClipChildren(false);
        inputRoot.setClipToPadding(false);

        LinearLayout mainContainer = new LinearLayout(this);
        mainContainer.setOrientation(LinearLayout.VERTICAL);

        toolbarLayout = new LinearLayout(this);
        toolbarLayout.setOrientation(LinearLayout.HORIZONTAL);
        toolbarLayout.setGravity(Gravity.CENTER_VERTICAL);
        toolbarLayout.setBackgroundColor(settings.keyboardBackgroundColor);

        // Drag Handle
        dragHandle = new View(this);
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        handleParams.weight = 0;
        handleParams.gravity = Gravity.CENTER;
        handleParams.setMargins(0, 8, 0, 8);
        dragHandle.setLayoutParams(handleParams);
        dragHandle.setBackgroundColor(Color.LTGRAY);
        dragHandle.setOnTouchListener((v, event) -> floatingModeController.onHandleTouch(v, event));

        // Spacer to push clipboard button to right
        View spacer1 = new View(this);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));
        View spacer2 = new View(this);
        spacer2.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 1));

        // Clipboard Button
        clipboardBtn = new Button(this);
        clipboardBtn.setText("📋");
        clipboardBtn.setText("Clip");
        clipboardBtn.setBackgroundColor(Color.TRANSPARENT);
        clipboardBtn.setTextColor(settings.keyIdleColor);
        clipboardBtn.setOnClickListener(v -> toggleClipboard());
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.weight = 0;
        clipboardBtn.setLayoutParams(btnParams);

        remoteIndicator = new TextView(this);
        remoteIndicator.setText("");
        remoteIndicator.setTextSize(12);
        remoteIndicator.setTypeface(Typeface.DEFAULT_BOLD);
        remoteIndicator.setTextColor(contrastColor(settings.keyboardBackgroundColor));
        remoteIndicator.setGravity(Gravity.CENTER);
        remoteIndicator.setPadding(dp(10), 0, dp(10), 0);

        toolbarLayout.addView(spacer1);
        toolbarLayout.addView(remoteIndicator);
        toolbarLayout.addView(dragHandle);
        toolbarLayout.addView(spacer2);
        toolbarLayout.addView(clipboardBtn);

        inputView = new HangulKeyboardView(this);
        inputView.setSettings(settings);
        updateShiftStateView();
        inputView.setOnKeyGestureListener(this);
        inputView.setOnPreviewOverlayListener(this);

        mainContainer.addView(toolbarLayout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainContainer.addView(inputView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        inputRoot.addView(mainContainer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));

        clipboardView = new ClipboardView(this, clipboardStore,
            () -> clipboardView.setVisibility(View.GONE),
            text -> {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) {
                    ic.commitText(text, 1);
                }
            });
        clipboardView.setVisibility(View.GONE);
        inputRoot.addView(clipboardView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        previewOverlayContainer = new FrameLayout(this);
        previewOverlayContainer.setClipChildren(false);
        previewOverlayContainer.setClipToPadding(false);
        previewPopup = new PopupWindow(previewOverlayContainer, 1, 1);
        previewPopup.setTouchable(false);
        previewPopup.setFocusable(false);
        previewPopup.setClippingEnabled(false);
        previewPopup.setBackgroundDrawable(null);
        ensurePreviewOverlay(0);

        floatingModeController.setOnPositionChangedListener(new FloatingModeController.OnPositionChangedListener() {
            @Override
            public void onPositionChanged(int offsetX, int offsetY) {
                applyFloatingMode();
            }
            @Override
            public void onFloatingModeChanged(boolean enabled) {
                updateToolbarVisibility();
                applyFloatingMode();
            }
        });

        updateToolbarVisibility();
        updateClipboardListener();
        return inputRoot;
    }

    private void updateToolbarVisibility() {
        if (toolbarLayout != null && floatingModeController != null && clipboardStore != null) {
            boolean floatingEnabled = false;
            boolean clipboardEnabled = clipboardStore.isEnabled() && !editorPolicy.password;
            boolean showToolbar = floatingEnabled || clipboardEnabled;
            toolbarLayout.setVisibility(showToolbar ? View.VISIBLE : View.GONE);
            dragHandle.setVisibility(floatingEnabled ? View.VISIBLE : View.INVISIBLE);
            clipboardBtn.setVisibility(clipboardEnabled ? View.VISIBLE : View.GONE);
            if (remoteIndicator != null) {
                remoteIndicator.setVisibility(View.GONE);
                remoteIndicator.setTextColor(contrastColor(settings.keyboardBackgroundColor));
            }
            if (!clipboardEnabled && clipboardView != null) {
                clipboardView.setVisibility(View.GONE);
            }
        }
    }

    private void toggleClipboard() {
        if (clipboardView != null) {
            boolean show = clipboardView.getVisibility() != View.VISIBLE;
            if (show) {
                clipboardView.refresh();
            }
            clipboardView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateClipboardListener() {
        if (clipboardManager == null || clipboardListener == null || clipboardStore == null) {
            return;
        }
        boolean shouldRegister = clipboardStore.isEnabled() && !editorPolicy.password;
        if (shouldRegister && !clipboardListenerRegistered) {
            clipboardManager.addPrimaryClipChangedListener(clipboardListener);
            clipboardListenerRegistered = true;
        } else if (!shouldRegister && clipboardListenerRegistered) {
            removeClipboardListener();
        }
    }

    private void removeClipboardListener() {
        if (clipboardManager != null && clipboardListener != null && clipboardListenerRegistered) {
            clipboardManager.removePrimaryClipChangedListener(clipboardListener);
            clipboardListenerRegistered = false;
        }
    }

    private void capturePrimaryClipboard() {
        if (clipboardManager == null
                || clipboardStore == null
                || !clipboardStore.isEnabled()
                || editorPolicy.password) {
            return;
        }
        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return;
        }
        CharSequence text = clip.getItemAt(0).coerceToText(this);
        if (text == null) {
            return;
        }
        clipboardStore.add(text.toString());
        if (clipboardView != null && clipboardView.getVisibility() == View.VISIBLE) {
            clipboardView.refresh();
        }
    }

    private void applyFloatingMode() {
        android.app.Dialog dialog = getWindow();
        if (dialog == null) return;
        android.view.Window window = dialog.getWindow();
        if (window == null) return;
        android.view.WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.x = 0;
        params.y = 0;
        window.setAttributes(params);
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        loadSettingsForEditor(info);
        if (inputView != null) {
            inputView.setSettings(settings);
            updateShiftStateView();
        }
        if (floatingModeController != null) {
            updateToolbarVisibility();
            applyFloatingMode();
        }
        updateClipboardListener();
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        loadSettingsForEditor(attribute);
        updateToolbarVisibility();
        updateClipboardListener();
        automata.reset();
        doubleSpacePeriodState.reset();
        englishShiftState.reset();
        pendingRemoteMetaState = 0;
        lockedRemoteMetaState = 0;
        updateShiftStateView();
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
        automata.reset();
        englishShiftState.reset();
        pendingRemoteMetaState = 0;
        lockedRemoteMetaState = 0;
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
        if (automata.getComposingText().isEmpty()) {
            return;
        }
        if (isComposingSelectionMismatch(newSelStart, newSelEnd, candidatesStart, candidatesEnd)) {
            InputConnection inputConnection = getCurrentInputConnection();
            if (inputConnection != null) {
                commitCurrent(inputConnection);
            } else {
                automata.reset();
            }
            doubleSpacePeriodState.reset();
        }
    }

    @Override
    public void onKeyGesture(String value) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null || value == null || value.isEmpty()) {
            return;
        }

        switch (value) {
            case KeyboardCommands.CMD_NOOP:
                return;
            case KeyboardCommands.CMD_DELETE:
                doubleSpacePeriodState.reset();
                delete(inputConnection);
                return;
            case KeyboardCommands.CMD_SPACE:
                commitSpace(inputConnection);
                return;
            case KeyboardCommands.CMD_ENTER:
                doubleSpacePeriodState.reset();
                performEnter(inputConnection);
                return;
            case KeyboardCommands.CMD_MOVE_LEFT:
                doubleSpacePeriodState.reset();
                moveCursor(inputConnection, false);
                return;
            case KeyboardCommands.CMD_MOVE_RIGHT:
                doubleSpacePeriodState.reset();
                moveCursor(inputConnection, true);
                return;
            case KeyboardCommands.CMD_TOGGLE_LANGUAGE:
                toggleLanguage(inputConnection);
                return;
            case KeyboardCommands.CMD_SHIFT_ONCE:
                handleShiftOnce();
                return;
            case KeyboardCommands.CMD_SHIFT_LOCK:
                handleShiftLock();
                return;
            case KeyboardCommands.CMD_RESERVED_PHRASES:
            case KeyboardCommands.CMD_RESERVED_LEFT:
            case KeyboardCommands.CMD_RESERVED_RIGHT:
            case KeyboardCommands.CMD_RESERVED_UP:
                commitReservedPhrase(inputConnection, value);
                return;
            case KeyboardCommands.CMD_DINGUL_CENTER_VOWEL:
                inputDingulContextualVowel(inputConnection, true);
                return;
            case KeyboardCommands.CMD_DINGUL_WIDE_VOWEL:
                inputDingulContextualVowel(inputConnection, false);
                return;
            case KeyboardCommands.CMD_OPEN_OPTIONS:
                openOptions(inputConnection);
                return;
            case KeyboardCommands.CMD_QUICK_SETTINGS:
                showQuickSettings();
                return;
            case KeyboardCommands.CMD_HAND_LEFT:
                setHandedness(HandednessMode.LEFT);
                return;
            case KeyboardCommands.CMD_HAND_RIGHT:
                setHandedness(HandednessMode.RIGHT);
                return;
            case KeyboardCommands.CMD_HAND_BALANCED:
                setHandedness(HandednessMode.BALANCED);
                return;
            case KeyboardCommands.CMD_INPUT_PICKER:
                showInputPicker();
                return;
            case KeyboardCommands.CMD_SETTINGS:
                openInputSettings();
                return;
            case KeyboardCommands.CMD_HIDE:
                commitCurrent(inputConnection);
                requestHideSelf(0);
                return;
            default:
                if (KeyboardCommands.isRemoteCommand(value)) {
                    handleRemoteCommand(inputConnection, value);
                    return;
                }
                inputText(inputConnection, value);
        }
    }

    private void inputDingulContextualVowel(InputConnection inputConnection, boolean centerVowelKey) {
        doubleSpacePeriodState.reset();
        char fallback = centerVowelKey ? 'ㅣ' : 'ㅡ';
        if (editorPolicy.rawKeyInput) {
            automata.reset();
            sendRawText(String.valueOf(fallback), inputConnection);
            return;
        }
        if (!editorPolicy.allowComposingText) {
            automata.reset();
            inputConnection.commitText(String.valueOf(fallback), 1);
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
        if (currentVowel == '\0'
                && combineWithPreviousOpenSyllableForDingulVowel(inputConnection, centerVowelKey)) {
            return;
        }
        inputText(inputConnection, String.valueOf(nextVowel));
    }

    private boolean combineWithPreviousOpenSyllableForDingulVowel(
            InputConnection inputConnection,
            boolean centerVowelKey) {
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
            inputConnection.deleteSurroundingText(1, 0);
            automata.reset();
            for (int i = 0; i < decomposed.length(); i++) {
                String committed = automata.input(decomposed.charAt(i));
                if (!committed.isEmpty()) {
                    inputConnection.commitText(committed, 1);
                }
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

        inputConnection.deleteSurroundingText(1, 0);
        automata.reset();
        for (int i = 0; i < decomposed.length(); i++) {
            String committed = automata.input(decomposed.charAt(i));
            if (!committed.isEmpty()) {
                inputConnection.commitText(committed, 1);
            }
        }
        String committed = automata.input(nextVowel);
        if (!committed.isEmpty()) {
            inputConnection.commitText(committed, 1);
        }
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

    private void inputText(InputConnection inputConnection, String text) {
        doubleSpacePeriodState.reset();
        if (settings.remoteModeEnabled && (pendingRemoteMetaState | lockedRemoteMetaState) != 0) {
            String remoteText = settings.keyboardMode == KeyboardMode.ENGLISH
                    ? englishShiftState.applyToInput(text)
                    : text;
            int remoteKeyCode = remotePrintableKeyCode(remoteText);
            if (remoteKeyCode != 0) {
                commitCurrent(inputConnection);
                sendRemoteKey(inputConnection, remoteKeyCode, remoteShiftMetaForText(remoteText));
                return;
            }
        }
        if (editorPolicy.rawKeyInput) {
            automata.reset();
            String rawText = settings.keyboardMode == KeyboardMode.ENGLISH
                    ? englishShiftState.applyToInput(text)
                    : text;
            sendRawText(rawText, inputConnection);
            updateShiftStateView();
            return;
        }
        if (settings.keyboardMode == KeyboardMode.ENGLISH) {
            inputConnection.commitText(englishShiftState.applyToInput(text), 1);
            updateShiftStateView();
            return;
        }

        if (!editorPolicy.allowComposingText) {
            automata.reset();
            inputConnection.commitText(text, 1);
            return;
        }

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (automata.isEmpty()
                    && HangulAutomata.isVowel(ch)
                    && combineWithPreviousStandaloneConsonant(inputConnection, ch)) {
                continue;
            }
            if (automata.isEmpty()
                    && HangulAutomata.canBeFinalConsonant(ch)
                    && combineWithPreviousOpenSyllable(inputConnection, ch)) {
                continue;
            }
            String committed = automata.input(ch);
            if (!committed.isEmpty()) {
                inputConnection.commitText(committed, 1);
            }
            updateComposing(inputConnection);
        }
    }

    private boolean combineWithPreviousStandaloneConsonant(InputConnection inputConnection, char vowel) {
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(1, 0);
        if (beforeCursor == null || beforeCursor.length() != 1) {
            return false;
        }
        char previous = beforeCursor.charAt(0);
        if (!HangulAutomata.isInitialConsonant(previous)) {
            return false;
        }

        inputConnection.deleteSurroundingText(1, 0);
        automata.reset();
        String committed = automata.input(previous);
        if (!committed.isEmpty()) {
            inputConnection.commitText(committed, 1);
        }
        committed = automata.input(vowel);
        if (!committed.isEmpty()) {
            inputConnection.commitText(committed, 1);
        }
        updateComposing(inputConnection);
        return true;
    }

    private boolean combineWithPreviousOpenSyllable(InputConnection inputConnection, char finalConsonant) {
        CharSequence beforeCursor = inputConnection.getTextBeforeCursor(1, 0);
        if (beforeCursor == null || beforeCursor.length() != 1) {
            return false;
        }
        String decomposed = HangulAutomata.decomposeOpenSyllable(beforeCursor.charAt(0));
        if (decomposed == null) {
            return false;
        }

        inputConnection.deleteSurroundingText(1, 0);
        automata.reset();
        for (int i = 0; i < decomposed.length(); i++) {
            String committed = automata.input(decomposed.charAt(i));
            if (!committed.isEmpty()) {
                inputConnection.commitText(committed, 1);
            }
        }
        String committed = automata.input(finalConsonant);
        if (!committed.isEmpty()) {
            inputConnection.commitText(committed, 1);
        }
        updateComposing(inputConnection);
        return true;
    }

    private void loadSettingsForEditor(EditorInfo info) {
        enterAction = ImeActionLabelResolver.resolve(info);
        editorPolicy = EditorInputPolicy.from(info);
        KeyboardSettings storedSettings = KeyboardPreferences.load(this);
        KeyboardMode runtimeMode = editorPolicy.initialKeyboardMode(storedSettings.keyboardMode);
        settings = storedSettings
                .withKeyboardMode(runtimeMode)
                .withEnterKeyLabel(enterAction.label)
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
    }

    private void commitSpace(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        if (settings.remoteModeEnabled || editorPolicy.rawKeyInput) {
            doubleSpacePeriodState.reset();
            sendKeyChar(' ');
            return;
        }
        DoubleSpacePeriodState.SpaceResult result = doubleSpacePeriodState.onSpace(
                settings.keyboardMode,
                settings.englishDoubleSpacePeriodEnabled && editorPolicy.allowTextConveniences,
                System.currentTimeMillis());
        if (result == DoubleSpacePeriodState.SpaceResult.REPLACE_PREVIOUS_SPACE_WITH_PERIOD_SPACE) {
            inputConnection.deleteSurroundingText(1, 0);
            inputConnection.commitText(". ", 1);
        } else {
            inputConnection.commitText(" ", 1);
        }
    }

    private void performEnter(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        if (settings.remoteModeEnabled || editorPolicy.rawKeyInput) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
            return;
        }
        if (enterAction.performEditorAction) {
            inputConnection.performEditorAction(enterAction.editorActionId);
            return;
        }
        if (!inputConnection.commitText("\n", 1)) {
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
        }
    }

    private void toggleLanguage(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        doubleSpacePeriodState.reset();
        englishShiftState.reset();
        boolean lockedToEnglish = editorPolicy.locksLanguageToggle();
        KeyboardMode nextMode = lockedToEnglish ? KeyboardMode.ENGLISH : settings.keyboardMode.next();
        settings = settings.withKeyboardMode(nextMode)
                .withEnterKeyLabel(enterAction.label)
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
        if (!lockedToEnglish) {
            KeyboardPreferences.saveKeyboardMode(this, nextMode);
        }
        if (inputView != null) {
            inputView.setSettings(settings);
            updateShiftStateView();
        }
    }

    private void handleShiftOnce() {
        if (settings.keyboardMode != KeyboardMode.ENGLISH) {
            return;
        }
        doubleSpacePeriodState.reset();
        englishShiftState.onShiftOnceCommand();
        updateShiftStateView();
    }

    private void handleShiftLock() {
        if (settings.keyboardMode != KeyboardMode.ENGLISH) {
            return;
        }
        doubleSpacePeriodState.reset();
        englishShiftState.onShiftLockCommand();
        updateShiftStateView();
    }

    private void updateShiftStateView() {
        if (inputView != null) {
            inputView.setEnglishShiftState(
                    settings.keyboardMode == KeyboardMode.ENGLISH && englishShiftState.isActive(),
                    settings.keyboardMode == KeyboardMode.ENGLISH && englishShiftState.isLocked());
            inputView.setRemoteMetaState(pendingRemoteMetaState, lockedRemoteMetaState);
        }
    }

    private void delete(InputConnection inputConnection) {
        if (settings.remoteModeEnabled || editorPolicy.rawKeyInput) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            return;
        }
        if (editorPolicy.allowComposingText && automata.backspace()) {
            updateComposing(inputConnection);
        } else {
            deleteOneCodePoint(inputConnection);
        }
    }

    private void deleteOneCodePoint(InputConnection inputConnection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && inputConnection.deleteSurroundingTextInCodePoints(1, 0)) {
            return;
        }
        inputConnection.deleteSurroundingText(1, 0);
    }

    private void moveCursor(InputConnection inputConnection, boolean right) {
        commitCurrent(inputConnection);
        if (isCursorAtBoundary(inputConnection, right)) {
            return;
        }
        int keyCode = right ? KeyEvent.KEYCODE_DPAD_RIGHT : KeyEvent.KEYCODE_DPAD_LEFT;
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
    }

    private boolean isCursorAtBoundary(InputConnection inputConnection, boolean right) {
        CharSequence surroundingText = right
                ? inputConnection.getTextAfterCursor(1, 0)
                : inputConnection.getTextBeforeCursor(1, 0);
        return surroundingText == null || surroundingText.length() == 0;
    }

    private void updateComposing(InputConnection inputConnection) {
        String composing = automata.getComposingText();
        if (composing.isEmpty()) {
            inputConnection.finishComposingText();
        } else {
            inputConnection.setComposingText(composing, 1);
        }
    }

    private boolean isComposingSelectionMismatch(
            int newSelStart,
            int newSelEnd,
            int candidatesStart,
            int candidatesEnd) {
        if (newSelStart != newSelEnd) {
            return true;
        }
        if (candidatesStart < 0 || candidatesEnd < 0) {
            return false;
        }
        return newSelStart < candidatesStart || newSelStart != candidatesEnd;
    }

    private void sendRawText(String text, InputConnection inputConnection) {
        for (int offset = 0; offset < text.length();) {
            int codePoint = text.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (codePoint == '\n') {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
            } else if (codePoint >= 0x20 && codePoint <= 0x7E) {
                sendKeyChar((char) codePoint);
            } else {
                inputConnection.commitText(new String(Character.toChars(codePoint)), 1);
            }
        }
    }

    private void commitCurrent(InputConnection inputConnection) {
        String composing = automata.flush();
        if (composing.isEmpty()) {
            inputConnection.finishComposingText();
        } else {
            inputConnection.commitText(composing, 1);
        }
    }

    private void commitReservedPhrase(InputConnection inputConnection, String command) {
        commitCurrent(inputConnection);
        doubleSpacePeriodState.reset();
        String phrase = KeyboardPreferences.loadReservedPhraseForCommand(this, command);
        if (phrase == null || phrase.isEmpty()) {
            return;
        }
        if (editorPolicy.rawKeyInput) {
            sendRawText(phrase, inputConnection);
        } else {
            inputConnection.commitText(phrase, 1);
        }
    }

    private void handleRemoteCommand(InputConnection inputConnection, String command) {
        commitCurrent(inputConnection);
        doubleSpacePeriodState.reset();

        switch (command) {
            case KeyboardCommands.CMD_REMOTE_CTRL_LATCH:
                handleRemoteMetaTap(remoteCtrlMeta());
                return;
            case KeyboardCommands.CMD_REMOTE_WIN_LATCH:
                handleRemoteMetaTap(remoteWinMeta());
                return;
            case KeyboardCommands.CMD_REMOTE_ALT_LATCH:
                handleRemoteMetaTap(remoteAltMeta());
                return;
            case KeyboardCommands.CMD_REMOTE_CTRL_LOCK:
                lockedRemoteMetaState = toggleLockedRemoteMeta(remoteCtrlMeta());
                updateShiftStateView();
                return;
            case KeyboardCommands.CMD_REMOTE_WIN_LOCK:
                lockedRemoteMetaState = toggleLockedRemoteMeta(remoteWinMeta());
                updateShiftStateView();
                return;
            case KeyboardCommands.CMD_REMOTE_ALT_LOCK:
                lockedRemoteMetaState = toggleLockedRemoteMeta(remoteAltMeta());
                updateShiftStateView();
                return;
            case KeyboardCommands.CMD_REMOTE_SHIFT_TAB:
                sendRemoteKey(inputConnection, KeyEvent.KEYCODE_TAB, KeyEvent.META_SHIFT_ON);
                return;
            case KeyboardCommands.CMD_REMOTE_CTRL_TAB:
                sendRemoteKey(inputConnection, KeyEvent.KEYCODE_TAB, KeyEvent.META_CTRL_ON);
                return;
            case KeyboardCommands.CMD_REMOTE_ALT_TAB:
                sendRemoteKey(inputConnection, KeyEvent.KEYCODE_TAB, KeyEvent.META_ALT_ON);
                return;
            case KeyboardCommands.CMD_REMOTE_CTRL_ENTER:
                sendRemoteKey(inputConnection, KeyEvent.KEYCODE_ENTER, KeyEvent.META_CTRL_ON);
                return;
            case KeyboardCommands.CMD_REMOTE_IME_TOGGLE:
                sendRemoteImeToggle(inputConnection);
                return;
            default:
                int keyCode = remoteKeyCodeFor(command);
                if (keyCode != 0) {
                    sendRemoteKey(inputConnection, keyCode, 0);
                }
        }
    }

    private int togglePendingRemoteMeta(int metaState) {
        return (pendingRemoteMetaState & metaState) == metaState
                ? pendingRemoteMetaState & ~metaState
                : pendingRemoteMetaState | metaState;
    }

    private void handleRemoteMetaTap(int metaState) {
        if ((lockedRemoteMetaState & metaState) == metaState) {
            lockedRemoteMetaState &= ~metaState;
            pendingRemoteMetaState &= ~metaState;
        } else {
            pendingRemoteMetaState = togglePendingRemoteMeta(metaState);
        }
        updateShiftStateView();
    }

    private int toggleLockedRemoteMeta(int metaState) {
        pendingRemoteMetaState &= ~metaState;
        return (lockedRemoteMetaState & metaState) == metaState
                ? lockedRemoteMetaState & ~metaState
                : lockedRemoteMetaState | metaState;
    }

    private int remoteCtrlMeta() {
        return KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON;
    }

    private int remoteWinMeta() {
        return KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON;
    }

    private int remoteAltMeta() {
        return KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON;
    }

    private void sendRemoteImeToggle(InputConnection inputConnection) {
        pendingRemoteMetaState = 0;
        lockedRemoteMetaState = 0;
        switch (settings.remoteImeShortcut) {
            case CTRL_SPACE:
                sendRemoteKey(inputConnection, KeyEvent.KEYCODE_SPACE, KeyEvent.META_CTRL_ON);
                return;
            case WIN_SPACE:
                sendRemoteKey(inputConnection, KeyEvent.KEYCODE_SPACE, KeyEvent.META_META_ON);
                return;
            case LANGUAGE_SWITCH:
                sendRemoteKey(inputConnection, KeyEvent.KEYCODE_LANGUAGE_SWITCH, 0);
                return;
            case ALT_SHIFT:
            default:
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ALT_LEFT));
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
                inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT));
        }
    }

    private void sendRemoteKey(InputConnection inputConnection, int keyCode, int metaState) {
        int combinedMetaState = metaState | pendingRemoteMetaState | lockedRemoteMetaState;
        pendingRemoteMetaState = 0;
        updateShiftStateView();
        inputConnection.sendKeyEvent(new KeyEvent(
                0,
                0,
                KeyEvent.ACTION_DOWN,
                keyCode,
                0,
                combinedMetaState));
        inputConnection.sendKeyEvent(new KeyEvent(
                0,
                0,
                KeyEvent.ACTION_UP,
                keyCode,
                0,
                combinedMetaState));
    }

    private int remoteKeyCodeFor(String command) {
        return RemoteKeyEventMap.keyCodeFor(command);
    }

    private int remotePrintableKeyCode(String text) {
        if (text == null || text.length() != 1) {
            return 0;
        }
        char ch = Character.toLowerCase(text.charAt(0));
        if (ch >= 'a' && ch <= 'z') {
            return KeyEvent.KEYCODE_A + (ch - 'a');
        }
        if (ch >= '1' && ch <= '9') {
            return KeyEvent.KEYCODE_1 + (ch - '1');
        }
        if (ch == '0') {
            return KeyEvent.KEYCODE_0;
        }
        return 0;
    }

    private int remoteShiftMetaForText(String text) {
        if (text == null || text.length() != 1) {
            return 0;
        }
        char ch = text.charAt(0);
        return ch >= 'A' && ch <= 'Z'
                ? KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON
                : 0;
    }

    private void showInputPicker() {
        InputMethodManager imm = getSystemService(InputMethodManager.class);
        if (imm != null) {
            imm.showInputMethodPicker();
        }
    }

    private void openOptions(InputConnection inputConnection) {
        commitCurrent(inputConnection);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setHandedness(HandednessMode mode) {
        doubleSpacePeriodState.reset();
        settings = settings.withHandednessPreset(mode)
                .withEnterKeyLabel(enterAction.label)
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
        KeyboardPreferences.saveHandednessPreset(this, settings);
        if (inputView != null) {
            inputView.setSettings(settings);
            updateShiftStateView();
        }
    }

    private void openInputSettings() {
        Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showQuickSettings() {
        if (inputRoot == null) {
            return;
        }
        if (quickSettingsPopup != null && quickSettingsPopup.isShowing()) {
            dismissQuickSettings();
            return;
        }

        SettingsUiPalette ui = SettingsUiPalette.from(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(12), dp(14), dp(14));
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surfaceRaised);
        background.setCornerRadius(dp(12));
        background.setStroke(Math.max(1, dp(1)), ui.border);
        panel.setBackground(background);

        TextView title = new TextView(this);
        title.setText("빠른 설정");
        title.setTextColor(ui.textPrimary);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        panel.addView(title, matchWrap());

        panel.addView(quickButton(remoteModeToggleLabel(), settings.remoteModeEnabled, v -> toggleRemoteMode()), topWrap(8));
        if (settings.remoteModeEnabled) {
            addRemoteTestControls(panel);
        }
        panel.addView(quickButton(numberRowToggleLabel(), activeNumberRowVisible(), v -> toggleActiveNumberRow()), topWrap(8));
        LinearLayout handRow = new LinearLayout(this);
        handRow.setOrientation(LinearLayout.HORIZONTAL);
        handRow.addView(handednessButton("왼쪽", HandednessMode.LEFT), weightedQuickParams(0, 4));
        handRow.addView(handednessButton("양손", HandednessMode.BALANCED), weightedQuickParams(0, 4));
        handRow.addView(handednessButton("오른쪽", HandednessMode.RIGHT), weightedQuickParams(0, 0));
        panel.addView(handRow, topWrap(6));
        panel.addView(quickButton("테마 선택", false, v -> {
            dismissQuickSettings();
            Intent intent = new Intent(this, ThemeSelectorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }), topWrap(6));
        panel.addView(quickButton("클립보드 테마 불러오기", false, v -> importThemeFromClipboard()), topWrap(6));

        panel.addView(quickButton("OK", false, v -> dismissQuickSettings()), topWrap(8));
        quickSettingsPopup = new PopupWindow(
                panel,
                Math.max(dp(280), getResources().getDisplayMetrics().widthPixels - dp(24)),
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);
        quickSettingsPopup.setOutsideTouchable(true);
        quickSettingsPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        quickSettingsPopup.setClippingEnabled(false);
        quickSettingsPopup.showAtLocation(inputRoot, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dp(12));
    }

    private void addRemoteTestControls(LinearLayout panel) {
        TextView label = new TextView(this);
        label.setText("원격 키 테스트");
        label.setTextColor(SettingsUiPalette.from(this).textSecondary);
        label.setTextSize(13);
        panel.addView(label, topWrap(10));

        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.addView(remoteTestButton("Esc", v -> sendRemoteTestKey(KeyEvent.KEYCODE_ESCAPE, 0)),
                weightedQuickParams(0, 4));
        row1.addView(remoteTestButton("Tab", v -> sendRemoteTestKey(KeyEvent.KEYCODE_TAB, 0)),
                weightedQuickParams(0, 4));
        row1.addView(remoteTestButton("F1", v -> sendRemoteTestKey(KeyEvent.KEYCODE_F1, 0)),
                weightedQuickParams(0, 4));
        row1.addView(remoteTestButton("Ctrl+A", v -> sendRemoteTestKey(
                KeyEvent.KEYCODE_A,
                KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON)),
                weightedQuickParams(0, 0));
        panel.addView(row1, topWrap(4));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.addView(remoteTestButton("Alt+Shift", v -> sendRemoteTestAltShift()),
                weightedQuickParams(0, 4));
        row2.addView(remoteTestButton("Ctrl+Space", v -> sendRemoteTestKey(
                KeyEvent.KEYCODE_SPACE,
                KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON)),
                weightedQuickParams(0, 4));
        row2.addView(remoteTestButton("Win+Space", v -> sendRemoteTestKey(
                KeyEvent.KEYCODE_SPACE,
                KeyEvent.META_META_ON | KeyEvent.META_META_LEFT_ON)),
                weightedQuickParams(0, 4));
        row2.addView(remoteTestButton("Lang", v -> sendRemoteTestKey(
                KeyEvent.KEYCODE_LANGUAGE_SWITCH,
                0)), weightedQuickParams(0, 0));
        panel.addView(row2, topWrap(4));
    }

    private Button remoteTestButton(String text, View.OnClickListener listener) {
        Button button = quickButton(text, false, listener);
        button.setTextSize(11);
        button.setMinHeight(dp(38));
        button.setPadding(dp(8), 0, dp(8), 0);
        return button;
    }

    private void sendRemoteTestKey(int keyCode, int metaState) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return;
        }
        commitCurrent(inputConnection);
        sendRemoteKey(inputConnection, keyCode, metaState);
    }

    private void sendRemoteTestAltShift() {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) {
            return;
        }
        commitCurrent(inputConnection);
        pendingRemoteMetaState = 0;
        lockedRemoteMetaState = 0;
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ALT_LEFT));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT));
    }

    private void dismissQuickSettings() {
        if (quickSettingsPopup != null) {
            quickSettingsPopup.dismiss();
        }
        quickSettingsPopup = null;
    }

    private Button quickButton(String text, boolean selected, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        styleQuickButton(button, selected);
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        return button;
    }

    private Button handednessButton(String text, HandednessMode mode) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        styleQuickButton(button, settings.handednessMode == mode);
        button.setOnClickListener(v -> {
            setHandedness(mode);
            dismissQuickSettings();
        });
        return button;
    }

    private void styleQuickButton(Button button, boolean selected) {
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        button.setAllCaps(false);
        button.setTextColor(selected ? ui.selectedText : ui.controlText);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(44));
        button.setPadding(dp(24), 0, dp(24), 0);
        GradientDrawable background = new GradientDrawable();
        background.setColor(selected ? ui.selectedFill : ui.controlFill);
        background.setCornerRadius(dp(8));
        background.setStroke(Math.max(1, dp(selected ? 2 : 1)), selected ? ui.selectedBorder : ui.border);
        button.setBackground(background);
    }

    private boolean activeNumberRowVisible() {
        return settings.showNumberRow;
    }

    private String numberRowToggleLabel() {
        String layout = settings.keyboardMode == KeyboardMode.ENGLISH ? "쿼티" : "딩굴";
        return layout + " number row: " + (activeNumberRowVisible() ? "on" : "off");
    }

    private String remoteModeToggleLabel() {
        return "Remote mode: " + (settings.remoteModeEnabled ? "on" : "off");
    }

    private void toggleRemoteMode() {
        settings = settings
                .withRemoteOptions(
                        !settings.remoteModeEnabled,
                        settings.remoteKeyPreset,
                        settings.remoteImeShortcut)
                .withEnterKeyLabel(enterAction.label)
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
        pendingRemoteMetaState = 0;
        lockedRemoteMetaState = 0;
        KeyboardPreferences.saveSettings(this, settings);
        applyCurrentSettingsToInputView();
        updateToolbarVisibility();
        dismissQuickSettings();
    }

    private void toggleActiveNumberRow() {
        if (settings.remoteModeEnabled) {
            Toast.makeText(this, "Remote mode forces number row", Toast.LENGTH_SHORT).show();
            dismissQuickSettings();
            return;
        }
        settings = (settings.keyboardMode == KeyboardMode.ENGLISH
                ? settings.withEnglishNumberRow(!settings.showEnglishNumberRow)
                : settings.withHangulNumberRow(!settings.showHangulNumberRow))
                .withEnterKeyLabel(enterAction.label)
                .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
        KeyboardPreferences.saveSettings(this, settings);
        if (inputView != null) {
            inputView.setSettings(settings);
            updateShiftStateView();
        }
        Toast.makeText(this, numberRowToggleLabel(), Toast.LENGTH_SHORT).show();
        dismissQuickSettings();
    }

    private void importThemeFromClipboard() {
        String json = currentClipboardText();
        if (json.isEmpty()) {
            Toast.makeText(this, "클립보드에 테마 JSON이 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            settings = KeyboardThemeJson.importTheme(settings, json)
                    .withEnterKeyLabel(enterAction.label)
                    .withRuntimeNumberRowForced(editorPolicy.forceNumberRow);
            KeyboardPreferences.saveSelectedThemeId(this, "");
            KeyboardPreferences.saveSettings(this, settings);
            applyCurrentSettingsToInputView();
            Toast.makeText(this, "클립보드 테마를 불러왔습니다", Toast.LENGTH_SHORT).show();
            dismissQuickSettings();
        } catch (IllegalArgumentException exception) {
            Toast.makeText(this, "유효한 테마 JSON이 아닙니다", Toast.LENGTH_SHORT).show();
        }
    }

    private String currentClipboardText() {
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip()) {
            return "";
        }
        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return "";
        }
        CharSequence text = clip.getItemAt(0).coerceToText(this);
        return text == null ? "" : text.toString().trim();
    }

    private void applyCurrentSettingsToInputView() {
        if (toolbarLayout != null) {
            toolbarLayout.setBackgroundColor(settings.keyboardBackgroundColor);
        }
        if (inputView != null) {
            inputView.setSettings(settings);
            updateShiftStateView();
        }
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams topWrap(int topMarginDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(topMarginDp);
        return params;
    }

    private LinearLayout.LayoutParams weightedQuickParams(int leftMarginDp, int rightMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        params.leftMargin = dp(leftMarginDp);
        params.rightMargin = dp(rightMarginDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int contrastColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        return luminance > 0.58 ? 0xFF111827 : 0xFFFFFFFF;
    }

    @Override
    public void onPreviewOverlayChanged(HangulKeyboardView.PreviewOverlaySpec spec) {
        if (spec == null) {
            return;
        }
        List<HangulKeyboardView.PreviewOverlaySpec> specs = new ArrayList<>();
        specs.add(spec);
        onPreviewOverlaysChanged(specs);
    }

    @Override
    public void onPreviewOverlaysChanged(List<HangulKeyboardView.PreviewOverlaySpec> specs) {
        if (inputView == null || previewPopup == null || previewOverlayContainer == null
                || specs == null || specs.isEmpty()) {
            dismissPreviewPopup();
            return;
        }
        int[] windowLocation = new int[2];
        inputView.getLocationInWindow(windowLocation);
        int maxBottom = inputView.getHeight();
        int requiredTopPad = dp(PREVIEW_POPUP_TOP_RESERVE_DP);
        for (HangulKeyboardView.PreviewOverlaySpec spec : specs) {
            requiredTopPad = Math.max(requiredTopPad, Math.max(0, -spec.y) + dp(4));
            maxBottom = Math.max(maxBottom, spec.y + spec.height);
        }
        int topPad = previewPopup.isShowing()
                ? Math.max(requiredTopPad, previewPopupTopPadPx)
                : requiredTopPad;
        previewPopupTopPadPx = topPad;
        int popupWidth = Math.max(inputView.getWidth(), 1);
        int popupHeight = Math.max(1, topPad + maxBottom + dp(4));
        previewOverlayContainer.setMinimumWidth(popupWidth);
        previewOverlayContainer.setMinimumHeight(popupHeight);
        for (int i = 0; i < specs.size(); i++) {
            TextView overlay = ensurePreviewOverlay(i);
            HangulKeyboardView.PreviewOverlaySpec spec = specs.get(i);
            applyPreviewOverlaySpec(overlay, spec);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(spec.width, spec.height);
            params.leftMargin = spec.x;
            params.topMargin = spec.y + topPad;
            overlay.setLayoutParams(params);
            overlay.setVisibility(View.VISIBLE);
        }
        for (int i = specs.size(); i < previewOverlayPool.size(); i++) {
            previewOverlayPool.get(i).setVisibility(View.GONE);
        }
        int popupX = windowLocation[0];
        int popupY = windowLocation[1] - topPad;
        if (previewPopup.isShowing()) {
            previewPopup.update(popupX, popupY, popupWidth, popupHeight);
        } else {
            previewPopup.setWidth(popupWidth);
            previewPopup.setHeight(popupHeight);
            previewPopup.showAtLocation(inputView, Gravity.NO_GRAVITY, popupX, popupY);
        }
    }

    private TextView ensurePreviewOverlay(int index) {
        while (previewOverlayPool.size() <= index) {
            TextView overlay = new TextView(this);
            overlay.setGravity(Gravity.CENTER);
            overlay.setSingleLine(true);
            overlay.setIncludeFontPadding(false);
            overlay.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            overlay.setVisibility(View.VISIBLE);
            previewOverlayPool.add(overlay);
            previewOverlayContainer.addView(overlay, new FrameLayout.LayoutParams(1, 1));
            if (previewOverlay == null) {
                previewOverlay = overlay;
            }
        }
        return previewOverlayPool.get(index);
    }

    private void applyPreviewOverlaySpec(TextView overlay, HangulKeyboardView.PreviewOverlaySpec spec) {
        overlay.setText(spec.label);
        overlay.setTextColor(spec.textColor);
        overlay.setTextSize(TypedValue.COMPLEX_UNIT_PX, spec.textSizePx);
        overlay.setAlpha(spec.alpha);
        overlay.setPivotX(spec.width / 2f);
        overlay.setPivotY(spec.height);
        overlay.setScaleX(spec.scale);
        overlay.setScaleY(spec.scale);
        overlay.setTypeface(KeyboardTypefaceCatalog.typefaceFor(
                this,
                settings.fontFamily,
                settings.primaryTextBold,
                settings.primaryTextItalic));
        if (spec.angularBubble) {
            overlay.setPadding(0, 0, 0, dp(22));
            overlay.setBackground(new PreviewBubbleDrawable(
                    spec.backgroundColor,
                    spec.borderColor,
                    spec.borderWidthPx,
                    spec.cornerRadiusPx,
                    dp(22)));
        } else {
            overlay.setPadding(0, 0, 0, 0);
            GradientDrawable background = new GradientDrawable();
            background.setColor(spec.backgroundColor);
            background.setCornerRadius(spec.cornerRadiusPx);
            if (spec.borderWidthPx > 0) {
                background.setStroke(spec.borderWidthPx, spec.borderColor);
            }
            overlay.setBackground(background);
        }
    }

    @Override
    public void onPreviewOverlayHidden() {
        dismissPreviewPopup();
    }

    private void dismissPreviewPopup() {
        if (previewPopup != null && previewPopup.isShowing()) {
            previewPopup.dismiss();
        }
        for (TextView overlay : previewOverlayPool) {
            overlay.setVisibility(View.GONE);
        }
        previewPopupTopPadPx = 0;
    }

    @Override
    public void onDestroy() {
        dismissPreviewPopup();
        dismissQuickSettings();
        removeClipboardListener();
        super.onDestroy();
    }
}
