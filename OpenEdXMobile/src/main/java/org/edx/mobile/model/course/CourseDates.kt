package org.edx.mobile.model.course

import com.google.gson.annotations.SerializedName
import org.edx.mobile.util.CourseDateType
import org.edx.mobile.util.DateUtil

data class CourseDates(
        @SerializedName("dates_banner_info") val datesBannerInfo: CourseDatesBannerInfo,
        @SerializedName("course_date_blocks") val courseDateBlocks: List<CourseDateBlock>?,
        @SerializedName("missed_deadlines") val missedDeadlines: Boolean = false,
        @SerializedName("missed_gated_content") val missedGatedContent: Boolean = false,
        @SerializedName("learner_is_full_access") val learnerIsFullAccess: Boolean = false,
        @SerializedName("user_timezone") val userTimezone: String = "",
        @SerializedName("verified_upgrade_link") val verifiedUpgradeLink: String = "",
        // LinkedHashMap to store list the courseDateBlocks against their dates, also preserve the dates insertion order.
        var courseDatesMap: LinkedHashMap<String, ArrayList<CourseDateBlock>> = LinkedHashMap()
) {
    fun organiseCourseDates() {
        organiseCourseDatesInBlock()
        if (isContainToday().not()) {
            // Adding today's date block manually if not present in the date
            courseDatesMap[CourseDateBlock.getTodayDateBlock().getSimpleDateTime()] = arrayListOf(CourseDateBlock.getTodayDateBlock())
        }
        // Sort the map entries date keys wise
        courseDatesMap = LinkedHashMap(courseDatesMap.toSortedMap(compareBy { DateUtil.convertToDate(it) }))
        setDateBlockBadge()
    }

    /**
     * Map the date blocks according to dates and stack all the blocks of same date against one key
     */
    private fun organiseCourseDatesInBlock() {
        courseDatesMap = LinkedHashMap()
        courseDateBlocks?.forEach { item ->
            val key = item.getSimpleDateTime()
            if (courseDatesMap.containsKey(key)) {
                (courseDatesMap[key] as ArrayList).add(item)
            } else {
                courseDatesMap[key] = arrayListOf(item)
            }
        }
    }

    /**
     * Utility method to check that list contains today's date block or not.
     */
    fun isContainToday(): Boolean {
        courseDateBlocks?.forEach {
            if (it.isToday()) {
                return true
            }
        }
        return false
    }

    /**
     * Set the Date Block Badge based on the date block data
     */
    private fun setDateBlockBadge() {
        var dueNextCount = 0
        courseDatesMap.keys.forEach { key ->
            courseDatesMap[key]?.forEach { item ->
                var dateBlockTag: CourseDateType = getDateTypeBadge(item)
                //Setting Due Next only for first occurrence
                if (dateBlockTag == CourseDateType.DUE_NEXT) {
                    if (dueNextCount == 0)
                        dueNextCount += 1
                    else
                        dateBlockTag = CourseDateType.BLANK
                }
                item.dateBlockBadge = dateBlockTag
            }
        }
    }

    /**
     * Return Pill/Badge type of date block based on data
     */
    private fun getDateTypeBadge(item: CourseDateBlock): CourseDateType {
        var dateBlockTag: CourseDateType = CourseDateType.BLANK
        item.dateType?.let {
            when (it) {
                CourseDateBlock.DateTypes.TODAY_DATE ->
                    dateBlockTag = CourseDateType.TODAY
                CourseDateBlock.DateTypes.COURSE_START_DATE,
                CourseDateBlock.DateTypes.COURSE_END_DATE ->
                    dateBlockTag = CourseDateType.BLANK
                CourseDateBlock.DateTypes.ASSIGNMENT_DUE_DATE -> {
                    when {
                        item.complete -> {
                            dateBlockTag = CourseDateType.COMPLETED
                        }
                        item.learnerHasAccess -> {
                            dateBlockTag = when {
                                item.link.isEmpty() -> {
                                    CourseDateType.NOT_YET_RELEASED
                                }
                                DateUtil.isDueDate(item.date) -> {
                                    CourseDateType.DUE_NEXT
                                }
                                DateUtil.isPastDate(item.date) -> {
                                    CourseDateType.PAST_DUE
                                }
                                else -> {
                                    CourseDateType.BLANK
                                }
                            }
                        }
                        else -> {
                            dateBlockTag = CourseDateType.VERIFIED_ONLY
                        }
                    }
                }
                CourseDateBlock.DateTypes.COURSE_EXPIRED_DATE ->
                    dateBlockTag = CourseDateType.COURSE_EXPIRED_DATE

                CourseDateBlock.DateTypes.CERTIFICATE_AVAILABLE_DATE,
                CourseDateBlock.DateTypes.VERIFIED_UPGRADE_DEADLINE,
                CourseDateBlock.DateTypes.VERIFICATION_DEADLINE_DATE ->
                    dateBlockTag = CourseDateType.BLANK
            }
        }
        return dateBlockTag
    }
}
