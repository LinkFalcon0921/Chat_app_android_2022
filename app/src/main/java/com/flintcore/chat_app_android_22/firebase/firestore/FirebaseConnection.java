package com.flintcore.chat_app_android_22.firebase.firestore;

import android.app.Activity;

import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.google.firebase.firestore.CollectionReference;

import java.util.Set;

public abstract class FirebaseConnection<ID, T> {
    protected CollectionReference collection;

    public abstract Set<T> getCollections();

    public abstract T getCollection(ID id);

    public abstract T getCollection(String[] keys, Object[] values);

    public abstract void addCollection(T t, Call onSuccess, Call onFail);

    public abstract void editCollection(T t, Call onSuccess, Call onFail);

    public abstract void deleteCollection(ID id, Call onSuccess, Call onFail);

    public abstract void deleteCollection(String[] keys, Object[] values, Call onSuccess, Call onFail);

}
