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
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 24/8/16.
 * vaghela.pintu31@gmail.com
 */
public class GiveRightAdapter extends BaseAdapter {
    private static final String TAG = GiveRightAdapter.class.getSimpleName();

    private List<ParticipantMember> mList;
    private List<ParticipantMember> mListClone;
    private List<ParticipantMember> mSelectedList = new ArrayList<>();
    private Context mContext;
    private static LayoutInflater inflater = null;
    private ArrayList<String> mRightsArray;
    private int counter = 0;

    public GiveRightAdapter(Context ctx, List<ParticipantMember> list, ArrayList<String> rightsArray) {
        mList = list;
        mListClone = list;
        mContext = ctx;
        mRightsArray = rightsArray;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        AppLog.d(TAG, new Gson().toJson(list).toString());


    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public ParticipantMember getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public class Holder {
      /*  private RoundedImageView imgUser;
        private CustomTextViewBold txtUserName;
        private CustomTextViewNormal txtNumber;
        private CheckBox cbRights;*/

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

//        String displayName = Utils.checkIfMe(mContext
//                , mList.get(position).getNumber(), mList.get(position).getName());

        if (mRightsArray != null && !mRightsArray.isEmpty()) {
            if (mList.get(position).getUserId().equalsIgnoreCase(mRightsArray.get(0))) {
                if (mList.get(position).getNumber().contains(AppConstant.SEPERATOR_STRING)) {
                    String[] ids = mList.get(position).getUserId().split(AppConstant.SEPERATOR_STRING);
                    if (getIsRightsUser(ids, mRightsArray.get(0))) {
                        addToSeletedList(mList.get(position));
                    }
                } else {
                    addToSeletedList(mList.get(position));
                }
            }
        }

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

                String userNameWith = name.length > 0 ? name[1] : "";
                userNameWith = userNameWith.replace(AppConstant.SEPERATOR_STRING, "");

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
        viewHolder.mCbIsAddToGroup.setChecked(mList.get(position).isCheck());
        viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(null);
        viewHolder.mCbIsAddToGroup.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            if (counter == 0) {
                                int pos = (int) buttonView.getTag();
                                addToSeletedList(mList.get(pos));
                                counter = 1;
                            } else {
                                buttonView.setChecked(false);
                                AlertDialogUtils.showSnackToast(mContext.getResources()
                                        .getString(R.string.give_right_select_only_onc_member_warning), mContext);
                            }
                        } else if (!isChecked && counter == 1) {
                            counter = 0;
                        }
                    }
                });


       /* convertView.setTag(R.id.cbAddGroup, viewHolder.mCbIsAddToGroup.isChecked());
        convertView.setTag(R.id.txtGroupMemberName, position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag(R.id.txtGroupMemberName);
                addToSeletedList(mList.get(pos));
            }
        });*/


        return convertView;
    }

    private void addToSeletedList(ParticipantMember object) {
        mSelectedList.clear();
        mSelectedList.add(object);
//        notifyDataSetChanged();
    }

    private boolean checkDataIntoList(ParticipantMember obj) {
        if (mSelectedList != null && mSelectedList.size() > 0) {
            for (int i = 0; i < mSelectedList.size(); i++) {
                if (mSelectedList.get(i).getNumber().equalsIgnoreCase(obj.getNumber())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ParticipantMember> getSelectedList() {
        return mSelectedList;
    }

    private boolean getIsRightsUser(String[] matchArray, String UserId) {
        boolean flag = false;
        for (int i = 0; i < matchArray.length; i++) {
            String id = matchArray[i];
            id = id.replace(AppConstant.SEPERATOR_STRING, "");
            if (id.equalsIgnoreCase(UserId)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public boolean checkIsSelected(ParticipantMember obj) {
        boolean flag = false;
        boolean isMatch = false;
        if (mSelectedList.size() > 0) {
            if (mSelectedList.get(0).getNumber().contains("-!-")) {

                String[] ids = mSelectedList.get(0).getNumber().split("-!-");

                for (int j = 0; j < ids.length; j++) {

                    String selectedId = ids[j];
                    selectedId = selectedId.replace("-!-", "");

                    if (obj.getNumber().contains("-!-")) {

                        String[] objNumber = obj.getNumber().split("-!-");

                        for (int k = 0; k < objNumber.length; k++) {
                            String objectNumber = objNumber[k];
                            objectNumber = objectNumber.replace("-!-", "");

                            if (objectNumber.equalsIgnoreCase(selectedId)) {
                                flag = true;
                                isMatch = true;
                                break;
                            }

                        }
                        if (isMatch) {
                            break;
                        }
                    }
                }
            } else {
                if (mSelectedList.get(0).getNumber().equalsIgnoreCase(obj.getNumber())) {
                    flag = true;
                }
            }
            flag = false;
        }
        return flag;
    }
}