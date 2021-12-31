package com.sushant.whatsapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.sushant.whatsapp.Models.Messages;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender  {

    String userFcmToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;
    String avatar,receiverId,email,status,senderId;


    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey ="AAAAxHlg9ro:APA91bFbKkDxD4PreTLlBA4j7fjirCtQpgg6CU3iYVlZBH5n9T-T03pd8_COhbrP3kapFdrNcW5uotRHY-efoJGfbJruLVNC2zA7WFpzjKdMVJ2_JEsUbpb4F_rBViaA5PqS0cZWSr3h";

    public FcmNotificationsSender(String userFcmToken, String title, String body,String avatar,String receiverId,String email,String status,String senderId, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.avatar=avatar;
        this.receiverId=receiverId;
        this.email=email;
        this.status=status;
        this.senderId=senderId;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public FcmNotificationsSender(String userFcmToken, String title, String body,String avatar,Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.avatar=avatar;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public void SendNotifications() {

        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);
            notiObject.put("image",avatar);
            notiObject.put("click_action",".ChatDetailsActivity");
            notiObject.put("icon", R.drawable.ic_circle_notifications); // enter icon that exists in drawable only
            JSONObject dataObject = new JSONObject();
            dataObject.put("UserId",senderId);
            dataObject.put("ProfilePic",avatar);
            dataObject.put("userEmail",email);
            dataObject.put("UserStatus",status);
            dataObject.put("UserName",title);
            mainObj.put("notification", notiObject);
            mainObj.put("data",dataObject);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    // code run is got response

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // code run is got error

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {


                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;


                }
            };
            requestQueue.add(request);


        } catch (JSONException e) {
            e.printStackTrace();
        }




    }
}
