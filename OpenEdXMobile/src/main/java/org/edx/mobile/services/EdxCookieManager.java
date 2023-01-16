package org.edx.mobile.services;

import android.content.Context;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;

import org.edx.mobile.authentication.LoginService;
import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.event.SessionIdRefreshEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.Config;
import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.EntryPointAccessors;
import okhttp3.Cookie;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A central place for course data model transformation
 */
@Singleton
public class EdxCookieManager {

    // We'll assume that cookies are valid for at least one hour; after that
    // they'll be required on API levels lesser than Marshmallow (which
    // provides an error callback with the HTTP error code) prior to usage.
    private static final long FRESHNESS_INTERVAL = TimeUnit.HOURS.toMillis(1);
    /**
     * The cookie to set for the course upsell revenue workflow to work on mobile end.
     */
    private static final String REV_934_COOKIE = "REV_934=mobile; expires=Tue, 31 Dec 2021 12:00:20 GMT; domain=.edx.org;";
    /**
     * The cookie is set to retain its value after session refresh.
     */
    private static final String DATA_CONSENT_COOKIE = "edx_do_not_sell=true; domain=.edx.org;";

    private long authSessionCookieExpiration = -1;

    protected final Logger logger = new Logger(getClass().getName());

    private static EdxCookieManager instance;

    Config config;
    LoginService loginService;

    private Call<RequestBody> loginCall;

    @Inject
    public EdxCookieManager(Config config, LoginService loginService) {
        this.config = config;
        this.loginService = loginService;
    }

    public static synchronized EdxCookieManager getSharedInstance(@NonNull final Context context) {
        if (instance == null) {
            instance = EntryPointAccessors.fromApplication(context,
                    EdxDefaultModule.ProviderEntryPoint.class).getEdxCookieManager();
        }
        return instance;
    }

    public void clearAllCookies() {
        CookieManager.getInstance().removeAllCookies(null);
        authSessionCookieExpiration = -1;
    }

    /**
     * Clears all session cookies but retain the required cookies if available
     */
    public void clearAndRetainCookies() {
        String cookie = CookieManager.getInstance().getCookie(config.getApiHostURL());
        clearAllCookies();
        // The `edx_do_not_sell` cookie relates to the Data Sell Consent Policy and we should retain
        // it if we are only refreshing the session cookies
        if (cookie != null && cookie.contains("edx_do_not_sell"))
            setDataConsentCookie();
    }

    public synchronized void tryToRefreshSessionCookie() {
        if (loginCall == null || loginCall.isCanceled()) {
            loginCall = loginService.login();
            loginCall.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull final Call<RequestBody> call,
                                       @NonNull final Response<RequestBody> response) {
                    clearAndRetainCookies();
                    final CookieManager cookieManager = CookieManager.getInstance();
                    for (Cookie cookie : Cookie.parseAll(
                            call.request().url(), response.headers())) {
                        cookieManager.setCookie(config.getApiHostURL(), cookie.toString());
                    }
                    authSessionCookieExpiration = System.currentTimeMillis() + FRESHNESS_INTERVAL;
                    EventBus.getDefault().post(new SessionIdRefreshEvent(true));
                    loginCall = null;
                }

                @Override
                public void onFailure(@NonNull final Call<RequestBody> call,
                                      @NonNull final Throwable error) {
                    EventBus.getDefault().post(new SessionIdRefreshEvent(false));
                    loginCall = null;
                }
            });
        }
    }

    public boolean isSessionCookieMissingOrExpired() {
        return authSessionCookieExpiration < System.currentTimeMillis();
    }

    /**
     * Ensures all cookies currently accessible through the getCookie API are written to persistent
     * storage. The flush() call will block the caller until it is done and may perform I/O.
     */
    public void retainSessionCookies() {
        CookieManager.getInstance().flush();
    }

    /**
     * Set a special cookie so that the server knows that the request for the course upsell
     * revenue workflow is coming from mobile end.
     */
    public void setMobileCookie() {
        CookieManager.getInstance().setCookie(config.getApiHostURL(), REV_934_COOKIE);
    }

    private void setDataConsentCookie() {
        CookieManager.getInstance().setCookie(config.getApiHostURL(), DATA_CONSENT_COOKIE);
        retainSessionCookies();
    }
}
