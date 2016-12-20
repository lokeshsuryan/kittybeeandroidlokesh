package com.kittyapplication.utils;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.kittyapplication.core.utils.SharedPrefsHelper;
import com.kittyapplication.custom.ContactSync;
import com.kittyapplication.custom.swipe.GetSimContact;
import com.kittyapplication.model.ContactNumber;
import com.kittyapplication.model.DeviceContacts;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 10/11/16.
 */

public class DeviceContactUtils {
    private static final String TAG = DeviceContactUtils.class.getSimpleName();
    private static DeviceContactUtils instance;
    private ArrayList<DeviceContacts> contactList = new ArrayList<>();
    private HashMap<String, DeviceContacts> contactsHashMap = new HashMap<>();

    public static synchronized DeviceContactUtils getInstance() {
        if (instance == null) {
            instance = new DeviceContactUtils();
        }
        return instance;
    }

    private DeviceContactUtils() {
        contactList = getList();
    }

    public List<DeviceContacts> getContactList() {
        List<DeviceContacts> list = new ArrayList<DeviceContacts>(contactsHashMap.values());
        return list;
    }

    public void getDeviceContacts(Context ctx) {

        //Get Phone Book Contacts
        ContactSync deviceContacts = new ContactSync(ctx);
        List<DeviceContacts> phoneContactList = deviceContacts.getDeviceContacts();

        // add phone book contact to hashmap
        addList(phoneContactList);

        //Get SIM Contacts
        GetSimContact simContact = new GetSimContact(ctx);
        List<DeviceContacts> simContactList = simContact.getContactList();

        addList(simContactList);
    }


    public synchronized void saveContactList() {
        try {
            if (contactsHashMap != null && contactsHashMap.size() > 0) {
                List<DeviceContacts> list = new ArrayList<DeviceContacts>(contactsHashMap.values());
                String strGroupChats = new Gson().toJson(list);
                SharedPrefsHelper.getInstance().save(AppConstant.CONTACT_UTILS, strGroupChats);
                this.contactList = getList();
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private ArrayList<DeviceContacts> getList() {
        try {
            String json = SharedPrefsHelper.getInstance().get(AppConstant.CONTACT_UTILS, null);
            if (json != null) {
                Type type = new TypeToken<ArrayList<DeviceContacts>>() {
                }.getType();
                return new Gson().fromJson(json, type);
            }
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public synchronized void add(DeviceContacts deviceContacts) {
//        contactList.add(groupChat);
        try {
            if (Utils.isValidList(deviceContacts.getPhoneNumber())) {
                for (int i = 0; i < deviceContacts.getPhoneNumber().size(); i++) {
                    contactsHashMap.put(deviceContacts.getPhoneNumber().get(i).getPhoneNumber(),
                            deviceContacts);
                    saveContactList();
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public synchronized void addList(List<DeviceContacts> deviceContacts) {
        try {
            if (Utils.isValidList(deviceContacts)) {
                for (int i = 0; i < deviceContacts.size(); i++) {
                    List<ContactNumber> numberList = deviceContacts.get(i).getPhoneNumber();
                    if (Utils.isValidList(numberList))
    
                        for (int j = 0; j < numberList.size(); j++) {
                            DeviceContacts contact = deviceContacts.get(i);
                            String number = numberList.get(j).getPhoneNumber();
                            if (number.charAt(0) == '0' || number.charAt(0) == '+') {
                                number = number.substring(1, number.length());
                            }
                            contactsHashMap.put(number, contact);
                        }
                }
            }
            saveContactList();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public synchronized void update(DeviceContacts deviceContacts) {
        try {
            if (Utils.isValidList(deviceContacts.getPhoneNumber())) {
                for (int i = 0; i < deviceContacts.getPhoneNumber().size(); i++) {
                    String number = deviceContacts.getPhoneNumber().get(i).getPhoneNumber();
                    contactsHashMap.put(number, deviceContacts);
                    saveContactList();
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public synchronized void remove(String phoneNumber) {
        contactsHashMap.remove(phoneNumber);
        saveContactList();
    }

    public DeviceContacts getByPhoneNumber(String phoneNumber) {
        try {
            if (contactsHashMap.containsKey(phoneNumber)) {
                return contactsHashMap.get(phoneNumber);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public void clear() {
        SharedPrefsHelper.getInstance().delete(AppConstant.CONTACT_UTILS);
    }

    public List<String> getContactNumberList() {
        try {
            if (contactsHashMap != null && contactsHashMap.size() > 0) {
                List<String> list = new ArrayList<String>(contactsHashMap.keySet());
                return list;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}