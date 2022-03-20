package com.sushant.whatsapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.sushant.whatsapp.Utils.ImageUtils;
import com.sushant.whatsapp.databinding.ActivitySettingsBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;
    String token, Token;
    boolean tokenExist, flag;
    ActivityResultLauncher<Intent> someActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Pic");
        dialog.setCancelable(false);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        database.getReference().child("Users").child(auth.getUid()).addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Token").exists()) {
                    tokenExist = true;
                    token = snapshot.child("Token").getValue(String.class);
                    assert token != null;
                    binding.textNotification.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_notification, 0, 0, 0);
                    binding.toggleNoti.setChecked(true);
                    binding.toggleNoti.setText("ON");
                    binding.toggleNoti.getThumbDrawable().setColorFilter(getResources().getColor(R.color.colorPurple), PorterDuff.Mode.MULTIPLY);
                    binding.toggleNoti.getTrackDrawable().setColorFilter(getResources().getColor(R.color.colorPurple), PorterDuff.Mode.MULTIPLY);
                } else {
                    binding.textNotification.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_notifications_off, 0, 0, 0);
                    binding.toggleNoti.setChecked(false);
                    tokenExist = false;
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
                if (!task.isSuccessful()) {
                    return;
                }
                Token = task.getResult();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                assert user != null;
            }
        });

        binding.toggleNoti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tokenExist) {
                    flag = false;
                    setToken(null);
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                    editor.putBoolean("Notification", flag);
                    editor.apply();
                    binding.toggleNoti.setChecked(false);
                    binding.toggleNoti.setText("OFF");
                    binding.toggleNoti.getThumbDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
                    binding.toggleNoti.getTrackDrawable().setColorFilter(getResources().getColor(R.color.grayBackground), PorterDuff.Mode.MULTIPLY);
                    Toast.makeText(getApplicationContext(), "Notification turned off!!", Toast.LENGTH_SHORT).show();
                } else {
                    flag = true;
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
                String username = binding.editUserName.getText().toString();
                String about = binding.editAbout.getText().toString();

                HashMap<String, Object> obj = new HashMap<>();
                obj.put("userName", username);
                obj.put("status", about);

                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                        .updateChildren(obj).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        Users users = snapshot1.getValue(Users.class);
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(users.getUserId()).child("Friends");
                                        Query checkStatus = reference.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid());
                                        checkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    HashMap<String, Object> obj1 = new HashMap<>();
                                                    obj1.put("userName", username);
                                                    obj1.put("status", about);
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
                                                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                    Groups groups = snapshot1.getValue(Groups.class);
                                                    assert groups != null;
                                                    if (groups.getGroupId() != null) {
                                                        DatabaseReference reference = database.getReference().child("Groups").child(users.getUserId()).child(groups.getGroupId()).child("participant");
                                                        Query checkStatus = reference.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid());
                                                        checkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                if (snapshot.exists()) {
                                                                    HashMap<String, Object> obj1 = new HashMap<>();
                                                                    obj1.put("userName", username);
                                                                    obj1.put("status", about);
                                                                    reference.child(FirebaseAuth.getInstance().getUid()).updateChildren(obj1);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });

                                                        HashMap<String, Object> obj1 = new HashMap<>();
                                                        obj1.put("userName", username);
                                                        obj1.put("status", about);
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
                Intent intent = new Intent(SettingsActivity.this, Privacy.class);
                startActivity(intent);
            }
        });

        //retrieving user data from real time database and setting them to the setting page views
        database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users user = snapshot.getValue(Users.class);
                        assert user != null;
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
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
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
                            Uri sFile = result.getData().getData();
                            dialog.show();

                            Bitmap bitmap1 = null;
                            try {
                                bitmap1 = ImageUtils.handleSamplingAndRotationBitmap(SettingsActivity.this, sFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            assert bitmap1 != null;
                            bitmap1.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                            byte[] img = baos.toByteArray();
                            binding.imgProfile.setImageBitmap(bitmap1);


                            final StorageReference reference = storage.getReference().child("Profile Pictures")
                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                            reference.putBytes(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(@NonNull Uri uri) {
                                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("profilePic").setValue(uri.toString());
                                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                                                    .child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                            Users users = snapshot1.getValue(Users.class);
                                                            assert users != null;
                                                            if (users.getUserId() != null) {
                                                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").
                                                                        child(users.getUserId()).child("Friends");
                                                                HashMap<String, Object> map = new HashMap<>();
                                                                map.put("profilePic", uri.toString());
                                                                reference.child(FirebaseAuth.getInstance().getUid()).updateChildren(map);
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                            database.getReference().child("Groups").child(Objects.requireNonNull(auth.getUid()))
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                            Groups groups = snapshot1.getValue(Groups.class);
                                                            assert groups != null;
                                                            database.getReference().child("Groups").child(auth.getUid()).child(groups.getGroupId())
                                                                    .child("participant").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.exists()) {
                                                                        for (DataSnapshot snapshot2 : snapshot.getChildren()) {
                                                                            Users users = snapshot2.getValue(Users.class);
                                                                            assert users != null;
                                                                            DatabaseReference reference = database.getReference().child("Groups")
                                                                                    .child(users.getUserId()).child(groups.getGroupId()).child("participant");
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
                });
    }

    private void setToken(String input) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Token", input);
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).updateChildren(map);
    }

    public void hideSoftKeyboard(Activity activity) {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}