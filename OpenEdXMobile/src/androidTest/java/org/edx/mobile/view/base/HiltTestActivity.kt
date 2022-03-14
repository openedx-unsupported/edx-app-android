package org.edx.mobile.view.base

import android.os.Bundle
import android.widget.LinearLayout
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.base.BaseAppActivity
import org.edx.mobile.model.course.CourseComponent
import org.edx.mobile.view.CourseUnitFragment
import org.edx.mobile.view.custom.PreLoadingListener

/**
 * The {@link CourseUnitWebViewFragment} requires its parent activity to implement the
 * {@link PreLoadingListener} interface, which is why this dummy activity has been created.
 */
@AndroidEntryPoint
class HiltTestActivity : BaseAppActivity(), CourseUnitFragment.HasComponent, PreLoadingListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = LinearLayout(this)
        // noinspection ResourceType
        view.id = 1
        setContentView(view)
    }

    override val component: CourseComponent?
        get() = null

    override fun navigateNextComponent() {}

    override fun navigatePreviousComponent() {}

    override fun setLoadingState(newState: PreLoadingListener.State) {}

    override fun isMainUnitLoaded(): Boolean {
        return false
    }
}
