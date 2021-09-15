package org.edx.mobile.view

import android.os.Bundle
import com.google.inject.Inject
import org.edx.mobile.base.BaseFragment
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.services.CourseManager

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
        unit =
            if (arguments == null) null else arguments?.getSerializable(Router.EXTRA_COURSE_UNIT) as CourseComponent
    }

    fun markComponentCompletion(isCompleted: Boolean) {
        unit?.let {
            courseManager?.getComponentByIdFromAppLevelCache(it.courseId, it.id)
                ?.setCompleted(if (isCompleted) 1 else 0)
        }
    }

    fun setHasComponentCallback(callback: HasComponent?) {
        hasComponentCallback = callback
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
        val component: CourseComponent?
        fun navigateNextComponent()
        fun navigatePreviousComponent()
    }
}
