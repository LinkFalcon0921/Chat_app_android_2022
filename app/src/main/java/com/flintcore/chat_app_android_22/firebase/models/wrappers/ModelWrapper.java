package com.flintcore.chat_app_android_22.firebase.models.wrappers;

import com.flintcore.chat_app_android_22.firebase.FirebaseConstants;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.ChatMessages;
import com.flintcore.chat_app_android_22.firebase.FirebaseConstants.Conversations;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ModelWrapper {

    private static ModelWrapper instance;

    private ModelWrapper() {
    }

    public static ModelWrapper getInstance() {
        if (Objects.isNull(instance)) {
            instance = new ModelWrapper();
        }

        return instance;
    }

    public Map<String, Object> getDocument(User user) {

        HashMap<String, Object> userMap = CollectionsHelper.getHashMap();

//        userMap.put(FirebaseConnection.DOCUMENT_ID, user.getId());
        userMap.put(FirebaseConstants.Users.KEY_ALIAS, user.getAlias());
        userMap.put(FirebaseConstants.Users.KEY_IMAGE, user.getImage());
        userMap.put(FirebaseConstants.Users.KEY_AVAILABLE, user.getAvailable());
//        todo methods that wrap as map
        userMap.put(FirebaseConstants.Users.KEY_LOGIN_OBJ, user.getUserAccess());
//        Single action database already add it
//        userMap.put(FirebaseConstants.SharedReferences.KEY_FMC_TOKEN, user.getToken());

        return userMap;
    }

    public Map<String, Object> getDocument(ChatMessage chatMessage) {
        HashMap<String, Object> chatMessageMap = CollectionsHelper.getHashMap();

        chatMessageMap.put(ChatMessages.KEY_SENDER,
                chatMessage.getSenderId());
        chatMessageMap.put(ChatMessages.KEY_RECEIVED,
                chatMessage.getReceivedId());
        chatMessageMap.put(ChatMessages.KEY_MESSAGE,
                chatMessage.getMessage());
        chatMessageMap.put(ChatMessages.KEY_DATETIME,
                chatMessage.getDatetime());

        return chatMessageMap;
    }

    public Map<String, Object> getDocument(Conversation conversation) {
        HashMap<String, Object> conversationMap = CollectionsHelper.getHashMap();

        conversationMap.put(Conversations.KEY_LAST_MESSAGE,
                conversation.getChatMessage().getId());
        conversationMap.put(Conversations.KEY_LAST_DATE,
                conversation.getLastDateSent());
        conversationMap.put(Conversations.KEY_CONVERSATION_RECEIVER,
                conversation.getReceiver());
        conversationMap.put(Conversations.KEY_MEMBERS,
                conversation.getMembers());

        return conversationMap;
    }


}
