package com.flintcore.chat_app_android_22.firebase.auth;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.CREDENTIALS_DOES_NOT_EXISTS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.NOT_VALID_CREEDENTIALS;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Objects;
import java.util.Optional;

public class EmailAuthentication {

    private static EmailAuthentication emailAuthentication;
    private FirebaseAuth authenticationInstance;

    private EmailAuthentication(CallResult<Exception> exCall) {
        try {
            this.authenticationInstance = FirebaseAuth.getInstance();
        } catch (Exception exception) {
            exCall.onCall(exception);
        }
    }

    public static EmailAuthentication getInstance(CallResult<Exception> exCall) {
        if (Objects.isNull(emailAuthentication)) {
            emailAuthentication = new EmailAuthentication(exCall);
        }

        return emailAuthentication;
    }

    public boolean isLoggedInFirebase(){
        return getUserCredentials() != null;
    }

    public void createUserInstance(@NonNull UserAccess access,
                                   CallResult<Optional<User>> credentialsCall,
                                   CallResult<Exception> exCallMessage) {

        this.authenticationInstance
                .createUserWithEmailAndPassword(access.getEmail(), access.getPass())
                .addOnCompleteListener(task -> {

                    if (!task.isComplete() || !task.isSuccessful()) {
                        //TODO Add logic to get Exception
                        exCallMessage.onCall(getException(NOT_VALID_CREEDENTIALS));
                        return;
                    }

                    AuthResult authResult = task.getResult();
                    User user = new User();

                    user.setId(authResult.getUser().getUid());
                    user.setUserAccess(access);

                    credentialsCall.onCall(Optional.of(user));
                }).addOnFailureListener(exCallMessage::onCall);
    }

    public void authenticateSignIn(UserAccess access, CallResult<Optional<User>> credentialCall,
                                   CallResult<Exception> exCallMessage) {

        this.authenticationInstance
                .signInWithEmailAndPassword(access.getEmail(), access.getPass())
                .addOnCompleteListener(task -> {

                    if (!task.isComplete() || !task.isSuccessful()) {
                        exCallMessage.onCall(getException(CREDENTIALS_DOES_NOT_EXISTS));
                        return;
                    }
                    FirebaseUser fbUser = task.getResult().getUser();

                    if (Objects.isNull(fbUser)) {
                        exCallMessage.onCall(getException(NOT_VALID_CREEDENTIALS));
                        return;
                    }

                    User user = new User();
                    user.setId(fbUser.getUid());
                    credentialCall.onCall(Optional.of(user));
                }).addOnFailureListener(exCallMessage::onCall);

    }

    /**
     * User image and user image as uri
     */
    public void setCurrentUserData(Uri imageUri, User user, CallResult<User> onSuccess, CallResult<Exception> exCall) {

        UserProfileChangeRequest userProfileUpdate = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getAlias())
                .setPhotoUri(imageUri)
                .build();

        this.authenticationInstance.getCurrentUser().updateProfile(userProfileUpdate)
                .addOnSuccessListener(result -> onSuccess.onCall(user))
                .addOnFailureListener(exCall::onCall);

    }

    public FirebaseUser getUserCredentials() {
        return this.authenticationInstance.getCurrentUser();
    }

    @NonNull
    private RuntimeException getException(String message) {
        return new RuntimeException(message);
    }


    public String getUserLoggedId() {
        return this.getUserCredentials().getUid();
    }
}
