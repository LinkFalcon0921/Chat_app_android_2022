package com.flintcore.chat_app_android_22.firebase.models.embbebed;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
/**Always create this class to login*/
public class UserAccess {

    private String email;
    private String pass;

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
}
