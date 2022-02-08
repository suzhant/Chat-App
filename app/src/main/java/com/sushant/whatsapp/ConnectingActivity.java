package com.sushant.whatsapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityConnetingBinding;

import java.util.HashMap;
import java.util.Objects;

public class ConnectingActivity extends AppCompatActivity {

    ActivityConnetingBinding binding;
    String profilePic,receiverName,receiverId,sendername,senderPP,email,lastOnline,StatusFromDB;
    FirebaseDatabase database;
    String userToken;
    Handler handler;
    Runnable runnable;
    FirebaseAuth auth;
    ValueEventListener eventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityConnetingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        profilePic=getIntent().getStringExtra("ProfilePic");
        receiverName=getIntent().getStringExtra("UserName");
        receiverId=getIntent().getStringExtra("UserId");
        email=getIntent().getStringExtra("userEmail");
//        lastOnline=getIntent().getStringExtra("LastOnline");
//        StatusFromDB=getIntent().getStringExtra("Status");


        Glide.with(this).load(profilePic).placeholder(R.drawable.placeholder).into(binding.imgPP);
        binding.txtUserName.setText(receiverName);

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                sendername = users.getUserName();
                senderPP=users.getProfilePic();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendCallInvitation();
        checkResponse();

        binding.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(ConnectingActivity.this,ChatDetailsActivity.class);
                intent.putExtra("UserId", receiverId);
                intent.putExtra("ProfilePic", profilePic);
                intent.putExtra("UserName",receiverName);
                intent.putExtra("userEmail",email);
                intent.putExtra("Status",StatusFromDB );
                intent.putExtra("UserStatus",lastOnline);
                startActivity(intent);
                finish();
            }
        });
    }

    private void checkResponse() {

       eventListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                        String response=snapshot.child("response").getValue(String.class);
                        String key=snapshot.child("key").getValue(String.class);
                            if ("accept".equals(response)){
                                Toast.makeText(ConnectingActivity.this, "Call Accepted", Toast.LENGTH_SHORT).show();
                                Intent intent= new Intent(ConnectingActivity.this,OutGoingCall.class);
                                intent.putExtra("key",key);
                                intent.putExtra("UserName",receiverName);
                                intent.putExtra("ProfilePic",profilePic);
                                intent.putExtra("UserId",receiverId);
                                startActivity(intent);
                                finish();
                            }else if ("reject".equals(response)){
                                Toast.makeText(ConnectingActivity.this, "Call Rejected", Toast.LENGTH_SHORT).show();
                                Intent intent= new Intent(ConnectingActivity.this,ChatDetailsActivity.class);
                                intent.putExtra("UserId", receiverId);
                                intent.putExtra("ProfilePic", profilePic);
                                intent.putExtra("UserName",receiverName);
                                intent.putExtra("userEmail",email);
//                                intent.putExtra("Status",StatusFromDB );
//                                intent.putExtra("UserStatus",lastOnline);
                                startActivity(intent);
                            }
                }else {
                    HashMap<String,Object> map= new HashMap<>();
                    map.put("onCall","false");
                    map.put("response","idle");
                    map.put("key",auth.getUid()+receiverId);
                    database.getReference().child("Users").child(receiverId).child("VideoCall").updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        database.getReference().child("Users").child(receiverId).child("VideoCall").addValueEventListener(eventListener);
    }


    private void sendCallInvitation() {
        database.getReference().child("Users").child(receiverId).child("Token").addListenerForSingleValueEvent(new ValueEventListener() {
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
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, sendername, "Incoming Video Call",senderPP,receiverId,email,auth.getUid(),"video",
                        "videoCall",".InComingCall",getApplicationContext(), ConnectingActivity.this);
                fcmNotificationsSender.SendNotifications();
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.getReference().child("Users").child(receiverId).child("VideoCall").removeEventListener(eventListener);
    }
}