package com.sushant.whatsapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivitySignUpBinding;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseAuth mAuth;
    ProgressDialog dialog;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.grayBackground));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this,R.color.grayBackground));

        mAuth = FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Creating an account");
        dialog.setMessage("We're Creating your account");


        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckConnection checkConnection= new CheckConnection();
                if (checkConnection.isConnected(getApplicationContext())){
                    showCustomDialog();
                    return;
                }
                if (binding.editName.getEditText().getText().toString().equals("") && binding.editEmail.getEditText().getText().toString().equals("") && binding.editPass.getEditText().getText().toString().equals("")) {
                    binding.editEmail.setErrorEnabled(true);
                    binding.editPass.setErrorEnabled(true);
                    binding.editName.setErrorEnabled(true);
                    binding.editEmail.setError("Field cannot be empty");
                    binding.editPass.setError("Field cannot be empty");
                    binding.editName.setError("Field cannot be empty");
                    return;
                } else {
                    if (!emailValidation() | !passValidation() | !userNameValidation()) {
                        return;
                    }
                }

                dialog.show();
                mAuth.createUserWithEmailAndPassword(binding.editEmail.getEditText().getText().toString(), binding.editPass.getEditText().getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                dialog.dismiss();
                                if (task.isSuccessful()) {
                                    FirebaseUser users= mAuth.getCurrentUser();
                                    users.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(getApplicationContext(), "Verification link sent to "+ users.getEmail(), Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "Verification link couldn't be sent", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    Users user = new Users(binding.editName.getEditText().getText().toString(), binding.editEmail.getEditText().getText().toString(), binding.editPass.getEditText().getText().toString());
                                    String id = task.getResult().getUser().getUid();
                                    user.setUserId(id);
                                    user.setProfilePic(String.valueOf(R.drawable.avatar));
                                    database.getReference().child("Users").child(id).setValue(user);
                                    database.getReference().child("Users").child(id).child("Connection").child("Status").setValue("offline");
                                    binding.etName.setText("");
                                    binding.etEmail.setText("");
                                    binding.etPass.setText("");
                                    Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignUpActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });

        binding.txtSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user= mAuth.getCurrentUser();
                if (user!=null){
                    mAuth.signOut();
                }
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean emailValidation() {
        String email = binding.editEmail.getEditText().getText().toString();
        if (email.isEmpty()) {
            binding.editEmail.setErrorEnabled(true);
            binding.editEmail.setError("Field cannot be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.setErrorEnabled(true);
            binding.editEmail.setError("Please provide a valid email");
            binding.editEmail.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
            binding.editEmail.requestFocus();
            return false;
        }else {
            binding.editEmail.setErrorEnabled(false);
            return true;
        }
    }

    public boolean passValidation() {
        String pass = binding.editPass.getEditText().getText().toString();
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(pass);
        if (pass.isEmpty()) {
            binding.editPass.setErrorEnabled(true);
            binding.editPass.setError("Field cannot be empty");
            return false;
        } else if (!m.matches()){
            binding.editPass.setErrorEnabled(true);
            binding.editPass.setError("Password should contain minimum 8 character,at least 1 letter and 1 number ");
            return false;
        }else {
            binding.editPass.setErrorEnabled(false);
            return true;
        }
    }

    public boolean userNameValidation() {
        String user = binding.editName.getEditText().getText().toString();
        if (user.isEmpty()) {
            binding.editName.setErrorEnabled(true);
            binding.editName.setError("Field cannot be empty");
            return false;
        } else {
            binding.editName.setErrorEnabled(false);
            return true;
        }
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(SignUpActivity.this);
        builder.setMessage("Please connect to the internet to proceed forward")
                .setTitle("No Connection")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

}