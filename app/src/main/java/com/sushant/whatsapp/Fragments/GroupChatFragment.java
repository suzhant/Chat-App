package com.sushant.whatsapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.ActivityCreateGroup;
import com.sushant.whatsapp.Adapters.GroupAdapter;
import com.sushant.whatsapp.Models.Groups;
import com.sushant.whatsapp.databinding.FragmentGroupChatBinding;

import java.util.ArrayList;
import java.util.Objects;


public class GroupChatFragment extends Fragment {

    FragmentGroupChatBinding binding;
    ArrayList<Groups> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager;
    GroupAdapter adapter;

    public GroupChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

        database.getReference().child("Groups").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Groups groups = dataSnapshot.getValue(Groups.class);
                    assert groups != null;
                    groups.setGroupId(dataSnapshot.getKey());
                    list.add(groups);
                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                database.getReference().child("Groups").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Groups groups = dataSnapshot.getValue(Groups.class);
                            assert groups != null;
                            groups.setGroupId(dataSnapshot.getKey());
                            list.add(groups);
                        }
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });
        return binding.getRoot();
    }
}