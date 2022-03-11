package org.edx.mobile.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.fragment_account.*
import org.assertj.core.api.Assertions
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock
import org.robolectric.shadows.support.v4.SupportFragmentController

class AccountFragmentTest : UiTest() {

    private lateinit var fragment: AccountFragment

    override fun setUp() {
        super.setUp()
        fragment = AccountFragment.newInstance(null)
        SupportFragmentController.setupFragment(
            fragment,
            FragmentActivity::class.java, android.R.id.content, mock(Bundle::class.java)
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
