package com.sushant.whatsapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.databinding.ActivityOutGoingCallBinding;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.URL;

public class OutGoingCall extends AppCompatActivity {

    ActivityOutGoingCallBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String receiverId,receiverName,receiverPP,key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityOutGoingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();

         receiverName=getIntent().getStringExtra("UserName");
         receiverPP=getIntent().getStringExtra("ProfilePic");
        receiverId =getIntent().getStringExtra("UserId");
         key=getIntent().getStringExtra("key");

//        binding.txtSenderName.setText(receiverName);
//        Glide.with(this).load(receiverPP).placeholder(R.drawable.placeholder).into(binding.imgSender);

        joinMeeting(key);
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
            JitsiMeetActivity.launch(OutGoingCall.this,options);
            finish();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }
}