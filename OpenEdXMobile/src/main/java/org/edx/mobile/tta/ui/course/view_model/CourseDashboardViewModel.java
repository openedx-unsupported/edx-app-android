package org.edx.mobile.tta.ui.course.view_model;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.edx.mobile.R;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.course.CourseAboutTab;
import org.edx.mobile.tta.ui.course.CourseDiscussionTab;
import org.edx.mobile.tta.ui.course.CourseHandoutsTab;
import org.edx.mobile.tta.ui.course.CourseMaterialTab;
import org.edx.mobile.view.AuthenticatedWebViewFragment;
import org.edx.mobile.view.CourseDiscussionTopicsFragment;
import org.edx.mobile.view.CourseHandoutFragment;
import org.edx.mobile.view.Router;

import java.util.ArrayList;
import java.util.List;

public class CourseDashboardViewModel extends BaseViewModel {

    public CourseDashboardPagerAdapter adapter;
    private List<Fragment> fragments;
    private List<String> titles;

    public Content content;
    private EnrolledCoursesResponse course;

    public CourseDashboardViewModel(Context context, TaBaseFragment fragment, Content content) {
        super(context, fragment);
        this.content = content;
        adapter = new CourseDashboardPagerAdapter(mActivity.getSupportFragmentManager());
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        loadCourseData();
    }

    private void loadCourseData(){
        mActivity.showLoading();

        mDataManager.getCourse(content.getSource_identity(), new OnResponseCallback<EnrolledCoursesResponse>() {
            @Override
            public void onSuccess(EnrolledCoursesResponse data) {
                course = data;
                setTabs();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
                setTabs();
            }
        });

    }

    public void setTabs(){
        fragments.add(CourseMaterialTab.newInstance(content, course));
        titles.add(mActivity.getString(R.string.course_material));

        CourseDiscussionTopicsFragment discussionFragment = new CourseDiscussionTopicsFragment();
        if (course != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, course);
            discussionFragment.setArguments(bundle);
        }
        discussionFragment.setRetainInstance(true);
        fragments.add(discussionFragment);
        titles.add(mActivity.getString(R.string.discussion));

        CourseHandoutFragment handoutFragment = new CourseHandoutFragment();
        if (course != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, course);
            handoutFragment.setArguments(bundle);
        }
        handoutFragment.setRetainInstance(true);
        fragments.add(handoutFragment);
        titles.add(mActivity.getString(R.string.handouts));

        if (course != null && course.getCourse() != null) {
            fragments.add(AuthenticatedWebViewFragment.newInstance(course.getCourse().getCourse_about()));
            titles.add(mActivity.getString(R.string.about));
        }

        try {
            adapter.addFragments(fragments, titles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class CourseDashboardPagerAdapter extends BasePagerAdapter {

        public CourseDashboardPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }

}
