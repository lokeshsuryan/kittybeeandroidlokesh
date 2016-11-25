package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Scorpion on 07-08-2016.
 */
public class OTPResponseDao {
    private String message;
    private int response;

    @SerializedName("quickfull_name")
    private String mFullname;
    private String userID;
    private String phone;

    @SerializedName("quicklogin")
    private String qblogin;
    private String status;

    @SerializedName("quickID")
    private String qbId;

    private String name;
    private String device;
    private String profilePic;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getResponse() {
        return response;
    }

    public void setResponse(int response) {
        this.response = response;
    }

    public String getFullname() {
        return mFullname;
    }

    public void setFullname(String mFullname) {
        this.mFullname = mFullname;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getQbLogin() {
        return qblogin;
    }

    public void setQbLogin(String qbLogin) {
        this.qblogin = qbLogin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getQbId() {
        return qbId;
    }

    public void setQbId(String qbId) {
        this.qbId = qbId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
