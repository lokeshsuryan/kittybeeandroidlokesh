package com.kittyapplication.ui.fragment;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.adapter.ChatListAdapter;
import com.kittyapplication.chat.utils.qb.QbDialogHolder;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.ResourceUtils;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.services.OfflineSupportIntentService;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.viewinterface.ChatView;
import com.kittyapplication.ui.viewmodel.ChatViewModel;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.GroupPrefHolder;
import com.kittyapplication.utils.KittyPrefHolder;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 8/8/16.
 */
public class ChatsFragment extends Fragment implements ChatView {
    private static final String TAG = ChatsFragment.class.getSimpleName();
    private View mRootView;
    private SwipeRefreshLayout mRefreshLayout;
    private ListView mLvChats;
    private CustomEditTextNormal mEdtSearch;
    private CustomTextViewNormal mTxtRefreshing;
    private ChatViewModel mViewModel;
    private ChatListAdapter mAdapter;
    private ImageView mImgAdvertise;
    private TextView mTxtEmpty;
    private List<ChatData> list;
    private boolean isSearch = false;
    private ArrayList<Kitty> kitties;
//    public ProgressBar mPbLoader;
    private HomeActivity mActivity;

    public static ChatsFragment newInstance(int pos) {
        ChatsFragment fragment = new ChatsFragment();
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
        mRootView = inflater.inflate(R.layout.fragment_chat_list, container, false);
        mActivity = (HomeActivity) getActivity();
        mTxtEmpty = (TextView) mRootView.findViewById(R.id.txtEmpty);
        mRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.srfChatList);
        mEdtSearch = (CustomEditTextNormal) mRootView.findViewById(R.id.edtSearchChatFragment);
        mLvChats = (ListView) mRootView.findViewById(R.id.lvChatList);
        mTxtRefreshing = (CustomTextViewNormal) mRootView.findViewById(R.id.txtRefreshing);

        mEdtSearch.setVisibility(View.GONE);
        mImgAdvertise = (ImageView) mRootView.findViewById(R.id.imgAdvertisement);
//        ((HomeActivity) getActivity()).setChatsFragment(this);
//        mPbLoader = (ProgressBar) mRootView.findViewById(R.id.pbLoaderChatFragment);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initChatList();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.setBanner(mImgAdvertise);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initChatList() {
//        showProgress();
//        mViewModel = new ChatViewModel(ChatsFragment.this);

        List<Kitty> prefList = KittyPrefHolder.getInstance().getGroupList();
        if (Utils.isValidList(prefList)) {
            kitties = new ArrayList<>();
            kitties.addAll(prefList);
            mAdapter = new ChatListAdapter(getActivity(), kitties);
//            hideProgress();
//            mViewModel.initRequest(false);
        } else {
            kitties = new ArrayList<>();
            mAdapter = new ChatListAdapter(getActivity(), kitties);
//            mViewModel.initRequest(true);
        }

        mLvChats.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mLvChats.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (mLvChats == null || mLvChats.getChildCount() == 0) ? 0 : mLvChats.getChildAt(0).getTop();
                mRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });


