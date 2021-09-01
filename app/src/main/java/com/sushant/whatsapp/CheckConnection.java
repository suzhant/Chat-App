package com.sushant.whatsapp;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.constraintlayout.motion.widget.Debug;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;


public class CheckConnection {
    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());

//        if (connectivityManager != null) {
//            NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
//            if (infos != null) {
//                for (NetworkInfo info : infos) {
//                    if (info.getState() == NetworkInfo.State.CONNECTED) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//        NetworkInfo wifiCon=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        NetworkInfo mobileCon=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//
//        return (wifiCon != null && wifiCon.isConnected()) || (mobileCon != null && mobileCon.isConnected());
    }

    public boolean isInternet() {

        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }
}

