package org.edx.mobile.http;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.event.NewVersionAvailableEvent;
import org.edx.mobile.third_party.versioning.ComparableVersion;
import org.edx.mobile.util.DateUtil;

import java.io.IOException;
import java.util.Date;

import de.greenrobot.event.EventBus;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * An OkHttp interceptor that checks for information about app
 * updates in the response headers, and broadcasts them on the event
 * bus if found.
 */
public class NewVersionBroadcastInterceptor implements Interceptor {
    /**
     * Header field name for the latest version number of the app
     * that is available in the app stores.
     */
    private static final String HEADER_APP_LATEST_VERSION =
            "EDX-APP-LATEST-VERSION";
    /**
     * Header field name for the last date up to which the API used
     * by the current version of the app will be supported and
     * maintained.
     */
    private static final String HEADER_APP_VERSION_LAST_SUPPORTED_DATE =
            "EDX-APP-VERSION-LAST-SUPPORTED-DATE";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        String appLatestVersion = response.header(HEADER_APP_LATEST_VERSION);
        if (appLatestVersion != null && new ComparableVersion(appLatestVersion)
                .compareTo(new ComparableVersion(BuildConfig.VERSION_NAME)) > 0) {
            final String lastSupportedDateString =
                    response.header(HEADER_APP_VERSION_LAST_SUPPORTED_DATE);
            final Date lastSupportedDate = lastSupportedDateString == null ?
                    null : DateUtil.convertToDate(lastSupportedDateString);
            EventBus.getDefault().postSticky(new NewVersionAvailableEvent(lastSupportedDate));
        }
        return response;
    }
}
