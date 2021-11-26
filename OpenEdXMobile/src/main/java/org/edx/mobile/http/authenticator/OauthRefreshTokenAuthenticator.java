package org.edx.mobile.http.authenticator;

import static org.edx.mobile.http.util.CallUtil.executeStrict;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.authentication.LoginService;
import org.edx.mobile.event.LogoutEvent;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.http.provider.RetrofitProvider;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Authenticator for 401 responses for refreshing oauth tokens. Checks for
 * the expired oauth token case and then uses the refresh token to retrieve a
 * new access token. Using the new access token, the original http request
 * that received the 401 will be attempted again. If no refresh_token is
 * present, no authentication attempt is made.
 */
@Singleton
public class OauthRefreshTokenAuthenticator implements Authenticator {

    private final Logger logger = new Logger(getClass().getName());
    private final static String TOKEN_EXPIRED_ERROR_MESSAGE = "token_expired";
    private final static String TOKEN_NONEXISTENT_ERROR_MESSAGE = "token_nonexistent";
    private final static String TOKEN_INVALID_GRANT_ERROR_MESSAGE = "invalid_grant";
    private final static String DISABLED_USER_ERROR_MESSAGE = "user_is_disabled";

    Lazy<Config> config;

    Lazy<LoginPrefs> loginPrefs;

    Lazy<RetrofitProvider> retrofitProvider;

    @Inject
    public OauthRefreshTokenAuthenticator(Lazy<Config> config, Lazy<RetrofitProvider> retrofitProvider,
                                          Lazy<LoginPrefs> loginPrefs
    ) {
        this.retrofitProvider = retrofitProvider;
        this.loginPrefs = loginPrefs;
        this.config = config;
    }

    @Override
    public synchronized Request authenticate(Route route, final Response response) throws IOException {
        logger.warn(response.toString());

        final AuthResponse currentAuth = loginPrefs.get().getCurrentAuth();
        if (null == currentAuth || null == currentAuth.refresh_token) {
            return null;
        }

        String errorCode = getErrorCode(response.peekBody(200).string());

        if (errorCode != null) {
            switch (errorCode) {
                case TOKEN_EXPIRED_ERROR_MESSAGE:
                    final AuthResponse refreshedAuth;
                    try {
                        refreshedAuth = refreshAccessToken(currentAuth);
                    } catch (HttpStatusException e) {
                        return null;
                    }
                    return response.request().newBuilder()
                            .header("Authorization", refreshedAuth.token_type + " " + refreshedAuth.access_token)
                            .build();
                case TOKEN_NONEXISTENT_ERROR_MESSAGE:
                case TOKEN_INVALID_GRANT_ERROR_MESSAGE:
                    // Retry request with the current access_token if the original access_token used in
                    // request does not match the current access_token. This case can occur when
                    // asynchronous calls are made and are attempting to refresh the access_token where
                    // one call succeeds but the other fails. https://github.com/edx/edx-app-android/pull/834
                    if (!response.request().headers().get("Authorization").split(" ")[1].equals(currentAuth.access_token)) {
                        return response.request().newBuilder()
                                .header("Authorization", currentAuth.token_type + " " + currentAuth.access_token)
                                .build();
                    }
                case DISABLED_USER_ERROR_MESSAGE:
                    // If the user is logged-in & marked as disabled then on the next server response
                    // app will force the user to logout and navigate it to the launcher screen.
                    EventBus.getDefault().post(new LogoutEvent());
                    break;
            }
        }
        return null;
    }

    @NonNull
    private AuthResponse refreshAccessToken(AuthResponse currentAuth)
            throws IOException, HttpStatusException {
        LoginService loginService = this.retrofitProvider.get().getNonOAuthBased().create(LoginService.class);

        AuthResponse refreshTokenData = executeStrict(loginService.refreshAccessToken(
                ApiConstants.TOKEN_TYPE_REFRESH, config.get().getOAuthClientId(), currentAuth.refresh_token));
        loginPrefs.get().storeRefreshTokenResponse(refreshTokenData);
        return refreshTokenData;
    }

    @Nullable
    private String getErrorCode(String responseBody) {
        try {
            JSONObject jsonObj = new JSONObject(responseBody);
            return jsonObj.getString("error_code");
        } catch (JSONException ex) {
            logger.warn("Unable to get error_code from 401 response");
            return null;
        }
    }
}
