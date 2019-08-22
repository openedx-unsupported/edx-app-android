package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.view.adapters.CourseUnitPagerAdapter;
import org.edx.mobile.view.custom.DisableableViewPager;
import org.edx.mobile.view.custom.PreLoadingListener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.InjectView;

public class CourseUnitNavigationActivity extends CourseBaseActivity implements
        CourseUnitVideoFragment.HasComponent, PreLoadingListener {
    protected Logger logger = new Logger(getClass().getSimpleName());

    private DisableableViewPager pager;
    private CourseComponent selectedUnit;

    private List<CourseComponent> unitList = new ArrayList<>();
    private CourseUnitPagerAdapter pagerAdapter;

    @InjectView(R.id.goto_next)
    private Button mNextBtn;
    @InjectView(R.id.goto_prev)
    private Button mPreviousBtn;
    @InjectView(R.id.next_unit_title)
    private TextView mNextUnitLbl;
    @InjectView(R.id.prev_unit_title)
    private TextView mPreviousUnitLbl;

    private PreLoadingListener.State viewPagerState = PreLoadingListener.State.DEFAULT;

    @Inject
    LastAccessManager lastAccessManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout insertPoint = (RelativeLayout) findViewById(R.id.fragment_container);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.view_course_unit_pager, null);
        insertPoint.addView(v, 0,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        pager = findViewById(R.id.pager);
        pagerAdapter = new CourseUnitPagerAdapter(getSupportFragmentManager(),
                environment.getConfig(), unitList, courseData, this);
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private Boolean firstTime = true;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (firstTime) {
                    firstTime = false;
                    CourseUnitFragment initialPage = (CourseUnitFragment) pagerAdapter.instantiateItem(pager, position);
                    if (initialPage != null) {
                        initialPage.onPageShow();
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    tryToUpdateForEndOfSequential();
                }
            }
        });

        findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);

        mPreviousBtn.setOnClickListener(view -> navigatePreviousComponent());
        mNextBtn.setOnClickListener(view -> navigateNextComponent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIForOrientation();
    }

    @Override
    public void navigatePreviousComponent() {
        int index = pager.getCurrentItem();
        if (index > 0) {
            pager.setCurrentItem(index - 1);
        }
    }

    @Override
    public void navigateNextComponent() {
        int index = pager.getCurrentItem();
        if (index < pagerAdapter.getCount() - 1) {
            pager.setCurrentItem(index + 1);
        }
    }

    @Override
    protected void onLoadData() {
        selectedUnit = courseManager.getComponentById(blocksApiVersion, courseData.getCourse().getId(), courseComponentId);
        updateDataModel();
    }

    private void setCurrentUnit(CourseComponent component) {
        this.selectedUnit = component;
        if (this.selectedUnit == null)
            return;

        courseComponentId = selectedUnit.getId();
        environment.getDatabase().updateAccess(null, selectedUnit.getId(), true);

        updateUIForOrientation();

        lastAccessManager.setLastAccessed(selectedUnit.getCourseId(), this.selectedUnit.getId());

        Intent resultData = new Intent();
        resultData.putExtra(Router.EXTRA_COURSE_COMPONENT_ID, courseComponentId);
        setResult(RESULT_OK, resultData);

        environment.getAnalyticsRegistry().trackScreenView(
                Analytics.Screens.UNIT_DETAIL, courseData.getCourse().getId(), selectedUnit.getInternalName());
        environment.getAnalyticsRegistry().trackCourseComponentViewed(selectedUnit.getId(),
                courseData.getCourse().getId(), selectedUnit.getBlockId());
    }

    private void tryToUpdateForEndOfSequential() {
        int curIndex = pager.getCurrentItem();
        setCurrentUnit(pagerAdapter.getUnit(curIndex));

        mPreviousBtn.setEnabled(curIndex > 0);
        mNextBtn.setEnabled(curIndex < pagerAdapter.getCount() - 1);

        findViewById(R.id.course_unit_nav_bar).requestLayout();

        setTitle(selectedUnit.getDisplayName());

        String currentSubsectionId = selectedUnit.getParent().getId();
        if (curIndex + 1 <= pagerAdapter.getCount() - 1) {
            String nextUnitSubsectionId = unitList.get(curIndex + 1).getParent().getId();
            if (currentSubsectionId.equalsIgnoreCase(nextUnitSubsectionId)) {
                mNextUnitLbl.setVisibility(View.GONE);
            } else {
                mNextUnitLbl.setText(unitList.get(curIndex + 1).getParent().getDisplayName());
                mNextUnitLbl.setVisibility(View.VISIBLE);
            }
        } else {
            // we have reached the end and next button is disabled
            mNextUnitLbl.setVisibility(View.GONE);
        }

        if (curIndex - 1 >= 0) {
            String prevUnitSubsectionId = unitList.get(curIndex - 1).getParent().getId();
            if (currentSubsectionId.equalsIgnoreCase(prevUnitSubsectionId)) {
                mPreviousUnitLbl.setVisibility(View.GONE);
            } else {
                mPreviousUnitLbl.setText(unitList.get(curIndex - 1).getParent().getDisplayName());
                mPreviousUnitLbl.setVisibility(View.VISIBLE);
            }
        } else {
            // we have reached the start and previous button is disabled
            mPreviousUnitLbl.setVisibility(View.GONE);
        }
    }

    private void updateDataModel() {
        unitList.clear();
        if (selectedUnit == null || selectedUnit.getRoot() == null) {
            logger.warn("selectedUnit is null?");
            return;   //should not happen
        }

        //if we want to navigate through all unit of within the parent node,
        //we should use courseComponent instead.   Requirement maybe changed?
        // unitList.addAll( courseComponent.getChildLeafs() );
        List<CourseComponent> leaves = new ArrayList<>();

        boolean isVideoMode = false;
        if (getIntent() != null) {
            isVideoMode = getIntent().getExtras().getBoolean(Router.EXTRA_IS_VIDEOS_MODE);
        }
        if (isVideoMode) {
            leaves = selectedUnit.getRoot().getVideos(false);
        } else {
            selectedUnit.getRoot().fetchAllLeafComponents(leaves, EnumSet.allOf(BlockType.class));
        }
        unitList.addAll(leaves);
        pagerAdapter.notifyDataSetChanged();

        int index = unitList.indexOf(selectedUnit);
        if (index >= 0) {
            pager.setCurrentItem(index);
            tryToUpdateForEndOfSequential();
        }

        if (pagerAdapter != null)
            pagerAdapter.notifyDataSetChanged();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUIForOrientation();
        if (selectedUnit != null) {
            environment.getAnalyticsRegistry().trackCourseComponentViewed(selectedUnit.getId(),
                    courseData.getCourse().getId(), selectedUnit.getBlockId());
        }
    }

    private void updateUIForOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && CourseUnitPagerAdapter.isCourseUnitVideo(selectedUnit)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setActionBarVisible(false);
            findViewById(R.id.course_unit_nav_bar).setVisibility(View.GONE);

        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setActionBarVisible(true);
            findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);
        }
    }

    public CourseComponent getComponent() {
        return selectedUnit;
    }

    @Override
    public void setLoadingState(@NonNull State newState) {
        viewPagerState = newState;
    }

    @Override
    public boolean isMainUnitLoaded() {
        return viewPagerState == State.MAIN_UNIT_LOADED;
    }
}
