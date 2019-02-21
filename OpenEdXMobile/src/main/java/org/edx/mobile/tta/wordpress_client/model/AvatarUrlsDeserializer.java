package org.edx.mobile.tta.wordpress_client.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Arjun Singh
 *         Created on 2016/01/07.
 */
public class AvatarUrlsDeserializer extends TypeAdapter<Map<String, String>> {

    private static final String TYPE_24 = "24";
    private static final String TYPE_48 = "48";
    private static final String TYPE_96 = "96";

    @Override
    public void write(JsonWriter out, Map<String, String> value) throws IOException {

    }

    @Override
    public Map<String, String> read(JsonReader in) throws IOException {
        Map<String, String> map = new HashMap<>();

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();

            if (name.equals(TYPE_24)) {
                map.put(TYPE_24, in.nextString());
            } else if (name.equals(TYPE_48)) {
                map.put(TYPE_48, in.nextString());
            } else if (name.equals(TYPE_96)) {
                map.put(TYPE_96, in.nextString());
            } else {
                in.skipValue();
            }
        }
        in.endObject();

        return map;
    }
}
