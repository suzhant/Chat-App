package com.sushant.whatsapp;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.Utils.Encryption;
import com.sushant.whatsapp.databinding.ActivityConnetingBinding;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ConnectingActivity extends AppCompatActivity {

    ActivityConnetingBinding binding;
    String profilePic, receiverName, receiverId, sendername, senderPP, email, lastOnline, StatusFromDB;
    FirebaseDatabase database;
    String userToken;
    Handler handler, videoHandler;
    Runnable runnable, videoRunnable;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    ValueEventListener eventListener;
    Ringtone r;
    String key, seen = "true", senderRoom, receiverRoom;
    CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnetingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        String path = "android.resource://" + getPackageName() + "/" + R.raw.call_sound;
        r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setLooping(true);
        }
        r.play();

        profilePic = getIntent().getStringExtra("ProfilePic");
        receiverName = getIntent().getStringExtra("UserName");
        receiverId = getIntent().getStringExtra("UserId");
        email = getIntent().getStringExtra("userEmail");
//        lastOnline=getIntent().getStringExtra("LastOnline");
//        StatusFromDB=getIntent().getStringExtra("Status");
        senderRoom = auth.getUid() + receiverId;
        receiverRoom = receiverId + auth.getUid();


        Glide.with(this).load(profilePic).placeholder(R.drawable.placeholder).into(binding.imgPP);
        binding.txtUserName.setText(receiverName);

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                sendername = users.getUserName();
                senderPP = users.getProfilePic();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        startCountDownTimer();
        sendCallInvitation();
        checkResponse();

        binding.btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConnectingActivity.this, ChatDetailsActivity.class);
                intent.putExtra("UserId", receiverId);
                intent.putExtra("ProfilePic", profilePic);
                intent.putExtra("UserName", receiverName);
                intent.putExtra("userEmail", email);
                intent.putExtra("Status", StatusFromDB);
                intent.putExtra("UserStatus", lastOnline);
                startActivity(intent);
                r.stop();
                sendRejectNotification();
                sendcancelNotification();
                updateDb();
                sendMessage();
                finish();
            }
        });

    }

    private void sendcancelNotification() {
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
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, sendername, "You missed a call", senderPP, receiverId, email, auth.getUid(), "cancel",
                        "videoCall", ".ChatDetailsActivity", getApplicationContext(), ConnectingActivity.this);
                fcmNotificationsSender.SendNotifications();
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    private void checkResponse() {
        databaseReference = database.getReference().child("Users").child(receiverId).child("VideoCall");
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String response = snapshot.child("response").getValue(String.class);
                    key = snapshot.child("key").getValue(String.class);
                    if ("accept".equals(response)) {
                        Toast.makeText(ConnectingActivity.this, "Call Accepted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ConnectingActivity.this, OutGoingCall.class);
                        intent.putExtra("key", key);
                        intent.putExtra("UserName", receiverName);
                        intent.putExtra("ProfilePic", profilePic);
                        intent.putExtra("UserId", receiverId);
                        startActivity(intent);
                        r.stop();
                        finish();
                    } else if ("reject".equals(response)) {
                        Toast.makeText(ConnectingActivity.this, "Call Rejected", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ConnectingActivity.this, ChatDetailsActivity.class);
                        intent.putExtra("UserId", receiverId);
                        intent.putExtra("ProfilePic", profilePic);
                        intent.putExtra("UserName", receiverName);
                        intent.putExtra("userEmail", email);
//                                intent.putExtra("Status",StatusFromDB );
//                                intent.putExtra("UserStatus",lastOnline);
                        startActivity(intent);
                        finish();
                        r.stop();
                    }
                } else {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("onCall", "false");
                    map.put("response", "idle");
                    map.put("key", auth.getUid() + receiverId);
                    database.getReference().child("Users").child(receiverId).child("VideoCall").updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(eventListener);
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
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, sendername, "Incoming Video Call", senderPP, receiverId, key, auth.getUid(), "video",
                        "videoCall", ".InComingCall", getApplicationContext(), ConnectingActivity.this);
                fcmNotificationsSender.SendNotifications();
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    private void sendRejectNotification() {
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
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, sendername, "You missed a call", senderPP, receiverId, email, auth.getUid(), "text",
                        "Chat", ".ChatDetailsActivity", getApplicationContext(), ConnectingActivity.this);
                fcmNotificationsSender.SendNotifications();
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    private void updateDb() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("onCall", "false");
        map.put("response", "reject");
        map.put("key", key);
        database.getReference().child("Users").child(Objects.requireNonNull(receiverId)).child("VideoCall").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(receiverId).child("VideoCall").updateChildren(map);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("VideoCall").removeValue();
                database.getReference().child("Users").child(receiverId).child("VideoCall").removeValue();
//                videoHandler = new Handler();
//                videoRunnable = new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                };
//                videoHandler.postDelayed(videoRunnable, 1000);
            }
        });
    }

    private void startCountDownTimer() {
        timer = new CountDownTimer(62000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                updateDb();
                sendRejectNotification();
                sendMessage();
            }
        };
        timer.start();
    }

    private void sendMessage() {
        String message = "You missed a Video Call.";
        String encryptMessage = Encryption.encryptMessage(message);
        String key = database.getReference().push().getKey();
        final Messages model = new Messages(auth.getUid(), encryptMessage, profilePic);
        Date date = new Date();
        model.setTimestamp(date.getTime());
        model.setType("videoCall");
        model.setSenderName(sendername);
        model.setMessageId(key);


        assert key != null;
        database.getReference().child("Chats").child(senderRoom).child(key).setValue(model)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Chats").child(receiverRoom).child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                String path = "android.resource://" + getPackageName() + "/" + R.raw.google_notification;
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
                                r.play();
                            }
                        });
                    }
                });

        seen = "false";
        updateSeen(seen, receiverId, auth.getUid());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            if (videoHandler.hasCallbacks(videoRunnable)) {
//                videoHandler.removeCallbacks(videoRunnable);
//            }
//        }
        if (databaseReference != null) {
            databaseReference.removeEventListener(eventListener);
        }
        timer.cancel();
    }


    private void updateSeen(String seen, String ID1, String ID2) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("seen", seen);
        database.getReference().child("Users").child(ID1).child("Friends").child(ID2).updateChildren(map);
    }
}