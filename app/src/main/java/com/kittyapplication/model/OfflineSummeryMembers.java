package com.kittyapplication.model;

import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 29/9/16.
 */
public class OfflineSummeryMembers {

    private String kittynext;

    private String count;

    private List<SummaryListDao> data;

    public String getKittynext() {
        return kittynext;
    }

    public void setKittynext(String kittynext) {
        this.kittynext = kittynext;
    }

    public List<SummaryListDao> getData() {
        return data;
    }

    public void setData(List<SummaryListDao> data) {
        this.data = data;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
