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
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
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
import com.sushant.whatsapp.databinding.ActivityFullScreenImageBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class FullScreenImage extends AppCompatActivity {

    ActivityFullScreenImageBinding binding;
    boolean isVisible = false, notify = false;
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
        binding = ActivityFullScreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

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
                            bitmap = handleSamplingAndRotationBitmap(FullScreenImage.this, Uri.parse(data));
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
                    binding.btmLayout.setVisibility(View.VISIBLE);
//                    binding.imgEdit.setVisibility(View.VISIBLE);
//                    binding.imgDownload.setVisibility(View.VISIBLE);
                    isVisible = true;
                } else {
                    binding.btmLayout.setVisibility(View.GONE);
//                    binding.imgEdit.setVisibility(View.GONE);
//                    binding.imgDownload.setVisibility(View.GONE);
                    isVisible = false;
                }
            }
        });

        binding.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                    startEditIntent();
                }else {
                    Dexter.withContext(getApplicationContext())
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                    startEditIntent();
                                }
                                @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(FullScreenImage.this, "Please accept permissions", Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
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
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                    downloadFile();
                }else {
                    Dexter.withContext(getApplicationContext())
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                                    downloadFile();
                                }
                                @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(FullScreenImage.this, "Please accept permissions", Toast.LENGTH_SHORT).show();
                                }
                                @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {/* ... */}
                            }).check();
                }
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

    private void startEditIntent(){
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
                            model.setMessage("send you a photo");
                            model.setImageUrl(filePath);
                            model.setType("photo");
                            updateLastMessage();

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

    private void updateLastMessage() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("lastMessage", "photo.jpg");
        database.getReference().child("Users").child(senderId).child("Friends").child(receiverId).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(receiverId).child("Friends").child(senderId).updateChildren(map);
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
        finish();
        super.onBackPressed();
    }
}