package com.kittyapplication.model;

import android.content.Context;
import android.view.View;

import com.google.gson.annotations.SerializedName;
import com.kittyapplication.core.utils.StringUtils;
import com.kittyapplication.utils.PreferanceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Scorpion on 07-08-2016.
 */
public class ChatData {

    @SerializedName("member_id")
    private String memberId;

    @SerializedName("host_id")
    private List<String> hostId = new ArrayList<String>();

    @SerializedName("rights")
    private List<String> rights = new ArrayList<String>();

    @SerializedName("groupID")
    private String groupID;

    @SerializedName("quick_id")
    private String quickId;

    @SerializedName("name")
    private String name;

    @SerializedName("category")
    private String category;

    @SerializedName("groupIMG")
    private String groupImage;

    @SerializedName("is_host")
    private String isHost;

    @SerializedName("is_admin")
    private String isAdmin;

    @SerializedName("is_venue")
    private String isVenue;

    @SerializedName("noOfHost")
    private String noOfHost;

    @SerializedName("kitty_id")
    private String kittyId;

    @SerializedName("kittyDate")
    private String kittyDate;

    @SerializedName("kittyTime")
    private String kittyTime;

    @SerializedName("punctuality")
    private String punctuality;

    @SerializedName("punctualityTime")
    private String punctualityTime;

    @SerializedName("punctualityTime2")
    private String punctualityTime2;

    @SerializedName("set_rule")
    private String setRule;

    private HashMap<String, String> hostName;

    private List<String> hostnumber;

    private boolean isHostEmpty;
    private int adminHostVisibility = View.GONE;
    private int chatHostVisibility = View.GONE;
    private int announcementVisibility = View.GONE;
    private int diaryVisibility = View.GONE;
    private int ruleVisibility = View.GONE;

    private int id;
    private boolean isChecked;
    private boolean isAnimated;
    private int selectedItemPosition;

    /**
     * @return The memberId
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * @param memberId The member_id
     */
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     * @return The hostId
     */
    public List<String> getHostId() {
        return hostId;
    }

    /**
     * @param hostId The host_id
     */
    public void setHostId(List<String> hostId) {
        this.hostId = hostId;
    }

    /**
     * @return The rights
     */
    public List<String> getRights() {
        return rights;
    }

    /**
     * @param rights The rights
     */
    public void setRights(List<String> rights) {
        this.rights = rights;
    }

    /**
     * @return The groupID
     */
    public String getGroupID() {
        return groupID;
    }

    /**
     * @param groupID The groupID
     */
    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    /**
     * @return The quickId
     */
    public String getQuickId() {
        return quickId;
    }

