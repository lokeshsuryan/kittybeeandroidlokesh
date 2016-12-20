package com.kittyapplication.ui.view;

import com.kittyapplication.model.RegisterResponseDao;

/**
 * Created by Dhaval Riontech on 8/8/16.
 */
public interface SignUpView {
    void gotoNextPage(String message, String mobile);

    void gotoHomePage(RegisterResponseDao dao);
}
