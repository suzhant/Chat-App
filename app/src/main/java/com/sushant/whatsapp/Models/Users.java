package com.sushant.whatsapp.Models;

import java.io.Serializable;

public class Users implements Serializable {
    private String profilePic,userName,mail,password,userId,lastMessage,status,request,Typing,role,joinedGroupOn;

    public Users(String profilePic, String userName, String mail, String password, String userId, String lastMessage, String status) {
        this.profilePic = profilePic;
        this.userName = userName;
        this.mail = mail;
        this.password = password;
        this.userId = userId;
        this.lastMessage = lastMessage;
        this.status = status;
    }

    public Users() {
    }

    //signUp constructor
    public Users(String userName, String mail, String password) {
        this.userName = userName;
        this.mail = mail;
        this.password = password;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUserId(String key) {
        return userId;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTyping() {
        return Typing;
    }

    public void setTyping(String typing) {
        Typing = typing;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getJoinedGroupOn() {
        return joinedGroupOn;
    }

    public void setJoinedGroupOn(String joinedGroupOn) {
        this.joinedGroupOn = joinedGroupOn;
    }
}
