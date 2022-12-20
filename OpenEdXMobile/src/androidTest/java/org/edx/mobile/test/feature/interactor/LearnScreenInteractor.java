package org.edx.mobile.test.feature.interactor;

import org.edx.mobile.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.edx.mobile.test.feature.matcher.ActionBarMatcher.isInActionBar;
import static org.hamcrest.CoreMatchers.allOf;

public class LearnScreenInteractor {
    public LearnScreenInteractor observeLearnScreen() {
        // Look for "Learn" title which (we assume) is only present on the landing screen
        onView(allOf(isInActionBar(), withText(R.string.label_learn))).check(matches(isCompletelyDisplayed()));
        return this;
    }

    public NavigationDrawerInteractor openNavigationDrawer() {
        return NavigationDrawerInteractor.open();
    }
}
