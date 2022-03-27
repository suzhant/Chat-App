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
        if (oldMessage.getuId() != null && newMessage.getuId() != null) {
            return oldMessage.getuId().equals(newMessage.getuId());
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Messages oldMessage = mOldMessages.get(oldItemPosition);
        final Messages newMessage = mNewMessages.get(newItemPosition);
        return oldMessage.equals(newMessage);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        final Messages oldMessage = mOldMessages.get(oldItemPosition);
        final Messages newMessage = mNewMessages.get(newItemPosition);

        Bundle bundle = new Bundle();

        if (oldMessage.getMessage() != null && newMessage.getMessage() != null && !oldMessage.getMessage().equals(newMessage.getMessage())) {
            bundle.putString("newMessage", newMessage.getMessage());
        } else if (oldMessage.getProfilePic() != null && newMessage.getProfilePic() != null && !oldMessage.getProfilePic().equals(newMessage.getProfilePic())) {
            bundle.putString("newUserPic", newMessage.getProfilePic());
        } else if (oldMessage.getReaction() == -1 && oldMessage.getReaction() != newMessage.getReaction()) {
            bundle.putInt("newReaction", newMessage.getReaction());
        } else if (oldMessage.getImageUrl() != null && newMessage.getImageUrl() != null && !oldMessage.getImageUrl().equals(newMessage.getImageUrl())) {
            bundle.putString("newImageUrl", newMessage.getImageUrl());
        } else if (oldMessage.getVideoFile() != null && newMessage.getVideoFile() != null && !oldMessage.getVideoFile().equals(newMessage.getVideoFile())) {
            bundle.putString("newVideoFile", newMessage.getVideoFile());
        } else if (oldMessage.getAudioFile() != null && newMessage.getAudioFile() != null && !oldMessage.getAudioFile().equals(newMessage.getAudioFile())) {
            bundle.putString("newAudioFile", newMessage.getAudioFile());
        }

        if (bundle.size() == 0) {
            return null;
        }

        return bundle;
    }
}
