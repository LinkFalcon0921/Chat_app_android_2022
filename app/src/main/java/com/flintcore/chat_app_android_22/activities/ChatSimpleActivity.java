package com.flintcore.chat_app_android_22.activities;

import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Messages.FAIL_GET_RESPONSE;
import static com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Results.MESSAGE;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.flintcore.chat_app_android_22.adapters.ChatMessagingAdapter;
import com.flintcore.chat_app_android_22.databinding.ActivityChatSimpleBinding;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations;
import com.flintcore.chat_app_android_22.firebase.firestore.FirebaseConnection;
import com.flintcore.chat_app_android_22.firebase.firestore.chatMessages.ChatMessageCollection;
import com.flintcore.chat_app_android_22.firebase.firestore.conversations.ConversationCollection;
import com.flintcore.chat_app_android_22.firebase.firestore.users.UserCollection;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.firebase.models.UserConstants;
import com.flintcore.chat_app_android_22.firebase.queries.QueryCondition;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.PreferencesManager;
import com.flintcore.chat_app_android_22.utilities.callback.Call;
import com.flintcore.chat_app_android_22.utilities.callback.CallResult;
import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.flintcore.chat_app_android_22.utilities.views.DefaultConfigs;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

public class ChatSimpleActivity extends AppCompatActivity
        implements EventListener<QuerySnapshot> {

    private ActivityChatSimpleBinding binding;
    private PreferencesManager loggedPreferencesManager;
    private UserCollection userCollection;
    private ChatMessageCollection chatMessageCollection;
    private ConversationCollection<ChatMessage> conversationCollection;

    private Conversation actualConversation;
    private Collection<ChatMessage> chatMessages;
    private ChatMessagingAdapter chatAdapter;
    private ListenerRegistration conversationOnRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = ActivityChatSimpleBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

//        label: append instance to firestore / firebase instances...
        setFireStoreConnections();

//        label: Set the inputs and buttons interactions
        configureFields();
        setListeners();

        //  label: Load the given conversation data.
        loadUserSelectedDetails();
        loadChatData();

        setUserReceiverAvailableListener();
    }


    //  label Logic to receive message
    public void onEvent(QuerySnapshot result, FirebaseFirestoreException error) {
        if (Objects.nonNull(error) || Objects.isNull(result)) {
            HashMap<String, Object> hashMap = CollectionsHelper.getHashMap();
            hashMap.put(MESSAGE, error.getMessage());
            getDefaultOnFailCall().start(hashMap);
            return;
        }

        Iterator<DocumentChange> iterator = result.getDocumentChanges().iterator();

        loadUserChatMessages(iterator);
    }

    //    label recursive method for load chat in app
    private void loadUserChatMessages(Iterator<DocumentChange> messageIterator) {
        if (!messageIterator.hasNext()) {
            showChatView();
            smoothToLast();
            return;
        }

        DocumentChange documentChange = messageIterator.next();
        ChatMessage chatMessage = documentChange.getDocument().toObject(ChatMessage.class);

        switch (documentChange.getType()) {

            case ADDED:
                this.chatMessages.add(chatMessage);
                addChatMessage(chatMessage);
                break;

            case MODIFIED:
                updateChatMessage(chatMessage);
                break;

            case REMOVED:
                removeChatMessage(chatMessage);
        }

        loadUserChatMessages(messageIterator);
    }

    //    label on add new chat message in listen
    private void addChatMessage(ChatMessage chatMessage) {
        String message = Encryptions.decrypt(chatMessage.getMessage());
        chatMessage.setMessage(message);

        int chatIndex = inIndexCollection(this.chatMessages, chatMessage);
        this.chatAdapter.notifyItemInserted(chatIndex);
    }

    //    label update if the chat message was changed.
    private void updateChatMessage(ChatMessage chatMessage) {
        CallResult<ChatMessage> onChatMatches = chat -> {
            int chatIndex = inIndexCollection(this.chatMessages, chat);
            chat.setMessage(chatMessage.getMessage());
            chat.setDatetime(chatMessage.getDatetime());
            this.chatAdapter.notifyItemChanged(chatIndex);
        };

        this.chatMessages.stream()
                .filter(chat -> chat.getId().equals(chatMessage.getId()))
                .findFirst()
                .ifPresent(onChatMatches::onCall);
    }

    //    label remove if the chat message was removed.
    private void removeChatMessage(ChatMessage chatMessage) {
        CallResult<ChatMessage> onChatMatches = chat -> {
            int chatIndex = inIndexCollection(this.chatMessages, chat);
            this.chatMessages.remove(chat);
            this.chatAdapter.notifyItemRemoved(chatIndex);
        };

        this.chatMessages.stream()
                .filter(chat -> chat.getId().equals(chatMessage.getId()))
                .findFirst()
                .ifPresent(onChatMatches::onCall);
    }

    private <T extends Comparable<T>> int inIndexCollection(Collection<T> collection, T conversation) {
        return new ArrayList<>(collection).indexOf(conversation);
    }

    private void setFireStoreConnections() {
        this.userCollection = UserCollection.getInstance(getDefaultOnFailCall());

        this.chatMessageCollection = ChatMessageCollection
                .getChatMessageCollectionInstance(getDefaultOnFailCall());

        CallResult<Exception> onFail = getOnFailCallResult();
        this.conversationCollection = ConversationCollection
                .getConversationInstance(onFail);
    }

    @NonNull
    private CallResult<Exception> getOnFailCallResult() {
        return fail -> MessagesAppGenerator.showToast(getApplicationContext(),
                fail, FAIL_GET_RESPONSE);
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

    // label Action send message
    private synchronized void sendMessageAction() {
        String message = this.binding.inputMessage.getText().toString();
        if (message.trim().isEmpty()) {
            return;
        }

        Optional<String> member = getReceiverConversationMember();

        String receiverId = member.orElse(getLoggedUserId());

        message = Encryptions.encrypt(message);

        ChatMessage messageSent = new ChatMessage();

        messageSent.setSenderId(getLoggedUserId());
        messageSent.setReceivedId(receiverId);
        messageSent.setMessage(message);
        messageSent.setDatetime(new Date());

//        label when chat was inserted
        CallResult<Void> onChatInserted = task -> {
            this.actualConversation.setChatMessage(messageSent);
            this.actualConversation.setLastDateSent(messageSent.getDatetime());
            this.actualConversation.getReceiver().setReceiver(receiverId);
            this.actualConversation.getReceiver().setWasViewed(false);

            if (Objects.isNull(this.actualConversation.getId())) {
                Collection<QueryCondition<String, Object>> queryAppendConversation =
                        setQueryAppendConversationListener();

//                label to listen Conversation
                CallResult<Void> onCreateNewConversation = tasked -> {
                };

                this.conversationCollection.addCollectionById(this.actualConversation,
                        queryAppendConversation,
                        onCreateNewConversation,
                        getOnFailCallResult());
                return;
            }

            CallResult<Task<Void>> onSuccess = tasked -> {
//                label it do nothing so thats ok!
                smoothToLast();
            };

            CallResult<Exception> onFail = getOnFailCallResult();


            this.conversationCollection.update(this.actualConversation, onSuccess, onFail);

        };

        CallResult<Exception> onFailSaved = getOnFailCallResult();

        Collection<QueryCondition<String, Object>> queryChatInsertionConditions =
                CollectionsHelper.getArrayList();

        this.chatMessageCollection.addCollectionById(messageSent, queryChatInsertionConditions,
                onChatInserted, onFailSaved);

    }

    //    TODO
    @NonNull
    private Intent getUserLoggedInfo() {
        Intent userLoggedInfo = new Intent(this, InfoUserActivity.class);

        User userToInfo = new User();

        userToInfo.setAlias(this.actualConversation.getSenderName());
        userToInfo.setImage(this.actualConversation.getSenderImage());
        userToInfo.setImage(this.actualConversation.getSenderImage());

        userLoggedInfo.putExtra(FirebaseConstants.Users.KEY_USER_OBJ, userToInfo);
        return userLoggedInfo;
    }

    //  label  Get data from intent and preferences
    private void loadUserSelectedDetails() {
        this.loggedPreferencesManager = new PreferencesManager(getApplicationContext(),
                FirebaseConstants.SharedReferences.KEY_CHAT_USER_LOGGED_PREFERENCES);

        this.actualConversation = ((Conversation) getIntent()
                .getSerializableExtra(Conversations.KEY_CONVERSATION_OBJ));

        setReceiverInfoView();
    }

    //    label methods to set data for oldest conversations
    private void setReceiverInfoView() {
        this.binding.userChatAlias.setText(this.actualConversation.getSenderName());
    }


    // label: Methods to apply Listener to chat.

    private void listenMessages() {
        Collection<QueryCondition<String, Object>> queryChatMessageListener =
                setQueryChatMessageListener();

        this.chatMessageCollection.applyChatListener(queryChatMessageListener, this::onEvent);

    }

    //    label Set queries for chat messages listener and communicate.
    private Collection<QueryCondition<String, Object>> setQueryChatMessageListener() {
        Collection<QueryCondition<String, Object>> queryWhereListener = CollectionsHelper.getArrayList();

//        label query all members in the conversation
        QueryCondition<String, Object> filterByChatSenderId = new QueryCondition
                .Builder<String, Object>()
                .setKey(FirebaseConstants.ChatMessages.KEY_SENDER)
                .setValue(this.actualConversation.getMembers())
                .setMatchType(QueryCondition.MatchType.IN)
                .build();

        QueryCondition<String, Object> filterByChatReceiverId = new QueryCondition
                .Builder<String, Object>()
                .setKey(FirebaseConstants.ChatMessages.KEY_RECEIVED)
                .setValue(this.actualConversation.getMembers())
                .setMatchType(QueryCondition.MatchType.IN)
                .build();

        queryWhereListener.add(filterByChatSenderId);
        queryWhereListener.add(filterByChatReceiverId);

        return queryWhereListener;
    }
    //    Load the data from the messages in the database.

    private void loadChatData() {
        this.chatMessages = new TreeSet<>(Comparator.comparing(ChatMessage::getDatetime));
        this.chatAdapter = new ChatMessagingAdapter(getLoggedUserId(), this.chatMessages);
        this.binding.chatMessageRecycler.setAdapter(this.chatAdapter);

//        label: listen conversation
        listenConversation();
//        label: listen messages
        listenMessages();
    }

    //    Load and listen for a conversation
    private synchronized void listenConversation() {
        if (Objects.nonNull(this.actualConversation.getId())) {
            return;
        }

        EventListener<QuerySnapshot> onListenConversation = (result, fail) -> {
            if (Objects.isNull(result) || Objects.nonNull(fail)) {
                return;
            }

            List<DocumentSnapshot> snapshotList = result.getDocuments();

            for (DocumentSnapshot doc : snapshotList) {
                Collection<String> listConversion =
                        (Collection<String>) doc.get(Conversations.KEY_MEMBERS);

                int listConvSize = listConversion.size();

                int actualMembersSize = this.actualConversation.getMembers().size();

                // label: Sizes matches
                if (Objects.equals(actualMembersSize, listConvSize) &&
                    // label: and has the same values.
                        this.actualConversation.getMembers().containsAll(listConversion)) {
                    this.actualConversation.setId(doc.getId());
                    this.conversationOnRegistration.remove();
                    this.conversationOnRegistration = null;
                    return;
                }
            }
        };

        Collection<QueryCondition<String, Object>> conversationsConditions =
                setOnListenConversationsConditions();

//        label: Listen to the conversations if empty or got for the first time
        conversationOnRegistration = this.conversationCollection
                .applyCollectionListenerWithResult(conversationsConditions, onListenConversation);
    }

    private Collection<QueryCondition<String, Object>> setOnListenConversationsConditions() {
        Collection<QueryCondition<String, Object>> conditions = CollectionsHelper.getArrayList();

        QueryCondition<String, Object> conversationListener = new QueryCondition.Builder<String, Object>()
                .setKey(Conversations.KEY_MEMBERS)
                .setMatchType(QueryCondition.MatchType.ARRAY_IN)
                .setValue(this.actualConversation.getMembers().get(0))
                .build();

        conditions.add(conversationListener);

        return conditions;
    }

    private void smoothToLast() {
        if (chatMessages.isEmpty()) {
            return;
        }
        this.binding.chatMessageRecycler.smoothScrollToPosition(this.chatMessages.size() - 1);
    }

    private Collection<QueryCondition<String, Object>> setQueryAppendConversationListener() {
        Collection<QueryCondition<String, Object>> queryConditions = CollectionsHelper.getArrayList();
//        label Add queries here

        QueryCondition<String, Object> queryListenForAllMembers = new QueryCondition.Builder<String, Object>()
                .setKey(Conversations.KEY_MEMBERS)
                .setValue(this.actualConversation.getMembers())
                .setMatchType(QueryCondition.MatchType.ARRAY_IN_ANY)
                .build();

        queryConditions.add(queryListenForAllMembers);

        return queryConditions;
    }

    @NonNull
    private Optional<String> getReceiverConversationMember() {
        return this.actualConversation.getMembers()
                .stream().filter(mb -> !Objects.equals(mb, getLoggedUserId()))
                .findFirst();
    }

    private Collection<QueryCondition<String, Object>> setQueryChatInsertionConditions() {
        Collection<QueryCondition<String, Object>> queryConditions = CollectionsHelper.getArrayList();

        return queryConditions;
    }

    //    Get logged user
    private String getLoggedUserId() {
        return this.loggedPreferencesManager.getString(FirebaseConstants.Users.KEY_USER_ID);
    }

    private Call getDefaultOnFailCall() {
        return data -> {
            String message = (String) data.get(MESSAGE);
            MessagesAppGenerator.showToast(getApplicationContext(), message,
                    FAIL_GET_RESPONSE);
        };
    }

    //   label Get user availability
    private void setUserReceiverAvailableListener() {
        Collection<QueryCondition<String, Object>> queryUserAvailableListener = setQueryUserAvailableListener();

        this.userCollection.applyUserAvailability(queryUserAvailableListener, this.userAvailabilityListenerDefault);
    }

    private Collection<QueryCondition<String, Object>> setQueryUserAvailableListener() {
        Collection<QueryCondition<String, Object>> queryWhereListener = CollectionsHelper.getArrayList();

        this.actualConversation.getMembers()
                .stream()
//            label not equals to the instance object
                .filter(mb -> !Objects.equals(getLoggedUserId(), mb))
                .map(mb -> new QueryCondition.Builder<String, Object>()
                        .setKey(FirebaseConnection.DOCUMENT_ID)
                        .setValue(mb)
                        .setMatchType(QueryCondition.MatchType.EQUALS)
                        .build())
                .forEach(queryWhereListener::add);

        return queryWhereListener;
    }


    //  label  FOR event to get availability user.
    private final EventListener<QuerySnapshot> userAvailabilityListenerDefault = (value, error) -> {
        if (Objects.isNull(value) || Objects.nonNull(error)) {
            setUserAvailabilityViewItem(false);
            return;
        }

        List<DocumentSnapshot> documents = value.getDocuments();

        documents.stream().findFirst()
                .ifPresent(doc -> {
                    User user = doc.toObject(User.class);

                    switch (user.getAvailable()) {
                        case UserConstants.AVAILABLE:
                            setUserAvailabilityViewItem(true);
                            break;
                        default:
                        case UserConstants.NOT_AVAILABLE:
                            setUserAvailabilityViewItem(false);

                    }
                });
    };

    private void setUserAvailabilityViewItem(boolean flag) {
        if (flag) {
            this.binding.availableFlag.setVisibility(View.VISIBLE);
            return;
        }

        this.binding.availableFlag.setVisibility(View.GONE);
    }

    private void showChatView() {
        this.binding.progressBar.setVisibility(View.GONE);
        this.binding.chatMessageRecycler.setVisibility(View.VISIBLE);
    }

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