package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.adapters.RecentMessageAdapter;
import com.flintcore.chat_app_android_22.databinding.ActivityMainBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results;
import com.flintcore.chat_app_android_22.firebase.auth.EmailAuthentication;
import com.flintcore.chat_app_android_22.firebase.firestore.conversations.ConversationCollection;
import com.flintcore.chat_app_android_22.firebase.firestore.users.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.UserConstants;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.listeners.OnRecyclerItemListener;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity
        implements EventListener<QuerySnapshot>, OnRecyclerItemListener<Conversation> {

    private ActivityMainBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private UserCollection userCollection;
    private ConversationCollection<ChatMessage> conversationCollection;
    private EmailAuthentication emailAuthentication;

    private User loggedUser;

    private RecentMessageAdapter recentMessageAdapter;
    private Collection<Conversation> conversations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.loggedPreferencesManager = new PreferencesManager(getApplicationContext(),
                FirebaseConstants.SharedReferences.KEY_CHAT_USER_LOGGED_PREFERENCES);

        setFireStoreConnection();

        if (!validatesAllCredentials()) {
            signOutUser();
            return;
        }

        loadLoggedImage();
        updateToken();
        loadRecentConversations();

        setListeners();
    }

    private boolean validatesAllCredentials() {
        List<Boolean> checks = Arrays.asList(this.emailAuthentication.isLoggedInFirebase(),
                this.loggedPreferencesManager.contains(getPreferencesValidatorKeys()));
        boolean validateBoolean = validateBoolean(checks);

        if (validateBoolean) {
            this.loggedUser = new User();
            this.loggedUser.setId(getLoggedUserId());
        }

        return validateBoolean;
    }

    private boolean validateBoolean(@NonNull Collection<Boolean> checks) {
        return checks.stream().allMatch(b -> Objects.equals(b, Boolean.TRUE));
    }

    @NonNull
    private List<String> getPreferencesValidatorKeys() {
        return Arrays.asList(Users.KEY_IS_SIGNED_IN, Users.KEY_USER_ID, Users.KEY_IMAGE);
    }

    //  label  On Event for recent conversations
    @Override
    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
        if (Objects.nonNull(error)) {
            MessagesAppGenerator.showToast(getApplicationContext(), error, Messages.FAIL_GET_RESPONSE);
            showRecentListView();
            return;
        }

        if (Objects.isNull(value)) {
            showRecentListView();
            return;
        }

//      Conversation load
        List<DocumentChange> documentChanges = value.getDocumentChanges();

        if (documentChanges.isEmpty()) {
            showRecentListView();
            return;
        }
