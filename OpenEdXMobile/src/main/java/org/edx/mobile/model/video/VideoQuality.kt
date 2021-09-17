package org.edx.mobile.model.video

import org.edx.mobile.R

enum class VideoQuality(val titleResId: Int) {
    AUTO(R.string.auto_recommended_text),
    OPTION_360P(R.string.video_quality_p360),
    OPTION_540P(R.string.video_quality_p540),
    OPTION_720P(R.string.video_quality_p720);

    val value: String = this.name.replace("OPTION_", "").toLowerCase()
}
