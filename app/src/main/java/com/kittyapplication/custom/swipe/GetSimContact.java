package com.kittyapplication.custom.swipe;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.kittyapplication.model.ContactNumber;
import com.kittyapplication.model.DeviceContacts;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfBloodPrince(RIONTECH)
 * Riontech on 9/11/16.
 */

public class GetSimContact {
    private static final String TAG = GetSimContact.class.getSimpleName();
    private Context mContext;


    public GetSimContact(Context ctx) {
        mContext = ctx;
    }

    public List<DeviceContacts> getContactList() {
        List<DeviceContacts> contacts = new ArrayList<>();
        try {
            // TODO Getting Sim Contact
            Uri simUri = Uri.parse("content://icc/adn");
            Cursor cursorSim = mContext.getContentResolver().query(simUri, null, null, null, null);

            if (cursorSim != null) {
                while (cursorSim.moveToNext()) {
                    String simContactName = cursorSim.getString(cursorSim.getColumnIndex("name"));
                    String simContactNumber = cursorSim.getString(cursorSim.getColumnIndex("number"));
                    simContactNumber.replaceAll("\\D", "");
                    simContactNumber.replaceAll("&", "");
                    simContactName = simContactName.replace("|", "");

                    if (Utils.isValidString(simContactNumber) && simContactNumber.length() >= 7) {
                        DeviceContacts deviceContacts = new DeviceContacts();
                        deviceContacts.setFullname(simContactName);
                        List<ContactNumber> numbersList = new ArrayList<>();
                        ContactNumber number = new ContactNumber();
                        number.setPhoneNumber(simContactNumber);
                        numbersList.add(number);
                        deviceContacts.setPhoneNumber(numbersList);
                        contacts.add(deviceContacts);
                    }
                }
                cursorSim.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        AppLog.e(TAG, "SIM CONTActs SIZE = " + contacts.size());
        return contacts;
    }
}
