package com.sushant.whatsapp.Utils;

import androidx.recyclerview.widget.DiffUtil;

import com.sushant.whatsapp.Models.Messages;

import java.util.ArrayList;

public class ChatDiffCallback extends DiffUtil.Callback {
    private final ArrayList<Messages> mOldChatList;
    private final ArrayList<Messages> mNewChatList;

    public ChatDiffCallback(ArrayList<Messages> mOldEmployeeList, ArrayList<Messages> mNewEmployeeList) {
        this.mOldChatList = mOldEmployeeList;
        this.mNewChatList = mNewEmployeeList;
    }

    @Override
    public int getOldListSize() {
        return mOldChatList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewChatList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldChatList.get(oldItemPosition).getuId().equals(mNewChatList.get(
                newItemPosition).getuId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final Messages oldMessage = mOldChatList.get(oldItemPosition);
        final Messages newMessage = mNewChatList.get(newItemPosition);

        return oldMessage.getMessage().equals(newMessage.getMessage());
    }
}
