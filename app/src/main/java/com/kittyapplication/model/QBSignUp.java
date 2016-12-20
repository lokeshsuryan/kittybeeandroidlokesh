package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MIT on 8/17/2016.
 */
public class QBSignUp {
    @SerializedName("ID")
    private String id;
    @SerializedName("login")
    private String login;
    @SerializedName("full_name")
    private String fullName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
