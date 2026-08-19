package com.superl3.s3keyboard;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

final class SettingsArrayAdapter<T> extends ArrayAdapter<T> {
    SettingsArrayAdapter(Context context, T[] items) {
        super(context, android.R.layout.simple_spinner_item, items);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return style(super.getView(position, convertView, parent), false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return style(super.getDropDownView(position, convertView, parent), true);
    }

    private View style(View view, boolean dropdown) {
        SettingsUiPalette ui = SettingsUiPalette.from(getContext());
        view.setBackgroundColor(dropdown ? ui.surfaceRaised : Color.TRANSPARENT);
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            text.setTextColor(dropdown ? ui.textPrimary : ui.controlText);
            text.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            text.setMinHeight(SettingsRowBuilder.dp(getContext(), 44));
            text.setPadding(
                    SettingsRowBuilder.dp(getContext(), 14),
                    0,
                    SettingsRowBuilder.dp(getContext(), 14),
                    0);
        }
        return view;
    }
}
