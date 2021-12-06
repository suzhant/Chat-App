package com.sushant.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sushant.whatsapp.Models.GroupChat;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityFinalCreateGroupBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class FinalCreateGroup extends AppCompatActivity{

    ActivityFinalCreateGroupBinding binding;
    ProgressDialog dialog;
    FirebaseDatabase database;
    ArrayList<Users> list;
    String uid,name,profilePic,mail;
    FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    FirebaseStorage storage;
    String id,image;
    ValueEventListener eventListener;
    DatabaseReference reference1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityFinalCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database= FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        dialog= new ProgressDialog(this);
        dialog.setMessage("Uploading Image");
        dialog.setCancelable(false);

        list= (ArrayList<Users>) getIntent().getSerializableExtra("participantList");

        database.getReference().child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user= snapshot.getValue(Users.class);
                uid=user.getUserId();
                name=user.getUserName();
                profilePic= user.getProfilePic();
                mail=user.getMail();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        UUID uuid= UUID.randomUUID();
        id=uuid+"";

        binding.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.editName.getEditText().getText().toString().isEmpty()){
                    binding.editName.setError("Field cannot be empty");
                    return;
                }
                GroupChat groupChat= new GroupChat();
                String GroupName= binding.editName.getEditText().getText().toString();
                groupChat.setGroupName(GroupName);
                groupChat.setGroupId(id);

                for (int i=0;i<list.size();i++){
                    Users users= list.get(i);
                    DatabaseReference reference= database.getReference().child("Group Chats").child(users.getUserId()).child(id);
                    reference.setValue(groupChat).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            for (int i=0;i<list.size();i++){
                                Users users= list.get(i);
                                if (FirebaseAuth.getInstance().getUid()!=null){
                                    if (FirebaseAuth.getInstance().getUid().equals(users.getUserId())){
                                        users.setRole("Admin");
                                    }else{
                                        users.setRole("normal");
                                    }
                                    reference.child("participant").child(users.getUserId()).setValue(users);
                                }
                            }
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Users admin= new Users();
                            admin.setUserId(uid);
                            admin.setUserName(name);
                            admin.setProfilePic(profilePic);
                            admin.setMail(mail);
                            admin.setRole("Admin");
                            reference.child("participant").child(admin.getUserId()).setValue(admin);
                        }
                    });
                }

                DatabaseReference reference= database.getReference().child("Group Chats").child(user.getUid()).child(id);
                reference.setValue(groupChat).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        for (int i=0;i<list.size();i++){
                            Users users= list.get(i);
                            if (FirebaseAuth.getInstance().getUid()!=null){
                                if (FirebaseAuth.getInstance().getUid().equals(users.getUserId())){
                                    users.setRole("Admin");
                                }else{
                                    users.setRole("normal");
                                }
                                reference.child("participant").child(users.getUserId()).setValue(users);
                            }
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Users admin= new Users();
                        admin.setUserId(uid);
                        admin.setUserName(name);
                        admin.setProfilePic(profilePic);
                        admin.setMail(mail);
                        admin.setRole("Admin");
                        reference.child("participant").child(admin.getUserId()).setValue(admin);
                    }
                });

                database.getReference().child("Group Chats").child(FirebaseAuth.getInstance().getUid()).child(id).child("groupPP").setValue(image);
                reference1=database.getReference().child("Group Chats").child(FirebaseAuth.getInstance().getUid()).child(id).child("participant");
                eventListener= new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Users users = snapshot1.getValue(Users.class);
                                assert users != null;
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Chats").child(users.getUserId()).child(id);
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("groupPP", image);
                                reference.updateChildren(map);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                reference1.addValueEventListener(eventListener);

                Intent intent= new Intent(FinalCreateGroup.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                Toast.makeText(getApplicationContext(), "Group Created Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        binding.imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,20);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data!=null){
            if (data.getData()!=null) {
                Uri sFile = data.getData();
                binding.imgProfile.setImageURI(sFile);

                final StorageReference reference = storage.getReference().child("Group Pictures").child(id);
                dialog.show();

                reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                dialog.hide();
                                image=uri.toString();
                                Toast.makeText(FinalCreateGroup.this, "Profile Pic Updated", Toast.LENGTH_SHORT).show();
                                binding.btnCreate.setEnabled(true);
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reference1.removeEventListener(eventListener);
    }
}