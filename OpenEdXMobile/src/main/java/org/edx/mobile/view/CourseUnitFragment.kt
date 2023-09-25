package org.edx.mobile.view

import android.os.Bundle
import androidx.annotation.NonNull
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.event.CourseOutlineRefreshEvent
import org.edx.mobile.extenstion.serializable
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.services.CourseManager
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

abstract class CourseUnitFragment : BaseFragment() {

    @JvmField
    protected var unit: CourseComponent? = null
    private var hasComponentCallback: HasComponent? = null

    @Inject
    protected lateinit var environment: IEdxEnvironment

    @JvmField
    @Inject
    protected var courseManager: CourseManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unit = arguments?.serializable(Router.EXTRA_COURSE_UNIT)
    }

    fun markComponentCompletion(isCompleted: Boolean) {
        unit?.let {
            /* TODO: Fix the update cache mechanism, cuz this method returning course data
                by-value(not by ref) that produce issue to update the course dashboard form the
                app-level cache. To fix the problem need to introduce the optimized way to traverse
                the course components (tree elements).
            */
            courseManager?.getComponentByIdFromAppLevelCache(it.courseId, it.id)
                ?.setCompleted(if (isCompleted) 1 else 0)
            EventBus.getDefault().post(CourseOutlineRefreshEvent())
        }
    }

    fun setHasComponentCallback(callback: HasComponent?) {
        hasComponentCallback = callback
    }

    /**
     * Method to update the `courseData` after successful purchase of a course by the user.
     */
    fun updateCourseUnit(courseId: String, componentId: String) {
        hasComponentCallback?.refreshCourseData(courseId, componentId)
    }

    /**
     * Method to initialize IAP observers for the screen that needs to update the `courseData`.
     */
    fun initializeBaseObserver() {
        hasComponentCallback?.initializeIAPObserver()
    }

    /**
     * This method contains the status that screen has the Casting supported video content or not.
     *
     * @return true if screen has casting supported video content, else false
     */
    open fun hasCastSupportedVideoContent(): Boolean {
        return false
    }

    interface HasComponent {
        fun navigateNextComponent()
        fun navigatePreviousComponent()

        // Abstract method to update the `courseData` after successful purchase of a course by the user.
        fun refreshCourseData(@NonNull courseId: String, @NonNull componentId: String)

        // Abstract method to initialize IAP observers for the screen that needs to update the `courseData`.
        fun initializeIAPObserver()
    }
}
