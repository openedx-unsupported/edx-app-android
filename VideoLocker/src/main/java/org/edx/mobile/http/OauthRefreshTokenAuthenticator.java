package org.edx.mobile.http;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.jakewharton.retrofit.Ok3Client;

import org.edx.mobile.authentication.LoginService;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit.RestAdapter;
import roboguice.RoboGuice;

/**
 * Authenticator for 401 responses for refreshing oauth tokens. Checks for
 * the expired oauth token case and then uses the refresh token to retrieve a
 * new access token. Using the new access token, the original http request
 * that received the 401 will be attempted again. If no refresh_token is
 * present, no authentication attempt is made.
 */
public class OauthRefreshTokenAuthenticator implements Authenticator {

    private final Logger logger = new Logger(getClass().getName());
    private final static String TOKEN_EXPIRED_ERROR_MESSAGE = "token_expired";
    private final static String TOKEN_NONEXISTENT_ERROR_MESSAGE = "token_nonexistent";
    private final static String TOKEN_INVALID_GRANT_ERROR_MESSAGE = "invalid_grant";
    private Context context;

    @Inject
    Config config;

    @Inject
    LoginPrefs loginPrefs;


    public OauthRefreshTokenAuthenticator(Context context) {
        this.context = context;
        RoboGuice.injectMembers(context, this);
    }

    @Override
    public Request authenticate(Route route, final Response response) throws IOException {
        logger.warn(response.toString());

        final AuthResponse currentAuth = loginPrefs.getCurrentAuth();
        if (null == currentAuth || null == currentAuth.refresh_token) {
            return null;
        }
        String response_body = response.peekBody(200).string();

        switch (getErrorCode(response_body)) {
            case TOKEN_EXPIRED_ERROR_MESSAGE:
                break;
            case TOKEN_NONEXISTENT_ERROR_MESSAGE:
            case TOKEN_INVALID_GRANT_ERROR_MESSAGE:
                if (!response.request().headers().get("Authorization").split(" ")[1].equals(currentAuth.access_token)) {
                    return response.request().newBuilder()
                            .header("Authorization", currentAuth.token_type + " " + currentAuth.access_token)
                            .build();
                }
            default:
                return null;
        }

        final AuthResponse refreshedAuth;
        try {
            refreshedAuth = refreshAccessToken(currentAuth);
        } catch (HttpException e) {
            return null;
        }
        return response.request().newBuilder()
                .header("Authorization", refreshedAuth.token_type + " " + refreshedAuth.access_token)
                .build();
    }

    @NonNull
    private AuthResponse refreshAccessToken(AuthResponse currentAuth) throws HttpException {
        OkHttpClient client = OkHttpUtil.getClient(context);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new Ok3Client(client))
                .setEndpoint(config.getApiHostURL())
                .build();
        LoginService loginService = restAdapter.create(LoginService.class);

        AuthResponse refreshTokenResponse;
        refreshTokenResponse = loginService.refreshAccessToken(
                "refresh_token", config.getOAuthClientId(), currentAuth.refresh_token);
        loginPrefs.storeRefreshTokenResponse(refreshTokenResponse);
        return refreshTokenResponse;
    }

    private String getErrorCode(String responseBody) {
        try {
            JSONObject jsonObj = new JSONObject(responseBody);
            return jsonObj.getString("error_code");
        } catch (JSONException ex) {
            return null;
        }
    }
}
