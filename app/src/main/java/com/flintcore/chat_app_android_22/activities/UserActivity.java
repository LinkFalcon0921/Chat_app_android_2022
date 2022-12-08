package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.NO_USERS_AVAILABLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.adapters.RecyclerUserView;
import com.flintcore.chat_app_android_22.databinding.ActivityUserBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.auth.EmailAuthentication;
import com.flintcore.chat_app_android_22.firebase.firestore.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.listeners.OnRecyclerItemListener;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


public class UserActivity extends AppCompatActivity implements OnRecyclerItemListener<User> {

    private ActivityUserBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private UserCollection userCollection;
    private EmailAuthentication emailAuthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.loggedPreferencesManager = new PreferencesManager(getApplicationContext(),
                FirebaseConstants.SharedReferences.KEY_CHAT_USER_LOGGED_PREFERENCES);

        this.userCollection = UserCollection.getInstance(getDefaultOnFail());
        this.emailAuthentication = EmailAuthentication.getInstance(showMessageCallResultException());
        setListeners();

        loadUsersInView();
    }

    private void setListeners() {
        this.binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private Call getDefaultOnFail() {
        return unused -> {
            showErrorMessageOnLoad();
        };
    }

    //    Charge all the users available in the database.
//    Recycler creation here

    private void loadUsersInView() {
        startLoadUsers(true);

        if (!this.emailAuthentication.isLoggedInFirebase()) {
//           TODO Test this. In case delete the user while loading other users.
            getParent().recreate();
            finish();
            return;
        }

        String userId = getLoggedUserId();


        CallResult<Collection<User>> onSuccess = users -> {
            startLoadUsers(false);
            if (Objects.isNull(users) || users.isEmpty()) {
                return;
            }

//              Recycler view
            RecyclerUserView recyclerAdapter = new RecyclerUserView(users, this);
            this.binding.recyclerUserList.setAdapter(recyclerAdapter);
            this.binding.recyclerUserList.setVisibility(View.VISIBLE);
            this.binding.progressBar.setVisibility(View.GONE);
        };

        CallResult<Exception> onFail = fail -> {
            startLoadUsers(false);
            this.binding.errorTxt.setText(NO_USERS_AVAILABLE);
            this.binding.errorTxt.setVisibility(View.VISIBLE);
        };

        this.userCollection.getCollections(userId, onSuccess, onFail);

    }

    private String getLoggedUserId() {
        return this.emailAuthentication.getUserLoggedId();
    }

    private void showErrorMessageOnLoad() {
        startLoadUsers(false);
        this.binding.errorTxt.setText(NO_USERS_AVAILABLE);
        this.binding.errorTxt.setVisibility(View.VISIBLE);
    }

    private void startLoadUsers(boolean isLoading) {
        if (isLoading) {
            this.binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            this.binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //    Recycler view item selection

    @Override
    public void onClick(User user) {
        Intent chatUserIntent = new Intent(getApplicationContext(), ChatSimpleActivity.class);
        chatUserIntent.putExtra(FirebaseConstants.Users.KEY_USER_OBJ, user);
        startActivity(chatUserIntent);
        finish();
    }

    @NonNull
    private CallResult<Exception> showMessageCallResultException() {
        return fail -> MessagesAppGenerator
                .showToast(getApplicationContext(), fail, FAIL_GET_RESPONSE);
    }
}