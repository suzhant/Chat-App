package com.sushant.whatsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.ParticipantAdapter;
import com.sushant.whatsapp.Interface.isClicked;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityCreateGroupBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class ActivityCreateGroup extends AppCompatActivity {

    ActivityCreateGroupBinding binding;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager;
    ParticipantAdapter adapter;
    DatabaseReference ref;
    ValueEventListener valueEventListener1;
    ArrayList<Users> participant = new ArrayList<>();
    int size = 0;
    isClicked clicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();

        clicked = new isClicked() {
            @Override
            public void isClicked(Boolean b, int position) {
                Users users = list.get(position);
                if (b) {
                    participant.add(users);
                    size++;
                } else {
                    participant.remove(users);
                    size--;
                }
            }
        };

        adapter = new ParticipantAdapter(list, this, clicked);
        binding.participantRecycler.setItemAnimator(new DefaultItemAnimator());
        binding.participantRecycler.setAdapter(adapter);
        binding.participantRecycler.addItemDecoration(new DividerItemDecoration(binding.participantRecycler.getContext(), DividerItemDecoration.VERTICAL));
        layoutManager = new LinearLayoutManager(this);
        binding.participantRecycler.setLayoutManager(layoutManager);
        database = FirebaseDatabase.getInstance();
        getAllUsers();


        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        binding.forwardArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (size > 1) {
                    Intent intent = new Intent(ActivityCreateGroup.this, FinalCreateGroup.class);
                    intent.putExtra("participantList", participant);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Please select more than one participant", Toast.LENGTH_SHORT).show();
                }
            }
        });


        binding.editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(binding.editMessage.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void getAllUsers() {
        valueEventListener1 = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users users = dataSnapshot.getValue(Users.class);
                        assert users != null;
                        users.setUserId(dataSnapshot.getKey());
                        if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                            if (users.getRequest() != null && users.getRequest().equals("Accepted")) {
                                list.add(users);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                if (list.isEmpty()) {
                    binding.txtNoFriend.setVisibility(View.VISIBLE);
                } else {
                    binding.txtNoFriend.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("Friends");
        ref.addValueEventListener(valueEventListener1);
    }

    private void searchUser(String query) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("Friends");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    assert users != null;
                    assert user != null;
                    if (!users.getUserId().equals(user.getUid()) && users.getUserId() != null) {
                        if (users.getRequest() != null && users.getUserName().contains(query.toLowerCase()) || users.getMail().contains(query.toLowerCase())) {
                            list.add(users);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}