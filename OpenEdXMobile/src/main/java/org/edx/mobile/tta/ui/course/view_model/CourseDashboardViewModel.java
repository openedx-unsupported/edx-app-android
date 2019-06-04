package org.edx.mobile.tta.ui.course.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.course.CourseMaterialTab;
import org.edx.mobile.tta.ui.course.discussion.CourseDiscussionTab;
import org.edx.mobile.tta.ui.course.discussion.DiscussionLandingFragment;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.AuthenticatedWebViewFragment;
import org.edx.mobile.view.CourseHandoutFragment;
import org.edx.mobile.view.Router;
import org.edx.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class CourseDashboardViewModel extends BaseViewModel {

    public CourseDashboardPagerAdapter adapter;
    private List<Fragment> fragments;
    private List<String> titles;

    public Content content;
    private EnrolledCoursesResponse course;
    private CourseComponent rootComponent;

    public ObservableBoolean offlineVisible = new ObservableBoolean();
    public ObservableInt initialPosition = new ObservableInt();
    public ObservableInt tabPosition = new ObservableInt();

    private int position;

    public ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            initialPosition.set(i);
            PageViewStateCallback callback = (PageViewStateCallback) fragments.get(i);
            if (callback != null){
                callback.onPageShow();
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public CourseDashboardViewModel(BaseVMActivity activity, Content content, int tabPosition) {
        super(activity);
        this.content = content;
        position = tabPosition;
        adapter = new CourseDashboardPagerAdapter(mActivity.getSupportFragmentManager());
        fragments = new ArrayList<>();
        titles = new ArrayList<>();
        loadCourseData();
    }

    @Override
    public void onResume() {
        super.onResume();
        onEventMainThread(new NetworkConnectivityChangeEvent());
    }

    private void loadCourseData(){
        mActivity.showLoading();

        mDataManager.getCourse(content.getSource_identity(), new OnResponseCallback<EnrolledCoursesResponse>() {
            @Override
            public void onSuccess(EnrolledCoursesResponse data) {
                course = data;
                mDataManager.getCourseComponent(course.getCourse().getId(),
                        new OnResponseCallback<CourseComponent>() {
                            @Override
                            public void onSuccess(CourseComponent data) {
                                rootComponent = data;
                                mActivity.hideLoading();
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

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
                setTabs();
            }
        });

    }

    public void setTabs(){
        fragments.add(CourseMaterialTab.newInstance(content, course, rootComponent));
        titles.add(mActivity.getString(R.string.course_material));

        /*CourseDiscussionTopicsFragment discussionFragment = new CourseDiscussionTopicsFragment();
        if (course != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(Router.EXTRA_COURSE_DATA, course);
            discussionFragment.setArguments(bundle);
        }
        discussionFragment.setRetainInstance(true);
        fragments.add(discussionFragment);*/
        fragments.add(CourseDiscussionTab.newInstance(course));
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

        Uri uri = Uri.parse(course == null ? "" : course.getCourse().getCourse_about())
                .buildUpon()
                .appendQueryParameter(Constants.KEY_HIDE_ACTION, "true")
                .build();
        fragments.add(AuthenticatedWebViewFragment.newInstance(uri.toString()));
        titles.add(mActivity.getString(R.string.about));

        /*if (rootComponent != null) {
            for (IBlock block: rootComponent.getChildren()){
                CourseComponent comp = (CourseComponent) block;

                if (comp.isContainer()) {
                    for (IBlock childBlock : comp.getChildren()) {
                        CourseComponent child = (CourseComponent) childBlock;

                        if (child.getDisplayName().contains("कोर्स के बारे में")) {
                            CourseComponent childComp = (CourseComponent) child.getChildren().get(0);
                            fragments.add(AuthenticatedWebViewFragment.newInstance(childComp.getBlockUrl()));
                            titles.add(mActivity.getString(R.string.about));
                            break;
                        }
                    }
                }
            }
        }*/

        try {
            adapter.setFragments(fragments, titles);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.tabPosition.set(position);

        PageViewStateCallback callback = (PageViewStateCallback) fragments.get(position);
        if (callback != null){
            callback.onPageShow();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event){
        if (NetworkUtil.isConnected(mActivity)){
            offlineVisible.set(false);
        } else {
            offlineVisible.set(true);
        }
    }

    public void registerEventBus(){
        EventBus.getDefault().register(this);
    }

    public void unRegisterEventBus(){
        EventBus.getDefault().unregister(this);
    }

    public void openShareMenu(View anchor) {

        if (course == null){
            return;
        }

        ShareUtils.showCourseShareMenu(getActivity(), anchor, course,
                mDataManager.getEdxEnvironment().getAnalyticsRegistry(), mDataManager.getEdxEnvironment());

    }

    public class CourseDashboardPagerAdapter extends BasePagerAdapter {

        public CourseDashboardPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }

}
