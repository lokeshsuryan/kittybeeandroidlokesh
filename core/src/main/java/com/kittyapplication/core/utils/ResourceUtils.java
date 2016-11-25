package com.kittyapplication.core.utils;

import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import com.kittyapplication.core.CoreApp;


public class ResourceUtils {

    public static String getString(@StringRes int stringId) {
        return CoreApp.getInstance().getString(stringId);
    }

    public static Drawable getDrawable(@DrawableRes int drawableId) {
        return ContextCompat.getDrawable(CoreApp.getInstance().getBaseContext(), drawableId);
    }

    public static int getColor(@ColorRes int colorId) {
        return ContextCompat.getColor(CoreApp.getInstance().getBaseContext(), colorId);
    }

    public static int getDimen(@DimenRes int dimenId) {
        return (int) CoreApp.getInstance().getResources().getDimension(dimenId);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static Drawable changeResourceColor(int color, int iconResId) {
        final Drawable icon = getDrawable(iconResId);
        icon.setColorFilter(getColor(color), PorterDuff.Mode.SRC_ATOP);
        return icon;
    }
}
