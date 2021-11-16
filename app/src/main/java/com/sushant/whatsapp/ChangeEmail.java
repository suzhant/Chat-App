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
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.databinding.ActivityChangeEmailBinding;

import java.util.HashMap;
import java.util.Objects;

public class ChangeEmail extends AppCompatActivity {

    ActivityChangeEmailBinding binding;
    FirebaseAuth auth;

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

//                FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()) {
//                            Users user = snapshot.getValue(Users.class);
//                            assert user != null;
//                            passFromDb = user.getPassword();
//                            if (passFromDb.equals(pass)) {
//                                user.setMail(email);
//                                HashMap<String,Object> map= new HashMap<>();
//                                map.put("mail",user.getMail());
//                                FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid()).updateChildren(map);
//                                FirebaseUser user1=auth.getCurrentUser();
//                                assert user1 != null;
//                                user1.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void unused) {
//                                        Toast.makeText(getApplicationContext(), "Email Changed", Toast.LENGTH_SHORT).show();
//                                        hideSoftKeyboard();
//                                        binding.editEmail.getEditText().getText().clear();
//                                        binding.editPass.getEditText().getText().clear();
//                                        binding.editEmail.clearFocus();
//                                        binding.editPass.clearFocus();
//                                        auth.signOut();
//                                        FirebaseDatabase.getInstance().goOffline();
//                                        Intent intent= new Intent(ChangeEmail.this,SignInActivity.class);
//                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                        startActivity(intent);
//                                        finish();
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Toast.makeText(getApplicationContext(), "Email Couldn't be changed"+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                            }else {
//                                binding.editPass.setError("Incorrect Password");
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                    }
//                });
                FirebaseUser user = auth.getCurrentUser();

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
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    assert user != null;
                                    user.verifyBeforeUpdateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            user.updateEmail(email)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getApplicationContext(), "Email updated", Toast.LENGTH_SHORT).show();
                                                                HashMap<String,Object> map= new HashMap<>();
                                                                map.put("mail",user.getEmail());
                                                                FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid()).updateChildren(map);
                                                                hideSoftKeyboard();
                                                                binding.editEmail.getEditText().getText().clear();
                                                                binding.editPass.getEditText().getText().clear();
                                                                binding.editEmail.clearFocus();
                                                                binding.editPass.clearFocus();
                                                                auth.signOut();
                                                                updateStatus();
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
                                                                Intent intent = new Intent(ChangeEmail.this, SignInActivity.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                startActivity(intent);
                                                                finish();
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
        FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid()).child("Connection").updateChildren(obj);
    }

}