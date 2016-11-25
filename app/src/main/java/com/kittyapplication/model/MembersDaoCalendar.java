package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Dhaval Riontech on 13/8/16.
 */
public class MembersDaoCalendar {
    private String id;
    private String number;
    private String name;
    private String host;
    private String delete;
    private String paid;
    private String present;
    private String punctuality;
    @SerializedName("get_punctuality")
    private String getPunctuality;

    //TODO Xyz Parameter - 21 Oct 2016 (Dhaval)
    private Date hostedByDate;
    private boolean isCouple;
    private boolean userDeleted;
    private String usersName;
    private boolean punctualCouple;
    private boolean isPaid;
    private boolean afterHostedByDate;
    private boolean isPresent;
    private boolean availablePunctuality;
    private boolean isPunctuality;
    private boolean isHost;
    private boolean isGetPunctuality;

    public Date getHostedByDate() {
        return hostedByDate;
    }

    public void setHostedByDate(Date hostedByDate) {
        this.hostedByDate = hostedByDate;
    }

    public boolean isCouple() {
        return isCouple;
    }

    public void setCouple(boolean couple) {
        isCouple = couple;
    }

    public void setUserDeleted(boolean userDeleted) {
        this.userDeleted = userDeleted;
    }

    public boolean isUserDeleted() {
        return userDeleted;
    }

    public String getUsersName() {
        return usersName;
    }

    public void setUsersName(String usersName) {
        this.usersName = usersName;
    }

    public boolean isPunctualCouple() {
        return punctualCouple;
    }

    public void setPunctualCouple(boolean punctualCouple) {
        this.punctualCouple = punctualCouple;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public boolean isAfterHostedByDate() {
        return afterHostedByDate;
    }

    public void setAfterHostedByDate(boolean afterHostedByDate) {
        this.afterHostedByDate = afterHostedByDate;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent(boolean present) {
        isPresent = present;
    }

    public boolean isAvailablePunctuality() {
        return availablePunctuality;
    }

    public void setAvailablePunctuality(boolean availablePunctuality) {
        this.availablePunctuality = availablePunctuality;
    }

    public boolean isPunctuality() {
        return isPunctuality;
    }

    public void setPunctuality(boolean punctuality) {
        isPunctuality = punctuality;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public boolean isGetPunctuality() {
        return isGetPunctuality;
    }

    public void setGetPunctuality(boolean getPunctuality) {
        isGetPunctuality = getPunctuality;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public String getPaid() {
        return paid;
    }

    public void setPaid(String paid) {
        this.paid = paid;
    }

    public String getPresent() {
        return present;
    }

    public void setPresent(String present) {
        this.present = present;
    }

    public String getPunctuality() {
        return punctuality;
    }

    public void setPunctuality(String punctuality) {
        this.punctuality = punctuality;
    }

    public String getGetPunctuality() {
        return getPunctuality;
    }

    public void setGetPunctuality(String getPunctuality) {
        this.getPunctuality = getPunctuality;
    }
}
