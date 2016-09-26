package org.edx.mobile.util.links;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class EdxCourseInfoLink {
    public static final String SCHEME = "edxapp";
    public static final String AUTHORITY = "course_info";
    public static final String PATH_ID_PARAMETER_NAME = "path_id";
    public static final String PATH_ID_COURSE_PREFIX = "course/";

    @NonNull
    public final String pathId;

    public EdxCourseInfoLink(@NonNull String pathId) {
        this.pathId = pathId;
    }

    @Nullable
    public static EdxCourseInfoLink parse(@Nullable String uriStr) {
        if (TextUtils.isEmpty(uriStr)) {
            return null;
        }
        final Uri uri = Uri.parse(uriStr);
        if (!(SCHEME.equals(uri.getScheme()) && AUTHORITY.equals(uri.getAuthority()))) {
            return null;
        }
        String pathId = uri.getQueryParameter(PATH_ID_PARAMETER_NAME);
        if (TextUtils.isEmpty(pathId)) {
            return null;
        }
        if (pathId.startsWith(PATH_ID_COURSE_PREFIX)) {
            pathId = pathId.substring(PATH_ID_COURSE_PREFIX.length()).trim();
        }
        if (TextUtils.isEmpty(pathId)) {
            return null;
        }
        return new EdxCourseInfoLink(pathId);
    }
}
