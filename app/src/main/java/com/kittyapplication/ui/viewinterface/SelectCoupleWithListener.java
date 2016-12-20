package com.kittyapplication.ui.viewinterface;

import com.kittyapplication.model.ContactDao;

/**
 * Created by Dhaval Soneji Riontech on 1/9/16.
 */
public interface SelectCoupleWithListener {
    void getSelectedCoupleWithMember(ContactDao memberFromDialog, ContactDao memberFromList);
    void dismissDialog();
}
