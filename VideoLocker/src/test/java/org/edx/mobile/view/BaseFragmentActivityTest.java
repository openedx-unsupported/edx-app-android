package org.edx.mobile.view;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.event.FlyingMessageEvent;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.dialog.WebViewDialogFragment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowView;
import org.robolectric.util.ActivityController;

import de.greenrobot.event.EventBus;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

// TODO: Test network connectivity change events too, after we manage to mock them
@RunWith(RobolectricGradleTestRunner.class)
public class BaseFragmentActivityTest {
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
     * Method for defining whether the activity has a drawer configured
     *
     * @return true if the drawer is configured
     */
    protected boolean hasDrawer() {
        return false;
    }

    /**
     * Method for defining whether the onTick() method is called periodically
     *
     * @return true if onTick() is called
     */
    protected boolean runsOnTick() {
        return true;
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
     * Testing window content overlay hack for API level 18
     */
    @Test
    @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void updateActionBarShadowTest() {
        BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass()).create().get();

        // Get the content view
        View contentView = activity.findViewById(android.R.id.content);

        // Make sure it's a valid instance of a FrameLayout
        assumeThat(contentView, instanceOf(FrameLayout.class));
        TypedValue tv = new TypedValue();

        // Get the windowContentOverlay value of the current theme
        assumeTrue(activity.getTheme().resolveAttribute(
                android.R.attr.windowContentOverlay, tv, true));

        // If it's a valid resource, confirm that is has been set as
        // the foreground drawable for the content view
        assumeTrue(tv.resourceId != 0);
        Drawable contentForeground = ((FrameLayout) contentView).getForeground();
        assertEquals(tv.resourceId, Shadows.shadowOf(
                contentForeground).getCreatedFromResId());
    }

    /**
     * Testing full screen mode in landscape
     */
    @Test
    @Config(qualifiers = "land")
    public void landscapeFullscreenTest() {
        ActivityController<? extends BaseFragmentActivity> controller =
                Robolectric.buildActivity(getActivityClass());
        BaseFragmentActivity activity = controller.get();
        activity.getResources().getConfiguration().orientation =
                Configuration.ORIENTATION_LANDSCAPE;
        controller.create();
        Resources.Theme theme = activity.getTheme();
        TypedValue tv = new TypedValue();
        assertTrue(theme.resolveAttribute(android.R.attr.windowContentOverlay, tv, true));
        assertEquals(TypedValue.TYPE_NULL, tv.type);
        controller.start();
        Window window = activity.getWindow();
        int windowAttributes = window.getAttributes().flags;
        assertTrue((windowAttributes & WindowManager.LayoutParams.FLAG_FULLSCREEN) > 0);
        assertTrue(window.hasFeature(Window.FEATURE_NO_TITLE));
    }

    /**
     * Generic method to assert action bar visibility state on a specified orientation
     *
     * @param orientation The orientation it should be tested on
     * @param expected The expected visibility state
     */
    private void assertActionBarShowing(int orientation, boolean expected) {
        ActivityController<? extends BaseFragmentActivity> controller =
                Robolectric.buildActivity(getActivityClass());
        BaseFragmentActivity activity = controller.get();
        activity.getResources().getConfiguration().orientation = orientation;
        controller.create().start();
        ActionBar bar = activity.getActionBar();
        assumeNotNull(bar);
        assertEquals(expected, bar.isShowing());
    }

    /**
     * Testing whether action bar is displayed in portrait orientation
     */
    @Test
    @Config(qualifiers = "port")
    public void showActionBarOnPortraitTest() {
        assertActionBarShowing(Configuration.ORIENTATION_PORTRAIT, true);
    }

    /**
     * Testing whether action bar is hidden in landscape orientation
     */
    @Test
    @Config(qualifiers = "land")
    public void hideActionBarOnLandscapeTest() {
        assertActionBarShowing(Configuration.ORIENTATION_LANDSCAPE, false);
    }

