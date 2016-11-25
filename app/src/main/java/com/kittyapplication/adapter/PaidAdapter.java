package com.kittyapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.MemberData;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 19/8/16.
 * vaghela.pintu31@gmail.com
 */
public class PaidAdapter extends BaseAdapter {
    private static final String TAG = PaidAdapter.class.getSimpleName();

    private List<MemberData> mList;
    private List<MemberData> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private List<MemberData> mSelectedList;
    private int mType;

    public PaidAdapter(Context context, List<MemberData> lists) {
        mList = lists;
        mListClone = lists;
        mContext = context;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectedList = new ArrayList<>();
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

    public class Holder {
        private RoundedImageView mImgUser;
        private CustomTextViewBold mTxtUserName;
        private CustomTextViewNormal mTxtNumber;
        private RadioButton rbPaid, rbNotPaid = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout_is_paid, parent, false);
            viewHolder = new Holder();
            viewHolder.mImgUser = (RoundedImageView) convertView.findViewById(R.id.imgContactUser);
            viewHolder.mTxtUserName = (CustomTextViewBold) convertView.findViewById(R.id.txtContactName);
            viewHolder.mTxtNumber = (CustomTextViewNormal) convertView.findViewById(R.id.txtContactTitle);
            viewHolder.rbPaid = (RadioButton) convertView.findViewById(R.id.rbPaid);
            viewHolder.rbNotPaid = (RadioButton) convertView.findViewById(R.id.rbNotPaid);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }


        ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getImage(),
                viewHolder.mImgUser);

        if (mList.get(position).getPhone() != null &&
                mList.get(position).getPhone().length() > 0) {
            String displayName = Utils.getDisplayNameByPhoneNumber(mContext,
                    mList.get(position).getPhone());

            if (Utils.isValidString(displayName)) {
                viewHolder.mTxtUserName.setText(displayName);
            } else {
                viewHolder.mTxtUserName.setText(mList.get(position).getName());
            }
        } else {
            viewHolder.mTxtUserName.setText(mList.get(position).getName());
        }


        AppLog.d(TAG, new Gson().toJson(mList.get(position)).toString());

        if (Utils.isValidString(mList.get(position).getPhone())) {
            viewHolder.mTxtNumber.setText(mList.get(position).getPhone());
        } else {
            viewHolder.mTxtNumber.setText(mList.get(position).getPhone());
        }

        if (mList.get(position).getIsPaid().equalsIgnoreCase("")) {
            viewHolder.rbPaid.setChecked(false);
            viewHolder.rbNotPaid.setChecked(false);
        } else if (mList.get(position).getIsPaid().equalsIgnoreCase("0")) {
            viewHolder.rbPaid.setChecked(false);
            viewHolder.rbNotPaid.setChecked(true);
        } else {
            viewHolder.rbPaid.setChecked(true);
            viewHolder.rbNotPaid.setChecked(false);
        }


        viewHolder.rbPaid.setTag(position);
        viewHolder.rbPaid.
                setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int pos = (int) buttonView.getTag();
                        mList.get(pos).setIsPaid("1");
                    }
                });

        viewHolder.rbNotPaid.setTag(position);
        viewHolder.rbNotPaid.
                setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int pos = (int) buttonView.getTag();
                        mList.get(pos).setIsPaid("0");
                    }
                });

        return convertView;
    }

    private void addToSeletedList(MemberData object, boolean isAdd) {
        if (isAdd) {
            if (mSelectedList.isEmpty()) {
                mSelectedList.add(object);
            } else {
                if (!checkIsSelected(object)) {
                    //if (!mSelectedList.contains(object)) {
                    mSelectedList.add(object);
                    // }
                }
            }
        } else {
            if (mSelectedList.size() > 0)
                mSelectedList.remove(object);
        }
    }

    public List<MemberData> getSelectedList() {
        return mSelectedList;
    }

    public boolean checkIsSelected(MemberData obj) {
        if (mSelectedList.size() > 0) {
            for (int i = 0; i < mSelectedList.size(); i++) {
                if (mSelectedList.get(i).getPhone().equalsIgnoreCase(obj.getPhone())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<MemberData> getListData() {
        return mList;
    }

    public void reloadData() {
        mList = mListClone;
        notifyDataSetChanged();
    }
}