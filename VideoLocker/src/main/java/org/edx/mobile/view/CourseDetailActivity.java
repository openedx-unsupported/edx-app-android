package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.base.CourseDetailBaseFragment;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.module.analytics.ISegment;

import roboguice.inject.InjectExtra;

public class CourseDetailActivity extends BaseSingleFragmentActivity {

    public static Intent newIntent(@NonNull Context context, @NonNull CourseDetail courseDetail) {
        return new Intent(context, CourseDetailActivity.class)
                .putExtra(CourseDetailFragment.COURSE_DETAIL, courseDetail);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.course_detail_title);
        //environment.getSegment().trackScreenView(ISegment.Screens.???? + CourseDetail.course_id); //TODO Course Detail Screen, figure out what information to send.
    }

    @Override
    public Fragment getFirstFragment() {
        return new CourseDetailFragment();
    }

    public void enrollButtonClicked(View view) {
        // TODO Enroll Button
        Toast.makeText(getApplicationContext(), "Enroll Button Clicked", Toast.LENGTH_SHORT).show();
    }
}
