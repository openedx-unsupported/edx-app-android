package org.edx.mobile.util.images;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.StartType;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.ResourceUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public enum CourseCardUtils {
    ;
    public static final long SEVEN_DAYS_IN_MILLIS = 604800000L;

    /**
     * Checks if the given date is past today.
     *
     * @param today     Today's date.
     * @param otherDate Other date to cross-match with today's date.
     * @return <code>true</code> if the other date is past today,
     * <code>false</code> otherwise.
     */
    public static boolean isDatePassed(@NonNull Date today, @Nullable String otherDate) {
        return otherDate != null && today.after(DateUtil.convertToDate(otherDate));
    }

    public static String getFormattedDate(Context context, String start, String end, StartType start_type, String start_display) {
        return getFormattedDate(context, new Date(), null, start, end, start_type, start_display);
    }

    /**
     * Provides a formatted date string taking various dates related to a course in account like
     * start date, end date and expiry date.
     * <br>Example outputs:
     * <br>Starting January 15
     * <br>Course access expires in 4 days
     * <br>Course access expired on February 15
     * <br>Ending February 15
     *
     * @param context       The context.
     * @param today         Today's date.
     * @param expiry        Expiry date.
     * @param start         Start date.
     * @param end           End date.
     * @param start_type    Tell's if a course's start date is formatted, unformatted or unset.
     * @param start_display Tell's how the server represents the start date based on start_type.
     * @return A formatted date.
     */
    public static String getFormattedDate(Context context, Date today, String expiry, String start, String end, StartType start_type, String start_display) {
        final CharSequence formattedDate;
        // check if "start" date has passed
        if (isDatePassed(today, start)) {
            if (expiry != null) {
                final Date expiryDate = DateUtil.convertToDate(expiry);
                final long dayDifferenceInMillies;
                if (today.after(expiryDate)) {
                    dayDifferenceInMillies = today.getTime() - expiryDate.getTime();
                } else {
                    dayDifferenceInMillies = expiryDate.getTime() - today.getTime();
                }

                // check if "expiry" date has passed
                if (isDatePassed(today, expiry)) {
                    if (dayDifferenceInMillies > SEVEN_DAYS_IN_MILLIS) {
                        formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                                .label_expired_on, "date", DateUtil.formatDateWithNoYear(expiryDate.getTime()));
                    } else {
                        final CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(expiryDate.getTime(), today.getTime(),
                                DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString().toLowerCase();
                        formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                                .label_expired, "date", timeSpan);
                    }
                } else {
                    if (dayDifferenceInMillies > SEVEN_DAYS_IN_MILLIS) {
                        formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                                .label_expires_on, "date", DateUtil.formatDateWithNoYear(expiryDate.getTime()));
                    } else {
                        final CharSequence timeSpan = DateUtils.getRelativeTimeSpanString(expiryDate.getTime(), today.getTime(),
                                DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString().toLowerCase();
                        formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                                .label_expires, "date", timeSpan);
                    }
                }
            } else {
                final Date endDate = DateUtil.convertToDate(end);
                if (endDate == null) {
                    return null;
                } else if (isDatePassed(today, end)) {
                    // if "end" date has passed
                    formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                            .label_ended, "date", DateUtil.formatDateWithNoYear(endDate.getTime()));
                } else {
                    formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                            .label_ending, "date", DateUtil.formatDateWithNoYear(endDate.getTime()));
                }
            }
        } else {
            if (start_type == StartType.TIMESTAMP && !TextUtils.isEmpty(start)) {
                final Date startDate = DateUtil.convertToDate(start);
                formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                        .label_starting, "date", DateUtil.formatDateWithNoYear(startDate.getTime()));
            } else if (start_type == StartType.STRING && !TextUtils.isEmpty(start_display)) {
                formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                        .label_starting, "date", start_display);

            } else {
                formattedDate = ResourceUtil.getFormattedString(context.getResources(), R.string
                        .label_starting, "date", context.getString(R.string.assessment_soon));
            }
        }

        return formattedDate.toString();
    }

    public static String getFormattedDate(@NonNull Context context, @NonNull EnrolledCoursesResponse enrolledCourse) {
        final CourseEntry course = enrolledCourse.getCourse();
        return CourseCardUtils.getFormattedDate(
                context,
                new Date(),
                enrolledCourse.getAuditAccessExpires(),
                course.getStart(),
                course.getEnd(),
                course.getStartType(),
                course.getStartDisplay());
    }

    public static String getFormattedDate(@NonNull Context context, @NonNull CourseEntry course) {
        return CourseCardUtils.getFormattedDate(
                context,
                course.getStart(),
                course.getEnd(),
                course.getStartType(),
                course.getStartDisplay());
    }

    public static String getFormattedDate(@NonNull Context context, @NonNull CourseDetail course) {
        return CourseCardUtils.getFormattedDate(
                context,
                course.start,
                course.end,
                course.start_type,
                course.start_display);
    }

    public static String getDescription(String org, String number, String formattedStartDate) {
        List<CharSequence> sections = new ArrayList<>();

        if (!TextUtils.isEmpty(org)) {
            sections.add(org);
        }

        if (!TextUtils.isEmpty(number)) {
            sections.add(number);
        }

        if (null != formattedStartDate) {
            sections.add(formattedStartDate);
        }

        return TextUtils.join(" | ", sections);
    }
}
