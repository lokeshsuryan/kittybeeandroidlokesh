package com.kittyapplication.ui.viewmodel;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.DrawerAdapter;
import com.kittyapplication.ui.activity.AboutUsActivity;
import com.kittyapplication.ui.activity.BaseActivity;
import com.kittyapplication.ui.activity.CalendarActivity;
import com.kittyapplication.ui.activity.ContactUsActivity;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.activity.NotesActivity;
import com.kittyapplication.ui.activity.NotificationActivity;
import com.kittyapplication.ui.activity.ProfileActivity;
import com.kittyapplication.ui.activity.PromotionalActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;

/**
 * Created by Pintu Riontech on 7/8/16.
 * vaghela.pintu31@gmail.com
 */
public class BaseViewModel {
    private static final String TAG = BaseViewModel.class.getSimpleName();
    private BaseActivity mActivity;

    public BaseViewModel(BaseActivity activity) {
        mActivity = activity;
    }

    /**
     * Set up navigation View Item
     *
     * @param view
     */
    public void setUpDrawerItem(DrawerLayout view) {
        ListView lvDrawerItems = (ListView) view.findViewById(R.id.lvDrawer);
        lvDrawerItems.setAdapter(new DrawerAdapter(mActivity));
        lvDrawerItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onDrawerItemClickEvent(position);
            }
        });
    }

    /**
     * on Drawer Item Click Event
     *
     * @param pos
     */
    private void onDrawerItemClickEvent(int pos) {
        mActivity.toggle();
        switch (pos) {
            //home
            case 0:
                if (mActivity instanceof HomeActivity) {
                    AppLog.d(TAG, "HOME ACTIVITY");
                } else {
                    mActivity.startActivity(new Intent(mActivity, HomeActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                }//mActivity.finish();
                break;

            //my profile
            case 1:
                mActivity.startActivity(new Intent(mActivity, ProfileActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            //notification
            case 2:
                mActivity.startActivity(new Intent(mActivity, NotificationActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            //add group
            case 3:
                AlertDialogUtils.showCreateKittyDialog(mActivity);
                break;

            //my kitty
            case 4:
                mActivity.startActivity(new Intent(mActivity, CalendarActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            //personal getNotes
            case 5:
                mActivity.startActivity(new Intent(mActivity, NotesActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            //contact us
            case 6:
                Intent intent = new Intent(mActivity, ContactUsActivity.class);
                intent.putExtra(AppConstant.USER_PROFILE_ID, "124")
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                mActivity.startActivity(intent);
                break;

            //about us
            case 7:
                Intent aboutUsIntent = new Intent(mActivity, AboutUsActivity.class);
                mActivity.startActivity(aboutUsIntent);
                break;
        }
    }

    /**
     * on Bottom menu click event
     *
     * @param pos
     */
    public void onBottomItemSelect(int pos) {
        if (mActivity instanceof PromotionalActivity) {
            ((PromotionalActivity) mActivity).finish();
        }
        Intent intent = new Intent(mActivity, PromotionalActivity.class);
        AppLog.d(TAG, "postion" + pos);
        intent.putExtra("pos", pos);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        mActivity.startActivity(intent);
    }
}
