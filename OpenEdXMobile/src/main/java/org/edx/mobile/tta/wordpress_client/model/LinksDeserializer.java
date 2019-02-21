package org.edx.mobile.tta.wordpress_client.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the JSON '_links' field and converts it to an array list of link objects
 *
 * @author Arjun Singh
 *         Created on 2015/12/03.
 */
public class LinksDeserializer extends TypeAdapter<List<Link>> {

    private static final String NAME_SELF = "self";
    private static final String NAME_COLLECTION = "collection";
    private static final String NAME_AUTHOR = "author";
    private static final String NAME_REPLIES = "replies";
    private static final String NAME_VERSION_HISTORY = "version-history";
    private static final String NAME_FEATURED_MEDIA = "https://api.w.org/featuredmedia";
    private static final String NAME_ATTACHMENT = "https://api.w.org/attachment";
    private static final String NAME_TERMS = "https://api.w.org/term";
    private static final String NAME_META = "https://api.w.org/meta";

    private static final String NAME_FIELD_HREF = "href";
    private static final String NAME_FIELD_EMBEDDABLE = "embeddable"; // ?????
    private static final String NAME_FIELD_TAXONOMY = "taxonomy";


    @Override
    public void write(JsonWriter out, List<Link> value) throws IOException {

    }

    @Override
    public List<Link> read(JsonReader in) throws IOException {
        List<Link> links = new ArrayList<>();

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();

            // TODO all fields are arrays, but maybe check if not array

            if (name.equals(NAME_TERMS)) {
                //in.beginArray();
                in.skipValue();
            } else {
                in.beginArray();
                while (in.hasNext()) {
                    in.beginObject();
                    while (in.hasNext()) {
                        String fieldName = in.nextName();
                        if (fieldName.equals(NAME_FIELD_HREF)) {
                            Link link = new Link();
                            link.setTitle(name);
                            link.setHref(in.nextString());
                        } else {
                            in.skipValue();
                        }
                    }
                    in.endObject();
                }
                in.endArray();
            }
        }
        in.endObject();

        return links;
    }
}
