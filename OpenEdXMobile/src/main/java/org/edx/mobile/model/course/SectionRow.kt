package org.edx.mobile.model.course

import androidx.recyclerview.widget.DiffUtil

data class SectionRow(val type: Int, val component: CourseComponent) {

    val isCoursewareRow: Boolean
        get() = type == SUB_SECTION ||
                type == SECTION

    companion object {
        const val RESUME_COURSE_ITEM = 0
        const val SECTION = 1
        const val SUB_SECTION = 2

        // Update this count according to the section types mentioned above
        const val NUM_OF_SECTION_ROWS = 3
    }

    /**
     * The callback for calculating the difference between two non-null items in a list.
     */
    object SectionRowComparator : DiffUtil.ItemCallback<SectionRow>() {
        /**
         * To check whether two objects represent the same item
         */
        override fun areItemsTheSame(oldItem: SectionRow, newItem: SectionRow): Boolean {
            return oldItem == newItem
        }

        /**
         * To check whether two items have the same data. With a RecyclerView.Adapter, we should return
         * whether the items' visual representations are the same.
         */
        override fun areContentsTheSame(oldItem: SectionRow, newItem: SectionRow): Boolean {
            return oldItem.component == newItem.component &&
                    oldItem.component.id == newItem.component.id
        }
    }
}
