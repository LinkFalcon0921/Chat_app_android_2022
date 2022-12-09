package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ConversationReceiver;
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
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results;
import com.flintcore.chat_app_android_22.firebase.auth.EmailAuthentication;
import com.flintcore.chat_app_android_22.firebase.firestore.conversations.ConversationCollection;
import com.flintcore.chat_app_android_22.firebase.firestore.chatMessages.ChatMessageCollection;
import com.flintcore.chat_app_android_22.firebase.firestore.users.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.UserConstants;
import com.flintcore.chat_app_android_22.listeners.OnRecyclerItemListener;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity
        implements EventListener<QuerySnapshot>, OnRecyclerItemListener<Conversation> {

    //    Count for check when the data is completed.
    private int countDataToUpdated;

    private ActivityMainBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private UserCollection userCollection;
    private ConversationCollection conversationCollection;
    private ChatMessageCollection chatMessageCollection;
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

        if (validatesAllCredentials()) {
            return;
        }

        setFireStoreConnection();

        loadLoggedImage();
        updateToken();
        loadRecentMessagesObjects();

        listenRecentMessages();
        setListeners();
    }

    private boolean validatesAllCredentials() {
        List<Boolean> checks = Arrays.asList(!this.emailAuthentication.isLoggedInFirebase(),
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

    //  label  EventListener for recent conversations
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

        if (Objects.isNull(documentChanges) || documentChanges.isEmpty()) {
            getOnFinishLoadConversations().run();
            return;
        }
//        label set filter of user.

        String userId = this.loggedPreferencesManager.getString(Users.KEY_USER_ID);

        //Charge count of updates

        for (DocumentChange documentChange : documentChanges) {
            QueryDocumentSnapshot doc = documentChange.getDocument();

            switch (documentChange.getType()) {
                case ADDED:
                    Conversation newConversation = doc.toObject(Conversation.class);
                    newConversation.setId(doc.getId());

                    setAdditionalConversationData(newConversation, userId);
                    break;

                case MODIFIED:
                    Conversation conversation = doc.toObject(Conversation.class);

                    CallResult<Conversation> onUpdateRecentConversation = cv -> {
                        Call onFail = unused -> {
                        };
                        this.chatMessageCollection.getCollection(
                                conversation.getLastMessageId(),
                                data -> {
                                    ChatMessage message = (ChatMessage) data.get(ChatMessages.KEY_CHAT_OBJ);
                                    if (Objects.isNull(message)) {
                                        return;
                                    }

                                    cv.setMessage(message.getMessage());
                                    int inCollection = getIndexInCollection(this.conversations, cv);

                                    cv.setLastDateSent(conversation.getLastDateSent());
//                                    Apply changes
                                    cv.setReceiver(conversation.getReceiver());
                                    updateInsertedConversation(inCollection, cv, this.conversations);
                                }, onFail);
                    };

                    getOptionalConversation(doc.getId(), onUpdateRecentConversation);
                    break;

                default:
                    showRecentListView();
            }

        }

    }

    //  label  Method to update the recycler when map all data.
    private <T extends Comparable<T>> void updateInsertedConversation(int lastUpdatedPosition, T conversation, Collection<T> conversations) {
        showRecentListView();
        ArrayList<T> arrayList = new ArrayList<>(conversations);

        int searchIndex = getIndexInCollection(arrayList, conversation);

        this.recentMessageAdapter.notifyItemRangeChanged(searchIndex, lastUpdatedPosition);
    }

    private <T extends Comparable<T>> int getIndexInCollection(Collection<T> list, T conversation) {
        showRecentListView();
        ArrayList<T> conversationsList = new ArrayList<T>(list);
        int searchIndex = Collections.binarySearch(conversationsList, conversation);
        return searchIndex;
    }

    private void addInsertedConversation(Conversation conversation) {
        ArrayList<Conversation> arrayList = new ArrayList<>(this.conversations);

        int searchIndex = getIndexInCollection(arrayList, conversation);

        this.recentMessageAdapter.notifyItemInserted(searchIndex);
    }


    @Override
    public void onClick(Conversation conversation) {
        Intent chatRecentIntent = new Intent(getApplicationContext(), ChatSimpleActivity.class);
        HashMap<String, Object> whereArgs = CollectionsHelper.getHashMap();
        whereArgs.put(FieldPath.documentId().toString(), conversation.getLastSenderId());

        this.userCollection.getCollection(whereArgs,
                data -> {
                    User userReceived = (User) data.get(Users.KEY_USER_OBJ);
                    if (Objects.isNull(userReceived)) {
                        return;
                    }

//                    Set flag to already recent message saw.
                    conversation.getReceiver().setWasViewed(true);
                    this.conversationCollection.updateCollection(conversation, vd -> {
                    }, getOnFailFirebaseConnection());
                    chatRecentIntent.putExtra(Users.KEY_USER_OBJ, userReceived);
                    startActivity(chatRecentIntent);
                },
                fail -> getOnFailFirebaseConnection());

    }

    //    Update action when all actions done.

    private Runnable getOnFinishLoadConversations() {
        return () -> {
            if (!this.conversations.isEmpty()) {
                this.recentMessageAdapter.notifyDataSetChanged();
                this.binding.recentConversationsRecycler.smoothScrollToPosition(0);
            }
            showRecentListView();
        };
    }

    private void showRecentListView() {
        this.binding.recentConversationsRecycler.setVisibility(View.VISIBLE);
        this.binding.progressBar.setVisibility(View.GONE);
    }
    //    Add recent messages to the list view

    private void setAdditionalConversationData(Conversation conversation, String userId) {
        this.chatMessageCollection.getCollection(conversation.getLastMessageId(),
                dataChat -> {
                    ChatMessage message = (ChatMessage) dataChat.get(ChatMessages.KEY_CHAT_OBJ);
                    if (Objects.isNull(message)) {
                        return;
                    }

                    conversation.setMessage(message.getMessage());

                    String receiver = userId.equals(message.getSenderId()) ?
                            message.getReceivedId() : message.getSenderId();

//                    Call to add conversations
                    Call userCallResult = dataUser -> {
                        User user = (User) dataUser.get(Users.KEY_USER_OBJ);
                        if (Objects.isNull(user)) {
                            return;
                        }

                        conversation.setLastSenderId(receiver);
                        conversation.setSenderName(user.getAlias());
                        conversation.setSenderImage(user.getImage());

                        this.conversations.add(conversation);
                        addInsertedConversation(conversation);
                    };
                    this.userCollection.getCollection(receiver,
                            userCallResult, fail -> endOnNoFoundRecentMessages(Messages.NO_CHATS_RECENT));

                }, getOnFailFirebaseConnection());
    }


    private void setFireStoreConnection() {
        this.userCollection = UserCollection.getInstance(getOnFailFirebaseConnection());

        this.conversationCollection = ConversationCollection.getConversationInstance(getOnFailFirebaseConnection());

        this.chatMessageCollection = ChatMessageCollection.getChatMessageCollectionInstance(getOnFailFirebaseConnection());

        this.emailAuthentication = EmailAuthentication.getInstance(getExceptionCallResult());
    }

    private void loadRecentMessagesObjects() {
        this.conversations = new TreeSet<>(Comparator.comparing(Conversation::getLastDateSent));
        this.recentMessageAdapter = new RecentMessageAdapter(this.conversations, this::onClick);
        this.binding.recentConversationsRecycler.setAdapter(this.recentMessageAdapter);
    }

    //   label Listen recent messages in the app via conversations.
    private void listenRecentMessages() {

        final OnCompleteListener<QuerySnapshot> getRecentConversations = task -> {

            if (!task.isComplete() || !task.isSuccessful()) {
                showRecentListView();
                return;
            }

            QuerySnapshot taskResult = task.getResult();

            if (Objects.isNull(taskResult) || taskResult.isEmpty()) {
                showRecentListView();
                return;
            }

            for (DocumentSnapshot document : taskResult.getDocuments()) {

                Conversation c = document.toObject(Conversation.class);
                Map<Object, Object> whereChatArgs = CollectionsHelper.getHashMap();

                whereChatArgs.put(FieldPath.documentId().toString(), c.getLastMessageId());
                whereChatArgs.put(FirebaseConstants.ChatMessages.KEY_SENDER, getLoggedUserId());

                CallResult<ChatMessage> onSuccess = chatMessage -> {
                    HashMap<Object, Object> whereReceiverArgs = CollectionsHelper.getHashMap();
                    whereReceiverArgs.put(ConversationReceiver.KEY_CONVERSATION_LAST_RECEIVER_ID, chatMessage.getReceivedId());
                    this.conversationCollection.applyCollectionListener(whereReceiverArgs, this::onEvent);

                    whereReceiverArgs.put(ConversationReceiver.KEY_CONVERSATION_LAST_RECEIVER_ID, chatMessage.getSenderId());
                    this.conversationCollection.applyCollectionListener(whereReceiverArgs, this::onEvent);

                };

                this.chatMessageCollection.getCollection(whereChatArgs,
                        onSuccess, getExceptionCallResult());
            }

        };

        this.conversationCollection.applyCollectionListener(getRecentConversations);
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

    /**Delete token and logout*/
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


    private void endOnNoFoundRecentMessages(@NonNull String message) {
        getOnFinishLoadConversations().run();
        MessagesAppGenerator.showToast(getApplicationContext(), message, Messages.FAIL_GET_RESPONSE);
    }


    private void showUnableLogoutMessage() {
        MessagesAppGenerator.showToast(getApplicationContext(), "It was not possible logged out.",
                Messages.FAIL_GET_RESPONSE);
    }
}