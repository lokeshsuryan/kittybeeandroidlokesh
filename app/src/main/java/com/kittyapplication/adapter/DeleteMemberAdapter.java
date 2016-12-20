package com.kittyapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 18/8/16.
 * vaghela.pintu31@gmail.com
 */
public class DeleteMemberAdapter extends BaseAdapter {
    private static final String TAG = DeleteMemberAdapter.class.getSimpleName();

    private List<ParticipantMember> mList;
    private List<ParticipantMember> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private List<ParticipantMember> mSelectedList;

    public DeleteMemberAdapter(Context context, List<ParticipantMember> lists) {
        mList = lists;
        mListClone = lists;
        mContext = context;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSelectedList = new ArrayList<>();
        AppLog.d(TAG, ">>>" + new Gson().toJson(lists).toString());
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
        /*private RoundedImageView mImgUser;
        private CustomTextViewBold mTxtUserName;
        private CustomTextViewNormal mTxtNumber;
        private CheckBox mCbDeleteMember = null;
        private RelativeLayout mRlMain;*/

        private RoundedImageView mImgUser, mImgUserWith;
        private CustomTextViewBold mTxtUserName, mTxtUserNameWith;
        private CustomTextViewNormal mTxtNumber, mTxtNumberWith;
        private CheckBox mCbIsAddToGroup = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        try {
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

            //For Check it is Couple Or Not
            if (mList.get(position).getNumber().contains("-!-")) {

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

                    String phoneWith = number.length > 0 ? number[1] : "";
                    phoneWith = phoneWith.replace(AppConstant.SEPERATOR_STRING, "");

                    String userNameWith = null;
                    try {
                        userNameWith = name.length > 0 ? name[1] : "";
                        userNameWith = userNameWith.replace(AppConstant.SEPERATOR_STRING, "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String profileWith = images.length > 0 ? images[1] : "";
                    profileWith = profileWith.replace(AppConstant.SEPERATOR_STRING, "");


                    viewHolder.mTxtNumber.setText(phone);

                    viewHolder.mTxtUserName.setText(Utils.checkIfMe(mContext,
                            phone, userName));

                    ImageUtils.getImageLoader(mContext).displayImage(profile,
                            viewHolder.mImgUser);

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
                            int pos = (int) buttonView.getTag();
                            if (isChecked) {
                                addToSeletedList(mList.get(pos), true);
                            } else {
                                addToSeletedList(mList.get(pos), false);
                            }
                        }
                    });


            convertView.setTag(R.id.cbAddGroup, viewHolder.mCbIsAddToGroup.isChecked());
            convertView.setTag(R.id.txtGroupMemberName, position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = (boolean) v.getTag(R.id.cbAddGroup);
                    int pos = (int) v.getTag(R.id.txtGroupMemberName);
                    if (isChecked) {
                        addToSeletedList(mList.get(pos), false);
                    } else {
                        addToSeletedList(mList.get(pos), true);
                    }
                }
            });
        } catch (Exception e) {
        }
        return convertView;
    }

    private void addToSeletedList(ParticipantMember object, boolean isAdd) {
        if (isAdd) {
            if (mSelectedList.isEmpty()) {
                mSelectedList.add(object);
            } else {
                if (!checkIsSelected(object)) {
                    mSelectedList.add(object);
                }
            }
        } else {
            if (mSelectedList.size() > 0)
                mSelectedList.remove(object);
        }
        notifyDataSetChanged();
    }

    public List<ParticipantMember> getSelectedList() {
        return mSelectedList;
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