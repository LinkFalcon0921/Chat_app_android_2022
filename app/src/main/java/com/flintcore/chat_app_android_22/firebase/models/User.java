package com.flintcore.chat_app_android_22.firebase.models;

import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class User {
    private String id;
    private String alias;
    private String image;
    private UserAccess login;

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

    public UserAccess getLogin() {
        return login;
    }

    public void setLogin(UserAccess login) {
        this.login = login;
    }

}
