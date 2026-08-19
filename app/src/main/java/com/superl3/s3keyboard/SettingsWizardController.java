package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

final class SettingsWizardController {
    private static final String STATE_SELECTED_INDEX = "settings_wizard_selected_index";
    private static final String STATE_SHOW_ALL = "settings_wizard_show_all";
    private static final String STATE_SEARCH_QUERY = "settings_wizard_search_query";

    private final Activity context;
    private final LinearLayout contentRoot;
    private final Runnable scrollContentToTop;
    private final List<Step> steps = new ArrayList<>();
    private final List<Button> stepButtons = new ArrayList<>();
    private final TextView statusLabel;
    private final HorizontalScrollView stepScroller;
    private final LinearLayout stepButtonRow;
    private final Button previousButton;
    private final Button nextButton;
    private final Button modeButton;
    private final GesturePracticeInputController searchInputController;
    private final EditText searchInput;
    private final ImageButton clearSearchButton;

    private boolean showAll;
    private int selectedIndex;
    private String normalizedSearchQuery = "";

    SettingsWizardController(
            Activity context,
            LinearLayout chromeRoot,
            LinearLayout contentRoot,
            Runnable scrollContentToTop) {
        this.context = context;
        this.contentRoot = contentRoot;
        this.scrollContentToTop = RuntimeDefaults.runnable(scrollContentToTop);

        LinearLayout chrome = SettingsRowBuilder.vertical(context);
        chrome.setPadding(
                SettingsRowBuilder.dp(context, 16),
                SettingsRowBuilder.dp(context, 12),
                SettingsRowBuilder.dp(context, 16),
                SettingsRowBuilder.dp(context, 14));
        SettingsUiPalette ui = SettingsUiPalette.from(context);
        GradientDrawable background = new GradientDrawable();
        background.setColor(ui.surface);
        background.setCornerRadius(SettingsRowBuilder.dp(context, 8));
        background.setStroke(Math.max(1, SettingsRowBuilder.dp(context, 1)), ui.border);
        chrome.setBackground(background);
        chromeRoot.addView(chrome, SettingsRowBuilder.matchWrapWithTop(context, 8));

        searchInputController = new GesturePracticeInputController(context);
        searchInput = searchInputController.createInput(
                DemoFieldProfile.fromName("search"),
                false);
        searchInput.setHint(R.string.settings_search_hint);
        searchInput.setSaveEnabled(false);
        searchInput.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_keyboard_search,
                0,
                0,
                0);
        searchInput.setCompoundDrawablePadding(SettingsRowBuilder.dp(context, 8));
        searchInput.setCompoundDrawableTintList(ColorStateList.valueOf(ui.textSecondary));

        clearSearchButton = new ImageButton(context);
        clearSearchButton.setContentDescription(context.getString(R.string.settings_search_clear));
        SettingsViewStyler.iconButton(
                clearSearchButton,
                context,
                R.drawable.ic_settings_clear);
        clearSearchButton.setVisibility(View.GONE);
        clearSearchButton.setOnClickListener(view -> searchInput.setText(""));

