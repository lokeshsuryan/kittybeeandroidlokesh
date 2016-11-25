package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Pintu Riontech on 24/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ReqGiveRights {

    private String from;
    @SerializedName("kitty_id")
    private String kittyId;
    private String to;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getKittyId() {
        return kittyId;
    }

    public void setKittyId(String kittyId) {
        this.kittyId = kittyId;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
