package org.edx.mobile.test.feature;

import android.content.Intent;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.EdxEnvironment;
import org.edx.mobile.view.SplashActivity;
import org.edx.mobile.view.adapters.MyCoursesAdapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringStartsWith.startsWith;


/**
 * Hacky Test that will run through ALL screens to easily collect analytic event outputs.
 *
 * Screens that are not accessed:
 *
 * Course discovery
 * Course info
 *
 */

@RunWith(AndroidJUnit4.class)
public class TouchEachScreen {

    protected EdxEnvironment environment;

    @Rule
    public ActivityTestRule<SplashActivity> mActivityRule = new ActivityTestRule<>(
            SplashActivity.class, false, false);

    @Before
    public void setup() {
        // Ensure we are not logged in
        final MainApplication application = MainApplication.instance();
        environment = application.getInjector().getInstance(EdxEnvironment.class);
        environment.getLoginPrefs().clear();
        environment.getAnalyticsRegistry().resetIdentifyUser();


        mActivityRule.launchActivity(new Intent());
    }

    @Test
    public void withNoTestAssertions_navigateToAllScreens() throws InterruptedException {
        from_launchActivity_login();
        fromMyCourses_navigateToAllProfileScreens();
        fromMyCourses_navigateToAllCourseDashboardScreens();

    }

    public void fromMyCourses_navigateToAllCourseDashboardScreens() {
        onView(withText(startsWith("Demo Course"))).perform(click());
        fromCourseDashboard_navigateToHandouts();
        fromCourseDashboard_navigateToAnnouncements();
        fromCourseDashboard_navigateToAllDiscussionsScreens();
        fromCourseDashboard_navigateToAllCoursewareScreens();
    }

    public void fromCourseDashboard_navigateToHandouts() {
        onView(withText(startsWith("Handouts"))).perform(click());
        pressBack();
    }

    public void fromCourseDashboard_navigateToAnnouncements() {
        onView(withText(startsWith("Announcements"))).perform(click());
        pressBack();
    }

    public void fromCourseDashboard_navigateToAllDiscussionsScreens() {
        onView(withText(startsWith("Discussion"))).perform(click());
        onView(withId(R.id.discussion_topics_searchview)).perform(clearText(), typeText("forum search string"));

        pressBack();
    }

    public void fromCourseDashboard_navigateToAllCoursewareScreens() {
        onView(withText(startsWith("Courseware"))).perform(click());
        pressBack();
    }

    public void from_launchActivity_login() {
        onView(withId(R.id.log_in)).perform(click());
        onView(withId(R.id.email_et)).perform(clearText(), typeText("clee+test@edx.org"));
        onView(withId(R.id.password_et)).perform(typeText("edx"));
        closeSoftKeyboard();
        onView(withId(R.id.login_button_layout)).perform(click());
    }

    public void fromMyCourses_navigateToAllProfileScreens() {
        DrawerActions.openDrawer(R.id.drawer_layout);
        onView(withId(R.id.profile_image)).perform(click());
        onView(withId(R.id.edit_profile)).perform(click());
        onView(withText(startsWith("Birth year"))).perform(click());
        pressBack();
        onView(withText(startsWith("Location"))).perform(click());
        pressBack();
        onView(withText(startsWith("Primary language"))).perform(click());
        pressBack();
        onView(withText(startsWith("About me"))).perform(click());
        pressBack();
    }
}
