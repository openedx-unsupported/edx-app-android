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

        if (!isTokenExpired(response.peekBody(HttpStatus.OK).string())) {
            return null;
        }

        final AuthResponse currentAuth = loginPrefs.getCurrentAuth();
        if (null == currentAuth || null == currentAuth.refresh_token) {
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

    /**
     * Checks the if the error_code in the response body is the token_expired error code.
     */
    private boolean isTokenExpired(String responseBody) {
        try {
            JSONObject jsonObj = new JSONObject(responseBody);
            String errorCode = jsonObj.getString("error_code");
            return errorCode.equals(TOKEN_EXPIRED_ERROR_MESSAGE);
        } catch (JSONException ex) {
            return false;
        }
    }
}
