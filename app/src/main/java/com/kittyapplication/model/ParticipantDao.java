package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 17/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ParticipantDao {

    private String id;
    private String name;
    @SerializedName("quick_id")
    private String quickId;
    private String groupIMG;
    private int count;
    private List<ParticipantMember> participant = new ArrayList<>();

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

    public String getQuickId() {
        return quickId;
    }

    public void setQuickId(String quickId) {
        this.quickId = quickId;
    }

    public String getGroupIMG() {
        return groupIMG;
    }

    public void setGroupIMG(String groupIMG) {
        this.groupIMG = groupIMG;
    }

    public List<ParticipantMember> getParticipant() {
        return participant;
    }

    public void setParticipant(List<ParticipantMember> participant) {
        this.participant = participant;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
