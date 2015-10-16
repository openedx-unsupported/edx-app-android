package org.edx.mobile.util;

import android.support.annotation.NonNull;

import java.util.Locale;

public class LocaleUtils {
    public static String getCountryNameFromCode(@NonNull String countryCode) {
        return new Locale("", countryCode).getDisplayCountry();
    }

    public static String getLanguageNameFromCode(@NonNull String languageCode) {
        return new Locale(languageCode).getDisplayLanguage();
    }
}
