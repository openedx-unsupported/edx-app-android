package org.edx.mobile.discovery.net.course;

import com.google.inject.Inject;
import com.squareup.okhttp.RequestBody;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.discovery.DiscoveryBaseApi;
import org.edx.mobile.discovery.DiscoveryRetrofitProvider;
import org.edx.mobile.discovery.model.DiscoverySubject;
import org.edx.mobile.discovery.model.EnrollAndUnenrollData;
import org.edx.mobile.discovery.model.OrganisationList;
import org.edx.mobile.discovery.model.OrganisationModel;
import org.edx.mobile.discovery.model.ProgramModel;
import org.edx.mobile.discovery.model.SearchResult;
import org.edx.mobile.discovery.model.TagModel;
import org.json.JSONObject;

import retrofit2.Call;

public class CourseApi extends DiscoveryBaseApi {
    private CourseService courseService;

    @Inject
    public CourseApi(DiscoveryRetrofitProvider retrofitProvider) {
        courseService = retrofitProvider.getDiscoveryBaseRetrofit().create(CourseService.class);
    }

    public Call<DiscoverySubject> getDiscoverySubjects(String auth_token, String lang) {
        return courseService.getSubjects(auth_token, lang);
    }

    public Call<OrganisationList> getOrganisations(String auth_token, String lang) {
        return courseService.getOrganisation(auth_token, lang);
    }

    public Call<TagModel> getTopicsWithSubjectName(String auth_token, String lang, String subjectName) {
        return courseService.getTopics(auth_token, lang, subjectName);
    }

    public Call<SearchResult> getSearchResult(String auth_token, String lang, String page_size, String query) {
        return courseService.getSearch(auth_token, lang, page_size, query);
    }

    public Call<SearchResult> getSearchNextResult(String auth_token, String lang, String page, String page_size, String query) {
        return courseService.getSearchNextResult(auth_token, lang, page, page_size, query);
    }

    public Call<ProgramModel> getProgramsWithTopicName(String auth_token, String lang, String topic_name) {
        return courseService.getProgramsWithTopicName(auth_token, lang, topic_name);
    }
   /* public Call<AuthResponse> getEnroll(String auth_token,JSONObject data) {
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),data.toString());
        return courseService.getEnroll(auth_token,body);
    }*/
}
