package com.flintcore.chat_app_android_22.firebase.firestore.users;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public interface IUserCollection<E extends Exception> {

    <K extends String, V extends Object>
    void addCollectionById(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Void> onCompleteListener,
                           CallResult<E> onFailListener);

    <K extends String, V extends Object>
    void getCollectionById(@NonNull User user,
                           @NonNull List<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Task<DocumentSnapshot>> onCompleteListener,
                           CallResult<E> onFailListener);

    <K extends String, V extends Object>
    void getCollections(@NonNull List<QueryCondition<K, V>> whereConditions,
                        @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                        CallResult<E> onFailListener);

    <K extends String, V extends Object>
    void deleteCollection(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                          @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                          CallResult<E> onFailListener);

    <K extends String, V extends Object>
    void updateAvailability(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                            @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                            CallResult<E> onFailListener);

    <K extends String, V extends Object>
    void updateToken(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                     @NonNull CallResult<String> onCompleteListener,
                     CallResult<E> onFailListener);

    <K extends String, V extends Object>
    void applyUserListener(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                     @NonNull EventListener<QuerySnapshot> l,
                     CallResult<E> onFailListener);

}
