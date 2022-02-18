package com.sushant.whatsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.sushant.whatsapp.databinding.ActivityForgotPasswordBinding;

import java.util.Objects;

public class ForgotPassword extends AppCompatActivity {

    ActivityForgotPasswordBinding binding;
    FirebaseAuth auth;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        binding.getRoot().setFocusable(true);
        binding.getRoot().setFocusableInTouchMode(true);

        broadcastReceiver = new InternetCheckServices();
        registerBroadcastReceiver();

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard();
            }
        });

        binding.btnResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckConnection checkConnection = new CheckConnection();
                if (checkConnection.isConnected(getApplicationContext())) {
                    showCustomDialog();
                    return;
                }
                String email = binding.editEmail.getEditText().getText().toString().trim();
                if (email.isEmpty()) {
                    binding.editEmail.setErrorEnabled(true);
                    binding.editEmail.setError("Email is empty");
                    binding.editEmail.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.design_default_color_error)));
                    binding.editEmail.requestFocus();
                    showSoftKeyboard();
                    return;
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.editEmail.setErrorEnabled(true);
                    binding.editEmail.setError("Please provide a valid email");
                    binding.editEmail.requestFocus();
                    showSoftKeyboard();
                    return;
                }
                auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            hideSoftKeyboard();
                            Objects.requireNonNull(binding.etEmail.getText()).clear();
                            binding.editEmail.setErrorEnabled(false);
                            binding.editEmail.setStartIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimaryDark)));
                            binding.editEmail.clearFocus();
                            hideSoftKeyboard();
                            Toast.makeText(getApplicationContext(), "Check your email", Toast.LENGTH_SHORT).show();
                        } else {
                            binding.editEmail.setErrorEnabled(true);
                            binding.editEmail.setError("Email doesn't exist");
                            binding.editEmail.requestFocus();
                        }
                    }
                });

            }
        });


    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPassword.this);
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

    public void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void registerBroadcastReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

    }

    private void unregisterNetwork() {
        try {
            unregisterReceiver(broadcastReceiver);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetwork();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetwork();
    }
}