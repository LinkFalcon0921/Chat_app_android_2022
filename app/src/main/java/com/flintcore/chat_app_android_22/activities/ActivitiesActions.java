package com.flintcore.chat_app_android_22.activities;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.encrypt.ImageFormatter;

import java.io.FileNotFoundException;
import java.util.Objects;

public class ActivitiesActions {

    public static abstract class ActivitiesResults {
        public static ActivityResultLauncher<Intent> getImagePicker(@NonNull ActivityResultCaller activity, CallResult<Uri> onGet){
            ActivityResultCallback<ActivityResult> activityResultActivityResultCallback = imageUri -> {
                Intent uriData = imageUri.getData();
                if (imageUri.getResultCode() != RESULT_OK && Objects.isNull(uriData)) {
                    return;
                }
                Uri uri = uriData.getData();

                if (Objects.isNull(uri)) {
                    return;
                }

                onGet.onCall(uri);
            };

            return activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    activityResultActivityResultCallback
            );
        }



    }
}
