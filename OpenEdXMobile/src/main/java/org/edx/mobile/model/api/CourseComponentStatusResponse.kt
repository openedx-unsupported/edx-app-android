package org.edx.mobile.model.api

import com.google.gson.annotations.SerializedName

data class CourseComponentStatusResponse(
        @SerializedName("last_visited_module_id")
        var lastVisitedModuleId: String? = null,
        @SerializedName("last_visited_module_path")
        var lastVisitedModulePaths: ArrayList<String> = arrayListOf(),
        @SerializedName("last_visited_block_id")
        var lastVisitedBlockId: String? = null
)
