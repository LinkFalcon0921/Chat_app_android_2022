package com.flintcore.chat_app_android_22.firebase.firestore;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.NO_USERS_AVAILABLE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.SharedReferences.KEY_FMC_TOKEN;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.COLLECTION;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_EMAIL;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_LOGIN_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_PASS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USERS_LIST;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class UserCollection extends FirebaseConnection<String, User> {

    public static final String DEFAULT_MESSAGE_UNABLE_TOKEN_UPDATE = "Unable to sign out." +
            "\nCheck your internet connection";
    private static UserCollection instance;

    private UserCollection(Call onFail) {
        try {
            this.collection = FirebaseFirestore.getInstance().collection(COLLECTION);
        } catch (Exception e) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("message", e.getMessage());

            onFail.start(data);
        }
    }

    public static UserCollection getInstance(Call onFail) {
        if (Objects.isNull(instance)) {
            instance = new UserCollection(onFail);
        }

        return instance;
    }

    @Override
    public void getCollections(String userId, Call onSuccess, Call onFail) {

        this.collection.get()
                .addOnSuccessListener(result -> {
                    List<DocumentSnapshot> documents = result.getDocuments();

                    if (documents.isEmpty()) {
                        Exception ex = new RuntimeException(NO_USERS_AVAILABLE);
                        callOnFail(onFail, ex);
                        return;
                    }

                    Map<String, Object> values = new HashMap<>();
                    List<User> users = new LinkedList<>();

                    documents.stream().filter(u -> !u.getId().equals(userId)).forEach(document -> {
                        User user = filterAllUserDocumentData(document);

                        users.add(user);
                    });

                    values.put(KEY_USERS_LIST, Optional.ofNullable(users));
                    onSuccess.start(values);
                })
                .addOnFailureListener(fail -> {
                    callOnFail(onFail, new RuntimeException("No users available"));
                });

    }

    @NonNull
    private User filterAllUserDocumentData(DocumentSnapshot document) {
        String id = document.getId();
        String image = document.get(KEY_IMAGE, String.class);
        String alias = document.get(KEY_ALIAS, String.class);
        String token = document.get(KEY_FMC_TOKEN, String.class);

        User user = new User();
        user.setId(id);
        user.setImage(image);
        user.setAlias(alias);
        user.setToken(token);

        UserAccess userAccess = document.get(KEY_LOGIN_OBJ, UserAccess.class);

//        UserAccess userAccess = new UserAccess();
//        userAccess.setEmail(email);
//        userAccess.setPass(pass);

        user.setUserAccess(userAccess);
        return user;
    }

    @Override
    public void getCollections(Call onSuccess, Call onFail) {

        this.collection.get()
                .addOnSuccessListener(result -> {
                    List<DocumentSnapshot> documents = result.getDocuments();

                    if (documents.isEmpty()) {
                        Exception ex = new RuntimeException(NO_USERS_AVAILABLE);
                        callOnFail(onFail, ex);
                        return;
                    }
                    Map<String, Object> values = new HashMap<>();
                    List<User> users = new LinkedList<>();

                    documents.forEach(document -> {
                        String id = document.getId();
                        String image = document.get(KEY_IMAGE, String.class);
                        String alias = document.get(KEY_ALIAS, String.class);

                        Map<String, Object> access = document.get(KEY_LOGIN_OBJ, Map.class);
                        String email = (String) access.get(KEY_EMAIL);
                        String pass = (String) access.get(KEY_PASS);

                        User user = new User();
                        user.setId(id);
                        user.setImage(image);
                        user.setAlias(alias);

                        UserAccess userAccess = new UserAccess();
                        userAccess.setEmail(email);
                        userAccess.setPass(pass);

                        user.setUserAccess(userAccess);

                        users.add(user);
                    });

                    values.put(KEY_USERS_LIST, users);
                    onSuccess.start(values);
                })
                .addOnFailureListener(fail -> {
                    callOnFail(onFail, new RuntimeException("No users available"));
                });

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

    private void callOnFail(Call onFail, Exception e) {
        Map<String, Object> results = new HashMap<>();
        results.put(FirebaseConstants.Results.MESSAGE, e.getMessage());
        onFail.start(results);
    }

    private void loadData(Query referenceQuery, Call onSuccess, Call onFail) {
        referenceQuery.get()
                .addOnCompleteListener(result -> {

                    QuerySnapshot documentSnapshots = result.getResult();
                    if (result.isSuccessful() && documentSnapshots != null) {
                        HashMap<String, Object> hashMap = new HashMap<>();

                        List<DocumentSnapshot> snapshotList = documentSnapshots.getDocuments();
                        if (snapshotList.isEmpty()) {
                            hashMap.put(FirebaseConstants.Results.MESSAGE, "The credentials does not exists");
                            onFail.start(hashMap);
                        }

                        DocumentSnapshot actDoc = snapshotList.get(0);
                        Map<String, Object> results = hashMap;

                        String id = actDoc.getId();
                        String alias = (String) actDoc.get(KEY_ALIAS);
                        String image = ((String) actDoc.get(KEY_IMAGE));

                        results.put(KEY_USER_ID, id);
                        results.put(KEY_ALIAS, alias);
                        results.put(KEY_IMAGE, image);

                        onSuccess.start(results);
                    }

                }).addOnFailureListener(fail -> {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(FirebaseConstants.Results.MESSAGE, "The user does not exists");
                });
    }

    @Override
    public void getCollection(String s, Call onSuccess, Call onFail) {
    }

    @Override
    public void addCollection(User user, Call onSuccess, Call onFail) {
        Map<String, Object> data;

        data = new HashMap<>();

        data.put(KEY_ALIAS, user.getAlias());
        data.put(KEY_IMAGE, user.getImage());
        data.put(KEY_LOGIN_OBJ, user.getUserAccess());

        Map<String, Object> results = new HashMap<>();
        this.collection.add(data)
                .addOnSuccessListener(result -> {
                    data.clear();

                    results.put("message", "User created.");
                    results.put(KEY_USER_ID, result.getId());
                    results.put(KEY_ALIAS, user.getAlias());
                    results.put(KEY_IMAGE, user.getImage());

                    onSuccess.start(results);

                })
                .addOnFailureListener(fail -> {
                    callOnFail(onFail, fail);
                });

    }

    @Override
    public void editCollection(User user, Call onSuccess, Call onFail) {
    }

    @Override
    public void deleteCollection(String s, Call onSuccess, Call onFail) {
    }

    @Override
    public void deleteCollection(String[] keys, Object[] values, Call onSuccess, Call onFail) {
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
    public void updateToken(String id, Call onSuccess, Call onFail) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(result ->
                        this.connectAndGetToken(id, result, onSuccess, onFail));
    }

    @Override
    public void clearToken(String id, Call onSuccess, Call onFail) {
        this.collection.document(id)
                .update(KEY_FMC_TOKEN, FieldValue.delete())
                .addOnSuccessListener(unused -> {
                    onSuccess.start(null);
                }).addOnFailureListener(fail -> {
                    Map<String, Object> values = new HashMap<>();
                    values.put(FirebaseConstants.Results.MESSAGE, DEFAULT_MESSAGE_UNABLE_TOKEN_UPDATE);
                    onFail.start(values);
                });
    }
}
