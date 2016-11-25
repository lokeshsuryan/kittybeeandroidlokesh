package com.kittyapplication.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.listener.AlertMessageListener;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.ui.activity.CoupleKittyCreateActivity;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pintu Riontech on 15/8/16.
 * vaghela.pintu31@gmail.com
 */
public class DisplayCoupleAdapter extends BaseAdapter implements AlertMessageListener {
    private static final String TAG = DisplayCoupleAdapter.class.getSimpleName();

    private List<ContactDao> mList;
    private List<ContactDao> mListClone;
    private Context mContext;
    private static LayoutInflater inflater = null;
    private int mSelectedObjectPos;

    public DisplayCoupleAdapter(Context context, List<ContactDao> lists) {
        mList = lists;
        mListClone = lists;
        mContext = context;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        private CustomTextViewBold txtCoupleToName, txtCoupleWithName;
        private ImageView imgDeleteCouple;
        private RoundedImageView imgCoupleTo, imgCoupleWith;
        private CustomTextViewNormal txtCoupleToNumber, txtCoupleWithNumber;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout_couple, parent, false);
            viewHolder = new Holder();
            viewHolder.imgCoupleTo = (RoundedImageView) convertView.findViewById(R.id.imgCoupleTo);
            viewHolder.imgCoupleWith = (RoundedImageView) convertView.findViewById(R.id.imgCoupleWith);
            viewHolder.txtCoupleToName = (CustomTextViewBold) convertView.findViewById(R.id.txtCoupleToName);
            viewHolder.txtCoupleWithName = (CustomTextViewBold) convertView.findViewById(R.id.txtCoupleWithName);
            viewHolder.imgDeleteCouple = (ImageView) convertView.findViewById(R.id.imgDeleteCouple);
            viewHolder.txtCoupleToNumber = (CustomTextViewNormal) convertView.findViewById(R.id.txtCoupleToNumber);
            viewHolder.txtCoupleWithNumber = (CustomTextViewNormal) convertView.findViewById(R.id.txtCoupleWithNumber);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

        String[] couple = mList.get(position).getName().split("-!-");
        String[] number = mList.get(position).getPhone().split("-!-");
        String[] images = mList.get(position).getImage().split("-!-");

//        viewHolder.txtCoupleToName.setText(Utils.checkIfMe(mContext,
//                number.length > 0 ? number[0] : "",
//                couple.length > 0 ? couple[0] : ""));
//
//        viewHolder.txtCoupleWithName.setText(Utils.checkIfMe(mContext,
//                number.length > 0 ? number[1] : "",
//                couple.length > 0 ? couple[1] : ""));

        viewHolder.txtCoupleToName.setText(Utils.checkNumberIsLoginUser(mContext
                , number.length > 0 ? number[0] : "", couple.length > 0 ? couple[0] : ""));

        viewHolder.txtCoupleWithName.setText(Utils.checkNumberIsLoginUser(mContext
                , number.length > 0 ? number[1] : "", couple.length > 0 ? couple[1] : ""));

        viewHolder.txtCoupleWithNumber.setText(number.length > 0 ? number[1] : "");
        viewHolder.txtCoupleToNumber.setText(number.length > 0 ? number[0] : "");

        ImageUtils.getImageLoader(mContext).displayImage(images.length > 0 ? images[0] : "",
                viewHolder.imgCoupleTo);

        ImageUtils.getImageLoader(mContext).displayImage(images.length > 0 ? images[1] : "",
                viewHolder.imgCoupleWith);

        viewHolder.imgDeleteCouple.setTag(position);
        viewHolder.imgDeleteCouple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag();
                mSelectedObjectPos = pos;
                showDialog(mList.get(pos));
            }
        });
        return convertView;
    }


    public List<ContactDao> getMemberList() {
        return mList;
    }

    private void showDialog(ContactDao user) {
        AlertDialogUtils.showAlertMessageDeleteCouple(mContext, this, user);
    }


    private void removeCouple(ContactDao user) {
        ContactDao obj = user;
        List<ContactDao> list = new ArrayList<>();
        String[] couple = obj.getName().split("-!-");
        String[] images = obj.getImage().split("-!-");
        String[] phone = obj.getPhone().split("-!-");
        String[] fullname = obj.getFullName().split("-!-");
        String[] registration = obj.getRegistration().split("-!-");
        String[] status = obj.getStatus().split("-!-");
        String[] id = obj.getID().split("-!-");

        for (int i = 0; i < 2; i++) {
            ContactDao object = new ContactDao();
            object.setImage(getStringFromArray(i, images));
            object.setName(getStringFromArray(i, couple));
            object.setPhone(getStringFromArray(i, phone));
            object.setStatus(getStringFromArray(i, status));
            object.setRegistration(getStringFromArray(i, registration));
            object.setFullName(getStringFromArray(i, fullname));
            object.setID(getStringFromArray(i, id));
            list.add(object);
        }
        mList.remove(obj);
        ((CoupleKittyCreateActivity) mContext).setCreateCoupleDataList(list, true);
    }

    @Override
    public void onClickYes(ContactDao obj) {
        removeCouple(obj);
        notifyDataSetChanged();
    }

    @Override
    public void onClickNo() {

    }

    public void addDataToList(List<ContactDao> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void reloadData() {
        new ReloadDataTask().execute();
    }

    /**
     *
     */
    private class ReloadDataTask extends AsyncTask<Void, Void, Void> {
        List<ContactDao> list = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... params) {
            if (mList != null && !mList.isEmpty()) {
                for (int i = 0; i < mList.size(); i++) {
                    String[] couple = mList.get(i).getName().split("-!-");
                    String[] images = mList.get(i).getImage().split("-!-");
                    String[] phone = mList.get(i).getPhone().split("-!-");
                    String[] fullname = mList.get(i).getFullName().split("-!-");
                    String[] registration = mList.get(i).getRegistration().split("-!-");
                    String[] status = mList.get(i).getStatus().split("-!-");
                    String[] id = mList.get(i).getID().split("-!-");
                    for (int j = 0; j < 2; j++) {
                        ContactDao object = new ContactDao();
                        object.setImage((getStringFromArray(j, images)));
                        object.setName((getStringFromArray(j, couple)));
                        object.setPhone((getStringFromArray(j, phone)));
                        object.setRegistration((getStringFromArray(j, fullname)));
                        object.setStatus((getStringFromArray(j, registration)));
                        object.setFullName((getStringFromArray(j, status)));
                        object.setID(getStringFromArray(j, id));
                        list.add(object);
                    }
                }
            } else {
                AppLog.d(TAG, "no data in list");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mList.clear();
            AppLog.d(TAG, new Gson().toJson(list).toString());
            ((CoupleKittyCreateActivity) mContext).setCreateCoupleDataList(list, true);
        }
    }

    private String getStringFromArray(int pos, String[] array) {
        String str = "";
        try {
            if (array[pos] != null && array[pos].length() > 0) {
                str = array[pos];
            } else {
                str = "";
            }
        } catch (Exception e) {
            str = "";
        }
        return str;
    }

}