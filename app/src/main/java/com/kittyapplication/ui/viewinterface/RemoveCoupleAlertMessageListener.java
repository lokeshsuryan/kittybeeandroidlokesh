package com.kittyapplication.ui.viewinterface;

import com.kittyapplication.model.ContactDaoWithHeader;

/**
 * Created by Dhaval Soneji Riontech on 1/9/16.
 */
public interface RemoveCoupleAlertMessageListener {
    void onClickYes(ContactDaoWithHeader user);

    void onClickNo();
}
