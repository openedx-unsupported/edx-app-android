package org.edx.mobile.user;

import org.edx.mobile.http.RetroHttpException;

import java.util.Map;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.mime.TypedOutput;

public interface UserService {
    @GET("/api/user/v1/accounts/{username}")
    Account getAccount(@Path("username") String username) throws RetroHttpException;

    @PATCH("/api/user/v1/accounts/{username}")
    Account updateAccount(@Path("username") String username, @Body Map<String, Object> fields) throws RetroHttpException;

    @POST("/api/user/v1/accounts/{username}/image")
    Response setProfileImage(@Path("username") String username, @Header("Content-Disposition") String contentDisposition, @Body TypedOutput file) throws RetroHttpException;

    @DELETE("/api/user/v1/accounts/{username}/image")
    Response deleteProfileImage(@Path("username") String username) throws RetroHttpException;
}
