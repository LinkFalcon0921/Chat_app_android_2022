package com.flintcore.chat_app_android_22.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.flintcore.chat_app_android_22.databinding.ActivityChatSimpleBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.models.User;

public class ChatSimpleActivity extends AppCompatActivity {

    private ActivityChatSimpleBinding binding;
    private User receivedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityChatSimpleBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        setListeners();
        loadUserSelectedDetails();
    }

    private void setListeners(){
        this.binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserSelectedDetails(){
        this.receivedUser = ((User) getIntent()
                .getSerializableExtra(FirebaseConstants.Users.KEY_USER_OBJ));

        this.binding.userChat.setText(this.receivedUser.getAlias());
    }
}