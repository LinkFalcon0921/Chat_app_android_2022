package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_CONFIRM_PASS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IS_SIGNED_IN;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.databinding.ActivitySignUpBinding;
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
import com.flintcore.chat_app_android_22.utilities.encrypt.ImageFormatter;
import com.flintcore.chat_app_android_22.utilities.models.generator.DocumentValidators;
import com.flintcore.chat_app_android_22.utilities.views.DefaultConfigs;
import com.makeramen.roundedimageview.RoundedDrawable;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferencesManager preferencesManager;

    private Uri imageUriData;
    private EmailAuthentication emailAuthentication;
    private UserCollection userCollection;
    private DocumentValidators.UserValidator userValidator;

    //    Activity results
//    pick image
    private ActivityResultLauncher<Intent> imagePickerActionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.preferencesManager = new PreferencesManager(getApplicationContext(),
                FirebaseConstants.SharedReferences.KEY_CHAT_USER_LOGGED_PREFERENCES);

        setFirebaseInstance();

        if (this.emailAuthentication.isLoggedInFirebase()) {
            startActivity(goToMainIntent());
        }

        configureFields();
        setUpActivityResults();
        setListenersButtons();

    }

    private void setUpActivityResults() {
        setImagePicker();
    }

    private void setImagePicker() {
        CallResult<Uri> uriCallResult = uri -> {
            try {
                if (Objects.isNull(imageUriData)) {
                    return;
                }

                Bitmap previewSelected = ImageFormatter
                        .getImageAs(getContentResolver(), imageUriData);

                this.binding.imagePreview.setImageBitmap(previewSelected);

                this.binding.imagePreviewTxt.setVisibility(View.GONE);

            } catch (FileNotFoundException e) {
                getExceptionCallResultDefault().onCall(e);
            }

        };
        this.imagePickerActionResult = ActivitiesActions
                        .ActivitiesResults.getImagePicker(this, uriCallResult);
    }

    private void setFirebaseInstance() {
        this.userCollection = UserCollection.getInstance(getOnFailFirebaseConnection());

        this.userValidator = new DocumentValidators.UserValidator();

        this.emailAuthentication = EmailAuthentication
                .getInstance(getExceptionCallResultDefault());
    }

    @NonNull
    private CallResult<Exception> getExceptionCallResultDefault() {
        return fail -> {
            startFirebaseRequest(false);
            MessagesAppGenerator.showToast(getApplicationContext(), fail, FAIL_GET_RESPONSE);
            this.binding.signUpBtn.setEnabled(true);
            this.binding.imagePreview.setEnabled(true);
        };

    }

    private void configureFields() {
        this.binding.aliasTxt.setFilters(
                new InputFilter[]{DefaultConfigs.InputFilters.NAME_INPUT_FILTER});

        this.binding.emailTxt.setFilters(
                new InputFilter[]{DefaultConfigs.InputFilters.EMAIL_INPUT_FILTER});

        this.binding.passTxt.setFilters(
                new InputFilter[]{DefaultConfigs.InputFilters.PASS_INPUT_FILTER});
        this.binding.passConfirmTxt.setFilters(
                new InputFilter[]{DefaultConfigs.InputFilters.PASS_INPUT_FILTER});

    }

    public void showMessage(String message) {
        if (Objects.isNull(message)) {
            message = FAIL_GET_RESPONSE;
        }

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    private Call getOnFailFirebaseConnection() {
        return data -> showMessage(data.get(MESSAGE).toString());
    }

    //    Set Listeners of the buttons
    public void setListenersButtons() {
//     TODO  Return to view SignIn
//        this.binding.signInBtn.setOnClickListener(v -> onBackPressed());

        this.binding.signUpBtn.setOnClickListener(v -> {
            v.setEnabled(false);
            signUpToFirebase();
        });

        this.binding.imagePreview.setOnClickListener(v -> {
            v.setEnabled(false);
            Intent imagePick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePickerActionResult.launch(imagePick);
        });
    }

    private void signUpToFirebase() {

        startFirebaseRequest(true);

        Bitmap imageBitmap = null;
        Drawable drawable = this.binding.imagePreview.getDrawable();

        if (Objects.nonNull(drawable)) {
//            Beware with RoundedDrawable.toBitmap()
            imageBitmap = ((RoundedDrawable) drawable).getSourceBitmap();
        }

        String alias = this.binding.aliasTxt.getText().toString();
        String email = this.binding.emailTxt.getText().toString();

//        Always create this class to login
        UserAccess access = new UserAccess();
        String pass = this.binding.passTxt.getText().toString();
        String conf_pass = this.binding.passConfirmTxt.getText().toString();

        access.setEmail(email);
        access.setPass(pass);

        User newUser = new User();
        newUser.setAlias(alias);
        newUser.setUserAccess(access);
        HashMap<String, Object> values = CollectionsHelper.getHashMap();

        // Fill values in map to validate
        values.put(KEY_CONFIRM_PASS, conf_pass);

//        When the user is validated
        CallResult<Optional<User>> onValidateUser = userValidated -> {
            if (!userValidated.isPresent()) {
                return;
            }
            User user = userValidated.get();
            this.imageUriData = Uri.parse(user.getImage());
//            When user credentials is valid
            CallResult<Optional<User>> onValidUserAccess = accessValidated -> {
                if (!accessValidated.isPresent()) {
                    return;
                }

                user.setId(accessValidated.get().getId());

//                Add ImageUri and name to Firebase Authentication
                CallResult<Void> onSuccess = userWithAccess -> {
//                    User data in the database
                    CallResult<User> onUserSettled = userDone -> {
                        this.imageUriData = null;
                        savePreferences(userDone);
                        startActivity(goToMainIntent());
                    };

//                    If the data was not settled in Firebase
                    CallResult<Exception> notSettledDataUser = getExceptionCallResultDefault();

                    this.emailAuthentication.setCurrentUserData(this.imageUriData, user,
                            onUserSettled, notSettledDataUser);
                };

                CallResult<Exception> onFail = getExceptionCallResultDefault();

                // Insert the user
                List<QueryCondition<String, Object>> queryConditions = CollectionsHelper.getArrayList();

                this.userCollection.addCollectionById(user, queryConditions, onSuccess, onFail);
            };

//            Add email credentials
            this.emailAuthentication.createUserInstance(access, onValidUserAccess, getExceptionCallResultDefault());
        };

        CallResult<Exception> onFailValidation = getExceptionCallResultDefault();

        this.userValidator.validateUser(imageBitmap, newUser, values, onValidateUser, onFailValidation);
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

    private Intent goToMainIntent() {
        Intent logUpIntent = new Intent(getApplicationContext(), MainActivity.class);
        logUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return logUpIntent;
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