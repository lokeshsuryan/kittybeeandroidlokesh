package com.kittyapplication.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;

import java.util.regex.Pattern;

/**
 * Created by Priyanka on 6/13/2016.
 */
public class IncomingSmsReceiver extends BroadcastReceiver {

    private static final String TAG = IncomingSmsReceiver.class.getSimpleName();
    final SmsManager sms = SmsManager.getDefault();
    public Pattern p = Pattern.compile("(|^)\\d{4}");
    String phoneNumber, senderNum, message;

    public static String stripNonDigits(
            final CharSequence input /* inspired by seh's comment */) {
        final StringBuilder sb = new StringBuilder(
                input.length() /* also inspired by seh's comment */);
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c > 47 && c < 58) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppLog.e(TAG, "onReceive");
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[0]);
                    phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    senderNum = phoneNumber;
                    message = currentMessage.getMessageBody();
                } // end for loop
            } // bundle is null
            if (senderNum.contains("RM-KITTYB")
                    || senderNum.contains("VM-KITTYB")
                    || senderNum.contains("KITTYB")) {
                if (message.length() != 0) {
                    String str_otp = stripNonDigits(message);
                    Intent intent1 = new Intent(AppConstant.OTP_ACTION);
                    intent1.putExtra(AppConstant.OTP_ACTION, str_otp);
                    context.sendBroadcast(intent1);
                    AppLog.e(TAG, "received success>>>>> ");
                }
            }
        } catch (Exception e) {
            AppLog.e("SmsReceiver", "Exception smsReceiver" + e);
        }
    }
}
