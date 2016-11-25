package com.kittyapplication;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.kittyapplication.chat.App;
import com.kittyapplication.core.gcm.GooglePlayServicesHelper;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.services.GCMRegistrationService;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Dhaval Riontech on 8/8/16.
 */
public class AppApplication extends App {
    private static Context sContext;
    private static String mGCMId;
    private static ChatData dairyData;
    private CreateGroup group;
    private boolean isCreateGroup = false;
    private boolean isRefresh = false;
    private static int selectedPosition = -1;
    private String updatedGroupId;
    private boolean isQbRefresh = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        sContext = this;
    }

    public static Context getContext() {
        return sContext;
    }

    public static AppApplication getInstance() {
        return (AppApplication) sContext;
    }


    public static void setGCMID(GCMRegistrationService gcmRegistrationService, String token) {
        mGCMId = token;
    }

    public static String getGCMId() {
        return new GooglePlayServicesHelper().getGcmRegIdFromPreferences();
    }

    public static ChatData getDairyData() {
        return dairyData;
    }

    public static void setDairyData(ChatData dairyData) {
        AppApplication.dairyData = dairyData;
    }

    public CreateGroup getGroup() {
        return group;
    }

    public void setGroup(CreateGroup group) {
        this.group = group;
    }

    public boolean isCreateGroup() {
        return isCreateGroup;
    }

    public void setIsCreateGroup(boolean createGroup) {
        isCreateGroup = createGroup;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }

    public void setUpdatedGroupId(String updatedGroupId) {
        this.updatedGroupId = updatedGroupId;
    }

    public String getUpdatedGroupId() {
        return updatedGroupId;
    }

    public static int getSelectedPosition() {
        return selectedPosition;
    }

    public static void setSelectedPosition(int mSelectedObject) {
        AppApplication.selectedPosition = mSelectedObject;
    }

    public boolean isQbRefresh() {
        return isQbRefresh;
    }

    public void setQbRefresh(boolean qbRefresh) {
        isQbRefresh = qbRefresh;
    }
}
