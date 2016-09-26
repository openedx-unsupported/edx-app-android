package org.edx.mobile.util.links;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class EdxEnrollLink {
    public static final String SCHEME = "edxapp";
    public static final String AUTHORITY = "enroll";
    public static final String COURSE_ID_PARAMETER_NAME = "course_id";
    public static final String EMAIL_OPT_IN_PARAMETER_NAME = "email_opt_in";

    @NonNull
    public final String courseId;
    public final boolean emailOptIn;

    public EdxEnrollLink(@NonNull String courseId, boolean emailOptIn) {
        this.courseId = courseId;
        this.emailOptIn = emailOptIn;
    }

    /**
     * @return An EdxEnrollLink if uri is valid and contains a course ID, otherwise null
     */
    @Nullable
    public static EdxEnrollLink parse(@Nullable String uriStr) {
        if (TextUtils.isEmpty(uriStr)) {
            return null;
        }
        {
            // The edx.org course catalog does not encode plus signs in the course_id parameter
            // We encode them here before parsing the URI, to prevent conversion to space characters
            // TODO: Remove this hack once it's fixed on the server: https://openedx.atlassian.net/browse/MA-1901
            uriStr = uriStr.replace("+", "%2B");
        }
        final Uri uri = Uri.parse(uriStr);
        if (!(SCHEME.equals(uri.getScheme()) && AUTHORITY.equals(uri.getAuthority()))) {
            return null;
        }
        final String courseId = uri.getQueryParameter(COURSE_ID_PARAMETER_NAME);
        if (TextUtils.isEmpty(courseId)) {
            return null;
        }
        return new EdxEnrollLink(
                courseId,
                Boolean.parseBoolean(uri.getQueryParameter(EMAIL_OPT_IN_PARAMETER_NAME)));
    }
}
