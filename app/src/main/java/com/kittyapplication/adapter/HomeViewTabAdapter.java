package com.kittyapplication.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.kittyapplication.R;
import com.kittyapplication.ui.fragment.BeeChatFragment;
import com.kittyapplication.ui.fragment.ContactsFragment;
import com.kittyapplication.ui.fragment.KittiesFragment;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public class HomeViewTabAdapter extends FragmentPagerAdapter {

    private static String TAG = HomeViewTabAdapter.class.getSimpleName();

    private Context mContext;


    private SparseArray<Fragment> registeredFragments = new SparseArray<>();

    public HomeViewTabAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        this.mContext = ctx;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getStringArray(R.array.home_tab_title)[position];
    }

    @Override
    public int getCount() {
        return mContext.getResources().getStringArray(R.array.home_tab_title).length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return KittiesFragment.newInstance(position);
            case 1:
                return BeeChatFragment.newInstance(position);
            case 2:
                return ContactsFragment.newInstance(position);
            default:
                return BeeChatFragment.newInstance(position);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}