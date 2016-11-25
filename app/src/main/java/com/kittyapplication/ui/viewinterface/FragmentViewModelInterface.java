package com.kittyapplication.ui.viewinterface;

import android.support.v4.widget.SwipeRefreshLayout;

/**
 * Created by Pintu Riontech on 9/8/16.
 * vaghela.pintu31@gmail.com
 */
public interface FragmentViewModelInterface {

    void initRequest(boolean showLoader);

    void hideDialog();

    void showDialog();

    void hideRefreshLayout();

    void onSwipeRefreshReloadData(SwipeRefreshLayout layout);
}
