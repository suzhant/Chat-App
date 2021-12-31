package com.sushant.whatsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.iceteck.silicompressorr.SiliCompressor;
import com.squareup.picasso.Picasso;
import com.sushant.whatsapp.Models.Groups;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivitySettingsBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
                }else {
                    binding.textNotification.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_notifications_off,0,0,0);
                    tokenExist=false;
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

        binding.textNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tokenExist){
                    flag=false;
                    setToken(null);
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putBoolean("Notification", flag);
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "Notification turned Off!!", Toast.LENGTH_SHORT).show();
                }else {
                    flag=true;
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putBoolean("Notification", flag);
                    editor.apply();
                    setToken(Token);
                    Toast.makeText(getApplicationContext(), "Notification turned On!!", Toast.LENGTH_SHORT).show();
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
                                        database.getReference().child("Groups").child(users.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
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
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
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
//                File file= new File(Objects.requireNonNull(getFilePath(sFile)));
                dialog.show();
                Bitmap bitmap=null;
                try{
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                byte[] img = baos.toByteArray();
//                Bitmap bitmap1= null;
//                try {
//                    bitmap1 = modifyOrientation(bitmap,file.getAbsolutePath());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                Glide.with(this).load(sFile).into(binding.imgProfile);


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

    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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