package com.sushant.whatsapp;

import static com.sushant.whatsapp.R.color.red;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.GroupChatAdapter;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityGroupChatBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    Animation scale_up, scale_down;
    FirebaseAuth auth;
    String senderId,profilePic,sendername,Gid,GPP,Gname,CreatedOn,CreatedBy;
    boolean notify = false;
    FirebaseDatabase database;
    String userToken;
    Handler handler;
    Runnable runnable;
    ArrayList<String> list= new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();


        Gid = getIntent().getStringExtra("GId");
        GPP = getIntent().getStringExtra("GPic");
        Gname = getIntent().getStringExtra("GName");
        CreatedOn=getIntent().getStringExtra("CreatedOn");
        CreatedBy=getIntent().getStringExtra("CreatedBy");

        senderId = FirebaseAuth.getInstance().getUid();

        binding.groupName.setText(Gname);
        Glide.with(this).load(GPP).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.profileImage);

        scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        scale_up = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        auth = FirebaseAuth.getInstance();

        binding.icSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(GroupChatActivity.this,GroupSettings.class);
                intent.putExtra("GId1",Gid);
                intent.putExtra("GPic1",GPP);
                intent.putExtra("GName1",Gname);
                intent.putExtra("CreatedOn1",CreatedOn);
                intent.putExtra("CreatedBy1",CreatedBy);
                startActivity(intent);
            }
        });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ArrayList<Messages> messageModel = new ArrayList<>();

        senderId = FirebaseAuth.getInstance().getUid();

        final GroupChatAdapter chatAdapter = new GroupChatAdapter(messageModel, this);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        layoutManager.setStackFromEnd(true);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        binding.editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    binding.imgCamera.setVisibility(View.GONE);
                    binding.imgGallery.setVisibility(View.GONE);
                    binding.imgMic.setVisibility(View.GONE);
                    binding.icSend.setImageResource(R.drawable.ic_send);
                    binding.icSend.setColorFilter(getResources().getColor(R.color.white));
                } else {
                    binding.imgCamera.setVisibility(View.VISIBLE);
                    binding.imgGallery.setVisibility(View.VISIBLE);
                    binding.imgMic.setVisibility(View.VISIBLE);
                    binding.icSend.setImageResource(R.drawable.ic_favorite);
                    binding.icSend.setColorFilter(getResources().getColor(red));

                }
            }
        });

        database.getReference().child("Group Chat").child(Gid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = messageModel.size();
                        messageModel.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Messages model = dataSnapshot.getValue(Messages.class);
                            messageModel.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();
//                        Collections.sort(messageModel, (obj1, obj2) -> obj1.getTimestamp().compareTo(obj2.getTimestamp()));
                        if (count == 0) {
                            chatAdapter.notifyDataSetChanged();
                        } else {
                            chatAdapter.notifyItemRangeChanged(messageModel.size(), messageModel.size());
                            binding.chatRecyclerView.smoothScrollToPosition(messageModel.size() - 1);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                sendername = users.getUserName();
                profilePic=users.getProfilePic();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).child(Gid).child("participant").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Users user = snapshot1.getValue(Users.class);
                    assert user != null;
                    if (!FirebaseAuth.getInstance().getUid().equals(user.getUserId())){
                        list.add(user.getUserId());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.icSend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void sendMessage() {
        notify = true;
        binding.icSend.startAnimation(scale_down);
        binding.icSend.startAnimation(scale_up);
        final String message = binding.editMessage.getText().toString();
        if (!message.isEmpty()) {
            final Messages model = new Messages(senderId, message, profilePic);
            Date date = new Date();
            model.setTimestamp(date.getTime());
            model.setType("text");
            model.setSenderName(sendername);
            updateLastMessage(message);
            binding.editMessage.getText().clear();

            if (notify) {
                for (int i=0;i<list.size();i++){
                    sendNotification(list.get(i), sendername, message);
                }
            }

            notify = false;

            database.getReference().child("Group Chat").child(Gid).push().setValue(model);

        } else {
            notify = true;
            binding.icSend.startAnimation(scale_down);
            binding.icSend.startAnimation(scale_up);
            int unicode = 0x2764;
            String heart=new String(Character.toChars(unicode));
            final Messages model1 = new Messages(senderId,heart, profilePic);
            model1.setType("text");
            model1.setSenderName(sendername);
            Date date = new Date();
            model1.setTimestamp(date.getTime());
            updateLastMessage(heart);

            if (notify) {
                for (int i=0;i<list.size();i++){
                    sendNotification(list.get(i), sendername, heart);
                }
            }
            notify = false;

            database.getReference().child("Group Chat").child(Gid).push().setValue(model1);
        }
    }

    private void updateLastMessage(String message){
        HashMap<String,Object> map= new HashMap<>();
        map.put("lastMessage",message);
        map.put("senderName",sendername);
        map.put("senderId",senderId);
        database.getReference().child("Group Chat").child("Last Messages").child(Gid).updateChildren(map);
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
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, userName, msg, getApplicationContext(), GroupChatActivity.this);
                fcmNotificationsSender.SendNotifications();
            }
        };
        if (notify) {
            handler.postDelayed(runnable, 2000);
        }
    }
}