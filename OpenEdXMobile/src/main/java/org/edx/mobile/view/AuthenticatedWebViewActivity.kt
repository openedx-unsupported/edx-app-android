package org.edx.mobile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import org.edx.mobile.base.BaseSingleFragmentActivity
import org.edx.mobile.model.course.CourseComponent

class AuthenticatedWebViewActivity : BaseSingleFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(EXTRA_SCREEN_TITLE)
    }

    override fun getFirstFragment(): Fragment {
        val url = intent.getStringExtra(EXTRA_COMPONENT_URL) ?: ""
        return AuthenticatedWebViewFragment.newInstance(url)
    }

    companion object {
        private const val EXTRA_SCREEN_TITLE = "screen_title"
        private const val EXTRA_COMPONENT_URL = "component_block_url"

        @JvmStatic
        fun newIntent(activity: Context?, unit: CourseComponent): Intent {
            val intent = Intent(activity, AuthenticatedWebViewActivity::class.java)
            intent.putExtra(EXTRA_COMPONENT_URL, unit.blockUrl)
            intent.putExtra(EXTRA_SCREEN_TITLE, unit.displayName)
            return intent
        }
    }
}
