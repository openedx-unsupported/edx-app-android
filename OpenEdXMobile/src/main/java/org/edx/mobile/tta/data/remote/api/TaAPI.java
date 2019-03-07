package org.edx.mobile.tta.data.remote.api;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.inject.Singleton;

import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.agenda.AgendaList;
import org.edx.mobile.tta.data.model.content.BookmarkResponse;
import org.edx.mobile.tta.data.model.content.TotalLikeResponse;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;
import org.edx.mobile.tta.data.model.library.CollectionItemsResponse;
import org.edx.mobile.tta.data.model.library.ConfigModifiedDateResponse;
import org.edx.mobile.tta.data.model.search.FilterSection;
import org.edx.mobile.tta.data.model.search.SearchFilter;
import org.edx.mobile.tta.data.remote.service.TaService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

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

    public Call<List<Content>> getMyAgendaContent(long sourseId){
        return taService.getMyAgendaContent(sourseId);
    }
    public Call<List<Content>> getStateAgendaContent(long sourseId){
        return taService.getStateAgendaContent(sourseId);
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
}
