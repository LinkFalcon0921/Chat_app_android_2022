package com.flintcore.chat_app_android_22.firebase;

public interface FirebaseConstants {

    interface SharedReferences {
        String KEY_CHAT_USER_LOGGED_PREFERENCES = "userLoggedPreferences";
        String KEY_FMC_TOKEN = "FMC_LOGGED";
    }

    interface Messages {
        String FAIL_GET_RESPONSE = "The database is not available";
        String NO_USERS_AVAILABLE = "No users available";
        String NO_CHATS_RECENT = "No chat recently";
    }

    interface Results {
        String MESSAGE = "message";
    }

    interface KeyEncryption {
        String KEY = "DES/CBC/NoPadding";
    }

    interface Users {
        String COLLECTION = "users";
        String KEY_USER_OBJ = "user";
        String KEY_ALIAS = "alias";
        String KEY_IMAGE = "image";
        String KEY_LOGIN_OBJ = "userAccess";
        String KEY_EMAIL = "email";
        String KEY_PASS = "pass";
        String KEY_CONFIRM_PASS = "pass";

        //        Defaults
        String KEY_USERS_LIST = "userList";
        String KEY_IS_SIGNED_IN = "isSignedIn";
        String KEY_USER_ID = "userId";
    }

    interface ChatMessages {

        String KEY_COLLECTION = "chatMessage";
        String KEY_ID = "chatId";
        String KEY_SENDER = "senderId";
        String KEY_RECEIVED = "receivedId";
        String KEY_MESSAGE = "message";
        String KEY_DATETIME = "datetime";

        //        Defaults
        String KEY_LIST_CHATS = "chatsMessagesList";
        String KEY_CHAT_OBJ = "conversationObj";

    }
    interface Conversations{
        String KEY_COLLECTION = "conversations";
        String KEY_ID = "conversationId";
        String KEY_LAST_MESSAGE_ID = "lastMessageId";
        String KEY_SENDER = "senderId";
        //        String KEY_SENDER_ID = "conversationSenderId";
//        String KEY_RECEIVER_ID = "conversationReceiverId";
        String KEY_LAST_DATE = "lastDateSent";

//      Defaults
        String KEY_CHAT_CONVERSATIONS = "conversationList";
        String KEY_CONVERSATION_OBJ = "conversationObj";
    }
}
