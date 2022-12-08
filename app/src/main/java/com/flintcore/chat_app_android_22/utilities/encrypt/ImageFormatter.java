package com.flintcore.chat_app_android_22.utilities.encrypt;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class ImageFormatter {

    public static Bitmap getImageAs(ContentResolver contentResolver, Uri imageUri) throws FileNotFoundException {
        InputStream imageUriStream = contentResolver.openInputStream(imageUri);
        return BitmapFactory.decodeStream(imageUriStream);
    }
}
