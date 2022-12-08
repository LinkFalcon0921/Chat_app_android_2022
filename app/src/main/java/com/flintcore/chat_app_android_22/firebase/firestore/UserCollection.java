package com.flintcore.chat_app_android_22.firebase.firestore;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.NO_USER;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.SharedReferences.KEY_FMC_TOKEN;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.COLLECTION;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_AVAILABLE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_OBJ;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.firestore.users.IUserCollection;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


public class UserCollection extends FirebaseConnection<String, User> implements IUserCollection<Exception> {

    public static final String DEFAULT_MESSAGE_UNABLE_TOKEN_UPDATE = "Unable to sign out." +
            "\nCheck your internet connection";
    public static final FieldPath DOCUMENT_ID = FieldPath.documentId();
    private static UserCollection instance;

    private UserCollection(Call onFail) {
        super(DEFAULT_ORDER_BY_FIELD);
        try {
            this.collection = FirebaseFirestore.getInstance().collection(COLLECTION);
        } catch (Exception e) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("message", e.getMessage());

            onFail.start(data);
        }
    }

    private UserCollection(CallResult<Exception> onFail) {
        super();
        try {
            this.collection = FirebaseFirestore.getInstance().collection(COLLECTION);
        } catch (Exception e) {
            onFail.onCall(e);
        }
    }

    public static UserCollection getInstance(Call onFail) {
        if (Objects.isNull(instance)) {
            instance = new UserCollection(onFail);
        }

        return instance;
    }

    public static UserCollection getInstance(CallResult<Exception> onFail) {
        if (Objects.isNull(instance)) {
            instance = new UserCollection(onFail);
        }

        return instance;
    }

