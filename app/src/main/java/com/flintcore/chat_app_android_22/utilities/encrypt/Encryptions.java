package com.flintcore.chat_app_android_22.utilities.encrypt;

import static com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions.EncryptionValues.ENCRYPT_DIFF_VALUE;
import static com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions.EncryptionValues.ENCRYPT_DIVIDE_VALUE;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class Encryptions {

    interface EncryptionValues {
        byte ENCRYPT_DIVIDE_VALUE = 7;
        byte ENCRYPT_DIFF_VALUE = 7;
    }

    private static Encryptions encryptions;

    private Encryptions() {
    }

    public static Encryptions getInstance() {
        if (Objects.isNull(encryptions)) {
            encryptions = new Encryptions();
        }

        return encryptions;
    }

//    DONE!
    public static String encrypt(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] newBytedText = new byte[bytes.length * 2];

        int newBytesCount = 0;
        for (int indx = 0; indx < bytes.length; indx++, newBytesCount+=2) {
            byte actByteDiv = (byte) (bytes[indx] / ENCRYPT_DIVIDE_VALUE);
            byte actByteMod = (byte) (bytes[indx] % ENCRYPT_DIFF_VALUE);

            newBytedText[newBytesCount] = actByteDiv;
            newBytedText[newBytesCount+1] = actByteMod;
        }

        return new String(newBytedText, StandardCharsets.UTF_8);

    }

    public static String decrypt(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] newBytedText = new byte[bytes.length / 2];

        int newBytesCount = 0;
        for (int indx = 0; indx < bytes.length; indx+=2, newBytesCount++) {
            byte actByteDiv = (byte) (bytes[indx] * ENCRYPT_DIVIDE_VALUE);
            byte actByteMod = bytes[indx+1];

            newBytedText[newBytesCount] = (byte) (actByteDiv + actByteMod);
        }

        return new String(newBytedText, StandardCharsets.UTF_8);

    }

    @Deprecated
    public static byte[] encrypt(byte[] data) {

        return data;
    }

    @Deprecated
    public static byte[] decrypt(byte[] data) {

        return data;
    }

    @Deprecated
    public static String encryptToString(byte[] data) {

        return new String(data, StandardCharsets.UTF_8);
    }

    @Deprecated
    public static byte[] decryptFromString(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        return dataBytes;
    }

    public static String encryptAndroidImageToString(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static byte[] decryptAndroidImageFromString(String data) {
        return Base64.decode(data, Base64.DEFAULT);
    }
}
