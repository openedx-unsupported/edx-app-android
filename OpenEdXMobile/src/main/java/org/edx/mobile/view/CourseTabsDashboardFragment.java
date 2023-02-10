package org.edx.mobile.view;

import static org.edx.mobile.model.iap.IAPFlowData.IAPFlowType;
import static org.edx.mobile.view.Router.EXTRA_COURSE_COMPONENT_ID;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.android.billingclient.api.SkuDetails;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.databinding.FragmentCourseTabsDashboardBinding;
import org.edx.mobile.databinding.FragmentDashboardErrorLayoutBinding;
import org.edx.mobile.deeplink.DeepLinkManager;
import org.edx.mobile.deeplink.Screen;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.event.IAPFlowEvent;
import org.edx.mobile.event.MainDashboardRefreshEvent;
import org.edx.mobile.event.MoveToDiscoveryTabEvent;
import org.edx.mobile.exception.ErrorMessage;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.FragmentItemModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.EnrollmentMode;
import org.edx.mobile.model.iap.IAPFlowData;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.analytics.InAppPurchasesAnalytics;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.InAppPurchasesException;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.util.images.CourseCardUtils;
import org.edx.mobile.util.images.ShareUtils;
import org.edx.mobile.util.observer.EventObserver;
import org.edx.mobile.view.adapters.FragmentItemPagerAdapter;
import org.edx.mobile.view.custom.error.EdxCourseAccessErrorState;
import org.edx.mobile.view.custom.error.EdxErrorState;
import org.edx.mobile.view.dialog.CourseModalDialogFragment;
import org.edx.mobile.view.dialog.FullscreenLoaderDialogFragment;
import org.edx.mobile.viewModel.InAppPurchasesViewModel;
import org.edx.mobile.wrapper.InAppPurchasesDialog;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CourseTabsDashboardFragment extends BaseFragment {
    private static final String ARG_COURSE_NOT_FOUND = "ARG_COURSE_NOT_FOUND";
    protected final Logger logger = new Logger(getClass().getName());

    private EnrolledCoursesResponse courseData;

    @Inject
    protected IEdxEnvironment environment;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @Inject
    CourseAPI courseApi;

    @Inject
    InAppPurchasesAnalytics iapAnalytics;

    @Inject
    InAppPurchasesDialog iapDialog;

    private InAppPurchasesViewModel iapViewModel;

    private boolean isTitleCollapsed = false;
    private boolean isTitleExpanded = true;

    private List<FragmentItemModel> fragmentItemModels;

    @Nullable
    private FragmentCourseTabsDashboardBinding binding;

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

    public static CourseTabsDashboardFragment newInstance(Bundle bundle) {
        final CourseTabsDashboardFragment fragment = new CourseTabsDashboardFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        courseData = (EnrolledCoursesResponse) requireArguments().getSerializable(Router.EXTRA_COURSE_DATA);
        binding = FragmentCourseTabsDashboardBinding.inflate(inflater, container, false);

        if (courseData != null) {
            setHasOptionsMenu(courseData.getCourse().getCoursewareAccess().hasAccess());
            environment.getAnalyticsRegistry().trackScreenView(
                    Analytics.Screens.COURSE_DASHBOARD, courseData.getCourse().getId(), null);
            if (!courseData.getCourse().getCoursewareAccess().hasAccess()) {
                final boolean auditAccessExpired = new Date().after(DateUtil.convertToDate(courseData.getAuditAccessExpires()));
                if (auditAccessExpired) {
                    if (courseData.isUpgradeable() && environment.getAppFeaturesPrefs().isValuePropEnabled()) {
                        setupIAPLayout();
                    } else {
                        setupToolbar(true);
                        binding.accessError.setVisibility(View.VISIBLE);
                        binding.accessError.setState(EdxCourseAccessErrorState.State.AUDIT_ACCESS_EXPIRED);
                        binding.toolbar.tabs.setVisibility(View.GONE);
                        binding.accessError.setPrimaryButtonListener(onFindCourseClick());
                    }
                } else {
                    FragmentDashboardErrorLayoutBinding errorLayoutBinding = FragmentDashboardErrorLayoutBinding.inflate(inflater, container, false);
                    errorLayoutBinding.errorMsg.setText(R.string.course_not_started);
                    return errorLayoutBinding.getRoot();
                }
            } else {
                setupToolbar(false);
                setViewPager();
            }
            return binding.getRoot();
        } else if (getArguments().getBoolean(ARG_COURSE_NOT_FOUND)) {
            // The case where we have invalid course data
            binding.loadingError.getRoot().setVisibility(View.VISIBLE);
            binding.loadingError.dismiss.setVisibility(View.VISIBLE);
            binding.loadingError.dismiss.setOnClickListener(onCloseClick());
            binding.loadingError.state.setState(EdxErrorState.State.LOAD_ERROR, Screen.COURSE_DASHBOARD);
            binding.loadingError.state.setActionListener(onCloseClick());
            return binding.getRoot();
        } else {
            // The case where we need to fetch course's data based on its courseId
            fetchCourseById();
            final FrameLayout frameLayout = new FrameLayout(getActivity());
            frameLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            frameLayout.addView(inflater.inflate(R.layout.loading_indicator, container, false));
            return frameLayout;
        }
    }

    private void setupIAPLayout() {
        binding.accessError.setVisibility(View.VISIBLE);
        binding.accessError.setState(EdxCourseAccessErrorState.State.IS_UPGRADEABLE);
        binding.toolbar.tabs.setVisibility(View.GONE);
        setupToolbar(true);

        iapViewModel = new ViewModelProvider(this).get(InAppPurchasesViewModel.class);
        boolean isPurchaseEnabled = environment.getAppFeaturesPrefs().isIAPEnabled(environment.getLoginPrefs().isOddUserId());
        if (isPurchaseEnabled) {
            initIAPObservers();
            // Shimmer container taking sometime to get ready and perform the animation, so
            // by adding the some delay fixed that issue for lower-end devices, and for the
            // proper animation.
            binding.accessError.postDelayed(
                    () -> iapViewModel.initializeProductPrice(courseData.getCourseSku()), 1500);
        }
        binding.accessError.setSecondaryButtonListener(onFindCourseClick());
    }

    private void initIAPObservers() {
        iapViewModel.getProductPrice().observe(getViewLifecycleOwner(), new EventObserver<>(skuDetails -> {
            setUpUpgradeButton(skuDetails);
            return null;
        }));

        iapViewModel.getLaunchPurchaseFlow().observe(getViewLifecycleOwner(), new EventObserver<>(launchPurchaseFlow -> {
            iapViewModel.purchaseItem(requireActivity(), environment.getLoginPrefs().getUserId(),
                    courseData.getCourseSku());
            return null;
        }));

        iapViewModel.getShowLoader().observe(getViewLifecycleOwner(), new EventObserver<>(showLoader -> {
            binding.accessError.enableUpgradeButton(showLoader);
            return null;
        }));

        iapViewModel.getErrorMessage().observe(getViewLifecycleOwner(), new EventObserver<>(errorMessage -> {
            handleIAPException(errorMessage);
            return null;
        }));

        iapViewModel.getProductPurchased().observe(getViewLifecycleOwner(), new EventObserver<>(iapFlowData -> {
            showFullscreenLoader(iapFlowData);
            return null;
        }));
    }

    private void setUpUpgradeButton(SkuDetails skuDetails) {
        // The app get the sku details instantly, so add some wait to perform
        // animation at least one cycle.
        binding.accessError.setPrimaryButtonText(ResourceUtil.getFormattedString(
                getResources(),
                R.string.label_upgrade_course_button,
                AppConstants.PRICE,
                skuDetails.getPrice()
        ).toString());

        binding.accessError.setPrimaryButtonListener(view -> {
            iapAnalytics.trackIAPEvent(Analytics.Events.IAP_UPGRADE_NOW_CLICKED, "", "");
            iapViewModel.startPurchaseFlow(skuDetails.getSku());
        });
    }

    private void handleIAPException(final ErrorMessage errorMessage) {
        DialogInterface.OnClickListener retryListener = null;
        if (HttpStatus.NOT_ACCEPTABLE == ((InAppPurchasesException) errorMessage.getThrowable()).getHttpErrorCode()) {
            retryListener = (dialogInterface, i) -> {
                // already purchased course.
                iapViewModel.getIapFlowData().setVerificationPending(false);
                iapViewModel.getIapFlowData().setFlowType(IAPFlowType.SILENT);
                showFullscreenLoader(iapViewModel.getIapFlowData());
            };
        } else if (errorMessage.canRetry()) {
            retryListener = (dialogInterface, i) -> {
                if (errorMessage.getRequestType() == ErrorMessage.PRICE_CODE) {
                    iapViewModel.initializeProductPrice(courseData.getCourseSku());
                }
            };
        }
        iapDialog.handleIAPException(this, errorMessage, retryListener, null);
    }

    private void showFullscreenLoader(IAPFlowData iapFlowData) {
        // To proceed with the same instance of dialog fragment in case of orientation change
        FullscreenLoaderDialogFragment fullscreenLoader = FullscreenLoaderDialogFragment.getRetainedInstance(getChildFragmentManager());
        if (fullscreenLoader == null) {
            fullscreenLoader = FullscreenLoaderDialogFragment.newInstance(iapFlowData);
        }
        fullscreenLoader.show(getChildFragmentManager(), FullscreenLoaderDialogFragment.TAG);
    }

    private View.OnClickListener onCloseClick() {
        return v -> requireActivity().finish();
    }

    private View.OnClickListener onFindCourseClick() {
        return v -> {
            environment.getAnalyticsRegistry().trackUserFindsCourses();
            EventBus.getDefault().post(new MoveToDiscoveryTabEvent(Screen.DISCOVERY));
            requireActivity().finish();
        };
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handleTabSelection(requireArguments());
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Method to handle the tab-selection of the ViewPager based on screen name {@link Screen}
     * which may be sent through a deep link.
     *
     * @param bundle arguments
     */
    private void handleTabSelection(@Nullable Bundle bundle) {
        if (bundle != null && binding != null) {
            @ScreenDef String screenName = bundle.getString(Router.EXTRA_SCREEN_NAME);
            if (screenName != null && !bundle.getBoolean(Router.EXTRA_SCREEN_SELECTED, false)) {
                for (int i = 0; i < fragmentItemModels.size(); i++) {
                    final FragmentItemModel item = fragmentItemModels.get(i);
                    if (shouldSelectFragment(item, screenName)) {
                        binding.pager.setCurrentItem(i);
                        break;
                    }
                }
                // Setting `EXTRA_SCREEN_SELECTED` to true, so that upon recreation of the fragment the tab defined in
                // the deep link is not auto-selected again.
                bundle.putBoolean(Router.EXTRA_SCREEN_SELECTED, true);
            }
        }
    }

    /**
     * Determines if a tab fragment needs to be selected based on screen name.
     *
     * @param item       {@link FragmentItemModel} assigned to a tab.
     * @param screenName screen name param coming from {@link DeepLinkManager}
     * @return <code>true</code> if the specified tab fragment needs to be selected, <code>false</code> otherwise
     */
    private boolean shouldSelectFragment(@NonNull FragmentItemModel item, @NonNull @ScreenDef String screenName) {
        return (screenName.equals(Screen.COURSE_VIDEOS) && item.getTitle().equals(getString(R.string.videos_title))) ||
                (screenName.equals(Screen.COURSE_DISCUSSION) && item.getTitle().equals(getString(R.string.discussion_title))) ||
                (screenName.equals(Screen.DISCUSSION_POST) && item.getTitle().equals(getString(R.string.discussion_title))) ||
                (screenName.equals(Screen.DISCUSSION_TOPIC) && item.getTitle().equals(getString(R.string.discussion_title))) ||
                (screenName.equals(Screen.COURSE_DATES) && item.getTitle().equals(getString(R.string.label_dates))) ||
                (screenName.equals(Screen.COURSE_HANDOUT) && item.getTitle().equals(getString(R.string.handouts_title))) ||
                (screenName.equals(Screen.COURSE_ANNOUNCEMENT) && item.getTitle().equals(getString(R.string.announcement_title)));
    }

    public void setViewPager() {
        binding.pager.setVisibility(View.VISIBLE);
        UiUtils.INSTANCE.enforceSingleScrollDirection(binding.pager);
        fragmentItemModels = getFragmentItems();
        binding.toolbar.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.pager.setAdapter(new FragmentItemPagerAdapter(requireActivity(), fragmentItemModels));
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                final FragmentItemModel item = fragmentItemModels.get(position);
                if (item.getListener() != null) {
                    item.getListener().onFragmentSelected();
                }
            }
        });

        new TabLayoutMediator(binding.toolbar.tabs, binding.pager, (tab, position) -> {
            tab.setText(fragmentItemModels.get(position).getTitle());
        }).attach();

        if (fragmentItemModels.size() - 1 > 1) {
            binding.pager.setOffscreenPageLimit(fragmentItemModels.size() - 1);
        }
    }

    public void setupToolbar(boolean isErrorCase) {
        binding.toolbar.getRoot().setVisibility(View.VISIBLE);
        binding.toolbar.collapsedToolbarTitle.setText(courseData.getCourse().getName());
        binding.toolbar.courseOrganization.setText(courseData.getCourse().getOrg());
        binding.toolbar.courseTitle.setText(courseData.getCourse().getName());

        String expiryDate = CourseCardUtils.getFormattedDate(requireContext(), courseData);
        if (!TextUtils.isEmpty(expiryDate)) {
            binding.toolbar.courseExpiryDate.setVisibility(View.VISIBLE);
            binding.toolbar.courseExpiryDate.setText(expiryDate);
        }

        if (environment.getConfig().isCourseSharingEnabled()) {
            binding.toolbar.courseTitle.setMovementMethod(LinkMovementMethod.getInstance());
            SpannableString spannableString = org.edx.mobile.util.TextUtils.setIconifiedText(
                    requireContext(),
                    courseData.getCourse().getName(),
                    R.drawable.ic_share,
                    v -> ShareUtils.showCourseShareMenu(requireActivity(), binding.toolbar.courseTitle,
                            courseData, analyticsRegistry, environment)
            );
            binding.toolbar.courseTitle.setText(spannableString);
        }

        binding.toolbar.collapsedToolbarDismiss.setOnClickListener(v -> requireActivity().finish());
        binding.toolbar.expandedToolbarDismiss.setOnClickListener(v -> requireActivity().finish());

        if (!isErrorCase && courseData.isUpgradeable() && environment.getAppFeaturesPrefs().isValuePropEnabled()) {
            binding.toolbar.layoutUpgradeBtn.getRoot().setVisibility(View.VISIBLE);
            ((ShimmerFrameLayout) binding.toolbar.layoutUpgradeBtn.getRoot()).hideShimmer();
            binding.toolbar.layoutUpgradeBtn.btnUpgrade.setOnClickListener(view1 -> CourseModalDialogFragment.newInstance(
                            Analytics.Screens.PLS_COURSE_DASHBOARD,
                            courseData.getCourseId(),
                            courseData.getCourseSku(),
                            courseData.getCourse().getName(),
                            courseData.getCourse().isSelfPaced())
                    .show(getChildFragmentManager(), CourseModalDialogFragment.TAG));
            binding.toolbar.layoutUpgradeBtn.btnUpgrade.setText(R.string.value_prop_course_card_message);
        } else {
            binding.toolbar.layoutUpgradeBtn.getRoot().setVisibility(View.GONE);
        }

        binding.toolbar.appbar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int maxScroll = appBarLayout.getTotalScrollRange();
            float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
            handleToolbarVisibility(binding.toolbar.collapsedToolbarLayout, binding.toolbar.expandedToolbarLayout, percentage);
        });
        ViewAnimationUtil.startAlphaAnimation(binding.toolbar.collapsedToolbarLayout, View.INVISIBLE);
    }

    private void fetchCourseById() {
        final String courseId = getArguments().getString(Router.EXTRA_COURSE_ID);
        courseApi.getEnrolledCourses().enqueue(new CourseAPI.GetCourseByIdCallback(getActivity(), courseId, null) {
            @Override
            protected void onResponse(@NonNull final EnrolledCoursesResponse course) {
                if (getActivity() != null) {
                    getArguments().putSerializable(Router.EXTRA_COURSE_DATA, course);
                    FullscreenLoaderDialogFragment fullscreenLoader = FullscreenLoaderDialogFragment
                            .getRetainedInstance(getChildFragmentManager());
                    if (fullscreenLoader != null && fullscreenLoader.isResumed()) {
                        new SnackbarErrorNotification(binding.getRoot()).showError(R.string.purchase_success_message);
                        // Add close listener cuz on fullscreenLoader released by the parent fragment
                        // while restarting and app leads to crash.
                        fullscreenLoader.closeLoader(() -> getActivity().runOnUiThread(() ->
                                UiUtils.INSTANCE.restartFragment(CourseTabsDashboardFragment.this))
                        );
                    }
                }
            }

            @Override
            protected void onFailure(@NonNull final Throwable error) {
                if (getActivity() != null) {
                    FullscreenLoaderDialogFragment fullscreenLoader = FullscreenLoaderDialogFragment
                            .getRetainedInstance(getChildFragmentManager());
                    if (fullscreenLoader != null && fullscreenLoader.isResumed()) {
                        iapViewModel.dispatchError(ErrorMessage.COURSE_REFRESH_CODE, null, error);
                    } else {
                        getArguments().putBoolean(ARG_COURSE_NOT_FOUND, true);
                        UiUtils.INSTANCE.restartFragment(CourseTabsDashboardFragment.this);
                        logger.error(new Exception("Invalid Course ID provided via deeplink: " + courseId), true);
                    }
                }
            }
        });
    }

    private List<FragmentItemModel> getFragmentItems() {
        final Bundle arguments = getArguments();
        @ScreenDef String screenName = null;
        if (arguments != null) {
            screenName = arguments.getString(Router.EXTRA_SCREEN_NAME);
        }
        ArrayList<FragmentItemModel> items = new ArrayList<>();
        // Add course outline tab
        items.add(new FragmentItemModel(CourseOutlineFragment.class,
                getResources().getString(R.string.label_home),
                CourseOutlineFragment.makeArguments(courseData, getArguments().getString(EXTRA_COURSE_COMPONENT_ID),
                        false, screenName), () ->
                environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.COURSE_OUTLINE,
                        courseData.getCourse().getId(), null)
        ));
        // Add videos tab
        if (environment.getConfig().isCourseVideosEnabled()) {
            items.add(new FragmentItemModel(CourseOutlineFragment.class,
                    getResources().getString(R.string.videos_title)
                    , CourseOutlineFragment.makeArguments(courseData, null, true, null),
                    () -> environment.getAnalyticsRegistry().trackScreenView(
                            Analytics.Screens.VIDEOS_COURSE_VIDEOS, courseData.getCourse().getId(), null)
            ));
        }
        // Add discussion tab
        if (environment.getConfig().isDiscussionsEnabled() &&
                !TextUtils.isEmpty(courseData.getCourse().getDiscussionUrl())) {
            items.add(new FragmentItemModel(CourseDiscussionTopicsFragment.class,
                    getResources().getString(R.string.discussion_title),
                    getArguments(), () -> environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FORUM_VIEW_TOPICS,
                    courseData.getCourse().getId(), null, null)
            ));
        }
        // Add important dates tab
        if (environment.getConfig().isCourseDatesEnabled()) {
            items.add(new FragmentItemModel(CourseDatesPageFragment.class,
                    getResources().getString(R.string.label_dates),
                    CourseDatesPageFragment.makeArguments(courseData), () -> analyticsRegistry.trackScreenView(Analytics.Screens.COURSE_DATES,
                    courseData.getCourse().getId(), null)
            ));
        }
        // Add handouts tab
        items.add(new FragmentItemModel(CourseHandoutFragment.class,
                getResources().getString(R.string.handouts_title),
                CourseHandoutFragment.makeArguments(courseData), () -> analyticsRegistry.trackScreenView(Analytics.Screens.COURSE_HANDOUTS,
                courseData.getCourse().getId(), null)
        ));
        // Add announcements tab
        items.add(new FragmentItemModel(CourseAnnouncementsFragment.class,
                getResources().getString(R.string.announcement_title),
                CourseAnnouncementsFragment.makeArguments(courseData), () -> analyticsRegistry.trackScreenView(Analytics.Screens.COURSE_ANNOUNCEMENTS,
                courseData.getCourse().getId(), null)
        ));
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
        if (binding == null) return;

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
            binding.toolbar.courseTitle.setEnabled(false);
            binding.toolbar.expandedToolbarDismiss.setEnabled(false);
            binding.toolbar.layoutUpgradeBtn.getRoot().setEnabled(false);
        } else {
            if (!isTitleExpanded) {
                ViewAnimationUtil.startAlphaAnimation(expandedToolbar, View.VISIBLE);
                isTitleExpanded = true;
            }
            binding.toolbar.courseTitle.setEnabled(true);
            binding.toolbar.expandedToolbarDismiss.setEnabled(true);
            binding.toolbar.layoutUpgradeBtn.getRoot().setEnabled(true);
        }
    }

    @Subscribe
    public void onEventMainThread(IAPFlowEvent event) {
        if (!this.isResumed()) {
            return;
        }
        if (event.getFlowAction() == IAPFlowData.IAPAction.PURCHASE_FLOW_COMPLETE) {
            courseData.setMode(EnrollmentMode.VERIFIED.toString());
            getArguments().putString(Router.EXTRA_COURSE_ID, courseData.getCourseId());
            fetchCourseById();
            EventBus.getDefault().post(new MainDashboardRefreshEvent());
        }
    }
}
