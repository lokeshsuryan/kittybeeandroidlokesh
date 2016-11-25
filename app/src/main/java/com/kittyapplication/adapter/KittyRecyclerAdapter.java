package com.kittyapplication.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.core.ui.adapter.BaseSelectedRecyclerViewAdapter;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.ui.activity.KittyDiaryActivity;
import com.kittyapplication.ui.activity.PromotionalActivity;
import com.kittyapplication.ui.activity.SettingActivity;
import com.kittyapplication.ui.activity.VenueActivity;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MIT on 11/15/2016.
 */

public class KittyRecyclerAdapter extends BaseSelectedRecyclerViewAdapter<ChatData>
        implements Filterable {

    private static final String TAG = KittyRecyclerAdapter.class.getSimpleName();
    private ItemFilter filter;
    private ProgressDialog mDialog;
    private List<ChatData> mListClone;
    private ActionMode currentActionMode;

    public KittyRecyclerAdapter(Context context, List<ChatData> objectsList) {
        super(context, objectsList);
        filter = new ItemFilter();
        mListClone = objectsList;
    }

    public void setCurrentActionMode(ActionMode currentActionMode) {
        this.currentActionMode = currentActionMode;
    }

    public void addCreatedDialog(ChatData group) {
        try {
            addAt(0, group);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateObject(int pos, ChatData data) {
        try {
            getList().set(pos, data);
            notifyDataSetChanged();
        } catch (Exception e) {

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = inflater.inflate(R.layout.row_chat_list, null, false);
        return new GroupChatHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        GroupChatHolder chatHolder = (GroupChatHolder) holder;
        chatHolder.txtChatLastMessage.setVisibility(View.GONE);
        chatHolder.bind(getItem(position), position);
        downloadMore(position);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            try {
                List<ChatData> list = mListClone;
                int count = list.size();
                final ArrayList<ChatData> newList = new ArrayList<>(count);

                String filterableString;

                for (int i = 0; i < count; i++) {
                    filterableString = list.get(i).getName();
                    if (filterableString.toLowerCase().contains(filterString.toLowerCase())) {
                        AppLog.d(TAG, "Match = " + list.get(i).getName());
                        newList.add(list.get(i));
                    }
                }

                results.values = newList;
                results.count = newList.size();
                AppLog.d(TAG, "Count" + newList.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            try {
                updateList((List<ChatData>) results.values);
                notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void reloadData() {
        try {
            updateList(mListClone);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialog() {
        mDialog = new ProgressDialog(context);
        mDialog.setMessage(context.getResources().getString(R.string.loading_text));
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


    class GroupChatHolder extends RecyclerView.ViewHolder {
        CustomTextViewBold txtChatTitle;
        CustomTextViewNormal txtChatLastMessage;
        ImageView imgChatHost;
        ImageView imgChatAnnouncement;
        ImageView imgChatDiary;
        ImageView imgChatAdminHost;
        RoundedImageView imgChatUser;
        RelativeLayout rlMain;
        ImageView cbSelected;
        TextView txtMessageUnreadCount;

        public GroupChatHolder(View convertView) {
            super(convertView);
            imgChatUser = (RoundedImageView) convertView.findViewById(R.id.imgChatUser);
            imgChatHost = (ImageView) convertView.findViewById(R.id.imgChatHost);
            imgChatDiary = (ImageView) convertView.findViewById(R.id.imgChatDairy);
            imgChatAnnouncement = (ImageView) convertView.findViewById(R.id.imgChatAnnouncement);
            txtChatTitle = (CustomTextViewBold) convertView.findViewById(R.id.txtChatTitle);
            txtChatLastMessage = (CustomTextViewNormal) convertView.findViewById(R.id.txtChatLastMessage);
            txtMessageUnreadCount = (TextView) convertView.findViewById(R.id.txtMessageUnreadCount);
            imgChatAdminHost = (ImageView) convertView.findViewById(R.id.imgChatHostAdmin);
            rlMain = (RelativeLayout) convertView.findViewById(R.id.rlChatRow);
            cbSelected = (ImageView) convertView.findViewById(R.id.cbSelected);
        }

        private void setItemClickListener(ChatData data, int pos) {
            itemView.setTag(data);
            itemView.setTag(R.id.cbSelected, pos);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag(R.id.cbSelected);
                    AppApplication.getInstance().setSelectedPosition(pos);
                    ChatData data = (ChatData) v.getTag();
                    Intent intent = new Intent((AppCompatActivity) context, SettingActivity.class);
                    intent.putExtra(AppConstant.INTENT_CHAT, new Gson().toJson(data));
                    ((AppCompatActivity) context).startActivity(intent);
                }
            });

        }

        public void bind(ChatData data, int pos) {
            try {
                txtChatTitle.setText(data.getName());
                cbSelected.setVisibility(View.GONE);
                txtMessageUnreadCount.setVisibility(View.GONE);
                setKittyView(data);
                if (data != null && Utils.isValidString(data.getGroupImage()))
                    setProfileImage(data.getGroupImage());
                setItemClickListener(data, pos);
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

        private void setKittyView(ChatData chatData) {
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

        private void setProfileImage(String url) {
            try {
                ImageUtils.getImageLoader(rlMain.getContext()).displayImage(url, imgChatUser);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
}

