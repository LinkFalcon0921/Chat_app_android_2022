package com.flintcore.chat_app_android_22.utilities.callback;

import androidx.annotation.NonNull;

@FunctionalInterface
public interface CallResult<TResult> {
    void onCall(@NonNull TResult r);
}
