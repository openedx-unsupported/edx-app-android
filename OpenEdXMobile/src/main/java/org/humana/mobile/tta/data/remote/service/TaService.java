package org.humana.mobile.tta.data.remote.service;

import com.google.inject.Inject;
import com.google.inject.Provider;

import org.humana.mobile.http.constants.ApiConstants;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.NotificationResponse;
import org.humana.mobile.tta.data.local.db.table.CalendarEvents;
import org.humana.mobile.tta.data.local.db.table.Content;
import org.humana.mobile.tta.data.local.db.table.ContentStatus;
import org.humana.mobile.tta.data.local.db.table.Feed;
import org.humana.mobile.tta.data.local.db.table.Notification;
import org.humana.mobile.tta.data.local.db.table.Period;
import org.humana.mobile.tta.data.local.db.table.Program;
import org.humana.mobile.tta.data.local.db.table.Section;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.local.db.table.UnitStatus;
import org.humana.mobile.tta.data.model.CountResponse;
import org.humana.mobile.tta.data.model.CourseProgram;
import org.humana.mobile.tta.data.model.StatusResponse;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.data.model.UpdateResponse;
import org.humana.mobile.tta.data.model.agenda.AgendaList;
import org.humana.mobile.tta.data.model.content.BookmarkResponse;
import org.humana.mobile.tta.data.model.content.CertificateStatusResponse;
import org.humana.mobile.tta.data.model.content.MyCertificatesResponse;
import org.humana.mobile.tta.data.model.content.TotalLikeResponse;
import org.humana.mobile.tta.data.model.feed.SuggestedUser;
import org.humana.mobile.tta.data.model.library.CollectionConfigResponse;
import org.humana.mobile.tta.data.model.library.CollectionItemsResponse;
import org.humana.mobile.tta.data.model.library.ConfigModifiedDateResponse;
import org.humana.mobile.tta.data.model.profile.ChangePasswordResponse;
import org.humana.mobile.tta.data.model.profile.FeedbackResponse;
import org.humana.mobile.tta.data.model.profile.FollowStatus;
import org.humana.mobile.tta.data.local.db.table.CurricullamModel;
import org.humana.mobile.tta.data.model.program.NotificationCountResponse;
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramUser;
import org.humana.mobile.tta.data.model.program.UnitConfiguration;
import org.humana.mobile.tta.data.model.program.UnitPublish;
import org.humana.mobile.tta.data.model.search.SearchFilter;

import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

//TaService
public interface TaService {

    class TaProvider implements Provider<TaService> {

        @Inject
        private Retrofit retrofit;

        @Override
        public TaService get() {
            return retrofit.create(TaService.class);
        }
    }

    @GET(ApiConstants.URL_MX_GET_COLLECTION_CONFIG)
    Call<CollectionConfigResponse> getCollectionConfig();

    @GET(ApiConstants.URL_MX_GET_CONFIG_MODIFIED_DATE)
    Call<ConfigModifiedDateResponse> getConfigModifiedDate();

    @GET(ApiConstants.URL_MX_GET_COLLECTION_ITEMS)
    Call<List<CollectionItemsResponse>> getCollectionItems(
            @Query(Constants.KEY_LIST_IDS) String commaSeparatedListIds,
            @Query(Constants.KEY_SKIP) int skip,
            @Query(Constants.KEY_TAKE) int take
    );

    @GET(ApiConstants.URL_MX_GET_STATE_AGENDA_COUNT)
    Call<List<AgendaList>> getStateAgendaCount();

    @GET(ApiConstants.URL_MX_GET_MY_AGENDA_COUNT)
    Call<AgendaList> getMyAgendaCount();

    @GET(ApiConstants.URL_MX_GET_MY_AGENDA_CONTENT)
    Call<List<Content>> getMyAgendaContent(@Query(Constants.KEY_SOURCE_ID) long sourceId);

