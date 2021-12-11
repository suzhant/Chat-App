package com.sushant.whatsapp.Models;

import java.io.Serializable;

public class Groups implements Serializable {
    private String groupId,groupName,groupPP,createdOn,createdBy;

    public Groups() {
    }
    public Groups(String groupId, String groupName) {
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

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
