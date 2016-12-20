package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Pintu Riontech on 19/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ReqAddMember {

    @SerializedName("group_id")
    private String groupId;
    private List<MemberData> member;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public List<MemberData> getMember() {
        return member;
    }

    public void setMember(List<MemberData> member) {
        this.member = member;
    }
}
