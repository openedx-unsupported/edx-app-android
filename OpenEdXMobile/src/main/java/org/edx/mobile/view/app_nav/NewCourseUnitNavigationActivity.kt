package org.edx.mobile.view.app_nav

import android.os.Bundle
import org.edx.mobile.base.BaseFragmentActivity
import org.edx.mobile.databinding.ActivityCourseUnitNavigationNewBinding

class NewCourseUnitNavigationActivity : BaseFragmentActivity() {

    private lateinit var binding: ActivityCourseUnitNavigationNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCourseUnitNavigationNewBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
