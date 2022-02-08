package com.sushant.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.databinding.ActivityInComingCallBinding;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.util.HashMap;

public class InComingCall extends AppCompatActivity {

    ActivityInComingCallBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String receiverName,receiverPP,senderId,key,type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityInComingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

        receiverName=getIntent().getStringExtra("UserName");
        receiverPP=getIntent().getStringExtra("ProfilePic");
        senderId=getIntent().getStringExtra("UserId");
        key=auth.getUid()+senderId;
        type=getIntent().getStringExtra("type");

        binding.txtSenderName.setText(receiverName);
        Glide.with(this).load(receiverPP).placeholder(R.drawable.placeholder).into(binding.imgSender);

        binding.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String response="accept";
                 sendResponse(response);
                 joinMeeting(key);
                finish();
            }
        });

        binding.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String response="reject";
                sendResponse(response);
                Intent intent= new Intent(InComingCall.this,ChatDetailsActivity.class);
                intent.putExtra("UserId", senderId);
                intent.putExtra("ProfilePic", receiverPP);
                intent.putExtra("UserName",receiverName);
                startActivity(intent);
            }
        });

        checkResponse();

    }

    private void checkResponse() {
        database.getReference().child("Users").child(senderId).child("VideoCall").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String response=snapshot.child("response").getValue(String.class);
                    if ("reject".equals(response)){
                        Intent intent= new Intent(InComingCall.this,ChatDetailsActivity.class);
                        intent.putExtra("UserId", senderId);
                        intent.putExtra("ProfilePic", receiverPP);
                        intent.putExtra("UserName",receiverName);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendResponse(String response) {
        if (response.equals("accept")){
            HashMap<String,Object> map= new HashMap<>();
            map.put("onCall","true");
            map.put("response","accept");
            map.put("key",key);
            database.getReference().child("Users").child(auth.getUid()).child("VideoCall").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    database.getReference().child("Users").child(senderId).child("VideoCall").updateChildren(map);
                }
            });

            Handler handler= new Handler();
            Runnable runnable= new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("Users").child(auth.getUid()).child("VideoCall").removeValue();
                    database.getReference().child("Users").child(senderId).child("VideoCall").removeValue();
                }
            };
            handler.postDelayed(runnable,2000);

        }else if (response.equals("reject")){
            HashMap<String,Object> map= new HashMap<>();
            map.put("onCall","false");
            map.put("response","reject");
            map.put("key",key);
            database.getReference().child("Users").child(auth.getUid()).child("VideoCall").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    database.getReference().child("Users").child(senderId).child("VideoCall").updateChildren(map);
                }
            });
            Handler handler= new Handler();
            Runnable runnable= new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("Users").child(auth.getUid()).child("VideoCall").removeValue();
                    database.getReference().child("Users").child(senderId).child("VideoCall").removeValue();
                }
            };
            handler.postDelayed(runnable,2000);
        }
    }
    private void joinMeeting(String key) {
        try {
            JitsiMeetConferenceOptions options= new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(key)
                    .setAudioMuted(true)
                    .setVideoMuted(true)
                    .setAudioOnly(false)
                    .setWelcomePageEnabled(false)
                    .setConfigOverride("requireDisplayName", true)
                    .build();
            JitsiMeetActivity.launch(InComingCall.this,options);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

}