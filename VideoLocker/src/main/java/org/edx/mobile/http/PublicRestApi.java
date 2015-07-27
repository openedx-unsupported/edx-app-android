package org.edx.mobile.http;

import org.edx.mobile.model.api.AuthResponse;
import org.edx.mobile.model.api.ResetPasswordResponse;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * we group all mobile endpoints which do NOT require oauth token
 */
public interface PublicRestApi {
    /**
     * Resets password for the given email address.
     * @param email
     * @return
     * @throws Exception
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_PASSWORD_RESET)
    ResetPasswordResponse doResetPassword(@Field("email") String email);


    /**
     * Executes HTTP POST for auth call, and returns response.
     *
     * @return
     * @throws Exception
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_ACCESS_TOKEN)
    AuthResponse doLogin(@Field("grant_type") String grant_type,
                         @Field("client_id") String client_id,
                         @Field("username") String username,
                         @Field("password") String password);

    @FormUrlEncoded
    @POST(ApiConstants.URL_EXCHANGE_ACCESS_TOKEN)
    AuthResponse doExchangeAccessToken(@Field("access_token") String accessToken,
                                       @Field("client_id") String clientId,
                                       @Path(ApiConstants.GROUP_ID) String groupId);
}