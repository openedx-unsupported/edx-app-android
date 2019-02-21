package org.edx.mobile.tta.wordpress_client.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Arjun Singh
 *         Created on 2016/03/03.
 */
public class DataConverters {

    private static SimpleDateFormat sPostDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss zz", Locale.US);

    public static final String JSON_ARRAY_CATEGORY_IDS = "categoryIds";
    public static final String JSON_ARRAY_TAG_IDS = "tagIds";

    public static String makeCategoryString(List<Long> categories) {
        return makeTaxonomyString(categories, JSON_ARRAY_CATEGORY_IDS);
    }

    public static List<Long> makeCategoryIdList(String categoryString) {
        return makeTaxonomyIdList(categoryString, JSON_ARRAY_CATEGORY_IDS);
    }

    public static String makeTagString(List<Long> tags) {
        return makeTaxonomyString(tags, JSON_ARRAY_TAG_IDS);
    }

    public List<Long> makeTagIdList(String tagString) {
        return makeTaxonomyIdList(tagString, JSON_ARRAY_TAG_IDS);
    }

    private static String makeTaxonomyString(List<Long> categories, String jsonName) {
        JSONObject object = new JSONObject();

        try {
            object.put(jsonName, new JSONArray(categories));

            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<Long> makeTaxonomyIdList(String categoryString, String jsonName) {
        if (TextUtils.isEmpty(categoryString)) {
            return new ArrayList<>();
        }

        try {
            JSONObject object = new JSONObject(categoryString);

            JSONArray array = object.getJSONArray(jsonName);

            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                ids.add(array.getLong(i));
            }
            return ids;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static long convertWpDateToLong(String dateInput) {
        try {
            if (dateInput != null) {
                return sPostDateFormat.parse(dateInput + " UTC").getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
