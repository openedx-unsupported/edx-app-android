package org.edx.mobile.test;

import org.edx.mobile.util.LocaleUtils;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class LocaleUtilsTest extends BaseTestCase {
    @Test
    public void testLanguageCodeFromLocale() {
        final Locale nonSpanish = Locale.US;
        final Locale latinAmericanSpanish = new Locale("es-419");
        final Locale argentinianSpanish = new Locale("es-AR");
        assertEquals("en", LocaleUtils.getLanguageCodeFromLocale(nonSpanish));
        assertEquals("es-419", LocaleUtils.getLanguageCodeFromLocale(latinAmericanSpanish));
        assertEquals("es-419", LocaleUtils.getLanguageCodeFromLocale(argentinianSpanish));
    }
}
