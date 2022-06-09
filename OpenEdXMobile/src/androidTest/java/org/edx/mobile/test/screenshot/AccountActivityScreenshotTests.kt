package org.edx.mobile.test.screenshot.test

import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.facebook.testing.screenshot.Screenshot
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.edx.mobile.R
import org.edx.mobile.feature.data.TestValues
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.view.AccountActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

@HiltAndroidTest
class AccountActivityScreenshotTests {

    @get:Rule(order = 0)
    var hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var mActivityRule = ActivityTestRule(AccountActivity::class.java, true, false)

    @get:Rule(order = 2)
    var testName = TestName()

    @get:Rule(order = 3)
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)


    private var loginPrefs: LoginPrefs? = null
    private var activity: AccountActivity? = null

    @Before
    fun before() {
        hiltAndroidRule.inject()
        loginPrefs = LoginPrefs(ApplicationProvider.getApplicationContext())
        loginPrefs?.storeUserProfile(TestValues.DUMMY_PROFILE)
        activity = mActivityRule.launchActivity(null)
    }

    @Test
    fun testScreenshot_recordAccountActivity() {
        activity?.runOnUiThread {
            (activity?.findViewById<View>(R.id.app_version) as TextView).text = String.format(
                "%s %s",
                activity?.getString(R.string.label_app_version), TestValues.DUMMY_APP_VERSION
            )
            Screenshot.snap(activity?.findViewById(android.R.id.content))
                .setName(javaClass.name + "_" + testName.methodName).record()
        }
    }

    @After
    fun after() {
        loginPrefs?.clear()
    }
}
