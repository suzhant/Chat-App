package com.sushant.whatsapp;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.databinding.ActivityOutGoingCallBinding;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;
import java.util.HashMap;

public class OutGoingCall extends JitsiMeetActivity {

    ActivityOutGoingCallBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String receiverId, receiverName, receiverPP, key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutGoingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        receiverName = getIntent().getStringExtra("UserName");
        receiverPP = getIntent().getStringExtra("ProfilePic");
        receiverId = getIntent().getStringExtra("UserId");
        key = getIntent().getStringExtra("key");

//        binding.txtSenderName.setText(receiverName);
//        Glide.with(this).load(receiverPP).placeholder(R.drawable.placeholder).into(binding.imgSender);

        joinMeeting(key);
        sendResponse();
    }

    private void joinMeeting(String key) {
        try {
            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(new URL("https://meet.jit.si"))
                    .setRoom(key)
                    .setAudioMuted(true)
                    .setVideoMuted(true)
                    .setAudioOnly(false)
                    .setWelcomePageEnabled(false)
                    .setConfigOverride("requireDisplayName", true)
                    .build();
            JitsiMeetActivity.launch(OutGoingCall.this, options);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void sendResponse() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("onCall", "true");
        map.put("response", "accept");
        map.put("key", key);
        database.getReference().child("Users").child(auth.getUid()).child("VideoCall").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(receiverId).child("VideoCall").updateChildren(map);
            }
        });

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                database.getReference().child("Users").child(auth.getUid()).child("VideoCall").removeValue();
                database.getReference().child("Users").child(receiverId).child("VideoCall").removeValue();
            }
        };
        handler.postDelayed(runnable, 2000);
    }

}