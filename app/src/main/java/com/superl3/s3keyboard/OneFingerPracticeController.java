package com.superl3.s3keyboard;

import android.app.Activity;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.inputmethod.BaseInputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

final class OneFingerPracticeController {
    private final Activity activity;
    private final GesturePracticeInputController inputController;
    private final OneFingerPracticeSession session = new OneFingerPracticeSession();
    private final SettingsUiPalette palette;

    private TextView lessonLabel;
    private TextView targetText;
    private TextView gestureGuide;
    private TextView progressText;
    private EditText input;
    private Button previousButton;
    private Button nextButton;
    private boolean syncing;

    OneFingerPracticeController(Activity activity) {
        this.activity = activity;
        this.inputController = new GesturePracticeInputController(activity);
        this.palette = SettingsUiPalette.from(activity);
    }

    void addTo(LinearLayout root) {
        SettingsRowBuilder.labelRow(activity, root, R.string.one_finger_practice_title, 14);
        SettingsRowBuilder.secondaryLabelRow(
                activity,
                root,
                R.string.one_finger_practice_privacy,
                4);

        LinearLayout practice = SettingsRowBuilder.vertical(activity);
        practice.setPadding(
                SettingsRowBuilder.dp(activity, 10),
                SettingsRowBuilder.dp(activity, 10),
                SettingsRowBuilder.dp(activity, 10),
                SettingsRowBuilder.dp(activity, 10));
        root.addView(practice, SettingsRowBuilder.matchWrapWithTop(activity, 8));
        inputController.setFocusRetentionRegion(practice);

        lessonLabel = SettingsRowBuilder.label(activity, "");
        lessonLabel.setGravity(Gravity.CENTER);
        practice.addView(lessonLabel, SettingsRowBuilder.matchWrap());

        targetText = SettingsRowBuilder.label(activity, "");
        targetText.setGravity(Gravity.CENTER);
        targetText.setTextSize(24);
        targetText.setTypeface(targetText.getTypeface(), Typeface.BOLD);
        targetText.setPadding(0, SettingsRowBuilder.dp(activity, 12), 0, 0);
        practice.addView(targetText, SettingsRowBuilder.matchWrap());

        gestureGuide = SettingsRowBuilder.secondaryLabel(activity, "");
        gestureGuide.setGravity(Gravity.CENTER);
        gestureGuide.setPadding(
                SettingsRowBuilder.dp(activity, 4),
                SettingsRowBuilder.dp(activity, 8),
                SettingsRowBuilder.dp(activity, 4),
                0);
        practice.addView(gestureGuide, SettingsRowBuilder.matchWrap());

        progressText = SettingsRowBuilder.secondaryLabel(activity, "");
        progressText.setGravity(Gravity.CENTER);
        practice.addView(progressText, SettingsRowBuilder.matchWrapWithTop(activity, 6));

        input = inputController.createInput(DemoFieldProfile.STANDARD, false);
        input.setHint(R.string.one_finger_practice_hint);
        input.setMaxLines(3);
        input.setSaveEnabled(false);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!syncing) {
                    onInputChanged(editable);
                }
            }
        });
        practice.addView(input, SettingsRowBuilder.matchWrapWithTop(activity, 10));

        LinearLayout actions = SettingsRowBuilder.horizontal(activity);
        previousButton = SettingsRowBuilder.button(
                activity,
                R.string.one_finger_practice_previous,
                view -> moveLesson(-1));
        Button resetButton = SettingsRowBuilder.button(
                activity,
                R.string.one_finger_practice_reset,
                view -> clearInput());
        nextButton = SettingsRowBuilder.button(
                activity,
                R.string.one_finger_practice_next,
                view -> moveLesson(1));
        actions.addView(previousButton, SettingsRowBuilder.weightedWrap(activity, 1f, 0, 4));
        actions.addView(resetButton, SettingsRowBuilder.weightedWrap(activity, 1f, 4, 4));
        actions.addView(nextButton, SettingsRowBuilder.weightedWrap(activity, 1f, 4, 0));
        practice.addView(actions, SettingsRowBuilder.matchWrapWithTop(activity, 8));
        render(session.evaluate(""));
    }

    void hideKeyboardWhenTouchingOutside(MotionEvent event) {
        inputController.hideKeyboardWhenTouchingOutside(event);
    }

    void onEnabledChanged(boolean enabled) {
        if (!enabled) {
            clearInput();
            inputController.clearFocusAndHideKeyboard();
        }
    }

    private void onInputChanged(Editable editable) {
        render(session.evaluate(
                editable,
                BaseInputConnection.getComposingSpanStart(editable)));
    }

    private void moveLesson(int direction) {
        if (direction < 0) {
            session.previous();
        } else {
            session.next();
        }
        clearInput();
    }

    private void clearInput() {
        if (input == null) {
            return;
        }
        syncing = true;
        input.setText("");
        syncing = false;
        render(session.evaluate(""));
    }

    private void render(OneFingerPracticeSession.Progress progress) {
        if (progress == null || lessonLabel == null) {
            return;
        }
        OneFingerPracticeSession.Lesson lesson = session.currentLesson();
        lessonLabel.setText(activity.getString(
                R.string.one_finger_practice_step_format,
                session.currentIndex() + 1,
                session.lessonCount(),
                activity.getString(lesson.labelResId)));
        targetText.setText(styledTarget(progress));
        gestureGuide.setText(lesson.guideResId);
        if (progress.complete) {
            progressText.setText(R.string.one_finger_practice_complete);
            progressText.setTextColor(palette.specialForeground);
        } else if (progress.mismatch) {
            progressText.setText(activity.getString(
                    R.string.one_finger_practice_mismatch_format,
                    progress.matchedLength + 1));
            progressText.setTextColor(palette.specialForeground);
        } else if (progress.compositionPending) {
            progressText.setText(activity.getString(
                    R.string.one_finger_practice_composing_format,
                    progress.matchedLength,
                    progress.target.length()));
            progressText.setTextColor(palette.textSecondary);
        } else {
            progressText.setText(activity.getString(
                    R.string.one_finger_practice_progress_format,
                    progress.matchedLength,
                    progress.target.length()));
            progressText.setTextColor(palette.textSecondary);
        }
        previousButton.setEnabled(session.hasPrevious());
        nextButton.setEnabled(session.hasNext());
        SettingsViewStyler.button(nextButton, activity, progress.complete && session.hasNext());
    }

    private CharSequence styledTarget(OneFingerPracticeSession.Progress progress) {
        SpannableString styled = new SpannableString(progress.target);
        styled.setSpan(
                new ForegroundColorSpan(palette.textSecondary),
                0,
                styled.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (progress.matchedLength > 0) {
            styled.setSpan(
                    new ForegroundColorSpan(palette.specialForeground),
                    0,
                    Math.min(progress.matchedLength, styled.length()),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return styled;
    }
}
