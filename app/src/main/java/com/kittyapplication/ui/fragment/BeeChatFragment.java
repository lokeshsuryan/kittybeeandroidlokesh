package com.kittyapplication.ui.fragment;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.ChatRecyclerAdapter;
import com.kittyapplication.core.ui.listener.PaginationHistoryListener;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.sqlitedb.DBQueryHandler.OnQueryHandlerListener;
import com.kittyapplication.sync.SyncGroupOperation;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.view.DividerItemDecoration;
import com.kittyapplication.ui.viewmodel.ChatViewModel;
import com.kittyapplication.utils.AppLog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;

import java.util.ArrayList;

/**
 * Created by Pintu Riontech on 8/8/16.
 */
public class BeeChatFragment extends Fragment {
    private static final String TAG = BeeChatFragment.class.getSimpleName();
    private View mRootView;
    private SwipeRefreshLayout mRefreshLayout;
    private CustomEditTextNormal mEdtSearch;
    private CustomTextViewNormal mTxtRefreshing;
    private ChatViewModel mViewModel;
    private ChatRecyclerAdapter mAdapter;
    private ImageView mImgAdvertise;
    private TextView mTxtEmpty;
    private boolean isSearch = false;
    private ArrayList<Kitty> kitties;
    public ProgressBar mPbLoader;
    private HomeActivity mActivity;
    private RecyclerView mRecyclerView;

    public static BeeChatFragment newInstance(int pos) {
        BeeChatFragment fragment = new BeeChatFragment();
        Bundle args = new Bundle();
        args.putInt("position", pos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat_recyclerview, container, false);
        mActivity = (HomeActivity) getActivity();
        initView();
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.setBanner(mImgAdvertise);
        if (AppApplication.getInstance().isQbRefresh()) {
            AppApplication.getInstance().setQbRefresh(false);
            try {
                showProgress();
                loadData(0);
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mTxtEmpty = (TextView) mRootView.findViewById(R.id.txtEmpty);
        mRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.srfChatList);
        mEdtSearch = (CustomEditTextNormal) mRootView.findViewById(R.id.edtSearchChatFragment);
        mTxtRefreshing = (CustomTextViewNormal) mRootView.findViewById(R.id.txtRefreshing);
        mEdtSearch.setVisibility(View.GONE);
        mImgAdvertise = (ImageView) mRootView.findViewById(R.id.imgAdvertisement);
        mPbLoader = (ProgressBar) mRootView.findViewById(R.id.pbLoaderChatFragment);

        initRecyclerView();
        initRefreshLayout();
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.chatRecyclerView);
        int orientation = LinearLayoutManager.HORIZONTAL;
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), orientation);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        kitties = new ArrayList<>();
        mViewModel = new ChatViewModel(getContext());
    }

    private void initRefreshLayout() {
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                syncData();
            }
        });
    }

    private void setView() {
        if (mAdapter == null) {
            showProgress();
            mAdapter = new ChatRecyclerAdapter(getActivity(), kitties);
            HomeActivity activity = (HomeActivity) getActivity();
            mAdapter.setCurrentActionMode(activity.getCurrentActionMode());
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int topRowVerticalPosition = (mRecyclerView == null || layoutManager.getChildCount() == 0) ? 0 : layoutManager.getChildAt(0).getTop();
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    mRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                }
            });

            mAdapter.setPaginationHistoryListener(new PaginationHistoryListener() {
                @Override
                public void downloadMore() {
//                    loadData();
                }
            });

            loadData(mAdapter.getCount());
        }
    }

    private void loadData(int count) {
        mViewModel.fetchKitties(count, new OnQueryHandlerListener<ArrayList<Kitty>>() {
            @Override
            public void onResult(final ArrayList<Kitty> result) {
                kitties = result;
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
                bindView(result);
//                    }
//                });

            }
        });
    }

    private void bindView(ArrayList<Kitty> result) {
        try {
            if (result.size() > 0) {
//                if (mRefreshLayout.isRefreshing())
                mAdapter.getList().clear();

//                for (Kitty kitty : result) {
//                    mAdapter.add(kitty);
//                }
                mAdapter.addList(kitties);

                hideProgress();
            } else if (result.size() == 0 && mAdapter.getItemCount() == 0) {
                syncData();
            }

        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    private void syncData() {
        SyncGroupOperation syncOperation = new SyncGroupOperation();
        syncOperation.syncDialogs(new SyncGroupOperation.OnSyncComplete() {
            @Override
            public void onCompleted(boolean hasDate) {
                if (!hasDate && mAdapter.getItemCount() == 0) {
                    showEmptyView();
                    Log.d(TAG, "onCompleted: if");
                } else {
                    Log.d(TAG, "onCompleted: else");
                    loadData(0);
                }
            }
        });
    }


    public void showEmptyView() {
        try {
            hideProgress();
            String emptyStringArray[] = {"To Start managing your Kitty, Create group. Tap   at the top right on your screen"};
            SpannableString spannableString = new SpannableString(emptyStringArray[0]);
            Drawable mDrawable = ContextCompat.getDrawable(mRootView.getContext(), R.drawable.ic_add_group_big);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.color_blue), PorterDuff.Mode.MULTIPLY));
            mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
            ImageSpan imageSpan = new ImageSpan(mDrawable, ImageSpan.ALIGN_BOTTOM);
            spannableString.setSpan(imageSpan, 48, 49, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            mTxtEmpty.setText(spannableString);
            mRecyclerView.setVisibility(View.GONE);
            mTxtEmpty.setVisibility(View.VISIBLE);
//            mViewModel.setBanner(mImgAdvertise);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void hideSearch() {
        mEdtSearch.setVisibility(View.GONE);
    }

    public void showLayout() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mTxtEmpty.setVisibility(View.GONE);
    }

    public void updatedDialog(QBDialog dialog) {
        showLayout();
        mAdapter.updatedDialog(dialog);
    }


    public void updateMessage(final QBChatMessage message) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.updatedMessage(message);
            }
        });
    }

    public void notifyData() {
        try {
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showProgress() {
        mPbLoader.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mPbLoader != null)
                        mPbLoader.setVisibility(View.GONE);
                }
            });
        }

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
}