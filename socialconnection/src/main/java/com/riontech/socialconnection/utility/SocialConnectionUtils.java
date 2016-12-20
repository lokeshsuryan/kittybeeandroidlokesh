package com.riontech.socialconnection.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

/**
 * Created by Pintu Riontech on 25/5/16.
 * vaghela.pintu31@gmail.com
 */
public class SocialConnectionUtils {
    private static final String TAG = SocialConnectionUtils.class.getSimpleName();

    /**
     * Check Internet Available or Not
     */
    public static boolean checkInternetConnection(final Context context) {
        final ConnectivityManager conMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Show Toast
     *
     * @param message
     * @param context
     */
    public static void showToast(final String message, final Context context) {
        final Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setText(message);
        toast.show();
    }

}