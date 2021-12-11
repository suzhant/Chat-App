package com.sushant.whatsapp;

import static com.sushant.whatsapp.R.color.red;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class ChatDetailsActivity extends AppCompatActivity {

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
    String senderId,receiverId,senderRoom,receiverRoom,profilePic;
    ValueEventListener eventListener1,eventListener2;
    Query checkStatus,checkStatus1;
    String currentPhotoPath;



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
        Glide.with(this).load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);

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
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        binding.imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)==
                        PackageManager.PERMISSION_GRANTED){
                    dispatchTakePictureIntent();
                }else {
                    ActivityCompat.requestPermissions(ChatDetailsActivity.this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},CAMERA_PERMISSION_CODE);
                }

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
            binding.editMessage.getText().clear();
            updateLastMessage(message);

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
            updateLastMessage(heart);

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==PICK_IMAGE){
            if (resultCode==Activity.RESULT_OK){
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
        if (requestCode == REQUEST_IMAGE_CAPTURE){
        if (resultCode==Activity.RESULT_OK){
                if(data!=null) {
                    if (data.getData() != null) {
                        File f = new File(currentPhotoPath);
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        this.sendBroadcast(mediaScanIntent);
                        uploadToFirebase(contentUri);
                    }
                }
            }
        }
    }

    private void uploadToFirebase(Uri uri){
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Chats Images").child(FirebaseAuth.getInstance().getUid()).child(calendar.getTimeInMillis() + "");
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
                                sendNotification(receiverId, sendername,fdelete.getName());
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

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.sushant.whatsapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
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