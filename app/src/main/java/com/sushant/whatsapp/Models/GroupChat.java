package com.sushant.whatsapp.Models;

import java.util.ArrayList;

public class GroupChat {
    private String groupId,groupName;
    private ArrayList<Users> participant;

    public GroupChat() {
    }
    public GroupChat(String groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<Users> getParticipant() {
        return participant;
    }

    public void setParticipant(ArrayList<Users> participant) {
        this.participant = participant;
    }
}
