package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_ALIAS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_CONFIRM_PASS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IMAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_IS_SIGNED_IN;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_PASS;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.MainActivity;
import com.flintcore.chat_app_android_22.databinding.ActivitySignUpBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.firestore.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.UserAccess;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.models.generator.DocumentGenerators;
import com.makeramen.roundedimageview.RoundedDrawable;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SignUpActivity extends AppCompatActivity {


    private ActivitySignUpBinding binding;
    private PreferencesManager preferencesManager;
    private UserCollection collection;
    private DocumentGenerators.UserGenerator userGenerator;

    //    Activity results
//    pick image
    private final ActivityResultLauncher<Intent> imagePickerActionResult =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Intent imageIntent = result.getData();
                        if (result.getResultCode() == RESULT_OK && Objects.nonNull(imageIntent)) {
                            try {
                                Uri imageData = imageIntent.getData();
                                if (Objects.isNull(imageData)) {
                                    return;
                                }

                                InputStream imageUriStream = getContentResolver().openInputStream(imageData);
                                Bitmap previewSelected = BitmapFactory.decodeStream(imageUriStream);
                                this.binding.imagePreview.setImageBitmap(previewSelected);

                                this.binding.imagePreviewTxt.setVisibility(View.GONE);

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.collection = UserCollection.getInstance(getOnFailFirebaseConnection());
        this.userGenerator = new DocumentGenerators.UserGenerator();


        setListenersButtons();

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

            if (Objects.isNull(message)) {
                Toast.makeText(getApplicationContext(), FAIL_GET_RESPONSE, Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        };
    }

    //    Set Listeners of the buttons
    public void setListenersButtons() {
//     TODO  Return to view SignIn
//        this.binding.signInBtn.setOnClickListener(v -> onBackPressed());

        this.binding.signUpBtn.setOnClickListener(v -> signUpToFirestore());
        this.binding.imagePreview.setOnClickListener(v -> {
            Intent imagePick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.imagePickerActionResult.launch(imagePick);
        });
    }

    private void signUpToFirestore() {
        Map<String, Object> values = new HashMap<>();

        Bitmap imageBitmap = null;
        Drawable drawable = this.binding.imagePreview.getDrawable();

        if (Objects.nonNull(drawable)) {
//            Beware with RoundedDrawable.toBitmap()
            imageBitmap =  ((RoundedDrawable) drawable).getSourceBitmap();
        }

        String alias = this.binding.aliasTxt.getText().toString();
        String email = this.binding.emailTxt.getText().toString();

//        Always create this class to login
        UserAccess access = new UserAccess();
        String pass = this.binding.passTxt.getText().toString();
        String conf_pass = this.binding.passTxt.getText().toString();

        access.setEmail(email);
        access.setPass(pass);

        //        Fill values in map
        values.put(KEY_IMAGE, imageBitmap);

        values.put(FirebaseConstants.Users.KEY_ALIAS, alias);

        values.put(FirebaseConstants.Users.KEY_LOGIN_OBJ, access);

        values.put(KEY_CONFIRM_PASS, conf_pass);

        Call onFailDefault = data -> {
            startFirebaseRequest(false);
            String message = data.get(MESSAGE).toString();
            showMessage(message);
        };

        Optional<User> optionalUser = userGenerator.validateUserInfo(values,
                onFailDefault);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            startFirebaseRequest(true);

            Call onSuccessConnection = data -> {
                this.preferencesManager.put(KEY_IS_SIGNED_IN,
                        true);

                this.preferencesManager.put(KEY_USER_ID,
                        data.get(KEY_USER_ID).toString());

                this.preferencesManager.put(KEY_ALIAS,
                        data.get(KEY_ALIAS).toString());

                this.preferencesManager.put(KEY_IMAGE,
                        data.get(KEY_IMAGE).toString());

                startActivity(goToMainIntent());
            };

            this.collection.addCollection(user, onSuccessConnection, onFailDefault);
        }

    }

    private Intent goToMainIntent() {
        Intent logUpIntent = new Intent(getApplicationContext(), MainActivity.class);
        logUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(logInIntent);
        return logUpIntent;
    }

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