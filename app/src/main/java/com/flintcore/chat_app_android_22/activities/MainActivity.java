package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages.KEY_CHAT_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.NO_CHATS_RECENT;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_OBJ;

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
import com.flintcore.chat_app_android_22.firebase.firestore.ChatMessageCollection;
import com.flintcore.chat_app_android_22.firebase.firestore.ConversationCollection;
import com.flintcore.chat_app_android_22.firebase.firestore.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.Conversation;
import com.flintcore.chat_app_android_22.listeners.OnRecyclerItemListener;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity
        implements EventListener<QuerySnapshot>, OnRecyclerItemListener<Conversation> {

    private boolean flagFirstLogin;
    //    Count for check when the data is completed.
    private int countDataUpdated;

    private ActivityMainBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private UserCollection userCollection;
    private ConversationCollection conversationCollection;
    private ChatMessageCollection chatMessageCollection;

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
//        TODO logic to offline
        this.flagFirstLogin = true;

        this.loadLoggedImage();
        this.updateToken();
        loadRecentMessages();

        listenRecentMessages();
        this.setListeners();
    }

    //    EventListener for recent conversations
    @Override
    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
        if (Objects.nonNull(error) || Objects.isNull(value)) {
            MessagesAppGenerator.showToast(getApplicationContext(), "Check internet connection", FAIL_GET_RESPONSE);
        }

        List<DocumentChange> documentChanges = value.getDocumentChanges();

        if (Objects.isNull(documentChanges) || documentChanges.isEmpty()) {
            getOnFinishLoadConversations().run();
            return;
        }
