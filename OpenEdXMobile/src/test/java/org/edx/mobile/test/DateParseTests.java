package org.edx.mobile.test;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.EmailUtil;
import org.junit.Test;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.assertj.core.api.Java6Assertions.fail;
import static org.junit.Assert.*;

public class DateParseTests extends BaseTestCase {

    @Test
    public void testGetCurrentTimeStamp() {
        String date = DateUtil.getCurrentTimeStamp();
        assertNotNull(date);
        print("modification date = " + date);
        // Verify that it's been formatted to an ISO 8601
        // compatible format.
        try {
            ISO8601Utils.parse(date, new ParsePosition(0));
        } catch (ParseException e) {
            fail("Invalid date format: '" + date +
                    "' is not an ISO 8601 compliant string", e);
        }
    }

    @Test
    public void testDateParsing() throws Exception {
        String start = "1970-01-01T05:00:00Z";
        Date date = toDate(start);
        assertTrue(date != null);
        print(date.toString());
    }

    private Date toDate(String strDate) {
        SimpleDateFormat input_format = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            Date date = input_format.parse(strDate);
            return date;
        } catch (ParseException e) {
            logger.error(e);
        }
        return null;
    }

    @Test
    public void testDateCompare() throws Exception {
        String start = "1970-01-01T05:00:00Z";
        Date startDate = DateUtil.convertToDate(start);
        Date today = new Date();
        print("start=" + startDate.toString());
        print("today=" + today.toString());
        boolean started = today.after(startDate);
        print("started=" + started);
    }



    /**
     * This tests the formatted start date of a course
     * @throws Exception
     */
    @Test
    public void testFormatCourseNotStartedDate() throws Exception {
        String inputDate = "2014-11-20T05:00:00Z";

        String outputDate = DateUtil.formatCourseNotStartedDate(inputDate);
        assertNotNull(outputDate);
        //FIXME - This check is currently only for English locale.
        //Need to have a more generic test case based on Locale
        if(Locale.getDefault().getLanguage().equals(Locale.ENGLISH)){
            String expectedOutput = "November 20, 2014";
            assertEquals(expectedOutput, outputDate);
        }
    }

}
