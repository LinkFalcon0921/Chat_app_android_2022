package com.flintcore.chat_app_android_22.firebase.firestore.conversations;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.firestore.IFirestoreCollection;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.Map;

public interface IConversationCollection<T, C, K> extends IFirestoreCollection<T, Exception> {

    void update(T t, CallResult<Task<Void>> onComplete, CallResult<Exception> onFailListener);

    <K extends String, V extends Object>
    void getCollectionByLastChatMessage(@NonNull C c,
                           @NonNull Collection<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                           CallResult<Exception> onFailListener);

    <K extends String, V extends Object>
    void applyCollectionListener(Collection<QueryCondition<K, V>> whereArgs,
                                 @NonNull EventListener<QuerySnapshot> l);
}
