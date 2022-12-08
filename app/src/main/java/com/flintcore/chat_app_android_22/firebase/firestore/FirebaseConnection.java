package com.flintcore.chat_app_android_22.firebase.firestore;

import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.firebase.firestore.CollectionReference;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class FirebaseConnection<ID, T> {
    protected CollectionReference collection;

    public abstract void getCollections(Call onSuccess, Call onFail);
    public abstract void getCollections(ID id, Call onSuccess, Call onFail);

    public abstract void getCollection(ID id, Call onSuccess, Call onFail);
//    public abstract void getCollection(ID id, CallResult onSuccess, CallResult onFail);

    public abstract void getCollection(Map<String, Object> whereArgs, Call onSuccess, Call onFail);

    public abstract void addCollection(T t, Call onSuccess, Call onFail);

    public abstract void editCollection(T t, Call onSuccess, Call onFail);

    public abstract void deleteCollection(ID id, Call onSuccess, Call onFail);

    public abstract void deleteCollection(String[] keys, Object[] values, Call onSuccess, Call onFail);

    public  abstract void updateToken(ID id, Call onSuccess, Call onFail);
    public  abstract void clearToken(ID id, Call onSuccess, Call onFail);
}
