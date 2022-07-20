package org.edx.mobile.discussion

import com.google.gson.annotations.SerializedName

data class CommentBody(
    @SerializedName("thread_id")
    val threadId: String,

    @SerializedName("raw_body")
    val rawBody: String,

    @SerializedName("parent_id")
    val parentId: String?
)
