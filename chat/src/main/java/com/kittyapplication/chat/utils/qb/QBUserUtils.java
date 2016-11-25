package com.kittyapplication.chat.utils.qb;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.kittyapplication.chat.utils.ImageLoaderUtils;
import com.kittyapplication.core.CoreApp;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.io.InputStream;

/**
 * Created by MIT on 8/29/2016.
 */
public class QBUserUtils {
    private static final String TAG = QBUserUtils.class.getSimpleName();

    public static void updateProfilePicture(final QBUser user, String uri, final QBEntityCallback<QBUser> callback) {
        if (uri != null && !uri.contains("http")) {
            Log.d(TAG, "File upload url" + uri);
//            File file = new File(ImageLoaderUtils.getPath(Uri.parse(uri)));
//        Log.d(TAG, "File path " + file.getAbsolutePath());
            QBContent.uploadFileTask(new File(uri), false, null, new QBEntityCallback<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle params) {
                    Log.d(TAG, "Uploaded QBFile " + qbFile.toString());
                    int uploadedFileID = qbFile.getId();
                    user.setFileId(uploadedFileID);
                    updateUserProfile(user, callback);
                }

                @Override
                public void onError(QBResponseException errors) {
                    Log.e(TAG, "Uploaded QBFile " + errors.getMessage());
                }
            }, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {
                    Log.e(TAG, "Uploaded QBFile progress" + progress);
                }
            });
        }
    }

    public static void downloadUserProfilePicture(int profileId, QBEntityCallback<InputStream> callback) {
        QBContent.downloadFileById(profileId, callback, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {

            }
        });
    }

    public static void updateUserProfile(QBUser user, QBEntityCallback<QBUser> callback) {
        QBUsers.updateUser(user, callback);
    }
}
