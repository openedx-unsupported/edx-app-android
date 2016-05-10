package org.edx.mobile.test;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.apache.commons.lang.time.DateUtils;
import org.edx.mobile.core.ServerJsonDateAdapterFactory;
import org.edx.mobile.util.DateUtil;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
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

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        assertEquals(2014, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, calendar.get(Calendar.MONTH));
        assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(20, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(16, calendar.get(Calendar.MINUTE));
        assertEquals(45, calendar.get(Calendar.SECOND));
    }

    @Test
    public void testParsingMicrosecondsDate() {
        String dateString = "\"2014-11-06T20:16:45.232333Z\"";
        Gson gson = newGson();
        Date date = gson.fromJson(dateString, Date.class);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        assertEquals(2014, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, calendar.get(Calendar.MONTH));
        assertEquals(6, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(20, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(16, calendar.get(Calendar.MINUTE));
        assertEquals(45, calendar.get(Calendar.SECOND));
    }

    @Test
    public void testParsingNull() {
        String dateString = "null";
        Gson gson = newGson();
        Date date = gson.fromJson(dateString, Date.class);

        assertEquals(null, date);
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

        baseDate = DateUtils.truncate(baseDate, Calendar.SECOND);
        assertEquals(baseDate.getTime(), date.getTime());
    }

}
