package com.riontech.socialconnection.model;

/**
 * Created by Pintu Riontech on 28/7/16.
 * vaghela.pintu31@gmail.com
 */
public class SocialUserResponse {
    private String userName;
    private String userId;
    private String userGender;
    private String userEmail;
    private String userAccessToken;
    private String userLocation;
    private String mImgUrl;
    private int mSocialType;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public String getmImgUrl() {
        return mImgUrl;
    }

    public void setImgUrl(String mImgUrl) {
        this.mImgUrl = mImgUrl;
    }

    public int getSocialType() {
        return mSocialType;
    }

    public void setSocialType(int socialType) {
        this.mSocialType = socialType;
    }
}
