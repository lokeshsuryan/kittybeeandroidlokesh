package com.kittyapplication.ui.viewmodel;

import com.kittyapplication.model.QBSignUp;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.utils.AppLog;
import com.quickblox.users.model.QBUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by MIT on 8/17/2016.
 */
public abstract class QBUserViewModel {
    private static String TAG = QBUserViewModel.class.getSimpleName();

    public void addQBUser(final QBUser qbUser) {
        QBSignUp qbSignUp = new QBSignUp();
        qbSignUp.setFullName(qbUser.getFullName());
        qbSignUp.setLogin(qbUser.getLogin());
        qbSignUp.setId("" + qbUser.getId());
        onQBAdded();
    }

    protected abstract void onQBAdded();
}