    @GET(ApiConstants.URL_MX_GET_STATE_AGENDA_CONTENT)
    Call<List<Content>> getStateAgendaContent(@Query(Constants.KEY_SOURCE_ID) long sourceId,
                                              @Query(Constants.KEY_LIST_ID) long list_id);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_SET_BOOKMARK)
    Call<BookmarkResponse> setBookmark(@FieldMap Map<String, Long> parameters);

    @GET(ApiConstants.URL_MX_IS_CONTENT_MY_AGENDA)
    Call<StatusResponse> isContentMyAgenda(@Query(Constants.KEY_CONTENT_ID) long contentId);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_SET_LIKE)
    Call<StatusResponse> setLike(@FieldMap Map<String, Long> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_SET_LIKE)
    Call<StatusResponse> setLikeUsingSourceIdentity(@FieldMap Map<String, String> parameters);

    @GET(ApiConstants.URL_MX_TOTAL_LIKE)
    Call<TotalLikeResponse> totalLike(@Query(Constants.KEY_CONTENT_ID) long contentId);

    @GET(ApiConstants.URL_MX_IS_LIKE)
    Call<StatusResponse> isLike(@Query(Constants.KEY_CONTENT_ID) long contentId);

    @GET(ApiConstants.URL_MX_USER_ENROLLMENT_COURSE)
    Call<EnrolledCoursesResponse> userEnrollmentCourse(@Query(Constants.KEY_COURSE_ID) String courseId);

    @Headers("Cache-Control: only-if-cached, max-stale")
    @GET(ApiConstants.URL_MX_USER_ENROLLMENT_COURSE)
    Call<EnrolledCoursesResponse> userEnrollmentCourseFromCache(@Query(Constants.KEY_COURSE_ID) String courseId);

    @GET
    Call<Void> getHtmlFromUrl(@Url HttpUrl absoluteUrl);

    @GET(ApiConstants.URL_MX_GET_SEARCH_FILTER)
    Call<SearchFilter> getSearchFilter();

    @POST(ApiConstants.URL_MX_SEARCH)
    Call<List<Content>> search(@Body Map<String, Object> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_SUBMIT_FEEDBACK)
    Call<FeedbackResponse> submitFeedback(@FieldMap Map<String, String> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_RESETPSWD)
    Call<ChangePasswordResponse> changePassword(@FieldMap Map<String, String> parameters);

    @GET(ApiConstants.URL_MX_GET_MY_CERTIFICATES)
    Call<MyCertificatesResponse> getMyCertificates();

    @GET(ApiConstants.URL_MX_GET_CERTIFICATE_STATUS)
    Call<CertificateStatusResponse> getCertificateStatus(@Query(Constants.KEY_COURSE_ID) String courseId);

    @GET(ApiConstants.URL_MX_GET_CERTIFICATE)
    Call<MyCertificatesResponse> getCertificate(@Query(Constants.KEY_COURSE_ID) String courseId);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_GENERATE_CERTIFICATE)
    Call<CertificateStatusResponse> generateCertificate(@FieldMap Map<String, String> parameters);

    @GET(ApiConstants.URL_MX_GET_CONTENT)
    Call<Content> getContent(@Query(Constants.KEY_CONTENT_ID) long contentId);

    @GET(ApiConstants.URL_MX_GET_SUGGESTED_USERS)
    Call<List<SuggestedUser>> getSuggestedUsers(@Query(Constants.KEY_TAKE) int take,
                                                       @Query(Constants.KEY_SKIP) int skip);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_FOLLOW_USER)
    Call<StatusResponse> followUser(@FieldMap Map<String, String> parameters);

    @POST(ApiConstants.URL_MX_ASSISTANT_SEARCH)
    Call<List<Content>> assistantSearch(@Body Map<String, Object> parameters);

    @POST(ApiConstants.URL_MX_CREATE_GET_NOTIFICATIONS)
    Call<List<Notification>> createNotifications(@Body List<Notification> notifications);

    @POST(ApiConstants.URL_MX_UPDATE_NOTIFICATIONS)
    Call<CountResponse> updateNotifications(@Body Map<String, Object> parameters);

