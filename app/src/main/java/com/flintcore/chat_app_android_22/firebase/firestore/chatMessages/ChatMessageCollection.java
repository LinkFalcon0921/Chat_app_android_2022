package com.flintcore.chat_app_android_22.firebase.firestore.chatMessages;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages.KEY_CHAT_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages.KEY_COLLECTION;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages.KEY_RECEIVED;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages.KEY_SENDER;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.firestore.FirebaseConnection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatMessageCollection extends FirebaseConnection
        implements IChatMessageCollection {

    private static ChatMessageCollection chatMessageCollectionInstance;

    private ChatMessageCollection(CallResult<Exception> onFail) {
        super();
        try {
            this.collection = FirebaseFirestore.getInstance().collection(KEY_COLLECTION);
        } catch (Exception ex) {
            onFail.onCall(ex);
        }
    }

    public static ChatMessageCollection getChatMessageCollectionInstance(Call onFail) {
        if (Objects.isNull(chatMessageCollectionInstance)) {
            chatMessageCollectionInstance = new ChatMessageCollection(onFail);
        }

        return chatMessageCollectionInstance;
    }

    public static ChatMessageCollection getChatMessageCollectionInstance(CallResult<Exception> onFail) {
        if (Objects.isNull(chatMessageCollectionInstance)) {
            chatMessageCollectionInstance = new ChatMessageCollection(onFail);
        }

        return chatMessageCollectionInstance;
    }

    public ChatMessageCollection(Call onFail) {
        super();
        try {
            this.collection = FirebaseFirestore.getInstance().collection(KEY_COLLECTION);
        } catch (Exception ex) {
            callOnFail(onFail, ex);
        }
    }

    @Override
    public <K extends String, V> void getCollectionById(@NonNull ChatMessage chatMessage,
                                                        @NonNull Collection<QueryCondition<K, V>> whereConditions,
                                                        @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener, CallResult<Exception> onFailListener) {
        this.getFirebaseQueryWithId(chatMessage.getId(), whereConditions)
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);
    }

    @Override
    public <K extends String, V> void getCollectionById(@NonNull ChatMessage chatMessage,
                                                        @NonNull CallResult<Task<DocumentSnapshot>> onCompleteListener,
                                                        CallResult<Exception> onFailListener) {
        this.collection.document(chatMessage.getId())
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);
    }

    /**
     * ID not necessary in this case
     */
    @Override
    public <K extends String, V>
    void addCollectionById(@NonNull ChatMessage chatMessage,
                           @NonNull Collection<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Void> onSuccess, CallResult<Exception> onFailListener) {
        Map<String, Object> document = this.wrapper.getDocument(chatMessage);

        DocumentReference documentReference;

//        label check if the user has an inserted ID
        if (Objects.nonNull(chatMessage.getId())) {
            documentReference = this.collection.document(chatMessage.getId());
        } else {
            documentReference = this.collection.document();
            chatMessage.setId(documentReference.getId());
        }

        documentReference
                .set(document)
                .addOnSuccessListener(onSuccess::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

    public <K extends String, V extends Object>
    void applyChatListener(@NonNull Collection<QueryCondition<K, V>> whereConditions,
                           EventListener<QuerySnapshot> l) {

        this.getFirebaseQuery(whereConditions)
                .addSnapshotListener(l);

    }


    private void callOnFail(Call onFail, Exception e) {
        Map<String, Object> results = new HashMap<>();
        results.put(FirebaseConstants.Results.MESSAGE, e.getMessage());
        onFail.start(results);
    }

//    Label methods to implement


    @Override
    public <K extends String, V>
    void getCollections(@NonNull Collection<QueryCondition<K, V>> whereConditions,
                        @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                        CallResult<Exception> onFailListener) {

    }

    @Override
    public <K extends String, V> void deleteCollection(@NonNull ChatMessage chatMessage, @NonNull Collection<QueryCondition<K, V>> whereConditions, @NonNull CallResult<Task<Void>> onCompleteListener, CallResult<Exception> onFailListener) {
        this.collection.document(chatMessage.getId())
                .delete()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);
    }
}
