package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Dhaval Riontech on 15/8/16.
 */
public class DiarySubmitDao {
    @SerializedName("KittyId")
    private String kittyId;

    @SerializedName("get_punctuality")
    private String punctuality;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("members")
    private List<MembersDaoCalendar> memberCheckList;

    public String getKittyId() {
        return kittyId;
    }

    public void setKittyId(String kittyId) {
        this.kittyId = kittyId;
    }

    public String getPunctuality() {
        return punctuality;
    }

    public void setPunctuality(String punctuality) {
        this.punctuality = punctuality;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<MembersDaoCalendar> getMemberCheckList() {
        return memberCheckList;
    }

    public void setMemberCheckList(List<MembersDaoCalendar> memberCheckList) {
        this.memberCheckList = memberCheckList;
    }
}
