package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dhaval Riontech on 16/8/16.
 */
public class CalendarDao {
    @SerializedName("groupid")
    private int groupid;

    @SerializedName("group_name")
    private String groupName;

    @SerializedName("kitty_date")
    private String kittyDate;

    @SerializedName("kitty_time")
    private String kittyTime;

    @SerializedName("kitty_vanue")
    private String kittyVanue;

    public int getGroupid() {
        return groupid;
    }

    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getKittyDate() {
        return kittyDate;
    }

    public void setKittyDate(String kittyDate) {
        this.kittyDate = kittyDate;
    }

    public String getKittyTime() {
        return kittyTime;
    }

    public void setKittyTime(String kittyTime) {
        this.kittyTime = kittyTime;
    }

    public String getKittyVanue() {
        return kittyVanue;
    }

    public void setKittyVanue(String kittyVanue) {
        this.kittyVanue = kittyVanue;
    }
}
