package com.kittyapplication.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kittyapplication.R;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.Utils;

/**
 * Created by Pintu Riontech on 22/8/16.
 * vaghela.pintu31@gmail.com
 */
public class AboutUsActivity extends BaseActivity {
    private static final String TAG = AboutUsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(AboutUsActivity.this).inflate(
                R.layout.activity_about_us, null);
        addLayoutToContainer(view);
        hideLeftIcon();
        hideBottomLayout();


        showProgressDialog();
        WebView webView = (WebView) view.findViewById(R.id.wvAboutUse);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                hideProgressDialog();
            }
        });
        webView.loadUrl(AppConstant.ABOUT_US_URL);
        //TODO
        //webView.loadUrl(AppConstant.ABOUT_US_PRIVACY_URL);
    }

    @Override
    String getActionTitle() {
        return getResources().getString(R.string.about_us);
    }

    @Override
    boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (hasDrawer()) {
                    toggle();
                } else {
                    onBackPressed();
                }
                break;

            case R.id.menu_home_jump:
                Utils.openActivity(this, HomeActivity.class);
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }
}
