package com.sushant.whatsapp.Models;

public class Messages {
    private String uId,message, messageId,profilePic,senderId,receiverId,senderName,receiverName,type,imageUrl,audioFile;
    private Long timestamp;

    public Messages(String uId, String profilePic, Long timestamp) {
        this.uId = uId;
        this.profilePic = profilePic;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }
}
