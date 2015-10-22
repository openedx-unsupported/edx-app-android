package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import org.edx.mobile.R;
import org.edx.mobile.event.DownloadEvent;
import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import de.greenrobot.event.EventBus;

import static org.assertj.android.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

public abstract class CourseBaseActivityTest extends BaseFragmentActivityTest {
    /**
     * Method for defining the subclass of {@link CourseBaseActivity} that
     * is being tested. Should be overridden by subclasses.
     *
     * @return The {@link CourseBaseActivity} subclass that is being tested
     */
    @Override
    protected Class<? extends CourseBaseActivity> getActivityClass() {
        return CourseBaseActivity.class;
    }

    /**
     * Parameterized flag for whether to provide the course ID explicitly, or
     * allow CourseBaseActivity to fallback to loading the base course.
     */
    @Parameter
    public boolean provideCourseId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Intent getIntent() {
        EnrolledCoursesResponse courseData;
        try {
            courseData = api.getEnrolledCourses().get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Intent intent = super.getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(Router.EXTRA_ENROLLMENT, courseData);
        if (provideCourseId) {
            CourseComponent courseComponent;
            try {
                courseComponent = serviceManager.getCourseStructure(
                        courseData.getCourse().getId(),
                        OkHttpUtil.REQUEST_CACHE_TYPE.IGNORE_CACHE);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            extras.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseComponent.getId());
        }
        intent.putExtra(Router.EXTRA_BUNDLE, extras);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean appliesPrevTransitionOnRestart() {
        return true;
    }

    /**
     * Testing initialization
     */
    @Test
    @SuppressLint("RtlHardcoded")
    public void initializeTest() {
        ActivityController<? extends CourseBaseActivity> controller =
                Robolectric.buildActivity(getActivityClass()).withIntent(getIntent());
        CourseBaseActivity activity = controller.get();

        controller.create();
        assertNotNull(activity.findViewById(R.id.offline_bar));
        assertNotNull(activity.findViewById(R.id.last_access_bar));
        assertNotNull(activity.findViewById(R.id.video_download_indicator));
        View downloadInProgressButton =
                activity.findViewById(R.id.download_in_progress_button);
        assertNotNull(downloadInProgressButton);
        DrawerLayout drawerLayout = (DrawerLayout)
                activity.findViewById(R.id.drawer_layout);
        if (drawerLayout != null) {
            assertEquals(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                    drawerLayout.getDrawerLockMode(Gravity.LEFT));
            assertEquals(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                    drawerLayout.getDrawerLockMode(Gravity.RIGHT));
        }

        controller.postCreate(null).resume().postResume().visible();
        // Is there any way to test options menu invalidation?
        assertTrue(downloadInProgressButton.performClick());
        assertNextStartedActivity(activity, DownloadListActivity.class);
    }

    /**
     * Testing functionality upon receiving a DownloadEvent
     */
    @Test
    @Ignore // Hangs indefinitely. Fixed in https://github.com/robolectric/robolectric/pull/2017
    public void downloadEventTest() {
        CourseBaseActivity activity =
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).setup().get();
        View downloadProgressBar =
                activity.findViewById(R.id.download_in_progress_bar);
        assumeNotNull(downloadProgressBar);
        assertThat(downloadProgressBar).isNotVisible();
        EventBus eventBus = EventBus.getDefault();
        eventBus.post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
        assertDownloadProgressBarVisible(activity.getWindow().getDecorView(),
                downloadProgressBar);
    }

    /**
     * Method for asserting the functionality of
     * {@link CourseBaseActivity#setVisibilityForDownloadProgressView} which
     * is called upon receiving a {@link DownloadEvent}. The base
     * implementation just makes the download progress bar visible
     * immediately, but this behaviour may be overridden in subclasses.
     *
     * @param rootView The root View for finding Views by id
     * @param downloadProgressBar The download progress bar View
     */
    protected void assertDownloadProgressBarVisible(
            View rootView, View downloadProgressBar) {
        assertThat(downloadProgressBar).isVisible();
    }

    /**
     * Testing process start and finish method functionality
     */
    @Test
    public void processLifecycleTest() {
        // We need to retrieve the progressWheel view before calling visible(), since that
        // initializes fragment views as well, which might add other views with the same id
        ActivityController<? extends CourseBaseActivity> controller =
                Robolectric.buildActivity(getActivityClass()).withIntent(getIntent())
                        .create().start().postCreate(null).resume();
        CourseBaseActivity activity = controller.get();
        ProgressBar progressWheel = (ProgressBar)
                activity.findViewById(R.id.progress_spinner);
        controller.visible();
        if (progressWheel == null) {
            activity.startProcess();
            activity.finishProcess();
        } else {
            assertThat(progressWheel).isNotVisible();
            activity.startProcess();
            assertThat(progressWheel).isVisible();
            activity.finishProcess();
            assertThat(progressWheel).isNotVisible();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void initializeOptionsMenuTest() {
        ShadowActivity shadowActivity = Shadows.shadowOf(
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).setup().get());
        Menu menu = shadowActivity.getOptionsMenu();
        assertNotNull(menu);
        MenuItem shareOnWebItem = menu.findItem(R.id.action_share_on_web);
        if (menu.findItem(R.id.action_share_on_web) != null) {
            Drawable shareOnWebIcon = shareOnWebItem.getIcon();
            assertThat(shareOnWebIcon).isInstanceOf(IconDrawable.class);
            // IconDrawable doesn't expose any property getters..
            // should we use reflection? Or add it to the imported class?

            shadowActivity.clickMenuItem(R.id.action_share_on_web);
            // How to get the shown custom PopupMenu?
        }

        MenuItem changeModelItem = menu.findItem(R.id.action_change_mode);
        if (changeModelItem != null) {
            Drawable shareOnWebIcon = changeModelItem.getIcon();
            assertThat(shareOnWebIcon).isInstanceOf(IconDrawable.class);
        }
    }

    /**
     * Ignoring download progress menu visibility states testing defined in
     * {@link BaseFragmentActivityTest}, as since {@link CourseBaseActivity}
     * overrides the {@link android.app.Activity#onPrepareOptionsMenu(Menu)}
     * implementation, there is no longer any testable point where onTick() is
     * called. The correct implementation for it is only provided in the
     * @{link CourseVideoListActivity} subclass anyway.
     */
    @Override
    @Ignore
    @Test
    public void downloadProgressViewTest() {}
}
