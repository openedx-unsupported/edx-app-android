package org.edx.mobile.tta.wordpress_client.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * @author Arjun Singh
 *         Created on 2015/12/03.
 */
public class StatusDeserializer extends TypeAdapter<WPStatus> {

    @Override
    public void write(JsonWriter out, WPStatus value) throws IOException {

    }

    @Override
    public WPStatus read(JsonReader in) throws IOException {
        WPStatus status = new WPStatus();

        String str = in.nextString();

        if (str.equals("closed")) {
            status.setStatus(WPStatus.CLOSED);
        } else if (str.equals("open")) {
            status.setStatus(WPStatus.OPEN);
        }

        return status;
    }
}
