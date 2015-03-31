package org.edx.mobile.module.serverapi.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by rohan on 2/6/15.
 */
class GsonParser implements IParser {

    private Gson gson = new GsonBuilder().create();

    @Override
    public <T> T parseObject(String json, Class<T> cls) throws Exception {
        return gson.fromJson(json, cls);
    }

    @Override
    public <T> T parseObject(Reader reader, Class<T> cls) throws Exception {
        return gson.fromJson(reader, cls);
    }

    @Override
    public <T> List<T> parseList(String json, final Class<T> cls) throws Exception {
        Type listType = new TypeToken<List<T>>() {}.getType();
        List<T> list = gson.fromJson(json, listType);
        return list;
    }
}
