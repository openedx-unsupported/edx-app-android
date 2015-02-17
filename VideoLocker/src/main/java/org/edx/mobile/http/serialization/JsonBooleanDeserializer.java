package org.edx.mobile.http.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by yervant on 1/19/15.
 */
public class JsonBooleanDeserializer implements JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            String value = json.getAsJsonPrimitive().getAsString();
            return value.toLowerCase().equals("true");
        } catch (ClassCastException e) {
            throw new JsonParseException("Cannot parse json date '" + json.toString() + "'", e);
        }
    }

    public static Gson getCaseInsensitiveBooleanGson(){

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Boolean.class, new JsonBooleanDeserializer());
        Gson gson = builder.create();

        return gson;
    }
}
