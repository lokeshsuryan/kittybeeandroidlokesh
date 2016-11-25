package com.kittyapplication.chat.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;

public class SharedPreferencesUtil {
    private static final String QB_USER_ID = "qb_user_id";
    private static final String QB_USER_LOGIN = "qb_user_login";
    private static final String QB_USER_PASSWORD = "qb_user_password";
    private static final String QB_USER_FULL_NAME = "qb_user_full_name";
    private static final String TAG = SharedPreferencesUtil.class.getSimpleName();

    public static void saveQbUser(QBUser qbUser) {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();
        helper.save(QB_USER_ID, qbUser.getId());
        helper.save(QB_USER_LOGIN, qbUser.getLogin());
        helper.save(QB_USER_PASSWORD, Consts.QUICK_BLOX_PASSWORD);
        helper.save(QB_USER_FULL_NAME, qbUser.getFullName());
        Log.d(TAG, "PrefQBUser =>" + new Gson().toJson(getQbUser()));
    }

    public static void removeQbUser() {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();
        helper.delete(QB_USER_ID);
        helper.delete(QB_USER_LOGIN);
        helper.delete(QB_USER_PASSWORD);
        helper.delete(QB_USER_FULL_NAME);
    }

    public static boolean hasQbUser() {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();
        return helper.has(QB_USER_LOGIN) && helper.has(QB_USER_PASSWORD);
    }

    public static QBUser getQbUser() {
        SharedPrefsHelper helper = SharedPrefsHelper.getInstance();

        if (hasQbUser()) {
            Integer id = helper.get(QB_USER_ID);
            String login = helper.get(QB_USER_LOGIN);
            String password = helper.get(QB_USER_PASSWORD);
            String fullName = helper.get(QB_USER_FULL_NAME);

            QBUser user = new QBUser(login, password);
            user.setId(id);
            user.setFullName(fullName);
            return user;
        } else {
            return null;
        }
    }

}
