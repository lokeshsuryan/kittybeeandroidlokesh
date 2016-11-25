package com.kittyapplication.rest;


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
import com.kittyapplication.model.OTPResponseDao;
import com.kittyapplication.model.OfflineDao;
import com.kittyapplication.model.ParticipantDao;
import com.kittyapplication.model.PromotionalDao;
import com.kittyapplication.model.RegisterRequestDao;
import com.kittyapplication.model.RegisterResponseDao;
import com.kittyapplication.model.ReqAddMember;
import com.kittyapplication.model.ReqChangeGroupName;
import com.kittyapplication.model.ReqDeleteMember;
import com.kittyapplication.model.ReqGiveRights;
import com.kittyapplication.model.ReqInvite;
import com.kittyapplication.model.ReqRefreshGroup;
import com.kittyapplication.model.ServerRequest;
import com.kittyapplication.model.ServerRequestPopup;
import com.kittyapplication.model.ServerResponse;
import com.kittyapplication.model.SignUpDao;
import com.kittyapplication.model.SummaryListDao;
import com.kittyapplication.model.VenueResponseDao;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Scorpion .
 */
public interface RestClient {

    @POST("start")
    Call<SignUpDao> login(@Body ServerRequest request);

    @POST("otp")
    Call<OTPResponseDao> otp(@Body ServerRequest request);

    @POST("register")
    Call<RegisterResponseDao> register(@Body RegisterRequestDao request);

    @POST("fbLogin")
    Call<RegisterResponseDao> fbLogin(@Body RegisterRequestDao request);

    @POST("gmailLogin")
    Call<RegisterResponseDao> gmailLogin(@Body RegisterRequestDao request);

    @POST("getGroup/{userId}")
    Call<ServerResponse<List<ChatData>>> getGroupChatData(@Path("userId") String userId);

    @POST("getArticle")
    Call<ServerResponse<ArticleDao>> getPopUpData(@Body ServerRequestPopup data);


    @POST("promotion/{id}")
    Call<ServerResponse<List<PromotionalDao>>> getPromotionalData(@Path("id") String id);


    @POST("contact/{userId}")
    Call<ServerResponse<List<ContactDao>>> syncContactToServer(@Path("userId") String userId, @Body HashMap<String, String> hashMapContact);

    @GET("profile")
    Call<ServerResponse<MyProfileResponseDao>> profile(@Query("id") String userId);

    @POST("editProfile/{userId}")
    Call<ServerResponse<MyProfileResponseDao>> editProfile(@Path("userId") String userId, @Body MyProfileRequestDao request);

    @POST("getNotification/{userId}")
    Call<ServerResponse<List<NotificationDao>>> getNotification(@Path("userId") String userId);

    @POST("deleteNotification")
    Call<ServerResponse<List<NotificationDao>>> deleteNotification(@Body NotificationDao request);

    @POST("getBill")
    Call<ServerResponse<BillDao>> getBill(@Body BillDao request);

    @POST("addBill")
    Call<ServerResponse<BillDao>> addBill(@Body BillDao request);

    @POST("editBill")
    Call<ServerResponse<BillDao>> editBill(@Body BillDao request);

    @POST("addAttendance")
    Call<ServerResponse> addAttendance(@Body AddAttendanceDao request);

    @POST("banner")
    Call<ServerResponse<List<BannerDao>>> getBanner();

    @POST("getAttendance")
    Call<ServerResponse<List<AttendanceDataDao>>> getAttendance(@Body NotificationDao request);

    @POST("addGroup/{userId}")
    Call<ServerResponse<OfflineDao>> addGroup(@Path("userId") String id, @Body CreateGroup obj);

    @POST("createKitty")
    Call<ServerResponse<OfflineDao>> createKitty(@Body CreateGroup obj);

    @POST("notes")
    Call<ServerResponse<List<NotesResponseDao>>> getNotes(@Body NotesRequestDao request);

