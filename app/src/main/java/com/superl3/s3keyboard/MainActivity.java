package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    static final int REQUEST_EDIT_LAYOUT = 1001;

    private KeyboardSettings settings;
    private KeyboardLayoutProfiles layoutProfiles;
    private KeyboardErgonomicsOptions ergonomicsOptions = KeyboardErgonomicsOptions.DEFAULT;
    private AndroidImeSettingsController androidImeSettingsController;
    private LayoutSettingsController layoutSettingsController;
    private InputAssistanceSettingsController inputAssistanceSettingsController;
    private InputFeelSettingsController inputFeelSettingsController;
    private OneFingerInputSettingsController oneFingerInputSettingsController;
    private RemoteWindowsSettingsController remoteWindowsSettingsController;
    private TypographySettingsController typographySettingsController;
    private DisplayStyleSettingsController displayStyleSettingsController;
    private MotionEffectSettingsController motionEffectSettingsController;
    private boolean demoShowKeyboard;
    private boolean demoOverlayTestbed;
    private boolean demoWearTestbed;
    private DemoFieldProfile demoFieldProfile = DemoFieldProfile.STANDARD;
    private SettingsHubController settingsHubController;
    private SettingsWizardController settingsWizardController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsSystemBars.apply(this);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        loadCurrentPreferences();
        int softInputMode = demoShowKeyboard
                ? WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                : WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
        if (demoOverlayTestbed) {
            softInputMode |= WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        }
        getWindow().setSoftInputMode(softInputMode);
        setContentView(createContentView());
        if (settingsWizardController != null) {
            settingsWizardController.restoreState(savedInstanceState);
        }
        syncControls();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (settingsWizardController != null) {
            settingsWizardController.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentPreferences();
        syncControls();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (settingsHubController != null) {
            settingsHubController.hideKeyboardWhenTouchingOutside(event);
        }
        if (settingsWizardController != null) {
            settingsWizardController.hideKeyboardWhenTouchingOutside(event);
        }
        if (oneFingerInputSettingsController != null) {
            oneFingerInputSettingsController.hideKeyboardWhenTouchingOutside(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void applyIntentOverrides(Intent intent) {
        DemoSettingsIntentOverrides.Result result = DemoSettingsIntentOverrides.apply(
                this,
                intent,
                settings,
                demoFieldProfile,
                demoShowKeyboard,
                isDebuggableBuild());
        settings = result.settings;
        demoFieldProfile = result.fieldProfile;
        demoShowKeyboard = result.showKeyboard;
        demoOverlayTestbed = result.overlayTestbed;
        demoWearTestbed = result.wearTestbed;
    }

    private void loadCurrentPreferences() {
        settings = KeyboardPreferences.load(this);
        layoutProfiles = KeyboardPreferences.loadLayoutProfiles(this);
        ergonomicsOptions = KeyboardPreferences.loadErgonomicsOptions(this);
        applyIntentOverrides(getIntent());
    }

    private View createContentView() {
        if (demoOverlayTestbed) {
            return TransparentOverlayTestbedView.create(this, demoShowKeyboard);
        }
        if (demoWearTestbed) {
            return new WearOnePressTestbedView(this);
        }
        int padding = SettingsRowBuilder.dp(this, 16);
        SettingsUiPalette ui = SettingsUiPalette.from(this);
        LinearLayout page = SettingsRowBuilder.vertical(this);
        page.setBackgroundColor(ui.background);
        page.setFocusableInTouchMode(true);
        SettingsSystemBars.applyTopInset(page);
        if (!demoShowKeyboard) {
            page.requestFocus();
        }

        TextView title = SettingsRowBuilder.label(this, R.string.app_name);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setPadding(padding, padding, padding, 0);
        LinearLayout contentRoot = SettingsRowBuilder.vertical(this);
        contentRoot.setGravity(Gravity.CENTER_HORIZONTAL);
        contentRoot.setPadding(padding, 0, padding, padding);
        if (usesCompactHeightSettingsLayout()) {
            ScrollView pageScroll = new ScrollView(this);
            pageScroll.setFillViewport(true);
            pageScroll.setBackgroundColor(ui.background);
            LinearLayout scrollingPage = SettingsRowBuilder.vertical(this);
            scrollingPage.addView(title, SettingsRowBuilder.matchWrap());
            settingsWizardController = new SettingsWizardController(
                    this,
                    scrollingPage,
                    contentRoot,
                    () -> pageScroll.post(() -> pageScroll.scrollTo(0, contentRoot.getTop())));
            scrollingPage.addView(contentRoot, SettingsRowBuilder.matchWrap());
            pageScroll.addView(scrollingPage);
            page.addView(pageScroll, SettingsRowBuilder.matchWeightedFill());
        } else {
            page.addView(title, SettingsRowBuilder.matchWrap());
            ScrollView contentScroll = new ScrollView(this);
            contentScroll.setFillViewport(true);
            contentScroll.setBackgroundColor(ui.background);
            contentScroll.addView(contentRoot);
            settingsWizardController = new SettingsWizardController(
                    this,
                    page,
                    contentRoot,
                    () -> contentScroll.scrollTo(0, 0));
            page.addView(contentScroll, SettingsRowBuilder.matchWeightedFill());
        }

        LinearLayout hubSection = addWizardStep(
                R.string.settings_hub_title,
                R.string.settings_search_keywords_hub);
        settingsHubController = new SettingsHubController(
                this,
                this::settings,
                this::markCurrentThemeCustom,
                this::saveSettings);
        settingsHubController.addTo(hubSection, demoFieldProfile, demoShowKeyboard);
        LinearLayout oneFingerSection = addWizardStep(
                R.string.settings_one_finger_section,
                R.string.settings_search_keywords_one_finger);
        oneFingerInputSettingsController = new OneFingerInputSettingsController(
                this,
                this::syncControls);
        oneFingerInputSettingsController.addTo(oneFingerSection);

        LinearLayout layoutSection = addWizardStep(
                R.string.settings_layout_section,
                R.string.settings_search_keywords_layout);
        layoutSettingsController = new LayoutSettingsController(
                this,
                this::settings,
                this::ergonomicsOptions,
                this::markCurrentThemeCustom,
                this::saveSettings,
                this::saveHangulLayoutProfile,
                this::saveEnglishLayoutProfile,
                this::saveDingulDotEnterKeyEnabled,
                this::saveErgonomicsOptions,
                this::syncControls);
        layoutSettingsController.addTo(layoutSection);

        LinearLayout inputSection = addWizardStep(
                R.string.settings_input_feel_section,
                R.string.settings_search_keywords_input);
        inputFeelSettingsController = new InputFeelSettingsController(
                this,
                this::settings,
                this::saveSettings,
                this::syncControls);
        inputFeelSettingsController.addTo(inputSection);

        LinearLayout displaySection = addWizardStep(
                R.string.settings_display_section,
                R.string.settings_search_keywords_display);
        addVisibleVisualControls(displaySection);

        LinearLayout reservedSection = addWizardStep(
                R.string.settings_reserved_phrase_section,
                R.string.settings_search_keywords_reserved);
        new ReservedPhraseSettingsController(this).addTo(reservedSection);

        LinearLayout remoteSection = addWizardStep(
                R.string.settings_remote_windows_section,
                R.string.settings_search_keywords_remote);
        remoteWindowsSettingsController = new RemoteWindowsSettingsController(
                this,
                this::settings,
                this::saveSettings,
                this::syncControls);
        remoteWindowsSettingsController.addTo(remoteSection);

        LinearLayout androidSection = addWizardStep(
                R.string.settings_android_ime_section,
                R.string.settings_search_keywords_android);
        androidImeSettingsController =
                new AndroidImeSettingsController(this, this::isDebuggableBuild, this::syncControls);
        androidImeSettingsController.addTo(androidSection);
        SettingsRowBuilder.buttonRow(
                this,
                androidSection,
                R.string.settings_backup_restore,
                12,
                view -> startActivity(new Intent(this, BackupRestoreActivity.class)));

        settingsWizardController.finishSetup();
        return page;
    }

    private boolean usesCompactHeightSettingsLayout() {
        return demoShowKeyboard || getResources().getConfiguration().screenHeightDp < 520;
    }

    private void addVisibleVisualControls(LinearLayout root) {
        LinearLayout keySection = SettingsSubsection.add(
                this,
                root,
                R.string.settings_display_keys_subsection,
                true).content;
        displayStyleSettingsController = new DisplayStyleSettingsController(
                this,
                this::settings,
                this::markCurrentThemeCustom,
                this::saveSettings);
        displayStyleSettingsController.addPackControlsTo(keySection);
        displayStyleSettingsController.addPointKeycapControlTo(keySection);

        LinearLayout typographySection = SettingsSubsection.add(
                this,
                root,
                R.string.settings_display_typography_subsection,
                false).content;
        typographySettingsController = new TypographySettingsController(
                this,
                this::settings,
                this::saveSettings);
        typographySettingsController.addTo(typographySection);

        LinearLayout hintsSection = SettingsSubsection.add(
                this,
                root,
                R.string.settings_display_hints_subsection,
                false).content;
        inputAssistanceSettingsController = new InputAssistanceSettingsController(
                this,
                this::settings,
                this::ergonomicsOptions,
                this::isDebuggableBuild,
                this::saveSettings,
                this::saveSettingsAndErgonomics,
                this::syncControls);
        inputAssistanceSettingsController.addTo(hintsSection);

        LinearLayout motionSection = SettingsSubsection.add(
                this,
                root,
                R.string.settings_display_motion_subsection,
                false).content;
        motionEffectSettingsController = new MotionEffectSettingsController(this, this::syncControls);
        motionEffectSettingsController.addTo(motionSection);

    }

    private KeyboardSettings settings() {
        return settings;
    }

    private KeyboardErgonomicsOptions ergonomicsOptions() {
        return ergonomicsOptions;
    }

    private void saveSettings(KeyboardSettings newSettings) {
        settings = newSettings;
        saveAndSync();
    }

    private void saveSettingsAndErgonomics(
            KeyboardSettings newSettings,
            KeyboardErgonomicsOptions newErgonomicsOptions) {
        settings = newSettings;
        ergonomicsOptions = newErgonomicsOptions;
        KeyboardPreferences.saveErgonomicsOptions(this, ergonomicsOptions);
        saveAndSync();
    }

    private void saveHangulLayoutProfile(KeyboardLayoutProfile profile) {
        layoutProfiles = layoutProfiles.withHangulLayout(profile);
        KeyboardPreferences.saveHangulLayoutProfile(this, profile);
    }

    private void saveEnglishLayoutProfile(KeyboardLayoutProfile profile) {
        layoutProfiles = layoutProfiles.withEnglishLayout(profile);
        KeyboardPreferences.saveEnglishLayoutProfile(this, profile);
    }

    private void saveDingulDotEnterKeyEnabled(boolean enabled) {
        layoutProfiles = layoutProfiles.withDingulDotEnterKeyEnabled(enabled);
        KeyboardPreferences.saveDingulDotEnterKeyEnabled(this, enabled);
    }

    private void saveErgonomicsOptions(KeyboardErgonomicsOptions options) {
        ergonomicsOptions = options;
        saveErgonomicsAndSync();
    }

    private void saveAndSync() {
        settings = KeyboardPreferences.applyAccentPlacementPolicy(this, settings);
        KeyboardPreferences.saveSettings(this, settings);
        syncControls();
    }

    private void saveErgonomicsAndSync() {
        KeyboardPreferences.saveErgonomicsOptions(this, ergonomicsOptions);
        syncControls();
    }

    private void syncControls() {
        if (layoutSettingsController == null) {
            return;
        }

        layoutSettingsController.sync(settings, layoutProfiles);
        if (settingsHubController != null) {
            settingsHubController.sync();
        }
        if (androidImeSettingsController != null) {
            androidImeSettingsController.sync();
        }
        if (inputAssistanceSettingsController != null) {
            inputAssistanceSettingsController.sync(settings);
        }
        if (inputFeelSettingsController != null) {
            inputFeelSettingsController.sync(settings);
        }
        if (oneFingerInputSettingsController != null) {
            oneFingerInputSettingsController.sync();
        }
        if (remoteWindowsSettingsController != null) {
            remoteWindowsSettingsController.sync(settings);
        }
        if (typographySettingsController != null) {
            typographySettingsController.sync(settings);
        }
        if (displayStyleSettingsController != null) {
            displayStyleSettingsController.sync(settings);
        }
        if (motionEffectSettingsController != null) {
            motionEffectSettingsController.sync();
        }
    }

    private LinearLayout addWizardStep(int titleResId, int keywordsResId) {
        return settingsWizardController.addStep(titleResId, keywordsResId);
    }

    private boolean isDebuggableBuild() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_LAYOUT && resultCode == RESULT_OK) {
            loadCurrentPreferences();
            syncControls();
        }
    }

    private void markCurrentThemeCustom() {
        KeyboardPreferences.saveSelectedThemeId(this, "");
    }

}
