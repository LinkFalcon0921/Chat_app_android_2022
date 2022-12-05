package com.flintcore.chat_app_android_22.utilities.encrypt;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.KeyEncryption.KEY;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public final class Encryptions {

    private static Encryptions encryptions;
    private Cipher encrypter;
    private SecretKey keySecret;

    private Encryptions() {
    }

    public static Encryptions getInstance() {
        if (Objects.isNull(encryptions)) {
            encryptions = new Encryptions();
            try {
                encryptions.keySecret = KeyGenerator.getInstance(KEY).generateKey();
                encryptions.encrypter = Cipher.getInstance(KEY);

            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        }

        return encryptions;
    }

    public static String encrypt(String text) {
        try {
            Cipher encrypter = getInstance().encrypter;
            byte[] textByted = text.getBytes(StandardCharsets.UTF_8);

            encrypter.init(Cipher.ENCRYPT_MODE, encryptions.keySecret);

            text = new String(encrypter.doFinal(textByted), StandardCharsets.UTF_8);

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return text;
    }

    public static String decrypt(String text) {
        try {
            Cipher encrypter = getInstance().encrypter;
            SecretKey secretKey = encryptions.keySecret;

            byte[] textByted = text.getBytes(StandardCharsets.UTF_8);

            encrypter.init(Cipher.DECRYPT_MODE, secretKey);

            text = new String(encrypter.doFinal(textByted), StandardCharsets.UTF_8);

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return text;
    }

    public static byte[] encrypt(byte[] data) {
        try {
            Cipher encrypter = getInstance().encrypter;

            encrypter.init(Cipher.ENCRYPT_MODE, encryptions.keySecret);

            data = encrypter.doFinal(data);

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static byte[] decrypt(byte[] data) {
        try {
            Cipher encrypter = getInstance().encrypter;
            SecretKey secretKey = encryptions.keySecret;

            encrypter.init(Cipher.DECRYPT_MODE, secretKey);

            data = encrypter.doFinal(data);

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static String encryptToString(byte[] data) {
        try {
            Cipher encrypter = getInstance().encrypter;

            encrypter.init(Cipher.ENCRYPT_MODE, encryptions.keySecret);

            data = encrypter.doFinal(data);

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return new String(data, StandardCharsets.UTF_8);
    }

    public static byte[] decryptFromString(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        try {
            Cipher encrypter = getInstance().encrypter;
            SecretKey secretKey = encryptions.keySecret;

            encrypter.init(Cipher.DECRYPT_MODE, secretKey);

            dataBytes = data.getBytes(StandardCharsets.UTF_8);

            dataBytes = encrypter.doFinal(dataBytes);

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return dataBytes;
    }

    public static String encryptAndroidImageToString(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static byte[] decryptAndroidImageFromString(String data) {
        return Base64.decode(data, Base64.DEFAULT);
    }
}
