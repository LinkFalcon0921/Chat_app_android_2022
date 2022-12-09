package com.flintcore.chat_app_android_22.firebase.firestore.users;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.NO_USER;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.SharedReferences.KEY_FMC_TOKEN;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.COLLECTION;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_AVAILABLE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_OBJ;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.firestore.FirebaseConnection;
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


public class UserCollection extends FirebaseConnection implements IUserCollection<User> {

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
    void getCollections(@NonNull Collection<QueryCondition<K, V>> whereConditions,
                        @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener, CallResult<Exception> onFailListener) {

        this.getFirebaseQuery(whereConditions)
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

// TODO: 12/8/2022 delete

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
                           @NonNull Collection<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Task<QuerySnapshot>> onCompleteListener,
                           CallResult<Exception> onFailListener) {

        if (Objects.nonNull(user.getId())) {
            this.getFirebaseQueryWithId(user.getId(), whereConditions)
                    .get()
                    .addOnCompleteListener(onCompleteListener::onCall)
                    .addOnFailureListener(onFailListener::onCall);
            return;
        }

        this.getFirebaseQuery(whereConditions)
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

    public <K extends String, V>
    void getCollectionById(@NonNull User user,
                           @NonNull CallResult<Task<DocumentSnapshot>> onCompleteListener,
                           CallResult<Exception> onFailListener) {

        this.collection.document(user.getId())
                .get()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);
    }

    @Override
    public <K extends String, V extends Object>
    void addCollectionById(@NonNull User user, @NonNull Collection<QueryCondition<K, V>> whereConditions,
                           @NonNull CallResult<Void> onSuccess, CallResult<Exception> onFailListener) {
        Map<String, Object> document = this.wrapper.getDocument(user);

        this.collection.document(user.getId())
                .set(document)
                .addOnSuccessListener(onSuccess::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

    /**
     * Set null to delete.
     */
    @Override
    public void clearToken(User user, CallResult<Task<Void>> onCompleteListener,
                           CallResult<Exception> onFailListener) {

        FirebaseMessaging.getInstance()
                .deleteToken()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

    public void updateToken(User user){

        if (Objects.isNull(user)) {
            return;
        }

        Object value = Objects.isNull(user.getToken()) ?
                FieldValue.delete() : user.getToken();


        this.collection.document(user.getId())
                .update(KEY_FMC_TOKEN, value);
    }

    @Override
    public <K extends String, V extends Object>
    void appendToken(@NonNull User user,
                     @NonNull CallResult<String> onCompleteListener, CallResult<Exception> onFailListener) {

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }

    @Override
    public <K extends String, V extends Object>
    void updateAvailability(@NonNull User user, @NonNull Collection<QueryCondition<K, V>> whereConditions,
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

    @Override
    public <K extends String, V extends Object>
    void applyUserListener(@NonNull User user, @NonNull Collection<QueryCondition<K, V>> whereConditions,
                           @NonNull EventListener<QuerySnapshot> l,
                           CallResult<Exception> onFailListener) {

        this.getFirebaseQueryWithId(user.getId(), whereConditions)
                .addSnapshotListener(l);

    }

//    label Set userAvailability via queries

    @Override
    public <K extends String, V>
    void applyUserAvailability(@NonNull Collection<QueryCondition<K, V>> whereConditions,
                               EventListener<QuerySnapshot> l) {

        this.getFirebaseQuery(whereConditions)
                .addSnapshotListener(l);

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
    void deleteCollection(@NonNull User user,
                          @NonNull Collection<QueryCondition<K, V>> whereConditions,
                          @NonNull CallResult<Task<Void>> onCompleteListener,
                          CallResult<Exception> onFailListener) {

        this.collection.document(user.getId())
                .delete()
                .addOnCompleteListener(onCompleteListener::onCall)
                .addOnFailureListener(onFailListener::onCall);

    }


}
