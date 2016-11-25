package com.kittyapplication.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.QbDialogHolder;
import com.kittyapplication.core.async.BaseAsyncTask;
import com.kittyapplication.core.ui.adapter.BaseListAdapter;
import com.kittyapplication.core.utils.ResourceUtils;
import com.kittyapplication.core.utils.Toaster;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.listener.ChatOptionClickListener;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.model.ReqRefreshGroup;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.KittyDiaryActivity;
import com.kittyapplication.ui.activity.PromotionalActivity;
import com.kittyapplication.ui.activity.VenueActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.GroupPrefHolder;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.KittyPrefHolder;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by MIT on 8/23/2016.
 */
public class ChatListAdapter extends BaseListAdapter<Kitty> implements Filterable, ChatOptionClickListener {

    private static final String TAG = ChatListAdapter.class.getSimpleName();
    private List<Kitty> mListClone = new ArrayList<>();
    private ItemFilter filter;
    private Context mContext;
    private ProgressDialog mDialog;
    private int mPosition;

    private interface OnUserSelectionListener {
        void onProceed();

        void onCancel();
    }

    public ChatListAdapter(Context context, ArrayList<Kitty> list) {
        super(context, list);
        mListClone.addAll(list);
        filter = new ItemFilter();
        mContext = context;
    }

