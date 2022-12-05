package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_EMAIL;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IS_SIGNED_IN;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_LOGIN_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_PASS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.MainActivity;
import com.flintcore.chat_app_android_22.databinding.ActivitySignInBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.firestore.UserCollection;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.flintcore.chat_app_android_22.utilities.models.generator.DocumentValidators;
import com.flintcore.chat_app_android_22.utilities.views.DefaultConfigs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    public static final String DAT_STR = ".";
    private ActivitySignInBinding binding;
    private PreferencesManager preferencesManager;
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

        this.userCollection = UserCollection.getInstance(getOnFailFirebaseConnection());
        this.userValidator = new DocumentValidators.UserValidator();

        configurateFields();
        setListenersButtons();
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
        Map<String, Object> values = new HashMap<>();

        String email = this.binding.emailTxt.getText().toString();
        String pass = this.binding.passTxt.getText().toString();

//        Always create this class to login
        values.put(KEY_EMAIL, email);
        values.put(KEY_PASS, pass);

        Call onFail = data ->  {
            startFirebaseRequest(false);
            getOnFailFirebaseConnection().start(data);
        };

        Call onSuccess = data -> {
            Call onSuccessLogged = loggedData -> {
                startFirebaseRequest(true);
                savePreferences(loggedData);

                startActivity(goToMainIntent());
            };

            data.clear();
            data.put(KEY_LOGIN_OBJ.concat(DAT_STR).concat(KEY_EMAIL), email);
            data.put(KEY_LOGIN_OBJ.concat(DAT_STR).concat(KEY_PASS), Encryptions.encrypt(pass));

            this.userCollection
                    .getCollection(data, onSuccessLogged, onFail);
        };

        this.userValidator.validateCredentials(values, onSuccess, onFail);
    }

    private void savePreferences(Map<String, Object> data) {
        this.preferencesManager.put(KEY_IS_SIGNED_IN,
                true);

        this.preferencesManager.put(KEY_USER_ID,
                data.get(KEY_USER_ID).toString());

        this.preferencesManager.put(KEY_ALIAS,
                data.get(KEY_ALIAS).toString());

        this.preferencesManager.put(KEY_IMAGE,
                data.get(KEY_IMAGE).toString());
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