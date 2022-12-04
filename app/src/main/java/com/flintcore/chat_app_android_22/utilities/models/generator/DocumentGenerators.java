package com.flintcore.chat_app_android_22.utilities.models.generator;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_CONFIRM_PASS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_LOGIN_OBJ;

import android.graphics.Bitmap;
import android.util.Patterns;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class DocumentGenerators {

    public static class UserGenerator {

        public Optional<User> validateUserInfo(Map<String, Object> values, Call onFail) {

            try {
//                image validation
                Bitmap imageBitmap = ((Bitmap) values.get(KEY_IMAGE));

                if (Objects.isNull(imageBitmap)) {
                    throw new RuntimeException("Select a image");
                }

//                Encode image
                String encodedImage = encodeImage(imageBitmap);

//                Alias validations
                Object alias = values.get(KEY_ALIAS);

                if (Objects.isNull(alias) || alias.toString().trim().isEmpty()) {
                    throw new RuntimeException("Alias must be filled");
                }

//                Email validation
                UserAccess access = ((UserAccess) values.get(KEY_LOGIN_OBJ));
                String email = access.getEmail();

                if (Objects.isNull(email) || email.trim().isEmpty()) {
                    throw new RuntimeException("Email must be filled");
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email.toString()).matches()) {
                    throw new RuntimeException("Alias must be filled");
                }

//                Password validation

                String pass = access.getPass();
                String confirmPass = values.getOrDefault(KEY_CONFIRM_PASS, "").toString();

                if (Objects.isNull(pass) || pass.trim().isEmpty()) {
                    throw new RuntimeException("Password must be filled");
                }

                if (confirmPass.trim().isEmpty()) {
                    throw new RuntimeException("Password confirmation must be filled");
                }

                if (!Objects.equals(pass, confirmPass)) {
                    throw new RuntimeException("Passwords must be equals");
                }

                User user = new User();

                user.setImage(encodedImage);
                user.setAlias(alias.toString());

                UserAccess login = new UserAccess();
                login.setEmail(email);
                login.setPass(pass);

                user.setLogin(login);

                return Optional.of(user);

            } catch (Exception e) {
                values.clear();
                values.put(FirebaseConstants.Results.MESSAGE, e.getMessage());
                onFail.start(values);

            }

            return Optional.empty();
        }

        private String encodeImage(Bitmap bitmap) {
            int quality = 50;

            int previewWidth = 200;
            int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

            Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
            ByteArrayOutputStream byteOutputImage = new ByteArrayOutputStream();
            previewBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteOutputImage);

            byte[] bytedImage = byteOutputImage.toByteArray();

            return Encryptions.encryptToString(bytedImage);

        }

    }

}
