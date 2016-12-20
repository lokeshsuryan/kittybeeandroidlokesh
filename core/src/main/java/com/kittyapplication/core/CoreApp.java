package com.kittyapplication.core;

import android.app.Application;

public class CoreApp extends Application {

    private static CoreApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static synchronized CoreApp getInstance() {
        return instance;
    }

}