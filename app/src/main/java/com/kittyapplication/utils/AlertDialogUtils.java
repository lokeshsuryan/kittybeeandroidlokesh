package com.kittyapplication.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kittyapplication.R;
import com.kittyapplication.adapter.SelectCoupleWithListAdapter;
import com.kittyapplication.adapter.SelectWithAdapter;
import com.kittyapplication.custom.AddKittyRulesListener;
import com.kittyapplication.custom.CustomEditTextNormal;
import com.kittyapplication.custom.CustomTextViewNormal;
import com.kittyapplication.custom.MiddleOfKittyListener;
import com.kittyapplication.listener.AddKidsCountListener;
import com.kittyapplication.listener.AlertMessageListener;
import com.kittyapplication.listener.ChangeGroupNameListener;
import com.kittyapplication.listener.ChatOptionClickListener;
import com.kittyapplication.listener.CoupleWithListener;
import com.kittyapplication.listener.GetImageFromListener;
import com.kittyapplication.listener.KittyManagerListener;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.ContactDaoWithHeader;
import com.kittyapplication.ui.activity.NotesActivity;
import com.kittyapplication.ui.viewinterface.RemoveCoupleAlertMessageListener;
import com.kittyapplication.ui.viewinterface.SelectCoupleWithListener;

import java.util.List;

/**
 * Created by Dhaval Riontech on 7/8/16.
 */
public class AlertDialogUtils {
    private static final String TAG = AlertDialogUtils.class.getSimpleName();
    private static Toast mToast = null;

