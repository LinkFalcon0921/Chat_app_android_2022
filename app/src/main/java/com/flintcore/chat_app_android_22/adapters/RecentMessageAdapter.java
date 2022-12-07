package com.flintcore.chat_app_android_22.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flintcore.chat_app_android_22.databinding.UserRecentItemContainerBinding;
import com.flintcore.chat_app_android_22.firebase.models.Conversation;
import com.flintcore.chat_app_android_22.listeners.OnRecyclerItemListener;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;

import java.util.Collection;

public class RecentMessageAdapter extends RecyclerView.Adapter<RecentMessageAdapter.ConversationHolder> {

    private final Collection<Conversation> recentMessages;
    private final OnRecyclerItemListener<Conversation> onClickListener;

    public RecentMessageAdapter(Collection<Conversation> recentMessages, OnRecyclerItemListener<Conversation> onClickListener) {
        this.recentMessages = recentMessages;
        this.onClickListener = onClickListener;
    }

    private static void applyImage(String image, ImageView imageView) {
        byte[] imageBytes = Encryptions.decryptAndroidImageFromString(image);
        Bitmap bitmapUser = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        imageView.setImageBitmap(bitmapUser);
    }

    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserRecentItemContainerBinding binding = UserRecentItemContainerBinding
                .inflate(LayoutInflater.from(parent.getContext()),
                        parent, false);

        return new ConversationHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationHolder holder, int position) {
        holder.setData(this.recentMessages.toArray(new Conversation[0])[position],
                this.onClickListener);
    }

    @Override
    public int getItemCount() {
        return this.recentMessages.size();
    }

    //    Class static

    static class ConversationHolder extends RecyclerView.ViewHolder {

        private final UserRecentItemContainerBinding binding;

        public ConversationHolder(@NonNull UserRecentItemContainerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setData(Conversation recentMessage, OnRecyclerItemListener<Conversation> l) {
            applyImage(recentMessage.getSenderImage(), this.binding.ImagePreview);
            this.binding.nameTxt.setText(recentMessage.getSenderName());
            this.binding.secondOptionalTxt.setText(Encryptions.decrypt(recentMessage.getMessage()));
            this.binding.getRoot().setOnClickListener(v -> l.onClick(recentMessage));
        }
    }
}
