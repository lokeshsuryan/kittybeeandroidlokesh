package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 18/11/16.
 */

public class VersionDao {

    private String id;
    @SerializedName("device_type")
    private String deviceType;
    private String version;
    @SerializedName("last_update")
    private String lastUpdate;

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
