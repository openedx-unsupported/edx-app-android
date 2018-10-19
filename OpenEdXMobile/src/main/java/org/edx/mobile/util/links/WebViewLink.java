package org.edx.mobile.util.links;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.edx.mobile.util.UrlUtil;

import java.util.Map;

/**
 * Object class to parse and store links that we need within a WebView.
 */
public class WebViewLink {
    public static final String SCHEME = "edxapp";
    public static final String PATH_ID_COURSE_PREFIX = "course/";

    public enum Authority {
        COURSE_INFO("course_info"),
        ENROLL("enroll"),
        ENROLLED_PROGRAM_INFO("enrolled_program_info"),
        ENROLLED_COURSE_INFO("enrolled_course_info");

        private String key;

        Authority(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    public class Param {
        public static final String PATH_ID = "path_id";
        public static final String COURSE_ID = "course_id";
        public static final String EMAIL_OPT = "email_opt_in";
    }

    @NonNull
    public Authority authority;

    @NonNull
    public Map<String, String> params;

    public WebViewLink(@NonNull Authority authority, @NonNull Map<String, String> params) {
        this.authority = authority;
        this.params = params;
    }

    @Nullable
    public static WebViewLink parse(@Nullable String uriStr) {
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
        // Validate the URI scheme
        if (!(SCHEME.equals(uri.getScheme()))) {
            return null;
        }

        // Validate the URI authority
        Authority uriAuthority = null;
        for (Authority authority : Authority.values()) {
            if (authority.key.equals(uri.getAuthority())) {
                uriAuthority = authority;
                break;
            }
        }
        if (uriAuthority == null) {
            return null;
        }

        // Parse the URI params
        final Map<String, String> params = UrlUtil.getQueryParams(uri);

        return new WebViewLink(uriAuthority, params);
    }
}
