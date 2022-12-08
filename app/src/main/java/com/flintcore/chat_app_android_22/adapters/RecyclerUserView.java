package com.flintcore.chat_app_android_22.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.flintcore.chat_app_android_22.databinding.UserItemContainerBinding;
import com.flintcore.chat_app_android_22.firebase.models.User;
import com.flintcore.chat_app_android_22.listeners.OnRecyclerItemListener;
import com.flintcore.chat_app_android_22.utilities.encrypt.Encryptions;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RecyclerUserView extends RecyclerView.Adapter<RecyclerUserView.UserViewHolder> {

    private final OnRecyclerItemListener<User> onClickRecycler;
    private final Collection<User> users;

    public RecyclerUserView(Collection<User> users, OnRecyclerItemListener<User> onClickRecycler) {
        this.users = users;
        this.onClickRecycler = onClickRecycler;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserItemContainerBinding binding = UserItemContainerBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);

        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setDataUser(this.getDataUser(position));
    }

    private User getDataUser(int position) {
        return this.users.toArray(new User[0])[position];
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    private void applyImage(String image, ImageView imageView){
       byte[] imageBytes = Encryptions.decryptAndroidImageFromString(image);
        Bitmap bitmapUser = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        imageView.setImageBitmap(bitmapUser);
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        private UserItemContainerBinding binding;

        public UserViewHolder(UserItemContainerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setDataUser(User user){
            this.binding.nameTxt.setText(user.getAlias());
            applyImage(user.getImage(), this.binding.ImagePreview);
            this.binding.secondOptionalTxt.setText(user.getUserAccess().getEmail());
            this.itemView.setOnClickListener(v -> {
                v.setEnabled(false);
                onClickRecycler.onClick(user);
                v.setEnabled(true);
            });
        }
    }
}
