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

// Deals with dates using the formats we get from the LMS
public class ServerJsonDateAdapterFactory implements TypeAdapterFactory {

    private final SimpleDateFormat serverDateFormatter;

    public ServerJsonDateAdapterFactory() {
        serverDateFormatter = new SimpleDateFormat(DateUtil.ISO_8601_DATE_TIME_FORMAT, Locale.US);
        serverDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType()!= Date.class) {
            return null;
        }

        TypeAdapter<Date> defaultAdapter = (TypeAdapter<Date>) gson.getDelegateAdapter(this, type);
        return (TypeAdapter<T>) new ServerJsonDateAdapter(defaultAdapter, serverDateFormatter);
    }

    final class ServerJsonDateAdapter extends TypeAdapter<Date> {
        private final TypeAdapter<Date> defaultAdapter;
        private final SimpleDateFormat dateFormatter;

        ServerJsonDateAdapter(TypeAdapter<Date> defaultAdapter, SimpleDateFormat dateFormatter) {
            this.defaultAdapter = defaultAdapter;
            this.dateFormatter = dateFormatter;
        }


        @Override
        public void write(JsonWriter out, Date value) throws IOException {
            defaultAdapter.write(out, value);
        }

        @Override
        public Date read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else {
                String dateString = in.nextString();

                // date formatters aren't thread safe, so sync on them
                synchronized (dateFormatter) {
                    try {
                        return dateFormatter.parse(dateString);
                    } catch (ParseException ignored) {
                        // Sometimes the server sends us strings with extra microseconds
                        // ICU doesn't like those and we don't need the precision, so just strip them out
                        String withoutMicroseconds = dateString.replaceFirst("\\..*Z", "Z");
                        try {
                            return dateFormatter.parse(withoutMicroseconds);
                        } catch (ParseException e) {
                            // fall through to final failure
                        }
                    }
                }
                throw new JsonSyntaxException("Couldn't parse date: '" + dateString + "'");
            }
        }
    }
}
