package com.sushant.whatsapp.Models;

import java.io.Serializable;
import java.util.Objects;

public class Users implements Serializable {
    private String profilePic, userName, mail, password, lastMessage, status, request, Typing, role, joinedGroupOn, seen, nickName, lastStory;
    public String userId;
    private int storiesCount = 0, seenCount = 0, unseenCount = 0;

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

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Users users = (Users) o;
        return Objects.equals(profilePic, users.profilePic) && Objects.equals(userName, users.userName) && Objects.equals(mail, users.mail)
                && Objects.equals(password, users.password) && Objects.equals(lastMessage, users.lastMessage) && Objects.equals(status, users.status)
                && Objects.equals(request, users.request) && Objects.equals(Typing, users.Typing) && Objects.equals(role, users.role)
                && Objects.equals(joinedGroupOn, users.joinedGroupOn) && Objects.equals(seen, users.seen) && Objects.equals(nickName, users.nickName)
                && Objects.equals(userId, users.userId);
    }

    public int getStoriesCount() {
        return storiesCount;
    }

    public void setStoriesCount(int storiesCount) {
        this.storiesCount = storiesCount;
    }

    public String getLastStory() {
        return lastStory;
    }

    public void setLastStory(String lastStory) {
        this.lastStory = lastStory;
    }

    public int getSeenCount() {
        return seenCount;
    }

    public void setSeenCount(int seenCount) {
        this.seenCount = seenCount;
    }

    public int getUnseenCount() {
        return unseenCount;
    }

    public void setUnseenCount(int unseenCount) {
        this.unseenCount = unseenCount;
    }
}
