package org.edx.mobile.model.video

import org.edx.mobile.R

enum class VideoQuality(val titleResId: Int) {
    AUTO(R.string.auto_recommended_text),
    OPTION_360P(R.string.p360_quality_option),
    OPTION_540P(R.string.p540_quality_option),
    OPTION_720P(R.string.p720_quality_option)
}
