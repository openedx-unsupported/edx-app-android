package org.edx.mobile.model

import com.google.gson.annotations.SerializedName

data class AjaxCallData(
        @SerializedName("status")
        val status: Int,

        @SerializedName("url")
        val url: String,

        @SerializedName("response_text")
        val responseText: String
) {
    fun isCompletionRequest(): Boolean {
        return url.contains(CompletionType.HTML.completionURL) ||
                url.contains(CompletionType.PROBLEM.completionURL) ||
                url.contains(CompletionType.DRAG_AND_DROP.completionURL) ||
                (url.contains(CompletionType.ORA.completionURL) && responseText.contains("is--complete"))
    }


    enum class CompletionType(val completionURL: String) {
        HTML("publish_completion"),
        PROBLEM("problem_check"),
        DRAG_AND_DROP("do_attempt"),
        ORA("render_grade");
    }
}
