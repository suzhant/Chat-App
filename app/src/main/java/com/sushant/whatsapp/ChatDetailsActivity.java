package com.sushant.whatsapp;

import static com.sushant.whatsapp.R.color.red;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.sushant.whatsapp.Adapters.ChatAdapter;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityChatDetailsBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ChatDetailsActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
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
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderId,receiverId,senderRoom,receiverRoom,profilePic;
    ValueEventListener eventListener1,eventListener2;
    Query checkStatus,checkStatus1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        dialog= new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);


        database = FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        scale_up = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        blackCircle = findViewById(R.id.black_circle);


        senderId = auth.getUid();
        receiverId = getIntent().getStringExtra("UserId");
        String userName = getIntent().getStringExtra("UserName");
        profilePic = getIntent().getStringExtra("ProfilePic");
        String email = getIntent().getStringExtra("userEmail");
        String Status = getIntent().getStringExtra("UserStatus");

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
                intent.putExtra("StatusPA",Status);
                startActivity(intent);
            }
        });


        Glide.with(this).load(profilePic).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.profileImage);
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Object> map= new HashMap<>();
                map.put("Typing","Not Typing...");
                database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
                finish();//Method finish() will destroy your activity and show the one that started it.
            }
        });

        final ArrayList<Messages> messageModel = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messageModel, this, receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        binding.chatRecyclerView.setLayoutManager(layoutManager);



        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

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
                        binding.chatRecyclerView.getRecycledViewPool().clear();
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


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        checkStatus = reference.orderByChild("userId").equalTo(receiverId);
        eventListener1= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String StatusFromDB = snapshot.child(receiverId).child("Connection").child("Status").getValue(String.class);
                    assert StatusFromDB != null;

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
                    checkStatus1 = reference1.orderByChild("userId").equalTo(senderId);
                    eventListener2= new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String presence = snapshot.child(senderId).child("Friends").child(receiverId).child("Typing").getValue(String.class);
                                if (StatusFromDB.equals("online")) {
                                    if ("Typing...".equals(presence)) {
                                        binding.imgStatus.setColorFilter(Color.GREEN);
                                        binding.txtStatus.setText(presence);
                                    }else{
                                        binding.imgStatus.setColorFilter(Color.GREEN);
                                        binding.txtStatus.setText(StatusFromDB);
                                    }
                                }else {
                                    binding.imgStatus.setColorFilter(Color.GRAY);
                                    binding.txtStatus.setText(StatusFromDB);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    };
                    checkStatus1.addValueEventListener(eventListener2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        checkStatus.addValueEventListener(eventListener1);




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
                    HashMap<String,Object> map= new HashMap<>();
                    map.put("Typing","Typing...");
                    database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
                } else {
                    binding.imgCamera.setVisibility(View.VISIBLE);
                    binding.imgGallery.setVisibility(View.VISIBLE);
                    binding.imgMic.setVisibility(View.VISIBLE);
                    binding.icSend.setImageResource(R.drawable.ic_favorite);
                    binding.icSend.setColorFilter(getResources().getColor(red));
                    HashMap<String,Object> map= new HashMap<>();
                    map.put("Typing","Not Typing...");
                    database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
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
                    model.setType("text");
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
                    model1.setType("text");
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

        binding.imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==PICK_IMAGE){
            if (data!=null) {
                if (data.getData() != null) {
                    Uri selectedImage = data.getData();
                    File dir = getCacheDir();
                    String file = SiliCompressor.with(this).compress(String.valueOf(selectedImage), dir);
                    Calendar calendar = Calendar.getInstance();
                    Uri uri = Uri.parse(file);
                    final StorageReference reference = storage.getReference().child("Chats Images").child(FirebaseAuth.getInstance().getUid()).child(calendar.getTimeInMillis() + "");
                    dialog.show();
                    reference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            File fdelete = new File(getFilePath(uri));

                            if (fdelete.exists()) {
                                if (fdelete.delete()) {
                                    System.out.println("file Deleted :");
                                } else {
                                    System.out.println("file not Deleted :");
                                }
                            }
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @RequiresApi(api = Build.VERSION_CODES.P)
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        notify = true;
                                        final Messages model = new Messages(senderId, filePath, profilePic);
                                        Date date = new Date();
                                        model.setTimestamp(date.getTime());
                                        model.setType("photo");
                                        binding.editMessage.getText().clear();

                                        if (notify) {
                                            sendNotification(receiverId, sendername, filePath);
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

                                    }
                                });
                            }
                        }
                    });

                }
            }
        }
    }
    private String getFilePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String picturePath = cursor.getString(columnIndex); // returns null
            cursor.close();
            return picturePath;
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        checkStatus.removeEventListener(eventListener1);
        checkStatus1.removeEventListener(eventListener2);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        checkStatus.removeEventListener(eventListener1);
        checkStatus1.removeEventListener(eventListener2);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        checkStatus.addValueEventListener(eventListener1);
        checkStatus1.addValueEventListener(eventListener2);
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        HashMap<String,Object> map= new HashMap<>();
        map.put("Typing","Not Typing...");
        database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
        super.onBackPressed();
    }
}