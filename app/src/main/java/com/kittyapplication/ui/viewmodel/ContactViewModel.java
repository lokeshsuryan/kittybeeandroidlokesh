package com.kittyapplication.ui.viewmodel;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kittyapplication.R;
import com.kittyapplication.adapter.ContactAdapter;
import com.kittyapplication.custom.AlphaBaticSortContact;
import com.kittyapplication.custom.ContactSync;
import com.kittyapplication.custom.ImageLoaderListenerUniversal;
import com.kittyapplication.custom.swipe.GetSimContact;
import com.kittyapplication.model.AppContactList;
import com.kittyapplication.model.BannerDao;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.ContactNumber;
import com.kittyapplication.model.DeviceContacts;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.activity.HomeActivity;
import com.kittyapplication.ui.fragment.ContactsFragment;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.DeviceContactUtils;
import com.kittyapplication.utils.ImageUtils;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Pintu Riontech on 8/8/16.
 */
public class ContactViewModel implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = ContactViewModel.class.getSimpleName();
    private static final int REQUEST_PHONE_STATE_PERMISSION = 123;
    private ContactsFragment mFragment;
    private Context mContext;
    private ProgressDialog mDialog;
    private SwipeRefreshLayout mRefreshLayout;
    private ProgressBar mPbLoader;
    public boolean isInit = false;
    private List<DeviceContacts> mListDeviceContact = new ArrayList<>();
    private DeviceContactUtils mContactUtils;

    public ContactViewModel(ContactsFragment fragment) {
        mFragment = fragment;
        mContext = fragment.getActivity();
        isInit = true;
        mContactUtils = DeviceContactUtils.getInstance();
    }

    public void init() {
        if (checkCallPermission()) {
            AppContactList contactListObject = PreferanceUtils.getContactListFromPreferance(mContext);
            if (contactListObject == null
                    || contactListObject.getContactList() == null
                    || contactListObject.getContactList().isEmpty()) {
                callAPI(true);
            } else {
                setListview(contactListObject.getContactList());
//                mContext.startService(new Intent(mContext, ContactSyncIntentService.class));
            }
        }
    }

    /**
     *
     */
    public void callAPI(boolean flag) {
        if (Utils.checkInternetConnection(mContext)) {
            if (flag)
                showDialog();
//            new ContactSyncTask().execute();
            AppLog.e(TAG, "=================STARTED====================");
            ContactSyncTask task = new ContactSyncTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            ((HomeActivity) mContext).showSnackbar(mContext.getResources().getString(R.string.no_internet_available));
        }
    }

    public void setSearchFilter(EditText editText, final ContactAdapter adapter) {
        try {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    adapter.getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void showDialog() {
       /* mDialog = new ProgressDialog(mContext);
        mDialog.setMessage(mContext.getResources().getString(R.string.loading_text));
        mDialog.show();*/

//        mPbLoader = (ProgressBar) mFragment.getView().findViewById(R.id.pbLoaderChatFragment);
        mFragment.mPbLoader.setVisibility(View.VISIBLE);
    }

    private void hideDialog() {
       /* try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }*/

        mFragment.mPbLoader.setVisibility(View.GONE);
    }


    public void onSwipeRefreshReloadData(SwipeRefreshLayout refreshLayout) {
        mRefreshLayout = refreshLayout;
        mRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorAccent));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isInit = false;
                mFragment.showLayout();
                if (checkCallPermission())
                    callAPI(false);
            }
        });
    }

    private boolean checkCallPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(mContext,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPhoneStatePermission();
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            requestPhoneStatePermission();
        }
    }

    private void requestPhoneStatePermission() {
        mFragment.requestPermissions(
                new String[]{
                        Manifest.permission.READ_CONTACTS},
                REQUEST_PHONE_STATE_PERMISSION);
    }


    private Callback<ServerResponse<List<ContactDao>>> syncContactCallBack = new Callback<ServerResponse<List<ContactDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<ContactDao>>> call, Response<ServerResponse<List<ContactDao>>> response) {
            try {
                if (response.code() == 200) {
                    List<ContactDao> data = response.body().getData();
                    if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                        new DeviceContactSyncWithServerTask(data, mListDeviceContact).execute();
                    } else {
                        hideDialog();
                        hideRefreshLayout();
                        if (Utils.isValidString(response.body().getMessage())) {
                            ((HomeActivity) mContext).showSnackbar(response.body().getMessage());
                        } else {
                            ((HomeActivity) mContext).showSnackbar(mContext.getResources().getString(R.string.server_error));
                        }
                        mFragment.hideLayout();
                        AppLog.e(TAG, "Response code = 0");
                    }
                } else {
                    hideDialog();
                    hideRefreshLayout();
                    if (Utils.isValidString(response.body().getMessage())) {
                        ((HomeActivity) mContext).showSnackbar(response.body().getMessage());
                    } else {
                        ((HomeActivity) mContext).showSnackbar(mContext.getResources().getString(R.string.server_error));
                    }
                    mFragment.hideLayout();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<ContactDao>>> call, Throwable t) {
            hideDialog();
            hideRefreshLayout();
            AppLog.d(TAG, "fail");
            ((HomeActivity) mContext).showSnackbar(mContext.getResources().getString(R.string.server_error));
            mFragment.hideLayout();
        }
    };

    public void setBanner(ImageView img) {
        try {
            String itemName = mContext.getResources().getStringArray(R.array.adv_banner_name)[2];
            if (PreferanceUtils.getBannerFromPreferance(mContext) != null) {
                List<BannerDao> bannerDaoList = PreferanceUtils.getBannerFromPreferance(mContext).getBanner();
                if (bannerDaoList != null && !bannerDaoList.isEmpty()) {
                    for (int i = 0; i < bannerDaoList.size(); i++) {
                        if (itemName.equalsIgnoreCase(bannerDaoList.get(i).getTitle())) {
                            img.setVisibility(View.VISIBLE);
                            ImageUtils.getImageLoader(mContext).displayImage(bannerDaoList.get(i).getThamb(), img,
                                    new ImageLoaderListenerUniversal(mContext, img, "drawable://" + R.drawable.no_image_bottom_banner));
                            img.setTag(bannerDaoList.get(i).getUrl());
                            img.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String url = (String) v.getTag();
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    mContext.startActivity(browserIntent);
                                }
                            });
                            break;
                        } else {
                            img.setVisibility(View.GONE);
                        }
                    }
                    //img.setVisibility(View.GONE);
                } else {
                    img.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            img.setVisibility(View.GONE);
        }
