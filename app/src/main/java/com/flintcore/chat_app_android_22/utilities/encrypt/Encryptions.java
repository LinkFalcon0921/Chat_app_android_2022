package com.flintcore.chat_app_android_22.utilities.encrypt;

import static com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions.EncryptionValues.ENCRYPT_DIFF_VALUE;
import static com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions.EncryptionValues.ENCRYPT_DIVIDE_VALUE;

import android.util.Base64;

import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class Encryptions {

    public static final String DEFAULT_DELIMITER = ",";

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
        byte[] bytesText = text.getBytes(StandardCharsets.UTF_8);
        StringJoiner joiner = new StringJoiner(DEFAULT_DELIMITER);

        for (byte b : bytesText) {
            byte actByteDiv = (byte) (b / ENCRYPT_DIVIDE_VALUE);
            byte actByteMod = (byte) (b % ENCRYPT_DIFF_VALUE);

            joiner.add(Byte.toString(actByteDiv))
                    .add(Byte.toString(actByteMod));
        }

        return joiner.toString();

    }

    public static String decrypt(String text) {
        String[] bytesText = text.split(DEFAULT_DELIMITER);

        int newLength = bytesText.length / 2;
        int additionalLength = bytesText.length % 2;
        byte[] characters = new byte[newLength + additionalLength];

        int pos = 0, newPos = 0;
        while (pos < bytesText.length) {
//           label : pos Increment while the data is read
            byte FpartByte = asByte((Byte.parseByte(bytesText[pos++]) * ENCRYPT_DIVIDE_VALUE));
            byte SpartByte = Byte.parseByte(bytesText[pos++]);

            characters[newPos++] = (byte) (FpartByte + SpartByte);
        }

        return new String(characters, StandardCharsets.UTF_8);

    }

    private static byte asByte(int i) {
        return (byte) i;
    }

    public static String encryptAndroidImageToString(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static byte[] decryptAndroidImageFromString(String data) {
        return Base64.decode(data, Base64.DEFAULT);
    }
}