//        mViewModel.onSwipeRefreshReloadData(mRefreshLayout);
    }

    @Override
    public void getChatList(List<ChatData> list) {
        this.list = list;
        mLvChats.setVisibility(View.VISIBLE);
        mTxtEmpty.setVisibility(View.GONE);
        mImgAdvertise.setVisibility(View.VISIBLE);

        refreshChats();
    }

    @Override
    public void showEmptyView() {
        try {
//            hideProgress();
            String emptyStringArray[] = {"To Start managing your Kitty, Create group. Tap   at the top right on your screen"};
            SpannableString spannableString = new SpannableString(emptyStringArray[0]);
            Drawable mDrawable = ContextCompat.getDrawable(mRootView.getContext(), R.drawable.ic_add_group_big);
            mDrawable.setColorFilter(new
                    PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.color_blue), PorterDuff.Mode.MULTIPLY));
            mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
            ImageSpan imageSpan = new ImageSpan(mDrawable, ImageSpan.ALIGN_BOTTOM);
            spannableString.setSpan(imageSpan, 48, 49, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            mTxtEmpty.setText(spannableString);

//            mViewModel.hideRefreshLayout();
//            mViewModel.hideDialog();
//            mLvChats.setVisibility(View.GONE);
//            mTxtEmpty.setVisibility(View.VISIBLE);
//            mViewModel.onSwipeRefreshReloadData(mRefreshLayout);
//            mViewModel.setBanner(mImgAdvertise);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
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
                ((HomeActivity) getActivity()).setDrawerItem();
                break;
            case R.id.menu_home_search:
                if (mAdapter != null) {
                    if (!isSearch) {
                        isSearch = true;
                        mEdtSearch.setVisibility(View.VISIBLE);
                        mLvChats.setVisibility(View.VISIBLE);
                        mViewModel.setSearchFilter(mEdtSearch, mAdapter);
                    } else {
                        isSearch = false;
                        hideSearch();
                        mLvChats.setVisibility(View.VISIBLE);
                        mAdapter.reloadData();
                        mAdapter.notifyDataSetChanged();
                        Utils.hideKeyboard(getActivity(), mEdtSearch);
                    }
                }
                break;

            case R.id.menu_home_add_group:
                AlertDialogUtils.showCreateKittyDialog(getActivity());
                break;
        }
        return true;
    }*/

    private void hideSearch() {
        mEdtSearch.setVisibility(View.GONE);
    }

    public void showLayout() {
        mLvChats.setVisibility(View.VISIBLE);
        mTxtEmpty.setVisibility(View.GONE);
    }

    public void reloadData() {
        if (ConnectivityUtils.checkInternetConnection(getActivity())) {
            // TODO 29-10-2016 Commented as not required to get dialogs from QB
            ((HomeActivity) getActivity()).loadDialogsFromQb();
//            mViewModel.initRequest(false);
        } else {
//            mViewModel.hideRefreshLayout();
            HomeActivity activity = (HomeActivity) getActivity();
            activity.showSnackbar(ResourceUtils.getString(R.string.no_internet_connection));
        }
    }

    /**
     * Refresh chat list
     */
    private void refreshChats() {
        if (QbDialogHolder.getInstance().getDialogList() == null
                || QbDialogHolder.getInstance().getDialogList().isEmpty()
                || list == null || list.isEmpty()) {
            return;
        }
        RefreshTask task = new RefreshTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     *
     */
    private class RefreshTask extends AsyncTask<Void, Kitty, ArrayList<Kitty>> {
        ArrayList<Kitty> mergedList = new ArrayList<>();
        ArrayList<QBDialog> dialogList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogList.addAll(QbDialogHolder.getInstance().getDialogList());
            showLayout();
        }

        @Override
        protected ArrayList<Kitty> doInBackground(Void... params) {
            try {
                for (int i = 0; i < dialogList.size(); i++) {
                    QBDialog qbDialog = dialogList.get(i);

                    Kitty kitty = new Kitty();
                    kitty.setId(i);
                    kitty.setQbDialog(qbDialog);
                    if (list != null) {
                        boolean isFlag = false;
                        for (ChatData chatData : list) {
                            chatData.setHostEmpty();
                            chatData.setGroupRightsVisibility(getActivity());

                            if (chatData.getQuickId().equals(qbDialog.getDialogId())) {
                                kitty.setGroup(chatData);
                                isFlag = true;
                                break;
                            }
                        }

                        if (!isFlag && qbDialog.getType() == QBDialogType.PRIVATE) {
                            //                                publishProgress(kitty);
                            mergedList.add(kitty);
                        } else if (isFlag) {
                            mergedList.add(kitty);
                        }
                    }
                }
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
            return mergedList;
        }


        @Override
        protected void onPostExecute(ArrayList<Kitty> cloneChat) {
            super.onPostExecute(cloneChat);
            if (cloneChat != null && cloneChat.isEmpty()) {
                showEmptyView();
            }

            mAdapter.updateList(cloneChat);
//            mViewModel.hideRefreshLayout();
//            mViewModel.hideDialog();
            mAdapter.updateChatList(cloneChat);
//            hideProgress();

            //add groupchat list into preferance
            KittyPrefHolder.getInstance().saveGroupChats(cloneChat);

            if (!PreferanceUtils.getIsOfflineDataAvailable(getActivity())) {
                WorkerThread mQbDialogWorkerThread = new WorkerThread("myWorkerThread");
                mQbDialogWorkerThread.start();
                mQbDialogWorkerThread.prepareHandler();
                mQbDialogWorkerThread.postTask(task);
            }
        }
    }

    /**
     *
     */
    public void refreshChatsAsync() {
        RefreshAsyncTask task = new RefreshAsyncTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     *
     */
    private class RefreshAsyncTask extends AsyncTask<Void, Kitty, ArrayList<Kitty>> {
        final ArrayList<Kitty> groupChatsClone = new ArrayList<>();
        final ArrayList<QBDialog> dialogList = new ArrayList<>();
        final List<ChatData> listClone = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogList.addAll(QbDialogHolder.getInstance().getDialogList());
            listClone.addAll(GroupPrefHolder.getInstance().getGroupList());

            showHideRefreshingText(true);
        }

        @Override
        protected ArrayList<Kitty> doInBackground(Void... params) {
            try {
                for (QBDialog qbDialog : dialogList) {
                    AppLog.e(TAG, "background process");
                    Kitty kitty = new Kitty();
                    kitty.setQbDialog(qbDialog);
                    if (listClone != null) {
                        for (ChatData chatData : listClone) {
                            chatData.setHostEmpty();
                            chatData.setGroupRightsVisibility(getActivity());

                            if (chatData.getQuickId().equals(qbDialog.getDialogId())) {
                                kitty.setGroup(chatData);
                                break;
                            }
                        }
                    }
                    publishProgress(kitty);
                }
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Kitty... values) {
            super.onProgressUpdate(values);
            if (values != null) {
                groupChatsClone.add(values[0]);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Kitty> cloneChat) {
            super.onPostExecute(cloneChat);
            showLayout();
            mAdapter.updateList(groupChatsClone);
            mAdapter.updateChatList(groupChatsClone);
            mAdapter.notifyDataSetChanged();

            //add groupchat list into preferance
            KittyPrefHolder.getInstance().saveGroupChats(groupChatsClone);

            showHideRefreshingText(false);
        }
    }

//    }

//    public void addedNewCreatedDialog(String createdDialogId) {
//        QBDialog dialog = QbDialogHolder.getInstance().getQBDialogByDialogId(createdDialogId);
//        AppLog.d(TAG, "Created dialog::" + new Gson().toJson(dialog));
//        if (mAdapter != null) {
//            mAdapter.addCreatedDialog(dialog);
//        }
//    }

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

    /*public void enableDisableListView(boolean flag) {
        mLvChats.setEnabled(flag);
    }*/

    public void notifyData() {
        try {
            mAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show hide refreshing text
     *
     * @param isVisible true = visible & false = gone
     */
    private void showHideRefreshingText(boolean isVisible) {
        if (isVisible) {
            mTxtRefreshing.setVisibility(View.VISIBLE);
        } else {
            mTxtRefreshing.setVisibility(View.GONE);
        }
    }

//    public void showProgress() {
//        mPbLoader.setVisibility(View.VISIBLE);
//    }
//
//    public void hideProgress() {
//        mPbLoader.setVisibility(View.GONE);
//    }

    public void updateGroup() {
        try {
            Kitty kitty = new Kitty();

            String groupId = AppApplication.getInstance().getUpdatedGroupId();
            ChatData chatData = GroupPrefHolder.getInstance().getByGroupId(groupId);
            int position = QbDialogHolder.getInstance()
                    .getDialogIndex(chatData.getQuickId());
            kitty.setId(position);
            QBDialog dialog = QbDialogHolder.getInstance()
                    .getQBDialogByDialogId(chatData.getQuickId());
            kitty.setGroup(chatData);
            kitty.setQbDialog(dialog);
            mAdapter.updatedGroupChat(kitty);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateList(List<ChatData> data) {
        list = data;
    }

    /**
     *
     */
    private class WorkerThread extends HandlerThread {
        private Handler mWorkerHandler;

        public WorkerThread(String name) {
            super(name);
        }

        public void postTask(Runnable task) {
            mWorkerHandler.post(task);
        }

        public void prepareHandler() {
            mWorkerHandler = new Handler(getLooper());
        }

    }

    /**
     *
     */
    Runnable task = new Runnable() {
        @Override
        public void run() {
            // if offline data is available  than start the service
            getActivity().startService(new Intent(getActivity(), OfflineSupportIntentService.class));
        }
    };

    public void updateGroupList() {
        AppLog.d(TAG, "updateGroupList");
        List<Kitty> list = KittyPrefHolder.getInstance().getGroupList();
        mAdapter.updateList(list);
    }
}