package org.edx.mobile.tta.utils;

import com.google.gson.Gson;

public class JsonUtil {

    public static String objectToJson(Object object){
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    public static <T> T jsonToObject(String json, Class<T> tClass){
        Gson gson = new Gson();
        return gson.fromJson(json, tClass);
    }

}
