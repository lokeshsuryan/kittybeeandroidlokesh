package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 10/8/16.
 * vaghela.pintu31@gmail.com
 */
public class CreateGroup implements Serializable {

    @SerializedName("QuickGroupId")
    private String quickGroupId = "";

    private String amount = "";

    private String category = "";

    private String days = "";

    private String destination = "";

    private String drink = "";

    private String drinkAmount = "";

    private String fine = "";

    private String fineAmount = "";

    private String foodBy = "";

    private String foodamount = "";

    private String game = "";

    private String gameType = "";

    private String groupIMG = "";

    private String hosts = "";

    private String kittyTime = "";

    private String name = "";

    private String normal = "";

    private String normalprice = "";

    private String note = "";

    private String punctuality = "";

    private String punctualityAmount = "";

    @SerializedName("punctualityAmount2")
    private String punctualityAmountTwo = "";

    private String punctualityTime = "";

    @SerializedName("punctualityTime2")
    private String punctualityTimeTwo = "";

    private String setRule = "";

    private String tambola = "";

    private String tambolaprice = "";

    private String week = "";

    private List<ContactDao> groupMember = new ArrayList<>();

    public String getQuickGroupId() {
        return quickGroupId;
    }

    public void setQuickGroupId(String quickGroupId) {
        this.quickGroupId = quickGroupId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDrink() {
        return drink;
    }

    public void setDrink(String drink) {
        this.drink = drink;
    }

    public String getDrinkAmount() {
        return drinkAmount;
    }

    public void setDrinkAmount(String drinkAmount) {
        this.drinkAmount = drinkAmount;
    }

    public String getFine() {
        return fine;
    }

    public void setFine(String fine) {
        this.fine = fine;
    }

    public String getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(String fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getFoodBy() {
        return foodBy;
    }

    public void setFoodBy(String foodBy) {
        this.foodBy = foodBy;
    }

    public String getFoodamount() {
        return foodamount;
    }

    public void setFoodamount(String foodamount) {
        this.foodamount = foodamount;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public String getGroupIMG() {
        return groupIMG;
    }

    public void setGroupIMG(String groupIMG) {
        this.groupIMG = groupIMG;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public String getKittyTime() {
        return kittyTime;
    }

    public void setKittyTime(String kittyTime) {
        this.kittyTime = kittyTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormal() {
        return normal;
    }

    public void setNormal(String normal) {
        this.normal = normal;
    }

    public String getNormalprice() {
        return normalprice;
    }

    public void setNormalprice(String normalprice) {
        this.normalprice = normalprice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPunctuality() {
        return punctuality;
    }

    public void setPunctuality(String punctuality) {
        this.punctuality = punctuality;
    }

    public String getPunctualityAmount() {
        return punctualityAmount;
    }

    public void setPunctualityAmount(String punctualityAmount) {
        this.punctualityAmount = punctualityAmount;
    }

    public String getPunctualityAmountTwo() {
        return punctualityAmountTwo;
    }

    public void setPunctualityAmountTwo(String punctualityAmountTwo) {
        this.punctualityAmountTwo = punctualityAmountTwo;
    }

    public String getPunctualityTime() {
        return punctualityTime;
    }

    public void setPunctualityTime(String punctualityTime) {
        this.punctualityTime = punctualityTime;
    }

    public String getPunctualityTimeTwo() {
        return punctualityTimeTwo;
    }

    public void setPunctualityTimeTwo(String punctualityTimeTwo) {
        this.punctualityTimeTwo = punctualityTimeTwo;
    }

    public String getSetRule() {
        return setRule;
    }

    public void setSetRule(String setRule) {
        this.setRule = setRule;
    }

    public String getTambola() {
        return tambola;
    }

    public void setTambola(String tambola) {
        this.tambola = tambola;
    }

    public String getTambolaprice() {
        return tambolaprice;
    }

    public void setTambolaprice(String tambolaprice) {
        this.tambolaprice = tambolaprice;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public List<ContactDao> getGroupMember() {
        return groupMember;
    }

    public void setGroupMember(List<ContactDao> groupMember) {
        this.groupMember = groupMember;
    }

    private String noOfHost = "";

    public String getNoOfHost() {
        return noOfHost;
    }

    public void setNoOfHost(String noOfHost) {
        this.noOfHost = noOfHost;
    }

    private String first_kitty;

    public String getFirst_kitty() {
        return first_kitty;
    }

    public void setFirst_kitty(String first_kitty) {
        this.first_kitty = first_kitty;
    }

    private String groupID;

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    private String groupId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupID;
    }

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SerializedName("group_name")
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
