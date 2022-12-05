package com.flintcore.chat_app_android_22.firebase.firestore;

import com.google.firebase.firestore.EventListener;

import java.util.Map;

public interface ChatListener<L, O> {
    void setListener(O t1, O t2, Map<String, Object> whereArgs, EventListener<L> l);
}
