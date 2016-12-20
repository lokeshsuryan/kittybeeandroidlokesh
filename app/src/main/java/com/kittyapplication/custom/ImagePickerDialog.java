package com.kittyapplication.custom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;

import com.kittyapplication.R;
import com.kittyapplication.cropimage.CropImage;
import com.kittyapplication.listener.ImagePickerListener;
import com.kittyapplication.utils.AppLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Pintu Riontech on 12/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ImagePickerDialog {
    private static final String TAG = ImagePickerDialog.class.getSimpleName();
    private static final int SELECT_FILE = 2;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_CROP = 3;
    private Activity mActivity;
    private File mTempFile, mFileTemp;
    private Bitmap mBitmapProfile;
    public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.png";
    private ImagePickerListener mListener;

    public ImagePickerDialog(Activity activity, ImagePickerListener listener) {
        mActivity = activity;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(),
                    TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(mActivity.getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }
        mListener = listener;
        //selectImage();

    }


    /**
     * @param uri
     * @return
     */
    public String getPath(final Uri uri) {
        final String[] projection = {MediaStore.Images.Media.DATA};
        final Cursor cursor = mActivity.getContentResolver().query(uri, projection, null,
                null, null);
        final int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     *
     */
    public void shareFile() {
        try {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(final Void... params) {
                    saveInSdcard();
                    return null;
                }

                @Override
                protected void onPostExecute(final Void result) {
                    super.onPostExecute(result);
                }

            }.execute();
        } catch (final Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
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
                    .getAbsolutePath() + "/cravy");
            if (!dir.exists()) {
                dir.mkdir();
            }
            mTempFile = new File(dir, System.currentTimeMillis() + ".jpeg");
            fo = new FileOutputStream(mTempFile);

            mBitmapProfile.compress(Bitmap.CompressFormat.JPEG, 100, fo);
            fo.flush();
            fo.close();
            MediaStore.Images.Media.insertImage(mActivity.getContentResolver(),
                    mTempFile.getAbsolutePath(), mTempFile.getName(),
                    mTempFile.getName());
        } catch (final Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        } finally {
            try {
                fo.close();
            } catch (final Throwable e) {
            }
        }
    }

    /**
     * @param bitmap
     * @return
     */
    private Bitmap getMutableBitmap(final Bitmap bitmap) {
        try {
            final Bitmap mutBmp = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            final Canvas can = new Canvas(mutBmp);
            can.drawBitmap(bitmap, 0, 0, new Paint());
            return mutBmp;
        } catch (final Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     */
    private void cropCapturedImage() {
        Intent intent = new Intent(mActivity, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 3);
        intent.putExtra(CropImage.ASPECT_Y, 2);
        mActivity.startActivityForResult(intent, REQUEST_CROP);
    }

    /**
     *
     */
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Gallery",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.SettingsDialogTheme);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int item) {
                if (items[item].equals("Take Photo")) {
                    final Intent intent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    final File f = new File(Environment
                            .getExternalStorageDirectory(),
                            TEMP_PHOTO_FILE_NAME);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    mActivity.startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Gallery")) {
                    final Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    mActivity.startActivityForResult(
                            Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }


    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        if (resultCode == mActivity.RESULT_OK) {
            File f = new File(Environment.getExternalStorageDirectory()
                    .toString());
            if (requestCode == REQUEST_CAMERA) {
                for (final File temp : f.listFiles()) {
                    if (temp.getName().equals(TEMP_PHOTO_FILE_NAME)) {
                        f = temp;
                        break;
                    }
                }

                cropCapturedImage();
            } else if (requestCode == SELECT_FILE) {
                try {
                    InputStream inputStream = mActivity.getContentResolver()
                            .openInputStream(data.getData());
                    FileOutputStream fileOutputStream = new FileOutputStream(
                            mFileTemp);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();
                    cropCapturedImage();
                } catch (Exception e) {
                    AppLog.e(TAG, e.getMessage(), e);
                }

            } else if (requestCode == REQUEST_CROP) {
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    AppLog.e(TAG, "Path nullll");
                    return;
                }

                mBitmapProfile = BitmapFactory.decodeFile(mFileTemp.getPath());
                mListener.getBitmapImageFromPhone(mBitmapProfile);
                f.delete();
            }
        } else {
            AppLog.e(TAG, "error in pic...");
        }
    }

    /**
     * @param is
     * @param os
     */
    public static void copyStream(final InputStream is, final OutputStream os) {
        final int buffer_size = 1024;
        try {
            final byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                // Read byte from input stream
                final int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                // Write byte from output stream
                os.write(bytes, 0, count);
            }
        } catch (final Exception ex) {
            AppLog.e(TAG, ex.getMessage(), ex);
        }
    }


    public void getImagesFrom(int type) {
        if (type == 0) {
            final Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            final File f = new File(Environment
                    .getExternalStorageDirectory(),
                    TEMP_PHOTO_FILE_NAME);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            mActivity.startActivityForResult(intent, REQUEST_CAMERA);
        } else if (type == 1) {
            final Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            mActivity.startActivityForResult(
                    Intent.createChooser(intent, "Select File"),
                    SELECT_FILE);
        }
    }

    public String getPath() {
        String path = "";
        if (mFileTemp != null) {
            path = mFileTemp.getPath();
        }
        return path;
    }
}
