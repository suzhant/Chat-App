package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivitySettingsBinding;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(SettingsActivity.this,MainActivity.class);
                startActivity(intent);
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
                        database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.avatar).into(binding.imgProfile);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data.getData()!=null){
            Uri sFile= data.getData();
            binding.imgProfile.setImageURI(sFile);

            final StorageReference reference= storage.getReference().child("Profile Pictures").child(FirebaseAuth.getInstance().getUid());

            reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).child("profilePic").setValue(uri.toString());
                            database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                        HashMap<String,Object> map= new HashMap<>();
                                                        map.put("profilePic",uri.toString());
                                                        reference.child(FirebaseAuth.getInstance().getUid()).updateChildren(map);
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


                            Toast.makeText(SettingsActivity.this, "Profile Pic Updated", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        }
    }

    public void hideSoftKeyboard(Activity activity){
        View view=this.getCurrentFocus();
        if (view!=null){
            InputMethodManager imm =(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}