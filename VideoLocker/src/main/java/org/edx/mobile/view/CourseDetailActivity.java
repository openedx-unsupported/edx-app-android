/*
 * CourseDetailActivity
 *
 * Activity that holds the fragments related to the course detail.
 */

package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.course.CourseDetail;

public class CourseDetailActivity extends BaseSingleFragmentActivity {

    public static Intent newIntent(@NonNull Context context, @NonNull CourseDetail courseDetail) {
        return new Intent(context, CourseDetailActivity.class)
                .putExtra(CourseDetailFragment.COURSE_DETAIL, courseDetail);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.find_courses_title);
        //environment.getSegment().trackScreenView(ISegment.Screens.???? + CourseDetail.course_id); //TODO Course Detail Screen, figure out what information to send.
    }

    @Override
    public Fragment getFirstFragment() {
        return new CourseDetailFragment();
    }
}
