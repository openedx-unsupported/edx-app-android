package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ActivityController;
import org.robolectric.util.Scheduler;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

// TODO: Test network connectivity change events too, after we manage to mock them
public abstract class BaseFragmentActivityTest extends UiTest {
    /**
     * Method for defining the subclass of {@link BaseFragmentActivity} that
     * is being tested. Should be overridden by subclasses.
     *
     * @return The {@link BaseFragmentActivity} subclass that is being tested
     */
    protected Class<? extends BaseFragmentActivity> getActivityClass() {
        return BaseFragmentActivity.class;
    }

    /**
     * Method for constructing the {link Intent} to be used to start the
     * {link Activity} instance. Should be overridden by subclasses to
     * attach any additional data to be passed.
     *
     * @return The {@link Intent} used to start the {link Activity}
     */
    protected Intent getIntent() {
        return new Intent(RuntimeEnvironment.application, getActivityClass());
    }

    /**
     * Method for defining whether the activity has a drawer configured
     *
     * @return true if the drawer is configured
     */
    protected boolean hasDrawer() {
        return false;
    }

    /**
     * Method for defining whether the transition animation is overridden
     * with custom animation on restart
     *
     * @return true if the transition animation is overridden on restart
     */
    protected boolean appliesPrevTransitionOnRestart() {
        return false;
    }

    /**
     * Generic method for asserting pending transition animation
     *
     * @param shadowActivity The shadow activity
     * @param enterAnim      The enter animation resource
     * @param exitAnim       The exit animation resource
     */
    private static void assertOverridePendingTransition(ShadowActivity shadowActivity,
                                                        @AnimRes int enterAnim, @AnimRes int exitAnim) {
        assertEquals(enterAnim, shadowActivity
                .getPendingTransitionEnterAnimationResourceId());
        assertEquals(exitAnim, shadowActivity
                .getPendingTransitionExitAnimationResourceId());
    }

