package org.edx.mobile.authentication;

import org.edx.mobile.http.ApiConstants;
import org.edx.mobile.http.ApiConstants.TokenType;
import org.edx.mobile.http.HttpException;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.module.prefs.LoginPrefs;

import java.util.Map;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

import static org.edx.mobile.http.ApiConstants.URL_MY_USER_INFO;

public interface LoginService {

    /**
     * If there are form validation errors, this method will throw an {@link org.edx.mobile.http.HttpResponseStatusException} with 400 or 409 error code.
     * In case of validation errors the response body will be {@link org.edx.mobile.model.api.FormFieldMessageBody}.
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_REGISTRATION)
    Response register(@FieldMap Map<String, String> parameters) throws HttpException;

    /**
     * Depending on the query parameters for this endpoint, a different action will be triggered
     * on the server side. In this case, we are sending a user and password to get the AuthResponse.
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_ACCESS_TOKEN)
    AuthResponse getAccessToken(@Field("grant_type") String grant_type,
                                @Field("client_id") String client_id,
                                @Field("username") String username,
                                @Field("password") String password) throws HttpException;

    /**
     * Depending on the query parameters for this endpoint, a different action will be triggered
     * on the server side. In this case, we are using our refresh_token to get a new AuthResponse.
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_ACCESS_TOKEN)
    AuthResponse refreshAccessToken(@Field("grant_type") String grant_type,
                                    @Field("client_id") String client_id,
                                    @Field("refresh_token") String refresh_token) throws HttpException;


    /**
     * Authenticate with edX using an access token from a third party OAuth provider.
     * @param accessToken access token retrieved from third party OAuth provider (i.e. Facebook, Google)
     * @param clientId edX OAuth client ID from config
     * @param groupId Group ID as returned from {@link ApiConstants#getOAuthGroupIdForAuthBackend(LoginPrefs.AuthBackend)}
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_EXCHANGE_ACCESS_TOKEN)
    AuthResponse exchangeAccessToken(@Field("access_token") String accessToken,
                                     @Field("client_id") String clientId,
                                     @Path(ApiConstants.GROUP_ID) String groupId) throws HttpException;

    /**
     * Revoke the specified refresh or access token, along with all other tokens based on the same
     * application grant.
     * @param clientId The client ID
     * @param token The refresh or access token to be revoked
     * @param tokenTypeHint The type of the token to be revoked; This should be either
     *                      'access_token' or 'refresh_token'
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_REVOKE_TOKEN)
    Response revokeAccessToken(@Field("client_id") String clientId,
                               @Field("token") String token,
                               @Field("token_type_hint") @TokenType String tokenTypeHint) throws HttpException;

    /**
     * Reset password for account associated with an email address.
     */
    @FormUrlEncoded
    @POST(ApiConstants.URL_PASSWORD_RESET)
    ResetPasswordResponse resetPassword(@Field("email") String email) throws HttpException;


    /**
     * @return basic profile information for currently authenticated user.
     */
    @GET(URL_MY_USER_INFO)
    ProfileModel getProfile() throws HttpException;
}