//        todo set filter of user.

        String userId = this.loggedPreferencesManager.getString(KEY_USER_ID);

        //Charge count of updates
        this.updateCountRecentData(documentChanges.size());

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
                                    ChatMessage message = (ChatMessage) data.get(KEY_CHAT_OBJ);
                                    if(Objects.isNull(message)){
                                        return;
                                    }

                                    cv.setMessage(message.getMessage());
                                    cv.setLastDateSent(conversation.getLastDateSent());
                                    updateCountRecentData(-1);
                                }, onFail);
                    };

                    getOptionalConversation(doc.getId(), onUpdateRecentConversation);
            }

        }

    }

    //    Method to update the recycler when map all data.
    private void updateCountRecentData(int newCount) {
        this.countDataUpdated += newCount;
        if (this.countDataUpdated == 0) {
            getOnFinishLoadConversations().run();
        }
    }

    @Override
    public void onClick(Conversation conversation) {
        Intent chatRecentIntent = new Intent(getApplicationContext(), ChatSimpleActivity.class);
        HashMap<String, Object> whereArgs = getNewStringHashMap();
        whereArgs.put(FieldPath.documentId().toString(), conversation.getSenderId());

        this.userCollection.getCollection(whereArgs,
                data -> {
                    User userReceived = (User) data.get(KEY_USER_OBJ);
                    if (Objects.isNull(userReceived)) {
                        return;
                    }
                    chatRecentIntent.putExtra(KEY_USER_OBJ, userReceived);
                    startActivity(chatRecentIntent);
                },
                fail -> getOnFailFirebaseConnection());

    }

    //    Update action when all actions done.

    private Runnable getOnFinishLoadConversations() {
        return () -> {
            if (!this.conversations.isEmpty()) {
                this.flagFirstLogin = false;
                this.recentMessageAdapter.notifyDataSetChanged();
                this.binding.recentConversationsRecycler.smoothScrollToPosition(0);
            }

            this.binding.recentConversationsRecycler.setVisibility(View.VISIBLE);
            this.binding.progressBar.setVisibility(View.GONE);
        };
    }
    //    Add recent messages to the list view

    private void setAdditionalConversationData(Conversation conversation, String userId) {
        this.chatMessageCollection.getCollection(conversation.getLastMessageId(),
                dataChat -> {
                    ChatMessage message = (ChatMessage) dataChat.get(KEY_CHAT_OBJ);
                    if (Objects.isNull(message)) {
                        return;
                    }

                    conversation.setMessage(message.getMessage());

                    String receiver = userId.equals(message.getSenderId()) ?
                            message.getReceivedId() : message.getSenderId();

                    this.userCollection.getCollection(receiver,
                            dataUser -> {
                                User user = (User) dataUser.get(FirebaseConstants.Users.KEY_USER_OBJ);
                                if (Objects.isNull(user)) {
                                    return;
                                }

                                conversation.setSenderId(receiver);
                                conversation.setSenderName(user.getAlias());
                                conversation.setSenderImage(user.getImage());

                                this.conversations.add(conversation);
                                updateCountRecentData(-1);
                            }, fail -> endOnNoFoundRecentMessages(NO_CHATS_RECENT));

                }, getOnFailFirebaseConnection());
    }

    private void setFireStoreConnection() {
        this.userCollection = UserCollection.getInstance(getOnFailFirebaseConnection());

        this.conversationCollection = ConversationCollection.getConversationInstance(getOnFailFirebaseConnection());

        this.chatMessageCollection = ChatMessageCollection.getChatMessageCollectionInstance(getOnFailFirebaseConnection());
    }

    private void loadRecentMessages() {
        this.conversations = new TreeSet<>(Comparator.comparing(Conversation::getLastDateSent));
        this.recentMessageAdapter = new RecentMessageAdapter(this.conversations, this);
        this.binding.recentConversationsRecycler.setAdapter(this.recentMessageAdapter);
    }

    private void listenRecentMessages() {
        final OnCompleteListener<QuerySnapshot> getChatsRelated = task -> {
            if (!task.isComplete() || !task.isSuccessful()) {
                return;
            }

            QuerySnapshot taskResult = null;
            if (task.isComplete() && task.isSuccessful()
                    && Objects.isNull(taskResult = task.getResult())) {
                getOnFinishLoadConversations().run();
                return;
            }

//            Receiver id
            String userId = this.loggedPreferencesManager.getString(KEY_USER_ID);

            taskResult.getDocuments()
                    .forEach(documentSnapshot -> {

                        Conversation c = documentSnapshot.toObject(Conversation.class);
                        c.setId(documentSnapshot.getId());

                        if (Objects.isNull(c)) {
                            return;
                        }
//                        Get the conversation
                        this.chatMessageCollection.getCollection(c.getLastMessageId(), data -> {
                            ChatMessage msg = (ChatMessage) data.get(KEY_CHAT_OBJ);

                            if (Objects.isNull(msg)) {
                                endOnNoFoundRecentMessages(NO_CHATS_RECENT);
                                return;
                            }

                            if (!(msg.getSenderId().equals(userId) || msg.getReceivedId().equals(userId))) {
                                return;
                            }

                            HashMap<Object, Object> hashMap = getNewHashMap();
                            hashMap.put(FieldPath.documentId(), c.getId());

                            this.conversationCollection.applyCollectionListener(hashMap, this);
                        }, fail -> {
                            endOnNoFoundRecentMessages(NO_CHATS_RECENT);
                        });
                    });

        };

        this.conversationCollection.applyCollectionListener(getChatsRelated);
    }

    @Deprecated
    private void getOptionalConversation(String id, Consumer<Conversation> consumer, Runnable onFail) {
        Optional<Conversation> conversationOptional = this.conversations
                .stream()
                .filter(cv -> cv.getId().equals(id))
                .findFirst();

        if (!conversationOptional.isPresent()) {
            onFail.run();
            return;
        }

        conversationOptional.ifPresent(consumer);
    }

    private void getOptionalConversation(String conversationId, CallResult<Conversation> callResult) {
        Optional<Conversation> conversationOptional = this.conversations.stream()
                .filter(cv -> cv.getId().equals(conversationId)).findFirst();

        conversationOptional.ifPresent(callResult::onCall);
    }

    @NonNull
    private HashMap<Object, Object> getNewHashMap() {
        return new HashMap<>();
    }

    private HashMap<String, Object> getNewStringHashMap() {
        return new HashMap<>();
    }

    private void setListeners() {
        this.binding.logoutBtn.setOnClickListener(v -> signOutUser());
//        Open users List to add
        this.binding.fabAddUser.setOnClickListener(v -> startActivity(goToUserAddListIntent()));
    }

    private Intent goToUserAddListIntent() {
        Intent userListIntent = new Intent(getApplicationContext(), UserActivity.class);
        return userListIntent;
    }

    private void signOutUser() {
        String userId = this.loggedPreferencesManager.getString(KEY_USER_ID);


        Call onSuccess = unused -> {
            this.loggedPreferencesManager.clear();
            startActivity(goToSignInIntent());
            finish();
        };

        Call onFail = getOnFailFirebaseConnection();

        this.userCollection.clearToken(userId, onSuccess, onFail);
    }

    @NonNull
    private Intent goToSignInIntent() {
        Intent signUpIntent = new Intent(getApplicationContext(), SignInActivity.class);
        return signUpIntent;
    }

    private void updateToken() {
        if (!this.flagFirstLogin) {
            return;
        }

        String userId = this.loggedPreferencesManager.getString(KEY_USER_ID);

        Call onSuccess = data -> {
            MessagesAppGenerator.showToast(getApplicationContext(), "Sign in successfully!", FAIL_GET_RESPONSE);
        };

        Call onFail = getOnFailFirebaseConnection();

        this.userCollection.updateToken(userId, onSuccess, onFail);
        this.flagFirstLogin = false;
    }

    private void loadLoggedImage() {
        byte[] imageBytes = Encryptions.decryptAndroidImageFromString(this.loggedPreferencesManager.getString(FirebaseConstants.Users.KEY_IMAGE));

        Bitmap imageBit = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        this.binding.imagePreview.setImageBitmap(imageBit);
    }

    @NonNull
    private Call getOnFailFirebaseConnection() {
        return data -> {
            String message = (String) data.get(MESSAGE);
            MessagesAppGenerator.showToast(getApplicationContext(), message, FAIL_GET_RESPONSE);
        };
    }


    private void endOnNoFoundRecentMessages(@NonNull String message) {
        getOnFinishLoadConversations().run();
        MessagesAppGenerator.showToast(getApplicationContext(), message, FAIL_GET_RESPONSE);
    }


}