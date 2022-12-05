package com.flintcore.chat_app_android_22.utilities.callback;

import androidx.annotation.Nullable;

import java.util.Map;

@FunctionalInterface
public interface Call {
    void start(@Nullable Map<String, Object> data);
}
