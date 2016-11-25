package com.kittyapplication.listener;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 18/10/16.
 */

public interface SearchListener {

    void getSearchString(String str);

    void onSearchBarVisible();

    void onSearchBarHide();
}
