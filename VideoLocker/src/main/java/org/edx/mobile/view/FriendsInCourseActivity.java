package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.CourseEntry;

public class FriendsInCourseActivity extends BaseSingleFragmentActivity {

    private static final String TAG = FriendsInCourseActivity.class.getCanonicalName();
    public static final String EXTRA_COURSE = TAG + ".course";
    public static final String EXTRA_FRIENDS_TAB_LINK = TAG + ".showLink";
    private CourseEntry course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        course = (CourseEntry) getIntent().getSerializableExtra(FriendsInCourseActivity.EXTRA_COURSE);
        if (course == null) {
            throw new IllegalArgumentException("missing course");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().getBooleanExtra(FriendsInCourseActivity.EXTRA_FRIENDS_TAB_LINK, false)) {
            setTitle(course.getName());
        } else {
            setTitle(getString(R.string.friends_in_this_course));
        }
    }

    @Override
    public Fragment getFirstFragment() {

        CourseEntry courseData = (CourseEntry) getIntent().getSerializableExtra(FriendsInCourseActivity.EXTRA_COURSE);

        Bundle args = new Bundle();
        args.putSerializable(FriendsInCourseFragment.ARG_COURSE, courseData);

        if (getIntent().getBooleanExtra(FriendsInCourseActivity.EXTRA_FRIENDS_TAB_LINK, false)){

            args.putSerializable(FriendsInCourseFragment.ARG_SHOW_COURSE_LINK, true);

        }

        Fragment fragment = new FriendsInCourseFragment();
        fragment.setArguments(args);

        return fragment;
    }

}