    /**
     * Generic method for asserting pending transition animation
     *
     * @param shadowActivity The shadow activity
     * @param enterAnim The enter animation resource
     * @param exitAnim The exit animation resource
     */
    private static void assertOverridePendingTransition(ShadowActivity shadowActivity,
            @AnimRes int enterAnim, @AnimRes int exitAnim) {
        assertEquals(enterAnim, shadowActivity
                .getPendingTransitionEnterAnimationResourceId());
        assertEquals(exitAnim, shadowActivity
                .getPendingTransitionExitAnimationResourceId());
    }

    /**
     * Assert pending transition animation override with custom slide animation
     *
     * @param shadowActivity The shadow activity
     */
    private void assertAppliedTransitionNext(ShadowActivity shadowActivity) {
        assertOverridePendingTransition(shadowActivity,
                R.anim.slide_in_from_end, R.anim.slide_out_to_start);
    }

    /**
     * Assert previous transition animation override with custom slide animation
     *
     * @param shadowActivity The shadow activity
     */
    private void assertAppliedTransitionPrev(ShadowActivity shadowActivity) {
        assertOverridePendingTransition(shadowActivity,
                R.anim.slide_in_from_start, R.anim.slide_out_to_end);
    }

    /**
     * Testing overall lifecycle and setup
     */
    @Test
    public void lifecycleTest() {
        ActivityController<? extends BaseFragmentActivity> controller =
                Robolectric.buildActivity(getActivityClass());
        BaseFragmentActivity activity = controller.get();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);

        controller.create();
        // Check custom transition animation override
        assertAppliedTransitionNext(shadowActivity);

