package com.flintcore.chat_app_android_22.utilities.Messages;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class MessagesAppGenerator {
    public static void showToast(Context context, @Nullable String message, @NonNull String failGetResponse) {
        if (Objects.isNull(message)) {
            message = failGetResponse;
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
