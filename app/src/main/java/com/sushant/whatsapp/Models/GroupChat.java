package com.sushant.whatsapp.Models;

import java.io.Serializable;

public class GroupChat implements Serializable {
    private String groupId,groupName,groupPP;

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

    public String getGroupPP() {
        return groupPP;
    }

    public void setGroupPP(String groupPP) {
        this.groupPP = groupPP;
    }
}
