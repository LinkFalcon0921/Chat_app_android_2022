package com.flintcore.chat_app_android_22.listeners;

import java.io.Serializable;

@FunctionalInterface
public interface OnRecyclerItemListener<T extends Serializable> {
    void onClick(T t);
}
