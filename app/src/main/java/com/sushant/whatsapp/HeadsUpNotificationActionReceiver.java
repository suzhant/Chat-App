package com.sushant.whatsapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sushant.whatsapp.Models.Messages;
import com.sushant.whatsapp.Utils.Encryption;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class HeadsUpNotificationActionReceiver extends BroadcastReceiver {
    String senderId, receiverName, profilePic, key;
    FirebaseDatabase database;
    FirebaseAuth auth;
    Ringtone r;
    String senderRoom, receiverRoom;

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

            String path = "android.resource://" + context.getPackageName() + "/" + R.raw.google_notification;
            r = RingtoneManager.getRingtone(context, Uri.parse(path));

            senderRoom = auth.getUid() + senderId;
            receiverRoom = senderId + auth.getUid();

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
            sendMessage();
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

    private void sendMessage() {
        String key = database.getReference().push().getKey();
        String message = "You missed a Video Call.";
        String encryptMessage = Encryption.encryptMessage(message);
        final Messages model = new Messages(senderId, encryptMessage, profilePic);
        Date date = new Date();
        model.setTimestamp(date.getTime());
        model.setType("videoCall");
        updateLastMessage(message);

        assert key != null;
        database.getReference().child("Chats").child(receiverRoom).child(key).setValue(model)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("Chats").child(senderRoom).child(key).setValue(model);
                    }
                });

    }

    private void updateLastMessage(String message) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("lastMessage", message);
        database.getReference().child("Users").child(senderId).child("Friends").child(auth.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                database.getReference().child("Users").child(auth.getUid()).child("Friends").child(senderId).updateChildren(map);
            }
        });
    }

}