//        label set filter of user.

        readAllRecentConversations(documentChanges.iterator());

    }

    //    label Recursive call until end read Conversations.
    private void readAllRecentConversations(Iterator<DocumentChange> iterator) {
//        label ends if not found more
        if (!iterator.hasNext()) {
            showRecentListView();
            return;
        }

        DocumentChange documentChange = iterator.next();
        QueryDocumentSnapshot documentSnapshot = documentChange.getDocument();
        Conversation conversation = documentSnapshot.toObject(Conversation.class);
        DocumentReference chatMessageDocument = documentSnapshot.get(Conversations.KEY_LAST_MESSAGE_ID, DocumentReference.class);

//        Map the reference Chat to object
        CallResult<Task<DocumentSnapshot>> onChatReferenceMapped = task -> {
            if (!task.isSuccessful() || !task.isComplete()) {
                showRecentListView();
                return;
            }

            ChatMessage chatMessage = task.getResult().toObject(ChatMessage.class);
            conversation.setChatMessage(chatMessage);

            //      label  Filter the data
            switch (documentChange.getType()) {
                case ADDED:
                    setAdditionalConversationData(conversation);
                    break;

                case MODIFIED:
                    CallResult<Conversation> onFoundCallResult = c -> {
                        updateRecentConversation(c, conversation);
                    };
                    getOptionalConversation(conversation.getId(), onFoundCallResult);

                    break;
                case REMOVED:
                    removeRecentConversation(conversation);
            }

            readAllRecentConversations(iterator);
        };

        CallResult<Exception> onFail = getExceptionCallResult();

        mapChatMessageReference(chatMessageDocument, onChatReferenceMapped, onFail);
    }

    private void mapChatMessageReference(DocumentReference chatMessageDocument, CallResult<Task<DocumentSnapshot>> onChatReferenceMapped, CallResult<Exception> onFail) {
        chatMessageDocument.get()
                .addOnCompleteListener(onChatReferenceMapped::onCall)
                .addOnFailureListener(onFail::onCall);
    }

    private void removeRecentConversation(Conversation conversation) {
        int indexInCollection = getIndexInCollection(this.conversations, conversation);
        this.conversations.remove(conversation);
        this.recentMessageAdapter.notifyItemRemoved(indexInCollection);
    }

    //  label  Method to update the recycler when map all data.
    private void
    updateRecentConversation(Conversation conversation, Conversation newConversation) {
        int searchIndex = getIndexInCollection(this.conversations, conversation);

        if (searchIndex < 0) {
            addRecentConversation(conversation);
            return;
        }
        fillConversationData(conversation, newConversation);
        this.recentMessageAdapter.notifyItemChanged(searchIndex);
    }

    private void fillConversationData(Conversation conversation, Conversation newConversation) {
        conversation.setChatMessage(newConversation.getChatMessage());
        conversation.setLastDateSent(newConversation.getLastDateSent());
        conversation.setReceiver(newConversation.getReceiver());
    }

    private <T extends Comparable<T>> int getIndexInCollection(Collection<T> list, T conversation) {
        int index = new ArrayList<T>(list).indexOf(conversation);
        showRecentListView();
        return index;

    }

    //  label  Adapter notified when Conversation is added
    private void addRecentConversation(Conversation conversation) {
        ArrayList<Conversation> arrayList = new ArrayList<>(this.conversations);

        int searchIndex = getIndexInCollection(arrayList, conversation);

        this.recentMessageAdapter.notifyItemInserted(searchIndex);
    }

    //    label listener to recent conversation.
    @Override
    public void onClick(Conversation conversation) {
        CallResult<Task<Void>> onCompleteUpdate = task -> {
            if (!task.isComplete() || !task.isSuccessful()) {
                return;
            }

            conversation.getReceiver().setWasViewed(true);
            Intent chatRecentIntent = new Intent(getApplicationContext(), ChatSimpleActivity.class);
            chatRecentIntent.putExtra(Conversations.KEY_CONVERSATION_OBJ, conversation);
            startActivity(chatRecentIntent);
        };

        this.conversationCollection.update(conversation, onCompleteUpdate, getExceptionCallResult());
    }

    private void showRecentListView() {
        this.binding.recentConversationsRecycler.setVisibility(View.VISIBLE);
        this.binding.progressBar.setVisibility(View.GONE);
    }

    //   label Add recent messages to the list view
    private void setAdditionalConversationData(@NonNull Conversation conversation) {

//        label get member id.
        Optional<String> optionalMember = conversation.getMembers().stream()
                .filter(mb -> !Objects.equals(getLoggedUserId(), mb))
                .findFirst();

        optionalMember.ifPresent(mb -> {
            User user = new User();
            user.setId(mb);

            CallResult<Task<DocumentSnapshot>> onFoundUserReceiver = task -> {
                if (!task.isComplete() || !task.isSuccessful()) {
                    return;
                }

                User userFound = task.getResult().toObject(User.class);
                conversation.setSenderImage(userFound.getImage());
                conversation.setSenderName(userFound.getAlias());
                this.conversations.add(conversation);
                addRecentConversation(conversation);
            };

            this.userCollection.getCollectionById(user,
                    onFoundUserReceiver, getExceptionCallResult());
        });

    }


    private void setFireStoreConnection() {
        this.userCollection = UserCollection.getInstance(getExceptionCallResult());

        this.conversationCollection = ConversationCollection.getConversationInstance(getExceptionCallResult());

        this.emailAuthentication = EmailAuthentication.getInstance(getExceptionCallResult());
    }

    private void loadRecentConversations() {
        this.conversations = new TreeSet<>(Comparator.comparing(Conversation::getLastDateSent));

        this.recentMessageAdapter = new RecentMessageAdapter(this.conversations, this::onClick, getLoggedUserId());
        this.recentMessageAdapter.setExceptionCallResult(getExceptionCallResult());

        this.binding.recentConversationsRecycler.setAdapter(this.recentMessageAdapter);

        listenRecentMessages();
    }

    //   label Listen recent messages in the app via conversations.
    private void listenRecentMessages() {

        Collection<QueryCondition<String, Object>> queryRecentListener = setQueryRecentListener();

        this.conversationCollection.applyCollectionListener(queryRecentListener, this::onEvent);
    }

    //    label recent query list
    private Collection<QueryCondition<String, Object>> setQueryRecentListener() {
        Collection<QueryCondition<String, Object>> whereListener = CollectionsHelper.getArrayList();

        QueryCondition<String, Object> getByMemberQuery = new QueryCondition.Builder<String, Object>()
                .setKey(Conversations.KEY_MEMBERS)
                .setValue(Arrays.asList(getLoggedUserId()))
                .setMatchType(QueryCondition.MatchType.ARRAY_IN_ANY)
                .build();

        whereListener.add(getByMemberQuery);

        return whereListener;

    }

    //    label get query for get chat message and add it
    private Collection<QueryCondition<String, Object>> setQueryChatGetMessage(ChatMessage message) {
        Collection<QueryCondition<String, Object>> whereListener = CollectionsHelper.getArrayList();

        QueryCondition<String, Object> getChatMessageById = new QueryCondition.Builder<String, Object>()
                .setKey(Conversations.KEY_LAST_MESSAGE_ID)
                .setKey(message.getId())
                .setMatchType(QueryCondition.MatchType.EQUALS)
                .build();

        whereListener.add(getChatMessageById);

        return whereListener;

    }
    //    Listen recent messages in the app via conversations.

    private String getLoggedUserId() {
        return this.loggedPreferencesManager.getString(Users.KEY_USER_ID);
    }

    private void getOptionalConversation(String conversationId, CallResult<Conversation> callResult) {
        Optional<Conversation> conversationOptional = this.conversations.stream()
                .filter(cv -> cv.getId().equals(conversationId)).findFirst();

        conversationOptional.ifPresent(callResult::onCall);
    }

    //    label button listeners
    private void setListeners() {
        this.binding.logoutBtn.setOnClickListener(v -> signOutUser());
//        Open users List to add
        this.binding.fabAddUser.setOnClickListener(v -> startActivity(goToUserAddListIntent()));
    }

    private Intent goToUserAddListIntent() {
        Intent userListIntent = new Intent(getApplicationContext(), UserActivity.class);
        return userListIntent;
    }

    /**
     * Delete token and logout
     */
    private void signOutUser() {
        clearToken();
    }

    @NonNull
    private Intent goToSignInIntent() {
        Intent signUpIntent = new Intent(getApplicationContext(), SignInActivity.class);
        return signUpIntent;
    }

    //   label update the user token
    private void updateToken() {
        CallResult<String> onSuccess = getUpdateActionToken();

        CallResult<Exception> onFail = getExceptionCallResult();

        this.userCollection.appendToken(this.loggedUser, onSuccess, onFail);
    }

    //    label clear the user token, also logout.
    private void clearToken() {
        CallResult<Task<Void>> onComplete = getOnDeleteToken();

        CallResult<Exception> onFail = getExceptionCallResult();

        this.userCollection.clearToken(this.loggedUser, onComplete, onFail);
    }

    private CallResult<Task<Void>> getOnDeleteToken() {
        return token -> {
            if (!token.isComplete() || !token.isSuccessful()) {
                showUnableLogoutMessage();
                return;
            }

            if (Objects.isNull(this.loggedUser)) {
                startActivity(goToSignInIntent());
                return;
            }

            this.userCollection.updateToken(loggedUser);

            this.userCollection.updateAvailable(getLoggedUserId(), UserConstants.NOT_AVAILABLE);
            this.loggedPreferencesManager.clear();
            FirebaseAuth.getInstance().signOut();
            startActivity(goToSignInIntent());
            finish();
        };
    }

    private CallResult<String> getUpdateActionToken() {
        return empty -> {
            this.userCollection.updateAvailable(getLoggedUserId(), UserConstants.AVAILABLE);
            MessagesAppGenerator.showToast(getApplicationContext(), Messages.SIGN_IN_SUCCESSFUL,
                    Messages.FAIL_GET_RESPONSE);
        };
    }

    private void loadLoggedImage() {
        if (!this.loggedPreferencesManager.contains(Users.KEY_IMAGE)) {
            getExceptionCallResult().onCall(new RuntimeException(Messages.NOT_FOUND_DATA_USER_REGISTERED));
            signOutUser();
            return;
        }
        String userImage = this.loggedPreferencesManager.getString(Users.KEY_IMAGE);

        byte[] imageBytes = Encryptions.decryptAndroidImageFromString(userImage);

        Bitmap imageBit = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        this.binding.imagePreview.setImageBitmap(imageBit);
    }

    @NonNull
    private CallResult<Exception> getExceptionCallResult() {
        return fail -> {
            MessagesAppGenerator
                    .showToast(getApplicationContext(), fail, Messages.FAIL_GET_RESPONSE);
            showRecentListView();
        };
    }

    @NonNull
    private Call getOnFailFirebaseConnection() {
        return data -> {
            String message = (String) data.get(Results.MESSAGE);
            MessagesAppGenerator.showToast(getApplicationContext(), message, Messages.FAIL_GET_RESPONSE);
        };
    }

    private void showUnableLogoutMessage() {
        MessagesAppGenerator.showToast(getApplicationContext(), "It was not possible logged out.",
                Messages.FAIL_GET_RESPONSE);
    }
}