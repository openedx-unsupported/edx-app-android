package org.edx.mobile.util;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.accessibility.CaptioningManager;

import org.edx.mobile.R;
import org.edx.mobile.annotation.Nullable;
import org.edx.mobile.model.api.TranscriptModel;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.user.FormOption;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class LocaleUtils {

    public static String getCountryNameFromCode(@NonNull String countryCode) throws InvalidLocaleException {
        final String uppercaseRegion = countryCode.toUpperCase(Locale.ROOT);
        if (!isValidBcp47Alpha(uppercaseRegion, 2, 2)) {
            throw new InvalidLocaleException("Invalid region: " + countryCode);
        }
        return new Locale("", countryCode).getDisplayCountry();
    }

    /**
     * Provides the list of countries fetched from the system.
     *
     * @return A list of {@link FormOption} containing country names and their codes.
     */
    public static List<FormOption> getCountries() {
        final List<FormOption> countries = new ArrayList<>();
        for (String countryCode : Locale.getISOCountries()) {
            try {
                countries.add(new FormOption(getCountryNameFromCode(countryCode), countryCode));
            } catch (InvalidLocaleException e) {
                e.printStackTrace();
            }
        }
        sortBasedOnLocale(countries);
        return countries;
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

    /**
     * Provides the list of languages fetched from the system.
     *
     * @return A list of {@link FormOption} containing language names and their codes.
     */
    public static List<FormOption> getLanguages() {
        final List<FormOption> languages = new ArrayList<>();
        for (String languageCode : Locale.getISOLanguages()) {
            try {
                final String languageName = getLanguageNameFromCode(languageCode);
                /*
                Ignore language codes that don't translate to a proper language name. This can be
                detected by case-sensitive matching of the code with its language name
                i.e. if both are same, its not a valid language.
                For example: language names like aa, ace, ada, ady, ae etc.

                This exemption is necessary because of following excerpt found inside
                Locale.getISOLanguages() function's javadoc:
                "The list returned by this method does not contain ALL valid codes that can be used
                to create Locales."

                ### Special case ###
                zxx = No linguistic content.
                */
                if (!languageCode.equals(languageName) && !languageCode.equals("zxx")) {
                    languages.add(new FormOption(languageName, languageCode));
                }
            } catch (InvalidLocaleException e) {
                e.printStackTrace();
            }
        }
        sortBasedOnLocale(languages);
        return languages;
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

    private static void sortBasedOnLocale(List<FormOption> options) {
        final Collator collator = Collator.getInstance(Locale.getDefault());
        collator.setStrength(Collator.PRIMARY);
        Collections.sort(options, (option1, option2) -> collator.compare(option1.getName(), option2.getName()));
    }

    /**
     * Default Language List
     *
     * @param transcript
     * @return list of languages available in transcript
     */
    public static LinkedHashMap<String, String> getLanguageList(TranscriptModel transcript) {
        if (transcript != null) {
            return transcript.getLanguageList();
        }
        return null;
    }

    /**
     * Utility method that return the Current language code of the device
     *
     * @param context - current {@link Context}of the application
     * @return current language code
     */
    public static String getCurrentDeviceLanguage(Context context) {
        final CaptioningManager captionManager = (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        String languageCode = null;
        // Check if captioning is enabled in accessibility settings
        if (captionManager.isEnabled()) {
            final Locale cManagerLocale = captionManager.getLocale();
            if (cManagerLocale != null) {
                languageCode = cManagerLocale.getLanguage();
            }
        }
        if (TextUtils.isEmpty(languageCode)) {
            languageCode = Locale.getDefault().getLanguage();
        }
        // Android return iw in case of Hebrew
        if ("iw".equals(languageCode)) {
            languageCode = "he";
        }
        return languageCode;
    }

    /**
     * Utility method to extract the download url from the {@link TranscriptModel} based on
     * the last select transcript language or current device language.
     *
     * @param context    - current application {@link Context}
     * @param transcript - {@link TranscriptModel} transcript model contains transcript info
     * @return downloadable transcript url that can be null if transcripts are not available
     */
    @Nullable
    public static String getTranscriptURL(@NonNull Context context,
                                          @NonNull TranscriptModel transcript) {
        String subtitleLanguage = new LoginPrefs(context).getSubtitleLanguage();
        if (subtitleLanguage == null ||
                subtitleLanguage.equals(context.getString(R.string.lbl_cc_none))) {
            subtitleLanguage = LocaleUtils.getCurrentDeviceLanguage(context);
        }
        String transcriptUrl = null;
        if (transcript.containsKey(subtitleLanguage)) {
            transcriptUrl = transcript.get(subtitleLanguage);
        } else if (transcript.entrySet().size() > 0) {
            if (transcript.containsKey(Locale.ENGLISH.getLanguage())) {
                transcriptUrl = transcript.get(Locale.ENGLISH.getLanguage());
            } else {
                transcriptUrl = transcript.entrySet().iterator().next().getValue();
            }
        }
        return transcriptUrl;
    }
}
