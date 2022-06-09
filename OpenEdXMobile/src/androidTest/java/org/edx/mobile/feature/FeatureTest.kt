package org.edx.mobile.feature

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import okhttp3.mockwebserver.MockWebServer
import org.edx.mobile.core.EdxEnvironment
import org.edx.mobile.feature.mock.MockServerDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
abstract class FeatureTest {

    @get:Rule(order = 0)
    var hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var environment: EdxEnvironment

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.dispatcher = MockServerDispatcher().ResponseDispatcher()
        mockWebServer.start(8080)

        hiltAndroidRule.inject()

        environment.loginPrefs?.clear()
        environment.analyticsRegistry?.resetIdentifyUser()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
}
