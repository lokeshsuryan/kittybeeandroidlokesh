package com.kittyapplication.custom;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kittyapplication.utils.FontCacheUtils;

/**
 * Created by Dhaval Riontech on 6/8/16.
 */
public class CustomTextViewNormal extends TextView {
    
    public CustomTextViewNormal(Context context) {
        super(context);
        applyCustomFont(context);
    }

    public CustomTextViewNormal(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context);
    }

    public CustomTextViewNormal(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyCustomFont(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomTextViewNormal(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void applyCustomFont(Context context) {
        Typeface customFont = FontCacheUtils.getTypeface("ufonts_com_gotham_book.ttf", context);
        setTypeface(customFont);
    }
}
