package com.sushant.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.sushant.whatsapp.databinding.ActivityPrivacyBinding;

public class Privacy extends AppCompatActivity {

    ActivityPrivacyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPrivacyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Privacy");
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#7C4DFF"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        binding.btnUpdatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Privacy.this,ChangePassword.class);
                startActivity(intent);
            }
        });

        binding.btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Privacy.this,ChangeEmail.class);
                startActivity(intent);
            }
        });

        binding.btnDeleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent= new Intent(Privacy.this, DeleteAccount.class);
                    startActivity(intent);
            }
        });
    }
}