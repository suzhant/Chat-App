package com.sushant.whatsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.MemberAdapter;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityGroupSettingsBinding;

import java.util.ArrayList;
import java.util.Map;

public class GroupSettings extends AppCompatActivity {

    ActivityGroupSettingsBinding binding;
    ValueEventListener valueEventListener1;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;
    LinearLayoutManager layoutManager;
    MemberAdapter adapter;
    DatabaseReference ref;
    String Gid,GName,GPP,CreatedOn,CreatedBy;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        Gid=getIntent().getStringExtra("GId1");
        GName=getIntent().getStringExtra("GName1");
        GPP=getIntent().getStringExtra("GPic1");
        CreatedOn=getIntent().getStringExtra("CreatedOn1");
        CreatedBy=getIntent().getStringExtra("CreatedBy1");

        AlertDialog.Builder builder = new AlertDialog.Builder(GroupSettings.this);
        builder.setMessage("Do you want to leave Group?")
                .setTitle("Leave Group");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).child(Gid).child("participant").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snapshot1: snapshot.getChildren()){
                            Users users=snapshot1.getValue(Users.class);
                            DatabaseReference reference=database.getReference().child("Groups").child(users.getUserId()).child(Gid).child("participant");
                            Query participant=reference.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid());
                            participant.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot snapshot2:snapshot.getChildren()){
                                        Users users1=snapshot2.getValue(Users.class);
                                        if (users1.getRole().equals("Admin")){
                                            reference.child(FirebaseAuth.getInstance().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Query query=reference.orderByValue().limitToLast(1);
                                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            for (DataSnapshot snapshot3: snapshot.getChildren()){
                                                                String userId=snapshot3.child("userId").getValue(String.class);
                                                                reference.child(userId).child("role").setValue("Admin");
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                                }
                                            });

                                        }else {
                                            reference.child(FirebaseAuth.getInstance().getUid()).removeValue();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).child(Gid).removeValue();

                Intent intent= new Intent(GroupSettings.this, MainActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        dialog=builder.create();

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
                intent.putExtra("CreatedOn1",CreatedOn);
                intent.putExtra("CreatedBy1",CreatedBy);
                startActivity(intent);
            }
        });

        binding.btnLeaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
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