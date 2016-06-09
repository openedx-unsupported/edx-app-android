package org.edx.mobile.authentication;

import org.edx.mobile.http.ApiConstants;
import org.edx.mobile.http.RetroHttpException;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface LoginService {

    /**
     * Depending on the query parameters for this endpoint, a different action will be triggered
     * on the server side. In this case, we are sending a user and password to get the AuthResponse.
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_ACCESS_TOKEN)
    AuthResponse getAccessToken(@Field("grant_type") String grant_type,
                                @Field("client_id") String client_id,
                                @Field("username") String username,
                                @Field("password") String password) throws RetroHttpException;

    /**
     * Depending on the query parameters for this endpoint, a different action will be triggered
     * on the server side. In this case, we are using our refresh_token to get a new AuthResponse.
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_ACCESS_TOKEN)
    AuthResponse refreshAccessToken(@Field("grant_type") String grant_type,
                                    @Field("client_id") String client_id,
                                    @Field("refresh_token") String refresh_token) throws RetroHttpException;
}
