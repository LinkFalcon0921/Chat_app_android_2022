package com.flintcore.chat_app_android_22.firebase.firestore;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.COLLECTION;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_LOGIN_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserCollection extends FirebaseConnection<String, User> {

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
    public void getCollections(Call onSuccess, Call onFail) {
    }

    public void getCollection(@NonNull Map<String, Object> whereArgs, Call onSuccess, Call onFail) {
        User user;


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
            Map<String, Object> results = new HashMap<>();
            results.put(FirebaseConstants.Results.MESSAGE, e.getMessage());
            onFail.start(results);
        }

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

        try {
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
                        results.clear();
                        results.put("message", fail.getMessage());

                        onFail.start(results);
                    });

        } catch (Exception exception) {
        }

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
}
