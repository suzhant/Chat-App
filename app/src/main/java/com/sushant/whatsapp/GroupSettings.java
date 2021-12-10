package com.sushant.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.MemberAdapter;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityGroupSettingsBinding;

import java.util.ArrayList;

public class GroupSettings extends AppCompatActivity {

    ActivityGroupSettingsBinding binding;
    ValueEventListener valueEventListener1;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager;
    MemberAdapter adapter;
    DatabaseReference ref;
    String Gid,GName,GPP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        Gid=getIntent().getStringExtra("GId1");
        GName=getIntent().getStringExtra("GName1");
        GPP=getIntent().getStringExtra("GPic1");

        binding.txtGroupName.setText(GName);

        adapter = new MemberAdapter(list,Gid,this);
        binding.participantRecycler.setItemAnimator(new DefaultItemAnimator());
        binding.participantRecycler.setAdapter(adapter);
        binding.participantRecycler.addItemDecoration(new DividerItemDecoration(binding.participantRecycler.getContext(), DividerItemDecoration.VERTICAL));
        layoutManager = new LinearLayoutManager(this);
        binding.participantRecycler.setLayoutManager(layoutManager);
        database = FirebaseDatabase.getInstance();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getAllUsers();

        binding.btnAddParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(getApplicationContext(), AddParticipant.class);
                intent.putExtra("GId1",Gid);
                intent.putExtra("GName1",GName);
                intent.putExtra("GPic1",GPP);
                startActivity(intent);
            }
        });

    }

    private void getAllUsers() {
        valueEventListener1= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    assert users != null;
                    users.setUserId(dataSnapshot.getKey());
                    list.add(users);

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref = FirebaseDatabase.getInstance().getReference("Groups").child(FirebaseAuth.getInstance().getUid()).child(Gid).child("participant");
        ref.addValueEventListener(valueEventListener1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(valueEventListener1);
    }
}