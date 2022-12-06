package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.adapters.ChatMessagingAdapter;
import com.flintcore.chat_app_android_22.databinding.ActivityChatSimpleBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.firestore.ChatMessageCollection;
import com.flintcore.chat_app_android_22.firebase.firestore.ConversationCollection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.Conversation;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.flintcore.chat_app_android_22.utilities.views.DefaultConfigs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ChatSimpleActivity extends AppCompatActivity
        implements EventListener<QuerySnapshot>, OnCompleteListener<QuerySnapshot>,
        OnSuccessListener<DocumentReference> {

    private ActivityChatSimpleBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private ChatMessageCollection chatMessageCollection;
    private ConversationCollection conversationCollection;

    private String senderId;
    private User receivedUser;
    private Conversation actualConversation;
    private List<ChatMessage> chatMessages;
    private ChatMessagingAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityChatSimpleBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        setFireStoreConnections();

        configureFields();
        loadUserSelectedDetails();
        setListeners();
        loadChatData();
    }

    //    Logic to receive message
    public void onEvent(QuerySnapshot result, FirebaseFirestoreException error) {
        if (Objects.nonNull(error) || Objects.isNull(result)) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(MESSAGE, error.getMessage());
            getDefaultOnFailCall().start(hashMap);
        }

        int previousChangeCount = this.chatMessages.size();

        for (DocumentChange documentChange : result.getDocumentChanges()) {
            QueryDocumentSnapshot documentSnapshot = documentChange.getDocument();

            switch (documentChange.getType()) {

                default:
                case ADDED:
                    ChatMessage newChatMessage = documentSnapshot
                            .toObject(ChatMessage.class);
                    newChatMessage.setId(documentSnapshot.getId());

                    String message = Encryptions.decrypt(newChatMessage.getMessage());
                    newChatMessage.setMessage(message);

                    this.chatMessages.add(newChatMessage);
            }
        }

        this.chatMessages.sort(Comparator.comparing(ChatMessage::getDatetime));
        int actualChatSize = this.chatMessages.size();

        if (previousChangeCount > 0) {
            this.chatAdapter.notifyItemRangeInserted(previousChangeCount, actualChatSize);
            refreshChatView(actualChatSize);
            this.binding.chatMessageRecycler.setVisibility(View.VISIBLE);
        } else {
            this.chatAdapter.notifyDataSetChanged();
        }
        this.binding.progressBar.setVisibility(View.GONE);

//        Load recent messages
        if (Objects.isNull(this.actualConversation) && actualChatSize > 0) {
            checkRecentMessages();
        }

    }

//    Called when a recent conversation is created or updated.
    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        QuerySnapshot documentSnapshots = task.getResult();
        if (!task.isSuccessful() || Objects.isNull(documentSnapshots) || documentSnapshots.isEmpty()) {
            return;
        }

        List<DocumentSnapshot> documents = documentSnapshots.getDocuments();

        if (documents.isEmpty()) {
            return;
        }

        DocumentSnapshot document = documents.get(0);
        this.actualConversation = document.toObject(Conversation.class);

        this.actualConversation.setId(document.getId());
    }

    @NonNull
    private Call getOnFailUnableRecentChats() {
        return unused -> MessagesAppGenerator.showToast(getApplicationContext(),
                "No chats recently", FAIL_GET_RESPONSE);
    }

