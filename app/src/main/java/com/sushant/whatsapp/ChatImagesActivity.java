package com.sushant.whatsapp;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.ChatImagePreviewAdapter;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.databinding.ActivityChatImagesBinding;

import java.util.ArrayList;

public class ChatImagesActivity extends AppCompatActivity {

    ActivityChatImagesBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String receiverId;
    DatabaseReference imgRef;
    ValueEventListener imgListener;
    String senderRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatImagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Chat Images");
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#7C4DFF"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        receiverId = getIntent().getStringExtra("UserId");
        senderRoom = auth.getUid() + receiverId;

        final ArrayList<Messages> imageModel = new ArrayList<>();
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        binding.chatImageRecycler.setLayoutManager(staggeredGridLayoutManager);
        binding.chatImageRecycler.setHasFixedSize(true);
        final ChatImagePreviewAdapter imageAdapter = new ChatImagePreviewAdapter(imageModel, this, receiverId);
        binding.chatImageRecycler.setAdapter(imageAdapter);

        imgRef = database.getReference().child("Chats").child(senderRoom);
        imgListener = new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageModel.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Messages model = dataSnapshot.getValue(Messages.class);
                    assert model != null;
                    if (model.getImageUrl() != null) {
                        imageModel.add(model);
                    }
                }
                imageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        imgRef.addListenerForSingleValueEvent(imgListener);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}