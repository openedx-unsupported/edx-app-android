package org.edx.mobile.test.feature;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.Switch;

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
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
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
 * Find Courses
 *
 */

@RunWith(AndroidJUnit4.class)
public class TouchEachScreen {

    protected EdxEnvironment environment;

    @Rule
    public IntentsTestRule<SplashActivity> mActivityRule = new IntentsTestRule<>(
            SplashActivity.class, false, false);

    @Before
    public void setup() {
        // Ensure we are not logged in
//        final MainApplication application = MainApplication.instance();
//        environment = application.getInjector().getInstance(EdxEnvironment.class);
//        environment.getLoginPrefs().clear();
//        environment.getAnalyticsRegistry().resetIdentifyUser();


        mActivityRule.launchActivity(new Intent());
    }

    @Test
    public void withNoTestAssertions_navigateToAllScreens() throws InterruptedException {
//        fromLaunchActivity_login();
//        fromMyCourses_navigateToAllSettingsScreens();
//        fromMyCourses_navigateToAllCourseDashboardScreens();
        fromMyCourses_navigateToAllProfileScreens();


    }

    public void fromMyCourses_navigateToAllSettingsScreens() throws InterruptedException {
        DrawerActions.openDrawer(R.id.drawer_layout);
        onView(withId(R.id.drawer_option_my_settings)).perform(click());

        // Hacky way to ensure that the setting is on
        onView(withId(R.id.wifi_setting)).perform(click());
        try {
            onView(withId(R.id.positiveButton)).perform(click());
        } catch (Exception e) {
            onView(withId(R.id.wifi_setting)).perform(click());
            onView(withId(R.id.positiveButton)).perform(click());
        }

    }

    public void fromMyCourses_navigateToAllCourseDashboardScreens() {
        DrawerActions.openDrawer(R.id.drawer_layout);
        onView(withId(R.id.drawer_option_my_courses)).perform(click());
        onView(withText(startsWith("Introduction to Clee"))).perform(click());
        fromCourseDashboard_navigateToHandoutsAndAnnouncements();
        fromCourseDashboard_navigateToAllDiscussionsScreens();
        fromCourseDashboard_navigateToAllCoursewareScreens();
        fromCourseDashboard_navigateToAllCertificateScreens();
        pressBack();
    }

    public void fromCourseDashboard_navigateToAllCertificateScreens() {
        onView(withText(startsWith("View Certificate"))).perform(click());
        onView(withId(R.id.menu_item_share)).perform(click());
        Intent resultData = new Intent();
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(toPackage("com.android.messaging")).respondWith(result);
        onView(withText(startsWith("Share your certificate"))).perform(click());
        onView(withText(startsWith("Messaging"))).perform(click());
        pressBack();

    }

    public void fromCourseDashboard_navigateToAllDiscussionsScreens() {
        onView(withText(startsWith("Discussion"))).perform(click());
        onView(withId(R.id.discussion_topics_searchview)).perform(typeText("forum search string"));
        pressBack();
        onView(withText(startsWith("All Posts"))).perform(click());
        onView(withText(startsWith("test-automation-thread"))).perform(click());
        onView(withText(startsWith("Discussion"))).perform(click());
        onView(withText(startsWith("1 comment"))).perform(click());
        onView(withText(startsWith("Add a comment"))).perform(click());
        closeSoftKeyboard();
        pressBack();
        pressBack();
        onView(withText(startsWith("Add a response"))).perform(click());
        closeSoftKeyboard();
        pressBack();
        pressBack();
        onView(withText(startsWith("Create a new post"))).perform(click());
        closeSoftKeyboard();
        pressBack();
        pressBack();
        pressBack();
    }

    public void fromCourseDashboard_navigateToAllCoursewareScreens() {
        onView(withText(startsWith("Courseware"))).perform(click());
        pressBack();
    }

    public void fromLaunchActivity_login() {
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
        onView(withId(R.id.change_photo)).perform(click());

        Bitmap icon = BitmapFactory.decodeResource(
                InstrumentationRegistry.getTargetContext().getResources(),
                R.mipmap.ic_launcher);
        Intent resultData = new Intent();
        resultData.putExtra("data", icon);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(toPackage("com.android.camera")).respondWith(result);

        onView(withText(startsWith("Take photo"))).perform(click());
        onView(withId(R.id.save)).perform(click());
        pressBack();
        pressBack();


    }

    public void fromCourseDashboard_navigateToHandoutsAndAnnouncements() {
        onView(withText(startsWith("Handouts"))).perform(click());
        pressBack();
        onView(withText(startsWith("Announcements"))).perform(scrollTo(), click());
        pressBack();
    }

}
