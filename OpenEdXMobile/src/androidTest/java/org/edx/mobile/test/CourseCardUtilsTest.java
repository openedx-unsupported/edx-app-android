package org.edx.mobile.test;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.api.StartType;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.images.CourseCardUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CourseCardUtilsTest {
    private Context context;
    private Date today;
    private String end;

    @Before
    public void setup() {
        context = MainApplication.instance();
        today = DateUtil.convertToDate("2018-12-01T00:00:00+05:00"); // Today: 1 December 2018
        end = "2019-12-01T00:00:00+05:00";
    }

    @Test
    public void testCourseStartingDateBeforeExpiry() {
        final String expectedOutput = "Starting December 5";
        final String expiry = "2018-12-30T00:00:00+05:00";
        final String start = "2018-12-05T00:00:00+05:00";
        final String output = CourseCardUtils.getFormattedDate(context, today, expiry, start, end, StartType.TIMESTAMP, null);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testCourseStartedAndExpiringInAWeek() {
        final String expectedOutput = "Course access expires in 6 days";
        final String expiry = "2018-12-07T00:00:00+05:00";
        final String start = "2018-11-01T00:00:00+05:00";
        final String output = CourseCardUtils.getFormattedDate(context, today, expiry, start, end, StartType.TIMESTAMP, null);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testCourseStartedAndExpiringInMoreThanAWeek() {
        final String expectedOutput = "Course access expires on December 9";
        final String expiry = "2018-12-09T00:00:00+05:00";
        final String start = "2018-11-01T00:00:00+05:00";
        final String output = CourseCardUtils.getFormattedDate(context, today, expiry, start, end, StartType.TIMESTAMP, null);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testCourseStartedAndExpiredAWeekAgo() {
        final String expectedOutput = "Course access expired 7 days ago";
        final String expiry = "2018-11-24T00:59:59+05:00";
        final String start = "2018-11-01T00:00:00+05:00";
        final String output = CourseCardUtils.getFormattedDate(context, today, expiry, start, end, StartType.TIMESTAMP, null);
        assertEquals(expectedOutput, output);
    }

    @Test
    public void testCourseStartedAndExpiredMoreThanAWeekAgo() {
        final String expectedOutput = "Course access expired on November 1";
        final String expiry = "2018-11-01T00:00:00+05:00";
        final String start = "2018-10-01T00:00:00+05:00";
        final String output = CourseCardUtils.getFormattedDate(context, today, expiry, start, end, StartType.TIMESTAMP, null);
        assertEquals(expectedOutput, output);
    }
}
