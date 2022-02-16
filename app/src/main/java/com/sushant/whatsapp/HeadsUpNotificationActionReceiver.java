package com.sushant.whatsapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class HeadsUpNotificationActionReceiver extends BroadcastReceiver {
    String senderId, receiverName, profilePic, key;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            String action = intent.getStringExtra("ACTION");
            senderId = intent.getStringExtra("UserId");
            profilePic = intent.getStringExtra("ProfilePic");
            receiverName = intent.getStringExtra("UserName");
//             receiverId=intent.getStringExtra("receiverId");
            key = intent.getStringExtra("key");

            database = FirebaseDatabase.getInstance();
            auth = FirebaseAuth.getInstance();


            if (action != null) {
                performClickAction(context, action);
            }

            // Close the notification after the click action is performed.

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
//            context.stopService(new Intent(context, FirebaseMessagingService.class));
        }
    }

    private void performClickAction(Context context, String action) {
        if (action.equals("RECEIVE_CALL")) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(101);
            Intent openIntent = null;
            openIntent = new Intent(context, OutGoingCall.class);
            openIntent.putExtra("key", key);
            openIntent.putExtra("UserName", receiverName);
            openIntent.putExtra("ProfilePic", profilePic);
            openIntent.putExtra("UserId", senderId);
            openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openIntent);
        } else if (action.equals("CANCEL_CALL")) {
            updateDb();
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(101);
//            context.stopService(new Intent(context, FirebaseMessagingService.class));
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }
    }

    private void updateDb() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("onCall", "false");
        map.put("response", "reject");
        map.put("key", key);
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("VideoCall").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(senderId).child("VideoCall").updateChildren(map);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(auth.getUid()).child("VideoCall").removeValue();
                database.getReference().child("Users").child(senderId).child("VideoCall").removeValue();
            }
        });
    }
}
