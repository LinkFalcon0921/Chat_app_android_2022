package com.flintcore.chat_app_android_22.firebase.models;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {

    @Exclude
    private String id;

    private String senderId;
    private String receivedId;
    private String message;
    private Date datetime;

    public ChatMessage() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceivedId() {
        return receivedId;
    }

    public void setReceivedId(String receivedId) {
        this.receivedId = receivedId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }
}
