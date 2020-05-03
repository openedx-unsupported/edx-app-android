package org.edx.mobile.test.screenshot.test

import android.view.View
import android.widget.TextView
import androidx.test.rule.ActivityTestRule
import com.facebook.testing.screenshot.Screenshot
import org.edx.mobile.R
import org.edx.mobile.base.MainApplication
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.test.feature.data.TestValues
import org.edx.mobile.view.AccountActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

class AccountActivityScreenshotTests {

    @get:Rule
    var mActivityRule = ActivityTestRule(AccountActivity::class.java, true, false)
    @get:Rule
    var testName = TestName()
    private var loginPrefs: LoginPrefs? = null
    private var activity: AccountActivity? = null

    @Before
    fun before() {
        loginPrefs = LoginPrefs(MainApplication.application.baseContext)
        loginPrefs?.storeUserProfile(TestValues.DUMMY_PROFILE)
        activity = mActivityRule.launchActivity(null)
    }

    @Test
    fun testScreenshot_recordAccountActivity() {
        activity?.runOnUiThread {
            (activity?.findViewById<View>(R.id.tv_version_no) as TextView).text = String.format("%s %s",
                    activity?.getString(R.string.label_version), TestValues.DUMMY_APP_VERSION)
            Screenshot.snap(activity?.findViewById(android.R.id.content)).setName(javaClass.name + "_" + testName.methodName).record()
        }
    }

    @After
    fun after() {
        loginPrefs?.clear()
    }
}
