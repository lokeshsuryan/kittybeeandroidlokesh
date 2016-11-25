package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dhaval Riontech on 11/8/16.
 */
public class AddAttendanceDao {
    @SerializedName("groupId")
    private String groupId;

    @SerializedName("yes")
    private String attendanceYes;

    @SerializedName("no")
    private String attendanceNo;

    @SerializedName("maybe")
    private String attendanceMayBe;

    @SerializedName("kittyId")
    private String kittyId;
    private String userId;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAttendanceYes() {
        return attendanceYes;
    }

    public void setAttendanceYes(String attendanceYes) {
        this.attendanceYes = attendanceYes;
    }

    public String getAttendanceNo() {
        return attendanceNo;
    }

    public void setAttendanceNo(String attendanceNo) {
        this.attendanceNo = attendanceNo;
    }

    public String getAttendanceMayBe() {
        return attendanceMayBe;
    }

    public void setAttendanceMayBe(String attendanceMayBe) {
        this.attendanceMayBe = attendanceMayBe;
    }

    public String getKittyId() {
        return kittyId;
    }

    public void setKittyId(String kittyId) {
        this.kittyId = kittyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
