package com.kittyapplication.chat.utils.qb;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.core.CoreApp;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.DeviceUtils;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.quickblox.auth.QBAuth;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;

import java.util.ArrayList;
import java.util.Date;

public class QbAuthUtils {

    private static final String TAG = QbAuthUtils.class.getSimpleName();

    public static boolean isSessionActive() {
        try {
            String token = QBAuth.getBaseService().getToken();
            Date expirationDate = QBAuth.getBaseService().getTokenExpirationDate();

            if (TextUtils.isEmpty(token)) {
                return false;
            }

            if (System.currentTimeMillis() >= expirationDate.getTime()) {
                return false;
            }

            return true;
        } catch (BaseServiceException ignored) {
        }

        return false;
    }

    public static void subscribeWithQBPushNotification(String gcmId) {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            /*QBPushNotifications.createSubscription(getQBSubscription(gcmId),
                    new QBEntityCallback<ArrayList<QBSubscription>>() {
                        @Override
                        public void onSuccess(ArrayList<QBSubscription> qbSubscriptions, Bundle bundle) {
                            Log.i(TAG, "Successfully subscribed for QB push messages");
                            SharedPrefsHelper.getInstance().save(Consts.QB_SUBSCRIPTION, true);
                        }

                        @Override
                        public void onError(QBResponseException error) {
                            Log.w(TAG, "Unable to subscribe for QB push messages; " + error.toString());
                            SharedPrefsHelper.getInstance().save(Consts.QB_SUBSCRIPTION, false);
                        }
                    });*/


            QBPushNotifications.createSubscription(getQBSubscription(gcmId)).performAsync(
                    new QBEntityCallback<ArrayList<QBSubscription>>() {
                        @Override
                        public void onSuccess(ArrayList<QBSubscription> qbSubscriptions, Bundle bundle) {
                            Log.i(TAG, "Successfully subscribed for QB push messages");
                            SharedPrefsHelper.getInstance().save(Consts.QB_SUBSCRIPTION, true);

                        }

                        @Override
                        public void onError(QBResponseException error) {
                            Log.w(TAG, "Unable to subscribe for QB push messages; " + error.toString());
                            SharedPrefsHelper.getInstance().save(Consts.QB_SUBSCRIPTION, false);
                        }
                    });
        }
    }

    public static void subscribeWithQBPushNotification(String gcmId, QBEntityCallback<ArrayList<QBSubscription>> callback) {
        QBPushNotifications.createSubscription(getQBSubscription(gcmId)).performAsync(callback);
    }

    private static QBSubscription getQBSubscription(String gcmId) {
        QBSubscription qbSubscription = new QBSubscription();
        qbSubscription.setNotificationChannel(QBNotificationChannel.GCM);
        qbSubscription.setDeviceUdid(DeviceUtils.getDeviceUid());
        qbSubscription.setRegistrationID(gcmId);
        qbSubscription.setEnvironment(QBEnvironment.DEVELOPMENT); // Don't forget to change QBEnvironment to PRODUCTION when releasing application
        return qbSubscription;
    }
}
