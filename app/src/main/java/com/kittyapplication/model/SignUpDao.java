package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Scorpion on 07-08-2016.
 */
public class SignUpDao {

    private String message;
    private int response;
    private String status;
    private String mobile;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}

