package com.flintcore.chat_app_android_22.utilities.models.generator;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_CONFIRM_PASS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_EMAIL;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_LOGIN_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_PASS;

import android.graphics.Bitmap;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class DocumentValidators {

    public static class UserValidator {
        public UserValidator() {
        }

        public void validateImage(Bitmap image, CallResult<Exception> exCallMessage) throws Exception {

            if (Objects.isNull(image)) {
                throw getException("Select a image");
            }

        }

        public void validateUser(Bitmap image, User user,
                                           Map<String, Object> additionalFields,
                                           @NonNull CallResult<Optional<User>> onCompleteValidation,
                                           @NonNull CallResult<Exception> onFailMessageCall) {

            try {
                if (Objects.isNull(user)) {
                    throw getException("Fill the form.");
                }

                validateImage(image, onFailMessageCall);
                user.setImage(encodeImage(image));

                this.validateUser(user, additionalFields, onCompleteValidation, onFailMessageCall);

            } catch (Exception e) {
                onFailMessageCall.onCall(e);
            }


        }

        public void validateUserCredentials(UserAccess userAccess,
                                 Map<String, Object> additionalFields) throws Exception{
            try {
                String email = userAccess.getEmail().trim();

                if (email.isEmpty()) {
                    throw getException("Fill user email");
                }
                userAccess.setEmail(email);

                String pass = userAccess.getPass().trim();

                if (pass.isEmpty()) {
                    throw getException("Fill user pass");
                }

                if (pass.length() < 5){
                    throw getException("For security:\nPassword must be larger.");
                }

                Object confirmPass = additionalFields.getOrDefault(KEY_CONFIRM_PASS, "");
                if (!Objects.equals(pass, confirmPass)){
                    throw getException("Passwords must be equals");
                }

                userAccess.setPass(pass);
            } catch (Exception e) {
                throw e;
            }

        }

        public void validateUser(User user,
                                           Map<String, Object> additionalFields,
                                           @NonNull CallResult<Optional<User>> onCompleteValidation,
                                           @NonNull CallResult<Exception> onFailMessageCall) {

            try {
                if (Objects.isNull(user)) {
                    throw getException("Fill the form.");
                }

                String alias = user.getAlias().trim();
                if (alias.isEmpty()) {
                    throw getException("Fill alias field");
                }
                user.setAlias(alias);

                UserAccess userAccess = user.getUserAccess();
                this.validateUserCredentials(userAccess, additionalFields);

                onCompleteValidation.onCall(Optional.of(user));

            } catch (Exception e) {
                onFailMessageCall.onCall(e);
            }

        }

        private Exception getException(String message) {
            return new RuntimeException(message);
        }

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
                String pass = access.getPass();

                validateCredentials(email, pass, onFail);

                String confirmPass = values.getOrDefault(KEY_CONFIRM_PASS, "")
                        .toString();

                if (confirmPass.trim().isEmpty()) {
                    throw new RuntimeException("Password confirmation must be filled");
                }

                if (!Objects.equals(pass, confirmPass)) {
                    throw new RuntimeException("Passwords must be equals");
                }

//                Applying data.
                User user = new User();

                user.setImage(encodedImage);
                user.setAlias(alias.toString());

                UserAccess login = new UserAccess();
                login.setEmail(email);
                login.setPass(Encryptions.encrypt(pass));

                user.setUserAccess(login);

                return Optional.of(user);

            } catch (Exception e) {
                callOnFailException(values, onFail, e);

            }

            return Optional.empty();
        }

        private void callOnFailException(Call onFail, Exception e) {
            Map<String, Object> values = new HashMap<>();
            values.put(FirebaseConstants.Results.MESSAGE, e.getMessage());
            onFail.start(values);
        }

        private void validateCredentials(String email, String pass, Call onFail) {
            try {
                validateCredentialValues(email, pass);
            } catch (Exception ex) {
                callOnFailException(onFail, ex);
            }
        }

        public void validateCredentials(Map<String, Object> values, @Nullable Call onSuccess, Call onFail) {
            try {

                String email = (String) values.get(KEY_EMAIL);
                String pass = (String) values.get(KEY_PASS);

                validateCredentialValues(email, pass);

                if (Objects.nonNull(onSuccess)) {
//                    TODO
                    onSuccess.start(values);
                }
            } catch (Exception ex) {
                callOnFailException(values, onFail, ex);
            }
        }

        private String encodeImage(Bitmap bitmap) {
            int quality = 50;

            int previewWidth = 200;
            int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

            Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
            ByteArrayOutputStream byteOutputImage = new ByteArrayOutputStream();
            previewBitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteOutputImage);

            byte[] bytedImage = byteOutputImage.toByteArray();

//            return Encryptions.encryptToString(bytedImage);
            return Encryptions.encryptAndroidImageToString(bytedImage);
        }

        private void callOnFailException(Map<String, Object> values, Call onFail, Exception e) {
            values.clear();
            values.put(FirebaseConstants.Results.MESSAGE, e.getMessage());
            onFail.start(values);
        }

        private void validateCredentialValues(String email, String pass) {
            if (Objects.isNull(email) || email.trim().isEmpty()) {
                throw new RuntimeException("Email must be filled");
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                throw new RuntimeException("Alias must be filled");
            }

//                Password validation
            if (Objects.isNull(pass) || pass.trim().isEmpty()) {
                throw new RuntimeException("Password must be filled");
            }
        }

//        End UserValidator
    }
}