    /**
     * @param message
     * @param context
     */
    public static void showSnackToast(final String message, final Context context) {
//        if (mToast != null)
//            mToast.cancel();
//
//        final LayoutInflater inflater = (LayoutInflater) context
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View layout = inflater.inflate(R.layout.toast_layout, null);
//        final TextView text = (TextView) layout
//                .findViewById(R.id.tv_toast_message);
//        text.setText(message);
//
//        mToast = new Toast(context);
//        mToast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
//        mToast.setDuration(Toast.LENGTH_SHORT);
//        mToast.setView(layout);
//        mToast.show();

        try {
            Toast.makeText(context, message,
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    public static void showCreateKittyDialog(final Context ctx) {
        try {
            /*final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
            LayoutInflater inflater = LayoutInflater.from(ctx);
            final View alertView = inflater.inflate(R.layout.dialog_create_kitty, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);

            alertView.findViewById(R.id.rlKittyWithCouple)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            String type = ctx.getResources().getStringArray(R.array.kitty)[0];
                            Utils.openAddGroupActivity(ctx, type);
                        }
                    });
            alertView.findViewById(R.id.rlKittyWithKids)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            String type = ctx.getResources().getStringArray(R.array.kitty)[1];
                            Utils.openAddGroupActivity(ctx, type);
                        }
                    });
            alertView.findViewById(R.id.rlNormalKitty)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            String type = ctx.getResources().getStringArray(R.array.kitty)[2];
                            Utils.openAddGroupActivity(ctx, type);
                        }
                    });

            alertView.findViewById(R.id.txtCreateKidsKittyCancle)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
            dialog.show();*/

            final Dialog dialog = new Dialog(ctx, R.style.SettingsDialogTheme);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setContentView(R.layout.dialog_create_kitty);
            dialog.findViewById(R.id.rlKittyWithCouple)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            String type = ctx.getResources().getStringArray(R.array.kitty)[0];
                            Utils.openAddGroupActivity(ctx, type);
                        }
                    });
            dialog.findViewById(R.id.rlKittyWithKids)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            String type = ctx.getResources().getStringArray(R.array.kitty)[1];
                            Utils.openAddGroupActivity(ctx, type);
                        }
                    });
            dialog.findViewById(R.id.rlNormalKitty)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            String type = ctx.getResources().getStringArray(R.array.kitty)[2];
                            Utils.openAddGroupActivity(ctx, type);
                        }
                    });

            dialog.findViewById(R.id.txtCreateKidsKittyCancle)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
            dialog.show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void showCreateNotesDialog(final NotesActivity activity, String dialogTitle) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(activity).create();
            LayoutInflater inflater = LayoutInflater.from(activity);
            final View alertView = inflater.inflate(R.layout.dialog_create_notes, null);
            final EditText txtTitle = (EditText) alertView.findViewById(R.id.txtNotesTitle);
            final EditText txtData = (EditText) alertView.findViewById(R.id.txtNotesData);
            ((TextView) alertView.findViewById(R.id.txtlabelCreateNotes)).setText(dialogTitle);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);

            alertView.findViewById(R.id.txtAddNotes)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Utils.isValidString(txtTitle.getText().toString())) {
                                if (Utils.isValidString(txtData.getText().toString())) {
                                    dialog.dismiss();
                                    activity.saveNote(txtTitle.getText().toString(), txtData.getText().toString());
                                } else {
                                    AlertDialogUtils.showSnackToast("Add Notes Detail", activity);
                                }
                            } else {
                                AlertDialogUtils.showSnackToast("Add Notes Title", activity);
                            }
                        }
                    });
            alertView.findViewById(R.id.imgCancelNotes)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
            dialog.show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void showAddKittyRulesDialog(Context ctx, final AddKittyRulesListener listener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(ctx.getResources().getString(R.string.add_kitty_rules)).setCancelable(false)
                    .setPositiveButton(ctx.getResources().getString(R.string.later),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    //new CreateGroupAsync().execute();
                                    listener.clickOnLater();
                                    dialog.cancel();
                                }
                            })
                    .setNegativeButton(ctx.getResources().getString(R.string.now),
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    listener.clickOnNow();
                                    dialog.cancel();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Resources.NotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void showMiddleOfKittyDialog(Context ctx, final MiddleOfKittyListener listener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(ctx.getResources().getString(R.string.in_middle_kitty_string)).setCancelable(false)
                    .setPositiveButton(ctx.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            listener.clickOnNo();
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(ctx.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            listener.clickOnYes();
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Resources.NotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    public static void showImagePickerDialog(final Context ctx, final GetImageFromListener listener) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
            LayoutInflater inflater = LayoutInflater.from(ctx);
            final View alertView = inflater.inflate(R.layout.dialog_image_picker, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);

            alertView.findViewById(R.id.rlCamera)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.getImageFrom(0);
                            dialog.dismiss();
                        }
                    });
            alertView.findViewById(R.id.rlGallery)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            listener.getImageFrom(1);
                            dialog.dismiss();
                        }
                    });
            alertView.findViewById(R.id.txtImagePickerCancel)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
            dialog.show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void showDialogSelectCouple(Context ctx, final List<ContactDao> list, final CoupleWithListener listener, int pos) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
            LayoutInflater inflater = LayoutInflater.from(ctx);
            final View alertView = inflater.inflate(R.layout.dialog_select_couple, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);
            final ContactDao memberTo = list.get(pos);
            list.remove(pos);
            ListView lvSelectWith = (ListView) alertView.findViewById(R.id.lvSelectWith);
            lvSelectWith.setAdapter(new SelectWithAdapter(ctx, list));
            lvSelectWith.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listener.getSelectedCoupleWithMember(list.get(position), memberTo);
                    dialog.dismiss();
                }
            });
            alertView.findViewById(R.id.txtSelectWithCancel)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
            dialog.show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * @param ctx
     * @param list
     * @param listener
     * @param pos
     */
    public static void showDialogSelectCouple1(Context ctx, final List<ContactDaoWithHeader> list,
                                               final SelectCoupleWithListener listener, int pos) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
            LayoutInflater inflater = LayoutInflater.from(ctx);
            final View alertView = inflater.inflate(R.layout.dialog_select_couple, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            final ContactDao memberFromList = list.get(pos).getData();
            list.remove(pos);
            ListView lvSelectWith = (ListView) alertView.findViewById(R.id.lvSelectWith);
            lvSelectWith.setAdapter(new SelectCoupleWithListAdapter(ctx, list));
            lvSelectWith.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listener.getSelectedCoupleWithMember(list.get(position).getData(), memberFromList);
                    dialog.dismiss();
                }
            });
            alertView.findViewById(R.id.txtSelectWithCancel)
                    .setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            listener.dismissDialog();
                            dialog.dismiss();
                        }
                    });
            dialog.show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void showAlertMessageDeleteCouple(Context ctx,
                                                    final AlertMessageListener listener,
                                                    final ContactDao user) {
       /* final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        final View alertView = inflater.inflate(R.layout.dialog_alert_message, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setView(alertView);
        CustomTextViewNormal alertMessage = (CustomTextViewNormal) alertView.findViewById(R.id.txtAlertMessage);
        alertMessage.setText(ctx.getResources().getString(R.string.alert_message_couple));

        alertView.findViewById(R.id.alertMessageYes)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClickYes(user);
                        dialog.dismiss();
                    }
                });
        alertView.findViewById(R.id.alertMessageNo)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onClickNo();
                        dialog.dismiss();
                    }
                });
        dialog.show();*/


        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(ctx.getResources().getString(R.string.alert_message_couple)).setCancelable(false)
                    .setPositiveButton(ctx.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            listener.onClickNo();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(ctx.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            listener.onClickYes(user);
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Resources.NotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void showAlertMessageDeleteCouple1(Context ctx,
                                                     final RemoveCoupleAlertMessageListener listener,
                                                     final ContactDaoWithHeader user) {

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(ctx.getResources().getString(R.string.alert_message_couple)).setCancelable(false)
                    .setPositiveButton(ctx.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            listener.onClickNo();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(ctx.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            listener.onClickYes(user);
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void showChnageGroupNameDialog(final Context ctx, final ChangeGroupNameListener listener) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
            LayoutInflater inflater = LayoutInflater.from(ctx);
            final View alertView = inflater.inflate(R.layout.dialog_change_group_name, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable
                    (new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);

            final CustomEditTextNormal edtGroupName = (
                    CustomEditTextNormal) alertView.findViewById(R.id.edtGroupName);

            alertView.findViewById(R.id.txtDone)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Utils.isValidText(edtGroupName,
                                    ctx.getResources().getString(R.string.group_name))) {
                                listener.getChangedGroupName(edtGroupName.getText().toString());
                                dialog.dismiss();
                            }
                        }
                    });
            dialog.show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void showServerError(String error, Context context) {
        try {
            if (Utils.isValidString(error)) {
                showSnackToast(error, context);
            } else {
                showSnackToast(context.getResources().getString(R.string.server_error), context);
            }
        } catch (Resources.NotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    public static void showChatOptionDialog(Context ctx,
                                            int type,
                                            final int pos,
                                            final ChatOptionClickListener listener) {
        try {
            final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
            LayoutInflater inflater = LayoutInflater.from(ctx);
            final View alertView = inflater.inflate(R.layout.dialog_chat_option, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable
                    (new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);

            LinearLayout llGroupOption = (LinearLayout) alertView.findViewById(R.id.llGroupOption);
            LinearLayout llPrivateOption = (LinearLayout) alertView.findViewById(R.id.llPrivateChatOption);

            //        type ==0 for display group chat option
            //        type ==1 for display private chat option
            //        type ==2 for display only clear chat option

            if (type == 0) {
                llGroupOption.setVisibility(View.VISIBLE);
                llPrivateOption.setVisibility(View.GONE);
            } else if (type == 1) {
                llGroupOption.setVisibility(View.GONE);
                llPrivateOption.setVisibility(View.VISIBLE);
            } else {
                llGroupOption.setVisibility(View.GONE);
                llPrivateOption.setVisibility(View.GONE);
            }

            alertView.findViewById(R.id.txtChatOptionCancle)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

            // group chat option
            alertView.findViewById(R.id.txtChatOptionDeleteGroup)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onDeleteGroup(pos);
                            dialog.dismiss();
                        }
                    });
            alertView.findViewById(R.id.txtChatOptionRefreshGroup)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onRefreshGroupChatData(pos);
                            dialog.dismiss();
                        }
                    });

            //private chat option
            alertView.findViewById(R.id.txtPrivateChatOptionDeleteChat)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onDeletePrivateChatData(pos);
                            dialog.dismiss();
                        }
                    });


            dialog.show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void showEnterKidsCountDialog(final Context ctx, final AddKidsCountListener listener, final int pos) {

        try {
            final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
            LayoutInflater inflater = LayoutInflater.from(ctx);
            final View alertView = inflater.inflate(R.layout.dialog_kids_count, null);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable
                    (new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setView(alertView);
            final CustomEditTextNormal edtKidsCount = (
                    CustomEditTextNormal) alertView.findViewById(R.id.edtKidsCount);
            edtKidsCount.setInputType(InputType.TYPE_CLASS_NUMBER);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            edtKidsCount.setFocusable(true);

            alertView.findViewById(R.id.txtDone)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Utils.isValidString(edtKidsCount.getText().toString())) {
                                listener.getKidsCount(pos, edtKidsCount.getText().toString());
                                dialog.dismiss();
                            } else {
                                edtKidsCount.setError(ctx.getResources().getString(R.string.kids_count_only_one_warning));
                            }
                        }
                    });
            dialog.show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    /**
     * @param ctx
     * @param message
     */
    public static void showOKButtonPopUpWithMessage(Context ctx, String message,
                                                    final MiddleOfKittyListener listener) {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(ctx.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();*/

        final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        final View alertView = inflater.inflate(R.layout.dialog_alert_message, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setView(alertView);
        CustomTextViewNormal alertMessage = (CustomTextViewNormal) alertView.findViewById(R.id.txtAlertMessage);
        alertMessage.setText(message);

        alertView.findViewById(R.id.alertMessageNo).setVisibility(View.GONE);
        CustomTextViewNormal txtYes = (CustomTextViewNormal) alertView.findViewById(R.id.alertMessageYes);

        txtYes.setText(ctx.getResources().getString(R.string.ok));

        txtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.clickOnYes();
                dialog.dismiss();
            }
        });
        /*alertView.findViewById(R.id.alertMessageNo)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.clickOnNo();
                        dialog.dismiss();
                    }
                });*/
        dialog.show();


    }

    public static void showYesNoAlert(Context ctx, String message, final MiddleOfKittyListener listener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(message).setCancelable(false)
                    .setPositiveButton(ctx.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            listener.clickOnNo();
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(ctx.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            listener.clickOnYes();
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Resources.NotFoundException e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    public static void showKittyManagerDialog(final Context ctx, final KittyManagerListener listener) {
        final AlertDialog dialog = new AlertDialog.Builder(ctx).create();
        LayoutInflater inflater = LayoutInflater.from(ctx);
        final View alertView = inflater.inflate(R.layout.dialog_kitty_manager, null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setView(alertView);
        String selectedRadio = "";
        final RadioGroup rbManager = (RadioGroup) alertView.findViewById(R.id.rgKittyManager);


        alertView.findViewById(R.id.alertMessageYes)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int radioButtonId = rbManager.getCheckedRadioButtonId();
                        RadioButton radioButton = (RadioButton) rbManager.findViewById(radioButtonId);
                        listener.getKittyManager(radioButton.getText().toString());
                        dialog.dismiss();
                    }
                });
        alertView.findViewById(R.id.alertMessageNo)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }


}
