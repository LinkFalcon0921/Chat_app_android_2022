package com.flintcore.chat_app_android_22.firebase.models.embbebed;

import com.google.firebase.firestore.DocumentId;

import java.util.Objects;

public class ConversationReceiver {
    @DocumentId
    private String id;

    private String receiver;
    private boolean wasViewed;

    public ConversationReceiver() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public boolean getWasViewed() {
        return wasViewed;
    }

    public void setWasViewed(boolean wasViewed) {
        this.wasViewed = wasViewed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationReceiver)) return false;
        ConversationReceiver that = (ConversationReceiver) o;
        return wasViewed == that.wasViewed && id.equals(that.id) && Objects.equals(receiver, that.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, receiver, wasViewed);
    }
}
