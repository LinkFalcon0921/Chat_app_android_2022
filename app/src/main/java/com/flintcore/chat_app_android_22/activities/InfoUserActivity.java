package com.flintcore.chat_app_android_22.activities;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.databinding.ActivityInfoUserBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users;
import com.flintcore.chat_app_android_22.firebase.firestore.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.encrypt.ImageFormatter;

public class InfoUserActivity extends AppCompatActivity {

    private ActivityInfoUserBinding binding;
    private User relatedUser;
    private UserCollection userCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFirebaseInstance();
        setUserInfoInView();
        setButtonsListeners();

    }

    private void setButtonsListeners() {
        this.binding.backBtn.setOnClickListener(v -> onBackPressed());
        this.binding.deleteBtn.setOnClickListener(v -> {/*TODO Delete the user logic*/});
    }

    private void setUserInfoInView() {
        this.relatedUser = (User) getIntent().getSerializableExtra(Users.KEY_USER_OBJ);

        String relatedUserImage = this.relatedUser.getImage();
        String email = this.relatedUser.getUserAccess().getEmail();

        Bitmap imageBitmap = ImageFormatter.getImageAs(getContentResolver(), relatedUserImage);
        this.binding.imagePreview.setImageBitmap(imageBitmap);

        this.binding.topActionBarTxt.setText(this.relatedUser.getAlias());
        this.binding.secondBodyTxt.setText(email);
    }

    private void setFirebaseInstance() {
        this.userCollection = UserCollection.getInstance(getExceptionOnLoadFirebaseInstanceCallResult());
    }

    @NonNull
    private CallResult<Exception> getExceptionOnLoadFirebaseInstanceCallResult() {
        return fail -> MessagesAppGenerator.showToast(getApplicationContext(),
                fail, Messages.FAIL_GET_RESPONSE);
    }

}