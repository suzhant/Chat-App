package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityChangePasswordBinding;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangePassword extends AppCompatActivity {

    ActivityChangePasswordBinding binding;
    FirebaseAuth auth;
    String passFromDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
            }
        });

        binding.btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oPass = binding.editOldPass.getEditText().getText().toString().trim();
                String nPass = binding.editNewPass.getEditText().getText().toString().trim();
                boolean oldPass = passValidation(binding.editOldPass);
                boolean newPass = passValidation(binding.editNewPass);
                if (oPass.isEmpty() && nPass.isEmpty()){
                    emptyError(binding.editOldPass);
                    emptyError(binding.editNewPass);
                }
                if (!oldPass | !newPass) {
                    return;
                }
//                    FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if (snapshot.exists()) {
//                                Users user = snapshot.getValue(Users.class);
//                                assert user != null;
//                                passFromDb = user.getPassword();
//                                if (passFromDb.equals(oPass)) {
//                                    user.setPassword(nPass);
//                                    HashMap<String,Object> map= new HashMap<>();
//                                    map.put("password",user.getPassword());
//                                    FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid()).updateChildren(map);
//                                    FirebaseUser user1=auth.getCurrentUser();
//                                    assert user1 != null;
//                                    user1.updatePassword(nPass).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void unused) {
//                                            Toast.makeText(getApplicationContext(), "Password Changed", Toast.LENGTH_SHORT).show();
//                                            hideSoftKeyboard();
//                                            binding.editNewPass.getEditText().getText().clear();
//                                            binding.editOldPass.getEditText().getText().clear();
//                                            binding.editOldPass.clearFocus();
//                                            binding.editNewPass.clearFocus();
//                                            auth.signOut();
//                                            FirebaseDatabase.getInstance().goOffline();
//                                            Intent intent= new Intent(ChangePassword.this,SignInActivity.class);
//                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                            startActivity(intent);
//                                            finish();
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(getApplicationContext(), "Password Couldn't be changed"+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//
//                                }else {
//                                    binding.editOldPass.setError("Incorrect Old Password");
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                        }
//                    });

                FirebaseUser user = auth.getCurrentUser();
                assert user != null;
                AuthCredential credential = EmailAuthProvider
                        .getCredential(user.getEmail(), oPass);

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Re-authenticated", Toast.LENGTH_SHORT).show();
                                    user.updatePassword(nPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Password Changed", Toast.LENGTH_SHORT).show();
                                            HashMap<String,Object> map= new HashMap<>();
                                            map.put("password",nPass);
                                            FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid()).updateChildren(map);
                                            hideSoftKeyboard();
                                            binding.editNewPass.getEditText().getText().clear();
                                            binding.editOldPass.getEditText().getText().clear();
                                            binding.editOldPass.clearFocus();
                                            binding.editNewPass.clearFocus();
                                            auth.signOut();
                                            updateStatus();
                                            GoogleSignIn.getClient(getApplicationContext(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                                                    .signOut().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(getApplicationContext(), "SignOut success", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), "SignOut failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });
//                                            FirebaseDatabase.getInstance().goOffline();
                                            Intent intent= new Intent(ChangePassword.this,SignInActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                            } else { Toast.makeText(getApplicationContext(), "Password Couldn't be changed", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    binding.editOldPass.setError("Wrong Password");
//                                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

        });

        binding.txtMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangePassword.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public boolean passValidation(TextInputLayout password) {
        String pass = password.getEditText().getText().toString().trim();
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=\\S+$).{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(pass);
        if (pass.isEmpty()) {
            emptyError(password);
            return false;
        } else if (!m.matches()){
            password.setErrorEnabled(true);
            password.setError("Password should contain minimum 8 character,at least 1 letter and 1 number ");
            return false;
        }else {
            password.setErrorEnabled(false);
            password.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            password.clearFocus();
            return true;
        }
    }

    public void emptyError(TextInputLayout password){
        password.setErrorEnabled(true);
        password.setError("Field cannot be empty");
        password.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
        password.requestFocus();
    }
    void updateStatus(){
        HashMap<String,Object> obj= new HashMap<>();
        obj.put("Status", "offline");
        FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid()).child("Connection").updateChildren(obj);
    }

}