package com.flintcore.chat_app_android_22.firebase.models.embbebed;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

//    For recent messages
public class Conversation {
    @Exclude
    private String id;
    private String lastMessageId;
    private Date lastDateSent;

    @Exclude
    private String senderImage;

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String conversationId) {
        this.id = conversationId;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    @Exclude
    public String getSenderImage() {
        return senderImage;
    }

    @Exclude
    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public Date getLastDateSent() {
        return lastDateSent;
    }

    public void setLastDateSent(Date lastDateSent) {
        this.lastDateSent = lastDateSent;
    }
}
