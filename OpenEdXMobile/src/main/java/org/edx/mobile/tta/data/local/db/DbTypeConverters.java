package org.edx.mobile.tta.data.local.db;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DbTypeConverters {

    @TypeConverter
    public static List<Long> fromString(String value) {
        Type listType = new TypeToken<List<Long>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(List<Long> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }

}
