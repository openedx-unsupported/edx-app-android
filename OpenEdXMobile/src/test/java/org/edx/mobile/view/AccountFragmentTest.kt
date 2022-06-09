package org.edx.mobile.view

import android.os.Bundle
import android.view.View
import org.assertj.core.api.Assertions
import org.edx.mobile.base.HiltTestActivity
import org.edx.mobile.util.UiTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.support.v4.SupportFragmentController

@RunWith(RobolectricTestRunner::class)
class AccountFragmentTest : UiTest() {

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
            Assertions.assertThat(fragment.binding.btnViewProfile.visibility)
                .isEqualTo(View.VISIBLE)
        } else {
            Assertions.assertThat(fragment.binding.btnViewProfile.visibility).isEqualTo(View.GONE)
        }
    }
}
