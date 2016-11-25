package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ServerResponse<T> {

    private int response;

    private String status;

    private String message;

    @SerializedName("kittynext")
    private String kittyNext;

    private String category;

    @SerializedName("upaired")
    private List<ContactDao> unPairList = new ArrayList<>();

    private OfflineDao group;

    @SerializedName("group_id")
    private String groupId;


    private OfflineDao groupData;

    private OfflineDao allData;

    private VersionDao android;

    private VersionDao ios;

    private T data;

    private int count;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getKittyNext() {
        return kittyNext;
    }

    public void setKittyNext(String kittyNext) {
        this.kittyNext = kittyNext;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<ContactDao> getUpaired() {
        return unPairList;
    }

    public void setUpaired(List<ContactDao> upaired) {
        this.unPairList = upaired;
    }


    public OfflineDao getGroup() {
        return group;
    }

    public void setGroup(OfflineDao group) {
        this.group = group;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public OfflineDao getGroupData() {
        return groupData;
    }

    public void setGroupData(OfflineDao groupData) {
        this.groupData = groupData;
    }

    public OfflineDao getAllData() {
        return allData;
    }

    public void setAllData(OfflineDao allData) {
        this.allData = allData;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public VersionDao getAndroid() {
        return android;
    }

    public void setAndroid(VersionDao android) {
        this.android = android;
    }

    public VersionDao getIos() {
        return ios;
    }

    public void setIos(VersionDao ios) {
        this.ios = ios;
    }
}