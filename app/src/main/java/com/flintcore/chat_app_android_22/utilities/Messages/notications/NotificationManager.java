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

import java.util.Date;
import java.util.Set;
import java.util.Objects;

public class NotificationManager {

    protected interface Constants {
        //        Notification
        String NOTIFICATION_CHANNEL_ID = "Chat_Notifications_0s19";
        String NOTIFICATION_APP_NAME = "Flint Core Chat";

        int RANDOM_RANGE_ID = 121;

        //        Messaging
        String NOTIFICATIONS_FAIL = "Not notifications available";
    }

    private NotificationChannel channelDefaultNotification;
    private boolean enable;
    private Set<String> disabledConversations;

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

    //    add a conversationId as disabled.
    public void addDisabledConversation(String conversationId) {
        this.disabledConversations.add(conversationId);
    }

    //    remove the conversation.
    public void removeConversation(String conversation) {
        this.disabledConversations.removeIf(c -> c.equals(conversation));
    }

    //    Check if the conversationId was disabled
    private boolean wasDisabled(String conversationId) {
        return this.disabledConversations.contains(conversationId);
    }

    /**
     * Start a notification if conversation was not disabled.
     */
    public synchronized void notify(@NonNull final Context context,
                                    @NonNull final NotificationManagerCompat notificationCompat,
                                    @NonNull final Conversation conversation,
                                    @NonNull final Date lastDate) {

//        Check if manager or conversation was disabled
        if (!isEnable() || wasDisabled(conversation.getId())) {
            return;
        }

        Date conversationLastDateSent = conversation.getLastDateSent();

//        Cancel if the conversation date was before the lastDate
        if (lastDate.after(conversationLastDateSent)) {
            return;
        }

//        Sender title
        String contentTitle = conversation.getSenderName();

//        message text body
        ChatMessage chatMessage = conversation.getChatMessage();
        String message = Encryptions.decrypt(chatMessage.getMessage());

        Notification notificationToGet = new NotificationCompat.Builder(context,
                Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat_notification)
                .setContentTitle(contentTitle)
                .setContentText(message)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
//               TODO: Change this later
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                .build();

        int id = (int) (Math.random() * Constants.RANDOM_RANGE_ID);
        notificationCompat.notify(id, notificationToGet);
    }
}
