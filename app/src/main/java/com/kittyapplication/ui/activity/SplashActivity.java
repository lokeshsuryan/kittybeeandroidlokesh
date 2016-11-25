package com.kittyapplication.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.kittyapplication.R;
import com.kittyapplication.chat.ui.activity.ChatSplashActivity;
import com.kittyapplication.core.utils.ResourceUtils;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;

/**
 * Created by Dhaval Riontech on 7/8/16.
 */
public class SplashActivity extends ChatSplashActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_SCREEN_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imgbg = (ImageView) findViewById(R.id.imgSignUpBackGround);
        ImageUtils.getImageLoader(this).displayImage("drawable://"
                + R.drawable.login_screen_bg, imgbg);

    }

    @Override
    protected View getRootView() {
        return findViewById(R.id.rlSplashRoot);
    }

    protected String getAppName() {
        return ResourceUtils.getString(R.string.app_name);
    }

    protected void proceedToTheNextActivity() {
        if (PreferanceUtils.isRegistered(SplashActivity.this)) {
            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        } else {
            startActivity(new Intent(SplashActivity.this, SignUpActivity.class));
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

//        new Handler().postDelayed(runnable, SPLASH_SCREEN_TIME_OUT);
    }

    /**
     *
     */
    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            proceedToTheNextActivity();
        }
    };
}
