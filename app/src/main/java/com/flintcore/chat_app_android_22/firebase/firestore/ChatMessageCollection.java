package com.flintcore.chat_app_android_22.firebase.firestore;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages.*;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatMessageCollection extends FirebaseConnection<String, ChatMessage> implements ChatListener<QuerySnapshot, String> {

    protected static final String NO_CHATS_RECENTLY = "No chats recently";
    protected static final String UNABLE_TO_CONNECT_TO_THE_DATABASE = "Unable to connect to the database";

    private static ChatMessageCollection chatMessageCollectionInstance;

    public static ChatMessageCollection getChatMessageCollectionInstance(Call onFail) {
        if (Objects.isNull(chatMessageCollectionInstance)) {
            chatMessageCollectionInstance = new ChatMessageCollection(onFail);
        }

        return chatMessageCollectionInstance;
    }

    public ChatMessageCollection(Call onFail) {
        try {
            this.collection = FirebaseFirestore.getInstance().collection(KEY_COLLECTION);
        } catch (Exception ex) {
            callOnFail(onFail, ex);
        }
    }

    @Override
    @Deprecated
    public void getCollections(Call onSuccess, Call onFail) {
        callOnFail(onFail, new RuntimeException("Not implemented..."));
    }


    @Override
    public void getCollections(String idReceiver, Call onSuccess, Call onFail) {
        Map<String, Object> results = getHashMap();
        this.collection
                .whereEqualTo(KEY_SENDER, idReceiver)
                .orderBy(KEY_DATETIME)
                .get()
                .addOnCompleteListener(result -> {
                    try {
                        if (result.isCanceled() || !result.isSuccessful()) {
                            throw throwDefaultException(UNABLE_TO_CONNECT_TO_THE_DATABASE);
                        }

                        QuerySnapshot documentSnapshots = result.getResult();
                        if (Objects.isNull(documentSnapshots) || documentSnapshots.isEmpty()) {
                            throw throwDefaultException(NO_CHATS_RECENTLY);
                        }

                        List<DocumentSnapshot> documents = documentSnapshots.getDocuments();

                        if (documents.isEmpty()) {
                            throw throwDefaultException(NO_CHATS_RECENTLY);
                        }
                        List<ChatMessage> messages = new LinkedList<>();

                        documents.stream()
                                .map(this::mapChatDocument)
                                .forEach(messages::add);

                        results.put(KEY_LIST_CHATS, messages);
                        onSuccess.start(results);
                    } catch (Exception ex) {
                        callOnFail(onFail, ex);
                    }
                })
                .addOnFailureListener(fail -> callOnFail(onFail, fail));
    }


    @Override
    public void getCollection(String s, Call onSuccess, Call onFail) {

    }

    @Override
    public void getCollection(Map<String, Object> whereArgs, Call onSuccess, Call onFail) {

    }

    @Override
    public void addCollection(ChatMessage chatMessage, Call onSuccess, Call onFail) {
        this.collection.add(chatMessage)
                .addOnCompleteListener(result -> onSuccess.start(null))
                .addOnFailureListener(fail -> {
                    fail = new RuntimeException(FirebaseConstants.Messages.FAIL_GET_RESPONSE);
                    callOnFail(onFail, fail);
                });
    }

    @Override
    public void editCollection(ChatMessage chatMessage, Call onSuccess, Call onFail) {

    }

    @Override
    public void deleteCollection(String s, Call onSuccess, Call onFail) {

    }

    @Override
    public void deleteCollection(String[] keys, Object[] values, Call onSuccess, Call onFail) {

    }

    @Override
    @Deprecated
    public void updateToken(String s, Call onSuccess, Call onFail) {

    }

    @Override
    @Deprecated
    public void clearToken(String s, Call onSuccess, Call onFail) {

    }

    //    Add chat listener to the app
    @Override
    public void setListener(@NonNull String sender, @NonNull String receiver, Map<String, Object> whereArgs,
                            @NonNull EventListener<QuerySnapshot> l) {

        Query querySender = this.collection.whereEqualTo(KEY_SENDER, sender)
                .whereEqualTo(KEY_RECEIVED, receiver);

        Query queryReceiver = this.collection.whereEqualTo(KEY_RECEIVED, sender)
                .whereEqualTo(KEY_SENDER, receiver);

//        Set additional where, is not null
        if (Objects.nonNull(whereArgs)) {
            for (Map.Entry<String, Object> where : whereArgs.entrySet()) {
                querySender = querySender.whereEqualTo(where.getKey(), where.getValue());
                queryReceiver = queryReceiver.whereEqualTo(where.getKey(), where.getValue());
            }
        }

        querySender.addSnapshotListener(l);
        queryReceiver.addSnapshotListener(l);

    }

    @NonNull
    private RuntimeException throwDefaultException(String s) {
        return new RuntimeException(s);
    }

    private void callOnFail(Call onFail, Exception e) {
        Map<String, Object> results = new HashMap<>();
        results.put(FirebaseConstants.Results.MESSAGE, e.getMessage());
        onFail.start(results);
    }

    @NonNull
    private HashMap<String, Object> getHashMap() {
        return new HashMap<>();
    }

    @NonNull
    private ChatMessage mapChatDocument(DocumentSnapshot doc) {
        ChatMessage message = doc.toObject(ChatMessage.class);
        message.setId(doc.getId());
        return message;
    }
}