//    Called when a recent conversation is updated and ready to notify notify
    @Override
    public void onSuccess(DocumentReference document) {
        document.get()
                .addOnSuccessListener(result -> {
                    if (!result.exists()) {
                        return;
                    }

                    this.actualConversation = result.toObject(Conversation.class);
                    this.actualConversation.setId(result.getId());
                });
    }

    private void setFireStoreConnections() {
        this.chatMessageCollection = ChatMessageCollection
                .getChatMessageCollectionInstance(getDefaultOnFailCall());

        this.conversationCollection = ConversationCollection
                .getConversationInstance(getDefaultOnFailCall());

    }

    private void configureFields() {
        this.binding.inputMessage.setFilters(new InputFilter[]{
                DefaultConfigs.InputFilters.MESSAGE_INPUT_FILTER
        });
    }

    //    Buttons listeners
    private void setListeners() {
        this.binding.backBtn.setOnClickListener(v -> onBackPressed());
        this.binding.sendBtn.setOnClickListener(v -> sendMessageAction());
    }

    //    Get data from intent and preferences
    private void loadUserSelectedDetails() {
        this.loggedPreferencesManager = new PreferencesManager(getApplicationContext(),
                FirebaseConstants.SharedReferences.KEY_CHAT_USER_LOGGED_PREFERENCES);

        this.senderId = this.loggedPreferencesManager
                .getString(FirebaseConstants.Users.KEY_USER_ID);

        this.receivedUser = ((User) getIntent()
                .getSerializableExtra(FirebaseConstants.Users.KEY_USER_OBJ));

        this.binding.userChat.setText(this.receivedUser.getAlias());
    }

    //    Methods to apply Listener to chat.
    private void listenMessages() {
        this.chatMessageCollection.setListener(this.senderId, this.receivedUser.getId(),
                null, this);
    }

    //    Load the data from the messages in the database.
    private void loadChatData() {
        this.chatMessages = new LinkedList<>();

        this.chatAdapter = new ChatMessagingAdapter(this.senderId, this.chatMessages);
        this.binding.chatMessageRecycler.setAdapter(this.chatAdapter);

        listenMessages();

    }

    //    Set a listener for notify new messages
    private void checkRecentMessages() {
        if (this.chatMessages.isEmpty()) {
            return;
        }

        ChatMessage message = this.chatMessages.get(this.chatMessages.size() - 1);

        this.conversationCollection
                .getCollection(message, this, getDefaultOnFailCall());

        ChatMessage messageInverted = new ChatMessage();
        messageInverted.setSenderId(message.getReceivedId());
        messageInverted.setReceivedId(message.getSenderId());

        this.conversationCollection
                .getCollection(messageInverted, this, getDefaultOnFailCall());
    }


    //    Send the message
    private void sendMessageAction() {
        String message = this.binding.inputMessage.getText().toString();
        if (message.trim().isEmpty()) {
            return;
        }

        String receiverId = this.receivedUser.getId();

        message = Encryptions.encrypt(message);

        Date messageSentDate = new Date();

        ChatMessage messageSent = new ChatMessage();

        messageSent.setSenderId(this.senderId);
        messageSent.setReceivedId(receiverId);
        messageSent.setMessage(message);
        messageSent.setDatetime(messageSentDate);

        Call onSuccess = unused -> {
            if (Objects.nonNull(this.actualConversation)) {
                setLastRecentConversation(messageSent);
            } else {
                setNewRecentConversation(messageSent);
            }

            this.binding.inputMessage.setText(null);
            refreshChatView(this.chatMessages.size());
        };

        Call onFail = getDefaultOnFailCall();

        this.chatMessageCollection.addCollection(messageSent, onSuccess, onFail);
    }

    private void setNewRecentConversation(ChatMessage messageSent) {
        this.actualConversation = new Conversation();
        this.actualConversation.setSenderImage(this.receivedUser.getImage());
        this.actualConversation.setLastMessageId(messageSent.getId());
        this.actualConversation.setLastDateSent(new Date());
        this.conversationCollection.addCollection(this.actualConversation, this);
    }

    // For recent listener : create new recent conversation
    private void setLastRecentConversation(ChatMessage messageSent) {
        this.actualConversation.setLastMessageId(messageSent.getMessage());
        this.actualConversation.setLastDateSent(new Date());
        this.conversationCollection.updateCollection(this.actualConversation);
    }

    private void refreshChatView(int chatMessagesPos) {
        this.binding.chatMessageRecycler.smoothScrollToPosition(chatMessagesPos - 1);
    }

    private Call getDefaultOnFailCall() {
        return data -> {
            String message = (String) data.get(MESSAGE);
            MessagesAppGenerator.showToast(getApplicationContext(), message,
                    FAIL_GET_RESPONSE);

        };
    }
}