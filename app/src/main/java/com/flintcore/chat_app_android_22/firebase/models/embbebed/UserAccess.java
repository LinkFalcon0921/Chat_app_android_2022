package com.flintcore.chat_app_android_22.firebase.models.embbebed;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.Objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
/**Always create this class to login*/
public class UserAccess implements Serializable {

    @DocumentId
    private String id;

    private String email;
    private String pass;

    public UserAccess() {
    }

    @DocumentId
    public String getId() {
        return id;
    }
    @DocumentId
    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

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
