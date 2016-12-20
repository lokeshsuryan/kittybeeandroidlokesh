package com.kittyapplication.model;

import com.kittyapplication.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Device Contacts Model Class
 *
 * @author Riontech.com
 */
public class DeviceContacts {
    private String tag;

    private String userId;

    private String fullname;

    private String password;

    private List<ContactNumber> phoneNumberList = new ArrayList<ContactNumber>();

    private String email;

    private String birthDate;

    private String bloodGroup;

    private String gender;

    private String userPhoto;


    private String homeAddress;


    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<ContactNumber> getPhoneNumber() {
        return phoneNumberList;
    }

    public void setPhoneNumber(List<ContactNumber> phoneNumber) {
        this.phoneNumberList = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    @Override
    public String toString() {
        return StringUtils.toJson(getClass(), this);
    }
}