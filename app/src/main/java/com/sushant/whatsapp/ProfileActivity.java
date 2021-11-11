package com.sushant.whatsapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityProfileBinding;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    FirebaseDatabase database;
    boolean friend=false;
    String sendername,pp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        database=FirebaseDatabase.getInstance();

        String userName = getIntent().getStringExtra("UserNamePA");
        String profilePic = getIntent().getStringExtra("ProfilePicPA");
        String email=getIntent().getStringExtra("EmailPA");
        String Receiverid=getIntent().getStringExtra("UserIdPA");

        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.imgProfile);
        binding.txtEmail.setText(email);
        binding.txtUserName.setText(userName);

        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        database.getReference().child("Users").child(Objects.requireNonNull(user.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users user = snapshot.getValue(Users.class);
                        assert user != null;
                       sendername=user.getUserName();
                       pp=user.getProfilePic();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        DatabaseReference reference = database.getReference("Users").child(user.getUid()).child("Friends");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1:snapshot.getChildren()){
                        Users users=snapshot1.getValue(Users.class);
                        if (users.getUserId().equals(Receiverid)){
                        if (users.getRequest().equals("Accepted")){
                            binding.btnAddFriend.setText("Unfriend");
                            binding.btnAddFriend.setBackgroundColor(Color.RED);
                            friend=true;
                        }
                        if (users.getRequest().equals("Req_Sent")){
                            binding.btnAddFriend.setText("Cancel Friend Request");
                            binding.btnAddFriend.setBackgroundColor(Color.RED);
                            friend=true;
                        }

                        if (users.getRequest().equals("Req_Pending")){
                            binding.btnAddFriend.setVisibility(View.GONE);
                            binding.btnAccept.setVisibility(View.VISIBLE);
                            binding.btnReject.setVisibility(View.VISIBLE);
                        }
                        }
                    }
                }
                binding.btnAddFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (friend){
                            database.getReference().child("Users").child(user.getUid()).child("Friends").child(Receiverid).removeValue();
                            database.getReference().child("Users").child(Receiverid).child("Friends").child(user.getUid()).removeValue();
                            binding.btnAddFriend.setText("Add friend");
                            binding.btnAddFriend.setBackgroundColor(0x09af00);
                        }else{
                            Users user1 = new Users();
                            user1.setMail(email);
                            user1.setUserName(userName);
                            user1.setUserId(Receiverid);
                            user1.setProfilePic(profilePic);
                            user1.setRequest("Req_Sent");

                            Users user2 = new Users();
                            user2.setMail(user.getEmail());
                            user2.setUserName(sendername);
                            user2.setUserId(user.getUid());
                            user2.setProfilePic(pp);
                            user2.setRequest("Req_Pending");

                            database.getReference().child("Users").child(user.getUid()).child("Friends").child(Receiverid).setValue(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("Users").child(Receiverid).child("Friends").child(user.getUid()).setValue(user2);
                                }
                            });

                            binding.btnAddFriend.setText("Unfriend");
                            binding.btnAddFriend.setBackgroundColor(Color.RED);
                        }

                    }
                });

                binding.btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Users user1 = new Users();
                        user1.setMail(email);
                        user1.setUserName(userName);
                        user1.setUserId(Receiverid);
                        user1.setRequest("Accepted");

                        Users user2 = new Users();
                        user2.setMail(user.getEmail());
                        user2.setUserName(sendername);
                        user2.setUserId(user.getUid());
                        user2.setRequest("Accepted");

                        database.getReference().child("Users").child(user.getUid()).child("Friends").child(Receiverid).setValue(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("Users").child(Receiverid).child("Friends").child(user.getUid()).setValue(user2);
                            }
                        });
                        binding.btnAccept.setVisibility(View.GONE);
                        binding.btnReject.setVisibility(View.GONE);
                        binding.btnAddFriend.setVisibility(View.VISIBLE);
                    }
                });

                binding.btnReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        database.getReference().child("Users").child(user.getUid()).child("Friends").child(Receiverid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("Users").child(Receiverid).child("Friends").child(user.getUid()).removeValue();
                            }
                        });
                        binding.btnAccept.setVisibility(View.GONE);
                        binding.btnReject.setVisibility(View.GONE);
                        binding.btnAddFriend.setVisibility(View.VISIBLE);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

}