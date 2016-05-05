package org.edx.mobile.http;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.jakewharton.retrofit.Ok3Client;

import org.edx.mobile.authentication.LoginService;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.PrefManager;
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
 * that received the 401 will be attempted again.
 */
public class OauthRefreshTokenAuthenticator implements Authenticator {

    private final Logger logger = new Logger(getClass().getName());
    private final static String TOKEN_EXPIRED_ERROR_MESSAGE = "token_expired";
    private Context context;

    @Inject
    Config config;

    public OauthRefreshTokenAuthenticator(Context context) {
        this.context = context;
        RoboGuice.injectMembers(context, this);
    }

    @Override
    public Request authenticate(Route route, final Response response) throws IOException {
        logger.warn(response.toString());

        if (!isTokenExpired(response.peekBody(200).string())) {
            return null;
        }

        PrefManager pref = new PrefManager(context, PrefManager.Pref.LOGIN);
        refreshAccessToken(pref);
        return response.request().newBuilder()
                .header("Authorization", pref.getCurrentAuth().token_type + " " + pref.getCurrentAuth().access_token)
                .build();
    }

    private void refreshAccessToken(@NonNull PrefManager pref) {
        OkHttpClient client = OkHttpUtil.getClient(context);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new Ok3Client(client))
                .setEndpoint(config.getApiHostURL())
                .build();
        LoginService loginService = restAdapter.create(LoginService.class);

        AuthResponse refreshTokenResponse;
        refreshTokenResponse = loginService.refreshAccessToken(
                "refresh_token", config.getOAuthClientId(), pref.getCurrentAuth().refresh_token);
        Gson gson = new GsonBuilder().create();
        pref.put(PrefManager.Key.AUTH_JSON, gson.toJson(refreshTokenResponse));
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
