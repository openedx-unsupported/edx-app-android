package org.edx.mobile.authentication;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.HttpResponseStatusException;
import org.edx.mobile.http.RetroHttpException;
import org.edx.mobile.model.api.RegisterResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.services.ServiceManager;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.observer.BasicObservable;
import org.edx.mobile.util.observer.Observable;

import retrofit.RestAdapter;

@Singleton
public class LoginAPI {

    @NonNull
    private final LoginService loginService;

    @NonNull
    private final Config config;

    @NonNull
    private final LoginPrefs loginPrefs;

    @NonNull
    private final ServiceManager serviceManager;

    @NonNull
    private final ISegment segment;

    @NonNull
    private final NotificationDelegate notificationDelegate;

    @NonNull
    private final BasicObservable<LogInEvent> logInEvents = new BasicObservable<>();

    @Inject
    public LoginAPI(@NonNull RestAdapter restAdapter, @NonNull Config config, @NonNull LoginPrefs loginPrefs, @NonNull ServiceManager serviceManager, @NonNull ISegment segment, @NonNull NotificationDelegate notificationDelegate) {
        this.config = config;
        this.loginPrefs = loginPrefs;
        this.serviceManager = serviceManager;
        this.segment = segment;
        this.notificationDelegate = notificationDelegate;
        loginService = restAdapter.create(LoginService.class);
    }

    @NonNull
    public AuthResponse getAccessToken(@NonNull String username,
                                       @NonNull String password) throws RetroHttpException {
        String grantType = "password";
        String clientID = config.getOAuthClientId();
        return loginService.getAccessToken(grantType, clientID, username, password);
    }

    @NonNull
    public AuthResponse logInUsingEmail(String email, String password) throws Exception {
        try {
            final AuthResponse response = getAccessToken(email, password);
            if (!response.isSuccess()) {
                throw new AuthException(response.error);
            }
            finishLogIn(response, LoginPrefs.AuthBackend.PASSWORD, email.trim());
            return response;
        } catch (HttpResponseStatusException ex) {
            if (ex.getStatusCode() >= 400 && ex.getStatusCode() < 500) {
                throw new AuthException(ex);
            }
            throw ex;
        }
    }

    @NonNull
    public AuthResponse logInUsingFacebook(String accessToken) throws Exception {
        final AuthResponse response = serviceManager.loginByFacebook(accessToken);
        finishSocialLogIn(response, LoginPrefs.AuthBackend.FACEBOOK);
        return response;
    }

    @NonNull
    public AuthResponse logInUsingGoogle(String accessToken) throws Exception {
        final AuthResponse response = serviceManager.loginByGoogle(accessToken);
        finishSocialLogIn(response, LoginPrefs.AuthBackend.GOOGLE);
        return response;
    }

    private void finishSocialLogIn(@NonNull AuthResponse response, @NonNull LoginPrefs.AuthBackend authBackend) throws Exception {
        if (response.error != null && response.error.equals("401")) {
            throw new AccountNotLinkedException();
        }
        finishLogIn(response, authBackend, "");
    }

    private void finishLogIn(@NonNull AuthResponse response, @NonNull LoginPrefs.AuthBackend authBackend, @NonNull String usernameUsedToLogIn) throws Exception {
        loginPrefs.storeAuthTokenResponse(response, authBackend);
        try {
            response.profile = serviceManager.getProfile();
        } catch (Throwable e) {
            // The app doesn't properly handle the scenario that we are logged in but we don't have
            // a cached profile. So if we fail to fetch the profile, let's erase the stored token.
            // TODO: A better approach might be to fetch the profile *before* storing the token.
            loginPrefs.clearAuthTokenResponse();
            throw e;
        }
        loginPrefs.setLastAuthenticatedEmail(usernameUsedToLogIn);
        segment.identifyUser(
                response.profile.id.toString(),
                response.profile.email,
                usernameUsedToLogIn);
        final String backendKey = loginPrefs.getAuthBackendKeyForSegment();
        if (backendKey != null) {
            segment.trackUserLogin(backendKey);
        }
        notificationDelegate.resubscribeAll();
        logInEvents.sendData(new LogInEvent());
    }

    private void register(@NonNull Bundle parameters) throws Exception {
        final RegisterResponse res = serviceManager.register(parameters);
        if (!res.isSuccess()) {
            throw new RegistrationException(res);
        }
    }

    @NonNull
    public AuthResponse registerUsingEmail(@NonNull Bundle parameters) throws Exception {
        register(parameters);
        return logInUsingEmail(parameters.getString("username"), parameters.getString("password"));
    }

    @NonNull
    public AuthResponse registerUsingGoogle(@NonNull Bundle parameters, @NonNull String accessToken) throws Exception {
        register(parameters);
        return logInUsingGoogle(accessToken);
    }

    @NonNull
    public AuthResponse registerUsingFacebook(@NonNull Bundle parameters, @NonNull String accessToken) throws Exception {
        register(parameters);
        return logInUsingFacebook(accessToken);
    }

    @NonNull
    public Observable<LogInEvent> getLogInEvents() {
        return logInEvents;
    }

    public static class AccountNotLinkedException extends Exception {
    }

    public static class RegistrationException extends Exception {
        @NonNull
        private final RegisterResponse registerResponse;

        public RegistrationException(@NonNull RegisterResponse registerResponse) {
            this.registerResponse = registerResponse;
        }

        @NonNull
        public RegisterResponse getRegisterResponse() {
            return registerResponse;
        }
    }
}
