package com.kittyapplication.sqlitedb;

/**
 * Created by Pintu Riontech on 16/9/16.
 * vaghela.pintu31@gmail.com
 */
public class SQLConstants {

    // DATA BASE NAME AND VERSION
    public static final String DATABASE_NAME = "kittyBee.db";
    public static final int DATABASE_VERSION = 4;

    // DATA TABLE COLUMN NAME

    // These 3 are common to all as we are storing id, JSON and timestamp.
    public static final String KEY_DATA = "_data";
    public static final String KEY_ID = "_id";
    public static final String KEY_TIMESTAMP = "journal_timestamp";

    public static final String KEY_GROUP_ID = "group_id";
    public static final String KEY_KITTY_ID = "kitty_id";
    public static final String COLUMN_NAME_NOTE_TYPE = "note_type";
    public static final String KEY_IS_SYNC = "is_sync";
    public static final String KEY_NOTE_ID = "note_id";


    //DATA TABLE NAMES
    public static final String TABLE_GROUP = "tbl_group";
    public static final String TABLE_QB_DIALOGS = "tbl_qb_dialogs";
    public static final String TABLE_KITTIES = "tbl_kitties";
    public static final String TABLE_KITTY_NOTES = "tbl_kitty_notes";
    public static final String TABLE_KITTY_PERSONAL_NOTES = "tbl_personal_notes";
    public static final String TABLE_PERSONAL_NOTES = "tbl_notes";
    public static final String TABLE_KITTY_RULES = "tbl_kitty_rules";
    public static final String TABLE_DAIRY = "tbl_dairy";
    public static final String TABLE_UPDATE_DAIRY = "tbl_update_dairy";
    public static final String TABLE_PARTICIPANT = "tbl_participant";
    public static final String TABLE_ATTENDANCE = "tbl_attendance";
    public static final String TABLE_BILL = "tbl_bill";
    public static final String TABLE_SUMMARY = "tbl_summary";
    public static final String TABLE_VENUE = "tbl_venue";
    public static final String TABLE_SETTING = "tbl_setting";
    public static final String TABLE_MESSAGE = "tbl_message";
    public static final String TABLE_GROUP_CHAT_MEMBER = "tbl_group_chat_member";
}
