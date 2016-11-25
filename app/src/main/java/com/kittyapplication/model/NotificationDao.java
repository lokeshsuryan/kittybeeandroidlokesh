package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Scorpion on 08-08-2016.
 */
public class NotificationDao {
    private String message;
    private String id;

    @SerializedName("notifyTime")
    private String notificationTime;

    @SerializedName("groupImg")
    private String groupImage;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("venue_id")
    private String venueId;

    @SerializedName("user_id")
    private String userId;
    private String type;

    @SerializedName("kitty_id")
    private String kittyId;

    @SerializedName("group_name")
    private String groupName;

    private List<String> hostName;

    private List<String> hostProfileName;

    private List<String> hostnumber;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKittyId() {
        return kittyId;
    }

    public void setKittyId(String kittyId) {
        this.kittyId = kittyId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getHostName() {
        return hostName;
    }

    public void setHostName(List<String> hostName) {
        this.hostName = hostName;
    }

    public List<String> getHostProfileName() {
        return hostProfileName;
    }

    public void setHostProfileName(List<String> hostProfileName) {
        this.hostProfileName = hostProfileName;
    }

    public List<String> getHostnumber() {
        return hostnumber;
    }

    public void setHostnumber(List<String> hostnumber) {
        this.hostnumber = hostnumber;
    }
}
