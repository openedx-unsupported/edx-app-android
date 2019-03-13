package org.edx.mobile.deeplink;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.edx.mobile.deeplink.Screen.COURSE_DASHBOARD;
import static org.edx.mobile.deeplink.Screen.COURSE_VIDEOS;
import static org.edx.mobile.deeplink.Screen.PROFILE;

/**
 * Denotes that a String parameter, field or method return value is expected
 * to be a String reference (e.g. {@link Screen#COURSE_DASHBOARD}).
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({COURSE_DASHBOARD, COURSE_VIDEOS, PROFILE})
public @interface ScreenDef {
}
