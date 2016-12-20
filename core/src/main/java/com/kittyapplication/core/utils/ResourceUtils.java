package com.kittyapplication.core.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;


public class ResourceUtils {


    public static String getString(Context context, @StringRes int stringId) {
        return context.getString(stringId);
    }

    public static Drawable getDrawable(Context context, @DrawableRes int drawableId) {
        return ContextCompat.getDrawable(context, drawableId);
    }

    public static int getColor(Context context, @ColorRes int colorId) {
        return ContextCompat.getColor(context, colorId);
    }

    public static int getDimen(Context context, @DimenRes int dimenId) {
        return (int) context.getResources().getDimension(dimenId);
    }

    public static int dpToPx(Context context, int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(Context context, int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static Drawable changeResourceColor(Context context, int color, int iconResId) {
        final Drawable icon = getDrawable(context, iconResId);
        icon.setColorFilter(getColor(context, color), PorterDuff.Mode.SRC_ATOP);
        return icon;
    }

    public static Drawable changeResourceColor(Context context, int color, Drawable iconResId) {
//        final Drawable icon = getDrawable(context, iconResId);
        iconResId.setColorFilter(getColor(context, color), PorterDuff.Mode.SRC_ATOP);
        return iconResId;
    }

}
