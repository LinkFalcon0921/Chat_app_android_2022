package com.flintcore.chat_app_android_22.utilities.callback;

@FunctionalInterface
public interface CallResult<TResult> {
    void onCall(TResult r);
}
