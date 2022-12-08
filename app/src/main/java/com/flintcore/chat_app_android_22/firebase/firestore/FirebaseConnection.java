package com.flintcore.chat_app_android_22.firebase.firestore;

import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

// TODO REFACTOR ALL Firebase Connection class


public abstract class FirebaseConnection<ID, T> {

    public static final String DEFAULT_ORDER_BY_FIELD = FieldPath.documentId().toString();

    protected CollectionReference collection;
    private final String DEFAULT_ORDER;

    public FirebaseConnection() {
        this.DEFAULT_ORDER = FirebaseConnection.DEFAULT_ORDER_BY_FIELD;
    }

    public FirebaseConnection(String DEFAULT_ORDER_BY_FIELD) {
        this.DEFAULT_ORDER = DEFAULT_ORDER_BY_FIELD;
    }

    @SafeVarargs
    public  final <K extends String, V extends Object & List<Object>>
        Query getFirebaseQuery(@NotNull Collection<QueryCondition<K, V>> queries)
    {
        return getFirebaseQuery(queries.toArray(new QueryCondition[0]));
    }

    @SafeVarargs
    public  final <K extends String, V extends Object & List<Object>>
        Query getFirebaseQuery(@NotNull QueryCondition<K, V>... queries)
    {

        Query query = this.collection.orderBy(DEFAULT_ORDER);

        for (QueryCondition<K, V> condition : queries) {
            switch (condition.getMatchType()) {
                case IN:
                    query = this.collection.whereIn(condition.getKey(), condition.getValue());
                    break;

                case NOT_EQUALS:
                    query = query.whereNotEqualTo(condition.getKey(), condition.getValue());
                    break;

                default:
                case EQUALS:
                    query = query.whereEqualTo(condition.getKey(), condition.getValue());
            }

        }

        return query;
    }
}
