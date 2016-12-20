package com.kittyapplication.ui.callback;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.chat.utils.qb.QbDialogHolder;
import com.kittyapplication.core.ui.adapter.BaseSelectedRecyclerViewAdapter;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.ResourceUtils;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.model.ReqRefreshGroup;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.providers.KittyBeeContract;
import com.kittyapplication.rest.Singleton;
import com.kittyapplication.ui.viewmodel.KittyViewModel;
import com.kittyapplication.utils.AlertDialogUtils;
import com.kittyapplication.utils.AppConstant;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by MIT on 8/30/2016.
 */
public class ChatActionModeCallback implements ActionMode.Callback {
    private static final String TAG = ChatActionModeCallback.class.getSimpleName();
    private BaseSelectedRecyclerViewAdapter adapter;
    private ActionMode currentActionMode;
    private Context context;
    private boolean isPrivate;

    public ChatActionModeCallback(BaseSelectedRecyclerViewAdapter adapter, Context context, boolean isPrivate) {
        this.adapter = adapter;
        this.context = context;
        this.isPrivate = isPrivate;
    }

    private interface OnUserSelectionListener {
        void onProceed();

        void onCancel();
    }

    public void setCurrentActionMode(ActionMode currentActionMode) {
        this.currentActionMode = currentActionMode;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_action_mode_home, menu);
        if (isPrivate) {
            menu.findItem(R.id.menu_action_refresh).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_dialogs_action_delete:
                deleteChat();

                return true;
            case R.id.menu_action_refresh:
                refreshChatData();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        System.out.println("onDestroyActionMode");
        adapter.clearSelection();
        currentActionMode = null;
    }

