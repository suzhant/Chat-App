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
        return mOldUsers.get(oldItemPosition).userId.equals(mNewUsers.get(newItemPosition).userId);
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
        if (!oldUser.getLastStory().equals(newUsers.getLastStory())) {
            bundle.putString("newLastStory", newUsers.getLastMessage());
        } else if (!oldUser.getProfilePic().equals(newUsers.getProfilePic())) {
            bundle.putString("newPic", newUsers.getProfilePic());
        } else if (!oldUser.getUserName().equals(newUsers.getUserName())) {
            bundle.putString("newUserName", newUsers.getUserName());
        } else if (oldUser.getStoriesCount() != newUsers.getStoriesCount()) {
            bundle.putInt("newStoriesCount", newUsers.getStoriesCount());
        }

        if (bundle.size() == 0) {
            return null;
        }

        return bundle;
    }
}
