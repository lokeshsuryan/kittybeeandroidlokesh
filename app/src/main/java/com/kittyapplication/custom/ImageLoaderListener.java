package com.kittyapplication.custom;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * @auther Pintu vaghela
 */
public class ImageLoaderListener implements ImageLoadingListener {

    private ProgressBar mPbLoader;

    public ImageLoaderListener(ProgressBar pbdialog) {
        mPbLoader = pbdialog;
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        mPbLoader.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        mPbLoader.setVisibility(View.GONE);
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        mPbLoader.setVisibility(View.GONE);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        mPbLoader.setVisibility(View.GONE);
    }
}
