package org.edx.mobile.deeplink;

import static org.edx.mobile.deeplink.Screen.*;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes that a String parameter, field or method return value is expected
 * to be a String reference (e.g. {@link Screen#COURSE_DASHBOARD}).
 */
@Retention(RetentionPolicy.SOURCE)
@StringDef({COURSE_DASHBOARD, COURSE_VIDEOS, COURSE_DISCUSSION, COURSE_DATES, COURSE_HANDOUT,
        COURSE_ANNOUNCEMENT, PROGRAM, PROFILE, USER_PROFILE, DISCOVERY, DISCOVERY_COURSE_DETAIL,
        DISCOVERY_PROGRAM_DETAIL, DISCUSSION_POST, DISCUSSION_TOPIC, DELETE_ACCOUNT, TERMS_OF_SERVICE,
        PRIVACY_POLICY})
public @interface ScreenDef {
}
