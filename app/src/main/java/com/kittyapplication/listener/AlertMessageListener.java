package com.kittyapplication.listener;

import com.kittyapplication.model.ContactDao;

/**
 * Created by Pintu Riontech on 15/8/16.
 * vaghela.pintu31@gmail.com
 */
public interface AlertMessageListener {

    void onClickYes(ContactDao user);

    void onClickNo();
}
