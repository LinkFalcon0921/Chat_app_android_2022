package com.flintcore.chat_app_android_22.firebase.firestore.conversations;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_COLLECTION;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_LAST_MESSAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations;
import com.flintcore.chat_app_android_22.firebase.firestore.FirebaseConnection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConversationCollection<D extends ChatMessage> extends FirebaseConnection
        implements IConversationCollection<Conversation, D, String> {

    private static ConversationCollection<ChatMessage> conversationInstance;
    private CollectionReference chatMessageCollection;

    private ConversationCollection(Call onFail) {
        super();
        try {
            this.collection = FirebaseFirestore.getInstance()
                    .collection(KEY_COLLECTION);

        } catch (Exception ex) {
            callOnFail(onFail, ex);
        }
    }

    private ConversationCollection(CallResult<Exception> onFail) {
        super();
        try {
            this.collection = FirebaseFirestore.getInstance()
                    .collection(KEY_COLLECTION);
            this.chatMessageCollection = FirebaseFirestore.getInstance()
                    .collection(FirebaseConstants.ChatMessages.KEY_COLLECTION);

        } catch (Exception ex) {
            onFail.onCall(ex);
        }
    }

    public static ConversationCollection<ChatMessage> getConversationInstance(Call onFail) {
        if (Objects.isNull(conversationInstance)) {
            conversationInstance = new ConversationCollection<>(onFail);
        }

        return conversationInstance;
    }

    public static ConversationCollection<ChatMessage> getConversationInstance(CallResult<Exception> onFail) {
        if (Objects.isNull(conversationInstance)) {
            conversationInstance = new ConversationCollection<>(onFail);
        }

        return conversationInstance;
    }

    @Override
    public <K extends String, V>
    void addCollectionById(@NonNull Conversation conversation,
                           @NonNull Collection<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Void> onSuccess,
                           @NonNull CallResult<Exception> onFailListener) {

        Map<String, Object> document = this.wrapper.getDocument(conversation);

        DocumentReference documentRefToAdd;

        if (Objects.nonNull(conversation.getId())) {
            documentRefToAdd = this.collection.document(conversation.getId());
        } else {
            documentRefToAdd = this.collection.document();
            conversation.setId(documentRefToAdd.getId());
        }

        if (document.containsKey(KEY_LAST_MESSAGE)) {
            String documentPath = (String) document.get(KEY_LAST_MESSAGE);
            document.put(KEY_LAST_MESSAGE, this.chatMessageCollection.document(documentPath));
        }

        documentRefToAdd
                .set(document)
                .addOnSuccessListener(onSuccess::onCall)
                .addOnFailureListener(onFailListener::onCall);
    }

    @Override
    public void update(@NonNull Conversation conversation,
                       @NonNull CallResult<Task<Void>> onComplete,
                       @NonNull CallResult<Exception> onFailListener) {

        Map<String, Object> fields = this.wrapper.getDocument(conversation);

        if (fields.containsKey(KEY_LAST_MESSAGE)) {
            String chatDocumentPath = (String) fields.get(KEY_LAST_MESSAGE);
            fields.replace(KEY_LAST_MESSAGE, this.chatMessageCollection.document(chatDocumentPath));
        }

        this.collection.document(conversation.getId())
                .update(fields)
                .addOnCompleteListener(onComplete::onCall)
                .addOnFailureListener(onFailListener::onCall);
    }

    @Override
    public <K extends String, V>
    void getCollections(@NonNull Collection<QueryCondition<K, V>> whereConditions,
                        @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                        CallResult<Exception> onFailListener) {

        this.getFirebaseQuery(whereConditions)
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }


    //    label get by Chat obj
    public <K extends String, V extends Object>
    void getCollectionByLastChatMessage(@NonNull D chatMessage,
                                        @NonNull Collection<QueryCondition<K, V>> whereConditions,
                                        @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                                        CallResult<Exception> onFailListener) {
        this.collection.
                whereEqualTo(Conversations.KEY_LAST_MESSAGE, chatMessage.getId())
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

    /**
     * Use message id for query
     */

//    label delete
    public void getCollection(ChatMessage message,
                              OnCompleteListener<QuerySnapshot> onComplete, Call onFail) {
        this.collection
                .whereEqualTo(KEY_LAST_MESSAGE, message.getId())
                .get()
                .addOnCompleteListener(onComplete)
                .addOnFailureListener(fail -> callOnFail(onFail, fail));
    }

    @Override
    public <K extends String, V>
    void getCollectionById(@NonNull Conversation conversation,
                           @NonNull CallResult<Task<DocumentSnapshot>> onCompleteListener,
                           CallResult<Exception> onFailListener) {

    }

    @Override
    public <K extends String, V>
    void getCollectionById(@NonNull Conversation conversation,
                           @NonNull Collection<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                           @NonNull CallResult<Exception> onFailListener) {

        this.getFirebaseQueryWithId(conversation.getId(), whereConditions)
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

    @Override
    public <K extends String, V>
    void deleteCollection(@NonNull Conversation conversation,
                          @NonNull Collection<QueryCondition<K, V>> whereConditions,
                          @NonNull CallResult<Task<Void>> onCompleteListener,
                          CallResult<Exception> onFailListener) {

        this.collection.document(conversation.getId())
                .delete()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

    @Override
    public <K extends String, V>
    void applyCollectionListener(Collection<QueryCondition<K, V>> whereArgs,
                                 @NonNull EventListener<QuerySnapshot> l) {

        this.getFirebaseQuery(whereArgs)
                .addSnapshotListener(l);
    }

    public <K extends String, V>
    ListenerRegistration applyCollectionListenerWithResult(Collection<QueryCondition<K, V>> whereArgs,
                                                 @NonNull EventListener<QuerySnapshot> l) {

        return this.getFirebaseQuery(whereArgs)
                .addSnapshotListener(l);
    }

    private void callOnFail(Call onFail, Exception ex) {
        Map<String, Object> map = new HashMap<>();
        map.put(MESSAGE, ex.getMessage());
        onFail.start(map);
    }

    private RuntimeException throwsException(String message) {
        return new RuntimeException(message);
    }

}
