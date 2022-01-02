package com.sushant.whatsapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;;import com.google.android.material.snackbar.Snackbar;


public class InternetCheckServices extends BroadcastReceiver {
    boolean isOnline;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Dialog dialog= new Dialog(context, android.R.style.Theme_NoTitleBar);
        dialog.setContentView(R.layout.no_connection);
        Button retry=dialog.findViewById(R.id.btnRetry);
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
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
            CheckConnection checkConnection= new CheckConnection();
            //                dialog.show();
            isOnline= !checkConnection.isConnected(context) || checkConnection.isInternet();
            if (!isOnline){
                AlertDialog dialog1 = builder.create();
                dialog1.show();
            }
//            retry.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (!isOnline){
//                        Toast.makeText(context.getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
//                    }
//                    else {
////                        ((Activity)context).finish();
////                        Intent intent1= new Intent(context,context.getClass());
////                        context.startActivity(intent1);
////                        dialog.dismiss();
//                    }
//                }
//            });
      }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}

