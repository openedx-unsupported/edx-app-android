package org.edx.mobile.feature.matcher

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

object ActionBarMatcher {

    @JvmStatic
    fun isInActionBar(): Matcher<View> {
        return ViewMatchers.isDescendantOfA(
            ViewMatchers.withParent(
                ViewMatchers.isAssignableFrom(
                    Toolbar::class.java
                )
            )
        )
    }

    @JvmStatic
    fun isInActionBarInteraction(): ViewInteraction {
        return Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.isAssignableFrom(
                    TextView::class.java
                ), ViewMatchers.withParent(
                    ViewMatchers.isAssignableFrom(
                        Toolbar::class.java
                    )
                )
            )
        )
    }
}
