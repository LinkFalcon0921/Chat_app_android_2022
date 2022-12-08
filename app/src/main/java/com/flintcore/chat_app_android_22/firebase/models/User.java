package com.flintcore.chat_app_android_22.firebase.models;

import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

// User id of firebase Auth

@Data
public class User implements Serializable, Comparable<User>{

    @DocumentId
    private String id;
    private String token;
    private String alias;
    private String image;
    private int available;

    @Exclude
    private UserAccess userAccess;

    public User() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @DocumentId
    public String getId() {
        return id;
    }
    @DocumentId
    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Exclude
    public UserAccess getUserAccess() {
        return userAccess;
    }

    @Exclude
    public void setUserAccess(UserAccess userAccess) {
        this.userAccess = userAccess;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(alias, user.alias) && Objects.equals(image, user.image) && Objects.equals(userAccess, user.userAccess);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, alias, image, userAccess);
    }


    @Override
    public int compareTo(User user) {
        return Comparator.comparing(User::getId)
                .thenComparing(User::hashCode)
                .thenComparing(User::getToken)
                .compare(this, user);
    }
}
