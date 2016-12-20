package com.kittyapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kittyapplication.model.AppContactList;
import com.kittyapplication.model.ArticleDao;
import com.kittyapplication.model.BannerData;
import com.kittyapplication.model.ChatDao;
import com.kittyapplication.model.RegisterResponseDao;
import com.quickblox.chat.model.QBChatDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by riontech1 on 26/1/16.
 */
public class PreferanceUtils {

    private static final String TAG = PreferanceUtils.class.getSimpleName();

    /**
     * Insert string value in Shared Preferences
     *
     * @param context of application
     * @param value   to store in preferences
     * @param key     using which value is mapped
     * @return
     */
    public static boolean putStringInPreferences(final Context context,
                                                 final String value, final String key) {
        final SharedPreferences sharedPreferences = context
                .getSharedPreferences(AppConstant.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
        return true;
    }

    /**
     * Get Data from preferance
     *
     * @param context
     * @param defaultValue
     * @param key
     * @return
     */
    public static String getStringFromPreferences(final Context context,
                                                  final String defaultValue, final String key) {
        final SharedPreferences sharedPreferences = context
                .getSharedPreferences(AppConstant.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
        final String temp = sharedPreferences.getString(key, defaultValue);
        return temp;
    }

    /**
     * Insert booblean in preferance
     *
     * @param context
     * @param value
     * @param key
     * @return
     */
    public static boolean putBooleanInPreferences(final Context context,
                                                  final boolean value, final String key) {
        final SharedPreferences sharedPreferences = context
                .getSharedPreferences(AppConstant.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
        return true;
    }

    /**
     * Get boolean from preferance
     *
     * @param context
     * @param defaultValue
     * @param key
     * @return
     */
    public static boolean getBooleanFromPreferences(final Context context,
                                                    final boolean defaultValue, final String key) {
        final SharedPreferences sharedPreferences = context
                .getSharedPreferences(AppConstant.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Insert integer value in preferences
     *
     * @param context
     * @param value
     * @param key
     * @return
     */
    public static boolean putIntegerInPreferences(final Context context,
                                                  final int value, final String key) {
        final SharedPreferences sharedPreferences = context
                .getSharedPreferences(AppConstant.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
        return true;
    }

    /**
     * Return integer preference value
     *
     * @param context
     * @param defaultValue
     * @param key
     * @return
     */
    public static int getIntegerFromPreferences(final Context context,
                                                final int defaultValue, final String key) {
        final SharedPreferences sharedPreferences = context
                .getSharedPreferences(AppConstant.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
        final int temp = sharedPreferences.getInt(key, defaultValue);
        return temp;
    }

    /**
     * get is Already Login
     *
     * @param context
     * @return
     */
    public static boolean isRegistered(Context context) {
        return PreferanceUtils.getBooleanFromPreferences(context, false,
                AppConstant.IS_REGISTERED);
    }

    /**
     * Set is Already Login
     *
     * @param context
     * @param isRegistered
     */
    public static void setIsRegistered(Context context, boolean isRegistered) {
        PreferanceUtils.putBooleanInPreferences(context, isRegistered,
                AppConstant.IS_REGISTERED);
    }


    /**
     * Set TimetableRecyclerRow Token From Preference
     *
     * @param context
     * @param users
     */
    public static void setUserToken(Context context, String users) {
        PreferanceUtils.putStringInPreferences(context, users,
                AppConstant.USER_TOKEN);
    }

    /**
     * Get TimetableRecyclerRow Token From Preference
     *
     * @param context
     * @return
     */
    public static String getUserToken(Context context) {
        return PreferanceUtils.getStringFromPreferences(context,
                "", AppConstant.USER_TOKEN);
    }


    /**
     * Clear Data From Preference
     *
     * @param ctx
     */
    public static void clearPreference(Context ctx) {
        SharedPreferences settings = ctx.getSharedPreferences(AppConstant.PREFERENCE_NAME, ctx.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * Set GCM Token
     *
     * @param context
     * @param token
     */
    public static void setGCMID(Context context, String token) {
        PreferanceUtils.putStringInPreferences(context, token,
                AppConstant.GCM_TOKEN);
    }

    /**
     * Get GCM Token
     *
     * @param context
     * @return
     */
    public static String getRegGCMID(Context context) {
        return PreferanceUtils.getStringFromPreferences(context, "",
                AppConstant.GCM_TOKEN);
    }

    /**
     * set TimetableRecyclerRow From Preference
     *
     * @param context
     * @param users
     */
    public static void setLoginUserObject(Context context, String users) {
        PreferanceUtils.putStringInPreferences(context, users,
                AppConstant.USER);
    }

    /**
     * Get TimetableRecyclerRow From Preference
     *
     * @param context
     * @return
     */
    public static RegisterResponseDao getLoginUserObject(Context context) {
        String user = PreferanceUtils.getStringFromPreferences(context, "",
                AppConstant.USER);
        if (user != null)
            return new Gson().fromJson(user, RegisterResponseDao.class);
        else
            return null;
    }


    /**
     * @param context
     * @param object
     */
    public static void setContactListIntoPreferance(Context context, AppContactList object) {
        try {
            String contactListInString = new Gson().toJson(object).toString();
            PreferanceUtils.putStringInPreferences(context, contactListInString,
                    AppConstant.PREFERANCE_CONTACTS);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @param context
     * @return
     */
    public static AppContactList getContactListFromPreferance(Context context) {
        try {
            String json = PreferanceUtils.getStringFromPreferences(context, null,
                    AppConstant.PREFERANCE_CONTACTS);
            if (json != null) {
                return new Gson().fromJson(json, AppContactList.class);
            }
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return new AppContactList();
    }


    /**
     * @param context
     * @param list
     */
    public static void setBannerIntoPreferance(Context context, BannerData list) {
        try {
            String contactListInString = new Gson().toJson(list).toString();
            PreferanceUtils.putStringInPreferences(context, contactListInString,
                    AppConstant.PREFERANCE_BANNERS);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @param context
     * @return
     */
    public static BannerData getBannerFromPreferance(Context context) {
        BannerData bannerDataObject = null;
        try {
            String bannerData = PreferanceUtils.getStringFromPreferences(context, "",
                    AppConstant.PREFERANCE_BANNERS);
            if (bannerData != null && bannerData.length() > 0)
                bannerDataObject = new Gson().fromJson(bannerData, BannerData.class);
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return bannerDataObject;
    }


    public static void setChatDataToPreferance(Context context, ChatDao obj) {
        try {
            String contactListInString = new Gson().toJson(obj).toString();
            PreferanceUtils.putStringInPreferences(context, contactListInString,
                    AppConstant.PREFERANCE_CHAT);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static ChatDao getChatDataFromPreferance(Context ctx) {
        String chatData = PreferanceUtils.getStringFromPreferences(ctx, "",
                AppConstant.PREFERANCE_CHAT);
        ChatDao dataObject = null;
        try {
            if (chatData != null)
                dataObject = new Gson().fromJson(chatData, ChatDao.class);
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

        return dataObject;
    }

    public static void setPopUpDataToPreferance(Context context, ArticleDao obj) {
        try {
            String contactListInString = new Gson().toJson(obj).toString();
            PreferanceUtils.putStringInPreferences(context, contactListInString,
                    AppConstant.POP_UP_DATA);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static ArticleDao getPopUpDataToPreferance(Context ctx) {
        ArticleDao popUpObject = null;
        try {
            String popupData = PreferanceUtils.getStringFromPreferences(ctx, "",
                    AppConstant.POP_UP_DATA);
            if (popupData != null && popupData.length() > 0)
                popUpObject = new Gson().fromJson(popupData, ArticleDao.class);
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return popUpObject;
    }

    public static void setQucikBloxIdToPreferance(Context context, String id) {
        PreferanceUtils.putStringInPreferences(context, id,
                AppConstant.QUICK_BLOX_ID);
    }

    public static String getQucikBloxIdToPreferance(Context ctx) {
        return PreferanceUtils.getStringFromPreferences(ctx, "",
                AppConstant.QUICK_BLOX_ID);
    }

    public static void setHasNotification(Context context, boolean flag) {
        PreferanceUtils.putBooleanInPreferences(context, flag,
                AppConstant.NOTIFICATION_FLAG);
    }

    public static boolean getHasNotification(Context context) {
        return PreferanceUtils.getBooleanFromPreferences(context, false,
                AppConstant.NOTIFICATION_FLAG);
    }

    public static void setQbDialogInPreferance(Context context, ArrayList<QBChatDialog> list) {
        PreferanceUtils.putStringInPreferences(context, new Gson().toJson(list).toString(), AppConstant.QB_DIALOG_DATA);
    }

    public static ArrayList<QBChatDialog> getDialogFromPreferance(Context context) {
        ArrayList<QBChatDialog> list;
        String json = PreferanceUtils.getStringFromPreferences(context, "", AppConstant.QB_DIALOG_DATA);
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<QBChatDialog>>() {
        }.getType();
        if (Utils.isValidString(json)) {
            list = gson.fromJson(json, listType);
        } else {
            list = new ArrayList<>();
        }
        return list;
    }


    /**
     * @param isOfflineDataAvailable
     * @param ctx
     */
    public static void setIsOfflineDataAvailable(boolean isOfflineDataAvailable, Context ctx) {
        PreferanceUtils.putBooleanInPreferences(ctx,
                isOfflineDataAvailable,
                AppConstant.PREF_OFFLINE_DATA_AVAILABLE);
    }

    /**
     * @param ctx
     * @return
     */
    public static boolean getIsOfflineDataAvailable(Context ctx) {
        return PreferanceUtils.getBooleanFromPreferences(ctx,
                false,
                AppConstant.PREF_OFFLINE_DATA_AVAILABLE);
    }


    /**
     * @param context
     * @param list
     */
    /*public static void setKitties(Context context, List<Kitty> list) {
        PreferanceUtils.putStringInPreferences(context,
                new Gson().toJson(list).toString(), AppConstant.PREF_KITTIES);
    }*/

    /**
     * @param context
     * @return
     *//*
    public static List<Kitty> getKitties(Context context) {
        List<Kitty> list = null;
        try {
            String json = PreferanceUtils.getStringFromPreferences(context, "",
                    AppConstant.PREF_KITTIES);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Kitty>>() {
            }.getType();
            if (Utils.isValidString(json)) {
                list = gson.fromJson(json, listType);
            } else {
                list = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }*/


    /**
     * get is Already Login
     *
     * @param context
     * @return
     */
    public static boolean hasDeviceID(Context context) {
        return PreferanceUtils.getBooleanFromPreferences(context, false,
                AppConstant.PREF_DEVICE_ID);
    }

    /**
     * Set is Already Login
     *
     * @param context
     * @param isRegistered
     */
    public static void setDeviceID(Context context, boolean isRegistered) {
        PreferanceUtils.putBooleanInPreferences(context, isRegistered,
                AppConstant.PREF_DEVICE_ID);
    }
}
