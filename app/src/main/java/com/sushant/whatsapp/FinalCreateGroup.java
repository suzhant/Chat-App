package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Models.GroupChat;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityFinalCreateGroupBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FinalCreateGroup extends AppCompatActivity{

    ActivityFinalCreateGroupBinding binding;
    ProgressDialog dialog;
    FirebaseDatabase database;
    ArrayList<Users> list;
    String uid,name,profilePic,mail;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityFinalCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database= FirebaseDatabase.getInstance();
        dialog= new ProgressDialog(this);
        dialog.setMessage("Creating Group");
        dialog.setCancelable(false);

        list= (ArrayList<Users>) getIntent().getSerializableExtra("participantList");

        database.getReference().child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user= snapshot.getValue(Users.class);
                uid=user.getUserId();
                name=user.getUserName();
                profilePic= user.getProfilePic();
                mail=user.getMail();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String GroupName= binding.editName.getEditText().getText().toString();
                GroupChat groupChat= new GroupChat();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                groupChat.setGroupName(GroupName);
                String id= GroupName+timeStamp;
                groupChat.setGroupId(id);

                for (int i=0;i<list.size();i++){
                    Users users= list.get(i);
                    DatabaseReference reference= database.getReference().child("Group Chats").child(users.getUserId()).child(id);
                    reference.setValue(groupChat).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            for (int i=0;i<list.size();i++){
                                Users users= list.get(i);
                                if (FirebaseAuth.getInstance().getUid()!=null){
                                    if (FirebaseAuth.getInstance().getUid().equals(users.getUserId())){
                                        users.setRole("Admin");
                                    }else{
                                        users.setRole("normal");
                                    }
                                    reference.child("participant").child(users.getUserId()).setValue(users);
                                }
                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Users admin= new Users();
                            admin.setUserId(uid);
                            admin.setUserName(name);
                            admin.setProfilePic(profilePic);
                            admin.setMail(mail);
                            admin.setRole("Admin");
                            reference.child("participant").child(admin.getUserId()).setValue(admin);
                        }
                    });
                }

                DatabaseReference reference= database.getReference().child("Group Chats").child(user.getUid()).child(id);
                reference.setValue(groupChat).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        for (int i=0;i<list.size();i++){
                            Users users= list.get(i);
                            if (FirebaseAuth.getInstance().getUid()!=null){
                                if (FirebaseAuth.getInstance().getUid().equals(users.getUserId())){
                                    users.setRole("Admin");
                                }else{
                                    users.setRole("normal");
                                }
                                reference.child("participant").child(users.getUserId()).setValue(users);
                            }
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Users admin= new Users();
                        admin.setUserId(uid);
                        admin.setUserName(name);
                        admin.setProfilePic(profilePic);
                        admin.setMail(mail);
                        admin.setRole("Admin");
                        reference.child("participant").child(admin.getUserId()).setValue(admin);
                    }
                });

                Intent intent= new Intent(FinalCreateGroup.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                Toast.makeText(getApplicationContext(), "Group Created Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }



}