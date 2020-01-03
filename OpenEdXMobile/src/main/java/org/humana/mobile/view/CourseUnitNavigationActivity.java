package org.humana.mobile.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.humana.mobile.R;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.model.course.BlockType;
import org.humana.mobile.model.course.CourseComponent;
import org.humana.mobile.module.analytics.Analytics;
import org.humana.mobile.services.LastAccessManager;
import org.humana.mobile.services.ViewPagerDownloadManager;
import org.humana.mobile.tta.Constants;
import org.humana.mobile.tta.data.DataManager;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.custom.DropDownFilterView;
import org.humana.mobile.view.adapters.CourseUnitPagerAdapter;
import org.humana.mobile.view.common.PageViewStateCallback;
import org.humana.mobile.view.custom.DisableableViewPager;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import roboguice.inject.InjectView;


/**
 *
 */
public class CourseUnitNavigationActivity extends CourseBaseActivity implements CourseUnitVideoFragment.HasComponent {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private DisableableViewPager pager;
    private CourseComponent selectedUnit;

    private List<CourseComponent> unitList = new ArrayList<>();
    private CourseUnitPagerAdapter pagerAdapter;
    public final String EXTRA_Unit_TYPE = "unitType";
    public  final String EXTRA_TITLE = "unitTitle";

    private FloatingActionButton mfabDialog;
    DataManager mDataManager;

    @InjectView(R.id.goto_next)
    private Button mNextBtn;
    @InjectView(R.id.goto_prev)
    private Button mPreviousBtn;
    @InjectView(R.id.next_unit_title)
    private TextView mNextUnitLbl;
    @InjectView(R.id.prev_unit_title)
    private TextView mPreviousUnitLbl;

    private float unitRating;
    private String unitType, unitTitle;

