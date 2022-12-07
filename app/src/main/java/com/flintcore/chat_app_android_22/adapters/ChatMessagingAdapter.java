package com.flintcore.chat_app_android_22.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flintcore.chat_app_android_22.databinding.ItemContentReceiveMessageBinding;
import com.flintcore.chat_app_android_22.databinding.ItemContentSentMessageBinding;
import com.flintcore.chat_app_android_22.firebase.models.ChatMessage;
import com.flintcore.chat_app_android_22.utilities.dates.DateUtils;

import java.util.Collection;
import java.util.List;

public class ChatMessagingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface ViewType {
        int SENT = 0, RECEIVED = 1;
    }

    private final Collection<ChatMessage> messages;
    private final String senderId;
    private final DateUtils dateUtils;

    public ChatMessagingAdapter(@NonNull String senderId, Collection<ChatMessage> messages) {
        this.senderId = senderId;
        this.messages = messages;
        this.dateUtils = DateUtils.getDateUtils("MMMM dd, yyyy hh:mm a");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ViewType.SENT:
                ItemContentSentMessageBinding bindingSent = ItemContentSentMessageBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false);

                return new SendMessageHolder(bindingSent);

            default:
            case ViewType.RECEIVED:
                ItemContentReceiveMessageBinding bindingReceived = ItemContentReceiveMessageBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false);

                return new ReceivedMessageHolder(bindingReceived);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = getChatMessage(position);
        switch (getItemViewType(position)) {
            case ViewType.SENT:
                ((SendMessageHolder) holder).setData(this.dateUtils,
                        message);
                break;
            default:
            case ViewType.RECEIVED:
                ((ReceivedMessageHolder) holder).setData(this.dateUtils,
                        message);
        }
    }

    private ChatMessage getChatMessage(int position) {
        ChatMessage message = this.messages.toArray(new ChatMessage[0])[position];
        return message;
    }

    @Override
    public int getItemCount() {
        return this.messages.size();
    }

//    Overrides not required methods


    @Override
    public int getItemViewType(int position) {
        String senderId = this.getChatMessage(position).getSenderId();
        if (senderId.equals(this.senderId)) {
            return ViewType.SENT;
        }
        return ViewType.RECEIVED;
    }

    static class SendMessageHolder extends RecyclerView.ViewHolder {
        private final ItemContentSentMessageBinding binding;

        public SendMessageHolder(ItemContentSentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(DateUtils dateUtils, ChatMessage message) {
            this.binding.textMessage.setText(message.getMessage());
            String dateText = dateUtils.getReadableDate(message.getDatetime());
            this.binding.dateMessageSent.setText(dateText);
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        private final ItemContentReceiveMessageBinding binding;

        public ReceivedMessageHolder(ItemContentReceiveMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(DateUtils dateUtils, ChatMessage message) {
            this.binding.textMessage.setText(message.getMessage());
            String dateText = dateUtils.getReadableDate(message.getDatetime());
            this.binding.dateMessageReceived.setText(dateText);
        }
    }
}
