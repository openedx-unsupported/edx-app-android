package org.edx.mobile.test;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.edx.mobile.core.ServerJsonDateAdapterFactory;
import org.edx.mobile.util.DateUtil;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class ServerJsonDateAdapterFactoryTest {

    private Gson newGson() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat(DateUtil.ISO_8601_DATE_TIME_FORMAT)
                .registerTypeAdapterFactory(new ServerJsonDateAdapterFactory())
                .serializeNulls()
                .create();
        return gson;
    }

    @Test
    public void testParsingNormalDate() {
        String dateString = "\"2014-11-06T20:16:45Z\"";
        Gson gson = newGson();
        Date date = gson.fromJson(dateString, Date.class);

        assertNotNull(date);
    }

    @Test
    public void testParsingMicrosecondsDate() {
        String dateString = "\"2014-11-06T20:16:45.232333Z\"";
        Gson gson = newGson();
        Date date = gson.fromJson(dateString, Date.class);

        assertNotNull(date);
    }

    @Test
    public void testParsingNonsenseRaises() {
        String dateString = "foobar";
        Gson gson = newGson();

        try {
            Date date = gson.fromJson(dateString, Date.class);
            fail("Parsing should raise an exception");
        }
        catch (JsonSyntaxException exception) {
            // expecting failure
        }
    }

    @Test
    public void testSerializingUsesISO8601() throws ParseException {
        Date baseDate = new Date();
        Gson gson = newGson();
        // remove the quotes surrounding the date since it's a json string
        String dateString = gson.toJson(baseDate).replace("\"", "");
        SimpleDateFormat formatter = new SimpleDateFormat(DateUtil.ISO_8601_DATE_TIME_FORMAT);
        Date date = formatter.parse(dateString);

        // Can't the compare the dates directly since they might have different values at sub second precision
        // so compare them componentwise for the fields we care about

        Calendar baseCalendar = Calendar.getInstance();
        baseCalendar.setTime(baseDate);

        Calendar parsedCalendar = Calendar.getInstance();
        parsedCalendar.setTime(date);

        assertEquals(parsedCalendar.get(Calendar.YEAR), baseCalendar.get(Calendar.YEAR));
        assertEquals(parsedCalendar.get(Calendar.MONTH), baseCalendar.get(Calendar.MONTH));
        assertEquals(parsedCalendar.get(Calendar.DAY_OF_MONTH), baseCalendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(parsedCalendar.get(Calendar.MINUTE), baseCalendar.get(Calendar.MINUTE));
        assertEquals(parsedCalendar.get(Calendar.HOUR), baseCalendar.get(Calendar.HOUR));
        assertEquals(parsedCalendar.get(Calendar.SECOND), baseCalendar.get(Calendar.SECOND));
    }

}
