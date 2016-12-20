package com.kittyapplication.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kittyapplication.R;
import com.kittyapplication.custom.CustomTextViewBold;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.RoundedImageView;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.ContactDaoWithHeader;
import com.kittyapplication.ui.activity.SelectCoupleActivity;
import com.kittyapplication.ui.viewinterface.RemoveCoupleAlertMessageListener;
import com.kittyapplication.ui.viewinterface.SelectCoupleWithListener;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dhaval Soneji Riontech on 1/9/16.
 */
public class SelectCoupleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements RemoveCoupleAlertMessageListener, SelectCoupleWithListener {
    private static final String TAG = SelectCoupleListAdapter.class.getSimpleName();
    private ArrayList<ContactDaoWithHeader> mList;
    private Context mContext;
    private boolean isDialogOpen = false;
    private static final Object LOCK = new Object();


    public SelectCoupleListAdapter(ArrayList<ContactDaoWithHeader> list, Context context) {
        mList = list;
        mContext = context;
        ((SelectCoupleActivity) mContext).setCloneList(mList);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        switch (viewType) {
            case AppConstant.ADD_GROUP_TYPE_HEADER:
                View viewHeader = inflater.inflate(R.layout.layout_header_unselect, viewGroup, false);
                viewHolder = new HeaderHolder(viewHeader);
                break;
            case AppConstant.ADD_GROUP_TYPE_HEADER_2:
                View viewHeader2 = inflater.inflate(R.layout.layout_header_unselect, viewGroup, false);
                viewHolder = new HeaderHolder(viewHeader2);
                break;
            case AppConstant.ADD_GROUP_TYPE_SINGLE:
                View viewSingle = inflater.inflate(R.layout.row_layout_single, viewGroup, false);
                viewHolder = new SingleHolder(viewSingle);
                break;
            case AppConstant.ADD_GROUP_TYPE_COUPLE:
                View viewCouple = inflater.inflate(R.layout.row_layout_couple, viewGroup, false);
                viewHolder = new CoupleHolder(viewCouple);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case AppConstant.ADD_GROUP_TYPE_HEADER:
                HeaderHolder headerHolder = (HeaderHolder) viewHolder;
                configureHeader(headerHolder, position);
                break;
            case AppConstant.ADD_GROUP_TYPE_HEADER_2:
                HeaderHolder headerHolder2 = (HeaderHolder) viewHolder;
                configureHeader(headerHolder2, position);
                break;
            case AppConstant.ADD_GROUP_TYPE_SINGLE:
                SingleHolder singleHolder = (SingleHolder) viewHolder;
                configureSingle(singleHolder, position);
                break;
            case AppConstant.ADD_GROUP_TYPE_COUPLE:
                CoupleHolder coupleHolder = (CoupleHolder) viewHolder;
                configureCouple(coupleHolder, position);
                break;
        }
    }

    private void configureHeader(HeaderHolder headerHolder, int position) {
        if (mList.get(position).getHeader() == AppConstant.ADD_GROUP_TYPE_HEADER) {
            headerHolder.getTxtHeaderTwo().setText(mContext.getResources().getString(R.string.select_couple));
        } else if (mList.get(position).getHeader() == AppConstant.ADD_GROUP_TYPE_HEADER_2) {
            headerHolder.getTxtHeaderTwo().setText(mContext.getResources().getString(R.string.selected_couple));
        }
    }

