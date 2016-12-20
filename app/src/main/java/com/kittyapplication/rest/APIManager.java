package com.kittyapplication.rest;

import com.kittyapplication.AppApplication;
import com.kittyapplication.R;
import com.kittyapplication.chat.utils.chat.ChatHelper;
import com.kittyapplication.core.CoreApp;
import com.kittyapplication.core.utils.ConnectivityUtils;
import com.kittyapplication.core.utils.ResourceUtils;
import com.kittyapplication.core.utils.Toaster;
import com.kittyapplication.model.AddAttendanceDao;
import com.kittyapplication.model.ArticleDao;
import com.kittyapplication.model.AttendanceDataDao;
import com.kittyapplication.model.BannerDao;
import com.kittyapplication.model.BillDao;
import com.kittyapplication.model.CalendarDao;
import com.kittyapplication.model.ChangeHostDao;
import com.kittyapplication.model.ChatData;
import com.kittyapplication.model.CityDao;
import com.kittyapplication.model.ContactDao;
import com.kittyapplication.model.CreateGroup;
import com.kittyapplication.model.DiaryHostSelectionDao;
import com.kittyapplication.model.DiaryResponseDao;
import com.kittyapplication.model.DiarySubmitDao;
import com.kittyapplication.model.HedsUpData;
import com.kittyapplication.model.MyProfileRequestDao;
import com.kittyapplication.model.MyProfileResponseDao;
import com.kittyapplication.model.NotesRequestDao;
import com.kittyapplication.model.NotesResponseDao;
import com.kittyapplication.model.NotificationDao;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ParticipantDao;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.ReqAddMember;
import com.kittyapplication.model.ReqChangeGroupName;
import com.kittyapplication.model.ReqDeleteMember;
import com.kittyapplication.model.ReqGiveRights;
import com.kittyapplication.model.ReqInvite;
import com.kittyapplication.model.ReqRefreshGroup;
import com.kittyapplication.model.ServerRequestPopup;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.model.VenueResponseDao;
import com.kittyapplication.ui.executor.ExecutorThread;
import com.kittyapplication.utils.LoginUserPrefHolder;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by MIT on 10/24/2016.
 */
public class APIManager {
    private static APIManager instance = new APIManager();
    private RestClient restClient;
    private RegisterResponseDao user;
    private ExecutorThread executorThread;

    public interface APICallback<T> {
        void onSuccess(T response);
    }

    public static APIManager getInstance() {
        if (instance == null) {
            instance = new APIManager();
        }
        return instance;
    }

    private APIManager() {
        restClient = Singleton.getInstance().getRestAuthenticatedOkClient();
        user = LoginUserPrefHolder.getInstance().getUser();
        executorThread = new ExecutorThread();
    }

    public void getGroups(Callback<ServerResponse<List<ChatData>>> callback) {
        Call<ServerResponse<List<ChatData>>> call = restClient.getGroupChatData(user.getUserID());
        call.enqueue(callback);
    }

