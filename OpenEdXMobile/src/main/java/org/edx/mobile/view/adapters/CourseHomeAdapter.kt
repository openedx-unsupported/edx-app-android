package org.edx.mobile.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.edx.mobile.R
import org.edx.mobile.databinding.RowResumeCourseBinding
import org.edx.mobile.databinding.SectionRowLayoutBinding
import org.edx.mobile.extenstion.isNotVisible
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.HasDownloadEntry
import org.edx.mobile.model.course.SectionRow
import org.edx.mobile.module.db.IDatabase

class CourseHomeAdapter(
    private val dbStore: IDatabase,
    private val itemClickListener: OnItemClickListener
) : ListAdapter<SectionRow, RecyclerView.ViewHolder>(SectionRow.SectionRowComparator) {

    private var isAnySectionExpended = false

    private var checkedItem = Pair(-1, -1)

    private val sectionsExpandedState = mutableMapOf<String, Boolean>()

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SectionRow.RESUME_COURSE_ITEM) {
            return ResumeCourseViewHolder(
                RowResumeCourseBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return SectionViewHolder(
                SectionRowLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == SectionRow.RESUME_COURSE_ITEM) {
            (holder as ResumeCourseViewHolder).bind(getItem(position).component)
        } else {
            (holder as SectionViewHolder).bind(getItem(position).component)
        }
    }

    fun getItem(parentPosition: Int, childPosition: Int): CourseComponent? {
        return if (parentPosition >= itemCount) {
            null
        } else {
            getItem(parentPosition).component.children[childPosition] as CourseComponent
        }
    }

    fun updateList(position: Int = -1) {
        if (position != -1) {
            notifyItemChanged(position)
        } else {
            notifyItemRangeChanged(0, itemCount)
        }
    }

    fun setItemChecked(parentPosition: Int, childPosition: Int) {
        checkedItem = Pair(parentPosition, childPosition)
        updateList()
    }

    fun clearChoicesAndUpdateUI(refresh: Boolean = true) {
        checkedItem = Pair(-1, -1)
        if (refresh) updateList()
    }

    inner class SectionViewHolder(val binding: SectionRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sectionComponent: CourseComponent) {
            binding.tvSectionTitle.text = sectionComponent.displayName
            if (sectionComponent.children.size > 0) {
                binding.ivExpandSection.setVisibility(true)
                setupSubSectionList(sectionComponent)
            } else {
                binding.ivExpandSection.setVisibility(false)
            }

            sectionComponent.isCompleted.apply {
                binding.ivCompletedSection.setVisibility(this)
                if (this) {
                    binding.root.background = ResourcesCompat.getDrawable(
                        binding.root.resources,
                        R.drawable.edx_success_xx_light_fill_neutral_dark_border,
                        binding.root.context.theme
                    )
                } else {
                    binding.root.background = ResourcesCompat.getDrawable(
                        binding.root.resources,
                        R.drawable.edx_neutral_white_t_fill_neutral_dark_border,
                        binding.root.context.theme
                    )
                    if (!isAnySectionExpended) {
                        isAnySectionExpended = true
                        sectionsExpandedState[sectionComponent.id] = true
                    }
                }
            }

            updateSubSectionVisibility(sectionsExpandedState[sectionComponent.id] == true)

            binding.root.setOnClickListener {
                val isExpended = binding.rvSubSection.isNotVisible()
                sectionsExpandedState[sectionComponent.id] = isExpended
                updateSubSectionVisibility(isExpended)
            }
        }

        private fun updateSubSectionVisibility(isVisible: Boolean) {
            binding.rvSubSection.setVisibility(isVisible)
            updateExpandViewIconState(isVisible)
        }

        private fun setupSubSectionList(sectionComponent: CourseComponent) {
            var selectedItem = -1
            if (checkedItem.first == bindingAdapterPosition) {
                selectedItem = checkedItem.second
            }
            binding.rvSubSection.apply {
                val linearLayoutManager =
                    object : LinearLayoutManager(binding.root.context) {
                        override fun canScrollVertically(): Boolean {
                            return false
                        }
                    }
                layoutManager = linearLayoutManager
                isNestedScrollingEnabled = false
                adapter = CourseSubSectionAdapter(
                    sectionComponent,
                    dbStore,
                    object : CourseSubSectionAdapter.OnItemClickListener {
                        override fun onSubSectionItemClick(itemView: LinearLayout, position: Int) {
                            val parentPosition = bindingAdapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                itemClickListener.onSectionItemClick(
                                    itemView,
                                    parentPosition,
                                    position
                                )
                            }
                        }

                        override fun viewDownloadsStatus() {
                            itemClickListener.viewDownloadsStatus()
                        }

                        override fun download(downloadableVideos: List<HasDownloadEntry>) {
                            itemClickListener.download(downloadableVideos = downloadableVideos)
                        }

                        override fun onSubSectionLongClick(itemView: LinearLayout, position: Int) {
                            val parentPosition = bindingAdapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                itemClickListener.onSectionItemLongClick(
                                    itemView,
                                    parentPosition,
                                    position
                                )
                            }
                        }
                    },
                    selectedItem
                )
            }
        }

        private fun updateExpandViewIconState(isChildListVisible: Boolean) {
            binding.ivExpandSection.setImageResource(
                if (isChildListVisible) {
                    R.drawable.ic_drop_up
                } else {
                    R.drawable.ic_drop_down
                }
            )
        }
    }

    inner class ResumeCourseViewHolder(val binding: RowResumeCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(lastAccessedComponent: CourseComponent) {
            binding.root.setOnClickListener {
                itemClickListener.resumeCourseClicked(lastAccessedComponent)
            }
        }
    }

    interface OnItemClickListener {
        fun onSectionItemLongClick(itemView: LinearLayout, parentPosition: Int, childPosition: Int)
        fun onSectionItemClick(itemView: LinearLayout, parentPosition: Int, childPosition: Int)
        fun resumeCourseClicked(lastAccessedComponent: CourseComponent)
        fun viewDownloadsStatus()
        fun download(downloadableVideos: List<HasDownloadEntry>)
    }
}
