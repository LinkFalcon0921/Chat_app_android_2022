package com.flintcore.chat_app_android_22.firebase.firestore.chatMessages;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.firestore.IFirestoreCollection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;

public interface IChatMessageCollection extends IFirestoreCollection<ChatMessage, Exception> {

     <K extends String, V extends Object> void applyChatListener(@NonNull Collection<QueryCondition<K, V>> whereConditions,
                                                                 EventListener<QuerySnapshot> l);

}
