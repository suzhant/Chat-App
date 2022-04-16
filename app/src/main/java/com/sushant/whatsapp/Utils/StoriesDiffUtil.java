package com.sushant.whatsapp.Utils;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.sushant.whatsapp.Models.Users;

import java.util.ArrayList;

public class StoriesDiffUtil extends DiffUtil.Callback {
    private final ArrayList<Users> mOldUsers;
    private final ArrayList<Users> mNewUsers;

    public StoriesDiffUtil(ArrayList<Users> mOldUsers, ArrayList<Users> mNewUsers) {
        this.mOldUsers = mOldUsers;
        this.mNewUsers = mNewUsers;
    }

    @Override
    public int getOldListSize() {
        return mOldUsers.size();
    }

    @Override
    public int getNewListSize() {
        return mNewUsers.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        final Users oldUser = mOldUsers.get(oldItemPosition);
        final Users newUsers = mNewUsers.get(newItemPosition);
        if (oldUser.getUserId() != null && newUsers.getUserId() != null) {
            return oldUser.userId.equals(newUsers.userId);
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Users oldUser = mOldUsers.get(oldItemPosition);
        final Users newUsers = mNewUsers.get(newItemPosition);
        return oldUser.equals(newUsers);
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        final Users oldUser = mOldUsers.get(oldItemPosition);
        final Users newUsers = mNewUsers.get(newItemPosition);

        Bundle bundle = new Bundle();
        if (oldUser.getLastStory() != null && newUsers.getLastStory() != null && !oldUser.getLastStory().equals(newUsers.getLastStory())) {
            bundle.putString("newLastStory", newUsers.getLastStory());
        } else if (oldUser.getProfilePic() != null && newUsers.getProfilePic() != null && !oldUser.getProfilePic().equals(newUsers.getProfilePic())) {
            bundle.putString("newPic", newUsers.getProfilePic());
        } else if (oldUser.getUserName() != null && newUsers.getUserName() != null && !oldUser.getUserName().equals(newUsers.getUserName())) {
            bundle.putString("newUserName", newUsers.getUserName());
        } else if (oldUser.getStoriesCount() == 0 && oldUser.getStoriesCount() != newUsers.getStoriesCount()) {
            bundle.putInt("newStoriesCount", newUsers.getStoriesCount());
        }

        if (bundle.size() == 0) {
            return null;
        }

        return bundle;
    }
}
