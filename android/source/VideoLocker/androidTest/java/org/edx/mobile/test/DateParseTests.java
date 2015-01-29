package org.edx.mobile.test;

import android.util.Log;

import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.Emailutill;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParseTests extends BaseTestCase {

    public void testGetModificationDate() throws Exception {
        // example output is :   "2014-11-20 22:10:54.569200+00:00"
        
        String date = DateUtil.getModificationDate();
        assertNotNull(date);
        print("modification date = " + date);
        assertTrue(date.length() == 32);
    }
    
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
            e.printStackTrace();
        }
        return null;
    }

    public void testDateCompare() throws Exception {
        String start = "1970-01-01T05:00:00Z";
        Date startDate = DateUtil.convertToDate(start);
        Date today = new Date();
        print("start=" + startDate.toString());
        print("today=" + today.toString());
        boolean started = today.after(startDate);
        print("started=" + started);
    }

    public void testEmail() throws Exception {
        String to = "user@edx.org";
        String subject = "edX Test Email";
        String email = "hi, this is test email";
        Emailutill.sendEmail(getInstrumentation()
                .getTargetContext(), to, subject, email);
        print("sending email...");
    }

    /**
     * This tests the formatted start date of a course
     * @throws Exception
     */
    public void testFormatCourseNotStartedDate() throws Exception {
        String inputDate = "2014-11-20T05:00:00Z";
        String expectedOutput = "November 20, 2014";

        String outputDate = DateUtil.formatCourseNotStartedDate(inputDate);
        assertNotNull(outputDate);
        assertEquals(expectedOutput,outputDate);
    }

}
