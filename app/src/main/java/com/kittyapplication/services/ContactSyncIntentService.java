package com.kittyapplication.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.kittyapplication.custom.AlphaBaticSortContact;
import com.kittyapplication.custom.ContactSync;
import com.kittyapplication.model.AppContactList;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.ContactNumber;
import com.kittyapplication.model.DeviceContacts;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.PreferanceUtils;
import com.kittyapplication.utils.Utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Scorpion on 21-08-2016.
 */
public class ContactSyncIntentService extends IntentService {
    private static final String TAG = ContactSyncIntentService.class.getSimpleName();
    private Context mContext;
    private List<DeviceContacts> mDeviceContactList;

    public ContactSyncIntentService() {
        super("ContactSyncIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ContactSyncIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppLog.d("", "Inside onHandleIntent of service");
        mContext = this;
        deviceTask();
    }

    /**
     *
     */
    private void deviceTask() {
        HashMap<String, String> hashMap = new HashMap<>();
        ContactSync contactSync = new ContactSync(mContext);
        mDeviceContactList = contactSync.getDeviceContacts();

        for (int i = 0; i < mDeviceContactList.size(); i++) {
            if (mDeviceContactList.get(i).getPhoneNumber() != null
                    && !mDeviceContactList.get(i).getPhoneNumber().isEmpty()) {
                List<ContactNumber> contactList = mDeviceContactList.get(i).getPhoneNumber();
                for (int j = 0; j < contactList.size(); j++) {
                    hashMap.put(contactList.get(j).getPhoneNumber(),
                            mDeviceContactList.get(i).getFullname());
                }
            }
        }
        hashMap.put("number", PreferanceUtils.getLoginUserObject(mContext).getPhone());

        Call<ServerResponse<List<ContactDao>>> call =
                Singleton.getInstance().getRestOkClient().
                        syncContactToServer(PreferanceUtils.getLoginUserObject(mContext).getUserID(), hashMap);
        call.enqueue(syncContactCallBack);
    }

    /**
     *
     */
    private Callback<ServerResponse<List<ContactDao>>> syncContactCallBack = new Callback<ServerResponse<List<ContactDao>>>() {
        @Override
        public void onResponse(Call<ServerResponse<List<ContactDao>>> call, Response<ServerResponse<List<ContactDao>>> response) {
            if (response.code() == 200) {
                List<ContactDao> data = response.body().getData();
                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS
                        && data != null && !data.isEmpty()) {
                    List<ContactDao> list = data;
                    Collections.sort(list, new AlphaBaticSortContact());
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getRegistration().equalsIgnoreCase("1")) {
                            ContactDao obj = list.get(i);
                            list.remove(i);
                            list.add(0, obj);
                        }
                    }
                    new DeviceContactSyncWithServerTask(list, mDeviceContactList).execute();
                }
            }
        }

        @Override
        public void onFailure(Call<ServerResponse<List<ContactDao>>> call, Throwable t) {
        }
    };

    /**
     *
     */
    public class DeviceContactSyncWithServerTask
            extends AsyncTask<Void, Void, List<ContactDao>> {
        List<ContactDao> mServerList;
        List<DeviceContacts> mDeviceContactList;


        public DeviceContactSyncWithServerTask(List<ContactDao> serverList,
                                               List<DeviceContacts> deviceContactList) {
            mServerList = serverList;
            mDeviceContactList = deviceContactList;
        }

        @Override
        protected List<ContactDao> doInBackground(Void... params) {
            List<ContactDao> mDeviceContactMergeList =
                    Utils.mergeContactsWithServer(mDeviceContactList,
                            mServerList);
            return mDeviceContactMergeList;
        }

        @Override
        protected void onPostExecute(List<ContactDao> list) {
            super.onPostExecute(list);
            AppContactList contactListObject = new AppContactList();
            contactListObject.setContactList(list);
            PreferanceUtils.setContactListIntoPreferance(mContext, contactListObject);
            /*Intent intent = new Intent(AppConstant.CONTACT_SYNC_ACTION);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);*/
        }
    }
}
