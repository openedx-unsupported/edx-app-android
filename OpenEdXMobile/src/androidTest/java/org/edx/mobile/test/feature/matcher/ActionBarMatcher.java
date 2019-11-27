package org.edx.mobile.test.feature.matcher;

import android.view.View;

import org.hamcrest.Matcher;

import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public enum ActionBarMatcher {
    ;

    public static Matcher<View> isInActionBar() {
        return isDescendantOfA(withId(com.google.android.material.R.id.action_bar_container));
    }
}

