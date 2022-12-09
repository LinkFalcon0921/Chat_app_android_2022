package com.flintcore.chat_app_android_22.firebase;

public interface FirebaseConstants {

    String DAT = ".";

    interface SharedReferences {
        String KEY_CHAT_USER_LOGGED_PREFERENCES = "userLoggedPreferences";
        String KEY_FMC_TOKEN = "token";
    }

    interface Messages {
        String FAIL_GET_RESPONSE = "The database is not available";
        String NO_USERS_AVAILABLE = "No users available";
        String NO_CHATS_RECENT = "No chat recently";
        String NO_USER = "No user present";
        String NOT_VALID_CREEDENTIALS = "Not valid creedentials. \nUse another one.";
        String CREDENTIALS_DOES_NOT_EXISTS = "User credentials does not exists";
        String NOT_FOUND_DATA_USER_REGISTERED = "Error getting data.\nLogging out...";
        String SIGN_IN_SUCCESSFUL = "Sign in successfully!";
        String SIGN_OUT_SUCCESSFUL = "Sign out successfully!";
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
        String KEY_AVAILABLE = "available";

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

    interface Conversations {
        String KEY_COLLECTION = "conversations";
        String KEY_ID = "conversationId";
        String KEY_LAST_MESSAGE_ID = "lastMessageId";
        String KEY_SENDER = "senderId";
        /**
         * This is the object to check if message was saw by user.
         */
        String KEY_CONVERSATION_STATUS = "receiver.receiver";
        String KEY_LAST_DATE = "lastDateSent";

        //      Defaults
        String KEY_CHAT_CONVERSATIONS = "conversationList";
        String KEY_CONVERSATION_OBJ = "conversationObj";
    }

    interface ConversationReceiver {
        String KEY_COLLECTION = "conversationReceiver";
        String KEY_OBJ = "receiver";
        String KEY_ID = "conversationReceiverId";
        String KEY_CONVERSATION_LAST_RECEIVER_ID = KEY_OBJ.concat(DAT).concat("receiver");
        String KEY_VIEWED = KEY_OBJ.concat(DAT).concat("wasViewed");
    }

}
