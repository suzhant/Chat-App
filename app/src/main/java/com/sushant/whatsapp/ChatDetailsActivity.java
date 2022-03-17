package com.sushant.whatsapp;

import static com.sushant.whatsapp.R.color.red;
import static com.sushant.whatsapp.R.color.white;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sushant.whatsapp.Adapters.ChatAdapter;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.Utils.Encryption;
import com.sushant.whatsapp.databinding.ActivityChatDetailsBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;


public class ChatDetailsActivity extends AppCompatActivity implements DefaultLifecycleObserver {

    private static final int REQUEST_IMAGE_CAPTURE = 200;
    ActivityChatDetailsBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    Animation scale_up, scale_down;
    ImageView blackCircle, stopRecording, btnSend;
    Handler handler, audioHandler;
    Runnable runnable, audioRunnable;
    boolean notify = false, recording;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String senderId, receiverId, senderRoom, receiverRoom, profilePic, senderPP, email, Status, receiverName, StatusFromDB, userToken, sendername, seen = "true", receiverNickName, senderNickName;
    long lastOnline;
    ValueEventListener eventListener1, eventListener2, chatListener, senderListener, tokenListener, eventListener, receiverListener, senderNickNameListener;
    Query checkStatus, checkStatus1;
    DatabaseReference infoConnected, chatRef, senderRef, tokenRef, receiverRef, senderNickNameRef;
    TextView txtTimer, txtRecording;
    long recordedTime;
    Dialog memberDialog;
    LinearLayoutManager layoutManager;
    private static String fileName = null;
    private MediaRecorder recorder = null;
    private static final String LOG_TAG = "AudioRecordTest";
    CountDownTimer timer;
    int pos, numItems;
    ArrayList<Messages> messageModel;
    ActivityResultLauncher<Intent> someActivityResultLauncher;


    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
//        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.chatStatusColor));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPurple));

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/recorded_audio.3gp";

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);

        memberDialog = new Dialog(ChatDetailsActivity.this);
        memberDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        memberDialog.setContentView(R.layout.bottom_sheet_audio_player);
        stopRecording = memberDialog.findViewById(R.id.stopRecording);
        btnSend = memberDialog.findViewById(R.id.btn_send);
        txtRecording = memberDialog.findViewById(R.id.txtRecording);
        txtTimer = memberDialog.findViewById(R.id.txt_timer);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
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

        // binding.userName.setText(receiverName);
        Glide.with(this).load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);
        checkConn();

        messageModel = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        binding.chatRecyclerView.setHasFixedSize(true);
        layoutManager.setStackFromEnd(true);
        final ChatAdapter chatAdapter = new ChatAdapter(messageModel, this, receiverId);
        chatAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        senderNickNameRef = database.getReference().child("Users").child(receiverId).child("Friends").child(Objects.requireNonNull(auth.getUid()));
        senderNickNameListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                assert users != null;
                if (users.getNickName() != null) {
                    sendername = users.getNickName();
                    senderNickName = users.getNickName();
                } else {
                    sendername = users.getUserName();
                }
                senderPP = users.getProfilePic();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        senderNickNameRef.addValueEventListener(senderNickNameListener);

        binding.icSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ChatSettings.class);
                intent.putExtra("UserId", receiverId);
                intent.putExtra("UserName", receiverNickName);
                intent.putExtra("ProfilePic", profilePic);
                intent.putExtra("Email", email);
                intent.putExtra("Status", Status);
                intent.putExtra("SenderNickName", senderNickName);
                startActivity(intent);
            }
        });


        binding.chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy != 0) {
                    binding.fabChat.setVisibility(View.VISIBLE);
                }
                pos = layoutManager.findLastCompletelyVisibleItemPosition();
                numItems = Objects.requireNonNull(binding.chatRecyclerView.getAdapter()).getItemCount();
                if (pos >= numItems - 5) {
                    binding.fabChat.setVisibility(View.GONE);
                }
            }
        });


        binding.fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutManager.scrollToPosition(messageModel.size() - 1);
            }
        });

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seen = "true";
                updateSeen(seen, senderId, receiverId);
                HashMap<String, Object> map = new HashMap<>();
                map.put("Typing", "Not Typing...");
                database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
                if (binding.audioLayout.getVisibility() == View.VISIBLE) {
                    binding.audioLayout.setVisibility(View.GONE);
                    binding.linear.setVisibility(View.VISIBLE);
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();//Method finish() will destroy your activity and show the one that started it.
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatDetailsActivity.this, ProfileActivity.class);
                intent.putExtra("UserIdPA", receiverId);
                intent.putExtra("UserNamePA", receiverName);
                intent.putExtra("ProfilePicPA", profilePic);
                intent.putExtra("EmailPA", email);
                intent.putExtra("StatusPA", Status);
                startActivity(intent);
            }
        });

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        updateSeen(seen, senderId, receiverId);

        chatRef = database.getReference().child("Chats").child(senderRoom);
        chatListener = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = messageModel.size();
                messageModel.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Messages model = dataSnapshot.getValue(Messages.class);
                    assert model != null;
                    //  model.setMessageId(model.getMessageId());
                    if (model.getMessage() != null) {
                        try {
                            model.setMessage(Encryption.decryptMessage(model.getMessage()));
                        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                    if (model.getImageUrl() != null) {
                        try {
                            model.setImageUrl(Encryption.decryptMessage(model.getImageUrl()));
                        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                    if (model.getAudioFile() != null) {
                        try {
                            model.setAudioFile(Encryption.decryptMessage(model.getAudioFile()));
                        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    model.setProfilePic(profilePic);
                    messageModel.add(model);
                }
                chatAdapter.notifyDataSetChanged();
                if (count == 0) {
                    chatAdapter.notifyDataSetChanged();
                } else {
                    //binding.chatRecyclerView.smoothScrollToPosition(messageModel.size() - 1);
                    pos = layoutManager.findLastCompletelyVisibleItemPosition();
                    numItems = Objects.requireNonNull(binding.chatRecyclerView.getAdapter()).getItemCount();
                    if (pos >= numItems - 2) {
                        chatAdapter.notifyItemRangeChanged(messageModel.size(), messageModel.size());
                        binding.chatRecyclerView.smoothScrollToPosition(messageModel.size() - 1);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        chatRef.addValueEventListener(chatListener);
        chatRef.keepSynced(true);


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
                if (editable.length() > 0) {
                    binding.imgCamera.setVisibility(View.GONE);
                    binding.imgGallery.setVisibility(View.GONE);
                    binding.imgMic.setVisibility(View.GONE);
                    binding.icSend.setImageResource(R.drawable.ic_send);
                    binding.icSend.setColorFilter(getResources().getColor(R.color.white));
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Typing", "Typing...");
                    database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
                } else {
                    binding.imgCamera.setVisibility(View.VISIBLE);
                    binding.imgGallery.setVisibility(View.VISIBLE);
                    binding.imgMic.setVisibility(View.VISIBLE);
                    binding.icSend.setImageResource(R.drawable.ic_favorite);
                    binding.icSend.setColorFilter(getResources().getColor(red));
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Typing", "Not Typing...");
                    database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
                }
            }
        });


//        senderRef = database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()));
//        senderListener = new ValueEventListener() {
//            @RequiresApi(api = Build.VERSION_CODES.P)
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Users users = snapshot.getValue(Users.class);
//                assert users != null;
//                sendername = users.getUserName();
//                senderPP = users.getProfilePic();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        };
//        senderRef.addValueEventListener(senderListener);

        receiverListener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                assert users != null;
                if (users.getNickName() != null) {
                    receiverNickName = users.getNickName();
                    binding.userName.setText(receiverNickName);
                } else {
                    receiverNickName = users.getUserName();
                    binding.userName.setText(receiverName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        receiverRef = database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("Friends").child(receiverId);
        receiverRef.addValueEventListener(receiverListener);


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
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                someActivityResultLauncher.launch(intent);

            }
        });
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            if (result.getData().getClipData() != null) {
                                ClipData clipData = result.getData().getClipData();
                                int count = clipData.getItemCount();
                                for (int i = 0; i < count; i++) {
                                    Uri imageUrl = clipData.getItemAt(i).getUri();
                                    createImageBitmap(imageUrl);
                                }

                            } else if (result.getData().getData() != null) {
                                Uri selectedImage = result.getData().getData();
                                createImageBitmap(selectedImage);
                            }
                        }
                    }
                });

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
                checkAudioPermission();
            }
        });

        memberDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (recorder != null) {
                    stopRecording();
                    recording = false;
                    timer.cancel();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (audioHandler.hasCallbacks(audioRunnable)) {
                        audioHandler.removeCallbacks(audioRunnable);
                    }
                }
                stopRecording.setColorFilter(ContextCompat.getColor(ChatDetailsActivity.this, white));
                stopRecording.setImageResource(R.drawable.ic_stop_circle);
                txtRecording.setText("Recording :");
                txtTimer.setText("wait..");
                btnSend.setVisibility(View.GONE);
            }
        });

        stopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recording) {
                    stopRecording.setImageResource(R.drawable.ic_delete);
                    stopRecording.setColorFilter(ContextCompat.getColor(ChatDetailsActivity.this, red));
                    txtRecording.setText("Recorded :");
                    btnSend.setVisibility(View.VISIBLE);
                    recordedTime = 30 - recordedTime;
                    txtTimer.setText(recordedTime + " sec");
                    recording = false;
                    if (recorder != null) {
                        stopRecording();
                    }
                    timer.cancel();
                } else {
                    stopRecording.setImageResource(R.drawable.ic_stop_circle);
                    txtRecording.setText("Recording :");
                    btnSend.setVisibility(View.GONE);
                    memberDialog.dismiss();
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recording = false;
                stopRecording.setImageResource(R.drawable.ic_stop_circle);
                txtRecording.setText("Recording :");
                btnSend.setVisibility(View.GONE);
                memberDialog.dismiss();
                uploadAudioToFirebase();
            }
        });

        binding.icVideoCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkResponse("video");
            }
        });

        binding.icAudioCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkResponse("audio");
            }
        });

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        // app moved to foreground
        CheckConnection checkConnection = new CheckConnection();
        if (checkConnection.isConnected(getApplicationContext())) {
            binding.txtChatConn.setVisibility(View.VISIBLE);
        } else {
            binding.txtChatConn.setVisibility(View.GONE);
        }
        if (auth.getCurrentUser() != null) {
//            database.goOnline();
            updateStatus("online");
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        // app moved to background
        if (auth.getCurrentUser() != null) {
            updateStatus("offline");
        }
    }

    private void createImageBitmap(Uri imageUrl) {
        Bitmap bitmap = null;
        try {
            bitmap = handleSamplingAndRotationBitmap(ChatDetailsActivity.this, imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert bitmap != null;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        int length = bytes.length / 1024;
        uploadImageToFirebase(bytes, length);
    }

    private void checkAudioPermission() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        String path = "android.resource://" + getPackageName() + "/" + R.raw.when_604;
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
                        r.play();
                        stopRecording.setEnabled(false);
                        memberDialog.show();
                        memberDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        memberDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        memberDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                        memberDialog.getWindow().setGravity(Gravity.BOTTOM);
                        memberDialog.getWindow().getAttributes().windowAnimations = R.style.NoAnimation;
                        audioHandler = new Handler();
                        audioRunnable = new Runnable() {
                            @Override
                            public void run() {
                                recording = true;
                                stopRecording.setEnabled(true);
                                startRecording();
                                startCountDownTimer();
                            }
                        };
                        audioHandler.postDelayed(audioRunnable, 1000);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ChatDetailsActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                }).check();
    }

    private void checkResponse(String type) {

        database.getReference().child("Users").child(receiverId).child("VideoCall").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String onCall = snapshot.child("onCall").getValue(String.class);
                    if ("false".equals(onCall)) {
                        Intent intent = new Intent(ChatDetailsActivity.this, ConnectingActivity.class);
                        intent.putExtra("ProfilePic", profilePic);
                        intent.putExtra("UserName", receiverName);
                        intent.putExtra("UserId", receiverId);
                        intent.putExtra("userEmail", email);
                        intent.putExtra("type", type);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(ChatDetailsActivity.this, "Users is on Another Call", Toast.LENGTH_SHORT).show();
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
        });
    }

    private void uploadAudioToFirebase() {
        dialog.setMessage("Uploading Audio...");
        dialog.show();
        Calendar calendar = Calendar.getInstance();
        StorageReference filepath = storage.getReference().child("Audio").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        Uri uri = Uri.fromFile(new File(fileName));
        filepath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    binding.linear.setVisibility(View.VISIBLE);
                    binding.audioLayout.setVisibility(View.GONE);
                    dialog.dismiss();
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();
                            notify = true;
                            String key = database.getReference().push().getKey();
                            Date date = new Date();
                            final Messages model = new Messages(senderId, profilePic, date.getTime());
                            String encryptedMessage = Encryption.encryptMessage(filePath);

                            model.setAudioFile(encryptedMessage);
                            model.setType("audio");
                            model.setMessageId(key);
                            binding.editMessage.getText().clear();
                            updateLastMessage(Encryption.getAudioLast());

                            if (notify) {
                                sendNotification(receiverId, sendername, filePath, senderPP, email, senderId, "audio");
                            }
                            notify = false;

                            assert key != null;
                            database.getReference().child("Chats").child(senderRoom).child(key).setValue(model)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("Chats").child(receiverRoom).child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    String path = "android.resource://" + getPackageName() + "/" + R.raw.google_notification;
                                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
                                                    r.play();
                                                    binding.chatRecyclerView.smoothScrollToPosition(messageModel.size());
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
        txtTimer.setVisibility(View.VISIBLE);
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long l) {
//                binding.txtTimer.setText(l/1000 +" sec");
                recordedTime = l / 1000;
                txtTimer.setText(l / 1000 + " sec");
            }

            @Override
            public void onFinish() {
                stopRecording.setImageResource(R.drawable.ic_delete);
                stopRecording.setColorFilter(ContextCompat.getColor(ChatDetailsActivity.this, red));
                txtRecording.setText("Recorded :");
                btnSend.setVisibility(View.VISIBLE);
                recordedTime = 30 - recordedTime;
                txtTimer.setText(recordedTime + " sec");
                stopRecording();
                recording = false;
            }
        };
        timer.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    if (data.getData() != null) {
                        Uri selectedImage = data.getData();
                        createImageBitmap(selectedImage);
                    }
                }
            }
        }
    }

    private void uploadImageToFirebase(byte[] uri, int length) {
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Chats Images").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        if (length > 256) {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        }
        dialog.setProgress(0);
        dialog.setMessage("Uploading Image");
        dialog.show();
        UploadTask uploadTask = reference.putBytes(uri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                // Uri fdelete = Uri.fromFile(new File(uri.toString()));
                // File fdelete= new File(uri.toString());
                //File fdelete = new File(Objects.requireNonNull(getFilePath(uri)));

                if (task.isSuccessful()) {
                    dialog.dismiss();
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @RequiresApi(api = Build.VERSION_CODES.P)
                        @Override
                        public void onSuccess(Uri uri) {
                            String filePath = uri.toString();
                            notify = true;
                            String key = database.getReference().push().getKey();
                            Date date = new Date();
                            final Messages model = new Messages(senderId, profilePic, date.getTime());
                            String encryptedMessage = Encryption.encryptMessage(filePath);
                            model.setImageUrl(encryptedMessage);
                            model.setType("photo");
                            model.setMessageId(key);
                            binding.editMessage.getText().clear();
                            updateLastMessage(Encryption.getPhotoLast());

                            if (notify) {
                                sendNotification(receiverId, sendername, filePath, senderPP, email, senderId, "photo");
                            }
                            notify = false;

                            assert key != null;
                            database.getReference().child("Chats").child(senderRoom).child(key).setValue(model)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("Chats").child(receiverRoom).child(key).setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    String path = "android.resource://" + getPackageName() + "/" + R.raw.google_notification;
                                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
                                                    r.play();
                                                    binding.chatRecyclerView.smoothScrollToPosition(messageModel.size());
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
                if (length > 256) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    int currentProgress = (int) progress;
                    dialog.setProgress(currentProgress);
                }
            }
        });
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

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void sendMessage() {
        notify = true;
        binding.icSend.startAnimation(scale_down);
        binding.icSend.startAnimation(scale_up);
        final String message = binding.editMessage.getText().toString();
        String key = database.getReference().push().getKey();
        assert key != null;
        if (!message.isEmpty()) {
            binding.chatRecyclerView.smoothScrollToPosition(messageModel.size());
            String encryptedMessage = Encryption.encryptMessage(message);
            final Messages model = new Messages(senderId, encryptedMessage, profilePic);
            Date date = new Date();
            model.setTimestamp(date.getTime());
            model.setType("text");
            model.setMessageId(key);
            binding.editMessage.getText().clear();
            updateLastMessage(message);

            if (notify) {
                sendNotification(receiverId, sendername, message, senderPP, email, senderId, "text");
            }
            notify = false;


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


        } else {
            notify = true;
            binding.icSend.startAnimation(scale_down);
            binding.icSend.startAnimation(scale_up);
            int unicode = 0x2764;
            String heart = new String(Character.toChars(unicode));
            String encryptedMessage = Encryption.encryptMessage(heart);
            final Messages model1 = new Messages(senderId, encryptedMessage, profilePic);
            model1.setType("text");
            Date date = new Date();
            model1.setTimestamp(date.getTime());
            model1.setMessageId(key);
            updateLastMessage(heart);

            if (notify) {
                sendNotification(receiverId, sendername, heart, senderPP, email, senderId, "text");
            }
            notify = false;

            database.getReference().child("Chats").child(senderRoom).child(key).setValue(model1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("Chats").child(receiverRoom).child(key).setValue(model1).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        seen = "false";
        updateSeen(seen, receiverId, senderId);
    }

    private void updateLastMessage(String message) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("lastMessage", message);
        database.getReference().child("Users").child(senderId).child("Friends").child(receiverId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
            }
        });
    }

    private void updateSeen(String seen, String ID1, String ID2) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("seen", seen);
        database.getReference().child("Users").child(ID1).child("Friends").child(ID2).updateChildren(map);
    }

    private void getTypingStatus() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        checkStatus = reference.orderByChild("userId").equalTo(receiverId);
        eventListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    StatusFromDB = snapshot.child(receiverId).child("Connection").child("Status").getValue(String.class);
                    lastOnline = snapshot.child(receiverId).child("Connection").child("lastOnline").getValue(Long.class);
                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd hh:mm a");
                    String dateString = formatter.format(new Date(lastOnline));


                    assert StatusFromDB != null;

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users");
                    checkStatus1 = reference1.orderByChild("userId").equalTo(senderId);
                    eventListener2 = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String presence = snapshot.child(senderId).child("Friends").child(receiverId).child("Typing").getValue(String.class);
                                if (StatusFromDB.equals("online")) {
                                    if ("Typing...".equals(presence)) {
                                        binding.imgStatus.setColorFilter(Color.GREEN);
                                        binding.txtStatus.setText(presence);
                                    } else {
                                        binding.imgStatus.setColorFilter(Color.GREEN);
                                        binding.txtStatus.setText(StatusFromDB);
                                    }
                                } else {
                                    binding.imgStatus.setColorFilter(Color.GRAY);
                                    binding.txtStatus.setText("Last Online: " + dateString);
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
    private void sendNotification(String receiver, String userName, String msg, String image, String email, String senderId, String msgType) {
        tokenRef = database.getReference().child("Users").child(receiver).child("Token");
        tokenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userToken = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        tokenRef.addValueEventListener(tokenListener);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, userName, msg, image, receiver, email, senderId, msgType,
                        "Chat", ".ChatDetailsActivity", getApplicationContext(), ChatDetailsActivity.this);
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

    void checkConn() {
        CheckConnection checkConnection = new CheckConnection();
        if (checkConnection.isConnected(getApplicationContext())) {
            binding.txtChatConn.setVisibility(View.VISIBLE);
        } else {
            binding.txtChatConn.setVisibility(View.GONE);
        }
    }

    private void manageConnection() {
        final DatabaseReference status = database.getReference().child("Users").child((auth.getUid())).child("Connection").child("Status");
        final DatabaseReference lastOnlineRef = database.getReference().child("Users").child(auth.getUid()).child("Connection").child("lastOnline");
        infoConnected = database.getReference(".info/connected");

        eventListener = infoConnected.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    status.setValue("online");
                    lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                } else {
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
        if (checkStatus != null) {
            checkStatus.removeEventListener(eventListener1);
        }
        if (checkStatus1 != null) {
            checkStatus1.removeEventListener(eventListener2);
        }
        if (infoConnected != null) {
            infoConnected.removeEventListener(eventListener);
        }
        if (chatRef != null) {
            chatRef.keepSynced(false);
            chatRef.removeEventListener(chatListener);
        }
        if (senderNickNameRef != null) {
            senderNickNameRef.removeEventListener(senderNickNameListener);
        }
        if (receiverRef != null) {
            receiverRef.removeEventListener(receiverListener);
        }
        if (tokenRef != null) {
            tokenRef.removeEventListener(tokenListener);
        }
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (checkStatus != null) {
            checkStatus.removeEventListener(eventListener1);
        }
        if (checkStatus1 != null) {
            checkStatus1.removeEventListener(eventListener2);
        }
        if (chatRef != null) {
            chatRef.keepSynced(false);
            chatRef.removeEventListener(chatListener);
        }
        if (senderNickNameRef != null) {
            senderNickNameRef.removeEventListener(senderNickNameListener);
        }
        if (receiverRef != null) {
            receiverRef.removeEventListener(receiverListener);
        }
        if (tokenRef != null) {
            tokenRef.removeEventListener(tokenListener);
        }
        super.onStop();
    }

    @Override
    protected void onRestart() {
        checkConn();
        if (checkStatus != null) {
            checkStatus.addValueEventListener(eventListener1);
        }
        if (checkStatus1 != null) {
            checkStatus1.addValueEventListener(eventListener2);
        }
        if (chatRef != null) {
            chatRef.keepSynced(true);
            chatRef.addValueEventListener(chatListener);
        }
        if (receiverRef != null) {
            receiverRef.addValueEventListener(receiverListener);
        }
        if (senderNickNameRef != null) {
            senderNickNameRef.addValueEventListener(senderNickNameListener);
        }
        if (tokenRef != null) {
            tokenRef.addValueEventListener(tokenListener);
        }
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Typing", "Not Typing...");
        database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
        finish();
        super.onBackPressed();
    }

    void updateStatus(String status) {
        HashMap<String, Object> obj = new HashMap<>();
        obj.put("Status", status);
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