    /**
     * Testing overall lifecycle and setup
     */
    @Test
    public void lifecycleTest() {
        ActivityController<? extends BaseFragmentActivity> controller =
                Robolectric.buildActivity(getActivityClass()).withIntent(getIntent());
        BaseFragmentActivity activity = controller.get();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        controller.create().start();
        // Action bar state initialization
        ActionBar bar = activity.getSupportActionBar();
        if (bar != null) {
            int displayOptions = bar.getDisplayOptions();
            assertTrue((displayOptions & ActionBar.DISPLAY_HOME_AS_UP) > 0);
            assertTrue((displayOptions & ActionBar.DISPLAY_SHOW_HOME) > 0);
            assertTrue(null == activity.findViewById(android.R.id.home));
        }

        controller.postCreate(null).resume().postResume().visible();
        DrawerLayout mDrawerLayout = (DrawerLayout)
                activity.findViewById(R.id.drawer_layout);
        boolean hasDrawer = hasDrawer();
        if (mDrawerLayout != null) {
            // NavigationFragment initialization
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragmentManager.executePendingTransactions();
            assert fragmentManager.findFragmentByTag("NavigationFragment") != null;

            if (hasDrawer) {
                CharSequence contentDescription = activity.findViewById(
                        android.R.id.home).getContentDescription();
                assertFalse(mDrawerLayout.isDrawerOpen(GravityCompat.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);
                mDrawerLayout.openDrawer(GravityCompat.START);
                assertTrue(mDrawerLayout.isDrawerOpen(GravityCompat.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);
                activity.closeDrawer();
                assertFalse(mDrawerLayout.isDrawerOpen(GravityCompat.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);

                mDrawerLayout.openDrawer(GravityCompat.START);
                assertTrue(mDrawerLayout.isDrawerOpen(GravityCompat.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);
                Configuration config = activity.getResources().getConfiguration();
                assertNotEquals(Configuration.ORIENTATION_LANDSCAPE, config.orientation);
                config.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                activity.onConfigurationChanged(config);
                assertTrue(mDrawerLayout.isDrawerOpen(GravityCompat.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);
            }
        }

        // Action bar home button
        assertTrue(shadowActivity.clickMenuItem(android.R.id.home));
        if (hasDrawer) {
            assertNotNull(mDrawerLayout);
            assertTrue(mDrawerLayout.isDrawerOpen(GravityCompat.START));
            assertTrue(shadowActivity.clickMenuItem(android.R.id.home));
            assertFalse(mDrawerLayout.isDrawerOpen(GravityCompat.START));
            activity.finish();
        }
        assertThat(activity).isFinishing();
    }

    /**
     * Generic method for asserting title setup
     *
     * @param activity The activity instance
     * @param title    The expected title
     */
    protected void assertTitle(BaseFragmentActivity activity, CharSequence title) {
        ActionBar bar = activity.getSupportActionBar();
        assumeNotNull(bar);
        assumeNotNull(title);
        Typeface type = Typeface.createFromAsset(
                activity.getAssets(), "fonts/OpenSans-Semibold.ttf");
        int titleId = activity.getResources().getIdentifier(
                "action_bar_title", "id", "android");
        TextView titleTextView = (TextView) activity.findViewById(titleId);
        assumeNotNull(titleTextView);
        assertThat(titleTextView).hasCurrentTextColor(
                activity.getResources().getColor(R.color.edx_white));
        assertEquals(type, titleTextView.getTypeface());
        assertEquals(bar.getTitle(), title);
    }

    /**
     * Testing title setup
     */
    @Test
    public void setTitleTest() {
        BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).create().get();
        CharSequence title = "test";
        activity.setTitle(title);
        assertTitle(activity, title);
    }

    /**
     * Generic method for asserting view animation method functionality
     *
     * @param view    The animated view
     * @param trigger A {@link Runnable} that triggers the animation
     */
    protected void assertAnimateLayouts(View view, Runnable trigger) {
        // The foreground scheduler needs to be paused so that the
        // temporary visibility of the animated View can be verified.
        Scheduler foregroundScheduler = ShadowApplication.getInstance()
                .getForegroundThreadScheduler();
        boolean wasPaused = foregroundScheduler.isPaused();
        if (!wasPaused) {
            foregroundScheduler.pause();
        }
        assertThat(view).isGone();
        trigger.run();
        assertThat(view).isVisible();
        Animation animation = view.getAnimation();
        assertNotNull(animation);
        assertThat(animation.getStartTime())
                .isLessThanOrEqualTo(AnimationUtils.currentAnimationTimeMillis());
        assertThat(animation).hasStartOffset(0);
        foregroundScheduler.unPause();
        assertThat(view).isGone();
        if (wasPaused) {
            foregroundScheduler.pause();
        }
    }

    /**
     * Testing view animation methods
     */
    @Test
    public void animateLayoutsTest() {
        final BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).setup().get();
        final View view = new View(activity);
        view.setVisibility(View.GONE);
        activity.addContentView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        assertAnimateLayouts(view, new Runnable() {
            @Override
            public void run() {
                activity.animateLayouts(view);
            }
        });
        activity.stopAnimation(view);
        assertThat(view).hasAnimation(null);
    }

    /**
     * Generic message for asserting show info method functionality
     *
     * @param activity The activity instance
     * @param message  The message that is expected to be displayed
     * @param trigger  A {@link Runnable} that triggers the showing of the
     *                 message
     */
    protected void assertShowInfoMessage(final BaseFragmentActivity activity,
                                         final String message, final Runnable trigger) {
        final TextView messageView = (TextView)
                activity.findViewById(R.id.flying_message);
        assumeNotNull(messageView);
        assertAnimateLayouts(messageView, new Runnable() {
            @Override
            public void run() {
                trigger.run();
                assertThat(messageView).hasText(message);
            }
        });
    }

    /**
     * Testing show info method
     */
    @Test
    public void showInfoMessageTest() {
        final BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).setup().get();
        TextView messageView = new TextView(activity);
        messageView.setId(R.id.flying_message);
        messageView.setVisibility(View.GONE);
        activity.addContentView(messageView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        assertThat(messageView).hasText("");
        final String message = "test";
        assertShowInfoMessage(activity, message, new Runnable() {
            @Override
            public void run() {
                assumeTrue(activity.showInfoMessage(message));
            }
        });
    }

    /**
     * Generic method for asserting next started activity along with
     * the custom transition animation override
     *
     * @param currentActivity   The current activity
     * @param nextActivityClass The class of the newly started activity
     */
    protected Intent assertNextStartedActivity(BaseFragmentActivity currentActivity,
                                               Class<? extends Activity> nextActivityClass) {
        ShadowActivity shadowActivity = Shadows.shadowOf(currentActivity);
        Intent intent = shadowActivity.getNextStartedActivity();
        assertNotNull(intent);
        assertThat(intent).hasComponent(currentActivity, nextActivityClass);
        return intent;
    }

    /**
     * Generic method for asserting next started activity along with
     * the custom transition animation override
     *
     * @param currentActivity   The current activity
     * @param nextActivityClass The class of the newly started activity
     * @param requestCode       The request code
     */
    protected Intent assertNextStartedActivityForResult(
            BaseFragmentActivity currentActivity,
            Class<? extends Activity> nextActivityClass, int requestCode) {
        ShadowActivity shadowActivity = Shadows.shadowOf(currentActivity);
        ShadowActivity.IntentForResult intentForResult =
                shadowActivity.getNextStartedActivityForResult();
        assertNotNull(intentForResult);
        assertThat(intentForResult.intent).hasComponent(
                currentActivity, nextActivityClass);
        assertEquals(requestCode, intentForResult.requestCode);
        return intentForResult.intent;
    }

    /**
     * Testing method for enabling and disabling UI interaction
     */
    @Test
    public void tryToSetUIInteractionTest() {
        BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).setup().get();
        assertFalse(activity.tryToSetUIInteraction(true));
        assertFalse(activity.tryToSetUIInteraction(false));
    }
}
