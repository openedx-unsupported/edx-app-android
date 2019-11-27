package org.edx.mobile.http.serialization;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Gson adapter for converting between ISO 8601 date and (@link Date).
 */
/* Unfortunately, the Gson library doesn't provide a built-in ISO
 * 8601 date type adapter. The default date type adapters do fall
 * back to parsing the ISO 8601 pattern, but serialization is done to
 * a system-default or custom pattern using the local time zone (and
 * without recording the UTC offset).
 */
public final class ISO8601DateTypeAdapter extends TypeAdapter<Date> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings("unchecked") // Type equality is ensured at runtime.
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            return typeToken.getRawType() != Date.class ? null :
                    (TypeAdapter<T>) new ISO8601DateTypeAdapter().nullSafe();
        }
    };

    // Only allow the factory to instantiate this, to ensure that it's always wrapped
    // in a null-safe wrapper, which enables the removal of the boilerplate code.
    private ISO8601DateTypeAdapter() {}

    @Override
    public void write(@NonNull JsonWriter out, @NonNull Date date) throws IOException {
        out.value(ISO8601Utils.format(date, true));
    }

    @Override
    @NonNull
    public Date read(final JsonReader in) throws IOException {
        final String date = in.nextString();
        final ParsePosition parsePosition = new ParsePosition(0);
        try {
            return ISO8601Utils.parse(date, parsePosition);
        } catch (ParseException e) {
            throw new JsonSyntaxException(date, e);
        }
    }
}