//    @GET(ApiConstants.URL_MX_CREATE_GET_NOTIFICATIONS)
    @GET(ApiConstants.URL_MX_GET_NOTIFICATION)
    Call<NotificationResponse> getNotifications(@Query(Constants.KEY_TAKE) int take,
                                                @Query(Constants.KEY_SKIP) int skip,
                                                @Query(Constants.KEY_COURSE_ID) String CourseId);

    @GET(ApiConstants.URL_MX_GET_FEEDS)
    Call<List<Feed>> getFeeds(@Query(Constants.KEY_TAKE) int take,
                                      @Query(Constants.KEY_SKIP) int skip);

    @GET(ApiConstants.URL_MX_GET_CONTENT)
    Call<Content> getContentFromSourceIdentity(@Query(Constants.KEY_SOURCE_IDENTITY) String sourceIdentity);

    @POST(ApiConstants.URL_MX_SET_USER_CONTENT)
    Call<List<ContentStatus>> setUserContent(@Body List<ContentStatus> statuses);

    @GET(ApiConstants.URL_MX_GET_MY_CONTENT_STATUS)
    Call<List<ContentStatus>> getMyContentStatus();

    @GET(ApiConstants.URL_MX_GET_USER_CONTENT_STATUS)
    Call<List<ContentStatus>> getUserContentStatus(@Query(Constants.KEY_CONTENT_IDS) List<Long> contentIds);

    @GET(ApiConstants.URL_MX_GET_UNIT_STATUS)
    Call<List<UnitStatus>> getUnitStatus(@Query(Constants.KEY_COURSE_KEY) String courseId);

    @GET(ApiConstants.URL_MX_GET_FOLLOW_STATUS)
    Call<FollowStatus> getFollowStatus(@Query(Constants.KEY_USERNAME) String username);

    @GET(ApiConstants.URL_MX_GET_PROGRAMS)
    Call<List<Program>> getPrograms();

    @GET(ApiConstants.URL_MX_GET_CURRICULLAM)
    Call<CurricullamModel> getCurricullam(@Query(value = Constants.COURSE_KEY,encoded = true) String programId);

    @GET(ApiConstants.URL_MX_GET_NOTIFICATION_COUNT)
    Call<NotificationCountResponse> getNotificationCount(@Query(value = Constants.KEY_ACTION_PARENT_ID) String course_id);

    @GET(ApiConstants.URL_MX_GET_NOTIFICATION_READ)
    Call<SuccessResponse> getReadNotification(@Query(Constants.KEY_ID) String id,
                                              @Query(Constants.KEY_ACTION_PARENT_ID) String course_id);

    @GET(ApiConstants.URL_MX_GET_SECTIONS)
    Call<List<Section>> getSections(@Query(Constants.KEY_PROGRAM_ID) String programId);

    @GET(ApiConstants.URL_MX_GET_UNIT_PUBLISH)
    Call<UnitPublish> getUnitPublish(@Query(value=Constants.KEY_UNIT_ID, encoded = true) String unitId);

    @POST(ApiConstants.URL_MX_GET_PROGRAM_FILTERS)
    Call<List<ProgramFilter>> getProgramFilters(@Body Map<String, Object> parameters);

    @POST(ApiConstants.URL_MX_GET_PERIODS)
    Call<List<Period>> getPeriods(@Body Map<String, Object> parameters);

    @POST(ApiConstants.URL_MX_GET_UPDATE_PERIOD)
    Call<SuccessResponse> updatePeriod(@Body Map<String, Object> parameters);

    @POST(ApiConstants.URL_MX_GET_UNITS)
    Call<List<Unit>> getUnits(@Body Map<String, Object> parameters);

    @POST(ApiConstants.URL_MX_GET_USER_UNITS)
    Call<List<Unit>> getUserUnits(@Body Map<String, Object> parameters);

    @POST(ApiConstants.URL_MX_GET_ALL_UNITS)
    Call<List<Unit>> getAllUnits(@Body Map<String, Object> parameters);

    @POST(ApiConstants.URL_MX_GET_CALENDAR_EVENT)
    Call<List<CalendarEvents>> getCalendarEvents(@Body Map<String, Object> parameters);

    @GET(ApiConstants.URL_MX_GET_USERS)
    Call<List<ProgramUser>> getUsers(@Query(Constants.KEY_PROGRAM_ID) String programId,
                                     @Query(Constants.KEY_SECTION_ID) String sectionId,
                                     @Query(Constants.KEY_TAKE) int take,
                                     @Query(Constants.KEY_SKIP) int skip);

    @GET(ApiConstants.URL_MX_GET_PENDING_USERS)
    Call<List<ProgramUser>> getPendingUsers(@Query(Constants.KEY_PROGRAM_ID) String programId,
                                     @Query(Constants.KEY_SECTION_ID) String sectionId,
                                     @Query(Constants.KEY_TAKE) int take,
                                     @Query(Constants.KEY_SKIP) int skip);

    @POST(ApiConstants.URL_MX_GET_PENDING_UNITS)
    Call<List<Unit>> getPendingUnits(@Body Map<String, Object> parameters);

    @POST
    Call<SuccessResponse> setSpecificSession(@Body Map<String, Object> parameters,
                                             @Url String url,@Header ("Cookie") String instructor_cookie);

    @GET(ApiConstants.URL_MX_GET_COURSE_COMPONENT_UNITS)
    Call<CourseComponent> getCourseComponentUnits(@Query(Constants.KEY_UNIT_ID) String unit_id);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_CREATE_PERIOD)
    Call<SuccessResponse> createPeriod(@FieldMap Map<String, String> parameters);


    @POST(ApiConstants.URL_MX_SAVE_PERIOD)
    Call<SuccessResponse> savePeriod(@Body Map<String, Object> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_APPROVE_UNIT)
    Call<SuccessResponse> approveUnit(@FieldMap Map<String, String> parameters);

    @FormUrlEncoded
    @POST(ApiConstants.URL_MX_REJECT_UNIT)
    Call<SuccessResponse> rejectUnit(@FieldMap Map<String, String> parameters);

    //for app update
    @GET(ApiConstants.URL_MX_GET_APP_UPDATE)
    Call<UpdateResponse> getAppUpdate(@Query(Constants.VERSION_NAME) String v_name,
                                      @Query(Constants.VERSION_CODE) Long v_code);

    @POST(ApiConstants.URL_MX_SET_PROPOSED_DATE)
    Call<SuccessResponse> setProposedDate(@Body Map<String, Object> parameters);

    @POST(ApiConstants.URL_MX_SEND_NOTIFICATION)
    Call<SuccessResponse> sendNotification(@Body Map<String, Object> parameters);

    @GET(ApiConstants.URL_MX_GET_UNIT_CONFIGURATION)
    Call<UnitConfiguration> getUnitConfiguration();

    @GET(ApiConstants.URL_MX_GET_PROGRAM_COURSE)
    Call<CourseProgram> getProgramCourses();
}
