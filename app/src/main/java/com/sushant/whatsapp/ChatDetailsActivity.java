package com.sushant.whatsapp;

import static com.sushant.whatsapp.R.color.red;
import static com.sushant.whatsapp.R.color.ucrop_color_widget_rotate_mid_line;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;
import com.sushant.whatsapp.Adapters.ChatAdapter;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityChatDetailsBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;



public class ChatDetailsActivity extends AppCompatActivity implements LifecycleObserver {

    public static final int PICK_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 200;
    private static final int CAMERA_PERMISSION_CODE = 300;
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
    String senderId,receiverId,senderRoom,receiverRoom,profilePic,senderPP,email,Status,receiverName;
    ValueEventListener eventListener1,eventListener2;
    Query checkStatus,checkStatus1;
    String currentPhotoPath,seen="true";
    DatabaseReference chat;
    ValueEventListener eventListener;
    DatabaseReference infoConnected;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    private MediaRecorder recorder = null;
    private static final String LOG_TAG = "AudioRecordTest";

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private final String [] permissions = {Manifest.permission.RECORD_AUDIO};

    boolean recording;
    CountDownTimer timer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/recorded_audio.3gp";

        dialog= new ProgressDialog(this);
        dialog.setCancelable(false);


        database = FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        scale_up = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        blackCircle = findViewById(R.id.black_circle);

        manageConnection();

        senderId = auth.getUid();
        receiverId = getIntent().getStringExtra("UserId");
        receiverName = getIntent().getStringExtra("UserName");
        profilePic = getIntent().getStringExtra("ProfilePic");
        email = getIntent().getStringExtra("userEmail");
        Status = getIntent().getStringExtra("UserStatus");

