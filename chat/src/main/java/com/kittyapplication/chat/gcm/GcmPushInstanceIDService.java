package com.kittyapplication.chat.gcm;


import android.os.Bundle;
import android.util.Log;

import com.kittyapplication.chat.utils.Consts;
import com.kittyapplication.core.gcm.CoreGcmPushInstanceIDService;

public class GcmPushInstanceIDService extends CoreGcmPushInstanceIDService {
    private static final String TAG = GcmPushInstanceIDService.class.getSimpleName();

    @Override
    public String getSenderId() {
        return Consts.GCM_SENDER_ID;
    }
}
