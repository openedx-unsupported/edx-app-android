package org.edx.mobile.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.databinding.RowCourseListBinding
import org.edx.mobile.extenstion.setVisibility
import org.edx.mobile.model.api.EnrolledCoursesComparator
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.util.images.CourseCardUtils
import org.edx.mobile.util.images.ImageUtils
import org.edx.mobile.util.images.TopAnchorFillWidthTransformation

abstract class MyCoursesListAdapter(
    private val environment: IEdxEnvironment
) : ListAdapter<EnrolledCoursesResponse, MyCoursesListAdapter.CourseCardViewHolder>
    (EnrolledCoursesComparator) {

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
        holder.bind(getItem(position))
    }

    inner class CourseCardViewHolder(
        val binding: RowCourseListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(courseData: EnrolledCoursesResponse) {
            initCourseCardContent(courseData)
            initValuePropModel(courseData)
        }

        private fun initCourseCardContent(courseData: EnrolledCoursesResponse) {
            binding.courseCard.courseName.text = courseData.course.name
            courseData.course.getCourse_image(environment.config.apiHostURL)?.let {
                setCourseImage(it)
            }
            if (courseData.course.hasUpdates()) {
                setHasUpdates(courseData)
            } else {
                CourseCardUtils.getFormattedDate(binding.root.context, courseData)?.let {
                    setDetails(it)
                }
            }
            binding.root.setOnClickListener {
                onItemClicked(courseData)
            }
        }

        private fun initValuePropModel(courseData: EnrolledCoursesResponse) {
            binding.courseCard.llGradedContentLayout.apply {
                if (courseData.isUpgradeable && environment.featuresPrefs.isValuePropEnabled) {
                    setVisibility(true)
                    setOnClickListener {
                        onValuePropClicked(courseData)
                    }
                } else {
                    setVisibility(false)
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

        private fun setHasUpdates(courseData: EnrolledCoursesResponse) {
            binding.courseCard.courseDetails.setVisibility(false)
            binding.courseCard.newCourseContentLayout.setVisibility(true)
            binding.courseCard.newCourseContentLayout.tag = courseData.course
            binding.courseCard.newCourseContentLayout.setOnClickListener {
                onAnnouncementClicked(courseData)
            }
        }

        private fun setDetails(date: String) {
            binding.courseCard.newCourseContent.setVisibility(false)
            binding.courseCard.courseDetails.setVisibility(true)
            binding.courseCard.courseDetails.text = date
        }
    }

    abstract fun onItemClicked(model: EnrolledCoursesResponse)
    abstract fun onAnnouncementClicked(model: EnrolledCoursesResponse)
    abstract fun onValuePropClicked(model: EnrolledCoursesResponse)
}
