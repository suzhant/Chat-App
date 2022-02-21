package com.sushant.whatsapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;


public class InternetCheckServices extends BroadcastReceiver {
    boolean isOnline;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Dialog dialog = new Dialog(context, android.R.style.Theme_NoTitleBar);
        dialog.setContentView(R.layout.no_connection);
        Button retry = dialog.findViewById(R.id.btnRetry);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("No Connection").setMessage("You are not Connected to Internet");
        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        try {
            CheckConnection checkConnection = new CheckConnection();
            isOnline = !checkConnection.isConnected(context) || checkConnection.isInternet();
                if (!isOnline) {
                    AlertDialog dialog1 = builder.create();
                    dialog1.show();
                }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}

