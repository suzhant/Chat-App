package com.sushant.whatsapp;

import android.app.Activity;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSender  {

    String userFcmToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;
    String avatar,receiverId,email,senderId,msgType,Gid,Type,Notification,click_action;


    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey ="AAAAxHlg9ro:APA91bFbKkDxD4PreTLlBA4j7fjirCtQpgg6CU3iYVlZBH5n9T-T03pd8_COhbrP3kapFdrNcW5uotRHY-efoJGfbJruLVNC2zA7WFpzjKdMVJ2_JEsUbpb4F_rBViaA5PqS0cZWSr3h";

    public FcmNotificationsSender(String userFcmToken, String title, String body,String avatar,String receiverId,String email,String senderId,String msgType,String Type,String click_action, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.avatar=avatar;
        this.receiverId=receiverId;
        this.email=email;
        this.senderId=senderId;
        this.msgType=msgType;
        this.Type=Type;
        this.click_action=click_action;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public FcmNotificationsSender(String userFcmToken, String title, String body, String avatar, String Gid, String misType, String Type, String Notification,String click_action, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.avatar=avatar;
        this.Gid=Gid;
        this.msgType=misType;
        this.Type=Type;
        this.Notification=Notification;
        this.click_action=click_action;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public FcmNotificationsSender(String userFcmToken, String title, String body,String avatar,String msgType,String Type,String Notification,String click_action, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.avatar=avatar;
        this.msgType=msgType;
        this.Type=Type;
        this.Notification=Notification;
        this.click_action=click_action;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public void SendNotifications() {

        RequestQueue requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
//            JSONObject notiObject = new JSONObject();
//            notiObject.put("title", title);
//            notiObject.put("body", body);
//            notiObject.put("image",avatar);
//            notiObject.put("click_action",".ChatDetailsActivity");
//            notiObject.put("icon", R.drawable.ic_circle_notifications); // enter icon that exists in drawable only
            JSONObject dataObject = new JSONObject();
            dataObject.put("Type",Type);
            dataObject.put("GId",Gid);
            dataObject.put("title",title);
            dataObject.put("UserId",senderId);
            dataObject.put("message",body);
            dataObject.put("ProfilePic",avatar);
            dataObject.put("userEmail",email);
            dataObject.put("UserName",title);
            dataObject.put("msgType",msgType);
            dataObject.put("Notification",Notification);
            dataObject.put("click_action",click_action);
            dataObject.put("icon",R.drawable.ic_circle_notifications);
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