//  label methods

    @Override
    public <K extends String, V extends Object>
    void getCollections(@NonNull List<QueryCondition<K, V>> whereConditions,
                        @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener, CallResult<Exception> onFailListener) {

        this.getFirebaseQuery(whereConditions)
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

//    TODO DELETE AND REPLACE

    public void getCollections(String userId, CallResult<Collection<User>> onSuccess, CallResult<Exception> onFail) {

        this.collection
                .whereNotEqualTo(FieldPath.documentId(), userId)
                .get()
                .addOnSuccessListener(result -> {
                    Collection<User> userIds = result.getDocuments()
                            .stream().map(doc -> doc.toObject(User.class))
                            .collect(Collectors.toList());

                    onSuccess.onCall(userIds);
                })
                .addOnFailureListener(onFail::onCall);

    }

    public void getCollection(@NonNull Map<String, Object> whereArgs, Call onSuccess, Call onFail) {
        try {
            Query referenceQuery = null;

            for (Map.Entry<String, Object> where : whereArgs.entrySet()) {
                if (Objects.isNull(referenceQuery)) {
                    referenceQuery = this.collection.whereEqualTo(where.getKey(), where.getValue());
                    continue;
                }

                referenceQuery = referenceQuery.whereEqualTo(where.getKey(), where.getValue());
            }

            loadData(referenceQuery, onSuccess, onFail);
        } catch (Exception e) {
            callOnFail(onFail, e);
        }

    }

    private void loadData(Query referenceQuery, Call onSuccess, Call onFail) {
        referenceQuery.get()
                .addOnCompleteListener(result -> {
                    QuerySnapshot documentSnapshots = result.getResult();
                    if (!result.isSuccessful() && Objects.isNull(documentSnapshots)) {
                        callOnFail(onFail, throwsDefaultException(NO_USER));
                        return;
                    }

                    HashMap<String, Object> hashMap = new HashMap<>();

                    List<DocumentSnapshot> snapshotList = documentSnapshots.getDocuments();
                    if (snapshotList.isEmpty()) {
                        callOnFail(onFail, throwsDefaultException(NO_USER));
                        return;
                    }

                    DocumentSnapshot actDoc = snapshotList.get(0);
                    Map<String, Object> results = hashMap;

                    User user = actDoc.toObject(User.class);
                    user.setId(actDoc.getId());
                    results.put(KEY_USER_OBJ, user);

                    onSuccess.start(results);


                }).addOnFailureListener(fail -> callOnFail(onFail, fail));
    }

    public <K extends String, V>
    void getCollectionById(@NonNull User user,
                           @NonNull List<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Task<DocumentSnapshot>> onCompleteListener,
                           CallResult<Exception> onFailListener) {

        this.collection.document(user.getId())
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);
    }

    public void getCollection(String userId, Call onSuccess, Call onFail) {
        this.collection.document(userId)
                .get()
                .addOnCompleteListener(result -> {
                    if (!result.isSuccessful() || !result.getResult().exists()) {
                        callOnFail(onFail, throwsDefaultException("No chats recently"));
                    }

                    DocumentSnapshot documentSnapshot = result.getResult();
                    User user = documentSnapshot.toObject(User.class);
                    user.setId(documentSnapshot.getId());

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(KEY_USER_OBJ, user);

                    onSuccess.start(hashMap);
                })
                .addOnFailureListener(fail -> callOnFail(onFail, fail));
    }


    @Override
    public <K extends String, V extends Object>
    void addCollectionById(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Void> onCompleteListener, CallResult<Exception> onFailListener) {

        this.collection.document(user.getId())
                .set(user)
                .addOnSuccessListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

//    TODO LABEL Complete the method for user info activity

    public void deleteCollection(String s, Call onSuccess, Call onFail) {
    }

    private void connectAndGetToken(String id, String token, Call onSuccess, Call onFail) {
        this.collection.document(id)
                .update(KEY_FMC_TOKEN, token)
                .addOnCompleteListener(result -> onSuccess.start(null))
                .addOnFailureListener(fail -> {
                    Map<String, Object> values = new HashMap<>();
                    values.put(FirebaseConstants.Results.MESSAGE, "Check your internet connection");
                    onFail.start(values);
                });
    }

    @Override
    public <K extends String, V extends Object>
    void updateToken(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                     @NonNull CallResult<String> onCompleteListener, CallResult<Exception> onFailListener) {

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

    // TODO: REPLACE AND DELETE

    public void updateToken(String id, Call onSuccess, Call onFail) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(result ->
                        connectAndGetToken(id, result, onSuccess, onFail));
    }


    // TODO: REPLACE AND DELETE
    public void clearToken(String id, Call onSuccess, Call onFail) {
        this.collection.document(id)
                .update(KEY_FMC_TOKEN, FieldValue.delete())
                .addOnSuccessListener(unused -> {
                    onSuccess.start(null);
                }).addOnFailureListener(fail -> {
                    callOnFail(onFail, fail);
                });
    }


    @Override
    public <K extends String, V extends Object>
    void updateAvailability(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                            @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener, CallResult<Exception> onFailListener) {
        this.collection
                .document(user.getId())
                .update(KEY_AVAILABLE, user.getAvailable());
    }


    // TODO: REPLACE AND DELETE


    public void updateAvailable(String userId, int newValue) {
        this.collection
                .document(userId)
                .update(KEY_AVAILABLE, newValue);
    }

    public <K extends String, V extends Object>
    void applyUserListener(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                           @NonNull EventListener<QuerySnapshot> l,
                           CallResult<Exception> onFailListener) {

        getQueryWithUserId(user.getId(), whereConditions)
                .addSnapshotListener(l);

    }

    //    label Method to get Query with user id isntance
    @NonNull
    private <K extends String, V extends Object>
    Query getQueryWithUserId(String userId, @NonNull List<QueryCondition<K, V>> whereConditions) {
        return this.getFirebaseQuery(whereConditions)
                .whereEqualTo(DOCUMENT_ID, userId);
    }


    // TODO: REPLACE AND DELETE

    public void applyUserAvailability(@NonNull Map<Object, Object> whereArgs, EventListener<QuerySnapshot> l) {
        if (whereArgs.isEmpty()) {
            return;
        }

        Query referenceQuery = null;

        for (Map.Entry<Object, Object> entry : whereArgs.entrySet()) {
            if (Objects.isNull(referenceQuery)) {
                referenceQuery = this.collection
                        .whereEqualTo(entry.getKey().toString(), entry.getValue().toString());
                continue;
            }
            referenceQuery = referenceQuery
                    .whereEqualTo(entry.getKey().toString(), entry.getValue().toString());
        }

        referenceQuery.addSnapshotListener(l);
    }

    // TODO: REPLACE AND DELETE

    @Deprecated
    private void callOnFail(Call onFail, Exception e) {
        Map<String, Object> results = new HashMap<>();
        results.put(FirebaseConstants.Results.MESSAGE, e.getMessage());
        onFail.start(results);
    }

    @NonNull
    private RuntimeException throwsDefaultException(String message) {
        return new RuntimeException(message);
    }

//    label methods to fill

    @Override
    public <K extends String, V extends Object>
    void deleteCollection(@NonNull User user, @NonNull List<QueryCondition<K, V>> whereConditions,
                          @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener, CallResult<Exception> onFailListener) {

    }


}
