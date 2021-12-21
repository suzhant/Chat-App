package com.sushant.whatsapp.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.AddMemberAdapter;
import com.sushant.whatsapp.Adapters.RemoveUserAdapter;
import com.sushant.whatsapp.Interface.isClicked;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.R;
import com.sushant.whatsapp.databinding.FragmentGroupUsersBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class GroupUsersFragment extends Fragment {

    FragmentGroupUsersBinding binding;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager;
    RemoveUserAdapter adapter;
    ArrayList<Users> participant= new ArrayList<>();
    int size=0;
    isClicked clicked;
    DatabaseReference ref;
    ValueEventListener valueEventListener1;
    String Gid;
    AlertDialog dialog;

    public GroupUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentGroupUsersBinding.inflate(getLayoutInflater(),container,false);

        assert getArguments() != null;
        Gid = getArguments().getString("GroupId");


        clicked= new isClicked() {
            @Override
            public void isClicked(Boolean b, int position) {
                Users users=list.get(position);
                if (b){
                    participant.add(users);
                    size++;
                }else {
                    participant.remove(users);
                    size--;
                }
                if (size>0){
                    binding.fab.setVisibility(View.VISIBLE);
                }else {
                    binding.fab.setVisibility(View.GONE);
                }
            }
        };


        adapter = new RemoveUserAdapter(list, getContext(),clicked);
        binding.groupRecyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.groupRecyclerView.setAdapter(adapter);
        binding.groupRecyclerView.addItemDecoration(new DividerItemDecoration(binding.groupRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        layoutManager = new LinearLayoutManager(getContext());
        binding.groupRecyclerView.setLayoutManager(layoutManager);
        database = FirebaseDatabase.getInstance();

        AlertDialog.Builder builder= new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to remove selected users?").setTitle("Remove Users");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DatabaseReference reference=database.getReference().child("Groups").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(Gid).child("participant");
                for (int j=0;j<participant.size();j++){
                    Users users1=participant.get(j);
                    database.getReference().child("Groups").child(users1.getUserId()).child(Gid).setValue(null);
                    reference.child(users1.getUserId()).setValue(null);
                }

                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1:snapshot.getChildren()){
                            Users users=snapshot1.getValue(Users.class);
                            for (int j=0;j<participant.size();j++){
                                Users users1=participant.get(j);
                                database.getReference().child("Groups").child(users.getUserId()).child(Gid).child("participant").child(users1.getUserId()).setValue(null);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                list.removeAll(participant);
                binding.groupRecyclerView.setAdapter(new RemoveUserAdapter(list,getContext(),clicked));
                participant.clear();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                participant.clear();
                binding.groupRecyclerView.setAdapter(new RemoveUserAdapter(list,getContext(),clicked));
            }
        });
        dialog=builder.create();

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });


        getAllUsers();

        return binding.getRoot();
    }
    private void getAllUsers() {
        valueEventListener1= new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users users = dataSnapshot.getValue(Users.class);
                        assert users != null;
                        users.setUserId(dataSnapshot.getKey());
                        if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                                list.add(users);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                if (list.isEmpty()){
                    binding.txtNoFriend.setVisibility(View.VISIBLE);
                }else {
                    binding.txtNoFriend.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref =FirebaseDatabase.getInstance().getReference("Groups").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(Gid).child("participant");
        ref.addValueEventListener(valueEventListener1);
    }

}