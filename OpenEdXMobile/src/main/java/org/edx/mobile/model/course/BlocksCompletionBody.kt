package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName

class BlocksCompletionBody(

    @SerializedName("username")
    val username: String,

    @SerializedName("course_key")
    val courseKey: String,

    blockIds: List<String>
) {
    var blocks = mutableMapOf<String, String>()

    init {
        blockIds.forEach { blockId -> blocks[blockId] = "1" }
    }
}
