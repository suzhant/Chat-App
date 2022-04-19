package com.sushant.whatsapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class FriendRequestActionReceiver extends BroadcastReceiver {

    String senderId;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            String action = intent.getStringExtra("ACTION");
            senderId = intent.getStringExtra("UserId");

            database = FirebaseDatabase.getInstance();
            auth = FirebaseAuth.getInstance();

            if (action != null) {
                performClickAction(context, action);
            }

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }
    }

    private void performClickAction(Context context, String action) {
        if (action.equals("ACCEPT_FRIEND")) {
            accept();
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(100);
        } else if (action.equals("REJECT_FRIEND")) {
            reject();
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(100);
        }
    }

    private void accept() {
        HashMap<String, Object> obj1 = new HashMap<>();
        obj1.put("request", "Accepted");

        HashMap<String, Object> obj2 = new HashMap<>();
        obj2.put("request", "Accepted");

        database.getReference().child("Users").child(auth.getUid()).child("Friends").child(senderId).updateChildren(obj1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(senderId).child("Friends").child(auth.getUid()).updateChildren(obj2);
            }
        });
    }

    private void reject() {
        database.getReference().child("Users").child(auth.getUid()).child("Friends").child(senderId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(senderId).child("Friends").child(auth.getUid()).removeValue();
            }
        });
    }
}