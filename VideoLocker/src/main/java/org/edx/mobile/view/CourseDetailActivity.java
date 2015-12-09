package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.base.CourseDetailBaseFragment;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.module.analytics.ISegment;

import roboguice.inject.InjectExtra;

public class CourseDetailActivity extends BaseSingleFragmentActivity {

    @InjectExtra(Router.EXTRA_COURSE_DETAIL)
    private CourseDetail courseDetail;

    private Fragment fragment;

    public static Intent newIntent(@NonNull Context context) {
        return new Intent(context, CourseDetailActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.course_detail_title);
//        environment.getSegment().trackScreenView(); TODO COURSE DETAILLLLLLL SCREEENNNNN
    }

    @Override
    public Fragment getFirstFragment() {
        fragment = new CourseDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Router.EXTRA_COURSE_DETAIL, courseDetail);
        fragment.setArguments(bundle);
        return fragment;
    }
}
