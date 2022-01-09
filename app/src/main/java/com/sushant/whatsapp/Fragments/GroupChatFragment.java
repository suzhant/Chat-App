package com.sushant.whatsapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sushant.whatsapp.ActivityCreateGroup;
import com.sushant.whatsapp.Adapters.GroupAdapter;
import com.sushant.whatsapp.Models.Groups;
import com.sushant.whatsapp.R;
import com.sushant.whatsapp.databinding.FragmentGroupChatBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class GroupChatFragment extends Fragment {

    FragmentGroupChatBinding binding;
    ArrayList<Groups> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager;
    GroupAdapter adapter;
    ValueEventListener eventListener,eventListener1;
    DatabaseReference groupChat;
    Set<String> hset = new HashSet<>();
    ArrayList<String> id ;

    public GroupChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGroupChatBinding.inflate(inflater, container, false);

        database = FirebaseDatabase.getInstance();
        adapter= new GroupAdapter(list,getContext());
        binding.chatRecyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getContext());
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        binding.fabCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getContext(), ActivityCreateGroup.class);
                startActivity(intent);
            }
        });

        groupChat=database.getReference().child("Groups").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        eventListener=new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Groups groups = dataSnapshot.getValue(Groups.class);
                    assert groups != null;
                    if (groups.getGroupId()!=null){
                        list.add(groups);
                    }
                }
                database.getReference().child("GroupList").addValueEventListener(eventListener1);
                adapter.notifyDataSetChanged();

//                adapter= new GroupAdapter(list,getContext());
//                binding.chatRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        groupChat.addValueEventListener(eventListener);
        eventListener1= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hset.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Groups groups=snapshot1.getValue(Groups.class);
                    assert groups != null;
                    if (groups.getGroupId()!=null){
                        hset.add(groups.getGroupId());
                    }
                }
                subscribeTopic(hset);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        database.getReference().child("GroupList").addValueEventListener(eventListener1);

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                groupChat.addValueEventListener(eventListener);
                database.getReference().child("GroupList").addValueEventListener(eventListener1);
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        groupChat.removeEventListener(eventListener);
        database.getReference().child("GroupList").removeEventListener(eventListener1);
        groupChat.keepSynced(false);
    }

    private void subscribeTopic(Set<String> hset){
        id=new ArrayList<>(hset);
        for (int i=0;i<id.size();i++){
            boolean flag=false;
            for (int j=0;j<list.size();j++){
                Groups groups=list.get(j);
                if (groups.getGroupId().equals(id.get(i))){
                    flag=true;
                    break;
                }
            }
            if (flag){
                FirebaseMessaging.getInstance().subscribeToTopic(id.get(i));
            }else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(id.get(i));
            }
        }
    }

}