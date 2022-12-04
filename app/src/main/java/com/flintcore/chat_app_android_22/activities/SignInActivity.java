package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IS_SIGNED_IN;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.databinding.ActivitySignInBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

//        this.preferencesManager = new PreferencesManager(getApplicationContext(),
//                FirebaseConstants.SharedReferences.CHAT_USER_LOGGED_PREFERENCES);
//
//        if(this.preferencesManager.getBoolean(KEY_IS_SIGNED_IN)){
//            startActivity(goToMainIntent());
//        }

        setListenersButtons();
    }

    //    Set Listeners of the buttons
    public void setListenersButtons(){
        this.binding.signUpBtn.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(signUpIntent);
        });
    }
}