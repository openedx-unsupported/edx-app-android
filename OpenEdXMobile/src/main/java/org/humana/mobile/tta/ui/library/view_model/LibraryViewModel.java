package org.humana.mobile.tta.ui.library.view_model;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Gravity;

import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Category;
import org.humana.mobile.tta.data.local.db.table.Period;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.library.CollectionConfigResponse;
import org.humana.mobile.tta.data.model.program.NotificationCountResponse;
import org.humana.mobile.tta.event.CourseEnrolledEvent;
import org.humana.mobile.tta.event.program.ShowStudentUnitsEvent;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.BasePagerAdapter;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.interfaces.SearchPageOpenedListener;
import org.humana.mobile.tta.ui.programs.curricullam.CurricullamFragment;
import org.humana.mobile.tta.ui.programs.pendingUnits.PendingUsersFragment;
import org.humana.mobile.tta.ui.programs.schedule.ScheduleFragment;
import org.humana.mobile.tta.ui.programs.students.StudentsFragment;
import org.humana.mobile.tta.ui.programs.units.UnitsFragment;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitsViewModel;
import org.humana.mobile.view.CourseDiscussionTopicsFragment;
import org.humana.mobile.view.Router;
import org.humana.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.ResponseBody;

public class LibraryViewModel extends BaseViewModel {

    private List<Fragment> fragments;
    private List<String> titles;
    public ListingPagerAdapter adapter;

    private CollectionConfigResponse cr;
    private List<Category> categories;
    private SearchPageOpenedListener searchPageOpenedListener;
    private EnrolledCoursesResponse course;

    public ObservableInt initialPosition = new ObservableInt();
    public ObservableInt tabPosition = new ObservableInt();
    public ObservableInt tooltipGravity = new ObservableInt();
    public ObservableInt tooltipPosition = new ObservableInt();
    public ObservableField<String> tooltipText = new ObservableField();

    public ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            initialPosition.set(i);
            tabPosition.set(i);
            PageViewStateCallback callback = (PageViewStateCallback) fragments.get(i);
            if (callback != null) {
                callback.onPageShow();
            }


        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public LibraryViewModel(Context context, TaBaseFragment fragment, SearchPageOpenedListener searchPageOpenedListener) {
        super(context, fragment);

        mActivity.showLoading();
        categories = new ArrayList<>();
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        this.searchPageOpenedListener = searchPageOpenedListener;

        adapter = new ListingPagerAdapter(mFragment.getChildFragmentManager());

        getEnrolledCourse();

        // populateTabs();

//        getData();

    }

    private void populateTabs() {
        mActivity.showLoading();
        fragments.clear();
        titles.clear();
        ArrayList<String> demolist = new ArrayList<String>();
        demolist.add("Schedule");
        demolist.add("Units");
        if (mDataManager.getLoginPrefs().getRole() != null) {
            if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                demolist.add("Pending units");
            }
        }
        demolist.add("Students");
        demolist.add("Discussion");
        demolist.add("Curriculum");

        try {
            ScheduleFragment scheduleFragment = new ScheduleFragment();
            if (course != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Router.EXTRA_COURSE_DATA, course);
                scheduleFragment.setArguments(bundle);
            }
            if (!scheduleFragment.isAdded()) {
                fragments.add(scheduleFragment);
            }

            UnitsFragment unitsFragment = new UnitsFragment();
            if (course != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Router.EXTRA_COURSE_DATA, course);
                unitsFragment.setArguments(bundle);
            }
            if (!unitsFragment.isAdded()) {
                fragments.add(unitsFragment);
            }
            PendingUsersFragment pendingUsersFragment = new PendingUsersFragment();
            if (mDataManager.getLoginPrefs().getRole() != null) {
                if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Instructor.name())) {
                    if (!pendingUsersFragment.isAdded()) {
                        fragments.add(pendingUsersFragment);
                    }
                }
            }
            StudentsFragment studentsFragment = new StudentsFragment();
            if (!studentsFragment.isAdded()) {
                fragments.add(new StudentsFragment());
            }
            CourseDiscussionTopicsFragment discussionFragment = new CourseDiscussionTopicsFragment();
            if (course != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Router.EXTRA_COURSE_DATA, course);
                discussionFragment.setArguments(bundle);

                discussionFragment.setRetainInstance(true);
                if (!discussionFragment.isAdded()) {
                    fragments.add(discussionFragment);
                }
            } else {
                if (!discussionFragment.isAdded()) {
                    fragments.add(discussionFragment);
                }
            }
            CurricullamFragment curricullamFragment = new CurricullamFragment();
            if (!curricullamFragment.isAdded()) {
                fragments.add(new CurricullamFragment());
            }
            try {
                adapter.setFragments(fragments, demolist);
            }catch (IllegalStateException ie){
                ie.printStackTrace();
            }
            mActivity.hideLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        if (!mDataManager.getLoginPrefs().isScheduleTootipSeen()) {
//            setTooltip();
//        }
    }

    private void getEnrolledCourse() {

        mDataManager.getenrolledCourseByOrg("Humana", new OnResponseCallback<List<EnrolledCoursesResponse>>() {
            @Override
            public void onSuccess(List<EnrolledCoursesResponse> data) {

                if (mDataManager.getLoginPrefs().getProgramId() != null) {
                    for (EnrolledCoursesResponse item : data) {
                        if (item.getCourse().getId().trim().toLowerCase()
                                .equals(mDataManager.getLoginPrefs().getProgramId().trim().toLowerCase())) {
                            course = item;
                            break;
                        }

                    }
                }
                populateTabs();
            }

            @Override
            public void onFailure(Exception e) {
                populateTabs();
            }
        });

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CourseEnrolledEvent event) {
        this.course = event.getCourse();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ShowStudentUnitsEvent event) {
        tabPosition.set(1);
    }

    public void registerEventBus() {
        EventBus.getDefault().registerSticky(this);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }


    public class ListingPagerAdapter extends BasePagerAdapter {
        public ListingPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }

    private void setTooltip(){
        tooltipGravity.set(Gravity.BOTTOM);
        tooltipText.set("Tabs");
        tooltipPosition.set(2);

    }

}
