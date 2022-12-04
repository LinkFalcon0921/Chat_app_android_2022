package com.flintcore.chat_app_android_22.firebase.firestore;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.COLLECTION;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_LOGIN_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;

import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
        if(Objects.isNull(instance)){
            instance = new UserCollection(onFail);
        }

        return instance;
    }

    @Override
    @Deprecated
    public Set<User> getCollections() {
        return null;
    }

    public User getCollection(String[] keys, Object[] values) {
        User user;

        Query referenceQuery = this.collection.whereEqualTo(keys[0], values[0]);
        for (int index = 1; index < keys.length; index++) {
            referenceQuery = referenceQuery.whereEqualTo(keys[index], values[index]);
        }

        user = loadData(referenceQuery);

        return user;
    }

//    TODO

    private User loadData(Query referenceQuery) {

        return null;
    }

    @Override
    public User getCollection(String s) {
        return null;
    }

    @Override
    public void addCollection(User user, Call onSuccess, Call onFail) {
        Map<String, Object> data;

        try {
            data = new HashMap<>();

            data.put(KEY_ALIAS, user.getAlias());
            data.put(KEY_IMAGE, user.getImage());
            data.put(KEY_LOGIN_OBJ, user.getLogin());

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
