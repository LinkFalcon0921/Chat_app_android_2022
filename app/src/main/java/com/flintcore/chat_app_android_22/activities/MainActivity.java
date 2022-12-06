package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages.KEY_CHAT_OBJ;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations.KEY_LAST_MESSAGE_ID;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Users.KEY_USER_ID;

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
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements EventListener<QuerySnapshot> {

    private boolean flagFirstLogin = true;
    private ActivityMainBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private UserCollection userCollection;
    private ConversationCollection conversationCollection;
    private ChatMessageCollection chatMessageCollection;

    private RecentMessageAdapter recentMessageAdapter;
    private List<Conversation> conversations;

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
            MessagesAppGenerator.showToast(getApplicationContext(),
                    "Check ethernet connection", FAIL_GET_RESPONSE);
        }

        List<DocumentChange> documentChanges = value.getDocumentChanges();

        if (documentChanges.isEmpty()) {
            return;
        }
//        todo set filter of user.

        String userId = this.loggedPreferencesManager.getString(KEY_USER_ID);

        documentChanges.stream()
                .filter(doc -> doc.getType().equals(DocumentChange.Type.ADDED))
                .map(DocumentChange::getDocument)
                .map(doc -> {
                    Conversation newConversation = doc.toObject(Conversation.class);
                    newConversation.setId(doc.getId());

                    this.chatMessageCollection.getCollection(newConversation.getLastMessageId(),
                            dataChat -> {
                                ChatMessage message = (ChatMessage) dataChat.get(KEY_CHAT_OBJ);
                                // TODO
                                String sender = userId.equals(message.getSenderId()) ?
                                        message.getReceivedId() : message.getSenderId();

                                this.userCollection.getCollection(sender,
                                        dataUser -> {
                                            User user = (User) dataUser.get(FirebaseConstants.Users.KEY_USER_OBJ);
                                            newConversation.setSenderImage(user.getImage());
                                        },
                                        getOnFailFirebaseConnection());

                            },
                            getOnFailFirebaseConnection());

                    return newConversation;
                }).forEach(this.conversations::add);

        documentChanges.stream()
                .filter(doc -> doc.getType().equals(DocumentChange.Type.MODIFIED))
                .map(DocumentChange::getDocument)
                .forEach(doc -> {
                    this.conversations.stream()
                            //TODO

                            .filter(cv -> cv.getId().equals(doc.getId()))
                            .findFirst().ifPresent(cv -> {
                                Conversation updatedConversation = doc.toObject(Conversation.class);
                                cv.setLastMessageId(updatedConversation.getLastMessageId());
                                cv.setSenderImage(updatedConversation.getSenderImage());
                                cv.setLastDateSent(updatedConversation.getLastDateSent());
                            });
                });

        this.conversations.sort(Comparator.comparing(Conversation::getLastDateSent));
        this.recentMessageAdapter.notifyDataSetChanged();
        this.binding.recentConversationsRecycler.smoothScrollToPosition(0);
        this.binding.recentConversationsRecycler.setVisibility(View.VISIBLE);
        this.binding.progressBar.setVisibility(View.GONE);
    }

    private void setFireStoreConnection() {
        this.userCollection = UserCollection.getInstance(getOnFailFirebaseConnection());

        this.conversationCollection = ConversationCollection
                .getConversationInstance(getOnFailFirebaseConnection());

        this.chatMessageCollection = ChatMessageCollection
                .getChatMessageCollectionInstance(getOnFailFirebaseConnection());
    }

    private void loadRecentMessages() {
        this.conversations = new LinkedList<>();
        this.recentMessageAdapter = new RecentMessageAdapter(this.conversations);
        this.binding.recentConversationsRecycler.setAdapter(this.recentMessageAdapter);
    }

    private void listenRecentMessages() {
        final OnCompleteListener<QuerySnapshot> getChatsRelated = task -> {
            if (!task.isComplete() || !task.isSuccessful()) {
                return;
            }

            QuerySnapshot taskResult = task.getResult();

            taskResult.getDocuments()
                    .forEach(documentSnapshot -> {
                        Conversation c = documentSnapshot.toObject(Conversation.class);
                        if (Objects.isNull(c)){
                            return;
                        }

                        this.chatMessageCollection.getCollection(
                                c.getLastMessageId(),
                                data -> {
                                    ChatMessage msg = (ChatMessage) data.get(KEY_CHAT_OBJ);

                                    HashMap<String, Object> hashMap = getNewHashMap();
                                    hashMap.put(KEY_LAST_MESSAGE_ID, msg.getId());
                                    this.conversationCollection.applyCollectionListener(hashMap, this);
                                },
                                fail -> MessagesAppGenerator.showToast(getApplicationContext(),
                                        fail.get(MESSAGE).toString(), FAIL_GET_RESPONSE));
                    });

        };

        this.conversationCollection.applyCollectionListener(getChatsRelated);
    }

    @NonNull
    private HashMap<String, Object> getNewHashMap() {
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
        String userId = this.loggedPreferencesManager
                .getString(KEY_USER_ID);


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


        String userId = this.loggedPreferencesManager
                .getString(KEY_USER_ID);

        Call onSuccess = data -> {
            MessagesAppGenerator.showToast(getApplicationContext(), "Sign in successfully!", FAIL_GET_RESPONSE);
        };

        Call onFail = getOnFailFirebaseConnection();

        this.userCollection.updateToken(userId, onSuccess, onFail);
        this.flagFirstLogin = false;
    }

    private void loadLoggedImage() {
        byte[] imageBytes = Encryptions.decryptAndroidImageFromString(
                this.loggedPreferencesManager
                        .getString(FirebaseConstants.Users.KEY_IMAGE)
        );

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


}