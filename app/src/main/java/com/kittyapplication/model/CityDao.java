package com.kittyapplication.model;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 8/10/16.
 */

public class CityDao {

    private String cityId;
    private String cityName;
    private String latitude;
    private String longitude;

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
