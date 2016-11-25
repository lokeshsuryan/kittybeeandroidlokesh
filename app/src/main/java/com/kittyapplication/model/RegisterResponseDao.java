package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dhaval Riontech on 8/8/16.
 */
public class RegisterResponseDao {
    private int response;
    private String status;
    private String message;
    private String userID;
    private String name;
    private String profilePic;
    private String phone;

    private String quickID;

    @SerializedName("quicklogin")
    private String quickLogin;

    @SerializedName("quickfull_name")
    private String fullName;

    public int getResponse() {
        return response;
    }

    public void setResponse(int response) {
        this.response = response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getQuickID() {
        return quickID;
    }

    public void setQuickID(String quickID) {
        this.quickID = quickID;
    }

    public String getQuickLogin() {
        return quickLogin;
    }

    public void setQuickLogin(String quickLogin) {
        this.quickLogin = quickLogin;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
