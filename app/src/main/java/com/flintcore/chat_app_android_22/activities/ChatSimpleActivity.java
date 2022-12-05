package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.adapters.ChatMessagingAdapter;
import com.flintcore.chat_app_android_22.databinding.ActivityChatSimpleBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.firestore.ChatMessageCollection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.dates.DateUtils;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.flintcore.chat_app_android_22.utilities.views.DefaultConfigs;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ChatSimpleActivity extends AppCompatActivity implements EventListener<QuerySnapshot> {

    private ActivityChatSimpleBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private ChatMessageCollection chatMessageCollection;
//    private DateUtils dateUtils;

    private String senderId;
    private User receivedUser;
    private List<ChatMessage> chatMessages;
    private ChatMessagingAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityChatSimpleBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        this.chatMessageCollection = ChatMessageCollection
                .getChatMessageCollectionInstance(getDefaultOnFailCall());

//        this.dateUtils = DateUtils.getDateUtils("MMMM dd, yyyy hh:mm a");

        configurateFields();
        loadUserSelectedDetails();
        setListeners();
        loadChatData();
    }

    private void configurateFields() {
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

    private void sendMessageAction() {
        String message = this.binding.inputMessage.getText().toString();
        if(message.trim().isEmpty()){
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
            this.binding.inputMessage.setText(null);
            refreshChatView(this.chatMessages.size());
        };

        Call onFail = getDefaultOnFailCall();

        this.chatMessageCollection.addCollection(messageSent, onSuccess, onFail);

    }

    private void refreshChatView(int chatMessages) {
        this.binding.chatMessageRecycler.smoothScrollToPosition(chatMessages);
    }

    private Call getDefaultOnFailCall() {
        return data -> {
            String message = (String) data.get(MESSAGE);
            MessagesAppGenerator.showToast(getApplicationContext(), message,
                    FirebaseConstants.Messages.FAIL_GET_RESPONSE);

        };
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
            switch (documentChange.getType()) {

                default:
                case ADDED:
                    ChatMessage newChatMessage = documentChange.getDocument()
                            .toObject(ChatMessage.class);

                    String message = Encryptions.decrypt(newChatMessage.getMessage());
                    newChatMessage.setMessage(message);

                    this.chatMessages.add(newChatMessage);
            }
        }

        this.chatMessages.sort(Comparator.comparing(ChatMessage::getDatetime));
        int actualChatSize = this.chatMessages.size();

        if (previousChangeCount > 0) {
            this.chatAdapter.notifyItemRangeInserted(previousChangeCount, actualChatSize);
            refreshChatView(actualChatSize - 1);
            this.binding.chatMessageRecycler.setVisibility(View.VISIBLE);
        } else {
            this.chatAdapter.notifyDataSetChanged();
        }
        this.binding.progressBar.setVisibility(View.GONE);
    }
}