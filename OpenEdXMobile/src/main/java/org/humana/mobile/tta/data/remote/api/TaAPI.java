package org.humana.mobile.tta.data.remote.api;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.inject.Singleton;

import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
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
import org.humana.mobile.tta.data.model.program.ProgramFilter;
import org.humana.mobile.tta.data.model.program.ProgramUser;
import org.humana.mobile.tta.data.model.search.FilterSection;
import org.humana.mobile.tta.data.model.search.SearchFilter;
import org.humana.mobile.tta.data.remote.service.TaService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import okhttp3.HttpUrl;
import retrofit2.Call;

@Singleton
public class TaAPI {

    @NonNull
    private final TaService taService;

    @Inject
    public TaAPI(@NonNull TaService taService) {
        this.taService = taService;
    }

    public Call<CollectionConfigResponse> getCollectionConfig(){
        return taService.getCollectionConfig();
    }

    public Call<ConfigModifiedDateResponse> getConfigModifiedDate(){
        return taService.getConfigModifiedDate();
    }

    public Call<List<CollectionItemsResponse>> getCollectionItems(Bundle parameters){
        long[] listIds = parameters.getLongArray(Constants.KEY_LIST_IDS);
        int skip = parameters.getInt(Constants.KEY_SKIP);
        int take = parameters.getInt(Constants.KEY_TAKE);
        StringBuilder builder = new StringBuilder();
        if (listIds != null){
            for (long id: listIds){
                builder.append(id).append(",");
            }
        }
        if (builder.length() > 0){
            builder.deleteCharAt(builder.length() - 1);
        }
        return taService.getCollectionItems(builder.toString(), skip, take);
    }

    public Call<List<AgendaList>> getStateAgendaCount(){
        return taService.getStateAgendaCount();
    }

    public Call<AgendaList> getMyAgendaCount(){
        return taService.getMyAgendaCount();
    }

    public Call<List<Content>> getMyAgendaContent(long sourceId){
        return taService.getMyAgendaContent(sourceId);
    }
    public Call<List<Content>> getStateAgendaContent(long sourceId, long list_id){
        return taService.getStateAgendaContent(sourceId, list_id);
    }


    public Call<BookmarkResponse> setBookmark(long contentId){
        Map<String, Long> parameters = new HashMap<>();
        parameters.put(Constants.KEY_CONTENT_ID, contentId);
        return taService.setBookmark(parameters);
    }

    public Call<StatusResponse> isContentMyAgenda(long contentId) {
        return taService.isContentMyAgenda(contentId);
    }

    public Call<StatusResponse> setLike(long contentId) {
        Map<String, Long> parameters = new HashMap<>();
        parameters.put(Constants.KEY_CONTENT_ID, contentId);
        return taService.setLike(parameters);
    }