    @POST("addNote")
    Call<ServerResponse<List<NotesResponseDao>>> addNote(@Body NotesRequestDao request);

    @POST("getVenue/{venueId}")
    Call<ServerResponse<List<VenueResponseDao>>> getVenue(@Path("venueId") String id);

    @POST("addVenue")
    Call<ServerResponse<List<VenueResponseDao>>> addVenue(@Body VenueResponseDao request);

    @POST("editVenue/{venueId}")
    Call<ServerResponse<List<VenueResponseDao>>> editVenue(@Path("venueId") String id, @Body VenueResponseDao requests);

    @POST("dairies/{diaryId}")
    Call<ServerResponse<DiaryResponseDao>> getDairies(@Path("diaryId") String id);

    @POST("hostList/{groupId}")
    Call<ServerResponse<List<SummaryListDao>>> hostList(@Path("groupId") String id);

    @POST("addDairies")
    Call<ServerResponse<List<SummaryListDao>>> addDairies(@Body DiarySubmitDao request);


    @POST("getRule/{groupId}")
    Call<ServerResponse<List<CreateGroup>>> getRuleData(@Path("groupId") String groupId);

    @POST("editRule/{groupID}")
    Call<ServerResponse<List<CreateGroup>>> updateRuleData(@Path("groupID") String userId, @Body CreateGroup group);

    @POST("selectHost/{userId}")
    Call<ServerResponse<OfflineDao>> selectHost(@Path("userId") String userId, @Body DiaryHostSelectionDao request);

    @POST("calander/{userId}")
    Call<ServerResponse<List<CalendarDao>>> calander(@Path("userId") String userID);


    @POST("setting/{groupId}")
    Call<ServerResponse<ParticipantDao>> getParticipant(@Path("groupId") String groupId);

    @POST("addMember")
    Call<ServerResponse<OfflineDao>> addMember(@Body ReqAddMember addMember);


    @POST("deleteGroup/{groupId}")
    Call<ServerResponse> refershGroup(@Path("groupId") String groupId,
                                      @Body ReqRefreshGroup addMember);

    @POST("deleteMember")
    Call<ServerResponse<OfflineDao>> deleteMember(@Body ReqDeleteMember addMember);


    @POST("settingUpdate/{groupId}")
    Call<ServerResponse> chnageGroupName(@Path("groupId") String groupId,
                                         @Body ReqChangeGroupName reqObj);

    @POST("invite")
    Call<ServerResponse> inviteMember(
            @Body ReqInvite reqObj);

//    @POST("deleteMember/{groupId}")
//    Call<ServerResponse> refreshGroup(@Path("groupId") String groupId, @Body ReqDeleteMember addMember);


    @POST("giveRight")
    Call<ServerResponse> giveRights(
            @Body ReqGiveRights reqObj);


    @POST("deleteGroup/{groupId}")
    Call<ServerResponse> deleteGroup(@Path("groupId") String groupId,
                                     @Body ReqRefreshGroup reqObj);


    @POST("events")
    Call<ServerResponse<List<HedsUpData>>> getHedsUpData();


    @POST("getMember/{groupId}")
    Call<ServerResponse<List<ContactDao>>> getMembersData(@Path("groupId") String groupId);


    @POST("addMemberList")
    Call<ServerResponse<List<ContactDao>>> addMemberList(@Body HashMap<String, String> hashMap);


    @POST("changeHost/{groupId}")
    Call<ServerResponse<OfflineDao>> changeHost(@Path("groupId") String groupId,
                                                @Body ChangeHostDao dao);

    @POST("getOffLine/{userId}")
    Call<ServerResponse<List<OfflineDao>>> getOfflineSupportData(@Path("userId") String userId);

    @POST("getCities")
    Call<ServerResponse<List<CityDao>>> getCity();

    @POST("version")
    Call<ServerResponse> getUpdatedVersion();
}
