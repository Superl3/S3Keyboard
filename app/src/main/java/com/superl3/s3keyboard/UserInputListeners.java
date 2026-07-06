package com.superl3.s3keyboard;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

final class UserInputListeners {
    private UserInputListeners() {
    }

    static CompoundButton.OnCheckedChangeListener checked(
            BooleanSupplier canHandleUserChange,
            Consumer<Boolean> onUserCheckedChanged) {
        BooleanSupplier safeCanHandleUserChange =
                RuntimeDefaults.trueBooleanSupplier(canHandleUserChange);
        Consumer<Boolean> safeOnUserCheckedChanged =
                RuntimeDefaults.booleanConsumer(onUserCheckedChanged);
        return (buttonView, isChecked) -> {
            if (safeCanHandleUserChange.getAsBoolean()) {
                safeOnUserCheckedChanged.accept(isChecked);
            }
        };
    }

    static AdapterView.OnItemSelectedListener itemSelected(
            BooleanSupplier canHandleSelection,
            IntConsumer onUserItemSelected) {
        return itemSelected(canHandleSelection, onUserItemSelected, false);
    }

    static AdapterView.OnItemSelectedListener itemSelectedAfterInitialSelection(
            IntConsumer onUserItemSelected) {
        return itemSelected(() -> true, onUserItemSelected, true);
    }

    static SeekBar.OnSeekBarChangeListener seekBar(
            BooleanSupplier canHandleUserChange,
            IntConsumer onUserProgressChanged) {
        BooleanSupplier safeCanHandleUserChange =
                RuntimeDefaults.trueBooleanSupplier(canHandleUserChange);
        IntConsumer safeOnUserProgressChanged =
                RuntimeDefaults.intConsumer(onUserProgressChanged);
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && safeCanHandleUserChange.getAsBoolean()) {
                    safeOnUserProgressChanged.accept(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
    }

    static TextWatcher text(
            BooleanSupplier canHandleUserChange,
            Consumer<String> onUserTextChanged) {
        BooleanSupplier safeCanHandleUserChange =
                RuntimeDefaults.trueBooleanSupplier(canHandleUserChange);
        Consumer<String> safeOnUserTextChanged =
                RuntimeDefaults.stringConsumer(onUserTextChanged);
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!safeCanHandleUserChange.getAsBoolean()) {
                    return;
                }
                safeOnUserTextChanged.accept(RuntimeDefaults.stringOrEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private static AdapterView.OnItemSelectedListener itemSelected(
            BooleanSupplier canHandleSelection,
            IntConsumer onUserItemSelected,
            boolean skipInitialSelection) {
        BooleanSupplier safeCanHandleSelection =
                RuntimeDefaults.trueBooleanSupplier(canHandleSelection);
        IntConsumer safeOnUserItemSelected =
                RuntimeDefaults.intConsumer(onUserItemSelected);
        return new AdapterView.OnItemSelectedListener() {
            private boolean skipNextSelection = skipInitialSelection;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (skipNextSelection) {
                    skipNextSelection = false;
                    return;
                }
                if (safeCanHandleSelection.getAsBoolean()) {
                    safeOnUserItemSelected.accept(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
    }
}
