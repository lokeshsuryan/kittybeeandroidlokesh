package com.kittyapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.List;

/**
 * Created by Pintu Riontech on 17/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ParticipantAdapter extends BaseAdapter {
    private static final String TAG = ContactAdapter.class.getSimpleName();

    private List<ParticipantMember> mList;
    private List<ParticipantMember> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private String mIsRuleSet;

    public ParticipantAdapter(Context context, List<ParticipantMember> lists, String isSetRule) {
        mList = lists;
        mListClone = lists;
        mContext = context;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIsRuleSet = isSetRule;
        AppLog.d(TAG, "Size =  " + mList.size());
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        private RoundedImageView imgUser, imgUserWith;
        private CustomTextViewNormal txtMemberNumber, txtMemberNumberWith,
                txtParticipantHosted, txtCurrentHost, txtAdmin, txtNotHosted;
        private CustomTextViewBold txtMemberName, txtMemberNameWith;


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        try {
            if (convertView == null) {
                viewHolder = new Holder();
                convertView = inflater.inflate(R.layout.row_layout_participant, parent, false);
                viewHolder.imgUser = (RoundedImageView) convertView.findViewById(R.id.imgParticipant);
                viewHolder.txtMemberNumber = (CustomTextViewNormal) convertView.findViewById(R.id.txtParticipantNumber);
                viewHolder.txtMemberName = (CustomTextViewBold) convertView.findViewById(R.id.txtParticipantName);
                viewHolder.txtCurrentHost = (CustomTextViewNormal) convertView.findViewById(R.id.txtParticipantCurrentHost);
                viewHolder.txtAdmin = (CustomTextViewNormal) convertView.findViewById(R.id.txtParticipantAdmin);

                viewHolder.imgUserWith = (RoundedImageView) convertView.findViewById(R.id.imgParticipantWith);
                viewHolder.txtMemberNumberWith = (CustomTextViewNormal) convertView.findViewById(R.id.txtParticipantNumberWith);
                viewHolder.txtMemberNameWith = (CustomTextViewBold) convertView.findViewById(R.id.txtParticipantNameWith);
                viewHolder.txtParticipantHosted = (CustomTextViewNormal) convertView.findViewById(R.id.txtHostedParticipant);
                viewHolder.txtNotHosted = (CustomTextViewNormal) convertView.findViewById(R.id.txtParticipantNotHosted);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (Holder) convertView.getTag();
            }

            viewHolder.txtAdmin.setVisibility(View.GONE);
            viewHolder.txtCurrentHost.setVisibility(View.GONE);
            viewHolder.txtParticipantHosted.setVisibility(View.GONE);
            if (Utils.isValidString(mIsRuleSet) && mIsRuleSet.equalsIgnoreCase("1")) {
                viewHolder.txtNotHosted.setVisibility(View.VISIBLE);
            } else {
                viewHolder.txtNotHosted.setVisibility(View.GONE);
            }

            //For Check it is Couple Or Not
            if (mList.get(position).getNumber().contains("-!-")) {

                viewHolder.txtMemberNameWith.setVisibility(View.VISIBLE);
                viewHolder.txtMemberNumberWith.setVisibility(View.VISIBLE);
                viewHolder.imgUserWith.setVisibility(View.VISIBLE);


                String[] name = mList.get(position).getName().split("-!-");
                String[] images = mList.get(position).getProfile().split("-!-");
                String[] number = mList.get(position).getNumber().split("-!-");
                String[] currentHost = mList.get(position).getCurrentHost().split("-!-");
                String[] admin = mList.get(position).getIsAdmin().split("-!-");
                String[] hosted = mList.get(position).getHost().split("-!-");

                if (number.length == 1) {
                    String phone = number.length > 0 ? number[0] : "";
                    phone = phone.replace(AppConstant.SEPERATOR_STRING, "");

                    String userName = name.length > 0 ? name[0] : "";
                    userName = userName.replace(AppConstant.SEPERATOR_STRING, "");

                    String profile = images.length > 0 ? images[0] : "";
                    profile = profile.replace(AppConstant.SEPERATOR_STRING, "");

                    viewHolder.txtMemberNumber.setText(phone);

                    viewHolder.txtMemberName.setText(Utils.checkIfMe(mContext,
                            phone, userName));

                    ImageUtils.getImageLoader(mContext).displayImage(profile,
                            viewHolder.imgUser);

                    viewHolder.txtMemberNameWith.setVisibility(View.GONE);
                    viewHolder.txtMemberNumberWith.setVisibility(View.GONE);
                    viewHolder.imgUserWith.setVisibility(View.GONE);


                    String isAdmin = admin.length > 0 ? admin[0] : "";
                    String isCurrentHost = currentHost.length > 0 ? currentHost[0] : "";
                    String isHost = hosted.length > 0 ? hosted[0] : "";

                    isAdmin = isAdmin.replace(AppConstant.SEPERATOR_STRING, "");
                    isCurrentHost = isCurrentHost.replace(AppConstant.SEPERATOR_STRING, "");

                    if (Utils.isValidString(mIsRuleSet) && mIsRuleSet.equalsIgnoreCase("1")) {
                        if (isHost.equalsIgnoreCase("1")) {
                            viewHolder.txtParticipantHosted.setVisibility(View.VISIBLE);
                            viewHolder.txtCurrentHost.setVisibility(View.GONE);
                            viewHolder.txtNotHosted.setVisibility(View.GONE);
                        }

                        if (isCurrentHost.equalsIgnoreCase("1")) {
                            viewHolder.txtCurrentHost.setVisibility(View.VISIBLE);
                            viewHolder.txtParticipantHosted.setVisibility(View.GONE);
                            viewHolder.txtNotHosted.setVisibility(View.GONE);
                        }
                    }

                    if (isAdmin.equalsIgnoreCase("1")) {
                        viewHolder.txtAdmin.setVisibility(View.VISIBLE);
                    }


                } else {
                    String phone = number.length > 0 ? number[0] : "";
                    phone = phone.replace(AppConstant.SEPERATOR_STRING, "");

                    String userName = name.length > 0 ? name[0] : "";
                    userName = userName.replace(AppConstant.SEPERATOR_STRING, "");

                    String profile = images.length > 0 ? images[0] : "";
                    profile = profile.replace(AppConstant.SEPERATOR_STRING, "");

                    viewHolder.txtMemberNumber.setText(phone);

                    viewHolder.txtMemberName.setText(Utils.checkIfMe(mContext,
                            phone, userName));

                    ImageUtils.getImageLoader(mContext).displayImage(profile,
                            viewHolder.imgUser);

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

                    viewHolder.txtMemberNumberWith.setText(phoneWith);

                    viewHolder.txtMemberNameWith.setText(Utils.checkIfMe(mContext,
                            phoneWith, userNameWith));

                    ImageUtils.getImageLoader(mContext).displayImage(profileWith,
                            viewHolder.imgUserWith);


                    if (checkIsAdminOrHost(admin)) {
                        viewHolder.txtAdmin.setVisibility(View.VISIBLE);
                    }
                    if (Utils.isValidString(mIsRuleSet) && mIsRuleSet.equalsIgnoreCase("1")) {
                        if (checkIsAdminOrHost(hosted)) {
                            viewHolder.txtParticipantHosted.setVisibility(View.VISIBLE);
                            viewHolder.txtCurrentHost.setVisibility(View.GONE);
                            viewHolder.txtNotHosted.setVisibility(View.GONE);
                        }
                        if (checkIsAdminOrHost(currentHost)) {
                            viewHolder.txtCurrentHost.setVisibility(View.VISIBLE);
                            viewHolder.txtParticipantHosted.setVisibility(View.GONE);
                            viewHolder.txtNotHosted.setVisibility(View.GONE);
                        }
                    }

                }

            } else {

                viewHolder.txtMemberName.setText(Utils.checkIfMe(mContext,
                        mList.get(position).getNumber(),
                        mList.get(position).getName()));

                if (Utils.isValidString(mList.get(position).getNumber())) {
                    viewHolder.txtMemberNumber.setText(mList.get(position).getNumber());
                } else {
                    viewHolder.txtMemberNumber.setText(mList.get(position).getNumber());
                }

                ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getProfile(),
                        viewHolder.imgUser);

                viewHolder.txtMemberNameWith.setVisibility(View.GONE);
                viewHolder.txtMemberNumberWith.setVisibility(View.GONE);
                viewHolder.imgUserWith.setVisibility(View.GONE);


                if (mList.get(position).getIsAdmin().equalsIgnoreCase("1")) {
                    viewHolder.txtAdmin.setVisibility(View.VISIBLE);
                }
                if (Utils.isValidString(mIsRuleSet) && mIsRuleSet.equalsIgnoreCase("1")) {
                    if (mList.get(position).getHost().equalsIgnoreCase("1")) {
                        viewHolder.txtParticipantHosted.setVisibility(View.VISIBLE);
                        viewHolder.txtCurrentHost.setVisibility(View.GONE);
                        viewHolder.txtNotHosted.setVisibility(View.GONE);
                    }
                    if (mList.get(position).getCurrentHost().equalsIgnoreCase("1")) {
                        viewHolder.txtCurrentHost.setVisibility(View.VISIBLE);
                        viewHolder.txtParticipantHosted.setVisibility(View.GONE);
                        viewHolder.txtNotHosted.setVisibility(View.GONE);
                    }
                }
            }
        } catch (Exception e) {

        }

        return convertView;
    }

    private boolean checkIsAdminOrHost(String[] adminValue) {
        boolean flag = false;
        for (int i = 0; i < adminValue.length; i++) {
            if (adminValue[i].equalsIgnoreCase("1")) {
                flag = true;
                break;
            }
        }

        return flag;
    }

}