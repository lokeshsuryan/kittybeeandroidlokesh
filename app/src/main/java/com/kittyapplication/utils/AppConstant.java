package com.kittyapplication.utils;

import com.google.android.gms.location.LocationRequest;
import com.kittyapplication.R;

/**
 * Created by Dhaval Riontech on 8/8/16.
 */
public class AppConstant {
    public static final int CONNECTION_TIMEOUT = 120000;

    //TODO BOOLEAN VENTURA SERVER URL
//    public static final String BASE_URL = "http://booleanventura.com/sandbox/api/";

    //TODO LIVE SERVER URL
    public static final String BASE_URL = "http://kittybee.in/api/";

    public static final int RESPONSE_SUCCESS = 1;
    public static final int RESPONSE_FAIL = 0;
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
    public static final String MOBILE = "mobile_number";
    public static final String GOOGLE_KEY = "AIzaSyDZTtCeWoKU-5tnujUvBk8HBKj8JI73Scg";
    public static final int SOCIAL_TYPE_FACEBOOK = 0;
    public static final int SOCIAL_TYPE_GOOGLE = 1;
    public static final String PREFERENCE_NAME = "com.kittyapplication";
    public static final String IS_REGISTERED = PREFERENCE_NAME + "is_register";
    public static final String USER_TOKEN = PREFERENCE_NAME + "user-token";
    public static final String GCM_TOKEN = "";
    public static final String USER = PREFERENCE_NAME + "users";
    public static final long SPLASH_SCREEN_TIME_OUT = 1500;
    public static final int NO_PHOTO_AVAILABLE = R.mipmap.ic_launcher;
    public static final int ACTION_REQUEST_PROFILE = 1;
    public static final int ACTION_REQUEST_FAMILY = 2;
    public static final int ACTION_REQUEST_COUPLE = 3;
    public static final String[] GENDER = {"Male", "Female", "Not Now"};
    public static final String PREFERANCE_CONTACTS = PREFERENCE_NAME + "contacts";
    public static final String ADD_VENUE = "add venue";
    public static final String EDIT_VENUE = "edit venue";
    public static final String PREFERANCE_BANNERS = PREFERENCE_NAME + "banners";
    public static final String USER_PROFILE_ID = "user_profile_id";
    public static final String PUT_ATTENDANCE_EXTRA = "put_attendance_extra";
    public static final String ATTENDANCE_CHECKED = "1";
    public static final String NOTES_TYPE_PERSONAL = "personal";
    public static final String NOTES_TYPE_KITTY = "kitty";
    public static final String KITTY_ID = "kittyId";
    public static final String GROUP_ID = "groupId";
    public static final String QUICK_ID = "quickId";

    public static final String PREFERANCE_CHAT = PREFERENCE_NAME + "chat";
    public static final String ATTENDANCE_UNCHECKED = "0";

    public static final String INTENT_KITTY_TYPE = "type";
    public static final String INTENT_KITTY_IS_COUPLE_HOST = "couple_host";
    public static final String INTENT_KITTY_DATA = "data";
    public static final String INTENT_DIARY_DATA = "diary_data";
    public static final String NO_OF_HOST = "noOfHost";
    public static final String KITTY_DATE = "kitty_date";
    public static final String KITTY_NAME = "kitty_name";
    public static final String NOTES_TYPE = "notes_types";
    public static final String VENUE_PUNCH = "venue_puch";

    public static final String POP_UP_DATA = PREFERENCE_NAME + "pop_up_data";
    public static final String QUICK_BLOX_ID = PREFERENCE_NAME + "quick_blox_id";
    public static final String INTENT_CHAT = "chat_data";
    public static final String INTENT_ADD_MEMBER = "add_member";
    public static final String INTENT_ADD_MEMBER_KITTY_DATE = "add_member_kitty_date";
    public static final String INTENT_ADD_MEMBER_GROUP_ID = "add_member_group_id";
    public static final String INTENT_PAID_DATA = "paid_data";
    public static final String INTENT_KITTY_ID = "kitty_id";
    public static final String PROFILE_UPDATE = "profile_update";
    public static final String SEPERATOR_STRING = "-!-";
    public static final String OTP_ACTION = "kittybee_otp_action";
    public static final String CONTACT_SYNC_ACTION = "kittybee_contact_sync_action";

