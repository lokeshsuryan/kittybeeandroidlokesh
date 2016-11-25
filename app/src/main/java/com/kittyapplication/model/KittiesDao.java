package com.kittyapplication.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Dhaval Riontech on 13/8/16.
 */
public class KittiesDao {
    private String id;

    @SerializedName("group_id")
    private String groupId;

    @SerializedName("member_id")
    private String memberId;
    private List<String> rights;

    @SerializedName("get_punctuality")
    private String getPunctuality;

    @SerializedName("admin_id")
    private String adminId;

    @SerializedName("kitty_done")
    private String kittyDone;

    @SerializedName("current_host")
    private String currentHost;

    @SerializedName("kitty_date")
    private String kittyDate;

    @SerializedName("host_id")
    private List<String> hostId;

    @SerializedName("host_name")
    private List<String> hostName;

    @SerializedName("members")
    private List<MembersDaoCalendar> members;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public List<String> getRights() {
        return rights;
    }

    public void setRights(List<String> rights) {
        this.rights = rights;
    }

    public String getGetPunctuality() {
        return getPunctuality;
    }

    public void setGetPunctuality(String getPunctuality) {
        this.getPunctuality = getPunctuality;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getKittyDone() {
        return kittyDone;
    }

    public void setKittyDone(String kittyDone) {
        this.kittyDone = kittyDone;
    }

    public String getCurrentHost() {
        return currentHost;
    }

    public void setCurrentHost(String currentHost) {
        this.currentHost = currentHost;
    }

    public String getKittyDate() {
        return kittyDate;
    }

    public void setKittyDate(String kittyDate) {
        this.kittyDate = kittyDate;
    }

    public List<String> getHostId() {
        return hostId;
    }

    public void setHostId(List<String> hostId) {
        this.hostId = hostId;
    }

    public List<String> getHostName() {
        return hostName;
    }

    public void setHostName(List<String> hostName) {
        this.hostName = hostName;
    }

    public List<MembersDaoCalendar> getMembers() {
        return members;
    }

    public void setMembers(List<MembersDaoCalendar> members) {
        this.members = members;
    }

    @SerializedName("host_Number")
    private List<String> hostNumber;

    public List<String> getHostNumber() {
        return hostNumber;
    }

    public void setHostNumber(List<String> hostNumber) {
        this.hostNumber = hostNumber;
    }

    private List<NotesResponseDao> kittyPersonalNotes;

    private List<NotesResponseDao> kittyNotes;

    private BillDao billData;

    public List<NotesResponseDao> getKittyPersonalNotes() {
        return kittyPersonalNotes;
    }

    public void setKittyPersonalNotes(List<NotesResponseDao> kittyPersonalNotes) {
        this.kittyPersonalNotes = kittyPersonalNotes;
    }

    public List<NotesResponseDao> getKittyNotes() {
        return kittyNotes;
    }

    public void setKittyNotes(List<NotesResponseDao> kittyNotes) {
        this.kittyNotes = kittyNotes;
    }

    public BillDao getBillData() {
        return billData;
    }

    public void setBillData(BillDao billData) {
        this.billData = billData;
    }
}
