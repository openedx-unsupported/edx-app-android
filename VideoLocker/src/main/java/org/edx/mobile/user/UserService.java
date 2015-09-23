package org.edx.mobile.user;

import org.edx.mobile.http.RetroHttpException;

import retrofit.http.GET;
import retrofit.http.Path;

public interface UserService {
    @GET("/api/user/v1/accounts/{username}")
    Account getAccount(@Path("username") String username) throws RetroHttpException;
}
