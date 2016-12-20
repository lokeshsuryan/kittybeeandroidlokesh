package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Pintu Riontech on 19/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ReqDeleteMember {

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("kitty_id")
    private String kittyId;

    @SerializedName("member_id")
    private List<String> memberId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getKittyId() {
        return kittyId;
    }

    public void setKittyId(String kittyId) {
        this.kittyId = kittyId;
    }

    public List<String> getMemberId() {
        return memberId;
    }

    public void setMemberId(List<String> memberId) {
        this.memberId = memberId;
    }
}
