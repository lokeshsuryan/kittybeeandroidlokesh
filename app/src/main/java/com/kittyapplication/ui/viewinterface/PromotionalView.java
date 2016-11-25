package com.kittyapplication.ui.viewinterface;

import com.kittyapplication.model.PromotionalDao;

import java.util.List;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public interface PromotionalView {

    void getDataList(List<PromotionalDao> list);

    void hideMainLayout();

    void showMainLayout();
}
