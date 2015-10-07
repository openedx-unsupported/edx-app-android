package org.edx.mobile.user;

import org.edx.mobile.http.RetroHttpException;

import java.util.Map;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.PATCH;
import retrofit.http.Path;

public interface UserService {
    @GET("/api/user/v1/accounts/{username}")
    Account getAccount(@Path("username") String username) throws RetroHttpException;

    @PATCH("/api/user/v1/accounts/{username}")
    @Headers("Content-Type: application/merge-patch+json")
    Account updateAccount(@Path("username") String username, @Body Map<String, String> fields) throws RetroHttpException;
}
