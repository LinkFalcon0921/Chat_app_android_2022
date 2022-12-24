package com.flintcore.chat_app_android_22.utilities.Messages.notications;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.flintcore.chat_app_android_22.R;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.utilities.Messages.MessagesAppGenerator;
import com.flintcore.chat_app_android_22.utilities.collections.CollectionsHelper;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.Objects;

public class NotificationManager {

    protected static interface Constants {
        //        Notification
        String NOTIFICATION_CHANNEL_ID = "Chat_Notifications_0s19";
        String NOTIFICATION_APP_NAME = "Flint Core Chat";

        int RANDOM_RANGE_ID = 121;

        //        Messaging
        String NOTIFICATIONS_FAIL = "Not notifications available";
    }

    private NotificationChannel channelDefaultNotification;
    private boolean enable;
    private Set<Conversation> disabledConversations;

    public NotificationManager(Application app) {
        this.enable = true;
        this.disabledConversations = CollectionsHelper.getHashSet();
        setNotificationChannel(app);
    }

    private void setNotificationChannel(Application app) {
        try {
//            Setting the notification Manager
            this.channelDefaultNotification = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    Constants.NOTIFICATION_APP_NAME,
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );

            app.getSystemService(android.app.NotificationManager.class)
                    .createNotificationChannel(this.channelDefaultNotification);

        } catch (Exception ex) {
            MessagesAppGenerator.showToast(app, Constants.NOTIFICATIONS_FAIL,
                    Constants.NOTIFICATIONS_FAIL);
        }
    }

    public synchronized void setEnable(Activity activity, boolean value) {
        if (Objects.isNull(activity)) {
            return;
        }

        this.enable = value;
    }

    public synchronized boolean isEnable() {
        return this.enable;
    }

    //    add a conversation as disabled.
    private void addDisabledConversation(Conversation conversation) {
        this.disabledConversations.add(conversation);
    }

    //    remove the conversation.
    private void removeConversation(Conversation conversation) {
        this.disabledConversations.remove(conversation);
    }

    //    Check if the conversation was disabled
    private boolean wasDisabled(Conversation conversation) {
        return this.disabledConversations.contains(conversation);
    }

    /**
     * Start a notification if conversation was not disabled.
     */
    public synchronized void notify(@NonNull final Context context,
                                    @NonNull final NotificationManagerCompat notificationCompat,
                                    @NonNull final Conversation conversation) {

//        Check if manager or conversation was disabled
        if (!isEnable() || wasDisabled(conversation)) {
            return;
        }

//        Check if the date was before to the last
        ChatMessage chatMessage = conversation.getChatMessage();
        String messageConverted = Encryptions.decrypt(chatMessage.getMessage());

        Notification notificationToGet = new NotificationCompat.Builder(
                context,
                Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat_notification)
                .setContentTitle(Constants.NOTIFICATION_APP_NAME)
                .setContentText(messageConverted)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        int id = (int) (Math.random() * Constants.RANDOM_RANGE_ID);
        notificationCompat.notify(id, notificationToGet);
    }
}
