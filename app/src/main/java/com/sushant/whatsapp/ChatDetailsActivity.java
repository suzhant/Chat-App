package com.sushant.whatsapp;

import static com.sushant.whatsapp.R.color.red;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.sushant.whatsapp.Adapters.ChatAdapter;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityChatDetailsBinding;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailsActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "notification";
    ActivityChatDetailsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    Animation scale_up, scale_down;
    ImageView blackCircle;
    String userToken;
    Handler handler;
    Runnable runnable;
    String sendername;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();


        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        scale_up = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        blackCircle = findViewById(R.id.black_circle);

        final String senderId = auth.getUid();
        String receiverId = getIntent().getStringExtra("UserId");
        String userName = getIntent().getStringExtra("UserName");
        String profilePic = getIntent().getStringExtra("ProfilePic");
        String email = getIntent().getStringExtra("userEmail");
        String Status = getIntent().getStringExtra("Status");

        binding.userName.setText(userName);
//        binding.txtStatus.setText(Status);
//
//        if (Status.equals("online")) {
////            binding.txtStatus.setTextColor(Color.GREEN);
//            binding.imgStatus.setColorFilter(Color.GREEN);
//        } else {
////            binding.txtStatus.setTextColor(Color.WHITE);
//            binding.imgStatus.setColorFilter(Color.GRAY);
//        }

        binding.icSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("UserIdPA", receiverId);
                intent.putExtra("UserNamePA", userName);
                intent.putExtra("ProfilePicPA", profilePic);
                intent.putExtra("EmailPA", email);
                startActivity(intent);
            }
        });


        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();//Method finish() will destroy your activity and show the one that started it.
            }
        });

        final ArrayList<Messages> messageModel = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModel, this, receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        binding.chatRecyclerView.setLayoutManager(layoutManager);


        final String senderRoom = senderId + receiverId;
        final String receiverRoom = receiverId + senderId;

        database.getReference().child("Chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = messageModel.size();
                        messageModel.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Messages model = dataSnapshot.getValue(Messages.class);
                            model.setMessageId(dataSnapshot.getKey());
                            model.setProfilePic(profilePic);
                            messageModel.add(model);

                        }
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


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        Query checkStatus = reference.orderByChild("userId").equalTo(receiverId);
        checkStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String StatusFromDB = snapshot.child(receiverId).child("Connection").child("Status").getValue(String.class);
                    assert StatusFromDB != null;
                    binding.txtStatus.setText(StatusFromDB);
                    if (StatusFromDB.equals("online") || StatusFromDB.equals("Typing...")) {
                        binding.imgStatus.setColorFilter(Color.GREEN);
                    } else {
                        binding.imgStatus.setColorFilter(Color.GRAY);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        binding.editMessage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                binding.imgCamera.setVisibility(View.GONE);
//                binding.imgGallery.setVisibility(View.GONE);
//                binding.imgMic.setVisibility(View.GONE);
//            }
//        });
//
//        binding.editMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (binding.editMessage.getText().length()>0){
//                    binding.icFavorite.setVisibility(View.GONE);
//                    binding.icSend.setVisibility(View.VISIBLE);
//                }else{
//                    binding.icFavorite.setVisibility(View.VISIBLE);
//                    binding.icSend.setVisibility(View.GONE);
//                }
//            }
//        });

        binding.editMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length()>0) {
                    binding.imgCamera.setVisibility(View.GONE);
                    binding.imgGallery.setVisibility(View.GONE);
                    binding.imgMic.setVisibility(View.GONE);
                    binding.icSend.setImageResource(R.drawable.ic_send);
                    binding.icSend.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("Connection").child("Status").setValue("Typing...");
                } else {
                    binding.imgCamera.setVisibility(View.VISIBLE);
                    binding.imgGallery.setVisibility(View.VISIBLE);
                    binding.imgMic.setVisibility(View.VISIBLE);
                    binding.icSend.setImageResource(R.drawable.ic_favorite);
                    binding.icSend.setColorFilter(getResources().getColor(red));
                    database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("Connection").child("Status").setValue("online");
                }
            }
        });

        database.getReference().child("Users").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                sendername = users.getUserName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        binding.icSend.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.P)
            public void onClick(View view) {
                notify = true;
                binding.icSend.startAnimation(scale_down);
                binding.icSend.startAnimation(scale_up);
                final String message = binding.editMessage.getText().toString();
                if (!message.isEmpty()) {
                    final Messages model = new Messages(senderId, message, profilePic);
                    Date date = new Date();
                    model.setTimestamp(date.getTime());
                    binding.editMessage.getText().clear();

                    if (notify) {
                        sendNotification(receiverId, sendername, message);
                    }
                    notify = false;

                    database.getReference().child("Chats").child(senderRoom).push().setValue(model)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("Chats").child(receiverRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                        }
                                    });
                                }
                            });

                } else {
                    notify = true;
                    binding.icSend.startAnimation(scale_down);
                    binding.icSend.startAnimation(scale_up);
                    int unicode = 0x2764;
                    String heart=new String(Character.toChars(unicode));
                    final Messages model1 = new Messages(senderId,heart, profilePic);
                    Date date = new Date();
                    model1.setTimestamp(date.getTime());

                    if (notify) {
                        sendNotification(receiverId, sendername, heart);
                    }
                    notify = false;

                    database.getReference().child("Chats").child(senderRoom).push().setValue(model1)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("Chats").child(receiverRoom).push().setValue(model1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                        }
                                    });
                                }
                            });
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
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, userName, msg, getApplicationContext(), ChatDetailsActivity.this);
                fcmNotificationsSender.SendNotifications();
            }
        };
        if (notify) {
            handler.postDelayed(runnable, 2000);
        }
    }

//        void refresh(String text){
//        Handler refresh= new Handler();
//        Runnable runnable= new Runnable() {
//            @Override
//            public void run() {
//                binding.txtStatus.setText(text);
//            }
//        };
//            refresh.postDelayed(runnable,1000);
//        }

}