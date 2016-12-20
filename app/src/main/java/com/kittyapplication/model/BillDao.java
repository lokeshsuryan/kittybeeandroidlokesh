package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Scorpion on 08-08-2016.
 */
public class BillDao {
    @SerializedName("advanced_paid")
    private String advancedPaid;

    private String amount;

    @SerializedName("balance_with")
    private String balanceWith;

    @SerializedName("carry_forword")
    private String carryForword;

    private String collection;

    @SerializedName("gift_game")
    private String giftGame;

    @SerializedName("group_id")
    private String groupId;

    private String id;

    private String imageBill;

    private String image;

    @SerializedName("kitty_id")
    private String kittyId;

    @SerializedName("member_present")
    private String memberPresent;

    @SerializedName("previous_balance")
    private String previousBalance;

    public String getAdvancedPaid() {
        return advancedPaid;
    }

    public void setAdvancedPaid(String advancedPaid) {
        this.advancedPaid = advancedPaid;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getBalanceWith() {
        return balanceWith;
    }

    public void setBalanceWith(String balanceWith) {
        this.balanceWith = balanceWith;
    }

    public String getCarryForword() {
        return carryForword;
    }

    public void setCarryForword(String carryForword) {
        this.carryForword = carryForword;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getGiftGame() {
        return giftGame;
    }

    public void setGiftGame(String giftGame) {
        this.giftGame = giftGame;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageBill() {
        return imageBill;
    }

    public void setImageBill(String image) {
        this.imageBill = image;
    }

    public String getKittyId() {
        return kittyId;
    }

    public void setKittyId(String kittyId) {
        this.kittyId = kittyId;
    }

    public String getMemberPresent() {
        return memberPresent;
    }

    public void setMemberPresent(String memberPresent) {
        this.memberPresent = memberPresent;
    }

    public String getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(String previousBalance) {
        this.previousBalance = previousBalance;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
