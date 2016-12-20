package com.kittyapplication.ui.fragment;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.ChatRecyclerAdapter;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.core.ui.listener.PaginationHistoryListener;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.sqlitedb.DBQueryHandler.OnQueryHandlerListener;
import com.kittyapplication.sync.SyncGroupOperation;
import com.kittyapplication.sync.callback.DataSyncListener;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.view.DividerItemDecoration;
import com.kittyapplication.ui.viewmodel.ChatViewModel;
import com.kittyapplication.ui.viewmodel.KittyViewModel;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;

/**
 * Created by Pintu Riontech on 8/8/16.
 */
public class BeeChatFragment extends BaseFragment {
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
    //    private ArrayList<Kitty> kitties;
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
                if (mAdapter != null) {
                    refreshView();
                    loadData();
                }
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }
        if (AppApplication.getInstance().getChatData() != null) {
            updateObject(AppApplication.getInstance().getChatData());
        }

    }

    private void updateObject(ChatData chatData) {
        if (chatData != null && Utils.isValidString(chatData.getQuickId())) {
            KittyViewModel model = new KittyViewModel(getActivity());
            model.updateGroupByDialogId(chatData, chatData.getQuickId());
            mAdapter.notifyDataSetChanged();
            AppApplication.getInstance().setChatData(null);
            new UpdateObjectTask().execute(chatData);
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

    private void refreshView() {
        setLimit(mAdapter.getCount());
        setSkip(0);
    }

    private void setView() {
        if (mAdapter == null) {
            showProgress();
            mAdapter = new ChatRecyclerAdapter(getActivity(), new ArrayList<Kitty>());
            mAdapter.setHasFooter(false);
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
                    if (!isFilter()) loadData();
                }
            });

            loadData();
        }
    }

    private void loadData() {
        AppLog.e(TAG, "loadData: skip => " + getSkip() + " limit => " + getLimit());
        mViewModel.fetchKitties(getSkip(), getLimit(), new OnQueryHandlerListener<ArrayList<Kitty>>() {
            @Override
            public void onResult(final ArrayList<Kitty> result) {
                AppLog.e(TAG, "Local onResult: " + result.size());
                bindView(result);
            }
        });
    }

    private void bindView(ArrayList<Kitty> result) {
        try {
            if (result.size() > 0) {
                if (getSkip() == 0) {
                    mAdapter.getList().clear();
                }
                mAdapter.addAtLast(result);
                hideProgress();
                setLimit(0);
                setSkip(mAdapter.getCount());
                showLayout();
            } else {
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
        AppLog.e(TAG, "syncData: ");
        SyncGroupOperation syncOperation = new SyncGroupOperation();
        syncOperation.syncDialogs(getSkip(), getLimit(), new DataSyncListener() {
            @Override
            public void onCompleted(int itemCount) {
                AppLog.e(TAG, "syncData: onCompleted" + itemCount);
                if (itemCount == 0 && mAdapter.getCount() == 0) {
                    showEmptyView();
                    return;
                } else if (itemCount == 0) {
                    mAdapter.setTapOnHold();
                    return;
                } else if (itemCount < ChatHelper.ITEMS_PER_PAGE) {
                    mAdapter.setTapOnHold();
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

    public void updatedDialog(QBChatDialog dialog, int index) {
        showLayout();
        mAdapter.updatedDialog(dialog, index);
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
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPbLoader.setVisibility(View.GONE);
            }
        });
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


    private class UpdateObjectTask extends AsyncTask<ChatData, Void, Kitty> {
        private ChatData mUpdatedData;

        @Override
        protected Kitty doInBackground(ChatData... params) {
            AppLog.d(TAG, "REFRSH STRAT");
            Kitty updateKitty = null;
            mUpdatedData = params[0];
            if (mAdapter != null && Utils.isValidList(mAdapter.getList())) {
                for (int i = 0; i < mAdapter.getList().size(); i++) {
                    Kitty matchWithKitty = mAdapter.getList().get(i);
                    if (matchWithKitty.getGroup() != null
                            && Utils.isValidString(matchWithKitty.getGroup().getQuickId())) {
                        if (mUpdatedData.getQuickId().
                                equalsIgnoreCase(matchWithKitty.getGroup().getQuickId())) {
                            updateKitty = mAdapter.getList().get(i);
                            break;
                        }
                    }
                }
            }
            return updateKitty;
        }

        @Override
        protected void onPostExecute(Kitty kitty) {
            super.onPostExecute(kitty);
            if (kitty != null) {
                AppLog.d(TAG, "mUpdatedData.getName() " + mUpdatedData.getName());
                kitty.getGroup().setName(mUpdatedData.getName());
                kitty.getQbDialog().setName(mUpdatedData.getName());
                kitty.getGroup().setGroupImage(mUpdatedData.getGroupImage());
                mAdapter.updatedGroupChat(kitty);
                mAdapter.notifyDataSetChanged();
            } else {
                AppLog.d(TAG, "Null object Getting");
            }
            AppLog.d(TAG, "COMPLETE");
        }
    }
}