package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Dhaval Riontech on 13/8/16.
 */
public class DiaryResponseDao {
    private String id;
    private String name;
    private String leftKitties;
    private String category;
    private String noOfHost;
    private String noOfPunctuality;

    @SerializedName("Kitty_amount")
    private String kittyAmount;

    private String punctualityAmount;
    private String punctualityAmount2;

    private List<KittiesDao> kitties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeftKitties() {
        return leftKitties;
    }

    public void setLeftKitties(String leftKitties) {
        this.leftKitties = leftKitties;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNoOfHost() {
        return noOfHost;
    }

    public void setNoOfHost(String noOfHost) {
        this.noOfHost = noOfHost;
    }

    public String getNoOfPunctuality() {
        return noOfPunctuality;
    }

    public void setNoOfPunctuality(String noOfPunctuality) {
        this.noOfPunctuality = noOfPunctuality;
    }

    public String getKittyAmount() {
        return kittyAmount;
    }

    public void setKittyAmount(String kittyAmount) {
        this.kittyAmount = kittyAmount;
    }

    public String getPunctualityAmount() {
        return punctualityAmount;
    }

    public void setPunctualityAmount(String punctualityAmount) {
        this.punctualityAmount = punctualityAmount;
    }

    public String getPunctualityAmount2() {
        return punctualityAmount2;
    }

    public void setPunctualityAmount2(String punctualityAmount2) {
        this.punctualityAmount2 = punctualityAmount2;
    }

    public List<KittiesDao> getKitties() {
        return kitties;
    }

    public void setKitties(List<KittiesDao> kitties) {
        this.kitties = kitties;
    }
}
