package com.kittyapplication.ui.viewinterface;

import com.kittyapplication.model.CreateGroup;

/**
 * Created by Pintu Riontech on 13/8/16.
 * vaghela.pintu31@gmail.com
 */
public interface KittyRuleView {

    CreateGroup getData();

    void setData(CreateGroup data);

    void setImageViewData(String type);

    void getKittyDate(String date);

}
