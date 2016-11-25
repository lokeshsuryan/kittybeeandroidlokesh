package com.kittyapplication.utils;

import com.google.gson.Gson;
import com.kittyapplication.core.CoreApp;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.kittyapplication.model.RegisterResponseDao;

/**
 * Created by MIT on 10/24/2016.
 */
public class LoginUserPrefHolder {
    private RegisterResponseDao user;
    private static LoginUserPrefHolder instance;

    public static LoginUserPrefHolder getInstance() {
        if (instance == null)
            instance = new LoginUserPrefHolder();

        return instance;
    }

    private LoginUserPrefHolder() {
        user = PreferanceUtils.getLoginUserObject(CoreApp.getInstance());
//        String json = SharedPrefsHelper.getInstance().get(AppConstant.USER);
//        user = new Gson().fromJson(json, RegisterResponseDao.class);
    }

    public RegisterResponseDao getUser() {
        return user;
    }

    public void saveUser(RegisterResponseDao user) {
        String strUser = new Gson().toJson(user);
        PreferanceUtils.setLoginUserObject(CoreApp.getInstance(), strUser);
    }
}