    @Inject
    LastAccessManager lastAccessManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout insertPoint = (RelativeLayout) findViewById(R.id.fragment_container);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.view_course_unit_pager, null);
        insertPoint.addView(v, 0,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        pager = (DisableableViewPager) findViewById(R.id.pager);
        pagerAdapter = new CourseUnitPagerAdapter(getSupportFragmentManager(),
                environment.getConfig(), unitList, courseData, this);
        pager.setAdapter(pagerAdapter);

        mfabDialog = findViewById(R.id.fab);
        mDataManager = DataManager.getInstance(CourseUnitNavigationActivity.this);


        savedInstanceState = getIntent().getExtras();
        unitType = savedInstanceState.getString(EXTRA_Unit_TYPE, "");
        unitTitle = savedInstanceState.getString(EXTRA_TITLE,"");


        if (!Constants.UNIT_ID.equals(""))
        {
            mfabDialog.setVisibility(View.VISIBLE);
        }


        mfabDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                approveReturn(Constants.UNIT_ID, unitType, unitTitle, "");
            }
        });

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    int curIndex = pager.getCurrentItem();
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, curIndex);
                    if (curView != null)
                        curView.onPageDisappear();
                }
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int curIndex = pager.getCurrentItem();
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, curIndex);
                    if (curView != null)
                        curView.onPageShow();
                    tryToUpdateForEndOfSequential();
                }
            }
        });

        findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);

        mPreviousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigatePreviousComponent();
            }
        });
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateNextComponent();
            }
        });
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
            PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, index);
            if (curView != null)
                curView.onPageDisappear();
            pager.setCurrentItem(index - 1);
        }
    }

    @Override
    public void navigateNextComponent() {
        int index = pager.getCurrentItem();
        if (index < pagerAdapter.getCount() - 1) {
            PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, index);
            if (curView != null)
                curView.onPageDisappear();
            pager.setCurrentItem(index + 1);
        }
    }

    @Override
    protected void onLoadData() {
        selectedUnit = courseManager.getComponentById(courseData.getCourse().getId(), courseComponentId);
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

        ViewPagerDownloadManager.instance.setMainComponent(selectedUnit, unitList);

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

    protected void hideLastAccessedView(View v) {
    }

    protected void showLastAccessedView(View v, String title, View.OnClickListener listener) {
    }

    @Override
    protected void onOffline() {
    }


    public void approveReturn(String unitId, String type, String title, String desc) {
        final Dialog dialog = new Dialog(CourseUnitNavigationActivity.this);
        dialog.setContentView(R.layout.dialog_approve_return_unit);
        Button btnApprove = (Button) dialog.findViewById(R.id.btn_approve);
        Button btnReturn = (Button) dialog.findViewById(R.id.btn_return);
        EditText etRemarks = dialog.findViewById(R.id.et_remarks);
        DropDownFilterView filterView = dialog.findViewById(R.id.filter_view);
//        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        TextView mtv_rating = dialog.findViewById(R.id.tv_ratings);
//        EditText dialogText =  dialog.findViewById(R.id.et_period_name);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);

        List<DropDownFilterView.FilterItem> items = new ArrayList<>();

        List<String> filterItems = new ArrayList<>();
        filterItems.add("Ratings");
        filterItems.add("Poor");
        filterItems.add("Fair");
        filterItems.add("Good");
        filterItems.add("Very Good");
        filterItems.add("Excellent");
        for (String filterItem : filterItems) {
            items.add(new DropDownFilterView.FilterItem(filterItem, filterItem,
                    false, R.color.primary_cyan, R.drawable.t_background_tag_hollow
            ));
        }
        filterView.setFilterItems(items);
        filterView.setOnFilterItemListener((v, item, position, prev) -> {
            switch (item.getName()) {
                case "Poor":
                    unitRating = 1;
                    break;
                case "Fair":
                    unitRating = 2;
                    break;
                case "Good":
                    unitRating = 3;
                    break;
                case "Very Good":
                    unitRating = 4;
                    break;
                case "Excellent":
                    unitRating = 5;
                    break;
            }
        });
        btnApprove.setOnClickListener(v -> {
            String remarks = etRemarks.getText().toString();
            approveUnits(unitId, remarks, (int) unitRating, type, title,desc);
            dialog.dismiss();
        });

        btnReturn.setOnClickListener(v -> {
            String remarks = etRemarks.getText().toString();

            rejectUnits(unitId, remarks, (int) unitRating, type, title, desc);
            dialog.dismiss();
        });
        dialog.setCancelable(true);
        dialog.show();

    }


    public void approveUnits(String unitId, String remarks, int rating, String unitType, String unitTitle, String desc) {

        mDataManager.approveUnit(unitId,
                Constants.USERNAME, remarks, rating, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        Toast.makeText(CourseUnitNavigationActivity.this,"Unit Approved", Toast.LENGTH_SHORT).show();
                        sendNotifications(unitTitle, unitType ,desc, "AprroveUnit",unitId,
                                mDataManager.getLoginPrefs().getProgramId(), Constants.USERNAME);
                        CourseUnitNavigationActivity.this.finish();

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
    }

    public void rejectUnits(String unitId, String remarks, int rating, String unitType, String unitTitle, String unitDesc) {
        mDataManager.rejectUnit(unitId,
                Constants.USERNAME, remarks, rating, new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse data) {
                        Toast.makeText(CourseUnitNavigationActivity.this,"Unit Returned", Toast.LENGTH_SHORT).show();
                        sendNotifications(unitTitle,unitType ,unitDesc, "ReturnUnit",unitId,
                                mDataManager.getLoginPrefs().getProgramId(),Constants.USERNAME);

                        CourseUnitNavigationActivity.this.finish();

                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });
    }

    private void sendNotifications(String title, String type, String desc, String action,
                                   String action_id, String action_parent_id, String respondent) {
        String unique_id = Settings.Secure.getString(CourseUnitNavigationActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mDataManager.sendNotifications(title, type, desc, action, action_id, action_parent_id,
                respondent,unique_id,
                new OnResponseCallback<SuccessResponse>() {
                    @Override
                    public void onSuccess(SuccessResponse response) {
                        if (response.getSuccess()){
//                            Toast.makeText(CourseUnitNavigationActivity.this,"Notification sent..", Toast.LENGTH_SHORT).show();
                            Log.d("Notification", "Notification sent..");
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.d("Notification Failure..", e.getMessage());
                    }
                });
    }

}


