package org.edx.mobile.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.RowCourseListBinding
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.model.api.CourseEntry
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.util.images.CourseCardUtils
import org.edx.mobile.util.images.ImageUtils
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation

private val itemDiffCallback = object : DiffUtil.ItemCallback<EnrolledCoursesResponse>() {
    override fun areItemsTheSame(
        oldItem: EnrolledCoursesResponse,
        newItem: EnrolledCoursesResponse,
    ): Boolean {
        return oldItem.courseId == newItem.courseId
    }

    override fun areContentsTheSame(
        oldItem: EnrolledCoursesResponse,
        newItem: EnrolledCoursesResponse,
    ): Boolean {
        return oldItem == newItem
    }
}

abstract class MyCoursesListAdapter(
    private val environment: IEdxEnvironment
) : ListAdapter<EnrolledCoursesResponse, MyCoursesListAdapter.CourseCardViewHolder>
    (itemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseCardViewHolder {
        return CourseCardViewHolder(
            RowCourseListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CourseCardViewHolder, position: Int) {
        val courseData = getItem(position)
        holder.bind(courseData)
        holder.binding.apply {
            root.setOnClickListener {
                onItemClicked(courseData)
            }
            courseCard.newCourseContentLayout.setOnClickListener {
                onAnnouncementClicked(courseData)
            }
            courseCard.llGradedContentLayout.setOnClickListener {
                onValuePropClicked(courseData)
            }
        }
    }

    abstract fun onItemClicked(model: EnrolledCoursesResponse)
    abstract fun onAnnouncementClicked(model: EnrolledCoursesResponse)
    abstract fun onValuePropClicked(model: EnrolledCoursesResponse)

    inner class CourseCardViewHolder(
        val binding: RowCourseListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(courseData: EnrolledCoursesResponse) {
            binding.courseCard.courseName.text = courseData.course.name
            courseData.course.getCourse_image(environment.config.apiHostURL)?.let {
                setCourseImage(it)
            }
            binding.courseCard.llGradedContentLayout.setVisibility(
                courseData.isUpgradeable && environment.appFeaturesPrefs.isValuePropEnabled()
            )
            if (courseData.course.hasUpdates()) {
                setHasUpdates(courseData.course)
            } else {
                CourseCardUtils.getFormattedDate(binding.root.context, courseData)?.let {
                    setDetails(it)
                }
            }
        }

        private fun setCourseImage(imageUrl: String) {
            val context = binding.courseCard.courseImage.context
            if (ImageUtils.isValidContextForGlide(context)) {
                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_course_card_image)
                    .transform(TopAnchorFillWidthTransformation())
                    .into(binding.courseCard.courseImage)
            }
        }

        private fun setHasUpdates(courseData: CourseEntry) {
            binding.courseCard.courseDetails.setVisibility(false)
            binding.courseCard.newCourseContentLayout.setVisibility(true)
            binding.courseCard.newCourseContentLayout.tag = courseData
        }

        private fun setDetails(date: String) {
            binding.courseCard.newCourseContent.setVisibility(false)
            binding.courseCard.courseDetails.setVisibility(true)
            binding.courseCard.courseDetails.text = date
        }
    }
}
