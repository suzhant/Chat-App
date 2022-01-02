package com.sushant.whatsapp;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class FirebaseOffline extends Application {

    FirebaseDatabase database;
    ValueEventListener eventListener;
    DatabaseReference infoConnected;
    FirebaseAuth auth;
    @Override
    public void onCreate() {
        super.onCreate();
        database=FirebaseDatabase.getInstance();
        auth=FirebaseAuth.getInstance();
        if (auth.getCurrentUser()!=null){
            database.setPersistenceEnabled(true);
            database.goOnline();
            manageConnection();
        }

    }

    private void manageConnection() {
            final DatabaseReference status = database.getReference().child("Users").child((auth.getUid())).child("Connection").child("Status");
            final DatabaseReference lastOnlineRef = database.getReference().child("Users").child(auth.getUid()).child("Connection").child("lastOnline");
            infoConnected = database.getReference(".info/connected");

            eventListener=infoConnected.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        status.setValue("online");
                        lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                    }else{
                        status.onDisconnect().setValue("offline");
                        lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);
        if (infoConnected!=null){
            infoConnected.removeEventListener(eventListener);
        }
    }

}
