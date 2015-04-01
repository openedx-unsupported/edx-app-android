package org.edx.mobile.module.serverapi.parser;

import com.google.gson.Gson;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohan on 2/6/15.
 */
class GsonParser implements IParser {

    private Gson gson = new Gson();

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
        Type type = com.google.gson.internal.$Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, cls);
        List<T> list = gson.fromJson(json, type);
        return list;
    }
}
