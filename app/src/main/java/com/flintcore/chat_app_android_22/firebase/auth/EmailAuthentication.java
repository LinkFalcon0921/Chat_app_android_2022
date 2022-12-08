package com.flintcore.chat_app_android_22.firebase.auth;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.CREDENTIALS_DOES_NOT_EXISTS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.NOT_VALID_CREEDENTIALS;

import androidx.annotation.NonNull;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    public void createUserInstance(@NonNull UserAccess access,
                                   CallResult<Optional<UserAccess>> credentialsCall,
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

                    access.setId(authResult.getUser().getUid());

                    credentialsCall.onCall(Optional.of(access));
                }).addOnFailureListener(exCallMessage::onCall);
    }

    public void authenticateSignIn(UserAccess access, CallResult<Optional<UserAccess>> credentialCall,
                                   CallResult<Exception> exCallMessage) {

        this.authenticationInstance
                .signInWithEmailAndPassword(access.getEmail(), access.getPass())
                .addOnCompleteListener(task -> {

                    if (!task.isComplete() || !task.isSuccessful()) {
                        exCallMessage.onCall(getException(CREDENTIALS_DOES_NOT_EXISTS));
                        return;
                    }
                    FirebaseUser user = task.getResult().getUser();

                    if (Objects.isNull(user)) {
                        exCallMessage.onCall(getException(NOT_VALID_CREEDENTIALS));
                        return;
                    }

                    access.setId(user.getUid());
                    credentialCall.onCall(Optional.of(access));
                }).addOnFailureListener(exCallMessage::onCall);

    }

    @NonNull
    private RuntimeException getException(String message) {
        return new RuntimeException(message);
    }


}
