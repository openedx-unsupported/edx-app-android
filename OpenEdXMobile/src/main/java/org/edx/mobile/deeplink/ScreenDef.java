package org.edx.mobile.deeplink;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.edx.mobile.deeplink.Screen.ACCOUNT;
import static org.edx.mobile.deeplink.Screen.COURSE_DASHBOARD;
import static org.edx.mobile.deeplink.Screen.COURSE_DISCOVERY;
import static org.edx.mobile.deeplink.Screen.COURSE_VIDEOS;
import static org.edx.mobile.deeplink.Screen.PROFILE;
import static org.edx.mobile.deeplink.Screen.PROGRAM;
import static org.edx.mobile.deeplink.Screen.PROGRAM_DISCOVERY;

/**
 * Denotes that a String parameter, field or method return value is expected
 * to be a String reference (e.g. {@link Screen#COURSE_DASHBOARD}).
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({COURSE_DASHBOARD, COURSE_VIDEOS, PROGRAM, ACCOUNT, PROFILE, COURSE_DISCOVERY, PROGRAM_DISCOVERY})
public @interface ScreenDef {
}