    public void getPopUpData(ServerRequestPopup request) {
        Call<ServerResponse<ArticleDao>> call = restClient.getPopUpData(request);
        call.enqueue(new Callback<ServerResponse<ArticleDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<ArticleDao>> call, Response<ServerResponse<ArticleDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<ArticleDao>> call, Throwable t) {

            }
        });
    }

    /*public void getPromotionalData(String id) {
        Call<ServerResponse<List<PromotionalDao>>> call = restClient.getPromotionalData(id);
        call.enqueue(new Callback<ServerResponse<List<PromotionalDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<PromotionalDao>>> call, Response<ServerResponse<List<PromotionalDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<PromotionalDao>>> call, Throwable t) {

            }
        });
    }*/

    public void getProfile() {
        Call<ServerResponse<MyProfileResponseDao>> call = restClient.profile(user.getUserID());
        call.enqueue(new Callback<ServerResponse<MyProfileResponseDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<MyProfileResponseDao>> call, Response<ServerResponse<MyProfileResponseDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<MyProfileResponseDao>> call, Throwable t) {

            }
        });
    }

    public void editProfile(MyProfileRequestDao responseDao) {
        Call<ServerResponse<MyProfileResponseDao>> call = restClient.editProfile(user.getUserID(), responseDao);
        call.enqueue(new Callback<ServerResponse<MyProfileResponseDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<MyProfileResponseDao>> call, Response<ServerResponse<MyProfileResponseDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<MyProfileResponseDao>> call, Throwable t) {

            }
        });
    }

    public void getNotification() {
        Call<ServerResponse<List<NotificationDao>>> call = restClient.getNotification(user.getUserID());
        call.enqueue(new Callback<ServerResponse<List<NotificationDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<NotificationDao>>> call, Response<ServerResponse<List<NotificationDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<NotificationDao>>> call, Throwable t) {

            }
        });
    }

    public void deleteNotification(NotificationDao notificationDao) {
        Call<ServerResponse<List<NotificationDao>>> call = restClient.deleteNotification(notificationDao);
        call.enqueue(new Callback<ServerResponse<List<NotificationDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<NotificationDao>>> call, Response<ServerResponse<List<NotificationDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<NotificationDao>>> call, Throwable t) {

            }
        });
    }

    public void getBill(BillDao billDao) {
        Call<ServerResponse<BillDao>> call = restClient.getBill(billDao);
        call.enqueue(new Callback<ServerResponse<BillDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<BillDao>> call, Response<ServerResponse<BillDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<BillDao>> call, Throwable t) {

            }
        });
    }

    public void addBill(BillDao billDao) {
        Call<ServerResponse<BillDao>> call = restClient.addBill(billDao);
        call.enqueue(new Callback<ServerResponse<BillDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<BillDao>> call, Response<ServerResponse<BillDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<BillDao>> call, Throwable t) {

            }
        });
    }

    public void editBill(BillDao billDao) {
        Call<ServerResponse<BillDao>> call = restClient.editBill(billDao);
        call.enqueue(new Callback<ServerResponse<BillDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<BillDao>> call, Response<ServerResponse<BillDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<BillDao>> call, Throwable t) {

            }
        });
    }

    public void addAttendance(AddAttendanceDao attendanceDao) {
        Call<ServerResponse> call = restClient.addAttendance(attendanceDao);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    public void getBanner() {
        Call<ServerResponse<List<BannerDao>>> call = restClient.getBanner();
        call.enqueue(new Callback<ServerResponse<List<BannerDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<BannerDao>>> call, Response<ServerResponse<List<BannerDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<BannerDao>>> call, Throwable t) {

            }
        });
    }

    public void getAttendance(NotificationDao notificationDao) {
        Call<ServerResponse<List<AttendanceDataDao>>> call = restClient.getAttendance(notificationDao);
        call.enqueue(new Callback<ServerResponse<List<AttendanceDataDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<AttendanceDataDao>>> call, Response<ServerResponse<List<AttendanceDataDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<AttendanceDataDao>>> call, Throwable t) {

            }
        });
    }

    public void addGroup(CreateGroup group) {
        Call<ServerResponse<OfflineDao>> call = restClient.addGroup(user.getUserID(), group);
        call.enqueue(new Callback<ServerResponse<OfflineDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {

            }
        });
    }

    public void createKitty(CreateGroup group) {
        Call<ServerResponse<OfflineDao>> call = restClient.createKitty(group);
        call.enqueue(new Callback<ServerResponse<OfflineDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {

            }
        });
    }

    public void getNotes(NotesRequestDao requestDao) {
        Call<ServerResponse<List<NotesResponseDao>>> call = restClient.getNotes(requestDao);
        call.enqueue(new Callback<ServerResponse<List<NotesResponseDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<NotesResponseDao>>> call, Response<ServerResponse<List<NotesResponseDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<NotesResponseDao>>> call, Throwable t) {

            }
        });
    }

    public void addNote(NotesRequestDao requestDao) {
        Call<ServerResponse<List<NotesResponseDao>>> call = restClient.getNotes(requestDao);
        call.enqueue(new Callback<ServerResponse<List<NotesResponseDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<NotesResponseDao>>> call, Response<ServerResponse<List<NotesResponseDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<NotesResponseDao>>> call, Throwable t) {

            }
        });
    }

    public void getVenue(String venueId) {
        Call<ServerResponse<List<VenueResponseDao>>> call = restClient.getVenue(venueId);
        call.enqueue(new Callback<ServerResponse<List<VenueResponseDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<VenueResponseDao>>> call, Response<ServerResponse<List<VenueResponseDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<VenueResponseDao>>> call, Throwable t) {

            }
        });
    }

    public void addVenue(VenueResponseDao responseDao) {
        Call<ServerResponse<List<VenueResponseDao>>> call = restClient.addVenue(responseDao);
        call.enqueue(new Callback<ServerResponse<List<VenueResponseDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<VenueResponseDao>>> call, Response<ServerResponse<List<VenueResponseDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<VenueResponseDao>>> call, Throwable t) {

            }
        });
    }

    public void editVenue(String venueId, VenueResponseDao responseDao) {
        Call<ServerResponse<List<VenueResponseDao>>> call = restClient.editVenue(venueId, responseDao);
        call.enqueue(new Callback<ServerResponse<List<VenueResponseDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<VenueResponseDao>>> call, Response<ServerResponse<List<VenueResponseDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<VenueResponseDao>>> call, Throwable t) {

            }
        });
    }

    public void getDiaries(String diaryId) {
        Call<ServerResponse<DiaryResponseDao>> call = restClient.getDairies(diaryId);
        call.enqueue(new Callback<ServerResponse<DiaryResponseDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<DiaryResponseDao>> call, Response<ServerResponse<DiaryResponseDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<DiaryResponseDao>> call, Throwable t) {

            }
        });
    }

    public void addDiaries(DiarySubmitDao diarySubmitDao) {
        Call<ServerResponse<List<SummaryListDao>>> call = restClient.addDairies(diarySubmitDao);
        call.enqueue(new Callback<ServerResponse<List<SummaryListDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<SummaryListDao>>> call, Response<ServerResponse<List<SummaryListDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<SummaryListDao>>> call, Throwable t) {

            }
        });
    }

    public void getHotList(String groupId) {
        Call<ServerResponse<List<SummaryListDao>>> call = restClient.hostList(groupId);
        call.enqueue(new Callback<ServerResponse<List<SummaryListDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<SummaryListDao>>> call, Response<ServerResponse<List<SummaryListDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<SummaryListDao>>> call, Throwable t) {

            }
        });
    }

    public void getRuleData(String groupId) {
        Call<ServerResponse<List<CreateGroup>>> call = restClient.getRuleData(groupId);
        call.enqueue(new Callback<ServerResponse<List<CreateGroup>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<CreateGroup>>> call, Response<ServerResponse<List<CreateGroup>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<CreateGroup>>> call, Throwable t) {

            }
        });
    }

    public void updateRuleData(String groupId, CreateGroup group) {
        Call<ServerResponse<List<CreateGroup>>> call = restClient.updateRuleData(groupId, group);
        call.enqueue(new Callback<ServerResponse<List<CreateGroup>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<CreateGroup>>> call, Response<ServerResponse<List<CreateGroup>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<CreateGroup>>> call, Throwable t) {

            }
        });
    }

    public void selectHost(DiaryHostSelectionDao hostSelectionDao) {
        Call<ServerResponse<OfflineDao>> call = restClient.selectHost(user.getUserID(), hostSelectionDao);
        call.enqueue(new Callback<ServerResponse<OfflineDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {

            }
        });
    }

    public void getParticipant(String groupId) {
        Call<ServerResponse<ParticipantDao>> call = restClient.getParticipant(groupId);
        call.enqueue(new Callback<ServerResponse<ParticipantDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<ParticipantDao>> call, Response<ServerResponse<ParticipantDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<ParticipantDao>> call, Throwable t) {

            }
        });
    }

    public void calender() {
        Call<ServerResponse<List<CalendarDao>>> call = restClient.calander(user.getUserID());
        call.enqueue(new Callback<ServerResponse<List<CalendarDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<CalendarDao>>> call, Response<ServerResponse<List<CalendarDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<CalendarDao>>> call, Throwable t) {

            }
        });
    }

    public void addMember(ReqAddMember member) {
        Call<ServerResponse<OfflineDao>> call = restClient.addMember(member);
        call.enqueue(new Callback<ServerResponse<OfflineDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {

            }
        });
    }

    public void deleteMember(ReqDeleteMember member) {
        Call<ServerResponse<OfflineDao>> call = restClient.deleteMember(member);
        call.enqueue(new Callback<ServerResponse<OfflineDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {

            }
        });
    }

    public void refreshGroup(String groupId, ReqRefreshGroup refreshGroup) {
        Call<ServerResponse> call = restClient.refershGroup(groupId, refreshGroup);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    public void changeGroupName(String groupId, ReqChangeGroupName groupName) {
        Call<ServerResponse> call = restClient.chnageGroupName(groupId, groupName);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    public void inviteMember(ReqInvite reqInvite) {
        Call<ServerResponse> call = restClient.inviteMember(reqInvite);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    public void giveRights(ReqGiveRights giveRights) {
        Call<ServerResponse> call = restClient.giveRights(giveRights);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }

    public void deleteGroup(String groupId, ReqRefreshGroup group, Callback<ServerResponse> callback) {
        Call<ServerResponse> call = restClient.deleteGroup(groupId, group);
        call.enqueue(callback);
    }

    public void getHadsUp() {
        Call<ServerResponse<List<HedsUpData>>> call = restClient.getHedsUpData();
        call.enqueue(new Callback<ServerResponse<List<HedsUpData>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<HedsUpData>>> call, Response<ServerResponse<List<HedsUpData>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<HedsUpData>>> call, Throwable t) {

            }
        });
    }

    public void getMembers(String groupId) {
        Call<ServerResponse<List<ContactDao>>> call = restClient.getMembersData(groupId);
        call.enqueue(new Callback<ServerResponse<List<ContactDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<ContactDao>>> call, Response<ServerResponse<List<ContactDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<ContactDao>>> call, Throwable t) {

            }
        });
    }

    public void addMembers(HashMap<String, String> members) {
        Call<ServerResponse<List<ContactDao>>> call = restClient.addMemberList(members);
        call.enqueue(new Callback<ServerResponse<List<ContactDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<ContactDao>>> call, Response<ServerResponse<List<ContactDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<ContactDao>>> call, Throwable t) {

            }
        });
    }

    public void changHost(String groupId, ChangeHostDao hostDao) {
        Call<ServerResponse<OfflineDao>> call = restClient.changeHost(groupId, hostDao);
        call.enqueue(new Callback<ServerResponse<OfflineDao>>() {
            @Override
            public void onResponse(Call<ServerResponse<OfflineDao>> call, Response<ServerResponse<OfflineDao>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<OfflineDao>> call, Throwable t) {

            }
        });
    }

    public void getOfflineSupport() {
        Call<ServerResponse<List<OfflineDao>>> call = restClient.getOfflineSupportData(user.getUserID());
        call.enqueue(new Callback<ServerResponse<List<OfflineDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<OfflineDao>>> call, Response<ServerResponse<List<OfflineDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<OfflineDao>>> call, Throwable t) {

            }
        });
    }

    public void getCities() {
        Call<ServerResponse<List<CityDao>>> call = restClient.getCity();
        call.enqueue(new Callback<ServerResponse<List<CityDao>>>() {
            @Override
            public void onResponse(Call<ServerResponse<List<CityDao>>> call, Response<ServerResponse<List<CityDao>>> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse<List<CityDao>>> call, Throwable t) {

            }
        });
    }

    public void loadDialog(final int skip, final int limit, final QBEntityCallback<ArrayList<QBChatDialog>> callback) {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            new ExecutorThread().startExecutor().postTask(new Runnable() {
                @Override
                public void run() {
                    QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();
                    ChatHelper.getInstance().getDialogs(requestGetBuilder, callback, skip, limit);
                }
            });
        } else {
            Toaster.longToast(ResourceUtils.getString(AppApplication.getInstance(), R.string.no_internet_available));
        }
    }

    public void loadUser(final QBChatDialog qbDialog, final QBEntityCallback<ArrayList<QBUser>> callback) {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
            new ExecutorThread().startExecutor().postTask(new Runnable() {
                @Override
                public void run() {
                    ChatHelper.getInstance().getUsersFromDialog(qbDialog, callback);
                }
            });
        } else {
            Toaster.longToast(ResourceUtils.getString(AppApplication.getInstance(), R.string.no_internet_available));
        }
    }

    public void loadChatHistory(final QBChatDialog qbDialog, final int pageNo, final QBEntityCallback<ArrayList<QBChatMessage>> callback) {
        if (ConnectivityUtils.checkInternetConnection(CoreApp.getInstance())) {
//            new ExecutorThread().startExecutor().postTask(new Runnable() {
//                @Override
//                public void run() {
            ChatHelper.getInstance().loadChatHistory(qbDialog, pageNo, callback);
//                }
//            });
        } else {
            Toaster.longToast(ResourceUtils.getString(AppApplication.getInstance(), R.string.no_internet_available));
        }
    }
}
