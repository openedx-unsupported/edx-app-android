package org.edx.mobile.tta.data.remote.api;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.inject.Singleton;

import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentStatus;
import org.edx.mobile.tta.data.local.db.table.Feed;
import org.edx.mobile.tta.data.local.db.table.Notification;
import org.edx.mobile.tta.data.local.db.table.UnitStatus;
import org.edx.mobile.tta.data.model.CountResponse;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.data.model.content.BookmarkResponse;
import org.edx.mobile.tta.data.model.content.CertificateStatusResponse;
import org.edx.mobile.tta.data.model.content.MyCertificatesResponse;
import org.edx.mobile.tta.data.model.content.TotalLikeResponse;
import org.edx.mobile.tta.data.model.feed.SuggestedUser;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;
import org.edx.mobile.tta.data.model.library.CollectionItemsResponse;
import org.edx.mobile.tta.data.model.library.ConfigModifiedDateResponse;
import org.edx.mobile.tta.data.model.profile.ChangePasswordResponse;
import org.edx.mobile.tta.data.model.profile.FeedbackResponse;
import org.edx.mobile.tta.data.model.profile.FollowStatus;
import org.edx.mobile.tta.data.model.search.FilterSection;
import org.edx.mobile.tta.data.model.search.SearchFilter;
import org.edx.mobile.tta.data.remote.service.TaService;

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
}