//        Utils.setBannerItem(mFragment.getActivity(),
//                mContext.getResources().getStringArray(R.array.adv_banner_name)[2],
//                img);
    }

    /**
     *
     */
    private class ContactSyncTask extends AsyncTask<Void, Void, Void> {
        HashMap<String, String> hashMap = new HashMap<>();

        @Override
        protected Void doInBackground(Void... params) {
            ContactSync deviceContacts = new ContactSync(mFragment.getActivity());
            List<DeviceContacts> phoneContactList = deviceContacts.getDeviceContacts();
            mContactUtils.addList(phoneContactList);

            // Get SIM Contacts
            GetSimContact simContact = new GetSimContact(mFragment.getActivity());
            List<DeviceContacts> simContactList = simContact.getContactList();
            mContactUtils.addList(simContactList);
            mListDeviceContact = mContactUtils.getInstance().getContactList();

            /*mListDeviceContact = Utils.mergerSimContactWithList(phoneContactList,
                    simContactList);
            mListDeviceContact.addAll(phoneContactList);
            mListDeviceContact.addAll(simContactList);*/

            for (int i = 0; i < mListDeviceContact.size(); i++) {
                if (mListDeviceContact.get(i).getPhoneNumber() != null
                        && !mListDeviceContact.get(i).getPhoneNumber().isEmpty()) {
                    List<ContactNumber> contactList = mListDeviceContact.get(i).getPhoneNumber();
                    for (int j = 0; j < contactList.size(); j++) {
                        if (!contactList.get(j).getPhoneNumber()
                                .equalsIgnoreCase(PreferanceUtils.getLoginUserObject(mContext).getPhone())) {
                            String number = contactList.get(j).getPhoneNumber();
                            if (number.charAt(0) == '0' || number.charAt(0) == '+') {
                                number = number.substring(1, number.length());
                            }
                            hashMap.put(number, mListDeviceContact.get(i).getFullname());
                        }
                    }
                }
            }
            hashMap.put("number", PreferanceUtils.getLoginUserObject(mContext).getPhone());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Call<ServerResponse<List<ContactDao>>> call =
                    Singleton.getInstance().getRestOkClient().
                            syncContactToServer(PreferanceUtils.getLoginUserObject(mContext).getUserID(), hashMap);
            call.enqueue(syncContactCallBack);
        }
    }

    public void hideRefreshLayout() {
        if (mRefreshLayout != null && mRefreshLayout.isRefreshing()) {
            mRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * @param data
     */
    public void setListview(List<ContactDao> data) {
        if (data != null && !data.isEmpty()) {
            AppContactList contactListObject = new AppContactList();
            mFragment.showLayout();

            int index = 0;

            // TODO Clone array list instead of direct reference
            List<ContactDao> list = new ArrayList<>();
            list.addAll(data);

            Collections.sort(list, new AlphaBaticSortContact());
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getRegistration().equalsIgnoreCase("1")) {
                    try {
                        ContactDao obj = list.get(i);
                        if (Utils.isValidString(obj.getPhone())) {
                            /*obj.setName(Utils.getDisplayNameByPhoneNumber(mContext,
                                    obj.getPhone()));*/
                            list.remove(i);
                            list.add(index, obj);
                            index++;
                        }
                    } catch (Exception e) {
                        AppLog.e(TAG, e.getMessage(), e);
                    }
                }
            }

            mFragment.getDataList(list);
            contactListObject.setContactList(list);
            PreferanceUtils.setContactListIntoPreferance(mContext, contactListObject);
        } else {
            ((HomeActivity) mContext).showSnackbar
                    (mContext.getResources().getString(R.string.server_error));
            mFragment.hideLayout();
        }
    }


    /**
     *
     */
    public class DeviceContactSyncWithServerTask extends AsyncTask<Void, Void, List<ContactDao>> {
        List<ContactDao> mServerList;
        List<DeviceContacts> mDeviceContactList;


        public DeviceContactSyncWithServerTask(List<ContactDao> serverList,
                                               List<DeviceContacts> deviceContactList) {
            mServerList = serverList;
            mDeviceContactList = deviceContactList;
        }

        @Override
        protected List<ContactDao> doInBackground(Void... params) {
            List<ContactDao> mergedList = new ArrayList<>();
            try {
                List<String> numberList = mContactUtils.getInstance().getContactNumberList();
                String currentUserPhNumber = PreferanceUtils.getLoginUserObject(mFragment.getFragmentContext()).getPhone();
                if (Utils.isValidList(mServerList) && Utils.isValidList(numberList)) {
                    for (int i = 0; i < numberList.size(); i++) {
                        String phNumber = numberList.get(i);
                        boolean match = false;
                        for (int j = 0; j < mServerList.size(); j++) {
                            String serverNumber = mServerList.get(j).getPhone();
                            if (serverNumber.equalsIgnoreCase(phNumber)) {
                                mergedList.add(mServerList.get(j));
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            // check current login user & ph number are not same .
                            if (!currentUserPhNumber.equalsIgnoreCase(phNumber)) {
                                ContactDao dao = new ContactDao();
                                DeviceContacts contacts = mContactUtils.getByPhoneNumber(phNumber);
                                dao.setFullName(contacts.getFullname());
                                dao.setName(contacts.getFullname());
                                dao.setPhone(phNumber);
                                AppLog.d(TAG, "ph" + phNumber + "-" + contacts.getFullname());
                                mergedList.add(dao);
                            }
                        }
                    }
                }


            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }


            return mergedList;
        }

        @Override
        protected void onPostExecute(List<ContactDao> list) {
            super.onPostExecute(list);
            AppLog.d(TAG, list.size());
            hideDialog();
            hideRefreshLayout();
            setListview(list);
        }
    }

    public void callDeviceContactMergerTask(List<ContactDao> list) {
        ContactSync deviceContacts = new ContactSync(mFragment.getActivity());
        List<DeviceContacts> phoneContactList = deviceContacts.getDeviceContacts();
        new DeviceContactSyncWithServerTask(list, phoneContactList);
    }
}