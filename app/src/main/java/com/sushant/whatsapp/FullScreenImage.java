package com.sushant.whatsapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.Utils.Encryption;
import com.sushant.whatsapp.Utils.ImageUtils;
import com.sushant.whatsapp.databinding.ActivityFullScreenImageBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class FullScreenImage extends AppCompatActivity {

    ActivityFullScreenImageBinding binding;
    boolean isVisible = true, notify = false;
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    AlertDialog.Builder alertDialog;
    ProgressDialog dialog;
    FirebaseStorage storage;
    String senderId, receiverId, profilePic, senderRoom, receiverRoom, sendername, senderPP, email, receiverName, image, userToken;
    FirebaseAuth auth;
    FirebaseDatabase database;
    Handler handler;
    Runnable runnable;
    String data;
    ValueEventListener senderDetailsListener, TokenListener;
    DatabaseReference senderRef, TokenRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setAllowEnterTransitionOverlap(true);
        binding = ActivityFullScreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black_95));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.black_95));

        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);

        image = getIntent().getStringExtra("messageImage");
        senderId = auth.getUid();
        receiverId = getIntent().getStringExtra("UserId");
        profilePic = getIntent().getStringExtra("ProfilePic");
        email = getIntent().getStringExtra("userEmail");
        receiverName = getIntent().getStringExtra("UserName");

        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        Glide.with(this).load(image).placeholder(R.drawable.placeholder).into(binding.fullScreenImage);

        alertDialog = new AlertDialog.Builder(FullScreenImage.this).setTitle("Image")
                .setMessage("Do you want to send the edited image?")
                .setCancelable(false)
                .setIcon(R.drawable.ic_gallery)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bitmap bitmap = null;
                        try {

                            assert data != null;
                            bitmap = ImageUtils.handleSamplingAndRotationBitmap(FullScreenImage.this, Uri.parse(data));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        assert bitmap != null;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] bytes = baos.toByteArray();
                        int length = bytes.length / 1024;
                        uploadToFirebase(bytes, length);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });


        senderDetailsListener = new ValueEventListener() {
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
        };
        senderRef = database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()));
        senderRef.addValueEventListener(senderDetailsListener);

        binding.fullScreenImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isVisible) {
//                    binding.btmLayout.setVisibility(View.VISIBLE);
                    binding.layoutback.setVisibility(View.VISIBLE);
                    binding.imgDownload.setVisibility(View.VISIBLE);
                    binding.imgEdit.setVisibility(View.VISIBLE);
                    isVisible = true;
                } else {
//                    binding.btmLayout.setVisibility(View.GONE);
                    binding.layoutback.setVisibility(View.GONE);
                    binding.imgDownload.setVisibility(View.GONE);
                    binding.imgEdit.setVisibility(View.GONE);
                    isVisible = false;
                }
            }
        });

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(FullScreenImage.this, ChatDetailsActivity.class);
//                intent.putExtra("UserId", receiverId);
//                intent.putExtra("ProfilePic", profilePic);
//                intent.putExtra("UserName", receiverName);
//                intent.putExtra("userEmail", email);
//                intent.putExtra("pos", pos);
//                intent.putExtra("state", "fromFull");
//                startActivity(intent);
                onBackPressed();
            }
        });

        binding.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    startEditIntent();
                } else {
                    Dexter.withContext(getApplicationContext())
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    startEditIntent();
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(FullScreenImage.this, "Please accept permissions", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                            }).check();
                }
            }
        });

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            // There are no request codes
                            data = result.getData().getDataString();
                            alertDialog.show();
                        }
                    }
                });

        binding.imgDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    downloadFile();
                } else {
                    Dexter.withContext(getApplicationContext())
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    downloadFile();
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(FullScreenImage.this, "Please accept permissions", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                            }).check();
                }
            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                assert users != null;
                receiverName = users.getUserName();
                profilePic = users.getProfilePic();
                email = users.getMail();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void downloadFile() {
        Toast.makeText(this, "Started Downloading..", Toast.LENGTH_SHORT).show();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmsshhmmss");
        String date = dateFormat.format(new Date());
        String name = "IMG" + date + ".jpg";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(image));
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(name)
                .setDescription("Downloading image...")
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, name);
        if (isDownloadManagerAvailable()) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.allowScanningByMediaScanner();
        }
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }


    private static boolean isDownloadManagerAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    private void startEditIntent() {
        Intent dsPhotoEditorIntent = new Intent(FullScreenImage.this, DsPhotoEditorActivity.class);
        dsPhotoEditorIntent.setData(Uri.parse(image));
        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Edited Pics");
        int[] toolsToHide = {DsPhotoEditorActivity.TOOL_ORIENTATION, DsPhotoEditorActivity.TOOL_CROP};
        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE, toolsToHide);
        someActivityResultLauncher.launch(dsPhotoEditorIntent);
    }

    private void uploadToFirebase(byte[] uri, int length) {
        Calendar calendar = Calendar.getInstance();
        final StorageReference reference = storage.getReference().child("Chats Images").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(calendar.getTimeInMillis() + "");
        dialog.show();
        UploadTask uploadTask = reference.putBytes(uri);
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
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
                            String encryptedMessage = Encryption.encryptMessage(filePath);
                            model.setImageUrl(encryptedMessage);
                            model.setType("photo");
                            model.setMessage(Encryption.getPhotoLast());


                            if (notify) {
                                sendNotification(receiverId, sendername, filePath, senderPP, email, senderId);
                            }
                            notify = false;

                            database.getReference().child("Chats").child(senderRoom).push().setValue(model)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference().child("Chats").child(receiverRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    String path = "android.resource://" + getPackageName() + "/" + R.raw.google_notification;
                                                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
                                                    r.play();
                                                    Toast.makeText(FullScreenImage.this, "Sent Pic Successfully", Toast.LENGTH_SHORT).show();
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
                    dialog.setMessage("Uploading Image: " + currentProgress + "%");
                } else {
                    dialog.setMessage("Uploading Image...");
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    private void sendNotification(String receiver, String userName, String msg, String image, String email, String senderId) {
        TokenListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userToken = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        TokenRef = database.getReference().child("Users").child(receiver).child("Token");
        TokenRef.addListenerForSingleValueEvent(TokenListener);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(userToken, userName, msg, image, receiver, email, senderId, "photo",
                        "Chat", ".ChatDetailsActivity", getApplicationContext(), FullScreenImage.this);
                fcmNotificationsSender.SendNotifications();
            }
        };
        if (notify) {
            handler.postDelayed(runnable, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        if (TokenRef != null) {
            TokenRef.removeEventListener(TokenListener);
        }
        if (senderRef != null) {
            senderRef.removeEventListener(senderDetailsListener);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
        super.onBackPressed();
    }
}