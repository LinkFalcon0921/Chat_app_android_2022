package com.flintcore.chat_app_android_22.utilities.callback;

import java.util.Map;

@FunctionalInterface
public interface Call {
    void start(Map<String, Object> data);
}
