package com.flintcore.chat_app_android_22.firebase.firestore;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages.KEY_RECEIVED;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages.KEY_SENDER;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_COLLECTION;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_CONVERSATION_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_LAST_DATE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_LAST_MESSAGE_ID;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.Conversation;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConversationCollection implements ConversationCollectionActions<String>{

    private static ConversationCollection conversationInstance;
    private CollectionReference collection;

    private ConversationCollection(Call onFail) {
        try {
            this.collection = FirebaseFirestore.getInstance()
                    .collection(KEY_COLLECTION);

        } catch (Exception ex) {
            callOnFail(onFail, ex);
        }
    }

    public static ConversationCollection getConversationInstance(Call onFail) {
        if (Objects.isNull(conversationInstance)) {
            conversationInstance = new ConversationCollection(onFail);
        }

        return conversationInstance;
    }

    @Override
    public void addCollection(Conversation conversation,
                              OnSuccessListener<DocumentReference> onSuccess) {
        this.collection
                .add(conversation)
                .addOnSuccessListener(onSuccess);
    }

    @Override
    public void updateCollection(Conversation conversation) {
        this.collection
                .document(conversation.getId())
                .update(
                        KEY_LAST_MESSAGE_ID, conversation.getLastMessageId(),
                        KEY_LAST_DATE, conversation.getLastDateSent()
                );
    }

    public void getCollection(ChatMessage message,
                              OnCompleteListener<QuerySnapshot> onComplete, Call onFail) {

        this.collection
                .whereEqualTo(KEY_SENDER, message.getSenderId())
                .whereEqualTo(KEY_RECEIVED, message.getReceivedId())
                .get()
                .addOnCompleteListener(onComplete)
                .addOnFailureListener(fail -> callOnFail(onFail, fail));

    }

    @Override
    public void applyCollectionListener(Map<String, Object> whereArgs, @NonNull EventListener<QuerySnapshot> l) {
        if (Objects.isNull(whereArgs) || whereArgs.isEmpty()){
            return;
        }

        Query referenceQuery = null;

        for (Map.Entry<String, Object> where : whereArgs.entrySet()) {
            if (Objects.isNull(referenceQuery)) {
                referenceQuery = this.collection.whereEqualTo(where.getKey(), where.getValue());
                continue;
            }

            referenceQuery = referenceQuery.whereEqualTo(where.getKey(), where.getValue());
        }

        referenceQuery.addSnapshotListener(l);
    }

    @Override
    public void applyCollectionListener(OnCompleteListener<QuerySnapshot> onComplete) {
        this.collection
                .get()
                .addOnCompleteListener(onComplete);
    }

    private void callOnFail(Call onFail, Exception ex) {
        Map<String, Object> map = new HashMap<>();
        map.put(MESSAGE, ex.getMessage());
        onFail.start(map);
    }

    private RuntimeException throwsException(String message) {
        return new RuntimeException(message);
    }

    private void loadData(Query referenceQuery, Call onSuccess, Call onFail) {
        referenceQuery.get()
                .addOnCompleteListener(result -> {
                    QuerySnapshot documentSnapshots = result.getResult();
                    if (result.isSuccessful() && documentSnapshots != null) {

                        List<DocumentSnapshot> snapshotList = documentSnapshots.getDocuments();
                        if (snapshotList.isEmpty()) {
                            callOnFail(onFail, throwsException("No chats recently"));
                            return;
                        }
                        HashMap<String, Object> hashMap = new HashMap<>();

                        DocumentSnapshot actDoc = snapshotList.get(0);
                        Map<String, Object> results = hashMap;

                        Conversation conversation = actDoc.toObject(Conversation.class);
                        conversation.setId(actDoc.getId());
                        results.put(KEY_CONVERSATION_OBJ, conversation);

                        onSuccess.start(results);
                    }

                }).addOnFailureListener(fail -> {
                    callOnFail(onFail, fail);
                });
    }

}
