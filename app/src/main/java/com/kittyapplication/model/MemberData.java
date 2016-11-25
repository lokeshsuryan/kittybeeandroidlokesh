package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Pintu Riontech on 22/8/16.
 * vaghela.pintu31@gmail.com
 */
public class MemberData {

    String image="";
    @SerializedName("is_member")
    String isMember="";
    @SerializedName("is_paid")
    String isPaid = "";
    String name="";
    String phone="";
    String registration="0";
    String status="";
    String userid="";
    String id="";
    String mamber_id="";

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIsMember() {
        return isMember;
    }

    public void setIsMember(String isMember) {
        this.isMember = isMember;
    }

    public String getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(String isPaid) {
        this.isPaid = isPaid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMamber_id() {
        return mamber_id;
    }

    public void setMamber_id(String mamber_id) {
        this.mamber_id = mamber_id;
    }
}
