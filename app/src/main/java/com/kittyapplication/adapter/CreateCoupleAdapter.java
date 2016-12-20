package com.kittyapplication.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.listener.CoupleWithListener;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.ui.activity.CoupleKittyCreateActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 12/8/16.
 * vaghela.pintu31@gmail.com
 */
public class CreateCoupleAdapter extends BaseAdapter implements CoupleWithListener {
    private static final String TAG = CreateCoupleAdapter.class.getSimpleName();

    private List<ContactDao> mList, mTempList;
    private List<ContactDao> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;

    public CreateCoupleAdapter(Context context, List<ContactDao> lists) {
        mList = lists;
        mListClone = lists;
        mContext = context;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        AppLog.d(TAG, "Size =" + new Gson().toJson(lists).toString());
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
        private CustomTextViewNormal txtContactTitle;
        private CustomTextViewBold txtContactName;
        private CustomTextViewNormal txtContactInvite;
        private ImageView imgContactPhone;
        private RoundedImageView imgContactUser;
        private RelativeLayout rlMain;
        private RelativeLayout rlButtons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout_contact, null);
            viewHolder = new Holder();
            viewHolder.imgContactUser = (RoundedImageView) convertView.findViewById(R.id.imgContactUser);
            viewHolder.txtContactName = (CustomTextViewBold) convertView.findViewById(R.id.txtContactName);
            viewHolder.txtContactTitle = (CustomTextViewNormal) convertView.findViewById(R.id.txtContactTitle);
            viewHolder.rlMain = (RelativeLayout) convertView.findViewById(R.id.rlContact);
            viewHolder.rlButtons = (RelativeLayout) convertView.findViewById(R.id.rlContactButtons);
            viewHolder.imgContactPhone = (ImageView) viewHolder.rlButtons.findViewById(R.id.imgContactPhone);
            viewHolder.txtContactInvite = (CustomTextViewNormal) viewHolder.rlButtons.findViewById(R.id.txtContactInvite);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

//        String displayName = Utils.checkIfMe(mContext,
//                mList.get(position).getPhone()
//                , mList.get(position).getName());


        viewHolder.txtContactName.setText(Utils.checkNumberIsLoginUser(mContext
                , mList.get(position).getPhone(), mList.get(position).getName()));


        viewHolder.txtContactTitle.setText(mList.get(position).getPhone());

        ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getImage(),
                viewHolder.imgContactUser);

        viewHolder.txtContactInvite.setVisibility(View.GONE);
        viewHolder.imgContactPhone.setVisibility(View.VISIBLE);
        viewHolder.imgContactPhone.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add_couple_bg));
        //ImageUtils.getImageLoader(mContext).displayImage("drawable://" + R.drawable.ic_add_couple_bg, viewHolder.imgContactPhone);
        convertView.setTag(R.layout.row_layout_contact, position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag(R.layout.row_layout_contact);
                notifyDataSetChanged();
                if (mList.size() > 1) {
                    showDialog(pos);
                } else {

                    ((CoupleKittyCreateActivity) mContext).showSnackbar(mContext.getResources().getString(R.string.create_couple_warning));
                }
            }
        });
        return convertView;
    }


    public List<ContactDao> getMemberList() {
        return mList;
    }


    private void showDialog(int pos) {
        mTempList = new ArrayList<>();
        mTempList.addAll(mList);
        AlertDialogUtils.showDialogSelectCouple(mContext, mTempList, this, pos);
    }

    @Override
    public void getSelectedCoupleWithMember(ContactDao member, ContactDao memberTo) {
        createCouple(member, memberTo);
    }

    public void createCouple(ContactDao memeber, ContactDao memberTo) {
        ContactDao coupleObject = new ContactDao();
        ContactDao coupleTo = memberTo;

        AppLog.d(TAG, "Selected Member + " + new Gson().toJson(memeber).toString());
        AppLog.d(TAG, "Selected Memberasdasd + " + new Gson().toJson(coupleTo).toString());

        coupleObject.setName(mContext.getResources().getString(R.string.add_couple, coupleTo.getName(), memeber.getName()));
        coupleObject.setPhone(mContext.getResources().getString(R.string.add_couple, coupleTo.getPhone(), memeber.getPhone()));
        coupleObject.setIsHost(mContext.getResources().getString(R.string.add_couple, coupleTo.getIsHost(), memeber.getIsHost()));
        coupleObject.setID(mContext.getResources().getString(R.string.add_couple, coupleTo.getID(), memeber.getID()));
        coupleObject.setImage(mContext.getResources().getString(R.string.add_couple, coupleTo.getImage(), memeber.getImage()));
        coupleObject.setFullName(mContext.getResources().getString(R.string.add_couple, coupleTo.getFullName(), memeber.getFullName()));
        coupleObject.setUserId(mContext.getResources().getString(R.string.add_couple, coupleTo.getUserId(), memeber.getUserId()));
        coupleObject.setLogin(mContext.getResources().getString(R.string.add_couple, coupleTo.getLogin(), memeber.getLogin()));
        coupleObject.setRegistration(mContext.getResources().getString(R.string.add_couple, coupleTo.getRegistration(), memeber.getRegistration()));
        coupleObject.setIs_Paid(mContext.getResources().getString(R.string.add_couple, coupleTo.getIs_Paid(),
                memeber.getIs_Paid()));
        coupleObject.setID(mContext.getResources().getString(R.string.add_couple, coupleTo.getID(),
                memeber.getID()));
        mList.remove(memberTo);
        mList.remove(memeber);

        AppLog.d(TAG, "+absdjkn" + new Gson().toJson(coupleObject).toString());
        AppLog.d(TAG, "List =" + new Gson().toJson(mList).toString());

        List<ContactDao> list = new ArrayList<>();
        list.add(coupleObject);
        ((CoupleKittyCreateActivity) mContext).setDataCoupleListAdapter(list);
        notifyDataSetChanged();
    }

    public void addMemberList(List<ContactDao> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }
}