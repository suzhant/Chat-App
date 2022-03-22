package com.sushant.whatsapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.databinding.ActivityChatSettingsBinding;

import java.util.HashMap;
import java.util.Objects;

public class ChatSettings extends AppCompatActivity {
    ActivityChatSettingsBinding binding;
    AlertDialog.Builder dialog;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String senderId, receiverId, senderRoom, receiverName, receiverPP, senderNickName;
    private String m_Text = "";

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
        receiverName = getIntent().getStringExtra("UserName");
        receiverPP = getIntent().getStringExtra("ProfilePic");
        senderNickName = getIntent().getStringExtra("SenderNickName");
        senderRoom = senderId + receiverId;
        if (senderNickName == null) {
            senderNickName = "none";
        }

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

        binding.txtImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatSettings.this, ChatImagesActivity.class);
                intent.putExtra("UserId", receiverId);
                startActivity(intent);
            }
        });

        binding.txtNickName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent= new Intent(ChatSettings.this,NickName.class);
//                intent.putExtra("UserId",receiverId);
//                intent.putExtra("UserName",receiverName);
//                intent.putExtra("ProfilePic",receiverPP);
//                startActivity(intent);
                showDialog();
            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatSettings.this, R.style.AlertDialogCustom);
        builder.setMessage("My NickName : " + senderNickName + "\n" +
                "Friend's NickName : " + receiverName);

        View viewInflated = LayoutInflater.from(ChatSettings.this).inflate(R.layout.nickname_dialog_box, findViewById(android.R.id.content), false);
        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        input.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) ChatSettings.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                showKeyboard(input, inputMethodManager);
            }
        }, 100);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                if (m_Text.isEmpty()) {
                    Toast.makeText(ChatSettings.this, "Field is Empty!! Try Again.", Toast.LENGTH_SHORT).show();
                    return;
                }
                setNickName(m_Text);
                Toast.makeText(ChatSettings.this, "Created NickName Successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteNickName();
                Toast.makeText(ChatSettings.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        // Title
        TextView titleView = new TextView(ChatSettings.this);
        titleView.setText("NickName");
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(20, 20, 20, 20);
        titleView.setTextSize(20F);
        titleView.setTypeface(Typeface.DEFAULT_BOLD);
        titleView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPurple));
        titleView.setTextColor(ContextCompat.getColor(this, R.color.white));

        AlertDialog dialog = builder.create();
        dialog.setCustomTitle(titleView);
        dialog.show();

        //buttons
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPurple));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPurple));
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this, R.color.red));


        //message
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setGravity(Gravity.START);
            messageView.setTextColor(Color.BLACK);
            messageView.setTypeface(Typeface.SANS_SERIF);
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                InputMethodManager inputMethodManager = (InputMethodManager) ChatSettings.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                hideKeyboard(input, inputMethodManager);
            }
        });
    }

    private void showKeyboard(EditText input, InputMethodManager inputMethodManager) {
        input.requestFocus();
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    private void hideKeyboard(EditText input, InputMethodManager inputMethodManager) {
        input.clearFocus();
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private void setNickName(String nickname) {
        DatabaseReference databaseReference = database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("Friends").child(receiverId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("nickName", nickname);
        databaseReference.updateChildren(hashMap);
    }

    private void deleteNickName() {
        DatabaseReference databaseReference = database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("Friends").child(receiverId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("nickName", null);
        databaseReference.updateChildren(hashMap);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}