    private void deleteChat() {
        try {
            showAlertDialog(context.getString(R.string.delete_group_dialog, "delete"),
                    new OnUserSelectionListener() {
                        @Override
                        public void onProceed() {
                            if (isPrivate) {
                                deleteSelectedDialogs(getChat());
                            } else {
                                deleteGroup();
                            }
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

//    private boolean isPrivate() {
//        final GroupChat data = getChat();
//        if (data.getQbDialog().getType() == QBDialogType.PRIVATE)
//            return true;
//        return false;
//    }

    private void finishAction() {
        if (currentActionMode != null) {
            currentActionMode.finish();
        }
    }

    private void deleteSelectedDialogs(final Kitty kitty) {
        final KittyViewModel model = new KittyViewModel(context);
        if (ConnectivityUtils.checkInternetConnection(context)) {
            try {
                finishAction();
//        final Collection<QBDialog> selectedDialogs = dialogsAdapter.getSelectedItems();
                ChatHelper.getInstance().deleteDialog(kitty.getQbDialog(), new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        System.out.println("QBDialog = >" + kitty.getQbDialog().getName() + " has deleted");
                        model.deleteKittyByQBDialogId(kitty.getQbDialog().getDialogId());
                        QbDialogHolder.getInstance().deleteDialogById(kitty.getQbDialog().getDialogId());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                    }
                });
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        } else {
            try {
                String id = KittyBeeContract.Kitties.QB_DIALOG_ID;
                if (kitty.getDialogId().length() > id.length()) {
                    String defaultId = kitty.getDialogId().substring(0, id.length());
                    if (defaultId.equals(id)) {
                        model.deleteKitty(kitty.getId());
                    }
                } else
                    model.updateDeleteFlag(kitty.getId());
            } catch (Exception e) {
                AppLog.e(TAG, e.getMessage(), e);
            }
        }

        startItemRemoveAnimation(kitty);
    }

    private void startItemRemoveAnimation(final Kitty kitty) {
        try {
            View view = kitty.getSelectedItemView();
            Animation anim = AnimationUtils.loadAnimation(context, R.anim.view_left_to_right);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    adapter.remove(kitty.getSelectedItemPosition());
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(anim);
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }


    private void deleteGroup() {
        try {
            AppLog.d(TAG, "selected size" + adapter.getSelectedItems().size());
            final Kitty data = getChat();
            ChatData chat = data.getGroup();
            final KittyViewModel model = new KittyViewModel(context);
            // option delete Group Chat
            if (data.getGroup() != null &&
                    data.getGroup().getIsAdmin() != null
                    && data.getGroup().getIsAdmin().equalsIgnoreCase("1")) {


                if (Utils.isValidString(data.getQbDialog().getDialogId())) {

                    ReqRefreshGroup deleteGroupObject = new ReqRefreshGroup();
                    deleteGroupObject.setDelete("1");
                    if (ConnectivityUtils.checkInternetConnection(context)) {
                        Call<ServerResponse> call = Singleton.getInstance()
                                .getRestOkClient().deleteGroup(chat.getGroupID(), deleteGroupObject);
                        call.enqueue(new Callback<ServerResponse>() {
                            @Override
                            public void onResponse(Call<ServerResponse> call,
                                                   Response<ServerResponse> response) {
                                if (response.code() == 200) {
                                    if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                        deleteSelectedDialogs(data);
                                    } else {
                                        if (Utils.isValidString(response.body().getMessage())) {
                                            showToast(response.body().getMessage());
                                        } else {
                                            showToast(R.string.server_error);
                                        }
                                    }
                                } else {
                                    if (Utils.isValidString(response.body().getMessage())) {
                                        showToast(response.body().getMessage());
                                    } else {
                                        showToast(R.string.server_error);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ServerResponse> call, Throwable t) {
                                showToast(R.string.server_error);
                            }
                        });
                    } else {
                        model.updateDeleteFlag(data.getId());
                        adapter.remove(data);
                    }
                }
            } else {
                showToast(R.string.delete_group_warning);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }

    }

    private void showToast(@StringRes int id) {
        finishAction();
        AlertDialogUtils.showSnackToast(ResourceUtils.getString(AppApplication.getInstance(), id), context);
    }

    private void showToast(String msg) {
        finishAction();
        AlertDialogUtils.showSnackToast(msg, context);
    }

    private void refreshChatData() {
        try {
            final Kitty data = getChat();
            ChatData chat = data.getGroup();
            finishAction();
            if (data.getGroup().getIsAdmin().equalsIgnoreCase("1")) {

                if (Utils.checkInternetConnection(context)) {

                    ReqRefreshGroup reqRefreshGroup = new ReqRefreshGroup();
                    reqRefreshGroup.setDelete("0");
                    Call<ServerResponse> call = Singleton.getInstance()
                            .getRestOkClient().refershGroup(chat.getGroupID(),
                                    reqRefreshGroup);
                    call.enqueue(new Callback<ServerResponse>() {
                        @Override
                        public void onResponse(Call<ServerResponse> call,
                                               Response<ServerResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().getResponse() == AppConstant.RESPONSE_SUCCESS) {
                                    if (Utils.isValidString(response.body().getMessage())) {
                                        showToast(response.body().getMessage());
                                    }
                                } else {
                                    if (Utils.isValidString(response.body().getMessage())) {
                                        showToast(response.body().getMessage());
                                    }
                                }
                            } else {
                                if (Utils.isValidString(response.body().getMessage())) {
                                    showToast(response.body().getMessage());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ServerResponse> call, Throwable t) {

                        }
                    });
                } else {
                    showToast(R.string.no_internet_available);
                }
            } else {
                showToast(R.string.refresh_group_warning);
            }
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private void showAlertDialog(String msg, final OnUserSelectionListener listener) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(msg);
            builder.setTitle("Alert");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onProceed();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    listener.onCancel();
                }
            });

            builder.create().show();
        } catch (Exception e) {
            AppLog.e(TAG, e.getMessage(), e);
        }
    }

    private Kitty getChat() {
        final ArrayList<Kitty> selectedDialogs = (ArrayList<Kitty>) adapter.getSelectedItems();
        System.out.println("selectedDialogs = >" + selectedDialogs.size());
        return selectedDialogs.get(0);
    }
}
