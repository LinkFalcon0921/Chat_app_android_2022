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
        SharedPreferences.Editor editor = this.getEditor();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void put(String key, String value) {
        SharedPreferences.Editor editor = this.getEditor();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean contains(String key){
        return this.sharedPreferences.contains(key);
    }

    public boolean getBoolean(String key) {
        return this.sharedPreferences.getBoolean(key, false);
    }

    public String getString(String key) {
        return this.sharedPreferences.getString(key, DEFAULT_STRING);
    }

    public void clear() {
        SharedPreferences.Editor editor = this.getEditor();
        editor.clear();
        editor.apply();
    }


    private SharedPreferences.Editor getEditor() {
        return this.sharedPreferences.edit();
    }

    @Deprecated
    private void applyChanges() {
        this.sharedPreferences.edit().apply();
    }
}

