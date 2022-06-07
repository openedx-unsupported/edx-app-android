package org.edx.mobile.feature.interactor;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.edx.mobile.feature.matcher.ActionBarMatcher.isInActionBar;
import static org.hamcrest.CoreMatchers.allOf;

import androidx.test.espresso.matcher.ViewMatchers;

import org.edx.mobile.R;

public class MyCoursesScreenInteractor {
    public MyCoursesScreenInteractor observeMyCoursesScreen() {
        // Look for "My Courses" title which (we assume) is only present on the landing screen
        onView(allOf(isInActionBar(), withText(R.string.label_my_courses))).check(matches(isCompletelyDisplayed()));
        return this;
    }

    public ProfileScreenInteractor navigateToProfileScreen() {
        onView(ViewMatchers.withContentDescription(R.string.profile_title)).perform(click());
        return new ProfileScreenInteractor();
    }
}
