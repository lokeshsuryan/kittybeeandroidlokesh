package com.kittyapplication.chat.utils.qb;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.callback.OnFileUploadListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by MIT on 8/22/2016.
 */
public class FileUploadUtils {
    public static void uploadFile(final File item, final OnFileUploadListener listener) {
        listener.onUploading(item, 1);
        ChatHelper.getInstance().loadFileAsAttachment(item, new QBEntityCallback<QBAttachment>() {
            @Override
            public void onSuccess(QBAttachment result, Bundle params) {
                listener.onUploaded(item, result);
            }

            @Override
            public void onError(QBResponseException e) {
                listener.onUploadError(item, e);
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(final int progress) {
                listener.onUploading(item, progress);
            }
        });
    }
}
