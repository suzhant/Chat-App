package com.sushant.whatsapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityDeleteAcccountBinding;

import java.util.HashMap;

public class DeleteAccount extends AppCompatActivity {
    ActivityDeleteAcccountBinding binding;
    FirebaseAuth auth;
    DatabaseReference reference;
    FirebaseDatabase database;
    String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDeleteAcccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Delete Account");
        
        auth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
        reference=database.getReference().child("Users");

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
            }
        });

        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
        {
            sendUserToLoginActivity();
        }else{
            uid=user.getUid();
        }

        binding.btnDeleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Pass = binding.editNewPass.getEditText().getText().toString().trim();
                if (Pass.isEmpty()){
                    emptyError(binding.editNewPass);
                }

                assert user != null;
                AuthCredential credential = EmailAuthProvider
                        .getCredential(user.getEmail(), Pass);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    deleteUserFromFriends(user.getUid());
                                    deleteUser(uid);
                                    Toast.makeText(getApplicationContext(), "Re-authenticated", Toast.LENGTH_SHORT).show();
                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                sendUserToLoginActivity();
                                                Log.d("TAG", "onComplete: User deleted"+user.getEmail());
                                                Toast.makeText(getApplicationContext(), "User Account has been Deleted", Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(getApplicationContext(), "Account couldn't be deleted", Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                                } else {
                                    binding.editNewPass.setError("Wrong Password");
//                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


        binding.txtMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeleteAccount.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void sendUserToLoginActivity() {
        Intent intent= new Intent(DeleteAccount.this,SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void emptyError(TextInputLayout password){
        password.setErrorEnabled(true);
        password.setError("Field cannot be empty");
        password.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
        password.requestFocus();
    }

//    void deleteUserFromFriends(String userid){
//        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()){
//                    for (DataSnapshot snapshot1:snapshot.getChildren()){
//                        Users users=snapshot1.getValue(Users.class);
//                        if (users.getUserId()!=userid){
//                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(users.getUserId()).child("Friends");
//                            Query checkStatus = reference1.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid());
//                            checkStatus.addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    if (snapshot.exists()) {
//                                        HashMap<String,Object> map= new HashMap<>();
//                                        map.put(userid,null);
//                                        reference1.updateChildren(map);
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(@NonNull DatabaseError error) {
//
//                                }
//                            });
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    void deleteUserFromFriends(String userid){
        DatabaseReference reference1=FirebaseDatabase.getInstance().getReference().child("Users").child(userid).child("Friends");
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Users users= snapshot1.getValue(Users.class);
                    DatabaseReference reference2= FirebaseDatabase.getInstance().getReference().child("Users").child(users.getUserId()).child("Friends");
                    HashMap<String,Object> map= new HashMap<>();
                    map.put(userid,null);
                    reference2.updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void deleteUser(String userid){
        HashMap<String,Object> obj= new HashMap<>();
        obj.put(userid,null);
        reference.updateChildren(obj);
    }
}