package com.flintcore.chat_app_android_22.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.flintcore.chat_app_android_22.databinding.ActivityChatSimpleBinding;

public class ChatSimpleActivity extends AppCompatActivity {

    private ActivityChatSimpleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityChatSimpleBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
    }
}