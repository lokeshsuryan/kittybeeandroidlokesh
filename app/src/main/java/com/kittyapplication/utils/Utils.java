package com.kittyapplication.utils;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kittyapplication.R;
import com.kittyapplication.custom.ImageLoaderListenerUniversal;
import com.kittyapplication.model.BannerDao;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.ContactNumber;
import com.kittyapplication.model.DeviceContacts;
import com.kittyapplication.model.MemberData;
import com.kittyapplication.model.MembersDaoCalendar;
import com.kittyapplication.model.ParticipantMember;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.ui.activity.AddGroupActivity;
import com.kittyapplication.ui.activity.ProfileActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.support.v4.content.ContextCompat.getColor;


/**
 * Created by Pintu Riontech on 25/5/16.
 * vaghela.pintu31@gmail.com
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    private static ProgressDialog mDialog;

    /**
     * Check Internet Available or Not
     */
    public static boolean checkInternetConnection(final Context context) {
        try {
            final ConnectivityManager conMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable()
                    && conMgr.getActiveNetworkInfo().isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    /**
     * open Activity
     *
     * @param context
     * @param c
     */
    public static void openActivity(Context context, Class<?> c) {
        try {
            context.startActivity(new Intent(context, c)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    ));
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Create Badge For Menu For Toolbar
     *
     * @param showStar
     * @param backgroundImageId
     * @param context
     * @return
     */
    public static Drawable buildCounterDrawable(boolean showStar, int backgroundImageId, Context context, RelativeLayout mRlNotification) {
        try {
            if (context != null) {
                mRlNotification.setBackgroundResource(backgroundImageId);
                int size = RelativeLayout.LayoutParams.WRAP_CONTENT;
                mRlNotification.setLayoutParams(new RelativeLayout.LayoutParams(size, size));
                if (!showStar) {
                    View counterTextPanel = mRlNotification.findViewById(R.id.imgNotification);
                    counterTextPanel.setVisibility(View.GONE);
                } else {
                    TextView textView = (TextView) mRlNotification.findViewById(R.id.txtBadgeText);
                    textView.setText("*");
                    textView.setVisibility(View.VISIBLE);
                }
                mRlNotification.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                mRlNotification.layout(0, 0, mRlNotification.getMeasuredWidth(), mRlNotification.getMeasuredHeight());
                mRlNotification.setDrawingCacheEnabled(true);
                mRlNotification.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                Bitmap bitmap = Bitmap.createBitmap(mRlNotification.getDrawingCache());
                mRlNotification.setDrawingCacheEnabled(false);

                return new BitmapDrawable(context.getResources(), bitmap);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Check Edit text Validation
     *
     * @param edt
     * @param str
     * @return
     */
    public static boolean isValidText(EditText edt, String str) {
        if (edt != null) {
            String string = edt.getText().toString().trim();
            if (string != null && edt.getText().toString().length() > 0) {
                return true;
            } else {
                edt.setError(str + " " + edt.getContext().getResources().getString(R.string.is_require_text));
                return false;
            }
        }
        return false;
    }

    public static boolean isValidName(EditText edt, String str) {
        if (edt != null) {
            String string = edt.getText().toString().trim();
            if (string.matches("[a-zA-z]+([ '-][a-zA-Z]+)*")) {
                return true;
            } else {
                edt.setError("Invalid " + str);
                return false;
            }
        }
        return false;
    }

    public final static boolean isValidEmail(EditText edt, String str) {
        if (edt != null) {
            String string = edt.getText().toString().trim();
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(string).matches()) {
                return true;
            } else {
                edt.setError("Invalid " + str);
                return false;
            }
        }
        return false;
    }

    /**
     * get text From Edit Text
     *
     * @param edt
     * @return
     */
    public static String getText(EditText edt) {
        return edt.getText().toString();
    }

    /**
     * Hide Keyboard programtically
     *
     * @param context
     * @param view
     */
    public static void hideKeyboard(final Context context, final View view) {
        try {
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Check Server data is Valid or not
     *
     * @param str
     * @return
     */
    public static boolean isValidString(String str) {
        try {
            if (str != null && str.length() > 0 && !TextUtils.isEmpty(str)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    /**
     * show toast message
     *
     * @param message
     */
    public static void showToast(String message, Context ctx) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
    }


    /**
     * Set Imei Option To Edit Text
     *
     * @param edt
     * @param isNext
     * @param ctx
     */
    public static void setImeiOption(final EditText edt, final boolean isNext, final Context ctx) {
        try {
            edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    try {
                        if (isNext) {
                            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                AppLog.e(TAG, edt.getText().toString());
                                ((View) v.getTag()).setFocusable(true);
                            }
                        } else {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                Utils.hideKeyboard(ctx, v);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Check Server mobile number is valid
     *
     * @param edt
     * @return
     */
    public static boolean isValidMobileNumber(EditText edt) {
        try {
            if (edt != null) {
                String string = edt.getText().toString().trim();
                if (string != null && edt.getText().toString().length() > 0) {
                    if (!Pattern.matches("[a-zA-Z]+", edt.getText().toString())) {
                        if (edt.getText().toString().length() != 10) {
                            edt.setError(edt.getContext().getResources().getString(R.string.kindly_enter_valid_mobile_new));
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    edt.setError(edt.getContext().getResources().getString(R.string.kindly_enter_valid_mobile_new));
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check Server mobile otp number is valid
     *
     * @param edt
     * @param str
     * @return
     */
    public static boolean isValidOtpNumber(EditText edt, String str) {
        try {
            if (edt != null) {
                String string = edt.getText().toString().trim();
                if (string != null && edt.getText().toString().length() > 0) {
                    if (!Pattern.matches("[a-zA-Z]+", edt.getText().toString())) {
                        if (edt.getText().toString().length() < 4 || edt.getText().toString().length() > 10) {
                            edt.setError(edt.getContext().getResources().getString(R.string.enter_valid_otp_sent_by_us_new));
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        edt.setError(edt.getContext().getResources().getString(R.string.enter_valid_otp_sent_by_us_new));
                        return false;
                    }
                } else {
                    edt.setError(edt.getContext().getResources().getString(R.string.enter_otp_sent_by_us_new));
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Show Progress dialog
     *
     * @param context
     */
    public static void showProgressDialog(Context context) {
        try {
            mDialog = new ProgressDialog(context.getApplicationContext());
            mDialog.setMessage(context.getResources().getString(R.string.loading_text));
            mDialog.show();
        } catch (Resources.NotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Hide Progress dialog
     */
    public static void hideProgressDialog() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static String getIMEI(Context context) {
        TelephonyManager mngr = (TelephonyManager)
                context.getSystemService(context.TELEPHONY_SERVICE);
        String imei = mngr.getDeviceId();
        return imei;
    }


    public static void openAddGroupActivity(Context ctx, String kittyType) {
        try {
            Intent intent = new Intent(ctx, AddGroupActivity.class);
            intent.putExtra("type", kittyType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            ctx.startActivity(intent);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void getBase64EncodedString(Uri data, Context context) {

        try {
            final Uri imageUri = data;
            final InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * set adv banner
     *
     * @param ctx
     * @param itemName
     * @param img
     */
    public static final void setBannerItem(final Context ctx, String itemName, ImageView img) {
        try {
            if (PreferanceUtils.getBannerFromPreferance(ctx) != null) {
                List<BannerDao> bannerDaoList = PreferanceUtils.getBannerFromPreferance(ctx).getBanner();
                if (bannerDaoList != null && !bannerDaoList.isEmpty()) {
                    for (int i = 0; i < bannerDaoList.size(); i++) {
                        if (itemName.equalsIgnoreCase(bannerDaoList.get(i).getTitle())) {
                            img.setVisibility(View.VISIBLE);
                            ImageUtils.getImageLoader(ctx).displayImage(bannerDaoList.get(i).getThamb(), img,
                                    new ImageLoaderListenerUniversal(ctx, img, "drawable://" + R.drawable.no_image_bottom_banner));
                            img.setTag(bannerDaoList.get(i).getUrl());
                            img.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String url = (String) v.getTag();
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    ctx.startActivity(browserIntent);
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
    }

    public static void makePhoneCall(String phoneNumber, Context context) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            context.startActivity(callIntent);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void makeSmsCall(String phoneNumber, ProfileActivity context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null)));
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    public static final String getImageInString(Bitmap bitmap) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos); //bm is the bitmap object
            byte[] byteArrayImage = baos.toByteArray();
            return Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            return "";
        }
    }


    public static void setKittyRuleImage(String type, Context ctx, ImageView img) {
        try {
            int imageId = 0;
            int imageBgColor = 0;
            if (type.equalsIgnoreCase(ctx.getResources().getStringArray(R.array.kitty)[0])) {
                imageId = R.drawable.ic_kitty_with_couple;
                imageBgColor = R.color.kitty_with_couple_bg;
            } else if (type.equalsIgnoreCase(ctx.getResources().getStringArray(R.array.kitty)[1])) {
                imageId = R.drawable.ic_kitty_with_kids;
                imageBgColor = R.color.kitty_with_kids_bg;
            } else {
                imageId = R.drawable.ic_normal_kitty;
                imageBgColor = R.color.normal_kitty_bg;
            }
            ImageUtils.getImageLoader(ctx).displayImage("drawable://" + imageId, img);
            img.setBackgroundColor(getColor(ctx, imageBgColor));
        } catch (Resources.NotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    /**
     * @param ctx
     * @param phoneNumber
     * @return
     */
    public static String getDisplayNameByPhoneNumber(Context ctx, String phoneNumber) {
        String displayName = phoneNumber;
        try {
            ContentResolver localContentResolver = ctx.getContentResolver();
            Cursor contactLookupCursor =
                    localContentResolver.query
                            (Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
                                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME},
                                    null, null, null);
            if (contactLookupCursor != null && contactLookupCursor.getCount() > 0) {
                contactLookupCursor.moveToLast();
                displayName = contactLookupCursor.getString
                        (contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

            if (contactLookupCursor != null) {
                contactLookupCursor.close();
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return displayName;
    }


    public static ContactDao createCoupleObject(Context ctx, ContactDao coupleTo,
                                                ContactDao coupleWith) {
        ContactDao coupleObject = null;
        try {
            coupleObject = new ContactDao();
            coupleObject.setName(ctx.getResources().getString(R.string.add_couple,
                    coupleTo.getName(), coupleWith.getName()));
            coupleObject.setPhone(ctx.getResources().getString(R.string.add_couple,
                    coupleTo.getPhone(), coupleWith.getPhone()));
            coupleObject.setIsHost(ctx.getResources().getString(R.string.add_couple,
                    coupleTo.getIsHost(), coupleWith.getIsHost()));
            coupleObject.setID(ctx.getResources().getString(R.string.add_couple,
                    coupleTo.getID(), coupleWith.getID()));
            coupleObject.setImage(ctx.getResources().getString(R.string.add_couple,
                    coupleTo.getImage(), coupleWith.getImage()));
            coupleObject.setFullName(ctx.getResources().getString(R.string.add_couple,
                    coupleTo.getFullName(), coupleWith.getFullName()));
            coupleObject.setUserId(ctx.getResources().getString(R.string.add_couple,
                    coupleTo.getUserId(), coupleWith.getUserId()));
            coupleObject.setLogin(ctx.getResources().getString(R.string.add_couple,
                    coupleTo.getLogin(), coupleWith.getLogin()));
            coupleObject.setRegistration(ctx.getResources().getString(R.string.add_couple,
                    coupleTo.getRegistration(), coupleWith.getRegistration()));
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return coupleObject;
    }

    public static List<ContactDao> seprateCoupleObjectToMember(ContactDao coupleObject) {
        List<ContactDao> listMember = new ArrayList<>();
        try {
            String[] couple = coupleObject.getName().split(AppConstant.SEPERATOR_STRING);
            String[] images = coupleObject.getImage().split(AppConstant.SEPERATOR_STRING);
            String[] phone = coupleObject.getPhone().split(AppConstant.SEPERATOR_STRING);
            String[] fullname = coupleObject.getFullName().split(AppConstant.SEPERATOR_STRING);
            String[] registration = coupleObject.getRegistration().split(AppConstant.SEPERATOR_STRING);
            String[] status = coupleObject.getStatus().split(AppConstant.SEPERATOR_STRING);
            String[] id = coupleObject.getID().split(AppConstant.SEPERATOR_STRING);

            for (int i = 0; i < 2; i++) {
                ContactDao object = new ContactDao();
                object.setImage(getStringFromArray(i, images));
                object.setName(getStringFromArray(i, couple));
                object.setPhone(getStringFromArray(i, phone));
                object.setStatus(getStringFromArray(i, status));
                object.setRegistration(getStringFromArray(i, registration));
                object.setFullName(getStringFromArray(i, fullname));
                object.setID(getStringFromArray(i, id));
                listMember.add(object);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return listMember;
    }

    public static List<MembersDaoCalendar> seprateCoupleObjectToMember(MembersDaoCalendar coupleObject) {
        List<MembersDaoCalendar> listMember = new ArrayList<>();
        try {
            if (coupleObject.getNumber().contains(AppConstant.SEPERATOR_STRING)) {
                String[] id = coupleObject.getId().split(AppConstant.SEPERATOR_STRING);
                String[] number = coupleObject.getNumber().split(AppConstant.SEPERATOR_STRING);
                String[] name = coupleObject.getName().split(AppConstant.SEPERATOR_STRING);
                String[] host = coupleObject.getHost().split(AppConstant.SEPERATOR_STRING);
                String[] delete = coupleObject.getDelete().split(AppConstant.SEPERATOR_STRING);
                String[] getPunctuality = null;
                if (coupleObject.getGetPunctuality() != null)
                    getPunctuality = coupleObject.getGetPunctuality().split(AppConstant.SEPERATOR_STRING);
                if (number.length == 2) {
                    for (int i = 0; i < 2; i++) {
                        MembersDaoCalendar object = new MembersDaoCalendar();
                        object.setId(getStringFromArray(i, id));
                        object.setNumber(getStringFromArray(i, number));
                        object.setName(getStringFromArray(i, name));
                        object.setHost(getStringFromArray(i, host));
                        object.setDelete(getStringFromArray(i, delete));
                        if (getPunctuality != null)
                            object.setGetPunctuality(getStringFromArray(i, getPunctuality));
                        listMember.add(object);
                    }
                } else {
                    listMember.add(coupleObject);
                }
            } else {
                listMember.add(coupleObject);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return listMember;
    }

    public static List<SummaryListDao> seprateCoupleObjectToMember(SummaryListDao coupleObject) {
        List<SummaryListDao> listMember = new ArrayList<>();
        try {
            if (coupleObject.getNumber().contains(AppConstant.SEPERATOR_STRING)) {
                String[] id = coupleObject.getId().split(AppConstant.SEPERATOR_STRING);
                String[] number = coupleObject.getNumber().split(AppConstant.SEPERATOR_STRING);
                String[] name = coupleObject.getName().split(AppConstant.SEPERATOR_STRING);
                String[] host = coupleObject.getHost().split(AppConstant.SEPERATOR_STRING);
                String[] currentHost = coupleObject.getCurrentHost().split(AppConstant.SEPERATOR_STRING);
                String[] profilePic = coupleObject.getProfilePic().split(AppConstant.SEPERATOR_STRING);

                if (number.length == 2) {

                    for (int i = 0; i < 2; i++) {
                        SummaryListDao object = new SummaryListDao();
                        object.setId(getStringFromArray(i, id).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setNumber(getStringFromArray(i, number).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setName(getStringFromArray(i, name).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setHost(getStringFromArray(i, host).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setCurrentHost(getStringFromArray(i, currentHost).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setProfilePic(getStringFromArray(i, profilePic).replace(AppConstant.SEPERATOR_STRING, ""));
                        listMember.add(object);
                    }
                } else {
                    listMember.add(coupleObject);
                }
            } else {
                listMember.add(coupleObject);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return listMember;
    }

    public static String getStringFromArray(int pos, String[] array) {
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

    public static String checkIfMe(Context context, String number, String name) {
        try {
            RegisterResponseDao dao = PreferanceUtils.getLoginUserObject(context);

            if (dao == null) {
                return number;
            } else if (dao != null && number.equalsIgnoreCase(dao.getPhone())) {
                return context.getString(R.string.you);
            } else {
                if (number != null && number.length() > 0) {
                    String displayName = getDisplayNameByPhoneNumber(context, number);
                    if (displayName != null && displayName.length() > 0) {
                        if (isValidString(displayName)) {
                            return displayName;
                        } else {
                            return number;
                            /*if (isValidString(name)) {
                                return name;
                            } else {
                                return number;
                            }*/
                        }
                    } else {
                        /*if (isValidString(name)) {
                            return name;
                        } else {
                            return number;
                        }*/
                        return number;
                    }
                } else {
                    /*if (isValidString(name)) {
                        return name;
                    } else {
                        return number;
                    }*/
                    return number;
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            /*if (isValidString(name)) {
                return name;
            } else {
                return number;
            }*/
            return number;
        }
    }

    /**
     * @param context
     * @param number
     * @return
     */
    public static String getNameForDiary(Context context, String number, String name) {
        try {
            RegisterResponseDao dao = PreferanceUtils.getLoginUserObject(context);
            if (dao == null) {
                return name;
            }

            if (dao != null && number.equalsIgnoreCase(dao.getPhone())) {
                return context.getString(R.string.you);
            } else {
                if (number != null && number.length() > 0) {
                    String displayName = getDisplayNameByPhoneNumber(context, number);
                    if (displayName != null && displayName.length() > 0) {
                        if (isValidString(displayName)) {
                            return displayName;
                        } else {
                            if (isValidString(name)) {
                                return name;
                            } else {
                                return number;
                            }
                        }
                    } else {
                        if (isValidString(name)) {
                            return name;
                        } else {
                            return number;
                        }
                    }
                } else {
                    if (isValidString(name)) {
                        return name;
                    } else {
                        return number;
                    }
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            if (isValidString(name)) {
                return name;
            } else {
                return number;
            }
        }
    }

    public static final List<MemberData>
    convertContactDataListIntoMemberDataList(List<ContactDao> list) {
        List<MemberData> memberList = new ArrayList<>();
        try {
            if (list != null && !list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    MemberData memberData = new MemberData();
                    memberData.setName(list.get(i).getName());
                    memberData.setImage(list.get(i).getImage());
                    memberData.setIsPaid(list.get(i).getIs_Paid());
                    memberData.setPhone(list.get(i).getPhone());
                    memberData.setIsMember("");
                    memberData.setRegistration(list.get(i).getRegistration());
                    memberData.setStatus(list.get(i).getStatus());
                    memberData.setUserid(list.get(i).getUserId());
                    memberData.setId(list.get(i).getID());
                    memberData.setMamber_id(list.get(i).getMamberID());
                    memberList.add(memberData);
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return memberList;
    }

    public static String checkNumberIsLoginUser(Context ctx, String phoneNumber, String name) {
        try {
            if (PreferanceUtils.getLoginUserObject(ctx).getPhone()
                    .equalsIgnoreCase(phoneNumber)) {
                return ctx.getResources().getString(R.string.you);
            } else {
                return name;
            }
        } catch (Resources.NotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
            return name;
        }
    }


    public static List<Integer> convertStringIdListIntoIntegerList(List<String> list) {
        List<Integer> listId = new ArrayList<>();
        try {
            for (int i = 0; i < list.size(); i++) {
                if (Utils.isValidString(list.get(i)))
                    listId.add(Integer.valueOf(list.get(i)));
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        AppLog.d(TAG, "INteger IDS = " + new Gson().toJson(listId));
        return listId;
    }

    public static List<ContactDao> mergeContactsWithServer(List<DeviceContacts> phoneContactsList,
                                                           List<ContactDao> serverContactList) {
        List<ContactDao> mergedList = new ArrayList<>();
        try {
            if (phoneContactsList != null && serverContactList != null) {
                for (int i = 0; i < phoneContactsList.size(); i++) {

                    String name = phoneContactsList.get(i).getFullname();

                    if (phoneContactsList.get(i).getPhoneNumber() != null &&
                            !phoneContactsList.get(i).getPhoneNumber().isEmpty()) {

                        List<ContactNumber> contactList = phoneContactsList.get(i).getPhoneNumber();

                        for (int j = 0; j < contactList.size(); j++) {

                            boolean isMatched = false;
                            for (int k = 0; k < serverContactList.size(); k++) {
                                if (contactList.get(j).getPhoneNumber().
                                        equalsIgnoreCase(serverContactList.get(k).getPhone())) {
                                    mergedList.add(serverContactList.get(k));
                                    isMatched = true;
                                    break;
                                }
                            }

                            // check if not matched then add phone contact with default values in list
                            if (!isMatched) {
                                ContactDao dao = new ContactDao();
                                dao.setPhone(contactList.get(j).getPhoneNumber());
                                dao.setName(name);
                                mergedList.add(dao);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return mergedList;
    }

    public static SummaryListDao checkIfSingleMemberInCoupleKitty(SummaryListDao summaryListDao) {
        SummaryListDao dao = new SummaryListDao();
        if (summaryListDao.getNumber().contains(AppConstant.SEPERATOR_STRING)) {
            String[] number = summaryListDao.getNumber().split(AppConstant.SEPERATOR_STRING);

            if (number.length == 2) {
                return summaryListDao;
            } else {
                dao.setId(summaryListDao.getId().replace(AppConstant.SEPERATOR_STRING, ""));
                dao.setNumber(summaryListDao.getNumber().replace(AppConstant.SEPERATOR_STRING, ""));
                dao.setName(summaryListDao.getName().replace(AppConstant.SEPERATOR_STRING, ""));
                dao.setProfilePic(summaryListDao.getProfilePic().replace(AppConstant.SEPERATOR_STRING, ""));
                dao.setCurrentHost(summaryListDao.getCurrentHost().replace(AppConstant.SEPERATOR_STRING, ""));
                dao.setHost(summaryListDao.getHost().replace(AppConstant.SEPERATOR_STRING, ""));
                return dao;
            }
        } else {
            return summaryListDao;
        }
    }

    public static <T> Boolean isValidList(List<T> list) {
        if (list != null && !list.isEmpty() && list.size() > 0) {
            return true;
        } else {
            return false;
        }
    }


    public static List<ParticipantMember> seprateCoupleObjectToParticipentMember(ParticipantMember coupleObject) {
        List<ParticipantMember> listMember = new ArrayList<>();
        try {
            if (coupleObject.getNumber().contains(AppConstant.SEPERATOR_STRING)) {
                String[] id = coupleObject.getId().split(AppConstant.SEPERATOR_STRING);
                String[] number = coupleObject.getNumber().split(AppConstant.SEPERATOR_STRING);
                String[] name = coupleObject.getName().split(AppConstant.SEPERATOR_STRING);
                String[] host = coupleObject.getHost().split(AppConstant.SEPERATOR_STRING);
                String[] currentHost = coupleObject.getCurrentHost().split(AppConstant.SEPERATOR_STRING);
                String[] profilePic = coupleObject.getProfile().split(AppConstant.SEPERATOR_STRING);
                String[] admin = coupleObject.getIsAdmin().split(AppConstant.SEPERATOR_STRING);
                String[] chatID = coupleObject.getChatId().split(AppConstant.SEPERATOR_STRING);

                for (int i = 0; i < number.length; i++) {
                    ParticipantMember object = new ParticipantMember();
                    object.setId(getStringFromArray(i, id).replace(AppConstant.SEPERATOR_STRING, ""));
                    object.setNumber(getStringFromArray(i, number).replace(AppConstant.SEPERATOR_STRING, ""));
                    object.setName(getStringFromArray(i, name).replace(AppConstant.SEPERATOR_STRING, ""));
                    object.setHost(getStringFromArray(i, host).replace(AppConstant.SEPERATOR_STRING, ""));
                    object.setCurrentHost(getStringFromArray(i, currentHost).replace(AppConstant.SEPERATOR_STRING, ""));
                    object.setProfile(getStringFromArray(i, profilePic).replace(AppConstant.SEPERATOR_STRING, ""));
                    object.setIsAdmin(getStringFromArray(i, admin).replace(AppConstant.SEPERATOR_STRING, ""));
                    object.setChatId(getStringFromArray(i, chatID).replace(AppConstant.SEPERATOR_STRING, ""));
                    listMember.add(object);
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return listMember;
    }


    public static Bitmap getBitmapFromString(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    public static boolean isValidURL(String str) {
        if (URLUtil.isValidUrl(str)) {
            return true;
        } else {
            return false;
        }
    }

    public static String checkIfMeForBoth(Context context, String number, String name) {
        try {
            if (!number.contains(AppConstant.SEPERATOR_STRING)) {
                if (number.equalsIgnoreCase(PreferanceUtils
                        .getLoginUserObject(context).getPhone())) {
                    return context.getString(R.string.you);
                } else {
                    if (number != null && number.length() > 0) {
                        String displayName = getDisplayNameByPhoneNumber(context, number);
                        if (displayName != null && displayName.length() > 0) {
                            return displayName.replace(AppConstant.SEPERATOR_STRING, "");
                        } else {
                            return number.replace(AppConstant.SEPERATOR_STRING, "");
                        }
                    } else {
                        return number.replace(AppConstant.SEPERATOR_STRING, "");
                    }
                }
            } else {
                String[] numbers = number.split(AppConstant.SEPERATOR_STRING);
                String[] names = name.split(AppConstant.SEPERATOR_STRING);
                String strName = "";
                //member one
                if (numbers[0].replace(AppConstant.SEPERATOR_STRING, "")
                        .equalsIgnoreCase(PreferanceUtils
                                .getLoginUserObject(context).getPhone())) {
                    strName = context.getString(R.string.you);
                } else {
                    if (numbers[0].replace(AppConstant.SEPERATOR_STRING, "")
                            != null && numbers[0].length() > 0) {
                        String displayName = getDisplayNameByPhoneNumber(context, numbers[0].replace(AppConstant.SEPERATOR_STRING, ""));
                        if (displayName != null && displayName.length() > 0) {
                            strName = displayName;
                        } else {
                            strName = numbers[0].replace(AppConstant.SEPERATOR_STRING, "");
                        }
                    } else {
                        strName = numbers[0].replace(AppConstant.SEPERATOR_STRING, "");
                    }
                }
                //member two
                if (numbers.length == 2) {
                    if (numbers[1].replace(AppConstant.SEPERATOR_STRING, "")
                            .equalsIgnoreCase(PreferanceUtils
                                    .getLoginUserObject(context).getPhone())) {
                        strName = strName.concat(context.getResources().getString(R.string.and_seperat_with_space_both_side)
                                + context.getString(R.string.you));
                    } else {
                        if (numbers[1] != null && numbers[1].length() > 0) {
                            String displayName = getDisplayNameByPhoneNumber(context, numbers[1].replace(AppConstant.SEPERATOR_STRING, ""));
                            if (displayName != null && displayName.length() > 0) {
                                strName = strName.concat(context.getResources().getString(R.string.and_seperat_with_space_both_side)
                                        + displayName);
                            } else {
                                strName = strName.concat(context.getResources().getString(R.string.and_seperat_with_space_both_side)
                                        + numbers[1].replace(AppConstant.SEPERATOR_STRING, ""));
                            }
                        } else {
                            strName = strName.concat(context.getResources().getString(R.string.and_seperat_with_space_both_side)
                                    + numbers[1].replace(AppConstant.SEPERATOR_STRING, ""));
                        }
                    }
                }
                return strName;
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
            return name;
        }
    }


    public static String checkIfMeInCouple(Context ctx, String number, String name) {
        // check is couple or not
        if (name.contains(AppConstant.SEPERATOR_STRING)) {
            // seprate the object
            String[] names = name.split(AppConstant.SEPERATOR_STRING);
            String[] numbers = number.split(AppConstant.SEPERATOR_STRING);
            String result = "";

            // is couple
            if (numbers.length == 2) {
                for (int i = 0; i < 2; i++) {
                    if (numbers[i].replace(AppConstant.SEPERATOR_STRING, "").equalsIgnoreCase
                            (PreferanceUtils.getLoginUserObject(ctx).getPhone())) {
                        result = result.concat(ctx.getResources()
                                .getString(R.string.you_couple));
                    } else {
                        // check number is in phone book or not
                        // if  not than display profile name.
                        String userName = "";
                        try {
                            userName = names[i].replace(AppConstant.SEPERATOR_STRING, "");
                        } catch (Exception e) {
                            userName = "";
                        }
                        String phBookName = Utils.checkIfMe(ctx, numbers[i].replace(AppConstant.SEPERATOR_STRING, "")
                                , userName);
                        result = result.concat(phBookName + ctx.getResources()
                                .getString(R.string.dash));
                    }
                }
                result = result.substring(0, (result.length() - 1));
                return result;
            } else {
                // single member with seprator string "-!-"
                if (number.replace(AppConstant.SEPERATOR_STRING, "").equalsIgnoreCase
                        (PreferanceUtils.getLoginUserObject(ctx).getPhone())) {
                    return ctx.getResources().getString(R.string.you);
                } else {
                    String username = "";
                    try {
                        username = names[0];
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String phBookName = Utils.checkIfMe(ctx, numbers[0].replace(AppConstant.SEPERATOR_STRING, "")
                            , username.replace(AppConstant.SEPERATOR_STRING, ""));
                    return phBookName;
                }
            }
        } else {
            //not couple
            if (number.equalsIgnoreCase
                    (PreferanceUtils.getLoginUserObject(ctx).getPhone())) {
                return ctx.getResources().getString(R.string.you);
            } else {
                return Utils.checkIfMe(ctx, number, name);
            }
        }
    }

    public static List<ParticipantMember> seprateCoupleObjectToPrticipant
            (ParticipantMember coupleObject) {
        List<ParticipantMember> listMember = new ArrayList<>();
        try {
            if (coupleObject.getNumber().contains(AppConstant.SEPERATOR_STRING)) {
                String[] id = coupleObject.getId().split(AppConstant.SEPERATOR_STRING);
                String[] number = coupleObject.getNumber().split(AppConstant.SEPERATOR_STRING);
                String[] name = coupleObject.getName().split(AppConstant.SEPERATOR_STRING);
                String[] host = coupleObject.getHost().split(AppConstant.SEPERATOR_STRING);
                String[] currentHost = coupleObject.getCurrentHost().split(AppConstant.SEPERATOR_STRING);
                String[] profilePic = coupleObject.getProfile().split(AppConstant.SEPERATOR_STRING);
                String[] userId = coupleObject.getUserId().split(AppConstant.SEPERATOR_STRING);
                String[] isAdmin = coupleObject.getIsAdmin().split(AppConstant.SEPERATOR_STRING);
                String[] chatId = coupleObject.getChatId().split(AppConstant.SEPERATOR_STRING);
                String[] delete = coupleObject.getDelete().split(AppConstant.SEPERATOR_STRING);
                String[] status = coupleObject.getStatus().split(AppConstant.SEPERATOR_STRING);


                if (number.length == 2) {

                    for (int i = 0; i < 2; i++) {
                        ParticipantMember object = new ParticipantMember();
                        object.setId(getStringFromArray(i, id).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setNumber(getStringFromArray(i, number).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setName(getStringFromArray(i, name).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setHost(getStringFromArray(i, host).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setCurrentHost(getStringFromArray(i, currentHost).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setProfile(getStringFromArray(i, profilePic).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setUserId(getStringFromArray(i, userId).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setIsAdmin(getStringFromArray(i, isAdmin).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setChatId(getStringFromArray(i, chatId).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setDelete(getStringFromArray(i, delete).replace(AppConstant.SEPERATOR_STRING, ""));
                        object.setStatus(getStringFromArray(i, status).replace(AppConstant.SEPERATOR_STRING, ""));

                        listMember.add(object);
                    }
                } else {
                    listMember.add(coupleObject);
                }
            } else {
                listMember.add(coupleObject);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        return listMember;
    }

    public static String removeSeperatorFromSingleCoupleKittyMember(String pStr) {
        String str = pStr;
        if (str.contains(AppConstant.SEPERATOR_STRING)) {
            String[] splitStr = str.split(AppConstant.SEPERATOR_STRING);
            if (isValidString(splitStr[0]) && !isValidString(splitStr[1])) {
                return splitStr[0];
            } else {
                return str;
            }
        } else {
            return str;
        }
    }

    public static List<DeviceContacts> mergerSimContactWithList(List<DeviceContacts> phoneContact,
                                                                List<DeviceContacts> simContact) {
        List<DeviceContacts> mergedList = new ArrayList<>();
        try {
            if (Utils.isValidList(phoneContact) && Utils.isValidList(simContact)) {
                for (int i = 0; i < phoneContact.size(); i++) { //732
                    String name = phoneContact.get(i).getFullname();

                    if (Utils.isValidList(phoneContact.get(i).getPhoneNumber())) {
                        List<ContactNumber> contactList = phoneContact.get(i).getPhoneNumber();

                        for (int j = 0; j < contactList.size(); j++) { /// 3
                            if (Utils.isValidList(simContact)) {
                                for (int l = 0; l < simContact.size(); l++) { //237/////

                                    List<ContactNumber> simContactNumber = simContact.get(l).getPhoneNumber();
                                    boolean isMatched = false;
                                    for (int k = 0; k < simContactNumber.size(); k++) {
                                        if (contactList.get(j).getPhoneNumber().
                                                equalsIgnoreCase(simContactNumber
                                                        .get(k).getPhoneNumber())) {
                                            mergedList.add(simContact.get(l));
                                            isMatched = true;
                                            break;
                                        }
                                    }

                                    // check if not matched then add phone contact with default values in list
                                    if (!isMatched) {
                                        DeviceContacts deviceContacts = new DeviceContacts();
                                        deviceContacts.setFullname(name);
                                        List<ContactNumber> numbersList = new ArrayList<>();
                                        ContactNumber number = new ContactNumber();
                                        number.setPhoneNumber(contactList.get(j).getPhoneNumber());
                                        numbersList.add(number);
                                        deviceContacts.setPhoneNumber(numbersList);
                                        mergedList.add(deviceContacts);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (Utils.isValidList(phoneContact)) {
                    mergedList.addAll(phoneContact);
                } else if (Utils.isValidList(simContact)) {
                    mergedList.addAll(simContact);
                }
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
        AppLog.e(TAG, "MERGED LIST = " + mergedList.size());
        return mergedList;

    }

    public static void openProfileActivity(Context ctx, String userID) {
        Intent profileIntent = new Intent(ctx, ProfileActivity.class);
        profileIntent.putExtra(AppConstant.USER_PROFILE_ID, userID);
        ctx.startActivity(profileIntent);
    }
}
