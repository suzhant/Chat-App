package com.sushant.whatsapp;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.databinding.ActivityNickNameBinding;

import java.util.HashMap;
import java.util.Objects;

public class NickName extends AppCompatActivity {
    ActivityNickNameBinding binding;
    private String m_Text = "";
    FirebaseAuth auth;
    FirebaseDatabase database;
    String receiverId, receiverName, receiverPP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNickNameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("NickNames");
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#7C4DFF"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        receiverId = getIntent().getStringExtra("UserId");
        receiverName = getIntent().getStringExtra("UserName");
        receiverPP = getIntent().getStringExtra("ProfilePic");


        DatabaseReference databaseReference = database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("Friends").child(receiverId);

    }

    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(NickName.this, R.style.AlertDialogCustom);
        builder.setTitle("Nickname");

        View viewInflated = LayoutInflater.from(NickName.this).inflate(R.layout.nickname_dialog_box, findViewById(android.R.id.content), false);

        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        input.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) NickName.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                showKeyboard(input, inputMethodManager);
            }
        }, 100);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                m_Text = input.getText().toString();
                setNickName(m_Text);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                InputMethodManager inputMethodManager = (InputMethodManager) NickName.this.getSystemService(Context.INPUT_METHOD_SERVICE);
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
}