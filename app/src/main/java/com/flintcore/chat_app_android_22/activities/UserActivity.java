package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.NO_USERS_AVAILABLE;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.adapters.RecyclerUserView;
import com.flintcore.chat_app_android_22.databinding.ActivityUserBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.firestore.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class UserActivity extends AppCompatActivity {

    private ActivityUserBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private UserCollection userCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.loggedPreferencesManager = new PreferencesManager(getApplicationContext(),
                FirebaseConstants.SharedReferences.KEY_CHAT_USER_LOGGED_PREFERENCES);

        this.userCollection = UserCollection.getInstance(getDefaultOnFail());
        setListeners();

        loadUsersInView();
    }

    private void setListeners(){
        this.binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private Call getDefaultOnFail() {
        return unused -> {
            showErrorMessageOnLoad();
        };
    }

//    Charge all the users available in the database.
    private void loadUsersInView() {
        startLoadUsers(true);

        String userId = this.loggedPreferencesManager.getString(FirebaseConstants.Users.KEY_USER_ID);
        Call onSuccess = data -> {
            Optional<List<User>> users = (Optional<List<User>>) data.get(FirebaseConstants.Users.KEY_USERS_LIST);

            if (Objects.nonNull(users) && users.isPresent()) {
                startLoadUsers(false);

                RecyclerUserView recyclerAdapter = new RecyclerUserView(users.get());
                this.binding.recyclerUserList.setAdapter(recyclerAdapter);
                this.binding.recyclerUserList.setVisibility(View.VISIBLE);
                this.binding.progressBar.setVisibility(View.GONE);
            }
        };

        Call onFail = unused -> {
            startLoadUsers(false);
            getDefaultOnFail().start(unused);
        };

        this.userCollection.getCollections(userId, onSuccess, onFail);

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
}