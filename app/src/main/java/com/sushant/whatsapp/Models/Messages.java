package com.sushant.whatsapp.Models;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class Messages {
    private String uId,message, messageId,profilePic,senderId,receiverId,senderName,receiverName,type;
    private Long timestamp;

    public Messages(String uId, String message, Long timestamp) {
        this.uId = uId;
        this.message = message;
        this.timestamp = timestamp;
    }
    public Messages(String uId, String message) {
        this.uId = uId;
        this.message = message;
    }


    public Messages() {
    }

    public Messages(String uId, String message, String profilePic) {
        this.uId = uId;
        this.message = message;
        this.profilePic = profilePic;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
