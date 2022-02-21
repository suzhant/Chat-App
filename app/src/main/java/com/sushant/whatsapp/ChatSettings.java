package com.sushant.whatsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.databinding.ActivityChatSettingsBinding;

public class ChatSettings extends AppCompatActivity {
    ActivityChatSettingsBinding binding;
    AlertDialog.Builder dialog;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String senderId, receiverId, senderRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // getSupportActionBar().hide();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chat Settings");
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#7C4DFF"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        senderId = auth.getUid();
        receiverId = getIntent().getStringExtra("UserId");
        senderRoom = senderId + receiverId;

        dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Are you sure?")
                .setTitle("Delete Conversation")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        database.getReference().child("Chats").child(senderRoom).removeValue();
                        Intent intent = new Intent(ChatSettings.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        binding.txtDeleteConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}