    public static final String ABOUT_US_PRIVACY_URL = "http://booleanventura.com/sandbox/public/privacy.html";
//    public static final String ABOUT_US_URL = "http://booleanventura.com/sandbox/public/about.html";

    //    http://kittybee.in/about
    public static final String ABOUT_US_URL = "http://kittybee.in/about";
    public static final String INTENT_IS_COUPLE = "is_couple";


    public static final String PENDING_NOTIFICATION = PREFERENCE_NAME + "pending_notification";

    public static final String NOT_NOW = "NotNow";


    public static final String INTENT_GIVE_RIGHTS_DATA = "give_rights_data";
    public static final String INTENT_GIVE_RIGHTS_ARRYA = "give_rights_array";
    public static final String NOTIFICATION_CARD = "notification_card_intent";
    public static final String NOTIFICAION_ATTENTION = "notification_broad_cast";
    public static final String NOTIFICATION_FLAG = "notification_flag";

    public static final int REQUEST_CREATE_DIALOG = 111;
    public static final int REQUEST_UPDATE_DIALOG = 222;
    public static final String CREATED_DIALOG = "created_dialog";
    public static final String UPDATED_DIALOG = "updated_dialog";
    public static final int TIME_TO_AUTOMATICALLY_DISMISS_ITEM = 3000;

    public static final String INTENT_MEDIA_DATA = "intent_media_data";
    public static final String INTENT_DIALOG_ID = "intent_dialog_id";
    public static final int ADD_GROUP_TYPE_HEADER = 1;
    public static final int ADD_GROUP_TYPE_HEADER_2 = 2;
    public static final int ADD_GROUP_TYPE_SINGLE = 3;
    public static final int ADD_GROUP_TYPE_COUPLE = 4;

    public static final String KIIITY_TIME = "kitty_time";

    public static final String EXTRA_PARTICIPENT_MEMBER = "participan_member";
    public static final String EXTRA_IS_CREATE_KITTY = "create_kitty";
    public static final String EXTRA_HEADS_UP_DESCRIPTION = "heads_up_desc";
    public static final String ACTION_SERVER_DETAILS = "com.kittyapplication.sync";


    public static final String INTENT_CHANGE_HOST = "change_host";
    public static final String INTENT_CHAT_DATA = "chat_data";


    public static final String QB_DIALOG_DATA = "qb_dialog_data";
    public static final String SETTING_DATA = "setting_data";


    public static final String CREATE_GROUP_ACTION = "kitty_bee_create_group_action";
    public static final String STRING_ZERO = "0";
    public static final String STRING_ONE = "1";
    public static final String STRING_TWO = "2";

    public static final String INTENT_GET_KITTY_RULE = "kitty_rule";
    public static final String ACTION_UPDATE_GROUP = PREFERENCE_NAME + ".ACTION_UPDATE_GROUP";
    public static final String PREF_OFFLINE_DATA_AVAILABLE = "offline_data_available";
    public static final String PREF_KITTIES = "get_kittes";

    public static final String APP_MSG_ID = "app_msg_id";

    public static final String NETWORK_STAT_ACTION = PREFERENCE_NAME + ".NETWORK_CONNECTED";

    public static final String CONTACT_UTILS = "contact_utils";

    public static final String LAST_INSERTED_ID = "last_inserted_id";


    public static final int LOCATION_INTERVAL = 1000; // 30
    public static final long EXPIRE_TIME_LOCATION = 5000;
    public static final int MIN_DISTANCE = 0; // Meter
    public static final int LOCATION_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

    public static final String EXTRA_CLICKED_ITEM_INDEX = "clicked_item_index";

    public static final String PREF_DEVICE_ID = "deviceId";

}
