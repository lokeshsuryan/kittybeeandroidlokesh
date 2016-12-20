package com.kittyapplication.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.KittyDiaryActivity;
import com.kittyapplication.ui.activity.KittyWithKidsActivity;
import com.kittyapplication.ui.activity.PromotionalActivity;
import com.kittyapplication.ui.activity.RuleActivity;
import com.kittyapplication.ui.activity.SelectCoupleActivity;
import com.kittyapplication.ui.activity.SettingActivity;
import com.kittyapplication.ui.activity.VenueActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 8/11/16.
 */

public class KittiesAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = KittiesAdapter.class.getSimpleName();

    private List<ChatData> mList;
    private List<ChatData> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private ItemFilter filter;
    private static ProgressDialog mDialog;

    public KittiesAdapter(Context context, List<ChatData> lists) {
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
    public ChatData getItem(int position) {
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
        ImageView imgChatHost;
        ImageView imgChatAnnouncement;
        ImageView imgChatDiary;
        ImageView imgChatAdminHost;
        ImageView imgRule;
        RoundedImageView imgChatUser;
        RelativeLayout rlMain;
        TextView txtMessageUnreadCount;
        private int position;

        public void setPosition(int position) {
            this.position = position;
        }

        /**
         * Set Row View
         *
         * @param chatData
         */
        public void bind(ChatData chatData) {
            setChatTitle(chatData.getName());
//            chatData.setHostEmpty();
//            chatData.setGroupRightsVisibility(mContext);
            disableIcons();
            setGroupChatView(chatData);
            if (Utils.isValidString(chatData.getGroupImage()))
                setProfileImage(chatData.getGroupImage());
        }

        /**
         * set venue diary announcement icon
         *
         * @param chatData
         */
        private void setRulesVisibility(ChatData chatData) {
            try {
                imgChatAdminHost.setVisibility(chatData.getAdminHostVisibility());
                imgChatHost.setVisibility(chatData.getChatHostVisibility());
                imgChatAnnouncement.setVisibility(chatData.getAnnouncementVisibility());
                imgChatDiary.setVisibility(chatData.getDiaryVisibility());
                imgRule.setVisibility(chatData.getRuleVisibility());
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
                    setRuleClick(chatData);
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

        private void setRuleClick(ChatData data) {
            imgRule.setTag(data);
            imgRule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ChatData data = (ChatData) v.getTag();
                        getMembers(data);
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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder chatListHolder; // view lookup cache stored in tag
        try {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_chat_list, parent, false);

                chatListHolder = new Holder();
                chatListHolder.imgChatUser = (RoundedImageView) convertView.findViewById(R.id.imgChatUser);
                chatListHolder.imgChatHost = (ImageView) convertView.findViewById(R.id.imgChatHost);
                chatListHolder.imgRule = (ImageView) convertView.findViewById(R.id.imgKittyRule);
                chatListHolder.imgChatDiary = (ImageView) convertView.findViewById(R.id.imgChatDairy);
                chatListHolder.imgChatAnnouncement = (ImageView) convertView.findViewById(R.id.imgChatAnnouncement);
                chatListHolder.txtChatTitle = (CustomTextViewBold) convertView.findViewById(R.id.txtChatTitle);
                chatListHolder.txtChatLastMessage = (CustomTextViewNormal) convertView.findViewById(R.id.txtChatLastMessage);
                chatListHolder.txtMessageUnreadCount = (TextView) convertView.findViewById(R.id.txtMessageUnreadCount);
                chatListHolder.imgChatAdminHost = (ImageView) convertView.findViewById(R.id.imgChatHostAdmin);
                chatListHolder.rlMain = (RelativeLayout) convertView.findViewById(R.id.rlChatRow);
                convertView.setTag(chatListHolder);
            } else {
                chatListHolder = (Holder) convertView.getTag();
            }
            chatListHolder.txtChatLastMessage.setVisibility(View.GONE);
            chatListHolder.setPosition(position);
            chatListHolder.bind(getItem(position));

            convertView.setTag(R.id.imgChatHostAdmin, position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int pos = (int) v.getTag(R.id.imgChatHostAdmin);
                        Intent intent = new Intent(mContext, SettingActivity.class);
                        intent.putExtra(AppConstant.INTENT_CHAT, new Gson().toJson(mList.get(pos)));
                        mContext.startActivity(intent);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });

        } catch (Exception e) {
        }

        return convertView;
    }


    /**
     * filter data
     */
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            List<ChatData> list = mListClone;
            int count = list.size();
            final ArrayList<ChatData> newList = new ArrayList<>(count);

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
            mList = (List<ChatData>) results.values;
            notifyDataSetChanged();
        }
    }

    public void reloadData() {
        mList = mListClone;
        notifyDataSetChanged();
    }

    public void updateList(List<ChatData> updatedList) {
        AppLog.d(TAG, updatedList.size());
        System.out.println("================== updateList ==========================");
        mListClone = updatedList;
        mList = updatedList;
        notifyDataSetChanged();
    }


    private void getMembers(final ChatData data) {
        if (Utils.checkInternetConnection(mContext)) {
            showProgressDialog(mContext);
            Singleton.getInstance().getRestOkClient().getMembersData(data.getGroupID())
                    .enqueue(new Callback<ServerResponse<List<ContactDao>>>() {
                        @Override
                        public void onResponse(Call<ServerResponse<List<ContactDao>>> call,
                                               Response<ServerResponse<List<ContactDao>>> response) {
                            try {
                                hideProgressDialog();
                                if (response.code() == 200) {
                                    List<ContactDao> dataList = response.body().getData();
                                    if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                        if (dataList != null && !dataList.isEmpty()) {
                                            openKittyRule(dataList, data);
                                        } else {
                                            AppLog.d(TAG, "datalist is null");
                                            showServerError(response.body().getMessage());
                                        }
                                    } else {
                                        AppLog.d(TAG, "datalist is null");
                                        showServerError(response.body().getMessage());
                                    }
                                } else {
                                    AppLog.d(TAG, "datalist is null");
                                    showServerError(response.body().getMessage());
                                }
                            } catch (Exception e) {
                                hideProgressDialog();
                                AppLog.e(TAG, e.getMessage(), e);
                            }
                        }

                        @Override
                        public void onFailure(Call<ServerResponse<List<ContactDao>>> call,
                                              Throwable t) {
                            hideProgressDialog();
                        }
                    });
        } else {
            AlertDialogUtils.showSnackToast(mContext.
                    getResources().getString(R.string.no_internet_available), mContext);
        }
    }

    private void openKittyRule(List<ContactDao> list, ChatData data) {
        CreateGroup group = new CreateGroup();
        group.setGroupMember(list);
        group.setGroupID(data.getGroupID());
        group.setName(data.getName());
        group.setCategory(data.getCategory());
        group.setGroupIMG(data.getGroupImage());
        if (group.getCategory().equalsIgnoreCase(mContext.getResources()
                .getStringArray(R.array.kitty)[0])) {
            Intent i = new Intent(mContext, SelectCoupleActivity.class);
            i.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(group).toString());
            i.putExtra(AppConstant.INTENT_KITTY_TYPE, 0);
            i.putExtra(AppConstant.EXTRA_IS_CREATE_KITTY, 1);
            mContext.startActivity(i);
        } else if (group.getCategory().equalsIgnoreCase(mContext.getResources()
                .getStringArray(R.array.kitty)[1])) {
            Intent i = new Intent(mContext, KittyWithKidsActivity.class);
            i.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(group).toString());
            i.putExtra(AppConstant.INTENT_KITTY_TYPE, 1);
            i.putExtra(AppConstant.EXTRA_IS_CREATE_KITTY, 1);
            mContext.startActivity(i);
        } else {
            Intent i = new Intent(mContext, RuleActivity.class);
            i.putExtra(AppConstant.INTENT_KITTY_DATA, new Gson().toJson(group).toString());
            i.putExtra(AppConstant.INTENT_KITTY_TYPE, 2);
            i.putExtra(AppConstant.EXTRA_IS_CREATE_KITTY, 1);
            mContext.startActivity(i);
        }
    }

    private void showServerError(String error) {
        try {
            if (Utils.isValidString(error)) {
                AlertDialogUtils.showSnackToast(error, mContext);
            } else {
                AlertDialogUtils.showSnackToast(mContext.getResources().getString(R.string.server_error), mContext);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show Progress dialog
     *
     * @param context
     */
    public static void showProgressDialog(Context context) {
        try {
            mDialog = new ProgressDialog(context);
            mDialog.setMessage(context.getResources().getString(R.string.loading_text));
            mDialog.show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Hide Progress dialog
     */
    public static void hideProgressDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }
}