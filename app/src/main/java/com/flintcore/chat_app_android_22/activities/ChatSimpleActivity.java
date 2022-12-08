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
import com.flintcore.chat_app_android_22.firebase.firestore.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.UserConstants;
import com.flintcore.chat_app_android_22.firebase.models.embbebed.ConversationReceiver;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.flintcore.chat_app_android_22.utilities.views.DefaultConfigs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class ChatSimpleActivity extends AppCompatActivity
        implements EventListener<QuerySnapshot>, OnCompleteListener<QuerySnapshot>,
        OnSuccessListener<DocumentReference> {

    public static final int MAX_COUNT = 2;

    private ActivityChatSimpleBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private UserCollection userCollection;
    private ChatMessageCollection chatMessageCollection;
    private ConversationCollection conversationCollection;

    private int counter;
    private User receivedUser;
    private Conversation actualConversation;
    private Collection<ChatMessage> chatMessages;
    private ChatMessagingAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityChatSimpleBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());
        this.counter = 0;

        setFireStoreConnections();

        configureFields();
        loadUserSelectedDetails();
        setListeners();
        loadChatData();

        setUserReceiverAvailableListener();
    }


    //    Logic to receive message
    public void onEvent(QuerySnapshot result, FirebaseFirestoreException error) {
        if (Objects.nonNull(error) || Objects.isNull(result)) {
            HashMap<String, Object> hashMap = CollectionsHelper.getHashMap();
            hashMap.put(MESSAGE, error.getMessage());
            getDefaultOnFailCall().start(hashMap);
            return;
        }

        int previousChangeCount = this.chatMessages.size();

        for (DocumentChange documentChange : result.getDocumentChanges()) {
            QueryDocumentSnapshot documentSnapshot = documentChange.getDocument();

            switch (documentChange.getType()) {

                case ADDED:
                    ChatMessage newChatMessage = documentSnapshot
                            .toObject(ChatMessage.class);
                    newChatMessage.setId(documentSnapshot.getId());

                    String message = Encryptions.decrypt(newChatMessage.getMessage());
                    newChatMessage.setMessage(message);

                    this.chatMessages.add(newChatMessage);

                case REMOVED:
                    CallResult<ChatMessage> onChatMatches = this.chatMessages::remove;

                    this.chatMessages.stream()
                            .filter(chat -> chat.getId().equals(documentSnapshot.getId()))
                            .findFirst()
                            .ifPresent(onChatMatches::onCall);
            }
        }


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
        if (Objects.isNull(this.actualConversation)) {
            checkRecentMessages();
        }

    }

    //    Called when a recent conversation is created or updated.
    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        QuerySnapshot documentSnapshots = task.getResult();
        if (!task.isSuccessful() || Objects.isNull(documentSnapshots) || documentSnapshots.isEmpty()) {
            if (this.counter++ < MAX_COUNT) {
                getOnNotFoundReceivedCall(this.getChatMessage(this.chatMessages.size() - 1))
                        .start(null);
            }
            return;
        }

        List<DocumentSnapshot> documents = documentSnapshots.getDocuments();

        if (documents.isEmpty()) {
//        Set the conversation if not exists one
            this.actualConversation = new Conversation();
            return;
        }

        DocumentSnapshot document = documents.get(0);
        this.actualConversation = document.toObject(Conversation.class);
        this.actualConversation.setId(document.getId());

        ConversationReceiver conversationReceiver = this.actualConversation.getReceiver();
        if (conversationReceiver.getReceiver().equals(getLoggedUserId())
                && !conversationReceiver.getWasViewed()) {
            conversationReceiver.setWasViewed(true);
        }

        refreshChatView(this.chatMessages.size());
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
                    this.conversationCollection.applyCollectionListener(this::onComplete);

                    this.chatAdapter.notifyDataSetChanged();
                });
    }

    private void setFireStoreConnections() {
        this.userCollection = UserCollection.getInstance(getDefaultOnFailCall());

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

        this.receivedUser = ((User) getIntent()
                .getSerializableExtra(FirebaseConstants.Users.KEY_USER_OBJ));

        this.binding.userChat.setText(this.receivedUser.getAlias());
    }
    //    Methods to apply Listener to chat.

    private void listenMessages() {
        this.chatMessageCollection.setListener(getLoggedUserId(), this.receivedUser.getId(),
                null, this);
    }
    //    Load the data from the messages in the database.

    private void loadChatData() {
        this.chatMessages = new TreeSet<>(Comparator.comparing(ChatMessage::getDatetime));
        this.chatAdapter = new ChatMessagingAdapter(getLoggedUserId(), this.chatMessages);
        this.binding.chatMessageRecycler.setAdapter(this.chatAdapter);

        listenMessages();
    }
    //    Set a listener for notify new messages

    private void checkRecentMessages() {
        if (this.chatMessages.isEmpty()) {
            return;
        }

        ChatMessage message = getChatMessage(this.chatMessages.size() - 1);

        this.conversationCollection.getCollection(message, this::onComplete, getOnFailUnableRecentChats());
    }

    private ChatMessage getChatMessage(int position) {
        return this.chatMessages
                .toArray(new ChatMessage[0])[position];
    }

    //    Send the message

    /**
     * Not need parameters in method
     */
    @NonNull
    private Call getOnNotFoundReceivedCall(ChatMessage message) {
        return unused -> {
            ChatMessage messageInverted = new ChatMessage();
            messageInverted.setSenderId(message.getReceivedId());
            messageInverted.setReceivedId(message.getSenderId());

            this.conversationCollection
                    .getCollection(messageInverted, this::onComplete, getDefaultOnFailCall());
        };
    }

    // Action send message
    private void sendMessageAction() {
        String message = this.binding.inputMessage.getText().toString();
        if (message.trim().isEmpty()) {
            return;
        }

        String receiverId = this.receivedUser.getId();

        message = Encryptions.encrypt(message);

        Date messageSentDate = new Date();

        ChatMessage messageSent = new ChatMessage();

        messageSent.setSenderId(getLoggedUserId());
        messageSent.setReceivedId(receiverId);
        messageSent.setMessage(message);
        messageSent.setDatetime(messageSentDate);

        Call onSuccess = unused -> {
            updateConversation(messageSent);

            this.binding.inputMessage.setText(null);
            refreshChatView(this.chatMessages.size());
        };

        Call onFail = getDefaultOnFailCall();

        this.chatMessageCollection.addCollection(messageSent, onSuccess, onFail);
    }

    // On update conversation when send message
    private void updateConversation(ChatMessage messageSent) {
        setLastRecentConversation(messageSent);
//        Set last receiver in the chat
        this.actualConversation.getReceiver().setReceiver(messageSent.getReceivedId());
        this.actualConversation.getReceiver().setWasViewed(false);

    }

    private void setNewRecentConversation(ChatMessage messageSent) {
        this.actualConversation.setLastMessageId(messageSent.getId());
        this.actualConversation.setLastDateSent(new Date());
    }

    //    Get logged user
    private String getLoggedUserId() {
        return this.loggedPreferencesManager.getString(FirebaseConstants.Users.KEY_USER_ID);
    }

    // For recent listener : create new recent conversation

    private void setLastRecentConversation(ChatMessage messageSent) {
        if (Objects.isNull(this.actualConversation)) {
            setNewRecentConversation(messageSent);
            this.conversationCollection.addCollection(this.actualConversation, this::onSuccess);
            return;
        }

        this.actualConversation.setLastMessageId(messageSent.getId());
        this.actualConversation.setLastDateSent(new Date());
        this.conversationCollection.updateCollection(this.actualConversation);
    }

    private void refreshChatView(int chatMessagesPos) {

        try {
            this.binding.chatMessageRecycler.smoothScrollToPosition(chatMessagesPos - 1);
        } catch (Exception e) {
            this.binding.chatMessageRecycler.smoothScrollToPosition(this.chatMessages.size() - 1);
        }
    }

    private Call getDefaultOnFailCall() {
        return data -> {
            String message = (String) data.get(MESSAGE);
            MessagesAppGenerator.showToast(getApplicationContext(), message,
                    FAIL_GET_RESPONSE);
        };
    }

    @NonNull
    private Call getOnFailUnableRecentChats() {
        return unused -> MessagesAppGenerator.showToast(getApplicationContext(),
                "No chats recently", FAIL_GET_RESPONSE);
    }

    //    Get if receiver user is available
    private void setUserReceiverAvailableListener() {
        HashMap<Object, Object> whereArgs = CollectionsHelper.getHashMap();
        whereArgs.put(FieldPath.documentId(), this.receivedUser.getId());
        this.userCollection.applyUserAvailability(whereArgs, this.userAvailabilityDefault);
    }

    //    FOR event to get availability user.
    private final EventListener<QuerySnapshot> userAvailabilityDefault = (value, error) -> {
        if (Objects.isNull(value) || Objects.nonNull(error)) {
//            TODO logic to not get user availability
            return;
        }

        List<DocumentSnapshot> documents = value.getDocuments();

        documents.stream().findFirst()
                .ifPresent(doc -> {
                    User user = doc.toObject(User.class);

                    switch (user.getAvailable()) {
                        case UserConstants.AVAILABLE:
                            this.binding.availableFlag.setVisibility(View.VISIBLE);
                            break;
                        default:
                        case UserConstants.NOT_AVAILABLE:
                            this.binding.availableFlag.setVisibility(View.GONE);
                    }
                });
    };

    @Override
    protected void onResume() {
        super.onResume();

        this.userCollection.updateAvailable(getLoggedUserId(), UserConstants.AVAILABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.userCollection.updateAvailable(getLoggedUserId(), UserConstants.NOT_AVAILABLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        this.userCollection.updateAvailable(getLoggedUserId(), UserConstants.NOT_AVAILABLE);
    }

}