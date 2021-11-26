package org.edx.mobile.view

import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import org.edx.mobile.R
import org.edx.mobile.base.BaseSingleFragmentActivity

@AndroidEntryPoint
class CourseHandoutActivity : BaseSingleFragmentActivity() {
    override fun onStart() {
        super.onStart()
        title = getString(R.string.tab_label_handouts)
    }

    override fun getFirstFragment(): Fragment {
        val courseHandoutFragment = CourseHandoutFragment()
        courseHandoutFragment.arguments = intent.extras
        return courseHandoutFragment
    }
}
