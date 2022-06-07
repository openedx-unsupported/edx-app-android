package org.edx.mobile.feature.interactor

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import org.edx.mobile.R
import org.edx.mobile.feature.matcher.ActionBarMatcher.isInActionBarInteraction

class ProfileScreenInteractor {

    fun observeProfileScreen(): ProfileScreenInteractor {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val title = app.resources.getString(R.string.profile_title)
        isInActionBarInteraction()
            .check(ViewAssertions.matches(ViewMatchers.withText(title)))
            .check(ViewAssertions.matches(ViewMatchers.isCompletelyDisplayed()))
        return this
    }

    fun logOut(): LandingScreenInteractor {
        Espresso.onView(ViewMatchers.withText(R.string.label_sign_out_btn))
            .perform(ViewActions.click())
        return LandingScreenInteractor()
    }
}
