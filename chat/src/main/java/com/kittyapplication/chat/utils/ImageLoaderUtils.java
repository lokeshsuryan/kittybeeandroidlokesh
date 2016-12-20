package com.kittyapplication.chat.utils;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.kittyapplication.core.CoreApp;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by MIT on 8/22/2016.
 */
public class ImageLoaderUtils {
    private static ImageLoader imageLoader;

    public static ImageLoader getImageLoader(Context context) {
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                context).defaultDisplayImageOptions(getDisplayOption());
        if (imageLoader == null) {
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(builder.build());
        }
        return imageLoader;
    }

    public static DisplayImageOptions displayOptions;

    public static DisplayImageOptions getDisplayOption() {

        if (displayOptions == null)
            displayOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.ALPHA_8).build();
        return displayOptions;
    }

    public static DisplayImageOptions getDisplayOption(int resId) {
        if (displayOptions == null)
            displayOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .showImageForEmptyUri(resId)
                    .showImageOnFail(resId)
                    .showImageOnLoading(resId)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.ALPHA_8).build();
        return displayOptions;
    }

    public static DisplayImageOptions getDisplayOption(Drawable drawable) {
        if (displayOptions == null)
            displayOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .showImageForEmptyUri(drawable)
                    .showImageOnFail(drawable)
                    .showImageOnLoading(drawable)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .bitmapConfig(Bitmap.Config.ALPHA_8).build();
        return displayOptions;
    }

    /**
     * Get Rounded Corner Bitmap
     *
     * @return
     */
    public static DisplayImageOptions getDisplayOptionProfileRoundedCorner() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new RoundedBitmapDisplayer(10)) //rounded corner bitmap
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        return options;
    }


    public static String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = CoreApp.getInstance().getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            Log.e("ImageLoaderUtils", "cursor::" + cursor);
            return uri.toString();
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public static RoundedBitmapDrawable getCircleBitmapDrawable(Bitmap bitmap) {
        Resources resources = CoreApp.getInstance().getResources();
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(resources, bitmap);
        try {
            drawable.setCornerRadius(Math.max(bitmap.getWidth() / 2, bitmap.getHeight() / 2));
            drawable.setCircular(true);
            drawable.setAntiAlias(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

    public static ImageLoadingListener getImageLoadingListener(final ProgressBar progressBar) {
        ImageLoaderListener listener = new ImageLoaderListener(progressBar);
//        listener.setViewVisibility(hideView ? View.INVISIBLE : View.VISIBLE);
        return listener;
    }

    public static ImageLoadingListener getImageLoadingListener(final ProgressBar progressBar, final RelativeLayout relativeLayout) {
        ImageLoaderListener1 listener = new ImageLoaderListener1(progressBar, relativeLayout);
//        listener.setViewVisibility(hideView ? View.INVISIBLE : View.VISIBLE);
        return listener;
    }

    private static class ImageLoaderListener implements ImageLoadingListener {

        private ProgressBar progressBar;
        private int visibility = View.VISIBLE;

        public ImageLoaderListener(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        public void setViewVisibility(int visibility) {
            this.visibility = visibility;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            view.setVisibility(visibility);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            view.setVisibility(visibility);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            view.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            view.setVisibility(visibility);
            progressBar.setVisibility(View.GONE);
        }
    }

    private static class ImageLoaderListener1 implements ImageLoadingListener {

        private RelativeLayout relativeLayout;
        private ProgressBar progressBar;
        private int visibility = View.VISIBLE;

        public ImageLoaderListener1(ProgressBar progressBar, RelativeLayout relativeLayout) {
            this.progressBar = progressBar;
            this.relativeLayout = relativeLayout;
        }

        public void setViewVisibility(int visibility) {
            this.visibility = visibility;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            view.setVisibility(visibility);
            progressBar.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            view.setVisibility(visibility);
            progressBar.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            view.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            view.setVisibility(visibility);
            progressBar.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.GONE);
        }
    }
}
