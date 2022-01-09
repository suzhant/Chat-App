package com.sushant.whatsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sushant.whatsapp.Models.Groups;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivitySettingsBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String token,Token;
    boolean tokenExist,flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        dialog= new ProgressDialog(this);
        dialog.setMessage("Uploading Pic");
        dialog.setCancelable(false);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(SettingsActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        database.getReference().child("Users").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Token").exists()){
                    tokenExist=true;
                    token=snapshot.child("Token").getValue(String.class);
                    assert token != null;
                    binding.textNotification.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_notification,0,0,0);
                    binding.toggleNoti.setChecked(true);
                    binding.toggleNoti.setText("ON");
                    binding.toggleNoti.getThumbDrawable().setColorFilter(getResources().getColor(R.color.colorPurple), PorterDuff.Mode.MULTIPLY);
                    binding.toggleNoti.getTrackDrawable().setColorFilter(getResources().getColor(R.color.colorPurple), PorterDuff.Mode.MULTIPLY);
                }else {
                    binding.textNotification.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_notifications_off,0,0,0);
                    binding.toggleNoti.setChecked(false);
                    tokenExist=false;
                    binding.toggleNoti.setText("OFF");
                    binding.toggleNoti.getThumbDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
                    binding.toggleNoti.getTrackDrawable().setColorFilter(getResources().getColor(R.color.grayBackground), PorterDuff.Mode.MULTIPLY);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()){
                    return;
                }
                Token=task.getResult();
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
            }
        });

        binding.toggleNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tokenExist){
                    flag=false;
                    setToken(null);
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putBoolean("Notification", flag);
                    editor.apply();
                    binding.toggleNoti.setChecked(false);
                    binding.toggleNoti.setText("OFF");
                    binding.toggleNoti.getThumbDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
                    binding.toggleNoti.getTrackDrawable().setColorFilter(getResources().getColor(R.color.grayBackground), PorterDuff.Mode.MULTIPLY);
                    Toast.makeText(getApplicationContext(), "Notification turned off!!", Toast.LENGTH_SHORT).show();
                }else {
                    flag=true;
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putBoolean("Notification", flag);
                    editor.apply();
                    setToken(Token);
                    binding.toggleNoti.setChecked(true);
                    binding.toggleNoti.setText("ON");
                    binding.toggleNoti.getThumbDrawable().setColorFilter(getResources().getColor(R.color.colorPurple), PorterDuff.Mode.MULTIPLY);
                    binding.toggleNoti.getTrackDrawable().setColorFilter(getResources().getColor(R.color.colorPurple), PorterDuff.Mode.MULTIPLY);
                    Toast.makeText(getApplicationContext(), "Notification turned on!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(SettingsActivity.this);
                String username= binding.editUserName.getText().toString();
                String about=binding.editAbout.getText().toString();

                HashMap<String,Object> obj= new HashMap<>();
                obj.put("userName",username);
                obj.put("status",about);

                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                        .updateChildren(obj).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    for (DataSnapshot snapshot1:snapshot.getChildren()){
                                        Users users=snapshot1.getValue(Users.class);
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(users.getUserId()).child("Friends");
                                        Query checkStatus = reference.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid());
                                        checkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    HashMap<String,Object> obj1= new HashMap<>();
                                                    obj1.put("userName",username);
                                                    obj1.put("status",about);
                                                    reference.child(FirebaseAuth.getInstance().getUid()).updateChildren(obj1);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                        database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot snapshot1:snapshot.getChildren()){
                                                    Groups groups=snapshot1.getValue(Groups.class);
                                                    assert groups != null;
                                                    if (groups.getGroupId()!=null){
                                                        DatabaseReference reference = database.getReference().child("Groups").child(users.getUserId()).child(groups.getGroupId()).child("participant");
                                                        Query checkStatus = reference.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid());
                                                        checkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()) {
                                                                    HashMap<String,Object> obj1= new HashMap<>();
                                                                    obj1.put("userName",username);
                                                                    obj1.put("status",about);
                                                                    reference.child(FirebaseAuth.getInstance().getUid()).updateChildren(obj1);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });

                                                        HashMap<String,Object> obj1= new HashMap<>();
                                                        obj1.put("userName",username);
                                                        obj1.put("status",about);
                                                        database.getReference().child("Groups").child(FirebaseAuth.getInstance().getUid()).child(groups.getGroupId()).child("participant")
                                                                .child(FirebaseAuth.getInstance().getUid()).updateChildren(obj1);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });

                binding.editAbout.clearFocus();
                binding.editUserName.clearFocus();
                Toast.makeText(SettingsActivity.this, "Profile Update", Toast.LENGTH_SHORT).show();
            }
        });

        binding.txtPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(SettingsActivity.this,Privacy.class);
                startActivity(intent);
            }
        });

        //retrieving user data from real time database and setting them to the setting page views
        database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        Users user= snapshot.getValue(Users.class);
                        Glide.with(getApplicationContext()).load(user.getProfilePic()).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(binding.imgProfile);
                        binding.editUserName.setText(user.getUserName());
                        binding.editAbout.setText(user.getStatus());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        //loading images from the device after clicking button
        binding.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,33);
            }
        });
    }

    private void setToken(String input) {
        HashMap<String,Object> map= new HashMap<>();
        map.put("Token",input);
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).updateChildren(map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            if (data.getData()!=null) {
                Uri sFile = data.getData();
                dialog.show();

                Bitmap bitmap1= null;
                try {
                    bitmap1 = handleSamplingAndRotationBitmap(SettingsActivity.this,sFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assert bitmap1 != null;
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                byte[] img = baos.toByteArray();
                binding.imgProfile.setImageBitmap(bitmap1);


                final StorageReference reference = storage.getReference().child("Profile Pictures").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                reference.putBytes(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(@NonNull Uri uri) {
                                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("profilePic").setValue(uri.toString());
                                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                Users users = snapshot1.getValue(Users.class);
                                                assert users != null;
                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(users.getUserId()).child("Friends");
                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("profilePic", uri.toString());
                                                reference.child(FirebaseAuth.getInstance().getUid()).updateChildren(map);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                database.getReference().child("Groups").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            for (DataSnapshot snapshot1:snapshot.getChildren()){
                                                Groups groups=snapshot1.getValue(Groups.class);
                                                assert groups != null;
                                                database.getReference().child("Groups").child(auth.getUid()).child(groups.getGroupId()).child("participant").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()){
                                                            for (DataSnapshot snapshot2:snapshot.getChildren()){
                                                                Users users= snapshot2.getValue(Users.class);
                                                                assert users != null;
                                                                DatabaseReference reference = database.getReference().child("Groups").child(users.getUserId()).child(groups.getGroupId()).child("participant");
                                                                HashMap<String, Object> map = new HashMap<>();
                                                                map.put("profilePic", uri.toString());
                                                                reference.child(FirebaseAuth.getInstance().getUid()).updateChildren(map);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                dialog.dismiss();
                                Toast.makeText(SettingsActivity.this, "Profile Pic Updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }
    }

    public void hideSoftKeyboard(Activity activity){
        View view=this.getCurrentFocus();
        if (view!=null){
            InputMethodManager imm =(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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