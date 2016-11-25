package com.kittyapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 19/9/16.
 */
public class ChangeHostAdapter extends BaseAdapter {
    private static final String TAG = ChangeHostAdapter.class.getSimpleName();

    private List<ParticipantMember> mList;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private List<ParticipantMember> mSelectedList;
    private int mNoOfHostCount, mType;

    //for type == 1 for Aleready Hosted Members
    //for type == 2 for New Hosted Members

    public ChangeHostAdapter(Context context, List<ParticipantMember> lists, int count, int type) {
        mList = lists;
        mContext = context;
        mType = type;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectedList = new ArrayList<>();
        mNoOfHostCount = count;

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
        private RoundedImageView mImgUser, mImgUserWith;
        private CustomTextViewBold mTxtUserName, mTxtUserNameWith;
        private CustomTextViewNormal mTxtNumber, mTxtNumberWith;
        private CheckBox mCbIsAddToGroup = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout_couple_host, parent, false);
            viewHolder = new Holder();
            viewHolder.mImgUser = (RoundedImageView) convertView.findViewById(R.id.imgGroupMember);
            viewHolder.mTxtUserName = (CustomTextViewBold) convertView.findViewById(R.id.txtGroupMemberName);
            viewHolder.mTxtNumber = (CustomTextViewNormal) convertView.findViewById(R.id.txtGroupMemberNumber);
            viewHolder.mCbIsAddToGroup = (CheckBox) convertView.findViewById(R.id.cbAddGroup);
            viewHolder.mImgUserWith = (RoundedImageView) convertView.findViewById(R.id.imgGroupMemberTwo);
            viewHolder.mTxtUserNameWith = (CustomTextViewBold) convertView.findViewById(R.id.txtGroupMemberNameTwo);
            viewHolder.mTxtNumberWith = (CustomTextViewNormal) convertView.findViewById(R.id.txtGroupMemberNumberTwo);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

        try {
            //For Check it is Couple Or Not
            if (mList.get(position).getNumber().contains(AppConstant.SEPERATOR_STRING)) {

                viewHolder.mTxtNumberWith.setVisibility(View.VISIBLE);
                viewHolder.mTxtUserNameWith.setVisibility(View.VISIBLE);
                viewHolder.mImgUserWith.setVisibility(View.VISIBLE);


                String[] name = mList.get(position).getName().split(AppConstant.SEPERATOR_STRING);
                String[] images = mList.get(position).getProfile().split(AppConstant.SEPERATOR_STRING);
                String[] number = mList.get(position).getNumber().split(AppConstant.SEPERATOR_STRING);

                if (number.length == 1) {
                    String phone = number.length > 0 ? number[0] : "";
                    phone = phone.replace(AppConstant.SEPERATOR_STRING, "");

                    String userName = name.length > 0 ? name[0] : "";
                    userName = userName.replace(AppConstant.SEPERATOR_STRING, "");

                    String profile = images.length > 0 ? images[0] : "";
                    profile = profile.replace(AppConstant.SEPERATOR_STRING, "");

                    viewHolder.mTxtNumber.setText(phone);

                    viewHolder.mTxtUserName.setText(Utils.checkIfMe(mContext,
                            phone, userName));

                    ImageUtils.getImageLoader(mContext).displayImage(profile,
                            viewHolder.mImgUser);

                    viewHolder.mTxtNumberWith.setVisibility(View.GONE);
                    viewHolder.mTxtUserNameWith.setVisibility(View.GONE);
                    viewHolder.mImgUserWith.setVisibility(View.GONE);

                } else {
                    String phone = number.length > 0 ? number[0] : "";
                    phone = phone.replace(AppConstant.SEPERATOR_STRING, "");

                    String userName = name.length > 0 ? name[0] : "";
                    userName = userName.replace(AppConstant.SEPERATOR_STRING, "");

                    String profile = images.length > 0 ? images[0] : "";
                    profile = profile.replace(AppConstant.SEPERATOR_STRING, "");

                    viewHolder.mTxtNumber.setText(phone);

                    viewHolder.mTxtUserName.setText(Utils.checkIfMe(mContext,
                            phone, userName));

                    ImageUtils.getImageLoader(mContext).displayImage(profile,
                            viewHolder.mImgUser);

                    String phoneWith = number.length > 0 ? number[1] : "";
                    phoneWith = phoneWith.replace(AppConstant.SEPERATOR_STRING, "");

                    String userNameWith = null;
                    try {
                        userNameWith = name.length > 0 ? name[1] : "";
                        userNameWith = userNameWith.replace(AppConstant.SEPERATOR_STRING, "");
                    } catch (Exception e) {
                        e.printStackTrace();
                        userNameWith = "";
                    }

                    String profileWith = images.length > 0 ? images[1] : "";
                    profileWith = profileWith.replace(AppConstant.SEPERATOR_STRING, "");


                    viewHolder.mTxtNumberWith.setText(phoneWith);
                    viewHolder.mTxtUserNameWith.setText(Utils.checkIfMe(mContext,
                            phoneWith, userNameWith));
                    ImageUtils.getImageLoader(mContext).displayImage(profileWith,
                            viewHolder.mImgUserWith);
                }
            } else {

                viewHolder.mTxtUserName.setText(Utils.checkIfMe(mContext,
                        mList.get(position).getNumber(),
                        mList.get(position).getName()));

                if (Utils.isValidString(mList.get(position).getNumber())) {
                    viewHolder.mTxtNumber.setText(mList.get(position).getNumber());
                } else {
                    viewHolder.mTxtNumber.setText(mList.get(position).getNumber());
                }

                ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getProfile(),
                        viewHolder.mImgUser);

                viewHolder.mTxtNumberWith.setVisibility(View.GONE);
                viewHolder.mTxtUserNameWith.setVisibility(View.GONE);
                viewHolder.mImgUserWith.setVisibility(View.GONE);
            }

