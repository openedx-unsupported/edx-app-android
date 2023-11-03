package org.edx.mobile.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.model.api.AuthorizationDenialReason
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.DiscussionBlockModel
import org.edx.mobile.model.course.HtmlBlockModel
import org.edx.mobile.model.course.VideoBlockModel
import org.edx.mobile.util.VideoUtil
import org.edx.mobile.view.CourseUnitDiscussionFragment
import org.edx.mobile.view.CourseUnitEmptyFragment
import org.edx.mobile.view.CourseUnitFragment.HasComponent
import org.edx.mobile.view.CourseUnitMobileNotSupportedFragment
import org.edx.mobile.view.CourseUnitOnlyOnYoutubeFragment
import org.edx.mobile.view.CourseUnitVideoPlayerFragment
import org.edx.mobile.view.CourseUnitWebViewFragment
import org.edx.mobile.view.CourseUnitYoutubePlayerFragment
import org.edx.mobile.view.LockedCourseUnitFragment

class CourseUnitPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val environment: IEdxEnvironment,
    private val componentList: MutableList<CourseComponent>,
    private val courseData: EnrolledCoursesResponse,
    private val courseUpgradeData: CourseUpgradeResponse?,
    private val callback: HasComponent
) : FragmentStateAdapter(fragmentActivity) {

    private val fragments: MutableList<Fragment> = ArrayList()

    fun getComponent(pos: Int): CourseComponent {
        var unitPosition = pos
        if (pos >= componentList.size) {
            unitPosition = componentList.size - 1
        }
        if (pos < 0) {
            unitPosition = 0
        }
        return componentList[unitPosition]
    }

    override fun createFragment(pos: Int): Fragment {
        // FIXME: Remove this code once LEARNER-6713 is merged
        // Create a deep copy of original CourseComponent object with `root` and `parent` objects
        // removed to save memory.
        val minifiedUnit: CourseComponent = when (val unit = getComponent(pos)) {
            is VideoBlockModel -> VideoBlockModel(unit)
            is DiscussionBlockModel -> DiscussionBlockModel(unit)
            is HtmlBlockModel -> HtmlBlockModel(unit)
            else -> CourseComponent(unit)
        }
        minifiedUnit.courseSku = courseData.courseSku

        val unitFragment = when {
            minifiedUnit.authorizationDenialReason == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS -> {
                if (courseUpgradeData == null) {
                    CourseUnitMobileNotSupportedFragment.newInstance(minifiedUnit, courseData)
                } else {
                    minifiedUnit.courseSku = courseData.courseSku
                    LockedCourseUnitFragment.newInstance(
                        minifiedUnit,
                        courseData,
                        courseUpgradeData
                    )
                }
            }

            minifiedUnit is VideoBlockModel -> {
                val isYoutubeVideo = minifiedUnit.data.encodedVideos.isYoutubeVideo
                when {
                    VideoUtil.isCourseUnitVideo(environment, minifiedUnit) -> {
                        minifiedUnit.setVideoThumbnail(courseData.course.course_image)
                        CourseUnitVideoPlayerFragment.newInstance(
                            minifiedUnit,
                            pos < componentList.size,
                            (pos > 0)
                        )
                    }

                    isYoutubeVideo && environment.config.youtubePlayerConfig.isYoutubePlayerEnabled -> {
                        CourseUnitYoutubePlayerFragment.newInstance(minifiedUnit)
                    }

                    isYoutubeVideo -> {
                        CourseUnitOnlyOnYoutubeFragment.newInstance(minifiedUnit)
                    }

                    else -> {
                        CourseUnitMobileNotSupportedFragment.newInstance(minifiedUnit, courseData)
                    }
                }
            }

            minifiedUnit is DiscussionBlockModel && environment.config.isDiscussionsEnabled -> {
                CourseUnitDiscussionFragment.newInstance(minifiedUnit, courseData)
            }

            minifiedUnit.isMultiDevice.not() -> {
                CourseUnitMobileNotSupportedFragment.newInstance(minifiedUnit, courseData)
            }

            minifiedUnit.isEmptyComponent -> {
                CourseUnitEmptyFragment.newInstance(minifiedUnit)
            }

            minifiedUnit is HtmlBlockModel -> {
                minifiedUnit.setCourseId(courseData.course.id)
                CourseUnitWebViewFragment.newInstance(minifiedUnit, courseData)
            }

            else -> {
                CourseUnitMobileNotSupportedFragment.newInstance(minifiedUnit, courseData)
            }
        }
        unitFragment.setHasComponentCallback(callback)
        fragments.add(unitFragment)
        return unitFragment
    }

    override fun getItemCount(): Int = componentList.size

    fun getComponentIndex(component: CourseComponent): Int = componentList.indexOf(component)
}
