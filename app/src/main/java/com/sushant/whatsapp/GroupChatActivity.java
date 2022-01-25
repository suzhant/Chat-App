package com.sushant.whatsapp;

import static com.sushant.whatsapp.R.color.red;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.sushant.whatsapp.Adapters.GroupChatAdapter;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityGroupChatBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 26;
    ActivityGroupChatBinding binding;
    Animation scale_up, scale_down;
    FirebaseAuth auth;
    String senderId,profilePic,sendername,Gid,GPP,Gname,CreatedOn,CreatedBy,seen="true";
    boolean notify = false;
    FirebaseDatabase database;
    Handler handler;
    Runnable runnable;
    ArrayList<String> list= new ArrayList<>();
    FirebaseStorage storage;
    ProgressDialog dialog;
    String Notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();

        dialog= new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);


        Notification=getIntent().getStringExtra("Notification");
        Gid = getIntent().getStringExtra("GId");
        GPP = getIntent().getStringExtra("GPic");
        Gname = getIntent().getStringExtra("GName");
        CreatedOn=getIntent().getStringExtra("CreatedOn");
        CreatedBy=getIntent().getStringExtra("CreatedBy");


        senderId = FirebaseAuth.getInstance().getUid();
        updateSeen(seen,senderId);

        binding.groupName.setText(Gname);
        Glide.with(this).load(GPP).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.profileImage);

        scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        scale_up = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        auth = FirebaseAuth.getInstance();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ArrayList<Messages> messageModel = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        layoutManager.setStackFromEnd(true);
        binding.chatRecyclerView.setHasFixedSize(true);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        final GroupChatAdapter chatAdapter = new GroupChatAdapter(messageModel, this,Gid);
        binding.chatRecyclerView.setAdapter(chatAdapter);

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
                seen="true";
                updateSeen(seen,senderId);
                if ("true".equals(Notification)){
                    Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }else {
                    finish();
                }
            }
        });

        senderId = FirebaseAuth.getInstance().getUid();

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
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = messageModel.size();
                        messageModel.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Messages model = dataSnapshot.getValue(Messages.class);
                            assert model != null;
                            model.setMessageId(dataSnapshot.getKey());
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

        binding.icSend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        binding.imgGallery.setEnabled(false);
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

    private  void  updateSeen(String seen,String id){
        HashMap<String,Object> map= new HashMap<>();
        map.put("seen", seen);
        database.getReference().child("Groups").child(id).child(Gid).updateChildren(map);
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
                sendNotification("/topics/"+Gid, Gname, sendername + ": " + message, GPP,Gid,"text");
            }

            notify = false;

            database.getReference().child("Group Chat").child(Gid).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    String path = "android.resource://" + getPackageName() + "/" + R.raw.google_notification;
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
                    r.play();
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
            model1.setSenderName(sendername);
            Date date = new Date();
            model1.setTimestamp(date.getTime());
            updateLastMessage(heart);

            if (notify) {
                sendNotification( "/topics/"+Gid,Gname,sendername+": "+heart,GPP,Gid,"text");
            }
            notify = false;

            database.getReference().child("Group Chat").child(Gid).push().setValue(model1).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    String path = "android.resource://" + getPackageName() + "/" + R.raw.google_notification;
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
                    r.play();
                }
            });
        }
        seen="false";
        database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).child(Gid).child("participant").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    Users users=snapshot1.getValue(Users.class);
                    if (users.getUserId()!=null){
                        if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                            updateSeen(seen,users.getUserId());
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateLastMessage(String message){
        HashMap<String,Object> map= new HashMap<>();
        map.put("lastMessage",message);
        map.put("senderName",sendername);
        map.put("senderId",senderId);
        database.getReference().child("Group Chat").child("Last Messages").child(Gid).updateChildren(map);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void sendNotification( String topic,String GName, String msg,String GPic,String Gid,String msgType ) {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(topic, GName, msg,GPic,Gid,msgType,"Group","true",".GroupChatActivity",getApplicationContext(), GroupChatActivity.this);
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
            if (resultCode== Activity.RESULT_OK){
                if (data!=null) {
                    if (data.getData() != null) {
                        Uri selectedImage = data.getData();
                        File dir = getCacheDir();
                        String file = SiliCompressor.with(this).compress(String.valueOf(selectedImage), dir);
                        Uri uri = Uri.parse(file);
                        uploadToFirebase(uri);
                    }
                }
            }
        }
    }


    private void uploadToFirebase(Uri uri){
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Group Chat Pics").child(FirebaseAuth.getInstance().getUid()).child(calendar.getTimeInMillis() + "");
        dialog.show();
        reference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                File fdelete = new File(Objects.requireNonNull(getFilePath(uri)));

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
                            Date date = new Date();
                            final Messages model = new Messages(senderId, profilePic, date.getTime());
                            model.setMessage(fdelete.getName());
                            model.setImageUrl(filePath);
                            model.setType("photo");
                            binding.editMessage.getText().clear();
                            updateLastMessage(fdelete.getName());

                            if (notify) {
                                for (int i=0;i<list.size();i++){
                                    sendNotification("/topics/"+Gid, Gname, sendername+": "+fdelete.getName(),GPP,Gid,"photo");
                                }
                            }
                            notify = false;

                            database.getReference().child("Group Chat").child(Gid).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    String path = "android.resource://" + getPackageName() + "/" + R.raw.google_notification;
                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
                                    r.play();
                                }
                            });
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
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

}