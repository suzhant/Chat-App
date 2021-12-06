package com.sushant.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sushant.whatsapp.Adapters.ChatAdapter;
import com.sushant.whatsapp.Fragments.GroupChatFragment;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Models.Users;
import com.sushant.whatsapp.databinding.ActivityGroupChatBinding;

import java.util.ArrayList;
import java.util.Date;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    Animation scale_up, scale_down;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        String Gid=getIntent().getStringExtra("GId");
        String GPP=getIntent().getStringExtra("GPic");
        String Gname=getIntent().getStringExtra("GName");

        binding.groupName.setText(Gname);
        Glide.with(this).load(GPP).placeholder(R.drawable.avatar).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.profileImage);

        scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        scale_up = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        auth=FirebaseAuth.getInstance();


        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ArrayList<Messages> messageModel = new ArrayList<>();

        final String senderId = FirebaseAuth.getInstance().getUid();

        final ChatAdapter chatAdapter = new ChatAdapter(messageModel,  this);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        database.getReference().child("Group Chat")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModel.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Messages model = dataSnapshot.getValue(Messages.class);
                            messageModel.add(model);
                        }
                        chatAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.icSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.icSend.startAnimation(scale_down);
                binding.icSend.startAnimation(scale_up);
                final String message = binding.editMessage.getText().toString();
                if (!message.isEmpty()) {
                    final Messages model = new Messages(senderId, message);
                    model.setTimestamp(new Date().getTime());
                    binding.editMessage.setText("");

                    database.getReference().child("Group Chat").push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });
                } else {
                    binding.editMessage.setError("Message cannot be empty!");
                }
            }
        });


    }
}