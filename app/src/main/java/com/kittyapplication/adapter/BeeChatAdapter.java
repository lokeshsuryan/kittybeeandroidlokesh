package com.kittyapplication.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.listener.ChatOptionClickListener;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 8/11/16.
 */

public class BeeChatAdapter extends BaseAdapter implements Filterable, ChatOptionClickListener {
    private static final String TAG = KittiesAdapter.class.getSimpleName();

    private List<QBDialog> mList;
    private List<QBDialog> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private ItemFilter filter;
    private int mPosition;

    public BeeChatAdapter(Context context, List<QBDialog> lists) {
        mList = lists;
        mListClone = lists;
        mContext = context;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        filter = new ItemFilter();
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public QBDialog getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public class Holder {
        CustomTextViewBold txtChatTitle;
        CustomTextViewNormal txtChatLastMessage;
        RoundedImageView imgChatUser;
        TextView txtMessageUnreadCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder chatListHolder; // view lookup cache stored in tag
        try {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_layout_bee_chat, parent, false);

                chatListHolder = new Holder();
                chatListHolder.imgChatUser = (RoundedImageView) convertView.findViewById(R.id.imgChatUser);
                chatListHolder.txtChatTitle = (CustomTextViewBold) convertView.findViewById(R.id.txtChatTitle);
                chatListHolder.txtChatLastMessage = (CustomTextViewNormal) convertView.findViewById(R.id.txtChatLastMessage);
                chatListHolder.txtMessageUnreadCount = (TextView) convertView.findViewById(R.id.txtMessageUnreadCount);
                convertView.setTag(chatListHolder);
            } else {
                chatListHolder = (Holder) convertView.getTag();
            }
            chatListHolder.txtChatLastMessage.setVisibility(View.VISIBLE);
            chatListHolder.imgChatUser.setVisibility(View.VISIBLE);
            chatListHolder.txtChatTitle.setVisibility(View.VISIBLE);


            chatListHolder.txtChatTitle.setText(getItem(position).getName());
            chatListHolder.txtChatLastMessage.setText(getItem(position).getLastMessage());
            if (getItem(position).getUnreadMessageCount() > 0) {
                chatListHolder.txtMessageUnreadCount.setVisibility(View.VISIBLE);
                chatListHolder.txtMessageUnreadCount
                        .setText(String.valueOf(getItem(position)
                                .getUnreadMessageCount()));
            } else {
                chatListHolder.txtMessageUnreadCount.setVisibility(View.GONE);
            }

            ImageUtils.getImageLoader(mContext).displayImage(getItem(position).getPhoto(), chatListHolder.imgChatUser);

            convertView.setTag(R.id.imgChatHostAdmin, position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    try {
//                        int pos = (int) v.getTag(R.id.imgChatHostAdmin);
//                        String dialogId = mList.get(pos).getDialogId();
//                        ChatActivity.startForResult((Activity) mContext,
//                                AppConstant.REQUEST_UPDATE_DIALOG, dialogId, null);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            });
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
            QBDialog groupChat = getItem(pos);
            int type = 0;

            QBDialogType chatType = groupChat.getType();
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


    /**
     * filter data
     */
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            List<QBDialog> list = mListClone;
            int count = list.size();
            final ArrayList<QBDialog> newList = new ArrayList<>(count);

            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).getName();
                if (filterableString.toLowerCase().contains(filterString)) {
                    newList.add(list.get(i));
                }
            }

            results.values = newList;
            results.count = newList.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (List<QBDialog>) results.values;
            notifyDataSetChanged();
        }
    }

    public void reloadData() {
        mList = mListClone;
        notifyDataSetChanged();
    }

    public void updateList(List<QBDialog> updatedList) {
        mList = updatedList;
        notifyDataSetChanged();
    }


    private interface OnUserSelectionListener {
        void onProceed();

        void onCancel();
    }

    private void deleteGroup(int pos) {
        try {
            final QBDialog data = getItem(pos);
            final int position = pos;
            // option delete Group Chat
            /*if (data.getGroup() != null &&
                    data.getGroup().getIsAdmin() != null
                    && data.getGroup().getIsAdmin().equalsIgnoreCase("1")) {*/

            if (Utils.checkInternetConnection(mContext)) {

                    /*if (Utils.isValidString(data.getQbDialog().getDialogId())) {

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
                                                    public void onSuccess(Void aVoid, Bundle bundle) {*/
//                QbDialogHolder.getInstance().deleteDialogById(data.getDialogId());


                                      /*                  hideDialog();
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
                        });*/
            } else {
                AlertDialogUtils.showSnackToast(mContext.getResources()
                        .getString(R.string.no_internet_available), mContext);
            }
       /* }else{
            AlertDialogUtils.showSnackToast(mContext.getResources()
                    .getString(R.string.delete_group_warning), mContext);
        }*/
        } catch (Exception e) {

        }
    }

    @Override
    public void onDeleteGroup(final int pos) {
        /*try {
            Kitty data = getItem(pos);
            if (data.getGroup() != null &&
                    data.getGroup().getIsAdmin() != null
                    && data.getGroup().getIsAdmin().equalsIgnoreCase("1")) {

                showAlertDialog(context.getString(R.string.delete_group_dialog, "delete"),
                        pos, new ChatListAdapter.OnUserSelectionListener() {
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
        }*/

    }

    private void deleteChat(int position) {
        /*try {
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
        }*/

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
        /*try {
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
        }*/
    }


    @Override
    public void onDeletePrivateChatData(final int pos) {
        try {
            showAlertDialog(mContext.getString(R.string.delete_alert_msg, "delete"),
                    pos, new OnUserSelectionListener() {
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

    private void showAlertDialog(String msg, final int pos,
                                 final OnUserSelectionListener listener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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

}