    public Call<StatusResponse> setLikeUsingSourceIdentity(String sourceIdentity) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(Constants.KEY_SOURCE_IDENTITY, sourceIdentity);
        return taService.setLikeUsingSourceIdentity(parameters);
    }

    public Call<TotalLikeResponse> totalLike(long contentId) {
        return taService.totalLike(contentId);
    }

    public Call<StatusResponse> isLike(long contentId) {
        return taService.isLike(contentId);
    }

    public Call<EnrolledCoursesResponse> userEnrollmentCourse(String courseId) {
        return taService.userEnrollmentCourse(courseId);
    }

    public Call<EnrolledCoursesResponse> userEnrollmentCourseFromCache(String courseId) {
        return taService.userEnrollmentCourseFromCache(courseId);
    }

    public Call<Void> getHtmlFromUrl(HttpUrl absoluteUrl) {
        return taService.getHtmlFromUrl(absoluteUrl);
    }

    public Call<SearchFilter> getSearchFilter() {
        return taService.getSearchFilter();
    }

    public Call<List<Content>> search(int take, int skip, boolean isPriority, long listId, String searchText, List<FilterSection> filterSections) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.KEY_TAKE, take);
        parameters.put(Constants.KEY_SKIP, skip);
        parameters.put(Constants.KEY_IS_PRIORITY, isPriority);
        parameters.put(Constants.KEY_LIST_ID, listId);
        parameters.put(Constants.KEY_SEARCH_TEXT, searchText);
        parameters.put(Constants.KEY_FILTER_DATA, filterSections);

        return taService.search(parameters);
    }

    public Call<FeedbackResponse> submitFeedback(Bundle parameters){
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }
        return taService.submitFeedback(parameterMap);
    }

    public Call<ChangePasswordResponse> changePassword(Bundle parameters){
        final Map<String, String> parameterMap = new HashMap<>();
        for (String key : parameters.keySet()) {
            parameterMap.put(key, parameters.getString(key));
        }
        return taService.changePassword(parameterMap);
    }

    public Call<MyCertificatesResponse> getMyCertificates(){
        return taService.getMyCertificates();
    }

    public Call<CertificateStatusResponse> getCertificateStatus(String courseId){
        return taService.getCertificateStatus(courseId);
    }

    public Call<MyCertificatesResponse> getCertificate(String courseId){
        return taService.getCertificate(courseId);
    }

    public Call<CertificateStatusResponse> generateCertificate(String courseId){
        Map<String, String> parameters=new HashMap<>();
        parameters.put(Constants.KEY_COURSE_ID,courseId);
        return taService.generateCertificate(parameters);
    }

    public Call<Content> getContent(long contentId){
        return taService.getContent(contentId);
    }

    public Call<List<SuggestedUser>> getSuggestedUsers(int take, int skip){
        return taService.getSuggestedUsers(take, skip);
    }

    public Call<StatusResponse> followUser(String username){
        Map<String, String> parameters=new HashMap<>();
        parameters.put(Constants.KEY_FOLLOW_USERNAME,username);
        return taService.followUser(parameters);
    }

    public Call<List<Content>> assistantSearch(String searchText,List<String> tags) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.KEY_TAKE, 5);  //fixing this for now
        parameters.put(Constants.KEY_SKIP, 0);
        parameters.put(Constants.KEY_SEARCH_TEXT, searchText);
        parameters.put(Constants.KEY_TAGS, tags);
        return taService.assistantSearch(parameters);
    }

    public Call<List<Notification>> createNotifications(List<Notification> notifications){
        return taService.createNotifications(notifications);
    }

    public Call<CountResponse> updateNotifications(List<String> notificationIds){
        Map<String, Object> parameters=new HashMap<>();
        parameters.put(Constants.KEY_IDS, notificationIds);
        return taService.updateNotifications(parameters);
    }

    public Call<List<Notification>> getNotifications(int take, int skip){
        return taService.getNotifications(take, skip);
    }

    public Call<List<Feed>> getFeeds(int take, int skip){
        return taService.getFeeds(take, skip);
    }

    public Call<Content> getContentFromSourceIdentity(String sourceIdentity){
        return taService.getContentFromSourceIdentity(sourceIdentity);
    }

    public Call<List<ContentStatus>> setUserContent(List<ContentStatus> statuses){
        return taService.setUserContent(statuses);
    }

    public Call<List<ContentStatus>> getMyContentStatus(){
        return taService.getMyContentStatus();
    }

    public Call<List<ContentStatus>> getUserContentStatus(List<Long> contentIds){
        return taService.getUserContentStatus(contentIds);
    }

    public Call<List<UnitStatus>> getUnitStatus(String courseId){
        return taService.getUnitStatus(courseId);
    }

    public Call<FollowStatus> getFollowStatus(String username){
        return taService.getFollowStatus(username);
    }

    public Call<List<Program>> getPrograms(){
        return taService.getPrograms();
    }

    public Call<List<Section>> getSections(String programId){
        return taService.getSections(programId);
    }

    public Call<List<ProgramFilter>> getProgramFilters(String programId, String sectionId, String showIn, List<ProgramFilter> filters ){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_SHOW_IN, showIn);
        parameters.put(Constants.KEY_FILTERS, filters);
        return taService.getProgramFilters(parameters);
    }

    public Call<List<Period>> getPeriods(List<ProgramFilter> filters, String programId, String sectionId, String role, int take, int skip){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.KEY_TAKE, take);
        parameters.put(Constants.KEY_SKIP, skip);
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_ROLE, role);
        parameters.put(Constants.KEY_FILTERS, filters);
        return taService.getPeriods(parameters);
    }
    public Call<SuccessResponse> updatePeriods(String programId, String sectionId, String periodId, String periodName, long startDate, long endDate){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_PERIOD_START_DATE, startDate);
        parameters.put(Constants.KEY_PERIOD_END_DATE, endDate);
        parameters.put(Constants.KEY_PERIOD_ID, periodId);
        parameters.put(Constants.KEY_PERIOD_NAME, periodName);
        return taService.updatePeriod(parameters);
    }

    public Call<List<Unit>> getUnits(List<ProgramFilter> filters,String searchText, String programId, String sectionId,String role,
                                     String student_username, long period_id, int take, int skip,Long startDateTime,
                                     Long endDateTime){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.KEY_TAKE, take);
        parameters.put(Constants.KEY_SKIP, skip);
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_FILTERS, filters);
        parameters.put(Constants.KEY_ROLE, role);
        parameters.put(Constants.KEY_PERIOD_ID, period_id);
        parameters.put(Constants.KEY_STUDENT_USERNAME, student_username);
        parameters.put(Constants.KEY_START_DATE, startDateTime);
        parameters.put(Constants.KEY_END_DATE, endDateTime);
        parameters.put(Constants.KEY_SEARCH_TEXT, searchText);

        return taService.getUnits(parameters);
    }

    public Call<List<Unit>> getUserUnits(List<ProgramFilter> filters, String programId, String sectionId,String role,
                                     String studentName, int take, int skip){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.KEY_TAKE, take);
        parameters.put(Constants.KEY_SKIP, skip);
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_FILTERS, filters);
        parameters.put(Constants.KEY_ROLE, role);
        parameters.put(Constants.KEY_STUDENT_USERNAME, studentName);

        return taService.getUserUnits(parameters);
    }

    public Call<List<Unit>> getAllUnits(List<ProgramFilter> filters, String programId, String sectionId, String searchText, long periodId, int take, int skip){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.KEY_TAKE, take);
        parameters.put(Constants.KEY_SKIP, skip);
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_FILTERS, filters);
        parameters.put(Constants.KEY_SEARCH_TEXT, searchText);
        parameters.put(Constants.KEY_PERIOD_ID, periodId);

        return taService.getAllUnits(parameters);
    }

    public Call<List<CalendarEvents>> getCalenderEvents(String programId, String sectionId, String role, long eventCalendarDate, int count, int take, int skip){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Constants.KEY_TAKE, take);
        parameters.put(Constants.KEY_SKIP, skip);
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_ROLE, role);
        parameters.put(Constants.KEY_EVENT_CAL_DATE, eventCalendarDate);
        parameters.put(Constants.KEY_EVENT_COUNT, count);
        return taService.getCalendarEvents(parameters);
    }

    public Call<List<ProgramUser>> getUsers(String programId, String sectionId, int take, int skip){
        return taService.getUsers(programId, sectionId, take, skip);
    }

    public Call<List<ProgramUser>> getPendingUsers(String programId, String sectionId, int take, int skip){
        return taService.getPendingUsers(programId, sectionId, take, skip);
    }

    public Call<List<Unit>> getPendingUnits(String programId, String sectionId, String username, int take, int skip){
        Map<String, Object> parameters=new HashMap<>();
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_USERNAME, username);
        parameters.put(Constants.KEY_TAKE, take);
        parameters.put(Constants.KEY_SKIP, skip);
