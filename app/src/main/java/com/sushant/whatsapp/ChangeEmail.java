package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.sushant.whatsapp.databinding.ActivityChangeEmailBinding;

import java.util.HashMap;
import java.util.Objects;

public class ChangeEmail extends AppCompatActivity {

    ActivityChangeEmailBinding binding;
    FirebaseAuth auth;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Change Email");

        auth = FirebaseAuth.getInstance();

        binding.btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.editEmail.getEditText().getText().toString().trim();
                String pass = binding.editPass.getEditText().getText().toString().trim();
                if (email.isEmpty() && pass.isEmpty()) {
                    emptyError(binding.editEmail);
                    emptyError(binding.editPass);
                    return;
                } else if (!emailValidation() | !passValidation()) {
                    return;
                }

                FirebaseUser user = auth.getCurrentUser();
                if (user == null)
                {
                    sendUserToLoginActivity();
                }else{
                    uid=user.getUid();
                }

                assert user != null;
                AuthCredential credential = EmailAuthProvider
                        .getCredential(Objects.requireNonNull(user.getEmail()), pass);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "User re-authenticated.", Toast.LENGTH_SHORT).show();
                                    //Now change your email address \\
                                    user.verifyBeforeUpdateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            user.updateEmail(email)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getApplicationContext(), "Email updated", Toast.LENGTH_SHORT).show();
                                                                updateEmailInFriend(user.getUid(),user.getEmail());
                                                                updateEmail(user.getEmail());
                                                                hideSoftKeyboard();
                                                                binding.editEmail.getEditText().getText().clear();
                                                                binding.editPass.getEditText().getText().clear();
                                                                binding.editEmail.clearFocus();
                                                                binding.editPass.clearFocus();
                                                                updateStatus();
                                                                auth.signOut();
                                                                GoogleSignIn.getClient(getApplicationContext(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                                        .build()).signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText(getApplicationContext(), "Sign Out Success", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(getApplicationContext(), "Sign Out failed", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
//                                                                FirebaseDatabase.getInstance().goOffline();
                                                                    sendUserToLoginActivity();
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), "Email couldn't be changed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "Verify before email failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    binding.editPass.setError("Wrong Password");
//                                    Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });

        binding.txtMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangeEmail.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(ChangeEmail.this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public boolean emailValidation() {
        String email = binding.editEmail.getEditText().getText().toString().trim();
        if (email.isEmpty()) {
            emptyError(binding.editEmail);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.setErrorEnabled(true);
            binding.editEmail.setError("Please provide a valid email");
            binding.editEmail.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
            binding.editEmail.requestFocus();
            return false;
        } else {
            binding.editEmail.setErrorEnabled(false);
            binding.editEmail.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            binding.editEmail.clearFocus();
            return true;
        }
    }

    public boolean passValidation() {
        String pass = binding.editPass.getEditText().getText().toString().trim();
        if (pass.isEmpty()) {
            emptyError(binding.editPass);
            return false;
        } else {
            binding.editPass.setErrorEnabled(false);
            binding.editPass.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            binding.editPass.clearFocus();
            return true;
        }
    }

    public void emptyError(TextInputLayout password) {
        password.setErrorEnabled(true);
        password.setError("Field cannot be empty");
        password.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
        password.requestFocus();
    }

    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void updateStatus(){
        HashMap<String,Object> obj= new HashMap<>();
        obj.put("Status", "offline");
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Connection").updateChildren(obj);
    }

    void updateEmail(String email){
        HashMap<String,Object> map= new HashMap<>();
        map.put("mail",email);
        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);
    }

    void updateEmailInFriend(String userid,String email){
        DatabaseReference reference1=FirebaseDatabase.getInstance().getReference().child("Users").child(userid).child("Friends");
        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Users users= snapshot1.getValue(Users.class);
                    HashMap<String,Object> map= new HashMap<>();
                    map.put("mail",email);
                    DatabaseReference reference2= FirebaseDatabase.getInstance().getReference().child("Users").child(users.getUserId()).child("Friends");
                    reference2.child(userid).updateChildren(map);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}