package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dhaval Riontech on 11/8/16.
 */
public class AttendanceDataDao {
    private String name;
    private String number;

    @SerializedName("yes")
    private String attendanceYes;

    @SerializedName("no")
    private String attendanceNo;

    @SerializedName("maybe")
    private String attendanceMaybe;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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

    public String getAttendanceMaybe() {
        return attendanceMaybe;
    }

    public void setAttendanceMaybe(String attendanceMaybe) {
        this.attendanceMaybe = attendanceMaybe;
    }
}
