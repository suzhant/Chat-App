package com.sushant.whatsapp;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

import java.util.HashMap;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    FirebaseDatabase database;
    boolean friend=false;
    String sendername,pp,userStatus;
    boolean notify = false;
    String userToken;
    Handler handler;
    Runnable runnable;
    ValueEventListener eventListener;
    DatabaseReference reference;

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
        String status=getIntent().getStringExtra("StatusPA");

        Glide.with(this).load(profilePic).placeholder(R.drawable.avatar).into(binding.imgProfile);
        binding.txtEmail.setText(email);
        binding.txtUserName.setText(userName);
        binding.txtAbout.setText(status);

        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        database.getReference().child("Users").child(Objects.requireNonNull(user.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users users = snapshot.getValue(Users.class);
                        assert users != null;
                       sendername=users.getUserName();
                       pp=users.getProfilePic();
                       userStatus=users.getStatus();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        reference = database.getReference("Users").child(user.getUid()).child("Friends");
        eventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1:snapshot.getChildren()){
                        Users users=snapshot1.getValue(Users.class);
                        assert users != null;
                        if (Receiverid.equals(users.getUserId())){
                        if (users.getRequest().equals("Accepted")){
                            binding.btnAddFriend.setText("Unfriend");
                            binding.btnAddFriend.setBackgroundColor(Color.parseColor("#FF3D00"));
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
                    if (!snapshot.child(Receiverid).exists()){
                        binding.btnAddFriend.setVisibility(View.VISIBLE);
                        binding.btnAddFriend.setText("Add friend");
                        binding.btnAddFriend.setBackgroundColor(0x09af00);
                        binding.btnAccept.setVisibility(View.GONE);
                        binding.btnReject.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reference.addValueEventListener(eventListener);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
                friend=false;
            }
        });

        binding.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> obj1= new HashMap<>();
                obj1.put("request","Accepted");

                HashMap<String,Object> obj2= new HashMap<>();
                obj2.put("request","Accepted");

                database.getReference().child("Users").child(user.getUid()).child("Friends").child(Receiverid).updateChildren(obj1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Users").child(Receiverid).child("Friends").child(user.getUid()).updateChildren(obj2);
                    }
                });
                binding.btnAccept.setVisibility(View.GONE);
                binding.btnReject.setVisibility(View.GONE);
                binding.btnAddFriend.setVisibility(View.VISIBLE);
                friend=true;
            }
        });

        binding.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
                if (friend){
                    database.getReference().child("Users").child(user.getUid()).child("Friends").child(Receiverid).removeValue();
                    database.getReference().child("Users").child(Receiverid).child("Friends").child(user.getUid()).removeValue();
                    binding.btnAddFriend.setText("Add friend");
                    binding.btnAddFriend.setBackgroundColor(0x09af00);
                    friend=false;
                }else{
                    notify=true;
                    Users user1 = new Users();
                    user1.setMail(email);
                    user1.setUserName(userName);
                    user1.setUserId(Receiverid);
                    user1.setProfilePic(profilePic);
                    user1.setStatus(status);
                    user1.setTyping("Not Typing");
                    user1.setLastMessage("Say Hi!!");
                    user1.setRequest("Req_Sent");

                    Users user2 = new Users();
                    user2.setMail(user.getEmail());
                    user2.setUserName(sendername);
                    user2.setUserId(user.getUid());
                    user2.setProfilePic(pp);
                    user2.setStatus(userStatus);
                    user2.setTyping("Not Typing");
                    user2.setLastMessage("Say Hi!!");
                    user2.setRequest("Req_Pending");

                    if (notify) {
                        sendNotification(Receiverid, sendername, "sent you a Friend Request");
                    }
                    notify = false;


                    database.getReference().child("Users").child(user.getUid()).child("Friends").child(Receiverid).setValue(user1).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("Users").child(Receiverid).child("Friends").child(user.getUid()).setValue(user2).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(@NonNull Void unused) {
                                    Toast.makeText(getApplicationContext(), "Friend request sent to "+userName, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                    binding.btnAddFriend.setText("Unfriend");
                    binding.btnAddFriend.setBackgroundColor(Color.RED);
                    friend=true;
                }

            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void sendNotification(String receiver, String userName, String msg) {
        database.getReference().child("Users").child(receiver).child("Token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userToken = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, userName, msg, getApplicationContext(), ProfileActivity.this);
                fcmNotificationsSender.SendNotifications();
            }
        };
        if (notify) {
            handler.postDelayed(runnable, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reference!=null){
            Log.d("REF1", "onDestroy: called");
            reference.removeEventListener(eventListener);
        }

    }
}