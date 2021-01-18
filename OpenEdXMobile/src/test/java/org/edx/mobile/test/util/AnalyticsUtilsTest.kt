package org.edx.mobile.test.util

import org.assertj.core.api.Assertions.assertThat
import org.edx.mobile.module.analytics.Analytics
import org.edx.mobile.util.AnalyticsUtils
import org.junit.Test

class AnalyticsUtilsTest {
    @Test
    fun removeUnSupportedCharactersTest() {
        assertThat(AnalyticsUtils.removeUnSupportedCharacters("A: B-C D__E")).isEqualTo("A_B_C_D_E")
        assertThat(AnalyticsUtils.removeUnSupportedCharacters(Analytics.Screens.APP_REVIEWS_VIEW_RATING)).isEqualTo("AppReviews_View_Rating")
        assertThat(AnalyticsUtils.removeUnSupportedCharacters(Analytics.Screens.PROFILE_CHOOSE_BIRTH_YEAR)).isEqualTo("Choose_Form_Value_Birth_year")
    }
}
