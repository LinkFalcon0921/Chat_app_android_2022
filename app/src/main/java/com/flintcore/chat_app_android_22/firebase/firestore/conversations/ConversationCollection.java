package com.flintcore.chat_app_android_22.firebase.firestore.conversations;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_COLLECTION;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_LAST_DATE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_LAST_MESSAGE_ID;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.firestore.FirebaseConnection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConversationCollection extends FirebaseConnection
        implements IConversationCollection<Conversation, String> {

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

    private ConversationCollection(CallResult<Exception> onFail) {
        try {
            this.collection = FirebaseFirestore.getInstance()
                    .collection(KEY_COLLECTION);

        } catch (Exception ex) {
            onFail.onCall(ex);
        }
    }

    public static ConversationCollection getConversationInstance(CallResult<Exception> onFail) {
        if (Objects.isNull(conversationInstance)) {
            conversationInstance = new ConversationCollection(onFail);
        }

        return conversationInstance;
    }

    @Override
    public <K extends String, V>
    void addCollectionById(@NonNull Conversation conversation,
                           @NonNull List<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Void> onSuccess,
                           @NonNull CallResult<Exception> onFailListener) {

        this.collection.document(conversation.getId())
                .set(conversation)
                .addOnSuccessListener(onSuccess::onCall)
                .addOnFailureListener(onFailListener::onCall);
    }

    public void addCollection(Conversation conversation,
                              OnSuccessListener<DocumentReference> onSuccess) {
        this.collection
                .add(conversation)
                .addOnSuccessListener(onSuccess);
    }

    @Override
    public void update(@NonNull Conversation conversation,
                       Map<String, Object> fields,
                       @NonNull CallResult<Task<Void>> onComplete,
                       @NonNull CallResult<Exception> onFailListener) {

        this.collection.document(conversation.getId())
                .update(fields)
                .addOnCompleteListener(onComplete::onCall)
                .addOnFailureListener(onFailListener::onCall);
    }

    //    label delete

    public void updateCollection(@NonNull Conversation conversation, @NonNull CallResult<Void> onSuccess,
                                 @NonNull Call onFail) {
        this.collection
                .document(conversation.getId())
                .update(
                        KEY_LAST_MESSAGE_ID, conversation.getLastMessageId(),
                        KEY_LAST_DATE, conversation.getLastDateSent()
                ).addOnCompleteListener(result -> {
                    if (!result.isSuccessful()) {
                        return;
                    }

                    onSuccess.onCall(null);
                }).addOnFailureListener(fail -> {
                    callOnFail(onFail, throwsException("Unable to send"));
                });
    }

    @Override
    public <K extends String, V>
    void getCollections(@NonNull List<QueryCondition<K, V>> whereConditions,
                        @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                        CallResult<Exception> onFailListener) {

        this.getFirebaseQuery(whereConditions)
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
                .whereEqualTo(KEY_LAST_MESSAGE_ID, message.getId())
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
                           @NonNull List<QueryCondition<K, V>> whereConditions,
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
                          @NonNull List<QueryCondition<K, V>> whereConditions,
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

    //    label delete apply listener snapshot

    public void applyCollectionListener(Map<Object, Object> whereArgs, @NonNull EventListener<QuerySnapshot> l) {
        if (Objects.isNull(whereArgs) || whereArgs.isEmpty()) {
            return;
        }

        Query referenceQuery = null;

        for (Map.Entry<Object, Object> where : whereArgs.entrySet()) {
            if (Objects.isNull(referenceQuery)) {
                referenceQuery = this.collection.whereEqualTo(where.getKey().toString(), where.getValue());
                continue;
            }

            referenceQuery = referenceQuery.whereEqualTo(where.getKey().toString(), where.getValue());
        }

        /*ListenerRegistration listenerRegistration = */
        referenceQuery.addSnapshotListener(l);
    }

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

}
