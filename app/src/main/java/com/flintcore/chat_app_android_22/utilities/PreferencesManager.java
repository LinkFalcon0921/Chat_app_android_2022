package com.flintcore.chat_app_android_22.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    public static final String DEFAULT_STRING = "Empty";

    private final SharedPreferences sharedPreferences;

    public PreferencesManager(Context context, String keyReference) {
        this.sharedPreferences = context.getSharedPreferences(keyReference, Context.MODE_PRIVATE);
    }

    public void put(String key, boolean value) {
        this.getEditor().putBoolean(key, value);
        this.applyChanges();
    }

    public void put(String key, String value) {
        this.getEditor().putString(key, value);
        this.applyChanges();
    }

    public boolean getBoolean(String key) {
        return this.sharedPreferences.getBoolean(key, false);
    }

    public String getString(String key) {
        return this.sharedPreferences.getString(key, DEFAULT_STRING);
    }

    public void clear() {
        this.getEditor().clear();
        this.applyChanges();
    }


    private SharedPreferences.Editor getEditor() {
        return this.sharedPreferences.edit();
    }

    private void applyChanges() {
        this.sharedPreferences.edit().apply();
    }
}

