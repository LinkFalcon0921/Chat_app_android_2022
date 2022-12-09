package com.flintcore.chat_app_android_22.firebase.firestore.users;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.firestore.IFirestoreCollection;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IUserCollection<T> extends IFirestoreCollection<T,Exception> {

    <K extends String, V extends Object>
    void updateAvailability(@NonNull T t, @NonNull Collection<QueryCondition<K, V>> whereConditions,
                            @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                            CallResult<Exception> onFailListener);

    <K extends String, V extends Object>
    void appendToken(@NonNull T t,
                     @NonNull CallResult<String> onCompleteListener,
                     CallResult<Exception> onFailListener);

    <K extends String, V extends Object>
    void applyUserListener(@NonNull T t, @NonNull Collection<QueryCondition<K, V>> whereConditions,
                           @NonNull EventListener<QuerySnapshot> l,
                           CallResult<Exception> onFailListener);

    void clearToken(User t, CallResult<Task<Void>> onCompleteListener, CallResult<Exception> onFailListener);

    <K extends String, V extends Object>
    void applyUserAvailability(@NonNull Collection<QueryCondition<K, V>> whereConditions,
                               EventListener<QuerySnapshot> l);

}
