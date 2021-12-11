package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.AddMemberAdapter;
import com.sushant.whatsapp.Adapters.ParticipantAdapter;
import com.sushant.whatsapp.Interface.isClicked;
import com.sushant.whatsapp.Models.Groups;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityAddParticipantBinding;
import com.sushant.whatsapp.databinding.ActivityCreateGroupBinding;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddParticipant extends AppCompatActivity {

    ActivityAddParticipantBinding binding;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager;
    AddMemberAdapter adapter;
    DatabaseReference ref;
    ValueEventListener valueEventListener1;
    ArrayList<Users> participant= new ArrayList<>();
    int size=0;
    isClicked clicked;
    String Gid,Gname,GPP;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityAddParticipantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();

        Gid= getIntent().getStringExtra("GId1");
        Gname=getIntent().getStringExtra("GName1");
        GPP=getIntent().getStringExtra("GPic1");

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Groups").child(FirebaseAuth.getInstance().getUid())
                .child(Gid)
                .child("participant");


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
            }
        };

        adapter = new AddMemberAdapter(list, this,clicked,Gid);
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


        binding.txtAddParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (size>0){
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
                    addNewMember(timeStamp);
                    database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).child(Gid).child("participant").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshot1:snapshot.getChildren()){
                                Users users= snapshot1.getValue(Users.class);
                                for (int i=0;i<participant.size();i++){
                                    Users newMember=participant.get(i);
                                    newMember.setRole("normal");
                                    newMember.setJoinedGroupOn(timeStamp);
                                    addGroupInfo(Gid,users.getUserId());
                                    database.getReference().child("Groups").child(users.getUserId()).child(Gid).child("participant").child(newMember.getUserId()).setValue(newMember);
                                    database.getReference().child("Groups").child(newMember.getUserId()).child(Gid).child("participant").child(users.getUserId()).setValue(users);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    Intent intent= new Intent(AddParticipant.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    Toast.makeText(getApplicationContext(), "Member Added Successfully", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Please select participant", Toast.LENGTH_SHORT).show();
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

    void addGroupInfo(String groupId,String userId){
        HashMap<String,Object> map= new HashMap<>();
        map.put("groupId",Gid);
        map.put("groupName",Gname);
        map.put("groupPP",GPP);
        database.getReference().child("Groups").child(userId).child(groupId).updateChildren(map);
    }

    void addNewMember(String date){
        for (int i=0;i<participant.size();i++){
            Users newMember=participant.get(i);
            newMember.setRole("normal");
            newMember.setJoinedGroupOn(date);
            database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).child(Gid).child("participant").child(newMember.getUserId()).setValue(newMember);
        }
    }

    private void getAllUsers() {
        valueEventListener1= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users users = dataSnapshot.getValue(Users.class);
                        assert users != null;
                        if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                            if ("Accepted".equals(users.getRequest())){
                                list.add(users);
                            }
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
        ref =FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).child("Friends");
        ref.addValueEventListener(valueEventListener1);
    }

    private void searchUser(String query) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid()).child("Friends");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Users users = dataSnapshot.getValue(Users.class);
                        assert users != null;
                        if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())) {
                            if ("Accepted".equals(users.getRequest())){
                                if (users.getUserName().contains(query.toLowerCase())|| users.getMail().contains(query.toLowerCase())){
                                    list.add(users);
                                }
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ref!=null){
            ref.removeEventListener(valueEventListener1);
        }
    }
}