package com.kittyapplication.model;

/**
 * Created by Dhaval Riontech on 11/8/16.
 */
public class ServerReq<T> {
    private T data;
    private String deviceID = "0000000000000000000000";
    private String deviceType = "android";

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceType() {
        return deviceType;
    }

}
