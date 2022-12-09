package com.flintcore.chat_app_android_22.firebase.queries;

import androidx.annotation.NonNull;

public class QueryConditionImplement<K, V> implements QueryCondition<K, V> {

    protected final MatchType matchType;
    @NonNull
    protected final K key;
    @NonNull
    protected final V value;

    protected QueryConditionImplement(MatchType matchType, K key, V value) {
        this.matchType = matchType;
        this.key = key;
        this.value = value;
    }

    @NonNull
    @Override
    public MatchType getMatchType() {
        return this.matchType;
    }

    @NonNull
    @Override
    public K getKey() {
        return this.key;
    }

    @NonNull
    @Override
    public V getValue() {
        return this.value;
    }



}
