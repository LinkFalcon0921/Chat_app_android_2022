package com.flintcore.chat_app_android_22.firebase.queries;

import androidx.annotation.NonNull;

/**For query firebase collection*/
public interface QueryCondition<K, V> {
    @NonNull
    MatchType getMatchType();

    @NonNull
    K getKey();

    @NonNull
    V getValue();

    enum MatchType{
        EQUALS,
        NOT_EQUALS,
        IN
    }
}
