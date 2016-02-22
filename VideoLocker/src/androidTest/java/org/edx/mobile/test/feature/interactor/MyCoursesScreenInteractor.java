package org.edx.mobile.test.feature.interactor;

import org.edx.mobile.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.edx.mobile.test.feature.matcher.ActionBarMatcher.isInActionBar;
import static org.hamcrest.CoreMatchers.allOf;

public class MyCoursesScreenInteractor {
    public MyCoursesScreenInteractor observeMyCoursesScreen() {
        // Look for "My Courses" title which (we assume) is only present on the landing screen
        onView(allOf(isInActionBar(), withText(R.string.label_my_courses))).check(matches(isCompletelyDisplayed()));
        return this;
    }

    public NavigationDrawerInteractor openNavigationDrawer() {
        return NavigationDrawerInteractor.open();
    }
}
