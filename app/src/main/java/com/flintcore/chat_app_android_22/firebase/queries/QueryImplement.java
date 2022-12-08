package com.flintcore.chat_app_android_22.firebase.queries;

import androidx.annotation.NonNull;

abstract class QueryImplement<K, V> implements QueryCondition<K, V> {

    protected final MatchType matchType;
    @NonNull
    protected final K key;
    @NonNull
    protected final V value;

    protected QueryImplement(MatchType matchType, K key, V value) {
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

    public static <T extends MatchType ,K, V> QueryCondition<K, V> create(T t, @NonNull K k, @NonNull V v){
        return new QueryImplement<K, V>(t, k, v) {
            @NonNull
            @Override
            public MatchType getMatchType() {
                return super.getMatchType();
            }

            @NonNull
            @Override
            public K getKey() {
                return super.getKey();
            }

            @NonNull
            @Override
            public V getValue() {
                return super.getValue();
            }
        };
    }

}
