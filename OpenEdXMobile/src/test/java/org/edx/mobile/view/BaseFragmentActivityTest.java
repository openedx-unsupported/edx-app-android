package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import androidx.annotation.AnimRes;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.res.ResourcesCompat;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.util.Scheduler;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
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
                Robolectric.buildActivity(getActivityClass(), getIntent());
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

        // Action bar home button
        assertTrue(shadowActivity.clickMenuItem(android.R.id.home));
        activity.finish();
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
        Typeface type = ResourcesCompat.getFont(activity, R.font.inter_semi_bold);
        int titleId = activity.getResources().getIdentifier(
                "action_bar_title", "id", "android");
        TextView titleTextView = (TextView) activity.findViewById(titleId);
        assumeNotNull(titleTextView);
        assertThat(titleTextView).hasCurrentTextColor(
                activity.getResources().getColor(R.color.neutralWhite));
        assertEquals(type, titleTextView.getTypeface());
        assertEquals(bar.getTitle(), title);
    }

    /**
     * Testing title setup
     */
    @Test
    public void setTitleTest() {
        BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass(), getIntent()).create().get();
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
                Robolectric.buildActivity(
                    getActivityClass(),
                    getIntent()
                ).setup().get();
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
                Robolectric.buildActivity(getActivityClass(), getIntent()).setup().get();
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
                Robolectric.buildActivity(getActivityClass(), getIntent()).setup().get();
        assertFalse(activity.tryToSetUIInteraction(true));
        assertFalse(activity.tryToSetUIInteraction(false));
    }
}
