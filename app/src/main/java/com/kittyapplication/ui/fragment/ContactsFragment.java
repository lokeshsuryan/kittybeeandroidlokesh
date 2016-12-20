package com.kittyapplication.ui.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.adapter.ContactAdapter;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.ui.view.DividerItemDecoration;
import com.kittyapplication.ui.viewmodel.ContactViewModel;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ContactsFragment extends BaseFragment {
    private static final String TAG = ContactsFragment.class.getSimpleName();
    private View mRootView;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private CustomEditTextNormal mEdtSearch;
    private ContactViewModel mViewModel;
    private ContactAdapter mAdapter;
    private ImageView mImgAdvertise;
    private boolean isSearch = false;
    private TextView mTxtEmpty;
    //    private ContactReceiver mContactReceiver;
    public ProgressBar mPbLoader;

    public static ContactsFragment newInstance(int pos) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putInt("position", pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel.init();
        mViewModel.onSwipeRefreshReloadData(mRefreshLayout);
        mViewModel.setBanner(mImgAdvertise);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_contact, container, false);

        mViewModel = new ContactViewModel(ContactsFragment.this);

        mRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.srfChatList);
        mEdtSearch = (CustomEditTextNormal) mRootView.findViewById(R.id.edtSearchChatFragment);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.lvChatList);
        int orientation = LinearLayoutManager.HORIZONTAL;
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), orientation);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mImgAdvertise = (ImageView) mRootView.findViewById(R.id.imgAdvertisement);
        mTxtEmpty = (TextView) mRootView.findViewById(R.id.txtEmpty);
        mTxtEmpty.setText(getActivity().getResources().getString(R.string.no_data_found));
        mPbLoader = (ProgressBar) mRootView.findViewById(R.id.pbLoaderContactFragment);
        mPbLoader.setVisibility(View.GONE);
        return mRootView;
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                AppLog.d(TAG, "home click");
                ((HomeActivity) getActivity()).setDrawerItem();
                break;


            case R.id.menu_home_search:
                if (!isSearch) {
                    isSearch = true;
                    mEdtSearch.setVisibility(View.VISIBLE);
                    mViewModel.setSearchFilter(mEdtSearch, mAdapter);
                } else {
                    isSearch = false;
                    mEdtSearch.setVisibility(View.GONE);
                    mAdapter.reloadData();
                    Utils.hideKeyboard(getActivity(), mEdtSearch);
                }
                break;

            case R.id.menu_home_add_group:
                AlertDialogUtils.showCreateKittyDialog(getActivity());
                break;
        }
        return true;
    }*/

    public void getDataList(List<ContactDao> list) {
        showLayout();
        mAdapter = new ContactAdapter(getActivity(), list);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        /*mRecyclerView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (mRecyclerView == null || mRecyclerView.getChildCount() == 0) ? 0 : mRecyclerView.getChildAt(0).getTop();
                mRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mViewModel.callAPI(mViewModel.isInit);
            } else {
                mRecyclerView.setVisibility(View.GONE);
                mTxtEmpty.setVisibility(View.VISIBLE);
                mTxtEmpty.setText(getActivity().getResources().getString(R.string.contact_permission_text));
                mViewModel.hideRefreshLayout();
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void hideLayout() {
        mRecyclerView.setVisibility(View.GONE);
        mTxtEmpty.setVisibility(View.VISIBLE);
        //mViewModel.onSwipeRefreshReloadData(mRefreshLayout);
        mViewModel.setBanner(mImgAdvertise);
    }

    public void showLayout() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mTxtEmpty.setVisibility(View.GONE);
        mImgAdvertise.setVisibility(View.VISIBLE);
        mViewModel.setBanner(mImgAdvertise);
    }

    @Override
    public void onResume() {
        super.onResume();
        /*mContactReceiver = new ContactReceiver();
        // start listening for refresh local file list in
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mContactReceiver,
                new IntentFilter(AppConstant.CONTACT_SYNC_ACTION));*/

        //mViewModel.init();
    }

    @Override
    public void onPause() {
        super.onPause();
        /*LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mContactReceiver);*/
    }

    /**
     *
     */
    /*private class ContactReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            AppLog.i(TAG, "onReceive: ");
            AppContactList contactListObject = PreferanceUtils.getContactListFromPreferance(getActivity());
            mViewModel.setListview(contactListObject.getContactList());
            mViewModel.callDeviceContactMergerTask(contactListObject.getContactList());
        }
    }*/
    public Context getFragmentContext() {
        return getActivity();
    }

    /**
     * * Apply Filtration to list
     *
     * @param filterString
     */
    public void applyFilter(String filterString) {
        try {
            if (mAdapter != null)
                mAdapter.getFilter().filter(filterString);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void reloadData() {
        if (mAdapter != null && Utils.isValidList(mAdapter.getList())) {
            mAdapter.reloadData();
        }
    }

}