//        parameters.put(Constants.KEY_PERIOD_NAME, periodName);
        return taService.getPendingUnits(parameters);
    }

    public Call<SuccessResponse> setSpecificSession(String role, String username, String url,String instructor_cookie){
        Map<String, Object> parameters=new HashMap<>();
        parameters.put(Constants.KEY_ROLE, role);
        parameters.put(Constants.KEY_USER_NAME, username);
        return taService.setSpecificSession(parameters, url,instructor_cookie);
    }

    public Call<CourseComponent> getCourseComponentUnits(String unit_id){
        return taService.getCourseComponentUnits(unit_id);
    }

    public Call<SuccessResponse> createPeriod(String programId, String sectionId, String lang){
        Map<String, String> parameters=new HashMap<>();
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_LANG, lang);
//        parameters.put(Constants.KEY_PERIOD_NAME, periodName);
        return taService.createPeriod(parameters);
    }

    public Call<SuccessResponse> savePeriod(long periodId, List<String> addedIds, List<String> removedIds,
                                            Map<String, Long> proposedDateModified, Map<String, Long> proposedDateAdded){
        Map<String, Object> parameters=new HashMap<>();
        parameters.put(Constants.KEY_PERIOD_ID, String.valueOf(periodId));
        parameters.put(Constants.KEY_ADDED_UNITS, addedIds);
        parameters.put(Constants.KEY_REMOVED_UNITS, removedIds);
        parameters.put(Constants.KEY_PROPOSED_DATE_MODIFIED, proposedDateModified);
        parameters.put(Constants.KEY_PROPOSED_DATE_ADDED, proposedDateAdded);
        return taService.savePeriod(parameters);
    }

    public Call<SuccessResponse> approveUnit(String unitId, String username, String remarks, int rating){
        Map<String, String> parameters=new HashMap<>();
        parameters.put(Constants.KEY_UNIT_ID, unitId);
        parameters.put(Constants.KEY_USERNAME, username);
        parameters.put(Constants.KEY_APPROVE_REMARKS, remarks);
        parameters.put(Constants.KEY_APPROVE_RATING, String.valueOf(rating));
        return taService.approveUnit(parameters);
    }

    public Call<SuccessResponse> rejectUnit(String unitId, String username, String remarks, int rating){
        Map<String, String> parameters=new HashMap<>();
        parameters.put(Constants.KEY_UNIT_ID, unitId);
        parameters.put(Constants.KEY_USERNAME, username);
        parameters.put(Constants.KEY_RETURN_REMARKS, remarks);
        parameters.put(Constants.KEY_RETURN_RATING, String.valueOf(rating));
        return taService.rejectUnit(parameters);
    }

    public Call<UpdateResponse> getVersionUpdate(String v_name, Long v_code){
        return taService.getAppUpdate(v_name,v_code);
    }

    public Call<SuccessResponse> setProposedDate(String programId, String sectionId, long proposedDate,
                                                 long periodId, String unitId){
        Map<String, Object> parameters=new HashMap<>();
        parameters.put(Constants.KEY_PROGRAM_ID, programId);
        parameters.put(Constants.KEY_SECTION_ID, sectionId);
        parameters.put(Constants.KEY_UNIT_ID, unitId);
        parameters.put(Constants.KEY_PERIOD_ID, periodId);
        parameters.put(Constants.KEY_PROPOSED_DATE, proposedDate);
        return taService.setProposedDate(parameters);
    }
}
