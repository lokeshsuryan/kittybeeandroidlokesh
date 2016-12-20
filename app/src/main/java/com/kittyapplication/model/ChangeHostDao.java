package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 19/9/16.
 */
public class ChangeHostDao {

    @SerializedName("current_host")
    private List<ParticipantMember> currentHostList = new ArrayList<>();
    @SerializedName("new_host")
    private List<ParticipantMember> newHostList = new ArrayList<>();

    public List<ParticipantMember> getCurrentHostList() {
        return currentHostList;
    }

    public void setCurrentHostList(List<ParticipantMember> currentHostList) {
        this.currentHostList = currentHostList;
    }

    public List<ParticipantMember> getNewHostList() {
        return newHostList;
    }

    public void setNewHostList(List<ParticipantMember> newHostList) {
        this.newHostList = newHostList;
    }
}
