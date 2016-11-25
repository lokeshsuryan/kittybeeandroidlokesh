package com.kittyapplication.sync;

import android.os.Bundle;

import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.core.CoreApp;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.model.ReqRefreshGroup;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.APIManager;
import com.kittyapplication.sqlitedb.DBQueryHandler.OnQueryHandlerListener;
import com.kittyapplication.ui.viewmodel.KittyViewModel;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by MIT on 10/25/2016.
 */
public class SyncGroupOperation {
    private static final String TAG = SyncGroupOperation.class.getSimpleName();
    private KittyViewModel viewModel;
    private APIManager apiManager;

    public interface OnSyncComplete {
        void onCompleted(boolean hasData);
    }

    public SyncGroupOperation() {
        this.viewModel = new KittyViewModel(CoreApp.getInstance());
        this.apiManager = APIManager.getInstance();
    }

    public void syncDialogs(final OnSyncComplete syncComplete) {
        apiManager.loadDialog(new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> qbDialogs, Bundle bundle) {
                if (qbDialogs.size() == 0) {
                    if (syncComplete != null) {
                        syncComplete.onCompleted(false);
                    }
                } else {
                    saveDialogs(qbDialogs, syncComplete);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                AppLog.e(TAG, e.getMessage(), e);
                if (syncComplete != null) {
                    syncComplete.onCompleted(false);
                }
            }
        });
    }

    public void syncGroups(final OnSyncComplete syncComplete) {
        apiManager.getGroups(new Callback<ServerResponse<List<ChatData>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<ChatData>>> call, Response<ServerResponse<List<ChatData>>> response) {
                if (response.code() == 200 || response.code() == 201) {
                    ArrayList<ChatData> groups = (ArrayList<ChatData>) response.body().getData();
                    saveGroups(groups, syncComplete);
                } else {
                    if (syncComplete != null)
                        syncComplete.onCompleted(false);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse<List<ChatData>>> call, Throwable t) {
                AppLog.e(TAG, t.getMessage());
                if (syncComplete != null)
                    syncComplete.onCompleted(false);
            }
        });
    }


    private void saveDialogs(ArrayList<QBDialog> qbDialogs, OnSyncComplete syncComplete) {

        if (Utils.isValidList(qbDialogs)) {
            for (QBDialog qbDialog : qbDialogs) {
                //            Kitty kitty = new Kitty();
                //            kitty.setQbDialog(qbDialog);
                //            viewModel.saveKitty(kitty);
                viewModel.saveQBDialog(qbDialog);
            }
            if (syncComplete != null)
                syncComplete.onCompleted(true);
        } else {
            if (syncComplete != null)
                syncComplete.onCompleted(false);
        }
    }

    public void saveGroups(ArrayList<ChatData> chatDatas, OnSyncComplete syncComplete) {

        if (Utils.isValidList(chatDatas)) {
            for (ChatData chatData : chatDatas) {
                //            Kitty kitty = new Kitty();
                //            kitty.setGroup(chatData);
                //            viewModel.saveKitty(kitty);
                viewModel.saveGroup(chatData);
            }
            if (syncComplete != null)
                syncComplete.onCompleted(true);
        } else {
            if (syncComplete != null)
                syncComplete.onCompleted(false);
        }
    }

    public void syncDeletedGroup() {
        viewModel.fetchDeletedKitties(new OnQueryHandlerListener<ArrayList<Kitty>>() {
            @Override
            public void onResult(ArrayList<Kitty> result) {
                for (final Kitty kitty : result) {
                    ReqRefreshGroup deletedGroup = new ReqRefreshGroup();
                    deletedGroup.setDelete("1");
                    ChatData group = kitty.getGroup();
                    apiManager.deleteGroup(group.getGroupID(), deletedGroup, new Callback<ServerResponse>() {
                        @Override
                        public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                            if (response.code() == 200 || response.code() == 201) {
                                System.out.println("Deleted dialog" + kitty.getQbDialog().getName());
                                viewModel.deleteKitty(kitty.getId());
                                deleteDialog(kitty.getQbDialog());
                            }
                        }

                        @Override
                        public void onFailure(Call<ServerResponse> call, Throwable t) {

                        }
                    });
                }
            }
        });
    }

    private void deleteDialog(QBDialog dialog) {
        System.out.println(TAG + " deleteDialog: " + dialog.getName());
        ChatHelper.getInstance().deleteDialog(dialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                System.out.println(TAG + " onSuccess = > deleted ");

                // TODO Delete all messages sent by this dialog
                // from loacal db
            }

            @Override
            public void onError(QBResponseException e) {
                System.out.println(TAG + " onError = > " + e.getMessage());
            }
        });
    }
}
