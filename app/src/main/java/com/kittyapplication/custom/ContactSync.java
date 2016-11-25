package com.kittyapplication.custom;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

import com.kittyapplication.model.ContactNumber;
import com.kittyapplication.model.DeviceContacts;
import com.kittyapplication.utils.AppLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Pintu Riontech on 6/8/16.
 * vaghela.pintu31@gmail.com
 */
public class ContactSync {
    private static final String TAG = ContactSync.class.getSimpleName();
    private Context mContext;

    public ContactSync(Context ctx) {
        mContext = ctx;
    }

    /**
     * Fetching device contacts of user
     *
     * @return list of contacts
     */
    public List<DeviceContacts> getDeviceContacts() {
        Cursor cursor = null;
        final ArrayList<DeviceContacts> contacts = new ArrayList<>();
        try {
            final String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP
                    + " = '" + ("1") + "'";
            final String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                    + " COLLATE LOCALIZED ASC";

        /*Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                selection + " AND "
                        + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
                null, sortOrder);*/


            if (Build.VERSION.SDK_INT >= 23) {
                cursor = mContext.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI,
                        null,
                        selection + " AND "
                                + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1",
                        null, sortOrder);
            } else {
                cursor = mContext.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            }

            if (cursor == null || cursor.getCount() <= 0) {
                cursor = mContext.getContentResolver().query(
                        ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            }


            final Set<String> nameSet = new HashSet<>();
            final Set<String> phoneSet = new HashSet<>();

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    boolean nameFlag = false;
                    boolean phoneFlag = false;
                    int index = 0;
                    final String id = cursor.getString(cursor
                            .getColumnIndex(BaseColumns._ID));

                    DeviceContacts item = new DeviceContacts();

                    final Cursor phoneNumberCursor = mContext.getContentResolver()
                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                            + " = ?", new String[]{id}, null);

                    if (phoneNumberCursor != null && phoneNumberCursor.getCount() > 0) {
                        if (phoneNumberCursor.getCount() == 1) {
                            phoneNumberCursor.moveToFirst();
                            final ContactNumber number = new ContactNumber();
                            String phoneNumber = phoneNumberCursor
                                    .getString(
                                            phoneNumberCursor
                                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                    .replaceAll("\\s", "").toString().replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "");
                            ;

                            if (phoneNumber.length() > 10) {
                                phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                            }
                            if (phoneSet.add(phoneNumber)) {
                                phoneFlag = true;
                                number.setPhoneNumber(phoneNumber);
                                item.getPhoneNumber().add(number);
                            }
                        } else {
                            while (phoneNumberCursor.moveToNext()) {
                                final ContactNumber number = new ContactNumber();
                                String phoneNumber = phoneNumberCursor
                                        .getString(
                                                phoneNumberCursor
                                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                        .replaceAll("\\s", "").toString()
                                        .replaceAll("-", "").replaceAll("\\(", "").replaceAll("\\)", "");
                                ;

                                if (phoneNumber.length() > 10) {
                                    phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                                }

                                if (phoneSet.add(phoneNumber)) {
                                    phoneFlag = true;
                                    number.setPhoneNumber(phoneNumber);
                                    item.getPhoneNumber().add(number);
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    if (phoneNumberCursor != null)
                        phoneNumberCursor.close();

                    if (phoneFlag) {
                        final String fullName = cursor
                                .getString(cursor
                                        .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        if (nameSet.add(fullName)) {
                            nameFlag = true;
                            item.setFullname(fullName);
                        } else {
                            // AppLog.d(TAG, "Already there = " + fullName);
                            for (int i = 0; i < contacts.size(); i++) {
                                if (fullName.equalsIgnoreCase(contacts.get(i)
                                        .getFullname())) {
                                    item = contacts.get(i);
                                    index = i;
                                    break;
                                }
                            }
                        }

                        final Cursor emailCursor = mContext.getContentResolver().query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                        + " = ?", new String[]{id}, null);

                        if (emailCursor.getCount() == 1) {
                            emailCursor.moveToFirst();
                            item.setEmail(emailCursor.getString(emailCursor
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
                        } else {
                            while (emailCursor.moveToNext()) {
                                item.setEmail(emailCursor.getString(emailCursor
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
                            }
                        }

                        emailCursor.close();

                        final Uri contactPhotoUri = ContentUris.withAppendedId(
                                ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));

                        final Uri photoUri = Uri.withAppendedPath(contactPhotoUri,
                                ContactsContract.CommonDataKinds.Photo.PHOTO);
                        item.setUserPhoto(photoUri.toString());

                        if (nameFlag) {
                            contacts.add(item);
                        } else {
                            contacts.set(index, item);
                        }
                    }
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        AppLog.d(TAG, "Contacts Size == " + contacts.size());
        return contacts;
    }
}