            viewHolder.mCbIsAddToGroup.setTag(position);
            viewHolder.mCbIsAddToGroup.setChecked(checkIsSelected(mList.get(position)));
            viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(null);
            viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            try {
                                int pos = (int) buttonView.getTag();
                                if (isChecked) {
                                    if (mType == 2) {
                                        if (mNoOfHostCount != mSelectedList.size()) {
                                            addToSeletedList(mList.get(pos), true);
                                        } else {
                                            buttonView.setChecked(false);
                                            AlertDialogUtils.showSnackToast(mContext
                                                    .getResources()
                                                    .getString(R.string.change_host_select_no_host_warning, "" + mNoOfHostCount), mContext);
                                        }
                                    } else {
                                        addToSeletedList(mList.get(pos), true);
                                    }
                                } else {
                                    addToSeletedList(mList.get(pos), false);
                                }
                            } catch (Exception e) {
                                AppLog.e(TAG, e.getMessage(), e);
                            }
                        }
                    });


        /*convertView.setTag(R.id.cbAddGroup, viewHolder.mCbIsAddToGroup.isChecked());
        convertView.setTag(R.id.txtGroupMemberName, position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = (boolean) v.getTag(R.id.cbAddGroup);
                int pos = (int) v.getTag(R.id.txtGroupMemberName);
                if (isChecked) {
                    addToSeletedList(mList.get(pos), false);
                } else {
                    if (mType == 2) {
                        if (mNoOfHostCount != mSelectedList.size()) {
                            addToSeletedList(mList.get(pos), true);
                        } else {
                            AlertDialogUtils.showSnackToast(mContext
                                    .getResources().getString(R.string.change_host_select_no_host_warning,
                                            mNoOfHostCount), mContext);
                        }
                    } else {
                        addToSeletedList(mList.get(pos), true);
                    }

                }
            }
        });*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private void addToSeletedList(ParticipantMember object, boolean isAdd) {
        if (isAdd) {
            if (mSelectedList.isEmpty()) {
                mSelectedList.add(object);
            } else {
               /* if (mType == 2) {
                    if (mNoOfHostCount != mSelectedList.size()) {
                        if (!checkDataIntoList(object)) {
                            mSelectedList.add(object);
                        }
                    } else {
                        AlertDialogUtils.showSnackToast(mContext
                                .getResources().getString(R.string.change_host_select_no_host_warning,
                                        mNoOfHostCount), mContext);
                    }
                } else {
                    if (!checkDataIntoList(object)) {
                        mSelectedList.add(object);
                    }
                }*/
                if (!checkDataIntoList(object)) {
                    mSelectedList.add(object);
                }
            }
        } else {
            if (mSelectedList.size() > 0)
                mSelectedList.remove(object);
        }
//        notifyDataSetChanged();
    }

    public List<ParticipantMember> getSelectedList() {
        return mSelectedList;
    }

    public List<ParticipantMember> getListData() {
        return mList;
    }

    private boolean checkDataIntoList(ParticipantMember obj) {
        try {
            if (mSelectedList != null && mSelectedList.size() > 0) {
                for (int i = 0; i < mSelectedList.size(); i++) {
                    if (mSelectedList.get(i).getNumber().equalsIgnoreCase(obj.getNumber())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkIsSelected(ParticipantMember obj) {
        if (mSelectedList.size() > 0) {
            for (int i = 0; i < mSelectedList.size(); i++) {
                if (mSelectedList.get(i).getNumber().equalsIgnoreCase(obj.getNumber())) {
                    return true;
                }
            }
        }
        return false;
    }

}