package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;

/**
 * Created by Scorpion on 07-08-2016.
 */
public class ContactDao {
    private String phone = "";
    private String status = "";
    private String name = "";
    private String ID = "";

    @SerializedName("userid")
    private String userId = "";
    private String image = "";

    @SerializedName("registeration")
    private String registration = "0";
    private String login = "";

    @SerializedName("full_name")
    private String fullName = "";

    @SerializedName("member_id")
    private String memberId = "";

    @SerializedName("mamber_id")
    private String mamberID = "";

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @SerializedName("is_host")
    private String isHost = "";

    public String getIsHost() {
        return isHost;
    }

    public void setIsHost(String is_host) {
        this.isHost = is_host;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String kids = "";

    public String getKids() {
        return kids;
    }

    public void setKids(String kids) {
        this.kids = kids;
    }

    //@Expose(serialize = false, deserialize = false)
    @Since(2.0)
    private boolean isAlready = false;

    public boolean isAlready() {
        return isAlready;
    }

    //@Expose(serialize = false, deserialize = false)
    @Since(2.0)
    private String is_Paid = "";

    public void setAlready(boolean already) {
        isAlready = already;
    }

    public String getIs_Paid() {
        return is_Paid;
    }

    public void setIs_Paid(String is_Paid) {
        this.is_Paid = is_Paid;
    }

    //@Expose(serialize = false, deserialize = false)
    @Since(2.0)
    private boolean isInvite;

    public boolean isInvite() {
        return isInvite;
    }

    public void setInvite(boolean invite) {
        isInvite = invite;
    }

    @SerializedName("in_group")
    private String inGroup = "";

    public String getInGroup() {
        return inGroup;
    }

    public void setInGroup(String inGroup) {
        this.inGroup = inGroup;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMamberID() {
        return mamberID;
    }

    public void setMamberID(String mamberID) {
        this.mamberID = mamberID;
    }
}