        controller.start();
        // Social features state persistence
        PrefManager pmFeatures = new PrefManager(activity, PrefManager.Pref.FEATURES);
        assertEquals(NetworkUtil.isSocialFeatureFlagEnabled(activity),
                pmFeatures.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, false));
        // Action bar state initialization
        ActionBar bar = activity.getActionBar();
        if (bar != null) {
            int displayOptions = bar.getDisplayOptions();
            assertTrue((displayOptions & ActionBar.DISPLAY_HOME_AS_UP) > 0);
            assertTrue((displayOptions & ActionBar.DISPLAY_SHOW_HOME) > 0);
            Drawable homeIcon = ((ImageView) activity.findViewById(
                    android.R.id.home)).getDrawable();
            assertTrue(homeIcon == null || (homeIcon instanceof ColorDrawable
                    && ((ColorDrawable) homeIcon).getColor() == Color.TRANSPARENT));
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
                assertFalse(mDrawerLayout.isDrawerOpen(Gravity.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);
                mDrawerLayout.openDrawer(Gravity.START);
                assertTrue(mDrawerLayout.isDrawerOpen(Gravity.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);
                activity.closeDrawer();
                assertFalse(mDrawerLayout.isDrawerOpen(Gravity.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);

                mDrawerLayout.openDrawer(Gravity.START);
                assertTrue(mDrawerLayout.isDrawerOpen(Gravity.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);
                Configuration config = activity.getResources().getConfiguration();
                assertNotEquals(Configuration.ORIENTATION_LANDSCAPE, config.orientation);
                config.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                activity.onConfigurationChanged(config);
                assertTrue(mDrawerLayout.isDrawerOpen(Gravity.START));
                assertEquals(activity.getText(R.string.label_close), contentDescription);
            }
        }

        // Action bar home button
        shadowActivity.clickMenuItem(android.R.id.home);
        if (hasDrawer) {
            assertNotNull(mDrawerLayout);
            assertTrue(mDrawerLayout.isDrawerOpen(Gravity.START));
            shadowActivity.clickMenuItem(android.R.id.home);
            assertFalse(mDrawerLayout.isDrawerOpen(Gravity.START));
            activity.finish();
        }
        assertThat(activity).isFinishing();

        // Check custom transition animation override
        assertAppliedTransitionPrev(shadowActivity);
    }

    /**
     * Testing options menu initialization
     */
    @Test
    public void initializeOptionsMenuTest() {
        BaseFragmentActivity activity =
                Robolectric.setupActivity(getActivityClass());
        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        assertNotNull(menu);
        MenuItem offlineItem = menu.findItem(R.id.offline);
        assertThat(offlineItem).hasTitle(activity.getText(R.string.offline_text));
        // Can't see any method to confirm action layout source as well
        MenuItem progressItem = menu.findItem(R.id.progress_download);
        assertThat(progressItem).hasTitle(activity.getText(R.string.action_settings));
        MenuItem deleteItem = menu.findItem(R.id.delete_checkbox);
        assertThat(deleteItem).hasTitle(activity.getText(R.string.label_delete));
        MenuItem doneItem = menu.findItem(R.id.done_btn);
        assertThat(doneItem).hasTitle(activity.getText(R.string.done_text));
        MenuItem unreadDisplayItem = menu.findItem(R.id.unread_display);
        assertThat(unreadDisplayItem).hasTitle(activity.getText(R.string.unread_text));
    }

    /**
     * Generic method for asserting title setup
     *
     * @param activity The activity instance
     * @param title The expected title
     */
    protected void assertTitle(BaseFragmentActivity activity, CharSequence title) {
        ActionBar bar = activity.getActionBar();
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
        assertThat(bar).hasTitle(title);
    }

    /**
     * Testing title setup
     */
    @Test
    public void setTitleTest() {
        BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass()).create().get();
        CharSequence title = "test";
        activity.setTitle(title);
        assertTitle(activity, title);
    }

    /**
     * Generic method for asserting view animation method functionality
     *
     * @param view The animated view
     */
    protected void assertAnimateLayouts(View view) {
        assertThat(view).isVisible();
        Animation animation = view.getAnimation();
        assertNotNull(animation);
        assertThat(animation.getStartTime())
                .isLessThanOrEqualTo(AnimationUtils.currentAnimationTimeMillis());
        assertThat(animation)
                .hasStartOffset(0)
                .isExactlyInstanceOf(TranslateAnimation.class);
        TranslateAnimation translateAnimation = (TranslateAnimation) animation;
        assertThat(translateAnimation).hasDuration(500).isFillingAfter();
    }

    /**
     * Testing view animation methods
     */
    @Test
    public void animateLayoutsTest() {
        BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass()).create().get();
        View view = new View(activity);
        view.setVisibility(View.GONE);
        activity.setContentView(view);
        assertThat(view).isGone();
        activity.animateLayouts(view);
        assertAnimateLayouts(view);
        activity.stopAnimation(view);
        assertThat(view).hasAnimation(null);
    }

    /**
     * Generic message for asserting show info method functionality
     *
     * @param activity The activity instance
     * @param message The message that is expected to be displayed
     */
    protected void assertShowInfoMessage(
            BaseFragmentActivity activity, String message) {
        TextView messageView = (TextView)
                activity.findViewById(R.id.downloadMessage);
        assumeNotNull(messageView);
        assertThat(messageView).hasText(message);
        assertAnimateLayouts(messageView);
    }

    /**
     * Testing show info method
     */
    @Test
    public void showInfoMessageTest() {
        BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass()).create().get();
        TextView messageView = new TextView(activity);
        messageView.setId(R.id.downloadMessage);
        messageView.setVisibility(View.GONE);
        activity.setContentView(messageView);
        assertThat(messageView).hasText("").isGone();
        String message = "test";
        assumeTrue(activity.showInfoMessage(message));
        assertShowInfoMessage(activity, message);
    }

    /**
     * Testing show info method
     */
    @Test
    public void showOfflineAccessMessage() {
        BaseFragmentActivity activity =
                Robolectric.buildActivity(getActivityClass()).create().get();
        View offlineView = new View(activity);
        offlineView.setId(R.id.offline_access_panel);
        offlineView.setVisibility(View.GONE);
        activity.setContentView(offlineView);
        assertThat(offlineView).isGone();
        activity.showOfflineAccessMessage();
        assertAnimateLayouts(offlineView);
    }

    /**
     * Testing transition animation custom override on restart
     */
    @Test
    public void applyPrevTransitionOnRestartTest() {
        assumeTrue(appliesPrevTransitionOnRestart());
        ActivityController<? extends BaseFragmentActivity> controller =
                Robolectric.buildActivity(getActivityClass()).create().start();
        ShadowActivity shadowActivity = Shadows.shadowOf(controller.get());
        assertAppliedTransitionNext(shadowActivity);
        controller.stop().start().resume();
        assertAppliedTransitionPrev(shadowActivity);
    }

    /**
     * Generic method for asserting next started activity along with
     * the custom transition animation override
     *
     * @param currentActivity The current activity
     * @param nextActivityClass The class of the newly started activity
     */
    protected void assertNextStartedActivity(BaseFragmentActivity currentActivity,
            Class<? extends Activity> nextActivityClass) {
        ShadowActivity shadowActivity = Shadows.shadowOf(currentActivity);
        Intent intent = shadowActivity.peekNextStartedActivity();
        assertThat(intent).hasComponent(currentActivity, nextActivityClass);
        assertAppliedTransitionNext(shadowActivity);
    }

    /**
     * Testing download progress menu visibility states and click behaviour
     * (starting DownloadActivity). Only when both AppConstants.offline_flag
     * is true and there is a downloading entry in the database, should the
     * progress bar be visible.
     */
    @Test
    public void downloadProgressViewTest() {
        assumeTrue(runsOnTick());

        AppConstants.offline_flag = false;
        assertFalse(Shadows.shadowOf(Robolectric.setupActivity(getActivityClass()))
                .getOptionsMenu()
                .findItem(R.id.progress_download)
                .isVisible());

        AppConstants.offline_flag = true;
        assertFalse(Shadows.shadowOf(Robolectric.setupActivity(getActivityClass()))
                .getOptionsMenu()
                .findItem(R.id.progress_download)
                .isVisible());

        IDatabase db = DatabaseFactory.getInstance(DatabaseFactory.TYPE_DATABASE_NATIVE);
        DownloadEntry de = new DownloadEntry();
        de.username = "unittest";
        de.title = "title";
        de.videoId = "videoId-" + System.currentTimeMillis();
        de.size = 1024;
        de.duration = 3600;
        de.filepath = "/fakepath";
        de.url = "http://fake/url";
        de.eid = "fake_eid";
        de.chapter = "fake_chapter";
        de.section = "fake_section";
        de.lastPlayedOffset = 0;
        de.lmsUrl = "http://fake/lms/url";
        de.isCourseActive = 1;
        de.downloaded = DownloadEntry.DownloadedState.DOWNLOADING;
        Long rowId = db.addVideoData(de, null);
        assertNotNull(rowId);
        assertThat(rowId).isGreaterThan(0);
        assertFalse(Shadows.shadowOf(Robolectric.setupActivity(getActivityClass()))
                .getOptionsMenu()
                .findItem(R.id.progress_download)
                .isVisible());

        AppConstants.offline_flag = false;
        BaseFragmentActivity activity =
                Robolectric.setupActivity(getActivityClass());
        MenuItem progressItem = Shadows.shadowOf(activity)
                .getOptionsMenu()
                .findItem(R.id.progress_download);
        assertTrue(progressItem.isVisible());
        assertTrue(ShadowView.clickOn(progressItem.getActionView()));
        assertNextStartedActivity(activity, DownloadListActivity.class);
    }

    /**
     * Generic method for testing proper display of WebViewDialogFragment
     *
     * @param activity The activity instance
     * @param url The url provided
     * @param showTitle Whether the title is displayed
     * @param title The dialog title
     */
    protected static void showWebDialogTest(BaseFragmentActivity activity,
            String url, boolean showTitle, String title) {
        activity.showWebDialog(url, true, title);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.executePendingTransactions();
        Fragment webViewFragment = fragmentManager.findFragmentByTag("web-view-dialog");
        assertNotNull(webViewFragment);
        assertThat(webViewFragment).isInstanceOf(WebViewDialogFragment.class);
        WebViewDialogFragment webViewDialogFragment = (WebViewDialogFragment) webViewFragment;
        assertTrue(webViewDialogFragment.getShowsDialog());
        assertThat(webViewDialogFragment.getDialog()).isShowing();
        View dialogView = webViewDialogFragment.getView();
        assertNotNull(dialogView);
        WebView webView = (WebView) dialogView.findViewById(R.id.eula_webView);
        assertNotNull(webView);
        assertThat(webView).hasUrl(url);
        TextView titleView = (TextView) dialogView.findViewById(R.id.tv_dialog_title);
        assertNotNull(titleView);
        if (showTitle) {
            assertThat(titleView).isVisible().hasText(title);
        } else {
            assertThat(titleView).isNotVisible();
        }
        webViewDialogFragment.dismiss();
        fragmentManager.executePendingTransactions();
    }

    /**
     * Testing method for displaying web view dialog fragment
     */
    @Test
    public void showWebDialogTest() {
        BaseFragmentActivity activity =
                Robolectric.setupActivity(getActivityClass());
        String url = "https://www.edx.org";
        String title = "title";
        showWebDialogTest(activity, url, true, title);
        showWebDialogTest(activity, url, false, null);
        showWebDialogTest(activity, url, false, title);
        showWebDialogTest(activity, url, true, null);
    }

    /**
     * Testing flying info message display
     */
    @Test
    public void infoFlyingMessageDisplayTest() {
        BaseFragmentActivity activity =
                Robolectric.setupActivity(getActivityClass());
        assumeNotNull(activity.findViewById(R.id.downloadMessage));
        String message = "message";
        EventBus eventBus = EventBus.getDefault();
        assertNull(eventBus.getStickyEvent(FlyingMessageEvent.class));
        eventBus.postSticky(new FlyingMessageEvent(message));
        assertNull(eventBus.getStickyEvent(FlyingMessageEvent.class));
        assertShowInfoMessage(activity, message);
    }

    /**
     * Generic method for testing proper display of flying error messages
     *
     * @param activity The activity instance
     * @param header The message header
     * @param message The error message
     */
    protected void errorFlyingMessageDisplayTest(BaseFragmentActivity activity,
            String header, String message) {
        EventBus eventBus = EventBus.getDefault();
        assertNull(eventBus.getStickyEvent(FlyingMessageEvent.class));
        eventBus.postSticky(new FlyingMessageEvent(
                FlyingMessageEvent.MessageType.ERROR, header, message));
        assertNull(eventBus.getStickyEvent(FlyingMessageEvent.class));

        View errorView = activity.findViewById(R.id.error_layout);
        assertNotNull(errorView);
        assertThat(errorView).isInstanceOf(ViewGroup.class);
        TextView errorHeaderView = (TextView)
                activity.findViewById(R.id.error_header);
        TextView errorMessageView = (TextView)
                activity.findViewById(R.id.error_message);
        assertNotNull(errorHeaderView);
        assertNotNull(errorMessageView);
        if (TextUtils.isEmpty(header)) {
            assertThat(errorHeaderView).isNotVisible();
        } else {
            assertThat(errorHeaderView).isVisible().hasText(header);
        }
        if (message == null) {
            message = "";
        }
        assertThat(errorMessageView).hasText(message);
        assertAnimateLayouts(errorView);
    }

    /**
     * Testing different combinations of flying error messages
     */
    @Test
    public void errorFlyingMessageDisplayTest() {
        BaseFragmentActivity activity =
                Robolectric.setupActivity(getActivityClass());
        View errorView = activity.findViewById(R.id.error_layout);
        assumeNotNull(errorView);
        assumeThat(errorView, instanceOf(ViewGroup.class));

        String header = "header";
        String message = "message";
        errorFlyingMessageDisplayTest(activity, header, message);
        errorFlyingMessageDisplayTest(activity, null, message);
        errorFlyingMessageDisplayTest(activity, header, null);
        errorFlyingMessageDisplayTest(activity, null, null);
    }

    /**
     * Testing method for enabling and disabling UI interaction
     */
    @Test
    public void tryToSetUIInteractionTest() {
        BaseFragmentActivity activity =
                Robolectric.setupActivity(getActivityClass());
        assertFalse(activity.tryToSetUIInteraction(true));
        assertFalse(activity.tryToSetUIInteraction(false));
    }
}
