package com.kittyapplication.ui.activity;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.adapter.NotificationListAdapter;
import com.kittyapplication.custom.swipe.SwipeToDismissTouchListener;
import com.kittyapplication.custom.swipe.adapter.ListViewAdapter;
import com.kittyapplication.model.NotificationDao;
import com.kittyapplication.ui.viewmodel.NotificationViewModel;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Dhaval Riontech on 7/8/16.
 */
public class NotificationActivity extends BaseActivity {
    private ListView mLvNotification;
    private NotificationViewModel mViewModel;
    private ProgressDialog mDialog;
    private NotificationListAdapter mAdapter;
    private List<NotificationDao> mList;
    private ImageView mImgAdvertise;
    private TextView mTxtEmpty;
    private MenuItem menuItemDeleteAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(NotificationActivity.this).inflate(
                R.layout.fragment_notification, null);
        mLvNotification = (ListView) view.findViewById(R.id.lvNotification);
        mTxtEmpty = (TextView) view.findViewById(R.id.txtEmpty);
        mImgAdvertise = (ImageView) view.findViewById(R.id.imgAdvertisement);
        mViewModel = new NotificationViewModel(NotificationActivity.this);
        mViewModel.setBanner(mImgAdvertise);
        addLayoutToContainer(view);
        hideLeftIcon();
        showNotificationStar();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void init(ListView listView, List<NotificationDao> data) {
        final NotificationListAdapter adapter = new NotificationListAdapter(NotificationActivity.this, data);
        listView.setAdapter(adapter);
        final SwipeToDismissTouchListener<ListViewAdapter> touchListener =
                new SwipeToDismissTouchListener<>(
                        new ListViewAdapter(listView),
                        new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onPendingDismiss(ListViewAdapter recyclerView, int position) {

                            }

                            @Override
                            public void onDismiss(ListViewAdapter view, int position) {
                                mViewModel.deleteNotification(position, mList.get(position).getId());
                                adapter.remove(position);
                            }
                        });

        touchListener.setDismissDelay(AppConstant.TIME_TO_AUTOMATICALLY_DISMISS_ITEM);
        listView.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        listView.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss();
                } else {
                    if ((mList.get(position).getType().equalsIgnoreCase(AppConstant.ADD_VENUE)
                            || mList.get(position).getType().equalsIgnoreCase(AppConstant.EDIT_VENUE))) {
                        Intent intent = new Intent(NotificationActivity.this, NotificationCardActivity.class);
                        intent.putExtra(AppConstant.NOTIFICATION_CARD,
                                new Gson().toJson(mList.get(position)));
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    protected String getActionTitle() {
        return "NOTIFICATIONS";
    }

    @Override
    protected boolean isDisplayHomeAsUpEnabled() {
        return true;
    }

    @Override
    boolean hasDrawer() {
        return true;
    }

    public void showProgressDialog() {
        mDialog = new ProgressDialog(NotificationActivity.this);
        mDialog.setMessage(getResources().getString(R.string.loading_text));
        mDialog.show();
    }

    public void hideProgressDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setNotificationList(List<NotificationDao> data) {
        mTxtEmpty.setVisibility(View.GONE);
        mList = data;
        init(mLvNotification, data);
//        mAdapter = new NotificationListAdapter(NotificationActivity.this, data);
//        mLvNotification.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater = getMenuInflater();
        mInflater.inflate(R.menu.menu_mynotification, menu);
        menuItemDeleteAll = menu.findItem(R.id.menu_item_clear_all_notification);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear_all_notification:
                AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
                builder.setMessage(getResources().getString(R.string.alert_message_delete_notification)).setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                mViewModel.deleteAllNotification();
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                break;

            case android.R.id.home:
                if (hasDrawer()) {
                    hideNotificationStar();
                    toggle();
                } else {
                    onBackPressed();
                }
                break;

            case R.id.menu_home_jump:
                Utils.openActivity(this, HomeActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showEmptyLayout(String str) {
        menuItemDeleteAll.setVisible(false);
        mLvNotification.setVisibility(View.GONE);
        mTxtEmpty.setText(str);
        mTxtEmpty.setVisibility(View.VISIBLE);
    }

    public void hideEmptyLayout() {
        menuItemDeleteAll.setVisible(true);
        mLvNotification.setVisibility(View.VISIBLE);
        mTxtEmpty.setText("");
        mTxtEmpty.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferanceUtils.setHasNotification(this, false);
        hideNotificationStar();
    }
}
