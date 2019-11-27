package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.event.CourseDashboardRefreshEvent;
import org.edx.mobile.event.CourseUpgradedEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.view.Router.EXTRA_ANNOUNCEMENTS;
import static org.edx.mobile.view.Router.EXTRA_COURSE_DATA;
import static org.edx.mobile.view.Router.EXTRA_COURSE_ID;
import static org.edx.mobile.view.Router.EXTRA_DISCUSSION_THREAD_ID;
import static org.edx.mobile.view.Router.EXTRA_DISCUSSION_TOPIC_ID;
import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

public class CourseTabsDashboardActivity extends OfflineSupportBaseActivity {
    public static Intent newIntent(@NonNull Context activity,
                                   @Nullable EnrolledCoursesResponse courseData,
                                   @Nullable String courseId,
                                   @Nullable String topicId,
                                   @Nullable String threadId, boolean announcements,
                                   @Nullable @ScreenDef String screenName) {
        final Intent intent = new Intent(activity, CourseTabsDashboardActivity.class);
        intent.putExtra(EXTRA_COURSE_DATA, courseData);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_DISCUSSION_TOPIC_ID, topicId);
        intent.putExtra(EXTRA_DISCUSSION_THREAD_ID, threadId);
        intent.putExtra(EXTRA_ANNOUNCEMENTS, announcements);
        intent.putExtra(EXTRA_SCREEN_NAME, screenName);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    public Fragment getFirstFragment() {
        return CourseTabsDashboardFragment.newInstance(getIntent().getExtras());
    }

    @Override
    public Object getRefreshEvent() {
        return new CourseDashboardRefreshEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().removeStickyEvent(CourseUpgradedEvent.class);
    }
}
