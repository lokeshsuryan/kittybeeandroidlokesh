package com.kittyapplication.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kittyapplication.utils.ImageUtils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by Dhaval Soneji : sonejidhavalm@gmail.com on 23/8/16.
 */
public class ImageLoaderListenerUniversal implements ImageLoadingListener {

    private Context mContext;
    private ProgressBar mProgressBar;
    private ImageView mImageView;
    private String mImgDisplayOnFail;
    private boolean mHasProgressBar;

    public ImageLoaderListenerUniversal(Context context, ProgressBar progressBar, ImageView imageView, String imgDisplayOnFail) {
        mContext = context;
        mProgressBar = progressBar;
        mImageView = imageView;
        mImgDisplayOnFail = imgDisplayOnFail;
        mHasProgressBar = true;
    }

    public ImageLoaderListenerUniversal(Context context, ImageView imageView, String imgDisplayOnFail) {
        mContext = context;
        mImageView = imageView;
        mImgDisplayOnFail = imgDisplayOnFail;
        mHasProgressBar = false;
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        if (mHasProgressBar)
            mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        if (mHasProgressBar) {
            mProgressBar.setVisibility(View.GONE);
            ImageUtils.getImageLoader(mContext).displayImage(mImgDisplayOnFail, mImageView);
        }else{
            mImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        if (mHasProgressBar)
            mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {
        if (mHasProgressBar)
            mProgressBar.setVisibility(View.GONE);
    }
}
