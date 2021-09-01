package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.audiofx.AutomaticGainControl;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.IdentityProviders;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivitySignInBinding;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class SignInActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 10010;
    Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private ActivitySignInBinding binding;
    private ProgressDialog dialog;
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    FirebaseDatabase database;
    BroadcastReceiver broadcastReceiver;

    SharedPreferences sharedPreferences;
    boolean flag;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        broadcastReceiver=new InternetCheckServices();
        registerBroadcastReceiver();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Login");
        dialog.setMessage("Login to your account");

        binding.txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(SignInActivity.this,ForgotPassword.class);
                startActivity(intent);
            }
        });


        //sign in using biometrics
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("MY_APP_TAG", "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, REQUEST_CODE);
                break;
        }
        executor = ContextCompat.getMainExecutor(SignInActivity.this);
        biometricPrompt = new BiometricPrompt(this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                boolean isLogin = sharedPreferences.getBoolean("isGoogle", flag);
                if (!isLogin) {
                    String email = sharedPreferences.getString("email", "");
                    String pass = sharedPreferences.getString("password", "");
                    performAuth(email, pass);
                } else {
                    signIn();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();


        binding.imgFingerPrint.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        boolean isLogin = sharedPreferences.getBoolean("isLogin", false);
        if (isLogin) {
            binding.imgFingerPrint.setVisibility(View.VISIBLE);
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("843849987770-2imhf1vifnfvsms5c512r88op2pjkqav.apps.googleusercontent.com") //json file client_id
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(SignInActivity.this);
                CheckConnection checkConnection= new CheckConnection();
                if (!checkConnection.isConnected(getApplicationContext())){
                    showCustomDialog();
                    return;
                }
                String email = binding.editEmail.getEditText().getText().toString().trim();
                String pass = binding.editPass.getEditText().getText().toString().trim();
                if (email.isEmpty() && pass.isEmpty()) {
                    binding.editEmail.setErrorEnabled(true);
                    binding.editPass.setErrorEnabled(true);
                    binding.editEmail.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
                    binding.editPass.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
                    binding.editEmail.setError("Field cannot be empty");
                    binding.editPass.setError("Field cannot be empty");
                    return;
                }else if (!emailValidation() | !passValidation()){
                        return;
                }
                performAuth(email, pass);

            }
        });



        binding.txtSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        //automatically sign in user
        if (auth.getCurrentUser() != null && auth.getCurrentUser().isEmailVerified()) {
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    //check connection
    private void registerBroadcastReceiver(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            registerReceiver(broadcastReceiver,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

    }

    private void unregisterNetwork(){
        try {
            unregisterReceiver(broadcastReceiver);

        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }

    //Google Authentication
    int RC_SIGN_IN = 65;

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
//                            database.goOnline();
                            flag = true;
                            FirebaseUser user = auth.getCurrentUser();

                            Users users = new Users();
                            users.setMail(user.getEmail());
                            users.setUserId(user.getUid());
                            users.setUserName(user.getDisplayName());
                            users.setProfilePic(user.getPhotoUrl().toString());
//                            userStatus("online");
                            database.getReference().child("Users").child(user.getUid()).setValue(users);


                            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                            editor.putBoolean("isGoogle", flag);
                            editor.apply();

                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            intent.putExtra("googleToken",idToken);
                            startActivity(intent);
                            Toast.makeText(SignInActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();

                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            // updateUI(null);
                        }
                    }
                });
    }


    //Firebase Email Authentication
    public void performAuth(String email, String password) {
        dialog.show();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
//                            database.goOnline();
                            flag = false;
                            binding.etEmail.setText("");
                            binding.etPass.setText("");
                            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                            editor.putString("email", email);
                            editor.putString("password", password);
                            editor.putBoolean("isLogin", true);
                            editor.putBoolean("isGoogle", flag);
                            editor.apply();
                            HashMap<String,Object> map= new HashMap<>();
                            map.put("password",password);
                            FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getUid()).updateChildren(map);

//                            database.getReference().child("Users").child(user.getUid()).setValue(users);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(SignInActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public boolean emailValidation() {
        String email = binding.editEmail.getEditText().getText().toString().trim();
        if (email.isEmpty()) {
            binding.editEmail.setErrorEnabled(true);
            binding.editEmail.setError("Field cannot be empty");
            binding.editEmail.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
            binding.editEmail.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.setErrorEnabled(true);
            binding.editEmail.setError("Please provide a valid email");
            binding.editEmail.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
            binding.editEmail.requestFocus();
            return false;
        }
        else {
            binding.editEmail.setErrorEnabled(false);
            binding.editEmail.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            binding.editEmail.clearFocus();
            return true;
        }
    }

    public boolean passValidation() {
        String pass = binding.editPass.getEditText().getText().toString().trim();
        if (pass.isEmpty()) {
            binding.editPass.setErrorEnabled(true);
            binding.editPass.setError("Field cannot be empty");
            binding.editPass.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
            binding.editPass.requestFocus();
            return false;
        } else {
            binding.editPass.setErrorEnabled(false);
            binding.editPass.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
            binding.editPass.clearFocus();
            return true;
        }
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(SignInActivity.this);
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

    public void hideSoftKeyboard(Activity activity){
        View view=this.getCurrentFocus();
        if (view!=null){
            InputMethodManager imm =(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


}