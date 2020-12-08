package org.edx.mobile.view;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.databinding.FragmentDashboardErrorLayoutBinding;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.custom.ProgressWheel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.widget.FrameLayout.LayoutParams;

public class CourseTabsDashboardFragment extends TabsBaseFragment {
    private static final String ARG_COURSE_NOT_FOUND = "ARG_COURSE_NOT_FOUND";
    protected final Logger logger = new Logger(getClass().getName());

    @Nullable
    private FragmentDashboardErrorLayoutBinding errorLayoutBinding;

    private EnrolledCoursesResponse courseData;

    @Inject
    private AnalyticsRegistry analyticsRegistry;

    @Inject
    private CourseAPI courseApi;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateDownloadProgressRunnable;
    private MenuItem downloadsMenuItem;

    @NonNull
    public static CourseTabsDashboardFragment newInstance(
            @Nullable EnrolledCoursesResponse courseData, @Nullable String courseId,
            @Nullable @ScreenDef String screenName) {
        final Bundle bundle = new Bundle();
        bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        bundle.putSerializable(Router.EXTRA_COURSE_ID, courseId);
        bundle.putSerializable(Router.EXTRA_SCREEN_NAME, screenName);
        return newInstance(bundle);
    }

    public static CourseTabsDashboardFragment newInstance(@NonNull Bundle bundle) {
        final CourseTabsDashboardFragment fragment = new CourseTabsDashboardFragment();
        fragment.setArguments(bundle);
        return fragment;
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
        courseData = (EnrolledCoursesResponse) getArguments().getSerializable(Router.EXTRA_COURSE_DATA);
        if (courseData != null) {
            // The case where we have valid course data
            getActivity().setTitle(courseData.getCourse().getName());
            setHasOptionsMenu(courseData.getCourse().getCoursewareAccess().hasAccess());
            environment.getAnalyticsRegistry().trackScreenView(
                    Analytics.Screens.COURSE_DASHBOARD, courseData.getCourse().getId(), null);

            if (!courseData.getCourse().getCoursewareAccess().hasAccess()) {
                final boolean auditAccessExpired = courseData.getAuditAccessExpires() != null &&
                        new Date().after(DateUtil.convertToDate(courseData.getAuditAccessExpires()));
                errorLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard_error_layout, container, false);
                errorLayoutBinding.errorMsg.setText(auditAccessExpired ? R.string.course_access_expired : R.string.course_not_started);
                return errorLayoutBinding.getRoot();
            } else {
                return super.onCreateView(inflater, container, savedInstanceState);
            }
        } else if (getArguments().getBoolean(ARG_COURSE_NOT_FOUND)) {
            // The case where we have invalid course data
            errorLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard_error_layout, container, false);
            errorLayoutBinding.errorMsg.setText(R.string.cannot_show_dashboard);
            return errorLayoutBinding.getRoot();
        } else {
            // The case where we need to fetch course's data based on its courseId
            fetchCourseById();
            final FrameLayout frameLayout = new FrameLayout(getActivity());
            frameLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            frameLayout.addView(inflater.inflate(R.layout.loading_indicator, container, false));
            return frameLayout;
        }
    }

    private void fetchCourseById() {
        final String courseId = getArguments().getString(Router.EXTRA_COURSE_ID);
        courseApi.getEnrolledCourses().enqueue(new CourseAPI.GetCourseByIdCallback(getActivity(), courseId) {
            @Override
            protected void onResponse(@NonNull final EnrolledCoursesResponse course) {
                if (getActivity() != null) {
                    getArguments().putSerializable(Router.EXTRA_COURSE_DATA, course);
                    UiUtil.restartFragment(CourseTabsDashboardFragment.this);
                }
            }

            @Override
            protected void onFailure(@NonNull final Throwable error) {
                if (getActivity() != null) {
                    getArguments().putBoolean(ARG_COURSE_NOT_FOUND, true);
                    UiUtil.restartFragment(CourseTabsDashboardFragment.this);
                    logger.error(new Exception("Invalid Course ID provided via deeplink: " + courseId), true);
                }
            }
        });
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
        if (updateDownloadProgressRunnable != null) {
            updateDownloadProgressRunnable.run();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (updateDownloadProgressRunnable != null) {
            handler.removeCallbacks(updateDownloadProgressRunnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateDownloadProgressRunnable != null) {
            handler.removeCallbacks(updateDownloadProgressRunnable);
            /* Assigning null here so that when this fragment is destroyed (e.g. due to orientation
             * change) the runnable is recreated and the download progress is updated properly.
             */
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
                        progressWheel.setProgressPercent(0);
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
        final Bundle arguments = getArguments();
        @ScreenDef String screenName = null;
        if (arguments != null) {
            screenName = arguments.getString(Router.EXTRA_SCREEN_NAME);
        }
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
                    getArguments(),
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
            items.add(new FragmentItemModel(CourseDatesPageFragment.class,
                    getResources().getString(R.string.course_dates_title), FontAwesomeIcons.fa_calendar,
                    CourseDatesPageFragment.makeArguments(courseData.getCourse().getId(), courseData.getMode()),
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
                ResourcesFragment.makeArguments(courseData, screenName),
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
