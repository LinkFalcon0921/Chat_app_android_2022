package com.flintcore.chat_app_android_22.utilities.encrypt;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.KeyEncryption.KEY;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public final class Encryptions {

    private interface EncryptionValues {
        int ENCRYPT_DIVIDE_VALUE = 7;
        int ENCRYPT_DIFF_VALUE = 12;
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

    public static String encrypt(String text) {
        return text.chars()
                .map(v -> (v + EncryptionValues.ENCRYPT_DIVIDE_VALUE) -
                        EncryptionValues.ENCRYPT_DIFF_VALUE)
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    public static String decrypt(String text) {
        return text.chars()
                .map(v -> (v - EncryptionValues.ENCRYPT_DIVIDE_VALUE) +
                        EncryptionValues.ENCRYPT_DIFF_VALUE)
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
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
