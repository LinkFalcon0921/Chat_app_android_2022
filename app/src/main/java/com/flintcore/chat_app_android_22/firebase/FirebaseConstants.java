package com.flintcore.chat_app_android_22.firebase;

public interface FirebaseConstants {

    interface SharedReferences{
        String CHAT_USER_LOGGED_PREFERENCES = "userLoggedPreferences";
    }


    interface Messages{
        String FAIL_GET_RESPONSE = "The database is not available";
    }

    interface Results{
        String MESSAGE = "message";
    }

    interface KeyEncryption{
        String KEY = "AES";
    }

    interface Users{
        String COLLECTION = "users";
        String KEY_ALIAS = "alias";
        String KEY_IMAGE = "image";
        String KEY_LOGIN_OBJ = "user_login";
        String KEY_EMAIL = "email";
        String KEY_PASS = "pass";
        String KEY_CONFIRM_PASS = "pass";

//        Defaults
        String KEY_IS_SIGNED_IN = "isSignedIn";
        String KEY_USER_ID = "userId";
    }
}
