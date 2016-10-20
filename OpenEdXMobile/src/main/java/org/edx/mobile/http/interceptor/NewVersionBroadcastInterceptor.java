package org.edx.mobile.http.interceptor;

import org.edx.mobile.event.NewVersionAvailableEvent;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.Version;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * An OkHttp interceptor that checks for information about app
 * updates in the response headers, and broadcasts them on the event
 * bus if found.
 */
public class NewVersionBroadcastInterceptor implements Interceptor {
    // The header constants are public so that they can be
    // accessed from the tests.
    /**
     * Header field name for the latest version number of
     * the app that is available in the app stores.
     */
    public static final String HEADER_APP_LATEST_VERSION =
            "EDX-APP-LATEST-VERSION";
    /**
     * Header field name for the last date up to which the API used by
     * the current version of the app will be supported and maintained.
     */
    public static final String HEADER_APP_VERSION_LAST_SUPPORTED_DATE =
            "EDX-APP-VERSION-LAST-SUPPORTED-DATE";

    /**
     * The logger for this class.
     */
    private final Logger logger = new Logger(NewVersionBroadcastInterceptor.class);

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Response response = chain.proceed(chain.request());

        final Version appLatestVersion;
        {
            final String appLatestVersionString = response.header(HEADER_APP_LATEST_VERSION);
            if (appLatestVersionString == null) {
                appLatestVersion = null;
            } else {
                try {
                    appLatestVersion = new Version(appLatestVersionString);
                } catch (ParseException e) {
                    /* If the version number doesn't correspond to the
                     * schema, then discard the data and just return the
                     * response.
                     */
                    logger.error(e, true);
                    return response;
                }
            }
        }

        final Date lastSupportedDate = DateUtil.convertToDate(
                response.header(HEADER_APP_VERSION_LAST_SUPPORTED_DATE));

        final boolean isUnsupported = response.code() == HttpStatus.UPGRADE_REQUIRED;

        /* If the server is returning an error due to the app being no longer supported, then
         * let the response error handler deal with it, since it may want to overlay the error
         * message over the content area.
         */
        if (!isUnsupported) {
            // Pass these properties to the event broadcaster to validate and post if not
            // already posted.
            NewVersionAvailableEvent.post(appLatestVersion, lastSupportedDate, isUnsupported);
        }

        return response;
    }
}
