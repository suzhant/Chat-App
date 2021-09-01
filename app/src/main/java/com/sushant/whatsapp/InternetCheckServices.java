package com.sushant.whatsapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.RequiresApi;;import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class InternetCheckServices extends BroadcastReceiver {
    boolean isOnline;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        Dialog dialog= new Dialog(context, android.R.style.Theme_NoTitleBar);
        dialog.setContentView(R.layout.wifi_disconnect);
        Button retry=dialog.findViewById(R.id.btnRetry);

        try {
            CheckConnection checkConnection= new CheckConnection();
            if (!checkConnection.isConnected(context) && !checkConnection.isInternet()){
                isOnline=false;
                Toast.makeText(context.getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                dialog.show();
        }else {
                isOnline=true;
            }
            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isOnline){
                        Toast.makeText(context.getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                    else {
//                        ((Activity)context).finish();
//                        Intent intent1= new Intent(context,context.getClass());
//                        context.startActivity(intent1);
                        dialog.dismiss();
                    }
                }
            });
      }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}