    /**
     * @param quickId The quick_id
     */
    public void setQuickId(String quickId) {
        this.quickId = quickId;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category The category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return The groupImage
     */
    public String getGroupImage() {
        return groupImage;
    }

    /**
     * @param groupImage The groupImage
     */
    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    /**
     * @return The isHost
     */
    public String getIsHost() {
        return isHost;
    }

    /**
     * @param isHost The is_host
     */
    public void setIsHost(String isHost) {
        this.isHost = isHost;
    }

    /**
     * @return The isAdmin
     */
    public String getIsAdmin() {
        return isAdmin;
    }

    /**
     * @param isAdmin The is_admin
     */
    public void setIsAdmin(String isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * @return The isVenue
     */
    public String getIsVenue() {
        return isVenue;
    }

    /**
     * @param isVenue The is_venue
     */
    public void setIsVenue(String isVenue) {
        this.isVenue = isVenue;
    }

    /**
     * @return The noOfHost
     */
    public String getNoOfHost() {
        return noOfHost;
    }

    /**
     * @param noOfHost The noOfHost
     */
    public void setNoOfHost(String noOfHost) {
        this.noOfHost = noOfHost;
    }

    /**
     * @return The kittyId
     */
    public String getKittyId() {
        return kittyId;
    }

    /**
     * @param kittyId The kitty_id
     */
    public void setKittyId(String kittyId) {
        this.kittyId = kittyId;
    }

    /**
     * @return The kittyDate
     */
    public String getKittyDate() {
        return kittyDate;
    }

    /**
     * @param kittyDate The kittyDate
     */
    public void setKittyDate(String kittyDate) {
        this.kittyDate = kittyDate;
    }

    /**
     * @return The kittyTime
     */
    public String getKittyTime() {
        return kittyTime;
    }

    /**
     * @param kittyTime The kittyTime
     */
    public void setKittyTime(String kittyTime) {
        this.kittyTime = kittyTime;
    }

    /**
     * @return The punctuality
     */
    public String getPunctuality() {
        return punctuality;
    }

    /**
     * @param punctuality The punctuality
     */
    public void setPunctuality(String punctuality) {
        this.punctuality = punctuality;
    }

    /**
     * @return The punctualityTime
     */
    public String getPunctualityTime() {
        return punctualityTime;
    }

    /**
     * @param punctualityTime The punctualityTime
     */
    public void setPunctualityTime(String punctualityTime) {
        this.punctualityTime = punctualityTime;
    }

    /**
     * @return The punctualityTime2
     */
    public String getPunctualityTime2() {
        return punctualityTime2;
    }

    /**
     * @param punctualityTime2 The punctualityTime2
     */
    public void setPunctualityTime2(String punctualityTime2) {
        this.punctualityTime2 = punctualityTime2;
    }

    /**
     * @return The setRule
     */
    public String getRule() {
        return setRule;
    }

    /**
     * @param setRule The set_rule
     */
    public void setRule(String setRule) {
        this.setRule = setRule;
    }

    public boolean isHostEmpty() {
        return isHostEmpty;
    }

    public void setHostEmpty() {
        List<String> ids = this.getHostId();
        for (String id : ids) {
            if (id.equals("")) {
                isHostEmpty = true;
                break;
            }
        }
    }

    public void setGroupRightsVisibility(Context context) {
        try {
            if (this.getRule().equalsIgnoreCase("1")) {
//                RegisterResponseDao user = LoginUserPrefHolder.getInstance().getUser();
                RegisterResponseDao user = PreferanceUtils.getLoginUserObject(context);
                if (user != null) {
                    String userID = user.getUserID();
                    if (isHostEmpty() && getIsAdmin().equalsIgnoreCase("1")) {
                        adminHostVisibility = View.VISIBLE;
                        announcementVisibility = View.VISIBLE;
                    } else if (getIsHost().equalsIgnoreCase("1")) {
                        chatHostVisibility = View.VISIBLE;
                        announcementVisibility = View.VISIBLE;
                    } else if (getRights().contains(userID)) {
                        chatHostVisibility = View.VISIBLE;
                        announcementVisibility = View.VISIBLE;
                    }
                }
                ruleVisibility = View.GONE;
                diaryVisibility = View.VISIBLE;
            } else {
                diaryVisibility = View.GONE;
                chatHostVisibility = View.GONE;
                announcementVisibility = View.GONE;
                adminHostVisibility = View.GONE;
                ruleVisibility = View.VISIBLE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getAdminHostVisibility() {
        return adminHostVisibility;
    }

    public int getAnnouncementVisibility() {
        return announcementVisibility;
    }

    public int getChatHostVisibility() {
        return chatHostVisibility;
    }

    public int getDiaryVisibility() {
        return diaryVisibility;
    }


    public HashMap<String, String> getHostName() {
        return hostName;
    }

    public void setHostName(HashMap<String, String> hostName) {
        this.hostName = hostName;
    }

    public List<String> getHostnumber() {
        return hostnumber;
    }

    public void setHostnumber(List<String> hostnumber) {
        this.hostnumber = hostnumber;
    }

    public void setHostEmpty(boolean hostEmpty) {
        isHostEmpty = hostEmpty;
    }

    public int getRuleVisibility() {
        return ruleVisibility;
    }

    public void setAdminHostVisibility(int adminHostVisibility) {
        this.adminHostVisibility = adminHostVisibility;
    }

    public void setChatHostVisibility(int chatHostVisibility) {
        this.chatHostVisibility = chatHostVisibility;
    }

    public void setAnnouncementVisibility(int announcementVisibility) {
        this.announcementVisibility = announcementVisibility;
    }

    public void setRuleVisibility(int ruleVisibility) {
        this.ruleVisibility = ruleVisibility;
    }

    public void setDiaryVisibility(int diaryVisibility) {
        this.diaryVisibility = diaryVisibility;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public void setAnimated(boolean animated) {
        isAnimated = animated;
    }

    @Override
    public String toString() {
        return StringUtils.toJson(getClass(), this);
    }
}