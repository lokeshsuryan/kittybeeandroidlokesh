package com.kittyapplication.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhaval Riontech on 15/8/16.
 */
public class DiarySummaryListAdapter extends BaseAdapter {
    private static final String TAG = DiarySummaryListAdapter.class.getSimpleName();
    private final Context mContext;
    private final List<SummaryListDao> mList;
    private static LayoutInflater inflater = null;
    private boolean mIsManuallyHostList = false;
    private final List<SummaryListDao> mCheckedList;

    public DiarySummaryListAdapter(Context context, List<SummaryListDao> list, boolean isManuallyHostList) {
        mContext = context;
        mList = list;
        inflater = (LayoutInflater) this.mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mIsManuallyHostList = isManuallyHostList;
        mCheckedList = new ArrayList<>();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_layout_dairy_summary, parent, false);
            viewHolder = new Holder();
            viewHolder.imgUserPic = (ImageView) convertView.findViewById(R.id.imgHostPic);
            viewHolder.txtUserName = (TextView) convertView.findViewById(R.id.txtHostName);
            viewHolder.txtUserNumber = (TextView) convertView.findViewById(R.id.txtHostNumber);
            viewHolder.imgUserPic2 = (ImageView) convertView.findViewById(R.id.imgHostPic2);
            viewHolder.txtUserName2 = (TextView) convertView.findViewById(R.id.txtHostName2);
            viewHolder.txtUserNumber2 = (TextView) convertView.findViewById(R.id.txtHostNumber2);
            viewHolder.txtNotHosted = (TextView) convertView.findViewById(R.id.txtNotHosted);
            viewHolder.txtCurrentHost = (TextView) convertView.findViewById(R.id.txtCurrentHost);
            viewHolder.txtHosted = (TextView) convertView.findViewById(R.id.txtHosted);
            viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            viewHolder.progressBar2 = (ProgressBar) convertView.findViewById(R.id.progressBar2);
            viewHolder.rlUser2 = (RelativeLayout) convertView.findViewById(R.id.rlImage2);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }

        List<SummaryListDao> list = Utils.seprateCoupleObjectToMember(mList.get(position));

        if (list.size() == 2) {
            viewHolder.txtUserName.setText(checkIfMe(list.get(0).getNumber(), list.get(0).getName()));
            viewHolder.txtUserNumber.setText(list.get(0).getNumber());
            setImage(viewHolder.imgUserPic, viewHolder.progressBar, list.get(0).getProfilePic());

            viewHolder.rlUser2.setVisibility(View.VISIBLE);
            viewHolder.txtUserName2.setText(checkIfMe(list.get(1).getNumber(), list.get(1).getName()));
            viewHolder.txtUserNumber2.setText(list.get(1).getNumber());
            setImage(viewHolder.imgUserPic2, viewHolder.progressBar2, list.get(1).getProfilePic());
        } else {
            viewHolder.rlUser2.setVisibility(View.GONE);
            viewHolder.txtUserName.setText(checkIfMe(mList.get(position).getNumber().replace(AppConstant.SEPERATOR_STRING, ""), mList.get(position).getName().replace(AppConstant.SEPERATOR_STRING, "")));
            viewHolder.txtUserNumber.setText(mList.get(position).getNumber().replace(AppConstant.SEPERATOR_STRING, ""));
            setImage(viewHolder.imgUserPic, viewHolder.progressBar, mList.get(position).getProfilePic().replace(AppConstant.SEPERATOR_STRING, ""));
        }

        if (mIsManuallyHostList) {
            viewHolder.txtCurrentHost.setVisibility(View.GONE);
            viewHolder.txtNotHosted.setVisibility(View.GONE);
            viewHolder.txtHosted.setVisibility(View.GONE);
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setChecked(checkIsSelected(mList.get(position)));
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
//                        mCheckedList.add(mList.get(position));
                        addToSeletedList(mList.get(position), true);
                    } else {
//                        mCheckedList.remove(mCheckedList.indexOf(mList.get(position)));
                        addToSeletedList(mList.get(position), false);
                    }
                }
            });
            convertView.setTag(R.id.imgHostPic, viewHolder.checkBox.isChecked());
            convertView.setTag(R.id.checkBox, position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = (boolean) v.getTag(R.id.imgHostPic);
                    int pos = (int) v.getTag(R.id.checkBox);
                    if (isChecked) {
//                        mCheckedList.remove(mCheckedList.indexOf(mList.get(position)));
                        addToSeletedList(mList.get(pos), false);
                    } else {
                        addToSeletedList(mList.get(pos), true);
//                        mCheckedList.add(mList.get(position));
                    }
                    notifyDataSetChanged();
                }
            });
        } else {
            viewHolder.checkBox.setVisibility(View.GONE);

            // By default Not hosted will be visible
            viewHolder.txtNotHosted.setVisibility(View.VISIBLE);
            viewHolder.txtHosted.setVisibility(View.GONE);
            viewHolder.txtCurrentHost.setVisibility(View.GONE);

            if (list.size() == 2) {
                if (list.get(0).getHost().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED) ||
                        list.get(1).getHost().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    viewHolder.txtNotHosted.setVisibility(View.GONE);
                    viewHolder.txtHosted.setVisibility(View.VISIBLE);
                    viewHolder.txtHosted.setText(mContext.getResources().
                            getString(R.string.hosted_successfully));
                }

                if (list.get(0).getCurrentHost().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED) ||
                        list.get(1).getCurrentHost().equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    viewHolder.txtNotHosted.setVisibility(View.GONE);
                    viewHolder.txtHosted.setVisibility(View.GONE);
                    viewHolder.txtCurrentHost.setVisibility(View.VISIBLE);
                }
            } else {
                if (mList.get(position).getHost().replace(AppConstant.SEPERATOR_STRING, "")
                        .equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    viewHolder.txtCurrentHost.setVisibility(View.GONE);
                    viewHolder.txtNotHosted.setVisibility(View.GONE);
                    viewHolder.txtHosted.setVisibility(View.VISIBLE);
                    viewHolder.txtHosted.setText(mContext.getResources().getString(R.string.hosted_successfully));
                }
                if (mList.get(position).getCurrentHost().replace(AppConstant.SEPERATOR_STRING, "").equalsIgnoreCase(AppConstant.ATTENDANCE_CHECKED)) {
                    viewHolder.txtNotHosted.setVisibility(View.GONE);
                    viewHolder.txtHosted.setVisibility(View.GONE);
                    viewHolder.txtCurrentHost.setVisibility(View.VISIBLE);
                }
            }
        }
        return convertView;
    }

    private String checkIfMe(String number, String name) {
        /*if (number.equalsIgnoreCase(PreferanceUtils.getLoginUserObject(mContext).getPhone())) {
            return "You";
        } else {
            return name;
        }*/
        return Utils.checkIfMe(mContext, number, name);
    }

    private void setImage(ImageView imageView, final ProgressBar progressBar, String strImage) {
        ImageUtils.getImageLoader(mContext).displayImage(strImage, imageView,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        super.onLoadingStarted(imageUri, view);
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        super.onLoadingComplete(imageUri, view, loadedImage);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        super.onLoadingFailed(imageUri, view, failReason);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        super.onLoadingCancelled(imageUri, view);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public List<SummaryListDao> getCheckedList() {
        return mCheckedList;
    }

    public class Holder {
        private ImageView imgUserPic;
        private TextView txtUserName;
        private TextView txtUserNumber;
        private ProgressBar progressBar;
        private ImageView imgUserPic2;
        private TextView txtUserName2;
        private TextView txtUserNumber2;
        private ProgressBar progressBar2;
        private RelativeLayout rlUser2;
        private TextView txtCurrentHost;
        private TextView txtNotHosted;
        private TextView txtHosted;
        public CheckBox checkBox;
    }


    public boolean checkIsSelected(SummaryListDao obj) {
        if (mCheckedList.size() > 0) {
            for (int i = 0; i < mCheckedList.size(); i++) {
                // check checked list is current object is couple
                if (mCheckedList.get(i).getNumber().contains(AppConstant.SEPERATOR_STRING)) {
                    // seprate object
                    List<SummaryListDao> seprateObject =
                            Utils.seprateCoupleObjectToMember(mCheckedList.get(i));
                    // check eac
                    for (int j = 0; j < seprateObject.size(); j++) {
                        if (obj.getNumber().contains(AppConstant.SEPERATOR_STRING)) {
                            List<SummaryListDao> currentObj =
                                    Utils.seprateCoupleObjectToMember(obj);
                            for (int k = 0; k < currentObj.size(); k++) {
                                if (currentObj.get(k).getNumber()
                                        .equalsIgnoreCase(seprateObject.get(j).getNumber())) {
                                    return true;
                                }
                            }
                        } else {
                            if (obj.getNumber()
                                    .equalsIgnoreCase(seprateObject.get(j)
                                            .getNumber())) {
                                return true;
                            }
                        }
                    }
                } else {
                    if (obj.getNumber().contains(AppConstant.SEPERATOR_STRING)) {
                        List<SummaryListDao> currentObj =
                                Utils.seprateCoupleObjectToMember(obj);
                        for (int k = 0; k < currentObj.size(); k++) {
                            if (currentObj.get(k).getNumber()
                                    .equalsIgnoreCase(mCheckedList.get(i).getNumber())) {
                                return true;
                            }
                        }
                    } else {
                        if (obj.getNumber()
                                .equalsIgnoreCase(mCheckedList.get(i)
                                        .getNumber())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void addToSeletedList(SummaryListDao object, boolean isAdd) {
        if (isAdd) {
            if (mCheckedList.isEmpty()) {
                mCheckedList.add(object);
            } else {
                if (!checkIsSelected(object)) {
                    //if (!mSelectedList.contains(object)) {
                    mCheckedList.add(object);
                    // }
                }
            }
        } else {
            if (mCheckedList.size() > 0)
                mCheckedList.remove(object);
        }
        notifyDataSetChanged();
    }
}
