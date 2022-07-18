package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.course.CourseDetail;
import org.edx.mobile.module.analytics.Analytics;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity that holds the fragments related to the course detail.
 */
@AndroidEntryPoint
public class CourseDetailActivity extends BaseSingleFragmentActivity {

    public static Intent newIntent(@NonNull Context context, @NonNull CourseDetail courseDetail) {
        return new Intent(context, CourseDetailActivity.class)
                .putExtra(CourseDetailFragment.COURSE_DETAIL, courseDetail);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.COURSE_INFO_SCREEN);
    }

    @Override
    public Fragment getFirstFragment() {
        final CourseDetailFragment courseDetailFragment = new CourseDetailFragment();
        courseDetailFragment.setArguments(getIntent().getExtras());
        return courseDetailFragment;
    }
}
