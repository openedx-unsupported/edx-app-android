package org.edx.mobile.util;

import android.content.Context;

import com.squareup.phrase.Phrase;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;


public class ResourceUtil {
    public static final String QuantityHolder = "quantity";


    public static String getResourceString(String name) {
        Context context = MainApplication.instance().getApplicationContext();
        return getResourceString(name, context);
    }

    /**
     * get the string resources dynamically
     * @param name
     * @param context
     * @return
     */
    public static String getResourceString(String name, Context context) {
        int nameResourceID = context.getResources().getIdentifier(name, "string", context.getApplicationInfo().packageName);
        if (nameResourceID == 0) {
            throw new IllegalArgumentException("No resource string found with name " + name);
        } else {
            return context.getString(nameResourceID);
        }
    }

    public static String getResourceString(int resourceId){
        Context context = MainApplication.instance().getApplicationContext();
        return context.getString(resourceId);
    }

    public static CharSequence getFormattedString(int resourceId, String key, String value){
        if ( value == null )
            value = "";
        return Phrase.from(ResourceUtil.getResourceString(resourceId))
                .put(key, value) .format();
    }

    /**
     * mock Android's built-in context.getResources().getQuantityString API.
     * by default, quantity holder will be "quantity"
     * @param resourceId
     * @param quantity
     * @return
     */
    public static CharSequence getFormattedStringForQuantity(int resourceId,  int quantity ){
        return getFormattedStringForQuantity(resourceId, QuantityHolder, quantity);
    }


    public static CharSequence getFormattedStringForQuantity(int resourceId, String key, int quantity){
        Context context = MainApplication.instance().getApplicationContext();
        String template = context.getResources().getQuantityString(resourceId,  quantity);
        return Phrase.from(template) .put(key, quantity + "") .format();
    }
}
