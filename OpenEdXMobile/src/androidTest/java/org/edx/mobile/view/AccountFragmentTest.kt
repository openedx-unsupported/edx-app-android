package org.edx.mobile.view

import android.os.Bundle
import android.view.View
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.android.synthetic.main.fragment_account.*
import org.assertj.core.api.Assertions
import org.edx.mobile.base.UiTest
import org.edx.mobile.view.base.HiltTestActivity
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.support.v4.SupportFragmentController

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class AccountFragmentTest : UiTest() {

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @Before
    public fun init() {
        hiltAndroidRule.inject()
    }

    private lateinit var fragment: AccountFragment

    override fun setUp() {
        super.setUp()
        fragment = AccountFragment.newInstance(null)
        SupportFragmentController.setupFragment(
            fragment,
            HiltTestActivity::class.java, android.R.id.content, mock(Bundle::class.java)
        )
    }

    @Test
    fun initializeTest() {
        assertNotNull(fragment.view)
    }

    @Test
    fun checkButtonsVisibility() {
        if (config.isUserProfilesEnabled) {
            Assertions.assertThat(fragment.btn_view_profile.visibility).isEqualTo(View.VISIBLE)
        } else {
            Assertions.assertThat(fragment.btn_view_profile.visibility).isEqualTo(View.GONE)
        }
    }
}
