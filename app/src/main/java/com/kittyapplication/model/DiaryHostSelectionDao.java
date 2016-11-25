package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Dhaval Riontech on 16/8/16.
 */
public class DiaryHostSelectionDao {
    @SerializedName("hosts")
    private String noOfHosts;

    @SerializedName("kitty_date")
    private String kittyDate;

    @SerializedName("KittyId")
    private String KittyId;

    private String groupId;

    @SerializedName("user_id")
    private String userId;


    private List<SummaryListDao> member;

    public String getNoOfHosts() {
        return noOfHosts;
    }

    public void setNoOfHosts(String noOfHosts) {
        this.noOfHosts = noOfHosts;
    }

    public String getKittyDate() {
        return kittyDate;
    }

    public void setKittyDate(String kittyDate) {
        this.kittyDate = kittyDate;
    }

    public String getKittyId() {
        return KittyId;
    }

    public void setKittyId(String kittyId) {
        KittyId = kittyId;
    }

    public List<SummaryListDao> getMember() {
        return member;
    }

    public void setMember(List<SummaryListDao> member) {
        this.member = member;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
