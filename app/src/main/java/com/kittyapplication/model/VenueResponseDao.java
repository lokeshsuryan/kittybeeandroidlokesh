package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dhaval Riontech on 12/8/16.
 */
public class VenueResponseDao {
    private String id;
    private String groupID;
    private String kittyId;
    private String vanue;
    private String address;

    @SerializedName("lat")
    private int latitude;

    @SerializedName("lng")
    private int longitude;

    @SerializedName("kitty_date")
    private String kittyDate;
    private String venueTime;
    private String punctuality;
    private String punctuality2;
    private String dressCode;
    private String note;

    @SerializedName("kitty_id")
    private String reqKittyId;

    @SerializedName("kitty_time")
    private String reqKittyTime;

    @SerializedName("user_id")
    private String reqUserId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getKittyId() {
        return kittyId;
    }

    public void setKittyId(String kittyId) {
        this.kittyId = kittyId;
    }

    public String getVanue() {
        return vanue;
    }

    public void setVanue(String vanue) {
        this.vanue = vanue;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public String getKittyDate() {
        return kittyDate;
    }

    public void setKittyDate(String kittyDate) {
        this.kittyDate = kittyDate;
    }

    public String getVenueTime() {
        return venueTime;
    }

    public void setVenueTime(String venueTime) {
        this.venueTime = venueTime;
    }

    public String getPunctuality() {
        return punctuality;
    }

    public void setPunctuality(String punctuality) {
        this.punctuality = punctuality;
    }

    public String getPunctuality2() {
        return punctuality2;
    }

    public void setPunctuality2(String punctuality2) {
        this.punctuality2 = punctuality2;
    }

    public String getDressCode() {
        return dressCode;
    }

    public void setDressCode(String dressCode) {
        this.dressCode = dressCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getReqKittyId() {
        return reqKittyId;
    }

    public void setReqKittyId(String reqKittyId) {
        this.reqKittyId = reqKittyId;
    }

    public String getReqKittyTime() {
        return reqKittyTime;
    }

    public void setReqKittyTime(String reqKittyTime) {
        this.reqKittyTime = reqKittyTime;
    }

    public String getReqUserId() {
        return reqUserId;
    }

    public void setReqUserId(String reqUserId) {
        this.reqUserId = reqUserId;
    }
}
