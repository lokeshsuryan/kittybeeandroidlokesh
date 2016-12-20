package com.kittyapplication.chat.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.kittyapplication.chat.R;
import com.kittyapplication.chat.custom.CircluarProgressBarWithNumber;
import com.kittyapplication.chat.utils.ImageLoaderUtils;
import com.kittyapplication.core.CoreApp;
import com.kittyapplication.core.utils.ResourceUtils;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by MIT on 8/23/2016.
 */
public class AttachmentZoomActivity extends AppCompatActivity {

    private static final String EXTRA_URL = "url";

    private static final String FOLDER_NAME = "KittyBee";

    private RelativeLayout mRlMain;

    private String mURL;
    private static final String TAG = AttachmentZoomActivity.class.getSimpleName();

    private CircluarProgressBarWithNumber mProgressBar;

    public static void startMediaActivity(Activity activity, String url, View view) {
        Intent intent = new Intent(activity, AttachmentZoomActivity.class);
        intent.putExtra(AttachmentZoomActivity.EXTRA_URL, url);
        String transitionName = ResourceUtils.getString(CoreApp.getInstance(), R.string.zoom_image_transition);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, view, transitionName);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_attachment);

        mURL = getIntent().getStringExtra(EXTRA_URL);

        ImageView attachment = (ImageView) findViewById(R.id.imgZoomAttachment);
        mRlMain = (RelativeLayout) findViewById(R.id.rlAttachmentZoomActivity);
        mProgressBar =
                (CircluarProgressBarWithNumber) findViewById(R.id.pbZoomAttachment);

        ImageLoaderUtils.getImageLoader(this)
                .displayImage(mURL, attachment,
                        ImageLoaderUtils.getDisplayOption(),
                        ImageLoaderUtils.getImageLoadingListener(mProgressBar),
                        new ImageLoadingProgressListener() {
                            @Override
                            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                                int done = ((current * 100) / total);
                                mProgressBar.setProgress(done);
                            }

                        });

        findViewById(R.id.imgSaveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isStoragePermissionGranted()) {
                        if (mURL != null && mURL.length() > 0)
                            saveImage();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        });
    }


    private void saveImage() {
        try {
            File direct = new File(Environment.getExternalStorageDirectory()
                    + "/" + FOLDER_NAME);

            Calendar c = Calendar.getInstance();
            String fileName = String.valueOf(c.getTime()) + ".jpg";
            String appName = "KittyBee";


            if (!direct.exists()) {
                direct.mkdirs();
            }

            DownloadManager mgr = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

            Uri downloadUri = Uri.parse(mURL);
            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false).setTitle(appName)
                    .setDescription(fileName)
                    .setDestinationInExternalPublicDir("/" + FOLDER_NAME,
                            fileName);

            mgr.enqueue(request);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }


    /**
     * @return
     */
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                    ) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //resume tasks needing this permission
            saveImage();
        }
    }

    public void saveImageTask() {
        try {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(final Void... params) {
                    saveImage();
                    return null;
                }

                @Override
                protected void onPostExecute(final Void result) {
                    /*ErrorUtils.showSnackbar(mRlMain,
                            0, R.string.image_download_success, "");*/
                    super.onPostExecute(result);
                }

            }.execute();
        } catch (final Exception e) {
            Log.e("ATTACHMENT ACTIVITY", e.getMessage(), e);
        }
    }

    /**
     *
     */
    @SuppressLint({"InlinedApi", "NewApi"})
    private void saveInSdcard() {
        FileOutputStream fo = null;
        try {
            final File dir = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/" + FOLDER_NAME);
            if (!dir.exists()) {
                dir.mkdir();
            }

            URL url = new URL("http://....");
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            File mTempFile = new File(dir, System.currentTimeMillis() + ".jpeg");
            fo = new FileOutputStream(mTempFile);
            Bitmap mBitmapProfile = null;

            mBitmapProfile.compress(Bitmap.CompressFormat.JPEG, 100, fo);
            fo.flush();
            fo.close();
            MediaStore.Images.Media.insertImage(getContentResolver(),
                    mTempFile.getAbsolutePath(), mTempFile.getName(),
                    mTempFile.getName());
        } catch (final Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                fo.close();
            } catch (final Throwable e) {
            }
        }
    }
}
