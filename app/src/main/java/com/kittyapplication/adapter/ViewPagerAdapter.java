package com.kittyapplication.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import com.kittyapplication.model.DiaryDao;
import com.kittyapplication.ui.fragment.KittyDiaryFragments;
import com.kittyapplication.utils.AppLog;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 18/10/16.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = ViewPagerAdapter.class.getSimpleName();

    private SparseArray<Fragment> registeredFragments = new SparseArray<>();
    private DiaryDao mData;
    private Context mContext;

    public ViewPagerAdapter(FragmentManager fm, Context context, DiaryDao data) {
        super(fm);
        mContext = context;
        mData = data;
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public Fragment getItem(int position) {
        return new KittyDiaryFragments().newInstance(position, mData);
    }

    @Override
    public int getCount() {
        int count = mData.getDiaryData().getKitties().size();
        return count;
    }

    /*@Override
    public Object instantiateItem(ViewGroup container, int position) {
        AppLog.e(TAG, "instantiateItem");
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        AppLog.e(TAG, "destroyItem");
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        AppLog.e(TAG, "getRegisteredFragment");
        return registeredFragments.get(position);
    }*/
}
