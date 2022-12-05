package com.flintcore.chat_app_android_22;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.activities.SignInActivity;
import com.flintcore.chat_app_android_22.activities.UserActivity;
import com.flintcore.chat_app_android_22.databinding.ActivityMainBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.firestore.UserCollection;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;

public class MainActivity extends AppCompatActivity {

    private boolean flagFirstLogin = true;
    private ActivityMainBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private UserCollection userCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.loggedPreferencesManager = new PreferencesManager(getApplicationContext(),
                FirebaseConstants.SharedReferences.KEY_CHAT_USER_LOGGED_PREFERENCES);

        this.userCollection = UserCollection.getInstance(getOnFailFirebaseConnection());

        this.loadLoggedImage();
        this.updateToken();

        this.setListeners();
    }

    private void setListeners() {
        this.binding.logoutBtn.setOnClickListener(v -> signOutUser());
//        Open users List to add
        this.binding.fabAddUser.setOnClickListener(v -> startActivity(goToUserAddListIntent()));
    }

    private Intent goToUserAddListIntent() {
        Intent userListIntent = new Intent(getApplicationContext(), UserActivity.class);
        return userListIntent;
    }

    private void signOutUser() {
        String userId = this.loggedPreferencesManager
                .getString(FirebaseConstants.Users.KEY_USER_ID);


        Call onSuccess = unused -> {
            this.loggedPreferencesManager.clear();
            startActivity(goToSignInIntent());
            finish();
        };

        Call onFail = getOnFailFirebaseConnection();

        this.userCollection.clearToken(userId, onSuccess, onFail);
    }

    @NonNull
    private Intent goToSignInIntent() {
        Intent signUpIntent = new Intent(getApplicationContext(), SignInActivity.class);
        return signUpIntent;
    }

    private void updateToken() {
        if(!this.flagFirstLogin){
            return;
        }


        String userId = this.loggedPreferencesManager
                .getString(FirebaseConstants.Users.KEY_USER_ID);

        Call onSuccess = data -> {
            MessagesAppGenerator.showToast(getApplicationContext(), "Sign in successfully!", FirebaseConstants.Messages.FAIL_GET_RESPONSE);
        };

        Call onFail = getOnFailFirebaseConnection();

        this.userCollection.updateToken(userId, onSuccess, onFail);
        this.flagFirstLogin = false;
    }

    private void loadLoggedImage() {
        byte[] imageBytes = Encryptions.decryptAndroidImageFromString(
                this.loggedPreferencesManager
                        .getString(FirebaseConstants.Users.KEY_IMAGE)
        );

        Bitmap imageBit = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        this.binding.imagePreview.setImageBitmap(imageBit);
    }

    @NonNull
    private Call getOnFailFirebaseConnection() {
        return data -> {
            this.flagFirstLogin = true;
            String message = (String) data.get(MESSAGE);
            MessagesAppGenerator.showToast(getApplicationContext(), message, FirebaseConstants.Messages.FAIL_GET_RESPONSE);
        };
    }

}