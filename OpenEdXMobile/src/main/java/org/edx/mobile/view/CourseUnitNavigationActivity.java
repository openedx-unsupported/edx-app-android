package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.databinding.ViewCourseUnitPagerBinding;
import org.edx.mobile.event.CourseUpgradedEvent;
import org.edx.mobile.event.FileSelectionEvent;
import org.edx.mobile.event.VideoPlaybackEvent;
import org.edx.mobile.exception.ErrorMessage;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStatus;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.util.VideoUtil;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.adapters.CourseUnitPagerAdapter;
import org.edx.mobile.view.custom.PreLoadingListener;
import org.edx.mobile.view.dialog.CelebratoryModalDialogFragment;
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment;
import org.edx.mobile.viewModel.InAppPurchasesViewModel;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;

@AndroidEntryPoint
public class CourseUnitNavigationActivity extends CourseBaseActivity implements
        BaseCourseUnitVideoFragment.HasComponent, PreLoadingListener {
    protected Logger logger = new Logger(getClass().getSimpleName());

    private ViewPager2 pager2;
    private CourseComponent selectedUnit;

    private List<CourseComponent> unitList = new ArrayList<>();
    private CourseUnitPagerAdapter pagerAdapter;
    private InAppPurchasesViewModel iapViewModel;

    @Inject
    CourseAPI courseApi;

    @Inject
    InAppPurchasesAnalytics iapAnalytics;

    private PreLoadingListener.State viewPagerState = PreLoadingListener.State.DEFAULT;

    private boolean isFirstSection = false;
    private boolean isVideoMode = false;
    private boolean refreshCourse = false;

    private final FullscreenLoaderDialogFragment fullScreenLoader = FullscreenLoaderDialogFragment.newInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setToolbarAsActionBar();
        RelativeLayout insertPoint = (RelativeLayout) findViewById(R.id.fragment_container);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @NonNull ViewCourseUnitPagerBinding binding = ViewCourseUnitPagerBinding.inflate(inflater, null, false);
        insertPoint.addView(binding.getRoot(), 0,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        pager2 = findViewById(R.id.pager2);
        initAdapter();
        // Enforce to intercept single scrolling direction
        UiUtils.INSTANCE.enforceSingleScrollDirection(pager2);
        findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);

        getBaseBinding().gotoPrev.setOnClickListener(view -> navigatePreviousComponent());
        getBaseBinding().gotoNext.setOnClickListener(view -> navigateNextComponent());

        if (getIntent() != null) {
            isVideoMode = getIntent().getExtras().getBoolean(Router.EXTRA_IS_VIDEOS_MODE);
        }
        if (!isVideoMode) {
            getCourseCelebrationStatus();
        }
    }

    private void initAdapter() {
        pagerAdapter = new CourseUnitPagerAdapter(this, environment,
                unitList, courseData, courseUpgradeData, this);
        pager2.setAdapter(pagerAdapter);
        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                invalidateOptionsMenu();
            }

            @Override
            public void onPageSelected(int position) {
                if (pagerAdapter.getUnit(position).isMultiDevice()) {
                    // Disable ViewPager2 scrolling to enable horizontal scrolling to for the WebView (Specific HTML Components).
                    List<BlockType> horizontalBlocks = Arrays.asList(
                            BlockType.DRAG_AND_DROP_V2, BlockType.LTI_CONSUMER, BlockType.WORD_CLOUD);
                    pager2.setUserInputEnabled(!horizontalBlocks
                            .contains(pagerAdapter.getUnit(position).getType()));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    tryToUpdateForEndOfSequential();
                }
            }
        });
    }

    private void getCourseCelebrationStatus() {
        Call<CourseStatus> courseStatusCall = courseApi.getCourseStatus(courseData.getCourseId());
        courseStatusCall.enqueue(new ErrorHandlingCallback<CourseStatus>(this, null, null) {
            @Override
            protected void onResponse(@NonNull CourseStatus responseBody) {
                isFirstSection = responseBody.getCelebrationStatus().getFirstSection();
            }
        });
    }

    private void showCelebrationModal(boolean reCreate) {
        CelebratoryModalDialogFragment celebrationDialog = CelebratoryModalDialogFragment
                .newInstance(new CelebratoryModalDialogFragment.CelebratoryModelCallback() {
                    @Override
                    public void onKeepGoing() {
                        EventBus.getDefault().postSticky(new VideoPlaybackEvent(false));
                    }

                    @Override
                    public void onCelebrationShare(@NotNull View anchor) {
                        ShareUtils.showCelebrationShareMenu(CourseUnitNavigationActivity.this,
                                anchor, courseData, shareType -> environment.getAnalyticsRegistry()
                                        .trackCourseCelebrationShareClicked(courseData.getCourseId(),
                                                shareType.getUtmParamKey()));
                    }

                    @Override
                    public void celebratoryModalViewed() {
                        EventBus.getDefault().postSticky(new VideoPlaybackEvent(true));
                        if (!reCreate) {
                            courseApi.updateCourseCelebration(courseData.getCourseId())
                                    .enqueue(new ErrorHandlingCallback<Void>(CourseUnitNavigationActivity.this) {
                                        @Override
                                        protected void onResponse(@NonNull Void responseBody) {
                                            isFirstSection = false;
                                        }
                                    });
                            environment.getAnalyticsRegistry().trackCourseSectionCelebration(courseData.getCourseId());
                        }
                    }
                });
        celebrationDialog.setCancelable(false);
        celebrationDialog.show(getSupportFragmentManager(), CelebratoryModalDialogFragment.TAG);
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
        // CourseComponent#getAncestor(2) returns the section of a component
        CourseComponent currentBlockSection = selectedUnit.getAncestor(2);
        CourseComponent nextBlockSection = pagerAdapter.getUnit(pager2.getCurrentItem()).getAncestor(2);
        /*
         * Show celebratory modal when:
         * 1. We haven't arrived at component navigation from the Videos tab.
         * 2. The current section is the first section being completed (not necessarily the actual first section of the course).
         * 3. Section of the current and next components are different.
         */
        if (!isVideoMode && isFirstSection && !currentBlockSection.equals(nextBlockSection)) {
            showCelebrationModal(false);
        }
    }

    @Override
    public void refreshCourseData(@NonNull String courseId, @NonNull String componentId) {
        refreshCourse = true;
        updateCourseStructure(courseId, componentId);
    }

    @Override
    public void initializeIAPObserver() {
        iapViewModel = new ViewModelProvider(this).get(InAppPurchasesViewModel.class);
        iapViewModel.getShowFullscreenLoaderDialog().observe(this, canShowLoader -> {
            if (canShowLoader) {
                fullScreenLoader.show(getSupportFragmentManager(), FullscreenLoaderDialogFragment.TAG);
                iapViewModel.showFullScreenLoader(false);
            }
        });

        iapViewModel.getPurchaseFlowComplete().observe(this, isPurchaseCompleted -> {
            if (isPurchaseCompleted) {
                fullScreenLoader.dismiss();
                iapAnalytics.trackIAPEvent(Analytics.Events.IAP_COURSE_UPGRADE_SUCCESS, "", "");
                iapAnalytics.trackIAPEvent(Analytics.Events.IAP_UNLOCK_UPGRADED_CONTENT_TIME, "", "");
                iapAnalytics.trackIAPEvent(Analytics.Events.IAP_UNLOCK_UPGRADED_CONTENT_REFRESH_TIME, "", "");
                showPurchaseSuccessSnackbar();
                iapViewModel.resetPurchase(false);
            }
        });
    }

    @Override
    protected void onLoadData() {
        selectedUnit = courseManager.getComponentById(courseData.getCourseId(), courseComponentId);
        updateDataModel();
    }

    @Override
    protected void onCourseRefreshError(Throwable error) {
        if (fullScreenLoader != null && fullScreenLoader.isAdded()) {
            iapViewModel.dispatchError(ErrorMessage.COURSE_REFRESH_CODE, null, error);
        }
    }

    private void setCurrentUnit(CourseComponent component) {
        this.selectedUnit = component;
        if (this.selectedUnit == null)
            return;

        courseComponentId = selectedUnit.getId();
        environment.getDatabase().updateAccess(null, selectedUnit.getId(), true);

        updateUIForOrientation();

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

        getBaseBinding().gotoPrev.setEnabled(curIndex > 0);
        getBaseBinding().gotoNext.setEnabled(curIndex < pagerAdapter.getItemCount() - 1);

        findViewById(R.id.course_unit_nav_bar).requestLayout();

        setTitle(selectedUnit.getDisplayName());

        String currentSubsectionId = selectedUnit.getParent().getId();
        if (curIndex + 1 <= pagerAdapter.getItemCount() - 1) {
            String nextUnitSubsectionId = unitList.get(curIndex + 1).getParent().getId();
            if (currentSubsectionId.equalsIgnoreCase(nextUnitSubsectionId)) {
                getBaseBinding().nextUnitTitle.setVisibility(View.GONE);
            } else {
                getBaseBinding().nextUnitTitle.setText(unitList.get(curIndex + 1).getParent().getDisplayName());
                getBaseBinding().nextUnitTitle.setVisibility(View.VISIBLE);
            }
        } else {
            // we have reached the end and next button is disabled
            getBaseBinding().nextUnitTitle.setVisibility(View.GONE);
        }

        if (curIndex - 1 >= 0) {
            String prevUnitSubsectionId = unitList.get(curIndex - 1).getParent().getId();
            if (currentSubsectionId.equalsIgnoreCase(prevUnitSubsectionId)) {
                getBaseBinding().prevUnitTitle.setVisibility(View.GONE);
            } else {
                getBaseBinding().prevUnitTitle.setText(unitList.get(curIndex - 1).getParent().getDisplayName());
                getBaseBinding().prevUnitTitle.setVisibility(View.VISIBLE);
            }
        } else {
            // we have reached the start and previous button is disabled
            getBaseBinding().prevUnitTitle.setVisibility(View.GONE);
        }
        if (getBaseBinding().gotoPrev.isEnabled()) {
            getBaseBinding().gotoPrev.setTypeface(ResourcesCompat.getFont(this, R.font.inter_semi_bold));
        } else {
            getBaseBinding().gotoPrev.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        }

        if (getBaseBinding().gotoNext.isEnabled()) {
            getBaseBinding().gotoNext.setTypeface(ResourcesCompat.getFont(this, R.font.inter_semi_bold));
        } else {
            getBaseBinding().gotoNext.setTypeface(ResourcesCompat.getFont(this, R.font.inter_regular));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
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

        if (isVideoMode) {
            leaves = selectedUnit.getRoot().getVideos(false);
        } else {
            selectedUnit.getRoot().fetchAllLeafComponents(leaves, EnumSet.allOf(BlockType.class));
        }
        unitList.addAll(leaves);

        int index = unitList.indexOf(selectedUnit);

        if (refreshCourse) {
            initAdapter();
            resetPurchase();
        }

        if (index >= 0) {
            pager2.setCurrentItem(index, false);
            tryToUpdateForEndOfSequential();
        }

        if (pagerAdapter != null)
            pagerAdapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        // Add result data into the intent to trigger the signal that `courseData` is updated after
        // the course was purchased from a locked component screen.
        if (refreshCourse) {
            Intent resultData = new Intent();
            resultData.putExtra(AppConstants.COURSE_UPGRADED, true);
            setResult(RESULT_OK, resultData);
            refreshCourse = false;
        }
        super.onBackPressed();
    }

    private void resetPurchase() {
        if (fullScreenLoader.isAdded()) {
            // Persist Fullscreen loader for at least 3 seconds
            // for a better UX instead of a glitch
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    iapViewModel.resetPurchase(true);
                }
            }, fullScreenLoader.getRemainingVisibleTime());
        }
    }

    private void showPurchaseSuccessSnackbar() {
        new SnackbarErrorNotification(pager2).showError(R.string.purchase_success_message);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUIForOrientation();
        if (selectedUnit != null) {
            environment.getAnalyticsRegistry().trackCourseComponentViewed(selectedUnit.getId(),
                    courseData.getCourse().getId(), selectedUnit.getBlockId());
        }
        // Remove the celebration modal on configuration change before create a new one for landscape mode.
        Fragment celebrationModal = getSupportFragmentManager().findFragmentByTag(CelebratoryModalDialogFragment.TAG);
        if (celebrationModal != null) {
            getSupportFragmentManager().beginTransaction().remove(celebrationModal).commit();
            showCelebrationModal(true);
        }
    }

    private void updateUIForOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && VideoUtil.isCourseUnitVideo(environment, selectedUnit)) {
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
        if (selectedUnit != null && selectedUnit instanceof VideoBlockModel) {
            // Showing casting button only for native video block
            // Currently casting for youtube video isn't available
            return VideoUtil.isCourseUnitVideo(environment, selectedUnit);
        }
        return super.showGoogleCastButton();
    }

    @Subscribe
    public void onEvent(CourseUpgradedEvent event) {
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri[] results = null;
        if (requestCode == FileUtil.FILE_CHOOSER_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                // Executed when user select only one file.
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
            EventBus.getDefault().post(new FileSelectionEvent(results));
        }
    }
}