        binding.userName.setText(receiverName);
        Glide.with(this).load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);
        checkConn();

        final ArrayList<Messages> messageModel = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setStackFromEnd(true);
        binding.chatRecyclerView.setHasFixedSize(true);
        final ChatAdapter chatAdapter = new ChatAdapter(messageModel, this, receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        binding.icSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("UserIdPA", receiverId);
                intent.putExtra("UserNamePA", receiverName);
                intent.putExtra("ProfilePicPA", profilePic);
                intent.putExtra("EmailPA", email);
                intent.putExtra("StatusPA",Status);
                startActivity(intent);
            }
        });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seen="true";
                updateSeen(seen,senderId,receiverId);
                HashMap<String,Object> map= new HashMap<>();
                map.put("Typing","Not Typing...");
                database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
                if (binding.audioLayout.getVisibility()==View.VISIBLE){
                    binding.audioLayout.setVisibility(View.GONE);
                    binding.linear.setVisibility(View.VISIBLE);
                }
                Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();//Method finish() will destroy your activity and show the one that started it.
            }
        });

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        updateSeen(seen,senderId,receiverId);

        chat=database.getReference().child("Chats").child(senderRoom);
                chat.addValueEventListener(new ValueEventListener() {
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
                chat.keepSynced(true);


        getTypingStatus();

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
                    binding.icSend.setColorFilter(getResources().getColor(R.color.white));
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


        binding.icSend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        binding.imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(ChatDetailsActivity.this)
                        .galleryOnly()
                        .crop()
                        .start(PICK_IMAGE);
            }
        });

        binding.imgCamera.setEnabled(true);
        binding.imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(ChatDetailsActivity.this)
                        .cameraOnly()
                        .crop()
                        .start(REQUEST_IMAGE_CAPTURE);
            }
        });

        binding.imgMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(ChatDetailsActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
                binding.linear.setVisibility(View.GONE);
                binding.audioLayout.setVisibility(View.VISIBLE);
                recording=true;
                startRecording();
                startCountDownTimer();
            }
        });

        binding.stopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recording){
                    binding.stopRecording.setImageResource(R.drawable.ic_delete);
                    binding.txtRecording.setText("Recording stopped..");
                    binding.btnSend.setVisibility(View.VISIBLE);
                    recording=false;
                    timer.cancel();
                    stopRecording();
                }else {
                    binding.stopRecording.setImageResource(R.drawable.ic_stop_circle);
                    binding.txtRecording.setText("Recording..");
                    binding.btnSend.setVisibility(View.GONE);
                    binding.linear.setVisibility(View.VISIBLE);
                    binding.audioLayout.setVisibility(View.GONE);
                }
            }
        });

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording=false;
                binding.stopRecording.setImageResource(R.drawable.ic_stop_circle);
                binding.txtRecording.setText("Recording...");
                binding.audioLayout.setVisibility(View.GONE);
                binding.btnSend.setVisibility(View.GONE);
            uploadAudioToFirebase();
            }
        });

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    private void uploadAudioToFirebase() {
        dialog.setMessage("Uploading Audio...");
        dialog.show();
        Calendar calendar = Calendar.getInstance();
        StorageReference filepath=storage.getReference().child("Audio").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        Uri uri=Uri.fromFile(new File(fileName));
        filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    binding.linear.setVisibility(View.VISIBLE);
                    binding.audioLayout.setVisibility(View.GONE);
                    dialog.dismiss();
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();
                            notify = true;
                            Date date = new Date();
                            final Messages model = new Messages(senderId, profilePic, date.getTime());
                            model.setMessage("Recorded Audio");
                            model.setAudioFile(filePath);
                            model.setType("audio");
                            binding.editMessage.getText().clear();
                            updateLastMessage("Recorded Audio");

                            if (notify) {
                                sendNotification(receiverId, sendername,filePath,senderPP,email,senderId,"audio");
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

    private void startCountDownTimer() {
        binding.txtTimer.setVisibility(View.VISIBLE);
        timer= new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long l) {
                binding.txtTimer.setText(l/1000 +" sec");
            }

            @Override
            public void onFinish() {
                binding.stopRecording.setImageResource(R.drawable.ic_delete);
                binding.txtRecording.setText("Recording stopped..");
                binding.audioLayout.setVisibility(View.VISIBLE);
                binding.btnSend.setVisibility(View.VISIBLE);
                stopRecording();
            }
        };
       timer.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==PICK_IMAGE){
            if (resultCode==Activity.RESULT_OK){
                if (data!=null) {
                    if (data.getData() != null) {
                        Uri selectedImage = data.getData();
                        Bitmap bitmap=null;
                        try {
                            //convert uri to bitmap
                            //bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                            //convert uri to bitmap and handle rotation
                            bitmap = handleSamplingAndRotationBitmap(ChatDetailsActivity.this,selectedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
//                        File dir = getCacheDir();
//                        String file = SiliCompressor.with(this).compress(String.valueOf(selectedImage), dir);
//                        Uri uri = Uri.parse(file);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        assert bitmap != null;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] bytes = baos.toByteArray();
                        int length =  bytes.length/1024;
                        uploadToFirebase(bytes,PICK_IMAGE,length);
                    }
                }
            }
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE){
            if (resultCode==Activity.RESULT_OK){
                if(data!=null) {
                    if (data.getData() != null) {
                        Uri selectedImage = data.getData();
                        Bitmap bitmap=null;
                        try {
                           // bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                            bitmap = handleSamplingAndRotationBitmap(ChatDetailsActivity.this,selectedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        assert bitmap != null;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] bytes = baos.toByteArray();
//                        File dir = getCacheDir();
//                        String file = SiliCompressor.with(this).compress(String.valueOf(selectedImage), dir);
//                        Uri uri = Uri.parse(file);
                        int length =  bytes.length/1024;
                        uploadToFirebase(bytes,REQUEST_IMAGE_CAPTURE,length);
                    }
                }
            }
        }
    }

    private void uploadToFirebase(byte[] uri, int requestCode,int length){
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Chats Images").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        dialog.show();
        UploadTask uploadTask= reference.putBytes(uri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
               // Uri fdelete = Uri.fromFile(new File(uri.toString()));
               // File fdelete= new File(uri.toString());
                //File fdelete = new File(Objects.requireNonNull(getFilePath(uri)));

//                if (requestCode==PICK_IMAGE){
//                    if (file.exists()) {
//                        if (file.delete()) {
//                            System.out.println("file Deleted :");
//                        } else {
//                            System.out.println("file not Deleted :");
//                        }
//                    }
//                }

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
                            model.setMessage("send you a photo");
                            model.setImageUrl(filePath);
                            model.setType("photo");
                            binding.editMessage.getText().clear();
                            updateLastMessage("photo.jpg");

                            if (notify) {
                                sendNotification(receiverId, sendername,filePath,senderPP,email,senderId,"photo");
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //only works if image size is greater than 256kb!
                if (length>256){
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    int currentProgress=(int) progress;
                    dialog.setMessage("Uploading Image: " + currentProgress+ "%");
                }else {
                    dialog.setMessage("Uploading Image...");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        // app moved to foreground
        CheckConnection checkConnection= new CheckConnection();
        if (checkConnection.isConnected(getApplicationContext())){
            binding.txtChatConn.setVisibility(View.VISIBLE);
        }else {
            binding.txtChatConn.setVisibility(View.GONE);
        }
        if (auth.getCurrentUser()!=null){
//            database.goOnline();
            updateStatus("online");
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        // app moved to background
        if (auth.getCurrentUser()!=null){
            updateStatus("offline");
        }
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
            binding.editMessage.getText().clear();
            updateLastMessage(message);

            if (notify) {
                sendNotification(receiverId, sendername, message,senderPP,email,senderId,"text");
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
            updateLastMessage(heart);

            if (notify) {
                sendNotification(receiverId, sendername, heart,senderPP,email,senderId,"text");
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
        }
        seen="false";
        updateSeen(seen,receiverId,senderId);
    }

    private void updateLastMessage(String message){
        HashMap<String,Object> map= new HashMap<>();
        map.put("lastMessage",message);
        database.getReference().child("Users").child(senderId).child("Friends").child(receiverId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
            }
        });
    }

    private  void  updateSeen(String seen,String ID1,String ID2){
        HashMap<String,Object> map= new HashMap<>();
        map.put("seen", seen);
        database.getReference().child("Users").child(ID1).child("Friends").child(ID2).updateChildren(map);
    }

    private void getTypingStatus() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        checkStatus = reference.orderByChild("userId").equalTo(receiverId);
        eventListener1= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String StatusFromDB = snapshot.child(receiverId).child("Connection").child("Status").getValue(String.class);
                    Long lastOnline = snapshot.child(receiverId).child("Connection").child("lastOnline").getValue(Long.class);
                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd hh:mm a");
                    String dateString = formatter.format(new Date(lastOnline));


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
                                    binding.txtStatus.setText("Last Online: "+ dateString);
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
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void sendNotification(String receiver, String userName, String msg,String image,String email,String senderId,String msgType) {
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
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, userName, msg,image,receiver,email,senderId,msgType,"Chat",".ChatDetailsActivity",getApplicationContext(), ChatDetailsActivity.this);
                fcmNotificationsSender.SendNotifications();
            }
        };
        if (notify) {
            handler.postDelayed(runnable, 2000);
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

    void checkConn(){
        CheckConnection checkConnection= new CheckConnection();
        if (checkConnection.isConnected(getApplicationContext())){
            binding.txtChatConn.setVisibility(View.VISIBLE);
        }else {
            binding.txtChatConn.setVisibility(View.GONE);
        }
    }

    private void manageConnection() {
        final DatabaseReference status = database.getReference().child("Users").child((auth.getUid())).child("Connection").child("Status");
        final DatabaseReference lastOnlineRef = database.getReference().child("Users").child(auth.getUid()).child("Connection").child("lastOnline");
        infoConnected = database.getReference(".info/connected");

        eventListener=infoConnected.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    status.setValue("online");
                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                }else{
                    status.onDisconnect().setValue("offline");
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    protected void onDestroy() {
        checkStatus.removeEventListener(eventListener1);
        checkStatus1.removeEventListener(eventListener2);
        infoConnected.removeEventListener(eventListener);
        chat.keepSynced(false);
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        checkStatus.removeEventListener(eventListener1);
        checkStatus1.removeEventListener(eventListener2);
        chat.keepSynced(false);
        super.onStop();
    }

    @Override
    protected void onRestart() {
        checkConn();
        checkStatus.addValueEventListener(eventListener1);
        checkStatus1.addValueEventListener(eventListener2);
        chat.keepSynced(true);
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        HashMap<String,Object> map= new HashMap<>();
        map.put("Typing","Not Typing...");
        database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
        super.onBackPressed();
    }

    void updateStatus(String status){
        HashMap<String,Object> obj= new HashMap<>();
        obj.put("Status",status);
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("Connection").updateChildren(obj);
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = Math.min(heightRatio, widthRatio);

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }


}