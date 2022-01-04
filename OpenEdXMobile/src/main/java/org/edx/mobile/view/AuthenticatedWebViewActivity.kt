package org.edx.mobile.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import org.edx.mobile.base.BaseSingleFragmentActivity
import org.edx.mobile.model.course.CourseComponent

class AuthenticatedWebViewActivity : BaseSingleFragmentActivity() {

    private var isModalView: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(EXTRA_SCREEN_TITLE)
        isModalView = intent.getBooleanExtra(EXTRA_IS_MODAL_VIEW, false)
        if (isModalView || title.isNullOrEmpty()) {
            supportActionBar?.hide()
        }
    }

    override fun getFirstFragment(): Fragment {
        val url = intent.getStringExtra(EXTRA_COMPONENT_URL) ?: ""
        return AuthenticatedWebViewFragment.newInstance(url)
    }

    companion object {
        private const val EXTRA_SCREEN_TITLE = "screen_title"
        private const val EXTRA_COMPONENT_URL = "component_block_url"
        private const val EXTRA_IS_MODAL_VIEW = "is_modal_view"

        @JvmStatic
        fun newIntent(activity: Context?, unit: CourseComponent): Intent {
            return newIntent(activity, url = unit.blockUrl, screenTitle = unit.displayName, isModalView = false)
        }

        @JvmStatic
        fun newIntent(activity: Context?, url: String, screenTitle: String, isModalView: Boolean): Intent {
            val intent = Intent(activity, AuthenticatedWebViewActivity::class.java)
            intent.putExtra(EXTRA_COMPONENT_URL, url)
            intent.putExtra(EXTRA_SCREEN_TITLE, screenTitle)
            intent.putExtra(EXTRA_IS_MODAL_VIEW, isModalView)
            if (isModalView) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return intent
        }
    }

    override fun onBackPressed() {
        if (isModalView.not())
            super.onBackPressed()
    }
}
