package com.flintcore.chat_app_android_22.firebase.models.embbebed;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
/**Always create this class to login*/
public class UserAccess implements Serializable {
    //   User id
    private String id;
    @Exclude
    private String email;
    @Exclude
    private String pass;

    public UserAccess() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public String getEmail() {
        return email;
    }
    @Exclude
    public void setEmail(String email) {
        this.email = email;
    }
    @Exclude
    public String getPass() {
        return pass;
    }
    @Exclude
    public void setPass(String pass) {
        this.pass = pass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccess access = (UserAccess) o;
        return Objects.equals(email, access.email) && Objects.equals(pass, access.pass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, pass);
    }
}
