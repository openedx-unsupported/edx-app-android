package org.edx.mobile.util;

import android.support.annotation.NonNull;

import java.util.Locale;

public class LocaleUtils {

    public static String getCountryNameFromCode(@NonNull String countryCode) throws InvalidLocaleException {
        final String uppercaseRegion = countryCode.toUpperCase(Locale.ROOT);
        if (!isValidBcp47Alpha(uppercaseRegion, 2, 2)) {
            throw new InvalidLocaleException("Invalid region: " + countryCode);
        }
        return new Locale("", countryCode).getDisplayCountry();
    }

    public static String getLanguageNameFromCode(@NonNull String languageCode) throws InvalidLocaleException {
        final String lowercaseLanguage = languageCode.toLowerCase(Locale.ROOT);

        switch (lowercaseLanguage) {
            /* Chinese languages are special cases. The server uses different codes when compared
            to Android's locale library. Additionally, zh_CN and zh_TW are not languageCodes that
            Android's locale recognizes as exceptions. */
            case "zh_cn":
            case "zh_hans":
                return Locale.SIMPLIFIED_CHINESE.getDisplayLanguage();
            case "zh_tw":
            case "zh_hant":
                return Locale.TRADITIONAL_CHINESE.getDisplayLanguage();
            default:
                if (!isValidBcp47Alpha(lowercaseLanguage, 2, 3)) {
                    throw new InvalidLocaleException("Invalid language: " + languageCode);
                }
                return new Locale(languageCode).getDisplayLanguage();
        }
    }

    /*
     * Copied from Locale.Builder in API 21
     * https://github.com/google/j2objc/blob/master/jre_emul/android/platform/libcore/ojluni/src/main/java/java/util/Locale.java#L1766
     */
    private static boolean isValidBcp47Alpha(String string, int lowerBound, int upperBound) {
        final int length = string.length();
        if (length < lowerBound || length > upperBound) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            final char character = string.charAt(i);
            if (!(character >= 'a' && character <= 'z' ||
                    character >= 'A' && character <= 'Z')) {
                return false;
            }
        }
        return true;
    }
}
