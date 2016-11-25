package com.kittyapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.kittyapplication.R;
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
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 8/8/16.
 * vaghela.pintu31@gmail.com
 */
public class TempChatListAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = TempChatListAdapter.class.getSimpleName();

    private List<ChatData> mList;
    private List<ChatData> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private ItemFilter filter;

    public TempChatListAdapter(Context context, List<ChatData> lists) {
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
    public Object getItem(int position) {
        return position;
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
        private CustomTextViewBold txtChatTitle;
        private CustomTextViewNormal txtChatLastMessage;
        private ImageView imgChatHost;
        private ImageView imgChatAnnoucenment;
        private ImageView imgChatDiary;
        private ImageView imgChatAdminHost;
        private RoundedImageView imgChatUser;
        private RelativeLayout rlMain;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_chat_list, parent, false);

            viewHolder = new Holder();
            viewHolder.imgChatUser = (RoundedImageView) convertView.findViewById(R.id.imgChatUser);
            viewHolder.imgChatHost = (ImageView) convertView.findViewById(R.id.imgChatHost);
            viewHolder.imgChatDiary = (ImageView) convertView.findViewById(R.id.imgChatDairy);
            viewHolder.imgChatAnnoucenment = (ImageView) convertView.findViewById(R.id.imgChatAnnouncement);
            viewHolder.txtChatTitle = (CustomTextViewBold) convertView.findViewById(R.id.txtChatTitle);
            viewHolder.txtChatLastMessage = (CustomTextViewNormal) convertView.findViewById(R.id.txtChatLastMessage);
            viewHolder.imgChatAdminHost = (ImageView) convertView.findViewById(R.id.imgChatHostAdmin);
            viewHolder.rlMain = (RelativeLayout) convertView.findViewById(R.id.rlChatRow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }


        if (Utils.isValidString(mList.get(position).getName())) {
            viewHolder.txtChatTitle.setText(mList.get(position).getName());
        } else {
            viewHolder.txtChatTitle.setText("");
        }

        ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getGroupImage(), viewHolder.imgChatUser);

        if (!mList.get(position).getRule().equalsIgnoreCase("0")) {
            String userID = PreferanceUtils.getLoginUserObject(mContext).getUserID();
            ChatData data = mList.get(position);
            boolean isHostEmpty = getHostDetail(data);

            viewHolder.imgChatAdminHost.setVisibility(View.GONE);
            viewHolder.imgChatHost.setVisibility(View.GONE);
            viewHolder.imgChatAnnoucenment.setVisibility(View.GONE);
            viewHolder.imgChatDiary.setVisibility(View.GONE);


            if (isHostEmpty && data.getIsAdmin().equalsIgnoreCase("1")) {

                viewHolder.imgChatAdminHost.setVisibility(View.VISIBLE);
                viewHolder.imgChatAnnoucenment.setVisibility(View.VISIBLE);
            } else if (data.getIsHost().equalsIgnoreCase("1")) {
                viewHolder.imgChatHost.setVisibility(View.VISIBLE);
                viewHolder.imgChatAnnoucenment.setVisibility(View.VISIBLE);

            }
            if (data.getRights().contains(userID)) {

                viewHolder.imgChatHost.setVisibility(View.VISIBLE);
                viewHolder.imgChatAnnoucenment.setVisibility(View.VISIBLE);
            }

            viewHolder.imgChatDiary.setVisibility(View.VISIBLE);

        } else {
            viewHolder.imgChatAdminHost.setVisibility(View.GONE);
            viewHolder.imgChatHost.setVisibility(View.GONE);
            viewHolder.imgChatAnnoucenment.setVisibility(View.GONE);
            viewHolder.imgChatDiary.setVisibility(View.VISIBLE);
        }

        viewHolder.imgChatHost.setTag(position);
        viewHolder.imgChatHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag();
                AppLog.d(TAG, "Data = " + new Gson().toJson(mList.get(pos)).toString());
                Intent intent = new Intent(mContext, VenueActivity.class);
                intent.putExtra(AppConstant.KITTY_ID, mList.get(pos).getKittyId());
                intent.putExtra(AppConstant.GROUP_ID, mList.get(pos).getGroupID());
                intent.putExtra(AppConstant.KITTY_DATE, mList.get(pos).getKittyDate());
                intent.putExtra(AppConstant.KITTY_NAME, mList.get(pos).getName());
                intent.putExtra(AppConstant.VENUE_PUNCH, mList.get(pos).getPunctuality());
                mContext.startActivity(intent);
            }
        });

        viewHolder.imgChatDiary.setTag(position);
        viewHolder.imgChatDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open chat diary
                int pos = (int) v.getTag();
                Intent intent = new Intent(mContext, KittyDiaryActivity.class);
                intent.putExtra(AppConstant.INTENT_DIARY_DATA,
                        new Gson().toJson(mList.get(pos)).toString());
                mContext.startActivity(intent);
            }
        });

        viewHolder.imgChatAnnoucenment.setTag(position);
        viewHolder.imgChatAnnoucenment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PromotionalActivity.class);
                AppLog.d(TAG, "postion" + 5);
                intent.putExtra("pos", 5);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mContext.startActivity(intent);

            }
        });

        viewHolder.imgChatAdminHost.setTag(position);
        viewHolder.imgChatAdminHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //groupId
                //open Vanue activity
                int pos = (int) v.getTag();
                AppLog.d(TAG, "Data = " + new Gson().toJson(mList.get(pos)).toString());
                Intent intent = new Intent(mContext, VenueActivity.class);
                intent.putExtra(AppConstant.KITTY_ID, mList.get(pos).getKittyId());
                intent.putExtra(AppConstant.GROUP_ID, mList.get(pos).getGroupID());
                intent.putExtra(AppConstant.KITTY_DATE, mList.get(pos).getKittyDate());
                intent.putExtra(AppConstant.KITTY_NAME, mList.get(pos).getName());
                intent.putExtra(AppConstant.VENUE_PUNCH, mList.get(pos).getPunctuality());
                mContext.startActivity(intent);
            }
        });

        //viewHolder.txtChatLastMessage.setText(mList.get(position).get);

        viewHolder.rlMain.setTag(R.id.rlChatRow, position);
        viewHolder.rlMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag(R.id.rlChatRow);
                AppLog.d(TAG, "DATA = " + new Gson().toJson(mList.get(pos)).toString());
                Intent intent = new Intent(mContext, SettingActivity.class);
                intent.putExtra(AppConstant.INTENT_CHAT, new Gson().toJson(mList.get(pos)).toString());
                mContext.startActivity(intent);
            }
        });

//        convertView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mContext.startActivity(new Intent(mContext, TempChatActivity.class));
//            }
//        });
        return convertView;
    }


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


    private boolean getHostDetail(ChatData obj) {
        List<String> hostList = obj.getHostId();
        if (hostList != null && !hostList.isEmpty()) {
            for (int i = 0; i < hostList.size(); i++) {
                if (hostList.get(i).equalsIgnoreCase("")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void reloadData() {
        mList = mListClone;
        notifyDataSetChanged();
    }

}