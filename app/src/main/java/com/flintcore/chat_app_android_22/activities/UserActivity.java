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
import com.flintcore.chat_app_android_22.firebase.firestore.FirebaseConnection;
import com.flintcore.chat_app_android_22.firebase.firestore.users.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition.MatchType;
import com.flintcore.chat_app_android_22.listeners.OnRecyclerItemListener;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;


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

//        label add methods to validate credentials

        setFirebaseInstance();
        setListeners();

        loadUsersInView();
    }

    private void setFirebaseInstance() {
        this.userCollection = UserCollection.getInstance(getDefaultOnFail());
        this.emailAuthentication = EmailAuthentication.getInstance(showMessageCallResultException());
    }

    private void setListeners() {
        this.binding.backBtn.setOnClickListener(v -> onBackPressed());
    }

    private Call getDefaultOnFail() {
        return unused -> {
            showErrorMessageOnLoad();
        };
    }

    //   label Charge all the users available in the database.
    //   label Recycler creation here
    private void loadUsersInView() {
        startLoadUsers(true);

        if (!this.emailAuthentication.isLoggedInFirebase()) {
//           TODO Test this. In case delete the user while loading other users.
            getParent().recreate();
            finish();
            return;
        }

        String userId = getLoggedUserId();

//        label Set queries
        Collection<QueryCondition<String, Object>> whereArgs = setAllUsersQuery(userId);

        CallResult<Task<QuerySnapshot>> onSuccess = getOnFoundUsersCallResult();

        CallResult<Exception> onFail = getOnNotUsersCallResult();

        this.userCollection.getCollections(whereArgs, onSuccess, onFail);

    }

    // label Set all queries necessaries
    private Collection<QueryCondition<String, Object>> setAllUsersQuery(String userId) {
        Collection<QueryCondition<String, Object>> queryList = CollectionsHelper.getArrayList();

        QueryCondition<String, Object> userIdNotMatch = new QueryCondition.Builder<String, Object>()
                .setKey(FirebaseConnection.DOCUMENT_ID)
                .setValue(userId)
                .setMatchType(MatchType.NOT_EQUALS)
                .build();

        queryList.add(userIdNotMatch);

        return queryList;
    }

    /*label set and add all users to the view*/
    @NonNull
    private CallResult<Task<QuerySnapshot>> getOnFoundUsersCallResult() {
        return task -> {
            startLoadUsers(false);

//                label show no users founds
            if (!task.isComplete() || !task.isSuccessful()) {
                showErrorMessageOnLoad();
                return;
            }

            QuerySnapshot result = task.getResult();

//                label show no users founds
            if (result.isEmpty()) {
                showErrorMessageOnLoad();
                return;
            }
            Collection<User> users = new TreeSet<>(Comparator.comparing(User::getAlias));
            users.addAll(result.toObjects(User.class));

//              Recycler view
            RecyclerUserView recyclerAdapter = new RecyclerUserView(users, this);
            this.binding.recyclerUserList.setAdapter(recyclerAdapter);
            this.binding.recyclerUserList.setVisibility(View.VISIBLE);
            this.binding.progressBar.setVisibility(View.GONE);
        };
    }

//    label set in case not possible load users or something else.
    @NonNull
    private CallResult<Exception> getOnNotUsersCallResult() {
        return fail -> {
            startLoadUsers(false);
            this.binding.errorTxt.setText(NO_USERS_AVAILABLE);
            this.binding.errorTxt.setVisibility(View.VISIBLE);
        };
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

    //  label  Recycler view item selection onClick for start conversation

    @Override
    public void onClick(User user) {
        Intent chatUserIntent = new Intent(getApplicationContext(), ChatSimpleActivity.class);
        Conversation newConversation = new Conversation();
        newConversation.getMembers().add(getLoggedUserId());
//        label adding user data
        newConversation.getMembers().add(user.getId());
        newConversation.setSenderImage(user.getImage());
        newConversation.setSenderName(user.getAlias());

        chatUserIntent.putExtra(FirebaseConstants.Conversations.KEY_CONVERSATION_OBJ, newConversation);
        startActivity(chatUserIntent);
        finish();
    }

    @NonNull
    private CallResult<Exception> showMessageCallResultException() {
        return fail -> MessagesAppGenerator
                .showToast(getApplicationContext(), fail, FAIL_GET_RESPONSE);
    }
}