        searchInput.addTextChangedListener(UserInputListeners.text(
                () -> true,
                this::applySearch));
        searchInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE) {
                searchInputController.clearFocusAndHideKeyboard();
                return true;
            }
            return false;
        });
        LinearLayout searchRow = SettingsRowBuilder.horizontal(context);
        searchRow.addView(searchInput, SettingsRowBuilder.weightedWrap(context, 1f, 0, 8));
        searchRow.addView(clearSearchButton, SettingsRowBuilder.fixedSize(context, 48, 48));
        chrome.addView(searchRow, SettingsRowBuilder.matchWrapWithTop(context, 10));

        statusLabel = SettingsRowBuilder.label(context, "");
        chrome.addView(statusLabel, SettingsRowBuilder.matchWrapWithTop(context, 12));

        stepScroller = new HorizontalScrollView(context);
        stepScroller.setHorizontalScrollBarEnabled(false);
        stepScroller.setFillViewport(false);
        stepButtonRow = SettingsRowBuilder.horizontal(context);
        stepScroller.addView(stepButtonRow, new HorizontalScrollView.LayoutParams(
                HorizontalScrollView.LayoutParams.WRAP_CONTENT,
                HorizontalScrollView.LayoutParams.WRAP_CONTENT));
        chrome.addView(stepScroller, SettingsRowBuilder.matchWrapWithTop(context, 8));

        LinearLayout navigation = SettingsRowBuilder.horizontal(context);
        previousButton = SettingsRowBuilder.button(
                context,
                R.string.settings_wizard_previous,
                view -> selectRelative(-1));
        nextButton = SettingsRowBuilder.button(
                context,
                R.string.settings_wizard_next,
                view -> selectRelative(1));
        modeButton = SettingsRowBuilder.button(
                context,
                R.string.settings_wizard_show_all,
                view -> toggleMode());
        navigation.addView(previousButton, SettingsRowBuilder.weightedWrap(context, 1f, 0, 4));
        navigation.addView(nextButton, SettingsRowBuilder.weightedWrap(context, 1f, 4, 4));
        navigation.addView(modeButton, SettingsRowBuilder.weightedWrap(context, 1f, 4, 0));
        chrome.addView(navigation, SettingsRowBuilder.matchWrapWithTop(context, 10));
    }

    LinearLayout addStep(int titleResId, int keywordsResId) {
        SettingsSectionCard card = SettingsSectionCard.create(
                context,
                context.getString(titleResId),
                true);
        card.setToggleEnabled(false);
        String keywords = keywordsResId == 0 ? "" : context.getString(keywordsResId);
        Step step = new Step(
                titleResId,
                context.getString(titleResId) + " " + keywords,
                card);
        steps.add(step);
        contentRoot.addView(card.container, SettingsRowBuilder.matchWrapWithTop(context, 12));
        return card.content;
    }

    void finishSetup() {
        for (Step step : steps) {
            step.refreshSearchableText(collectSearchableText(step.card.content));
        }
        rebuildStepButtons();
        selectStep(0);
    }

    void hideKeyboardWhenTouchingOutside(MotionEvent event) {
        searchInputController.hideKeyboardWhenTouchingOutside(event);
    }

    void saveState(Bundle outState) {
        if (outState == null) {
            return;
        }
        outState.putInt(STATE_SELECTED_INDEX, selectedIndex);
        outState.putBoolean(STATE_SHOW_ALL, showAll);
        outState.putString(STATE_SEARCH_QUERY, searchInput.getText().toString());
    }

    void restoreState(Bundle savedState) {
        if (savedState == null || steps.isEmpty()) {
            return;
        }
        selectedIndex = Math.max(0, Math.min(
                savedState.getInt(STATE_SELECTED_INDEX, 0),
                steps.size() - 1));
        String query = RuntimeDefaults.stringOrDefault(
                savedState.getString(STATE_SEARCH_QUERY),
                "");
        if (!query.isEmpty()) {
            searchInput.setText(query);
            searchInput.setSelection(query.length());
            return;
        }
        normalizedSearchQuery = "";
        showAll = savedState.getBoolean(STATE_SHOW_ALL, false);
        update();
        scrollContentToTop.run();
    }

    private void rebuildStepButtons() {
        stepButtons.clear();
        stepButtonRow.removeAllViews();
        for (int i = 0; i < steps.size(); i++) {
            int index = i;
            Button button = SettingsRowBuilder.button(
                    context,
                    context.getString(steps.get(i).titleResId),
                    false,
                    view -> selectStep(index));
            button.setSingleLine(true);
            button.setTextSize(14);
            button.setMinWidth(SettingsRowBuilder.dp(context, 92));
            stepButtons.add(button);
            stepButtonRow.addView(
                    button,
                    SettingsRowBuilder.wrapContentWithLeft(context, i == 0 ? 0 : 6));
        }
    }

    private void selectRelative(int delta) {
        int adjacent = adjacentMatchingIndex(delta);
        if (adjacent >= 0) {
            selectStep(adjacent);
        }
    }

    private void selectStep(int index) {
        if (steps.isEmpty()) {
            return;
        }
        selectedIndex = Math.max(0, Math.min(index, steps.size() - 1));
        showAll = false;
        update();
        scrollContentToTop.run();
    }

    private void toggleMode() {
        if (searchActive()) {
            return;
        }
        showAll = !showAll;
        update();
        scrollContentToTop.run();
    }

    private void applySearch(String query) {
        normalizedSearchQuery = SettingsWizardSearch.normalize(query);
        clearSearchButton.setVisibility(searchActive() ? View.VISIBLE : View.GONE);
        showAll = false;
        if (searchActive() && !matchesSearch(selectedIndex)) {
            int firstMatch = firstMatchingIndex();
            if (firstMatch >= 0) {
                selectedIndex = firstMatch;
            }
        }
        update();
        scrollContentToTop.run();
    }

    private void update() {
        if (steps.isEmpty()) {
            return;
        }
        int matchCount = 0;
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            boolean matches = matchesSearch(i);
            if (matches) {
                matchCount++;
            }
            SettingsSubsection.setSearchExpansion(
                    step.card.content,
                    searchActive() && matches);
            boolean visible = matches && (showAll || i == selectedIndex);
            step.card.container.setVisibility(visible ? View.VISIBLE : View.GONE);
            step.card.setExpanded(true);
            step.card.setWizardTitle(i + 1, steps.size());
            if (i < stepButtons.size()) {
                stepButtons.get(i).setVisibility(
                        !searchActive() || matches ? View.VISIBLE : View.GONE);
                SettingsViewStyler.button(stepButtons.get(i), context, !showAll && i == selectedIndex);
            }
        }
        if (searchActive()) {
            statusLabel.setText(matchCount == 0
                    ? context.getString(R.string.settings_search_empty)
                    : context.getString(
                            R.string.settings_search_result_format,
                            matchCount,
                            context.getString(steps.get(selectedIndex).titleResId)));
        } else {
            statusLabel.setText(showAll
                    ? context.getString(R.string.settings_wizard_all_steps_status_format, steps.size())
                    : context.getString(
                            R.string.settings_wizard_current_step_status_format,
                            selectedIndex + 1,
                            steps.size(),
                            context.getString(steps.get(selectedIndex).titleResId)));
        }
        previousButton.setEnabled(!showAll && adjacentMatchingIndex(-1) >= 0);
        nextButton.setEnabled(!showAll && adjacentMatchingIndex(1) >= 0);
        modeButton.setText(showAll
                ? R.string.settings_wizard_show_steps
                : R.string.settings_wizard_show_all);
        modeButton.setVisibility(searchActive() ? View.GONE : View.VISIBLE);
        if (!showAll
                && selectedIndex < stepButtons.size()
                && stepButtons.get(selectedIndex).getVisibility() == View.VISIBLE) {
            Button selected = stepButtons.get(selectedIndex);
            stepScroller.post(() -> stepScroller.smoothScrollTo(
                    Math.max(0, selected.getLeft() - SettingsRowBuilder.dp(context, 12)),
                    0));
        }
    }

    private boolean searchActive() {
        return !normalizedSearchQuery.isEmpty();
    }

    private boolean matchesSearch(int index) {
        return index >= 0
                && index < steps.size()
                && SettingsWizardSearch.matches(
                        steps.get(index).searchableText,
                        normalizedSearchQuery);
    }

    private int firstMatchingIndex() {
        for (int i = 0; i < steps.size(); i++) {
            if (matchesSearch(i)) {
                return i;
            }
        }
        return -1;
    }

    private int adjacentMatchingIndex(int delta) {
        if (delta == 0 || steps.isEmpty() || showAll) {
            return -1;
        }
        for (int index = selectedIndex + delta;
             index >= 0 && index < steps.size();
             index += delta) {
            if (matchesSearch(index)) {
                return index;
            }
        }
        return -1;
    }

    private static String collectSearchableText(View root) {
        StringBuilder searchableText = new StringBuilder();
        appendSearchableText(root, searchableText);
        return searchableText.toString();
    }

    private static void appendSearchableText(View view, StringBuilder destination) {
        if (view == null || destination == null) {
            return;
        }
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            appendText(destination, textView.getText());
            appendText(destination, textView.getHint());
        }
        appendText(destination, view.getContentDescription());
        if (!(view instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) view;
        for (int index = 0; index < group.getChildCount(); index++) {
            appendSearchableText(group.getChildAt(index), destination);
        }
    }

    private static void appendText(StringBuilder destination, CharSequence value) {
        if (value == null || value.length() == 0) {
            return;
        }
        if (destination.length() > 0) {
            destination.append(' ');
        }
        destination.append(value);
    }

    private static final class Step {
        final int titleResId;
        final String baseSearchableText;
        final SettingsSectionCard card;
        String searchableText;

        Step(int titleResId, String searchableText, SettingsSectionCard card) {
            this.titleResId = titleResId;
            this.baseSearchableText = RuntimeDefaults.stringOrEmpty(searchableText);
            this.searchableText = this.baseSearchableText;
            this.card = card;
        }

        void refreshSearchableText(String contentText) {
            searchableText = baseSearchableText + " " + RuntimeDefaults.stringOrEmpty(contentText);
        }
    }
}
