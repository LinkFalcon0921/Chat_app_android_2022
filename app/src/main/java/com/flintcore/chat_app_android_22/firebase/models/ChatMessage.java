package com.flintcore.chat_app_android_22.firebase.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public class ChatMessage implements Serializable, Comparable<ChatMessage> {

    @DocumentId
    private String id;
    private String senderId;
    private String receivedId;
    private String message;
    private Date datetime;

    public ChatMessage() {
    }

    @DocumentId
    public String getId() {
        return id;
    }
    @DocumentId
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage message1 = (ChatMessage) o;
        return id.equals(message1.id) && senderId.equals(message1.senderId)
                && receivedId.equals(message1.receivedId)
                && Objects.equals(message, message1.message) && datetime.equals(message1.datetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, senderId, receivedId, message, datetime);
    }

    @Override
    public int compareTo(ChatMessage chatMessage) {
        return Comparator.comparing(ChatMessage::getId)
                .thenComparing(ChatMessage::getDatetime)
                .thenComparing(ChatMessage::getReceivedId)
                .thenComparing(ChatMessage::getSenderId)
                .compare(this, chatMessage);
    }
}
