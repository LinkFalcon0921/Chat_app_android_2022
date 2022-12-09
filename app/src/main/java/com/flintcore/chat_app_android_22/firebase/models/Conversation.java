package com.flintcore.chat_app_android_22.firebase.models;

import com.flintcore.chat_app_android_22.firebase.models.embbebed.ConversationReceiver;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

//    For recent messages
public class Conversation implements Serializable, Comparable<Conversation> {
    @DocumentId
    private String id;

    /**
     * Beware with object chatMessage
     */
    private ChatMessage chatMessage;
    private Date lastDateSent;

    private ConversationReceiver receiver;
    private List<String> members;

    @Exclude
    private String senderImage;
    @Exclude
    private String senderName;
    @Exclude
    private String message;

    public Conversation() {
        setMembers(new LinkedList<>());
        setChatMessage(new ChatMessage());
        setReceiver(new ConversationReceiver());
    }

    @DocumentId
    public String getId() {
        return id;
    }

    @DocumentId
    public void setId(String conversationId) {
        this.id = conversationId;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage lastMessageId) {
        this.chatMessage = lastMessageId;
    }

    @Exclude
    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conversation)) return false;
        Conversation that = (Conversation) o;
        return id.equals(that.id) && chatMessage.equals(that.chatMessage)
                && lastDateSent.equals(that.lastDateSent) && Objects.equals(receiver, that.receiver)
                && Objects.equals(senderImage, that.senderImage)
                && Objects.equals(senderName, that.senderName) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatMessage, lastDateSent, receiver, senderImage, senderName, message);
    }

    @Override
    public int compareTo(Conversation conversation) {
        Comparator<Conversation> membersComparator = (c1, c2) -> {
            List<String> usersC1 = c1.getMembers();
            List<String> usersC2 = c2.getMembers();

            return Boolean.compare(
                    usersC1.containsAll(usersC2),
                    usersC2.containsAll(usersC1));
        };
        return Comparator.comparing(Conversation::hashCode)
                .thenComparing(Conversation::getLastDateSent)
                .thenComparing(Conversation::getChatMessage)
                .thenComparing(membersComparator)
                .compare(this, conversation);
    }
}
