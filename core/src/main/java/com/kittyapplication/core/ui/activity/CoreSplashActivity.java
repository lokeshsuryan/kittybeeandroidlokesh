package com.kittyapplication.core.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;


public abstract class CoreSplashActivity extends CoreBaseActivity {
    private static final int SPLASH_DELAY = 1500;

    private static Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

    }

    protected abstract View getRootView();

    protected abstract String getAppName();

    protected abstract void proceedToTheNextActivity();

    protected void proceedToTheNextActivityWithDelay() {
        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                proceedToTheNextActivity();
            }
        }, SPLASH_DELAY);
    }

//    @Override
//    protected void showSnackbarError(View rootLayout, @StringRes int resId, Exception e, View.OnClickListener clickListener) {
//        rootLayout = findViewById(R.id.layout_root);
//        ErrorUtils.showSnackbar(rootLayout, resId, e, R.string.dlg_retry, clickListener);
//    }
}
