package org.edx.mobile.model

import com.google.gson.annotations.SerializedName

data class AjaxCallData(
    @SerializedName("status")
    val status: Int,

    @SerializedName("url")
    val url: String,

    @SerializedName("response_text")
    val responseText: String = ""
) {

    companion object {
        @JvmStatic
        fun isCompletionRequest(data: AjaxCallData): Boolean {
            return data.url.contains(CompletionType.HTML.completionURL) ||
                    data.url.contains(CompletionType.PROBLEM.completionURL) ||
                    data.url.contains(CompletionType.DRAG_AND_DROP.completionURL) ||
                    (data.url.contains(CompletionType.ORA.completionURL) && data.responseText.contains(
                        "is--complete"
                    ))
        }
    }

    enum class CompletionType(val completionURL: String) {
        HTML("publish_completion"),
        PROBLEM("problem_check"),
        DRAG_AND_DROP("do_attempt"),
        ORA("render_grade");
    }
}
