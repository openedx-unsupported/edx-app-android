package org.edx.mobile.user;

import org.edx.mobile.http.HttpException;
import org.edx.mobile.model.Page;
import org.edx.mobile.profiles.BadgeAssertion;

import java.util.Map;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedOutput;

public interface UserService {
    @GET("/api/user/v1/accounts/{username}")
    Account getAccount(@Path("username") String username) throws HttpException;

    @PATCH("/api/user/v1/accounts/{username}")
    Account updateAccount(@Path("username") String username, @Body Map<String, Object> fields) throws HttpException;

    @POST("/api/user/v1/accounts/{username}/image")
    Response setProfileImage(@Path("username") String username, @Header("Content-Disposition") String contentDisposition, @Body TypedOutput file) throws HttpException;

    @DELETE("/api/user/v1/accounts/{username}/image")
    Response deleteProfileImage(@Path("username") String username) throws HttpException;

    @GET("/api/mobile/v0.5/users/{username}/course_enrollments")
    Response getUserEnrolledCourses(@Path("username") String username, @Query("org") String org) throws HttpException;

    @GET("/api/badges/v1/assertions/user/{username}")
    Page<BadgeAssertion> getBadges(@Path("username") String username,
                                   @Query("page") int page,
                                   @Query("page_size") int pageSize) throws HttpException;
}
