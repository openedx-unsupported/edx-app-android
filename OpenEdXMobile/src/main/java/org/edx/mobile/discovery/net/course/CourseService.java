package org.edx.mobile.discovery.net.course;

import androidx.annotation.NonNull;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.discovery.DiscoveryConstants;
import org.edx.mobile.discovery.model.DiscoverySubject;
import org.edx.mobile.discovery.model.EnrollAndUnenrollData;
import org.edx.mobile.discovery.model.OrganisationList;
import org.edx.mobile.discovery.model.OrganisationModel;
import org.edx.mobile.discovery.model.ProgramModel;
import org.edx.mobile.discovery.model.SearchResult;
import org.edx.mobile.discovery.model.TagModel;
import org.edx.mobile.http.constants.ApiConstants;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CourseService {

    @GET("api/v1/subjects/")
    Call<DiscoverySubject> getSubjects(@Header(DiscoveryConstants.AUTHORIZATION) String authorization,@Header(DiscoveryConstants.ACCEPT_LANGUAGE) String lang);

    @GET("api/v1/organizations/")
    Call<OrganisationList> getOrganisation(@Header(DiscoveryConstants.AUTHORIZATION) String authorization,@Header(DiscoveryConstants.ACCEPT_LANGUAGE) String lang);

    @GET("extandedapi/get-program-topics/")
    Call<TagModel> getTopics(@Header(DiscoveryConstants.AUTHORIZATION) String authorization,@Header(DiscoveryConstants.ACCEPT_LANGUAGE) String lang, @Query("subject_name") String subjectName);

    @GET("api/v1/search/programs/details/")
    Call<ProgramModel> getProgramsWithTopicName(@Header(DiscoveryConstants.AUTHORIZATION) String authorization ,@Header(DiscoveryConstants.ACCEPT_LANGUAGE) String lang, @Query("program_topics") String subjectName);

    @GET("/extandedapi/custom-course-search/")
    Call<SearchResult> getSearch(@Header(DiscoveryConstants.AUTHORIZATION) String authorization ,@Header(DiscoveryConstants.ACCEPT_LANGUAGE) String lang, @Query("page_size") String page, @Query("q") String query);

    @GET("/extandedapi/custom-course-search/")
    Call<SearchResult> getSearchNextResult(@Header(DiscoveryConstants.AUTHORIZATION) String authorization ,@Header(DiscoveryConstants.ACCEPT_LANGUAGE) String lang, @Query("page") String page ,@Query("page_size") String page_size, @Query("q") String query);

    @NonNull
    @FormUrlEncoded
    @POST("explore-courses/course-bulk/")
    Call<AuthResponse> getEnroll(@Header(DiscoveryConstants.AUTHORIZATION) String authorization ,@Header(DiscoveryConstants.ACCEPT_LANGUAGE) String lang, @Field("enroll") String data);

    @NonNull
    @FormUrlEncoded
    @POST("explore-courses/course-bulk/")
    Call<AuthResponse> getUnenroll(@Header(DiscoveryConstants.AUTHORIZATION) String authorization,@Header(DiscoveryConstants.ACCEPT_LANGUAGE) String lang, @Field("unenroll") EnrollAndUnenrollData data);
}
