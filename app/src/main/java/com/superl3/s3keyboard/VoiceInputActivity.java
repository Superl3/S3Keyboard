package com.superl3.s3keyboard;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.speech.RecognizerIntent;

import java.util.ArrayList;

public final class VoiceInputActivity extends Activity {
    private static final int REQUEST_RECOGNIZE_SPEECH = 1;

    private ResultReceiver receiver;
    private boolean resultDelivered;

    static Intent intent(Context context, ResultReceiver receiver, String languageTag) {
        Intent intent = new Intent(context, VoiceInputActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(VoiceInputResult.EXTRA_RECEIVER, receiver);
        intent.putExtra(VoiceInputResult.EXTRA_LANGUAGE_TAG, languageTag);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = readReceiver(getIntent());
        if (receiver == null) {
            finish();
            return;
        }
        if (savedInstanceState == null) {
            launchRecognizer();
        }
    }

    private void launchRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_input_prompt));
        String languageTag = getIntent().getStringExtra(VoiceInputResult.EXTRA_LANGUAGE_TAG);
        if (languageTag != null && !languageTag.isEmpty()) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag);
        }
        if (intent.resolveActivity(getPackageManager()) == null) {
            deliver(VoiceInputResult.UNAVAILABLE, "");
            finish();
            return;
        }
        try {
            startActivityForResult(intent, REQUEST_RECOGNIZE_SPEECH);
        } catch (ActivityNotFoundException exception) {
            deliver(VoiceInputResult.UNAVAILABLE, "");
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_RECOGNIZE_SPEECH) {
            return;
        }
        if (resultCode != RESULT_OK || data == null) {
            deliver(VoiceInputResult.CANCELLED, "");
            finish();
            return;
        }
        ArrayList<String> candidates =
                data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String text = VoiceInputResult.firstRecognizedText(candidates);
        deliver(text.isEmpty() ? VoiceInputResult.NO_MATCH : VoiceInputResult.RECOGNIZED, text);
        finish();
    }

    private void deliver(int resultCode, String text) {
        if (resultDelivered || receiver == null) {
            return;
        }
        resultDelivered = true;
        Bundle data = new Bundle();
        data.putString(VoiceInputResult.EXTRA_TEXT, text);
        receiver.send(resultCode, data);
    }

    @SuppressWarnings("deprecation")
    private static ResultReceiver readReceiver(Intent intent) {
        if (intent == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= 33) {
            return intent.getParcelableExtra(
                    VoiceInputResult.EXTRA_RECEIVER,
                    ResultReceiver.class);
        }
        return intent.getParcelableExtra(VoiceInputResult.EXTRA_RECEIVER);
    }
}
