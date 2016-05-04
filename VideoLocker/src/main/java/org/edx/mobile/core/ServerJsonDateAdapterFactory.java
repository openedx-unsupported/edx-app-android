package org.edx.mobile.core;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.edx.mobile.util.DateUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ServerJsonDateAdapterFactory implements TypeAdapterFactory {

    private final List<SimpleDateFormat> dateFormatters;

    public ServerJsonDateAdapterFactory() {
        dateFormatters = new LinkedList<>();
        dateFormatters.add(new SimpleDateFormat(DateUtil.ISO_8601_DATE_TIME_FORMAT, Locale.US));
        dateFormatters.add(new SimpleDateFormat(DateUtil.ISO_8601_DATE_TIME_WITH_MICROSECONDS_FORMAT, Locale.US));

        for(SimpleDateFormat formatter : dateFormatters) {
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType()!= Date.class) {
            return null;
        }

        TypeAdapter<Date> defaultAdapter = (TypeAdapter<Date>) gson.getDelegateAdapter(this, type);
        return (TypeAdapter<T>) new ServerJsonDateAdapter(defaultAdapter, dateFormatters);
    }

    final class ServerJsonDateAdapter extends TypeAdapter<Date> {
        private final TypeAdapter<Date> defaultAdapter;
        private final List<SimpleDateFormat> dateFormatters;

        ServerJsonDateAdapter(TypeAdapter<Date> defaultAdapter, List<SimpleDateFormat> dateFormatters) {
            this.defaultAdapter = defaultAdapter;
            this.dateFormatters = dateFormatters;
        }


        @Override
        public void write(JsonWriter out, Date value) throws IOException {
            defaultAdapter.write(out, value);
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            String dateString = in.nextString();

            for (SimpleDateFormat dateFormat : dateFormatters) {
                // date formatters aren't thread safe, so sync on them
                synchronized(dateFormat) {
                    try {
                        return dateFormat.parse(dateString);
                    } catch (ParseException ignored) {
                        // suppress it and try the next loop item
                    }
                }
            }
            throw new JsonSyntaxException("Couldn't parse date: '" + dateString + "'");
        }
    }
}
