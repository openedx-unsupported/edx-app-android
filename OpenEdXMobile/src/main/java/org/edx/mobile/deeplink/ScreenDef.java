package org.edx.mobile.deeplink;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.edx.mobile.deeplink.Screen.*;

/**
 * Denotes that a String parameter, field or method return value is expected
 * to be a String reference (e.g. {@link Screen#COURSE_DASHBOARD}).
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({COURSE_DASHBOARD, COURSE_VIDEOS, COURSE_DISCUSSION, COURSE_DATES, COURSE_HANDOUT,
        COURSE_ANNOUNCEMENT, PROGRAM, ACCOUNT, SETTINGS, PROFILE, COURSE_DISCOVERY,
        PROGRAM_DISCOVERY, DEGREE_DISCOVERY, DISCUSSION_POST, DISCUSSION_TOPIC})
public @interface ScreenDef {
}
