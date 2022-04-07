package org.edx.mobile.view

import androidx.test.ext.junit.rules.ActivityScenarioRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.edx.mobile.interfaces.NetworkObserver
import org.edx.mobile.interfaces.NetworkSubject
import org.edx.mobile.whatsnew.WhatsNewActivity
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

/**
 * Test NetworkSubject implementations for correctness
 */
@HiltAndroidTest // 1
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class NetworkSubjectTest {
    @get:Rule(order = 0)
    var hiltAndroidRule = HiltAndroidRule(this)

    // define the rules for the activity that declared in the `AndroidManifest.xml` so used 
    // `WhatsNewActivity` cuz this activity not have any much functionality.
    @get:Rule(order = 1)
    var activityScenarioRule = ActivityScenarioRule(
        WhatsNewActivity::class.java
    )

    @Before
    fun init() {
        hiltAndroidRule.inject()
    }

    var networkSubject: NetworkSubject? = null

    @Test
    fun test() {
        activityScenarioRule.scenario.onActivity { activity ->
            networkSubject = activity
            val networkObserver = NetworkObserverTest()
            networkSubject?.registerNetworkObserver(networkObserver)
            Assert.assertTrue(networkObserver.isOnline)
            networkSubject?.notifyNetworkDisconnect()
            Assert.assertFalse(networkObserver.isOnline)
            networkSubject?.notifyNetworkConnect()
            Assert.assertTrue(networkObserver.isOnline)
            networkSubject?.unregisterNetworkObserver(networkObserver)
            Assert.assertTrue(networkObserver.isOnline)
            networkSubject?.notifyNetworkDisconnect()
            Assert.assertTrue(networkObserver.isOnline)
        }
    }

    private class NetworkObserverTest : NetworkObserver {
        var isOnline = true
        override fun onOnline() {
            isOnline = true
        }

        override fun onOffline() {
            isOnline = false
        }
    }
}
