package com.sushant.whatsapp.Utils;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.sushant.whatsapp.Models.Messages;

import java.util.ArrayList;

public class MessageDiffUtils extends DiffUtil.Callback {

    private final ArrayList<Messages> mOldMessages;
    private final ArrayList<Messages> mNewMessages;

    public MessageDiffUtils(ArrayList<Messages> mOldMessages, ArrayList<Messages> mNewMessages) {
        this.mOldMessages = mOldMessages;
        this.mNewMessages = mNewMessages;
    }

    @Override
    public int getOldListSize() {
        return mOldMessages.size();
    }

    @Override
    public int getNewListSize() {
        return mNewMessages.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        final Messages oldMessage = mOldMessages.get(oldItemPosition);
        final Messages newMessage = mNewMessages.get(newItemPosition);
        if (oldMessage.getMessageId() != null && newMessage.getMessageId() != null) {
            return oldMessage.getMessageId().equals(newMessage.getMessageId());
        } else {
            return false;
        }

    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Messages oldMessage = mOldMessages.get(oldItemPosition);
        final Messages newMessage = mNewMessages.get(newItemPosition);
        return oldMessage == newMessage;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        final Messages oldMessage = mOldMessages.get(oldItemPosition);
        final Messages newMessage = mNewMessages.get(newItemPosition);

        Bundle bundle = new Bundle();

        if (!oldMessage.getMessage().equals(newMessage.getMessage())) {
            bundle.putString("newMessage", newMessage.getMessage());
        } else if (!oldMessage.getProfilePic().equals(newMessage.getProfilePic())) {
            bundle.putString("newPic", newMessage.getProfilePic());
        } else if (oldMessage.getReaction() != newMessage.getReaction()) {
            bundle.putInt("newReaction", newMessage.getReaction());
        }

        if (bundle.size() == 0) {
            return null;
        }

        return bundle;
    }
}
