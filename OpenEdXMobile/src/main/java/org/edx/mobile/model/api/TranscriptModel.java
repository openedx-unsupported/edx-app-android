package org.edx.mobile.model.api;

import androidx.annotation.NonNull;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.InvalidLocaleException;
import org.edx.mobile.util.LocaleUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class TranscriptModel extends HashMap<String, String> {
    @NonNull
    private final Logger logger = new Logger(getClass().getName());

    public LinkedHashMap<String, String> getLanguageList() {
        LinkedHashMap<String, String> languageArray = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : this.entrySet()) {
            if (entry.getValue() != null) {
                try {
                    languageArray.put(entry.getKey(), LocaleUtils.getLanguageNameFromCode(entry.getKey()));
                } catch (InvalidLocaleException e) {
                    logger.error(e, true);
                }
            }
        }
        return this.size() == 0 ? null : languageArray;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TranscriptModel that = (TranscriptModel) o;
        if (size() != that.size()) {
            return false;
        }

        for (Map.Entry<String, String> entry : this.entrySet()) {
            final String value = entry.getValue();
            final String thatValue = that.get(entry.getKey());
            if (value != null ? !value.equals(thatValue) : thatValue != null) {
                return false;
            }
        }
        return true;
    }
}
