package com.kittyapplication.ui.fragment;

import android.support.v4.app.Fragment;

/**
 * Created by MIT on 11/23/2016.
 */

abstract public class BaseFragment extends Fragment {
    private boolean isFilter;
    private int skip;
    private int limit;

    abstract public void applyFilter(String query);

    public void setFilter(boolean filter) {
        isFilter = filter;
    }

    protected boolean isFilter() {
        return isFilter;
    }

    protected void setLimit(int limit) {
        this.limit = limit;
    }

    protected int getLimit() {
        return limit;
    }

    protected void setSkip(int skip) {
        this.skip = skip;
    }

    protected int getSkip() {
        return skip;
    }

    public abstract void reloadData();
}
