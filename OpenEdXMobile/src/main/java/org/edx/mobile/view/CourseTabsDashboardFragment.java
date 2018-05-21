package org.edx.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentDashboardErrorLayoutBinding;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.custom.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

public class CourseTabsDashboardFragment extends TabsBaseFragment {
    protected final Logger logger = new Logger(getClass().getName());

    @Nullable
    private FragmentDashboardErrorLayoutBinding errorLayoutBinding;

    private EnrolledCoursesResponse courseData;

    @Inject
    private AnalyticsRegistry analyticsRegistry;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateDownloadProgressRunnable;
    private MenuItem downloadsMenuItem;
    private boolean wasDownloadItemVisibleBeforeStopping = false;

    @NonNull
    public static CourseTabsDashboardFragment newInstance(EnrolledCoursesResponse courseData) {
        final CourseTabsDashboardFragment fragment = new CourseTabsDashboardFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        courseData = (EnrolledCoursesResponse) getArguments().getSerializable(Router.EXTRA_COURSE_DATA);
        getActivity().setTitle(courseData.getCourse().getName());
        setHasOptionsMenu(courseData.getCourse().getCoursewareAccess().hasAccess());
        environment.getAnalyticsRegistry().trackScreenView(
                Analytics.Screens.COURSE_DASHBOARD, courseData.getCourse().getId(), null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.course_dashboard_menu, menu);
        if (environment.getConfig().isCourseSharingEnabled()) {
            menu.findItem(R.id.menu_item_share).setVisible(true);
        } else {
            menu.findItem(R.id.menu_item_share).setVisible(false);
        }
        handleDownloadProgressMenuItem(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (courseData.getCourse().getCoursewareAccess().hasAccess()) {
            return super.onCreateView(inflater, container, savedInstanceState);
        } else {
            errorLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard_error_layout, container, false);
            errorLayoutBinding.errorMsg.setText(R.string.course_not_started);
            return errorLayoutBinding.getRoot();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                ShareUtils.showCourseShareMenu(getActivity(), getActivity().findViewById(R.id.menu_item_share),
                        courseData, analyticsRegistry, environment);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Only re-run the runnable if it was previously running when onStop was called which stopped it
        if (updateDownloadProgressRunnable != null && wasDownloadItemVisibleBeforeStopping) {
            updateDownloadProgressRunnable.run();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (updateDownloadProgressRunnable != null) {
            handler.removeCallbacks(updateDownloadProgressRunnable);
            wasDownloadItemVisibleBeforeStopping = downloadsMenuItem.isVisible();
            updateDownloadProgressRunnable = null;
        }
    }

    public void handleDownloadProgressMenuItem(Menu menu) {
        downloadsMenuItem = menu.findItem(R.id.menu_item_download_progress);
        final View progressView = downloadsMenuItem.getActionView();
        final ProgressWheel progressWheel = (ProgressWheel)
                progressView.findViewById(R.id.progress_wheel);
        downloadsMenuItem.setVisible(downloadsMenuItem.isVisible());
        progressWheel.setProgress(progressWheel.getProgress());
        progressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showDownloads(getActivity());
            }
        });
        if (updateDownloadProgressRunnable == null) {
            updateDownloadProgressRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!NetworkUtil.isConnected(getContext()) ||
                            !environment.getDatabase().isAnyVideoDownloading(null)) {
                        downloadsMenuItem.setVisible(false);
                    } else {
                        downloadsMenuItem.setVisible(true);
                        environment.getStorage().getAverageDownloadProgress(
                                new DataCallback<Integer>() {
                                    @Override
                                    public void onResult(Integer result) {
                                        int progressPercent = result;
                                        if (progressPercent >= 0 && progressPercent <= 100) {
                                            progressWheel.setProgressPercent(progressPercent);
                                        }
                                    }

                                    @Override
                                    public void onFail(Exception ex) {
                                        logger.error(ex);
                                    }
                                });
                    }
                    handler.postDelayed(this, DateUtils.SECOND_IN_MILLIS);
                }
            };
            updateDownloadProgressRunnable.run();
        }
    }

    @Override
    protected boolean showTitleInTabs() {
        return false;
    }

    @Override
    public List<FragmentItemModel> getFragmentItems() {
        ArrayList<FragmentItemModel> items = new ArrayList<>();
        // Add course outline tab
        items.add(new FragmentItemModel(CourseOutlineFragment.class, courseData.getCourse().getName(),
                FontAwesomeIcons.fa_list_alt,
                CourseOutlineFragment.makeArguments(courseData, null, null, false),
                new FragmentItemModel.FragmentStateListener() {
                    @Override
                    public void onFragmentSelected() {
                        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.COURSE_OUTLINE,
                                courseData.getCourse().getId(), null);
                        setDownloadProgressMenuItemVisibility(true);
                    }
                }));
        // Add videos tab
        if (environment.getConfig().isCourseVideosEnabled()) {
            items.add(new FragmentItemModel(CourseOutlineFragment.class,
                    getResources().getString(R.string.videos_title), FontAwesomeIcons.fa_film
                    , CourseOutlineFragment.makeArguments(courseData, null, null, true),
                    new FragmentItemModel.FragmentStateListener() {
                        @Override
                        public void onFragmentSelected() {
                            environment.getAnalyticsRegistry().trackScreenView(
                                    Analytics.Screens.VIDEOS_COURSE_VIDEOS, courseData.getCourse().getId(), null);
                            setDownloadProgressMenuItemVisibility(false);
                        }
                    }));
        }
        // Add discussion tab
        if (environment.getConfig().isDiscussionsEnabled() &&
                !TextUtils.isEmpty(courseData.getCourse().getDiscussionUrl())) {
            items.add(new FragmentItemModel(CourseDiscussionTopicsFragment.class,
                    getResources().getString(R.string.discussion_title), FontAwesomeIcons.fa_comments_o,
                    new FragmentItemModel.FragmentStateListener() {
                        @Override
                        public void onFragmentSelected() {
                            environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FORUM_VIEW_TOPICS,
                                    courseData.getCourse().getId(), null, null);
                            setDownloadProgressMenuItemVisibility(false);
                        }
                    }));
        }
        // Add important dates tab
        if (environment.getConfig().isCourseDatesEnabled()) {
            items.add(new FragmentItemModel(CourseDatesFragment.class,
                    getResources().getString(R.string.course_dates_title), FontAwesomeIcons.fa_calendar,
                    CourseDatesFragment.makeArguments(getContext(), environment, courseData),
                    new FragmentItemModel.FragmentStateListener() {
                        @Override
                        public void onFragmentSelected() {
                            analyticsRegistry.trackScreenView(Analytics.Screens.COURSE_DATES,
                                    courseData.getCourse().getId(), null);
                            setDownloadProgressMenuItemVisibility(false);
                        }
                    }));
        }
        // Add additional resources tab
        items.add(new FragmentItemModel(ResourcesFragment.class,
                getResources().getString(R.string.resources_title),
                FontAwesomeIcons.fa_ellipsis_h,
                new FragmentItemModel.FragmentStateListener() {
                    @Override
                    public void onFragmentSelected() {
                        setDownloadProgressMenuItemVisibility(false);
                    }
                }));
        return items;
    }

    private void setDownloadProgressMenuItemVisibility(boolean isVisible) {
        if (updateDownloadProgressRunnable != null) {
            handler.removeCallbacks(updateDownloadProgressRunnable);
            if (isVisible) {
                handler.post(updateDownloadProgressRunnable);
            } else {
                if (downloadsMenuItem != null) {
                    downloadsMenuItem.setVisible(false);
                }
            }
        }
    }
}
