package org.edx.mobile.view;

import static android.widget.FrameLayout.LayoutParams;
import static org.edx.mobile.view.Router.EXTRA_COURSE_COMPONENT_ID;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.databinding.FragmentDashboardErrorLayoutBinding;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.util.images.CourseCardUtils;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.view.dialog.CourseModalDialogFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CourseTabsDashboardFragment extends TabsBaseFragment {
    private static final String ARG_COURSE_NOT_FOUND = "ARG_COURSE_NOT_FOUND";
    protected final Logger logger = new Logger(getClass().getName());

    @Nullable
    private FragmentDashboardErrorLayoutBinding errorLayoutBinding;

    private EnrolledCoursesResponse courseData;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @Inject
    CourseAPI courseApi;

    private View upgradeBtn;
    private AppCompatImageView expandedToolbarDismiss;
    private MaterialTextView expandedCourseTitle;

    private boolean isTitleCollapsed = false;
    private boolean isTitleExpanded = true;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        courseData = (EnrolledCoursesResponse) getArguments().getSerializable(Router.EXTRA_COURSE_DATA);
        if (courseData != null) {
            setupToolbar();
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

    public void setupToolbar() {
        AppBarLayout appbar = getActivity().findViewById(R.id.appbar);

        Toolbar collapsedToolbar = getActivity().findViewById(R.id.collapsed_toolbar_layout);
        MaterialTextView collapsedToolbarTitle = getActivity().findViewById(R.id.collapsed_toolbar_title);
        AppCompatImageView collapsedToolbarDismiss = getActivity().findViewById(R.id.collapsed_toolbar_dismiss);

        LinearLayout expandedToolbar = getActivity().findViewById(R.id.expanded_toolbar_layout);
        expandedToolbarDismiss = getActivity().findViewById(R.id.expanded_toolbar_dismiss);
        expandedCourseTitle = getActivity().findViewById(R.id.course_title);
        MaterialTextView courseOrg = getActivity().findViewById(R.id.course_organization);
        MaterialTextView courseExpiryDate = getActivity().findViewById(R.id.course_expiry_date);
        upgradeBtn = getActivity().findViewById(R.id.layout_upgrade_btn);
        MaterialButton upgradeBtnText = upgradeBtn.findViewById(R.id.btn_upgrade);

        collapsedToolbarTitle.setText(courseData.getCourse().getName());
        courseOrg.setText(courseData.getCourse().getOrg());
        expandedCourseTitle.setText(courseData.getCourse().getName());

        String expiryDate = CourseCardUtils.getFormattedDate(requireContext(), courseData);
        if (!TextUtils.isEmpty(expiryDate)) {
            courseExpiryDate.setVisibility(View.VISIBLE);
            courseExpiryDate.setText(expiryDate);
        }

        if (environment.getConfig().isCourseSharingEnabled()) {
            expandedCourseTitle.setMovementMethod(LinkMovementMethod.getInstance());
            SpannableString spannableString = org.edx.mobile.util.TextUtils.setIconifiedText(
                    requireContext(),
                    courseData.getCourse().getName(),
                    R.drawable.ic_share,
                    v -> ShareUtils.showCourseShareMenu(requireActivity(), expandedCourseTitle,
                            courseData, analyticsRegistry, environment)
            );
            expandedCourseTitle.setText(spannableString);
        }

        collapsedToolbarDismiss.setOnClickListener(v -> getActivity().finish());
        expandedToolbarDismiss.setOnClickListener(v -> getActivity().finish());

        if (courseData.isAuditMode() && environment.getAppFeaturesPrefs().isValuePropEnabled()) {
            upgradeBtn.setVisibility(View.VISIBLE);
            ((ShimmerFrameLayout) upgradeBtn).hideShimmer();
            upgradeBtnText.setOnClickListener(view1 -> CourseModalDialogFragment.newInstance(
                            Analytics.Screens.PLS_COURSE_DASHBOARD,
                            courseData.getCourseId(),
                            courseData.getCourseSku(),
                            courseData.getCourse().getName(),
                            courseData.getCourse().isSelfPaced())
                    .show(getChildFragmentManager(), CourseModalDialogFragment.TAG));
            upgradeBtnText.setText(R.string.value_prop_course_card_message);
        } else {
            upgradeBtn.setVisibility(View.GONE);
        }

        appbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int maxScroll = appBarLayout.getTotalScrollRange();
            float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
            handleToolbarVisibility(collapsedToolbar, expandedToolbar, percentage);
        });
        ViewAnimationUtil.startAlphaAnimation(collapsedToolbar, View.INVISIBLE);
    }

    private void fetchCourseById() {
        final String courseId = getArguments().getString(Router.EXTRA_COURSE_ID);
        courseApi.getEnrolledCourses().enqueue(new CourseAPI.GetCourseByIdCallback(getActivity(), courseId, null) {
            @Override
            protected void onResponse(@NonNull final EnrolledCoursesResponse course) {
                if (getActivity() != null) {
                    getArguments().putSerializable(Router.EXTRA_COURSE_DATA, course);
                    UiUtils.INSTANCE.restartFragment(CourseTabsDashboardFragment.this);
                }
            }

            @Override
            protected void onFailure(@NonNull final Throwable error) {
                if (getActivity() != null) {
                    getArguments().putBoolean(ARG_COURSE_NOT_FOUND, true);
                    UiUtils.INSTANCE.restartFragment(CourseTabsDashboardFragment.this);
                    logger.error(new Exception("Invalid Course ID provided via deeplink: " + courseId), true);
                }
            }
        });
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
                R.drawable.ic_class,
                CourseOutlineFragment.makeArguments(courseData, getArguments().getString(EXTRA_COURSE_COMPONENT_ID),
                        false, screenName),
                new FragmentItemModel.FragmentStateListener() {
                    @Override
                    public void onFragmentSelected() {
                        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.COURSE_OUTLINE,
                                courseData.getCourse().getId(), null);
                    }
                }));
        // Add videos tab
        if (environment.getConfig().isCourseVideosEnabled()) {
            items.add(new FragmentItemModel(CourseOutlineFragment.class,
                    getResources().getString(R.string.videos_title), R.drawable.ic_videocam
                    , CourseOutlineFragment.makeArguments(courseData, null, true, null),
                    new FragmentItemModel.FragmentStateListener() {
                        @Override
                        public void onFragmentSelected() {
                            environment.getAnalyticsRegistry().trackScreenView(
                                    Analytics.Screens.VIDEOS_COURSE_VIDEOS, courseData.getCourse().getId(), null);
                        }
                    }));
        }
        // Add discussion tab
        if (environment.getConfig().isDiscussionsEnabled() &&
                !TextUtils.isEmpty(courseData.getCourse().getDiscussionUrl())) {
            items.add(new FragmentItemModel(CourseDiscussionTopicsFragment.class,
                    getResources().getString(R.string.discussion_title), R.drawable.ic_forum,
                    getArguments(),
                    new FragmentItemModel.FragmentStateListener() {
                        @Override
                        public void onFragmentSelected() {
                            environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FORUM_VIEW_TOPICS,
                                    courseData.getCourse().getId(), null, null);
                        }
                    }));
        }
        // Add important dates tab
        if (environment.getConfig().isCourseDatesEnabled()) {
            items.add(new FragmentItemModel(CourseDatesPageFragment.class,
                    getResources().getString(R.string.course_dates_title), R.drawable.ic_event,
                    CourseDatesPageFragment.makeArguments(courseData), () -> {
                analyticsRegistry.trackScreenView(Analytics.Screens.COURSE_DATES,
                        courseData.getCourse().getId(), null);
            }));
        }
        // Add additional resources tab
        items.add(new FragmentItemModel(ResourcesFragment.class,
                getResources().getString(R.string.resources_title), R.drawable.ic_more_horiz,
                ResourcesFragment.makeArguments(courseData, screenName),
                new FragmentItemModel.FragmentStateListener() {
                    @Override
                    public void onFragmentSelected() {
                    }
                }));
        return items;
    }

    /**
     * It will handle the toolbar's collapse or expand state based on the scroll position. It will
     * also disable the clickable views of the toolbar because the alpha attribute has been used to
     * handle the toolbar's transition from one state to another.
     * <p>
     * Inspiration: http://www.devexchanges.info/2016/03/android-tip-custom-coordinatorlayout.html
     *
     * @param collapsedToolbar Parent view for Toolbar design in collapsed state
     * @param expandedToolbar  Parent view for Toolbar design in expanded state
     * @param percentage       Percentage of Toolbar's current scroll over max scroll
     */
    private void handleToolbarVisibility(View collapsedToolbar, View expandedToolbar,
                                         float percentage) {
        final float PERCENTAGE_TO_SHOW_COLLAPSED_TOOLBAR = 0.9f;
        final float PERCENTAGE_TO_HIDE_EXPANDED_TOOLBAR = 0.8f;

        if (percentage >= PERCENTAGE_TO_SHOW_COLLAPSED_TOOLBAR) {
            if (!isTitleCollapsed) {
                ViewAnimationUtil.startAlphaAnimation(collapsedToolbar, View.VISIBLE);
                isTitleCollapsed = true;
            }
        } else {
            if (isTitleCollapsed) {
                ViewAnimationUtil.startAlphaAnimation(collapsedToolbar, View.INVISIBLE);
                isTitleCollapsed = false;
            }
        }

        if (percentage >= PERCENTAGE_TO_HIDE_EXPANDED_TOOLBAR) {
            if (isTitleExpanded) {
                ViewAnimationUtil.startAlphaAnimation(expandedToolbar, View.INVISIBLE);
                isTitleExpanded = false;
            }
            expandedCourseTitle.setEnabled(false);
            expandedToolbarDismiss.setEnabled(false);
            upgradeBtn.setEnabled(false);
        } else {
            if (!isTitleExpanded) {
                ViewAnimationUtil.startAlphaAnimation(expandedToolbar, View.VISIBLE);
                isTitleExpanded = true;
            }
            expandedCourseTitle.setEnabled(true);
            expandedToolbarDismiss.setEnabled(true);
            upgradeBtn.setEnabled(true);
        }
    }
}
