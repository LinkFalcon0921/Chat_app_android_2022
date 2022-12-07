package com.flintcore.chat_app_android_22.firebase.models;

import com.flintcore.chat_app_android_22.firebase.models.embbebed.ConversationReceiver;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

//    For recent messages
public class Conversation implements Serializable, Comparable<Conversation> {
    @DocumentId
    private String id;

    private String lastMessageId;
    private Date lastDateSent;
    private String lastSenderId;

    private ConversationReceiver receiver;

    @Exclude
    private String senderImage;
    @Exclude
    private String senderName;
    @Exclude
    private String message;

    public Conversation() {
        this.receiver = new ConversationReceiver();
    }

    @DocumentId
    public String getId() {
        return id;
    }
    @DocumentId
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
    public String getLastSenderId() {
        return lastSenderId;
    }

    public void setLastSenderId(String lastSenderId) {
        this.lastSenderId = lastSenderId;
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

    public ConversationReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(ConversationReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conversation)) return false;
        Conversation that = (Conversation) o;
        return id.equals(that.id) && lastMessageId.equals(that.lastMessageId)
                && lastDateSent.equals(that.lastDateSent) && lastSenderId.equals(that.lastSenderId)
                && Objects.equals(receiver, that.receiver) && Objects.equals(senderImage, that.senderImage)
                && Objects.equals(senderName, that.senderName) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastMessageId, lastDateSent, lastSenderId, receiver, senderImage, senderName, message);
    }

    @Override
    public int compareTo(Conversation conversation) {
        return Comparator.comparing(Conversation::hashCode)
                .thenComparing(Conversation::getLastDateSent)
                .thenComparing(Conversation::getLastMessageId)
                .compare(this, conversation);
    }
}
