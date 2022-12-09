package com.flintcore.chat_app_android_22.firebase.firestore;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.models.wrappers.ModelWrapper;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Query;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

// TODO REFACTOR ALL Firebase Connection class

/**Class necessaries to handle specific fields in the database.
 * <br/> - FieldPath and FieldValue
 * */
public abstract class FirebaseConnection {
    public static final String DOCUMENT_ID = FieldPath.documentId().toString();
    public static final String DEFAULT_ORDER_BY_FIELD = FieldPath.documentId().toString();

    private final String DEFAULT_ORDER;
    protected CollectionReference collection;
    protected ModelWrapper wrapper;

    public FirebaseConnection() {
        this(DEFAULT_ORDER_BY_FIELD);
    }

    public FirebaseConnection(String DEFAULT_ORDER_BY_FIELD) {
        this.DEFAULT_ORDER = DEFAULT_ORDER_BY_FIELD;
        this.wrapper = ModelWrapper.getInstance();
    }

    @NonNull
    protected final <K extends String, V extends Object>
        Query getFirebaseQuery(@NotNull Collection<QueryCondition<K, V>> queries) {

        Query query = this.collection.orderBy(DEFAULT_ORDER);

        for (QueryCondition<K, V> condition : queries) {
            switch (condition.getMatchType()) {
                case IN:
                    query = this.collection.whereIn(condition.getKey(), (List) condition.getValue());
                    break;

                case NOT_EQUALS:
                    query = query.whereNotEqualTo(condition.getKey(), condition.getValue());
                    break;

                case ARRAY_IN_ANY:
                    query = query.whereArrayContainsAny(condition.getKey(), (List<? extends Object>) condition.getValue());
                    break;

                case ARRAY_IN:
                    query = query.whereArrayContains(condition.getKey(), condition.getValue());

                default:
                case EQUALS:
                    query = query.whereEqualTo(condition.getKey(), condition.getValue());
            }

        }

        return query;
    }

    @NonNull
    protected <K extends String, V extends Object>
    Query getFirebaseQueryWithId(String userId, @NonNull Collection<QueryCondition<K, V>> whereConditions) {
        return this.getFirebaseQuery(whereConditions)
                .whereEqualTo(DOCUMENT_ID, userId);
    }

    @SafeVarargs
    @NonNull
    public  final <K extends String, V extends Object>
        Query getFirebaseQuery(@NotNull QueryCondition<K, V>... queries) {
        return getFirebaseQuery(Arrays.asList(queries));
    }
}
