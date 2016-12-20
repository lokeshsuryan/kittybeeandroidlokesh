package com.kittyapplication.chat.utils.qb.callback;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.core.exception.QBResponseException;

import java.io.File;

/**
 * Created by MIT on 8/22/2016.
 */
public interface OnFileUploadListener {
    void onUploaded(File file, QBAttachment attachment);
    void onUploadError(File file, QBResponseException ex);
    void onUploading(File file, int progress);
}
