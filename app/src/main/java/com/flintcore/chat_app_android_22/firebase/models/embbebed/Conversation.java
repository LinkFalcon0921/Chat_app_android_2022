package com.flintcore.chat_app_android_22.firebase.models.embbebed;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

//    For recent messages
public class Conversation implements Serializable {
    @Exclude
    private String id;

    private String lastMessageId;
    private Date lastDateSent;

    @Exclude
    private String senderId;
    @Exclude
    private String senderImage;
    @Exclude
    private String senderName;
    @Exclude
    private String message;

    @Exclude
    public String getId() {
        return id;
    }

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

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    @Exclude
    public String getMessage() {
        return message;
    }

    @Exclude
    public String getSenderName() {
        return senderName;
    }

    @Exclude
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getLastDateSent() {
        return lastDateSent;
    }

    public void setLastDateSent(Date lastDateSent) {
        this.lastDateSent = lastDateSent;
    }
}
