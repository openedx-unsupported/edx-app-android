package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class VideoInfo(
    @JvmField
    @SerializedName("url")
    val url: String = "",

    @JvmField
    @SerializedName("file_size")
    val fileSize: Long = 0,

    @SerializedName("stream_priority")
    val streamPriority: Int = DEFAULT_STREAM_PRIORITY

) : Serializable {
    companion object {
        const val DEFAULT_STREAM_PRIORITY: Int = -1
    }
}
