package com.sushant.whatsapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.sushant.whatsapp.Fragments.CallsFragment;
import com.sushant.whatsapp.Fragments.ChatsFragment;
import com.sushant.whatsapp.Fragments.GroupChatFragment;

public class FragmentsAdapter extends FragmentStateAdapter {

    public FragmentsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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
    public int getItemCount() {
        return 3;
    }


    //    public FragmentsAdapter(@NonNull FragmentManager fm) {
//        super(fm);
//    }
//
//    public FragmentsAdapter(@NonNull FragmentManager fm, int behavior) {
//        super(fm, behavior);
//    }
//
//    @NonNull
//    @Override
//    public Fragment getItem(int position) {
//        switch (position) {
//            case 1:
//                return new GroupChatFragment();
//            case 2:
//                return new CallsFragment();
//            case 0:
//            default:
//                return new ChatsFragment();
//        }
//    }
//
//    @Override
//    public int getCount() {
//        return 3;
//    }
//
//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        String title = null;
//        if (position == 0) {
//            title = "CHATS";
//        }
//        if (position == 1) {
//            title = "GROUP CHATS";
//        }
//        if (position == 2) {
//            title = "CALLS";
//        }
//
//        return title;
//    }
}
