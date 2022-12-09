package com.flintcore.chat_app_android_22.firebase.firestore;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public interface IFirestoreCollection<T, E extends Exception> {

    <K extends String, V extends Object>
    void addCollectionById(@NonNull T t, @NonNull List<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Void> onSuccess,
                           CallResult<E> onFailListener);

    <K extends String, V extends Object>
    void getCollectionById(@NonNull T t,
                           @NonNull CallResult<Task<DocumentSnapshot>> onCompleteListener,
                           CallResult<Exception> onFailListener);

    <K extends String, V extends Object>
    void getCollectionById(@NonNull T t,
                           @NonNull List<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                           CallResult<Exception> onFailListener);

    <K extends String, V extends Object>
    void getCollections(@NonNull List<QueryCondition<K, V>> whereConditions,
                        @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                        CallResult<E> onFailListener);

    <K extends String, V extends Object>
    void deleteCollection(@NonNull T t, @NonNull List<QueryCondition<K, V>> whereConditions,
                          @NonNull CallResult<Task<Void>> onCompleteListener,
                          CallResult<E> onFailListener);
}
