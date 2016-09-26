package org.edx.mobile.http.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.edx.mobile.model.Page;
import org.edx.mobile.model.PaginationData;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class JsonPageDeserializer implements JsonDeserializer<Page<?>> {
    @Override
    public Page<?> deserialize(JsonElement json, final Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final List<?> list = context.deserialize(json.getAsJsonObject().get("results"), new ParameterizedType() {
            public Type getRawType() {
                return List.class;
            }

            public Type getOwnerType() {
                return null;
            }

            public Type[] getActualTypeArguments() {
                return ((ParameterizedType) typeOfT).getActualTypeArguments();
            }
        });
        JsonElement paginationJson = json.getAsJsonObject().get("pagination");
        if (null == paginationJson || paginationJson.isJsonNull()) {
            paginationJson = json;
        }
        final PaginationData paginationData = context.deserialize(paginationJson, PaginationData.class);
        return new Page<>(paginationData, list);
    }
}
