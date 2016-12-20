package com.kittyapplication.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import com.kittyapplication.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.ByteArrayOutputStream;

/**
 * Created by Dhaval Riontech on 9/8/16.
 */
public class ImageUtils {
    private static ImageLoader imageLoader;
    private static String TAG = ImageUtils.class.getSimpleName();

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static ImageLoader getImageLoader(Context context) {
        try {
            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                    context).defaultDisplayImageOptions(getDisplayOption());
            if (imageLoader == null) {
                imageLoader = ImageLoader.getInstance();
                imageLoader.init(builder.build());
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return imageLoader;
    }

    public static DisplayImageOptions displayOptions;

    @SuppressWarnings("deprecation")
    public static DisplayImageOptions getDisplayOption() {

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
//        options.inJustDecodeBounds = true;
//        .showImageOnLoading(CravyConstants.ON_LOADING)

        try {
            if (displayOptions == null)
                displayOptions = new DisplayImageOptions.Builder()
                        .cacheOnDisc(true)
                        .cacheInMemory(true)
                        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                        .showImageForEmptyUri(R.drawable.ic_user)
                        .showImageOnFail(R.drawable.ic_user)
                        .bitmapConfig(Bitmap.Config.ALPHA_8).build();

        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return displayOptions;
    }

    /**
     * Get Rounded Corner Bitmap
     *
     * @return
     */
    public static DisplayImageOptions getDisplayOptionProfileRoundedCorner() {
        DisplayImageOptions options = null;
        try {
            options = new DisplayImageOptions.Builder()
                    .displayer(new RoundedBitmapDisplayer(10)) //rounded corner bitmap
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return options;
    }

    public static String getRealPathFromURI(Uri uri, Context context) {
        Cursor cursor = context.getContentResolver().query(uri, null, null,
                null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public static Bitmap getMutableBitmap(Bitmap bitmap) {
        try {

            Bitmap mutBmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas can = new Canvas(mutBmp);
            can.drawBitmap(bitmap, 0, 0, new Paint());
            return mutBmp;
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getDeviceWidth(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        try {
            ((Activity) context).getWindowManager().getDefaultDisplay()
                    .getMetrics(displaymetrics);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return displaymetrics.widthPixels;
    }

    private static int greatestCommonFactor(int width, int height) {
        return (height == 0) ? width : greatestCommonFactor(height, width % height);
    }

    public static int[] getResolutionRation(int width, int height) {
        int factor = greatestCommonFactor(width, height);
        int widthRatio = width / factor;
        int heightRatio = height / factor;
        return new int[]{widthRatio, heightRatio};
    }

    public static byte[] getImageBytes(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap getImageFromBytes(byte[] bytes) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }
}