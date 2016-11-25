package com.kittyapplication.chat.utils;


import com.kittyapplication.chat.R;
import com.kittyapplication.core.utils.ResourceUtils;

public interface Consts {
    // In GCM, the Sender ID is a project ID that you acquire from the API console
    String GCM_SENDER_ID = "23596211365";

    //TODO BOOLEAN VENTURA SERVER
   /* String QB_APP_ID = "42779";
    String QB_AUTH_KEY = "LLLxgsMYpBgmqwf";
    String QB_AUTH_SECRET = "x7rx-j7ZMSTnBWn";
    String QB_ACCOUNT_KEY = "KmWfth7Wo2EVHqtWiE7P";*/

    //TODO LIVE SERVER
    String QB_APP_ID = "45630";
    String QB_AUTH_KEY = "Mz6cxA6L7nMa3VV";
    String QB_AUTH_SECRET = "LnZf3XxxsDUPR29";
    String QB_ACCOUNT_KEY = "NC1zXvj6hkp6DbavtytW";

    String QB_SUBSCRIPTION = "qb_subscription";
    int PREFERRED_IMAGE_SIZE_PREVIEW = ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size);
    int PREFERRED_IMAGE_SIZE_FULL = ResourceUtils.dpToPx(320);

    public static final String KEY_USERNAME = "username";
    public static final String KEY_IMAGE = "image";
    public static final String QUICK_BLOX_PASSWORD = "KittyBeeArun";
    public static final String PREF_KEY_DIALOGS = "qb_dialogs";


}