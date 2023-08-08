package org.edx.mobile.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.model.api.AuthorizationDenialReason
import org.edx.mobile.model.api.CourseUpgradeResponse
import org.edx.mobile.model.api.EnrolledCoursesResponse
import org.edx.mobile.model.course.BlockType
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.model.course.DiscussionBlockModel
import org.edx.mobile.model.course.HtmlBlockModel
import org.edx.mobile.model.course.VideoBlockModel
import org.edx.mobile.util.Config
import org.edx.mobile.util.VideoUtil
import org.edx.mobile.view.CourseUnitDiscussionFragment
import org.edx.mobile.view.CourseUnitEmptyFragment
import org.edx.mobile.view.CourseUnitFragment
import org.edx.mobile.view.CourseUnitFragment.HasComponent
import org.edx.mobile.view.CourseUnitMobileNotSupportedFragment
import org.edx.mobile.view.CourseUnitOnlyOnYoutubeFragment
import org.edx.mobile.view.CourseUnitVideoPlayerFragment
import org.edx.mobile.view.CourseUnitWebViewFragment
import org.edx.mobile.view.CourseUnitYoutubePlayerFragment
import org.edx.mobile.view.LockedCourseUnitFragment

class NewCourseUnitPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val environment: IEdxEnvironment,
    componentList: MutableList<CourseComponent>,
    courseData: EnrolledCoursesResponse,
    courseUpgradeData: CourseUpgradeResponse?,
    callback: HasComponent
) : FragmentStateAdapter(fragmentActivity) {

    private val config: Config = environment.config
    private val componentList: MutableList<CourseComponent>
    private val courseData: EnrolledCoursesResponse
    private val courseUpgradeData: CourseUpgradeResponse?
    private val callback: HasComponent
    private val fragments: MutableList<Fragment> = ArrayList()

    init {
        this.componentList = componentList
        this.courseData = courseData
        this.courseUpgradeData = courseUpgradeData
        this.callback = callback
    }

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
            is VideoBlockModel -> {
                VideoBlockModel(unit)
            }

            is DiscussionBlockModel -> {
                DiscussionBlockModel(unit)
            }

            is HtmlBlockModel -> {
                HtmlBlockModel(unit)
            }

            else -> CourseComponent(unit)
        }
        minifiedUnit.courseSku = courseData.courseSku
        val unitFragment: CourseUnitFragment
        val isYoutubeVideo =
            minifiedUnit is VideoBlockModel && minifiedUnit.data.encodedVideos.youtubeVideoInfo != null
        unitFragment =
            if (minifiedUnit.authorizationDenialReason == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS) {
                if (courseUpgradeData == null) {
                    CourseUnitMobileNotSupportedFragment.newInstance(minifiedUnit, courseData)
                } else {
                    LockedCourseUnitFragment.newInstance(
                        minifiedUnit,
                        courseData,
                        courseUpgradeData
                    )
                }
            } else if (VideoUtil.isCourseUnitVideo(environment, minifiedUnit)) {
                val videoBlockModel = minifiedUnit as VideoBlockModel
                videoBlockModel.setVideoThumbnail(courseData.course.course_image)
                CourseUnitVideoPlayerFragment.newInstance(
                    videoBlockModel,
                    pos < componentList.size,
                    (pos > 0)
                )
            } else if (isYoutubeVideo && config.youtubePlayerConfig.isYoutubePlayerEnabled) {
                CourseUnitYoutubePlayerFragment.newInstance(minifiedUnit as VideoBlockModel)
            } else if (isYoutubeVideo) {
                CourseUnitOnlyOnYoutubeFragment.newInstance(minifiedUnit)
            } else if (config.isDiscussionsEnabled && minifiedUnit is DiscussionBlockModel) {
                CourseUnitDiscussionFragment.newInstance(minifiedUnit, courseData)
            } else if (!minifiedUnit.isMultiDevice) {
                CourseUnitMobileNotSupportedFragment.newInstance(minifiedUnit, courseData)
            } else if (minifiedUnit.type !== BlockType.VIDEO && minifiedUnit.type !== BlockType.HTML && minifiedUnit.type !== BlockType.OTHERS && minifiedUnit.type !== BlockType.DISCUSSION && minifiedUnit.type !== BlockType.PROBLEM && minifiedUnit.type !== BlockType.OPENASSESSMENT && minifiedUnit.type !== BlockType.DRAG_AND_DROP_V2 && minifiedUnit.type !== BlockType.WORD_CLOUD && minifiedUnit.type !== BlockType.LTI_CONSUMER) {
                CourseUnitEmptyFragment.newInstance(minifiedUnit)
            } else if (minifiedUnit is HtmlBlockModel) {
                minifiedUnit.setCourseId(courseData.course.id)
                CourseUnitWebViewFragment.newInstance(minifiedUnit, courseData)
            } else {
                CourseUnitMobileNotSupportedFragment.newInstance(minifiedUnit, courseData)
            }
        unitFragment.setHasComponentCallback(callback)
        fragments.add(unitFragment)
        return unitFragment
    }

    override fun getItemCount(): Int {
        return componentList.size
    }

    fun getComponentIndex(component: CourseComponent): Int {
        return componentList.indexOf(component)
    }
}
