package com.kittyapplication.ui.fragment;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.ActionMode;
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

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.KittyRecyclerAdapter;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.core.ui.listener.PaginationHistoryListener;
import com.kittyapplication.custom.ChatComparator;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.sqlitedb.DBQueryHandler;
import com.kittyapplication.sync.SyncGroupOperation;
import com.kittyapplication.sync.callback.DataSyncListener;
import com.kittyapplication.ui.executor.BackgroundExecutorThread;
import com.kittyapplication.ui.executor.Interactor;
import com.kittyapplication.ui.view.DividerItemDecoration;
import com.kittyapplication.ui.viewmodel.ChatViewModel;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;


public class KittiesFragment extends BaseFragment {
    private static final String TAG = KittiesFragment.class.getSimpleName();
    private View mRootView;
    private SwipeRefreshLayout mRefreshLayout;
    private CustomEditTextNormal mEdtSearch;
    private ChatViewModel mViewModel;
    private KittyRecyclerAdapter mAdapter;
    private ImageView mImgAdvertise;
    private TextView mTxtEmpty;
    private ArrayList<ChatData> mGroupList;
    public ProgressBar mPbLoader;
    private RecyclerView mRecyclerView;
    private ActionMode currentActionMode;

    public static KittiesFragment newInstance(int pos) {
        KittiesFragment fragment = new KittiesFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat_recyclerview, container, false);
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
        updateObject();

        if (AppApplication.getInstance().isRefresh()) {
            AppApplication.getInstance().setRefresh(false);
            try {
                setLimit(0);
                setSkip(0);
                showProgress();
//                refreshView();
                loadData();
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
    }

    private void refreshView() {
        setLimit(mAdapter.getCount());
        setSkip(0);
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
        mEdtSearch.setVisibility(View.GONE);
        mImgAdvertise = (ImageView) mRootView.findViewById(R.id.imgAdvertisement);
        mPbLoader = (ProgressBar) mRootView.findViewById(R.id.pbLoaderChatFragment);
        showProgress();
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


        mGroupList = new ArrayList<>();
        mViewModel = new ChatViewModel(getContext());
    }

    private void initRefreshLayout() {
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorAccent));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshView();
                syncData();
            }
        });
    }

    private void setView() {
        if (mAdapter == null) {
            mAdapter = new KittyRecyclerAdapter(getActivity(), mGroupList);
            mAdapter.setCurrentActionMode(currentActionMode);
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
                    if (!isFilter()) loadData();
                }
            });

            loadData();
        }
    }

    private void loadData() {
        Log.e(TAG, "loadData: skip :: " + getSkip() + " limit :: " + getLimit());
        mViewModel.fetchGroups(getSkip(), getLimit(),
                new DBQueryHandler.OnQueryHandlerListener<ArrayList<ChatData>>() {

                    @Override
                    public void onResult(final ArrayList<ChatData> result) {
                        bindView(result);
                    }
                });
    }

    private void bindView(final ArrayList<ChatData> result) {
        try {
            if (result.size() > 0) {
                BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
                backgroundExecutorThread.execute(new Interactor() {
                    @Override
                    public void execute() {
                        Collections.sort(result, new ChatComparator());
                        for (ChatData group : result) {
                            group.setHostEmpty();
                            group.setGroupRightsVisibility(getContext());
                        }


                    }
                }, new Interactor.OnExecutionFinishListener() {
                    @Override
                    public void onFinish() {

                        if (getSkip() == 0) {
                            mAdapter.getList().clear();
                            AppLog.e(TAG, "isRefreshing");
                        }
                        mAdapter.addAtLast(result);
                        hideProgress();
                        setSkip(mAdapter.getCount());
                        setLimit(0);
                        showLayout();
                    }
                });
            } else if (result.size() == 0 && mAdapter.getCount() == 0) {
                syncData();
            }

            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.setRefreshing(false);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

    }

    private void syncData() {
        SyncGroupOperation syncOperation = new SyncGroupOperation();
        syncOperation.syncGroups(new DataSyncListener() {
            @Override
            public void onCompleted(int itemCount) {
                AppLog.e(TAG, "syncData: onCompleted" + itemCount);
                if (itemCount == 0 && mAdapter.getCount() == 0) {
                    showEmptyView();
                    return;
                } else if (itemCount == 0) {
                    mAdapter.setPaginationHistoryListener(null);
                    return;
                } else if (itemCount < ChatHelper.ITEMS_PER_PAGE) {
                    mAdapter.setPaginationHistoryListener(null);
                }
                loadData();
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPbLoader.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Apply Filtration to list
     *
     * @param filterString
     */
    public void applyFilter(String filterString) {
        try {
            if (mAdapter != null && filterString != null)
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

    public void updateObject() {
        int pos = AppApplication.getInstance().getSelectedPosition();
        if (pos != -1) {
            ChatData data = mAdapter.getList().get(pos);
            mViewModel.fetchGroup(data.getGroupID(),
                    new DBQueryHandler.OnQueryHandlerListener<ArrayList<ChatData>>() {
                        @Override
                        public void onResult(ArrayList<ChatData> result) {
                            if (Utils.isValidList(result)) {
                                int pos = AppApplication.getInstance().getSelectedPosition();
                                ChatData data = result.get(0);
                                data.setHostEmpty();
                                data.setGroupRightsVisibility(getActivity());
                                AppLog.d(TAG, "update object" + new Gson().toJson(data).toString());
                                mAdapter.updateObject(pos, data);
                                AppApplication.getInstance().setSelectedPosition(-1);
                            }
                        }
                    });
        }
    }
}