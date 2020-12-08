package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.event.CourseUpgradedEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.adapters.CourseUnitPagerAdapter;
import org.edx.mobile.view.custom.PreLoadingListener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.InjectView;

public class CourseUnitNavigationActivity extends CourseBaseActivity implements
        BaseCourseUnitVideoFragment.HasComponent, PreLoadingListener {
    protected Logger logger = new Logger(getClass().getSimpleName());

    private ViewPager2 pager2;
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

        pager2 = findViewById(R.id.pager2);
        pagerAdapter = new CourseUnitPagerAdapter(this, environment.getConfig(),
                unitList, courseData, courseUpgradeData, this);
        pager2.setAdapter(pagerAdapter);
        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                invalidateOptionsMenu();
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    tryToUpdateForEndOfSequential();
                }
            }
        });
        // Enforce to intercept single scrolling direction
        UiUtil.enforceSingleScrollDirection(pager2);
        findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);

        mPreviousBtn.setOnClickListener(view -> navigatePreviousComponent());
        mNextBtn.setOnClickListener(view -> navigateNextComponent());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        /*
         * If the youtube player is not in a proper state then it throws the IllegalStateException.
         * To avoid the crash and continue the flow we are catching the exception.
         *
         * It may occur when the edX app was in background and user kills the on-device YouTube app.
         */
        try {
            super.onSaveInstanceState(outState);
        } catch (IllegalStateException e) {
            logger.error(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIForOrientation();
    }

    @Override
    public void navigatePreviousComponent() {
        int index = pager2.getCurrentItem();
        if (index > 0) {
            pager2.setCurrentItem(index - 1);
        }
    }

    @Override
    public void navigateNextComponent() {
        int index = pager2.getCurrentItem();
        if (index < pagerAdapter.getItemCount() - 1) {
            pager2.setCurrentItem(index + 1);
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
        int curIndex = pager2.getCurrentItem();
        setCurrentUnit(pagerAdapter.getUnit(curIndex));

        mPreviousBtn.setEnabled(curIndex > 0);
        mNextBtn.setEnabled(curIndex < pagerAdapter.getItemCount() - 1);

        findViewById(R.id.course_unit_nav_bar).requestLayout();

        setTitle(selectedUnit.getDisplayName());

        String currentSubsectionId = selectedUnit.getParent().getId();
        if (curIndex + 1 <= pagerAdapter.getItemCount() - 1) {
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
        if(mPreviousBtn.isEnabled()){
            mPreviousBtn.setTypeface(ResourcesCompat.getFont(this, R.font.inter_semi_bold));
        } else{
            mPreviousBtn.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        }

        if(mNextBtn.isEnabled()){
            mNextBtn.setTypeface(ResourcesCompat.getFont(this, R.font.inter_semi_bold));
        } else{
            mNextBtn.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
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
            pager2.setCurrentItem(index, false);
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

    @Override
    public boolean showGoogleCastButton() {
        if (pager2 != null && pagerAdapter != null) {
            CourseUnitFragment fragment = ((CourseUnitFragment) pagerAdapter.getItem(pager2.getCurrentItem()));
            return fragment != null && fragment.hasCastSupportedVideoContent();
        }
        return super.showGoogleCastButton();
    }

    public void onEvent(CourseUpgradedEvent event) {
        finish();
    }
}
