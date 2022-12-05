package com.flintcore.chat_app_android_22.firebase.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class User implements Serializable {

    private String token;
    private String id;
    private String alias;
    private String image;
    private UserAccess userAccess;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

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

    public UserAccess getUserAccess() {
        return userAccess;
    }

    public void setUserAccess(UserAccess userAccess) {
        this.userAccess = userAccess;
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

}
