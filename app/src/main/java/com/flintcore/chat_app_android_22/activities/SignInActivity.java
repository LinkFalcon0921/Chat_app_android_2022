package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_EMAIL;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IS_SIGNED_IN;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;

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
import com.flintcore.chat_app_android_22.firebase.firestore.users.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.UserConstants;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;
import com.flintcore.chat_app_android_22.utilities.models.generator.DocumentValidators;
import com.flintcore.chat_app_android_22.utilities.views.DefaultConfigs;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SignInActivity extends AppCompatActivity {

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


        setFirebaseInstance();

        this.preferencesManager = new PreferencesManager(getApplicationContext(),
                FirebaseConstants.SharedReferences.KEY_CHAT_USER_LOGGED_PREFERENCES);

        if (this.emailAuthentication.isLoggedInFirebase()) {
            startActivity(goToMainIntent());
        }

        setValidator();
        configureFields();
        setListenersButtons();
    }

    private void setValidator() {
        this.userValidator = new DocumentValidators.UserValidator();
    }

    private void setFirebaseInstance() {
        this.userCollection = UserCollection.getInstance(getOnFailFirebaseConnection());

        this.emailAuthentication = EmailAuthentication.getInstance(getExceptionCallResultDefault());
    }

    @NonNull
    private CallResult<Exception> getExceptionCallResultDefault() {
        return fail -> {
            MessagesAppGenerator
                    .showToast(getApplicationContext(), fail, FAIL_GET_RESPONSE);
            this.binding.signInBtn.setEnabled(true);
            startFirebaseRequest(false);
        };
    }

    private void configureFields() {
        this.binding.emailTxt.setFilters(
                new InputFilter[]{DefaultConfigs.InputFilters.EMAIL_INPUT_FILTER});

        this.binding.passTxt.setFilters(
                new InputFilter[]{DefaultConfigs.InputFilters.PASS_INPUT_FILTER});
    }

    //    Set Listeners of the buttons
    public void setListenersButtons() {
        this.binding.signUpBtn.setOnClickListener(v -> {
            v.setEnabled(false);
            Intent signUpIntent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(signUpIntent);
        });

        this.binding.signInBtn.setOnClickListener(v -> signInToFirebase());
    }

    private void signInToFirebase() {
        startFirebaseRequest(true);

        String email = this.binding.emailTxt.getText().toString();
        String pass = this.binding.passTxt.getText().toString();

//        Always create this class to login
        UserAccess access = new UserAccess();
        access.setEmail(email);
        access.setPass(pass);

        CallResult<UserAccess> onFieldsValidated = accessValidated -> {

            CallResult<Optional<User>> onUserFound = accessOptional -> {
                if (!accessOptional.isPresent()) {
                    MessagesAppGenerator.showToast(getApplicationContext(),
                            FirebaseConstants.Messages.NOT_VALID_CREEDENTIALS, FAIL_GET_RESPONSE);
                    return;
                }

                CallResult<Exception> onFailGetUser = getExceptionCallResultDefault();

                User userAccessed = accessOptional.get();

                List<QueryCondition<String, Object>> whereConditions = CollectionsHelper.getArrayList();

//                Call for get the data of user.
                CallResult<Task<QuerySnapshot>> callOnGetUser = userSnapshot -> {
                    if (!userSnapshot.isComplete() || !userSnapshot.isSuccessful()) {
                        callOnFailUserInfo(onFailGetUser);
                        return;
                    }

                    List<DocumentSnapshot> documents = userSnapshot.getResult().getDocuments();
                    if (documents.isEmpty()) {
                        callOnFailUserInfo(onFailGetUser);
                        return;
                    }

                    User userFound = documents.get(0).toObject(User.class);

                    if (Objects.isNull(userFound)) {
                        callOnFailUserInfo(onFailGetUser);
                        return;
                    }

                    savePreferences(userFound);
//                    Go to main
                    startActivity(goToMainIntent());
                };
                this.userCollection.getCollectionById(
                        userAccessed, whereConditions,
                        callOnGetUser, onFailGetUser);

            };

            CallResult<Exception> onFailAuth = getExceptionCallResultDefault();

            this.emailAuthentication
                    .authenticateSignIn(access, onUserFound, onFailAuth);
        };

        this.userValidator.validateUserCredentials(access, onFieldsValidated,
                getExceptionCallResultDefault());

    }

    private void callOnFailUserInfo(CallResult<Exception> onFailGetUser) {
        RuntimeException exception = new RuntimeException(FirebaseConstants.Messages.CREDENTIALS_DOES_NOT_EXISTS);
        onFailGetUser.onCall(exception);
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

        this.preferencesManager.put(KEY_EMAIL,
                user.getUserAccess().getEmail());

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
            startFirebaseRequest(false);
            String message = data.get(MESSAGE).toString();
            showMessage(message);
        };
    }

    //    Show the progress bar
    private void startFirebaseRequest(boolean isLoading) {
        if (isLoading) {
            this.binding.signInBtn.setEnabled(false);
            this.binding.signUpBtn.setVisibility(View.INVISIBLE);
            this.binding.progressBarSign.setVisibility(View.VISIBLE);
            return;
        }

        this.binding.signInBtn.setEnabled(true);
        this.binding.signInBtn.setVisibility(View.VISIBLE);
        this.binding.signUpBtn.setVisibility(View.VISIBLE);
        this.binding.progressBarSign.setVisibility(View.INVISIBLE);

    }
}