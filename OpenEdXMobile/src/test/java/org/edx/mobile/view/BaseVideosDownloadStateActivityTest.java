package org.edx.mobile.view;

import android.view.Menu;
import android.view.MenuItem;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseVideosDownloadStateActivity;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.util.AppConstants;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class BaseVideosDownloadStateActivityTest extends BaseFragmentActivityTest {
    /**
     * Method for defining the subclass of {@link BaseVideosDownloadStateActivity}
     * that is being tested. Should be overridden by subclasses.
     *
     * @return The {@link BaseVideosDownloadStateActivity} subclass that is being
     *         tested
     */
    @Override
    protected Class<? extends BaseVideosDownloadStateActivity> getActivityClass() {
        return BaseVideosDownloadStateActivity.class;
    }

    /**
     * Testing options menu initialization
     */
    @Test
    public void initializeOptionsMenuTest() {
        BaseVideosDownloadStateActivity activity =
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).setup().get();
        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        assertNotNull(menu);
        // Can't see any method to confirm action layout source as well
        MenuItem progressItem = menu.findItem(R.id.download_progress);
        assertNotNull(progressItem);
        assertThat(progressItem).hasTitle(activity.getText(R.string.action_settings));
    }

    /**
     * Testing download progress menu visibility states and click behaviour
     * (starting DownloadActivity). Only when both AppConstants.offline_flag
     * is true and there is a downloading entry in the database, should the
     * progress bar be visible.
     */
    @Test
    public void downloadProgressViewTest() {
        connectToNetwork();
        assertFalse(Shadows.shadowOf(Robolectric.buildActivity(getActivityClass())
                .withIntent(getIntent()).setup().get())
                .getOptionsMenu()
                .findItem(R.id.download_progress)
                .isVisible());

        disconnectFromNetwork();
        assertFalse(Shadows.shadowOf(Robolectric.buildActivity(getActivityClass())
                .withIntent(getIntent()).setup().get())
                .getOptionsMenu()
                .findItem(R.id.download_progress)
                .isVisible());

        IDatabase db = environment.getDatabase();
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
        assertFalse(Shadows.shadowOf(Robolectric.buildActivity(getActivityClass())
                .withIntent(getIntent()).setup().get())
                .getOptionsMenu()
                .findItem(R.id.download_progress)
                .isVisible());

        connectToNetwork();
        BaseVideosDownloadStateActivity activity =
                Robolectric.buildActivity(getActivityClass())
                        .withIntent(getIntent()).setup().get();
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        MenuItem downloadProgressMenuItem = shadowActivity
                .getOptionsMenu()
                .findItem(R.id.download_progress);
        assertTrue(downloadProgressMenuItem.isVisible());
        assertTrue(downloadProgressMenuItem.getActionView().performClick());
        assertNextStartedActivity(activity, DownloadListActivity.class);
    }
}
