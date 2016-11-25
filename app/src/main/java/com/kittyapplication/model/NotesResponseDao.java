package com.kittyapplication.model;

/**
 * Created by Dhaval Riontech on 12/8/16.
 */
public class NotesResponseDao {
    private int noteID;
    private String title;
    private String noteType;
    private String description;
    private String groupId;
    private String kitty;
    private String userId;


    public int getNoteID() {
        return noteID;
    }

    public void setNoteID(int noteID) {
        this.noteID = noteID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