    public void addCreatedDialog(QBDialog qbDialog) {
        try {
            Log.d(TAG, "addCreatedDialog");
            Kitty kitty = new Kitty();
            kitty.setQbDialog(qbDialog);
            addAt(0, kitty);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param message
     */
    public void updatedMessage(QBChatMessage message) {
        try {
            for (Kitty kitty : getList()) {
                QBDialog qbDialog = kitty.getQbDialog();
                if (qbDialog.getDialogId().equals(message.getDialogId())) {
                    qbDialog.setLastMessage(message.getBody());
                    int unreadCount = qbDialog.getUnreadMessageCount();
                    qbDialog.setUnreadMessageCount(unreadCount + 1);
                    remove(kitty);
                    addAt(0, kitty);
                    notifyDataSetChanged();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatedDialog(final QBDialog qbDialog) {
//        try {
//            AppLog.d(TAG, "updatedDialog::" + new Gson().toJson(qbDialog));
//            boolean isCreated = true;
//            String qbLastMessage = qbDialog.getLastMessage();
//            for (int i = 0; i < getList().size(); i++) {
//                Kitty kitty = getList().get(i);
//                String groupLastMessage = kitty.getQbDialog().getLastMessage();
//                if (kitty.getQbDialog().getDialogId().equals(qbDialog.getDialogId())) {
//                    // Don't update if no changes in last message
//                    if (qbLastMessage != null && !groupLastMessage.equals(qbLastMessage)) {
//                        remove(i);
//                        kitty.setQbDialog(qbDialog);
//                        addAt(0, kitty);
//                        notifyDataSetChanged();
//                        isCreated = false;
//                        break;
//                    } else {
//                        isCreated = false;
//                        break;
//                    }
//                }
//            }
//            AppLog.d(TAG, "Last Message::" + qbLastMessage);
//            // add new created dialog into list if last message created
//            if (isCreated) {
//                if (qbLastMessage != null && qbLastMessage.length() > 0) {
//                    Log.d(TAG, "isCreated ");
//                    addCreatedDialog(qbDialog);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {


            new BaseAsyncTask<Void, Void, Void>() {
                boolean isCreated = true;
                private Kitty kitty = null;
                private int index = 0;
                private boolean isUpdated = true;

                @Override
                public Void performInBackground(Void... params) throws Exception {
                    AppLog.d(TAG, "updatedDialog::" + new Gson().toJson(qbDialog));

                    try {
                        String qbLastMessage = qbDialog.getLastMessage();
                        for (int i = 0; i < getList().size(); i++) {
                            Kitty kitty = getList().get(i);
                            String groupLastMessage = kitty.getQbDialog().getLastMessage();
                            if (kitty.getQbDialog().getDialogId().equals(qbDialog.getDialogId())) {
                                // Don't update if no changes in last message
                                if (qbLastMessage != null && !groupLastMessage.equals(qbLastMessage)) {
                                    this.kitty = kitty;
                                    index = i;
                                    isCreated = false;
                                    break;
                                } else {
                                    isUpdated = false;
                                    isCreated = false;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public void onResult(Void aVoid) {
                    try {
                        String qbLastMessage = qbDialog.getLastMessage();
                        if (isCreated) {
                            if (qbLastMessage != null && qbLastMessage.length() > 0) {
                                Log.d(TAG, "isCreated ");
                                addCreatedDialog(qbDialog);
                            }
                        } else if (!isCreated && isUpdated) {
                            remove(index);
                            kitty.setQbDialog(qbDialog);
                            addAt(0, kitty);
                            notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param group
     */
    public void updatedGroupChat(Kitty group) {
        try {
            boolean isCreated = true;
            AppLog.d(TAG, "updatedGroupChat: " + group.getGroup().getName());
            for (int i = 0; i < getList().size(); i++) {
                Kitty kitty = getList().get(i);
                QBDialog qbDialog = group.getQbDialog();

                if (kitty.getQbDialog().getDialogId().equals(qbDialog.getDialogId())) {
                    // Don't update if no changes in last message
                    if (getList().size() < group.getId()) {
                        add(group);
                    } else if (getList().size() == group.getId()) {
                        remove(i);
                        addAt(group.getId() - 1, group);
                    } else {
                        remove(i);
                        addAt(group.getId(), group);
                    }

                    notifyDataSetChanged();
                    isCreated = false;
                    break;
                }
            }

            // add new created group into list if last message created
            if (isCreated) {
                if (getList().size() > group.getId())
                    addAt(group.getId(), group);
                else
                    add(group);
                AppLog.d(TAG, "Group position = >" + group.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        AppLog.e(TAG, "getView process");
        ChatListHolder chatListHolder; // view lookup cache stored in tag
        try {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_chat_list, parent, false);

                chatListHolder = new ChatListHolder();
                chatListHolder.imgChatUser = (RoundedImageView) convertView.findViewById(R.id.imgChatUser);
                chatListHolder.imgChatHost = (ImageView) convertView.findViewById(R.id.imgChatHost);
                chatListHolder.imgChatDiary = (ImageView) convertView.findViewById(R.id.imgChatDairy);
                chatListHolder.imgChatAnnouncement = (ImageView) convertView.findViewById(R.id.imgChatAnnouncement);
                chatListHolder.txtChatTitle = (CustomTextViewBold) convertView.findViewById(R.id.txtChatTitle);
                chatListHolder.txtChatLastMessage = (CustomTextViewNormal) convertView.findViewById(R.id.txtChatLastMessage);
                chatListHolder.txtMessageUnreadCount = (TextView) convertView.findViewById(R.id.txtMessageUnreadCount);
                chatListHolder.imgChatAdminHost = (ImageView) convertView.findViewById(R.id.imgChatHostAdmin);
                chatListHolder.rlMain = (RelativeLayout) convertView.findViewById(R.id.rlChatRow);
                convertView.setTag(chatListHolder);
            } else {
                chatListHolder = (ChatListHolder) convertView.getTag();
            }

            chatListHolder.setPosition(position);
            chatListHolder.bind(getItem(position));
            convertView.setTag(R.layout.row_chat_list, getItem(position));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Kitty chat = (Kitty) v.getTag(R.layout.row_chat_list);
                    QBDialog dialog = chat.getQbDialog();
                    ChatData data = chat.getGroup();
                    String strData = new Gson().toJson(data).toString();
//                    ChatActivity.startForResult((Activity) mContext,
//                            AppConstant.REQUEST_UPDATE_DIALOG, dialog.getDialogId(), null);
//                    ChatActivity.startForResult((Activity) context,
//                            AppConstant.REQUEST_UPDATE_DIALOG, dialog, null, null, strData);
                }
            });

            mPosition = position;
            convertView.setTag(R.id.rlChatRow, position);
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mPosition = (Integer) v.getTag(R.id.rlChatRow);
                    showChatOptionDialog((Integer) v.getTag(R.id.rlChatRow));
                    return false;
                }
            });
        } catch (Exception e) {
        }

        return convertView;
    }

    private void showChatOptionDialog(int pos) {
        //        type ==0 for display group chat option
        //        type ==1 for display private chat option
        //        type ==2 for display only clear chat option
        try {
            Kitty kitty = getItem(pos);
            int type = 0;

            QBDialogType chatType = kitty.getQbDialog().getType();
            if (chatType == QBDialogType.GROUP || chatType == QBDialogType.PUBLIC_GROUP) {
                type = 0;
                AlertDialogUtils.showChatOptionDialog(mContext, type, pos, this);
            } else if (chatType == QBDialogType.PRIVATE) {
                type = 1;
                AlertDialogUtils.showChatOptionDialog(mContext, type, pos, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private void deleteGroup(int pos) {
        try {
            final Kitty data = getItem(pos);
            final int position = pos;
            // option delete Group Chat
            if (data.getGroup() != null &&
                    data.getGroup().getIsAdmin() != null
                    && data.getGroup().getIsAdmin().equalsIgnoreCase("1")) {

                if (Utils.checkInternetConnection(mContext)) {

                    if (Utils.isValidString(data.getQbDialog().getDialogId())) {

                        showDialog();
                        // for delete dialog from server

                        ReqRefreshGroup deleteGroupObject = new ReqRefreshGroup();
                        deleteGroupObject.setDelete("1");
                        Call<ServerResponse> call = Singleton.getInstance()
                                .getRestOkClient().deleteGroup(getItem(mPosition).getGroup().getGroupID(),
                                        deleteGroupObject);
                        call.enqueue(new Callback<ServerResponse>() {
                            @Override
                            public void onResponse(Call<ServerResponse> call,
                                                   Response<ServerResponse> response) {
                                if (response.code() == 200) {
                                    if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                        ChatHelper.getInstance().deleteDialog(getItem(mPosition).getQbDialog(),
                                                new QBEntityCallback<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid, Bundle bundle) {
                                                        QbDialogHolder.getInstance().deleteDialogById(data.getQbDialog().getDialogId());
                                                        GroupPrefHolder.getInstance().remove(data.getQbDialog().getDialogId());
                                                        KittyPrefHolder.getInstance().remove(data.getQbDialog().getDialogId());

                                                        hideDialog();
                                                        remove(mPosition);
                                                        AlertDialogUtils.showSnackToast(
                                                                mContext.getResources().getString(R.string.delete_group_success),
                                                                mContext);
                                                        //deleteChat(position);
                                                    }

                                                    @Override
                                                    public void onError(QBResponseException e) {
                                                        hideDialog();
                                                        AlertDialogUtils.showSnackToast(mContext.getResources()
                                                                .getString(R.string.quick_blox_error_found), mContext);

                                                    }
                                                });


                                    } else {
                                        hideDialog();
                                        if (Utils.isValidString(response.body().getMessage())) {
                                            AlertDialogUtils.showSnackToast(response.body().getMessage(),
                                                    mContext);
                                        } else {
                                            AlertDialogUtils.showSnackToast(mContext.getResources().getString(R.string.server_error),
                                                    mContext);
                                        }
                                    }
                                } else {
                                    hideDialog();
                                    if (Utils.isValidString(response.body().getMessage())) {
                                        AlertDialogUtils.showSnackToast(response.body().getMessage(),
                                                mContext);
                                    } else {
                                        AlertDialogUtils.showSnackToast(mContext.getResources()
                                                        .getString(R.string.server_error),
                                                mContext);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ServerResponse> call, Throwable t) {
                                hideDialog();
                                AlertDialogUtils.showSnackToast(mContext.getResources()
                                        .getString(R.string.server_error), mContext);

                            }
                        });
                    }
                } else {
                    AlertDialogUtils.showSnackToast(mContext.getResources()
                            .getString(R.string.no_internet_available), mContext);
                }
            } else {
                AlertDialogUtils.showSnackToast(mContext.getResources()
                        .getString(R.string.delete_group_warning), mContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeleteGroup(final int pos) {
        try {
            Kitty data = getItem(pos);
            if (data.getGroup() != null &&
                    data.getGroup().getIsAdmin() != null
                    && data.getGroup().getIsAdmin().equalsIgnoreCase("1")) {

                showAlertDialog(context.getString(R.string.delete_group_dialog, "delete"),
                        pos, new OnUserSelectionListener() {
                            @Override
                            public void onProceed() {
                                deleteGroup(pos);
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
            } else {
                AlertDialogUtils.showSnackToast(mContext.getResources()
                        .getString(R.string.delete_group_warning), mContext);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    private void deleteChat(int position) {
        try {
            final int pos = position;
            final Kitty data = getItem(pos);
            if (Utils.checkInternetConnection(mContext)) {
                showDialog();
                ChatHelper.getInstance().deleteDialog(getItem(position).getQbDialog(),
                        new QBEntityCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid, Bundle bundle) {
                                hideDialog();
                                remove(mPosition);
                                AlertDialogUtils.showSnackToast(
                                        mContext.getResources()
                                                .getString(R.string.delete_group_success),
                                        mContext);
                                QbDialogHolder.getInstance().deleteDialogById(data.getQbDialog()
                                        .getDialogId());

                            }

                            @Override
                            public void onError(QBResponseException e) {
                                hideDialog();
                                AlertDialogUtils.showSnackToast(mContext.getResources()
                                        .getString(R.string.quick_blox_error_found), mContext);

                            }
                        });
            } else {
                AlertDialogUtils.showSnackToast(mContext.getResources()
                        .getString(R.string.no_internet_available), mContext);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        //For delete dialog from qb
       /* QbDialogUtils.deleteChatDialog(data.getQbDialog().getDialogId(),
                new QbUpdateDialogListener() {
                    @Override
                    public void onSuccessResponce() {
                        remove(pos);
                        AlertDialogUtils.showSnackToast(
                                mContext.getResources().getString(R.string.delete_group_success),
                                mContext);
                        deleteChat(pos);
                    }

                    @Override
                    public void onError() {
                        AlertDialogUtils.showSnackToast(mContext.getResources()
                                .getString(R.string.quick_blox_error_found), mContext);
                    }
                });*/
    }

    @Override
    public void onRefreshGroupChatData(int pos) {
        // option refresh Group Chat
        try {
            Kitty data = getItem(pos);
            if (data.getGroup().getIsAdmin().equalsIgnoreCase("1")) {

                if (Utils.checkInternetConnection(mContext)) {
                    showDialog();
                    ReqRefreshGroup reqRefreshGroup = new ReqRefreshGroup();
                    reqRefreshGroup.setDelete("0");
                    Call<ServerResponse> call = Singleton.getInstance()
                            .getRestOkClient().refershGroup(data.getGroup().getGroupID(),
                                    reqRefreshGroup);
                    call.enqueue(new Callback<ServerResponse>() {
                        @Override
                        public void onResponse(Call<ServerResponse> call,
                                               Response<ServerResponse> response) {
                            hideDialog();
                            if (response.code() == 200) {
                                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                    if (Utils.isValidString(response.body().getMessage())) {
                                        AlertDialogUtils.showSnackToast(response.body().getMessage(),
                                                mContext);
                                    }
                                } else {
                                    if (Utils.isValidString(response.body().getMessage())) {
                                        AlertDialogUtils.showSnackToast(response.body().getMessage(),
                                                mContext);
                                    }
                                }
                            } else {
                                if (Utils.isValidString(response.body().getMessage())) {
                                    AlertDialogUtils.showSnackToast(response.body().getMessage(),
                                            mContext);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ServerResponse> call, Throwable t) {
                            hideDialog();
                        }
                    });
                } else {
                    AlertDialogUtils.showSnackToast(mContext.getResources()
                            .getString(R.string.no_internet_available), mContext);
                }
            } else {
                AlertDialogUtils.showSnackToast(mContext.getResources()
                        .getString(R.string.refresh_group_warning), mContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDeletePrivateChatData(final int pos) {
        try {
            showAlertDialog(context.getString(R.string.delete_alert_msg, "delete"), pos, new OnUserSelectionListener() {
                @Override
                public void onProceed() {
                    deleteChat(pos);
                }

                @Override
                public void onCancel() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlertDialog(String msg, final int pos, final OnUserSelectionListener listener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(msg);
            builder.setTitle("Alert");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onProceed();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onCancel();
                }
            });

            builder.create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            try {
                final ArrayList<Kitty> newList = new ArrayList<>();

                AppLog.d(TAG, "FilterResults " + filterString);

                String filterableString;

                for (int i = 0; i < mListClone.size(); i++) {
                    filterableString = mListClone.get(i).getQbDialog().getName().toLowerCase();
                    if (filterableString.toLowerCase().contains(filterString)) {
                        newList.add(mListClone.get(i));
                    }
                }

//                if (newList.isEmpty())
//                    newList.addAll(mListClone)
                AppLog.d(TAG, "FilterResults " + new Gson().toJson(newList));

                results.values = newList;
                results.count = newList.size();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null) {
                if (results.count > 0)
                    updateList((List<Kitty>) results.values);
                else
                    updateList(new ArrayList<Kitty>());
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }


        }
    }

    public void reloadData() {
        try {
            AppLog.d(TAG, "reload " + new Gson().toJson(mListClone).toString());
            updateList(mListClone);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialog() {
        mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getResources().getString(R.string.loading_text));
        mDialog.show();
    }

    public void hideDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public void updateChatList(List<Kitty> list) {
        AppLog.d(TAG, "reload " + new Gson().toJson(list).toString());
        mListClone.clear();
        mListClone.addAll(list);
    }
}

class ChatListHolder {
    CustomTextViewBold txtChatTitle;
    CustomTextViewNormal txtChatLastMessage;
    ImageView imgChatHost;
    ImageView imgChatAnnouncement;
    ImageView imgChatDiary;
    ImageView imgChatAdminHost;
    RoundedImageView imgChatUser;
    RelativeLayout rlMain;
    TextView txtMessageUnreadCount;

    private int position;
    private ProgressDialog mDialog;

    public void setPosition(int position) {
        this.position = position;
    }

    public void bind(Kitty kitty) {
        try {
            QBDialog dialog = kitty.getQbDialog();
            setChatTitle(dialog.getName());

            setLastMessage(dialog.getLastMessage());
            if (dialog.getUnreadMessageCount() != null && dialog.getUnreadMessageCount() > 0)
                setUnreadMessageCount(dialog.getUnreadMessageCount());
            else
                txtMessageUnreadCount.setVisibility(View.GONE);
            setPrivateAndGroupChatAttribute(kitty);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPrivateAndGroupChatAttribute(Kitty kitty) {
        try {
            QBDialog dialog = kitty.getQbDialog();
            ChatData chatData = kitty.getGroup();
            switch (dialog.getType()) {
                case GROUP:
                case PUBLIC_GROUP:
                    disableIcons();
//                    setGroupChatView(chatData);
                    if (chatData != null && Utils.isValidString(chatData.getGroupImage()))
                        setProfileImage(chatData.getGroupImage());
                    break;

                case PRIVATE:
                    setProfileImage(dialog.getPhoto());
                    disableIcons();
                    break;

                default:
                    String msg = ResourceUtils.getString(R.string.chat_unsupported_type);
                    Toaster.shortToast(String.format("%s %s", msg, dialog.getType().name()));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setRulesVisibility(ChatData chatData) {
        try {
            imgChatAdminHost.setVisibility(chatData.getAdminHostVisibility());
            imgChatHost.setVisibility(chatData.getChatHostVisibility());
            imgChatAnnouncement.setVisibility(chatData.getAnnouncementVisibility());
            imgChatDiary.setVisibility(chatData.getDiaryVisibility());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setGroupChatView(ChatData chatData) {
        try {
            if (chatData != null) {
                setRulesVisibility(chatData);
                setAdminHostClick(chatData);
                setAnnouncementClick(chatData);
                setChatHostClick(chatData);
                setDiaryClick(chatData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableIcons() {
        imgChatAdminHost.setVisibility(View.GONE);
        imgChatHost.setVisibility(View.GONE);
        imgChatAnnouncement.setVisibility(View.GONE);
        imgChatDiary.setVisibility(View.GONE);
    }

//    private void setPrivateChatView(ChatData chatData) {
//        disableIcons();
//    }

    private void setProfileImage(String url) {
        try {
            ImageUtils.getImageLoader(rlMain.getContext()).displayImage(url, imgChatUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setChatTitle(String title) {
        txtChatTitle.setText(title);
    }

    private void setLastMessage(String message) {
        txtChatLastMessage.setText(message);
    }

    private void setDiaryClick(ChatData data) {
        imgChatDiary.setTag(data);
        imgChatDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ChatData data = (ChatData) v.getTag();
                    Intent intent = new Intent(v.getContext(), KittyDiaryActivity.class);
                    intent.putExtra(AppConstant.INTENT_DIARY_DATA,
                            new Gson().toJson(data).toString());
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setAdminHostClick(ChatData data) {
        imgChatAdminHost.setTag(data);
        imgChatAdminHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ChatData data = (ChatData) v.getTag();
                    Intent intent = new Intent(v.getContext(), VenueActivity.class);
                    intent.putExtra(AppConstant.INTENT_CHAT_DATA, new Gson().toJson(data));
                    /*intent.putExtra(AppConstant.GROUP_ID, data.getKittyId());
                    intent.putExtra(AppConstant.GROUP_ID, data.getGroupID());
                    intent.putExtra(AppConstant.KITTY_DATE, data.getKittyDate());
                    intent.putExtra(AppConstant.KITTY_NAME, data.getName());
                    intent.putExtra(AppConstant.VENUE_PUNCH, data.getPunctuality());
                    intent.putExtra(AppConstant.KIIITY_TIME, data.getKittyTime());*/
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setChatHostClick(ChatData data) {
        imgChatHost.setTag(data);
        imgChatHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ChatData data = (ChatData) v.getTag();
                    Intent intent = new Intent(v.getContext(), VenueActivity.class);
                    intent.putExtra(AppConstant.INTENT_CHAT_DATA, new Gson().toJson(data));
                   /* intent.putExtra(AppConstant.GROUP_ID, data.getKittyId());
                    intent.putExtra(AppConstant.GROUP_ID, data.getGroupID());
                    intent.putExtra(AppConstant.KITTY_DATE, data.getKittyDate());
                    intent.putExtra(AppConstant.KITTY_NAME, data.getName());
                    intent.putExtra(AppConstant.VENUE_PUNCH, data.getPunctuality());
                    intent.putExtra(AppConstant.KIIITY_TIME, data.getKittyTime());*/
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setAnnouncementClick(ChatData data) {
        imgChatAnnouncement.setTag(data);
        imgChatAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(v.getContext(), PromotionalActivity.class);
                    intent.putExtra("pos", 5);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setUnreadMessageCount(int count) {
        try {
            txtMessageUnreadCount.setVisibility(View.VISIBLE);
            txtMessageUnreadCount.setText("" + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
