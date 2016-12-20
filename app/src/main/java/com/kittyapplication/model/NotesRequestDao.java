package com.kittyapplication.model;

/**
 * Created by Dhaval Riontech on 12/8/16.
 */
public class NotesRequestDao {
    private String description;
    private String groupId;
    private String kitty;
    private String title;
    private String type;
    private String userId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getKitty() {
        return kitty;
    }

    public void setKitty(String kitty) {
        this.kitty = kitty;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
