package com.flintcore.chat_app_android_22.utilities.encrypt;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageFormatter {

    public static Bitmap getImageAs(ContentResolver contentResolver, String userImage) {
        byte[] imageBytes = Encryptions.decryptAndroidImageFromString(userImage);

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public static Bitmap getImageAs(@NonNull ContentResolver contentResolver, Uri imageUri) throws FileNotFoundException {
        InputStream imageUriStream = contentResolver.openInputStream(imageUri);
        return BitmapFactory.decodeStream(imageUriStream);
    }
}
