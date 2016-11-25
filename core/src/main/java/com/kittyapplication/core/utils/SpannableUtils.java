package com.kittyapplication.core.utils;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Mithun on 1/18/2016.
 */
public class SpannableUtils {
    private String string;
    private String spannableStr;
    private String[] spannableArray;
    private boolean isClickable;
    private boolean hasUnderline;
    private boolean isColored;
    private boolean hasStrike;
    private boolean isHighlight;
    private boolean isBold;
    private int textColor;
    private int highlightColor;
    private ClickAction action;
    private TextView textView;
    private Drawable drawable;
    public SpannableUtils(TextView textView) {
        this.textView = textView;
    }

    public interface ClickAction {
        void onClickAction(String clickedStr);
        void onClickImage();
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public String[] getSpannableArray() {
        return spannableArray;
    }

    public void setSpannableArray(String[] spannableArray) {
        this.spannableArray = spannableArray;
    }

    public String getSpannableStr() {
        return spannableStr;
    }

    public void setSpannableStr(String spannableStr) {
        this.spannableStr = spannableStr;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setIsClickable(boolean isClickable) {
        this.isClickable = isClickable;
    }

    public boolean isHasUnderline() {
        return hasUnderline;
    }

    public void setHasUnderline(boolean hasUnderline) {
        this.hasUnderline = hasUnderline;
    }

    public boolean isColored() {
        return isColored;
    }

    public void setIsColored(boolean isColored) {
        this.isColored = isColored;
    }

    public boolean isHasStrike() {
        return hasStrike;
    }

    public void setHasStrike(boolean hasStrike) {
        this.hasStrike = hasStrike;
    }

    public boolean isHighlight() {
        return isHighlight;
    }

    public void setIsHighlight(boolean isHighlight) {
        this.isHighlight = isHighlight;
    }

    public boolean isBold() {
        return isBold;
    }

    public void setIsBold(boolean isBold) {
        this.isBold = isBold;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(int highlightColor) {
        this.highlightColor = highlightColor;
    }

    public ClickAction getAction() {
        return action;
    }

    public void setAction(ClickAction action) {
        this.action = action;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    private class ClickableText extends ClickableSpan{
        private String clicked;
        public ClickableText(String clicked){
            this.clicked = clicked;
        }

        @Override
        public void onClick(View view) {
            action.onClickAction(clicked);
        }
    }

    public void buildSpannable() {
        SpannableString styledString = new SpannableString(string);

        if (spannableStr != null) {
            setSpan(spannableStr, styledString);
        } else if (spannableArray != null) {
            for (int i = 0; i < spannableArray.length; i++) {
                setSpan(spannableArray[i], styledString);
            }
        } else {
            setSpan(string, styledString);
        }
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(styledString);
    }

    private void setSpan(String str, final SpannableString styledString) {

        int start = string.indexOf(str);
        int end = start + str.length();
        int exclusive = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

        if (isBold) {
            styledString.setSpan(new StyleSpan(Typeface.BOLD), start, end, exclusive);
        } else {
            styledString.setSpan(new StyleSpan(Typeface.NORMAL), start, end, exclusive);
        }

        if (isClickable) {
            styledString.setSpan(new ClickableText(str), start, end, exclusive);
        }

        if (isColored) {
            styledString.setSpan(new ForegroundColorSpan(getTextColor()), start, end, exclusive);
        }

        if (hasStrike) {
            styledString.setSpan(new StrikethroughSpan(), start, end, exclusive);
        }

        if (hasUnderline) {
            styledString.setSpan(new UnderlineSpan(), start, end, exclusive);
        }

        if (isHighlight) {
            styledString.setSpan(new BackgroundColorSpan(getHighlightColor()), start, end, exclusive);
        }

        if(drawable != null){
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            final ImageSpan imageSpan = new ImageSpan(drawable);
            styledString.setSpan(imageSpan,
                    styledString.length() - 1,
                    styledString.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            // Make image clickable
            int startImg = styledString.getSpanStart(imageSpan);
            int endImg = styledString.getSpanEnd(imageSpan);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    action.onClickImage();
                }
            };

            styledString.setSpan(clickableSpan, startImg, endImg, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }
}
