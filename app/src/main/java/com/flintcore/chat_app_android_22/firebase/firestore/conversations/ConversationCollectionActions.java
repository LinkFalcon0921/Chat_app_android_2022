package com.flintcore.chat_app_android_22.firebase.firestore.conversations;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

@Deprecated
public interface ConversationCollectionActions<ID> {

    void getCollection(ChatMessage conversation, OnCompleteListener<QuerySnapshot> onSuccess,
                       Call onFail);

    void addCollection(Conversation conversation, OnSuccessListener<DocumentReference> onSuccess);

    void updateCollection(Conversation conversation, CallResult<Void> onSuccess, Call onFail);

    void applyCollectionListener(Map<Object, Object> whereArgs, @NonNull EventListener<QuerySnapshot> l);

    void applyCollectionListener(OnCompleteListener<QuerySnapshot> onComplete);
}
