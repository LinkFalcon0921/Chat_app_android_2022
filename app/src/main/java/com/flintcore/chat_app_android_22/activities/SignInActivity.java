package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_CONFIRM_PASS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_EMAIL;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IS_SIGNED_IN;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_LOGIN_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_PASS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_OBJ;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.databinding.ActivitySignInBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.auth.EmailAuthentication;
import com.flintcore.chat_app_android_22.firebase.firestore.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.UserConstants;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.flintcore.chat_app_android_22.utilities.models.generator.DocumentValidators;
import com.flintcore.chat_app_android_22.utilities.views.DefaultConfigs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SignInActivity extends AppCompatActivity {


    public static final String DAT_STR = ".";
    private ActivitySignInBinding binding;
    private PreferencesManager preferencesManager;

    private EmailAuthentication emailAuthentication;
    private UserCollection userCollection;
    private DocumentValidators.UserValidator userValidator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.preferencesManager = new PreferencesManager(getApplicationContext(),
                FirebaseConstants.SharedReferences.KEY_CHAT_USER_LOGGED_PREFERENCES);

        if (this.preferencesManager.getBoolean(KEY_IS_SIGNED_IN)) {
            startActivity(goToMainIntent());
        }

        setFirebaseInstance();

        configurateFields();
        setListenersButtons();
    }

    private void setFirebaseInstance() {
        this.userCollection = UserCollection.getInstance(getOnFailFirebaseConnection());
        this.userValidator = new DocumentValidators.UserValidator();
        this.emailAuthentication = EmailAuthentication.getInstance(getExceptionCallResultDefault());
    }

    @NonNull
    private CallResult<Exception> getExceptionCallResultDefault() {
        return fail -> MessagesAppGenerator
                .showToast(getApplicationContext(), fail, FAIL_GET_RESPONSE);
    }

    private void configurateFields() {
        this.binding.emailTxt.setFilters(
                new InputFilter[]{DefaultConfigs.InputFilters.EMAIL_INPUT_FILTER});

        this.binding.passTxt.setFilters(
                new InputFilter[]{DefaultConfigs.InputFilters.PASS_INPUT_FILTER});
    }

    //    Set Listeners of the buttons
    public void setListenersButtons() {
        this.binding.signUpBtn.setOnClickListener(v -> {
            Intent signUpIntent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(signUpIntent);
        });

        this.binding.signInBtn.setOnClickListener(v -> signInToFirebase());
    }

    private void signInToFirebase() {

        String email = this.binding.emailTxt.getText().toString();
        String pass = this.binding.passTxt.getText().toString();

//        Always create this class to login
        UserAccess access = new UserAccess();
        access.setEmail(email);
        access.setPass(pass);

        CallResult<Optional<UserAccess>> onUserFound = accessOptional -> {
            if (!accessOptional.isPresent()) {
                MessagesAppGenerator.showToast(getApplicationContext(),
                        FirebaseConstants.Messages.NOT_VALID_CREEDENTIALS, FAIL_GET_RESPONSE);
                return;
            }

            CallResult<Exception> onFailGetUser =  fail ->
                    MessagesAppGenerator.showToast(getApplicationContext(), fail.getMessage(), FAIL_GET_RESPONSE);


            UserAccess userAccess = accessOptional.get();

            this.userCollection.getCollection(
                    userAccess.getId(),
                    user -> {
                        user.setUserAccess(access);

                        savePreferences(user);
                        toMainIntentCall();
                    },
                   onFailGetUser);

        };

        CallResult<Exception> onFailAuth = getExceptionCallResultDefault();

        this.emailAuthentication
                .authenticateSignIn(access, onUserFound, onFailAuth);

    }

    private void toMainIntentCall() {
        startActivity(goToMainIntent());
    }

    private void savePreferences(User user) {
        this.preferencesManager.put(KEY_IS_SIGNED_IN,
                true);

        this.preferencesManager.put(KEY_USER_ID,
                user.getId());

        this.userCollection
                .updateAvailable(user.getId(), UserConstants.AVAILABLE);

        this.preferencesManager.put(KEY_ALIAS,
                user.getAlias());

        this.preferencesManager.put(KEY_IMAGE,
                user.getImage());
    }


    @Deprecated
    private void savePreferences(Map<String, Object> data) {
        this.preferencesManager.put(KEY_IS_SIGNED_IN,
                true);

        User user = (User) data.get(KEY_USER_OBJ);

        this.preferencesManager.put(KEY_USER_ID,
                user.getId());

        this.userCollection
                .updateAvailable(user.getId(), UserConstants.AVAILABLE);

        this.preferencesManager.put(KEY_ALIAS,
                user.getAlias());

        this.preferencesManager.put(KEY_IMAGE,
                user.getImage());
    }

    private Intent goToMainIntent() {
        Intent logUpIntent = new Intent(getApplicationContext(), MainActivity.class);
        logUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return logUpIntent;
    }

    public void showMessage(String message) {
        if (Objects.isNull(message)) {
            message = FAIL_GET_RESPONSE;
        }

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    private Call getOnFailFirebaseConnection() {
        return data -> {
            String message = data.get(MESSAGE).toString();
            showMessage(message);
        };
    }

    //    Show the progress bar
    private void startFirebaseRequest(boolean isLoading) {
        if (isLoading) {
            this.binding.signUpBtn.setVisibility(View.INVISIBLE);
            this.binding.progressBarSign.setVisibility(View.VISIBLE);
        } else {
            this.binding.signUpBtn.setVisibility(View.VISIBLE);
            this.binding.progressBarSign.setVisibility(View.INVISIBLE);
        }
    }
}