    private void configureSingle(SingleHolder singleHolder, int position) {
        try {
            singleHolder.getTxtContactName().setText(Utils.checkNumberIsLoginUser(mContext
                    , mList.get(position).getData().getPhone(), mList.get(position).getData().getName()));


            singleHolder.getTxtContactTitle().setText(mList.get(position).getData().getPhone());

            ImageUtils.getImageLoader(mContext).displayImage(mList.get(position).getData().getImage(),
                    singleHolder.imgContactUser);

            singleHolder.getRlMain().setTag(R.layout.row_layout_contact, position);
            singleHolder.getRlMain().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedPosition = (int) v.getTag(R.layout.row_layout_contact);
                    int countSingleMember = 0;
                    for (int i = 0; i < mList.size(); i++) {
                        if (mList.get(i).getHeader() == AppConstant.ADD_GROUP_TYPE_SINGLE) {
                            countSingleMember++;
                        }
                    }

                    if (countSingleMember > 1) {
                        if (!isDialogOpen) {
                            isDialogOpen = true;
                            showSelectCoupleDialog(selectedPosition);
                        }
                    } else {
                        ((SelectCoupleActivity) mContext)
                                .showSnackbar(mContext.getResources().getString(R.string.create_couple_warning));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureCouple(CoupleHolder coupleHolder, int position) {
        try {
            String[] couple = mList.get(position).getData().getName().split("-!-");
            String[] number = mList.get(position).getData().getPhone().split("-!-");
            String[] images = mList.get(position).getData().getImage().split("-!-");

            coupleHolder.getTxtCoupleToName().setText(Utils.checkNumberIsLoginUser(mContext
                    , number.length > 0 ? number[0] : "", couple.length > 0 ? couple[0] : ""));

            coupleHolder.getTxtCoupleWithName().setText(Utils.checkNumberIsLoginUser(mContext
                    , number.length > 0 ? number[1] : "", couple.length > 0 ? couple[1] : ""));

            coupleHolder.getTxtCoupleWithNumber().setText(number.length > 0 ? number[1] : "");
            coupleHolder.getTxtCoupleToNumber().setText(number.length > 0 ? number[0] : "");

            ImageUtils.getImageLoader(mContext).displayImage(images.length > 0 ? images[0] : "",
                    coupleHolder.getImgCoupleTo());

            ImageUtils.getImageLoader(mContext).displayImage(images.length > 0 ? images[1] : "",
                    coupleHolder.getImgCoupleWith());

            coupleHolder.getImgDeleteCouple().setTag(position);
            coupleHolder.getImgDeleteCouple().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (int) v.getTag();
                    showDialogFromCouple(mList.get(pos));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param selectedPosition
     */
    private void showSelectCoupleDialog(final int selectedPosition) {
        new AsyncTask<Void, Void, ArrayList<ContactDaoWithHeader>>() {
            ArrayList<ContactDaoWithHeader> mSinglesList = new ArrayList<>();

            @Override
            protected ArrayList<ContactDaoWithHeader> doInBackground(Void... params) {

                for (int i = 0; i < mList.size(); i++) {
                    if (mList.get(i).getHeader() == AppConstant.ADD_GROUP_TYPE_SINGLE) {
                        mSinglesList.add(mList.get(i));
                    }
                }
                return mSinglesList;
            }

            @Override
            protected void onPostExecute(ArrayList<ContactDaoWithHeader> contactDaoWithHeaders) {
                super.onPostExecute(contactDaoWithHeaders);
                final int FIRST_HEADER = 1;
                AlertDialogUtils.showDialogSelectCouple1
                        (mContext, mSinglesList, SelectCoupleListAdapter.this, selectedPosition - FIRST_HEADER);
            }
        }.execute();
    }

    private void showDialogFromCouple(ContactDaoWithHeader user) {
        try {
            AlertDialogUtils.showAlertMessageDeleteCouple1(mContext, this, user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getSelectedCoupleWithMember(ContactDao memberFromDialog, ContactDao memberFromList) {
        try {
            createCouple(memberFromDialog, memberFromList);
            isDialogOpen = false;
        } catch (Exception e) {
            isDialogOpen = false;
        }
    }

    @Override
    public void dismissDialog() {
        isDialogOpen = false;
    }

    public void createCouple(ContactDao memberFromDialog, ContactDao memberFromList) {
        try {
            ContactDao coupleObject = new ContactDao();
            ContactDao coupleTo = memberFromList;

            coupleObject.setName(mContext.getResources().getString(R.string.add_couple, coupleTo.getName(), memberFromDialog.getName()));
            coupleObject.setPhone(mContext.getResources().getString(R.string.add_couple, coupleTo.getPhone(), memberFromDialog.getPhone()));
            coupleObject.setIsHost(mContext.getResources().getString(R.string.add_couple, coupleTo.getIsHost(), memberFromDialog.getIsHost()));
            coupleObject.setID(mContext.getResources().getString(R.string.add_couple, coupleTo.getID(), memberFromDialog.getID()));
            coupleObject.setImage(mContext.getResources().getString(R.string.add_couple, coupleTo.getImage(), memberFromDialog.getImage()));
            coupleObject.setFullName(mContext.getResources().getString(R.string.add_couple, coupleTo.getFullName(), memberFromDialog.getFullName()));
            coupleObject.setUserId(mContext.getResources().getString(R.string.add_couple, coupleTo.getUserId(), memberFromDialog.getUserId()));
            coupleObject.setLogin(mContext.getResources().getString(R.string.add_couple, coupleTo.getLogin(), memberFromDialog.getLogin()));
            coupleObject.setRegistration(mContext.getResources().getString(R.string.add_couple, coupleTo.getRegistration(), memberFromDialog.getRegistration()));
            coupleObject.setIs_Paid(mContext.getResources().getString(R.string.add_couple, coupleTo.getIs_Paid(),
                    memberFromDialog.getIs_Paid()));
            coupleObject.setID(mContext.getResources().getString(R.string.add_couple, coupleTo.getID(),
                    memberFromDialog.getID()));
            coupleObject.setMamberID(mContext.getResources().getString(R.string.add_couple, coupleTo.getMamberID(),
                    memberFromDialog.getMamberID()));
            coupleObject.setMemberId(mContext.getResources().getString(R.string.add_couple, coupleTo.getMemberId(),
                    memberFromDialog.getMemberId()));

            ArrayList<ContactDaoWithHeader> tempList = new ArrayList<>();
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).getHeader() == AppConstant.ADD_GROUP_TYPE_SINGLE) {
                    if (mList.get(i).getData().getPhone().equalsIgnoreCase(memberFromList.getPhone()) ||
                            mList.get(i).getData().getPhone().equalsIgnoreCase(memberFromDialog.getPhone())) {
                    } else {
                        tempList.add(mList.get(i));
                    }
                } else {
                    tempList.add(mList.get(i));
                }
            }

            mList = tempList;
            if (mList.size() > 1) {
                if (mList.get(0).getHeader() == AppConstant.ADD_GROUP_TYPE_HEADER &&
                        mList.get(1).getHeader() == AppConstant.ADD_GROUP_TYPE_HEADER_2) {
                    mList.remove(0);
                }
            }

            ContactDaoWithHeader coupleDao = new ContactDaoWithHeader();
            coupleDao.setHeader(AppConstant.ADD_GROUP_TYPE_COUPLE);
            coupleDao.setData(coupleObject);

            addCoupleMemberToList(coupleDao);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void removeCoupleMemberFromList(ContactDaoWithHeader user) {
        try {
            // check if single list empty
            // if so: add header item for single list (using temporary List)
            if (mList.get(0).getHeader() == AppConstant.ADD_GROUP_TYPE_HEADER_2
                    && mList.get(1).getHeader() == AppConstant.ADD_GROUP_TYPE_COUPLE) {

                ContactDaoWithHeader headerDao = new ContactDaoWithHeader();
                headerDao.setHeader(AppConstant.ADD_GROUP_TYPE_HEADER);
                ArrayList<ContactDaoWithHeader> tempList = new ArrayList<>();
                tempList.add(headerDao);

                for (int i = 0; i < mList.size(); i++) {
                    tempList.add(mList.get(i));
                }
                mList = tempList;
            }

            //seperate couple into two single member
            ContactDaoWithHeader obj = user;
            List<ContactDao> list = new ArrayList<>();
            String[] couple = obj.getData().getName().split("-!-");
            String[] images = obj.getData().getImage().split("-!-");
            String[] phone = obj.getData().getPhone().split("-!-");
            String[] fullName = obj.getData().getFullName().split("-!-");
            String[] registration = obj.getData().getRegistration().split("-!-");
            String[] status = obj.getData().getStatus().split("-!-");
            String[] id = obj.getData().getID().split("-!-");
            String[] memberId = obj.getData().getMemberId().split("-!-");
            String[] mambarID = obj.getData().getMamberID().split("-!-");

            for (int i = 0; i < 2; i++) {
                ContactDao object = new ContactDao();
                object.setImage(getStringFromArray(i, images));
                object.setName(getStringFromArray(i, couple));
                object.setPhone(getStringFromArray(i, phone));
                object.setStatus(getStringFromArray(i, status));
                object.setRegistration(getStringFromArray(i, registration));
                object.setFullName(getStringFromArray(i, fullName));
                object.setID(getStringFromArray(i, id));
                object.setMemberId(getStringFromArray(i, memberId));
                object.setMamberID(getStringFromArray(i, mambarID));
                list.add(object);
            }
            mList.remove(obj);


            // retrieve position of second header section
            int position = 0;
            for (int i = 1; i < mList.size(); i++) {
                if (mList.get(i).getHeader() == AppConstant.ADD_GROUP_TYPE_HEADER_2) {
                    position = i;
                    break;
                }
            }

            // check if couple list is empty
            // if so: hide second header section
            if (mList.get(mList.size() - 1).getHeader() == AppConstant.ADD_GROUP_TYPE_HEADER_2) {
                mList.remove(mList.size() - 1);
                position = position - 1;
            }
            ((SelectCoupleActivity) mContext).setCreateCoupleDataList(list, true, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCoupleMemberToList(ContactDaoWithHeader coupleItem) {
        try {
            if (mList.get(mList.size() - 1).getHeader() == AppConstant.ADD_GROUP_TYPE_SINGLE) {
                mList.add(addHeaderItem());
                mList.add(coupleItem);
            } else {
                mList.add(coupleItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    private ContactDaoWithHeader addHeaderItem() {
        ContactDaoWithHeader contactDaoWithHeader = new ContactDaoWithHeader();
        contactDaoWithHeader.setHeader(AppConstant.ADD_GROUP_TYPE_HEADER_2);
        return contactDaoWithHeader;
    }

    public void appendSingleMemberToList(ArrayList<ContactDaoWithHeader> list, int position) {
        ArrayList<ContactDaoWithHeader> tempList = new ArrayList<>();
        try {
            for (int i = 0; i < mList.size(); i++) {
                if (i == (position)) {
                    for (int j = 0; j < list.size(); j++) {
                        tempList.add(list.get(j));
                    }
                }
                tempList.add(mList.get(i));
            }
            mList = tempList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    public void reloadData(ArrayList<ContactDaoWithHeader> cloneList) {
        try {
            mList = cloneList;
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ContactDao> getMemberListWithOutHeader() {
        ArrayList<ContactDao> list = new ArrayList<>();
        try {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).getHeader() == AppConstant.ADD_GROUP_TYPE_SINGLE ||
                        mList.get(i).getHeader() == AppConstant.ADD_GROUP_TYPE_COUPLE) {
                    list.add(mList.get(i).getData());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private class SingleHolder extends RecyclerView.ViewHolder {
        private CustomTextViewNormal txtContactTitle;
        private CustomTextViewBold txtContactName;
        private ImageView imgContactPhone;
        private RoundedImageView imgContactUser;
        private RelativeLayout rlMain;
        private RelativeLayout rlButtons;

        public CustomTextViewNormal getTxtContactTitle() {
            return txtContactTitle;
        }

        public void setTxtContactTitle(CustomTextViewNormal txtContactTitle) {
            this.txtContactTitle = txtContactTitle;
        }

        public CustomTextViewBold getTxtContactName() {
            return txtContactName;
        }

        public void setTxtContactName(CustomTextViewBold txtContactName) {
            this.txtContactName = txtContactName;
        }

        public ImageView getImgContactPhone() {
            return imgContactPhone;
        }

        public void setImgContactPhone(ImageView imgContactPhone) {
            this.imgContactPhone = imgContactPhone;
        }

        public RoundedImageView getImgContactUser() {
            return imgContactUser;
        }

        public void setImgContactUser(RoundedImageView imgContactUser) {
            this.imgContactUser = imgContactUser;
        }

        public RelativeLayout getRlMain() {
            return rlMain;
        }

        public void setRlMain(RelativeLayout rlMain) {
            this.rlMain = rlMain;
        }

        public RelativeLayout getRlButtons() {
            return rlButtons;
        }

        public void setRlButtons(RelativeLayout rlButtons) {
            this.rlButtons = rlButtons;
        }

        public SingleHolder(View view) {
            super(view);
            imgContactUser = (RoundedImageView) view.findViewById(R.id.imgContactUser);
            txtContactName = (CustomTextViewBold) view.findViewById(R.id.txtContactName);
            txtContactTitle = (CustomTextViewNormal) view.findViewById(R.id.txtContactTitle);
            rlMain = (RelativeLayout) view.findViewById(R.id.rlContact);
            rlButtons = (RelativeLayout) view.findViewById(R.id.rlContactButtons);
            imgContactPhone = (ImageView) rlButtons.findViewById(R.id.imgContactPhone);
        }
    }

    private class CoupleHolder extends RecyclerView.ViewHolder {
        private CustomTextViewBold txtCoupleToName, txtCoupleWithName;
        private ImageView imgDeleteCouple;
        private RoundedImageView imgCoupleTo, imgCoupleWith;
        private CustomTextViewNormal txtCoupleToNumber, txtCoupleWithNumber;

        public CustomTextViewBold getTxtCoupleToName() {
            return txtCoupleToName;
        }

        public void setTxtCoupleToName(CustomTextViewBold txtCoupleToName) {
            this.txtCoupleToName = txtCoupleToName;
        }

        public CustomTextViewBold getTxtCoupleWithName() {
            return txtCoupleWithName;
        }

        public void setTxtCoupleWithName(CustomTextViewBold txtCoupleWithName) {
            this.txtCoupleWithName = txtCoupleWithName;
        }

        public ImageView getImgDeleteCouple() {
            return imgDeleteCouple;
        }

        public void setImgDeleteCouple(ImageView imgDeleteCouple) {
            this.imgDeleteCouple = imgDeleteCouple;
        }

        public RoundedImageView getImgCoupleTo() {
            return imgCoupleTo;
        }

        public void setImgCoupleTo(RoundedImageView imgCoupleTo) {
            this.imgCoupleTo = imgCoupleTo;
        }

        public RoundedImageView getImgCoupleWith() {
            return imgCoupleWith;
        }

        public void setImgCoupleWith(RoundedImageView imgCoupleWith) {
            this.imgCoupleWith = imgCoupleWith;
        }

        public CustomTextViewNormal getTxtCoupleToNumber() {
            return txtCoupleToNumber;
        }

        public void setTxtCoupleToNumber(CustomTextViewNormal txtCoupleToNumber) {
            this.txtCoupleToNumber = txtCoupleToNumber;
        }

        public CustomTextViewNormal getTxtCoupleWithNumber() {
            return txtCoupleWithNumber;
        }

        public void setTxtCoupleWithNumber(CustomTextViewNormal txtCoupleWithNumber) {
            this.txtCoupleWithNumber = txtCoupleWithNumber;
        }

        public CoupleHolder(View view) {
            super(view);
            imgCoupleTo = (RoundedImageView) view.findViewById(R.id.imgCoupleTo);
            imgCoupleWith = (RoundedImageView) view.findViewById(R.id.imgCoupleWith);
            txtCoupleToName = (CustomTextViewBold) view.findViewById(R.id.txtCoupleToName);
            txtCoupleWithName = (CustomTextViewBold) view.findViewById(R.id.txtCoupleWithName);
            imgDeleteCouple = (ImageView) view.findViewById(R.id.imgDeleteCouple);
            txtCoupleToNumber = (CustomTextViewNormal) view.findViewById(R.id.txtCoupleToNumber);
            txtCoupleWithNumber = (CustomTextViewNormal) view.findViewById(R.id.txtCoupleWithNumber);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mList.get(position).getHeader()) {
            case AppConstant.ADD_GROUP_TYPE_HEADER:
                return AppConstant.ADD_GROUP_TYPE_HEADER;
            case AppConstant.ADD_GROUP_TYPE_HEADER_2:
                return AppConstant.ADD_GROUP_TYPE_HEADER_2;
            case AppConstant.ADD_GROUP_TYPE_SINGLE:
                return AppConstant.ADD_GROUP_TYPE_SINGLE;
            case AppConstant.ADD_GROUP_TYPE_COUPLE:
                return AppConstant.ADD_GROUP_TYPE_COUPLE;
        }
        return -1;
    }

    @Override
    public void onClickYes(ContactDaoWithHeader obj) {
        removeCoupleMemberFromList(obj);
//        notifyDataSetChanged();
    }

    @Override
    public void onClickNo() {

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

    public ContactDaoWithHeader getItem(int position) {
        return mList.get(position);
    }

    private class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView mTxtHeaderTwo;

        public HeaderHolder(View viewHeader) {
            super(viewHeader);
            mTxtHeaderTwo = (TextView) viewHeader.findViewById(R.id.txtHeaderTwo);
        }

        public TextView getTxtHeaderTwo() {
            return mTxtHeaderTwo;
        }

        public void setTxtHeaderTwo(TextView mTxtHeaderTwo) {
            this.mTxtHeaderTwo = mTxtHeaderTwo;
        }
    }

    // 1 - AppConstant.ADD_GROUP_TYPE_HEADER
    // 2 - AppConstant.ADD_GROUP_TYPE_HEADER_2
    // 3 - AppConstant.ADD_GROUP_TYPE_SINGLE
    // 4 - AppConstant.ADD_GROUP_TYPE_COUPLE
    public List<ContactDao> getSpecificList(int type) {
        List<ContactDao> list = new ArrayList<>();
        try {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).getHeader() == type) {
                    list.add(mList.get(i).getData());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ContactDao> getCouplePairedList() {
        List<ContactDao> list = new ArrayList<>();
        list = getSpecificList(AppConstant.ADD_GROUP_TYPE_COUPLE);
        return list;
    }
}
