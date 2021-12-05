package com.sushant.whatsapp.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.sushant.whatsapp.Fragments.CallsFragment;
import com.sushant.whatsapp.Fragments.ChatsFragment;
import com.sushant.whatsapp.Fragments.GroupChatFragment;

public class FragmentsAdapter extends FragmentPagerAdapter {
    public FragmentsAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public FragmentsAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new GroupChatFragment();
            case 2:
                return new CallsFragment();
            case 0:
            default:
                return new ChatsFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position == 0) {
            title = "CHATS";
        }
        if (position == 1) {
            title = "GROUP CHATS";
        }
        if (position == 2) {
            title = "CALLS";
        }

        return title;
    }
}
