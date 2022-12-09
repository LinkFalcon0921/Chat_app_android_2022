package com.flintcore.chat_app_android_22.firebase.queries;

import androidx.annotation.NonNull;

/**
 * For query firebase collection
 */
public interface QueryCondition<K, V> {
    @NonNull
    MatchType getMatchType();

    @NonNull
    K getKey();

    @NonNull
    V getValue();

    enum MatchType {
        EQUALS,
        NOT_EQUALS,
        /**Multiple values in a single field*/
        IN,
        ARRAY_IN,
        ARRAY_IN_ANY
    }

    class Builder<K, V> {
        private MatchType type;
        private K key;
        private V value;

        public Builder() {
        }

        public Builder<K, V> setKey(K k) {
            this.key = k;
            return this;
        }

        public Builder<K, V> setValue(V v) {
            this.value = v;
            return this;
        }


        public Builder<K, V> setMatchType(MatchType type) {
            this.type = type;
            return this;
        }

        public QueryCondition<K, V> build(){
            return new QueryConditionImplement<>(this.type, this.key, this.value);
        }
    }
}
