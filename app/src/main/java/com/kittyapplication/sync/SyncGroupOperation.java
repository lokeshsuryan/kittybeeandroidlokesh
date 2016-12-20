package com.kittyapplication.sync;

import android.os.Bundle;

import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.core.CoreApp;
import com.kittyapplication.core.utils.StringUtils;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.Kitty;
import com.kittyapplication.model.ReqRefreshGroup;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.rest.APIManager;
import com.kittyapplication.sqlitedb.DBQueryHandler.OnQueryHandlerListener;
import com.kittyapplication.sync.callback.DataSyncListener;
import com.kittyapplication.ui.executor.BackgroundExecutorThread;
import com.kittyapplication.ui.executor.Interactor;
import com.kittyapplication.ui.viewmodel.ChatMemberViewModel;
import com.kittyapplication.ui.viewmodel.KittyViewModel;
import com.kittyapplication.ui.viewmodel.MessageViewModel;
import com.kittyapplication.utils.AppLog;
import com.kittyapplication.utils.Utils;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

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

    public SyncGroupOperation() {
        this.viewModel = new KittyViewModel(CoreApp.getInstance());
        this.apiManager = APIManager.getInstance();
    }

    public void syncDialogs(int skip, int limit, final DataSyncListener listener) {
        apiManager.loadDialog(skip, limit, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbDialogs, Bundle bundle) {
                StringUtils.printBundle(bundle);
                AppLog.e(TAG, "syncDialogs onSuccess: " + qbDialogs.size());
                if (qbDialogs.size() == 0) {
                    if (listener != null)
                        listener.onCompleted(qbDialogs.size());
                    return;
                }
                saveDialogs(qbDialogs, listener);
            }

            @Override
            public void onError(QBResponseException e) {
                AppLog.e(TAG, e.getMessage(), e);
                if (listener != null) {
                    listener.onCompleted(0);
                }
            }
        });
    }

    public void syncGroups(final DataSyncListener listener) {
        apiManager.getGroups(new Callback<ServerResponse<List<ChatData>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<ChatData>>> call, Response<ServerResponse<List<ChatData>>> response) {
                if (response.code() == 200 || response.code() == 201) {
                    ArrayList<ChatData> groups = (ArrayList<ChatData>) response.body().getData();
                    if (groups != null && !groups.isEmpty())
                        saveGroups(groups, listener);
                    else if (listener != null)
                        listener.onCompleted(0);
                } else {
                    if (listener != null)
                        listener.onCompleted(0);
                }
            }

            @Override
            public void onFailure(Call<ServerResponse<List<ChatData>>> call, Throwable t) {
                AppLog.e(TAG, t.getMessage());
                if (listener != null)
                    listener.onCompleted(0);
            }
        });
    }


    private void saveDialogs(ArrayList<QBChatDialog> qbDialogs, DataSyncListener listener) {
        for (QBChatDialog qbDialog : qbDialogs) {
//            Kitty kitty = new Kitty();
//            kitty.setQbDialog(qbDialog);
//            viewModel.saveKitty(kitty);
//            saveMember(qbDialog);
            viewModel.saveQBDialog(qbDialog);
        }
        if (listener != null)
            listener.onCompleted(qbDialogs.size());
    }

    public void saveGroups(ArrayList<ChatData> chatDatas, DataSyncListener listener) {
        for (ChatData chatData : chatDatas) {
//            Kitty kitty = new Kitty();
//            kitty.setGroup(chatData);
//            viewModel.saveKitty(kitty);
            viewModel.saveGroup(chatData);
        }
        if (listener != null)
            listener.onCompleted(chatDatas.size());
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

    private void deleteDialog(QBChatDialog dialog) {
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

    private void saveMember(final QBChatDialog qbDialog) {
        apiManager.loadUser(qbDialog, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                for (QBUser qbUser : qbUsers) {
                    if (qbDialog.getType() != QBDialogType.PRIVATE) {
                        ChatMemberViewModel memberViewModel = new MessageViewModel(CoreApp.getInstance());
                        memberViewModel.addMember(memberViewModel.getMember(qbUser));
                        viewModel.saveQBDialog(qbDialog);
                    } else {
                        saveMember(qbUser, qbDialog);
                    }
                }
            }

            @Override
            public void onError(QBResponseException e) {
            }
        });
    }

    private void saveMember(final QBUser user, final QBChatDialog qbDialog) {
        BackgroundExecutorThread backgroundExecutorThread = new BackgroundExecutorThread();
        backgroundExecutorThread.execute(new Interactor() {
            @Override
            public void execute() {
                if (user.getPhone() != null && user.getPhone().length() > 0) {
                    user.setFullName(Utils.getDisplayNameByPhoneNumber(CoreApp.getInstance(), user.getPhone()));
                }
            }
        }, new Interactor.OnExecutionFinishListener() {
            @Override
            public void onFinish() {
                ChatMemberViewModel memberViewModel = new MessageViewModel(CoreApp.getInstance());
                memberViewModel.addMember(memberViewModel.getMember(user));
                qbDialog.setName(user.getFullName());
                viewModel.saveQBDialog(qbDialog);
            }
        });
    }
}
