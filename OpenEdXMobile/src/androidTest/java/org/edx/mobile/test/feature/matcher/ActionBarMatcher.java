package org.edx.mobile.test.feature.matcher;

import android.view.View;

import org.edx.mobile.R;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anyOf;


public enum ActionBarMatcher {
    ;

    public static Matcher<View> isInActionBar() {
        return anyOf(isDescendantOfA(withId(android.support.design.R.id.action_bar_container)),
                isDescendantOfA(withId(R.id.toolbar))); // Structures of views are not uniform. Action bar
               // and labels are located in a slightly different hierarchy (login page, my course page)
    }
}

